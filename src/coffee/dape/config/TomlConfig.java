package coffee.dape.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Charsets;

import coffee.dape.Dape;
import coffee.dape.utils.FUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.TimeUtils;
import coffee.khyonieheart.lilac.Lilac;
import coffee.khyonieheart.lilac.TomlConfiguration;
import coffee.khyonieheart.lilac.value.TomlArray;
import coffee.khyonieheart.lilac.value.TomlBoolean;
import coffee.khyonieheart.lilac.value.TomlDouble;
import coffee.khyonieheart.lilac.value.TomlFloat;
import coffee.khyonieheart.lilac.value.TomlInteger;
import coffee.khyonieheart.lilac.value.TomlLong;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlObjectType;
import coffee.khyonieheart.lilac.value.TomlString;
import coffee.khyonieheart.lilac.value.TomlTable;
import coffee.khyonieheart.lilac.value.formatting.TomlComment;

/**
 * 
 * @author Laeven
 * Implements TOML for managing server configurations
 */
public class TomlConfig implements DapeConfig
{
	private TomlConfiguration config;
	private Map<String,Object> defaults;
	private Path configFile;
	private String header;
	private boolean loaded;
	
	private int maxCorruptReplaceAttempts = 5;
	private int corruptReplaceAttempts = 0;
	
	private static final String CORRUPT_CONFIG_DIR = "corrupt_configs";
	
	/**
	 * Create a new configuration file object
	 * @param configFile Configuration file
	 * @param defaults Default values for this configuration file
	 * @param header The header comment of the configuration file
	 */
	public TomlConfig(Path configFile,Map<String,Object> defaults,String header)
	{
		this.configFile = configFile;
		this.defaults = defaults;
		this.header = header;
		
		if(Files.exists(configFile))
		{ 
			load();
			setDefaults();
			save();
			return;
		}
		
		firstTimeSetup();
	}
	
	/**
	 * Loads configuration file that already exists
	 */
	public void load()
	{
		if(this.configFile == null)
		{
			Logg.fatal("Cannot load configuration file as path is null!");
			Dape.forceShutdown();
		}
		
		/**
		 * Wow! error handling when parsing a config file! What a concept MD_5!
		 * Frickin' idiot...
		 */
		
		try
		{
			this.config = Lilac.tomlParser().getDecoder().decode(configFile.toFile());
			this.loaded = true;
		}
		catch(Exception e)
		{
			Logg.error("Error! Configuration file could not be parsed/read correctly!",e);
			replaceCorruptedConfig();
		}
	}
	
	/**
	 * In the event the configuration file cannot be read, it is moved to a corrupt files directory (if the user later wishes to consult it).
	 * A new configuration file is generated in its place to allow the server to continue functioning
	 */
	private void replaceCorruptedConfig()
	{
		// Prevents the server from continually attempting to replace the corrupt file in the event it keeps failing
		if((corruptReplaceAttempts++) >= maxCorruptReplaceAttempts)
		{
			Logg.fatal("Maximum corrupt configuration file replacement attempts reached!");
			Dape.forceShutdown();
			return;
		}
		
		corruptReplaceAttempts++;
		
		String fileName = "corrupt_arc_config_" + TimeUtils.getDateFormat(TimeUtils.PATTERN_DASH_dd_MM_yy);
		Path corruptConfigPath = Dape.internalFilePath(CORRUPT_CONFIG_DIR);
		
		// Make sure the corrupt configurations path exists
		FUtils.createDirectories(corruptConfigPath);
		
		int extraNumber = 1;
		
		// In the event (somehow) more than configuration file corrupts in the same second.
		while(Files.exists(Dape.internalFilePath(CORRUPT_CONFIG_DIR + File.separator + fileName)))
		{
			fileName = "corrupt_arc_config_" + TimeUtils.getDateFormat(TimeUtils.PATTERN_DASH_dd_MM_yy) + " (" + extraNumber + ")";
			extraNumber++;
		}
		
		Path finalPath = Dape.internalFilePath(CORRUPT_CONFIG_DIR + File.separator + fileName + ".yml");
		FUtils.copyFile(configFile,finalPath);
		
		if(!Files.exists(finalPath))
		{
			Logg.fatal("Corrupted configuration file '" + configFile.toString() + "' could not be moved to directory '" + corruptConfigPath.toAbsolutePath().toString() + "'!");
			Dape.forceShutdown();
			return;
		}
		
		FUtils.delete(configFile);
		
		if(Files.exists(configFile))
		{
			Logg.fatal("Corrupted configuration file '" + configFile.toString() + "' could not be deleted from original location!");
			Dape.forceShutdown();
			return;
		}
		
		Logg.warn("New configuration file has been created. Old configuration file moved to " + finalPath.toString());
		
		firstTimeSetup();
	}
	
