package coffee.dape.cmdparsers.astral.elevatedaccount;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;

import coffee.dape.Dape;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.AuthenticationMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.totp.TimedOTPAuthMethod;
import coffee.dape.config.Configurable;
import coffee.dape.config.items.ConfigItem;
import coffee.dape.event.ChatInputEvent;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.IllegalMethodCallException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.utils.ChatBuilder;
import coffee.dape.utils.ChatUtils;
import coffee.dape.utils.ChatUtils.InputHandler;
import coffee.dape.utils.ChecksumUtils;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.FUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.MapUtils;
import coffee.dape.utils.MathUtils;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.StringUtils;
import coffee.dape.utils.chat.InputListener;
import coffee.dape.utils.json.JUtils;
import coffee.dape.utils.json.PersistJson;
import coffee.dape.utils.security.Bouncer;
import coffee.dape.utils.security.HashingUtils;
import coffee.dape.utils.security.SecureString;
import coffee.dape.utils.structs.Namespace;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * 
 * @author Laeven
 * Controller for ElevatedAccounts
 */
public class ElevatedAccountCtrl implements InputListener
{
	private static final Map<UUID,ElevatedAccount> accounts;
	private static ConsoleAccount conAcc;
	private static final Path ELEVATED_ACCOUNTS_DIR = Dape.internalFilePath("elevated");
	public static final int TRUE;
	public static final int FALSE;
	private static ConsoleSetupPhase consoleSetupPhase = ConsoleSetupPhase.NONE;
	private static SecureString[] newPinTest = new SecureString[2];
	
	/**
	 * Changes to an authentication method from an elevated account user that can't be changed until the server stops
	 * Only applies when {@linkplain Config#LOCKED_AUTH_METHODS} is true
	 */
	private static final Map<UUID,List<AuthenticationMethod>> pendingAuthChanges = new HashMap<>();
	
	static
	{
		SecureRandom sr = new SecureRandom();
		TRUE = sr.nextInt(1,Integer.MAX_VALUE);
		FALSE = sr.nextInt(1,Integer.MAX_VALUE);
		
		accounts = new HashMap<>();
		
		if(TRUE == FALSE)
		{
			Logg.fatal("Elevated Account TRUE & FALSE definitions are the same!");
			Dape.forceShutdown();
		}
	}
	
	// Initialise
	public static final void init()
	{
		if(!ConsoleAccount.isSetup())
		{
			conAcc = new ConsoleAccount();
			Logg.print();
			Logg.fatal("Dape could not find a console account, create a new console account using /console setup");
			Logg.print();
			return;
		}
		
		try
		{
			String data = new String(Files.readAllBytes(Dape.internalFilePath("elevated" + File.separator + "console")));
			String[] dataParts = data.split(",");
			conAcc = new ConsoleAccount(new SecureString(dataParts[0]),Base64.getDecoder().decode(dataParts[1]));
		}
		catch (IOException e)
		{
			Logg.error("Could not load console account pin/password!",e);
			conAcc = new ConsoleAccount();
			return;
		}
		
		loadAll();
	}
	
	/**
	 * Set a pending change to an authentication method of an elevated account.
	 * <p>
	 * This pending change is committed when the server shuts down
	 * @param uuid UUID of an owner of an ElevatedAccount
	 * @param meth Authentication method to overwrite
	 * @throws IllegalMethodCallException 
	 */
	public static final void setPendingAuthChange(UUID uuid,AuthenticationMethod meth) throws IllegalMethodCallException
	{
		Bouncer.haltAllBut(coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.class,
				coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.staticpin.StaticPinSetup.class,
				coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.totp.TOTPSetup.class);
		
		if(hasElevatedAccount(uuid))
		{
			Logg.error("Player " + PlayerUtils.getName(uuid) + " is not an owner of an elevated account and thus cannot set a pending authentication change!");
			return;
		}
		
		if(!pendingAuthChanges.containsKey(uuid))
		{
			pendingAuthChanges.put(uuid,new ArrayList<>());
		}
		
		pendingAuthChanges.get(uuid).add(meth);
	}
	
