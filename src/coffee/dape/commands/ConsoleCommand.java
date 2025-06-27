package coffee.dape.commands;

import java.util.UUID;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.annos.Elevated;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.VMap;
import coffee.dape.cmdparsers.astral.elevatedaccount.ConsoleAccount;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl;
import coffee.dape.cmdparsers.astral.elevatedaccount.suggestions.ElevatedAccountSuggestions;
import coffee.dape.cmdparsers.astral.parser.ArgSet;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.cmdparsers.astral.types.ArgTypes;
import coffee.dape.exception.IllegalMethodCallException;
import coffee.dape.exception.MissingAnnotationException;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.PrintUtils;


/**
 * @author Laeven
 */
@CommandEx(name = "console",alias = {"con"},description = "A command for authenticating a pending elevated command via console")
public final class ConsoleCommand extends AstralExecutor
{
	public ConsoleCommand() throws MissingAnnotationException
	{
		super(ConsoleCommand.class);
		
		addPath("setup console",CmdSender.PLAYER,new ArgSet().of("setup"));
		
		addPath("auth console",CmdSender.CONSOLE,new ArgSet().of("auth").of("<pin/password>",ArgTypes.STRING).mapTo("pin"));
		
		addPath("unlock account",CmdSender.CONSOLE,new ArgSet().of("unlock").of("<elevated account>",ArgTypes.STRING,ElevatedAccountSuggestions.elevatedAccountOwners()).mapTo("owner"));
	}
	
	@CmdPath(name = "setup console",description = "Setup the console auth pin/password",syntax = "/console auth <pin/password>",usage = "/console auth 4938gy2s")
	public void setupConsole(Player p)
	{
		if(ConsoleAccount.isSetup())
		{
			PrintUtils.error(p,"Cannot re-setup console account! Console account is already set!");
			return;
		}
		
		ElevatedAccountCtrl.setupConsoleAccount(p);
	}
	
	@CmdPath(name = "auth console",description = "Authorises the console to execute an elevated command",syntax = "/console auth <pin/password>",usage = "/console auth 4938gy2s")
	public void authConsole(ConsoleCommandSender con,@VMap("pin") String pin)
	{
		if(!ElevatedAccountCtrl.getConsoleAccount().hasPendingCommand())
		{
			PrintUtils.error(con,"You have no pending commands to authorise!");
			return;
		}
		
		if(!ConsoleAccount.isSetup())
		{
			PrintUtils.error(con,"Cannot verify console pin as console account is not setup!");
			return;
		}
		
		try
		{
			ElevatedAccountCtrl.getConsoleAccount().auth(pin);
		}
		catch (IllegalMethodCallException e)
		{
			e.printStackTrace();
		}
	}
	
	@Elevated
	@CmdPath(name = "unlock account",description = "Authorises the console to execute an elevated command",syntax = "/console auth <pin/password>",usage = "/console auth 4938gy2s")
	public final void unlockAccount(ConsoleCommandSender con,@VMap("owner") String playerName)
	{
		if(!PlayerUtils.isAPlayer(playerName))
		{
			PrintUtils.error(con,playerName + " is not a real player!");
			return;
		}
		
		UUID accountOwner = PlayerUtils.getUUID(playerName);
		
		if(!ElevatedAccountCtrl.hasElevatedAccount(accountOwner))
		{
			PrintUtils.error(con,"This player has no Elevated Account to unlock!");
			return;
		}
		
		try
		{
			ElevatedAccountCtrl.getAccount(accountOwner).unlock();
			PrintUtils.success(con,"Unlocked elevated account for owner " + playerName);
		}
		catch (IllegalMethodCallException e)
		{
			e.printStackTrace();
		}
	}
}