	/**
	 * Saves configuration file
	 */
	public void save()
	{
		if(this.configFile == null) { Logg.error("The configuration file location is null"); return; }
		if(this.config == null) { Logg.error("The TomlConfiguration Object is null"); return; }
		
		try(Writer writer = new OutputStreamWriter(new FileOutputStream(this.configFile.toFile()), Charsets.UTF_8))
		{
			String data = Lilac.tomlParser().getEncoder().encode(this.config,Lilac.tomlParser().setPreserveComments(true));
			writer.write(data);
		}
		catch(Exception e)
		{
			Logg.error("Failed to save configuration file!",e);
		}
	}
	
	/**
	 * Converts a YAML key to a key suitable for TOML.
	 * 
	 * <p>"something.this.key" would be converted to:<br>
	 * [something]<br>
	 * this.key = <br>
	 * 
	 * <p>"something" would be converted to:<br>
	 * [global]<br>
	 * something =<br>
	 * @param key
	 * @return
	 */
	private String[] convertKeyToTomlKey(String key)
	{
		int split = key.indexOf('.');			
		String tableName = split == -1 ? "global" : key.substring(0,split);
		String keyName = split == -1 ? key : key.substring(split + 1,key.length());
		return new String[] {tableName,keyName};
	}
	
	/**
	 * Sets the defaults for the configuration file
	 * 
	 * <p>New configuration files have this done automatically
	 */
	public void setDefaults()
	{
		Map<String,TomlObject<?>> tomlDefaults = toTomlMap(this.defaults);
		
		for(String key : tomlDefaults.keySet())
		{
			String keys[] = convertKeyToTomlKey(key);		
			String tableName = keys[0];
			String keyName = keys[1];
			
			if(!this.config.hasKey(tableName))
			{
				this.config.set(tableName,new TomlTable(tableName,List.of()));
			}
			
			TomlTable newTable = new TomlTable(tableName,List.of());
			newTable.rebase(this.config.getTable(tableName));		
			newTable.get().put(keyName,tomlDefaults.get(key));
			this.config.set(tableName,newTable);
		}
	}
	
	/**
	 * Converts default configurations Map<String,Object> to a TomlObject map
	 * @param objMap Default configurations
	 * @return TOML map default configuration
	 */
	private Map<String,TomlObject<?>> toTomlMap(Map<String,Object> objMap)
	{
		Map<String,TomlObject<?>> tomlMap = new HashMap<>();
		
		for(Entry<String,Object> entry : objMap.entrySet())
		{
			if(entry.getValue() instanceof String val)
			{
				tomlMap.put(entry.getKey(),new TomlString(val));
				continue;
			}
			
			if(entry.getValue() instanceof Integer val)
			{
				tomlMap.put(entry.getKey(),new TomlInteger(val));
				continue;
			}
			
			if(entry.getValue() instanceof Double val)
			{
				tomlMap.put(entry.getKey(),new TomlDouble(val));
				continue;
			}
			
			if(entry.getValue() instanceof Float val)
			{
				tomlMap.put(entry.getKey(),new TomlFloat(val));
				continue;
			}
			
			if(entry.getValue() instanceof Long val)
			{
				tomlMap.put(entry.getKey(),new TomlLong(val));
				continue;
			}
			
			if(entry.getValue() instanceof Boolean val)
			{
				tomlMap.put(entry.getKey(),new TomlBoolean(val));
				continue;
			}
			
			if(entry.getValue() instanceof List<?> val)
			{
				List<TomlObject<?>> list = new ArrayList<>();
				
				if(list.isEmpty()) { continue; }
				
				tomlMap.put(entry.getKey(),new TomlArray(list));
				continue;
			}
			
			if(entry.getValue() instanceof Set<?> val)
			{
				List<TomlObject<?>> list = getTomlArray(val);
				
				if(list.isEmpty()) { continue; }
				
				tomlMap.put(entry.getKey(),new TomlArray(list));
				continue;
			}
			
			Logg.error("TOML value type " + entry.getValue().getClass().getCanonicalName() + " is not supported!");
		}
		
		return tomlMap;
	}
	
