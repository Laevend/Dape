package coffee.dape.chaosui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import coffee.dape.Dape;
import coffee.dape.chaosui.anno.ChaosGUI;
import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.components.ChaosRegion;
import coffee.dape.chaosui.components.buttons.AnimatedButton;
import coffee.dape.chaosui.components.buttons.TextInputButton;
import coffee.dape.chaosui.events.ChaosNavigateToEvent;
import coffee.dape.chaosui.instancedargs.SessionArgs1;
import coffee.dape.chaosui.instancedargs.SessionArgs10;
import coffee.dape.chaosui.instancedargs.SessionArgs2;
import coffee.dape.chaosui.instancedargs.SessionArgs3;
import coffee.dape.chaosui.instancedargs.SessionArgs4;
import coffee.dape.chaosui.instancedargs.SessionArgs5;
import coffee.dape.chaosui.instancedargs.SessionArgs6;
import coffee.dape.chaosui.instancedargs.SessionArgs7;
import coffee.dape.chaosui.instancedargs.SessionArgs8;
import coffee.dape.chaosui.instancedargs.SessionArgs9;
import coffee.dape.chaosui.instancedargs.SessionPlayer;
import coffee.dape.chaosui.interfaces.paginator.PaginatorItem;
import coffee.dape.chaosui.interfaces.paginator.PaginatorPanelItem;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.DelayUtils;
import coffee.dape.utils.InventoryUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.ItemUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.Logg.Common.Component;
import coffee.dape.utils.MathUtils;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.data.DataUtils;
import coffee.dape.utils.tools.ClasspathCollector;

public class ChaosFactory implements Listener
{
	private static Map<String,ChaosBuilder> guis = new HashMap<>();
	private static Map<UUID,CachedGUI> cachedGuis = new HashMap<>();
	private static Map<UUID,GUISession> sessions = new HashMap<>();
	private static Set<UUID> chatInputMode = new HashSet<>();
	private static BlankChaosHandler blankHandler = new BlankChaosHandler();
	
	private static Set<Class<?>> sessionArgumentInterfaces = new HashSet<>();
	
	// TODO add slot behaviour to give player items back when close
	
	/**
	 * Initialise all Chaos GUIs
	 */
	public static void init()
	{
		ClasspathCollector cc = new ClasspathCollector();
		Set<String> guiClasspaths = cc.getClasspathsWithAnnotation(ChaosGUI.class);
		
		for(String classpath : guiClasspaths)
		{
			cc.initClassNoArgs(classpath);
		}
		
		sessionArgumentInterfaces.add(SessionPlayer.class);
		sessionArgumentInterfaces.add(SessionArgs1.class);
		sessionArgumentInterfaces.add(SessionArgs2.class);
		sessionArgumentInterfaces.add(SessionArgs3.class);
		sessionArgumentInterfaces.add(SessionArgs4.class);
		sessionArgumentInterfaces.add(SessionArgs5.class);
		sessionArgumentInterfaces.add(SessionArgs6.class);
		sessionArgumentInterfaces.add(SessionArgs7.class);
		sessionArgumentInterfaces.add(SessionArgs8.class);
		sessionArgumentInterfaces.add(SessionArgs9.class);
		sessionArgumentInterfaces.add(SessionArgs10.class);
	}
	
	public static void addSessionArgumentInterface(Class<?> sessionArgumentInterface)
	{
		sessionArgumentInterfaces.add(sessionArgumentInterface);
	}
	
	public static Set<Class<?>> getSessionArgumentInterfaces()
	{
		return sessionArgumentInterfaces;
	}
	
	/**
	 * Adds a GUI to the global gui map.
	 * 
	 * <p>GUI's call this method automatically on initialising. No need to add manually
	 * 
	 * @param builder ChaosBuilder to add
	 */
	public static void addGUI(ChaosBuilder builder)
	{
		if(guis.containsKey(builder.getName()))
		{
			ChaosBuilder existingBuilder = guis.get(builder.getName());
			Logg.Common.printFail(Component.GUI,"Building",builder.getName());
			throw new IllegalStateException("Cannot initialise ChaosGUI '" + builder.getName() + "' as a builder already exists with the same name! " + existingBuilder.getClass().getSimpleName());
		}
		
		guis.put(builder.getName(),builder);
		Logg.Common.printOk(Component.GUI,"Building",builder.getName());
	}
	
