package coffee.dape.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import coffee.dape.Dape;

/**
 * @author Laeven
 * @since 1.0.0
 */
public class InventoryUtils
{
	//private int[] invSlots = new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,40};
	//private int[] armourSlots = new int[] {36,37,38,39};
	
	// TODO write better util functions for inventory that provide the ability to search and find items maybe using tree?
	// TODO can't use base64 must be a better way. Base64 takes too long in linear space for each item
	// TODO ItemStack stack.isSimelar() method for comparing item stacks without amount!
	
	/**
	 * Sets the item in the players inventory if it finds a space, otherwise it will drop it at their feet
	 * @param player The player
	 * @param item The item
	 */
	public static void safeInventorySetDropExtra(Player player,ItemStack item)
	{		
		// If there are not free inventory slots
		if(player.getInventory().firstEmpty() == -1)
		{			
			// Attempt to add items to the inventory, if it fails then return the items it could not place
			Map<Integer,ItemStack> couldNotStore = player.getInventory().addItem(item);
			
			// Check the map of items it could not place is empty
			if(couldNotStore.isEmpty()) { return; }
			
			// drop it
			for(ItemStack i : couldNotStore.values())
			{
				player.getWorld().dropItemNaturally(player.getLocation(),i);
			}
		}
		else
		{
			player.getInventory().addItem(item);
		}
	}
	
	/**
	 * Checks if the inventory has space for this item in its whole amount
	 * @param player The player
	 * @param item The item
	 */
	public static boolean inventoryHasSpace(Player player,ItemStack item)
	{		
		// If there are not free inventory slots
		if(player.getInventory().firstEmpty() == -1)
		{
			// Attempt to add items to the inventory, if it fails then return the items it could not place
			Map<Integer,ItemStack> couldNotStore = player.getInventory().addItem(item);
			
			// Check the map of items it could not place is empty
			return couldNotStore.isEmpty() ? true : false;
		}
		else
		{
			return true;
		}
	}
	
	/**
	 * Sets the item in the players inventory if it finds a space, otherwise it will return the amount it could not store
	 * @param player The player
	 * @param item The item
	 */
	public static int safeInventorySetReturnExtra(Player player,ItemStack item)
	{		
		// If there are not free inventory slots
		if(player.getInventory().firstEmpty() == -1)
		{			
			// Attempt to add items to the inventory, if it fails then return the items it could not place
			Map<Integer,ItemStack> couldNotStore = player.getInventory().addItem(item);
			
			// Check the map of items it could not place is empty
			if(couldNotStore.isEmpty()) { return 0; }
			
			// Return the amount of this item it could not store
			for(ItemStack stack : couldNotStore.values())
			{
				return stack.getAmount();
			}
		}
		else
		{
			player.getInventory().addItem(item);
		}
		
		return 0;
	}
	
	/**
	 * Gets an array of slot numbers where a type of material can be found in a players inventory
	 * @param player Player to search
	 * @param mat Material to find
	 * @return int array of slots, null otherwise
	 */
	public static int[] getSlotsWhereMaterialIsPresent(Player player,Material mat)
	{
		ItemStack[] contents = player.getInventory().getContents();
		List<Integer> slots = new ArrayList<>();
		
		for(int i = 0; i < contents.length; i++)
		{
			if(contents[i] == null) { continue; }
			
			if(contents[i].getType() == mat)
			{
				slots.add(i);
			}
		}
		
		if(slots.isEmpty()) { return null; }
		
		return slots.stream().mapToInt(Integer::intValue).toArray();
	}
	
	/**
	 * Gets the total amount of a type of material in a players inventory
	 * @param player Player to search
	 * @param mat Material to find
	 * @return total amount of this material found
	 */
	public static int getTotalAmountOfMaterial(Player player,Material mat)
	{
		ItemStack[] contents = player.getInventory().getContents();
		int amount = 0;
		
		for(int i = 0; i < contents.length; i++)
		{
			if(contents[i] == null) { continue; }
			
			if(contents[i].getType() == mat)
			{
				amount += contents[i].getAmount();
			}
		}
		
		return amount;
	}
	
