package coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.staticpin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

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
import coffee.dape.utils.Logg;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.chat.InputListener;
import coffee.dape.utils.security.EncryptUtils;
import coffee.dape.utils.security.HashingUtils;
import coffee.dape.utils.security.SecureByteArray;
import coffee.dape.utils.security.SecureString;
import coffee.dape.utils.structs.Namespace;

public final class StaticPinSetup implements InputListener
{
	private StaticPinSetupPhase setupState = StaticPinSetupPhase.NONE;
	private SecureString[] newPinTest = new SecureString[2];
	private static Map<UUID,StaticPinSetup> setupInstance = new HashMap<>();
	
	public static StaticPinSetup getInstance(Player p)
	{
		if(setupInstance.containsKey(p.getUniqueId())) { return setupInstance.get(p.getUniqueId()); }
		setupInstance.put(p.getUniqueId(),new StaticPinSetup());
		return setupInstance.get(p.getUniqueId());
	}
	
	public final void startSetup(Player p)
	{
		if(this.setupState != StaticPinSetupPhase.NONE) { return; }
		
		this.setupState = StaticPinSetupPhase.ASK_FOR_PIN;
		onSetup(new ChatInputEvent(p,null,null,null));
	}
	
	@InputHandler(ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_AUTH_METHOD_SETUP_STATIC_PIN)
	public static final void onChatInputStaticPinSetup(ChatInputEvent e)
	{
		getInstance(e.getPlayer()).onSetup(e);
	}
		