	public static void removeGUI(String guiName)
	{
		if(!guis.containsKey(guiName)) { return; }
		guis.remove(guiName);
	}
	
	public static ChaosBuilder getGUI(String guiName)
	{
		if(!guis.containsKey(guiName)) { return null; }
		return guis.get(guiName);
	}
	
	public static boolean isGUI(String guiName)
	{
		return guis.containsKey(guiName);
	}
	
	/**
	 * Opens a GUI for a player
	 * @param player Player to view the GUI
	 * @param guiName Name of the GUI
	 */
	public static void open(Player player,String guiName)
	{
		Objects.requireNonNull(player,"Player cannot be null!");
		Objects.requireNonNull(guiName,"GUI name cannot be null!");
		
		if(!guis.containsKey(guiName))
		{
			PrintUtils.actionBar(player,ColourUtils.applyColour("Error! GUI '" + guiName + "' doesn't exist!",ColourUtils.TEXT_ERROR));
			Logg.error("Player " + player.getName() + " attempted to open chaos gui '" + guiName + "' but it doesn't exist/did not initialise!");
			return;
		}
		
		// Prevents buggy GUI from having its items taken out of the handler fails
		try
		{
			ChaosNavigateToEvent cnte = new ChaosNavigateToEvent(player,isGUI(player.getOpenInventory().getTitle()) ? guis.get(player.getOpenInventory().getTitle()) : null,guis.get(guiName),ChaosFactory.getSession(player).isNavigatingUsingBackButton());
			Bukkit.getPluginManager().callEvent(cnte);
			
			if(cnte.isCancelled()) { return; }
			
			guis.get(guiName).buildWithNoArgs(player);
		}
		catch(Exception e1)
		{
			player.getOpenInventory().close();
			Logg.error("A problem occured when player " + player.getName() + " opened GUI " + guiName,e1);
			PrintUtils.error(player,"An error occured while opening this GUI! Please alert a member of staff!");
		}
	}
	
	/**
	 * Opens a GUI for a player with arguments
	 * @param player Player to open the GUI for
	 * @param guiName Name of the GUI to open
	 * @param arguments Arguments to pass to the GUI (for GUIs that have session set components)
	 */
	public static void open(Player player,String guiName,Object... arguments)
	{
		Objects.requireNonNull(player,"Player cannot be null!");
		Objects.requireNonNull(guiName,"GUI name cannot be null!");
		Objects.requireNonNull(arguments,"Arguments cannot be null!");
		
		if(!guis.containsKey(guiName))
		{
			PrintUtils.actionBar(player,ColourUtils.applyColour("Error! GUI '" + guiName + "' doesn't exist!",ColourUtils.TEXT_ERROR));
			Logg.error("Player " + player.getName() + " attempted to open chaos gui '" + guiName + "' but it doesn't exist/did not initialise!");
			return;
		}
		
		// Prevents buggy GUI from having its items taken out of the handler fails
		try
		{
			ChaosNavigateToEvent cnte = new ChaosNavigateToEvent(player,isGUI(player.getOpenInventory().getTitle()) ? guis.get(player.getOpenInventory().getTitle()) : null,guis.get(guiName),ChaosFactory.getSession(player).isNavigatingUsingBackButton());
			Bukkit.getPluginManager().callEvent(cnte);
			
			if(cnte.isCancelled()) { return; }
			
			guis.get(guiName).buildWithArgs(player,arguments);
		}
		catch(Exception e1)
		{
			player.getOpenInventory().close();
			Logg.error("A problem occured when player " + player.getName() + " opened GUI " + guiName,e1);
			PrintUtils.error(player,"An error occured while opening this GUI! Please alert a member of staff!");
		}
	}
	
