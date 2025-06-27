package coffee.dape.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

/**
 * @author Laeven
 * @since 1.0.0
 */
public class WorldUtils
{
	/**
	 * Gets the directory all worlds are in
	 * @return
	 */
	public static File getWorldContainer()
	{
		return Bukkit.getServer().getWorldContainer();
	}
	
	/**
	 * Gets the directory path all worlds are in
	 * @return
	 */
	public static String getWorldContainerPath()
	{
		// Substring as by default you get "D:\Vertex 1.20.2 Puffer\."
		// We don't need or want the extra slash or dot on the end so substring to remove it! "D:\Vertex 1.20.2 Puffer"
		return Bukkit.getServer().getWorldContainer().getAbsolutePath().substring(0,Bukkit.getServer().getWorldContainer().getAbsolutePath().length() - 2);
	}
	
	/**
	 * Gets the world directory path
	 * @param worldName Name of world to get world directory path for
	 * @return World directory path
	 */
	public static String getWorldPath(String worldName)
	{
		return getWorldContainerPath() + File.separator + worldName;
	}
	
	/**
	 * Checks if a world is already loaded
	 * @param worldName Name of world
	 * @return true if world is already loaded, otherwise false
	 */
	public static boolean isWorldLoaded(String worldName)
	{
		World w = Bukkit.getWorld(worldName);
		return w != null;
	}
	
	/**
	 * Retrieves a world, loaded or not.
	 * 
	 * <p>If world is already loaded, world it fetched from memory.
	 * <p>If world is not loaded, world is automatically loaded and returned.
	 * Given that a main world always has the folder 'playerdata',
	 * the nether world always has the folder 'DIM-1', and
	 * the end world always has the folder 'DIM1'.
	 * These assumptions are what determine what environment is used to load the world with.
	 * 
	 * @param worldName Name of the world to load
	 * @return returns null if world could not be found or environment could not be determined
	 */
	public static World getWorld(String worldName)
	{
		if(WorldUtils.isWorldLoaded(worldName))
		{
			return Bukkit.getWorld(worldName);
		}
		
		Path worldDirectoryPath = Paths.get(WorldUtils.getWorldPath(worldName));
		
		if(!Files.isDirectory(worldDirectoryPath)) { return null; }
		
		Path netherPath = Paths.get(worldDirectoryPath.toString() + File.separator + "DIM-1");
		Path endPath = Paths.get(worldDirectoryPath.toString() + File.separator + "DIM1");
		
		Environment env;
		
		// Both DIM-1 (nether) and DIM1 (end) are found in a single player world directory
		if(Files.isDirectory(netherPath) && Files.isDirectory(endPath))
		{
			env = Environment.NORMAL;
		}
		// If DIM-1 is found but not DIM1 then it's a server nether world
		else if(Files.isDirectory(netherPath) && !Files.isDirectory(endPath))
		{
			env = Environment.NETHER;
		}
		// If DIM1 is found but not DIM-1 then it's a server end world
		else if(Files.isDirectory(endPath) && !Files.isDirectory(netherPath))
		{
			env = Environment.THE_END;
		}
		// If neither DIM-1 or DIM1 is found then it's an additional over world that's not the default
		else
		{
			env = Environment.NORMAL;
		}
		
		Logg.info("World " + worldName + " was force loaded using environment " + env.toString() + " due to a subsystem request.");
		
		return new WorldCreator(worldName).environment(env).createWorld();
	}
	
	public static World createNewWorld(String worldName,Environment env)
	{
		if(WorldUtils.isWorldLoaded(worldName))
		{
			return Bukkit.getWorld(worldName);
		}
		
		Path worldDirectoryPath = Paths.get(WorldUtils.getWorldPath(worldName));
		
		if(Files.isDirectory(worldDirectoryPath)) { return getWorld(worldName); }
		
		return new WorldCreator(worldName).environment(env).keepSpawnInMemory(false).createWorld();
	}
	
	/**
	 * Checks if a world exists, loaded or not
	 * 
	 * @param worldName Name of the world to load
	 * @return True if this world exists.
	 */
	public static boolean worldExists(String worldName)
	{
		if(WorldUtils.isWorldLoaded(worldName)) { return true; }
		
		Path worldDirectoryPath = Paths.get(WorldUtils.getWorldPath(worldName));
		
		if(Files.isDirectory(worldDirectoryPath)) { return true; }
		return false;
	}
	
	public static String environmentToName(World w)
	{
		switch(w.getEnvironment())
		{
			case CUSTOM:
			{
				return "Special";
			}
			case NETHER:
			{
				return "Nether";
			}
			case NORMAL:
			{
				return "Overworld";
			}
			case THE_END:
			{
				return "End";
			}
		}
		
		return "Unknown";
	}
	
	/**
	 * Get a set of loaded Bukkit world names
	 * @return Set
	 */
	public static Set<String> getBukkitLoadedWorlds()
	{
		Set<String> worldNames = new HashSet<String>();
		
		for(World w : Bukkit.getWorlds())
		{
			worldNames.add(w.getName());
		}
		
		return worldNames;
	}
	
