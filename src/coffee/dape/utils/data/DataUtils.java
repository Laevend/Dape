package coffee.dape.utils.data;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import coffee.dape.Dape;
import coffee.dape.utils.Logg;

public class DataUtils
{
	private static NamespacedKey dataKey = new NamespacedKey(Dape.instance(),"JSON_DATA_MAP");
	private static Gson gsonPretty;
	private static Gson gson;
	private static final Set<Class<?>> PRIMITIVE_ARRAY_TYPES = Set.of
	(
		int[].class,
		float[].class,
		double[].class,
		boolean[].class,
		byte[].class,
		short[].class,
		long[].class,
		char[].class
	);
	private static final Set<PersistentDataType<?,?>> types = Set.of
	(
		PersistentDataType.BOOLEAN,
		PersistentDataType.BYTE,
		PersistentDataType.BYTE_ARRAY,
		PersistentDataType.DOUBLE,
		PersistentDataType.FLOAT,
		PersistentDataType.INTEGER,
		PersistentDataType.INTEGER_ARRAY,
		PersistentDataType.LONG,
		PersistentDataType.LONG_ARRAY,
		PersistentDataType.SHORT,
		PersistentDataType.STRING
	);
	
	static
	{
		gsonPretty = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	}
	
	/**
	 * Checks if this ItemStack has any persistent data
	 * @param stack ItemStack
	 * @return True if data is present, false otherwise
	 */
	public static boolean hasData(ItemStack stack)
	{
		return hasData(stack.getItemMeta());
	}
	
	/**
	 * Checks if this data holder has any persistent data
	 * @param dataHolder DataHolder
	 * @return True if data is present, false otherwise
	 */
	public static boolean hasData(PersistentDataHolder dataHolder)
	{
		PersistentDataContainer data = dataHolder.getPersistentDataContainer();
		return data.getKeys().size() > 0;
	}
	
	/**
	 * Checks if this ItemStack has a key present
	 * @param key The key used to access the data
	 * @param stack ItemStack
	 * @return True if data is present, false otherwise
	 */
	public static boolean has(String key,ItemStack stack)
	{
		return has(key,stack.getItemMeta());
	}
	
	/**
	 * Checks if this data holder has a key present
	 * @param key The key used to access the data
	 * @param dataHolder DataHolder
	 * @return True if data is present, false otherwise
	 */
	public static boolean has(String key,PersistentDataHolder dataHolder)
	{
		PersistentDataContainer data = dataHolder.getPersistentDataContainer();
		if(!hasData(dataHolder)) { return false; }
		NamespacedKey NSKey = new NamespacedKey(Dape.instance(),key);
		return data.getKeys().contains(NSKey);
	}
	
	/**
	 * Gets a value from the persistent container
	 * @param key The key used to access the data
	 * @param stack ItemStack
	 * @return DataContainerValue
	 */
	public static DataContainerValue get(String key,ItemStack stack)
	{
		return get(key,stack.getItemMeta());
	}
	
	/**
	 * Gets a value from the persistent container
	 * @param key The key used to access the data
	 * @param dataHolder DataHolder
	 * @return DataContainerValue
	 */
	public static DataContainerValue get(String key,PersistentDataHolder dataHolder)
	{
		PersistentDataContainer data = dataHolder.getPersistentDataContainer();
		return new DataContainerValue(key,data);
	}
	
	/**
	 * Gets JSON data container for this data holder
	 * @param dataHolder DataHolder
	 * @return JsonDataContainer
	 */
	private static JsonObject getJContainer(PersistentDataHolder dataHolder)
	{
		PersistentDataContainer container = dataHolder.getPersistentDataContainer();
		if(!hasData(dataHolder)) { return new JsonObject(); }
		
		String dataString = container.getOrDefault(dataKey,PersistentDataType.STRING,"NULL");
		
		if(dataString.equals("NULL")) { return new JsonObject(); }
		
		return JsonParser.parseString(dataString).getAsJsonObject();
	}
	
	/**
	 * Sets a value to the persistent container
	 * @param key The key used to access the data
	 * @param type Type the data should be saved as
	 * @param value Value of this data
	 * @param stack ItemStack
	 */
	public static void set(String key,Object value,ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		setNative(key,value,meta.getPersistentDataContainer());
		stack.setItemMeta(meta);
	}
	
