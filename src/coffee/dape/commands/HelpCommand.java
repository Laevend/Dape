package coffee.dape.commands;

import org.bukkit.command.CommandSender;

import coffee.dape.cmdparsers.astral.annos.CommandEx;
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
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.StringUtils;


/**
 * @author Laeven
 * @since 1.0.0
 */
@CommandEx(name = "help",description = "A command for providing information about the commands")
public class HelpCommand extends AstralExecutor
{
	public HelpCommand() throws MissingAnnotationException
	{
		super(HelpCommand.class);
		
		addPath("view command",CmdSender.ANY,new ArgSet().of("<commands>",ArgTypes.STRING,Suggestions.commandNames()).mapTo("cmdName"));
		
		addPath("view path of command",CmdSender.ANY,new ArgSet().of("<commands>",ArgTypes.STRING,Suggestions.commandNames()).mapTo("cmdName").of(0,Suggestions.commandPathIndexes()).mapTo("cmdIndex"));
	}
	
	@CmdPath(name = "view command",description = "Shows information about a vertex command",syntax = "/help <command>",usage = "/help hop")
	public void viewCommand(CommandSender sender,@VMap("cmdName") String cmdName)
	{
		if(!CommandFactory.getAstralCommandMap().keySet().contains(cmdName))
		{
			PrintUtils.raw(sender,ColourUtils.translate("&3> &cThis command doesn't exist!"));
			return;
		}
		
		AstralExecutor aex = CommandFactory.getAstralCommandMap().get(cmdName);	
		
		PrintUtils.printChatTitleFloatLeft(sender,cmdName + " - paths");
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,ColourUtils.translate("&9<> &7= Argument"));
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,ColourUtils.translate("&a&oName"));
		PrintUtils.raw(sender,ColourUtils.translate("&7" + aex.getCommandName()));
		PrintUtils.raw(sender,ColourUtils.translate("&a&oAlias"));
		
		for(String alias : aex.getAlias())
		{
			PrintUtils.raw(sender,ColourUtils.translate("&7" + alias));
		}
		
		PrintUtils.raw(sender,ColourUtils.translate("&a&oDescription"));
		PrintUtils.raw(sender,ColourUtils.translate("&7" + aex.getDescription()));
		PrintUtils.raw(sender,"");
		
		int i = 1;
		
		for(String childName : aex.getPathMeta().keySet())
		{
			PrintUtils.raw(sender,ColourUtils.translate("&c" + i + " &7> " + StringUtils.formatSyntaxString(aex.getPathMeta().get(childName).getSyntax())));
			i++;
		}
		
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,ColourUtils.translate("&7Type &e/help " + cmdName + " &9<number> &7to get more info."));
		PrintUtils.raw(sender,PrintUtils.getDivider());
	}
	
	@CmdPath(name = "view path of command",description = "Shows information about a child command of a vertex command",syntax = "help <command> <child command>",usage = "/help hop mellow")
	public void viewPathOfCommand(CommandSender sender,@VMap("cmdName") String cmdName,@VMap("cmdIndex") int cmdIndex)
	{
		if(!CommandFactory.getAstralCommandMap().keySet().contains(cmdName))
		{
			PrintUtils.raw(sender,ColourUtils.translate("&3> &cThis command doesn't exist!"));
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
		
		// TODO replace spaces in permissions with underscores
		
		String name = pMeta.getPathName();
		String syntax = StringUtils.formatSyntaxString(pMeta.getSyntax());
		String usage = StringUtils.formatSyntaxString(pMeta.getUsageExample());
		String description = pMeta.getDescription();
		
		PrintUtils.printChatTitleFloatLeft(sender,cmdName + ", Path " + name + " [" + cmdIndex + "]");
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,ColourUtils.translate("&9<> &7= Argument"));
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,ColourUtils.translate("&a&oSyntax"));
		PrintUtils.raw(sender,ColourUtils.translate(syntax));
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,ColourUtils.translate("&a&oDescription"));
		PrintUtils.raw(sender,ColourUtils.translate("&7" + description));
		PrintUtils.raw(sender,"");
		PrintUtils.raw(sender,ColourUtils.translate("&a&oUsage"));
		PrintUtils.raw(sender,ColourUtils.translate(usage));
		PrintUtils.raw(sender,PrintUtils.getDivider());
	}
}