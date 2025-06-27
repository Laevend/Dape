package coffee.dape.playerdata.data;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coffee.dape.Dape;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.playerdata.PlayerDataCtrl;
import coffee.dape.playerdata.clocks.PlayerDataBackupClock;
import coffee.dape.playerdata.clocks.PlayerDataDegradedClock;
import coffee.dape.playerdata.clocks.PlayerDataSaveClock;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.json.JUtils;
import coffee.dape.utils.json.PersistJson;
import coffee.dape.utils.structs.Namespace;
import coffee.dape.utils.tools.Deserialise;

/**
 * 
 * @author Laeven
 *
 */
public class PlayerData implements PersistJson
{
	public static final DateFormat SaveFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	public static final DateFormat DisplayFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
	private PlayerDataBackupClock backupClock = null;
	private PlayerDataSaveClock saveClock = null;
	private PlayerDataDegradedClock degradeClock = null;
	private static final String PLAYER_NOT_FOUND = "Player_Not_Found";
	
	private boolean degradedState = false;
	private boolean silenceDegradedAlarm = false;
	
	private UUID owner = null;
	private Date joinDate = null;
	private Date lastJoin = null;
	private Set<String> ipAddresses = null;
	private Set<String> usernames = null;
	private String mostRecentUsername = null;
	
	private PlayerInventory inv = null;
	private PlayerEnderchest echest = null;
	private PlayerEntityData entityData = null;
	private PlayerPostBox postData = null;
	
	private long playTime = 0L;
	private Set<UUID> ignoredPlayers = null;
	
	private Map<Namespace,PlayerDataExtension> extensionData = null;
	
	private JsonObject cachedSerialisedData = null;
	
	/**
	 * Create a new PlayerData
	 * @param p Player
	 */
	public PlayerData(Player p)
	{
		this(p.getUniqueId());
	}
	
	/**
	 * Create a new PlayerData
	 * @param uuid Player UUID
	 */
	public PlayerData(UUID uuid)
	{
		this.owner = uuid;
		this.ipAddresses = new HashSet<>();
		this.usernames = new HashSet<>();
		this.ignoredPlayers = new HashSet<>();
		
		this.joinDate = new Date();
		this.lastJoin = new Date();
		
		this.mostRecentUsername = PlayerUtils.getName(this.owner);
		if(this.mostRecentUsername == null)
		{
			this.mostRecentUsername = PLAYER_NOT_FOUND;
		}
		
		if(this.mostRecentUsername != PLAYER_NOT_FOUND)
		{
			this.usernames.add(this.mostRecentUsername);
		}
		
		if(PlayerUtils.isOnline(this.owner))
		{
			this.ipAddresses.add(Bukkit.getPlayer(this.owner).getAddress().getHostString());
		}
	}
	
	/**
	 * Gets extension data
	 * @param extensionKey Key used to access this record
	 * @return PlayerDataExtension relating to this key, or null if it doesn't exist
	 */
	public PlayerDataExtension getExtensionData(Namespace extensionKey)
	{
		if(this.extensionData == null) { return null; }
		if(!this.extensionData.containsKey(extensionKey)) { return null; }
		return this.extensionData.get(extensionKey);
	}
	
	/**
	 * Sets extension data
	 * @param extensionKey Key used to access this record
	 * @param extensionData Sub category data to store
	 */
	public void setExtensionData(Namespace extensionKey,PlayerDataExtension extensionData)
	{
		if(this.extensionData == null) { this.extensionData = new HashMap<>(); }
		this.extensionData.put(extensionKey,extensionData);
	}
	
	/**
	 * Removes extension data
	 * @param extensionKey Key used to access this record
	 */
	public void removeExtensionData(Namespace extensionKey)
	{
		if(this.extensionData == null) { return; }
		this.extensionData.remove(extensionKey);
	}
	
	/**
	 * Indicates this player data file is a temporary fix.
	 * This data file was a auto-restore from a backup when their main data file 
	 * could not be read correctly!
	 * @return
	 */
	public boolean isInDegradedState()
	{
		return degradedState;
	}

	public void setDegradedState(boolean tempData)
	{
		this.degradedState = tempData;
	}
	
