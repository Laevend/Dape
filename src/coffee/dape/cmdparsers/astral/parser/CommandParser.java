package coffee.dape.cmdparsers.astral.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import coffee.dape.cmdparsers.astral.flags.CFlag;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.structs.CmdTree.Node;
import coffee.dape.utils.structs.Pair;
import xdrop.fuzzywuzzy.FuzzySearch;

/**
 * @author Laeven
 * 
 * Astral Parser
 * 
 * Takes a command with its arguments as an input and runs
 * them through a tree of valid ways a command can be accepted
 */
public class CommandParser
{
	// Used to prefix commands to prevent conflictions
	public static final int FUZZY_SEARCH_RATIO_CUTOFF = 70;
	
	public static enum CmdSender
	{
		PLAYER,
		CONSOLE,
		ANY
	}
	
	// long string state is a string argument that has one or multiple spaces
	// it is set to true when a quote is opened and set to false when a quote is closed
	private static boolean longStringState = false;
	
	protected static int argIndex = -1;
	protected static String chosenPath = null;
	protected static Map<String,Object> mappedArgs = new HashMap<>();
	protected static String flagMessage = null;
	protected static Player playerExecutor = null;
	
	public static ParseResult parse(CommandSender sender,String commandName,String rawArgs[],AstralExecutor executor)
	{
		argIndex = -1;
		chosenPath = null;
		mappedArgs.clear();
		longStringState = false;
		playerExecutor = null;
		
		// Player executor only active if sender is player
		if(sender instanceof Player p) {playerExecutor = p;} 
		
		Logg.verb("Parsing Command -> &a'" + commandName + "'",Logg.VerbGroup.ASTRAL_PARSER);
		Logg.verb("Args Length Before morph -> " + rawArgs.length,Logg.VerbGroup.ASTRAL_PARSER);
		Logg.verb("Args before morph -> " + Arrays.asList(rawArgs),Logg.VerbGroup.ASTRAL_PARSER);
		
		// Prepare new array for arguments after being restructured
		String[] args;
		
		// If no args are 
		if(rawArgs.length >= 0)
		{
			// Pre-process the arguments
			args = concatenateQuoteArguments(rawArgs);
			
			if(args == null)
			{
				Logg.error("Quoted arguments could not be concatenated!");
				return new ParseResult(false,true);
			}
		}
		else
		{
			args = rawArgs;
		}
		
		Logg.verb("Args after morph -> " + Arrays.asList(args),Logg.VerbGroup.ASTRAL_PARSER);
		Logg.verb("Arg Length Post PrePro -> " + args.length,Logg.VerbGroup.ASTRAL_PARSER);
		Logg.verb("Parsing...",Logg.VerbGroup.ASTRAL_PARSER);
		
		// Recursive Tree Parsing
		if(!traverseCommandTree(args,executor.getArgTree().getRoot(),false).getValueA()) { return new ParseResult(false,true); }
		
		Logg.verb("Parsing Succeeded! Chosen Method -> " + chosenPath,Logg.VerbGroup.ASTRAL_PARSER);
		Logg.verb("Checking Permission...",Logg.VerbGroup.ASTRAL_PARSER);
		
		// Permission Check
		PathMeta meta = executor.getPathMeta().get(chosenPath);
		String permission = meta.getPermission();
		
		// TODO Duplicate perm check? AstralExecutor & CommandParser?
		// Check if player has command permission		
		PermissionCheck:
		if(!sender.hasPermission(permission) && !sender.hasPermission(CommandFactory.getGroupPermission(executor.getGroup())))
		{
			// If astral has been configured to allow command execution of commands in the default group without needing their permissions
			if(CommandFactory.Config.DEFAULT_GROUP_PERMISSION_NEEDED.get() && executor.getGroup().equals("default"))
			{
				break PermissionCheck;
			}
			
			return new ParseResult(false,false,permission);
		}
		
		// Console Sender Check
		if((sender instanceof ConsoleCommandSender) && meta.getSender().equals(CmdSender.PLAYER))
		{
			Logg.error(sender.getName() + " could not execute command " + commandName + " as it can only be executed by a player!");
			
			PrintUtils.error(sender,"You're trying to execute a command only executable by a player!");
			return new ParseResult(false,true);
		}
		
		// Sender Check
		if((sender instanceof Player) && meta.getSender().equals(CmdSender.CONSOLE))
		{
			Logg.error(sender.getName() + " could not execute command " + commandName + " as it can only be executed by the console!");
			PrintUtils.error(sender,"You're trying to execute a command only executable by the console!");
			
			return new ParseResult(false,true);
		}
		
		Logg.verb("Parse passed checks",Logg.VerbGroup.ASTRAL_PARSER);
		
		// Parse successful return result
		return new ParseResult(true,chosenPath,args,sender,mappedArgs);
	}
	
