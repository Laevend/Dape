package coffee.dape.playerdata.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.exception.MissingAnnotationException;


/**
 * 
 * @author Laeven
 *
 */
@CommandEx(name = "settings",alias = {"sett"},description = "A command for viewing your server settings")
public class PlayerSettingsCommand extends AstralExecutor
{
	public PlayerSettingsCommand() throws MissingAnnotationException
	{
		super(PlayerSettingsCommand.class);
		
		addPath("openPlayerSettings",CmdSender.PLAYER);
	}
	
	@CmdPath(name = "openPlayerSettings",description = "Opens your server settings menu",syntax = "/playersettings",usage = "/playersettings")
	public void openPlayerSettings(CommandSender sender,String[] args)
	{
		Player p = (Player) sender;
		ChaosFactory.open(p,ChaosFactory.Common.PLAYER_SETTINGS);
	}
}