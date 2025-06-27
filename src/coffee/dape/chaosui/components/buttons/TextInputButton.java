package coffee.dape.chaosui.components.buttons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.CachedGUI;
import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.events.ChaosTextInputEvent;
import coffee.dape.chaosui.handler.TextInputButtonHandler;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.InputUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.SoundUtils;

/**
 * 
 * @author Laeven
 *
 */
public class TextInputButton extends ChaosComponent
{
	private ItemStack textInputItem;
	private String title;
	private String inputMessage;
	private String buttonInteractMessage;
	private String inputValue;
	private Material textInputMat;
	private static final Sound defaultButtonSound = Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT;
	private static final float defaultButtonPitch = 1.5f;
	
	/**
	 * Create a new text input button
	 * 
	 * <p>This button will close the GUI and allow text input via chat.
	 * The next text the use chats with will be used as input and the GUI will be reopened.
	 * @param slot Slot this button will be placed in
	 * @param title The title of this button
	 * @param textInputMat The material of this sign button, must be a sign type!
	 */
	public TextInputButton(int slot,String title,String inputMessage,String buttonInteractMessage,String initialValue,Material textInputMat)
	{
		super(slot,null,Type.TEXT_INPUT_BUTTON);
		initTextInput(title,inputMessage,buttonInteractMessage,initialValue,textInputMat);
	}
	
	/**
	 * Create a new text input button
	 * 
	 * <p>This button will close the GUI and allow text input via chat.
	 * The next text the use chats with will be used as input and the GUI will be reopened.
	 * @param title The title of this button
	 * @param textInputMat The material of this sign button, must be a sign type!
	 * @param displayedValueTitle The title of what the value is
	 * @param defaultValue The default value displayed
	 */
	public TextInputButton(String title,String inputMessage,String buttonInteractMessage,String initialValue,Material textInputMat)
	{
		super(null,Type.TEXT_INPUT_BUTTON);
		initTextInput(title,inputMessage,buttonInteractMessage,initialValue,textInputMat);
	}
	
	private void initTextInput(String title,String inputMessage,String buttonInteractMessage,String initialValue,Material textInputMat)
	{
		this.title = title;
		this.inputMessage = inputMessage;
		this.buttonInteractMessage = buttonInteractMessage;
		this.inputValue = initialValue;
		this.textInputMat = textInputMat;
		
		textInputItem = ItemBuilder.of(textInputMat)
				.name(title,ColourUtils.LIGHT_GREEN)
				.lore(ColourUtils.applyColour("Current value: ",ColourUtils.TEXT) + ColourUtils.applyColour(initialValue,ColourUtils.VISTA_BLUE))
				.append("")
				.wrap(InputUtils.LeftClick.LEFT_CLICK + " " + ColourUtils.applyColour(buttonInteractMessage,ColourUtils.TEXT))
				.commit()
				.setData("text_input",initialValue)
				.create();
		
		setAppearance(textInputItem);
		setSound(defaultButtonSound,defaultButtonPitch);
		addActionListener(new ChaosActionListener()
		{
			public void onClick(ChaosClickEvent e)
			{
				if(e.getClick() != ClickType.LEFT) { return; }
				
				Player p = (Player) e.getWhoClicked();
				PrintUtils.info(p,inputMessage);
				ChaosFactory.setTextInputMode(p);
				ChaosFactory.getSession(p).setInputButtonSlotClicked(e.getBuilder(),e.getRawSlot());
				ChaosFactory.cacheGUI(p,e.getView(),e.getBuilder(),e.getRawSlot());
				p.closeInventory();
				SoundUtils.playSound(p,Sound.BLOCK_NOTE_BLOCK_XYLOPHONE,0.1f);
			}
		});
	}

	public static void onTextInput(AsyncPlayerChatEvent e)
	{
		if(!ChaosFactory.hasCachedGUI(e.getPlayer())) { return; }
		
		CachedGUI cGUI = ChaosFactory.getCachedGUI(e.getPlayer());
		InventoryView view = cGUI.getView();
		ChaosBuilder builder = ChaosFactory.getGUI(view.getTitle());
		String input = e.getMessage();
		int slot = ChaosFactory.getSession(e.getPlayer()).getInputButtonSlotClicked(builder);
		
		// event does not know which sign button was used in gui (if there are many, same goes with choice button)
		// maybe have a section for this in gui session? internal sign session?
		ChaosTextInputEvent textInputEvent = new ChaosTextInputEvent(view,ChaosFactory.getSession(e.getPlayer()).getInputButtonSlotClicked(builder),ChaosFactory.getGUI(view.getTitle()),e.getPlayer(),input);		
		e.getPlayer().openInventory(view);
		SoundUtils.playSound(e.getPlayer(),Sound.BLOCK_NOTE_BLOCK_XYLOPHONE,1.0f);
		ChaosFactory.clearCacheGUI(e.getPlayer());
		
		TextInputButton comp = null;
		
		if(builder.getSlots().containsKey(slot))
		{
			comp = (TextInputButton) builder.getSlots().get(slot).getSlotComponent();
		}
		else if(ChaosFactory.getSession(e.getPlayer()).getSessionSlots().containsKey(slot))
		{
			comp = (TextInputButton) ChaosFactory.getSession(e.getPlayer()).getSessionSlots().get(slot).getSlotComponent();
		}
		else
		{
			Logg.error("Could not accept text input for ChaosGUI " + builder.getName() + " Component for text input button could not be found!");
			return;
		}
		
		comp.setInputValue(input);
		view.setItem(slot,comp.getAppearance());
		
		if(builder.getHandler() instanceof TextInputButtonHandler handler)
		{
			handler.onTextInputButtonClick(textInputEvent);
		}
	}

	@Override
	public ItemStack getStack()
	{
		return getAppearance();
	}
	
	@Override
	public boolean isItemComponentType()
	{
		return true;
	}

	@Override
	public ChaosComponent getComponent()
	{
		return this;
	}

	public String getInputMessage()
	{
		return inputMessage;
	}
	
	public String getInputValue()
	{
		return inputValue;
	}
	
	public String getButtonInteractMessage()
	{
		return buttonInteractMessage;
	}

	public void setInputValue(String inputValue)
	{
		this.inputValue = inputValue;
		
		textInputItem = ItemBuilder.of(this.textInputMat)
				.name(title,ColourUtils.LIGHT_GREEN)
				.lore(ColourUtils.applyColour("Current value: ",ColourUtils.TEXT) + ColourUtils.applyColour(inputValue,ColourUtils.VISTA_BLUE))
				.append("")
				.wrap(InputUtils.LeftClick.LEFT_CLICK + " " + ColourUtils.applyColour(buttonInteractMessage,ColourUtils.TEXT))
				.commit()
				.setData("text_input",inputValue)
				.create();
		
		setAppearance(textInputItem);
	}
}