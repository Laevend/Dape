package coffee.dape.commands;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import coffee.dape.Dape;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.annos.Elevated;
import coffee.dape.cmdparsers.astral.annos.VMap;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccount;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.AuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.AuthenticationMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.staticpin.StaticPinAuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.totp.TimedOTPAuthMethod;
import coffee.dape.cmdparsers.astral.parser.ArgSet;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.cmdparsers.astral.suggestions.Suggestions;
import coffee.dape.cmdparsers.astral.types.ArgTypes;
import coffee.dape.exception.MissingAnnotationException;
import coffee.dape.utils.ChatUtils;
import coffee.dape.utils.MapUtils;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.security.ObfuscatedRandBaseEncoder;
import coffee.dape.utils.security.SecureString;
import coffee.dape.utils.structs.Namespace;


/**
 * @author Laeven
 */
@Elevated
@CommandEx(name = "elevate",description = "A command for adding/removing elevated accounts to players")
public class ElevateCommand extends AstralExecutor
{
	public ElevateCommand() throws MissingAnnotationException
	{
		super(ElevateCommand.class);
		
		addPath("elevate create",ElevatedAccountCtrl.Config.ONLY_CONSOLE_CAN_CREATE_NEW_ELEVATED_ACCOUNTS.get() ? CmdSender.CONSOLE : CmdSender.ANY,new ArgSet().of("create").of("<player>",ArgTypes.ONLINE_PLAYER,Suggestions.onlinePlayerNames()).mapTo("player"));
		
		addPath("elevate delete",ElevatedAccountCtrl.Config.ONLY_CONSOLE_CAN_CREATE_NEW_ELEVATED_ACCOUNTS.get() ? CmdSender.CONSOLE : CmdSender.ANY,new ArgSet().of("delete").of("<player>",ArgTypes.OFFLINE_PLAYER,Suggestions.onlinePlayerNames()).mapTo("player"));
		
		addPath("elevate force change pin",CmdSender.CONSOLE,new ArgSet().of("force-change-pin").of("<pin>",ArgTypes.SECURE_STRING).mapTo("pin").of("<player>",ArgTypes.OFFLINE_PLAYER,Suggestions.onlinePlayerNames()).mapTo("player"));
		
		addPath("elevate change pin",CmdSender.PLAYER,new ArgSet().of("change-pin").of("<old pin>",ArgTypes.SECURE_STRING).mapTo("old_pin").of("<new pin>",ArgTypes.SECURE_STRING).mapTo("new_pin"));
		
		addPath("elevate get secret",CmdSender.PLAYER,new ArgSet().of("peek-totp"));
		
		addPath("elevate setup",CmdSender.ANY,new ArgSet().of("setup"));
		
		addPath("elevate test",CmdSender.ANY,new ArgSet().of("test"));
	}
	
	@CmdPath(name = "elevate create",description = "Creates an elevated account",syntax = "/elevate create <player>",usage = "/elevate create Laeven_")
	public void elevateCreate(CommandSender con,@VMap("player") Player p)
	{
		// Check if this player already has an elevated account and that is it not already loaded and marked for deletion
		if(ElevatedAccountCtrl.hasElevatedAccount(p) && (ElevatedAccountCtrl.isAccountLoaded(p) && !ElevatedAccountCtrl.getAccount(p).isMarkedAsDeleted()))
		{
			PrintUtils.error(con,"This player already has an elevated account!");
			return;
		}
		
		if(ElevatedAccountCtrl.createNewElevatedAccount(p))
		{
			if(ElevatedAccountCtrl.Config.LOCK_ACCOUNT_ADDING.get())
			{
				PrintUtils.success(con,"Created elevated account for " + p.getName() + ". It will become active after a server restart.");
			}
			else
			{
				PrintUtils.success(con,"Created elevated account for " + p.getName() + ". Account is now active.");
			}
		}
	}
	
	@CmdPath(name = "elevate delete",description = "Delete an elevated account",syntax = "/elevate delete <player>",usage = "/elevate delete Laeven_")
	public void elevateRemove(CommandSender con,@VMap("player") OfflinePlayer op)
	{
		if(!ElevatedAccountCtrl.hasElevatedAccount(op.getUniqueId()) || ElevatedAccountCtrl.getAccount(op.getUniqueId()).isMarkedAsDeleted())
		{
			PrintUtils.error(con,"This player does not have an elevated account!");
			return;
		}
		
		ElevatedAccountCtrl.removeElevatedAccount(op.getUniqueId());
		PrintUtils.success(con,"Removed elevated account for " + op.getName());
	}
	
	@CmdPath(name = "elevate force change pin",description = "Changes a players elevated account pin",syntax = "/elevate force-change-pin <new pin>",usage = "/elevate force-change-pin Laeven_ 59dsh412")
	public void elevateChangePin(ConsoleCommandSender con,@VMap("pin") SecureString pin,@VMap("player") OfflinePlayer op)
	{
		if(!ElevatedAccountCtrl.hasElevatedAccount(op.getUniqueId()))
		{
			PrintUtils.error(con,"This player does not have an elevated account!");
			return;
		}
		
		if(!ElevatedAccountCtrl.Config.STATIC_PIN.get())
		{
			PrintUtils.error(con,"Static pins are not enabled! Cannot change pin!");
			return;
		}
		
		ElevatedAccount acc = ElevatedAccountCtrl.getAccount(op.getUniqueId());
		
		if(acc.isLocked())
		{
			PrintUtils.error(con,"This elevated account is locked! You need to unlock it via '/elevate unlock <player>' in the console.");
			return;
		}
		
		for(AuthenticationMethod meth : acc.getAuthMethods())
		{
			if(meth.getAuthType() != AuthMethod.STATIC_PIN) { continue; }
			
			StaticPinAuthMethod pinAuthMethod = (StaticPinAuthMethod) meth;
			pinAuthMethod.setPin(pin);
			PrintUtils.success(con,"Successfully changed pin for " + PlayerUtils.getName(op.getUniqueId()));
			ElevatedAccountCtrl.save(op.getUniqueId());
			return;
		}
		
		PrintUtils.error(con,"Authentication method for static pins is missing in this elevated account! Unable to change pin!");
	}
	
