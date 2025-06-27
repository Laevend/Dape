package coffee.dape.cmdparsers.astral.types;

import java.util.regex.Pattern;

/**
 * @author Laeven
 * 
 * This class defines the Hex argument type
 */
public class HexType extends ArgumentType
{	
	Pattern hexPattern = Pattern.compile("^#[0-9a-fA-F]{6}$",Pattern.CASE_INSENSITIVE);
	
	public HexType()
	{
		super("HEX");
	}
	
	public boolean isType(String argument)
	{
		return hexPattern.matcher(argument).matches();
	}
	
	@Override
	public String parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return argument;
	}
}
