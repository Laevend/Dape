package coffee.dape.playerdata.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coffee.dape.Dape;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.utils.DelayUtils;
import coffee.dape.utils.EntityUtils;
import coffee.dape.utils.MaterialUtils;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.WorldUtils;
import coffee.dape.utils.json.PersistJson;
import coffee.dape.utils.structs.Pair;
import coffee.dape.utils.tools.Deserialise;
import coffee.dape.utils.tools.Serialise;

/**
 * 
 * @author Laeven
 *
 */
public class PlayerEntityData implements PersistJson
{
	private PlayerData parent;
	
	private Location location = new Location(Bukkit.getWorld(WorldUtils.getDefaultWorldName()),0,Bukkit.getWorlds().getFirst().getHighestBlockYAt(0,0),0);
	private int expLevel = 0;
	private float expProgress = 0.0f;
	private boolean allowFlight = false;
	private float flySpeed = 0.1f;
	private boolean isHealthScaled = false;
	private double healthScale = 20.0d;
	private double health = 20.0d;
	private float exhaustion = 0.0f;
	private int starvationRate = 80;
	private int foodLevel = 20;
	private String locale = "en_us";
	private Location respawnLocation = null;
	private float walkSpeed = 0.2f;
	private int fireTicks = -20;
	private float fallDistance = 0.0f;
	private boolean isGliding = false;
	private boolean isGlowing = false;
	private int freezeTicks = 0;
	private int maxNoDamageTicks = 20;
	private int noDamageTicks = 0;
	private boolean isSwimming = false;
	private Vector velocity = new Vector(0.0d,-0.0784000015258789d,0.0d);
	private boolean hasVisualFire = false;
	private int maxAir = 300;
	private int remainingAir = 300;
	private Collection<PotionEffect> potionEffects = List.of();
	private Map<NamespacedKey,Collection<String>> advancements = new HashMap<>();
	private Map<String,Pair<Double,Collection<AttributeModifier>>> attributes = new HashMap<>();
	private Map<Statistic,Integer> statistics = new EnumMap<>(Statistic.class);
	private Map<Material,Integer> statisticsMineBlock = new EnumMap<>(Material.class);
	private Map<Material,Integer> statisticsCraftItem = new EnumMap<>(Material.class);
	private Map<Material,Integer> statisticsUseItem = new EnumMap<>(Material.class);
	private Map<Material,Integer> statisticsBreakItem = new EnumMap<>(Material.class);
	private Map<Material,Integer> statisticsPickup = new EnumMap<>(Material.class);
	private Map<Material,Integer> statisticsDrop = new EnumMap<>(Material.class);
	private Map<EntityType,Integer> statisticsKillEntity = new EnumMap<>(EntityType.class);
	private Map<EntityType,Integer> statisticsEntityKilledBy = new EnumMap<>(EntityType.class);
	
	public PlayerEntityData(PlayerData data)
	{
		this.parent = data;
	}
	
