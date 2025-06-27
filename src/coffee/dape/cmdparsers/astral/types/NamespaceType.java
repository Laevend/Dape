package coffee.dape.cmdparsers.astral.types;

import coffee.dape.utils.structs.Namespace;

/**
 * @author Laeven
 * 
 * This class defines the Namespace argument type
 */
public class NamespaceType extends ArgumentType
{
	public NamespaceType()
	{
		super("NAMESPACE");
	}
	
	public boolean isType(String argument)
	{
		return Namespace.namespaceSimpleStringPattern.matcher(argument).find();
	}
	
	@Override
	public Namespace parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return Namespace.fromString(argument);
	}
}
