package coffee.dape.utils.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import coffee.dape.utils.ItemUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.WorldUtils;
import coffee.dape.utils.structs.Pair;

/**
 * @author Laeven
 * A convenient class to deserialise common objects from their serialised forms back into their object forms
 */
public class Deserialise
{
	/**
	 * Deserialise a vector
	 * @param vector JsonObject representing a vector
	 * @return Vector
	 */
	public static Vector vector(JsonObject vector)
	{
		Objects.requireNonNull(vector,"JsonObject of vector cannot be null!");
		
		assertProperty("x",Deserialise.Type.NUMBER,vector);
		assertProperty("y",Deserialise.Type.NUMBER,vector);
		assertProperty("z",Deserialise.Type.NUMBER,vector);
		
		return new Vector(
				vector.get("x").getAsDouble(),
				vector.get("y").getAsDouble(),
				vector.get("z").getAsDouble());
	}
	
	/**
	 * Deserialise a location
	 * @param location JsonObject representing a location
	 * @return Location
	 */
	public static Location location(JsonObject location)
	{
		Objects.requireNonNull(location,"JsonObject of location cannot be null!");
		
		assertProperty("x",Deserialise.Type.NUMBER,location);
		assertProperty("y",Deserialise.Type.NUMBER,location);
		assertProperty("z",Deserialise.Type.NUMBER,location);
		assertProperty("pitch",Deserialise.Type.NUMBER,location);
		assertProperty("yaw",Deserialise.Type.NUMBER,location);
		assertProperty("world",Deserialise.Type.STRING,location);
		
		World world = WorldUtils.getWorld(location.get("world").getAsString());
		
		return new Location(
				world,
				location.get("x").getAsDouble(),
				location.get("y").getAsDouble(),
				location.get("z").getAsDouble(),
				location.get("yaw").getAsFloat(),
				location.get("pitch").getAsFloat());
	}
	
	public static final Pattern uuidPattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$",Pattern.CASE_INSENSITIVE);
	
	/**
	 * Deserialise a UUID
	 * @param element JsonElement that is a string representation of a UUID
	 * @return UUID
	 */
	public static UUID uuid(JsonElement element)
	{
		Objects.requireNonNull(element,"JsonElement of uuid cannot be null!");
		
		assertType(Deserialise.Type.STRING,element);
		
		if(!uuidPattern.matcher(element.getAsJsonPrimitive().getAsString()).matches())
		{
			throw new IllegalArgumentException("UUID '" + element.getAsJsonPrimitive().getAsString() + " is not a valid UUID!");
		}
		
		return UUID.fromString(element.getAsJsonPrimitive().getAsString());
	}
	
	/**
	 * Deserialise potion effects
	 * @param potionEffects JsonArray representing potion effects
	 * @return Collection of potion effects
	 */
	public static Collection<PotionEffect> potionEffects(JsonArray potionEffects)
	{
		Objects.requireNonNull(potionEffects,"JsonArray of potionEffects cannot be null!");
		
		Collection<PotionEffect> potionEffs = new ArrayList<>();
		
		for(JsonElement element : potionEffects)
		{
			assertType(Deserialise.Type.JSON_OBJECT,element);
			JsonObject potionObj = (JsonObject) element;
			
			assertProperty("type_namespace",Deserialise.Type.STRING,potionObj);
			assertProperty("type_key",Deserialise.Type.STRING,potionObj);
			
			NamespacedKey key = new NamespacedKey(potionObj.get("type_namespace").getAsString(),potionObj.get("type_key").getAsString());
			
			assertProperty("duration",Deserialise.Type.NUMBER,potionObj);
			assertProperty("amplifier",Deserialise.Type.NUMBER,potionObj);
			assertProperty("is_ambient",Deserialise.Type.BOOLEAN,potionObj);
			assertProperty("has_particles",Deserialise.Type.BOOLEAN,potionObj);
			assertProperty("has_icon",Deserialise.Type.BOOLEAN,potionObj);
			
			potionEffs.add(new PotionEffect(
					Registry.EFFECT.getOrThrow(key),
					potionObj.get("duration").getAsInt(),
					potionObj.get("amplifier").getAsInt(),
					potionObj.get("is_ambient").getAsBoolean(),
					potionObj.get("has_particles").getAsBoolean(),
					potionObj.get("has_icon").getAsBoolean()));
		}
		
		return potionEffs;
	}
	
