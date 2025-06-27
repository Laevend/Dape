package coffee.dape.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_21_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R4.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import coffee.dape.utils.data.DataUtils;

/**
 * @author Laeven
 */
public class ItemUtils
{
	private static final ItemStack ERROR_ITEMSTACK = ItemBuilder.of(Material.GREEN_CONCRETE).name("&aGeneric Item").lore().wrap("&cIf you are seeing this then something has gone wrong with loading the item stack that was to be here!").commit().setData("error_item",1).create();
	
	/**
	 * Checks an item stack to see if it has a custom display name.
	 * If it has a custom display name it will use that, if it does
	 * not, it will use the material as its display name.
	 * @param stack ItemStack
	 * @return ItemStack name
	 */
	public static String getName(ItemStack stack)
	{
		return stack.getItemMeta().hasDisplayName() ? stack.getItemMeta().getDisplayName() : MaterialUtils.getNameFromMaterial(stack.getType());
	}
	
	/**
	 * Returns an empty item stack
	 * @return
	 */
	public static ItemStack emptyItem()
	{
		return new ItemStack(Material.AIR);
	}
	
	/**
	 * Checks if this item stack is null or is air
	 * @param stack The item stack
	 * @return Is this item stack empty?
	 */
	public static boolean isNullOrAir(ItemStack stack)
	{
		return stack == null || stack.getType().equals(Material.AIR) ? true : false;
	}
	
	/**
	 * Returns true if ItemStack has lore
	 * @param stack ItemStack
	 * @return true/false
	 */
	public static boolean hasLore(ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		return meta.hasLore();
	}
	
	/**
	 * Returns true if ItemStack has item flags
	 * @param stack ItemStack
	 * @return true/false
	 */
	public static boolean hasItemFlags(ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		return meta.getItemFlags().size() > 0;
	}
	
	/**
	 * Returns true if ItemStack has an item flag of this type
	 * @param stack ItemStack
	 * @param flag ItemFlag to check for
	 * @return true/false
	 */
	public static boolean hasItemFlag(ItemStack stack,ItemFlag flag)
	{
		ItemMeta meta = stack.getItemMeta();
		return meta.hasItemFlag(flag);
	}
	
	/**
	 * Returns true if ItemStack has enchants
	 * @param stack ItemStack
	 * @return true/false
	 */
	public static boolean hasEnchantments(ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		return meta.hasEnchants();
	}
	
	/**
	 * Returns true if ItemStack has an enchant of this type
	 * @param stack ItemStack
	 * @param ench Enchant to check for
	 * @return true/false
	 */
	public static boolean hasEnchantment(ItemStack stack,Enchantment ench)
	{
		ItemMeta meta = stack.getItemMeta();
		return meta.hasEnchant(ench);
	}
	
	/**
	 * Returns true if ItemStack has attribute modifiers 
	 * @param stack ItemStack
	 * @return true/false
	 */
	public static boolean hasAttributes(ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		return meta.hasAttributeModifiers();
	}
	
	/**
	 * Returns true if ItemStack has an attribute modifier of this type
	 * 
	 * <p>This method also calls @link #hasAttributes(ItemStack) before checking for the
	 * existence of this attribute
	 * @param stack ItemStack
	 * @param att Attribute modifier to check for
	 * @return true/false
	 */
	public static boolean hasAttribute(ItemStack stack,Attribute att)
	{
		if(!ItemUtils.hasAttributes(stack)) { return false; }
		
		ItemMeta meta = stack.getItemMeta();
		return meta.getAttributeModifiers().containsKey(att);
	}
	
	/**
	 * Returns true if ItemStack has an attribute modifier of this type that is active in this slot
	 * 
	 * <p>This method also calls @link #hasAttributes(ItemStack) and @link #hasAttribute(ItemStack, Attribute)
	 * before checking for the existence of this attribute modifier at this equipment slot 
	 * @param stack ItemStack
	 * @param att Attribute modifier to check for
	 * @param slot EquipmentSlot this attribute modifies
	 * @return true/false
	 */
	public static boolean hasAttributeAtSlot(ItemStack stack,Attribute att,EquipmentSlotGroup slot)
	{
		if(!ItemUtils.hasAttributes(stack)) { return false; }
		if(!ItemUtils.hasAttribute(stack,att)) { return false; }
		
		ItemMeta meta = stack.getItemMeta();
		
		for(AttributeModifier mod : meta.getAttributeModifiers(att))
		{
			if(mod.getSlotGroup().toString().equals(slot.toString())) { return true; }
		}
		
		return false;
	}
	
