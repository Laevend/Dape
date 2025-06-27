package coffee.dape.utils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;

public class AttributeUtils
{
	// Attribute base values for a Player according to https://minecraft.wiki/w/Attribute
	private static Map<String,Double> attributeBaseValues = new HashMap<>();
	
	// As of 1.21.5, While Spigot does provide implementation for retrieving a default value for an attribute, you can only obtain this through
	// an AttributeInstance which requires an online 'Player'. We'd like to be able to do this without the presence of a player
	public static void collectDefaults()
	{
		Map<NamespacedKey,Attribute> attributeCache = getCraftbukkitRegistryCache();
		
		// In future updates, this will flag if new attributes were added and we need to manually add new base values.
		for(Entry<NamespacedKey,Attribute> entry : attributeCache.entrySet())
		{
			String attributeName = entry.getKey().getKey();
			
			switch(attributeName)
			{
				case MAX_HEALTH -> attributeBaseValues.put(attributeName,20d);
				
				
				case FOLLOW_RANGE -> attributeBaseValues.put(attributeName,32d);

				
				case KNOCKBACK_RESISTANCE -> attributeBaseValues.put(attributeName,0d);

				
				case MOVEMENT_SPEED -> attributeBaseValues.put(attributeName,0.1d);

				
				case FLYING_SPEED -> attributeBaseValues.put(attributeName,0.4d);

				
				case ATTACK_DAMAGE -> attributeBaseValues.put(attributeName,0.1d);

				
				case ATTACK_KNOCKBACK -> attributeBaseValues.put(attributeName,0d);

				
				case ATTACK_SPEED -> attributeBaseValues.put(attributeName,4.0d);

				
				case ARMOR -> attributeBaseValues.put(attributeName,0d);

				
				case ARMOR_TOUGHNESS -> attributeBaseValues.put(attributeName,0d);

				
				case FALL_DAMAGE_MULTIPLIER -> attributeBaseValues.put(attributeName,1d);

				
				case LUCK -> attributeBaseValues.put(attributeName,0d);

				
				case MAX_ABSORPTION -> attributeBaseValues.put(attributeName,0d);

				
				case SAFE_FALL_DISTANCE -> attributeBaseValues.put(attributeName,3d);

				
				case SCALE -> attributeBaseValues.put(attributeName,1d);

				
				case STEP_HEIGHT -> attributeBaseValues.put(attributeName,0.6d);

				
				case GRAVITY -> attributeBaseValues.put(attributeName,0.08d);

				
				case JUMP_STRENGTH -> attributeBaseValues.put(attributeName,0.42d);

				
				case BURNING_TIME -> attributeBaseValues.put(attributeName,1d);

				
				case EXPLOSION_KNOCKBACK_RESISTANCE -> attributeBaseValues.put(attributeName,0d);

				
				case MOVEMENT_EFFICIENCY -> attributeBaseValues.put(attributeName,0d);

				
				case OXYGEN_BONUS -> attributeBaseValues.put(attributeName,0d);

				
				case WATER_MOVEMENT_EFFICIENCY -> attributeBaseValues.put(attributeName,0d);

				
				case TEMPT_RANGE -> attributeBaseValues.put(attributeName,10d);
				
				
				case BLOCK_INTERACTION_RANGE -> attributeBaseValues.put(attributeName,4.5d);
				
				
				case ENTITY_INTERACTION_RANGE -> attributeBaseValues.put(attributeName,3d);
				
				
				case BLOCK_BREAK_SPEED -> attributeBaseValues.put(attributeName,1d);
				
				
				case MINING_EFFICIENCY -> attributeBaseValues.put(attributeName,0d);
				
				
				case SNEAKING_SPEED -> attributeBaseValues.put(attributeName,0.3d);
				
				
				case SUBMERGED_MINING_SPEED -> attributeBaseValues.put(attributeName,0.2d);
				
				
				case SWEEPING_DAMAGE_RATIO -> attributeBaseValues.put(attributeName,0d);
				
				
				case SPAWN_REINFORCEMENTS -> attributeBaseValues.put(attributeName,0d);
				
				
				default -> Logg.warn("Attribute " + attributeName + " has no base value!");			
			}
		}
	}
	
	/**
	 * Gets the attribute registry cache
	 * @return
	 */
	public static Map<NamespacedKey,Attribute> getCraftbukkitRegistryCache()
	{
		Registry<Attribute> registry = Registry.ATTRIBUTE;
		
		try
		{
			Field mapField = Registry.class.getDeclaredField("cache");
			@SuppressWarnings("unchecked")
			Map<NamespacedKey,Attribute> cache = (Map<NamespacedKey, Attribute>) mapField.get(registry);
			return cache;
		}
		catch(Exception e)
		{
			Logg.error("Could not retrieve attribute cache map!",e);
		}
		
		return Collections.emptyMap();
	}
	