	/**
	 * Sets a value to the persistent container
	 * @param key The key used to access the data
	 * @param type Type the data should be saved as
	 * @param value Value of this data
	 * @param dataHolder DataHolder
	 */
	public static void set(String key,Object value,PersistentDataHolder dataHolder)
	{
		setNative(key,value,dataHolder.getPersistentDataContainer());
	}
	
	/**
	 * Retarded method to natively set the PersistentDataType because YA CAN'T DO IT GENERICALLY... MY ASS
	 * @param key The key used to access the data
	 * @param type Type the data should be saved as
	 * @param value Value of this data
	 * @param container DataContainer
	 */
	private static void setNative(String key,Object value,PersistentDataContainer container)
	{
		NamespacedKey NSKey = new NamespacedKey(Dape.instance(),key);
		Object firstArrayValue = value.getClass().isArray() ? Array.get(value,0) : null;
		
		switch(value)
		{
			case String val -> { container.set(NSKey,PersistentDataType.STRING,val); return; }
			case Integer val -> { container.set(NSKey,PersistentDataType.INTEGER,val); return; }
			case Boolean val -> { container.set(NSKey,PersistentDataType.BOOLEAN,val); return; }
			case Float val -> { container.set(NSKey,PersistentDataType.FLOAT,val); return; }
			case Long val -> { container.set(NSKey,PersistentDataType.LONG,val); return; }
			case Double val -> { container.set(NSKey,PersistentDataType.DOUBLE,val); return; }
			case Byte val -> { container.set(NSKey,PersistentDataType.BYTE,val); return; }
			case Short val -> { container.set(NSKey,PersistentDataType.SHORT,val); return; }
			case JsonObject val -> { container.set(NSKey,PersistentDataType.STRING,gson.toJson(val)); return; }
			default -> {}
		}
		
		if(firstArrayValue == null)
		{
			throw new IllegalArgumentException("Unsupported value: " + value);
		}
		
		switch(firstArrayValue)
		{
			case Integer val -> { container.set(NSKey,PersistentDataType.INTEGER_ARRAY,(int[]) value); return; }
			case Long val -> { container.set(NSKey,PersistentDataType.LONG_ARRAY,(long[]) value); return; }
			case Byte val -> { container.set(NSKey,PersistentDataType.BYTE_ARRAY,(byte[]) value); return; }
			default -> throw new IllegalArgumentException("Unsupported value: " + firstArrayValue);
		}
	}
	
	/**
	 * Sets a value to the JSON data map
	 * @param key The key used to access the data
	 * @param value Value of this data
	 * @param stack ItemStack
	 */
	public static void setJ(String key,String value,ItemStack stack)
	{		
		ItemMeta meta = stack.getItemMeta();
		setJ(key,value,meta);
		stack.setItemMeta(meta);
	}
	
	/**
	 * Sets a value to the JSON data map
	 * @param key The key used to access the data
	 * @param value Value of this data
	 * @param stack ItemStack
	 */
	public static void setJ(String key,Boolean value,ItemStack stack)
	{		
		ItemMeta meta = stack.getItemMeta();
		setJ(key,value,meta);
		stack.setItemMeta(meta);
	}
	
	/**
	 * Sets a value to the JSON data map
	 * @param key The key used to access the data
	 * @param value Value of this data
	 * @param stack ItemStack
	 */
	public static void setJ(String key,Number value,ItemStack stack)
	{		
		ItemMeta meta = stack.getItemMeta();
		setJ(key,value,meta);
		stack.setItemMeta(meta);
	}
	
	/**
	 * Sets a value to the JSON data map
	 * @param key The key used to access the data
	 * @param value Value of this data
	 * @param stack ItemStack
	 */
	public static void setJ(String key,JsonPrimitive value,ItemStack stack)
	{		
		ItemMeta meta = stack.getItemMeta();
		setJ(key,value,meta);
		stack.setItemMeta(meta);
	}
	
