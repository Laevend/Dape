package coffee.dape.playerdata.clocks;

import org.bukkit.entity.Player;

import coffee.dape.notifications.NotificationCtrl;
import coffee.dape.playerdata.data.PlayerData;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.clocks.absclocks.FixedRateAsyncClock;

/**
 * 
 * @author Laeven
 * Alerts the console and player that the players data is in a degraded state
 */
public class PlayerDataDegradedClock extends FixedRateAsyncClock
{
	private PlayerData parent;
	
	public PlayerDataDegradedClock(PlayerData data)
	{
		super("Player Data Degraded Clock",(1000 * 60));
		this.parent = data;
	}

	@Override
	public void execute() throws Exception
	{
		if(!PlayerUtils.isOnline(parent.getOwner())) { return; }
		Player p = PlayerUtils.getPlayer(parent.getOwner());
		
		if(!parent.isSilenceDegradedAlarm())
		{
			NotificationCtrl.sendAlert(p,"Alert! Your PlayerData was found to be corrupted and could not be read correctly!"
					+ " Your data will not be saved!"
					+ " Please contact an administrator immediately!");
		}
		
		Logg.fatal("Player " + PlayerUtils.getName(parent.getOwner()) + " has degraded player data!");
		
		// Auto stop clock if no longer in degraded state
		if(!parent.isInDegradedState()) { this.stop(); }
	}
}