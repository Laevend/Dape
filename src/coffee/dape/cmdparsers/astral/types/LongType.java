package coffee.dape.cmdparsers.astral.types;

/**
 * @author Laeven
 * 
 * This class defines the Long argument type
 */
public class LongType extends ArgumentType
{	
	public LongType()
	{
		super("LONG");
	}
	
	public boolean isType(String argument)
	{
		try { Long.parseLong(argument); return true; }
		catch(Exception e) { return false; }
	}
	
	@Override
	public Long parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return Long.parseLong(argument);
	}
}