	/**
	 * Get a set of World environments
	 * @return Set
	 */
	public static Set<String> getWorldEnvironments()
	{
		Set<String> worldEnvironments = new HashSet<String>();
		
		for(Environment env : World.Environment.values())
		{
			worldEnvironments.add(env.toString());
		}
		
		return worldEnvironments;
	}
	
	/**
	 * Get a set of World types
	 * @return Set
	 */
	public static Set<String> getWorldTypes()
	{
		Set<String> worldTypes = new HashSet<String>();
		
		for(WorldType type : WorldType.values())
		{
			worldTypes.add(type.toString());
		}
		
		return worldTypes;
	}
	
	/**
	 * Get a set of all the folder names of each world folder
	 * @return Set
	 */
	public static Set<String> getWorldFolderNames()
	{
		Set<String> worldFolderNames = new HashSet<String>();
		
		File serverDirectory = Bukkit.getServer().getWorldContainer();
		
		for(File serverFile : serverDirectory.listFiles())
		{
			String worldFolderName = serverFile.getName();
			
			if(serverFile.isDirectory() && !serverFile.getName().equals("cache") && !serverFile.getName().equals("logs") && !serverFile.getName().equals("plugins"))
			{				
				SearchFolder:
				for(File worldFolderFile : serverFile.listFiles())
				{
					if(worldFolderFile.getName().equalsIgnoreCase("level.dat"))
					{
						worldFolderNames.add(worldFolderName);
						break SearchFolder;
					}
				}
			}
		}
		
		return worldFolderNames;
	}
	
	public static Set<String> getWorldNames()
	{
		return getWorldPaths().stream().map(Path::getFileName).map(Path::toString).collect(Collectors.toSet());
	}
	
	public static Set<Path> getWorldPaths()
	{
		Set<Path> worldPaths = new HashSet<>();
		
		try
		{
			Files.walkFileTree(Bukkit.getServer().getWorldContainer().toPath(),new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult postVisitDirectory(Path dir,IOException exc) throws IOException
				{
					Path levelDatPath = Path.of(dir.toString() + File.separator + "level.dat");
					
					if(Files.exists(levelDatPath))
					{
						worldPaths.add(dir);
					}
					
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch(Exception e)
		{
			Logg.error("Error occured attemping to grab world paths!",e);
		}
		
		return worldPaths;
	}
	
	/**
	 * Gets a list of unloaded world names
	 * @return Set
	 */
	public static Set<String> getUnloadedWorlds()
	{
		Set<String> unloadedWorlds = getWorldFolderNames();
		getBukkitLoadedWorlds().forEach(v -> unloadedWorlds.remove(v));
		
		return unloadedWorlds;
	}
	
	/**
	 * Gets a list of game rules
	 * @return
	 */
	public static Set<String> getGameRules()
	{
		Set<String> rules = new HashSet<>();
		
		for(GameRule<?> rule : GameRule.values())
		{
			rules.add(rule.getName());
		}
		
		return rules;
	}
	
	/**
	 * Gets a list of game rules for a world
	 * Object can either be an Integer or Boolean, (Boolean is preferred)
	 * @param world World
	 * @return GameRules map
	 */
	public static Map<String,Object> getGameRuleDefaultsForWorld(World world)
	{
		Map<String,Object> gameRules = new HashMap<String,Object>();
		
		for(GameRule<?> rule : GameRule.values())
		{
			gameRules.put(rule.getName(),world.getGameRuleDefault(rule));
		}
		
		return gameRules;
	}
	
	/**
	 * Unloads a world
	 * @param w World
	 * @param saveWorldOnUnload If the world should be saved on unloading
	 */
	public static void unloadWorld(World w,boolean saveWorldOnUnload)
	{
		if(Bukkit.getWorlds().get(0).getName().equals(w.getName()))
		{
			Logg.error("Cannot unload the main world!");
			return;
		}
		
		Location spawnLocationOfMainWorld = Bukkit.getWorlds().get(0).getSpawnLocation();
		
		w.getPlayers().forEach(v -> v.teleport(spawnLocationOfMainWorld));
		Arrays.asList(w.getLoadedChunks()).forEach(v -> v.unload());
		Bukkit.unloadWorld(w,saveWorldOnUnload);
	}
	
	public static String getDefaultWorldName()
	{
		Properties serverProp = new Properties();
		String levelName = null;
		
		try(InputStream is = Files.newInputStream(Paths.get("server.properties")))
		{
			serverProp.load(is);
			levelName = serverProp.getProperty("level-name");
			is.close();
			
			if(levelName == null || levelName.isEmpty() || levelName.isBlank())
			{
				Logg.warn("Could not fetch default world name from server.properties! Defaulting to 'world'");
				levelName = "world";
			}
		}
		catch(Exception e)
		{
			Logg.error("Error reading server.properties!",e);
		}
		
		return "world";
	}
	
	/**
	 * Edge of a Minecraft world a player can go without special tricks to bypass the border
	 * @return
	 */
	public static int getEdge()
	{
		return 29_999_984;
	}
}
