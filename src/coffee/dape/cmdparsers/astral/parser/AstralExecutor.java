package coffee.dape.cmdparsers.astral.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.annos.Elevated;
import coffee.dape.cmdparsers.astral.annos.VMap;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl;
import coffee.dape.cmdparsers.astral.elevatedaccount.PendingCommand;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.exception.DuplicateCommandPathException;
import coffee.dape.exception.IllegalMethodCallException;
import coffee.dape.exception.MissingAnnotationException;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.StringUtils;
import coffee.dape.utils.structs.CmdTree;
import coffee.dape.utils.structs.CmdTree.Node;
import coffee.dape.utils.structs.Pair;

/**
 * @author Laeven
 * 
 * This class provides implementation for the execution of this command and its tab complete suggestions.
 * 
 */
public class AstralExecutor implements CommandExecutor, TabCompleter
{
	private static final String UNKNOWN_COMMAND_MSG = "Unknown command. Type \"/help\" for help.";
	private static final String INTERNAL_ERROR_MSG = "An internal error occured with this command! Check console for more information.";
	private static final String PERMISSION_REGEX = "^(.+).([^.]+)$";
	private static final Set<Class<?>> PRIMITIVE_TYPE_SET = Set.of
	(
		int.class,
		byte.class,
		char.class,
		boolean.class,
		double.class,
		float.class,
		long.class,
		short.class
	);
	
	// CommandEx annotated class that extends this class
	private Class<? extends AstralExecutor> commandClass;
	
	// Path Name -> Path Meta
	private TreeMap<String,PathMeta> pathMap = new TreeMap<>();
	private Set<Long> pathChecksums = new HashSet<>();
	private CmdTree argTree = new CmdTree(this);
	
	// Used as a small cache to prevent iterating over the class multiple times looking for path logic methods
	private Map<String,Method> pathLogicMethodsFound = new HashMap<>();
	
	private String commandName;
	private String[] alias;
	private String description;
	private String permission;
	private String group;
	private boolean elevated = false;
	
	/**
	 * Creates a new Astral Executor using
	 * @param cmdClass The command class that extends the Astral Executor
	 * @throws MissingAnnotationException 
	 */
	public AstralExecutor(Class<? extends AstralExecutor> cmdClass) throws MissingAnnotationException
	{
		this.commandClass = cmdClass;
		
		// Check that this executor has the command annotation
		if(!this.commandClass.isAnnotationPresent(CommandEx.class))
		{
			throw new MissingAnnotationException("Command of class " + commandClass.getSimpleName() + " Is missing the Command annotation!");
		}
		
		CommandEx commandAnno = commandClass.getAnnotation(CommandEx.class);
		
		this.commandName = commandAnno.name();
		this.alias = commandAnno.alias();
		this.description = commandAnno.description();
		this.permission = !commandAnno.permission().matches(PERMISSION_REGEX) ? CommandFactory.COMMAND_PREFIX + ".command." + this.commandName.toLowerCase().replaceAll("\s++","_") : commandAnno.permission();
		this.group = commandAnno.group();
		
		// If command requires login credentials to execute
		if(this.commandClass.isAnnotationPresent(Elevated.class))
		{
			this.elevated = true;
		}
		
		// Cache path logic methods
		for(Method method : commandClass.getDeclaredMethods())
		{
			Logg.verb("Method name -> " + method.getName(),Logg.VerbGroup.ASTRAL_PARSER);
		
			// Lambda and switch cases used in command logic is registered as a method
			if(method.getName().contains("$")) { continue; }
			
			// Ignore methods with no path annotation
			if(!method.isAnnotationPresent(CmdPath.class)) { continue; }
			
			CmdPath pathAnno = method.getAnnotation(CmdPath.class);
			
			if(!StringUtils.isNotNullEmptyOrBlank(pathAnno.name()))
			{
				throw new IllegalArgumentException("Path annotations cannot have a null, empty, or blank name!");
			}
			
			// Duplicate Path check
			if(pathLogicMethodsFound.containsKey(pathAnno.name()))
			{
				Logg.fatal("Error! Path " + pathAnno.name() + " already exists as a different path!\n"
						+ " > The 'name' attribute of the Path annotation cannot be the same as another @Path annotation in the same class!");
				return;
			}
			
			pathLogicMethodsFound.put(pathAnno.name(),method);
		}
	}
	
