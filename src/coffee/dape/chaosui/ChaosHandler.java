package coffee.dape.chaosui;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.behaviour.Behaviours;
import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.components.ChaosRegion;
import coffee.dape.chaosui.components.buttons.AnimatedButton;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.events.ChaosCloseEvent;
import coffee.dape.chaosui.events.ChaosNavigateToEvent;
import coffee.dape.chaosui.events.ChaosTrueCloseEvent;
import coffee.dape.chaosui.handler.ClickHandler;
import coffee.dape.chaosui.handler.CloseHandler;
import coffee.dape.chaosui.handler.TrueCloseHandler;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.chaosui.slots.ChaosSlot;
import coffee.dape.utils.Cooldown;
import coffee.dape.utils.DelayUtils;
import coffee.dape.utils.ItemUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.SoundUtils;
/**
 * 
 * @author Laeven
 *
 */
public abstract class ChaosHandler
{
	private static final UUID clickCooldown = UUID.randomUUID();
	
	public void onClick(InventoryClickEvent e)
	{
		Logg.verb("Click Type -> " + e.getClick().toString(),Logg.VerbGroup.CHAOS_UI);
		
		// Some parts of Chaos call this event directly so this is needed
		e.setCancelled(true);
		
		if(e.getClickedInventory().getType() == e.getView().getBottomInventory().getType())
		{
			handlePlayerGUIClick(e);
			return;
		}
		else
		{
			switch(e.getClick())
			{
				case WINDOW_BORDER_LEFT:
				case WINDOW_BORDER_RIGHT:
				case MIDDLE:
				case NUMBER_KEY:
				case DROP:
				case CONTROL_DROP:
				case CREATIVE:
				case SWAP_OFFHAND:
				case UNKNOWN:
				{
					e.setCancelled(true);
					return;
				}
				default:{}
			}
			
			handleChaosGUIClick(e);
		}
	}
	
	private void handleChaosGUIClick(InventoryClickEvent e)
	{
		// Cool down check
		if(!clickCooldownCheck(e)) { return; }
		
		// Get Builder
		ChaosBuilder builder = ChaosFactory.getGUI(e.getView().getTitle());
		ChaosRegion region = builder.getRegion(e.getRawSlot());
		
		// Forward event to handler class if it also wants to handle
		if(builder.getHandler() instanceof ClickHandler handler)
		{
			handler.onClick(new ChaosClickEvent(
					e.getView(),
					e.getSlotType(),
					e.getRawSlot(),
					e.getClick(),
					e.getAction(),
					builder,
					region));
		}
		
		// Allow interface to handle click event
		if(builder.hasInterface())
		{
			builder.getInterface().handleClickEvent(new ChaosClickEvent(
					e.getView(),
					e.getSlotType(),
					e.getRawSlot(),
					e.getClick(),
					e.getAction(),
					builder,
					region));
		}
		
		// Check for slot occupancy and execute action listeners (if any)
		if(builder.isSlotOccupied(e.getRawSlot()))
		{
			ChaosComponent comp = builder.getSlots().get(e.getRawSlot()).getSlotComponent();
			
			handleComponent(e,comp,builder,region);
			
			// No further processing needed
			return;
		}
		
		GUISession sess = ChaosFactory.getSession((Player) e.getWhoClicked());
		
		// Check for session slot occupancy and execute action listeners (if any)
		if(sess.isSessionSlotOccupied(e.getRawSlot()))
		{
			ChaosComponent comp = sess.getSessionSlots().get(e.getRawSlot()).getSlotComponent();
			
			handleComponent(e,comp,builder,region);
			
			// No further processing needed
			return;
		}
		
		// Check for and execute action listeners (if any)
		if(region != null && region.hasActions())
		{
			for(ChaosActionListener listener : region.getActionListeners())
			{
				listener.onClick(new ChaosClickEvent(
						e.getView(),
						e.getSlotType(),
						e.getRawSlot(),
						e.getClick(),
						e.getAction(),
						builder,
						region));
			}
		}
		
		// Check for region behaviour
		if(region != null && region.hasBehaviour())
		{
			Behaviours beh = region.getBehaviour();
			
			// Check if region has behaviour for this click type
			if(beh.hasClickBehaviour(e.getClick()))
			{
				// Is the behaviour allowance conditional?
				if(beh.getBehaviour(e.getClick()).isConditional())
				{
					if(!beh.getBehaviour(e.getClick()).validateCondition((Player) e.getWhoClicked()))
					{
						e.setCancelled(true);
						return;
					}
				}
				
				// Click allowed
				e.setCancelled(false);
				return;
			}
		}
	}
	