	public static List<String> parseTabComplete(CommandSender sender,String[] args,AstralExecutor executor)
	{
		argIndex = -1;
		chosenPath = null;
		mappedArgs.clear();
		longStringState = false;
		playerExecutor = null;
		
		// Player executor only active if sender is player
		if(sender instanceof Player p) {playerExecutor = p;} 
		
		return traverseCommandTree(tidyUpArguments(args),executor.getArgTree().getRoot(),true).getValueB();
	}
	
	/**
	 * Combines arguments written inside quotes into one argument.
	 * @param args Array of arguments provided by spigot
	 * @return Array of new arguments with quoted arguments being represented as a single argument
	 */
	protected static String[] concatenateQuoteArguments(String[] args)
	{
		// If not arguments were entered, just return
		if(args.length == 0) { return args; }
		
		String argumentString = String.join(" ",args);
		long numOfQuotes = argumentString.chars().filter(v -> v == '\"').count();
		
		if(numOfQuotes == 0) { return args; }
		
		// Odd number of quotes. See isLongStringState ^
		longStringState = (numOfQuotes % 2 != 0);
		
		String longStringParts[] = argumentString.split("\"");
		List<String> newArgs = new ArrayList<>();
		
		Logg.verb("Parts -> " + Arrays.asList(longStringParts),Logg.VerbGroup.ASTRAL_PARSER);
		Logg.verb("length -> " + longStringParts.length,Logg.VerbGroup.ASTRAL_PARSER);
		
		Loop1:
		for(int i = 0; i < longStringParts.length; i++)
		{
			if(longStringParts[i].length() == 0) { continue Loop1; }
			
			if(i % 2 == 0)
			{				
				String tempArgs[] = longStringParts[i].split(" ");
				
				Loop2:
				for(String arg : tempArgs)
				{
					if(arg.length() == 0) { continue Loop2; }
					newArgs.add(arg);
				}
			}
			else
			{				
				newArgs.add(longStringParts[i]);
			}
		}
		
		return newArgs.toArray(new String[newArgs.size()]);
	}
	