	private List<TomlObject<?>> getTomlArray(Collection<?> val)
	{
		List<TomlObject<?>> list = new ArrayList<>();
		
		if(val.isEmpty()) { return list; }
		Object obj = val.iterator().next();
		
		if(obj instanceof String)
		{
			for(Object listObj : val)
			{
				list.add(new TomlString((String) listObj));
			}
			return list;
		}
		
		if(obj instanceof Integer)
		{
			for(Object listObj : val)
			{
				list.add(new TomlInteger((Integer) listObj));
			}
			return list;
		}
		
		if(obj instanceof Double)
		{
			for(Object listObj : val)
			{
				list.add(new TomlDouble((Double) listObj));
			}
			return list;
		}
		
		if(obj instanceof Float listVal)
		{
			for(Object listObj : val)
			{
				list.add(new TomlFloat((Float) listObj));
			}
			return list;
		}
		
		if(obj instanceof Long listVal)
		{
			for(Object listObj : val)
			{
				list.add(new TomlLong((Long) listObj));
			}
			return list;
		}
		
		if(obj instanceof Boolean listVal)
		{
			for(Object listObj : val)
			{
				list.add(new TomlBoolean((Boolean) listObj));
			}
			return list;
		}
		
		Logg.error("TOML array value type " + obj.getClass().getCanonicalName() + " is not supported!");
		return list;
	}
	
	public void reset()
	{
		FUtils.delete(configFile);
		firstTimeSetup();
	}
	
	/**
	 * Gets the configuration
	 * @return FileConfiguration
	 */
	public TomlConfiguration get()
	{
		return this.config;
	}
	
	/**
	 * Setup for new configuration files
	 */
	private void firstTimeSetup()
	{
		if(this.configFile == null)
		{
			Logg.fatal("Cannot load configuration file as path is null!");
			Dape.forceShutdown();
			return;
		}
		
		FUtils.createFile(configFile);
		
		try
		{
			this.config = Lilac.tomlParser().getDecoder().decode(configFile.toFile());
		}
		catch(Exception e)
		{
			Logg.fatal("Cannot load configuration file as TOML threw exception!",e);
			Dape.forceShutdown();
			return;
		}
		
		this.config.set("header",new TomlComment(header));
		setDefaults();
		save();
	}

	public boolean isLoaded()
	{
		return loaded;
	}

	@Override
	public void saveConfig()
	{
		this.save();
	}

	@Override
	public void reloadConfig()
	{
		this.load();
	}
	
	@Override
	public boolean hasKey(String key)
	{
		String[] keys = convertKeyToTomlKey(key);		
		return get().hasKey(keys[0]) && get().hasKey(keys[1]);
	}
	