	/**
	 * Indicates that this player has silenced the degraded alarm for themselves
	 * @return If the player has silenced the degraded alarm
	 */
	public boolean isSilenceDegradedAlarm()
	{
		return silenceDegradedAlarm;
	}

	public void setSilenceDegradedAlarm(boolean silenceDegradedAlarm)
	{
		this.silenceDegradedAlarm = silenceDegradedAlarm;
	}

	/**
	 * Owner of account
	 * @return
	 */
	public UUID getOwner()
	{
		return owner;
	}
	
	public Date getJoinDate()
	{
		return joinDate;
	}
	
	public void setJoinDate(Date joinDate)
	{
		this.joinDate = joinDate;
	}
	
	public Date getLastJoin()
	{
		return lastJoin;
	}
	
	public void setLastJoin(Date lastJoin)
	{
		this.lastJoin = lastJoin;
	}
	
	public Set<String> getIpAddresses()
	{
		return ipAddresses;
	}
	
	public void setIpAddresses(Set<String> ipAddresses)
	{
		this.ipAddresses = ipAddresses;
	}
	
	public void addIpAddress(String hostName)
	{
		this.ipAddresses.add(hostName);
	}
	
	public Set<String> getUsernames()
	{
		return usernames;
	}
	
	public void setUsernames(Set<String> usernames)
	{
		this.usernames = usernames;
	}
	
	public void addUsername(String name)
	{
		this.usernames.add(name);
	}
	
	public String getMostRecentUsername()
	{
		return mostRecentUsername;
	}
	
	public void setMostRecentUsername(String mostRecentUsername)
	{
		this.mostRecentUsername = mostRecentUsername;
	}
	
	public PlayerInventory getInventory()
	{
		if(this.inv == null) { this.inv = new PlayerInventory(this); }		
		return inv;
	}
	
	public void setInventory(PlayerInventory inv)
	{
		this.inv = inv;
	}
	
	public PlayerEnderchest getEnderchest()
	{
		if(this.echest == null) { this.echest = new PlayerEnderchest(this); }		
		return echest;
	}
	
	public void setEnderchest(PlayerEnderchest echest)
	{
		this.echest = echest;
	}
	
	public PlayerEntityData getEntityData()
	{
		if(this.entityData == null) { this.entityData = new PlayerEntityData(this); }		
		return entityData;
	}
	
	public void setEntityData(PlayerEntityData entityData)
	{
		this.entityData = entityData;
	}
	
	public PlayerPostBox getPostData()
	{
		if(this.postData == null) { this.postData = new PlayerPostBox(this); }		
		return postData;
	}
	
	public void setPosteData(PlayerPostBox postData)
	{
		this.postData = postData;
	}
	
	public long getPlayTime()
	{
		return playTime;
	}
	
	public void addPlayTime(long playTime)
	{
		this.playTime += playTime;
	}
	
	public void setPlayTime(long playTime)
	{
		this.playTime = playTime;
	}
	
	public String getPlayTimeFormatted()
	{
		long hours = TimeUnit.MILLISECONDS.toHours(this.playTime);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(this.playTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(this.playTime));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(this.playTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this.playTime));
		