	/**
	 * Called after the parent command class has finished defining all the syntaxes to pair with path logic methods.
	 * This checks the path logic methods that have been found were all paired up and there are no missing pairs.
	 */
	public void checkForUnpairedPaths()
	{
		if(pathLogicMethodsFound.size() == 0) { return; }
		
		Logg.warn("Missing syntax definitions found for the following paths in " + commandClass.getSimpleName() + ":");
		
		for(String pathName : pathLogicMethodsFound.keySet())
		{
			Logg.warn(" - " + pathName);
		}
		
		pathLogicMethodsFound.clear();
		pathLogicMethodsFound = null;
	}
	
	/**
	 * Adds path meta data for a path
	 * @param pathName Name of path used to retrieve path meta data
	 * @param sender The type of sender that's allowed to execute this path
	 * @param args The arguments of this path in string form
	 */
	public void addPath(String pathName,CmdSender sender,String... sArgs)
	{
		ArgSet set = ArgumentSetParser.parse(sArgs,this.getCommandName());
		
		if(set == null) { return; }
		
		addPath(pathName,sender,set);
	}
	
	/**
	 * Adds path meta data for a path
	 * @param pathName Name of path used to retrieve path meta data
	 * @param sender The type of sender that's allowed to execute this path
	 * @param argSet The arguments of this path
	 */
	public void addPath(String pathName,CmdSender sender,ArgSet argSet)
	{
		Argument[] args = new Argument[argSet.getArguments().size()];
		argSet.getArguments().toArray(args);
		
		addPath(pathName,sender,args);
	}
	
	/**
	 * Adds path meta data for a path
	 * @param pathName Name of path used to retrieve path meta data
	 * @param sender The type of sender that's allowed to execute this path
	 * @param args The arguments of this path
	 */
	public void addPath(String pathName,CmdSender sender,Argument... args)
	{
		// Construct path meta object using path name to find the method
		Method pathLogicMethod = null;
		
		try	{ pathLogicMethod = getPathMethod(pathName); }
		catch (MissingAnnotationException e) { Logg.error("Error initialising path",e); return; }
		
		PathMeta meta = new PathMeta(this,pathLogicMethod,sender,args);
		meta = populateMeta(meta,pathLogicMethod);
		
		// Store path meta in map for future execution
		this.pathMap.put(pathName,meta);
		
		try
		{
			// Add path arguments to tree
			addArgumentsToTree(meta);
		}
		catch (DuplicateCommandPathException e)
		{
			Logg.fatal("Duplicate Command Path",e);
		}
	}
	
	/**
	 * Adds path meta data for a path
	 * @param pathName Name of path used to retrieve path meta data
	 * @param sender The type of sender that's allowed to execute this path
	 */
	public void addPath(String pathName,CmdSender sender)
	{
		// Construct path meta object using path name to find the method
		Method pathLogicMethod = null;
		
		try	{ pathLogicMethod = getPathMethod(pathName); }
		catch (MissingAnnotationException e) { Logg.error("Error initialising path command",e); return; }
		
		PathMeta meta = new PathMeta(this,pathLogicMethod,sender);
		meta = populateMeta(meta,pathLogicMethod);
		
		// Store path meta in map for future execution
		this.pathMap.put(pathName,meta);
		
		// Path has no argument so it becomes the value of the root node
		this.argTree.getRoot().setPathName(pathName);
		this.argTree.getRoot().setEndNode(true);
	}
	
