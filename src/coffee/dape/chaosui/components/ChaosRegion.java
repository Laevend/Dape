package coffee.dape.chaosui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.behaviour.Behaviours;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.utils.ItemUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.MathUtils;

/**
 * 
 * @author Laeven
 * 
 * Handles the creation of regions inside the GUI.
 * Regions are areas of the GUI that can be used to test for when a player
 * attempts to click inside a region and/or having an action happen when a
 * player attempts to click in a region.
 */
public class ChaosRegion
{
	private TreeSet<Integer> area = new TreeSet<>();
	private String name;
	private Behaviours behaviour = null;
	private boolean deposit = false;	// If players can deposit items anywhere in this region
	private boolean fillable = false;	// If Chaos can fill this region with a material (if given)
	private boolean dropItems = false;	// If this region should drop all of its items when the GUI closes
	
	private List<ChaosActionListener> actionListeners = null;
	
	/**
	 * Creates a region within the GUI that occupies multiple slots
	 * @param rawSlotsArea Slots this region occupies
	 * @param regionName Name of this region
	 */
	public ChaosRegion(String regionName,int... rawSlotsArea)
	{
		Objects.requireNonNull(regionName,"Region name cannot be null!");
		Objects.requireNonNull(rawSlotsArea,"Raw slots area cannot be null!");
		
		this.name = regionName;
		
		for(int slot : rawSlotsArea)
		{
			this.area.add(slot);
		}
	}
	
	/**
	 * Creates a region within the GUI that occupies multiple slots
	 * @param area Set of slots this region occupies
	 * @param regionName Name of this region
	 */
	public ChaosRegion(String regionName,Set<Integer> area)
	{
		Objects.requireNonNull(regionName,"Region name cannot be null!");
		Objects.requireNonNull(area,"Area cannot be null!");
		
		if(area.isEmpty())
		{
			Logg.throwIllegalArgumentError("Area must contain at least 1 slot");
			return;
		}
		
		this.name = regionName;
		this.area.addAll(area);
	}
	
	/**
	 * Creates a region within the GUI that occupies multiple slots
	 * @param startSlot Slot this region starts occupying from
	 * @param endSlot Slot this region stops occupying at
	 * @param regionName Name of this region
	 */
	public ChaosRegion(String regionName,int startSlot,int endSlot)
	{
		Objects.requireNonNull(regionName,"Region name cannot be null!");
		Objects.requireNonNull(startSlot,"Start slot cannot be null!");
		Objects.requireNonNull(endSlot,"End slot cannot be null!");
		
		if(MathUtils.inclusiveRange(0,54,startSlot))
		{
			Logg.throwIllegalArgumentError("Start slot for a region cannot be less than 0 or more than 54!");
			return;
		}
		
		if(MathUtils.inclusiveRange(0,54,endSlot))
		{
			Logg.throwIllegalArgumentError("End slot for a region cannot be less than 0 or more than 54!");
			return;
		}
		
		if(startSlot >= endSlot)
		{
			Logg.throwIllegalArgumentError("Start slot for a region cannot be greater than or equal to the end slot!");
			return;
		}
		
		this.name = regionName;
		this.area.addAll(MathUtils.getSetOfNumbers(startSlot,endSlot));
	}
	
	public TreeSet<Integer> getArea()
	{
		return area;
	}

	public String getName()
	{
		return name;
	}
	
	public boolean isSlotInRegion(int slot)
	{
		return area.contains(slot);
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
	
	public boolean hasBehaviour()
	{
		return behaviour != null;
	}
	
	public void setBehaviour(Behaviours behaviour)
	{
		this.behaviour = behaviour;
	}
	
	public Behaviours getBehaviour()
	{
		if(behaviour == null) { behaviour = new Behaviours(); }
		return behaviour;
	}
	
	public boolean canDeposit()
	{
		return deposit;
	}

	public void setDeposit(boolean deposit)
	{
		this.deposit = deposit;
	}

	public boolean isFillable()
	{
		return fillable;
	}

	public void setFillable(boolean fillable)
	{
		this.fillable = fillable;
	}

	public boolean willDropItemsOnTrueClose()
	{
		return dropItems;
	}

	public void setDropItemsOnTrueClose(boolean dropItems)
	{
		this.dropItems = dropItems;
	}
	
	public void dropItems(InventoryView view)
	{
		for(int slot : area)
		{
			if(ItemUtils.isNullOrAir(view.getItem(slot))) { continue; }
			
			// Cloned and original deleted to prevent packet attacks
			ItemStack cloned = view.getItem(slot).clone();
			view.setItem(slot,null);
			view.getPlayer().getWorld().dropItem(view.getPlayer().getLocation(),cloned);
		}
	}
	
	public void clear(InventoryView view)
	{
		for(int slot : area)
		{
			view.setItem(slot,null);
		}
	}
	
	public ItemStack[] getContents(InventoryView view)
	{
		ItemStack[] contents = new ItemStack[area.size()];
		int counter = -1;
		
		for(int slot : area)
		{
			counter++;
			if(ItemUtils.isNullOrAir(view.getItem(slot))) { continue; }
			contents[counter] = view.getItem(slot).clone();
		}
		
		return contents;
	}
	
	public void setContents(InventoryView view,ItemStack[] contents)
	{
		int counter = -1;
		
		for(int slot : area)
		{
			counter++;
			if(counter >= contents.length) { return; }
			if(contents[counter] == null) { continue; }
			view.setItem(slot,contents[counter]);
		}
	}

	public class Common
	{
		public static final String HEADER = "header";
		public static final String BODY = "body";
		public static final String FOOTER = "footer";
		public static final String PAGINATOR_HEADER = "paginator_header";
		public static final String PAGINATOR_FOOTER = "paginator_footer";
		public static final String PAGINATOR_BODY = "paginator_body";
		
		public static final String TABS_HEADER = "paginator_header";
		public static final String TABS_BODY = "paginator_body";
	}
}