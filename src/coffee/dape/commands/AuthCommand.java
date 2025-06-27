package coffee.dape.commands;

import org.bukkit.entity.Player;

import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.VMap;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl;
import coffee.dape.cmdparsers.astral.parser.ArgSet;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.cmdparsers.astral.types.ArgTypes;
import coffee.dape.exception.IllegalMethodCallException;
import coffee.dape.exception.MissingAnnotationException;
import coffee.dape.utils.PrintUtils;


/**
 * @author Laeven
 */
@CommandEx(name = "auth",description = "A command for authenticating a pending elevated command")
public final class AuthCommand extends AstralExecutor
{
	public AuthCommand() throws MissingAnnotationException
	{
		super(AuthCommand.class);
		
		addPath("auth player",CmdSender.PLAYER,new ArgSet().of("<value>",ArgTypes.STRING).mapTo("value"));
	}
	
	@CmdPath(name = "auth player",description = "Authorises a player to execute an elevated command",syntax = "/auth <pin>",usage = "/auth 4938gy2s")
	public final void authPlayer(Player p,@VMap("value") String value)
	{
		if(!ElevatedAccountCtrl.hasElevatedAccount(p) || ElevatedAccountCtrl.getAccount(p).isMarkedAsDeleted())
		{
			PrintUtils.error(p,"You do not have an elevated account!");
			return;
		}
		
		if(!ElevatedAccountCtrl.getAccount(p).hasPendingCommand())
		{
			PrintUtils.error(p,"You have no pending commands to authorise!");
			return;
		}
		
		try
		{
			ElevatedAccountCtrl.getAccount(p.getUniqueId()).auth(value,p);
		}
		catch (IllegalMethodCallException e)
		{
			e.printStackTrace();
		}
	}
}