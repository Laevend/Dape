package coffee.dape.cmdparsers.astral.types;

import org.bukkit.entity.Player;

import coffee.dape.utils.PlayerUtils;

/**
 * @author Laeven
 * 
 * This class defines the Online Player argument type
 */
public class OnlinePlayerType extends ArgumentType
{
	public OnlinePlayerType()
	{
		super("ONLINE_PLAYER");
	}
	
	public boolean isType(String argument)
	{
		return PlayerUtils.isOnline(argument);
	}
	
	@Override
	public Player parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		return PlayerUtils.getPlayer(argument);
	}
}