	public final void onSetup(ChatInputEvent e)
	{
		Player p = e.getPlayer();
		
		if(!ElevatedAccountCtrl.hasElevatedAccount(p)) { PrintUtils.error(e.getPlayer(),"Error! You don't have an elevated account!"); return; }
		if(setupState == null) { PrintUtils.error(e.getPlayer(),"Error! Incorrect static pin setup phase!"); return; }
		
		// Gathering input from player
		switch(setupState)
		{
			case NONE ->
			{
				PrintUtils.error(e.getPlayer(),"Error! Incorrect static pin setup phase!");
				return;
			}
			// Asking for the pin
			case ASK_FOR_PIN ->
			{
				setupState = StaticPinSetupPhase.RECEIVE_PIN;
				ChatUtils.requestInput(p,"Enter a pin/password of your choice",Namespace.of(Dape.getNamespaceName(),ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_AUTH_METHOD_SETUP_STATIC_PIN));
				Logg.debug("?? input??");
				return;
			}
			case RECEIVE_PIN ->
			{
				setupState = StaticPinSetupPhase.ASK_FOR_PIN_CONFIRMATION;
				newPinTest[0] = new SecureString(e.getInput());
				
				// Auto request next pin
				onSetup(e);
				return;
			}
			// Adking to confirm the pin
			case ASK_FOR_PIN_CONFIRMATION ->
			{
				setupState = StaticPinSetupPhase.RECEIVE_PIN_CONFIRMATION;
				ChatUtils.requestInput(p,"To confirm, enter the same pin/password again",Namespace.of(Dape.getNamespaceName(),ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_AUTH_METHOD_SETUP_STATIC_PIN));
				return;
			}
			case RECEIVE_PIN_CONFIRMATION ->
			{
				setupState = StaticPinSetupPhase.NONE;
				newPinTest[1] = new SecureString(e.getInput());
			}
			default ->
			{
				PrintUtils.error(e.getPlayer(),"Error! Incorrect static pin setup phase! (" + setupState + ")");
				return;
			}
		}
		
		String pin = newPinTest[0].asString();
		int maxPinLength = ElevatedAccountCtrl.Config.STATIC_PIN_MAX_PIN_LENGTH.get();
		
		// Check pin meets validation requirements about contents and length
		if(pin.isEmpty()) { PrintUtils.error(p,"Pin/Password cannot be empty!"); return; }
		if(pin.isBlank()) { PrintUtils.error(p,"Pin/Password cannot be blank!"); return; }
		if(pin.length() < 3) { PrintUtils.error(p,"Pin/Password cannot be less than 3 characters!"); return; }
		if(pin.length() > maxPinLength) { PrintUtils.error(p,"Pin/Password cannot be more than " + maxPinLength + " characters!"); return; }
		if(pin.matches("\\s+")) { PrintUtils.error(p,"Pin/Password cannot contain space characters!"); return; }
		
		byte[] tempSalt = HashingUtils.generateSalt();
		
		// Compare both pins to check they match
		if(!HashingUtils.hashToString(newPinTest[0].asString(),tempSalt).equals(HashingUtils.hashToString(newPinTest[1].asString(),tempSalt)))
		{
			PrintUtils.error(p,"Pins/Passwords entered do not match!");
			return;
		}
		
		// Package new pin into JsonObject for deserialising later
		SecureByteArray salt = new SecureByteArray(HashingUtils.generateSalt());
		SecureString newPin = new SecureString(EncryptUtils.toBase64(HashingUtils.hash(pin,salt.asByteArray())));
		
		JsonObject staticPinObj = new JsonObject();
		staticPinObj.addProperty(StaticPinAuthMethod.PIN,newPin.asString());
		staticPinObj.addProperty(StaticPinAuthMethod.SALT,EncryptUtils.toBase64(salt.asByteArray()));
		
		boolean hasExistingAuthMethod = false;
		
		// If elevated account owner already has an existing static pin method we can overwrite it
		for(AuthenticationMethod meth : ElevatedAccountCtrl.getAccount(e.getPlayer()).getAuthMethods())
		{
			if(meth.getAuthType() != AuthMethod.STATIC_PIN) { continue; }
			hasExistingAuthMethod = true;
			
			StaticPinAuthMethod staticPinMethod = (StaticPinAuthMethod) meth;
			
			try
			{
				staticPinMethod.deserialise(staticPinObj);
			}
			catch(Exception e1)
			{
				Logg.error("Error occured deserialising new static pin data to existing static pin authentication method!",e1);
				PrintUtils.error(p,"An error occured updating your StaticPin!");
				return;
			}
			
			ElevatedAccountCtrl.save(e.getPlayer().getUniqueId());
			PrintUtils.success(p,"Pin updated!");
			break;
		}
		
		// If elevated account owner does not have an existing static pin method we need to create a new one
		if(!hasExistingAuthMethod)
		{
			StaticPinAuthMethod newStaticPinMethod;
			
			try
			{
				newStaticPinMethod = new StaticPinAuthMethod(staticPinObj);
			}
			catch (DeserialiseException e1)
			{
				Logg.error("Error occured deserialising new static pin data to new static pin authentication method!",e1);
				PrintUtils.error(p,"An error occured creating your StaticPin!");
				return;
			}
			
			// If authentication methods are locked, we can't add new ones. Set as pending until server shutdown
			if(ElevatedAccountCtrl.Config.LOCKED_AUTH_METHODS.get())
			{
				try
				{
					ElevatedAccountCtrl.setPendingAuthChange(e.getPlayer().getUniqueId(),newStaticPinMethod);
					PrintUtils.success(p,"Pin pending for creation!");
					PrintUtils.info(p,"For pin change to take effect, you or someone must restart the server!");
				}
				catch (IllegalMethodCallException e1)
				{
					Logg.error("Error occured setting new static pin authentication method as a pending change!",e1);
					PrintUtils.error(p,"An error occured attempting to set your StaticPin change as pending!");
					return;
				}
			}
			// Authentication methods are not locked so we can just add a new one
			else
			{
				ElevatedAccountCtrl.getAccount(e.getPlayer()).getAuthMethods().add(newStaticPinMethod);
				ElevatedAccountCtrl.save(e.getPlayer().getUniqueId());
				PrintUtils.success(p,"Pin created!");
			}
		}
		
		// overwrite memory
		newPinTest[0] = new SecureString("null");
		newPinTest[1] = new SecureString("null");
		newPinTest = null;
		
		// Remove temp pin method if exists
		ElevatedAccountCtrl.getAccount(e.getPlayer()).markTempPinForRemoval();
		
		PrintUtils.success(p,"StaticPin setup complete!");
	}
	
	private enum StaticPinSetupPhase
	{
		NONE,
		ASK_FOR_PIN,
		RECEIVE_PIN,
		ASK_FOR_PIN_CONFIRMATION,
		RECEIVE_PIN_CONFIRMATION
	}
}