	/**
	 * Create a Chaos GUI using the chest GUI type with a variable number of slots
	 * @param name The name of the GUI
	 * @param rowCount The number of inventory rows GUI will have
	 * @return Players view of inventory
	 */
	public static InventoryView createBlankGUI(String name,int rowCount,Player p)
	{
		Inventory inv = Bukkit.createInventory(null,rowCount * 9,name);
		p.openInventory(inv);
		
		return p.getOpenInventory();
	}
	
	/**
	 * Caches a GUIView for later use.
	 * 
	 * <p>This is used to cache a GUIView so the player can return to it
	 * later without having to re-apply changes or modifications they
	 * might already have done.
	 * @param p Player
	 * @param view InventoryView
	 * @param builder ChaosBuilder
	 * @param slot Slot of the component that was clicked
	 */
	public static void cacheGUI(Player p,InventoryView view,ChaosBuilder builder,int slot)
	{
		cachedGuis.put(p.getUniqueId(),new CachedGUI(view,builder,slot));
	}
	
	/**
	 * Removes a cached GUIView for a player
	 * @param p Player
	 */
	public static void clearCacheGUI(Player p)
	{
		cachedGuis.remove(p.getUniqueId());
	}
	
	/**
	 * Gets the cached GUIView
	 * @param p Player
	 * @return Cached GUIView of a GUI
	 */
	public static CachedGUI getCachedGUI(Player p)
	{
		return cachedGuis.get(p.getUniqueId());
	}
	
	/**
	 * Checks if this player has a cached GUI
	 * @param p Player
	 * @return true/false
	 */
	public static boolean hasCachedGUI(Player p)
	{
		return cachedGuis.containsKey(p.getUniqueId());
	}
	
	/**
	 * Sets the player in text input mode.
	 * The next message they type will be used for input
	 * @param p Player
	 */
	public static void setTextInputMode(Player p)
	{
		chatInputMode.add(p.getUniqueId());
	}
	
