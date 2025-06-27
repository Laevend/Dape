package coffee.dape.playerdata.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.annos.VMap;
import coffee.dape.cmdparsers.astral.parser.ArgSet;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.cmdparsers.astral.suggestions.Suggestions;
import coffee.dape.cmdparsers.astral.types.ArgTypes;
import coffee.dape.exception.MissingAnnotationException;


/**
 * @author Laeven
 */
@CommandEx(name = "playerprofile",description = "A command for viewing yours or other players profiles")
public class PlayerProfileCommand extends AstralExecutor
{
	public PlayerProfileCommand() throws MissingAnnotationException
	{
		super(PlayerProfileCommand.class);
		
		addPath("view",CmdSender.PLAYER);
		
		addPath("viewPlayer",CmdSender.PLAYER,new ArgSet().of("<player>",ArgTypes.PLAYER,Suggestions.onlinePlayerNames()).mapTo("player_uuid"));
	}
	
	@CmdPath(name = "view",description = "Opens your player profile",syntax = "/playerprofile view",usage = "/playerprofile view")
	public void view(CommandSender sender,String[] args)
	{
		Player p = (Player) sender;
		ChaosFactory.open(p,ChaosFactory.Common.PLAYER_PROFILE,p.getUniqueId());
	}
	
	@CmdPath(name = "viewPlayer",description = "Opens a players profile",syntax = "/playerprofile view <player>",usage = "/playerprofile view Laeven_")
	public void viewPlayer(CommandSender sender,@VMap("player_uuid") UUID playerUUID)
	{
		ChaosFactory.open((Player) sender,ChaosFactory.Common.PLAYER_PROFILE,playerUUID);
	}
}