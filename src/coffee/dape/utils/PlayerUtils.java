package coffee.dape.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import coffee.dape.utils.minecraftprofile.MinecraftProfileCtrl;
import coffee.dape.utils.minecraftprofile.data.MinecraftProfile;


/**
 * 
 * @author Laeven
 * 
 */
public class PlayerUtils
{
	/**
     * Clears players inventory
     * @param player
     */
	public static void clearInventory(Player player)
    {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }
	
	/**
	 * Heals a player back to 10 hearts (20 health)
	 * @param player
	 */
    public static void healPlayer(Player player)
    {
        player.setHealth(20.0d);
    }
    
    /**
     * Feeds a player and removes their exhaustion
     * @param player
     */
    public static void feedPlayer(Player player)
    {
        player.setFoodLevel(20);
        player.setExhaustion(0);
    }
    
    /**
     * Clears a players experience
     * @param player
     */
    public static void clearLevels(Player player)
    {
        player.setLevel(0);
        player.setExp(0);
    }
    
	/**
	 * Checks a player to see if they have a specified potion effect
	 * @param player
	 * @param type
	 * @return
	 */
	public static boolean hasPotionEffect(Player player,PotionEffectType type)
	{
		for(PotionEffect pe : player.getActivePotionEffects())
		{
			if(pe.getType().equals(type))
			{
				return true;
			}
		}
		
		return false;
	}
    
    /**
     * Clears a player of a single potion effect if they have it
     * @param player
     * @param effect
     */
    public static void clearEffect(Player player,PotionEffect effect)
    {
    	if(player.getActivePotionEffects().contains(effect))
    	{
    		player.getActivePotionEffects().remove(effect);
    	}
    }
    
    /**
     * Clears a player of all potion effects
     * @param player
     */
    public static void clearEffects(Player player)
    {
        for (PotionEffect effect : player.getActivePotionEffects())
        {
            player.removePotionEffect(effect.getType());
        }
    }
    
    /**
     * Checks if this playerName is a real player
     * @param playerName Name of a Minecraft Java Player
     * @return True if this playerName belongs to a player, false otherwise
     */
    public static boolean isAPlayer(String playerName)
    {
    	if(isOnline(playerName)) { return true; }
		return MinecraftProfileCtrl.isRealPlayerName(playerName);
    }
    
    /**
     * Checks if this playerUUID is a real player
     * @param playerUUID UUID of a Minecraft Java Player
     * @return True if this playerName belongs to a player, false otherwise
     */
    public static boolean isAPlayer(UUID playerUUID)
    {
    	if(isOnline(playerUUID)) { return true; }
		return MinecraftProfileCtrl.isRealPlayerUUID(playerUUID);
    }
	
	/**
	 * Checks if the player is online
	 * @param playerName Name of the player to check if it is online
	 * @return true if player is online, false otherwise
	 */
	public static boolean isOnline(String playerName)
	{
		Player p = Bukkit.getPlayerExact(playerName);
		return p != null;
	}
	
	/**
	 * Checks if the player is online
	 * @param UUID uuid of the player
	 * @return true if player is online, false otherwise
	 */
	public static boolean isOnline(UUID uuid)
	{
		Player p = Bukkit.getPlayer(uuid);
		return p != null;
	}
	
	/**
	 * Gets a player object if this player exists and is online
	 * @param playerName Name of the player to check if it is online
	 * @return Player object, null otherwise
	 */
	public static Player getPlayer(String playerName)
	{
		if(!isOnline(playerName)) { Logg.error("Player is not online!"); return null; }
		return Bukkit.getPlayerExact(playerName);
	}
	
	/**
	 * Gets a player object if this player exists and is online
	 * @param UUID uuid of the player 
	 * @return Player object, null otherwise
	 */
	public static Player getPlayer(UUID uuid)
	{
		if(!isOnline(uuid)) { Logg.error("Player is not online!"); return null; }
		return Bukkit.getPlayer(uuid);
	}
	
	/**
	 * Gets the name of a player regardless if they're online
	 * @param p Player
	 * @return Name of the player, null otherwise
	 */
	public static String getName(Player p)
	{
		if(p.isOnline()) { return p.getName(); }
		UUID uuid = p.getUniqueId();		
		return getName(uuid);
	}
	
