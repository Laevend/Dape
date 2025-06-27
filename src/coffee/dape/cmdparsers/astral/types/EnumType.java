package coffee.dape.cmdparsers.astral.types;

/**
 * @author Laeven
 * 
 * This class defines the Enum argument type
 */
public class EnumType<E extends Enum<E>> extends ArgumentType
{
	private Class<E> enumClass;
	
	public static <T extends Enum<T>> EnumType<T> of(Class<T> enumObj)
	{
		return new EnumType<T>(enumObj);
	}
	
	public EnumType(Class<E> clazz)
	{
		super("ENUM_" + clazz.getSimpleName().toUpperCase());
		this.enumClass = clazz;
		
		// Enums cannot be collected and init automatically because their type is determined by the enum class given
		ArgTypes.addArgumentType(this);
	}
	
	public boolean isType(String argument)
	{
		String arg = argument.toUpperCase().replaceAll("\\s+","_");
		
		for(E option : enumClass.getEnumConstants())
		{
			if(arg.equals(option.toString())) { return true; }
		}
		
		return false;
	}
	
	@Override
	public E parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return Enum.valueOf(enumClass,argument.toUpperCase().replaceAll("\\s+","_"));
	}
}
