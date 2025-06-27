package coffee.dape.cmdparsers.astral.types;

/**
 * @author Laeven
 * 
 * This class defines the Double argument type
 */
public class DoubleType extends ArgumentType
{	
	public DoubleType()
	{
		super("DOUBLE");
	}
	
	public boolean isType(String argument)
	{
		try { Double.parseDouble(argument); return true; }
		catch(Exception e) { return false; }
	}
	
	@Override
	public Double parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return Double.parseDouble(argument);
	}
}
