package coffee.dape.chaosui.interfaces.tab;

import java.util.List;
import java.util.Objects;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.components.ChaosRegion;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.events.ChaosDragEvent;
import coffee.dape.chaosui.handler.TabsHandler;
import coffee.dape.chaosui.interfaces.ChaosInterface;
import coffee.dape.chaosui.interfaces.tab.TabButton.TabButtonState;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.ItemUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.MathUtils;
import coffee.dape.utils.SoundUtils;
import coffee.dape.utils.security.Bouncer;
import coffee.dape.utils.structs.Pair;

/**
 * @author Laeven
 */
public class Tabs extends ChaosInterface
{
	public static final String PAGE_TAG = "tab_button_page";
	
	protected final static int defaultNextTabSlot = 7;	// Default slot where next tab button is displayed
	protected final static int defaultPrevTabSlot = 1;	// Default slot where previous tab button is displayed
	
	protected int nextTabSlot = 7;					// Slot where next tab button is displayed
	protected int prevTabSlot = 1;					// Slot where previous tab button is displayed
	protected int tabButtonsPageNumber = 1;			// When the number of tabs exceeds the space provided, tab pages are created. This is which page of tab buttons we are on.
	
	protected int iconsPerPage = 45;				// Number of icons displayed per page
	protected int tabButtonSlots[] = {2,3,4,5,6};	// Slots where tab buttons can be placed
	protected TabButton visibleTabButtons[];		// Array of a subset of TabButtons that are currently visible
	protected int tabSlots[];						// Slots where contents of nested builder can be built in
	protected int selectedTabIndex = 0;				// The tab index currently selected
	protected List<Pair<TabButton,? extends ChaosBuilder>> tabs;
	
	protected Button nextTabButton;
	protected Button prevTabButton;
	
	/**
	 * Creates a new Tabs interface
	 * @param <T> Nested builder using the @NestedChaosBuilder annotation
	 * @param tabs An ordered array of tabs containing the Button used to display a tab paired with a nested builder
	 */
	public Tabs(List<Pair<TabButton,? extends ChaosBuilder>> tabs)
	{
		super(ChaosInterface.Type.TABS);
		
		Bouncer.requireNotNullOrEmpty(tabs,"Tabs cannot be null or empty!");
		
		this.tabSlots = new int[this.iconsPerPage];
		for(int i = 9; i < 53; i++)
		{
			this.tabSlots[i - 9] = i;
		}
		
		this.tabs = tabs;
		
		indexTabButtonsAndAttatchRenderListeners();
	}
	
	/**
	 * Creates a new Tabs interface
	 * @param <T> Nested builder using the @NestedChaosBuilder annotation
	 * @param startSlot The first raw slot from which the tab contents should fill the GUI
	 * @param endSlot The last raw slot from which the tab contents should fill the GUI
	 */
	public Tabs(List<Pair<TabButton,? extends ChaosBuilder>> tabs,int startSlot, int endSlot)
	{
		super(ChaosInterface.Type.PAGINATOR);
		
		Bouncer.requireNotNullOrEmpty(tabs,"Tabs cannot be null or empty!");
		Objects.requireNonNull(startSlot,"Start slot cannot be null!");
		Objects.requireNonNull(endSlot,"End slot cannot be null!");
		
		if(MathUtils.inclusiveRange(0,54,startSlot))
		{
			Logg.throwIllegalArgumentError("Start slot for a tab cannot be less than 0 or more than 54!");
			return;
		}
		
		if(MathUtils.inclusiveRange(0,54,endSlot))
		{
			Logg.throwIllegalArgumentError("End slot for a tab cannot be less than 0 or more than 54!");
			return;
		}
		
		if(startSlot >= endSlot)
		{
			Logg.throwIllegalArgumentError("Start slot for a tab cannot be greater than or equal to the end slot!");
			return;
		}
		
		// +1 because slot numbering starts at 0
		this.iconsPerPage = (endSlot - startSlot) + 1;
		this.tabSlots = new int[this.iconsPerPage];
		
		for(int i = startSlot; i <= endSlot; i++)
		{
			this.tabSlots[i - startSlot] = i;
		}
		
		this.tabs = tabs;
		
		indexTabButtonsAndAttatchRenderListeners();
	}
	