	/**
	 * Returns true if ItemStack has custom model data
	 * @param stack ItemStack
	 * @return true/false
	 */
	public static boolean hasCustomModelData(ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		return meta.hasCustomModelData();
	}
	
	/**
	 * Returns true if ItemStack has data in its data container
	 * @param stack ItemStack
	 * @return true/false
	 */
	public static boolean hasData(ItemStack stack)
	{
		return !DataUtils.hasData(stack);
	}
	
	/**
	 * Returns true if ItemStack has data in its data container and with a specific key
	 * 
	 * <p>This method also calls @link #hasData(ItemStack) before checking for the existence of the key
	 * @param stack ItemStack
	 * @param key Key to check for
	 * @return true/false
	 */
	public static boolean hasDataWithKey(ItemStack stack,String key)
	{
		if(!ItemUtils.hasData(stack)) { return false; }
		return DataUtils.has(key,stack);
	}
	
	/**
	 * Serialise an ItemStack
	 * @param item The itemstack
	 * @return BASE64 Encoded string
	 * @throws IllegalStateException
	 */
	public static String toBase64(ItemStack item)
	{
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream(); BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos))
		{			
	        boos.writeObject(item);
	        boos.close();
	        baos.close();
	        return Base64.getEncoder().encodeToString(baos.toByteArray());
	    } 
		catch (Exception e)
		{
			Logg.error("Vertex could not encode base64 itemstack!",e);
			return null;
	    }
	}
	
	/**
	 * deserialise an ItemStack
	 * @param encodedItem BASE64 String
	 * @return ItemStack
	 * @throws IllegalStateException
	 */
	public static ItemStack fromBase64(String encodedItem)
	{
		byte [] data = Base64.getDecoder().decode(encodedItem);
		
		try(ByteArrayInputStream bais = new ByteArrayInputStream(data); BukkitObjectInputStream bois = new BukkitObjectInputStream(bais))
		{			
			ItemStack stack = (ItemStack) bois.readObject();
	        bois.close();
	        bais.close();
	        return stack;
		}
		catch(Exception e)
		{
			Logg.error("Could not decode base64 itemstack!",e);
			return ERROR_ITEMSTACK.clone();
		}
    }
	
	/**
	 * Checks if this ItemStack was the result of an error.
	 * <p>
	 * When an operation goes wrong that involves setting an ItemStack, a fallback generic item is used instead
	 * @param stack ItemStack to check
	 * @return True if this ItemStack is an error item, false otherwise
	 */
	public static boolean isErrorItemStack(ItemStack stack)
	{
		return DataUtils.has("error_item",stack);
	}
	
	/**
	 * Creates an array of item stacks based on the amount needed
	 * @param amount Amount of this material
	 * @param mat Material type
	 * @return Array of ItemStacks
	 */
	public static ItemStack[] createStacks(int amount,Material mat)
	{
		if(amount < 1) { return null; }
		
		int stackSize = mat.getMaxStackSize();
		int totalStacks = amount / stackSize;
		int remainder = amount % stackSize;
		
		if(totalStacks == 0)
		{
			return new ItemStack[] { new ItemStack(mat,remainder) };
		}
		
		ItemStack[] stacks;
		
		if(remainder > 0)
		{
			stacks = new ItemStack[totalStacks + 1];
			stacks[stacks.length - 2] = new ItemStack(mat,remainder);
		}
		else
		{
			stacks = new ItemStack[totalStacks];
		}
		
		for(int i = 0; i < totalStacks; i++)
		{
			stacks[i] = new ItemStack(mat,stackSize);
		}
		
		return stacks;
	}
	
	/**
	 * Clones an ItemStack
	 * @param stack ItemStack to clone
	 * @return Cloned ItemStack
	 */
	public static ItemStack clone(ItemStack stack)
	{
		return (ItemStack) CraftItemStack.asCraftCopy(stack);
	}
	
	public static net.minecraft.world.item.Item getNMSItem(ItemStack stack)
	{
		net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(stack);
		return nmsItemStack.h();
	}
	
	public static net.minecraft.world.item.Item getNMSItem(Material material)
	{
		return CraftMagicNumbers.getItem(material);
	}
}