	@InputHandler(ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_CONSOLE_SETUP)
	public static final void onChatInputConsoleSetup(ChatInputEvent e)
	{
		Player p = e.getPlayer();
		
		if(consoleSetupPhase == null || consoleSetupPhase == ConsoleSetupPhase.NONE)
		{
			PrintUtils.error(e.getPlayer(),"Error! Incorrect console setup phase! Restart server and try again!");
			return;
		}
		
		if(consoleSetupPhase == ConsoleSetupPhase.ASKING_FOR_PIN_OR_PASSWORD_1)
		{
			newPinTest[0] = new SecureString(e.getInput());
			consoleSetupPhase = ConsoleSetupPhase.ASKING_FOR_PIN_OR_PASSWORD_2;
			ChatUtils.requestInput(p,"To confirm, enter the same pin/password again",Namespace.of(Dape.getNamespaceName(),ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_CONSOLE_SETUP));
			return;
		}
		
		if(consoleSetupPhase != ConsoleSetupPhase.ASKING_FOR_PIN_OR_PASSWORD_2) { return; }
		consoleSetupPhase = ConsoleSetupPhase.NONE;
		newPinTest[1] = new SecureString(e.getInput());
		
		String pin = newPinTest[0].asString();
		
		if(pin.isEmpty()) { PrintUtils.error(p,"Pin/Password cannot be empty!"); return; }
		if(pin.isBlank()) { PrintUtils.error(p,"Pin/Password cannot be blank!"); return; }
		if(pin.length() < 9) { PrintUtils.error(p,"Pin/Password cannot be less than 9 characters!"); return; }
		if(pin.matches("\\s+")) { PrintUtils.error(p,"Pin/Password cannot contain space characters!"); return; }
		
		byte[] tempSalt = HashingUtils.generateSalt();
		
		if(!HashingUtils.hashToString(newPinTest[0].asString(),tempSalt).equals(HashingUtils.hashToString(newPinTest[1].asString(),tempSalt)))
		{
			PrintUtils.error(p,"Pins/Passwords entered do not match!");
			return;
		}
		
		try
		{
			conAcc.setPin(new SecureString(pin));
		}
		catch (IllegalMethodCallException ex)
		{
			ex.printStackTrace();
		}
		
		// overwrite memory
		newPinTest[0] = new SecureString("null");
		newPinTest[1] = new SecureString("null");
		newPinTest = null;
		
		try
		{
			FUtils.createDirectoriesForFile(Dape.internalFilePath("elevated" + File.separator + "console"));
			Files.write(Dape.internalFilePath("elevated" + File.separator + "console"),new String(conAcc.getHashedPin().asString() + "," + Base64.getEncoder().encodeToString(conAcc.getSalt().asByteArray())).getBytes());
		}
		catch (IOException ex)
		{
			Logg.error("Could not save console account pin/password!",ex);
			return;
		}
		
		PrintUtils.success(p,"Console setup complete!");
	}
	
	/**
	 * Method for first time setup of console auth
	 * @param p Player setting up console account
	 */
	public static final void setupConsoleAccount(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		Bouncer.probe();
		
		// Prevents re-setting up
		if(ConsoleAccount.isSetup()) { return; }
		if(consoleSetupPhase != null && consoleSetupPhase != ConsoleSetupPhase.NONE) { return; }
		
		consoleSetupPhase = ConsoleSetupPhase.ASKING_FOR_PIN_OR_PASSWORD_1;
		ChatUtils.requestInput(p,"Enter a pin/password that will be used to authorise any elevated commands via the console",Namespace.of(Dape.getNamespaceName(),ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_CONSOLE_SETUP));
	}
	
