package coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.totp;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.amdelamar.jotp.OTP;
import com.amdelamar.jotp.type.Type;
import com.google.gson.JsonObject;

import coffee.dape.Dape;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.AuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.AuthenticationMethod;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.IllegalMethodCallException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.utils.ImageUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.StringUtils;
import coffee.dape.utils.json.PersistJson;
import coffee.dape.utils.security.Bouncer;
import coffee.dape.utils.security.SecureString;
import coffee.dape.utils.tools.Deserialise;
import io.nayuki.fastqrcodegen.QrCode;
import io.nayuki.fastqrcodegen.QrCodeUtils;

public final class TimedOTPAuthMethod extends AuthenticationMethod implements PersistJson
{
	private SecureString secret;
	
	public TimedOTPAuthMethod()
	{
		super(AuthMethod.TIMED_OTP);
		
		// Random secret Base32 with 20 bytes (160 bits) length
		// (Use this to setup 2FA for new accounts).
		this.secret = new SecureString(new StringBuilder(OTP.randomBase32(30)));
	}
	
	public TimedOTPAuthMethod(JsonObject obj) throws DeserialiseException
	{
		super(AuthMethod.TIMED_OTP);
		deserialise(obj);
	}
	
	public TimedOTPAuthMethod(String secret)
	{
		super(AuthMethod.TIMED_OTP);
		
		this.secret = new SecureString(secret);
	}
	
	public String getURL(UUID player)
	{
		return OTP.getURL(secret.asString(),6,Type.TOTP,StringUtils.capitaliseFirstLetter(Dape.getNamespaceName().toLowerCase()),PlayerUtils.getName(player));
	}
	
	public BufferedImage getQrCode(UUID player)
	{
		QrCode totpQr = QrCode.encodeText(getURL(player),QrCode.Ecc.LOW);
		BufferedImage qrcode = QrCodeUtils.toImage(totpQr,2,1);
		
		// Map is 128 x 128 pixels
		BufferedImage mapImage = ImageUtils.getBlank(Color.WHITE,128,128);
		
		// Calculate offset to draw qrcode in middle of map
		int xOffset = 64 - (qrcode.getWidth() / 2);
		int yOffset = 64 - (qrcode.getHeight() / 2);
		return ImageUtils.drawImageOntop(mapImage,qrcode,xOffset,yOffset);
	}

	@Override
	public boolean verifyMethod(final String value,final Player player)
	{
		if(value.length() > 6 || value.length() < 6)
		{
			incrementAttempt();
			PrintUtils.error(player,"Authentication Failed, invalid passcode!");
			Logg.warn(player.getName() + " failed TOTP auth, value too long/too short!");
			return false;
		}
		
		if(!value.matches("^\\d{6}$"))
		{
			incrementAttempt();
			PrintUtils.error(player,"Authentication Failed, invalid passcode!");
			Logg.warn(player.getName() + " failed TOTP auth, value is not exclusively digits!");
			return false;
		}
		
		try
		{
			// Generate a Time-based OTP from the secret, using Unix-time
			// rounded down to the nearest 30 seconds.
			String hexTime = OTP.timeInHex(System.currentTimeMillis(),30);
			String code = OTP.create(secret.asString(),hexTime,6,Type.TOTP);
			
			Logg.info("\nHexTime: " + hexTime
					+ "\nCode: " + code
					+ "\nSecret: " + secret.asString()
					+ "\nValue: " + value);
			
			if(!OTP.verify(secret.asString(),hexTime,value,6,Type.TOTP))
			{
				incrementAttempt();
				PrintUtils.error(player,"Authentication Failed, invalid passcode!");
				return false;
			}
			
			return true;
		}
		catch(Exception e)
		{
			Logg.error("Error occured verifying TOTP!",e);
			PrintUtils.error(player,"Authentication Failed, unable to verify!");
			return false;
		}
	}
	
	@Override
	public String getAuthMessage()
	{
		return "Enter your one time passcode from your authenticator app using '/auth <value>'";
	}
	
	public SecureString getSecret()
	{
		return secret;
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
		this.secret = new SecureString(String.valueOf(sr.nextLong()));
	}

	public static final String SECRET = "secret";

	@Override
	public JsonObject serialise() throws SerialiseException
	{
		JsonObject obj = new JsonObject();
		obj.addProperty(SECRET,this.secret.asString());
		return obj;
	}

	@Override
	public void deserialise(JsonObject obj) throws DeserialiseException
	{
		try
		{
			Bouncer.haltAllBut(coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.class,
					coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccount.class,
					coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.totp.TimedOTPAuthMethod.class,
					coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.totp.TOTPSetup.class);
		}
		catch (IllegalMethodCallException e)
		{
			throw new DeserialiseException(e.getMessage());
		}
		
		this.secret = new SecureString(Deserialise.assertAndGetProperty(SECRET,Deserialise.Type.STRING,obj).getAsString());
	}
}