	/**
	 * Removes an amount of material from the players inventory
	 * 
	 * <p>This function will search a players inventory and deduct materials 
	 * from multiple stacks if necessary until the amount argument provided is 0
	 * @param player Player to search
	 * @param mat Material to find
	 * @param amount Total amount to remove
	 * @param removeOnlyIfPlayerHasItems Remove materials only if they exist, if there are still items to remove and the player does not have them, this transaction will be cancelled
	 * @return Amount of material that could not be removed as the player did not have this material in sufficient amounts
	 */
	public static int removeMaterialFromInventory(Player player,Material mat,int amount,boolean removeOnlyIfPlayerHasItems)
	{
		ItemStack[] clonedContents = new ItemStack[player.getInventory().getContents().length];
		System.arraycopy(player.getInventory().getContents(),0,clonedContents,0,player.getInventory().getContents().length);
		
		int amountRemaining = amount;
		
		Search:
		for(int i = 0; i < clonedContents.length; i++)
		{
			if(clonedContents[i] == null) { continue; }
			
			if(clonedContents[i].getType() == mat)
			{
				// Amount remaining is less than the stack amount so only take part of it
				if(amountRemaining < clonedContents[i].getAmount())
				{
					clonedContents[i].setAmount(clonedContents[i].getAmount() - amountRemaining);
					amountRemaining = 0;
					break Search;
				}
				
				amountRemaining -= clonedContents[i].getAmount();
				clonedContents[i] = null;
			}
		}
		
		if(amountRemaining > 0 && removeOnlyIfPlayerHasItems) { return amountRemaining; }
		
		player.getInventory().setContents(clonedContents);
		return amountRemaining;
	}
	
	// TODO Re-add virtual items
	
	/**
	 * Randomly distributes the contents of a list into an inventory contents ItemStack array
	 * @param inventoryRows rows this inventory has 1-6
	 * @param items List of items to populate inventory with
	 * @return ItemStack contents
	 */
	public static ItemStack[] randomisePlacementOfContents(int inventoryRows,List<ItemStack> items)
	{
		return null;
	}
	
	/**
	 * Drops a players entire inventory where they are standing
	 * @param player Player to drop inventory
	 */
	public static void dropInventory(Player player)
	{
		for(ItemStack item : player.getInventory().getContents())
		{
			if(item == null) { continue; }
			player.getWorld().dropItemNaturally(player.getLocation(),item);
		}
		
		for(ItemStack item : player.getInventory().getArmorContents())
		{
			if(item == null) { continue; }
			player.getWorld().dropItemNaturally(player.getLocation(),item);
		}
		
		//player.getInventory().getExtraContents()
		
		player.getInventory().clear();
	}
	
	public static void dropInventory(Location dropLocation,Inventory inv)
	{
		for(ItemStack item : inv.getContents())
		{
			if(item == null) { continue; }
			dropLocation.getWorld().dropItemNaturally(dropLocation,item);
		}
		
		inv.clear();
	}
	
	public static void dropInventoryCombatLogging(Player player)
	{
		for(ItemStack item : player.getInventory().getContents())
		{
			if(item == null) { continue; }
			Item entityItem = player.getWorld().dropItemNaturally(player.getLocation(),item);
			entityItem.setMetadata("NO_PICKUP",new FixedMetadataValue(Dape.instance(),player.getUniqueId().toString()));
		}
		
		for(ItemStack item : player.getInventory().getArmorContents())
		{
			if(item == null) { continue; }
			Item entityItem = player.getWorld().dropItemNaturally(player.getLocation(),item);
			entityItem.setMetadata("NO_PICKUP",new FixedMetadataValue(Dape.instance(),player.getUniqueId().toString()));
		}
		
		player.getInventory().clear();
	}
	
	/**
	 * Removes 1 of an item during an interaction event when the player
	 * is holding more than 1 of an item.
	 * @param stack ItemStack
	 * @return 
	 */
	public static ItemStack removeOneItem(ItemStack stack)
	{
		int itemAmount = stack.getAmount();
		
		if(itemAmount > 1)
		{
			stack.setAmount(itemAmount - 1);
			return stack;
		}
		else
		{
			return new ItemStack(Material.AIR);
		}
	}
	
	/**
	 * Checks if a player dragged items across multiple inventories
	 * @param e InventoryDragEvent
	 * @return True if player dragged items across multiple inventories, false otherwise
	 */
	public static boolean playerDraggedAcrossMultipleInventories(InventoryDragEvent e)
	{
		InventoryType topInvType = e.getView().getTopInventory().getType();
		InventoryType bottomInvType = e.getView().getBottomInventory().getType();
		
		boolean dragInGUI = false;
		boolean dragInPlayersInv = false;
		
		for(int slot : e.getRawSlots())
		{			
			// Dragging in the GUI
			if(e.getView().getInventory(slot).getType() == topInvType)
			{
				dragInGUI = true;
			}
			
			// Dragging in the players inventory
			if(e.getView().getInventory(slot).getType() == bottomInvType)
			{
				dragInPlayersInv = true;
			}
			
			// Dragging in the GUI and players inventory
			if(dragInGUI && dragInPlayersInv)
			{ 
				return true;
			}
		}
		
		return false;
	}
}
