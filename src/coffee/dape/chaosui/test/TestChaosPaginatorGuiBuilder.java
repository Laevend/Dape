package coffee.dape.chaosui.test;

import org.bukkit.inventory.InventoryView;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory.InvTemplate;
import coffee.dape.chaosui.anno.ChaosGUI;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.interfaces.paginator.Paginator;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.MaterialUtils;

@ChaosGUI(name = "TestGUIPaginator",handler = TestChaosPaginatorGuiHandler.class,template = InvTemplate.CHEST_6)
public class TestChaosPaginatorGuiBuilder extends ChaosBuilder //implements DynamicPaginatorContents
{
	@Override
	public void init()
	{
		setInterface(new Paginator(Paginator.wrapItemstacks(MaterialUtils.getListOfRandomMaterials(128))));
		
		putStaticComponent(new Button(4,ItemBuilder.of(HeadUtils.INFO_ICON.clone())
				.name("Information",ColourUtils.VISTA_BLUE)
				.lore()
				.wrap(ColourUtils.applyColour("Hello there this is some info. This is a test GUI for the paginator interface.",ColourUtils.TEXT))
				.commit()
				.create()));
		
		setNavigationBack("TestGUI",0);
	}

	@Override
	public void buildGUI(InventoryView view)
	{
		//GUISession session = getSession(view);
	}

//	@Override
//	public List<ChaosStack> refreshPaginator(Player p)
//	{
//		return MaterialUtils.getListOfRandomMaterials(128);
//	}
}
