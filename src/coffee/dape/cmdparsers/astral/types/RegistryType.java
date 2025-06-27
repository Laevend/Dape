package coffee.dape.cmdparsers.astral.types;

import org.bukkit.Keyed;
import org.bukkit.Registry;

/**
 * @author Laeven
 * 
 * This class defines the Registry argument type
 */
public class RegistryType<R extends Keyed> extends ArgumentType
{
	private Registry<R> registry;
	
	public static <R extends Keyed> RegistryType<R> of(Registry<R> reg)
	{
		return new RegistryType<R>(reg);
	}
	
	public RegistryType(Registry<R> registry)
	{
		super("REGISTRY" + registry.getClass().getSimpleName().toUpperCase());
		this.registry = registry;
		
		// Enums cannot be collected and init automatically because their type is determined by the enum class given
		ArgTypes.addArgumentType(this);
	}
	
	public boolean isType(String argument)
	{
		String arg = argument.toUpperCase().replaceAll("\\s+","_");
		
		if(registry.match(arg) == null) { return false; }
		return true;
	}
	
	@Override
	public R parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return registry.match(argument.toUpperCase().replaceAll("\\s+","_"));
	}
}