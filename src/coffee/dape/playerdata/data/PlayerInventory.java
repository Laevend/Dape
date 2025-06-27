package coffee.dape.playerdata.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.playerdata.PlayerDataCtrl;
import coffee.dape.utils.ItemUtils;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.json.PersistJson;

/**
 * 
 * @author Laeven
 *
 */
public class PlayerInventory implements PersistJson
{
	private PlayerData parent;
	private Map<Integer,ItemStack> inventory;
	
	private enum Slot
	{
		// Not used and not part of player inventory
//		CRAFT_GRID_RESULT(0),
//		CRAFT_GRID_TOP_LEFT(1),
//		CRAFT_GRID_TOP_RIGHT(2),
//		CRAFT_GRID_BOTTOM_LEFT(3),
//		CRAFT_GRID_BOTTOM_RIGHT(4),
		
		HOTBAR_1(0),
		HOTBAR_2(1),
		HOTBAR_3(2),
		HOTBAR_4(3),
		HOTBAR_5(4),
		HOTBAR_6(5),
		HOTBAR_7(6),
		HOTBAR_8(7),
		HOTBAR_9(8),
		
		INV_SLOT_1(9),
		INV_SLOT_2(10),
		INV_SLOT_3(11),
		INV_SLOT_4(12),
		INV_SLOT_5(13),
		INV_SLOT_6(14),
		INV_SLOT_7(15),
		INV_SLOT_8(16),
		INV_SLOT_9(17),
		INV_SLOT_10(18),
		INV_SLOT_11(19),
		INV_SLOT_12(20),
		INV_SLOT_13(21),
		INV_SLOT_14(22),
		INV_SLOT_15(23),
		INV_SLOT_16(24),
		INV_SLOT_17(25),
		INV_SLOT_18(26),
		INV_SLOT_19(27),
		INV_SLOT_20(28),
		INV_SLOT_21(29),
		INV_SLOT_22(30),
		INV_SLOT_23(31),
		INV_SLOT_24(32),
		INV_SLOT_25(33),
		INV_SLOT_26(34),
		INV_SLOT_27(35),
		
		BOOT_SLOT(36),
		LEGS_SLOT(37),
		CHEST_SLOT(38),
		HELM_SLOT(39),
		
		OFFHAND_SLOT(40),
		;
		
		int slot;
		
		Slot(int slot)
		{
			this.slot = slot;
		}
		
		public int getSlot()
		{
			return slot;
		}
	}
	
	public PlayerInventory(PlayerData data)
	{
		this.parent = data;
		this.inventory = new HashMap<>();
	}
	
	public PlayerInventory(UUID owner)
	{
		this.parent = PlayerDataCtrl.getPlayerData(owner);
		this.inventory = new HashMap<>();
	}
	
	/**
	 * Set an item in this players saved inventory
	 * @param stack Stack to save
	 * @param slot Slot
	 */
	public void setItem(ItemStack stack,Slot slot)
	{		
		setItem(stack,slot.getSlot());
	}
	
	/**
	 * Set an item in this players saved inventory
	 * @param stack Stack to save
	 * @param slot slot Id
	 */
	public void setItem(ItemStack stack,int slot)
	{
		if(slot > 44 || slot < 0) { throw new IllegalArgumentException("Inventory slots are between 0 and 44"); }
		
		this.inventory.put(slot,new ItemStack(stack));
	}
	
	/**
	 * Get an item from this players saved inventory
	 * @param slot Slot
	 * @return ItemStack
	 */
	public ItemStack getItem(Slot slot)
	{
		return getItem(slot.getSlot());
	}
	
	/**
	 * Get an item from this players saved inventory
	 * @param slot slot Id
	 * @return ItemStack
	 */
	public ItemStack getItem(int slot)
	{
		if(!containsItem(slot)) { return null; }
		return this.inventory.get(slot);
	}
	
	/**
	 * Checks if this players inventory contains an item at a slot
	 * @param slot Slot
	 * @return true if an ItemStack exists in this slot
	 */
	public boolean containsItem(Slot slot)
	{
		return containsItem(slot.getSlot());
	}
	
	/**
	 * Checks if this players inventory contains an item at a slot
	 * @param slot Slot
	 * @return true if an ItemStack exists in this slot
	 */
	public boolean containsItem(int slot)
	{
		return this.inventory.containsKey(slot);
	}

	/**
	 * Updates the contents of this object with the owners inventory
	 */
	public void updateInventory()
	{
		if(!PlayerUtils.isOnline(parent.getOwner())) { return; }
		
		Player p = PlayerUtils.getPlayer(parent.getOwner());
		
		this.inventory.clear();
		
		for(int i = 0; i < 41; i++)
		{
			ItemStack stack = p.getInventory().getItem(i);
			
			if(ItemUtils.isNullOrAir(stack)) { continue; }
			
			this.inventory.put(i,stack);
		}
	}
	
	/**
	 * Overwrites the players inventory with the saved
	 * inventory stored in this object
	 */
	public void loadInventoryToPlayer()
	{
		if(!PlayerUtils.isOnline(parent.getOwner())) { return; }
		
		Player p = PlayerUtils.getPlayer(parent.getOwner());
		
		p.getInventory().clear();
		
		for(int slot : this.inventory.keySet())
		{
			p.getInventory().setItem(slot,this.inventory.get(slot));
		}
	}
	
	public Map<Integer, ItemStack> getInventory()
	{
		return inventory;
	}

	public void setInventory(Map<Integer, ItemStack> inventory)
	{
		this.inventory = inventory;
	}

	public void setParent(PlayerData parent)
	{
		this.parent = parent;
	}
	
	public PlayerData getParent()
	{
		return parent;
	}
	
	@Override
	public JsonObject serialise() throws SerialiseException
	{
		JsonObject obj = new JsonObject();
		
		for(int slot : inventory.keySet())
		{
			String encodedItemStack = ItemUtils.toBase64(inventory.get(slot));
			
			if(encodedItemStack == null)
			{
				throw new SerialiseException("Inventory could not serialise ItemStack in slot '" + slot + "'");
			}
			
			obj.addProperty(String.valueOf(slot),encodedItemStack);
		}
		
		return obj;
	}


	@Override
	public void deserialise(JsonObject obj) throws DeserialiseException
	{
		inventory.clear();
		
		for(Entry<String,JsonElement> entry : obj.entrySet())
		{
			if(!entry.getKey().matches("\\d+"))
			{
				throw new DeserialiseException("Inventory could not load a slot. Slot '" + entry.getKey() + "' is not a real slot number");
			}
			
			ItemStack stack = ItemUtils.fromBase64(entry.getValue().getAsString());
			
			if(ItemUtils.isErrorItemStack(stack))
			{
				throw new DeserialiseException("Inventory could not load slot '" + entry.getKey() + "'. ItemStack could not be decoded!");
			}
			
			inventory.put(Integer.parseInt(entry.getKey()),stack);
		}
	}
}