package coffee.dape.chaosui.interfaces;

import org.bukkit.inventory.InventoryView;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.events.ChaosDragEvent;

/**
 * 
 * @author Laeven
 *
 * Not be confused with Java interfaces, ChaosInterfaces
 * are used to setup predetermined GUI regions and slot behaviours.
 * They can also be used to add additional functionality ontop
 * of the GUI.
 *
 */
public abstract class ChaosInterface
{
	private ChaosBuilder parent = null;
	private Type type;
	
	public ChaosInterface(Type type)
	{
		this.type = type;
	}
	
	public Type getType()
	{
		return type;
	}
	
	public void setParent(ChaosBuilder builder)
	{
		this.parent = builder;
	}
	
	public ChaosBuilder getParent()
	{
		return parent;
	}

	public enum Type
	{
		DEFAULT,				// GUI with default Header, Body, and Footer regions
		PAGINATOR,				// GUI with Header, Body, and Paginator Body regions
		TABS,					// GUI with Header, Body, and Tab Body regions
		CONFIRMATION_WINDOW,	// GUI that shows a confirmation window
		BLANK,					// GUI with a blank interface as it will be manually handled (custom)
	}
	
	/**
	 * Initialises the interface and sets up regions and slots
	 * @param builder
	 */
	public abstract void init(ChaosBuilder builder);
	
	/**
	 * Builds the interface into the GUI
	 * @param view
	 */
	public abstract void buildInterface(InventoryView view);
	
	/**
	 * Passes the InventoryClickEvent to allow the interface to decide how to handle it
	 * @param cce ChaosClickEvent
	 */
	public abstract void handleClickEvent(ChaosClickEvent e);
	
	/**
	 * Passes the InventoryDragEvent to allow the interface to decide how to handle it
	 * @param cde ChaosDragEvent
	 */
	public abstract void handleDragEvent(ChaosDragEvent e);
}