	@Override
	public Object getObject(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return null; }
		String[] keys = convertKeyToTomlKey(key);
		return get().getTable(keys[0]).get(keys[1]).get();
	}

	@Override
	public String getString(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return "NULL"; }
		String[] keys = convertKeyToTomlKey(key);
		return (String) get().getTable(keys[0]).get(keys[1]).get();
	}

	@Override
	public boolean getBoolean(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return false; }
		String[] keys = convertKeyToTomlKey(key);
		return (boolean) get().getTable(keys[0]).get(keys[1]).get();
	}

	@Override
	public int getInt(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return -1; }
		String[] keys = convertKeyToTomlKey(key);
		return (int) get().getTable(keys[0]).get(keys[1]).get();
	}

	@Override
	public long getLong(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return -1L; }
		String[] keys = convertKeyToTomlKey(key);
		return (long) get().getTable(keys[0]).get(keys[1]).get();
	}

	@Override
	public float getFloat(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return -1F; }
		String[] keys = convertKeyToTomlKey(key);
		return (float) get().getTable(keys[0]).get(keys[1]).get();
	}

	@Override
	public double getDouble(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return -1D; }
		String[] keys = convertKeyToTomlKey(key);
		return (double) get().getTable(keys[0]).get(keys[1]).get();
	}
	
	private List<TomlObject<?>> getTomlArray(String key)
	{
		String[] keys = convertKeyToTomlKey(key);
		TomlObject<?> tomlObjArray = get().getTable(keys[0]).get(keys[1]);
		
		if(tomlObjArray.getType() != TomlObjectType.ARRAY)
		{
			Logg.error("Could not retrieve string list '" + key + "'. Value is not a TOML array!");
			return Collections.emptyList();
		}
		
		@SuppressWarnings("unchecked")
		List<TomlObject<?>> tomlList = (List<TomlObject<?>>) tomlObjArray.get();
		return tomlList;
	}
	
	@Override
	public List<?> getList(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return Collections.emptyList(); }
		List<TomlObject<?>> tomlList = getTomlArray(key);
		if(tomlList.isEmpty()) { return Collections.emptyList(); }
		List<Object> list = new ArrayList<>(tomlList.size());
		
		for(TomlObject<?> objArrItem : tomlList)
		{
			list.add(objArrItem.get());
		}
		
		return list;
	}

	@Override
	public List<String> getStringList(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return Collections.emptyList(); }
		List<TomlObject<?>> tomlList = getTomlArray(key);
		if(tomlList.isEmpty()) { return Collections.emptyList(); }
		List<String> list = new ArrayList<>(tomlList.size());
		
		for(TomlObject<?> objArrItem : tomlList)
		{
			if(objArrItem.getType() != TomlObjectType.STRING)
			{
				Logg.error("Could not retrieve string list item '" + objArrItem.toString() + "'. Value is not a TOML string!");
				continue;
			}
			
			list.add((String) objArrItem.get());
		}
		
		return list;
	}

	@Override
	public List<Boolean> getBooleanList(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return Collections.emptyList(); }
		List<TomlObject<?>> tomlList = getTomlArray(key);
		if(tomlList.isEmpty()) { return Collections.emptyList(); }
		List<Boolean> list = new ArrayList<>(tomlList.size());
		
		for(TomlObject<?> objArrItem : tomlList)
		{
			if(objArrItem.getType() != TomlObjectType.BOOLEAN)
			{
				Logg.error("Could not retrieve boolean list item '" + objArrItem.toString() + "'. Value is not a TOML string!");
				continue;
			}
			
			list.add((Boolean) objArrItem.get());
		}
		
		return list;
	}

	@Override
	public List<Integer> getIntList(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return Collections.emptyList(); }
		List<TomlObject<?>> tomlList = getTomlArray(key);
		if(tomlList.isEmpty()) { return Collections.emptyList(); }
		List<Integer> list = new ArrayList<>(tomlList.size());
		
		for(TomlObject<?> objArrItem : tomlList)
		{
			if(objArrItem.getType() != TomlObjectType.INTEGER)
			{
				Logg.error("Could not retrieve int list item '" + objArrItem.toString() + "'. Value is not a TOML string!");
				continue;
			}
			
			list.add((Integer) objArrItem.get());
		}
		
		return list;
	}

	@Override
	public List<Long> getLongList(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return Collections.emptyList(); }
		List<TomlObject<?>> tomlList = getTomlArray(key);
		if(tomlList.isEmpty()) { return Collections.emptyList(); }
		List<Long> list = new ArrayList<>(tomlList.size());
		
		for(TomlObject<?> objArrItem : tomlList)
		{
			if(objArrItem.getType() != TomlObjectType.LONG)
			{
				Logg.error("Could not retrieve long list item '" + objArrItem.toString() + "'. Value is not a TOML string!");
				continue;
			}
			
			list.add((Long) objArrItem.get());
		}
		
		return list;
	}

	@Override
	public List<Float> getFloatList(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return Collections.emptyList(); }
		List<TomlObject<?>> tomlList = getTomlArray(key);
		if(tomlList.isEmpty()) { return Collections.emptyList(); }
		List<Float> list = new ArrayList<>(tomlList.size());
		
		for(TomlObject<?> objArrItem : tomlList)
		{
			if(objArrItem.getType() != TomlObjectType.FLOAT)
			{
				Logg.error("Could not retrieve float list item '" + objArrItem.toString() + "'. Value is not a TOML string!");
				continue;
			}
			
			list.add((Float) objArrItem.get());
		}
		
		return list;
	}

	@Override
	public List<Double> getDoubleList(String key)
	{
		if(!hasKey(key)) { Logg.warn("Could not retrieve '" + key + "'. Key not found!"); return Collections.emptyList(); }
		List<TomlObject<?>> tomlList = getTomlArray(key);
		if(tomlList.isEmpty()) { return Collections.emptyList(); }
		List<Double> list = new ArrayList<>(tomlList.size());
		
		for(TomlObject<?> objArrItem : tomlList)
		{
			if(objArrItem.getType() != TomlObjectType.DOUBLE)
			{
				Logg.error("Could not retrieve double list item '" + objArrItem.toString() + "'. Value is not a TOML string!");
				continue;
			}
			
			list.add((Double) objArrItem.get());
		}
		
		return list;
	}

	@Override
	public void set(String key,Object value)
	{
		String[] keys = convertKeyToTomlKey(key);
		
		if(!get().hasKey(keys[0]))
		{
			get().set(keys[0],new TomlTable(new ArrayList<>()));
		}
		
		if(get().getTable(keys[0]).containsKey(keys[1]));
	}
}
