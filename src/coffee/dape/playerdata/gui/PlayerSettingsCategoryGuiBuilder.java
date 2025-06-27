package coffee.dape.playerdata.gui;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.ChaosFactory.InvTemplate;
import coffee.dape.chaosui.anno.ChaosGUI;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.interfaces.paginator.DynamicPaginatorContents;
import coffee.dape.chaosui.interfaces.paginator.Paginator;
import coffee.dape.chaosui.interfaces.paginator.Paginator.DrawMode;
import coffee.dape.chaosui.interfaces.paginator.PaginatorItem;
import coffee.dape.playerdata.data.PlayerSettings;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;

@ChaosGUI(name = ChaosFactory.Common.PLAYER_SETTINGS_CATEGORY,handler = PlayerSettingsCategoryGuiHandler.class,template = InvTemplate.CHEST_6)
public class PlayerSettingsCategoryGuiBuilder extends ChaosBuilder implements DynamicPaginatorContents
{
	@Override
	public void init()
	{
		setInterface(new Paginator(DrawMode.BUTTON_PANEL));
		setNavigationBack(ChaosFactory.Common.PLAYER_SETTINGS);
		
		putStaticComponent(new Button(4,ItemBuilder.of(HeadUtils.INFO_ICON.clone())
				.name("Information",ColourUtils.VISTA_BLUE)
				.lore()
				.wrap(ColourUtils.applyColour("Here you can customise and tailor your server experience.",ColourUtils.TEXT))
				.create()));
	}

	@Override
	public void buildGUI(InventoryView view) {}

	@Override
	public List<PaginatorItem> refreshPaginator(Player p)
	{
		PlayerSettings.SettingCategory category = PlayerSettingsGuiBuilder.instance.getSettingCategoryInUse(p);
		return List.of(category.getSettings(p));
	}
}