	/**
	 * Creates a new Tabs interface
	 * @param <T> Nested builder using the @NestedChaosBuilder annotation
	 * @param tabs An ordered array of tabs containing the Button used to display a tab paired with a nested builder
	 * @param slots An integer array containing all the raw slots to be filled by the paginator
	 */
	public Tabs(List<Pair<TabButton,? extends ChaosBuilder>> tabs,int slots[])
	{
		super(ChaosInterface.Type.PAGINATOR);
		
		Bouncer.requireNotNullOrEmpty(tabs,"Tabs cannot be null or empty!");
		Bouncer.requireNotNullOrEmpty(slots,"Slots cannot be null!");
		
		for(int slot : slots)
		{
			Bouncer.requireNotNullAndInRange(slot,0,54,"You cannot have a slot larger than 54 or less than 0! -> " + slot);
		}
		
		this.tabSlots = slots;
		this.iconsPerPage = tabSlots.length;
		this.tabs = tabs;
		
		indexTabButtonsAndAttatchRenderListeners();
	}
	
	/**
	 * Indexes all the tab button components in the tab button list and attaches the action listener to render the tab
	 * <p>
	 * This acts as a quick look up to check if the next/previous scrolling buttons can scroll
	 */
	private void indexTabButtonsAndAttatchRenderListeners()
	{
		for(int i = 0; i < this.tabs.size(); i++)
		{
			Pair<TabButton,? extends ChaosBuilder> tab = this.tabs.get(i);
			tab.getValueA().setIndex(i);
			
			// TODO set action listener rather than just assume it is the front action listener?
			tab.getValueA().addActionListener(new ChaosActionListener()
			{
				@Override
				public void onClick(ChaosClickEvent e)
				{
					if(tab.getValueA().getState() == TabButtonState.DISABLED)
					{
						SoundUtils.playErrorSound((Player) e.getView().getPlayer());
						return;
					}
					
					// Change previous tab button state back to enabled
					tabs.get(selectedTabIndex).getValueA().setState(TabButtonState.ENABLED);
					
					// Change selected tab index to new button
					selectedTabIndex = tab.getValueA().getIndex();
					
					// Build nested GUI
					buildInterface(e.getView());
					
					// Change new tab button state to focused
					tab.getValueA().setState(TabButtonState.FOCUSED);
				}
			});
		}
	}
	
	/**
	 * Set the slots that tab buttons will appear in
	 * @param slots Array of raw slots
	 */
	public void setTabButtonSlots(int[] slots)
	{
		for(int slot : slots)
		{
			if(MathUtils.inclusiveRange(0,54,slot))
			{
				Logg.throwIllegalArgumentError("You cannot have a slot larger than 54 or less than 0! -> " + slot);
			}
		}
		
		this.tabButtonSlots = slots;
	}
	
	/**
	 * Gets the nested builder that a tab button is referencing
	 * @param buttonClicked TabButton component clicked
	 * @return Nested ChaosBuilder, or null if the index is out of bounds
	 */
	public ChaosBuilder getTab(TabButton buttonClicked)
	{
		if(MathUtils.inclusiveRange(0,tabs.size() - 1,buttonClicked.getIndex()))
		{
			Logg.error("Tabs interface could not retrieve tab at index " + buttonClicked.getIndex() + " as its out of bounds!");
			return null;
		}
		
		return tabs.get(buttonClicked.getIndex()).getValueB();
	}
	