	public static final boolean createNewElevatedAccount(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		
		if(accounts.containsKey(p.getUniqueId())) { return false; }
		if(Files.exists(Paths.get(ELEVATED_ACCOUNTS_DIR + File.separator + p.getUniqueId().toString() + ".json"))) { return false; }
		
		SecureString temporaryPin = new SecureString(StringUtils.getRandomAlphaNumeric(4));
		ElevatedAccount newAcc = new ElevatedAccount(p.getUniqueId(),temporaryPin);
		
		if(!save(newAcc)) { return false; }
		
		PrintUtils.info(p,"Your elevated account has been created.");
		PrintUtils.info(p,"You must setup alternate forms of authentication. This can be done through the account setup menu which can be opened via '/elevate setup'.");
		PrintUtils.info(p,"Your temporary pin is: " + temporaryPin.asString());
		
		if(Config.LOCK_ACCOUNT_ADDING.get())
		{
			Logg.info("Elevated account for player '" + p.getName() + "' created. Restart server to take effect.");
			return true;
		}
		
		accounts.put(newAcc.getOwner(),newAcc);
		Logg.info("Elevated account for player '" + p.getName() + "' created. Account is active immediately.");
		return true;
	}
	
	public static final boolean hasElevatedAccount(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		
		return hasElevatedAccountInMemory(p.getUniqueId()) || Files.exists(Paths.get(ELEVATED_ACCOUNTS_DIR + File.separator + p.getUniqueId().toString() + ".json"));
	}
	
	public static final boolean hasElevatedAccount(UUID uuid)
	{
		Objects.requireNonNull(uuid,"UUID cannot be null!");
		
		return hasElevatedAccountInMemory(uuid) || Files.exists(Paths.get(ELEVATED_ACCOUNTS_DIR + File.separator + uuid.toString() + ".json"));
	}
	
	public static final boolean hasElevatedAccountInMemory(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		
		return accounts.containsKey(p.getUniqueId());
	}
	
	public static final boolean hasElevatedAccountInMemory(UUID uuid)
	{
		Objects.requireNonNull(uuid,"UUID cannot be null!");
		
		return accounts.containsKey(uuid);
	}
	
	public static final boolean isAccountLoaded(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		
		return accounts.containsKey(p.getUniqueId());
	}
	
	public static final boolean isAccountLoaded(UUID uuid)
	{
		Objects.requireNonNull(uuid,"UUID cannot be null!");
		
		return accounts.containsKey(uuid);
	}
	
	public static final boolean hasElevatedAccountLoaded(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		
		return accounts.containsKey(p.getUniqueId());
	}
	
	public static final boolean hasElevatedAccountLoaded(UUID uuid)
	{
		Objects.requireNonNull(uuid,"UUID cannot be null!");
		
		return accounts.containsKey(uuid);
	}
	
	public static final void removeElevatedAccount(UUID uuid)
	{
		Objects.requireNonNull(uuid,"UUID cannot be null!");
		
		if(accounts.containsKey(uuid))
		{
			// Clear sooner rather than waiting for GC
			accounts.get(uuid).clear();
			accounts.get(uuid).markAsDeleted(true);
		}

		FUtils.delete(Paths.get(ELEVATED_ACCOUNTS_DIR + File.separator + uuid.toString() + ".json"));
	}
	
	public static ElevatedAccount getAccount(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		
		if(!accounts.containsKey(p.getUniqueId())) { return null; }
		return accounts.get(p.getUniqueId());
	}
	
	public static final Map<UUID,ElevatedAccount> getAccounts()
	{
		return accounts;
	}
	
	public static ElevatedAccount getAccount(UUID uuid)
	{
		Objects.requireNonNull(uuid,"UUID cannot be null!");
		
		if(!accounts.containsKey(uuid)) { return null; }
		return accounts.get(uuid);
	}

	public static final ConsoleAccount getConsoleAccount()
	{
		return conAcc;
	}
	