	/**
	 * Returns the path logic method
	 * @param pathName Name of path used to retrieve path meta data
	 * @return Path logic method
	 * @throws MissingAnnotationException Thrown when a path logic method cannot be found
	 */
	private Method getPathMethod(String pathName) throws MissingAnnotationException
	{
		if(pathLogicMethodsFound.containsKey(pathName)) { return pathLogicMethodsFound.remove(pathName); }
		
		throw new MissingAnnotationException("Could not find path logic method for '" + pathName + "' in command class '" + commandClass.getSimpleName() + "'. Check your path annotation names!");
	}
	
	/**
	 * Gets all the meta data from the annotations on the path command logic method and stores it in the PathMeta class
	 * @param meta PathMeta to populate
	 * @return PathMeta with meta data populated
	 */
	private PathMeta populateMeta(PathMeta meta,Method pathLogicMethod)
	{
		CmdPath pathAnno = pathLogicMethod.getAnnotation(CmdPath.class);
		
		meta.setPathName(pathAnno.name());
		meta.setDescription(pathAnno.description());
		meta.setPermission(!pathAnno.permission().matches(PERMISSION_REGEX) ? this.permission + ".path." + meta.getPathName().toLowerCase().replaceAll("\s++","_") : pathAnno.permission());
		meta.setSyntax(pathAnno.syntax());
		meta.setUsageExample(pathAnno.usage());
		meta.setHidden(pathAnno.hidden());
		
		// If command requires login credentials to execute
		if(pathLogicMethod.isAnnotationPresent(Elevated.class))
		{
			if(this.elevated)
			{
				Logg.warn("Redundant @Elevated annotation found in command class " + this.commandClass.getSimpleName() + "! @Elevated not needed on path commands if class has @Elevated!");
			}
			
			meta.setElevated(true);
		}
		
		// Check logic method has at least a CommandSender
		if(pathLogicMethod.getParameters().length < 1)
		{
			Logg.fatal("Error! Path " + meta.getPathName() + " has missing arguments in its logic method!\n"
					+ " > Check this paths logic method has a CommandSender, Player, or ConsoleCommandSender as its first argument.");
			return meta;
		}
		
		// Logic method only has a sender and nothing more
		if(pathLogicMethod.getParameters().length == 1) { return meta; }
		
		// Check if second argument of path logic method is a string array (using String args[])
		Class<?> stringArrayArgs = pathLogicMethod.getParameters()[1].getType();
		
		if(stringArrayArgs.getName().equals(String.class.getName()) && stringArrayArgs.isArray()) { return meta; }

		// Collect path logic methods parameter types with their annotated ids
		List<Pair<String,Class<?>>> mappedParameterTypes = new ArrayList<>();
		
		for(Parameter param : pathLogicMethod.getParameters())
		{
			if(!param.isAnnotationPresent(VMap.class)) { continue; }
			
			String argId = param.getAnnotation(VMap.class).value();
			
			if(argId == null || argId.isEmpty() || argId.isBlank())
			{
				Logg.fatal("Error! Path logic method contains a mapped argument that has a null, empty, or blank id!\n"
						+ " > Arg annotation must have some value for the 'id' attribute. It cannot be blank!\n"
						+ " > Arg 'id' attribute should reference an argument defined when calling 'addPath()'.\n"
						+ " > Arg 'id' attribute cannot be the same as another @Arg annotation in the same path logic method!");
				return meta;
			}
			
			mappedParameterTypes.add(new Pair<>(param.getAnnotation(VMap.class).value(),param.getType()));
		}
		
		meta.setMappedParameterTypes(mappedParameterTypes);
		
		return meta;
	}
	
