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
public class PlayerEnderchest implements PersistJson
{
	private PlayerData parent;
	private Map<Integer,ItemStack> enderchest;
	
	private enum Slot
	{
		EC_SLOT_1(0),
		EC_SLOT_2(1),
		EC_SLOT_3(2),
		EC_SLOT_4(3),
		EC_SLOT_5(4),
		EC_SLOT_6(5),
		EC_SLOT_7(6),
		EC_SLOT_8(7),
		EC_SLOT_9(8),
		EC_SLOT_10(9),
		EC_SLOT_11(10),
		EC_SLOT_12(11),
		EC_SLOT_13(12),
		EC_SLOT_14(13),
		EC_SLOT_15(14),
		EC_SLOT_16(15),
		EC_SLOT_17(16),
		EC_SLOT_18(17),
		EC_SLOT_19(18),
		EC_SLOT_20(19),
		EC_SLOT_21(20),
		EC_SLOT_22(21),
		EC_SLOT_23(22),
		EC_SLOT_24(23),
		EC_SLOT_25(24),
		EC_SLOT_26(25),
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
	
	public PlayerEnderchest(PlayerData data)
	{
		this.parent = data;
		this.enderchest = new HashMap<>();
	}
	
	public PlayerEnderchest(UUID owner)
	{
		this.parent = PlayerDataCtrl.getPlayerData(owner);
		this.enderchest = new HashMap<>();
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
		if(slot > 26 || slot < 0) { throw new IllegalArgumentException("Enderchest slots are between 0 and 26"); }
		
		this.enderchest.put(slot,new ItemStack(stack));
	}
	
	/**
	 * Get an item from this players saved enderchest
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
		return this.enderchest.get(slot);
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
		return this.enderchest.containsKey(slot);
	}
	
	/**
	 * Updates the contents of this object with the owners inventory
	 */
	public void updateEnderchest()
	{
		if(!PlayerUtils.isOnline(parent.getOwner())) { return; }
		
		Player p = PlayerUtils.getPlayer(parent.getOwner());
		
		this.enderchest.clear();
		
		for(int i = 0; i < 27; i++)
		{
			ItemStack stack = p.getEnderChest().getItem(i);
			
			if(ItemUtils.isNullOrAir(stack)) { continue; }
			
			this.enderchest.put(i,stack);
		}
	}
	
	/**
	 * Overwrites the players inventory with the saved
	 * inventory stored in this object
	 */
	public void loadEnderchestToPlayer()
	{
		if(!PlayerUtils.isOnline(parent.getOwner())) { return; }
		
		Player p = PlayerUtils.getPlayer(parent.getOwner());
		
		p.getEnderChest().clear();
		
		for(int slot : this.enderchest.keySet())
		{
			p.getEnderChest().setItem(slot,this.enderchest.get(slot));
		}
	}
	
	public Map<Integer,ItemStack> getEnderchest()
	{
		return enderchest;
	}

	public void setEnderchest(Map<Integer,ItemStack> enderchestContents)
	{
		this.enderchest = enderchestContents;
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
		
		for(int slot : enderchest.keySet())
		{
			String encodedItemStack = ItemUtils.toBase64(enderchest.get(slot));
			
			if(encodedItemStack == null)
			{
				throw new SerialiseException("EnderChest could not serialise ItemStack in slot '" + slot + "'");
			}
			
			obj.addProperty(String.valueOf(slot),encodedItemStack);
		}
		
		return obj;
	}


	@Override
	public void deserialise(JsonObject obj) throws DeserialiseException
	{
		enderchest.clear();
		
		for(Entry<String,JsonElement> entry : obj.entrySet())
		{
			if(!entry.getKey().matches("\\d+"))
			{
				throw new DeserialiseException("EnderChest could not load a slot. Slot '" + entry.getKey() + "' is not a real slot number");
			}
			
			ItemStack stack = ItemUtils.fromBase64(entry.getValue().getAsString());
			
			if(ItemUtils.isErrorItemStack(stack))
			{
				throw new DeserialiseException("EnderChest could not load slot '" + entry.getKey() + "'. ItemStack could not be decoded!");
			}
			
			enderchest.put(Integer.parseInt(entry.getKey()),stack);
		}
	}
}