	private static final boolean save(ElevatedAccount account)
	{
		boolean saveSuccessful = false;
		
		try
		{
			JsonObject data = account.serialise();
			
			// Apply pending changes if any
			if(pendingAuthChanges.containsKey(account.getOwner()))
			{
				for(AuthenticationMethod newMethodChange : pendingAuthChanges.get(account.getOwner()))
				{
					switch(newMethodChange.getAuthType())
					{
						case TEMP_PIN -> data.add(ElevatedAccount.TEMP_PIN_AUTH,((PersistJson) newMethodChange).serialise());
						case STATIC_PIN -> data.add(ElevatedAccount.STATIC_PIN_AUTH,((PersistJson) newMethodChange).serialise());
						case TIMED_OTP -> data.add(ElevatedAccount.TOTP_AUTH,((PersistJson) newMethodChange).serialise());
						case EMAIL_OTP -> data.add(ElevatedAccount.EMAIL_AUTH,((PersistJson) newMethodChange).serialise());
						case YUBI_KEY -> data.add(ElevatedAccount.YUBIKEY_AUTH,((PersistJson) newMethodChange).serialise());
						case MICROSOFT_AUTH -> data.add(ElevatedAccount.MICROSOFT_AUTH,((PersistJson) newMethodChange).serialise());
						default ->
						{
							throw new SerialiseException("Unknown auth type! '" + newMethodChange.getAuthType() + "'");
						}
					}
				}
			}
			
			String json = JUtils.toJsonString(data,true);
			Path path = Paths.get(ELEVATED_ACCOUNTS_DIR + File.separator + account.getOwner().toString() + ".json");
			JUtils.write(path,data,true);
			
			long checksumOfMemory = ChecksumUtils.getChecksum(json.getBytes());
			long checksumOfFile = FUtils.checksumFile(path);
			
			if(checksumOfMemory == checksumOfFile)
			{
				saveSuccessful = true;
			}
			else
			{
				Logg.error("ElevatedAccount for player " + PlayerUtils.getName(account.getOwner()) + " data on disk does not match data in memory! " + checksumOfFile + " != " + checksumOfMemory);
				return false;
			}
		}
		catch(SerialiseException e)
		{
			Logg.error("Could not serialise ElevatedAccount for player " + PlayerUtils.getName(account.getOwner()) + " (" + account.getOwner().toString() + ")",e);
			return false;
		}
		
		return saveSuccessful;
	}
	
	public static final boolean save(UUID uuid)
	{
		if(!accounts.containsKey(uuid))
		{
			Logg.error("Elevated account for player " + PlayerUtils.getName(uuid) + " does not exist in the accounts map!");
			return false;
		}
		
		try
		{
			Path accountPath = Paths.get(ELEVATED_ACCOUNTS_DIR + File.separator + uuid.toString() + ".json");
			
			JsonObject data = accounts.get(uuid).serialise();
			String json = JUtils.toJsonString(data,true);
			Path path = Paths.get(ELEVATED_ACCOUNTS_DIR + File.separator + uuid.toString() + ".json");
			JUtils.write(path,data,true);
			
			long checksumOfMemory = ChecksumUtils.getChecksum(json.getBytes());
			long checksumOfFile = FUtils.checksumFile(path);
			
			if(checksumOfMemory != checksumOfFile)
			{
				Logg.error("ElevatedAccount for player " + PlayerUtils.getName(uuid) + " data on disk does not match data in memory! " + checksumOfFile + " != " + checksumOfMemory);
				return false;
			}
			
			/**
			 * The reason we delete isMarkedAsDeleted() accounts after we've saved them is to prevent a situation
			 * where the elevated account on disk cannot be deleted despite it being requested to be deleted.
			 * 
			 * By saving the elevated account first with the mark of deletion, the next time the server is started,
			 * another attempt will be made to delete the account. If that fails, it will simply be not loaded.
			 */
			
			// Accounts marked as deleted 
			if(accounts.get(uuid).isMarkedAsDeleted())
			{
				// Check if the account still exists on disk
				if(Files.exists(accountPath))
				{
					// If so delete it
					FUtils.delete(accountPath);
					
					// If it still exists attempt a delete on exit
					if(Files.exists(accountPath))
					{
						Logg.error("An elevated account that was marked for deletion could not be deleted!");
						Logg.error("Dape will attempt to have this account deleted on server shutdown but should it not be able to, you will need to manually delete!");
						accountPath.toFile().deleteOnExit();
					}
					
					/**
					 * If this fails a manual delete will be needed.
					 * Not to worry as isMarkedAsDeleted() remains persistent in the file
					 */
				}
			}
			
			return true;
		}
		catch(SerialiseException e)
		{
			Logg.error("Could not serialise ElevatedAccount for player " + PlayerUtils.getName(uuid) + " (" + uuid.toString() + ")",e);
			return false;
		}
	}
	
