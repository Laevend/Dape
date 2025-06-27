package coffee.dape.chaosui.guis;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.InventoryView;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.ChaosFactory.InvTemplate;
import coffee.dape.chaosui.GUISession;
import coffee.dape.chaosui.anno.ChaosGUI;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.components.buttons.TextInputButton;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;

@ChaosGUI(name = ChaosFactory.Common.CONFIRMATION_INPUT,handler = ConfirmationTextInputGuiHandler.class,template = InvTemplate.CHEST_3)
public class ConfirmationTextInputGuiBuilder extends ChaosBuilder
{
	@Override
	public void init()
	{
		putStaticComponent(new Button(4,ItemBuilder.of(HeadUtils.EXCLAMATION_MARK.clone())
				.name("Confirmation",ColourUtils.VISTA_BLUE)
				.lore()
				.wrap(ColourUtils.applyColour("To confirm your choice, you must type a phrase.",ColourUtils.TEXT))
				.commit()
				.create()));
		
		putStaticComponent(new Button(10,ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
				.name("Cancel",ColourUtils.TEXT_ERROR)
				.create(),new ChaosActionListener()
		{
			@EventHandler
			public void onClick(ChaosClickEvent e)
			{
				ConfirmTextInputBox.getConfirmSession((Player) e.getWhoClicked()).getCancelAction().run();
			}
		}));
		
		setFill(Material.GRAY_STAINED_GLASS_PANE);
	}

	@Override
	public void buildGUI(InventoryView view)
	{
		if(ConfirmTextInputBox.getConfirmSession((Player) view.getPlayer()) == null)
		{
			throw new IllegalArgumentException(view.getPlayer().getName() + " opened confirm box incorrectly! Use ConfirmTextInputBox.openConfirmBox()");		
		}
		
		GUISession sess = ChaosFactory.getSession((Player) view.getPlayer());
		String confirmMsg = sess.getData(this,"confirm_msg").getAsString();
		String confirmPhrase = sess.getData(this,"confirm_phrase").getAsString();
		
		sess.setSessionComponent(new TextInputButton(16,"Confirmation Phrase",confirmMsg,"Enter phrase '" + confirmPhrase + "' to confirm","",Material.NAME_TAG));
		
		view.setItem(16,sess.getSessionSlots().get(16).getSlotComponent().getAppearance());
	}
}