	/**
	 * Sets a value to the JSON data map
	 * @param key The key used to access the data
	 * @param value Value of this data
	 * @param stack ItemStack
	 */
	public static void setJ(String key,JsonElement value,ItemStack stack)
	{		
		ItemMeta meta = stack.getItemMeta();
		setJ(key,value,meta);
		stack.setItemMeta(meta);
	}
	
	/**
	 * Sets a value to the JSON data map
	 * @param key The key used to access the data
	 * @param value Value of this data
	 * @param dataHolder DataHolder
	 */
	public static void setJ(String key,String value,PersistentDataHolder dataHolder)
	{
		JsonObject json = getJContainer(dataHolder);
		json.addProperty(key,value);
		setJContainer(json,dataHolder);
	}
	
	/**
	 * Sets a value to the JSON data map
	 * @param key The key used to access the data
	 * @param value Value of this data
	 * @param dataHolder DataHolder
	 */
	public static void setJ(String key,Boolean value,PersistentDataHolder dataHolder)
	{
		JsonObject json = getJContainer(dataHolder);
		json.addProperty(key,value);
		setJContainer(json,dataHolder);
	}
	
	/**
	 * Sets a value to the JSON data map
	 * @param key The key used to access the data
	 * @param value Value of this data
	 * @param dataHolder DataHolder
	 */
	public static void setJ(String key,Number value,PersistentDataHolder dataHolder)
	{
		JsonObject json = getJContainer(dataHolder);
		json.addProperty(key,value);
		setJContainer(json,dataHolder);
	}
	
	/**
	 * Sets a value to the JSON data map
	 * @param key The key used to access the data
	 * @param value Value of this data
	 * @param dataHolder DataHolder
	 */
	public static void setJ(String key,JsonPrimitive value,PersistentDataHolder dataHolder)
	{
		JsonObject json = getJContainer(dataHolder);
		json.add(key,value);
		setJContainer(json,dataHolder);
	}
	
	/**
	 * Sets a value to the JSON data map
	 * @param key The key used to access the data
	 * @param value Value of this data
	 * @param dataHolder DataHolder
	 */
	public static void setJ(String key,JsonElement value,PersistentDataHolder dataHolder)
	{
		JsonObject json = getJContainer(dataHolder);
		json.add(key,value);
		setJContainer(json,dataHolder);
	}
	
	public static void setJContainer(JsonObject data,ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		setJContainer(data,meta);
		stack.setItemMeta(meta);
	}
	
	public static void setJContainer(JsonObject data,PersistentDataHolder dataHolder)
	{
		String dataString = gson.toJson(data);
		dataHolder.getPersistentDataContainer().set(dataKey,PersistentDataType.STRING,dataString);
	}
	
	public static void remove(String key,ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		remove(key,meta);
		stack.setItemMeta(meta);
	}
	
	public static void remove(String key,PersistentDataHolder dataHolder)
	{
		NamespacedKey NSKey = new NamespacedKey(Dape.instance(),key);
		PersistentDataContainer data = dataHolder.getPersistentDataContainer();
		data.remove(NSKey);
	}
	
	/**
	 * Clears all data on the container
	 * @param stack ItemStack
	 */
	public static void clear(ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		clear(meta);
		stack.setItemMeta(meta);
	}
	
	/**
	 * Clears all data on the container
	 * @param dataHolder DataHolder
	 */
	public static void clear(PersistentDataHolder dataHolder)
	{
		PersistentDataContainer data = dataHolder.getPersistentDataContainer();
		Iterator<NamespacedKey> it = data.getKeys().iterator();
		
		while(it.hasNext())
		{
			NamespacedKey key = it.next();
			data.remove(key);
		}
	}
	
	/**
	 * Prints all data in this stacks persistent data container
	 * @param stack ItemStack
	 */
	public static void printData(ItemStack stack)
	{
		printData(stack.getItemMeta());
	}
	
	/**
	 * Prints all data in this data holders persistent data container
	 * @param dataHolder DataHolder
	 */
	public static void printData(PersistentDataHolder dataHolder)
	{
		Logg.info("--- Printing Data Container ---");
		printContainer(dataHolder.getPersistentDataContainer());
		Logg.info("");
		Logg.info("---    Printing Finished    ---");
		Logg.info("");
	}
	