	/**
	 * Traverses the command tree to find an argument match
	 * @param args array of arguments typed
	 * @param tree CmdTree to be traversed
	 * @param isCalledByTabComplete If this method is called by tab complete or command executor
	 * @return true/false if parse succeeded/failed
	 */
	private static Pair<Boolean,List<String>> traverseCommandTree(String[] args,Node node,boolean isCalledByTabComplete)
	{
		// Look at next argument
		argIndex++;
		
		Logg.verb("Arg Index : " + argIndex,Logg.VerbGroup.ASTRAL_PARSER);
		
		if(isCalledByTabComplete && argIndex == (args.length - 1))
		{
			return Pair.of(false,displaySuggestions(args,node));
		}
		
		// Reached end of arguments, check for an end node
		if(argIndex == args.length || args.length == 0)
		{
			Logg.verb("Arg length is 0 or is ended",Logg.VerbGroup.ASTRAL_PARSER);
			
			if(node.isEndNode())
			{
				chosenPath = node.getPathName();
				return Pair.of(true,Collections.emptyList());
			}
			
			return Pair.of(false,Collections.emptyList());
		}
		
		// Check static args first
		if(node.getStaticArgs().contains(args[argIndex]))
		{
			Logg.verb("Chosen argument (Static): " + args[argIndex],Logg.VerbGroup.ASTRAL_PARSER);
			
			StaticArgument staticArg = (StaticArgument) node.branches.get(args[argIndex]).getValue();
			
			// Check if argument is mapped to a logic method parameter
			if(staticArg.mapId != null)
			{
				mappedArgs.put(staticArg.mapId,args[argIndex]);
			}
			
			return traverseCommandTree(args,node.branches.get(args[argIndex]),isCalledByTabComplete);
		}
		
		// Temp define to hold this argument for checking when other types have been exhausted
		VarArgument varArgWithStringType = null;
		PlayerVarArgument playerVarArgWithStringType = null;
		
		// Check variable args (no string type)
		for(String varArgKey : node.getVarArgs())
		{
			// Retrieve VarArg from upstream branch
			VarArgument varArg = (VarArgument) node.branches.get(varArgKey).getValue();
			
			if(varArg.getArgumentType().getTypeName().equals("[VAR_TYPE:STRING]"))
			{
				varArgWithStringType = varArg;
				continue;
			}
			
			// Validate type against what player typed
			if(varArg.getArgumentType().isType(args[argIndex]))
			{
				// Check if argument is mapped to a logic method parameter
				if(varArg.mapId != null)
				{
					mappedArgs.put(varArg.mapId,varArg.getArgumentType().parse(args[argIndex]));
				}
				
				// Check flags
				if(varArg.getFlags() != null)
				{
					for(CFlag flag : varArg.getFlags())
					{
						if(!flag.validate(args[argIndex]))
						{
							if(!isCalledByTabComplete)
							{
								flagMessage = flag.getFailMessage();
							}
							
							return Pair.of(false,Collections.emptyList());
						}
					}
				}
				
				return traverseCommandTree(args,node.branches.get(varArg.getArgumentKey()),isCalledByTabComplete);
			}
		}
		
		// Check player variable args (no string type)
		for(String pVarArgKey : node.getPlayerVarArgs())
		{
			// Retrieve VarArg from upstream branch
			PlayerVarArgument pVarArg = (PlayerVarArgument) node.branches.get(pVarArgKey).getValue();
			
			if(pVarArg.getPlayerArgumentType().getTypeName().equals("[VAR_TYPE:STRING]"))
			{
				playerVarArgWithStringType = pVarArg;
				continue;
			}
			
			// Validate type against what player typed
			if(pVarArg.getPlayerArgumentType().isType(args[argIndex]))
			{
				// Check if argument is mapped to a logic method parameter
				if(pVarArg.mapId != null)
				{
					mappedArgs.put(pVarArg.mapId,pVarArg.getPlayerArgumentType().parse(args[argIndex]));
				}
				
				// Check flags
				if(pVarArg.getFlags() != null)
				{
					for(CFlag flag : pVarArg.getFlags())
					{
						if(!flag.validate(args[argIndex]))
						{
							if(!isCalledByTabComplete)
							{
								flagMessage = flag.getFailMessage();
							}
							
							return Pair.of(false,Collections.emptyList());
						}
					}
				}
				
				return traverseCommandTree(args,node.branches.get(pVarArg.getArgumentKey()),isCalledByTabComplete);
			}
		}
		
		// Check variable args (string type if found)
		if(varArgWithStringType != null && varArgWithStringType.getArgumentType().isType(args[argIndex]))
		{
			// Check if argument is mapped to a logic method parameter
			if(varArgWithStringType.mapId != null)
			{
				mappedArgs.put(varArgWithStringType.mapId,varArgWithStringType.getArgumentType().parse(args[argIndex]));
			}
			
			// Check flags
			if(varArgWithStringType.getFlags() != null)
			{
				for(CFlag flag : varArgWithStringType.getFlags())
				{
					if(!flag.validate(args[argIndex]))
					{
						if(!isCalledByTabComplete)
						{
							flagMessage = flag.getFailMessage();
						}
						
						return Pair.of(false,Collections.emptyList());
					}
				}
			}
			
			return traverseCommandTree(args,node.branches.get(varArgWithStringType.getArgumentKey()),isCalledByTabComplete);
		}
		
		// Check player variable args (string type if found)
		if(playerVarArgWithStringType != null && playerVarArgWithStringType.getPlayerArgumentType().isType(args[argIndex]))
		{
			// Check if argument is mapped to a logic method parameter
			if(playerVarArgWithStringType.mapId != null)
			{
				mappedArgs.put(playerVarArgWithStringType.mapId,playerVarArgWithStringType.getPlayerArgumentType().parse(args[argIndex]));
			}
			
			// Check flags
			if(playerVarArgWithStringType.getFlags() != null)
			{
				for(CFlag flag : playerVarArgWithStringType.getFlags())
				{
					if(!flag.validate(args[argIndex]))
					{
						if(!isCalledByTabComplete)
						{
							flagMessage = flag.getFailMessage();
						}
						
						return Pair.of(false,Collections.emptyList());
					}
				}
			}
			
			return traverseCommandTree(args,node.branches.get(playerVarArgWithStringType.getArgumentKey()),isCalledByTabComplete);
		}
		
		// Check Conditional variable argument
		if(node.getConVarArg() != null)
		{
			ConVarArgument conVarArg = (ConVarArgument) node.branches.get(node.getConVarArg()).getValue();
			
			// Called here to get correct argument type
			conVarArg.getSuggestions(args);
			
			// Validate type against what player typed
			if(conVarArg.getConditionalArgumentType().isType(args[argIndex]))
			{
				// Check if argument is mapped to a logic method parameter
				if(conVarArg.mapId != null)
				{
					mappedArgs.put(conVarArg.mapId,conVarArg.getConditionalArgumentType().parse(args[argIndex]));
				}
				
				// Check flags
				if(conVarArg.getFlags() != null)
				{
					for(CFlag flag : conVarArg.getFlags())
					{
						if(!flag.validate(args[argIndex]))
						{
							if(!isCalledByTabComplete)
							{
								flagMessage = flag.getFailMessage();
							}
							
							return Pair.of(false,Collections.emptyList());
						}
					}
				}
				
				return traverseCommandTree(args,node.branches.get(conVarArg.getArgumentKey()),isCalledByTabComplete);
			}
		}
		
		Logg.verb("No argument branches found and no end branches @ " + argIndex,Logg.VerbGroup.ASTRAL_PARSER);
		
		// Create suggestions
		return Pair.of(false,Collections.emptyList());
	}
	