	/**
	 * Handles and executes the action listener of a ChaosComponent
	 * @param e InventoryClickEvent
	 * @param comp ChaosComponent
	 * @param builder ChaosBuilder
	 * @param region ChaosRegion
	 */
	private void handleComponent(InventoryClickEvent e,ChaosComponent comp,ChaosBuilder builder,ChaosRegion region)
	{
		comp.playSound((Player) e.getWhoClicked());
		
		if(comp.hasActions())
		{
			for(ChaosActionListener listener : comp.getActionListeners())
			{
				listener.onClick(new ChaosClickEvent(
						e.getView(),
						e.getSlotType(),
						e.getRawSlot(),
						e.getClick(),
						e.getAction(),
						builder,
						region));
			}
		}
	}
	
	private void handlePlayerGUIClick(InventoryClickEvent e)
	{
		// Get Builder
		ChaosBuilder builder = ChaosFactory.getGUI(e.getView().getTitle());
		
		if(e.getClick().equals(ClickType.SHIFT_LEFT) || e.getClick().equals(ClickType.SHIFT_RIGHT))
		{
			int remainingAmount = e.getCurrentItem().getAmount();
			
			// Handle shift click to a slot allowing items
			for(ChaosRegion region : builder.getRegions().values())
			{
				if(!region.hasBehaviour()) { continue; }
				
				Behaviours beh = region.getBehaviour();
				
				// Check if region has behaviour for this click type
				if(!beh.hasClickBehaviour(e.getClick())) { continue; }
				
				// Is the behaviour allowance conditional?
				if(beh.getBehaviour(e.getClick()).isConditional())
				{
					if(!beh.getBehaviour(e.getClick()).validateCondition((Player) e.getWhoClicked()))
					{
						continue;
					}
				}
				
				// Region allowing shift click behaviour, now handle shift click of materials
				remainingAmount = handleShiftClick(e,region);
				
				// All amount was shift clicked successfully
				if(remainingAmount == 0)
				{
					e.setCurrentItem(null);
					return;
				}
				// Some amount was left
				else
				{
					ItemStack stack = e.getCurrentItem();
					stack.setAmount(remainingAmount);
				}
			}
			
			return;
		}
		
		e.setCancelled(false);
	}
	
