package coffee.dape.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.google.common.collect.Multimap;

import coffee.dape.Dape;
import coffee.dape.utils.GradientUtils.LinearGradient;
import coffee.dape.utils.data.DataUtils;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Laeven
 * @since 1.0.0
 */
public class ItemBuilderLegacy
{
	private final static Set<Character> colourDec = new HashSet<>();
	
	static
	{
		colourDec.add('0');
		colourDec.add('1');
		colourDec.add('2');
		colourDec.add('3');
		colourDec.add('4');
		colourDec.add('5');
		colourDec.add('6');
		colourDec.add('7');
		colourDec.add('8');
		colourDec.add('9');
		colourDec.add('a');
		colourDec.add('b');
		colourDec.add('c');
		colourDec.add('d');
		colourDec.add('e');
		colourDec.add('f');
		colourDec.add('k');
		colourDec.add('l');
		colourDec.add('m');
		colourDec.add('n');
		colourDec.add('o');
		colourDec.add('p');
	}
	
	// The item
	private ItemStack stack;
	
	/**
	 * Takes a currently existing item and adds it to the builder
	 * @param newStack The item stack
	 */
    public ItemBuilderLegacy(ItemStack newStack)
    {
        stack = newStack;
    }
    
    /**
     * Creates a new empty ItemStack with a material
     * @param mat The material to set the item as
     */
    public ItemBuilderLegacy(Material mat)
    {
    	stack = new ItemStack(mat);
    }
    
    /**
     * Creates a new empty ItemStack with a material
     * @param mat The material to set the item as
     * @param amount The amount of this item
     */
    public ItemBuilderLegacy(Material mat,int amount)
    {
    	stack = new ItemStack(mat,amount);
    }
    
    /**
     * Creates a new empty ItemStack in the item builder
     */
    public ItemBuilderLegacy()
    {
    	stack = new ItemStack(Material.GRASS_BLOCK);
    }
    
    /**
     * Sets the material of the item
     * @param mat The new material of the item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setMat(Material mat)
    {
        stack.setType(mat);
    	return this;
    }
    
    /**
     * Command methods used to convert the string to their 
     * native data type.
     */
    public ItemBuilderLegacy setMat(String mat)
    {
    	ItemMeta meta = this.stack.getItemMeta();
    	ItemBuilderLegacy b = setMat(MaterialUtils.getSpigotMaterial(mat));
    	b.getItemStack().setItemMeta(meta);
    	
    	return b;
    }

    /**
     * Sets the display name of the item
     * @param name The new name of the item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setName(String name) 
    {
    	ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(ColourUtils.translate("&r" + name));
        stack.setItemMeta(stackMeta);
        return this;
    }
    
    /**
     * Sets the display name of the item using a gradient
     * @param name The new name of the item
     * @param hexs Hex colours to use in this gradient
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setGradientName(String name,String... hexs) 
    {
    	if(hexs == null || hexs.length == 0) { Logg.error("No hex values were passed!"); return this; }
    	
    	ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(GradientUtils.applyGradient(name,LinearGradient.of(hexs)));
        stack.setItemMeta(stackMeta);
        return this;
    }
    
    /**
     * Sets the display name of the item using a gradient
     * @param name The new name of the item
     * @param grad The LinearGradient to use
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setGradientName(String name,LinearGradient grad) 
    {
    	ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(GradientUtils.applyGradient(name,grad));
        stack.setItemMeta(stackMeta);
        return this;
    }
    
    /**
     * Sets the display name of the item using a custom colour
     * @param name The new name of the item
     * @param colour The custom colour to use
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setCustomColourName(String name,Color colour) 
    {
    	ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(ColourUtils.applyColour(name,colour));
        stack.setItemMeta(stackMeta);
        return this;
    }
    
    /**
     * Sets the display name of the item using a custom colour
     * @param name The new name of the item
     * @param colourHex The hex value of the custom colour to use
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setCustomColourName(String name,String colourHex) 
    {
    	ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(ColourUtils.applyColour(name,ColourUtils.hexToColour(colourHex)));
        stack.setItemMeta(stackMeta);
        return this;
    }
    
    /**
     * Sets the display name of the item without any post colour formatting
     * @param name The new name of the item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setRawName(String name) 
    {
    	ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(name);
        stack.setItemMeta(stackMeta);
        return this;
    }
    
    /**
     * Resets the display name of the item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy resetName() 
    {
    	ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(null);
        stack.setItemMeta(stackMeta);
        return this;
    }

    /**
     * Sets the amount of that item
     * @param amount The amount of the item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setAmount(int amount)
    {
        stack.setAmount(amount);
        return this;
    }
    
    /**
     * Set the amount of the item
     * @param amount The amount of this item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setAmount(String amount)
    {
    	return setAmount(Integer.parseInt(amount));
    }
    
    /**
     * Overwrite all lore on this item with new lore
     * @param loreStrings The lore to be overwritten on this item with
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setLore(String... loreStrings) 
    {
    	List<String> loreList = new ArrayList<String>();
    	
    	for(String loreString : loreStrings)
    	{
    		loreList.add(ColourUtils.translate("&r" + loreString));
    	}

        ItemMeta meta = stack.getItemMeta();
        meta.setLore(loreList);
        stack.setItemMeta(meta);
        return this;
    }
    
    /**
     * Overwrite all lore on this item with new lore
     * @param numberOfCharactersPerLine Maximum number of characters that should be displayed in each line of lore
     * @param overflow If complete words should have the exception to overflow instead of being added to the next line
     * @param loreString The lore to be overwritten on this item with
     * @return ItemBuilder
     */    
    public ItemBuilderLegacy setLore(int numberOfCharactersPerLine,boolean overflow,String loreString)
    {
    	return constructLines(new ArrayList<>(),numberOfCharactersPerLine,overflow,loreString);
    }
    
