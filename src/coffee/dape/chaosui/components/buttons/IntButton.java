package coffee.dape.chaosui.components.buttons;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.InputUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.SoundUtils;
import coffee.dape.utils.data.DataUtils;

/**
 * 
 * @author Laeven
 *
 */
public class IntButton extends ChaosComponent
{
	private int max;
	private int min;
	private float pitchPerVal;
	
	public IntButton(int slot,String name,int defaultValue,int maxVal,int minVal)
	{
		super(slot,null,Type.INTEGER_INPUT_BUTTON);
		initIntButton(name,defaultValue,maxVal,minVal);
	}
	
	public IntButton(String name,int defaultValue,int maxVal,int minVal)
	{
		super(null,Type.INTEGER_INPUT_BUTTON);
		initIntButton(name,defaultValue,maxVal,minVal);
	}
	
	public void initIntButton(String name,int defaultValue,int maxVal,int minVal)
	{
		setAppearance(ItemBuilder.of(HeadUtils.MONITOR.clone())
				.name(name)
				.lore()
				.wrap("&8[ &e" + defaultValue + " &8]")
				.append("")
				.wrap(InputUtils.LeftClick.LEFT_CLICK + " " + ColourUtils.applyColour(" +1",ColourUtils.TEXT))
				.wrap(InputUtils.RightClick.RIGHT_CLICK + " " + ColourUtils.applyColour(" -1",ColourUtils.TEXT))
				.wrap(InputUtils.ShiftLeftClick.SHIFT_LEFT_CLICK + " " + ColourUtils.applyColour(" +10",ColourUtils.TEXT))
				.wrap(InputUtils.ShiftRightClick.SHIFT_RIGHT_CLICK + " " + ColourUtils.applyColour(" -10",ColourUtils.TEXT))
				.commit()
				.setData("button_value",defaultValue)
				.create());
		
		this.max = maxVal;
		this.min = minVal;
		this.pitchPerVal = 1.5f / (max - min);
		
		this.addActionListener(new ChaosActionListener()
		{
			@Override
			public void onClick(ChaosClickEvent e)
			{
				int oldValue = DataUtils.get("button_value",e.getCurrentItem()).asInt();
				int newValue = 0;
				
				switch(e.getClick())
				{
					case LEFT: { newValue = oldValue + 1; break; }
					case RIGHT: { newValue = oldValue - 1; break; }
					case SHIFT_LEFT: { newValue = oldValue + 10; break; }
					case SHIFT_RIGHT: { newValue = oldValue - 10; break; }
					default: { break; }
				}
				
				if(newValue > max || newValue < min)
				{
					SoundUtils.playErrorSound((Player) e.getWhoClicked());
					PrintUtils.error(e.getWhoClicked(),"Cannot increase/decrease value further!");
					return;
				}
				
				int pitchMultiply = (newValue - min);
				
				// + .5 because pitch sounds the same from 0.0 to 0.5
				float valueChangePitch = (pitchPerVal * pitchMultiply) + .5f;
				
				SoundUtils.playSound((Player) e.getWhoClicked(),Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE,valueChangePitch);
				DataUtils.set("button_value",newValue,e.getCurrentItem());
				
				e.getView().setItem(e.getRawSlot(),ItemBuilder.of(e.getCurrentItem()).lore().replace(0,"&8[ &e" + newValue + " &8]").commit().create());
			}
		});
	}
	
	public void setDefaultButtonValue(int newValue)
	{
		DataUtils.set("button_value",newValue,getStack());
		setAppearance(ItemBuilder.of(getStack()).lore().replace(0,"&8[ &e" + newValue + " &8]").commit().create());
	}
	
	public void setButtonValue(int newValue,InventoryView view)
	{
		DataUtils.set("button_value",newValue,view.getItem(getOccupyingSlot()));
		view.setItem(getOccupyingSlot(),ItemBuilder.of(view.getItem(getOccupyingSlot())).lore().replace(0,"&8[ &e" + newValue + " &8]").commit().create());
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