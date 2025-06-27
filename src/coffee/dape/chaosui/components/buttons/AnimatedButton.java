package coffee.dape.chaosui.components.buttons;

import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.utils.MathUtils;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.clocks.RepeatingClock;

/**
 * 
 * @author Laeven
 *
 */
public class AnimatedButton extends ChaosComponent
{
	private LinkedList<ItemStack> items;
	private int intervalInTicks = 20;
	private int variance = 0;			// Variance in the delay until the next item is shown
	private AnimatedButtonClock clock = null;
	private static final Sound defaultButtonSound = Sound.BLOCK_LODESTONE_BREAK;
	private static final float defaultButtonPitch = 2.0f;
	
	/**
	 * Creates an AnimatedItem that occupies 1 slot
	 * @param slot Slot this element occupies
	 * @param elementIcons The list of ItemStacks that will represent this element as an icon
	 * @param delay The delay in seconds between each ItemStack icon being displayed
	 */
	public AnimatedButton(int slot,int intervalInTicks,LinkedList<ItemStack> elementIcons)
	{
		super(slot,elementIcons.getFirst(),Type.ANIMATED_BUTTON);
		this.intervalInTicks = intervalInTicks;
		this.items = elementIcons;
		this.setSound(defaultButtonSound,defaultButtonPitch);
		
		buildAndCycle(MathUtils.getRandom(0,elementIcons.size() - 1));
	}
	
	/**
	 * Creates an AnimatedItem that occupies 1 slot
	 * @param slot Slot this element occupies
	 * @param elementIcons The list of ItemStacks that will represent this element as an icon
	 * @param delay The delay in seconds between each ItemStack icon being displayed
	 */
	public AnimatedButton(int slot,int intervalInTicks,LinkedList<ItemStack> elementIcons,ChaosActionListener listener)
	{
		this(slot,intervalInTicks,elementIcons);
		this.addActionListener(listener);
	}
	
	/**
	 * Creates an AnimatedItem that occupies 1 slot
	 * @param elementIcons The list of ItemStacks that will represent this element as an icon
	 * @param delay The delay in seconds between each ItemStack icon being displayed
	 */
	public AnimatedButton(int intervalInTicks,LinkedList<ItemStack> elementIcons)
	{
		super(elementIcons.getFirst(),Type.ANIMATED_BUTTON);
		this.intervalInTicks = intervalInTicks;
		this.items = elementIcons;
		this.setSound(defaultButtonSound,defaultButtonPitch);
		
		buildAndCycle(MathUtils.getRandom(0,elementIcons.size() - 1));
	}
	
	/**
	 * Creates an AnimatedItem that occupies 1 slot
	 * @param elementIcons The list of ItemStacks that will represent this element as an icon
	 * @param delay The delay in seconds between each ItemStack icon being displayed
	 */
	public AnimatedButton(int intervalInTicks,LinkedList<ItemStack> elementIcons,ChaosActionListener listener)
	{
		this(intervalInTicks,elementIcons);
		this.addActionListener(listener);
	}
	
	public LinkedList<ItemStack> getIcons()
	{
		return items;
	}
	
	public int getIntervalInTicks()
	{
		return intervalInTicks;
	}
	
	public int getVariance()
	{
		return variance;
	}

	public void setVariance(int variance)
	{
		this.variance = variance;
	}

	public AnimatedButtonClock getClock()
	{
		return clock;
	}
	
	public void buildAndCycle(int cycleTimes)
	{
		for(int i = 0; i < cycleTimes; i++)
		{
			ItemStack front = this.items.removeFirst();
			this.items.addLast(front);
		}
	}

	public void buildAndCycle(InventoryView view)
	{
		ItemStack front = this.items.removeFirst();
		this.items.addLast(front);
		view.setItem(getOccupyingSlot(),this.items.getFirst());
	}
	
	public void initClock(ChaosBuilder builder)
	{
		this.clock = new AnimatedButtonClock(builder,this.intervalInTicks,this.variance);
	}
	
	public void restartClock()
	{
		if(clock.isEnabled()) { return; }
		clock.start();
	}
	
	public void stop()
	{
		if(!clock.isEnabled()) { return; }
		clock.stop();
	}
	
	public class AnimatedButtonClock extends RepeatingClock
	{
		private ChaosBuilder builder;
		
		public AnimatedButtonClock(ChaosBuilder builder,int interval,int variance)
		{
			// TODO Use BukkitClockWithVariance
			super("Animated button clock",interval);
			this.builder = builder;
		}

		@Override
		public void execute() throws Exception
		{
			if(this.builder.getViewers().isEmpty()) { this.stop(); return; }
			if(!this.isEnabled() || clock.isCancelled()) { return; }
			
			for(UUID playerUUID : this.builder.getViewers())
			{			
				buildAndCycle(PlayerUtils.getPlayer(playerUUID).getOpenInventory());
			}
		}
	}

	@Override
	public ItemStack getStack()
	{
		return this.items.getFirst();
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