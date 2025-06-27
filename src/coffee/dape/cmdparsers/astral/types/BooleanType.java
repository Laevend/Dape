package coffee.dape.cmdparsers.astral.types;

/**
 * @author Laeven
 * 
 * This class defines the Boolean argument type
 */
public class BooleanType extends ArgumentType
{	
	public BooleanType()
	{
		super("BOOLEAN");
	}
	
	public boolean isType(String argument)
	{
		try { Boolean.parseBoolean(argument); return true; }
		catch(Exception e) { return false; }
	}

	@Override
	public Boolean parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return Boolean.parseBoolean(argument);
	}
}
