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
public final class MicrosoftAuthMethod extends AuthenticationMethod implements PersistJson
{
	public MicrosoftAuthMethod(final String secret)
	{
		super(AuthMethod.MICROSOFT_AUTH);
	}
	
	public MicrosoftAuthMethod(final JsonObject obj) throws DeserialiseException
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
		return "A request has been sent to your Microsoft authenticator app.";
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
