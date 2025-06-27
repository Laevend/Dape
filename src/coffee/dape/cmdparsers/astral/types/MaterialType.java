package coffee.dape.cmdparsers.astral.types;

import org.bukkit.Material;

import coffee.dape.utils.MaterialUtils;

/**
 * @author Laeven
 * 
 * This class defines the Material argument type
 */
public class MaterialType extends ArgumentType
{
	public MaterialType()
	{
		super("MATERIAL");
	}
	
	public boolean isType(String argument)
	{
		return MaterialUtils.getSpigotMaterial(argument) != null;
	}
	
	@Override
	public Material parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return MaterialUtils.getSpigotMaterial(argument);
	}
}
