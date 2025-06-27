package coffee.dape.feature.vnpc;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import com.google.gson.JsonElement;

import coffee.dape.Dape;
import coffee.dape.feature.vnpc.VNpc.InteractionType;
import coffee.dape.utils.FUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.MathUtils;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.SoundUtils;
import coffee.dape.utils.WorldUtils;
import coffee.dape.utils.data.DataUtils;
import coffee.dape.utils.structs.UID4;

public class VNpcCtrl
{
	private static final Path dataLocation = Dape.featureFilePath("vnpcs");
	private static Map<UID4,VNpc> vnpcs = new HashMap<>();
	private static Map<UUID,VNpcInteractSession> sessions = new HashMap<>();
	private static long sessionDuration = 20L;
	public static String DT_VNPC = "vnpc";
	
	public static UID4 addVNpc(String name,Location loc)
	{
		return addVNpc(name,loc.getWorld(),loc.getX(),loc.getY(),loc.getBlockZ());
	}
	
	public static UID4 addVNpc(String name,World world,double x,double y,double z)
	{
		Objects.requireNonNull(name,"Name cannot be null!");
		Objects.requireNonNull(world,"World cannot be null!");
		Objects.requireNonNull(x,"X cannot be null!");
		Objects.requireNonNull(y,"Y cannot be null!");
		Objects.requireNonNull(z,"Z cannot be null!");
		
		if(name.length() > 64) { throw new IllegalArgumentException("Name of VNpc cannot be longer than 64 characters!"); }
		double clampedX = MathUtils.clamp(WorldUtils.getEdge() *-1,WorldUtils.getEdge(),x);
		double clampedY = MathUtils.clamp(world.getMinHeight(),world.getMaxHeight(),y);
		double clampedZ = MathUtils.clamp(WorldUtils.getEdge() *-1,WorldUtils.getEdge(),z);
		
		VNpc npc = VNpc.create(name);
		
		npc.setWorld(world.getName());
		npc.setX(clampedX);
		npc.setY(clampedY);
		npc.setZ(clampedZ);
		npc.setProfession(Villager.Profession.MASON);
		npc.setVillagerType(Villager.Type.PLAINS);
		
		MerchantRecipe trade = new MerchantRecipe(new ItemStack(Material.GRASS_BLOCK),0,Integer.MAX_VALUE,true,0,1.0f);
		trade.addIngredient(new ItemStack(Material.EMERALD,1));
		
		npc.addTrade(trade);
		
		vnpcs.put(npc.getId(),npc);
		return npc.getId();
	}
	
	/**
	 * Checks if a trader exists
	 * @param key Key used to hold this trader in the map
	 * @return true if the trader exists in the map
	 */
	public static boolean contains(UID4 key)
	{
		Objects.requireNonNull(key,"UID4 cannot be null!");
		
		for(UID4 uid4 : vnpcs.keySet())
		{
			Logg.verb("UID4 " + uid4.toString(),Logg.VerbGroup.FEATURE_VNPC);
			Logg.verb(key.toString() + " == " + uid4.toString() + " ? > " + key.equals(uid4),Logg.VerbGroup.FEATURE_VNPC);
		}
		
		return vnpcs.containsKey(key);
	}
	
	/**
	 * Retrieves a trader from the map
	 * @param key Key used to hold this trader in the map
	 * @return Trader object
	 */
	public static VNpc getVNpc(UID4 key)
	{
		Objects.requireNonNull(key,"UID4 cannot be null!");
		return vnpcs.get(key);
	}
	
	/**
	 * Removes a trader from the map
	 * @param key Key used to hold this trader in the map
	 * @return Trader object
	 */
	public static VNpc remove(UID4 key)
	{
		Objects.requireNonNull(key,"UID4 cannot be null!");
		if(!contains(key)) { return null; }
		vnpcs.get(key).despawn();
		FUtils.delete(Paths.get(dataLocation.toAbsolutePath() + File.separator + key.toString() + ".json"));
		return vnpcs.remove(key);
	}
	
	/**
	 * Retrieves a set of keys for each trader
	 * @return Set of keys
	 */
	public static Set<UID4> getVNpcUIDs()
	{
		return vnpcs.keySet();
	}
	
	/**
	 * Retrieves a collection of all npcs held in the map
	 * @return Collection of npcs
	 */
	public static Collection<VNpc> getVNpcs()
	{
		return vnpcs.values();
	}
	