	/**
	 * Deserialise player advancements
	 * @param advancements JsonArray representing the awarded criteria for each advancement
	 * @return Map of player advancements
	 */
	public static Map<NamespacedKey,Collection<String>> advancements(JsonArray advancements)
	{
		Objects.requireNonNull(advancements,"JsonArray of advancements cannot be null!");
		Map<NamespacedKey,Collection<String>> advancementsMap = new HashMap<>();
		
		for(JsonElement element : advancements)
		{
			assertType(Deserialise.Type.JSON_OBJECT,element);
			JsonObject obj = (JsonObject) element;
			
			assertProperty("namespace",Deserialise.Type.STRING,obj);
			assertProperty("key",Deserialise.Type.STRING,obj);
			
			NamespacedKey key = new NamespacedKey(obj.get("namespace").getAsString(),obj.get("key").getAsString());
			JsonElement arrayElement = obj.get("criteria");
			assertType(Deserialise.Type.JSON_ARRAY,arrayElement);
			JsonArray criteriaArr = arrayElement.getAsJsonArray();
			
			Collection<String> awardedCriteria = new ArrayList<>();
			
			for(Iterator<JsonElement> it = criteriaArr.iterator(); it.hasNext();)
			{
				JsonElement itElement = it.next();
				assertType(Deserialise.Type.STRING,itElement);
				
				awardedCriteria.add(itElement.getAsString());
			}
			
			advancementsMap.put(key,awardedCriteria);
		}
		
		return advancementsMap;
	}
	
	/**
	 * Deserialise attributes
	 * @param attributes JsonArray representing attributes
	 * @return Map of attributes
	 */
	public static Map<String,Pair<Double,Collection<AttributeModifier>>> attributes(JsonArray attributes)
	{
		Objects.requireNonNull(attributes,"JsonArray of attributes cannot be null!");
		Map<String,Pair<Double,Collection<AttributeModifier>>> advancementsMap = new HashMap<>();
		
		for(JsonElement element : attributes)
		{
			assertType(Deserialise.Type.JSON_OBJECT,element);
			JsonObject pairObj = (JsonObject) element;
			
			assertProperty("key",Deserialise.Type.STRING,pairObj);
			assertProperty("base_value",Deserialise.Type.NUMBER,pairObj);
			
			JsonElement arrayElement = pairObj.get("modifiers");
			assertType(Deserialise.Type.JSON_ARRAY,arrayElement);
			JsonArray modsArr = arrayElement.getAsJsonArray();
			
			Collection<AttributeModifier> mods = new ArrayList<>();
			
			for(Iterator<JsonElement> it = modsArr.iterator(); it.hasNext();)
			{
				JsonElement itElement = it.next();
				assertType(Deserialise.Type.JSON_OBJECT,itElement);
				JsonObject modObj = (JsonObject) element;
				
				assertProperty("namespace",Deserialise.Type.STRING,modObj);
				assertProperty("key",Deserialise.Type.STRING,modObj);
				
				NamespacedKey nameKey = new NamespacedKey(modObj.get("namespace").getAsString(),modObj.get("key").getAsString());
				
				assertProperty("operation",Deserialise.Type.STRING,modObj);
				assertProperty("amount",Deserialise.Type.NUMBER,modObj);
				
				Operation op = assertEnum(modObj.get("operation").getAsString(),Operation.class);
				
				if(modObj.has("equipment_group"))
				{
					assertProperty("equipment_group",Deserialise.Type.STRING,modObj);
					EquipmentSlotGroup group = EquipmentSlotGroup.getByName(modObj.get("equipment_group").getAsString());
					
					if(group == null)
					{
						throw new IllegalArgumentException("EquipmentSlotGroup '" + modObj.get("equipment_group").getAsString() + " is not a valid EquipmentSlotGroup!");
					}
					
					mods.add(new AttributeModifier(nameKey,modObj.get("amount").getAsDouble(),op,group));
				}
				else
				{
					mods.add(new AttributeModifier(nameKey,modObj.get("amount").getAsDouble(),op,EquipmentSlotGroup.ANY));
				}
			}
			
			advancementsMap.put(
					pairObj.get("key").getAsString(),
					new Pair<>(pairObj.get("base_value").getAsDouble(),mods));
		}
		
		return advancementsMap;
	}	

	public Inventory inventory(JsonObject inventory)
	{
		Objects.requireNonNull(inventory,"JsonObject of inventory cannot be null!");
		
		assertProperty("inventory_type",Deserialise.Type.STRING,inventory);
		assertProperty("inventory_size",Deserialise.Type.NUMBER,inventory);
		assertEnum(inventory.get("inventory_type").getAsString(),InventoryType.class);
		
		InventoryType type = InventoryType.valueOf(inventory.remove("inventory_type").getAsString());
		int invSize = inventory.remove("inventory_size").getAsInt();
		Inventory inv;
		
		if(type == InventoryType.CHEST)
		{
			inv = Bukkit.createInventory(null,invSize);
		}
		else
		{
			inv = Bukkit.createInventory(null,type);
		}
		
		for(Entry<String,JsonElement> entry : inventory.entrySet())
		{
			if(!entry.getKey().matches("\\d+"))
			{
				throw new IllegalArgumentException("Inventory could not load a slot. Slot '" + entry.getKey() + "' is not a real slot number");
			}
			
			ItemStack stack = ItemUtils.fromBase64(entry.getValue().getAsString());
			
			if(ItemUtils.isErrorItemStack(stack))
			{
				throw new IllegalArgumentException("Inventory could not load slot '" + entry.getKey() + "'. ItemStack could not be decoded!");
			}
			
			inv.setItem(Integer.parseInt(entry.getKey()),stack);
		}
		
		return inv;
	}
	
