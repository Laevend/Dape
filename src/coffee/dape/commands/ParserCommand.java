package coffee.dape.commands;

import org.bukkit.command.CommandSender;

import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.annos.Elevated;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.VMap;
import coffee.dape.cmdparsers.astral.parser.ArgSet;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandFactory;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.cmdparsers.astral.parser.PathMeta;
import coffee.dape.cmdparsers.astral.suggestions.Suggestions;
import coffee.dape.cmdparsers.astral.types.ArgTypes;
import coffee.dape.exception.MissingAnnotationException;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.StringUtils;


/**
 * @author Laeven
 * @since 1.0.0
 */
@Elevated
@CommandEx(name = "parser",description = "A command to aid in debugging the parser")
public class ParserCommand extends AstralExecutor
{	
	public ParserCommand() throws MissingAnnotationException
	{
		super(ParserCommand.class);
		
		addPath("print cmd tree",CmdSender.ANY,new ArgSet().of("print-tree").of("<command>",ArgTypes.STRING,Suggestions.commandNames()).mapTo("cmdName"));
		
		addPath("print paths of command",CmdSender.ANY,new ArgSet().of("<commands>",ArgTypes.STRING,Suggestions.commandNames()).mapTo("cmdName"));
		
		addPath("print path details",CmdSender.ANY,new ArgSet().of("<commands>",ArgTypes.STRING,Suggestions.commandNames()).mapTo("cmdName").of(0,Suggestions.commandPathIndexes()).mapTo("cmdIndex"));
	}
	
	@CmdPath(name = "print cmd tree",description = "Prints a commands tree.",syntax = "/parser print-tree <command>",usage = "/parser print-tree itembuilder")
	public void printTree(CommandSender sender,@VMap("cmdName") String cmdName)
	{
		AstralExecutor executor = CommandFactory.getAstralCommandMap().get(cmdName);
		Logg.verb("Command Tree: " + cmdName + "\n" + executor.getArgTree().toString(),Logg.VerbGroup.COMMANDS);
	}
	
	@CmdPath(name = "print paths of command",description = "Displays information about a command",syntax = "/parser <command>",usage = "/parser parser")
	public void viewCommand(CommandSender sender,@VMap("cmdName") String cmdName)
	{
		if(!CommandFactory.getAstralCommandMap().keySet().contains(cmdName))
		{
			PrintUtils.raw(sender,"&3> &cThis command doesn't exist!");
			return;
		}
		
		AstralExecutor aex = CommandFactory.getAstralCommandMap().get(cmdName);	
		
		PrintUtils.printChatTitleFloatLeft(sender,cmdName + " - paths");
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,"&9<> &7= Argument");
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,"&a&oName");
		PrintUtils.raw(sender,"&7" + aex.getCommandName());
		PrintUtils.raw(sender,"&a&oAlias");
		
		for(String alias : aex.getAlias())
		{
			PrintUtils.raw(sender,"&7" + alias);
		}
		
		PrintUtils.raw(sender,"&a&oDescription");
		PrintUtils.raw(sender,"&7" + aex.getDescription());
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,"&aGroup&7: &c" + aex.getGroup());
		PrintUtils.raw(sender,"&aPermission&7: &c" + aex.getPermission());
		PrintUtils.raw(sender,"&aClass&7: &c" + aex.getCommandClass().getSimpleName());
		
		int i = 1;
		
		for(String childName : aex.getPathMeta().keySet())
		{
			PrintUtils.raw(sender,"&c" + i + " &7> " + StringUtils.formatSyntaxString(aex.getPathMeta().get(childName).getSyntax()));
			i++;
		}
		
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,"&7Type &e/help " + cmdName + " &9<number> &7to get more info.");
		PrintUtils.raw(sender,PrintUtils.getDivider());
	}
	
	@CmdPath(name = "print path details",description = "Displays detail about a command path",syntax = "parser <command> <path>",usage = "/parser parser 1")
	public void viewPathOfCommand(CommandSender sender,@VMap("cmdName") String cmdName,@VMap("cmdIndex") int cmdIndex)
	{
		if(!CommandFactory.getAstralCommandMap().keySet().contains(cmdName))
		{
			PrintUtils.raw(sender,"&3> &cThis command doesn't exist!");
			return;
		}
		
		AstralExecutor aex = CommandFactory.getAstralCommandMap().get(cmdName);	
		int count = 1;
		PathMeta pMeta = null;
		
		for(String cmd : aex.getPathMeta().keySet())
		{
			if(count == cmdIndex)
			{
				pMeta = aex.getPathMeta().get(cmd);
				break;
			}
			
			count++;
		}
		
		if(pMeta == null)
		{
			PrintUtils.raw(sender,"&3> &cThis syntax doesn't exist!");
			return;
		}
		
		String name = pMeta.getPathName();
		String syntax = StringUtils.formatSyntaxString(pMeta.getSyntax());
		String usage = StringUtils.formatSyntaxString(pMeta.getUsageExample());
		String description = pMeta.getDescription();
		String permission = pMeta.getPermission();
		boolean isElevated = pMeta.isElevated();
		boolean isHidden = pMeta.isHidden();
		CmdSender senderType = pMeta.getSender();
		boolean usingMappedArgs = pMeta.isUsingMappedArguments();
		String logicMethodName = pMeta.getPathMethod().getName();		
		
		PrintUtils.printChatTitleFloatLeft(sender,cmdName + ", Path " + name + " [" + cmdIndex + "]");
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,"&9<> &7= Argument");
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,"&a&oSyntax");
		PrintUtils.raw(sender,syntax);
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,"&a&oDescription");
		PrintUtils.raw(sender,"&7" + description);
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,"&a&oUsage");
		PrintUtils.raw(sender,usage);
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,"&aPermission&7: &c" + permission);
		PrintUtils.raw(sender,"&aElevated&7: &c" + isElevated);
		PrintUtils.raw(sender,"&aHidden&7: &c" + isHidden);
		PrintUtils.raw(sender,"&aSenderType&7: &c" + senderType.toString().toLowerCase());
		PrintUtils.raw(sender,"&aUsingMappedArgs&7: &c" + usingMappedArgs);
		PrintUtils.raw(sender,"&aLogicMethodName&7: &c" + logicMethodName);
		PrintUtils.raw(sender,PrintUtils.getDivider());
	}
}