		String formatted = String.format("%d %s %d %s %d %s",hours,hours == 1 ? "hr" : "hrs",minutes,minutes == 1 ? "min" : "mins",seconds,seconds == 1 ? "sec" : "secs");
		return formatted;
	}
	
	public Set<UUID> getIgnoredPlayers()
	{
		return ignoredPlayers;
	}
	
	public void addIgnoredPlayer(UUID player)
	{
		ignoredPlayers.add(player);
	}
	
	public void removeIgnoredPlayer(UUID player)
	{
		ignoredPlayers.remove(player);
	}
	
	public void setIgnoredPlayers(Set<UUID> ignoredPlayers)
	{
		this.ignoredPlayers = ignoredPlayers;
	}

	public void printDataToConsole()
	{
		try
		{
			String json = JUtils.toJsonString(serialise(),true);
			Logg.info("Player Data Json > " + PlayerUtils.getName(owner));
			Logg.info("&e" + json);
			return;
		}
		catch(SerialiseException e)
		{
			Logg.error("Could not serialise PlayerData " + owner.toString() + " (" + PlayerUtils.getName(owner) + ")",e);
		}
	}

	/**
	 * Get the file path of where a players data file is stored.
	 * @return Path of a players data file.
	 */
	public Path getPlayerDataPath()
	{
		return Dape.internalFilePath("playerdata" + File.separator + getOwner().toString() + File.separator + "playerdata.json");
	}
	
	/**
	 * Get the file path of where backups of a players data backups are stored.
	 * @return Path of a players data backups
	 */
	public Path getPlayerDataBackupDirectory()
	{
		return Dape.internalFilePath("playerdata" + File.separator + getOwner().toString() + File.separator + "backups");
	}
	
	/**
	 * Get the file path of where backups of corrupt player data files are stored.
	 * @return Path of a corrupt player data files
	 */
	public Path getPlayerDataCorruptDataDirectory()
	{
		return Dape.internalFilePath("playerdata" + File.separator + getOwner().toString() + File.separator + "corrupted");
	}
	
	public void scheduleSave()
	{
		// Check if clock is null. If it is, create a new clock
		if(this.saveClock == null) { this.saveClock = new PlayerDataSaveClock(this); this.saveClock.start(); return; }
		
		// Check if clock is not enabled because it has already ran
		if(!this.saveClock.isEnabled())
		{
			this.saveClock.start();
			return;
		}
		
		// If the clock is already running, delay the save
		this.saveClock.delay();
	}
	
	public void stopSaveClock()
	{
		// Clock is null so there is no clock to stop
		if(this.saveClock == null) { return; }
		
		// Clock is not null but has already stopped
		if(!this.saveClock.isEnabled()) { return; }
		
		this.saveClock.stop();
	}
	
	public void stopSaveClockNow()
	{
		// Clock is null so there is no clock to stop
		if(this.saveClock == null) { return; }
		
		// Clock is not null but has already stopped
		if(!this.saveClock.isEnabled()) { return; }
		
		this.saveClock.kill();
	}
	
	public void startBackupClock()
	{
		// Check if clock is null. If it is, create a new clock
		if(this.backupClock == null) { this.backupClock = new PlayerDataBackupClock(this); this.backupClock.start(); return; }
		
		// Check if clock is not enabled because it has already ran
		if(!this.backupClock.isEnabled())
		{
			this.backupClock.start();
			return;
		}
		
		Logg.warn("Cannot start backup clock for " + getMostRecentUsername() + ". It's already enabled!");
	}
	
	public void stopBackupClock()
	{
		// Clock is null so there is no clock to stop
		if(this.backupClock == null) { return; }
		
		// Clock is not null but has already stopped
		if(!this.backupClock.isEnabled()) { return; }
		
		this.backupClock.stop();
	}
	
	public void stopBackupClockNow()
	{
		// Clock is null so there is no clock to stop
		if(this.backupClock == null) { return; }
		
		// Clock is not null but has already stopped
		if(!this.backupClock.isEnabled()) { return; }
		
		this.backupClock.kill();
	}
	
	public void startDegradedClock()
	{
		if(!this.degradedState) { return; }
		
		// Check if clock is null. If it is, create a new clock
		if(this.degradeClock == null) { this.degradeClock = new PlayerDataDegradedClock(this); this.degradeClock.start(); return; }
		
		// Check if clock is not enabled because it has already ran
		if(!this.degradeClock.isEnabled())
		{
			this.degradeClock.start();
			return;
		}
		
		Logg.warn("Cannot start degraded clock for " + getMostRecentUsername() + ". It's already enabled!");
	}
	
	public void stopDegradedClock()
	{
		if(!this.degradedState) { return; }
		
		// Clock is null so there is no clock to stop
		if(this.degradeClock == null) { return; }
		
		// Clock is not null but has already stopped
		if(!this.degradeClock.isEnabled()) { return; }
		
		this.degradeClock.stop();
	}
	
	public void stopDegradedClockNow()
	{
		if(!this.degradedState) { return; }
		
		// Clock is null so there is no clock to stop
		if(this.degradeClock == null) { return; }
		
		// Clock is not null but has already stopped
		if(!this.degradeClock.isEnabled()) { return; }
		
		this.degradeClock.kill();
	}
	
	/**
	 * Gets the serialised representation of this instance of PlayerData the last time it was serialised
	 * <p>
	 * Depending on how recently {@link #serialise()} was called this data may up very new or old
	 * @return JsonObject of serialised player data
	 */
	public JsonObject getMostRecentSerialisedData()
	{
		return cachedSerialisedData;
	}
	
	// A flag to indicate that this data was auto-restored from backup as the players main data file was corrupted and the server attempted a temp fix
	public static final String IS_IN_DEGRADED_STATE = "is_in_degraded_state";
	
	public static final String OWNER = "owner";
	public static final String MOST_RECENT_USERNAME = "most_recent_username";
	public static final String JOIN_DATE = "join_date";
	public static final String LAST_JOIN = "last_join";
	public static final String USERNAMES = "usernames";
	public static final String IP_ADDRESSES = "ip_addresses";
	public static final String PLAY_TIME = "play_time";
	public static final String IGNORED_PLAYERS = "ignored_players";
	public static final String PLAYER_INVENTORY = "player_inventory";
	public static final String PLAYER_ENDERCHEST = "player_enderchest";
	public static final String PLAYER_ENTITY = "player_entity";
	public static final String PLAYER_POST = "player_post";
	public static final String EXTENSION_DATA = "extension_data";

	@Override
	public JsonObject serialise() throws SerialiseException
	{
		JsonObject obj = new JsonObject();
		
		obj.addProperty(IS_IN_DEGRADED_STATE,degradedState);
		obj.addProperty(OWNER,owner.toString());
		obj.addProperty(MOST_RECENT_USERNAME,mostRecentUsername);
		obj.addProperty(JOIN_DATE,PlayerData.SaveFormat.format(joinDate));
		obj.addProperty(LAST_JOIN,PlayerData.SaveFormat.format(lastJoin));
		
		JsonArray usernameArray = new JsonArray();
		for(String username : usernames)
		{
			usernameArray.add(username);
		}
		
		obj.add(USERNAMES,usernameArray);
		
		JsonArray ipArray = new JsonArray();
		for(String ip : ipAddresses)
		{
			ipArray.add(ip);
		}
		
		obj.add(IP_ADDRESSES,ipArray);
		obj.addProperty(PLAY_TIME,playTime);
		
		JsonArray ignoredPlayersArray = new JsonArray();
		for(UUID playerUUID : ignoredPlayers)
		{
			ignoredPlayersArray.add(playerUUID.toString());
		}
		
		obj.add(IGNORED_PLAYERS,ignoredPlayersArray);
		obj.add(PLAYER_INVENTORY,getInventory().serialise());
		obj.add(PLAYER_ENDERCHEST,getEnderchest().serialise());
		obj.add(PLAYER_ENTITY,getEntityData().serialise());
		obj.add(PLAYER_POST,getPostData().serialise());
		
		JsonObject extensionDataObj = new JsonObject();
		
		if(extensionData != null)
		{
			for(Entry<Namespace,PlayerDataExtension> extensionEntry : extensionData.entrySet())
			{
				extensionDataObj.add(extensionEntry.getKey().toSimpleString(),extensionEntry.getValue().serialise());
			}
		}
		
		obj.add(EXTENSION_DATA,extensionDataObj);
		
		cachedSerialisedData = obj;
		return obj;
	}

	@Override
	public void deserialise(JsonObject obj) throws DeserialiseException
	{
		Deserialise.assertProperty(IS_IN_DEGRADED_STATE,Deserialise.Type.BOOLEAN,obj);
		Deserialise.assertProperty(OWNER,Deserialise.Type.STRING,obj);
		Deserialise.assertProperty(MOST_RECENT_USERNAME,Deserialise.Type.STRING,obj);
		Deserialise.assertProperty(JOIN_DATE,Deserialise.Type.STRING,obj);
		Deserialise.assertProperty(LAST_JOIN,Deserialise.Type.STRING,obj);
		Deserialise.assertProperty(USERNAMES,Deserialise.Type.JSON_ARRAY,obj);
		Deserialise.assertProperty(IP_ADDRESSES,Deserialise.Type.JSON_ARRAY,obj);
		Deserialise.assertProperty(PLAY_TIME,Deserialise.Type.NUMBER,obj);
		Deserialise.assertProperty(IGNORED_PLAYERS,Deserialise.Type.JSON_ARRAY,obj);
		Deserialise.assertProperty(PLAYER_INVENTORY,Deserialise.Type.JSON_OBJECT,obj);
		Deserialise.assertProperty(PLAYER_ENDERCHEST,Deserialise.Type.JSON_OBJECT,obj);
		Deserialise.assertProperty(PLAYER_ENTITY,Deserialise.Type.JSON_OBJECT,obj);
		Deserialise.assertProperty(PLAYER_POST,Deserialise.Type.JSON_OBJECT,obj);
		Deserialise.assertProperty(EXTENSION_DATA,Deserialise.Type.JSON_OBJECT,obj);
		
		degradedState = obj.get(IS_IN_DEGRADED_STATE).getAsBoolean();
		owner = Deserialise.uuid(obj.get(OWNER));
		mostRecentUsername = obj.get(MOST_RECENT_USERNAME).getAsString();
		
		try
		{
			joinDate = PlayerData.SaveFormat.parse(obj.get(JOIN_DATE).getAsString());
			lastJoin = PlayerData.SaveFormat.parse(obj.get(LAST_JOIN).getAsString());
		}
		catch(ParseException e)
		{
			e.printStackTrace();
			throw new DeserialiseException("PlayerData failed to parse JoinDate and/or LastJoinDate");
		}
		
		Set<String> usernames = new HashSet<>();
		for(JsonElement ele : obj.get(USERNAMES).getAsJsonArray())
		{
			Deserialise.assertType(Deserialise.Type.STRING,ele);
			usernames.add(ele.getAsJsonPrimitive().getAsString());
		}
		
		this.usernames = usernames;
		
		Set<String> ipAddresses = new HashSet<>();
		for(JsonElement ele : obj.get(IP_ADDRESSES).getAsJsonArray())
		{
			Deserialise.assertType(Deserialise.Type.STRING,ele);
			ipAddresses.add(ele.getAsJsonPrimitive().getAsString());
		}
		
		this.ipAddresses = ipAddresses;
		playTime = obj.get(PLAY_TIME).getAsLong();
		
		Set<UUID> ignoredPlayers = new HashSet<>();
		for(JsonElement ele : obj.get(IGNORED_PLAYERS).getAsJsonArray())
		{
			Deserialise.assertType(Deserialise.Type.STRING,ele);
			UUID uuid = Deserialise.uuid(ele);
			ignoredPlayers.add(uuid);
		}
		
		this.ignoredPlayers = ignoredPlayers;
		
		getInventory().deserialise(obj.get(PLAYER_INVENTORY).getAsJsonObject());
		getEnderchest().deserialise(obj.get(PLAYER_ENDERCHEST).getAsJsonObject());
		getEntityData().deserialise(obj.get(PLAYER_ENTITY).getAsJsonObject());
		getPostData().deserialise(obj.get(PLAYER_POST).getAsJsonObject());
		
		JsonObject extensionDataObj = obj.get(EXTENSION_DATA).getAsJsonObject();
		Map<Namespace,PlayerDataExtension> extensionData = new HashMap<>();
		for(Entry<String,JsonElement> element : extensionDataObj.entrySet())
		{
			Deserialise.assertType(Deserialise.Type.JSON_OBJECT,element.getValue());
			Namespace extensionName = Namespace.fromString(element.getKey());
			
			if(PlayerDataCtrl.hasExtensionClass(extensionName))
			{
				Logg.warn("PlayerDataExtension '" + extensionName.toSimpleString() + "' is not registered or does not exist!");
				continue;
			}
			
			Class<? extends PlayerDataExtension> extensionClass = PlayerDataCtrl.getExtensionClass(extensionName);
			Constructor<? extends PlayerDataExtension> cons;
			
			try
			{
				 cons = extensionClass.getDeclaredConstructor(UUID.class);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new DeserialiseException("PlayerDataExtension " + extensionClass.getSimpleName() + " is missing a single argument constructor for " + UUID.class.toGenericString());
			}
			
			try
			{
				PlayerDataExtension instance = cons.newInstance(owner);
				extensionData.put(extensionName,instance);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new DeserialiseException("PlayerDataExtension " + extensionClass.getSimpleName() + " was not able to initialise using constructor " + cons.toGenericString());
			}
		}
		
		this.extensionData = extensionData;
	}
}