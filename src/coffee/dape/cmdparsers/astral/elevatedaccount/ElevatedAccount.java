package coffee.dape.cmdparsers.astral.elevatedaccount;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.AuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.AuthenticationMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.MicrosoftAuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.YubiKeyAuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.email.EmailOTPAuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.staticpin.StaticPinAuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.temppin.TempPinAuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.totp.TimedOTPAuthMethod;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.IllegalMethodCallException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.utils.Logg;
import coffee.dape.utils.MathUtils;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.clocks.RefillableIntervalClock;
import coffee.dape.utils.json.PersistJson;
import coffee.dape.utils.security.Bouncer;
import coffee.dape.utils.security.HashingUtils;
import coffee.dape.utils.security.ObfuscatedRandBaseEncoder;
import coffee.dape.utils.security.SecureByteArray;
import coffee.dape.utils.security.SecureString;
import coffee.dape.utils.tools.Deserialise;

public final class ElevatedAccount extends RefillableIntervalClock implements PersistJson
{
	private final UUID owner;
	
	private final SecureByteArray creationDate;
	private SecureByteArray lastAuthDate = ObfuscatedRandBaseEncoder.encode(0L);
	private final SecureByteArray cooldownInMili;
	private SecureByteArray locked;
	private SecureByteArray markedAsDeleted;
	private final List<AuthenticationMethod> authMethods;
	
	private PendingCommand pendingCommand = null;
	private SecureByteArray authLvl = ObfuscatedRandBaseEncoder.encode(0);
	private SecureByteArray checksum = null;
	
