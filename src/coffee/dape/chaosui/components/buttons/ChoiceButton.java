package coffee.dape.chaosui.components.buttons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.events.ChaosChoiceButtonClickEvent;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.handler.ChoiceButtonHandler;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.MathUtils;
import coffee.dape.utils.data.DataUtils;

/**
 * 
 * @author Laeven
 *
 */
public class ChoiceButton extends ChaosComponent
{
	private ItemStack[] stackChoices;
	public static final String CHOICE_INDEX = "choice_index";
	private static final Sound defaultButtonSound = Sound.UI_LOOM_SELECT_PATTERN;
	private static final float defaultButtonPitch = 1.2f;
	
	public ChoiceButton(int slot,String name,Material mat,String... choices)
	{
		super(slot,null,Type.CHOICE_BUTTON);
		initChoiceButton(name,mat,choices);
	}
	
	public ChoiceButton(String name,Material mat,String... choices)
	{
		super(null,Type.CHOICE_BUTTON);
		initChoiceButton(name,mat,choices);
	}
	
	private void initChoiceButton(String name,Material mat,String... choices)
	{
		this.stackChoices = new ItemStack[choices.length];
		List<String> choiceLore = new ArrayList<>();
		
		for(String lore : choices)
		{
			choiceLore.add(ColourUtils.translate("&7" + lore));
		}
		
		for(int i = 0; i < choices.length; i++)
		{
			this.stackChoices[i] = ItemBuilder.of(mat)
					.name(name)
					.lore(choiceLore)
					.replace(i,"&e" + choices[i])
					.commit()
					.setData("choice",choices[i])
					.create();
		}
		
		this.setAppearance(this.stackChoices[0]);
		this.setSound(defaultButtonSound,defaultButtonPitch);
		this.addActionListener(new ChaosActionListener()
		{
			@Override
			public void onClick(ChaosClickEvent e)
			{
				int oldChoice = DataUtils.get(CHOICE_INDEX,e.getView().getItem(e.getRawSlot())).asInt();
				ItemStack newChoiceStack = getNextStack(oldChoice,e.getClick());
				
				// Get new choice like this as getStack() will roll the index around to 0 if it goes too high
				int newChoice = DataUtils.get(CHOICE_INDEX,newChoiceStack).asInt();
				e.getView().setItem(e.getRawSlot(),newChoiceStack);
				
				if(e.getBuilder().getHandler() instanceof ChoiceButtonHandler handler)
				{
					handler.onChoiceButtonClick(new ChaosChoiceButtonClickEvent(e.getView(),e.getRawSlot(),e.getBuilder(),(Player) e.getWhoClicked(),oldChoice,newChoice,newChoiceStack));
				}
			}
		});
	}
	
	/**
	 * Moves a choice ItemStack to the front of the array making it the default one
	 * @param defaultChoice Default choice to set
	 */
	public void setDefaultValue(String defaultChoice)
	{
		String choice = "";
		
		for(int i = 0; i < stackChoices.length; i++)
		{
			choice = DataUtils.get("choice",stackChoices[i]).asString();
			if(defaultChoice.equals(choice))
			{
				// No point moving it it's already at index 0
				if(i == 0) { break; }
				
				setAppearance(this.stackChoices[i]);
				break;
			}
		}
	}
	
	public ItemStack getStack(int currentChoiceIndex)
	{
		return stackChoices[currentChoiceIndex];
	}
	
	public String getChoice(int currentChoiceIndex)
	{
		return DataUtils.get("choice",stackChoices[currentChoiceIndex]).asString();
	}
	
	public void setChoice(int newChoiceIndex,InventoryView view)
	{
		view.setItem(getOccupyingSlot(),stackChoices[newChoiceIndex]);
	}
	
	public void setChoice(String newChoice,InventoryView view)
	{
		String choice = "";
		
		for(int i = 0; i < stackChoices.length; i++)
		{
			choice = DataUtils.get("choice",stackChoices[i]).asString();
			
			if(newChoice.equals(choice))
			{
				view.setItem(getOccupyingSlot(),stackChoices[i]);
				break;
			}
		}
	}
	
	/**
	 * Gets a choice stack
	 * @return Chosen stack
	 */
	public ItemStack getNextStack(int currentChoiceIndex,ClickType type)
	{
		int newChoiceIndex = 0;
		
		if(type == ClickType.LEFT)
		{
			newChoiceIndex = MathUtils.clampAndRoll(0,stackChoices.length - 1,currentChoiceIndex,1);
		}
		else if(type == ClickType.RIGHT)
		{
			newChoiceIndex = MathUtils.clampAndRoll(0,stackChoices.length - 1,currentChoiceIndex,-1);
		}
		
		return stackChoices[newChoiceIndex];
	}
	
	@Override
	public ItemStack getAppearance()
	{
		return stackChoices[0];
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
}