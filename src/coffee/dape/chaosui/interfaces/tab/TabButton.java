package coffee.dape.chaosui.interfaces.tab;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.SoundUtils.SoundMixer;
import coffee.dape.utils.StdCols;

/**
 * 
 * @author Laeven
 *
 * Represents a TabButton which can be in 1 of three states. Enabled, Disabled, Focused. 
 */
public class TabButton extends ChaosComponent
{
	private TabButtonState state;
	private final EnumMap<TabButtonState,Button> buttons = new EnumMap<>(TabButtonState.class);
	
	// Index of where this button sits in the tab button array
	private int index;
	
	public TabButton(String buttonName,Button enabledButton,Button disabledButton,Button focusedButton)
	{
		super(enabledButton.getAppearance(),Type.BUTTON);
		
		Objects.requireNonNull(buttonName,"Button name cannot be null!");
		Objects.requireNonNull(enabledButton,"Enabled button cannot be null!");
		Objects.requireNonNull(disabledButton,"Disabled button cannot be null!");
		Objects.requireNonNull(focusedButton,"Focused button cannot be null!");
		
		buttons.put(TabButtonState.ENABLED,enabledButton);
		buttons.put(TabButtonState.DISABLED,disabledButton);
		buttons.put(TabButtonState.FOCUSED,focusedButton);
		
		setState(TabButtonState.ENABLED);
	}
	
	public TabButton(String buttonName)
	{
		super(ItemBuilder.of(Material.OAK_HANGING_SIGN).create(),Type.BUTTON);
		
		Objects.requireNonNull(buttonName,"Button name cannot be null!");
		
		buttons.put(TabButtonState.ENABLED,new Button(ItemBuilder.of(Material.OAK_HANGING_SIGN).name(buttonName,StdCols.TEXT_TITLE_BUTTON_ENABLED).create()));
		
		// TODO Change to Pale garden hanging sign when you update
		buttons.put(TabButtonState.DISABLED,new Button(ItemBuilder.of(Material.WARPED_HANGING_SIGN).name(buttonName,StdCols.TEXT_TITLE_BUTTON_DISABLED).create()));
		buttons.put(TabButtonState.FOCUSED,new Button(ItemBuilder.of(Material.OAK_HANGING_SIGN).name(buttonName,StdCols.TEXT_TITLE_BUTTON_FOCUSED).glint(true).create()));
		
		setState(TabButtonState.ENABLED);
	}
	
	public void setState(TabButtonState newState)
	{
		state = newState;
		setAppearance(buttons.get(state).getAppearance());
	}
	
	public Button getTabButton(TabButtonState state)
	{
		return buttons.get(state);
	}
	
	public Button setTabButton(TabButtonState state,Button button)
	{
		return buttons.put(state,button);
	}
	
	@Override
	public ItemStack getAppearance()
	{
		return buttons.get(state).getAppearance();
	}
	
	@Override
	public void setAppearance(ItemStack icon)
	{
		buttons.get(state).setAppearance(icon);
	}
	
	@Override
	public void setOccupyingSlot(int slotToOccupy)
	{
		super.setOccupyingSlot(slotToOccupy);
		
		for(Entry<TabButtonState,Button> entry : buttons.entrySet())
		{
			entry.getValue().setOccupyingSlot(slotToOccupy);
		}
	}
	
	@Override
	public void setSound(Sound sound,float pitch,float volume)
	{
		throw new UnsupportedOperationException("Cannot set sound of TabButton component! You must set the sound of the individual tab state buttons that make up this component. getTabButton().setSound()");
	}
	
	@Override
	public void setSound(Sound sound,float pitch)
	{
		throw new UnsupportedOperationException("Cannot set sound of TabButton component! You must set the sound of the individual tab state buttons that make up this component. getTabButton().setSound()");
	}
	
	@Override
	public void setSound(SoundMixer mixer)
	{
		throw new UnsupportedOperationException("Cannot set sound of TabButton component! You must set the sound of the individual tab state buttons that make up this component. getTabButton().setSound()");
	}
	
	@Override
	public SoundMixer getSoundMixer()
	{
		throw new UnsupportedOperationException("Cannot getSoundMixer of TabButton component! You must get the sound mixer of the individual tab state buttons that make up this component. getTabButton().getSoundMixer()");
	}
	
	@Override
	public boolean hasActions()
	{
		return buttons.get(state).hasActions();
	}
	
	@Override
	public boolean isPlaySoundOnClick()
	{
		return buttons.get(state).isPlaySoundOnClick();
	}
	
	@Override
	public void setPlaySoundOnClick(boolean playSoundOnClick)
	{
		buttons.get(state).setPlaySoundOnClick(playSoundOnClick);
	}
	
	@Override
	public void playSound(Player p)
	{
		buttons.get(state).playSound(p);
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
	public TabButton getComponent()
	{
		return this;
	}
	
	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}
	
	public EnumMap<TabButtonState, Button> getButtons()
	{
		return buttons;
	}

	public TabButtonState getState()
	{
		return state;
	}

	public enum TabButtonState
	{
		ENABLED,
		DISABLED,
		FOCUSED
	}
}