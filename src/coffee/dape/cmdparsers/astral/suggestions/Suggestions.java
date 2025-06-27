package coffee.dape.cmdparsers.astral.suggestions;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;

import coffee.dape.Dape;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandFactory;
import coffee.dape.cmdparsers.astral.parser.Comparators;
import coffee.dape.cmdparsers.astral.types.IntegerType;
import coffee.dape.utils.CacheUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.structs.Namespace;

/**
 * @author Laeven
 * @since 1.0.0
 */
public class Suggestions
{
	private static Map<Namespace,UnconditionalSuggestionList> suggestionLists = new HashMap<>();
	private static Map<Namespace,ConditionalSuggestionList> conditionalSuggestionLists = new HashMap<>();
	private static Map<Namespace,PlayerSuggestionList> playerSuggestionLists = new HashMap<>();
	
	/**
	 * Gets a suggestion list by name
	 * @param suggestionListNamespace Name of the suggestion list to fetch
	 * @return SuggestionList if the one requested exists, otherwise null
	 */
	public static SuggestionList get(Namespace namespace)
	{
		if(suggestionLists.containsKey(namespace))
		{
			return suggestionLists.get(namespace);
		}
		
		if(conditionalSuggestionLists.containsKey(namespace))
		{
			return conditionalSuggestionLists.get(namespace);
		}
		
		if(playerSuggestionLists.containsKey(namespace))
		{
			return playerSuggestionLists.get(namespace);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param suggestionListNamespace
	 * @return
	 */
	public static boolean hasSuggestionList(Namespace suggestionListNamespace)
	{
		return suggestionLists.containsKey(suggestionListNamespace) ? true :
			conditionalSuggestionLists.containsKey(suggestionListNamespace) ? true :
				playerSuggestionLists.containsKey(suggestionListNamespace);
	}
	
	/**
	 * Adds a suggestion list to a map of external suggestions
	 * @param list UnconditionalSuggestionList to add
	 */
	public static void addSuggestionList(SuggestionList list)
	{
		if(list instanceof UnconditionalSuggestionList uList)
		{
			suggestionLists.put(list.getSuggestionListNamespace(),uList);
			return;
		} 
		else if(list instanceof ConditionalSuggestionList cList)
		{
			conditionalSuggestionLists.put(list.getSuggestionListNamespace(),cList);
			return;
		}
		else if(list instanceof PlayerSuggestionList pList)
		{
			playerSuggestionLists.put(list.getSuggestionListNamespace(),pList);
			return;
		}
		
		Logg.error("Could not store suggestion list " + list.getSuggestionListNamespace() + " as it's not of type " +
				UnconditionalSuggestionList.class.toString() + " or " +
				ConditionalSuggestionList.class.toString() + " or " +
				PlayerSuggestionList.class.toString());
	}
	
	/**
	 * Checks if the suggestion list is that of a unconditional one
	 * @param list
	 * @return
	 */
	public static boolean isUnconditional(SuggestionList list)
	{
		return list instanceof UnconditionalSuggestionList;
	}
	
	/**
	 * Checks if the suggestion list is that of a conditional one
	 * @param list
	 * @return
	 */
	public static boolean isConditional(SuggestionList list)
	{
		return list instanceof ConditionalSuggestionList;
	}
	
	/**
	 * Checks if the suggestion list is that of a player one
	 * @param list
	 * @return
	 */
	public static boolean isPlayer(SuggestionList list)
	{
		return list instanceof PlayerSuggestionList;
	}
	
	/**
	 * Converts a list of hard coded suggestions into a SuggestionList
	 * @param suggestions list of suggestions
	 * @return SuggestionList containing all the hard coded suggestions
	 */
	public static UnconditionalSuggestionList of(String... suggestions)
	{
		UnconditionalSuggestionList sl = new UnconditionalSuggestionList(Namespace.of(Dape.getNamespaceName(),"unique_suggestion_list_" + UUID.randomUUID()),false)
		{
		    @Override
		    public void build()
		    {
		    	Arrays.stream(suggestions).forEach(v -> add(v));
		    }
		};
		
		return sl;
	}
	
	// TODO move to parser package
	public static UnconditionalSuggestionList commandNames()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"command_names");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,false,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	addAll(CacheUtils.commandNames().getCache());
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}
	
