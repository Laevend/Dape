package coffee.dape.chaosui;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import coffee.dape.chaosui.ChaosFactory.InvTemplate;
import coffee.dape.chaosui.anno.ChaosGUI;
import coffee.dape.chaosui.anno.NestedChaosGUI;
import coffee.dape.chaosui.behaviour.Behaviours;
import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.components.ChaosMultiComponent;
import coffee.dape.chaosui.components.ChaosRegion;
import coffee.dape.chaosui.components.buttons.AnimatedButton;
import coffee.dape.chaosui.components.buttons.BackButton;
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
import coffee.dape.chaosui.interfaces.ChaosInterface;
import coffee.dape.chaosui.interfaces.common.DefaultCI;
import coffee.dape.chaosui.interfaces.paginator.Paginator;
import coffee.dape.chaosui.slots.ChaosSlot;
import coffee.dape.exception.MissingAnnotationException;
import coffee.dape.utils.ClassUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.Logg.Common.Component;
import coffee.dape.utils.MathUtils;
import coffee.dape.utils.SoundUtils;
import coffee.dape.utils.SoundUtils.SoundMixer;
import coffee.dape.utils.security.Bouncer;

public abstract class ChaosBuilder
{
	private String name;					// GUI Name
	private ChaosHandler handler = null;	// Handler to handle click and drag interactions
	private InvTemplate template;			// Vanilla inventory template used as a base for the GUI
	private ChaosInterface cInterface;		// Interface determines how the GUI will behave when interacted with
	
	// Data about regions
	private Map<String,ChaosRegion> regions = new HashMap<>();
	
	// Data about slots
	private Map<Integer,ChaosSlot> slots = new HashMap<>();
	
	// Stores data about who is viewing this GUI
	private Set<UUID> viewers = new HashSet<>();
	
	// Material used to fill the GUI
	private Material fillMaterial = null;
	
	private SoundMixer openSoundMixer = null;
	private SoundMixer closeSoundMixer = null;
	
	// Prefix used in setting and getting data from players session for a group of GUI's
	private String GUIPrefix;
	
	// If this Builder is itself nested inside another builder
	private boolean nested = false;
	private Map<String,ChaosBuilder> nestedGUIs = null;
	
	private Class<?> sessionArgumentInterface = null;
	private int sessionArgumentNumber = 0;
	
	/**
	 * Initialises the GUI.
	 * <p>
	 * If the GUI uses the @ChaosGUI annotation, it is automatically called by {@link ChaosFactory#init()} during server startup.
	 * <p>
	 * If the GUI uses the @NestedChaosGUI annotation, it is NOT called automatically.
	 * You are expected to initialise it manually when using the {@link #nest(ChaosBuilder)} method in the parent GUI.
	 */
	public ChaosBuilder()
	{
		try
		{
			Class<?> builderClass = this.getClass();
			
			// Check if the initialising GUI is nested
			if(builderClass.isAnnotationPresent(NestedChaosGUI.class)) { return; }
			
			// Check that this builder has the gui annotation
			if(!builderClass.isAnnotationPresent(ChaosGUI.class))
			{
				Logg.Common.printFail(Component.GUI,"Building","? -> " + builderClass.getSimpleName());
				throw new MissingAnnotationException("ChaosGUI of class " + builderClass.getSimpleName() + " Is missing the '@ChaosGUI' annotation!");
			}
			
			ChaosGUI guiAnno = builderClass.getAnnotation(ChaosGUI.class);
			
			this.name = guiAnno.name();
			
			if(this.name.isBlank() || this.name.isEmpty())
			{
				Logg.Common.printFail(Component.GUI,"Building","? -> " + builderClass.getSimpleName());
				throw new IllegalArgumentException("ChaosGUI of class " + builderClass.getSimpleName() + " cannot have an empty or blank name!");
			}
			
			Class<?> handlerClass = guiAnno.handler();
			
			// If the Handler class is not the default Object.class meaning there is no handler
			if(!handlerClass.equals(Object.class))
			{
				if(!ChaosHandler.class.isAssignableFrom(handlerClass))
				{
					Logg.Common.printFail(Component.GUI,"Building",this.name);
					throw new IllegalArgumentException("ChaosGUI '" + this.name + "' cannot be initialised with a handler that does not extend ChaosHandler!");
				}
				
				try
				{
					Constructor<?> cons = handlerClass.getConstructor();
					this.handler = (ChaosHandler) cons.newInstance();
				}
				catch(NoSuchMethodException e)
				{
					Logg.Common.printFail(Component.GUI,"Building",this.name);
					Logg.fatal("ChaosGUI '" + this.name + "' cannot initialise handler because default constructor does not exist!",e);
					return;
				}
				catch(SecurityException e)
				{
					Logg.Common.printFail(Component.GUI,"Building",this.name);
					Logg.fatal("ChaosGUI '" + this.name + "' cannot initialise handler because a security manager is present preventing it!",e);
					return;
				}
				catch (Exception e)
				{
					Logg.Common.printFail(Component.GUI,"Building",this.name);
					Logg.fatal("ChaosGUI '" + this.name + "' cannot initialise handler!",e);
				}
			}
			
			this.template = guiAnno.template();
		    
			SessionInterfaceCheck:
			for(Class<?> sessionArgInterfaceUsed : ClassUtils.getAllInterfacesImplemented(builderClass))
			{
				if(sessionArgInterfaceUsed.equals(SessionPlayer.class)) { sessionArgumentInterface = SessionPlayer.class; sessionArgumentNumber = SessionPlayer.NUM_OF_ARGS; break SessionInterfaceCheck; }
				if(sessionArgInterfaceUsed.equals(SessionArgs1.class)) { sessionArgumentInterface = SessionArgs1.class; sessionArgumentNumber = SessionPlayer.NUM_OF_ARGS; break SessionInterfaceCheck; }
				if(sessionArgInterfaceUsed.equals(SessionArgs2.class)) { sessionArgumentInterface = SessionArgs2.class; sessionArgumentNumber = SessionPlayer.NUM_OF_ARGS; break SessionInterfaceCheck; }
				if(sessionArgInterfaceUsed.equals(SessionArgs3.class)) { sessionArgumentInterface = SessionArgs3.class; sessionArgumentNumber = SessionPlayer.NUM_OF_ARGS; break SessionInterfaceCheck; }
				if(sessionArgInterfaceUsed.equals(SessionArgs4.class)) { sessionArgumentInterface = SessionArgs4.class; sessionArgumentNumber = SessionPlayer.NUM_OF_ARGS; break SessionInterfaceCheck; }
				if(sessionArgInterfaceUsed.equals(SessionArgs5.class)) { sessionArgumentInterface = SessionArgs5.class; sessionArgumentNumber = SessionPlayer.NUM_OF_ARGS; break SessionInterfaceCheck; }
				if(sessionArgInterfaceUsed.equals(SessionArgs6.class)) { sessionArgumentInterface = SessionArgs6.class; sessionArgumentNumber = SessionPlayer.NUM_OF_ARGS; break SessionInterfaceCheck; }
				if(sessionArgInterfaceUsed.equals(SessionArgs7.class)) { sessionArgumentInterface = SessionArgs7.class; sessionArgumentNumber = SessionPlayer.NUM_OF_ARGS; break SessionInterfaceCheck; }
				if(sessionArgInterfaceUsed.equals(SessionArgs8.class)) { sessionArgumentInterface = SessionArgs8.class; sessionArgumentNumber = SessionPlayer.NUM_OF_ARGS; break SessionInterfaceCheck; }
				if(sessionArgInterfaceUsed.equals(SessionArgs9.class)) { sessionArgumentInterface = SessionArgs9.class; sessionArgumentNumber = SessionPlayer.NUM_OF_ARGS; break SessionInterfaceCheck; }
				if(sessionArgInterfaceUsed.equals(SessionArgs10.class)) { sessionArgumentInterface = SessionArgs10.class; sessionArgumentNumber = SessionPlayer.NUM_OF_ARGS; break SessionInterfaceCheck; }
			}
			
			try
			{
				init();
				
				// Use default interface if one is not supplied
				if(!hasInterface())
				{
					setInterface(new DefaultCI());
				}
				
				checkIfRegionsOverlap(false);
			}
			catch(Exception e)
			{
				Logg.Common.printFail(Component.GUI,"Building",this.name);
				Logg.error("ChaosGUI '" + this.name + "' cannot initialise!",e);
				return;
			}
			
			ChaosFactory.addGUI(this);
		}
		catch(Exception e)
		{
			Logg.Common.printFail(Component.GUI,"Building",this.name);
			Logg.error("An uncaught exception occured building nested GUI " + this.name,e);
		}
	}
	