	/**
	 * Handles shift clicking of an item from the players inventory into the GUI
	 * @param e InventoryClickEvent
	 * @param region ChaosRegion where shift clicking is allowed
	 * @return amount of item left to move should the entire amount not be able to fit into the regions assigned slots
	 */
	private int handleShiftClick(InventoryClickEvent e,ChaosRegion region)
	{
		Material currentItemMatType = e.getCurrentItem().getType();
		int maxStack = currentItemMatType.getMaxStackSize();
		int amountLeftToDeposit = e.getCurrentItem().getAmount();
		
		for(int slot : region.getArea())
		{
			Logg.verb("Checking Slot " + slot,Logg.VerbGroup.CHAOS_UI);
			Logg.verb("Amount left to deposit " + amountLeftToDeposit,Logg.VerbGroup.CHAOS_UI);
			
			// Check if slot already has an item in it
			if(ItemUtils.isNullOrAir(e.getView().getItem(slot)))
			{
				// Empty
				Logg.verb("set item",Logg.VerbGroup.CHAOS_UI);
				ItemStack currentItem = ItemUtils.clone(e.getCurrentItem());
				currentItem.setAmount(amountLeftToDeposit);
				e.getView().setItem(slot,currentItem);
				return 0;
			}
			
			// Item exists
			
			Logg.verb("Item already exists in slot, amount is " + e.getView().getItem(slot).getAmount(),Logg.VerbGroup.CHAOS_UI);
			ItemStack slotItem = e.getView().getItem(slot);
			
			// Different item type so check next slot
			if(currentItemMatType != slotItem.getType()) { continue; }
			
			// Calculate amount can deposit
			int slotAmount = slotItem.getAmount();
			
			// Amount remaining that can be added to the current item before it hits max stack amount
			int amountAvailableToFill = maxStack - slotAmount;
			
			Logg.verb("Amount Avail To Fill " + amountAvailableToFill,Logg.VerbGroup.CHAOS_UI);
			Logg.verb("MaxStack " + maxStack,Logg.VerbGroup.CHAOS_UI);
			Logg.verb("SlotAmount " + slotAmount,Logg.VerbGroup.CHAOS_UI);
			
			// If stack is full
			if(amountAvailableToFill == 0) { continue; }
			
			// If there is less or equal amount of item to add
			if(amountLeftToDeposit <= amountAvailableToFill)
			{
				Logg.verb("Filling existing slot with " + amountLeftToDeposit + " total (" + (slotAmount + amountLeftToDeposit) + ")",Logg.VerbGroup.CHAOS_UI);
				slotItem.setAmount(slotAmount + amountLeftToDeposit);
				e.getView().setItem(slot,slotItem);
				amountLeftToDeposit = 0;
				Logg.verb("Amount left to deposit1 " + amountLeftToDeposit,Logg.VerbGroup.CHAOS_UI);
				return 0;
			}
			// If there is more item to deposit than the available to fill
			else
			{
				Logg.verb("Filling existing slot with " + amountAvailableToFill + " total (" + (slotAmount + amountAvailableToFill) + ")",Logg.VerbGroup.CHAOS_UI);
				slotItem.setAmount(slotAmount + amountAvailableToFill);
				e.getView().setItem(slot,slotItem);
				amountLeftToDeposit -= amountAvailableToFill;
				Logg.verb("Amount left to deposit2 " + amountLeftToDeposit,Logg.VerbGroup.CHAOS_UI);
				
				// continue to next slot to fill the rest of the remaining amount to deposit
			}
		}
		
		return amountLeftToDeposit;
	}
	
	private Set<ChaosRegion> dragRegions = new HashSet<>();
	
	public void onDrag(InventoryDragEvent e)
	{
		Logg.verb("Drag Type -> " + e.getType().toString(),Logg.VerbGroup.CHAOS_UI);
		
		// Get Builder
		ChaosBuilder builder = ChaosFactory.getGUI(e.getView().getTitle());
		
		// If it was a quick click of 1 slot (which can sometimes induce a drag event if the mouse is not perfectly still)
		if(e.getRawSlots().size() == 1)
		{
			Logg.verb("Drag Event 1 slot",Logg.VerbGroup.CHAOS_UI);
			
			InventoryClickEvent clickEvent = new InventoryClickEvent(
					e.getView(),SlotType.CONTAINER,
					e.getRawSlots().iterator().next(),
					e.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT,
					InventoryAction.PLACE_ALL);
			
			onClick(clickEvent);
			
			if(!clickEvent.isCancelled())
			{
				e.setCancelled(false);
			}
			
			return;
		}
		
		dragRegions.clear();
		
		for(int slot : e.getRawSlots())
		{
			dragRegions.add(builder.getRegion(slot));
		}
		
		for(ChaosRegion region : dragRegions)
		{
			// Check for region behaviour
			if(region != null && region.hasBehaviour())
			{
				Behaviours beh = region.getBehaviour();
				
				if(beh.hasDragBehaviour(e.getType()))
				{
					if(!beh.getBehaviour(e.getType()).validateCondition((Player) e.getWhoClicked())) { return; }
				}
				
				e.setCancelled(false);
			}
		}
	}
	