	// TODO move to parser package
	public static ConditionalSuggestionList commandPathIndexes()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"command_path_indexes");
		if(conditionalSuggestionLists.containsKey(listNamespace)) { return conditionalSuggestionLists.get(listNamespace); }
		
		conditionalSuggestionLists.put(listNamespace,new ConditionalSuggestionList(listNamespace,Comparators.NUMERICALLY_BY_STRING)
		{
		    @Override
		    protected void build(String commandName)
		    {
		    	if(!CommandFactory.getAstralCommandMap().containsKey(commandName)) { return; }
	    		
		    	argumentType = new IntegerType();
	    		AstralExecutor ex = CommandFactory.getAstralCommandMap().get(commandName);
	    		
	    		for(int i = 0; i < ex.getPathMeta().keySet().size(); i++)
	    		{
	    			add(String.valueOf(i + 1));
	    		}
		    }
		});
		
		return conditionalSuggestionLists.get(listNamespace);
	}
	
	public static UnconditionalSuggestionList minecraftMaterials()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"minecraft_items");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,false,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	Arrays.stream(Material.values()).forEach(v -> add(v.toString().toLowerCase()));
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}
	
	/**
	 * Gets a suggestion list of loaded worlds
	 * @return SuggestionList
	 */
	public static UnconditionalSuggestionList loadedWorlds()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"loaded_worlds");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,true,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	Bukkit.getWorlds().forEach(v -> add(v.getName()));
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}
	
	/**
	 * Gets a suggestion list of online player names
	 * @return SuggestionList
	 */
	public static UnconditionalSuggestionList onlinePlayerNames()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"online_player_names");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,true,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	Bukkit.getOnlinePlayers().forEach(v -> add(v.getName()));
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}
	
	/**
	 * Gets a suggestion list of world environments
	 * @return SuggestionList
	 */
	public static UnconditionalSuggestionList worldEnvironments()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"world_environments");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,false,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	Arrays.stream(World.Environment.values()).forEach(v -> add(v.toString().toLowerCase()));
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}
	
	/**
	 * Gets a suggestion list of world types
	 * @return SuggestionList
	 */
	public static UnconditionalSuggestionList worldTypes()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"world_types");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,false,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	Arrays.stream(WorldType.values()).forEach(v -> add(v.toString().toLowerCase()));
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}
	
	public static UnconditionalSuggestionList unloadedWorlds()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"unloaded_worlds");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,true,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	Arrays.stream(Bukkit.getServer().getWorldContainer().listFiles()).forEach(v ->
				{
					if(v.isDirectory() && !v.getName().equals("cache") && !v.getName().equals("logs") && !v.getName().equals("plugins"))
					{				
						File levelDat = new File(v.getAbsolutePath() + File.separator + "level.dat");
						
						if(levelDat.exists())
						{
							add(v.getName());
						}
					}
				});
				
				Bukkit.getWorlds().forEach(v ->
				{
					if(get().contains(v.getName()))
					{
						remove(v.getName());
					}
				});
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}

	public static UnconditionalSuggestionList allWorlds()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"all_worlds");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,true,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	Arrays.stream(Bukkit.getServer().getWorldContainer().listFiles()).forEach(v ->
				{
					if(v.isDirectory() && !v.getName().equals("cache") && !v.getName().equals("logs") && !v.getName().equals("plugins"))
					{				
						File levelDat = new File(v.getAbsolutePath() + File.separator + "level.dat");
						
						if(levelDat.exists())
						{
							add(v.getName());
						}
					}
				});
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}
	
	public static UnconditionalSuggestionList itemFlags()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"item_flags");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,false,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	Arrays.stream(ItemFlag.values()).forEach(v -> add(v.toString().toLowerCase()));
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}

	public static UnconditionalSuggestionList attributes()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"attributes");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,false,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	Arrays.stream(Attribute.values()).forEach(v -> add(v.toString().toLowerCase()));
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}

	public static UnconditionalSuggestionList operations()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"operations");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,false,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	Arrays.stream(AttributeModifier.Operation.values()).forEach(v -> add(v.toString().toLowerCase()));
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}

	public static UnconditionalSuggestionList equipmentSlot()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"equipment_slot");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,false,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	Arrays.stream(EquipmentSlot.values()).forEach(v -> add(v.toString().toLowerCase()));
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}
	
	public static UnconditionalSuggestionList loggingVerboseGroups()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"logging_verbose_groups");
		if(suggestionLists.containsKey(listNamespace)) { return suggestionLists.get(listNamespace); }
		
		suggestionLists.put(listNamespace,new UnconditionalSuggestionList(listNamespace,true,Comparators.ALPHABETICALLY)
		{
		    @Override
		    public void build()
		    {
		    	for(Namespace group : Logg.getVerboseGroups())
		    	{
		    		add(group.toSimpleString());
		    	}
		    }
		});
		
		return suggestionLists.get(listNamespace);
	}
}