	/**
	 * Adds arguments to the command tree
	 * @param pm PathMeta
	 * @throws DuplicateCommandPathException 
	 */
	private void addArgumentsToTree(PathMeta pm) throws DuplicateCommandPathException
	{
		Argument[] args = pm.getArgs();
		this.pathName = pm.getPathName();
		argIterator = -1;
		
		// Hash/Crc32 the arguments to check a path like this doesn't already exist
		long checksum = pm.getChecksum();
		
		if(pathChecksums.contains(checksum))
		{
			this.pathName = null;
			throw new DuplicateCommandPathException("Path '" + pm.getPathName() + "' is a duplicate of another command path!");
		}
		
		pathChecksums.add(checksum);
		attatchBranchToTree(args,this.argTree.getRoot());
		this.pathName = null;
	}
	
	private int argIterator = -1;
	private String pathName;
	
	/**
	 * Recursively traversed the CmdTree to add any missing arguments
	 * that are not present
	 * @param args array of arguments from a PathCommandMeta
	 * @param node Command tree node
	 */
	private void attatchBranchToTree(Argument[] args,Node node)
	{
		argIterator++;
		
		// Add an End argument to the end of the argument tree path
		if(argIterator == args.length || args.length == 0)
		{
			node.setEndNode(true);
			node.setPathName(this.pathName);
			return;
		}
		
		if(node.branches.containsKey(args[argIterator].getArgumentKey()))
		{
			// Navigate to branch
			attatchBranchToTree(args,node.branches.get(args[argIterator].getArgumentKey()));
		}
		else
		{
			// Add new branch
			attatchBranchToTree(args,node.addBranch(args[argIterator].getArgumentKey(),args[argIterator]));
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args)
	{		
		// TODO add console perm logging (Console session logged in as a person will have this execution logged to them) 
		
		// Base command permission check
		PermissionCheck:
		if(!sender.hasPermission(cmd.getPermission()) && !sender.hasPermission(CommandFactory.getGroupPermission(this.getGroup())))
		{
			// If astral has been configured to allow command execution of commands in the default group without needing their permissions
			if(CommandFactory.Config.DEFAULT_GROUP_PERMISSION_NEEDED.get() && this.getGroup().equals("default"))
			{
				break PermissionCheck;
			}
			
			PrintUtils.raw(sender,UNKNOWN_COMMAND_MSG);
			printPermissionLack(sender,cmd,cmd.getPermission());
			return true;
		}
		
		ParseResult pr = null;
		
		try
		{
			// Parse the command first to check it is valid for execution
			pr = CommandParser.parse(sender,cmd.getName(),args,this);
		}
		catch(Exception e)
		{
			PrintUtils.error(sender,"Parser failed execution! Command may have bad architecture!");
			PrintUtils.error(sender,"Notify an administrator immediately!");
			Logg.error("Parser threw an exception! Command '" + cmd.getName() + "' may have bad architecture!",e);
			Logg.error("Common causes of bad command architecture:\n"
					+ " - Syntax name and Syntax logic method name are not the same,\n"
					+ " - Syntax is defined but no Syntax logic method is found,\n"
					+ " - Syntax logic method exists but no Syntax is defined for that method,\n"
					+ " - Syntax is defined but Syntax logic method is missing a Syntax annotation,\n"
					+ " - Missing Command annotation,\n"
					+ " - Missing mandatory attributes of Syntax annotation,\n"
					+ " - Missing mandatory attributes of Command annotation,\n"
					+ " - Command class not extending AstralExecutor.");
			return true;
		}
			
		// Check the result
		if(pr.getResult())
		{
			// Check for command elevation
			PathMeta pathMeta = this.pathMap.get(pr.getPathName());
			
			if(pathMeta.isElevated())
			{
				if(pr.getSender() instanceof Player p)
				{
					// Check player executing this elevated command has an elevated account
					if(!ElevatedAccountCtrl.hasElevatedAccount(p))
					{
						PrintUtils.error(pr.getSender(),"This command requires elevation! You don't have an elevated account!");
						return true;
					}
					
					// Check the account is loaded and doesn't just exist only on disk
					if(!ElevatedAccountCtrl.isAccountLoaded(p))
					{
						PrintUtils.error(pr.getSender(),"This command requires elevation! Your elevated account was not loaded on server startup!");
						return true;
					}
					
					// Check the account is not locked from too many incorrect auths
					if(ElevatedAccountCtrl.getAccount(p.getUniqueId()).isLocked())
					{
						PrintUtils.error(pr.getSender(),"This command requires elevation! Your elevated account is locked!");
						return true;
					}
					
					// Check if account has NOT already been authorised and will prompt user for auth input
					if(!ElevatedAccountCtrl.getAccount(p.getUniqueId()).isAuthed())
					{
						try
						{
							// Check elevated account has any authentication methods
							if(ElevatedAccountCtrl.getAccount(p.getUniqueId()).getAuthMethods().isEmpty())
							{
								Logg.error("This command requires elevation! But this user has no methods of authentication!");
								PrintUtils.error(p,"This command requires elevation! But you have no methods of authentication!");
								return true;
							}
							
							ElevatedAccountCtrl.getAccount(p.getUniqueId()).setPendingCommand(new PendingCommand(sender,cmd,label,args,this,System.currentTimeMillis()));
							PrintUtils.warn(pr.getSender(),"This command requires elevation!");							
							PrintUtils.info(p,ElevatedAccountCtrl.getAccount(p.getUniqueId()).getAuthMethods().getFirst().getAuthMessage());
						}
						catch (IllegalMethodCallException e)
						{
							Logg.error("Unexpected bad method call",e);
						}
						
						return true;
					}
				}
				else if(pr.getSender() instanceof ConsoleCommandSender)
				{
					if(!ElevatedAccountCtrl.getConsoleAccount().isAuthed())
					{
						try
						{
							ElevatedAccountCtrl.getConsoleAccount().setPendingCommand(new PendingCommand(sender,cmd,label,args,this,System.currentTimeMillis()));
							PrintUtils.warn(pr.getSender(),"This command requires elevation! Type '/console auth <pin/password>' to authorise this command");
						}
						catch (IllegalMethodCallException e)
						{
							Logg.error("Unexpected bad method call",e);
						}
						
						return true;
					}
				}
				else
				{
					PrintUtils.error(pr.getSender(),"This command requires elevation! This command cannot be authorised by this sender!");
					return true;
				}
			}
			
			try
			{
				// Execute path command
				executePathCommand(sender,args,pr);	
			}
			catch(Exception e)
			{
				PrintUtils.error(sender,"Command failed to execute properly! You entered bad arguments!");
				PrintUtils.raw(sender,ColourUtils.translate("&fUsage&8: &e/help " + cmd.getName()));
				Logg.error("Command '" + cmd.getName() + "' failed to execute correctly!",e);
			}
		}
		// CommandSender does not have path permission
		else if(!pr.hasPermission())
		{
			PrintUtils.sendPermErr(sender);
			printPermissionLack(sender,cmd,pr.getMissingPermission());
		}
		else
		{
			PrintUtils.raw(sender,ColourUtils.translate("&3> &cIncorrect syntax! Type &e/help <command> &cfor more information"));			
			
			if(CommandParser.flagMessage == null) { return true; }
			
			PrintUtils.raw(sender,ColourUtils.translate("&3> &c" + CommandParser.flagMessage));
			CommandParser.flagMessage = null;
		}
		
		return true;
	}
	
	/**
	 * Prepares the path method for execution.
	 * 
	 * <p>This method also has checks for path method name, path meta,sender type, and if the console/player has the permission to execute
	 * @param sender Executor of this command
	 * @param args Command arguments
	 * @param parseResult Result of command parsing
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	private void executePathCommand(CommandSender sender,String[] args,ParseResult parseResult) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		if(parseResult.getPathName() == null || parseResult.getPathName().isEmpty() || parseResult.getPathName().isBlank())
		{
			Logg.error("Error! Cmd -> '" + this.commandName + "' has path name that is null, empty, or blank!\n"
					+ " > Path logic method should have a @Path annotation.\n"
					+ " > Path annotation must have some value for the 'name' attribute. It cannot be blank!\n"
					+ " > The 'name' attribute of the Path annotation cannot be the same as another @Path annotation in the same class!");
			
			PrintUtils.error(sender,INTERNAL_ERROR_MSG);
			return;
		}
		
		if(!pathMap.containsKey(parseResult.getPathName()))
		{
			Logg.error("Error! Cmd -> '" + this.commandName + "' Path -> '" + parseResult.getPathName() + "' does not have any path meta!\n"
					+ " > Possible logic error with how path names are being added to map with their meta.");
			PrintUtils.error(sender,INTERNAL_ERROR_MSG);
			return;
		}
		
		PathMeta pathMeta = this.pathMap.get(parseResult.getPathName());
		Method pathMethod = pathMeta.getPathMethod();
		Object senderType = null;
		
		/* ===
		 * Sender type check
		 * ===
		 */
		
		// Player sender
		SenderInstanceOf:
		if(parseResult.getSender() instanceof Player playerSender)
		{
			// Check if logic method has Player type
			if(!pathMethod.getParameterTypes()[0].getName().equals(Player.class.getName())) { break SenderInstanceOf; }
			
			Logg.verb("Sender is Player",Logg.VerbGroup.ASTRAL_PARSER);
			senderType = playerSender;
		}
		
		// Console sender
		else if(parseResult.getSender() instanceof ConsoleCommandSender consoleSender)
		{
			// Check if logic method has ConsoleCommandSender type
			if(!pathMethod.getParameterTypes()[0].getName().equals(ConsoleCommandSender.class.getName())) { break SenderInstanceOf; }
			
			Logg.verb("Sender is Console",Logg.VerbGroup.ASTRAL_PARSER);
			senderType = consoleSender;
		}
		
		// Any sender
		if(senderType == null)
		{
			if(!pathMethod.getParameterTypes()[0].getName().equals(CommandSender.class.getName()))
			{
				Logg.error("Error! Cmd -> '" + this.commandName + "' Path -> '" + pathMeta.getPathName() + "' does not have a CommandSender in its logic method!\n"
						+ " > Check to ensure the FIRST argument of the path's logic method is of type CommandSender, Player, or ConsoleCommandSender");
				PrintUtils.error(sender,INTERNAL_ERROR_MSG);
				return;
			}
			
			Logg.verb("Sender is CommandSender",Logg.VerbGroup.ASTRAL_PARSER);
			senderType = parseResult.getSender();
		}
		
		/* ===
		 * Method invoking type check
		 * ===
		 */
		
		// (Player player,String args[])
		if(!pathMeta.isUsingMappedArguments())
		{
			// Using (CommandSender sender)
			if(pathMethod.getParameters().length == 1)
			{
				pathMethod.invoke(this,senderType);
			}
			
			// Using (CommandSender sender, String args[])
			else
			{
				pathMethod.invoke(this,senderType,parseResult.getArguments());
			}
			
			return;
		}
		
		// Arguments submitted by player (+1 to include the sender at the start)
		Object castedArguments[] = new Object[pathMeta.getNumberOfMappedArguments() + 1];
		castedArguments[0] = senderType;
		
		// Check for mapped arguments in method == arguments defined as mapped
		// -1 is not including the sender type which is the first argument
		if((pathMethod.getParameters().length - 1) != pathMeta.getNumberOfMappedArguments())
		{
			Logg.fatal("Error! Cmd -> '" + this.commandName + "' Path -> '" + pathMeta.getPathName() + "' defined as using mapped arguments but number of mapped arguments to mapped parameters do not match!\n"
					+ "Mapped arguments: " + pathMeta.getNumberOfMappedArguments() + "\n"
					+ "Mapped parameters: " + (pathMethod.getParameters().length - 1));
			return;
		}
		
		// fill object array up with auto casted player arguments and invoke
		for(int i = 0; i < pathMeta.getMappedParameterTypes().size(); i++)
		{
			Pair<String,Class<?>> mappedParamType = pathMeta.getMappedParameterTypes().get(i);
			
			// Check that the name of mapped parameter matches what was collected from the argument set
			if(!parseResult.getMappedArgs().containsKey(mappedParamType.getValueA()))
			{
				Logg.fatal("Error! Cmd -> '" + this.commandName + "' Path -> '" + pathMeta.getPathName() + "' has missing mapped parameter! Expected '" + mappedParamType.getValueA() + "'");
				return;
			}
			
			Object castedArgument = parseResult.getMappedArgs().get(mappedParamType.getValueA());
			Class<?> type = mappedParamType.getValueB();
			
			// Type check between the type of the casted argument and the type of the method parameter
			// Primitive types get a free pass
			if(!type.isInstance(castedArgument) && !PRIMITIVE_TYPE_SET.contains(type))
			{
				Logg.fatal("Error! Cmd -> '" + this.commandName + "' Path -> '" + pathMeta.getPathName() + "' has different types for mapped parameter " + i + " (" + type.getSimpleName() + ") and its corresponding argument");
				return;
			}
			
			// Get casted argument typed by player and add it to the object array for invoking the logic method
			castedArguments[i + 1] = castedArgument;
		}
		
		pathMethod.invoke(this,castedArguments);
	}
	
