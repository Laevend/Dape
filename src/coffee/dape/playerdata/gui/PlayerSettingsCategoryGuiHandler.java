package coffee.dape.playerdata.gui;

import org.bukkit.entity.Player;

import coffee.dape.chaosui.ChaosHandler;
import coffee.dape.chaosui.events.ChaosCloseEvent;
import coffee.dape.chaosui.handler.CloseHandler;
import coffee.dape.playerdata.PlayerDataCtrl;

public class PlayerSettingsCategoryGuiHandler extends ChaosHandler implements CloseHandler
{
	@Override
	public void onClose(ChaosCloseEvent e)
	{
		PlayerDataCtrl.getPlayerData((Player) e.getPlayer()).scheduleSave();
	}
}