	public static final boolean saveAll()
	{
		boolean allSavesSuccessful = true;
		
		for(UUID uuid : accounts.keySet())
		{
			if(!save(uuid))
			{
				allSavesSuccessful = false;
			}
		}
		
		return allSavesSuccessful;
	}
	
	/**
	 * Loads all accounts upon server start.
	 * New accounts cannot be added until server restart
	 * @return
	 */
	private static final void loadAll()
	{
		FUtils.createDirectories(ELEVATED_ACCOUNTS_DIR);
		List<Path> elevatedAccountsFiles = FUtils.getPathsInDirectory(ELEVATED_ACCOUNTS_DIR);
		
		if(elevatedAccountsFiles.size() == 0)
		{
			if(Config.LOCK_ACCOUNT_ADDING.get())
			{
				Collections.unmodifiableMap(accounts);
			}
			
			return;
		}
		
		for(Path elevatedAccountFile : FUtils.getPathsInDirectory(ELEVATED_ACCOUNTS_DIR))
		{
			if(!elevatedAccountFile.getFileName().toString().endsWith(".json")) { continue; }
			
			JsonObject obj = JUtils.readToObject(elevatedAccountFile);
			ElevatedAccount elevatedAccount;
			
			try
			{
				elevatedAccount = new ElevatedAccount(obj);
				
				// Do not add elevated accounts to the map that are marked as deleted
				if(elevatedAccount.isMarkedAsDeleted())
				{
					Logg.warn("Found elevated account marked for deletion! Owner: '" + PlayerUtils.getName(elevatedAccount.getOwner()) + "'");
					FUtils.delete(elevatedAccountFile);
					continue;
				}
			}
			catch(DeserialiseException e)
			{
				Logg.error("Error loading elevated account! '" + elevatedAccountFile.getFileName() + "'",e);
				continue;
			}
			
			Logg.info("Loaded elevated account for " + PlayerUtils.getName(elevatedAccount.getOwner()));
			accounts.put(elevatedAccount.getOwner(),elevatedAccount);
		}
		
		if(Config.LOCK_ACCOUNT_ADDING.get())
		{
			Collections.unmodifiableMap(accounts);
		}
	}
	
	public enum AuthMethod
	{
		TEMP_PIN,
		STATIC_PIN,
		TIMED_OTP,
		EMAIL_OTP,
		YUBI_KEY,
		MICROSOFT_AUTH
	}
	
	private enum ConsoleSetupPhase
	{
		NONE,
		ASKING_FOR_PIN_OR_PASSWORD_1,
		ASKING_FOR_PIN_OR_PASSWORD_2
	}
	
	public static class Config implements Configurable
	{
		public static final ConfigItem<Boolean> STATIC_PIN = new ConfigItem<>("elevated_accounts.auth_method.static_pin.enabled",true,"If static pins should be enabled as an authentication method.");
		public static final ConfigItem<Boolean> TOTP = new ConfigItem<>("elevated_accounts.auth_method.totp.enabled",true,"If timed one-time-passcodes should be enabled as an authentication method.");
		public static final ConfigItem<Boolean> EMAIL_OTP = new ConfigItem<>("elevated_accounts.auth_method.email_otp.enabled",true,"If email one-time-passcode should be enabled as an authentication method.");
		public static final ConfigItem<Boolean> YUBI_KEY = new ConfigItem<>("elevated_accounts.auth_method.yubi_key.enabled",false,"If Yubikey hardware auth should be enabled as an authentication method.");
		public static final ConfigItem<Boolean> MICROSOFT_AUTH = new ConfigItem<>("elevated_accounts.auth_method.microsoft_auth.enabled",false,"If Microsofts authenticator app should be enabled as an authentication method.");
		
		public static final ConfigItem<Long> AUTH_TIME = new ConfigItem<>("elevated_accounts.auth_time",300_000L,"The time that an elevated account will stay authorised allowing execution of other elevated commands without having to re-auth. Default is 5 minutes.")
		{
			@Override
			public Long clamp(Long value)
			{
				return MathUtils.clamp(1,Long.MAX_VALUE,value);
			}
		};
		
