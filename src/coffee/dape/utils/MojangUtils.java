package coffee.dape.utils;

import java.util.UUID;

import com.google.gson.JsonObject;

import coffee.dape.utils.json.JUtils;

/**
 * 
 * @author Laeven
 * Utility methods for calling the Mojang Public API
 */
public class MojangUtils
{
	/**
	 * Mojang at some point removed the ability to query their api for the status of it...
	 * So now I perform a quick query with a username to check its online.
	 */
	
	/**
	 * Checks if Mojang Public API is online
	 * @return True if API is online and working, false otherwise
	 */
	public static boolean isApiOnline()
	{
		String json = WebUtils.sendGet("https://api.mojang.com/users/profiles/minecraft/jeb__");
		return json != null;
	}
	
	/**
	 * Requests a new profile from the Mojang API
	 * @param uuid UUID of player
	 * @param withSignature If also requested the signature of the player to be returned in the API call
	 * @return JsonObject of a players profile, null otherwise
	 */
	public static JsonObject requestProfileFromAPI(UUID uuid,boolean withSignature)
	{
		String json = withSignature ?
				WebUtils.sendGet("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString() + "?unsigned=false") :
					WebUtils.sendGet("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString());
		
		if(json == null) { Logg.error("UUID not found or too many API requests!"); return null; }
		
		return JUtils.toJsonObject(json);
	}
	
	/**
	 * Requests a new UUID lookup from the Mojang API
	 * @param playerName Exact name of the player
	 * @return JsonObject of UUID lookup, null otherwise
	 */
	public static JsonObject requestUUIDFromAPI(String playerName)
	{
		String json = WebUtils.sendGet("https://api.mojang.com/users/profiles/minecraft/" + playerName);
		
		if(json == null) { Logg.error("Too many API requests!"); return new JsonObject(); }
		
		return JUtils.toJsonObject(json);
	}
}