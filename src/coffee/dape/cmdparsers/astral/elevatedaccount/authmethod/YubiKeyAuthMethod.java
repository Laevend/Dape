package coffee.dape.cmdparsers.astral.elevatedaccount.authmethod;

import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.AuthMethod;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.utils.json.PersistJson;

/**
 * 
 * @author Laeven
 * TODO Placeholder for later
 */
public final class YubiKeyAuthMethod extends AuthenticationMethod implements PersistJson
{
	public YubiKeyAuthMethod(final String secret)
	{
		super(AuthMethod.YUBI_KEY);
	}
	
	public YubiKeyAuthMethod(final JsonObject obj) throws DeserialiseException
	{
		super(AuthMethod.MICROSOFT_AUTH);
		deserialise(obj);
	}

	@Override
	public boolean verifyMethod(final String value,final Player player)
	{
		// TODO YubiKey verify logic
		
		return true;
	}
	
	@Override
	public String getAuthMessage()
	{
		return "Plug in your YubiKey and press the button.";
	}
	
	@Override
	public int maxAttempts()
	{
		return 5;
	}
	
	@Override
	public void clear()
	{
		
	}

	@Override
	public JsonObject serialise() throws SerialiseException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deserialise(JsonObject obj) throws DeserialiseException
	{
		// TODO Auto-generated method stub
		
	}
}