	/**
	 * Clears all npcs
	 */
	public static void clear()
	{
		vnpcs.values().forEach(v -> v.despawn());
		vnpcs.clear();
	}
	
	/**
	 * Gets an npc interact session
	 * @param uuid UUID of owner of this session
	 * @return
	 */
	public static VNpcInteractSession getSession(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		if(sessions.containsKey(p.getUniqueId())) { sessions.get(p.getUniqueId()).resetDuration(); }
		return sessions.get(p.getUniqueId());
	}
	
	/**
	 * Removes an npc interact session
	 * @param uuid UUID of owner of this session
	 */
	public static void removeSession(UUID uuid)
	{
		Objects.requireNonNull(uuid,"Session UUID cannot be null!");
		
		// End talk clocks if they're currently running
		if(sessions.containsKey(uuid)) { sessions.get(uuid).endTalkClock(); }
		sessions.remove(uuid);
	}
	
	/**
	 * Checks if this player has session data
	 * @param p Player
	 * @param key Key this data is stored with
	 * @return true if there is session data for this key, false otherwise
	 */
	public static boolean hasSessionData(Player p,String key)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		Objects.requireNonNull(key,"Key cannot be null!");
		if(!sessions.containsKey(p.getUniqueId())) { return false; }
		if(!sessions.get(p.getUniqueId()).getData().has(key)) { return false; }
		return true;
	}
	
	/**
	 * Removes session data for a player
	 * @param p Player
	 * @param key Key this data is stored with
	 */
	public static void removeSessionData(Player p,String key)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		Objects.requireNonNull(key,"Key cannot be null!");
		if(!sessions.containsKey(p.getUniqueId())) { return; }
		sessions.get(p.getUniqueId()).getData().remove(key);
	}
	
	/**
	 * Set session data for a player
	 * @param p Player
	 * @param key Key this data is stored with
	 * @param value Value stored
	 */
	public static void setSessionData(Player p,String key,boolean value)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		Objects.requireNonNull(key,"Key cannot be null!");
		Objects.requireNonNull(p,"Value cannot be null!");
		if(!sessions.containsKey(p.getUniqueId())) { Logg.error("Could not set session data for " + p.getName() + " as session is null!"); return; }
		sessions.get(p.getUniqueId()).getData().addProperty(key,value);
	}
	
	/**
	 * Set session data for a player
	 * @param p Player
	 * @param key Key this data is stored with
	 * @param value Value stored
	 */
	public static void setSessionData(Player p,String key,Number value)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		Objects.requireNonNull(key,"Key cannot be null!");
		Objects.requireNonNull(p,"Value cannot be null!");
		if(!sessions.containsKey(p.getUniqueId())) { Logg.error("Could not set session data for " + p.getName() + " as session is null!"); return; }
		sessions.get(p.getUniqueId()).getData().addProperty(key,value);
	}
	
	/**
	 * Set session data for a player
	 * @param p Player
	 * @param key Key this data is stored with
	 * @param value Value stored
	 */
	public static void setSessionData(Player p,String key,String value)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		Objects.requireNonNull(key,"Key cannot be null!");
		Objects.requireNonNull(p,"Value cannot be null!");
		if(!sessions.containsKey(p.getUniqueId())) { Logg.error("Could not set session data for " + p.getName() + " as session is null!"); return; }
		sessions.get(p.getUniqueId()).getData().addProperty(key,value);
	}
	
	/**
	 * Get session data from a player
	 * @param p Player
	 * @param key Key this data is stored with
	 * @return JsonElement data
	 */
	public static JsonElement getSessionData(Player p,String key)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		Objects.requireNonNull(key,"Key cannot be null!");
		if(!sessions.containsKey(p.getUniqueId())) { return null; }
		if(!sessions.get(p.getUniqueId()).getData().has(key)) { return null; }
		return sessions.get(p.getUniqueId()).getData().get(key);
	}
	
	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e)
	{
		if(!e.getEntityType().equals(EntityType.VILLAGER)) { return; }
		if(e.getDamager() == null) { return; }
		
		if(DataUtils.has(DT_VNPC,e.getEntity()))
		{
			e.setCancelled(true);
			
			if(e.getDamager() instanceof Player)
			{
				Player p = (Player) e.getDamager();
				p.damage(e.getDamage());
				PrintUtils.info(p,"Your damage was reflected back to you for trying to hit a npc.");
			}
			
			e.setDamage(0);
		}
	}
	
	@EventHandler
    public void interactAt(PlayerInteractEntityEvent e)
    {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        
        if(e.getRightClicked().getType() != EntityType.VILLAGER) { return; }
        if(DataUtils.has(DT_VNPC,e.getRightClicked()))
		{
        	e.setCancelled(true);
        	UID4 npcKey = UID4.fromString(DataUtils.get(DT_VNPC,e.getRightClicked()).asString());
        	VNpc npc = vnpcs.get(npcKey);
        	
    		if(npc.getInteractionType() == InteractionType.INTERACTABLE)
    		{
    			if(!sessions.containsKey(uuid))
            	{
    				sessions.put(uuid,new VNpcInteractSession(uuid,npcKey,sessionDuration));
    				sessions.get(uuid).setCurrentVNpcId(npcKey);
        			sessions.get(uuid).setCurrentActionListId(npc.getDefaultListId());
            	}
    			
    			sessions.get(uuid).resetDuration();
        		
        		if(!sessions.get(uuid).getCurrentVNpcId().equals(npcKey))
        		{
        			sessions.get(uuid).clearData();
        			sessions.get(uuid).endTalkClock();
        			sessions.get(uuid).setCurrentVNpcId(npcKey);
        			sessions.get(uuid).setCurrentActionListId(npc.getDefaultListId());
        			sessions.get(uuid).setPendingChatButtonExecution(null);
        		}
        		
        		SoundUtils.playSound(p,Sound.BLOCK_NOTE_BLOCK_XYLOPHONE,1.0f);
        		
        		if(sessions.get(uuid).getCurrentAction() != null)
        		{
        			sessions.get(uuid).getCurrentAction().execute(p);
        		}
        		else
        		{
        			SoundUtils.playSound(p,Sound.ENTITY_VILLAGER_AMBIENT,MathUtils.getRandom(0.0f,2.0f));
        			npc.getEntity().shakeHead();
        		}
    		}
    		else
    		{
    			npc.trade(p);
    		}
		}
    }
	
	@EventHandler
	public void onWorldUnload(WorldUnloadEvent e)
	{
		// Remove npcs when world is unloaded
		for(VNpc npc : getVNpcs())
		{
			if(!npc.getWorld().equals(e.getWorld().getName())) { continue; }
			npc.despawn();
		}
	}
	
	/**
	 * Respawns a npc
	 */
	public static void respawnAll()
	{
		vnpcs.values().forEach(v -> v.respawn());
	}
	
	/**
	 * Save a npc
	 * @param key key used to hold this npc in the map
	 */
	public static void save(UID4 key)
	{
//		if(!VillagerNpcCtrl.contains(key)) { return; }
//		new VillagerNpcWriter().toJson(VillagerNpcCtrl.getVillager(key),Paths.get(DataPaths.ARC_VILLAGER_NPCS.getPath() + File.separator + key + ".json"));
	}
	
	/**
	 * Save all npcs
	 */
	public static void saveAll()
	{
//		for(String key : villagers.keySet())
//		{
//			new VillagerNpcWriter().toJson(VillagerNpcCtrl.getVillager(key),Paths.get(DataPaths.ARC_VILLAGER_NPCS.getPath() + File.separator + key + ".json"));
//		}
	}
	
	/**
	 * Load all npcs
	 */
	public static void loadAll()
	{
//		villagers.clear();
//		
//		Path directory = DataPaths.ARC_VILLAGER_NPCS.getPath();
//		FileSysUtils.createDirectories(directory);
//		
//		if(directory.toFile().listFiles() == null) { return; }
//		if(directory.toFile().listFiles().length == 0) { return; }
//		
//		for(File file : DataPaths.ARC_VILLAGER_NPCS.getListOfFilesInDirectory())
//		{
//			VillagerNpc trader = new VillagerNpcReader().fromJson(file);
//			
//			if(trader == null) { continue; }
//			
//			VillagerNpcCtrl.addTrader(trader);
//			trader.spawn();
//		}
//		
//		DelayedTaskUtils.executeDelayedTask(() ->
//		{
//			for(VillagerNpc villager : VillagerNpcCtrl.getVillagers())
//			{
//				villager.spawn();
//			}
//		},5);
	}
}
