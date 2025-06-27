package coffee.dape.cmdparsers.astral.types;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author Laeven
 * 
 * This class defines the UUID argument type
 */
public class UUIDType extends ArgumentType
{	
	public static final Pattern uuidPattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$",Pattern.CASE_INSENSITIVE);
	
	public UUIDType()
	{
		super("UUID");
	}
	
	public boolean isType(String argument)
	{
		return uuidPattern.matcher(argument).matches();
	}
	
	@Override
	public UUID parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return UUID.fromString(argument);
	}
}