	/**
	 * Updates the contents of this object with the owners entity data
	 */
	@SuppressWarnings("deprecation")
	public void updatePlayerEntity()
	{
		if(!PlayerUtils.isOnline(parent.getOwner())) { return; }
		
		Player p = PlayerUtils.getPlayer(parent.getOwner());
		
		location = p.getLocation();
		expLevel = p.getLevel();
		expProgress = p.getExp();
		allowFlight = p.getAllowFlight();
		flySpeed = p.getFlySpeed();
		isHealthScaled = p.isHealthScaled();
		healthScale = p.getHealthScale();
		health = p.getHealth();
		exhaustion = p.getExhaustion();
		starvationRate = p.getStarvationRate();
		foodLevel = p.getFoodLevel();
		locale = p.getLocale();
		respawnLocation = p.getRespawnLocation();
		walkSpeed = p.getWalkSpeed();
		fireTicks = p.getFireTicks();
		fallDistance = p.getFallDistance();
		isGliding = p.isGliding();
		isGlowing = p.isGlowing();
		freezeTicks = p.getFreezeTicks();
		maxNoDamageTicks = p.getMaximumNoDamageTicks();
		noDamageTicks = p.getNoDamageTicks();
		isSwimming = p.isSwimming();
		velocity = p.getVelocity();
		hasVisualFire = p.isVisualFire();
		maxAir = p.getMaximumAir();
		remainingAir = p.getRemainingAir();
		potionEffects = p.getActivePotionEffects();
		
		if(Dape.getConfigFile().getBoolean("player_data.save_statistics"))
		{
			statistics.clear();
			statisticsMineBlock.clear();
			statisticsCraftItem.clear();
			statisticsUseItem.clear();
			statisticsBreakItem.clear();
			statisticsPickup.clear();
			statisticsDrop.clear();
			statisticsKillEntity.clear();
			statisticsEntityKilledBy.clear();
			
			// Save regular statistics, skip special ones for now
			for(Statistic stat : Statistic.values())
			{
				switch(stat)
				{
					case MINE_BLOCK:
					case CRAFT_ITEM:
					case USE_ITEM:
					case BREAK_ITEM:
					case PICKUP:
					case DROP:
					case KILL_ENTITY:
					case ENTITY_KILLED_BY:
					{
						continue;
					}
					default: {}
				}
				
				if(p.getStatistic(stat) != 0)
				{
					statistics.put(stat,p.getStatistic(stat));
				}
			}
			
			// Mine Block
			for(Material block : MaterialUtils.BLOCKS)
			{
				// Don't store statistics if they're 0, pointless waste of space
				if(p.getStatistic(Statistic.MINE_BLOCK,block) != 0)
				{
					statisticsMineBlock.put(block,p.getStatistic(Statistic.MINE_BLOCK,block));
				}
			}
			
			// Craft Item, Use Item, Break Item, Pickup & Drop
			for(Material item : MaterialUtils.ITEMS)
			{
				if(p.getStatistic(Statistic.CRAFT_ITEM,item) != 0)
				{
					statisticsCraftItem.put(item,p.getStatistic(Statistic.CRAFT_ITEM,item));
				}
				
				if(p.getStatistic(Statistic.USE_ITEM,item) != 0)
				{
					statisticsUseItem.put(item,p.getStatistic(Statistic.USE_ITEM,item));
				}
				
				if(p.getStatistic(Statistic.BREAK_ITEM,item) != 0)
				{
					statisticsBreakItem.put(item,p.getStatistic(Statistic.BREAK_ITEM,item));
				}
				
				if(p.getStatistic(Statistic.PICKUP,item) != 0)
				{
					statisticsPickup.put(item,p.getStatistic(Statistic.PICKUP,item));
				}
				
				if(p.getStatistic(Statistic.DROP,item) != 0)
				{
					statisticsDrop.put(item,p.getStatistic(Statistic.DROP,item));
				}
			}
			
			// Kill entity & Killed by entity
			for(EntityType entity : EntityUtils.LIVING_ENTITIES)
			{
				if(p.getStatistic(Statistic.KILL_ENTITY,entity) != 0)
				{
					statisticsKillEntity.put(entity,p.getStatistic(Statistic.KILL_ENTITY,entity));
				}
				
				if(p.getStatistic(Statistic.ENTITY_KILLED_BY,entity) != 0)
				{
					statisticsEntityKilledBy.put(entity,p.getStatistic(Statistic.ENTITY_KILLED_BY,entity));
				}
			}
		}
		
		if(Dape.getConfigFile().getBoolean("player_data.save_advancements"))
		{
			advancements.clear();
			
			for(Iterator<Advancement> it = Bukkit.advancementIterator(); it.hasNext();)
			{
				Advancement adv = it.next();
				
				// Same with advancements. Don't store if you've not been awarded any criteria, pointless waste of space
				if(p.getAdvancementProgress(adv).getAwardedCriteria().size() > 0)
				{
					advancements.put(adv.getKey(),p.getAdvancementProgress(adv).getAwardedCriteria());
				}
			}
		}
		
		attributes.clear();
		
		for(Iterator<Attribute> it = Registry.ATTRIBUTE.iterator(); it.hasNext();)
		{
			Attribute attribute = it.next();
			AttributeInstance attIns = p.getAttribute(attribute);
			
			// If attribute is not applicable to player then it will be null
			if(attIns == null) { continue; }
			
			attributes.put(attribute.getKey().getKey(),new Pair<>(attIns.getBaseValue(),attIns.getModifiers()));
		}
	}
	
