package coffee.dape.cmdparsers.astral.elevatedaccount.authmethod;

import org.bukkit.entity.Player;

import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.AuthMethod;
import coffee.dape.utils.security.ObfuscatedRandBaseEncoder;
import coffee.dape.utils.security.SecureByteArray;

public abstract class AuthenticationMethod
{
	private final AuthMethod type;
	private SecureByteArray markedForRemoval = ObfuscatedRandBaseEncoder.encode(ElevatedAccountCtrl.FALSE);
	private SecureByteArray attempt = ObfuscatedRandBaseEncoder.encode(1);
	
	public AuthenticationMethod(final AuthMethod type)
	{
		this.type = type;
	}
	
	public abstract boolean verifyMethod(final String value,final Player player);
	
	public abstract String getAuthMessage();
	
	public abstract int maxAttempts();

	public final AuthMethod getAuthType()
	{
		return type;
	}

	public final SecureByteArray getAttempt()
	{
		return attempt;
	}
	
	public final void incrementAttempt()
	{
		attempt = ObfuscatedRandBaseEncoder.encode(ObfuscatedRandBaseEncoder.decode(attempt) + 1);
	}
	
	public final void resetAttempt()
	{
		attempt = ObfuscatedRandBaseEncoder.encode(1);
	}
	
	public final boolean isMarkedForRemoval()
	{
		return ObfuscatedRandBaseEncoder.peek(markedForRemoval) == ElevatedAccountCtrl.TRUE;
	}
	
	public final void markForRemoval()
	{
		markedForRemoval = ObfuscatedRandBaseEncoder.encodeAndReplace(ElevatedAccountCtrl.TRUE,markedForRemoval);
	}
	
	public abstract void clear();
}