	private static void printContainer(PersistentDataContainer container)
	{
		PersistentDataContainer data = container;
		
		for(NamespacedKey key : data.getKeys())
		{
			for(PersistentDataType<?,?> type : types)
			{
				if(!data.has(key,type)) { continue; }
				
				String displayKey = key.getNamespace() + ":" + key.getKey();
				Object value = data.get(key,type);
				Logg.info("");				
				switch(value)
				{
					case Byte[] val:
					{
						Logg.info("&7{&eNSKey: " + displayKey + "&7}");
						Logg.info("&7{&eType : " + type.getComplexType().getSimpleName() + "&7}");
						Logg.info("&7{&eData : " + format((Byte[]) primToObjArray(data.get(key,type))) + "&7}");
						break;
					}
					case Integer[] val:
					{
						Logg.info("&7{&eNSKey: " + displayKey + "&7}");
						Logg.info("&7{&eType : " + type.getComplexType().getSimpleName() + "&7}");
						Logg.info("&7{&eData : " + format((Integer[]) primToObjArray(data.get(key,type))) + "&7}");
						break;
					}
					case Long[] val:
					{
						Logg.info("&7{&eNSKey: " + displayKey + "&7}");
						Logg.info("&7{&eType : " + type.getComplexType().getSimpleName() + "&7}");
						Logg.info("&7{&eData : " + format((Long[]) primToObjArray(data.get(key,type))) + "&7}");
						break;
					}
					case JsonObject val:
					{
						Logg.info("&7{&eNSKey: " + displayKey + "&7}");
						Logg.info("&7{&eType : " + type.getComplexType().getSimpleName() + "&7}");
						Logg.info("&7{&eData : " + gson.toJson(data.get(key,type)) + "&7}");
						break;
					}
					default:
					{
						Logg.info("&7{&eNSKey: " + displayKey + "&7}");
						Logg.info("&7{&eType : " + type.getComplexType().getSimpleName() + "&7}");
						Logg.info("&7{&eData : " + data.get(key,type).toString() + "&7}");
					}
				}
			}
		}
	}
	
	/**
	 * Prints all data in this stacks JSON data map
	 * @param stack ItemStack
	 */
	public static void printJData(ItemStack stack)
	{
		printJData(stack.getItemMeta());
	}
	
	/**
	 * Prints all data in this data holders JSON data map
	 * @param dataHolder DataHolder
	 */
	public static void printJData(PersistentDataHolder dataHolder)
	{
		Logg.info("--- Printing Json Data Map  ---");
		
		String json = gsonPretty.toJson(getJContainer(dataHolder));
		Logg.info("\n&e" + json);
		
		Logg.info("---    Printing Finished    ---");
		Logg.info("");
	}
	
	/**
	 * Converts a primitive array type to an object array type
	 * @param val Primitive array
	 * @return Object array
	 */
	public static Object[] primToObjArray(Object val)
	{
		Class<?> primitiveClass = val.getClass();
		Object[] output = null;
		
		for(Class<?> primArrayClass : PRIMITIVE_ARRAY_TYPES)
		{
			if(!primitiveClass.isAssignableFrom(primArrayClass)) { continue; }
			
			int len = Array.getLength(val);
			output = new Object[len];
			
			for(int i = 0; i < len; i++)
			{
				output[i] = Array.get(val,i);
			}
			
			break;
		}
		
		if(output == null) { Logg.error("Object passed is not a primitive type!"); }
		
		return output;
	}
	
	/**
	 * Formats an array to print all its values in a clean way
	 * @param <T> Type
	 * @param array The array for format
	 * @return String format of an array
	 */
	public static <T> String format(T[] array)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		
		for(int i = 0; i < array.length; i++)
		{
			sb.append(array[i].toString());
			if((i + 1) < array.length) { sb.append(", "); }
		}
		
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * Formats a collection to print all its values in a clean way
	 * @param <T> Type
	 * @param coll The collection to format
	 * @return String format of a collection
	 */
	public static <T> String format(Collection<T> coll)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		
		for(Iterator<T> it = coll.iterator(); it.hasNext();)
		{			
			sb.append(it.next());
			if(it.hasNext()) { sb.append(", "); }
		}
		
		sb.append("]");
		return sb.toString();
	}
}
