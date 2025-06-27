package coffee.dape.cmdparsers.astral.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_21_R4.command.CraftCommandMap;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;

import coffee.dape.Dape;
import coffee.dape.event.CommandRegisterEvent;
import coffee.dape.utils.Logg;
import coffee.dape.utils.Logg.Common.Component;

/**
 * @author Laeven
 * @since 0.6.0
 */
public class InternalCommandMap
{
	private Map<String,Map<String,PluginCommand>> prefixedCommands = new HashMap<>();
	private Class<?> pluginCommandClass;
	private Constructor<?> pluginCommandConstructor;
	private CraftCommandMap commandMap;
	
	/**
	 * Create a new instance of OpenCommandMap
	 * @param server Server this map will be used for
	 */
	public InternalCommandMap()
	{		
		try
		{
			Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			bukkitCommandMap.setAccessible(true);
			this.commandMap = (CraftCommandMap) bukkitCommandMap.get(Bukkit.getServer());
			
			this.pluginCommandClass = Class.forName("org.bukkit.command.PluginCommand");
			this.pluginCommandConstructor = this.pluginCommandClass.getDeclaredConstructor(String.class,Plugin.class);
			this.pluginCommandConstructor.setAccessible(true);
		}
		catch(Exception e)
		{
			Logg.error("PluginCommand class could not be found!",e);
		}
	}
	
	/**
	 * Adds a new command to the map
	 * @param prefix Fallback prefix used
	 * @param commandName Name of the command
	 * @param executor VertexExecutor used to execute this command
	 * @throws Exception if Alias fields cannot be accessed
	 */
	public void addCommand(AstralExecutor executor) throws Exception
	{
		PluginCommand cmd = (PluginCommand) this.pluginCommandConstructor.newInstance(executor.getCommandName(),Dape.instance());
		
		// md_5 has a dumb register() method with you need to declare to each command with a command class
		// However, this method does literally nothing and is more of a gate keeper for setting other variables in the command like Aliases
		// cmd.register(this.commandMap);
		// Edit: doesn't matter anyway! The method refuses to work properly! It only sets active aliases not the other 'aliases' variable! USELESS!
		
		Field aliases = cmd.getClass().getSuperclass().getDeclaredField("aliases");
		aliases.setAccessible(true);
		aliases.set(cmd,Lists.newArrayList(executor.getAlias()));
		
		Field activeAliases = cmd.getClass().getSuperclass().getDeclaredField("activeAliases");
		activeAliases.setAccessible(true);
		activeAliases.set(cmd,Lists.newArrayList(executor.getAlias()));
		
		cmd.setDescription(executor.getDescription());
		cmd.setExecutor((CommandExecutor) executor);
		cmd.setTabCompleter((TabCompleter) executor);
		
		if(executor.getPermission() == null || executor.getPermission().isEmpty() || executor.getPermission().isBlank())
		{
			executor.setPermission(CommandFactory.COMMAND_PREFIX + ".command." + executor.getCommandName());
			Logg.warn("Executor " + executor.getCommandName() + " has a null, empty, or blank permission! Permission set to " + executor.getPermission());
		}
		
		cmd.setPermission(executor.getPermission());
		
		// Removed as commands that a player does not have access to are never sent
		// cmd.setPermissionMessage("Unknown command. Type \"/help\" for help.");
		
		if(!this.prefixedCommands.containsKey(CommandFactory.COMMAND_PREFIX))
		{
			this.prefixedCommands.put(CommandFactory.COMMAND_PREFIX,new HashMap<>());
		}
		
		this.prefixedCommands.get(CommandFactory.COMMAND_PREFIX).put(cmd.getName(),cmd);
		
		if(this.commandMap.register(CommandFactory.COMMAND_PREFIX,this.prefixedCommands.get(CommandFactory.COMMAND_PREFIX).get(cmd.getName())))
		{
			Logg.Common.printOk(Component.COMMAND,"Building",executor.getCommandName());
			Bukkit.getPluginManager().callEvent(new CommandRegisterEvent(executor));
		}
		else
		{
			Logg.Common.printFail(Component.COMMAND,"Building",executor.getCommandName());
		}
	}
	
	/**
	 * Removes commands from the map
	 * 
	 * @param prefix Fallback prefix used
	 * @param command Name of this command
	 */
	public void removeCommands(String prefix,Set<String> commands)
	{
		if(!this.prefixedCommands.containsKey(prefix)) { return; }
		
		for(String command : commands)
		{
			if(!this.prefixedCommands.get(prefix).containsKey(command)) { continue; }
			
			this.prefixedCommands.get(prefix).remove(command);
		}
		
		flushCommandMap();
	}
	
	/**
	 * Removes commands from the map
	 * 
	 * @param prefix Fallback prefix used
	 * @param command Name of this command
	 */
	public void removeCommands(String prefix)
	{
		if(!this.prefixedCommands.containsKey(prefix)) { return; }
		this.prefixedCommands.remove(prefix);
		flushCommandMap();
	}
	
	/**
	 * Gets a set of all prefixes
	 * @return Set of command prefixes
	 */
	public Set<String> getPrefixes()
	{
		return prefixedCommands.keySet();
	}
	
	/**
	 * Gets a set of all commands for a prefix
	 * @param prefix Fallback prefix used
	 * @return Set of commands for a prefix
	 */
	public Set<String> getCommandsForPrefix(String prefix)
	{
		return prefixedCommands.get(prefix).keySet();
	}
	
	/**
	 * Flushes the command map by discarding all commands and
	 * re-building the map from commands added to this class
	 */
	private void flushCommandMap()
	{
		Logg.title("Flushing Command Map!");
		
		this.commandMap.clearCommands();
        
        List<String> prefixes = Lists.newArrayList(this.prefixedCommands.keySet());
        Collections.sort(prefixes);
        
        for(String prefix : prefixes)
        {
        	for(String command : this.prefixedCommands.get(prefix).keySet())
        	{
        		if(this.commandMap.register(prefix,this.prefixedCommands.get(prefix).get(command)))
        		{
        			Logg.Common.printOk(Component.COMMAND,"Building",command);
        		}
        		else
        		{
        			Logg.Common.printFail(Component.COMMAND,"Building",command);
        		}
        	}
        }
        
		CommandFactory.refreshCommands();
	}
	
	/**
	 * Returns the internal knownCommands command map
	 * @return Internal CommandMap
	 */
	public Map<String,Command> getInternalCommandMap()
	{
		return this.commandMap.getKnownCommands();
	}
}