	/**
	 * Initialises this builder as a nested GUI. Called from {@link #nest(ChaosBuilder, String)}
	 * @param parentBuilder A parent builder with the ChaosGUI annotation
	 * @param nestedBuilderName Name of the nested builder
	 */
	private void constructAsNested(ChaosBuilder parentBuilder,String nestedBuilderName)
	{
		this.name = nestedBuilderName;
		this.template = parentBuilder.getTemplate();
		
		try
		{
			Class<?> builderClass = this.getClass();
			
			// Check that this builder has the nested gui annotation
			if(!builderClass.isAnnotationPresent(NestedChaosGUI.class))
			{
				Logg.Common.printFail(Component.GUI,"Building Nested","? -> " + builderClass.getSimpleName());
				throw new MissingAnnotationException("NestedChaosGUI of class " + builderClass.getSimpleName() + " Is missing the '@NestedChaosGUI' annotation!");
			}
			
			NestedChaosGUI guiAnno = builderClass.getAnnotation(NestedChaosGUI.class);
			Class<?> handlerClass = guiAnno.handler();
			
			// If the Handler class is not the default Object.class meaning there is no handler
			if(!handlerClass.equals(Object.class))
			{
				if(!ChaosHandler.class.isAssignableFrom(handlerClass))
				{
					Logg.Common.printFail(Component.GUI,"Building Nested",this.name);
					throw new IllegalArgumentException("NestedChaosGUI '" + this.name + "' cannot be initialised with a handler that does not extend ChaosHandler!");
				}
				
				try
				{
					Constructor<?> cons = handlerClass.getConstructor();
					this.handler = (ChaosHandler) cons.newInstance();
				}
				catch(NoSuchMethodException e)
				{
					Logg.Common.printFail(Component.GUI,"Building Nested",this.name);
					Logg.fatal("NestedChaosGUI '" + this.name + "' cannot initialise handler because default constructor does not exist!",e);
					return;
				}
				catch(SecurityException e)
				{
					Logg.Common.printFail(Component.GUI,"Building Nested",this.name);
					Logg.fatal("NestedChaosGUI '" + this.name + "' cannot initialise handler because a security manager is present preventing it!",e);
					return;
				}
				catch (Exception e)
				{
					Logg.Common.printFail(Component.GUI,"Building Nested",this.name);
					Logg.fatal("NestedChaosGUI '" + this.name + "' cannot initialise handler!",e);
				}
			}
		    
			try
			{
				init();
				
				// Use default interface if one is not supplied
				if(!hasInterface())
				{
					setInterface(new DefaultCI());
				}
				
				checkIfRegionsOverlap(false);
			}
			catch(Exception e)
			{
				Logg.Common.printFail(Component.GUI,"Building Nested",this.name);
				Logg.error("ChaosGUI '" + this.name + "' cannot initialise!",e);
				return;
			}
			
			if(parentBuilder.getNestedGUIs().containsKey(this.name))
			{
				ChaosBuilder existingNestedBuilder = parentBuilder.getNestedGUIs().get(this.name);
				Logg.Common.printFail(Component.GUI,"Building Nested",this.name);
				throw new IllegalStateException("Cannot initialise NestedChaosGUI '" + this.name + "' as a nested builder already exists with the same name in parent builder " + parentBuilder.getName() + "! " + existingNestedBuilder.getClass().getSimpleName());
			}
			
			parentBuilder.addNestedGUI(this);
			
			Logg.Common.printOk(Component.GUI,"Building Nested",this.name);
		}
		catch(Exception e)
		{
			Logg.Common.printFail(Component.GUI,"Building Nested",this.name);
			Logg.error("An uncaught exception occured building nested GUI " + this.name,e);
		}
	}
	
