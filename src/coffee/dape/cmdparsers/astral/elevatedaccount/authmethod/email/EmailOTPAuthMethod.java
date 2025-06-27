package coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.email;

import java.security.SecureRandom;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.AuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.AuthenticationMethod;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.IllegalMethodCallException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.json.PersistJson;
import coffee.dape.utils.security.Bouncer;
import coffee.dape.utils.security.SecureString;
import coffee.dape.utils.tools.Deserialise;

/**
 * 
 * @author Laeven
 * TODO Placeholder for later
 */
public final class EmailOTPAuthMethod extends AuthenticationMethod implements PersistJson
{
	public static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*@[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*$");
	private SecureString emailAddress;
	private static SecureString sender = new SecureString(ElevatedAccountCtrl.Config.EMAIL_SENDER.get());
	private static SecureString host = new SecureString(ElevatedAccountCtrl.Config.EMAIL_HOST.get());
	private static SecureString mailServer = new SecureString(ElevatedAccountCtrl.Config.EMAIL_MAIL_SERVER.get());
	
	public EmailOTPAuthMethod(final String email)
	{
		super(AuthMethod.EMAIL_OTP);
		this.emailAddress = new SecureString(email);
	}
	
	public EmailOTPAuthMethod(final JsonObject obj) throws DeserialiseException
	{
		super(AuthMethod.MICROSOFT_AUTH);
		deserialise(obj);
	}

	@Override
	public boolean verifyMethod(final String otp,final Player player)
	{
		if(otp.length() > 6)
		{
			incrementAttempt();
			PrintUtils.error(player,"Authentication Failed, invalid passcode!");
			return false;
		}
		
		if(!otp.matches("^\\d{6}$"))
		{
			incrementAttempt();
			PrintUtils.error(player,"Authentication Failed, invalid passcode!");
			return false;
		}
		
		// TODO Email OTP verify logic
		
		return true;
	}
	
	@Override
	public String getAuthMessage()
	{
		return "We've sent you a one time passcode to your email. Enter it using '/auth <value>'";
	}
	
	@Override
	public int maxAttempts()
	{
		return 5;
	}
	
	@Override
	public void clear()
	{
		SecureRandom sr = new SecureRandom();
		this.emailAddress = new SecureString(String.valueOf(sr.nextLong()));
	}
	
	public final SecureString getEmailAddress()
	{
		return emailAddress;
	}

	public static final SecureString getSender()
	{
		return sender;
	}

	public static final SecureString getHost()
	{
		return host;
	}

	public static final SecureString getMailServer()
	{
		return mailServer;
	}

	public static final String EMAIL = "email";

	@Override
	public JsonObject serialise() throws SerialiseException
	{
		JsonObject obj = new JsonObject();
		obj.addProperty(EMAIL,emailAddress.asString());
		return obj;
	}

	@Override
	public void deserialise(JsonObject obj) throws DeserialiseException
	{
		try
		{
			Bouncer.haltAllBut(coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.class,
					coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccount.class,
					coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.email.EmailOTPAuthMethod.class,
					coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.email.EmailSetup.class);
		}
		catch (IllegalMethodCallException e)
		{
			throw new DeserialiseException(e.getMessage());
		}
		
		this.emailAddress = new SecureString(Deserialise.assertAndGetProperty(EMAIL,Deserialise.Type.STRING,obj).getAsString());
	}
}