    /**
     * Overwrite all lore on this item with new lore
     * @param loreList A list of Lore to be overwritten on this item with
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setLore(List<String> loreList) 
    {
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(loreList);
        stack.setItemMeta(meta);
        return this;
    }
    
    /**
     * Append the existing lore on this item
     * @param loreStrings The Lore to be appended to this item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy addLore(String... loreStrings) 
    {
        ItemMeta meta = stack.getItemMeta();
        List<String> currentLore = getStackLore();
        
    	for(String loreString : loreStrings)
    	{
    		currentLore.add(ColourUtils.translate("&r" + loreString));
    	}
        
        meta.setLore(currentLore);
        stack.setItemMeta(meta);
        return this;
    }
    
    /**
     * Append the existing lore on this item
     * @param numberOfCharactersPerLine Maximum number of characters that should be displayed in each line of lore
     * @param overflow If complete words should have the exception to overflow instead of being added to the next line
     * @param loreString The lore to be overwritten on this item with
     * @return ItemBuilder
     */
    public ItemBuilderLegacy addLore(int numberOfCharactersPerLine,boolean overflow,String loreString)
    {
        return constructLines(getStackLore(),numberOfCharactersPerLine,overflow,loreString);
    }
    
    /**
     * Returns the colour and decoration codes at the
     * beginning of a string
     * @param text String with colour codes
     * @return Colour and decoration codes
     */
    private String getColourSequence(String text)
	{
		int seek = 0;
		StringBuilder sb = new StringBuilder();
		
		if(text.length() == 0) { return ""; }
		
		while((text.charAt(seek) == ((char) 38) && (seek + 1) < text.length()) || (text.charAt(seek) == ((char) 167) && (seek + 1) < text.length()))
		{
			sb.append(text.substring(seek,seek + 2));
			seek+= 2;
		}
		
		return sb.toString();
	}
    
