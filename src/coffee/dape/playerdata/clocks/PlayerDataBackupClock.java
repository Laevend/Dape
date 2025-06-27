package coffee.dape.playerdata.clocks;

import java.util.Random;

import org.bukkit.entity.Player;

import coffee.dape.notifications.NotificationCtrl;
import coffee.dape.playerdata.PlayerDataCtrl;
import coffee.dape.playerdata.data.PlayerData;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.clocks.absclocks.FixedRateAsyncClock;

/**
 * 
 * @author Laeven
 *
 */
public class PlayerDataBackupClock extends FixedRateAsyncClock
{
	private PlayerData parent;
	
	public PlayerDataBackupClock(PlayerData data)
	{
		// 10 minutes + 1 to 60 seconds for variance.
		super("Player Data Backup Clock",600_000L + new Random().nextLong(1000,60_000));
		this.parent = data;
	}

	@Override
	public void execute() throws Exception
	{
		if(parent.isInDegradedState())
		{
			Logg.fatal("Player " + PlayerUtils.getName(parent.getOwner()) + " has degraded player data!");
			
			if(!PlayerUtils.isOnline(parent.getOwner())) { return; }
			Player p = PlayerUtils.getPlayer(parent.getOwner());
			
			NotificationCtrl.sendAlert(p,"Alert! Your PlayerData was found to be corrupted and could not be read correctly!"
					+ " Your data will not be saved!"
					+ " Please contact an administrator immediately! DO NOT IGNORE!");
			return;
		}
		
		Logg.info("Backing up PData from clock for -> " + parent.getMostRecentUsername());
		PlayerDataCtrl.backup(parent);
		PlayerDataCtrl.checkAndDeleteOldBackups(parent);
	}
}