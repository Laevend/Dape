package coffee.dape.chaosui.interfaces.paginator;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.GUISession;
import coffee.dape.chaosui.behaviour.additionalcompbehaviour.IconChange;
import coffee.dape.chaosui.behaviour.additionalcompbehaviour.NametagRename;
import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.components.ChaosRegion;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.events.ChaosDragEvent;
import coffee.dape.chaosui.handler.PaginatorHandler;
import coffee.dape.chaosui.interfaces.ChaosInterface;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.GradientUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.ItemUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.MathUtils;
import coffee.dape.utils.SoundUtils;
import coffee.dape.utils.data.DataUtils;

/**
 * @author Laeven
 * @since 1.0.0
 */
public class Paginator extends ChaosInterface
{
	public static final String PAGE_TAG = "page";
	
	protected int nextPageSlot = 50;		// Slot where next page button is displayed
	protected int prevPageSlot = 48;		// Slot where previous page button is displayed
	protected int pageNumberSlot = 49;		// Slot where page number is displayed
	
	protected int iconsPerPage = 36;		// Number of icons displayed per page
	protected int pageSlots[];				// Slots this page fills
	
	protected Button nextPageButton;
	protected Button prevPageButton;
	protected Button pageNumb;
	
	protected List<PaginatorItem> pageContent = new ArrayList<>();
	protected JsonObject config = new JsonObject();
	
	// Draw presets for paginator
	protected DrawMode drawMode = DrawMode.DEFAULT;
	
	/**
	 * Creates a new Paginator
	 */
	public Paginator()
	{
		super(ChaosInterface.Type.PAGINATOR);
		
		this.pageSlots = new int[this.iconsPerPage];
		for(int i = 9; i <= 44; i++)
		{
			this.pageSlots[i - 9] = i;
		}
	}
	
	/**
	 * Creates a new Paginator
	 * @param contents List of ItemStacks
	 */
	public Paginator(List<PaginatorItem> contents)
	{
		this();
		this.pageContent = contents;
	}
	
	/**
	 * Creates a new Paginator
	 * @param startSlot The first raw slot from which the paginator should fill the GUI
	 * @param endSlot The last raw slot from which the paginator should fill the GUI
	 */
	public Paginator(int startSlot, int endSlot)
	{
		super(ChaosInterface.Type.PAGINATOR);
		
		// +1 because 0 is not a number
		this.iconsPerPage = (endSlot - startSlot) + 1;
		this.pageSlots = new int[this.iconsPerPage];
		
		for(int i = startSlot; i <= endSlot; i++)
		{
			this.pageSlots[i - startSlot] = i;
		}
	}
	
	/**
	 * Creates a new Paginator
	 * @param startSlot The first raw slot from which the paginator should fill the GUI
	 * @param endSlot The last raw slot from which the paginator should fill the GUI
	 * @param contents List of ItemStacks
	 */
	public Paginator(int startSlot, int endSlot,List<PaginatorItem> contents)
	{
		this(startSlot,endSlot);
		this.pageContent = contents;
	}
	
	/**
	 * Creates a new Paginator
	 * @param slots An integer array containing all the raw slots to be filled by the paginator
	 */
	public Paginator(int slots[])
	{
		super(ChaosInterface.Type.PAGINATOR);
		pageSlots = slots;
		iconsPerPage = pageSlots.length;
	}
	
	/**
	 * Creates a new Paginator
	 * @param slots An integer array containing all the raw slots to be filled by the paginator
	 * @param contents List of ItemStacks
	 */
	public Paginator(int slots[],List<PaginatorItem> contents)
	{
		this(slots);
		this.pageContent = contents;
	}
	
	/**
	 * Creates a new Paginator with a specific draw preset
	 */
	public Paginator(DrawMode drawMode)
	{
		super(ChaosInterface.Type.PAGINATOR);
		
		this.iconsPerPage = 18;
		this.pageSlots = new int[] {9,10,11,12,13,14,15,16,17,27,28,29,30,31,32,33,34,35};
		this.drawMode = drawMode;
	}
	