		public static final ConfigItem<Integer> MIN_AUTH_METHODS = new ConfigItem<>("elevated_accounts.min_auth_methods",2,"Minimum number of authentication methods that a player must have active on their elevated account.")
		{
			@Override
			public Integer clamp(Integer value)
			{
				return MathUtils.clamp(1,5,value);
			}
		};
		
		public static final ConfigItem<Boolean> LOCKED_AUTH_METHODS = new ConfigItem<>("elevated_accounts.locked_auth_methods",false,"If elevated account owners will have their authentication methods locked on server boot." +
				" If true, owners of elevated accounts that want to add, remove, or change their authentication methods will not have their changes apply until a restart of the server.");
		public static final ConfigItem<Boolean> LOCK_ACCOUNT_ADDING = new ConfigItem<>("elevated_accounts.lock_account_adding",false,"If the server will prevent adding new elevated accounts upon boot." +
				" If true, No one will be able to add or remove elevated accounts. They will be read only.");
		
		public static final ConfigItem<Integer> STATIC_PIN_MAX_PIN_LENGTH = new ConfigItem<>("elevated_accounts.auth_method.static_pin.max_pin_length",32,"Maximum number of characters allowed in a static pin. Must be between 3 and 64")
		{
			@Override
			public Integer clamp(Integer value)
			{
				return MathUtils.clamp(3,64,value);
			}
		};
		
		public static final ConfigItem<Boolean> ONLY_CONSOLE_CAN_CREATE_NEW_ELEVATED_ACCOUNTS = new ConfigItem<>("elevated_accounts.auth_method.only_console_can_create_new_elevated_accounts",true,"When true, elevated accounts can only be created when the '/elevate create' command is executed in console.");
		
		public static final ConfigItem<String> EMAIL_SENDER = new ConfigItem<>("elevated_accounts.auth_method.email_otp.sender","DapeServerEmailSender@dape.com","Email ID of sender");
		public static final ConfigItem<String> EMAIL_HOST = new ConfigItem<>("elevated_accounts.auth_method.email_otp.host","127.0.0.1","Email host");
		public static final ConfigItem<String> EMAIL_MAIL_SERVER = new ConfigItem<>("elevated_accounts.auth_method.email_otp.mail_server","mail.smtp.host","Mail server");
	}
	
	public static class SecretViewWarning implements InputListener
	{
		@InputHandler(ChatUtils.HandlerNames.ELEVATED_ACCOUNTS_VIEW_SECRET)
		public static final void onChatInput(ChatInputEvent e)
		{
			// Check input session id is matching that given by the request
			if(!e.getPlayer().getUniqueId().equals(e.getSessionOwner())) { return; }
			if(!e.getInput().equalsIgnoreCase("y") && !e.getInput().equalsIgnoreCase("yes"))
			{
				Logg.info("Cancelled totp view.");
				return;
			}
			
			ElevatedAccount acc = ElevatedAccountCtrl.getAccount(e.getPlayer());
			
			for(AuthenticationMethod meth : acc.getAuthMethods())
			{
				if(meth.getAuthType() != AuthMethod.TIMED_OTP) { continue; }
				
				TimedOTPAuthMethod totpAuthMethod = (TimedOTPAuthMethod) meth;
				BufferedImage qrCode = totpAuthMethod.getQrCode(e.getPlayer().getUniqueId());
				ItemStack stack = MapUtils.personalImageToMap(qrCode,e.getPlayer().getUniqueId());
				
				e.getPlayer().getInventory().addItem(stack);
				
				PrintUtils.error(e.getPlayer(),"Only you can see the QR code on this map.");
				PrintUtils.error(e.getPlayer(),"DO NOT SHARE THIS QRCODE OR YOUR TOTP SECRET WITH ANYONE!");
				BaseComponent[] totpSecret = new ChatBuilder()
					.setMessage("&5Totp secret&8:&d" + totpAuthMethod.getSecret().asString().substring(0,7) + "...")
					.setHoverShowTextEvent(ColourUtils.translate("&eClick me to copy your secret to your clipboard"))
					.setClickCopyToClipboardEvent(totpAuthMethod.getSecret().asString()).getResult();
				PrintUtils.sendComp(e.getPlayer(),totpSecret);
				return;
			}
		}
	}
}