	/**
	 * Gets the name of a player regardless if they're online
	 * @param uuid UUID of the player
	 * @return Name of the player, null otherwise
	 */
	public static String getName(UUID uuid)
	{
		if(isOnline(uuid)) { return Bukkit.getPlayer(uuid).getName(); }
		MinecraftProfile mp = MinecraftProfileCtrl.getPlayerProfile(uuid,false);
		if(mp == null) { return null; }
		return mp.getName();
	}
	
	/**
	 * Gets the UUID of a player regardless if they're online
	 * @param playerName Name of the player
	 * @return UUID of the player, null otherwise
	 */
	public static UUID getUUID(String playerName)
	{
		if(isOnline(playerName)) { return Bukkit.getPlayerExact(playerName).getUniqueId(); }
		return MinecraftProfileCtrl.getUUID(playerName);
	}
	
	/**
	 * Gets the entity the player is looking at
	 * @param p Player
	 * @return Entity player is looking at. If player is not looking at an entity it will return null
	 */
	public static Entity getEntityLookingAt(Player p)
	{
		RayTraceResult result = p.getLocation().getWorld().rayTraceEntities(p.getEyeLocation().add(p.getEyeLocation().getDirection().normalize().multiply(2)),p.getEyeLocation().getDirection(),10);
		
		if(result == null) { return null; }
		if(result.getHitEntity() == null) { return null; }
		return result.getHitEntity();
	}
	
	/**
	 * Gets the block the player is looking at
	 * @param p Player
	 * @param maxDistance Max distance for the ray cast
	 * @return Block player is looking at, null otherwise if block is too far away
	 */
	public static Block getBlockLookingAt(Player p,int maxDistance)
	{
		Location eyeLoc = p.getEyeLocation();
		RayTraceResult result = p.getLocation().getWorld().rayTraceBlocks(eyeLoc,eyeLoc.getDirection(),maxDistance,FluidCollisionMode.NEVER);
		
		if(result == null) { return null; }
		if(result.getHitBlockFace() == null) { return null; }
		return result.getHitBlock();
	}
	
	public static Vector getLookDirection(Player p)
	{
		return p.getLocation().getDirection();
	}
	
	public static Vector getDirectionOfEntityFromPlayer(Player p,Entity e)
	{
		return p.getEyeLocation().subtract(e.getLocation()).toVector();
	}
	
	public static Vector getDirectionOfEntityFromPlayer(Player p,LivingEntity e)
	{
		return p.getEyeLocation().subtract(e.getEyeLocation()).toVector();
	}
	
	public static void resetSize(Player player)
	{
		if(player.getAttribute(Attribute.SCALE) != null) { player.getAttribute(Attribute.SCALE).setBaseValue(1); }
		if(player.getAttribute(Attribute.JUMP_STRENGTH) != null) { player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0.41D); }
		if(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE) != null) { player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).setBaseValue(4.5D); }
		if(player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE) != null) { player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).setBaseValue(3D); }  
		if(player.getAttribute(Attribute.MOVEMENT_SPEED) != null) { player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1D); }
		if(player.getAttribute(Attribute.FLYING_SPEED) != null) { player.getAttribute(Attribute.FLYING_SPEED).setBaseValue(0.4D); }
		if(player.getAttribute(Attribute.STEP_HEIGHT) != null) { player.getAttribute(Attribute.STEP_HEIGHT).setBaseValue(0.6D); }
		if(player.getAttribute(Attribute.SAFE_FALL_DISTANCE) != null) { player.getAttribute(Attribute.SAFE_FALL_DISTANCE).setBaseValue(3.0D); }
		if(player.getAttribute(Attribute.GRAVITY) != null) { player.getAttribute(Attribute.GRAVITY).setBaseValue(0.08D); }
		if(player.getAttribute(Attribute.WATER_MOVEMENT_EFFICIENCY) != null) { player.getAttribute(Attribute.WATER_MOVEMENT_EFFICIENCY).setBaseValue(0D); }
	}
}