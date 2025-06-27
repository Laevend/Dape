package coffee.dape.feature.vnpc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import coffee.dape.Dape;
import coffee.dape.feature.vnpc.actions.VNpcAction;
import coffee.dape.utils.ChatBuilder;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.SoundUtils;
import coffee.dape.utils.WorldUtils;
import coffee.dape.utils.clocks.RepeatingClock;
import coffee.dape.utils.data.DataUtils;
import coffee.dape.utils.structs.UID4;

public class VNpc
{
	private static final DecimalFormat locationFormat = new DecimalFormat("#.##");
	private final UID4 id;
	private String customName = "I am Npc";
	private String world = "world";
	private double x = 0d;
	private double y = 0d;
	private double z = 0d;
	private Villager.Profession profession;
	private Villager.Type villagerType;
	private VNpc.InteractionType interactionType = VNpc.InteractionType.NONE;
	private UID4 defaultListId = UID4.randomUID("default");
	private Map<UID4,ArrayList<VNpcAction>> actions = new HashMap<>();
	private List<MerchantRecipe> trades = new ArrayList<>();
	private WatchClock watch;
	private Villager entity = null;
	
	/**
	 * Creates a VNpc
	 * @return VNpc
	 */
	public static VNpc create(String vnpcName)
	{
		return new VNpc(vnpcName);
	}
	
	/**
	 * Creates a VNpc
	 * @return VNpc
	 */
	public static VNpc create(UID4 uid)
	{
		return new VNpc(uid);
	}
	
	private VNpc(String vnpcName)
	{
		id = UID4.randomUID(vnpcName);
		watch = new WatchClock();
		watch.start();
	}
	
	private VNpc(UID4 uid)
	{
		id = uid;
		watch = new WatchClock();
		watch.start();
	}
	
	public UID4 getId()
	{
		return id;
	}

	public String getCustomName()
	{
		return customName;
	}

	public void setCustomName(String customName)
	{
		this.customName = customName;
	}
	
	public void setLocation(Location loc)
	{
		this.world = loc.getWorld().getName();
		this.x = loc.getX();
		this.y = loc.getY();
		this.z = loc.getZ();
	}

	public String getWorld()
	{
		return world;
	}

	public void setWorld(String world)
	{
		this.world = world;
	}

	public double getX()
	{
		return x;
	}

	public void setX(double x)
	{
		this.x = x;
	}

	public double getY()
	{
		return y;
	}

	public void setY(double y)
	{
		this.y = y;
	}

	public double getZ()
	{
		return z;
	}

	public void setZ(double z)
	{
		this.z = z;
	}

	public Villager.Profession getProfession()
	{
		return profession;
	}

	public void setProfession(Villager.Profession profession)
	{
		this.profession = profession;
	}

	public Villager.Type getVillagerType()
	{
		return villagerType;
	}

	public void setVillagerType(Villager.Type villagerType)
	{
		this.villagerType = villagerType;
	}

	public VNpc.InteractionType getInteractionType()
	{
		return interactionType;
	}

	public void setInteractionType(VNpc.InteractionType interactionType)
	{
		this.interactionType = interactionType;
	}
	
	public void addAction(VNpcAction action)
	{
		addAction(defaultListId,action);
	}
	
	public void addAction(UID4 listId,VNpcAction action)
	{
		if(!actions.containsKey(listId))
		{
			actions.put(listId,new ArrayList<>());
		}
		
		actions.get(listId).add(action);
	}
	

	public Map<UID4,ArrayList<VNpcAction>> getActions()
	{
		return actions;
	}

	public void setActions(Map<UID4,ArrayList<VNpcAction>> actions)
	{
		this.actions = actions;
	}
	
	public UID4 getDefaultListId()
	{
		return defaultListId;
	}

	protected void setDefaultListId(UID4 defaultListId)
	{
		this.defaultListId = defaultListId;
	}

	public List<MerchantRecipe> getTrades()
	{
		return trades;
	}

	public void setTrades(List<MerchantRecipe> trades)
	{
		this.trades = trades;
	}
	
	public void addTrade(MerchantRecipe trade)
	{
		this.trades.add(trade);
	}
	
	public void removeTrade(int index)
	{
		this.trades.remove(index);
	}

	public Villager getEntity()
	{
		return entity;
	}
	
	/**
	 * Trade with this villager npc
	 * @param p Player to open trade window for
	 */
	public void trade(Player p)
	{
		if(this.interactionType != InteractionType.INTERACTABLE_MERCHANT && this.interactionType != InteractionType.INTERACTABLE_MERCHANT_ONE_PLAYER_ONLY)
		{
			this.entity.shakeHead();
			SoundUtils.playSound(p,Sound.ENTITY_VILLAGER_NO,0.0f);
			return;
		}
		
		Merchant tempMerch = Bukkit.createMerchant(this.customName);
		tempMerch.setRecipes(this.trades);
		
		switch(this.interactionType)
		{
			case INTERACTABLE_MERCHANT -> { p.openMerchant(tempMerch,true); }
			case INTERACTABLE_MERCHANT_ONE_PLAYER_ONLY ->
			{
				InventoryView iv = p.openMerchant(this.entity,false);
				
				if(iv != null) { return; }
				
				PrintUtils.info(p,"Cannot trade as another player is currently trading with this villager!");
			}
			default -> { Logg.error("Unknown interactable state: " + this.interactionType); return; }
		}
	}
	
