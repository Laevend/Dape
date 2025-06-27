package coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.staticpin;

import java.security.SecureRandom;
import java.util.Objects;

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
import coffee.dape.utils.security.EncryptUtils;
import coffee.dape.utils.security.HashingUtils;
import coffee.dape.utils.security.SecureByteArray;
import coffee.dape.utils.security.SecureString;
import coffee.dape.utils.tools.Deserialise;

public final class StaticPinAuthMethod extends AuthenticationMethod implements PersistJson
{
	private SecureString hashedPin = null;
	private SecureByteArray salt = null;
	
	public StaticPinAuthMethod(final String hashedPin,final byte[] salt)
	{
		super(AuthMethod.STATIC_PIN);
		
		Objects.requireNonNull(hashedPin,"HashedPin cannot be null!");
		Objects.requireNonNull(salt,"Salt cannot be null!");
		
		this.hashedPin = new SecureString(hashedPin);
		this.salt = new SecureByteArray(salt);
	}
	
	public StaticPinAuthMethod(JsonObject obj) throws DeserialiseException
	{
		super(AuthMethod.STATIC_PIN);
		deserialise(obj);
	}
	
	public StaticPinAuthMethod(final SecureString pin)
	{
		super(AuthMethod.STATIC_PIN);
		
		Objects.requireNonNull(pin,"Pin cannot be null!");
		
		this.salt = new SecureByteArray(HashingUtils.generateSalt());
		this.hashedPin = new SecureString(EncryptUtils.toBase64(HashingUtils.hash(pin.asString(),this.salt.asByteArray())));
	}

	@Override
	public boolean verifyMethod(final String value,final Player player)
	{
		if(value.length() > ElevatedAccountCtrl.Config.STATIC_PIN_MAX_PIN_LENGTH.get())
		{
			incrementAttempt();
			PrintUtils.error(player,"Authentication Failed, invalid pin!");
			return false;
		}
		
		String hashedPin = EncryptUtils.toBase64(HashingUtils.hash(value,this.salt.asByteArray()));		
		
		if(!hashedPin.equals(hashedPin))
		{
			incrementAttempt();
			PrintUtils.error(player,"Authentication Failed, invalid pin!");
			return false;
		}
		
		return true;
	}
	
	@Override
	public String getAuthMessage()
	{
		return "Enter your pin using '/auth <value>'";
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
	
	/**
	 * Requests a change of the existing pin by comparing old pin to the pin currently held.
	 * <p>Pin is only changed if old pin matches what is currently held.
	 * @param oldPin The existing pin held in this account
	 * @param newPin The new pin the player who owns this account wishes to change their pin to
	 * @return
	 */
	public boolean changePin(SecureString oldPin,SecureString newPin)
	{
		String hashedPin = EncryptUtils.toBase64(HashingUtils.hash(oldPin.toString(),this.salt.asByteArray()));
		
		if(!hashedPin.equals(this.hashedPin.toString()))
		{
			incrementAttempt();
			return false;
		}
		
		setPin(newPin);
		return true;
	}
	
	public void setPin(SecureString pin)
	{
		this.salt = new SecureByteArray(HashingUtils.generateSalt());
		this.hashedPin = new SecureString(EncryptUtils.toBase64(HashingUtils.hash(pin.asString(),this.salt.asByteArray())));
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
					coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.staticpin.StaticPinAuthMethod.class,
					coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.staticpin.StaticPinSetup.class);
		}
		catch (IllegalMethodCallException e)
		{
			throw new DeserialiseException(e.getMessage());
		}
		
		this.hashedPin = new SecureString(Deserialise.assertAndGetProperty(PIN,Deserialise.Type.STRING,obj).getAsString());
		this.salt = new SecureByteArray(EncryptUtils.fromBase64(Deserialise.assertAndGetProperty(SALT,Deserialise.Type.STRING,obj).getAsString()));
	}
}