    /**
     * Constructs lines of lore using a single line of lore. Each line length is specified by numberOfCharactersPerLine
     * @param numberOfCharactersPerLine Maximum number of characters that should be displayed in each line of lore
     * @param overflow If complete words should have the exception to overflow instead of being added to the next line
     * @param loreString The single line of lore to be divided up into multiple lines
     * @return ItemBuilder
     */  
    @SuppressWarnings("unused")
    @Deprecated
	private ItemBuilderLegacy constructLinesFast(List<String> loreList,int numberOfCharactersPerLine,boolean overflow,String loreString)
    {
    	StringBuilder sb = new StringBuilder();
        int currentLineLength = 0;
        String colourDec = "";
        
        for(String word : loreString.split(" "))
        {
        	String newColourDec = getColourSequence(word);
        	String strippedWord = new String(word);
        	strippedWord = ChatColor.stripColor(strippedWord);
        	
        	if(!newColourDec.equals("")) { colourDec = newColourDec; }
        	
        	if(currentLineLength + 1 + strippedWord.length() <= numberOfCharactersPerLine)
        	{
        		sb.append(colourDec + word + " ");
        		currentLineLength += strippedWord.length() + 1;
        	}
        	else if(overflow)
        	{
        		sb.append(colourDec + word + " ");
        		currentLineLength = 0;
        		loreList.add(ColourUtils.translate(sb.toString()));
        		sb.setLength(0);
        	}
        	else
        	{
        		currentLineLength = 0;
        		loreList.add(ColourUtils.translate(sb.toString()));
        		sb.setLength(0);
        		sb.append(colourDec + word + " ");
        		currentLineLength += strippedWord.length() + 1;
        	}
        }
        
        loreList.add(ColourUtils.translate(sb.toString()));
        
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(loreList);
        stack.setItemMeta(meta);
        return this;
    }
    
	/**
     * Constructs lines of lore using a single line of lore. Each line length is specified by numberOfCharactersPerLine
     * @param numberOfCharactersPerLine Maximum number of characters that should be displayed in each line of lore
     * @param overflow If complete words should have the exception to overflow instead of being added to the next line
     * @param loreString The single line of lore to be divided up into multiple lines
     * @return ItemBuilder
     */  
    private ItemBuilderLegacy constructLines(List<String> loreList,int numberOfCharactersPerLine,boolean overflow,String loreString)
    {
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(StringUtils.constructLines(loreList,numberOfCharactersPerLine,overflow,loreString));
        stack.setItemMeta(meta);
        return this;
    }
    
    /**
     * Append the existing lore on this item
     * @param loreList A list of Lore to be appended to this item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy addLore(List<String> loreList) 
    {
        ItemMeta meta = stack.getItemMeta();
        List<String> currentLore = getStackLore();
        
        for(String newLoreLine : loreList)
        {
        	currentLore.add(ColourUtils.translate(newLoreLine));
        }
        
        meta.setLore(currentLore);
        stack.setItemMeta(meta);
        return this;
    }
    
    /**
     * Attempts to remove lore at the index specified. If this line index doesn't exist nothing will be removed.
     * @param index Lore line index to remove (starting at 0)
     * @return ItemBuilder
     */
    public ItemBuilderLegacy removeLore(int index)
    {
    	ItemMeta meta = stack.getItemMeta();
        List<String> currentLore = getStackLore();
        
        if(index <= (currentLore.size() - 1))
        {
        	currentLore.remove(index);
        }
        
        meta.setLore(currentLore);
        stack.setItemMeta(meta);
        return this;
    }
    
    /**
     * Remove a line of lore based on its contents
     * @param loreToRemove Line of lore to find and remove
     * @return ItemBuilder
     */
    public ItemBuilderLegacy removeLore(String loreToRemove)
    {
    	ItemMeta meta = stack.getItemMeta();
        List<String> currentLore = getStackLore();
        Iterator<String> loreIt = currentLore.iterator();
        
        while(loreIt.hasNext())
        {
        	String strippedLore = ChatColor.stripColor(loreIt.next());
        	
        	if(loreToRemove.equals(strippedLore))
        	{
        		loreIt.remove();
        	}
        }
        
        meta.setLore(currentLore);
        stack.setItemMeta(meta);
        return this;
    }
    
    /**
     * Remove a line of lore based on its contents
     * @param loreToRemove Line of lore to find and remove
     * @return ItemBuilder
     */
    public ItemBuilderLegacy removeLore(String[] loreToRemove)
    {
    	ItemMeta meta = stack.getItemMeta();
        List<String> currentLore = getStackLore();
        Iterator<String> loreIt = currentLore.iterator();
        
        while(loreIt.hasNext())
        {
        	String strippedLore = ChatColor.stripColor(loreIt.next());
        	
        	LoreStringsToRemove:
        	for(String lore : loreToRemove)
        	{
            	if(lore.equals(strippedLore))
            	{
            		loreIt.remove();
            		break LoreStringsToRemove;
            	}
        	}
        }
        
        meta.setLore(currentLore);
        stack.setItemMeta(meta);
        return this;
    }
    
