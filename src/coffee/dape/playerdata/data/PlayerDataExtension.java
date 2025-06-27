package coffee.dape.playerdata.data;

import java.util.UUID;

import coffee.dape.playerdata.PlayerDataCtrl;
import coffee.dape.utils.json.PersistJson;
import coffee.dape.utils.structs.Namespace;

/**
 * @author Laeven
 * 
 * Extension data provides a quick method of bolting on more data classes
 * to the PlayerData class
 */
public abstract class PlayerDataExtension implements PersistJson
{
	private Namespace extensionName;
	private UUID owner;
	
	/**
	 * Initialising
	 * @param owner UUID of player who owns this extension data
	 * @param extensionName Name of the extension. This is used to retrieve the map using PlayerDataCtrl.getPlayerData().
	 */
	public PlayerDataExtension(UUID owner,Namespace extensionName)
	{
		this.owner = owner;
		this.extensionName = extensionName;
		PlayerDataCtrl.getPlayerData(owner).setExtensionData(extensionName,this);
	}

	public Namespace getExtensionName()
	{
		return extensionName;
	}
	
	public UUID getOwner()
	{
		return owner;
	}

	public void setOwner(UUID owner)
	{
		this.owner = owner;
	}
}