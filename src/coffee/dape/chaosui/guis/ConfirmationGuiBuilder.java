package coffee.dape.chaosui.guis;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.ChaosFactory.InvTemplate;
import coffee.dape.chaosui.GUISession;
import coffee.dape.chaosui.anno.ChaosGUI;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;

@ChaosGUI(name = ChaosFactory.Common.CONFIRMATION,template = InvTemplate.HOPPER)
public class ConfirmationGuiBuilder extends ChaosBuilder
{
	@Override
	public void init()
	{
		putStaticComponent(new Button(0,ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE)
				.name("Confirm",ColourUtils.TEXT_SUCCESS)
				.create(),new ChaosActionListener()
		{
			@EventHandler
			public void onClick(ChaosClickEvent e)
			{
				ConfirmBox.getConfirmSession((Player) e.getWhoClicked()).getConfirmAction().run();
			}
		}));
		
		putStaticComponent(new Button(1,ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE)
				.name("Confirm",ColourUtils.TEXT_SUCCESS)
				.create(),new ChaosActionListener()
		{
			@EventHandler
			public void onClick(ChaosClickEvent e)
			{
				ConfirmBox.getConfirmSession((Player) e.getWhoClicked()).getConfirmAction().run();
			}
		}));
		
		putStaticComponent(new Button(2,ItemBuilder.of(HeadUtils.INFO_ICON.clone())
				.name("Confirmation",ColourUtils.VISTA_BLUE)
				.create()));
		
		putStaticComponent(new Button(3,ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
				.name("Cancel",ColourUtils.TEXT_ERROR)
				.create(),new ChaosActionListener()
		{
			@EventHandler
			public void onClick(ChaosClickEvent e)
			{
				ConfirmBox.getConfirmSession((Player) e.getWhoClicked()).getCancelAction().run();
			}
		}));
		
		putStaticComponent(new Button(4,ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
				.name("Cancel",ColourUtils.TEXT_ERROR)
				.create(),new ChaosActionListener()
		{
			@EventHandler
			public void onClick(ChaosClickEvent e)
			{
				ConfirmBox.getConfirmSession((Player) e.getWhoClicked()).getCancelAction().run();
			}
		}));
	}

	@Override
	public void buildGUI(InventoryView view)
	{
		if(ConfirmBox.getConfirmSession((Player) view.getPlayer()) == null)
		{
			throw new IllegalArgumentException(view.getPlayer().getName() + " opened confirm box incorrectly! Use ConfirmBox.openConfirmBox()");		
		}
		
		GUISession sess = ChaosFactory.getSession((Player) view.getPlayer());
		String confirmQuestion = sess.getData(this,"confirm_question").getAsString();
		
		if(sess.hasGlobalData("confirm_item"))
		{
			view.setItem(2,ItemBuilder.of((ItemStack) sess.getGlobalData("confirm_item"))
					.lore("")
					.wrap(ColourUtils.applyColour(confirmQuestion,ColourUtils.TEXT))
					.commit()
					.create());
		}
		else
		{
			view.setItem(2,ItemBuilder.of(view.getItem(2))
					.lore()
					.wrap(ColourUtils.applyColour(confirmQuestion,ColourUtils.TEXT))
					.commit()
					.create());
		}
	}
}