	protected ElevatedAccount(final UUID owner,final SecureString tempPin)
	{
		// divided by 1000 as the cooldownInMili time here is in miliseconds but we want ticks so divide by 50 and add 1 as if cooldownInMili does not divide perfectly, we can't have part of a tick
		super("ElevatedAccount_" + PlayerUtils.getName(owner),(ElevatedAccountCtrl.Config.AUTH_TIME.get() / 50) + 1);
		this.owner = owner;
		this.creationDate = ObfuscatedRandBaseEncoder.encode(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		this.cooldownInMili = ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.Config.AUTH_TIME.get());
		
		List<AuthenticationMethod> methods = new ArrayList<>();
		
		methods.add(new TempPinAuthMethod(tempPin));
		
		if(ElevatedAccountCtrl.Config.LOCKED_AUTH_METHODS.get())
		{
			this.authMethods = Collections.unmodifiableList(methods);
		}
		else
		{
			this.authMethods = methods;
		}
		
		this.locked = ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.FALSE);
		this.markedAsDeleted = ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.FALSE);
	}
	
	protected ElevatedAccount(final UUID owner,final long creationDate,final boolean locked,final List<AuthenticationMethod> authMethods)
	{
		// divided by 1000 as the cooldownInMili time here is in miliseconds but we want ticks so divide by 50 and add 1 as if cooldownInMili does not divide perfectly, we can't have part of a tick
		super("ElevatedAccount_" + PlayerUtils.getName(owner),(ElevatedAccountCtrl.Config.AUTH_TIME.get() / 50) + 1);
		this.owner = owner;
		this.creationDate = ObfuscatedRandBaseEncoder.encode(creationDate);
		this.cooldownInMili = ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.Config.AUTH_TIME.get());
		
		if(ElevatedAccountCtrl.Config.LOCKED_AUTH_METHODS.get())
		{
			this.authMethods = Collections.unmodifiableList(authMethods);
		}
		else
		{
			this.authMethods = authMethods;
		}
		
		this.locked = locked ? ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.TRUE) : ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.FALSE);
		this.markedAsDeleted = ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.FALSE);
	}
	
	protected ElevatedAccount(final JsonObject obj) throws DeserialiseException
	{
		// divided by 1000 as the cooldownInMili time here is in miliseconds but we want ticks so divide by 50 and add 1 as if cooldownInMili does not divide perfectly, we can't have part of a tick
		super("ElevatedAccount_Unknown",(ElevatedAccountCtrl.Config.AUTH_TIME.get() / 50) + 1);
		
		this.owner = Deserialise.uuid(Deserialise.assertAndGetProperty(OWNER,Deserialise.Type.STRING,obj));
		this.creationDate = ObfuscatedRandBaseEncoder.encode(Deserialise.assertAndGetProperty(CREATION_DATE,Deserialise.Type.NUMBER,obj).getAsLong());
		this.cooldownInMili = ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.Config.AUTH_TIME.get());
		
		List<AuthenticationMethod> methods = new ArrayList<>();
		
		JsonObject tempPin = Deserialise.assertAndGetPropertyIfHas(TEMP_PIN_AUTH,Deserialise.Type.JSON_OBJECT,obj,new JsonObject());
		JsonObject staticPin = Deserialise.assertAndGetPropertyIfHas(STATIC_PIN_AUTH,Deserialise.Type.JSON_OBJECT,obj,new JsonObject());
		JsonObject timedTOTP = Deserialise.assertAndGetPropertyIfHas(TOTP_AUTH,Deserialise.Type.JSON_OBJECT,obj,new JsonObject());
		JsonObject emailOTP = Deserialise.assertAndGetPropertyIfHas(EMAIL_AUTH,Deserialise.Type.JSON_OBJECT,obj,new JsonObject());
		JsonObject yubiKey = Deserialise.assertAndGetPropertyIfHas(YUBIKEY_AUTH,Deserialise.Type.JSON_OBJECT,obj,new JsonObject());
		JsonObject microsoftAuth = Deserialise.assertAndGetPropertyIfHas(MICROSOFT_AUTH,Deserialise.Type.JSON_OBJECT,obj,new JsonObject());
		
		if(tempPin.size() != 0)
		{
			TempPinAuthMethod meth = new TempPinAuthMethod(tempPin);
			if(!meth.isMarkedForRemoval()) { methods.add(meth); }
		}
		
		if(staticPin.size() != 0)
		{
			StaticPinAuthMethod meth = new StaticPinAuthMethod(staticPin);
			if(!meth.isMarkedForRemoval()) { methods.add(meth); }
		}
		
		if(timedTOTP.size() != 0)
		{
			TimedOTPAuthMethod meth = new TimedOTPAuthMethod(timedTOTP);
			if(!meth.isMarkedForRemoval()) { methods.add(meth); }
		}
		
		if(emailOTP.size() != 0)
		{
			EmailOTPAuthMethod meth = new EmailOTPAuthMethod(emailOTP);
			if(!meth.isMarkedForRemoval()) { methods.add(meth); }
		}
		
		if(yubiKey.size() != 0)
		{
			YubiKeyAuthMethod meth = new YubiKeyAuthMethod(yubiKey);
			if(!meth.isMarkedForRemoval()) { methods.add(meth); }
		}
		
		if(microsoftAuth.size() != 0)
		{
			MicrosoftAuthMethod meth = new MicrosoftAuthMethod(microsoftAuth);
			if(!meth.isMarkedForRemoval()) { methods.add(meth); }
		}		
		
		if(ElevatedAccountCtrl.Config.LOCKED_AUTH_METHODS.get())
		{
			this.authMethods = Collections.unmodifiableList(methods);
		}
		else
		{
			this.authMethods = methods;
		}
		
		this.locked = Deserialise.assertAndGetProperty(LOCKED,Deserialise.Type.BOOLEAN,obj).getAsBoolean() ? ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.TRUE) : ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.FALSE);
		this.markedAsDeleted = Deserialise.assertAndGetProperty(MARKED_AS_DELETED,Deserialise.Type.BOOLEAN,obj).getAsBoolean() ? ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.TRUE) : ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.FALSE);
		
		// In deserialising, we need to deserialise the owner before passing it. Here we re-set the clocks name to the accounts owner.
		this.setClockName("ElevatedAccount_" + PlayerUtils.getName(owner));
	}

	public final UUID getOwner()
	{
		return owner;
	}

	public final SecureByteArray getCreationDate()
	{
		return creationDate;
	}

	public final SecureByteArray getLastAuthDate()
	{
		return lastAuthDate;
	}

	public final PendingCommand getPendingCommand()
	{
		return pendingCommand;
	}
	
	public final void setPendingCommand(PendingCommand pendingCommand) throws IllegalMethodCallException
	{
		Bouncer.haltAllBut(coffee.dape.cmdparsers.astral.parser.AstralExecutor.class);
		
		this.pendingCommand = pendingCommand;
		this.checksum = ObfuscatedRandBaseEncoder.encode(getCommandChecksum());
		resetAuthLvl();
		if(isEnabled()) { refill(); return; }
		start();
	}
	
	public boolean hasPendingCommand()
	{
		return pendingCommand == null ? false : pendingCommand.getLabel() == null ? false : true;
	}

	public final boolean isAuthed()
	{
		return (ObfuscatedRandBaseEncoder.peek(this.lastAuthDate) + ObfuscatedRandBaseEncoder.peek(cooldownInMili)) > System.currentTimeMillis();
	}
	
	public final void auth(String value,Player p) throws IllegalMethodCallException
	{
		Bouncer.haltAllBut(coffee.dape.commands.AuthCommand.class);
		
		// Null checks
		Objects.requireNonNull(value,"ElevatedAccount auth Value cannot be null!");
		Objects.requireNonNull(p,"ElevatedAccount auth Player cannot be null!");
		
		// Check if player has a pending command at all to execute
		if(!hasPendingCommand())
		{
			PrintUtils.info(p,"You have no pending command that needs authorising.");
			Logg.error("User " + p.getName() + " attempted to auth with no pending command!");
			return;
		}
		
		// Check if a different user is somehow using someone elses account?
		if(!p.getUniqueId().equals(owner))
		{
			PrintUtils.error(p,"Authentication Failed");
			Logg.error("User " + p.getName() + " attempted to auth " + 
					(pendingCommand == null ? "(no pending cmd)" : ("'/" + pendingCommand.getLabel() + "'")) + " using account owned by " + 
					PlayerUtils.getName(owner));
			return;
		}
		
		// Check if this elevated account has at least 1 authentication method
		// Why are you using an elevated account if you don't provide an authentication method? It's akin to clicking 'yes' on a UAC prompt
		if(authMethods == null || authMethods.size() == 0)
		{
			PrintUtils.error(p,"Authentication Failed");
			Logg.error("User " + p.getName() + " attempted to auth with an account that has 0 authentication methods!");
			return;
		}
		
		long authLvl = ObfuscatedRandBaseEncoder.decode(this.authLvl);
		
		// Notify admin of an invalid auth level. Might indicate another plugin was attempting to force authentication
		if(authLvl >= authMethods.size() || authLvl < 0)
		{
			Logg.error("User " + p.getName() + " attempted to auth with an account that has an invalid AuthLvl! (" + authLvl + ")");
			this.authLvl = ObfuscatedRandBaseEncoder.encode(0);
			return;
		}
		
		// clamp auth level
		authLvl = authMethods.size() == 1 ? 0 : MathUtils.clamp(0,authMethods.size() - 1,authLvl);
		
		System.out.println("authlvl " + authLvl);
		
		AuthenticationMethod meth = authMethods.get((int) authLvl);
		
		// Error feedback handled in verify method
		if(!meth.verifyMethod(value,p))
		{
			// Re-encode auth level
			this.authLvl = ObfuscatedRandBaseEncoder.encode(authLvl);
			
			// Check attempts on auth fail
			if(ObfuscatedRandBaseEncoder.peek(meth.getAttempt()) < meth.maxAttempts()) { return; }
			
			// Gone over max attempts, lock account. Account can only be unlocked by admin account
			locked = ObfuscatedRandBaseEncoder.encodeAndReplace(ElevatedAccountCtrl.TRUE,locked);
			
			// Reset attempts
			for(AuthenticationMethod method : authMethods)
			{
				method.resetAttempt();
			}
			
			return;
		}
		
		// Check if the current auth was the last auth required
		if(authLvl == (authMethods.size() - 1))
		{
			// Account owner is now authorised for elevated commands for the duration of 'cooldownInMili'
			this.lastAuthDate = ObfuscatedRandBaseEncoder.encodeAndReplace(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),this.lastAuthDate);
			this.authLvl = ObfuscatedRandBaseEncoder.encodeAndReplace(0,this.authLvl);
			
			// Reset attempts in authentication methods
			for(AuthenticationMethod method : authMethods)
			{
				method.resetAttempt();
			}
			
			long existingChecksum = ObfuscatedRandBaseEncoder.peek(checksum);
			long newChecksum = HashingUtils.getChecksum(pendingCommand.toString().getBytes());
			
			// Check command has not been modified via reflection
			if(existingChecksum != newChecksum)
			{
				PrintUtils.error(p,"Authentication Failed");
				Logg.error("User " + p.getName() + " attempted to auth with an account that had its pending command modified!");
				return;
			}
			
			// Execute command
			PrintUtils.success(p,"Authenticated!");
			pendingCommand.executeCommand();
			pendingCommand = new PendingCommand(null,null,null,null,null,new SecureRandom().nextLong());
			
			// Stop pending command removal clock, we've removed the the command ourselves
			if(isEnabled()) { stop(); return; }
			return;
		}
		
		// Another authentication method exists, provide user with input request for this authentication method
		authLvl++;
		this.authLvl = ObfuscatedRandBaseEncoder.encode(authLvl);
		PrintUtils.info(p,authMethods.get((int) authLvl).getAuthMessage());
	}
	
	@Override
	public void execute() throws Exception
	{
		Bouncer.haltAllBut(coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccount.class,
				coffee.dape.utils.clocks.RefillableIntervalClock.class);
		
		// Clear the pending command when auth period expires to prevent a command loitering in memory
		pendingCommand = new PendingCommand(null,null,null,null,null,new SecureRandom().nextLong());
		Logg.verb("Pending command for ElevatedAccount " + PlayerUtils.getName(owner) + " was cleared automatically",Logg.VerbGroup.ELEVATED_ACCOUNT);
	}
	
	private final long getCommandChecksum()
	{
		return HashingUtils.getChecksum(pendingCommand.toString().getBytes());
	}
	
	public final boolean isLocked()
	{
		return ObfuscatedRandBaseEncoder.peek(locked) == ElevatedAccountCtrl.TRUE;
	}
	
	/**
	 * Locks the account
	 */
	public final void lock()
	{
		locked = ObfuscatedRandBaseEncoder.encodeAndReplace(ElevatedAccountCtrl.TRUE,locked);
	}
	
	/**
	 * Unlocks the account
	 * @throws IllegalMethodCallException 
	 */
	public final void unlock() throws IllegalMethodCallException
	{
		Bouncer.haltAllBut(coffee.dape.commands.ConsoleCommand.class);
		locked = ObfuscatedRandBaseEncoder.encodeAndReplace(ElevatedAccountCtrl.FALSE,locked);
	}

	protected void markAsDeleted(boolean markDeleted)
	{
		this.markedAsDeleted = ObfuscatedRandBaseEncoder.encodeAndReplace(markDeleted ? ElevatedAccountCtrl.TRUE : ElevatedAccountCtrl.FALSE,this.markedAsDeleted);
	}

	/**
	 * Elevated accounts cannot be deleted or added once the server is online as they
	 * exist in a final unmodifiable map. Marking an account as deleted is the only way to remove an account
	 * @return
	 */
	public final boolean isMarkedAsDeleted()
	{
		return ObfuscatedRandBaseEncoder.peek(markedAsDeleted) == ElevatedAccountCtrl.TRUE;
	}
	
	public final List<AuthenticationMethod> getAuthMethods()
	{
		return authMethods;
	}
	
	/**
	 * Marks the temporary auth method for removal (if it exists)
	 */
	public final void markTempPinForRemoval()
	{
		for(int i = 0; i < authMethods.size(); i++)
		{
			if(authMethods.get(i).getAuthType() == AuthMethod.TEMP_PIN)
			{
				authMethods.get(i).markForRemoval();
				break;
			}
		}
	}
	
	/**
	 * Resets the authorisation stage to 0 in the event another command is executed before a
	 * player gets through all auth lvls to run the pending command.
	 */
	public void resetAuthLvl()
	{
		this.authLvl = ObfuscatedRandBaseEncoder.encodeAndReplace(0,this.authLvl);
	}

	/**
	 * Called by garbage collector.
	 * <p>
	 * {@inheritDoc}
	 */
	@SuppressWarnings("removal")
	@Override
	public void finalize() throws Throwable
	{
		try { clear(); }
		finally { super.finalize(); }
	}
	
	/**
	 * Overwrite variables with garbage data when account is garbage collected.
	 * <p>
	 * Also used when account adding & removing is locked and the account is marked for deletion.
	 */
	public final void clear()
	{
		SecureRandom sr = new SecureRandom();
		
		byte[] authDateScrambled = new byte[sr.nextInt(16,32)];
		byte[] lockedScrambled = new byte[sr.nextInt(16,32)];
		
		sr.nextBytes(authDateScrambled);
		sr.nextBytes(lockedScrambled);
		
		// Remove obfuscated references from instances		
		ObfuscatedRandBaseEncoder.remove(creationDate);
		ObfuscatedRandBaseEncoder.remove(lastAuthDate);
		ObfuscatedRandBaseEncoder.remove(cooldownInMili);
		ObfuscatedRandBaseEncoder.remove(locked);
		ObfuscatedRandBaseEncoder.remove(markedAsDeleted);
		ObfuscatedRandBaseEncoder.remove(authLvl);
		ObfuscatedRandBaseEncoder.remove(checksum);
		
		this.lastAuthDate = new SecureByteArray(authDateScrambled);
		this.pendingCommand = new PendingCommand(null,null,null,null,null,sr.nextLong());
		this.locked = new SecureByteArray(lockedScrambled);
		
		for(AuthenticationMethod meth : authMethods)
		{
			meth.clear();
		}
	}

	public static final String OWNER = "owner";
	public static final String CREATION_DATE = "creation_date";
	public static final String LOCKED = "locked";
	public static final String MARKED_AS_DELETED = "marked_as_deleted";
	
	public static final String TEMP_PIN_AUTH = "temp_pin_auth";
	public static final String STATIC_PIN_AUTH = "static_pin_auth";
	public static final String TOTP_AUTH = "totp_auth";
	public static final String EMAIL_AUTH = "email_auth";
	public static final String YUBIKEY_AUTH = "yubikey_auth";
	public static final String MICROSOFT_AUTH = "microsoft_auth";
	
	@Override
	public JsonObject serialise() throws SerialiseException
	{
		JsonObject obj = new JsonObject();
		
		obj.addProperty(OWNER,this.owner.toString());
		obj.addProperty(CREATION_DATE,ObfuscatedRandBaseEncoder.peek(creationDate));
		obj.addProperty(LOCKED,isLocked());
		obj.addProperty(MARKED_AS_DELETED,isMarkedAsDeleted());
		
		for(AuthenticationMethod authMethod : this.authMethods)
		{
			if(authMethod.isMarkedForRemoval())
			{
				Logg.info("Auth method '" + authMethod.getAuthType().toString().toLowerCase() + "' marked for removal.");
				continue;
			}
			
			switch(authMethod.getAuthType())
			{
				case TEMP_PIN -> obj.add(TEMP_PIN_AUTH,((PersistJson) authMethod).serialise());
				case STATIC_PIN -> obj.add(STATIC_PIN_AUTH,((PersistJson) authMethod).serialise());
				case TIMED_OTP -> obj.add(TOTP_AUTH,((PersistJson) authMethod).serialise());
				case EMAIL_OTP -> obj.add(EMAIL_AUTH,((PersistJson) authMethod).serialise());
				case YUBI_KEY -> obj.add(YUBIKEY_AUTH,((PersistJson) authMethod).serialise());
				case MICROSOFT_AUTH -> obj.add(MICROSOFT_AUTH,((PersistJson) authMethod).serialise());
				default ->
				{
					throw new SerialiseException("Unknown auth type! '" + authMethod.getAuthType() + "'");
				}
			}
		}
		
		return obj;
	}

	@Override
	public void deserialise(JsonObject obj) throws DeserialiseException
	{
		throw new UnsupportedOperationException("Due to declared final variables deserialise happens in constructor 'ElevatedAccount(final JsonObject obj)'");
	}
}
