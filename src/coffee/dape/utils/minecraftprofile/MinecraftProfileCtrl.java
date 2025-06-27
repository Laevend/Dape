package coffee.dape.utils.minecraftprofile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.gson.JsonObject;

import coffee.dape.Dape;
import coffee.dape.utils.FUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.MojangUtils;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.minecraftprofile.data.MinecraftProfile;

/**
 * 
 * @author Laeven
 * Utility controller for management of Minecraft player profile data. 
 * Caches player data to minimise requests to Mojang Public API.
 * Provides functions for also determining if a player name or UUID is real regardless if they joined the server or not
 */
public class MinecraftProfileCtrl implements Listener
{
	// TODO Implement logic to handle mismatched names and UUID's when a player changes their name
	private static Map<UUID,MinecraftProfile> profiles = new HashMap<>();
	private static Map<String,UUID> uuidLookupTable = new HashMap<>();
	
	//private static long MILI_30_DAYS = 2592000000L;
	
	private static final String MC_PROFILE_CACHE_DIR = "mc_profile_cache";
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		// Cache users name while they're on the server.
		// It's cheaper than calling the MojangAPI which is limited to 600 requests per 10 minutes.
		MinecraftProfileCtrl.getUUID(e.getPlayer().getName());
	}
	
	/**
	 * Gets a players profile.
	 * 
	 * <p>If players profile has already been requested and cached before,
	 * the cache will be returned.
	 * 
	 * <p>If the players profile doesn't already exist locally, a new request
	 * to the Mojang API will be made to fetch their profile and cache it.
	 * 
	 * <p>Mojang API can only be requested 600 times within 10 minutes.
	 * 
	 * @param uuid UUID of player
	 * @param forceAPICall forces a new call to the MojangAPI regardless if this players profile is cached
	 * @return MinecraftProfile
	 */
	public static MinecraftProfile getPlayerProfile(UUID uuid,boolean forceAPICall)
	{
		if(forceAPICall)
		{
			Logg.verb("Forcing API Call..",Logg.VerbGroup.MINECRAFT_PROFILE);
			return getNewMinecraftProfile(uuid);
		}
		
		if(profiles.containsKey(uuid))
		{
			Logg.verb("Profile found in memory cache",Logg.VerbGroup.MINECRAFT_PROFILE);
			return profiles.get(uuid);
		}
		
		Path minecraftProfileDir = Dape.internalFilePath(MC_PROFILE_CACHE_DIR);
		FUtils.createDirectories(minecraftProfileDir);
		
		if(minecraftProfileDir.toFile().listFiles().length == 0)
		{
			Logg.verb("No file cache found!",Logg.VerbGroup.MINECRAFT_PROFILE);
			return getNewMinecraftProfile(uuid);
		}
		
		Path filePath = Dape.internalFilePath(MC_PROFILE_CACHE_DIR + File.separator + uuid + ".json");
		
		if(Files.exists(filePath))
		{
			Logg.verb("Profile found in file cache",Logg.VerbGroup.MINECRAFT_PROFILE);
			MinecraftProfile profile = new MinecraftProfileReader().fromJson(filePath.toFile());
			return profile;
		}
		
		Logg.verb("No memory or file cache found!",Logg.VerbGroup.MINECRAFT_PROFILE);
		return getNewMinecraftProfile(uuid);
	}
	
	/**
	 * Gets a new MinecraftProfile and caches it
	 * @param uuid UUID of player
	 * @param withSignature If also requested the signature of the player to be returned in the API call
	 * @return MinecraftProfile
	 */
	private static MinecraftProfile getNewMinecraftProfile(UUID uuid)
	{
		if(!MojangUtils.isApiOnline()) { Logg.fatal("Cannot request new minecraft profile as Mojang Public API is down!"); return null; }
		
		JsonObject data = MojangUtils.requestProfileFromAPI(uuid,true);
		
		if(data == null) { return null; }
		
		MinecraftProfile profile = new MinecraftProfileReader().fromJson("MojangAPI",data);
		profiles.put(uuid,profile);
		save(uuid);
		return profiles.get(uuid);
	}
	
	public static void save(UUID uuid)
	{
		if(!profiles.containsKey(uuid)) { return; }
		new MinecraftProfileWriter().toJson(profiles.get(uuid),Dape.internalFilePath(MC_PROFILE_CACHE_DIR + File.separator + uuid + ".json"));
	}
	
	public static UUID getUUID(String playerName)
	{
		if(uuidLookupTable.containsKey(playerName))
		{
			Logg.verb("UUID found in memory cache",Logg.VerbGroup.MINECRAFT_PROFILE);
			return uuidLookupTable.get(playerName);
		}
		
		if(PlayerUtils.isOnline(playerName))
		{
			Logg.verb("UUID found from online player",Logg.VerbGroup.MINECRAFT_PROFILE);
			uuidLookupTable.put(playerName,PlayerUtils.getPlayer(playerName).getUniqueId());
			return uuidLookupTable.get(playerName);
		}
		
		Logg.verb("No uuid cache found, requesting api...",Logg.VerbGroup.MINECRAFT_PROFILE);
		UUID uuid = getNewUUIDLookup(playerName);
		
		if(uuid == null) { Logg.error("MojangAPI returned null to UUID request"); return null; }
		
		return uuid;
	}
	
	private static UUID getNewUUIDLookup(String playerName)
	{
		if(!MojangUtils.isApiOnline()) { Logg.fatal("Cannot request UUID lookup as Mojang Public API is down!"); return null; }
		
		JsonObject data = MojangUtils.requestUUIDFromAPI(playerName);
		if(data == null) { Logg.error("API returned 404!"); return null; }
		
		// Normal response
		if(data.has("name") && data.has("id"))
		{
			if(!data.get("name").getAsString().equals(playerName)) { Logg.error("JSON attribute 'name': " + data.get("name").getAsString() + " does not match playername " + playerName); return null; }
			String hexStringWithInsertedHyphens =  data.get("id").getAsString().replaceFirst( "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5" );
			return UUID.fromString(hexStringWithInsertedHyphens);
		}
		
		// Error response
		if(data.has("errorMessage"))
		{
			Logg.error("API returned an error!");
			Logg.error("API Response: " + data.get("errorMessage").getAsString());
			return null;
		}
		
		Logg.error("Missing JSON attributes?" + data.toString());
		return null;
	}
	
	/**
	 * Checks if a playerUUID is a valid player UUID (regardless if they've visited the server or not)
	 * @param playerUUID UUID of a java edition Minecraft player
	 * @return true if this is a real java edition Minecraft player UUID, false otherwise
	 */
	public static boolean isRealPlayerUUID(UUID playerUUID)
	{
		// Check cached profiles
		if(profiles.containsKey(playerUUID))
		{
			return true;
		}
		
		// Check if player is online
		if(PlayerUtils.isOnline(playerUUID))
		{
			Player p = Bukkit.getPlayer(playerUUID);
			
			// If player name is not already in lookup table, cache it
			// It's more expensive to check for a players UUID via name if no lookup exists
			if(!uuidLookupTable.containsKey(p.getName()))
			{
				uuidLookupTable.put(p.getName(),playerUUID);
			}
			
			// Cache profile while we're here for quicker lookups for the same player
			if(MojangUtils.isApiOnline())
			{
				MinecraftProfile mp = MinecraftProfileCtrl.getPlayerProfile(playerUUID,false);
				profiles.put(playerUUID,mp);
			}
			
			return true;
		}
		
		if(!MojangUtils.isApiOnline()) { Logg.fatal("Cannot confirm if " + playerUUID.toString() + " is a real player UUID as Mojang Public API is down!"); return false; }
		
		// Perform API lookup
		MinecraftProfile mp = MinecraftProfileCtrl.getPlayerProfile(playerUUID,false);
		
		if(mp != null)
		{
			return mp.getIdAsUUID().equals(playerUUID);
		}
		
		// MincraftProfile is null meaning API returned 404
		return false;
	}
	
	/**
	 * Checks if a playerUUID is a valid player UUID (regardless if they've visited the server or not)
	 * @param playerName Name of Java player (case and character sensitive)
	 * @return true if this is a real java edition Minecraft player name, false otherwise
	 */
	public static boolean isRealPlayerName(String playerName)
	{
		// Check cached lookup map
		if(uuidLookupTable.containsKey(playerName))
		{
			return true;
		}
		
		// Check if player is online
		if(PlayerUtils.isOnline(playerName))
		{
			uuidLookupTable.put(playerName,PlayerUtils.getPlayer(playerName).getUniqueId());
			return true;
		}
		
		if(!MojangUtils.isApiOnline()) { Logg.fatal("Cannot confirm if " + playerName + " is a real player name as Mojang Public API is down!"); return false; }
		
		// Perform API lookup
		UUID uuid = getNewUUIDLookup(playerName);
		
		if(uuid != null) { return true; }
		
		return false;
	}
}
