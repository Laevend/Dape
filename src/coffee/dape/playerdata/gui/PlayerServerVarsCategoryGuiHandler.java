package coffee.dape.playerdata.gui;

import java.util.UUID;

import org.bukkit.entity.Player;

import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.ChaosHandler;
import coffee.dape.chaosui.events.ChaosCloseEvent;
import coffee.dape.chaosui.events.ChaosTextInputEvent;
import coffee.dape.chaosui.handler.CloseHandler;
import coffee.dape.chaosui.handler.TextInputButtonHandler;
import coffee.dape.playerdata.PlayerDataCtrl;
import coffee.dape.playerdata.data.PlayerServerVariables;
import coffee.dape.playerdata.data.PlayerServerVariables.ServerVar;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.data.DataUtils;

public class PlayerServerVarsCategoryGuiHandler extends ChaosHandler implements CloseHandler, TextInputButtonHandler
{
	@Override
	public void onClose(ChaosCloseEvent e)
	{
		PlayerDataCtrl.getPlayerData((Player) e.getPlayer()).scheduleSave();
	}

	@Override
	public void onTextInputButtonClick(ChaosTextInputEvent e)
	{
		Player p = (Player) e.getPlayerWhoClicked();
		UUID playerViewing = PlayerServerVarsGuiBuilder.instance.getPlayerInUse(p);
		// Getting slot of the server var icon above the input button (-9 spaces)
		int slot = ChaosFactory.getSession(p).getInputButtonSlotClicked(e.getBuilder()) - 9;
		ServerVar var = ServerVar.valueOf(DataUtils.get("server_var",e.getView().getItem(slot)).asString());
		PlayerServerVariables.getInstance(playerViewing).set(var,e.getInput());
		PlayerServerVariables.ServerVarCategory category = PlayerServerVarsGuiBuilder.instance.getServerVarCategoryInUse(p);
		
		if(!PlayerUtils.isOnline(playerViewing)) { return; }
		
		// TODO Add ChatCtrl
		
		if(category == PlayerServerVariables.ServerVarCategory.CHAT || category == PlayerServerVariables.ServerVarCategory.RANKS)
		{
			//ChatCtrl.getTailor(e.getPlayerWhoClicked()).updateChatData();
		}
		
		if(category == PlayerServerVariables.ServerVarCategory.RANKS)
		{
			//ChatCtrl.getTailor(e.getPlayerWhoClicked()).getHoverInfo(true);
		}
	}
}
