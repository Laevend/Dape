package coffee.dape.chaosui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.interfaces.paginator.PaginatorItem;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.utils.Logg;
import coffee.dape.utils.SoundUtils;
import coffee.dape.utils.SoundUtils.SoundMixer;

/**
 * 
 * @author Laeven
 *
 */
public abstract class ChaosComponent implements PaginatorItem
{
	private int occupyingSlot = -999;
	private ItemStack appearance;
	private Type type;
	
	private SoundMixer soundMixer = null;
	private boolean playSoundOnClick = true;
	
	private List<ChaosActionListener> actionListeners = null;
	
	/**
	 * Creates a Chaos compound that occupies 1 slot
	 * @param slot Slot this element occupies
	 * @param elementIcon The ItemStack that will represent this element as an icon
	 */
	public ChaosComponent(int slot,ItemStack icon,Type type)
	{
		this.occupyingSlot = slot;
		this.appearance = icon;
		this.type = type;
	}
	
	/**
	 * Creates a Chaos compound that occupies 1 slot
	 * 
	 * <p>FOR USE ONLY WITH PAGINATORS OR OTHER INTERFACES THAT SET
	 * THE OCCUPYING SLOT DYNAMICALLY ON BUILDING!
	 * 
	 * @param elementIcon The ItemStack that will represent this element as an icon
	 */
	public ChaosComponent(ItemStack icon,Type type)
	{
		this.appearance = icon;
		this.type = type;
	}
	
	public enum Type
	{
		BUTTON,
		CHOICE_BUTTON,
		TEXT_INPUT_BUTTON,
		TOGGLEABLE_BUTTON,
		ANIMATED_BUTTON,
		INTEGER_INPUT_BUTTON,
	}
	
	public ItemStack getAppearance()
	{
		return appearance;
	}
	
	public void setAppearance(ItemStack icon)
	{
		this.appearance = icon;
	}

	public int getOccupyingSlot()
	{
		return occupyingSlot;
	}
	
	public void setOccupyingSlot(int slotToOccupy)
	{
		this.occupyingSlot = slotToOccupy;
	}
	
	public Type getType()
	{
		return type;
	}
	
	public void setSound(Sound sound,float pitch,float volume)
	{
		soundMixer = new SoundUtils().new SoundMixer(sound,pitch,volume);
	}
	
	public void setSound(Sound sound,float pitch)
	{
		soundMixer = new SoundUtils().new SoundMixer(sound,pitch);
	}
	
	public void setSound(SoundMixer mixer)
	{
		soundMixer = mixer;
	}
	
	public SoundMixer getSoundMixer()
	{
		return soundMixer;
	}
	
	public ChaosActionListener getFrontActionListener()
	{
		if(this.actionListeners == null) { return null; }
		return actionListeners.get(0);
	}
	
	public List<ChaosActionListener> getActionListeners()
	{
		if(this.actionListeners == null) { return Collections.emptyList(); }
		return actionListeners;
	}
	
	public void removeActionListener(int index)
	{
		if(this.actionListeners == null) { return; }
		actionListeners.remove(index);
	}

	public void addActionListener(ChaosActionListener actionListener)
	{
		if(this.actionListeners == null) { actionListeners = new ArrayList<>(); }
		this.actionListeners.add(actionListener);
	}
	
	public boolean hasActions()
	{
		return actionListeners != null;
	}

	public boolean isPlaySoundOnClick()
	{
		return playSoundOnClick;
	}

	public void setPlaySoundOnClick(boolean playSoundOnClick)
	{
		this.playSoundOnClick = playSoundOnClick;
	}

	public void playSound(Player p)
	{
		if(this.soundMixer == null) { return; }
		if(!this.playSoundOnClick) { return; }
		soundMixer.play(p);
	}
	
	public void update(InventoryView view)
	{
		if(this.occupyingSlot == -999) { Logg.warn("Cannot update component " + this.type.toString() + " as it's dynamic with no set slot"); return; }
		view.setItem(occupyingSlot,appearance);
	}
}