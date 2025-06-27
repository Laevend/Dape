package coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.totp;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;

import coffee.dape.Dape;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.AuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.AuthenticationMethod;
import coffee.dape.event.ChatInputEvent;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.IllegalMethodCallException;
import coffee.dape.utils.ChatUtils;
import coffee.dape.utils.ChatUtils.InputHandler;
import coffee.dape.utils.InventoryUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.MapUtils;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.chat.InputListener;
import coffee.dape.utils.structs.Namespace;

public final class TOTPSetup implements InputListener
{
	private TOTPSetupPhase setupState = TOTPSetupPhase.NONE;
	private TimedOTPAuthMethod totpMethod;
	private static Map<UUID,TOTPSetup> setupInstance = new HashMap<>();
	
	public static TOTPSetup getInstance(Player p)
	{
		if(setupInstance.containsKey(p.getUniqueId())) { return setupInstance.get(p.getUniqueId()); }
		setupInstance.put(p.getUniqueId(),new TOTPSetup());
		return setupInstance.get(p.getUniqueId());
	}
	
	public final void startSetup(Player p)
	{
		if(this.setupState != TOTPSetupPhase.NONE) { return; }
		
		this.setupState = TOTPSetupPhase.GENERATE_TOTP_SECRET;
		onSetup(new ChatInputEvent(p,null,null,null));
	}
	
	@InputHandler(ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_AUTH_METHOD_SETUP_TOTP)
	public static final void onChatInputStaticPinSetup(ChatInputEvent e)
	{
		getInstance(e.getPlayer()).onSetup(e);
	}
		
	public final void onSetup(ChatInputEvent e)
	{
		Player p = e.getPlayer();
		
		if(!ElevatedAccountCtrl.hasElevatedAccount(p)) { PrintUtils.error(e.getPlayer(),"Error! You don't have an elevated account!"); return; }
		if(setupState == null) { PrintUtils.error(e.getPlayer(),"Error! Incorrect TOTP setup state!"); return; }
		
		// Gathering input from player
		switch(setupState)
		{
			case NONE ->
			{
				PrintUtils.error(e.getPlayer(),"Error! Incorrect TOTP setup state!");
				return;
			}
			case GENERATE_TOTP_SECRET ->
			{
				setupState = TOTPSetupPhase.TEST_TOTP;
				
				totpMethod = new TimedOTPAuthMethod();				
				BufferedImage qrCode = totpMethod.getQrCode(e.getPlayer().getUniqueId());
				ItemStack stack = MapUtils.personalImageToMap(qrCode,e.getPlayer().getUniqueId());
				
				InventoryUtils.safeInventorySetDropExtra(p,stack);
				
				PrintUtils.info(p,"A new timed one-time-passcode secret has been generated. Scan the QR Code on the map you have been given in your authenticator app of choice.");
				ChatUtils.requestInput(p,"Enter your timed one-time-passcode",Namespace.of(Dape.getNamespaceName(),ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_AUTH_METHOD_SETUP_TOTP));
				return;
			}
			case TEST_TOTP ->
			{
				setupState = TOTPSetupPhase.NONE;
			}
			default ->
			{
				PrintUtils.error(e.getPlayer(),"Error! Incorrect TOTP setup state! (" + setupState + ")");
				return;
			}
		}
		
		String totpCode = e.getInput();
		if(!totpMethod.verifyMethod(totpCode,p)) { return; }
		
		JsonObject totpObj = new JsonObject();
		totpObj.addProperty(TimedOTPAuthMethod.SECRET,totpMethod.getSecret().asString());
		
		boolean hasExistingAuthMethod = false;
		
		// If elevated account owner already has an existing static pin method we can overwrite it
		for(AuthenticationMethod meth : ElevatedAccountCtrl.getAccount(e.getPlayer()).getAuthMethods())
		{
			if(meth.getAuthType() != AuthMethod.TIMED_OTP) { continue; }
			hasExistingAuthMethod = true;
			
			TimedOTPAuthMethod existingTOTPMethod = (TimedOTPAuthMethod) meth;
			
			try
			{
				existingTOTPMethod.deserialise(totpObj);
			}
			catch(Exception e1)
			{
				Logg.error("Error occured deserialising new TOTP data to existing TOTP authentication method!",e1);
				PrintUtils.error(p,"An error occured updating your timed one-time-passcode!");
				return;
			}
			
			ElevatedAccountCtrl.save(e.getPlayer().getUniqueId());
			PrintUtils.success(p,"TOTP updated!");
			break;
		}
		
		// If elevated account owner does not have an existing static pin method we need to create a new one
		if(!hasExistingAuthMethod)
		{
			TimedOTPAuthMethod newTOTPMethod;
			
			try
			{
				newTOTPMethod = new TimedOTPAuthMethod(totpObj);
			}
			catch (DeserialiseException e1)
			{
				Logg.error("Error occured deserialising new TOTP data to new TOTP authentication method!",e1);
				PrintUtils.error(p,"An error occured creating your TOTP!");
				return;
			}
			
			// If authentication methods are locked, we can't add new ones. Set as pending until server shutdown
			if(ElevatedAccountCtrl.Config.LOCKED_AUTH_METHODS.get())
			{
				try
				{
					ElevatedAccountCtrl.setPendingAuthChange(e.getPlayer().getUniqueId(),newTOTPMethod);
					PrintUtils.success(p,"TOTP pending for creation!");
					PrintUtils.info(p,"For TOTP change to take effect, you or someone must restart the server!");
				}
				catch (IllegalMethodCallException e1)
				{
					Logg.error("Error occured setting new TOTP authentication method as a pending change!",e1);
					PrintUtils.error(p,"An error occured attempting to set your TOTP change as pending!");
					return;
				}
			}
			// Authentication methods are not locked so we can just add a new one
			else
			{
				ElevatedAccountCtrl.getAccount(e.getPlayer()).getAuthMethods().add(newTOTPMethod);
				ElevatedAccountCtrl.save(e.getPlayer().getUniqueId());
				PrintUtils.success(p,"TOTP created!");
			}
		}
		
		// overwrite memory
		totpMethod = null;
		
		// Remove temp pin method if exists
		ElevatedAccountCtrl.getAccount(e.getPlayer()).markTempPinForRemoval();
		
		PrintUtils.success(p,"TOTP setup complete!");
	}
	
	private enum TOTPSetupPhase
	{
		NONE,
		GENERATE_TOTP_SECRET,
		TEST_TOTP
	}
}