	/**
	 * Prints out the command permission that a player is lacking in the console
	 * @param sender Sender of this command
	 * @param cmd Command the sender does not have permission for
	 * @param permission Permission that the sender does not have
	 */
	private void printPermissionLack(CommandSender sender,Command cmd,String permission)
	{
		if(sender instanceof Player)
		{
			Logg.error("Player " + ((Player) sender).getName() + " lacks permission " + permission + " for command " + cmd.getName());
		}
		else
		{
			Logg.error("Console lacks permission " + permission + " for command " + cmd.getName());
		}
	}
	
	/**
	 * Called by tab completion
	 */
	public List<String> onTabComplete(CommandSender sender,Command command,String alias,String[] args)
	{
		return CommandParser.parseTabComplete(sender,args,this);
	}
	
	/**
	 * Get the name of this command
	 * @return Command name
	 */
	public String getCommandName()
	{
		return commandName;
	}
	
	/**
	 * Get paths
	 * @return
	 */
	public TreeMap<String,PathMeta> getPathMeta()
	{
		return pathMap;
	}

	/**
	 * Get aliases of this command
	 * @return Command aliases
	 */
	public String[] getAlias()
	{
		return alias;
	}

	protected void setAlias(String[] alias)
	{
		this.alias = alias;
	}
	
	/**
	 * Get the description of this command
	 * @return Command description
	 */
	public String getDescription()
	{
		return description;
	}
	
	protected void setDescription(String description)
	{
		this.description = description;
	}
	
	/**
	 * Gets the command class
	 * @return Command class
	 */
	public Class<?> getCommandClass()
	{
		return commandClass;
	}

	protected void setCommandClass(Class<? extends AstralExecutor> commandClass)
	{
		this.commandClass = commandClass;
	}

	public String getPermission()
	{
		return permission;
	}

	protected void setPermission(String permission)
	{
		this.permission = permission;
	}

	public String getGroup()
	{
		return group;
	}

	public void setGroup(String group)
	{
		this.group = group;
	}

	public boolean isElevated()
	{
		return elevated;
	}

	protected void setElevated(boolean elevated)
	{
		this.elevated = elevated;
	}

	public CmdTree getArgTree()
	{
		return argTree;
	}
}