	public static List<String> displaySuggestions(String[] args,Node node)
	{
		// Edge case
		if(argIndex > (args.length - 1)) { return Collections.emptyList(); }
		
		// Don't suggest anything while typing a string
		if(longStringState)
		{
			Logg.warn("<=/ 3 \\=>");
			return Collections.emptyList();
		}
		
		// If next argument is empty, show all options
		if(args[argIndex].length() == 0)
		{
			Logg.verb("Getting next arguments...",Logg.VerbGroup.ASTRAL_PARSER);
			return getSuggestions(args,node);
		}
//		else if(argIndex < (args.length - 1))
//		{
//			Logg.verb("Future Arg? -> " + args[argIndex + 1]);
//			return Collections.emptyList();
//		}
		else
		{
			Logg.verb("Estimating...",Logg.VerbGroup.ASTRAL_PARSER);
			return estimate(args,node);
		}
	}
	
	/**
	 * This method will tidy up the arguments used in tab complete by
	 * respecting arguments in quotes as a single argument (like the parser does)
	 * and removing empty argument array slots.
	 * Empty array slots appear in tab completion but for some reason not in command execution
	 * @param args_ Arguments array to tidy up
	 * @return Tidy arguments for tab complete to work with
	 */
	public static String[] tidyUpArguments(String args_[])
	{
		if(args_.length == 0) { return args_; }
		
		Logg.verb("Array before tidy: " + Arrays.asList(args_),Logg.VerbGroup.ASTRAL_PARSER);
		
		String morphedArgs[] = concatenateQuoteArguments(args_);
		List<String> list = new ArrayList<>();
		
		for(String morphedArg : morphedArgs)
		{
			if(morphedArg.length() == 0) { continue; }
			list.add(morphedArg);
		}
		
		String newArgs[];
		
		// If last argument is empty (This means player has started to type next argument)
		if(args_[args_.length - 1].length() == 0)
		{
			newArgs = list.toArray(new String[list.size() + 1]);
			newArgs[newArgs.length - 1] = "";
		}
		else
		{
			newArgs = list.toArray(new String[list.size()]);
		}
		
		Logg.verb("Array after tidy: " + Arrays.asList(newArgs),Logg.VerbGroup.ASTRAL_PARSER);
		
		return newArgs;
	}
	