	/**
	 * Gets a value if it exists in the JsonObject or returns the default value
	 * @param key Key of Json property
	 * @param obj JsonObject to check
	 * @param defaultValue Value to return should the object not contain the property
	 * @return JsonElement
	 */
	public static JsonElement getOrDefault(String key,JsonObject obj,String defaultValue)
	{
		if(obj.has(key)) { return obj.get(key); }
		return JsonParser.parseString("{\"default_value\": \"" + defaultValue + "\"}").getAsJsonObject().get("default_value");
	}
	
	/**
	 * Gets a value if it exists in the JsonObject or returns the default value
	 * @param key Key of Json property
	 * @param obj JsonObject to check
	 * @param defaultValue Value to return should the object not contain the property
	 * @return JsonElement
	 */
	public static JsonElement getOrDefault(String key,JsonObject obj,boolean defaultValue)
	{
		if(obj.has(key)) { return obj.get(key); }
		return JsonParser.parseString("{\"default_value\": " + defaultValue + "}").getAsJsonObject().get("default_value");
	}
	
	/**
	 * Gets a value if it exists in the JsonObject or returns the default value
	 * @param key Key of Json property
	 * @param obj JsonObject to check
	 * @param defaultValue Value to return should the object not contain the property
	 * @return JsonElement
	 */
	public static JsonElement getOrDefault(String key,JsonObject obj,Number defaultValue)
	{
		if(obj.has(key)) { return obj.get(key); }
		return JsonParser.parseString("{\"default_value\": " + defaultValue + "}").getAsJsonObject().get("default_value");
	}
	
	/**
	 * Asserts an enum is part of an enum type
	 * @param <E> Enum
	 * @param enumString String representation of the enum
	 * @param enumType Enum class
	 * @return 
	 */
	public static <E extends Enum<E>> E assertEnum(String enumString,Class<E> enumType)
	{
		String value = enumString.toUpperCase().replaceAll("\\s+","_");
		
		for(E option : enumType.getEnumConstants())
		{
			if(value.equals(option.toString())) { return option; }
		}
		
		throw new IllegalArgumentException("Enum of value '" + enumString + "' is not a member of enum type " + enumType.getSimpleName() + "!");
	}
	
	/**
	 * Asserts the existence of and the type of a Json element
	 * @param <T> JsonType
	 * @param key Key of Json property
	 * @param type Type (Deserialise.Type)
	 * @param obj JsonObject to check
	 */
	public static <T> void assertProperty(String key,Type<T> type,JsonObject obj)
	{
		if(!obj.has(key))
		{
			throw new IllegalArgumentException("Json property '" + key + "' is missing!");
		}
		assertType(type,obj.get(key));
	}
	
	/**
	 * Asserts the type of a property if it exists
	 * @param <T> JsonType
	 * @param key Key of Json property
	 * @param type Type (Deserialise.Type)
	 * @param obj JsonObject to check
	 */
	public static <T> void assertPropertyIfHas(String key,Type<T> type,JsonObject obj)
	{
		if(!obj.has(key)) { return; }
		assertType(type,obj.get(key));
	}
	
	/**
	 * Asserts the existence of and the type of a Json element
	 * @param <T> JsonType
	 * @param key Key of Json property
	 * @param type Type (Deserialise.Type)
	 * @param obj JsonObject to check
	 * @return 
	 */
	public static <T> JsonElement assertAndGetProperty(String key,Type<T> type,JsonObject obj)
	{
		if(!obj.has(key))
		{
			throw new IllegalArgumentException("Json property '" + key + "' is missing!");
		}
		assertType(type,obj.get(key));
		return obj.get(key);
	}
	
	/**
	 * Asserts the type of a property if it exists
	 * @param <T> JsonType
	 * @param key Key of Json property
	 * @param type Type (Deserialise.Type)
	 * @param obj JsonObject to check
	 * @return 
	 */
	public static <T> Number assertAndGetPropertyIfHas(String key,Type<T> type,JsonObject obj,Number defaultValue)
	{
		if(!obj.has(key)) { return defaultValue; }
		assertType(type,obj.get(key));
		return obj.get(key).getAsNumber();
	}
	