	/**
	 * Gets the default base value for a player attribute without requiring the player to be online
	 * @param attributeName Attribute name
	 * @return Base value for the attribute name provided or -1d if the attribute is not real or no default value is set for it
	 */
	public static double getDefaultValue(String attributeName)
	{
		Objects.requireNonNull(attributeName,"Attribute name cannot be null!");
		
		Attribute att = Registry.ATTRIBUTE.getOrThrow(NamespacedKey.minecraft(attributeName));
		if(att == null) { return -1d; }
		
		if(!attributeBaseValues.containsKey(attributeName))
		{
			Logg.error("Attribute '" + attributeName + "' is a real attribute but has no default value set for it!");
			return -1d;
		}
		
		return attributeBaseValues.get(attributeName);
	}
	
    /**
     * Maximum health of an Entity.
     */
    public static final String MAX_HEALTH = "max_health";
    /**
     * Range at which an Entity will follow others.
     */
    public static final String FOLLOW_RANGE = "follow_range";
    /**
     * Resistance of an Entity to knockback.
     */
    public static final String KNOCKBACK_RESISTANCE = "knockback_resistance";
    /**
     * Movement speed of an Entity.
     */
    public static final String MOVEMENT_SPEED = "movement_speed";
    /**
     * Flying speed of an Entity.
     */
    public static final String FLYING_SPEED = "flying_speed";
    /**
     * Attack damage of an Entity.
     */
    public static final String ATTACK_DAMAGE = "attack_damage";
    /**
     * Attack knockback of an Entity.
     */
    public static final String ATTACK_KNOCKBACK = "attack_knockback";
    /**
     * Attack speed of an Entity.
     */
    public static final String ATTACK_SPEED = "attack_speed";
    /**
     * Armor bonus of an Entity.
     */
    public static final String ARMOR = "armor";
    /**
     * Armor durability bonus of an Entity.
     */
    public static final String ARMOR_TOUGHNESS = "armor_toughness";
    /**
     * The fall damage multiplier of an Entity.
     */
    public static final String FALL_DAMAGE_MULTIPLIER = "fall_damage_multiplier";
    /**
     * Luck bonus of an Entity.
     */
    public static final String LUCK = "luck";
    /**
     * Maximum absorption of an Entity.
     */
    public static final String MAX_ABSORPTION = "max_absorption";
    /**
     * The distance which an Entity can fall without damage.
     */
    public static final String SAFE_FALL_DISTANCE = "safe_fall_distance";
    /**
     * The relative scale of an Entity.
     */
    public static final String SCALE = "scale";
    /**
     * The height which an Entity can walk over.
     */
    public static final String STEP_HEIGHT = "step_height";
    /**
     * The gravity applied to an Entity.
     */
    public static final String GRAVITY = "gravity";
    /**
     * Strength with which an Entity will jump.
     */
    public static final String JUMP_STRENGTH = "jump_strength";
    /**
     * How long an entity remains burning after ignition.
     */
    public static final String BURNING_TIME = "burning_time";
    /**
     * Resistance to knockback from explosions.
     */
    public static final String EXPLOSION_KNOCKBACK_RESISTANCE = "explosion_knockback_resistance";
    /**
     * Movement speed through difficult terrain.
     */
    public static final String MOVEMENT_EFFICIENCY = "movement_efficiency";
    /**
     * Oxygen use underwater.
     */
    public static final String OXYGEN_BONUS = "oxygen_bonus";
    /**
     * Movement speed through water.
     */
    public static final String WATER_MOVEMENT_EFFICIENCY = "water_movement_efficiency";
    /**
     * Range at which mobs will be tempted by items.
     */
    public static final String TEMPT_RANGE = "tempt_range";
    /**
     * The block reach distance of a Player.
     */
    public static final String BLOCK_INTERACTION_RANGE = "block_interaction_range";
    /**
     * The entity reach distance of a Player.
     */
    public static final String ENTITY_INTERACTION_RANGE = "entity_interaction_range";
    /**
     * Block break speed of a Player.
     */
    public static final String BLOCK_BREAK_SPEED = "block_break_speed";
    /**
     * Mining speed for correct tools.
     */
    public static final String MINING_EFFICIENCY = "mining_efficiency";
    /**
     * Sneaking speed.
     */
    public static final String SNEAKING_SPEED = "sneaking_speed";
    /**
     * Underwater mining speed.
     */
    public static final String SUBMERGED_MINING_SPEED = "submerged_mining_speed";
    /**
     * Sweeping damage.
     */
    public static final String SWEEPING_DAMAGE_RATIO = "sweeping_damage_ratio";
    /**
     * Chance of a zombie to spawn reinforcements.
     */
    public static final String SPAWN_REINFORCEMENTS = "spawn_reinforcements";
}
