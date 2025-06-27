package coffee.dape.cmdparsers.astral.types;

/**
 * @author Laeven
 * 
 * This class defines the float argument type
 */
public class FloatType extends ArgumentType
{	
	public FloatType()
	{
		super("FLOAT");
	}
	
	public boolean isType(String argument)
	{
		try { Float.parseFloat(argument); return true; }
		catch(Exception e) { return false; }
	}
	
	@Override
	public Float parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return Float.parseFloat(argument);
	}
}