	@CmdPath(name = "elevate change pin",description = "Changes the players elevated account pin",syntax = "/elevate change-pin <old pin> <new pin>",usage = "/elevate change-pin 59dsh412 9a3jd7sh3")
	public void elevateChangePin(Player p,@VMap("old_pin") SecureString oldPin,@VMap("new_pin") SecureString newPin)
	{
		if(!ElevatedAccountCtrl.hasElevatedAccount(p))
		{
			PrintUtils.error(p,"You do not have an elevated account and can't do this!");
			return;
		}
		
		if(!ElevatedAccountCtrl.Config.STATIC_PIN.get())
		{
			PrintUtils.error(p,"Static pins are not enabled! Cannot change pin!");
			return;
		}
		
		ElevatedAccount acc = ElevatedAccountCtrl.getAccount(p.getUniqueId());
		
		if(acc.isLocked())
		{
			PrintUtils.error(p,"This elevated account is locked! You need to unlock it via '/elevate unlock <player>' in the console.");
			return;
		}
		
		for(AuthenticationMethod meth : acc.getAuthMethods())
		{
			if(meth.getAuthType() != AuthMethod.STATIC_PIN) { continue; }
			
			StaticPinAuthMethod pinAuthMethod = (StaticPinAuthMethod) meth;
			
			if(ObfuscatedRandBaseEncoder.decode(pinAuthMethod.getAttempt()) >= pinAuthMethod.maxAttempts())
			{
				acc.lock();
				return;
			}
			
			if(!pinAuthMethod.changePin(oldPin,newPin))
			{
				PrintUtils.error(p,"Old pin entered does not match pin stored!");
				return;
			}
			
			PrintUtils.success(p,"Successfully changed pin");
			ElevatedAccountCtrl.save(p.getUniqueId());
			return;
		}
		
		PrintUtils.error(p,"Authentication method for static pins is missing in this elevated account! Unable to change pin!");
	}
	
	@CmdPath(name = "elevate get secret",description = "Views the QR code secret for totp",syntax = "/elevate peek-totp",usage = "/elevate peek-totp")
	public void getTOTPSecret(Player p)
	{
		if(!ElevatedAccountCtrl.hasElevatedAccount(p.getUniqueId()))
		{
			PrintUtils.error(p,"This player does not have an elevated account!");
			return;
		}
		
		if(!ElevatedAccountCtrl.Config.TOTP.get())
		{
			PrintUtils.error(p,"Timed one-time-passcodes are not enabled! Cannot peek at TOTP secret!");
			return;
		}
		
		ElevatedAccount acc = ElevatedAccountCtrl.getAccount(p.getUniqueId());
		
		if(acc.isLocked())
		{
			PrintUtils.error(p,"This elevated account is locked! You need to unlock it via '/elevate unlock <player>' in the console.");
			return;
		}
		
		for(AuthenticationMethod meth : acc.getAuthMethods())
		{
			if(meth.getAuthType() != AuthMethod.TIMED_OTP) { continue; }
			PrintUtils.warn(p,"You are about to view your Totp secret! Make sure you are not streaming or recording to prevent leaks!");
			ChatUtils.requestInput(p,"Enter 'yes' or 'y' to continue and view your totp secret.",Namespace.of(Dape.getNamespaceName(),ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_VIEW_SECRET));
			return;
		}
		
		PrintUtils.error(p,"Authentication method for totp is missing in this elevated account! Unable to view secret!");
	}
	
	@CmdPath(name = "elevate setup",description = "Opens the setup menu for authentication",syntax = "/elevate setup",usage = "/elevate setup")
	public void elevateSetup(Player p)
	{
		if(!ElevatedAccountCtrl.hasElevatedAccount(p.getUniqueId()))
		{
			PrintUtils.error(p,"This player does not have an elevated account!");
			return;
		}
		
		ChaosFactory.open(p,ChaosFactory.Common.AUTHENTICATION_SETUP);
	}
	
	@CmdPath(name = "elevate test",description = "Used for testing your elevated account authentication",syntax = "/elevate test",usage = "/elevate test")
	public void elevateTestPin(CommandSender sender)
	{
		PrintUtils.info(sender,"If you're seeing this then you have successfuly executed the elevated command!");
		Player p = (Player) sender;
		
		for(AuthenticationMethod meth : ElevatedAccountCtrl.getAccount(p).getAuthMethods())
		{
			if(meth.getAuthType() == AuthMethod.TIMED_OTP)
			{
				TimedOTPAuthMethod tMeth = (TimedOTPAuthMethod) meth;
				BufferedImage qrCode = tMeth.getQrCode(p.getUniqueId());
				try
				{
					ImageIO.write(qrCode,"png",Dape.internalFilePath("qrcode.png").toFile());
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ItemStack stack = MapUtils.personalImageToMap(qrCode,p.getUniqueId());
				p.getInventory().addItem(stack);
			}
		}
	}
}