	/**
	 * Overwrites the players entity data with the saved
	 * entity data stored in this object
	 */
	@SuppressWarnings("deprecation")
	public void loadEntityDataToPlayer()
	{
		if(!PlayerUtils.isOnline(parent.getOwner())) { return; }
		
		Player p = PlayerUtils.getPlayer(parent.getOwner());
		
		// Move player to location in PlayerEntity data if further away than 2 blocks
		double distanceFromDataLocation = p.getLocation().distance(location);
		
		if(distanceFromDataLocation > 2)
		{
			p.teleport(location,TeleportCause.PLUGIN);
		}
		
		p.setLevel(expLevel);
		p.setExp(expProgress);
		p.setAllowFlight(allowFlight);
		p.setFlySpeed(flySpeed);
		p.setHealthScaled(isHealthScaled);
		p.setHealthScale(healthScale);
		p.setHealth(health);
		p.setExhaustion(exhaustion);
		p.setStarvationRate(starvationRate);
		p.setFoodLevel(foodLevel);
		p.setRespawnLocation(respawnLocation);
		p.setWalkSpeed(walkSpeed);
		p.setFireTicks(fireTicks);
		p.setFallDistance(fallDistance);
		p.setGliding(isGliding);
		p.setGlowing(isGlowing);
		p.setFreezeTicks(freezeTicks);
		p.setMaximumNoDamageTicks(maxNoDamageTicks);
		p.setNoDamageTicks(noDamageTicks);
		p.setSwimming(isSwimming);
		p.setVelocity(velocity);
		p.setVisualFire(hasVisualFire);
		p.setMaximumAir(maxAir);
		p.setRemainingAir(remainingAir);
		
		for(Iterator<PotionEffect> it = p.getActivePotionEffects().iterator(); it.hasNext();)
		{
			PotionEffect eff = it.next();
			p.removePotionEffect(eff.getType());
		}
		
		for(PotionEffect eff : potionEffects)
		{
			p.addPotionEffect(eff);
		}
		
		if(Dape.getConfigFile().getBoolean("player_data.save_statistics"))
		{
			// Remove old statistics
			// Save regular statistics, skip special ones for now
			for(Statistic stat : Statistic.values())
			{
				switch(stat)
				{
					case MINE_BLOCK:
					case CRAFT_ITEM:
					case USE_ITEM:
					case BREAK_ITEM:
					case PICKUP:
					case DROP:
					case KILL_ENTITY:
					case ENTITY_KILLED_BY:
					{
						continue;
					}
					default: {}
				}
				
				p.setStatistic(stat,0);
			}
			
			// Mine Block
			for(Material block : MaterialUtils.BLOCKS)
			{
				p.setStatistic(Statistic.MINE_BLOCK,block,0);
			}
			
			// Craft Item, Use Item, Break Item, Pickup & Drop
			for(Material item : MaterialUtils.ITEMS)
			{
				p.setStatistic(Statistic.CRAFT_ITEM,item,0);
				p.setStatistic(Statistic.USE_ITEM,item,0);
				p.setStatistic(Statistic.BREAK_ITEM,item,0);
				p.setStatistic(Statistic.PICKUP,item,0);
				p.setStatistic(Statistic.DROP,item,0);
			}
			
			// Kill entity & Killed by entity
			for(EntityType entity : EntityUtils.LIVING_ENTITIES)
			{
				p.setStatistic(Statistic.KILL_ENTITY,entity,0);
				p.setStatistic(Statistic.ENTITY_KILLED_BY,entity,0);
			}
			
			// Load regular statistics, skip special ones for now
			for(Statistic stat : statistics.keySet())
			{
				switch(stat)
				{
					case MINE_BLOCK:
					case CRAFT_ITEM:
					case USE_ITEM:
					case BREAK_ITEM:
					case PICKUP:
					case DROP:
					case KILL_ENTITY:
					case ENTITY_KILLED_BY:
					{
						continue;
					}
					default: {}
				}
				
				p.setStatistic(stat,statistics.get(stat));
			}
			
			// Mine Block
			for(Material block : statisticsMineBlock.keySet())
			{
				p.setStatistic(Statistic.MINE_BLOCK,block,statisticsMineBlock.get(block));
			}
			
			// Craft Item
			for(Material item : statisticsCraftItem.keySet())
			{
				p.setStatistic(Statistic.CRAFT_ITEM,item,statisticsCraftItem.get(item));
			}
			
			// Use Item
			for(Material item : statisticsUseItem.keySet())
			{
				p.setStatistic(Statistic.USE_ITEM,item,statisticsUseItem.get(item));
			}
			
			// Break Item
			for(Material item : statisticsBreakItem.keySet())
			{
				p.setStatistic(Statistic.BREAK_ITEM,item,statisticsBreakItem.get(item));
			}
			
			// Pickup
			for(Material item : statisticsPickup.keySet())
			{
				p.setStatistic(Statistic.PICKUP,item,statisticsPickup.get(item));
			}
			
			// Drop
			for(Material item : statisticsDrop.keySet())
			{
				p.setStatistic(Statistic.DROP,item,statisticsDrop.get(item));
			}
			
			// Kill Entity
			for(EntityType entity : statisticsKillEntity.keySet())
			{
				p.setStatistic(Statistic.KILL_ENTITY,entity,statisticsKillEntity.get(entity));
			}
			
			// Entity Killed By
			for(EntityType entity : statisticsEntityKilledBy.keySet())
			{
				p.setStatistic(Statistic.ENTITY_KILLED_BY,entity,statisticsEntityKilledBy.get(entity));
			}
		}
		
		/**
		 * Advancements are... something.
		 * There is no direct way (outside of NMS) to directly set a players advancement progress criteria.
		 * 
		 * This causes issue as if we want to set advancements to the Player entity chat will be spammed with
		 * advancements that player has achieved along with many many toasts of all the advancements.
		 * 
		 * This is not great. The solution for the time being is to temporarily disable the announcement of advancements
		 * and only add criteria to advancements that the player entity does not already have while also removing criteria
		 * that the entity data memory deems the player entity should not have.
		 * 
		 * In the worst case the player gets all the toasts for advancements they have previously achieved.
		 * In the best case the player notices nothing.
		 */
		
		if(Dape.getConfigFile().getBoolean("player_data.save_advancements"))
		{
			// Temporarily disable gamerule announcing while adding advancements back as it causes a lot of announcements
			boolean gameRuleAnnounce = p.getWorld().getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS);
			p.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS,false);
			