	/**
	 * Initialises the Tabs interface.
	 * 
	 * <p>This should be called AFTER setting next/prev button positions
	 * 
	 * @param builder The ChaosBuilder this paginator is being initialised for
	 */
	public void init(ChaosBuilder builder)
	{
		// Must set parent
		setParent(builder);
		builder.defineHeaderRegion(ChaosRegion.Common.TABS_HEADER);
		builder.defineRegion(new ChaosRegion(ChaosRegion.Common.PAGINATOR_BODY,tabSlots));
		
		// Check if the next and previous tab buttons are custom or default
		if(defaultNextTabSlot != nextTabSlot || defaultPrevTabSlot != prevTabSlot)
		{
			setTabButtonLocations();
		}
		
		visibleTabButtons = new TabButton[tabButtonSlots.length];
		
		for(int i = 0; i < tabButtonSlots.length && i < tabs.size(); i++)
		{
			visibleTabButtons[i] = tabs.get(i).getValueA();
		}
		
		nextTabButton = new Button(nextTabSlot,ItemBuilder.of(HeadUtils.RIGHT_ARROW.clone()).name("Scroll Right ->",ColourUtils.TEXT).create(),new ChaosActionListener()
		{
			@Override
			public void onClick(ChaosClickEvent e)
			{
				TabButton lastTabButton = (TabButton) e.getBuilder().getSlots().get(tabButtonSlots[tabButtonSlots.length - 1]).getSlotComponent();
				
				// If we're at the end of the array and there are no more tab buttons to scroll right to
				if((tabs.size() -1) >= lastTabButton.getIndex())
				{
					SoundUtils.playErrorSound((Player) e.getView().getPlayer());
					return;
				}
				
				// We're not at the end of the list so we shift all tab buttons down by 1
				for(int i = 0; i < visibleTabButtons.length; i++)
				{
					visibleTabButtons[i] = tabs.get(visibleTabButtons[i].getIndex() + 1).getValueA();
				}
				
				nextTabButton.playSound((Player) e.getView().getPlayer());
				buildInterface(e.getView());
			}
		});
		
		nextTabButton.setSound(Sound.ITEM_ARMOR_EQUIP_GENERIC,2.0f);
		
		prevTabButton = new Button(prevTabSlot,ItemBuilder.of(HeadUtils.LEFT_ARROW.clone()).name("<- Scroll Left",ColourUtils.TEXT).create(),new ChaosActionListener()
		{
			@Override
			public void onClick(ChaosClickEvent e)
			{
				TabButton firstTabButton = (TabButton) e.getBuilder().getSlots().get(tabButtonSlots[0]).getSlotComponent();
				
				// If we're at the start of the array and there are no more tab buttons to scroll left to
				if(firstTabButton.getIndex() == 0)
				{
					SoundUtils.playErrorSound((Player) e.getView().getPlayer());
					return;
				}
				
				// We're not at the end of the list so we shift all tab buttons down by 1
				for(int i = 0; i < visibleTabButtons.length; i++)
				{
					visibleTabButtons[i] = tabs.get(visibleTabButtons[i].getIndex() - 1).getValueA();
				}
				
				prevTabButton.playSound((Player) e.getView().getPlayer());
				buildInterface(e.getView());
			}
		});
		
		prevTabButton.setSound(Sound.ITEM_ARMOR_EQUIP_GENERIC,2.0f);
	}
	
	/**
	 * Sets tab navigation buttons according to the template used
	 */
	private void setTabButtonLocations()
	{
		switch(getParent().getTemplate())
		{
			case CHEST_1:
			{
				Logg.warn("ChaosGUI " + getParent().getName() + " cannot define locations for tab scrolling buttons!");
				break;
			}
			case CHEST_2:
			case BARREL:
			case CHEST_3:
			case CHEST_4:
			case CHEST_5:
			case CHEST_6:
			{
				this.nextTabSlot = 7;
				this.prevTabSlot = 1;
				break;
			}
			case DROPPER:
			{
				this.prevTabSlot = 6;
				this.nextTabSlot = 8;
				break;
			}
			case HOPPER:
			{
				Logg.warn("ChaosGUI " + getParent().getName() + " cannot define locations for paginator buttons!");
				break;
			}
			default:
			{
				Logg.fatal("ERROR! Invalid ChaosGUI template provided!");
			}
		}
	}
	
