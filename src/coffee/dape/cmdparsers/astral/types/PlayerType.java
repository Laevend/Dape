package coffee.dape.cmdparsers.astral.types;

import java.util.UUID;

import coffee.dape.utils.PlayerUtils;

/**
 * @author Laeven
 * 
 * This class defines the Player argument type
 * This player can be online OR offline
 */
public class PlayerType extends ArgumentType
{
	public PlayerType()
	{
		super("PLAYER");
	}
	
	public boolean isType(String argument)
	{
		// Check if this is a UUID string
		if(UUIDType.uuidPattern.matcher(argument).matches())
		{
			UUID uuid = UUID.fromString(argument);
			return PlayerUtils.isAPlayer(uuid);
		}
		
		// Must be a player name
		return PlayerUtils.isAPlayer(argument);
	}
	
	@Override
	public UUID parse(String argument)
	{
		if(!isType(argument)) { throw new IllegalArgumentException("Argument '" + argument + "' can not be parsed to type " + getTypeName()); }
		
		if(UUIDType.uuidPattern.matcher(argument).matches())
		{
			return UUID.fromString(argument);
		}
		
		// Must be a player name
		return PlayerUtils.getUUID(argument);
	}
}