			// Delayed by 1 tick to allow the gamerule change to take effect
			DelayUtils.executeDelayedBukkitTask(() ->
			{
				for(Iterator<Advancement> it = Bukkit.advancementIterator(); it.hasNext();)
				{
					Advancement adv = it.next();
					
					Collection<String> criteriaToRemove = new ArrayList<>();
					Collection<String> criteriaToAdd = new ArrayList<>();
					
					if(advancements.containsKey(adv.getKey()))
					{
						// Loop through advancements and make a note of advancement criteria the player already has
						for(String criteria : p.getAdvancementProgress(adv).getAwardedCriteria())
						{
							// Memory save does not have this criteria so it must be removed
							if(!advancements.get(adv.getKey()).contains(criteria))
							{
								criteriaToRemove.add(criteria);
							}
						}
						
						// Loop though advancements and make a note of advancement criteria the player does not have
						for(String criteria : p.getAdvancementProgress(adv).getRemainingCriteria())
						{
							// Memory save has this criteria so it must be added
							if(advancements.get(adv.getKey()).contains(criteria))
							{
								criteriaToAdd.add(criteria);
							}
						}
					}
					else
					{
						// Advancement doesn't exist in memory, all awarded criteria for this advancement will be removed
						for(String criteria : p.getAdvancementProgress(adv).getAwardedCriteria())
						{
							criteriaToRemove.add(criteria);
						}
					}
					
					for(String criteria : criteriaToRemove)
					{
						p.getAdvancementProgress(adv).revokeCriteria(criteria);
					}
					
					for(String criteria : criteriaToAdd)
					{
						p.getAdvancementProgress(adv).awardCriteria(criteria);
					}
				}
				
				// Set gamerule back to how it was
				p.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS,gameRuleAnnounce);
			},1);
		}
		
		for(Iterator<Attribute> it = Registry.ATTRIBUTE.iterator(); it.hasNext();)
		{
			Attribute attribute = it.next();
			AttributeInstance attIns = p.getAttribute(attribute);
			String attributeName = attribute.getKey().getKey();
			
			// If attribute is not applicable to player then it will be null. Also check if its even in the map (new attributes will not be)
			if(attIns == null || !attributes.containsKey(attributeName) ) { continue; }
			
			// Get base value and modifiers
			Pair<Double,Collection<AttributeModifier>> attributeBaseAndMods = attributes.get(attributeName);
			
			attIns.setBaseValue(attributeBaseAndMods.getValueA());
			
			// Remove old modifiers (if any)
			for(AttributeModifier mod : attIns.getModifiers())
			{
				attIns.removeModifier(mod);
			}
			
			// Set new modifiers
			for(AttributeModifier mod : attributeBaseAndMods.getValueB())
			{
				attIns.addModifier(mod);
			}
		}
	}
	
	public void setParent(PlayerData parent)
	{
		this.parent = parent;
	}
	
	public PlayerData getParent()
	{
		return parent;
	}
	
	public static final String LOCATION = "location";
	public static final String EXP_LEVEL = "exp_level";
	public static final String EXP_PROGRESS = "exp_progress";
	public static final String ALLOW_FLIGHT = "allow_flight";
	public static final String FLY_SPEED = "fly_speed";
	public static final String IS_HEALTH_SCALED = "is_health_scaled";
	public static final String HEALTH_SCALE = "health_scale";
	public static final String HEALTH = "health";
	public static final String EXHAUSTION = "exhaustion";
	public static final String STARVATION_RATE = "starvation_rate";
	public static final String FOOD_LEVEL = "food_level";
	public static final String LOCALE = "locale";
	public static final String RESPAWN_LOCATION = "respawn_location";
	public static final String WALK_SPEED = "walk_speed";
	public static final String FIRE_TICKS = "fire_ticks";
	public static final String FALL_DISTANCE = "fall_distance";
	public static final String IS_GLIDING = "is_gliding";
	public static final String IS_GLOWING = "is_glowing";
	public static final String FREEZE_TICKS = "freeze_ticks";
	public static final String MAX_NO_DAMAGE_TICKS = "max_no_damage_ticks";
	public static final String NO_DAMAGE_TICKS = "no_damage_ticks";
	public static final String IS_SWIMMING = "is_swimming";
	public static final String VELOCITY = "velocity";
	public static final String HAS_VISUAL_FIRE = "has_visual_fire";
	public static final String MAX_AIR = "max_air";
	public static final String REMAINING_AIR = "remaining_air";
	public static final String POTION_EFFECTS = "potion_effects";
	public static final String ADVANCEMENTS = "advancements";
	public static final String ATTRIBUTES = "attributes";
	public static final String STATISTICS = "statistics";
	
	@Override
	public JsonObject serialise() throws SerialiseException
	{
		JsonObject obj = new JsonObject();
		
		obj.add(LOCATION,Serialise.location(location));
		obj.addProperty(EXP_LEVEL,expLevel);
		obj.addProperty(EXP_PROGRESS,expProgress);
		obj.addProperty(ALLOW_FLIGHT,allowFlight);
		obj.addProperty(FLY_SPEED,flySpeed);
		obj.addProperty(IS_HEALTH_SCALED,isHealthScaled);
		obj.addProperty(HEALTH_SCALE,healthScale);
		obj.addProperty(HEALTH,health);
		obj.addProperty(EXHAUSTION,exhaustion);
		obj.addProperty(STARVATION_RATE,starvationRate);
		obj.addProperty(FOOD_LEVEL,foodLevel);
		obj.addProperty(LOCALE,locale);
		
		if(respawnLocation != null)
		{
			obj.add(RESPAWN_LOCATION,Serialise.location(respawnLocation));
		}
		
		obj.addProperty(WALK_SPEED,walkSpeed);
		obj.addProperty(FIRE_TICKS,fireTicks);
		obj.addProperty(FALL_DISTANCE,fallDistance);
		obj.addProperty(IS_GLIDING,isGliding);
		obj.addProperty(IS_GLOWING,isGlowing);
		obj.addProperty(FREEZE_TICKS,freezeTicks);
		obj.addProperty(MAX_NO_DAMAGE_TICKS,maxNoDamageTicks);
		obj.addProperty(NO_DAMAGE_TICKS,noDamageTicks);
		obj.addProperty(IS_SWIMMING,isSwimming);
		obj.add(VELOCITY,Serialise.vector(velocity));
		obj.addProperty(HAS_VISUAL_FIRE,hasVisualFire);
		obj.addProperty(MAX_AIR,maxAir);
		obj.addProperty(REMAINING_AIR,remainingAir);
		obj.add(POTION_EFFECTS,Serialise.potionEffects(potionEffects));
		
		if(Dape.getConfigFile().getBoolean("player_data.save_advancements"))
		{
			obj.add(ADVANCEMENTS,Serialise.advancements(advancements));
		}
		
		obj.add(ATTRIBUTES,Serialise.attributes(attributes));
		
		if(Dape.getConfigFile().getBoolean("player_data.save_statistics"))
		{
			JsonObject objStats = new JsonObject();
			
			JsonObject objStatMineBlock = new JsonObject();
			JsonObject objStatCraftItem = new JsonObject();
			JsonObject objStatUseItem = new JsonObject();
			JsonObject objStatBreakItem = new JsonObject();
			JsonObject objStatPickup = new JsonObject();
			JsonObject objStatDrop = new JsonObject();
			JsonObject objStatKillEntity = new JsonObject();
			JsonObject objStatEntityKilledBy = new JsonObject();
			
			// Load regular statistics, skip special ones for now
			for(Statistic stat : Statistic.values())
			{
				if(!statistics.containsKey(stat)) { continue; }
				
				switch(stat)
				{
					case MINE_BLOCK:
					case CRAFT_ITEM:
					case USE_ITEM:
					case BREAK_ITEM:
					case PICKUP:
					case DROP:
					case KILL_ENTITY:
					case ENTITY_KILLED_BY:
					{
						continue;
					}
					default: {}
				}
				
				objStats.addProperty(stat.toString(),statistics.get(stat));
			}
			
			// Mine Block
			for(Material block : statisticsMineBlock.keySet())
			{
				objStatMineBlock.addProperty(block.toString(),statisticsMineBlock.get(block));
			}
			
			// Craft Item
			for(Material item : statisticsCraftItem.keySet())
			{
				objStatCraftItem.addProperty(item.toString(),statisticsCraftItem.get(item));
			}
			
			// Use Item
			for(Material item : statisticsUseItem.keySet())
			{
				objStatUseItem.addProperty(item.toString(),statisticsUseItem.get(item));
			}
			
			// Break Item
			for(Material item : statisticsBreakItem.keySet())
			{
				objStatBreakItem.addProperty(item.toString(),statisticsBreakItem.get(item));
			}
			
			// Pickup
			for(Material item : statisticsPickup.keySet())
			{
				objStatPickup.addProperty(item.toString(),statisticsPickup.get(item));
			}
			
			// Drop
			for(Material item : statisticsDrop.keySet())
			{
				objStatDrop.addProperty(item.toString(),statisticsDrop.get(item));
			}
			
			// Kill Entity
			for(EntityType entity : statisticsKillEntity.keySet())
			{
				objStatKillEntity.addProperty(entity.toString(),statisticsKillEntity.get(entity));
			}
			
			// Entity Killed By
			for(EntityType entity : statisticsEntityKilledBy.keySet())
			{
				objStatEntityKilledBy.addProperty(entity.toString(),statisticsEntityKilledBy.get(entity));
			}
			
			objStats.add(Statistic.MINE_BLOCK.toString(),objStatMineBlock);
			objStats.add(Statistic.CRAFT_ITEM.toString(),objStatCraftItem);
			objStats.add(Statistic.USE_ITEM.toString(),objStatUseItem);
			objStats.add(Statistic.BREAK_ITEM.toString(),objStatBreakItem);
			objStats.add(Statistic.PICKUP.toString(),objStatPickup);
			objStats.add(Statistic.DROP.toString(),objStatDrop);
			objStats.add(Statistic.KILL_ENTITY.toString(),objStatKillEntity);
			objStats.add(Statistic.ENTITY_KILLED_BY.toString(),objStatEntityKilledBy);
			
			obj.add(STATISTICS,objStats);
		}
		
		return obj;
	}

	@Override
	public void deserialise(JsonObject obj) throws DeserialiseException
	{
		location = Deserialise.location(Deserialise.assertAndGetProperty(LOCATION,Deserialise.Type.JSON_OBJECT,obj).getAsJsonObject());
		expLevel = Deserialise.assertAndGetProperty(EXP_LEVEL,Deserialise.Type.NUMBER,obj).getAsInt();
		expProgress = Deserialise.assertAndGetProperty(EXP_PROGRESS,Deserialise.Type.NUMBER,obj).getAsFloat();
		allowFlight = Deserialise.assertAndGetProperty(ALLOW_FLIGHT,Deserialise.Type.BOOLEAN,obj).getAsBoolean();
		flySpeed = Deserialise.assertAndGetProperty(FLY_SPEED,Deserialise.Type.NUMBER,obj).getAsFloat();
		isHealthScaled = Deserialise.assertAndGetProperty(IS_HEALTH_SCALED,Deserialise.Type.BOOLEAN,obj).getAsBoolean();
		healthScale = Deserialise.assertAndGetProperty(HEALTH_SCALE,Deserialise.Type.NUMBER,obj).getAsDouble();
		health = Deserialise.assertAndGetProperty(HEALTH,Deserialise.Type.NUMBER,obj).getAsDouble();
		exhaustion = Deserialise.assertAndGetProperty(EXHAUSTION,Deserialise.Type.NUMBER,obj).getAsFloat();
		starvationRate = Deserialise.assertAndGetProperty(STARVATION_RATE,Deserialise.Type.NUMBER,obj).getAsInt();
		foodLevel = Deserialise.assertAndGetProperty(FOOD_LEVEL,Deserialise.Type.NUMBER,obj).getAsInt();
		locale = Deserialise.assertAndGetProperty(LOCALE,Deserialise.Type.STRING,obj).getAsString();
		
		if(obj.has(RESPAWN_LOCATION))
		{
			respawnLocation =  Deserialise.location(Deserialise.assertAndGetProperty(RESPAWN_LOCATION,Deserialise.Type.JSON_OBJECT,obj).getAsJsonObject());
		}
		
		walkSpeed = Deserialise.assertAndGetProperty(WALK_SPEED,Deserialise.Type.NUMBER,obj).getAsFloat();
		fireTicks = Deserialise.assertAndGetProperty(FIRE_TICKS,Deserialise.Type.NUMBER,obj).getAsInt();
		fallDistance = Deserialise.assertAndGetProperty(FALL_DISTANCE,Deserialise.Type.NUMBER,obj).getAsFloat();
		isGliding = Deserialise.assertAndGetProperty(IS_GLIDING,Deserialise.Type.BOOLEAN,obj).getAsBoolean();
		isGlowing = Deserialise.assertAndGetProperty(IS_GLOWING,Deserialise.Type.BOOLEAN,obj).getAsBoolean();
		freezeTicks = Deserialise.assertAndGetProperty(FREEZE_TICKS,Deserialise.Type.NUMBER,obj).getAsInt();
		maxNoDamageTicks = Deserialise.assertAndGetProperty(MAX_NO_DAMAGE_TICKS,Deserialise.Type.NUMBER,obj).getAsInt();
		noDamageTicks = Deserialise.assertAndGetProperty(NO_DAMAGE_TICKS,Deserialise.Type.NUMBER,obj).getAsInt();
		isSwimming = Deserialise.assertAndGetProperty(IS_SWIMMING,Deserialise.Type.BOOLEAN,obj).getAsBoolean();
		velocity = Deserialise.vector(Deserialise.assertAndGetProperty(VELOCITY,Deserialise.Type.JSON_OBJECT,obj).getAsJsonObject());
		hasVisualFire = Deserialise.assertAndGetProperty(HAS_VISUAL_FIRE,Deserialise.Type.BOOLEAN,obj).getAsBoolean();
		maxAir = Deserialise.assertAndGetProperty(MAX_AIR,Deserialise.Type.NUMBER,obj).getAsInt();
		remainingAir = Deserialise.assertAndGetProperty(REMAINING_AIR,Deserialise.Type.NUMBER,obj).getAsInt();
		potionEffects = Deserialise.potionEffects(Deserialise.assertAndGetProperty(POTION_EFFECTS,Deserialise.Type.JSON_ARRAY,obj).getAsJsonArray());
		
		if(Dape.getConfigFile().getBoolean("player_data.save_advancements"))
		{
			advancements = Deserialise.advancements(Deserialise.assertAndGetProperty(ADVANCEMENTS,Deserialise.Type.JSON_ARRAY,obj).getAsJsonArray());
		}
		
		attributes = Deserialise.attributes(Deserialise.assertAndGetProperty(ATTRIBUTES,Deserialise.Type.JSON_ARRAY,obj).getAsJsonArray());
		
		if(Dape.getConfigFile().getBoolean("player_data.save_statistics"))
		{
			JsonObject objStats = Deserialise.assertAndGetProperty(STATISTICS,Deserialise.Type.JSON_OBJECT,obj).getAsJsonObject();
			
			JsonObject objStatMineBlock = Deserialise.assertAndGetProperty(Statistic.MINE_BLOCK.toString(),Deserialise.Type.JSON_OBJECT,objStats).getAsJsonObject();
			JsonObject objStatCraftItem = Deserialise.assertAndGetProperty(Statistic.CRAFT_ITEM.toString(),Deserialise.Type.JSON_OBJECT,objStats).getAsJsonObject();
			JsonObject objStatUseItem = Deserialise.assertAndGetProperty(Statistic.USE_ITEM.toString(),Deserialise.Type.JSON_OBJECT,objStats).getAsJsonObject();
			JsonObject objStatBreakItem = Deserialise.assertAndGetProperty(Statistic.BREAK_ITEM.toString(),Deserialise.Type.JSON_OBJECT,objStats).getAsJsonObject();
			JsonObject objStatPickup = Deserialise.assertAndGetProperty(Statistic.PICKUP.toString(),Deserialise.Type.JSON_OBJECT,objStats).getAsJsonObject();
			JsonObject objStatDrop = Deserialise.assertAndGetProperty(Statistic.DROP.toString(),Deserialise.Type.JSON_OBJECT,objStats).getAsJsonObject();
			JsonObject objStatKillEntity = Deserialise.assertAndGetProperty(Statistic.KILL_ENTITY.toString(),Deserialise.Type.JSON_OBJECT,objStats).getAsJsonObject();
			JsonObject objStatEntityKilledBy = Deserialise.assertAndGetProperty(Statistic.ENTITY_KILLED_BY.toString(),Deserialise.Type.JSON_OBJECT,objStats).getAsJsonObject();
		

			for(Entry<String,JsonElement> statEntry : objStats.entrySet())
			{
				Statistic stat = Deserialise.assertEnum(statEntry.getKey(),Statistic.class);
				
				switch(stat)
				{
					case MINE_BLOCK:
					case CRAFT_ITEM:
					case USE_ITEM:
					case BREAK_ITEM:
					case PICKUP:
					case DROP:
					case KILL_ENTITY:
					case ENTITY_KILLED_BY:
					{
						continue;
					}
					default: {}
				}
				
				Deserialise.assertType(Deserialise.Type.NUMBER,statEntry.getValue());
				statistics.put(stat,statEntry.getValue().getAsInt());
			}
			
			// Mine Block
			for(Entry<String,JsonElement> statEntry : objStatMineBlock.entrySet())
			{
				Material block = Deserialise.assertEnum(statEntry.getKey(),Material.class);
				Deserialise.assertType(Deserialise.Type.NUMBER,statEntry.getValue());
				statisticsMineBlock.put(block,statEntry.getValue().getAsInt());
			}
			
			// Craft Item
			for(Entry<String,JsonElement> statEntry : objStatCraftItem.entrySet())
			{
				Material item = Deserialise.assertEnum(statEntry.getKey(),Material.class);
				Deserialise.assertType(Deserialise.Type.NUMBER,statEntry.getValue());
				statisticsCraftItem.put(item,statEntry.getValue().getAsInt());
			}
			
			// Use Item
			for(Entry<String,JsonElement> statEntry : objStatUseItem.entrySet())
			{
				Material item = Deserialise.assertEnum(statEntry.getKey(),Material.class);
				Deserialise.assertType(Deserialise.Type.NUMBER,statEntry.getValue());
				statisticsUseItem.put(item,statEntry.getValue().getAsInt());
			}
			
			// Break Item
			for(Entry<String,JsonElement> statEntry : objStatBreakItem.entrySet())
			{
				Material item = Deserialise.assertEnum(statEntry.getKey(),Material.class);
				Deserialise.assertType(Deserialise.Type.NUMBER,statEntry.getValue());
				statisticsBreakItem.put(item,statEntry.getValue().getAsInt());
			}
			
			// Pickup
			for(Entry<String,JsonElement> statEntry : objStatPickup.entrySet())
			{
				Material item = Deserialise.assertEnum(statEntry.getKey(),Material.class);
				Deserialise.assertType(Deserialise.Type.NUMBER,statEntry.getValue());
				statisticsPickup.put(item,statEntry.getValue().getAsInt());
			}
			
			// Drop
			for(Entry<String,JsonElement> statEntry : objStatDrop.entrySet())
			{
				Material item = Deserialise.assertEnum(statEntry.getKey(),Material.class);
				Deserialise.assertType(Deserialise.Type.NUMBER,statEntry.getValue());
				statisticsDrop.put(item,statEntry.getValue().getAsInt());
			}
			
			// Drop
			for(Entry<String,JsonElement> statEntry : objStatKillEntity.entrySet())
			{
				EntityType entity = Deserialise.assertEnum(statEntry.getKey(),EntityType.class);
				Deserialise.assertType(Deserialise.Type.NUMBER,statEntry.getValue());
				statisticsKillEntity.put(entity,statEntry.getValue().getAsInt());
			}
			
			// Drop
			for(Entry<String,JsonElement> statEntry : objStatEntityKilledBy.entrySet())
			{
				EntityType entity = Deserialise.assertEnum(statEntry.getKey(),EntityType.class);
				Deserialise.assertType(Deserialise.Type.NUMBER,statEntry.getValue());
				statisticsEntityKilledBy.put(entity,statEntry.getValue().getAsInt());
			}
		}
	}
}