	/**
	 * Initialises the Paginator.
	 * 
	 * <p>This should be called AFTER setting next/prev button positions and page icon position
	 * 
	 * @param builder The ChaosBuilder this paginator is being initialised for
	 */
	public void init(ChaosBuilder builder)
	{
		// Must set parent
		setParent(builder);
		builder.defineHeaderRegion(ChaosRegion.Common.PAGINATOR_HEADER);
		builder.defineRegion(new ChaosRegion(ChaosRegion.Common.PAGINATOR_BODY,pageSlots));
		builder.defineFooterRegion(ChaosRegion.Common.PAGINATOR_FOOTER);
		
		setPaginatorButtonLocations();
		
		nextPageButton = new Button(nextPageSlot,ItemBuilder.of(HeadUtils.RIGHT_ARROW.clone()).name("Next Page ->",ColourUtils.TEXT).create(),new ChaosActionListener()
		{
			@Override
			public void onClick(ChaosClickEvent e)
			{
				InventoryView view = e.getView();
				int pageNum = DataUtils.get(PAGE_TAG,view.getItem(pageNumberSlot)).asInt();
				
				// If GUI needs to be able to refresh the contents of the paginator
				if(e.getBuilder() instanceof DynamicPaginatorContents)
				{
					pageContent = ((DynamicPaginatorContents) e.getBuilder()).refreshPaginator((Player) e.getWhoClicked());
				}
				
				pageNum++;
				buildInterface(view,pageNum);
				GUISession sess = ChaosFactory.getSession((Player) e.getWhoClicked());
				sess.setPageNumber(e.getBuilder(),pageNum);
			}
		});
		
		nextPageButton.setSound(Sound.ITEM_ARMOR_EQUIP_GENERIC,2.0f);
		
		prevPageButton = new Button(prevPageSlot,ItemBuilder.of(HeadUtils.LEFT_ARROW.clone()).name("<- Prev Page",ColourUtils.TEXT).create(),new ChaosActionListener()
		{
			@Override
			public void onClick(ChaosClickEvent e)
			{
				InventoryView view = e.getView();
				int pageNum = DataUtils.get(PAGE_TAG,view.getItem(pageNumberSlot)).asInt();
				
				// If GUI needs to be able to refresh the contents of the paginator
				if(e.getBuilder() instanceof DynamicPaginatorContents)
				{
					pageContent = ((DynamicPaginatorContents) e.getBuilder()).refreshPaginator((Player) e.getWhoClicked());
				}
				
				pageNum--;
				buildInterface(view,pageNum);
				GUISession sess = ChaosFactory.getSession((Player) e.getWhoClicked());
				sess.setPageNumber(e.getBuilder(),pageNum);
			}
		});
		
		prevPageButton.setSound(Sound.ITEM_ARMOR_EQUIP_GENERIC,2.0f);
		
		pageNumb = new Button(pageNumberSlot,ItemBuilder.of(Material.KNOWLEDGE_BOOK).setData(PAGE_TAG,1)
				.name(ColourUtils.applyColour("Page ",ColourUtils.TEXT) + ColourUtils.applyColour("1",ColourUtils.VISTA_BLUE) + GradientUtils.applyGradient("/",GradientUtils.GOLDY) + ColourUtils.applyColour("1",ColourUtils.VISTA_BLUE)).create());
		pageNumb.setSound(Sound.ITEM_BOOK_PAGE_TURN,1.0f);
	}
	
