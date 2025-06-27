package coffee.dape.playerdata.clocks;

import coffee.dape.playerdata.PlayerDataCtrl;
import coffee.dape.playerdata.data.PlayerData;
import coffee.dape.utils.Logg;
import coffee.dape.utils.clocks.absclocks.HybridAsyncClock;

/**
 * 
 * @author Laeven
 *
 */
public class PlayerDataSaveClock extends HybridAsyncClock
{
	private PlayerData parent;
	
	public PlayerDataSaveClock(PlayerData data)
	{
		super("Player Data Save Clock");
		this.parent = data;
	}

	@Override
	public void execute() throws Exception
	{
		Logg.verb("Saving PData from clock for -> " + parent.getMostRecentUsername(),Logg.VerbGroup.PLAYER_DATA);
		this.parent.getInventory().updateInventory();
		this.parent.getEnderchest().updateEnderchest();
		this.parent.getEntityData().updatePlayerEntity();
		PlayerDataCtrl.savePlayerData(parent.getOwner(),false);
	}
}