    /**
     * Replaces lore at the specified index
     * @param index the index to replace lore at
     * @param loreString The new lore string to replace the old lore
     * @return ItemBuilder
     */
    public ItemBuilderLegacy replaceLore(int index,String loreString)
    {
    	ItemMeta meta = stack.getItemMeta();
    	List<String> currentLore = getStackLore();
        
        if(index <= (currentLore.size() - 1))
        {
        	currentLore.remove(index);
        	currentLore.add(index,ColourUtils.translate("&r" + loreString));
        }
        
        meta.setLore(currentLore);
        stack.setItemMeta(meta);
        return this;
    }
    
    /**
     * Inserts lore starting at a specific index
     * @param index the index to start inserting lore at
     * @param loreStrings the lore to add to this item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy insertLore(int index,String... loreStrings)
    {
    	ItemMeta meta = stack.getItemMeta();
    	List<String> currentLore = getStackLore();
        List<String> newLore = new ArrayList<>();
        
        for(int i = 0; i < currentLore.size(); i++)
        {
        	if(i == index)
        	{
        		for(String lore : loreStrings)
        		{
        			newLore.add(ColourUtils.translate("&r" + lore));
        		}
        	}
        	
        	newLore.add(currentLore.get(i));
        }
        
        meta.setLore(newLore);
        stack.setItemMeta(meta);
        return this;
    }
    
    /**
     * Inserts lore starting at a specific index
     * @param index the index to start inserting lore at
     * @param loreStrings the lore to add to this item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy insertLore(int index,List<String> loreStrings)
    {
    	insertLore(index,loreStrings.toArray(new String[loreStrings.size()]));
    	return this;
    }
    
    /**
     * Adds lore to the item
     * @param lore The new lore to replace it
     * @param loreLineContains Looks for a line of this lore that contains this string
     * @return ItemBuilder
     */
    public ItemBuilderLegacy replaceLore(String lore,String loreLineContains) 
    {
        ItemMeta meta = stack.getItemMeta();
        List<String> currentLore = getStackLore();
        
        LoreLineSearch:
        for(String loreLine : currentLore)
        {
        	if(loreLine.contains(loreLineContains))
        	{
        		loreLine = lore;
        		break LoreLineSearch;
        	}
        }
        
        meta.setLore(currentLore);
        stack.setItemMeta(meta);
        return this;
    }
    
    /**
     * Clears the lore of the item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy clearLore()
    {
    	ItemMeta meta = stack.getItemMeta();
    	meta.setLore(null);
    	//meta.setLore(Collections.emptyList());
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Gets lore if stack has lore, otherwise returns empty list
     * @return lore list
     */
    private List<String> getStackLore()
    {
    	if(stack.getItemMeta().hasLore())
        {
    		return new ArrayList<String>(stack.getItemMeta().getLore());
        }
        else
        {
        	return new ArrayList<String>();
        }
    }
    
