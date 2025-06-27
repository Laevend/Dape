package coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.temppin;

import java.security.SecureRandom;
import java.util.Objects;

import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.AuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.AuthenticationMethod;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.IllegalMethodCallException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.json.PersistJson;
import coffee.dape.utils.security.Bouncer;
import coffee.dape.utils.security.EncryptUtils;
import coffee.dape.utils.security.HashingUtils;
import coffee.dape.utils.security.SecureByteArray;
import coffee.dape.utils.security.SecureString;
import coffee.dape.utils.tools.Deserialise;

/**
 * A temporary pin that gets assigned to brand new elevated accounts before they setup their own authentication methods.
 * <p>
 * This should ONLY ever be used as a temporary authentication method while the elevated account owner is setting up their authentication methods.
 * IT IS NOT INTENDED TO BE USED AS A PERMANENT AUTHENTICATION METHOD!
 */
public final class TempPinAuthMethod extends AuthenticationMethod implements PersistJson
{
	private SecureString hashedPin = null;
	private SecureByteArray salt = null;
	
	public TempPinAuthMethod(JsonObject obj) throws DeserialiseException
	{
		super(AuthMethod.TEMP_PIN);
		deserialise(obj);
	}
	
	public TempPinAuthMethod(final SecureString ss)
	{
		super(AuthMethod.TEMP_PIN);		
		Objects.requireNonNull(ss,"Temporary pin cannot be null!");
		
		this.salt = new SecureByteArray(HashingUtils.generateSalt());
		this.hashedPin = new SecureString(EncryptUtils.toBase64(HashingUtils.hash(ss.asString(),this.salt.asByteArray())));
	}

	@Override
	public boolean verifyMethod(final String value,final Player player)
	{
		if(value.length() != 4)
		{
			incrementAttempt();
			PrintUtils.error(player,"Authentication Failed, invalid temporary pin!");
			return false;
		}
		
		String hashedPin = EncryptUtils.toBase64(HashingUtils.hash(value,this.salt.asByteArray()));		
		
		if(!hashedPin.equals(hashedPin))
		{
			incrementAttempt();
			PrintUtils.error(player,"Authentication Failed, invalid temporary pin!");
			return false;
		}
		
		return true;
	}
	
	@Override
	public String getAuthMessage()
	{
		return "Enter your temporary pin using '/auth <value>'";
	}
	
	@Override
	public int maxAttempts()
	{
		return 5;
	}

	public SecureString getHashedPin()
	{
		return hashedPin;
	}

	public SecureByteArray getSalt()
	{
		return salt;
	}
	
	@Override
	public void clear()
	{
		SecureRandom sr = new SecureRandom();
		
		byte[] saltScrambled = new byte[sr.nextInt(16,32)];
		
		sr.nextBytes(saltScrambled);
		
		this.hashedPin = new SecureString(String.valueOf(sr.nextLong()));
		this.salt = new SecureByteArray(saltScrambled);
	}
	
	public static final String PIN = "pin";
	public static final String SALT = "salt";

	@Override
	public JsonObject serialise() throws SerialiseException
	{
		JsonObject obj = new JsonObject();
		obj.addProperty(PIN,hashedPin.asString());
		obj.addProperty(SALT,EncryptUtils.toBase64(salt.asByteArray()));
		return obj;
	}

	@Override
	public void deserialise(JsonObject obj) throws DeserialiseException
	{
		try
		{
			Bouncer.haltAllBut(coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.class,
					coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccount.class,
					coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.temppin.TempPinAuthMethod.class);
		}
		catch (IllegalMethodCallException e)
		{
			throw new DeserialiseException(e.getMessage());
		}
		
		this.hashedPin = new SecureString(Deserialise.assertAndGetProperty(PIN,Deserialise.Type.STRING,obj).getAsString());
		this.salt = new SecureByteArray(EncryptUtils.fromBase64(Deserialise.assertAndGetProperty(SALT,Deserialise.Type.STRING,obj).getAsString()));
	}
}