	public static boolean isPlayerInTextInputMode(Player p)
	{
		return chatInputMode.contains(p.getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onTextInput(AsyncPlayerChatEvent e)
	{
		if(!chatInputMode.contains(e.getPlayer().getUniqueId())) { return; }
		
		e.setCancelled(true);
		
		DelayUtils.executeDelayedTask(() ->
		{
			TextInputButton.onTextInput(e);
			chatInputMode.remove(e.getPlayer().getUniqueId());
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClick(InventoryClickEvent e)
	{
		// The raw slot displays as -999 when clicking outside the inventory, don't ask me why
		// Raw slot displays -1 when clicking on the edge of an inventory...
		if(!isGUI(e.getView().getTitle()) || e.getRawSlot() == -999 || e.getRawSlot() == -1 || e.getInventory().getHolder() != null) { return; }
		
		// W H A T   I S   T H I S???
		// Why is this here? Prevents weird laggy behaviour when clicking page buttons fast
		if(e.getAction().equals(InventoryAction.NOTHING)) { return; }
		
		// Prevents buggy GUI from having its items taken out of the handler fails
		try
		{
			e.setCancelled(true);
			getGUI(e.getView().getTitle()).getHandler().onClick(e);
		}
		catch(Exception e1)
		{
			e.setCancelled(true);
			e.getWhoClicked().closeInventory();
			Logg.error("A problem occured when player " + e.getWhoClicked().getName() +" interacted (Click) with GUI '" + e.getView().getTitle() + "' at slot " + e.getRawSlot(),e1);
			PrintUtils.error(e.getWhoClicked(),"An error occured while interacting with this GUI! Please alert a member of staff!");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDrag(InventoryDragEvent e)
	{
		// The raw slot displays as -999 when clicking outside the inventory, don't ask me why
		// Raw slot displays -1 when clicking on the edge of an inventory...
		if(!isGUI(e.getView().getTitle()) || e.getInventory().getHolder() != null) { return; }
		
		// Not interested in drag events in a chaos GUI where the drag is ONLY happening in the players inventory
		if(!InventoryUtils.playerDraggedAcrossMultipleInventories(e))
		{
			// Dragging only in the players inventory
			if(e.getView().getInventory(e.getRawSlots().iterator().next()).getType() == e.getView().getBottomInventory().getType())
			{
				return;
			}
		}
		else
		{
			// Dragging across multiple inventories is horrible to resolve, so cancel.
			e.setCancelled(true);
			return;
		}
		
		// Prevents buggy GUI from having its items taken out of the handler fails
		try
		{
			e.setCancelled(true);
			getGUI(e.getView().getTitle()).getHandler().onDrag(e);
		}
		catch(Exception e1)
		{
			e.setCancelled(true);
			e.getWhoClicked().closeInventory();
			Logg.error("A problem occured when player " + e.getWhoClicked().getName() +" interacted (Drag) with GUI '" + e.getView().getTitle() + "'",e1);
			PrintUtils.error(e.getWhoClicked(),"An error occured while interacting with this GUI! Please alert a member of staff!");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClose(InventoryCloseEvent e)
	{
		// The raw slot displays as -999 when clicking outside the inventory, don't ask me why
		// Raw slot displays -1 when clicking on the edge of an inventory...
		if(!isGUI(e.getView().getTitle()) || e.getInventory().getHolder() != null) { return; }
		
		// Prevents buggy GUI from having its items taken out of the handler fails
		try
		{
			getGUI(e.getView().getTitle()).getHandler().onClose(e);
		}
		catch(Exception e1)
		{
			Logg.error("A problem occured when player " + e.getPlayer().getName() +" closed GUI '" + e.getView().getTitle() + "'",e1);
			PrintUtils.error(e.getPlayer(),"An error occured while closing this GUI! Please alert a member of staff!");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onNav(ChaosNavigateToEvent e)
	{
		if(e.getNavFrom() == null) { return; }
		e.getNavFrom().getHandler().onNavigate(e);
	}
	
	/**
	 * Invalidates a player session
	 * @param p Player
	 */
	public static void invalidateSession(Player p)
	{
		if(!sessions.containsKey(p.getUniqueId())) { return; }
		sessions.remove(p.getUniqueId());
	}
	
	/**
	 * Gets a player session. If one does not exist, it is automatically created
	 * @param p Player
	 * @return
	 */
	public static GUISession getSession(Player p)
	{
		if(!sessions.containsKey(p.getUniqueId()))
		{
			sessions.put(p.getUniqueId(),new GUISession());
		}
		
		GUISession session = sessions.get(p.getUniqueId());
		session.refresh();
		return session;
	}
	
	/**
	 * Wipes the entire contents of this gui
	 * @param view The inventory view
	 */
	public static void clearGUI(InventoryView view)
	{
		for(int i = 0; i < view.countSlots(); i++)
		{
			view.setItem(i,null);
		}
	}
	
	/**
	 * Wipes a specific part of this gui
	 * @param view The inventory view
	 * @param rawSlotStart The slot to start wiping from
	 * @param rawSlotFinish The slot to stop wiping at
	 */
	public static void clearGUI(InventoryView view,int rawSlotStart,int rawSlotFinish)
	{
		for(int i = rawSlotStart; i <= rawSlotFinish; i++)
		{
			view.setItem(i,null);
		}
	}
	
	/**
	 * Wipes a specific part of this gui
	 * @param view The inventory view
	 * @param listOfRawSlots A small integer array of raw slot numbers that ItemStacks will populate
	 */
	public static void clearGUI(InventoryView view,int listOfRawSlots[])
	{
		for(int i = 0; i < listOfRawSlots.length; i++)
		{
			view.setItem(listOfRawSlots[i],null);
		}
	}
	
	/**
	 * Paints a region of the GUI a material type, only painting to empty slots
	 * @param view The inventory view
	 * @param mat The material to paint with
	 */
	public static void paintGUI(InventoryView view,ChaosRegion region,Material mat)
	{
		if(!region.isFillable()) { return; }
		
		for(int slot : region.getArea())
		{
			if(ItemUtils.isNullOrAir(view.getItem(slot)))
			{
				view.setItem(slot,ItemBuilder.of(mat).name(" ").create());
			}
		}
	}
	
	/**
	 * Paints the GUI a random material type from a list of materials provided, only painting to empty slots
	 * @param view The inventory view
	 * @param mat The material to paint with
	 */
	public static void paintRandomGUI(InventoryView view,List<Material> mats)
	{
		Material mat = mats.get(MathUtils.getRandom(0,mats.size() - 1));
		
		for(int i = 0; i < view.getTopInventory().getSize(); i++)
		{
			if(ItemUtils.isNullOrAir(view.getItem(i)))
			{
				view.setItem(i,ItemBuilder.of(mat).name(" ").create());
			}
		}
	}
	
	/**
	 * Fill the AbyssGUI with ItemStacks using a list of specific raw slots to be filled.
	 * Starting at a specific point in the stacks list
	 * @param view The inventory view
	 * @param listOfRawSlots A small integer array of raw slot numbers that ItemStacks will populate
	 * @param stacksListStart The index to start getting ItemStacks from the list at
	 * @param stacks The list of ItemStacks to populate this AbyssGUI
	 */
	public static void fillGUI(InventoryView view,int listOfRawSlots[],int stacksListStart,List<PaginatorItem> stacks)
	{
		// Grab GUI in the event some stacks are ChaosComponents
		GUISession sess = ChaosFactory.getSession((Player) view.getPlayer());
		
		// Define a slot iterator
		int rawSlotIterator = 0;
		
		// Fill the inventory from the start slot to the end slot
		for(int i = stacksListStart; i < stacks.size() && rawSlotIterator < listOfRawSlots.length; i++)
		{
			view.setItem(listOfRawSlots[rawSlotIterator],stacks.get(i).getStack());
			
			// If stack is a chaos component, it is added to temp map of ChaosComponents
			if(stacks.get(i).isItemComponentType())
			{
				ChaosComponent comp = stacks.get(i).getComponent();
				
				// Occupying slot may be null or -999 for components being used as paginator items
				comp.setOccupyingSlot(listOfRawSlots[rawSlotIterator]);
				
				if(comp.getType() == ChaosComponent.Type.ANIMATED_BUTTON)
				{
					AnimatedButton aButton = (AnimatedButton) comp;
					aButton.initClock(getGUI(view.getTitle()));
					aButton.restartClock();
				}
				
				sess.setSessionComponent(comp);
			}
			
			if(stacks.get(i) instanceof PaginatorPanelItem pItem)
			{
				ChaosComponent panelButton = pItem.getPanelButton((Player) view.getPlayer());
				int buttonSlot = listOfRawSlots[rawSlotIterator] + 9;
				
				panelButton.setOccupyingSlot(buttonSlot);
				sess.setSessionComponent(panelButton);
				
				view.setItem(buttonSlot,sess.getSessionSlots().get(buttonSlot).getSlotComponent().getAppearance());
			}
			
			rawSlotIterator++;
		}
	}
	
	public static final String DT_GUI_ITEM = "gui_item";
	
	/**
	 * Signs a GUI with random GUI uuid's
	 * @param view InventoryView
	 */
	public static void signGUI(InventoryView view)
	{
		Bukkit.getScheduler().runTaskAsynchronously(Dape.instance(),() ->
		{
			ItemStack[] contents = view.getTopInventory().getContents();
			
			for(ItemStack s : contents)
			{
				if(s == null ) { continue; }
				ChaosFactory.signItem(s);
			}
		});
	}
	
	/**
	 * Signs a stack with a unique Id to prevent it being stolen from GUI's
	 * @param stack ItemStack
	 * @return Signed ItemStack
	 */
	public static synchronized ItemStack signItem(ItemStack stack)
	{
		if(DataUtils.has(DT_GUI_ITEM,stack)) { return stack; }
		DataUtils.set(DT_GUI_ITEM,UUID.randomUUID().toString(),stack);
		return stack;
	}
	
	/**
	 * Gets the substitute blank handler
	 * @return BlankChaosHandler
	 */
	public static BlankChaosHandler getBlankHandler()
	{
		return blankHandler;
	}
	
	public enum InvTemplate
	{
		CHEST_1(9),
		CHEST_2(18),
		CHEST_3(27),
		CHEST_4(36),
		CHEST_5(45),
		CHEST_6(54),
		BARREL(27),
		DROPPER(9),
		HOPPER(5);
		
		public int slotCount;
		
		InvTemplate(int slotCount)
		{
			this.slotCount = slotCount;
		}
	}	
	
	public static class Common
	{
		/* Blueprints */
		
		public static final String BLUEPRINT_NAME = "Blueprint Name";
		
		/* Menu */
		
		public static final String SERVER_MENU = "Server Menu";
		
		/* Confirmation */
		
		public static final String CONFIRMATION = "Confirmation";
		public static final String CONFIRMATION_INPUT = "Confirmation Input";
		
		/* Axis GUIs */
		
		public static final String CLAIM_MAP = "Claim Map";
		
		public static final String AXIS_MANAGEMENT = "Axis Management";						// Main Axis manage menu
		public static final String YOUR_CLUSTERS = "Your Clusters";							// Menu to view your clusters
		public static final String CLUSTER_MANAGEMENT = "Cluster Management";				// Menu to choose between viewing the chunk claims of that cluster or managing cluster permissions
		public static final String YOUR_CHUNK_CLAIMS = "Your Chunk Claims";					// Menu to view your chunk claims
		public static final String CHUNK_CLAIM_MANAGEMENT = "Chunk Claim Management";		// Menu to manage permissions for this chunk claim
		
		public static final String PERMISSION_MANAGEMENT = "Permission Management";			// Menu to manage permissions
		public static final String CONFIGURE_FLAGS = "Configure Flags";						// Menu to manage flags of a cluster
		
		public static final String PERMISSION_HOLDERS = "Permission Holders";				// Menu to manage permission holders
		public static final String ADD_PERMISSION_HOLDER = "Add Permission Holder";			// Menu to add a permission holder
		public static final String VIEW_PERMISSION_HOLDER = "View Permission Holder";		// Menu to view a permission holder
		public static final String REMOVE_PERMISSION_HOLDER = "Remove Permission Holder";	// Menu to remove a permission holder
		public static final String AFFECTED_BLOCKS = "Affected Blocks";						// Menu to view blocks that a permission affects
		
		public static final String CONFIGURE_CLAIMING = "Configure Claiming";				// Menu to select which cluster to claim chunks for
		
		/* Player Setting GUIs */
		
		public static final String PLAYER_SETTINGS = "Player Settings";
		public static final String PLAYER_SETTINGS_CATEGORY = "Player Settings Category";
		public static final String PLAYER_SERVER_VARIABLES = "Player Server Variables";
		public static final String PLAYER_SERVER_VARIABLES_CATEGORY = "Player Server Vars Category";
		public static final String PLAYER_PROFILE = "Player Profile";
		
		
		/* Villager GUIs */
		
		public static final String ADD_TRADE = "Add Trade";
		
		/* Home Network GUI */
		
		public static final String HOME_NETWORK = "Home Network";
		
		/* Trash GUI */
		
		public static final String TRASH_DISPOSAL = "Trash Disposal";
		
		/* Voting GUI */
		
		public static final String VOTING_MENU = "Voting Menu";
		public static final String VOTING_STORE = "Voting Store";
		
		/* Bestiary GUI */
		
		public static final String BESTIARY = "Bestiary";
		
		/* Warp GUI */
		
		public static final String WORLD_WARP = "World Warp";
		
		/* Post Box */
		
		public static final String POST_BOX = "Post Box";
		
		/* Elevated Account */
		
		public static final String AUTHENTICATION_SETUP = "Authentication Setup";
	}
}
