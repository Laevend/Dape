package coffee.dape.cmdparsers.astral.types;

import java.util.regex.Pattern;

/**
 * @author Laeven
 * 
 * This class is a base abstract for defining valid types of arguments.
 * The isType() method is to allow the developer to implement validation logic of their type.
 * An example might be wanting to implement a type for hexadecimal only using regex.
 */
public abstract class ArgumentType
{
	private String typeName;
	private static Pattern typeNameValidation = Pattern.compile("^[a-zA-Z0-9_-]*$");
	
	/**
	 * Create an ArgumentType
	 * @param typeName The name of this type that will be used when writing paths. This name is auto capitalised.
	 */
	public ArgumentType(String typeName)
	{
		if(typeName == null || typeName.isEmpty() || typeName.isBlank()) { throw new IllegalArgumentException("Argument typeName cannot be null, empty, or blank! Got '" + typeName + "'"); }
		if(!typeNameValidation.matcher(typeName).matches()) { throw new IllegalArgumentException("Argument typeName can only consist of letters, numbers, underscores and hyphens! Got '" + typeName + "'"); }
		
		this.typeName = "[VAR_TYPE:" + typeName.toUpperCase() + "]";
	}
	
	/**
	 * Runs validation logic to check if the argument passed is of this type
	 * @param argument Argument to be validated
	 * @return True if the argument meets the requirements of the validation logic, false otherwise
	 */
	public abstract boolean isType(String argument);
	
	/**
	 * Parses an argument as this type and returns it.
	 * You will need to cast it after it returns.
	 * 
	 * @param <T> The type to parse as
	 * @param argument Argument to be parsed
	 * @return The argument parsed as the type defined in this argument type class
	 */
	public abstract Object parse(String argument);

	public String getTypeName()
	{
		return typeName;
	}
}