	/**
	 * Asserts the type of a property if it exists
	 * @param <T> JsonType
	 * @param key Key of Json property
	 * @param type Type (Deserialise.Type)
	 * @param obj JsonObject to check
	 * @return 
	 */
	public static <T> Boolean assertAndGetPropertyIfHas(String key,Type<T> type,JsonObject obj,Boolean defaultValue)
	{
		if(!obj.has(key)) { return defaultValue; }
		assertType(type,obj.get(key));
		return obj.get(key).getAsBoolean();
	}
	
	/**
	 * Asserts the type of a property if it exists
	 * @param <T> JsonType
	 * @param key Key of Json property
	 * @param type Type (Deserialise.Type)
	 * @param obj JsonObject to check
	 * @return 
	 */
	public static <T> String assertAndGetPropertyIfHas(String key,Type<T> type,JsonObject obj,String defaultValue)
	{
		if(!obj.has(key)) { return defaultValue; }
		assertType(type,obj.get(key));
		return obj.get(key).getAsString();
	}
	
	/**
	 * Asserts the type of a property if it exists
	 * @param <T> JsonType
	 * @param key Key of Json property
	 * @param type Type (Deserialise.Type)
	 * @param obj JsonObject to check
	 * @return 
	 */
	public static <T> JsonObject assertAndGetPropertyIfHas(String key,Type<T> type,JsonObject obj,JsonObject defaultValue)
	{
		if(!obj.has(key)) { return defaultValue; }
		assertType(type,obj.get(key));
		return obj.get(key).getAsJsonObject();
	}
	
	/**
	 * Asserts the type of a property if it exists
	 * @param <T> JsonType
	 * @param key Key of Json property
	 * @param type Type (Deserialise.Type)
	 * @param obj JsonObject to check
	 * @return 
	 */
	public static <T> JsonArray assertAndGetPropertyIfHas(String key,Type<T> type,JsonObject obj,JsonArray defaultValue)
	{
		if(!obj.has(key)) { return defaultValue; }
		assertType(type,obj.get(key));
		return obj.get(key).getAsJsonArray();
	}
	
	/**
	 * Asserts the type of a JsonElement
	 * @param <T> JsonType
	 * @param type Type (Deserialise.Type)
	 * @param element JsonElement to check
	 */
	public static <T> void assertType(Type<T> type,JsonElement element)
	{
		if(element.isJsonNull())
		{
			throwAssert(element,type.type);
			return;
		}
		
		if(element.isJsonPrimitive())
		{
			if(element.getAsJsonPrimitive().isBoolean()) {if(!type.type.equals(Boolean.class.getSimpleName())) { throwAssert(element,type.type); } else {return;}}
			if(element.getAsJsonPrimitive().isString()) {if(!type.type.equals(String.class.getSimpleName())) { throwAssert(element,type.type); } else {return;}}
			if(element.getAsJsonPrimitive().isNumber()) {if(!type.type.equals(Number.class.getSimpleName())) { throwAssert(element,type.type); } else {return;}}
		}
		
		if(element.isJsonArray())
		{
			if(!type.type.equals(JsonArray.class.getSimpleName())) { throwAssert(element,type.type); }
			return;
		}
		
		if(element.isJsonObject())
		{
			if(!type.type.equals(JsonObject.class.getSimpleName())) { throwAssert(element,type.type); }
			return;
		}
		
		Logg.fatal("Unknown JsonElement!");
		throwAssert(element,type.type);
	}
	
	/**
	 * Throws an assert exception when an element is of an incorrect type
	 * @param element JsonElement
	 * @param type Type that it was expected to be
	 */
	private static void throwAssert(JsonElement element,String type)
	{
		throw new IllegalArgumentException("JsonElement '" + element.toString() + "' was different than expected type " + type + "!");
	}
	
	/**
	 * Class of JsonTypes
	 * @param <T>
	 */
	public static class Type<T>
	{
		public static final Type<Number> NUMBER = new Type<>(Number.class.getSimpleName());
		public static final Type<Boolean> BOOLEAN = new Type<>(Boolean.class.getSimpleName());
		public static final Type<String> STRING = new Type<>(String.class.getSimpleName());
		public static final Type<JsonArray> JSON_ARRAY = new Type<>(JsonArray.class.getSimpleName());
		public static final Type<JsonObject> JSON_OBJECT = new Type<>(JsonObject.class.getSimpleName());
		
		public final String type;
		
		private Type(String type)
		{
			this.type = type;
		}

		public String getType()
		{
			return type;
		}
	}
}
