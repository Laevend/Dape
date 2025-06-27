package coffee.dape.playerdata.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.parser.ArgSet;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.cmdparsers.astral.suggestions.Suggestions;
import coffee.dape.cmdparsers.astral.types.ArgTypes;
import coffee.dape.exception.MissingAnnotationException;
import coffee.dape.playerdata.gui.PlayerServerVarsGuiBuilder;
import coffee.dape.utils.PlayerUtils;


/**
 * 
 * @author Laeven
 *
 */
@CommandEx(name = "playerservervars",description = "A command for viewing a players server variables")
public class PlayerServerVariablesCommand extends AstralExecutor
{
	public PlayerServerVariablesCommand() throws MissingAnnotationException
	{
		super(PlayerServerVariablesCommand.class);
		
		addPath("openServerVariables",CmdSender.PLAYER);
		
		addPath("openPlayerServerVariables",CmdSender.PLAYER,new ArgSet().of("<player>",ArgTypes.STRING,Suggestions.onlinePlayerNames()));
	}
	
	@CmdPath(name = "openServerVariables",description = "Opens the server settings menu for yourself",syntax = "/playerservervars",usage = "/playerservervars")
	public void openServerVariables(CommandSender sender,String[] args)
	{
		Player p = (Player) sender;
		
		PlayerServerVarsGuiBuilder.instance.setPlayerInUse(p,p.getUniqueId());
		ChaosFactory.open(p,ChaosFactory.Common.PLAYER_SERVER_VARIABLES);
	}
	
	@CmdPath(name = "openPlayerServerVariables",description = "Opens the server settings menu for a player",syntax = "/playerservervars",usage = "/playerservervars")
	public void openPlayerServerVariables(CommandSender sender,String[] args)
	{
		Player p = (Player) sender;
		Player playerToView = PlayerUtils.getPlayer(args[0]);
		
		PlayerServerVarsGuiBuilder.instance.setPlayerInUse(p,playerToView.getUniqueId());
		ChaosFactory.open(p,ChaosFactory.Common.PLAYER_SERVER_VARIABLES);
	}
}