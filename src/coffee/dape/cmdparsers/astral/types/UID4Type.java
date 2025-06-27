package coffee.dape.cmdparsers.astral.types;

import coffee.dape.utils.structs.UID4;

/**
 * @author Laeven
 * 
 * This class defines the UID4 argument type
 */
public class UID4Type extends ArgumentType
{
	public UID4Type()
	{
		super("UID4");
	}
	
	public boolean isType(String argument)
	{
		return UID4.UID4Pattern.matcher(argument).find();
	}
	
	@Override
	public UID4 parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return UID4.fromString(argument);
	}
}