	public void onClose(InventoryCloseEvent e)
	{
		ChaosBuilder builder = ChaosFactory.getGUI(e.getView().getTitle());
		GUISession sess = ChaosFactory.getSession((Player) e.getPlayer());
		
		if(builder.getCloseSoundMixer() != null)
		{
			builder.getCloseSoundMixer().play((Location) e.getPlayer());
		}
		
		if(builder.getHandler() instanceof CloseHandler handler)
		{
			handler.onClose(new ChaosCloseEvent(e.getView(),builder));
		}
		
		builder.removeViewer((Player) e.getPlayer());
		
		if(builder.getViewers().isEmpty())
		{
			for(ChaosSlot slot : builder.getSlots().values())
			{
				if(slot.getSlotComponent().getType() == ChaosComponent.Type.ANIMATED_BUTTON)
				{
					((AnimatedButton) slot.getSlotComponent()).stop();
				}
			}
		}
		
		// If the GUI the player is looking in is no longer a ChaosGUI, they've closed the GUI.
		// This event is also fired when navigating to other GUI's as one must close for the other to open.
		DelayUtils.executeDelayedTask(() ->
		{
			Player p = (Player) e.getPlayer();
			
			if((ChaosFactory.isGUI(p.getOpenInventory().getTitle()) && e.getInventory().getHolder() == null) || ChaosFactory.isPlayerInTextInputMode(p))
			{
				// Still in chaos GUI
				return;
			}
			
			Logg.verb("Wiping GUI Session for " + e.getPlayer().getName(),Logg.VerbGroup.CHAOS_UI);
			sess.wipeData();
			
			if(builder.getHandler() instanceof TrueCloseHandler handler)
			{
				handler.onTrueClose(new ChaosTrueCloseEvent(e.getView(),builder));
			}
			
			for(ChaosRegion region : builder.getRegions().values())
			{
				if(region.willDropItemsOnTrueClose())
				{
					region.dropItems(e.getView());
				}
			}
		},5);
	}
	
	public void onNavigate(ChaosNavigateToEvent e)
	{
		GUISession sess = ChaosFactory.getSession(e.getWhoNavigated());
		
		if(e.isUsingBackButton())
		{
			sess.setNavigatingUsingBackButton(false);
			return;
		}
		
		// Ignore confirmation GUIs
		if(e.getNavTo().getName().equals(ChaosFactory.Common.CONFIRMATION)) { return; }
		if(e.getNavFrom().getName().equals(ChaosFactory.Common.CONFIRMATION)) { return; }
		if(e.getNavTo().getName().equals(ChaosFactory.Common.CONFIRMATION_INPUT)) { return; }
		if(e.getNavFrom().getName().equals(ChaosFactory.Common.CONFIRMATION_INPUT)) { return; }
		
		sess.setPreviousGUI(e.getNavTo(),e.getNavFrom());
	}
	
	/**
	 * Performs a cooldown check on the players click. This prevents a player clicking too fast.
	 * @param e InventoryInteractEvent
	 * @return True if the cooldown has expired and this click will be allowed through, false otherwise (click denied)
	 */
	private boolean clickCooldownCheck(InventoryInteractEvent e)
	{
		if(Cooldown.isCooling((Player) e.getWhoClicked(),clickCooldown))
		{
			PrintUtils.actionBar(e.getWhoClicked(),"&cYou're clicking too fast!");
			SoundUtils.playErrorSound((Player) e.getWhoClicked());
			return false;
		}
		
		Cooldown.setCooldown((Player) e.getWhoClicked(),clickCooldown,118);
		return true;
	}
}