	@Override
	public void handleClickEvent(ChaosClickEvent e)
	{
		if(e.getRegion() == null) { return; }
		
		if(!ItemUtils.isNullOrAir(e.getView().getItem(nextTabSlot)) && e.getRawSlot() == nextTabSlot)
		{
			nextTabButton.getFrontActionListener().onClick(e);
		}
		else if(!ItemUtils.isNullOrAir(e.getView().getItem(prevTabSlot)) && e.getRawSlot() == prevTabSlot)
		{
			prevTabButton.getFrontActionListener().onClick(e);
		}
		
		if(!e.getRegion().getName().equals(ChaosRegion.Common.TABS_BODY)) { return; }
		if(ItemUtils.isNullOrAir(e.getView().getItem(e.getRawSlot()))) { return; }
		
		SoundUtils.playSound((Player) e.getWhoClicked(),Sound.BLOCK_BAMBOO_HIT,0.1f);
		
		if(e.getBuilder().getHandler() instanceof TabsHandler handler)
		{
			for(int slot : tabButtonSlots)
			{
				if(slot == e.getRawSlot())
				{
					handler.onClickTab(e,(TabButton) e.getBuilder().getSlots().get(slot).getSlotComponent());
					break;
				}
			}
		}
	}

	@Override
	public void handleDragEvent(ChaosDragEvent e)
	{
		ChaosRegion region = e.getBuilder().getRegion(ChaosRegion.Common.TABS_BODY);
		if(!region.canDeposit()) { return; }
		region.getFrontActionListener().onDrag(e);
	}
	
	/**
	 * Updates the paginator with new content
	 * @param contents List of ItemStacks
	 */
	public void setTabs(List<Pair<TabButton,? extends ChaosBuilder>> tabs)
	{
		Objects.requireNonNull(tabs,"Tabs cannot be null!");
		if(tabs.isEmpty()) { Logg.throwIllegalArgumentError("Tabs cannot be empty"); }
		
		this.tabs = tabs;
		indexTabButtonsAndAttatchRenderListeners();
	}
	
	/**
	 * Build the tab interface and the nested builder attached to it
	 */
	@Override
	public void buildInterface(InventoryView view)
	{
		for(TabButton button : visibleTabButtons)
		{
			view.setItem(button.getOccupyingSlot(),button.getAppearance());
		}
		
		this.tabs.get(this.selectedTabIndex).getValueB().buildNestedWithNoArgs((Player) view.getPlayer());
	}
	
	public void allowDeposits(boolean depo)
	{
		ChaosRegion region = getParent().getRegion(ChaosRegion.Common.PAGINATOR_BODY);
		region.setDeposit(depo);
	}
	
	public int getNextTabSlot()
	{
		return nextTabSlot; 
	}

	public void setNextTabSlot(int nextTabSlot)
	{
		Objects.requireNonNull(prevTabSlot,"Next tab slot cannot be null!");
		
		if(!MathUtils.inclusiveRange(0,54,prevTabSlot))
		{
			Logg.throwIllegalArgumentError("Next tab slot is out of bounds! Must be between 0 and 54! Got " + selectedTabIndex);
			return;
		}
		
		this.nextTabSlot = nextTabSlot;
	}

	public int getPrevTabSlot()
	{
		return prevTabSlot;
	}

	public void setPrevTabSlot(int prevTabSlot)
	{
		Objects.requireNonNull(prevTabSlot,"Previous tab slot cannot be null!");
		
		if(!MathUtils.inclusiveRange(0,54,prevTabSlot))
		{
			Logg.throwIllegalArgumentError("Previous tab slot is out of bounds! Must be between 0 and 54! Got " + selectedTabIndex);
			return;
		}
		
		this.prevTabSlot = prevTabSlot;
	}

	public int getSelectedTabIndex()
	{
		return selectedTabIndex;
	}

	public void setSelectedTabIndex(int selectedTabIndex)
	{
		Objects.requireNonNull(selectedTabIndex,"Selected Tab Index cannot be null!");
		
		if(!MathUtils.inclusiveRange(0,this.tabs.size() - 1,selectedTabIndex))
		{
			Logg.throwIllegalArgumentError("Selected tab index is out of bounds! Must be between 0 and " + (this.tabs.size() - 1) + "! Got " + selectedTabIndex);
			return;
		}
		
		// Change previous tab button state back to enabled
		tabs.get(this.selectedTabIndex).getValueA().setState(TabButtonState.ENABLED);
		
		// Change selected tab index to new button
		this.selectedTabIndex = selectedTabIndex;
		
		// Change new tab button state to focused
		tabs.get(this.selectedTabIndex).getValueA().setState(TabButtonState.FOCUSED);
	}
	
	public int[] getTabSlots()
	{
		return tabSlots;
	}
}