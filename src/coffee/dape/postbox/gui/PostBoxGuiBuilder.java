package coffee.dape.postbox.gui;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.InventoryView;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.ChaosFactory.InvTemplate;
import coffee.dape.chaosui.anno.ChaosGUI;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.components.buttons.ToggleButton;
import coffee.dape.chaosui.interfaces.tab.TabButton;
import coffee.dape.chaosui.interfaces.tab.Tabs;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.SoundUtils;
import coffee.dape.utils.StdCols;
import coffee.dape.utils.structs.Pair;

@ChaosGUI(name = ChaosFactory.Common.POST_BOX,template = InvTemplate.CHEST_6)
public class PostBoxGuiBuilder extends ChaosBuilder
{
	@Override
	public void init()
	{
		setOpenSound(SoundUtils.DUSTING);
		setInterface(new Tabs(List.of(
				new Pair<>(new TabButton(
						"Inbox",
						new Button(ItemBuilder.of(Material.OAK_HANGING_SIGN).name("Inbox",ColourUtils.TEXT).create()),
						new Button(ItemBuilder.of(Material.PALE_OAK_HANGING_SIGN).name("Inbox",ColourUtils.TEXT).create()),
						new Button(ItemBuilder.of(Material.OAK_HANGING_SIGN).name("Inbox",ColourUtils.TEXT_SUCCESS).glint(true).create())
						),new InboxGuiBuilder()),

				new Pair<>(new TabButton(
						"Sent",
						new Button(ItemBuilder.of(Material.BIRCH_HANGING_SIGN).name("Sent",ColourUtils.TEXT).create()),
						new Button(ItemBuilder.of(Material.PALE_OAK_HANGING_SIGN).name("Sent",ColourUtils.TEXT).create()),
						new Button(ItemBuilder.of(Material.OAK_HANGING_SIGN).name("Sent",ColourUtils.TEXT_SUCCESS).glint(true).create())
						),new SentGuiBuilder()),

				new Pair<>(new TabButton(
						"Drafts",
						new Button(ItemBuilder.of(Material.OAK_HANGING_SIGN).name("Drafts",ColourUtils.TEXT).create()),
						new Button(ItemBuilder.of(Material.PALE_OAK_HANGING_SIGN).name("Drafts",ColourUtils.TEXT).create()),
						new Button(ItemBuilder.of(Material.OAK_HANGING_SIGN).name("Drafts",ColourUtils.TEXT_SUCCESS).glint(true).create())
						),new DraftsGuiBuilder())
				)));
		
		Button info = new Button(8,ItemBuilder.of(HeadUtils.INFO_ICON.clone())
				.name("Information",ColourUtils.VISTA_BLUE)
				.lore().wrap(ColourUtils.applyColour("This menu allows you to view, send, and draft messages.",StdCols.TEXT_DEFAULT))
				.commit()
				.create());
		
		Button compose = new Button(0,ItemBuilder.of(Material.WRITABLE_BOOK)
				.name("Compose Message",ColourUtils.VISTA_BLUE)
				.create());
		
		putAllStaticComponents(info,compose);
		
		putStaticComponent(new ToggleButton(53,true,"Deletion Warning Message"));
		
		setFill(Material.GRAY_STAINED_GLASS_PANE);
	}

	@Override
	public void buildGUI(InventoryView view) {}
}