	public void respawn()
	{
		despawn();
		
		if(!WorldUtils.isWorldLoaded(this.world)) { Logg.error("VNpc " + this.id.toString() + " could not be spawned as the world it exists in is not loaded or doesn't exist!"); return; }
		
		World world = Bukkit.getWorld(this.world);
		Location spawnLocation = new Location(world,this.x,this.y,this.z);
		
		if(!spawnLocation.getChunk().isLoaded()) { spawnLocation.getChunk().load(); }
		
		// Removing existing entity instance of this VNpc if it's present due to a crash
		for(Entity e : world.getNearbyEntities(spawnLocation,5,5,5))
		{
			if(!DataUtils.has(VNpcCtrl.DT_VNPC,e)) { continue; }
			if(!DataUtils.get(VNpcCtrl.DT_VNPC,e).asString().equals(this.id.toString())) { continue; }
			e.remove();
			Logg.warn("Orphaned VNpc entity found -> " + e.getLocation().toString());
		}
		
		this.entity = (Villager) world.spawnEntity(spawnLocation,EntityType.VILLAGER);
		this.entity.setVillagerType(this.villagerType);
		this.entity.setProfession(this.profession);
		this.entity.setPersistent(true);
		this.entity.setInvulnerable(true);
		this.entity.setAI(false);
		this.entity.setVillagerExperience(250);
		
		if(this.customName != null)
		{
			this.entity.setCustomName(ColourUtils.translate(this.customName));
			this.entity.setCustomNameVisible(true);
		}
		
		DataUtils.set(VNpcCtrl.DT_VNPC,this.id.toString(),this.entity);
		Dape.setAsPluginManaged(this.entity);
		
		this.watch.start();
	}
	
	public void despawn()
	{
		if(this.watch != null && this.watch.isEnabled())
		{
			this.watch.stop();
		}
		
		if(this.entity == null || this.entity.isDead())
		{
			// Entity is null so we look for the entity instead and despawn it that way
			if(!WorldUtils.isWorldLoaded(this.world)) { Logg.error("VNpc " + this.id.toString() + " could not be despawned as the world it exists in is not loaded or doesn't exist!"); return; }
			
			World world = Bukkit.getWorld(this.world);
			Location spawnLocation = new Location(world,this.x,this.y,this.z);
			spawnLocation.getChunk().addPluginChunkTicket(Dape.instance());
			
			for(Entity e : world.getNearbyEntities(spawnLocation,5,5,5))
			{
				if(!DataUtils.has(VNpcCtrl.DT_VNPC,e)) { continue; }
				if(!DataUtils.get(VNpcCtrl.DT_VNPC,e).asString().equals(this.id.toString())) { continue; }
				e.remove();
				Logg.verb("Orphaned VNpc entity removed -> " + e.getLocation().toString(),Logg.VerbGroup.FEATURE_VNPC);
			}
			
			spawnLocation.getChunk().removePluginChunkTicket(Dape.instance());
			this.entity = null;
			return;
		}
		
		this.entity.remove();
		this.entity = null;
	}
	
	public void printInfo(Player p)
	{
		PrintUtils.sendCenteredMessage(p,"NPC " + id.toString());
		PrintUtils.raw(p,ColourUtils.translate("&8Name: &a" + this.customName));
		PrintUtils.raw(p,ColourUtils.translate("&8World: &a" + this.world));
		PrintUtils.raw(p,ColourUtils.translate("&8Interaction: &a" + this.interactionType.toString().toLowerCase()));
		PrintUtils.raw(p,ColourUtils.translate("&8X: &a" + locationFormat.format(this.x)));
		PrintUtils.raw(p,ColourUtils.translate("&8Y: &a" + locationFormat.format(this.y)));
		PrintUtils.raw(p,ColourUtils.translate("&8Z: &a" + locationFormat.format(this.z)));
		
		PrintUtils.sendComp(p,new ChatBuilder("&8[&dTeleport to Villager&8]")
				.setHoverShowTextEvent("&eClick this text to teleport")
				.setClickRunCommandEvent("/minecraft:tp " + p.getName() + " " + this.x + " " + this.y + " " + this.z)
				.getResult());
	}
	
	private class WatchClock extends RepeatingClock
	{
		private PriorityQueue<Entity> nearEntities = getPriorityQueue();
		
		public WatchClock()
		{
			super("VillagerWatchClock (" + id.getPrefix() + ")",10);
		}

		@Override
		public void execute() throws Exception
		{
			if(entity == null || entity.isDead()) { stop(); return; }
			if(WorldUtils.getWorld(world).getPlayers().size() == 0) { return; }
			
			nearEntities.addAll(entity.getNearbyEntities(5,5,5).stream().filter(e -> e.getType() == EntityType.PLAYER).collect(Collectors.toList()));
			
			if(nearEntities.isEmpty()) { return; }
			
			entity.teleport(entity.getLocation().setDirection(nearEntities.peek().getLocation().subtract(entity.getLocation()).toVector()));
			nearEntities.clear();
		}
		
		private PriorityQueue<Entity> getPriorityQueue()
		{
			Comparator<Entity> cCompare = new Comparator<>()
			{
				@Override
				public int compare(Entity e1,Entity e2)
				{
					Double distance1 = e1.getLocation().distance(entity.getLocation());
					Double distance2 = e2.getLocation().distance(entity.getLocation());
					return Double.compare(distance1,distance2);
				}
			};
			
			return new PriorityQueue<Entity>(cCompare);
		}
	}
	
	public enum InteractionType
	{
		NONE,
		INTERACTABLE,
		INTERACTABLE_MERCHANT,
		INTERACTABLE_MERCHANT_ONE_PLAYER_ONLY,
	}
}
