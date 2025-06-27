package coffee.dape.playerdata.gui;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.ChaosFactory.InvTemplate;
import coffee.dape.chaosui.GUISession;
import coffee.dape.chaosui.anno.ChaosGUI;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.interfaces.paginator.Paginator;
import coffee.dape.playerdata.data.PlayerSettings;
import coffee.dape.playerdata.data.PlayerSettings.SettingCategory;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;

@ChaosGUI(name = ChaosFactory.Common.PLAYER_SETTINGS,template = InvTemplate.CHEST_6)
public class PlayerSettingsGuiBuilder extends ChaosBuilder
{
	public static final String DT_SETTING_CAT = "setting_cat";
	public static PlayerSettingsGuiBuilder instance;
	
	@Override
	public void init()
	{
		setInterface(new Paginator(List.of(PlayerSettings.SettingCategory.values())));
		
		putStaticComponent(new Button(4,ItemBuilder.of(HeadUtils.INFO_ICON.clone())
				.name("Information",ColourUtils.VISTA_BLUE)
				.lore()
				.wrap(ColourUtils.applyColour("Here you can customise and tailor your server experience.",ColourUtils.TEXT))
				.create()));
		
		instance = this;
	}

	@Override
	public void buildGUI(InventoryView view) {}
	
	public void setSettingCategoryInUse(Player p,SettingCategory cat)
	{
		GUISession sess = ChaosFactory.getSession(p);
		sess.setData(this,DT_SETTING_CAT,cat.toString());
	}
	
	public SettingCategory getSettingCategoryInUse(Player p)
	{
		GUISession sess = ChaosFactory.getSession(p);
		
		if(!sess.hasData(this,DT_SETTING_CAT)) { return null; }
		
		return SettingCategory.valueOf(sess.getData(this,DT_SETTING_CAT).getAsString().toUpperCase());
	}
}
