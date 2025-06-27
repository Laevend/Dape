package coffee.dape.cmdparsers.astral.types;

import coffee.dape.utils.security.SecureString;

/**
 * @author Laeven
 * 
 * This class defines the Secure String argument type.
 * Secure strings are designed for holding sensitive arguments in memory without being vulnerable to a JVM dump
 */
public class SecureStringType extends ArgumentType
{	
	public SecureStringType()
	{
		super("SECURE_STRING");
	}
	
	public boolean isType(String argument)
	{
		if(argument == null || argument.isEmpty() || argument.isBlank()) { return false; }
		return true;
	}
	
	@Override
	public SecureString parse(String argument)
	{
		return new SecureString(new StringBuilder(argument));
	}
}