	/**
	 * Returns a list of all suggestions from all branch node arguments.
	 * 
	 * <p>This is called when the player has not typed anything for their next argument
	 * @param args Argument array consisting of arguments the player has typed
	 * @param node A node in the command tree
	 * @return Suggestions
	 */
	private static List<String> getSuggestions(String args[],Node node)
	{		
		Logg.verb("Getting Suggestions",Logg.VerbGroup.ASTRAL_PARSER);
		
		if(node.branches.isEmpty()) { return Collections.emptyList(); }
		
		List<String> suggestions = new ArrayList<>();
		
		node.branches.values().forEach(branchNode ->
		{
			if(branchNode.getValue() instanceof ConVarArgument conVarArg)
			{
				suggestions.addAll(conVarArg.getSuggestions(args));
			}
			else if(branchNode.getValue() instanceof PlayerVarArgument conVarArg)
			{
				// Fail safe in the event a PlayerVarArgument argument attempts to get used with a ConsoleCommandSender
				if(playerExecutor == null)
				{
					Logg.fatal("ERROR! " + node.tree.getParent().getCommandName() + " contains a path using a PlayerVarArgument but is being used by console!");
					Logg.error("Error, PlayerVarArgument cannot be used by the Sender type 'ConsoleCommandSender'. Only type 'Player' can use it");
					return;
				}
				
				suggestions.addAll(conVarArg.getSuggestions(playerExecutor));
			}
			else
			{
				suggestions.addAll(branchNode.getValue().getSuggestions());
			}			
		});
		
		return suggestions;
	}
	
	/**
	 * Called from the {@link #displaySuggestions(String[], Node)} method, 
	 * The next argument has been determined as partially typed but
	 * not completed.
	 * This method will trim the total number of next possible arguments
	 * down to those who match the characters the argument typed starts
	 * with.
	 * 
	 * @param args The arguments typed by the player
	 * @param node A node in the command tree
	 * @return A trimmed list of all possible next arguments
	 */
	private static List<String> estimate(String[] args,Node node)
	{
		// Clear the list to generate new estimates
		List<String> allSuggestions = getSuggestions(args,node);
		
		// More than 50 items to search and estimation of o(n) is not feasible
		//if(allSuggestions.size() > 50) { return allSuggestions; }
		
		List<String> finalSuggestions = new ArrayList<String>();
		TreeMap<Integer,List<String>> ratioMap = new TreeMap<>(Collections.reverseOrder());
		
		for(String suggestion : allSuggestions)
		{
			int ratio = FuzzySearch.partialRatio(suggestion,args[argIndex]);
			
			if(ratio < FUZZY_SEARCH_RATIO_CUTOFF) { continue; }
			if(!ratioMap.containsKey(ratio)) { ratioMap.put(ratio,new ArrayList<>()); }
			
			ratioMap.get(ratio).add(suggestion);
		}
		
		ratioMap.forEach((k,v) ->
		{
			finalSuggestions.addAll(v);
		});
		
		// Return new estimates
		return finalSuggestions;
	}
}