	private void setPaginatorButtonLocations()
	{
		switch(getParent().getTemplate())
		{
			case CHEST_1:
			{
				Logg.warn("ChaosGUI " + getParent().getName() + " cannot define locations for paginator buttons!");
				break;
			}
			case CHEST_2:
			{
				this.prevPageSlot = 12;
				this.pageNumberSlot = 13;
				this.nextPageSlot = 14;
				break;
			}
			case BARREL:
			case CHEST_3:
			{
				this.prevPageSlot = 21;
				this.pageNumberSlot = 22;
				this.nextPageSlot = 23;
				break;
			}
			case CHEST_4:
			{
				this.prevPageSlot = 30;
				this.pageNumberSlot = 31;
				this.nextPageSlot = 32;
				break;
			}
			case CHEST_5:
			{
				this.prevPageSlot = 39;
				this.pageNumberSlot = 40;
				this.nextPageSlot = 41;
				break;
			}
			case CHEST_6:
			{
				this.prevPageSlot = 48;
				this.pageNumberSlot = 49;
				this.nextPageSlot = 50;
				break;
			}
			case DROPPER:
			{
				this.prevPageSlot = 6;
				this.pageNumberSlot = 7;
				this.nextPageSlot = 8;
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
		
		if(!ItemUtils.isNullOrAir(e.getView().getItem(nextPageSlot)) && e.getRawSlot() == nextPageSlot)
		{
			nextPageButton.playSound((Player) e.getWhoClicked());
			nextPageButton.getFrontActionListener().onClick(e);
		}
		else if(!ItemUtils.isNullOrAir(e.getView().getItem(prevPageSlot)) && e.getRawSlot() == prevPageSlot)
		{
			nextPageButton.playSound((Player) e.getWhoClicked());
			prevPageButton.getFrontActionListener().onClick(e);
		}
		else if(e.getRawSlot() == pageNumberSlot)
		{
			pageNumb.playSound((Player) e.getWhoClicked());
		}
		
		if(!e.getRegion().getName().equals(ChaosRegion.Common.PAGINATOR_BODY)) { return; }
		if(ItemUtils.isNullOrAir(e.getView().getItem(e.getRawSlot()))) { return; }
		
		if(e.getClick() == ClickType.RIGHT)
		{
			NametagRename.onChangeName(e);
			IconChange.onChangeIcon(e);
			return;
		}
		
		SoundUtils.playSound((Player) e.getWhoClicked(),Sound.BLOCK_BAMBOO_HIT,0.1f);
		
		if(e.getBuilder().getHandler() instanceof PaginatorHandler handler)
		{
			handler.onClickPaginatorItem(e,e.getView().getItem(e.getRawSlot()));
		}
	}

	@Override
	public void handleDragEvent(ChaosDragEvent e)
	{
		ChaosRegion region = e.getBuilder().getRegion(ChaosRegion.Common.PAGINATOR_BODY);
		if(!region.canDeposit()) { return; }
		region.getFrontActionListener().onDrag(e);
	}
	
	/**
	 * Updates the paginator with new content
	 * @param contents List of ItemStacks
	 */
	public void setContent(List<PaginatorItem> contents)
	{
		this.pageContent = contents;
	}
	
	@Override
	public void buildInterface(InventoryView view)
	{
		GUISession sess = ChaosFactory.getSession((Player) view.getPlayer());
		
		int pageNumber = 1;
		
		// Open GUI on page that player was on when they left the paginator
		if(sess.hasPageNumber(getParent()))
		{
			pageNumber = sess.getPageNumber(getParent());
		}
		
		buildInterface(view,pageNumber);
	}
	
	/**
	 * Builds the paginator to the inventory view
	 * @param view
	 * @param pageNum the page number to build at
	 */
	public void buildInterface(InventoryView view,int pageNum)
	{
		ChaosBuilder builder = ChaosFactory.getGUI(view.getTitle());
		
		// Refresh page content if dynamic
		if(builder instanceof DynamicPaginatorContents)
		{
			pageContent = ((DynamicPaginatorContents) builder).refreshPaginator((Player) view.getPlayer());
		}
		
		drawPage(view,pageNum,pageContent);
	}
	
	/**
	 * Draws contents of Paginator
	 * @param inv The inventory view
	 * @param pageNum The page number to view
	 * @param iconsPerPage The number of icons (ItemStacks) to display per page
	 * @param stacks The list of ItemStacks to populate the GUI with
	 */
	private void drawPage(InventoryView inv,int pageNum,List<PaginatorItem> stacks)
	{
		drawDefaultPage(inv,pageNum,stacks);
		
//		switch(drawMode)
//		{
//			case BUTTON_PANEL:
//			{
//				drawToggleButtonPage(inv,pageNum,stacks);
//				return;
//			}
//			default:
//			{
//				drawDefaultPage(inv,pageNum,stacks);
//			}
//		}
	}
	
	/**
	 * Draws contents of Paginator using default draw mode
	 * @param inv The inventory view
	 * @param pageNum The page number to view
	 * @param iconsPerPage The number of icons (ItemStacks) to display per page
	 * @param stacks The list of ItemStacks to populate the GUI with
	 */
	private void drawDefaultPage(InventoryView view,int pageNum,List<PaginatorItem> stacks)
	{
		int numOfPages = (stacks.size() / iconsPerPage);	// Number of total pages
		
		// Check if there is no remaining icons, if there are add a page to compensate for it
		if(stacks.size() % iconsPerPage != 0) { numOfPages++; }
		
		// Prevent an edge case were paginator will show page 0/1 instead of 1/1 because there are no items to show!
		if(stacks.size() == 0) { numOfPages++; }
		
		// Clamp the page number to within the available page numbers to prevent showing a page that no longer exists
		pageNum = MathUtils.clamp(1,numOfPages,pageNum);
		
		// Update page number
		pageNumb.setAppearance(ItemBuilder.of(Material.KNOWLEDGE_BOOK).setData(PAGE_TAG,pageNum)
				.name(ColourUtils.applyColour("Page ",ColourUtils.TEXT) + ColourUtils.applyColour(""+pageNum,ColourUtils.VISTA_BLUE) + GradientUtils.applyGradient("/",GradientUtils.GOLDY) + ColourUtils.applyColour(""+numOfPages,ColourUtils.VISTA_BLUE)).create());
		
		// Clear all parts of the gui apart from the footer 
		ChaosFactory.clearGUI(view,pageSlots);
		
		// Clear pre-existing buttons
		if(drawMode == DrawMode.BUTTON_PANEL)
		{
			ChaosFactory.clearGUI(view,18,26);
			ChaosFactory.clearGUI(view,36,44);
		}
		
		// Clear slots where navigation arrows are
		view.setItem(prevPageSlot,null);
		view.setItem(nextPageSlot,null);
		
		// Fill the GUI
		ChaosFactory.fillGUI(view,pageSlots,(pageNum * iconsPerPage) - iconsPerPage,stacks);
		
		// Navigation buttons
		if(pageNum > 1) { view.setItem(prevPageSlot,prevPageButton.getAppearance()); }
		if(pageNum < numOfPages) { view.setItem(nextPageSlot,nextPageButton.getAppearance()); }
		
		// Page number
		view.setItem(pageNumberSlot,pageNumb.getAppearance());
	}
	
	/**
	 * Draws contents of Paginator using toggle buttons for each stack
	 * @param inv The inventory view
	 * @param pageNum The page number to view
	 * @param iconsPerPage The number of icons (ItemStacks) to display per page
	 * @param stacks The list of ItemStacks to populate the GUI with
	 */
//	private void drawToggleButtonPage(InventoryView view,int pageNum,List<PaginatorItem> stacks)
//	{
//		drawDefaultPage(view,pageNum,stacks);
//		
//		GUISession sess = ChaosFactory.getSession((Player) view.getPlayer());
//		
//		int toggleSlot = 0;
//		
//		for(int i = 9; i <= 17; i++)
//		{
//			if(ItemUtils.isNullOrAir(view.getItem(i))) { continue; }
//			toggleSlot = i + 9;
//			sess.setTempComponent(getToggleButton(toggleSlot));
//			view.setItem(toggleSlot,sess.getTempSlots().get(toggleSlot).getSlotComponent().getAppearance());
//		}
//		
//		for(int i = 27; i <= 35; i++)
//		{
//			if(ItemUtils.isNullOrAir(view.getItem(i))) { continue; }
//			toggleSlot = i + 9;
//			sess.setTempComponent(getToggleButton(toggleSlot));
//			view.setItem(toggleSlot,sess.getTempSlots().get(toggleSlot).getSlotComponent().getAppearance());
//		}
//	}
//	
//	private ToggleButton getToggleButton(int slot)
//	{
//		ToggleButton togButt = new ToggleButton(slot);
//		togButt.getActionListeners().clear();
//		togButt.addActionListener(new ChaosActionListener()
//		{
//			@Override
//			public void onClick(ChaosClickEvent e)
//			{
//				if(!DataUtils.has(ToggleButton.DTAG,e.getView().getItem(e.getRawSlot())))
//				{
//					Logg.error("ChaosGUI " + e.getBuilder().getName() + " has a toggle button that has missing data tag for " + ToggleButton.DTAG + "!");
//					return;
//				}
//				
//				// Check current item displayed and swap to the other
//				if(DataUtils.get(ToggleButton.DTAG,e.getView().getItem(e.getRawSlot())).asString().equals(ToggleStatus.DISABLED.toString()))
//				{
//					e.getView().setItem(e.getRawSlot(),togButt.getEnabledStack());
//					SoundUtils.playSound((Player) e.getWhoClicked(),Sound.ENTITY_PUFFER_FISH_BLOW_UP,2.0f);
//					
//					if(e.getBuilder().getHandler() instanceof PaginatorToggleButtonHandler handler)
//					{
//						handler.onPaginatorToggleButtonClick(new ChaosPaginatorToggleButtonClickEvent(e.getView(),e.getRawSlot(),e.getBuilder(),(Player) e.getWhoClicked(),ToggleButton.ToggleStatus.DISABLED,ToggleButton.ToggleStatus.ENABLED,e.getView().getItem(e.getRawSlot() - 9)));
//					}
//				}
//				else
//				{
//					e.getView().setItem(e.getRawSlot(),togButt.getDisabledStack());
//					SoundUtils.playSound((Player) e.getWhoClicked(),Sound.ENTITY_PUFFER_FISH_BLOW_OUT,0.1f);
//					
//					if(e.getBuilder().getHandler() instanceof PaginatorToggleButtonHandler handler)
//					{
//						handler.onPaginatorToggleButtonClick(new ChaosPaginatorToggleButtonClickEvent(e.getView(),e.getRawSlot(),e.getBuilder(),(Player) e.getWhoClicked(),ToggleButton.ToggleStatus.ENABLED,ToggleButton.ToggleStatus.DISABLED,e.getView().getItem(e.getRawSlot() - 9)));
//					}
//				}
//			}
//		});
//		
//		return togButt;
//	}
	
	public void allowDeposits(boolean depo)
	{
		ChaosRegion region = getParent().getRegion(ChaosRegion.Common.PAGINATOR_BODY);
		region.setDeposit(depo);
	}
	
	public int getNextPageSlot()
	{
		return nextPageSlot; 
	}

	public void setNextPageSlot(int nextPageSlot)
	{
		this.nextPageSlot = nextPageSlot;
	}

	public int getPrevPageSlot()
	{
		return prevPageSlot;
	}

	public void setPrevPageSlot(int prevPageSlot)
	{
		this.prevPageSlot = prevPageSlot;
	}

	public int getPageNumberSlot()
	{
		return pageNumberSlot;
	}

	public void setPageNumberSlot(int pageNumberSlot)
	{
		this.pageNumberSlot = pageNumberSlot;
	}
	
	public int[] getPageSlots()
	{
		return pageSlots;
	}
	
	public static List<PaginatorItem> wrapItemstacks(List<ItemStack> stacks)
	{
		List<PaginatorItem> newStacks = new ArrayList<>();
		stacks.forEach(stack -> newStacks.add(new ChaosStackWrapper(stack)));
		return newStacks;
	}
	
	public static PaginatorItem wrapItemstack(ItemStack stack)
	{
		return new ChaosStackWrapper(stack);
	}
	
	public enum DrawMode
	{
		// The default way of drawing a paginator
		DEFAULT,
		
		// Button panel draws each page with each item having a button below it that influences it (For example a toggle button)
		BUTTON_PANEL
	}
	
	private static class ChaosStackWrapper implements PaginatorItem
	{
		private ItemStack stack;

		public ChaosStackWrapper(ItemStack stack)
		{
			this.stack = stack;
		}
		
		@Override
		public ItemStack getStack()
		{
			return stack;
		}
		
		@Override
		public boolean isItemComponentType()
		{
			return false;
		}

		@Override
		public ChaosComponent getComponent()
		{
			return null;
		}
	}
}