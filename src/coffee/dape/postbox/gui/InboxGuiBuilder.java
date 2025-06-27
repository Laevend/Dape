package coffee.dape.postbox.gui;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.anno.NestedChaosGUI;
import coffee.dape.chaosui.interfaces.paginator.DynamicPaginatorContents;
import coffee.dape.chaosui.interfaces.paginator.Paginator;
import coffee.dape.chaosui.interfaces.paginator.PaginatorItem;
import coffee.dape.playerdata.PlayerDataCtrl;
import coffee.dape.utils.SoundUtils;

@NestedChaosGUI(handler = InboxGuiHandler.class)
public class InboxGuiBuilder extends ChaosBuilder implements DynamicPaginatorContents
{
	@Override
	public void init()
	{
		setOpenSound(SoundUtils.DUSTING);
		setInterface(new Paginator());
		setFill(Material.GRAY_STAINED_GLASS_PANE);
	}

	@Override
	public void buildGUI(InventoryView view) {}
	
	@Override
	public List<PaginatorItem> refreshPaginator(Player p)
	{
		return List.copyOf(PlayerDataCtrl.getPlayerData(p).getPostData().getInbox());
	}
}