    /**
     * Adds an enchantment to this item
     * @param ench Enchantment to add
     * @param level Enchantment level
     * @return ItemBuilder
     */
    public ItemBuilderLegacy addEnchantment(Enchantment ench,int level)
    {
    	if(stack.getType() == Material.ENCHANTED_BOOK)
		{
			EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) stack.getItemMeta();
			storageMeta.addStoredEnchant(ench,level,true);
	    	stack.setItemMeta(storageMeta);
		}
		else
		{
			ItemMeta meta = stack.getItemMeta();
			meta.addEnchant(ench,level,true);
	    	stack.setItemMeta(meta);
		}
    	return this;
    }
    
    /**
     * Removes an enchantment from this item
     * @param ench Enchantment to remove
     * @return ItemBuilder
     */
    public ItemBuilderLegacy removeEnchantment(Enchantment ench)
    {
    	if(stack.getType() == Material.ENCHANTED_BOOK)
		{
			EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) stack.getItemMeta();
			if(!storageMeta.hasStoredEnchants()) { return this; }
	    	if(storageMeta.hasStoredEnchant(ench))
	    	{
	    		storageMeta.removeStoredEnchant(ench);
	    	}
	    	stack.setItemMeta(storageMeta);
		}
		else
		{
			ItemMeta meta = stack.getItemMeta();
			if(!meta.hasEnchants()) { return this; }
	    	if(meta.hasEnchant(ench))
	    	{
	    		meta.removeEnchant(ench);
	    	}
	    	stack.setItemMeta(meta);
		}
    	
    	ItemMeta meta = stack.getItemMeta();
    	if(!meta.hasEnchants()) { return this; }
    	if(meta.hasEnchant(ench))
    	{
    		meta.removeEnchant(ench);
    	}
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Replaces an enchantment on this item for another one
     * @param ench Enchantment to be removed
     * @param ench2 Enchantment to be replaced with
     * @param level Enchantment level
     * @return ItemBuilder
     */
    public ItemBuilderLegacy replaceEnchantment(Enchantment ench,Enchantment ench2,int level)
    {
    	ItemMeta meta = stack.getItemMeta();
    	if(!meta.hasEnchants()) { return this; }
    	if(meta.hasEnchant(ench))
    	{
    		meta.removeEnchant(ench);
    		meta.getEnchants().put(ench2,level);
    	}
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Overwrites the enchants on this item
     * @param maps Map of enchants
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setEnchants(Map<Enchantment,Integer> maps)
    {
    	ItemMeta meta = stack.getItemMeta();
    	
    	for(Enchantment enchant : meta.getEnchants().keySet())
 		{
 			meta.removeEnchant(enchant);
 		}
    	
    	for(Enchantment enchant : maps.keySet())
 		{
 			meta.addEnchant(enchant,maps.get(enchant),true);
 		}
    	
     	stack.setItemMeta(meta);
     	return this;
    }
    
    /**
     * Clears the enchants on this item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy clearEnchants()
    {
     	if(stack.getType() == Material.ENCHANTED_BOOK)
		{
			EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) stack.getItemMeta();
			
	    	for(Enchantment enchant : storageMeta.getStoredEnchants().keySet())
	 		{
	    		storageMeta.removeStoredEnchant(enchant);
	 		}
	    	
	    	stack.setItemMeta(storageMeta);
		}
		else
		{
			ItemMeta meta = stack.getItemMeta();
	    	
	    	for(Enchantment enchant : meta.getEnchants().keySet())
	 		{
	 			meta.removeEnchant(enchant);
	 		}
	    	
	     	stack.setItemMeta(meta);
		}
     	
     	return this;
    }
    
    /**
     * Adds ItemFlags to this item
     * @param flags ItemFlags
     * @return ItemBuilder
     */
    public ItemBuilderLegacy addItemFlags(ItemFlag... flags)
    {
    	ItemMeta meta = stack.getItemMeta();
    	meta.addItemFlags(flags);
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Removes ItemFlags from this item
     * @param flags ItemFlags
     * @return ItemBuilder
     */
    public ItemBuilderLegacy removeItemFlags(ItemFlag... flags)
    {
    	ItemMeta meta = stack.getItemMeta();
    	meta.removeItemFlags(flags);
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Overwrites the ItemFlags on this item
     * @param flags Set of ItemFlags
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setItemFlags(Set<ItemFlag> flags)
    {
    	ItemMeta meta = stack.getItemMeta();
    	meta.removeItemFlags(ItemFlag.values());
    	
    	for(ItemFlag flag : flags)
		{
			meta.addItemFlags(flag);
		}
    	
    	stack.setItemMeta(meta);
     	return this;
    }
    
    /**
     * Clears the ItemFlags on this item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy clearItemFlags()
    {
    	ItemMeta meta = stack.getItemMeta();
    	meta.removeItemFlags(ItemFlag.values());
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Makes an item unbreakable or not
     * @param isUnbreakable If Item is unbreakable
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setUnbreakable(boolean isUnbreakable)
    {
    	ItemMeta meta = stack.getItemMeta();
    	meta.setUnbreakable(isUnbreakable);
     	stack.setItemMeta(meta);
     	return this;
    }
    
    /**
     * Adds an attribute that applies to ALL equipment slots
     * @param att Attribute
     * @param op AttributeModifier Operation
     * @param amount Amount to modify
     * @return ItemBuilder
     */
    public ItemBuilderLegacy addAttribute(Attribute att,AttributeModifier.Operation op,double amount)
    {
    	ItemMeta meta = stack.getItemMeta();
    	meta.addAttributeModifier(att,new AttributeModifier(Dape.getNamespacedKey(),amount,op,EquipmentSlotGroup.ANY));
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Adds an attribute that applies to one equipment slot
     * @param att Attribute
     * @param slot EquipmentSlot this attribute applies to
     * @param op AttributeModifier Operation
     * @param amount Amount to modify
     * @return ItemBuilder
     */
    public ItemBuilderLegacy addAttribute(Attribute att,EquipmentSlotGroup slot,AttributeModifier.Operation op,double amount)
    {
    	ItemMeta meta = stack.getItemMeta();
    	meta.addAttributeModifier(att,new AttributeModifier(Dape.getNamespacedKey(),amount,op,slot));
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Removes an attribute that applies to ALL equipment slots
     * @param att Attribute
     * @return ItemBuilder
     */
    public ItemBuilderLegacy removeAttribute(Attribute att)
    {
    	ItemMeta meta = stack.getItemMeta();
    	
    	if(meta.hasAttributeModifiers())
    	{
    		if(!meta.getAttributeModifiers().containsKey(att)) { return this; }
    		
    		meta.removeAttributeModifier(att);
    	}
    	
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Removes an attribute that applies to one equipment slot
     * @param att Attribute
     * @param slot EquipmentSlot this attribute applies to
     * @return ItemBuilder
     */
    public ItemBuilderLegacy removeAttribute(Attribute att,EquipmentSlot slot)
    {
    	ItemMeta meta = stack.getItemMeta();
    	
    	if(meta.hasAttributeModifiers())
    	{
    		if(!meta.getAttributeModifiers().containsKey(att)) { return this; }
    		
    		for(AttributeModifier mod : meta.getAttributeModifiers().get(att))
    		{
    			if(mod.getName().equals(slot.toString()))
    			{
    				meta.removeAttributeModifier(att,mod);
    			}
    		}
    	}
    	
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Overwrites the attributes on this item
     * @param attMod Attribute,AttributeModifier MultiMap
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setAttributes(Multimap<Attribute,AttributeModifier> attMod)
    {
    	ItemMeta meta = stack.getItemMeta();
    	meta.setAttributeModifiers(attMod);
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Clears the attributes on this item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy clearAttributes()
    {
    	ItemMeta meta = stack.getItemMeta();
    	
    	if(!meta.hasAttributeModifiers()) { return this; }
    	
    	for(Attribute att : Attribute.values())
    	{
    		if(meta.getAttributeModifiers().containsKey(att))
    		{
    			meta.removeAttributeModifier(att);
    		}
    	}
    	
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Sets the durability of the item
     * 
     * <p>Durability values that go beyond the materials maximum will be set
     * to the materials maximum and values that go below 1 are set to 1.
     * @param dura The new durability of the item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setDurability(int dura)
    {
    	if(stack.getItemMeta() instanceof Damageable)
    	{
    		ItemMeta meta = stack.getItemMeta();
    		int durability = stack.getType().getMaxDurability();
    		
    		if(dura > 0 && dura <= stack.getType().getMaxDurability())
    		{
    			durability = stack.getType().getMaxDurability() - dura;
    		}
    		else if(dura <= 0)
    		{
    			// Remove item as it has reached max durability use
    			stack.setAmount(0);
    		}
    		else if(dura > stack.getType().getMaxDurability())
    		{
    			durability = 0;
    		}
    		
    		((Damageable) meta).setDamage(durability);
    		stack.setItemMeta(meta);
    	}
    	
    	return this;
    }
    
    /**
     * Deducts the durability of the item
     * 
     * <p>Deducting durability that results in a number lower than 1
     * will set the durability to 1.
     * @param dura The amount of durability to remove from the item
     * @return ItemBuilder
     */
    public ItemBuilderLegacy deductDurability(int dura)
    {
    	ItemMeta meta = stack.getItemMeta();
    	
    	if(meta instanceof Damageable)
    	{
    		int durability = ((Damageable) meta).getDamage();
    		
    		if((durability + dura) > stack.getType().getMaxDurability())
    		{
    			// Remove item as it has reached max durability use
    			stack.setAmount(0);
    		}
    		else
    		{
    			durability = durability + dura;
    		}
    		
    		((Damageable) meta).setDamage(durability);
    		stack.setItemMeta(meta);
    	}
    	
    	return this;
    }
    
    /**
     * Set durability
     * @param dura The new durability value to set this item with
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setDurability(String dura)
    {
    	return setDurability(Integer.parseInt(dura));
    }
    
    /**
     * Sets the custom model data ID used to reference a different model/texture
     * that will be used to render the item with.
     * 
     * <p>Set to -1 to clear/reset the custom model data value
     * @param customModeDataNumber CustomModelData number
     * @return ItemBuilder
     */
    public ItemBuilderLegacy setCustomModelData(int customModelDataNumber)
    {
    	ItemMeta meta = stack.getItemMeta();
    	
    	if(customModelDataNumber <= -1)
    	{
    		meta.setCustomModelData(null);
    	}
    	else
    	{
    		meta.setCustomModelData(customModelDataNumber);
    	}
    	
    	stack.setItemMeta(meta);
    	return this;
    }
	
	/**
	 * Sets data on this item
	 * @param key The data key
	 * @param DType dataType
	 * @param value The data value
	 */
	public ItemBuilderLegacy setData(String key,Object value)
	{
		DataUtils.set(key,value,stack);
		return this;
	}
	
	/**
	 * Clears DataContainer on this item
	 * @return ItemBuilder
	 */
	public ItemBuilderLegacy clearDataContainer()
	{
		DataUtils.clear(stack);
		return this;
	}
	
	/**
	 * Removes data from this item
	 * @param key The data key
	 */
	public ItemBuilderLegacy removeData(String key)
	{
		DataUtils.remove(key,stack);
		return this;
	}
    
    /**
     * Set meta data for this item
     * @param key The key used to store the data
     * @param data The meta data
     * @return ItemBuilder
     */
	public ItemBuilderLegacy addMetaDataString(NamespacedKey key,String data)
	{
    	ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().set(key,PersistentDataType.STRING,data);
		stack.setItemMeta(meta);
		return this;
	}
	
	/**
     * Set meta data for this item
     * @param key The key used to store the data
     * @param data The meta data
     * @return ItemBuilder
     */
	public ItemBuilderLegacy addMetaDataInteger(NamespacedKey key,int data)
	{
    	ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().set(key,PersistentDataType.INTEGER,data);
		stack.setItemMeta(meta);
		return this;
	}
	
	/**
     * Set meta data for this item
     * @param key The key used to store the data
     * @param data The meta data
     * @return ItemBuilder
     */
	public ItemBuilderLegacy addMetaDataLong(NamespacedKey key,long data)
	{
    	ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().set(key,PersistentDataType.LONG,data);
		stack.setItemMeta(meta);
		return this;
	}
	
	/**
     * Set meta data for this item
     * @param key The key used to store the data
     * @param data The meta data
     * @return ItemBuilder
     */
	public ItemBuilderLegacy addMetaDataDouble(NamespacedKey key,double data)
	{
    	ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().set(key,PersistentDataType.DOUBLE,data);
		stack.setItemMeta(meta);
		return this;
	}
	
	/**
     * Remove meta data for this item
     * @param key The key used to store the data
     * @return ItemBuilder
     */
	public ItemBuilderLegacy removeMetaData(NamespacedKey key)
	{
    	ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().remove(key);
		stack.setItemMeta(meta);
		return this;
	}
	
	/**
	 * Sets this stack as a player skull
	 * @param skull
	 * @return ItemBuilder
	 */
	public ItemBuilderLegacy setAsSkull(ItemStack skull)
	{
		stack = HeadUtils.convertItemStackToHead(skull,stack);
		return this;
	}
    
    /**
     * Returns the new ItemStack
     * @return ItemStack
     */
    public ItemStack getItemStack()
    {
        return stack;
    }
}