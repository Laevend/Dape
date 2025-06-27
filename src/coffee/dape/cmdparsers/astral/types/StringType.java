package coffee.dape.cmdparsers.astral.types;

/**
 * @author Laeven
 * 
 * This class defines the String argument type
 */
public class StringType extends ArgumentType
{	
	public StringType()
	{
		super("STRING");
	}
	
	public boolean isType(String argument)
	{
		if(argument == null || argument.isEmpty() || argument.isBlank()) { return false; }
		return true;
	}
	
	@Override
	public String parse(String argument)
	{
		return argument;
	}
}
