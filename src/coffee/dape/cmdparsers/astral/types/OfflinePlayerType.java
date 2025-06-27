package coffee.dape.cmdparsers.astral.types;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import coffee.dape.utils.PlayerUtils;

/**
 * @author Laeven
 * 
 * This class defines the offline Player argument type
 */
public class OfflinePlayerType extends ArgumentType
{
	public OfflinePlayerType()
	{
		super("OFFLINE_PLAYER");
	}
	
	public boolean isType(String argument)
	{
		return PlayerUtils.getUUID(argument) != null;
	}
	
	@Override
	public OfflinePlayer parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return Bukkit.getOfflinePlayer(PlayerUtils.getUUID(argument));
	}
}
