package coffee.dape.postbox.gui;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.ChaosHandler;
import coffee.dape.chaosui.components.buttons.ToggleButton;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.guis.ConfirmBox;
import coffee.dape.chaosui.handler.PaginatorHandler;
import coffee.dape.playerdata.PlayerDataCtrl;
import coffee.dape.playerdata.data.PlayerData;
import coffee.dape.playerdata.data.PlayerPostBox.Message;
import coffee.dape.playerdata.data.PlayerPostBox.Parcel;
import coffee.dape.utils.InventoryUtils;
import coffee.dape.utils.SoundUtils;
import coffee.dape.utils.data.DataUtils;

public class SentGuiHandler extends ChaosHandler implements PaginatorHandler
{
	@Override
	public void onClickPaginatorItem(ChaosClickEvent e,ItemStack paginatorItem)
	{
		Player p = (Player) e.getWhoClicked();
		UUID id = UUID.fromString(DataUtils.get("id",paginatorItem).asString());
		String postItemType = DataUtils.get("type",paginatorItem).asString();
		PlayerData data = PlayerDataCtrl.getPlayerData(p);
		ChaosBuilder builder = ChaosFactory.getGUI(ChaosFactory.Common.POST_BOX);
		ToggleButton deleteConfirmationButton = (ToggleButton) builder.getSlots().get(8).getSlotComponent();
		
		if(postItemType.equals("message"))
		{
			Message msg = (Message) data.getPostData().getInboxPost(id);
			
			switch(e.getClick())
			{
				case ClickType.LEFT ->
				{
					msg.readMessage(p);
					msg.setRead(true);
				}
				case ClickType.SHIFT_RIGHT ->
				{
					if(deleteConfirmationButton.isEnabled())
					{
						ConfirmBox.openConfirmBox(p,"Do you wish to delete this message?",msg.getStack(),() ->
						{
							data.getPostData().removeInboxPost(id);
						},() -> {});
						return;
					}
					
					data.getPostData().removeInboxPost(id);
				}
				case ClickType.MIDDLE ->
				{
					msg.setRead(false);
				}
				default -> { SoundUtils.playErrorSound(p); return; }
			}
		}
		else if(postItemType.equals("parcel"))
		{
			Parcel parcel = (Parcel) data.getPostData().getInboxPost(id);
			
			switch(e.getClick())
			{
				case ClickType.LEFT ->
				{
					for(ItemStack stack : ((BundleMeta) parcel.getStack().getItemMeta()).getItems())
					{
						InventoryUtils.safeInventorySetDropExtra(p,stack);
					}
					
					data.getPostData().removeInboxPost(id);
				}
				case ClickType.SHIFT_RIGHT ->
				{
					if(deleteConfirmationButton.isEnabled())
					{
						ConfirmBox.openConfirmBox(p,"Do you wish to delete this parcel?",parcel.getStack(),() ->
						{
							data.getPostData().removeInboxPost(id);
						},() -> {});
						return;
					}
					
					data.getPostData().removeInboxPost(id);
				}
				default -> { SoundUtils.playErrorSound(p); return; }
			}
		}
		else
		{
			throw new IllegalArgumentException("Unknown post item type '" + postItemType + "'");
		}
	}
}