	/**
	 * Initialises any prerequisite components the GUI needs
	 */
	public abstract void init() throws Exception;
	
	/**
	 * Builds a GUI and displays it to the player with arguments
	 * @param p The player to open the GUI to
	 * @param arguments Arguments to pass to the GUI
	 * @return InventoryView
	 */
	public InventoryView buildWithArgs(Player p,Object... arguments)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		Objects.requireNonNull(arguments,"Arguments cannot be null!");
		
		// Stop the clocks of animated buttons
		// Not doing this causes them to appear in other GUIS if navigated to quickly
		for(ChaosSlot slot : ChaosFactory.getSession(p).getSessionSlots().values())
		{
			if(slot.getSlotComponent().getType() == ChaosComponent.Type.ANIMATED_BUTTON)
			{
				((AnimatedButton) slot.getSlotComponent()).stop();
			}
		}
		
		// Clear temporary ChaosComponents
		ChaosFactory.getSession(p).getSessionSlots().clear();
		
		if(!initSessionComponents(p,arguments)) { return null; }
		
		return build(p);
	}
	
	/**
	 * Builds a GUI and displays it to the player
	 * @param p The player to open the GUI to
	 * @return InventoryView
	 */
	public InventoryView buildWithNoArgs(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		
		// Stop the clocks of animated buttons
		// Not doing this causes them to appear in other GUIS if navigated to quickly
		for(ChaosSlot slot : ChaosFactory.getSession(p).getSessionSlots().values())
		{
			if(slot.getSlotComponent().getType() == ChaosComponent.Type.ANIMATED_BUTTON)
			{
				((AnimatedButton) slot.getSlotComponent()).stop();
			}
		}
		
		// Clear temporary ChaosComponents
		ChaosFactory.getSession(p).getSessionSlots().clear();
		
		if(!initSessionComponents(p)) { return null; }
		
		return build(p);
	}
	
	/**
	 * Builds a GUI and displays it to the player
	 * @param p The player to open the GUI to
	 * @return InventoryView
	 */
	private InventoryView build(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		
		Inventory inv = null;
		InventoryView view = null;
		
		switch(template)
		{
			case CHEST_1:
			case CHEST_2:
			case CHEST_3:
			case CHEST_4:
			case CHEST_5:
			case CHEST_6:
			{
				inv = Bukkit.createInventory(null,template.slotCount,name);
				break;
			}
			case BARREL:
			{
				inv = Bukkit.createInventory(null,InventoryType.valueOf(template.toString()),name);
				break;
			}
			case DROPPER:
			{
				inv = Bukkit.createInventory(null,InventoryType.valueOf(template.toString()),name);
				break;
			}
			case HOPPER:
			{
				inv = Bukkit.createInventory(null,InventoryType.valueOf(template.toString()),name);
				break;
			}
			default:
			{
				Logg.fatal("ERROR! Invalid ChaosGUI template provided!");
				return null;
			}
		}
		
		if(inv == null)
		{
			Logg.fatal("ERROR! Unexpected Null Inventory on creation!");
			return null;
		}
		
		p.openInventory(inv);
		view = p.getOpenInventory();
		
		buildInterface(view);
		buildComponents(view);
		
		if(this.fillMaterial != null)
		{
			// Paint regions that only allow filling
			for(ChaosRegion region : regions.values())
			{
				ChaosFactory.paintGUI(view,region,fillMaterial);
			}
		}
		
		buildSessionComponents(p,view);
		
		buildGUI(view);
		
		if(openSoundMixer != null)
		{
			openSoundMixer.play(p);
		}
		
		ChaosFactory.signGUI(view);
		
		viewers.add(p.getUniqueId());
		return view;
	}
	
	/**
	 * Builds a nested GUI and displays it to the player with arguments
	 * @param p The player to open the GUI to
	 * @param arguments Arguments to pass to the GUI
	 * @return InventoryView
	 */
	public InventoryView buildNestedWithArgs(Player p,Object... arguments)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		Objects.requireNonNull(arguments,"Arguments cannot be null!");
		
		// Stop the clocks of animated buttons
		// Not doing this causes them to appear in other GUIS if navigated to quickly
		for(ChaosSlot slot : ChaosFactory.getSession(p).getSessionSlots().values())
		{
			if(slot.getSlotComponent().getType() == ChaosComponent.Type.ANIMATED_BUTTON)
			{
				((AnimatedButton) slot.getSlotComponent()).stop();
			}
		}
		
		// Clear temporary ChaosComponents
		ChaosFactory.getSession(p).getSessionSlots().clear();
		
		if(!initSessionComponents(p,arguments)) { return null; }
		
		return buildNested(p);
	}
	
	/**
	 * Builds a nested GUI and displays it to the player
	 * @param p The player to open the GUI to
	 * @return InventoryView
	 */
	public InventoryView buildNestedWithNoArgs(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		
		// Stop the clocks of animated buttons
		// Not doing this causes them to appear in other GUIS if navigated to quickly
		for(ChaosSlot slot : ChaosFactory.getSession(p).getSessionSlots().values())
		{
			if(slot.getSlotComponent().getType() == ChaosComponent.Type.ANIMATED_BUTTON)
			{
				((AnimatedButton) slot.getSlotComponent()).stop();
			}
		}
		
		// Clear temporary ChaosComponents
		ChaosFactory.getSession(p).getSessionSlots().clear();
		
		if(!initSessionComponents(p)) { return null; }
		
		return buildNested(p);
	}
	
	/**
	 * Builds a nested GUI and displays it to the player
	 * @param p The player to open the GUI to
	 * @return InventoryView
	 */
	private InventoryView buildNested(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		
		InventoryView view = p.getOpenInventory();
		
		buildInterface(view);
		buildComponents(view);
		
		if(this.fillMaterial != null)
		{
			// Paint regions that only allow filling
			for(ChaosRegion region : regions.values())
			{
				ChaosFactory.paintGUI(view,region,fillMaterial);
			}
		}
		
		buildSessionComponents(p,view);
		
		buildGUI(view);
		
		if(openSoundMixer != null)
		{
			openSoundMixer.play(p);
		}
		
		ChaosFactory.signGUI(view);
		
		viewers.add(p.getUniqueId());
		return view;
	}
	
	/**
	 * Initialises session components
	 * @param arguments Arguments used to initialise the components with
	 * @return True if the the SCAH is not null and correct handler method was called, false otherwise
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean initSessionComponents(Player p,Object... arguments)
	{
		if(sessionArgumentInterface == null) { return true; }
		
		if(arguments == null || arguments.length == 0)
		{
			if(!this.sessionArgumentInterface.equals(SessionPlayer.class))
			{
				logInitSessionComponentsError(arguments.length);
				return false;
			}
			
			((SessionPlayer) this).initSession(p);
			return true;
		}
		
		switch(arguments.length)
		{
			case 1 ->
			{
				if(!this.sessionArgumentInterface.equals(SessionArgs1.class))
				{
					logInitSessionComponentsError(arguments.length);
					return false;
				}
				
				((SessionArgs1) this).initSession(p,arguments[0]);
			}
			case 2 ->
			{
				if(!this.sessionArgumentInterface.equals(SessionArgs2.class))
				{
					logInitSessionComponentsError(arguments.length);
					return false;
				}
				
				((SessionArgs2) this).initSession(p,arguments[0],arguments[1]);
			}
			case 3 ->
			{
				if(!this.sessionArgumentInterface.equals(SessionArgs3.class))
				{
					logInitSessionComponentsError(arguments.length);
					return false;
				}
				
				((SessionArgs3) this).initSession(p,arguments[0],arguments[1],arguments[2]);
			}
			case 4 ->
			{
				if(!this.sessionArgumentInterface.equals(SessionArgs4.class))
				{
					logInitSessionComponentsError(arguments.length);
					return false;
				}
				
				((SessionArgs4) this).initSession(p,arguments[0],arguments[1],arguments[2],arguments[3]);
			}
			case 5 ->
			{
				if(!this.sessionArgumentInterface.equals(SessionArgs5.class))
				{
					logInitSessionComponentsError(arguments.length);
					return false;
				}
				
				((SessionArgs5) this).initSession(p,arguments[0],arguments[1],arguments[2],arguments[3],arguments[4]);
			}
			case 6 ->
			{
				if(!this.sessionArgumentInterface.equals(SessionArgs6.class))
				{
					logInitSessionComponentsError(arguments.length);
					return false;
				}
				
				((SessionArgs6) this).initSession(p,arguments[0],arguments[1],arguments[2],arguments[3],arguments[4],arguments[5]);
			}
			case 7 ->
			{
				if(!this.sessionArgumentInterface.equals(SessionArgs7.class))
				{
					logInitSessionComponentsError(arguments.length);
					return false;
				}
				
				((SessionArgs7) this).initSession(p,arguments[0],arguments[1],arguments[2],arguments[3],arguments[4],arguments[5],arguments[6]);
			}
			case 8 ->
			{
				if(!this.sessionArgumentInterface.equals(SessionArgs8.class))
				{
					logInitSessionComponentsError(arguments.length);
					return false;
				}
				
				((SessionArgs8) this).initSession(p,arguments[0],arguments[1],arguments[2],arguments[3],arguments[4],arguments[5],arguments[6],arguments[7]);
			}
			case 9 ->
			{
				if(!this.sessionArgumentInterface.equals(SessionArgs9.class))
				{
					logInitSessionComponentsError(arguments.length);
					return false;
				}
				
				((SessionArgs9) this).initSession(p,arguments[0],arguments[1],arguments[2],arguments[3],arguments[4],arguments[5],arguments[6],arguments[7],arguments[8]);
			}
			case 10 ->
			{
				if(!this.sessionArgumentInterface.equals(SessionArgs10.class))
				{
					logInitSessionComponentsError(arguments.length);
					return false;
				}
				
				((SessionArgs10) this).initSession(p,arguments[0],arguments[2],arguments[2],arguments[3],arguments[4],arguments[5],arguments[6],arguments[7],arguments[8],arguments[9]);
			}
			default ->
			{
				Logg.error("ChaosGUI's cannot natively take more than 10 arguments! To use more than 10 arguments, store arguments in session data!");
				return false;
			}
		}
		
		return true;
	}
	
	private void logInitSessionComponentsError(int gotArgNumber)
	{
		Logg.error("GUI '" + this.name + "' was passed an incorrect number of arguments for its SessionArg interface '" + this.sessionArgumentInterface.toString() + "'. Got " + gotArgNumber + ". Expected " + sessionArgumentNumber);
	}
	
	/**
	 * Builds a GUI using an existing InventoryView
	 * @param view The InventoryView the player is in
	 */
	public abstract void buildGUI(InventoryView view);
	
	/**
	 * Checks if any regions defined overlap each other.
	 * 
	 * <p>Overlapping regions can cause issues when attempting to retrieve a region when a slot is clicked/dragged on
	 * due a region is fetched based on what slot was clicked and depending on how the regions are ordered one region
	 * may be prioritised over another.
	 * @param silent If true, prints regions that have overlapping slots
	 * @return True if regions are overlapping, false otherwise
	 */
	public boolean checkIfRegionsOverlap(boolean silent)
	{
		Map<Integer,Set<ChaosRegion>> dupeMap = new HashMap<>();
		
		for(ChaosRegion region : regions.values())
		{
			for(int slot : region.getArea())
			{
				if(!dupeMap.containsKey(slot))
				{
					dupeMap.put(slot,new HashSet<>());
				}
				
				dupeMap.get(slot).add(region);
			}
		}
		
		boolean isOverlapping = false;
		
		for(int slot : dupeMap.keySet())
		{
			if(dupeMap.get(slot).size() > 1)
			{
				if(silent) { return true; }
				isOverlapping = true;
				
				Logg.warn("ChaosGUI " + this.name + " has overlapping region(s) for slot " + slot);
				
				for(ChaosRegion region : dupeMap.get(slot))
				{
					Logg.warn(" -> Region: " + region.getName());
				}
			}
		}
		
		return isOverlapping;
	}
	
	/**
	 * Get region for a raw slot
	 * @param rawSlot Rawslot number
	 * @return Region slot belongs to
	 */
	public ChaosRegion getRegion(int rawSlot)
	{
		Objects.requireNonNull(rawSlot,"Raw slot cannot be null!");
		
		for(ChaosRegion region : regions.values())
		{
			if(region.getArea().contains(rawSlot)) { return region; }
		}
		
		return null;
	}
	
	public ChaosRegion getRegion(String regionName)
	{
		Bouncer.requireNotNullOrEmpty(regionName,"Region name cannot be null, empty or blank!");
		
		if(!regions.containsKey(regionName)) { return null; }
		return regions.get(regionName);
	}
	
	/**
	 * Defines a chaos region
	 * @param region
	 */
	public void defineRegion(ChaosRegion region)
	{
		Objects.requireNonNull(region,"Region cannot be null!");
		
		regions.put(region.getName(),region);
	}
	
	public void defineRegion(String regionName,int... slotsOccupying)
	{
		Bouncer.requireNotNullOrEmpty(regionName,"Region name cannot be null, empty or blank!");
		Bouncer.requireNotNullOrEmpty(slotsOccupying,"Occupying slots cannot be null or empty!");
		
		ChaosRegion region = new ChaosRegion(regionName,slotsOccupying);
		regions.put(region.getName(),region);
	}
	
	public void defineRegion(String regionName,Set<Integer> slotsOccupying)
	{
		Bouncer.requireNotNullOrEmpty(regionName,"Region name cannot be null, empty or blank!");
		Bouncer.requireNotNullOrEmpty(slotsOccupying,"Occupying slots cannot be null or empty!");
		
		ChaosRegion region = new ChaosRegion(regionName,slotsOccupying);
		regions.put(region.getName(),region);
	}
	
	/**
	 * Defines a standard header region
	 */
	public void defineHeaderRegion(String headerName)
	{
		Bouncer.requireNotNullOrEmpty(headerName,"Header name cannot be null, empty or blank!");
		
		ChaosRegion region;
		
		switch(template)
		{
			case CHEST_1:
			case CHEST_2:
			case CHEST_3:
			case CHEST_4:
			case CHEST_5:
			case CHEST_6:
			case BARREL:
			{
				region = new ChaosRegion(headerName,MathUtils.getSetOfNumbers(0,8));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case DROPPER:
			{
				region = new ChaosRegion(headerName,MathUtils.getSetOfNumbers(0,2));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case HOPPER:
			{
				Logg.warn("ChaosGUI " + this.name + " cannot define basic header region!");
				break;
			}
			default:
			{
				Logg.fatal("ERROR! Invalid ChaosGUI template provided!");
			}
		}
	}
	
	/**
	 * Defines a standard footer region
	 */
	public void defineFooterRegion(String footerName)
	{
		Bouncer.requireNotNullOrEmpty(footerName,"Header name cannot be null, empty or blank!");
		
		ChaosRegion region;
		
		switch(template)
		{
			case CHEST_1:
			{
				Logg.warn("ChaosGUI " + this.name + " cannot define basic footer region!");
				break;
			}
			case CHEST_2:
			{
				region = new ChaosRegion(footerName,MathUtils.getSetOfNumbers(9,17));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case BARREL:
			case CHEST_3:
			{
				region = new ChaosRegion(footerName,MathUtils.getSetOfNumbers(18,26));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case CHEST_4:
			{
				region = new ChaosRegion(footerName,MathUtils.getSetOfNumbers(27,35));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case CHEST_5:
			{
				region = new ChaosRegion(footerName,MathUtils.getSetOfNumbers(36,44));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case CHEST_6:
			{
				region = new ChaosRegion(footerName,MathUtils.getSetOfNumbers(45,53));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case DROPPER:
			{
				region = new ChaosRegion(footerName,MathUtils.getSetOfNumbers(6,8));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case HOPPER:
			{
				Logg.warn("ChaosGUI " + this.name + " cannot define basic footer region!");
				break;
			}
			default:
			{
				Logg.fatal("ERROR! Invalid ChaosGUI template provided!");
			}
		}
	}
	
	/**
	 * Defines a standard body region
	 */
	public void defineBodyRegion(String bodyName)
	{
		Bouncer.requireNotNullOrEmpty(bodyName,"Header name cannot be null, empty or blank!");
		
		ChaosRegion region;
		
		switch(template)
		{
			case CHEST_1:
			{
				region = new ChaosRegion(bodyName,MathUtils.getSetOfNumbers(0,8));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case CHEST_2:
			{
				region = new ChaosRegion(bodyName,MathUtils.getSetOfNumbers(0,17));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case BARREL:
			case CHEST_3:
			{
				region = new ChaosRegion(bodyName,MathUtils.getSetOfNumbers(9,17));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case CHEST_4:
			{
				region = new ChaosRegion(bodyName,MathUtils.getSetOfNumbers(9,26));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case CHEST_5:
			{
				region = new ChaosRegion(bodyName,MathUtils.getSetOfNumbers(9,35));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case CHEST_6:
			{
				region = new ChaosRegion(bodyName,MathUtils.getSetOfNumbers(9,44));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case DROPPER:
			{
				region = new ChaosRegion(bodyName,MathUtils.getSetOfNumbers(3,5));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			case HOPPER:
			{
				region = new ChaosRegion(bodyName,MathUtils.getSetOfNumbers(0,4));
				region.setFillable(true);
				regions.put(region.getName(),region);
				break;
			}
			default:
			{
				Logg.fatal("ERROR! Invalid ChaosGUI template provided!");
			}
		}
	}
	
	/**
	 * Gets the instanced argument container
	 * This is used to call init for components that require arguments specific to the instance of the GUI to initialise
	 * @return InstancedArgContainer
	 */
	public Class<?> getSessionArgumentInterface()
	{
		return sessionArgumentInterface;
	}
	
	/**
	 * Set a static GUI component that will never change regardless of who views this GUI
	 * @param com Chaos Component to set
	 */
	public void putStaticComponent(ChaosComponent com) throws IllegalStateException
	{
		Objects.requireNonNull(com,"ChaosComponent cannot be null!");
		
		if(!slots.containsKey(com.getOccupyingSlot()))
		{
			slots.put(com.getOccupyingSlot(),new ChaosSlot(com.getOccupyingSlot()));
		}
		
		// GUI needs to know of animated button to enable their clocks
		if(com.getType() == ChaosComponent.Type.ANIMATED_BUTTON)
		{
			((AnimatedButton) com).initClock(this);
		}
		
		slots.get(com.getOccupyingSlot()).setSlotComponent(com);
	}
	
	/**
	 * Set many static GUI components that will never change regardless of who views this GUI
	 * @param coms Chaos Components
	 * @throws IllegalStateException
	 */
	public void putAllStaticComponents(ChaosComponent... coms) throws IllegalStateException
	{
		Objects.requireNonNull(coms,"ChaosComponents cannot be null!");
		
		for(ChaosComponent com : coms)
		{
			putStaticComponent(com);
		}
	}
	
	/**
	 * Set a static GUI multi component that will never change regardless of who views this GUI
	 * @param mcom ChaosMultiComponent to set
	 */
	public void putStaticMultiComponent(ChaosMultiComponent mcom) throws IllegalStateException
	{
		Objects.requireNonNull(mcom,"ChaosMultiComponent cannot be null!");
		
		for(ChaosComponent com : mcom.getComponents().values())
		{
			putStaticComponent(com);
		}
	}
	
	/**
	 * Set a session only GUI component that is only viewable for the player viewing this GUI
	 * @param p Player who's session this component will be apart of
	 * @param com Chaos Component
	 * @throws IllegalStateException
	 */
	public void putSessionComponent(Player p,ChaosComponent com) throws IllegalStateException
	{
		Objects.requireNonNull(com,"ChaosComponent cannot be null!");
		
		getSession(p).setSessionComponent(com);
	}
	
	/**
	 * Set many session only GUI components that is only viewable for the player viewing this GUI
	 * @param p Player who's session this component will be apart of
	 * @param coms Chaos Components
	 * @throws IllegalStateException
	 */
	public void putAllSessionComponents(Player p,ChaosComponent... coms) throws IllegalStateException
	{
		Objects.requireNonNull(coms,"ChaosComponents cannot be null!");
		
		for(ChaosComponent com : coms)
		{
			putSessionComponent(p,com);
		}
	}
	
	/**
	 * Set a session only GUI multi component that is only viewable for the player viewing this GUI
	 * @param p Player who's session this component will be apart of
	 * @param mcom ChaosMultiComponent to set
	 * @throws IllegalStateException
	 */
	public void putSessionMultiComponent(Player p,ChaosMultiComponent mcom) throws IllegalStateException
	{
		Objects.requireNonNull(mcom,"ChaosMultiComponents cannot be null!");
		
		for(ChaosComponent com : mcom.getComponents().values())
		{
			putSessionComponent(p,com);
		}
	}
	
	/**
	 * Checks if a slot is conditional meaning special logic must be tested
	 * to check if the player clicking can perform this click action
	 * @param slot Rawslot
	 * @return
	 */
	public boolean hasSlotBehaviour(int slot)
	{
		Bouncer.requireNotNullAndInRange(slot,0,54,"Slot cannot be null and must be a valid slot between 0 and 54!");
		
		// Loop is not a big deal as there are not many regions to check in a GUI
		for(ChaosRegion region : regions.values())
		{
			if(!region.getArea().contains(slot)) { continue; }
			return region.hasBehaviour();
		}
		
		return false;
	}
	
	/**
	 * Gets behaviour of the region the slot resides in
	 * @param slot Slot to get behaviour for
	 * @return Region behaviour for this slot, null if no behaviour exists or slot does not reside in a region
	 */
	public Behaviours getSlotBehaviour(int slot)
	{
		Bouncer.requireNotNullAndInRange(slot,0,54,"Slot cannot be null and must be a valid slot between 0 and 54!");
		
		// Loop is not a big deal as there are not many regions to check in a GUI
		for(ChaosRegion region : regions.values())
		{
			if(!region.getArea().contains(slot)) { continue; }
			return region.getBehaviour();
		}
		
		return null;
	}
	
	/**
	 * Checks if a slot has a chaos component inside it
	 * @param slot rawslot to check
	 * @return
	 */
	public boolean isSlotOccupied(int slot)
	{
		Bouncer.requireNotNullAndInRange(slot,0,54,"Slot cannot be null and must be a valid slot between 0 and 54!");
		
		if(!slots.containsKey(slot)) { return false; }
		return slots.get(slot).isOccupied();
	}
	
	public Map<Integer,ChaosSlot> getSlots()
	{
		return slots;
	}
	
	/**
	 * Set behaviour for a region
	 * @param region The name of the region
	 * @param behaviour SlotBehaviour detailing how this slot should behave when interacted with
	 */
	public void setRegionBehaviour(String regionName,Behaviours behaviour) throws IllegalStateException
	{
		Objects.requireNonNull(regionName,"Region name cannot be null!");
		Objects.requireNonNull(behaviour,"Behaviour cannot be null!");
		
		if(!regions.containsKey(regionName))
		{
			Logg.error("ChaosRegion " + regionName + " does not exist! Cannot set behaviour for this region in ChaosGUI " + this.name + "!");
			return;
		}
		
		regions.get(regionName).setBehaviour(behaviour);
	}
	
	/**
	 * Clears all existing regions
	 */
	public void clearRegions()
	{
		regions.clear();
	}
	
	public Map<String,ChaosRegion> getRegions()
	{
		return regions;
	}
	
	/**
	 * Sets the interface that defines how this gui will function
	 * @param cInterface GUI Interface
	 */
	public void setInterface(ChaosInterface cInterface)
	{
		Objects.requireNonNull(cInterface,"ChaosInterface cannot be null!");
		
		this.cInterface = cInterface;
		this.cInterface.init(this);
	}
	
	/**
	 * Builds the interface for the GUI
	 * @param view
	 */
	private void buildInterface(InventoryView view)
	{
		Objects.requireNonNull(view,"InventoryView cannot be null!");
		
		if(cInterface == null) { return; }
		cInterface.buildInterface(view);
	}
	
	/**
	 * Gets the interface this GUI is using
	 * @return ChaosInterface
	 */
	public ChaosInterface getInterface()
	{
		return cInterface;
	}
	
	/**
	 * Checks if this GUI has an interface at all
	 * @return True if this GUI uses an interface, false otherwise
	 */
	public boolean hasInterface()
	{
		return (this.cInterface != null) ? true : false;
	}
	
	/**
	 * Checks if this GUI uses the default interface
	 * @return True if this GUI uses the default interface, false if there is no interface or it doesn't use the default interface
	 */
	public boolean hasDefaultInterface()
	{
		if(!hasInterface()) { return false; }
		return this.cInterface.getType() == ChaosInterface.Type.DEFAULT;
	}
	
	/**
	 * Builds components that were added to this GUI
	 * @param view
	 */
	private void buildComponents(InventoryView view)
	{
		Objects.requireNonNull(view,"InventoryView cannot be null!");
		
		for(ChaosSlot cs : this.slots.values())
		{
			if(!cs.isOccupied()) { continue; }
			view.setItem(cs.getRawSlot(),cs.getSlotComponent().getAppearance());
			
			if(cs.getSlotComponent().getType() == ChaosComponent.Type.ANIMATED_BUTTON)
			{
				((AnimatedButton) cs.getSlotComponent()).restartClock();
			}
		}
	}
	
	private void buildSessionComponents(Player p,InventoryView view)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		Objects.requireNonNull(view,"InventoryView cannot be null!");
		
		for(ChaosSlot cs : ChaosFactory.getSession(p).getSessionSlots().values())
		{
			if(!cs.isOccupied()) { continue; }
			view.setItem(cs.getRawSlot(),cs.getSlotComponent().getAppearance());
			
			if(cs.getSlotComponent().getType() == ChaosComponent.Type.ANIMATED_BUTTON)
			{
				((AnimatedButton) cs.getSlotComponent()).restartClock();
			}
		}
	}
	
	/**
	 * Note to self,
	 * can't do a gui name check in the setNavigationBack() methods as
	 * these are usually called during an init of a GUI.
	 * At that point not all GUI's have been initialised so it's random
	 * chance if the GUI being navigated to has even been initialised yet
	 */
	
	/**
	 * Adds a navigation button to the GUI automatically
	 * This button will allow the user to navigate to another GUI
	 * 
	 * @param defaultGuiToNavigateTo Name of the GUI this button will navigate the user to
	 */
	public void setNavigationBack(String defaultGuiToNavigateTo)
	{
		Objects.requireNonNull(defaultGuiToNavigateTo,"Name of GUI to navigate to cannot be null!");
		
		BackButton bb = new BackButton(defaultGuiToNavigateTo);
		putStaticComponent(bb);
	}
	
	/**
	 * Adds a navigation button to the GUI automatically
	 * This button will allow the user to navigate to another GUI
	 * @param defaultGuiToNavigateTo Name of the GUI this button will navigate the user to
	 * @param slot The slot this button will be placed in
	 */
	public void setNavigationBack(String defaultGuiToNavigateTo,int slot)
	{
		Objects.requireNonNull(defaultGuiToNavigateTo,"Name of GUI to navigate to cannot be null!");
		Bouncer.requireNotNullAndInRange(slot,0,54,"Slot cannot be null and must be a valid slot between 0 and 54!");
		
		BackButton bb = new BackButton(defaultGuiToNavigateTo,slot);
		putStaticComponent(bb);
	}
	
	/**
	 * Paints the GUI when its being built. Painting happens at the end of the
	 * GUI building pipeline
	 * @param mat Material to paint the empty spaces as
	 * @param mode PaintMode
	 */
	public void setFill(Material mat)
	{	
		Objects.requireNonNull(mat,"Material cannot be null!");
		
		this.fillMaterial = mat;
	}
	
	/**
	 * Gets this builders associated handler class used for handling interaction
	 * @return ChaosHandler
	 */
	public ChaosHandler getHandler()
	{
		if(handler == null) { return ChaosFactory.getBlankHandler(); }
		return handler;
	}
	
	/**
	 * If this ChaosBuilder has an associated handler
	 * @return True if this GUI has a handler, false otherwise
	 */
	public boolean hasHandler()
	{
		return handler != null;
	}
	
	/**
	 * Removes a player viewing this GUI
	 * @param p Player
	 */
	public void removeViewer(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		
		removeViewer(p.getUniqueId());
	}
	
	/**
	 * Removes a player viewing this GUI
	 * @param uuid A players UUID
	 */
	public void removeViewer(UUID uuid)
	{
		Objects.requireNonNull(uuid,"UUID cannot be null!");
		
		this.viewers.remove(uuid);
	}
	
	/**
	 * Returns a map of players viewing this GUI
	 * @return
	 */
	public Set<UUID> getViewers()
	{
		return viewers;
	}
	
	/**
	 * Gets the template vanilla inventory used to build this GUI
	 * @return The Inventory Template type
	 */
	public InvTemplate getTemplate()
	{
		return template;
	}
	
	/**
	 * Gets the name of this GUI
	 * @return The name of the GUI
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * Set the sound to play when the GUI opens
	 * @param sound Sound
	 * @param pitch pitch
	 */
	public void setOpenSound(Sound sound,float pitch)
	{
		Objects.requireNonNull(sound,"Sound cannot be null!");
		Objects.requireNonNull(pitch,"Pitch cannot be null!");
		
		openSoundMixer = new SoundUtils().new SoundMixer(sound,pitch);
	}
	
	public void setOpenSound(SoundMixer mixer)
	{
		openSoundMixer = mixer;
	}
	
	public SoundMixer getOpenSoundMixer()
	{
		return openSoundMixer;
	}
	
	/**
	 * Set the sound to play when the GUI closes
	 * @param sound Sound
	 * @param pitch pitch
	 */
	public void setCloseSound(Sound sound,float pitch)
	{
		Objects.requireNonNull(sound,"Sound cannot be null!");
		Objects.requireNonNull(pitch,"Pitch cannot be null!");
		
		closeSoundMixer = new SoundUtils().new SoundMixer(sound,pitch);
	}
	
	public void setCloseSound(SoundMixer mixer)
	{
		closeSoundMixer = mixer;
	}
	
	public SoundMixer getCloseSoundMixer()
	{
		return closeSoundMixer;
	}

	public String getGUIPrefix()
	{
		return GUIPrefix;
	}
	
	public void setGUIPrefix(String guiPrefix)
	{
		Objects.requireNonNull(guiPrefix,"Sound cannot be null!");
		
		GUIPrefix = guiPrefix;
	}
	
	public void refresh(Player p)
	{
		Objects.requireNonNull(p,"Sound cannot be null!");
		
		build(p);
	}
	
	public void refreshPaginator(InventoryView view,Player p)
	{
		Objects.requireNonNull(view,"View cannot be null!");
		Objects.requireNonNull(p,"Player cannot be null!");
		
		if(!hasInterface()) { return; }
		if(this.cInterface.getType() != ChaosInterface.Type.PAGINATOR) { return; }
		
		Paginator paginator = (Paginator) this.cInterface;
		ChaosFactory.clearGUI(view,paginator.getPageSlots());
		cInterface.buildInterface(view);
		ChaosFactory.signGUI(view);
	}
	
	/**
	 * Nest a GUI inside this one
	 * @param nestedBuilder A nested builder with the NestedChaosGUI annotation
	 * @param nestedBuilderName Name of the nested builder
	 */
	public void nest(ChaosBuilder nestedBuilder,String nestedBuilderName)
	{
		Objects.requireNonNull(nestedBuilder,"Nested builder cannot be null!");
		Objects.requireNonNull(nestedBuilderName,"Nested builder name cannot be null!");
		
		nestedBuilder.constructAsNested(this,nestedBuilderName);
	}
	
	public void drawNest(String nestedBuilderName,Player player)
	{
		Objects.requireNonNull(nestedBuilderName,"Nested builder name cannot be null!");
		Objects.requireNonNull(player,"Player cannot be null!");
		
		if(!getNestedGUIs().containsKey(nestedBuilderName))
		{
			Logg.error("Nested GUI '" + nestedBuilderName + "' doesn't exist!");
			return;
		}
		
		getNestedGUIs().get(nestedBuilderName).buildNested(player);
	}
	
	public boolean isNested()
	{
		return nested;
	}

	public Map<String,ChaosBuilder> getNestedGUIs()
	{
		if(nestedGUIs == null) { nestedGUIs = new HashMap<>(); }
		return nestedGUIs;
	}

	public void addNestedGUI(ChaosBuilder nestedBuilder)
	{
		Objects.requireNonNull(nestedBuilder,"Nested builder cannot be null!");
		
		if(nestedGUIs == null) { nestedGUIs = new HashMap<>(); }
		nestedGUIs.put(nestedBuilder.getName(),nestedBuilder);
	}
	
	public void removeNestedGUI(String nestedBuilderName)
	{
		Objects.requireNonNull(nestedBuilderName,"Nested builder name cannot be null!");
		
		if(nestedGUIs == null) { return; }
		nestedGUIs.remove(nestedBuilderName);
	}
	
	/**
	 * Convenient method to grab session from ChaosFactory
	 * @param p Player
	 * @return GUISession
	 */
	public static GUISession getSession(Player p)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		return ChaosFactory.getSession(p);
	}
}