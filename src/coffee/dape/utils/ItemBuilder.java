package coffee.dape.utils;

import java.awt.Color;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import coffee.dape.Dape;
import coffee.dape.utils.GradientUtils.LinearGradient;
import coffee.dape.utils.data.DataUtils;
import coffee.dape.utils.minecraftprofile.MinecraftProfileCtrl;
import coffee.dape.utils.minecraftprofile.data.MinecraftProfile;

public class ItemBuilder
{
	private ItemStack stack = null;
	
	/**
	 * Creates a new ItemBuilder of a material type
	 * @param mat Material to use
	 * @return ItemBuilder
	 */
	public static ItemBuilder of(Material mat)
	{
		Objects.requireNonNull(mat,"Material cannot be null!");
		ItemBuilder ib = new ItemBuilder();
		ib.stack = new ItemStack(mat);
		return ib;
	}
	
	/**
	 * Creates a new ItemBuilder of a material type and an amount
	 * @param mat Material to use
	 * @param amount Amount of this item
	 * @return ItemBuilder
	 */
	public static ItemBuilder of(Material mat,int amount)
	{
		Objects.requireNonNull(mat,"Material cannot be null!");
		Objects.requireNonNull(amount,"Amount cannot be null!");
		ItemBuilder ib = new ItemBuilder();
		ib.stack = new ItemStack(mat,amount);
		return ib;
	}
	
	/**
	 * Creates a new ItemBuilder using another ItemStack as a base
	 * @param stack ItemStack to use as a base
	 * @return ItemBuilder
	 */
	public static ItemBuilder of(ItemStack stack)
	{
		Objects.requireNonNull(stack,"ItemStack cannot be null!");
		ItemBuilder ib = new ItemBuilder();
		ib.stack = new ItemStack(stack);
		return ib;
	}
	
	/**
	 * Creates a new ItemBuilder using a skull texture
	 * @param stack ItemStack to use as a base
	 * @return ItemBuilder
	 */
	public static ItemBuilder ofSkull(String skullTexture)
	{
		Objects.requireNonNull(skullTexture,"SkullTexture cannot be null!");
		ItemBuilder ib = new ItemBuilder();
		ib.stack = new ItemStack(Material.PLAYER_HEAD,1);
		ib.skull(skullTexture);
		return ib;
	}
	
	/**
	 * Creates a new ItemBuilder using a skull texture
	 * @param stack ItemStack to use as a base
	 * @param customUUID Uses a specific uuid to prevent skulls of the same texture from not stacking in a players inventory
	 * @return ItemBuilder
	 */
	public static ItemBuilder ofSkull(String skullTexture,UUID customUUID)
	{
		Objects.requireNonNull(skullTexture,"SkullTexture cannot be null!");
		ItemBuilder ib = new ItemBuilder();
		ib.stack = new ItemStack(Material.PLAYER_HEAD,1);
		ib.skull(skullTexture);
		return ib;
	}
	
	/**
	 * Creates a new ItemBuilder using a skull texture of a players head
	 * @param stack ItemStack to use as a base
	 * @return ItemBuilder
	 */
	public static ItemBuilder ofSkull(UUID playerUUID)
	{
		Objects.requireNonNull(playerUUID,"Player UUID cannot be null!");
		ItemBuilder ib = new ItemBuilder();
		ib.stack = new ItemStack(Material.PLAYER_HEAD,1);
		ib.skull(playerUUID);
		return ib;
	}
	
	/**
	 * Sets the items material type
	 * @param mat Material to use
	 * @return ItemBuilder
	 */
	public ItemBuilder mat(Material mat)
	{
		Objects.requireNonNull(mat,"Material cannot be null!");
		stack.setType(mat);
		return this;
	}
	
	/**
	 * Sets the name of this item stack
	 * @param itemName Name of this item stack
	 * @return ItemBuilder
	 */
	public ItemBuilder name(String itemName)
	{
		Objects.requireNonNull(itemName,"ItemName cannot be null!");
		ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(ColourUtils.translate("&r" + itemName));
        stack.setItemMeta(stackMeta);
        return this;
	}
	
	/**
	 * Sets the name of this item stack
	 * @param itemName Name of this item stack
	 * @param gradient Gradient to apply to this item name
	 * @return ItemBuilder
	 */
	public ItemBuilder name(String itemName,LinearGradient gradient)
	{
		Objects.requireNonNull(itemName,"ItemName cannot be null!");
		Objects.requireNonNull(gradient,"Gradient cannot be null!");
		ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(GradientUtils.applyGradient(itemName,gradient));
        stack.setItemMeta(stackMeta);
        return this;
	}
	
	/**
	 * Sets the name of this item stack
	 * @param itemName Name of this item stack
	 * @param colour Colour to apply to this item name
	 * @return ItemBuilder
	 */
	public ItemBuilder name(String itemName,Color colour)
	{
		Objects.requireNonNull(itemName,"ItemName cannot be null!");
		Objects.requireNonNull(colour,"Colour cannot be null!");
		ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(ColourUtils.applyColour(itemName,colour));
        stack.setItemMeta(stackMeta);
        return this;
	}
	
	/**
	 * Sets the name of this item stack
	 * @param itemName Name of this item stack
	 * @param hexColours Colour(s) to apply to this item name represented in hexadecimal
	 * @return ItemBuilder
	 */
	public ItemBuilder name(String itemName,String... hexColours)
	{
		Objects.requireNonNull(itemName,"ItemName cannot be null!");
		Objects.requireNonNull(hexColours,"Colour(s) cannot be null!");
		
		// Empty array? just set the name and no fancy colours for you!
		if(hexColours.length == 0)
		{
			return name(itemName);
		}
		
		// Array size of 1? Just set 1 colour
		if(hexColours.length == 1)
		{
			return name(itemName,ColourUtils.hexToColour(hexColours[0]));
		}
		
		// More colour? Gradient!
		return name(itemName,LinearGradient.of(hexColours));
	}
	
	/**
	 * Clears the name of this item stack
	 * @return ItemBuilder
	 */
	public ItemBuilder clearName()
	{
		ItemMeta stackMeta = stack.getItemMeta();
		stackMeta.setDisplayName(null);
		stack.setItemMeta(stackMeta);
		return this;
	}
	
	/**
	 * Sets the amount of this item from 1 - maximum stack size
	 * @param amount Amount of this item stack
	 * @return ItemBuilder
	 */
	public ItemBuilder amount(int amount)
	{
		Objects.requireNonNull(amount,"Amount cannot be null!");
		stack.setAmount(MathUtils.clamp(1,stack.getType().getMaxStackSize(),amount));
		return this;
	}
	
	/**
	 * Creates a lore builder for lore management
	 * @return LoreBuilder
	 */
	public LoreBuilder lore()
	{
		return new LoreBuilder(this);
	}
	
	/**
	 * Creates a lore builder for lore management
	 * @param lore Lore to append
	 * @return LoreBuilder
	 */
	public LoreBuilder lore(String lore)
	{
		return new LoreBuilder(this,lore);
	}
	
	/**
	 * Creates a lore builder for lore management
	 * @param lore List of lore to append
	 * @return LoreBuilder
	 */
	public LoreBuilder lore(List<String> lore)
	{
		return new LoreBuilder(this,lore);
	}
	
	/**
	 * Enchants the item stack, ignoring limits
	 * @param ench Enchantment
	 * @param lvl Enchantment level
	 * @return ItemBuilder
	 */
	public ItemBuilder enchant(Enchantment ench,int lvl)
	{
		Objects.requireNonNull(ench,"Enchantment cannot be null!");
		Objects.requireNonNull(lvl,"Enchantment level cannot be null!");
		
		if(stack.getType() == Material.ENCHANTED_BOOK)
		{
			EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) stack.getItemMeta();
			if(storageMeta.hasStoredEnchant(ench)) { storageMeta.removeStoredEnchant(ench); } 
			storageMeta.addStoredEnchant(ench,lvl,true);
	    	stack.setItemMeta(storageMeta);
	    	return this;
		}
		
		ItemMeta meta = stack.getItemMeta();
		if(meta.hasEnchant(ench)) { meta.removeEnchant(ench); }
		meta.addEnchant(ench,lvl,true);
    	stack.setItemMeta(meta);
    	return this;
	}
	
	/**
	 * Enchants the item stack, ignoring limits
	 * @param enchantMap Map of enchants to apply
	 * @return ItemBuilder
	 */
	public ItemBuilder enchant(Map<Enchantment,Integer> enchantMap)
	{
		Objects.requireNonNull(enchantMap,"Enchantment map cannot be null!");
		
		if(stack.getType() == Material.ENCHANTED_BOOK)
		{
			EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) stack.getItemMeta();
			
	    	for(Enchantment ench : storageMeta.getStoredEnchants().keySet())
	 		{
	    		if(storageMeta.hasStoredEnchant(ench)) { storageMeta.removeStoredEnchant(ench); } 
	    		storageMeta.addStoredEnchant(ench,enchantMap.get(ench),true);
	 		}
	    	
	    	stack.setItemMeta(storageMeta);
	    	return this;
		}
		
		ItemMeta meta = stack.getItemMeta();
		
		for(Enchantment ench : meta.getEnchants().keySet())
 		{
    		if(meta.hasEnchant(ench)) { meta.removeEnchant(ench); } 
    		meta.addEnchant(ench,enchantMap.get(ench),true);
 		}
		
    	stack.setItemMeta(meta);
    	return this;
	}
	
	/**
	 * Removes an enchant from the item stack
	 * @param ench Enchantment to remove
	 * @return ItemBuilder
	 */
	public ItemBuilder unenchant(Enchantment ench)
	{
		Objects.requireNonNull(ench,"Enchantment cannot be null!");
		
		if(stack.getType() == Material.ENCHANTED_BOOK)
		{
			EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) stack.getItemMeta();
			if(!storageMeta.hasStoredEnchants()) { return this; }
			if(storageMeta.hasStoredEnchant(ench)) { storageMeta.removeStoredEnchant(ench); } 
	    	stack.setItemMeta(storageMeta);
	    	return this;
		}
		
		ItemMeta meta = stack.getItemMeta();
		if(!meta.hasEnchants()) { return this; }
		if(meta.hasEnchant(ench)) { meta.removeEnchant(ench); }
    	stack.setItemMeta(meta);
    	return this;
	}
	
	/**
	 * Clears the item stack of all enchants
	 * @return ItemBuilder
	 */
	public ItemBuilder clearEnchants()
	{
		if(stack.getType() == Material.ENCHANTED_BOOK)
		{
			EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) stack.getItemMeta();
			if(!storageMeta.hasStoredEnchants()) { return this; }
			
	    	for(Enchantment enchant : storageMeta.getStoredEnchants().keySet())
	 		{
	    		storageMeta.removeStoredEnchant(enchant);
	 		}
			
	    	stack.setItemMeta(storageMeta);
	    	return this;
		}
		
		ItemMeta meta = stack.getItemMeta();
		if(!meta.hasEnchants()) { return this; }
		
		for(Enchantment enchant : meta.getEnchants().keySet())
 		{
 			meta.removeEnchant(enchant);
 		}
		
    	stack.setItemMeta(meta);
    	return this;
	}
	
	/**
	 * Makes the item glint or not regardless of enchantment
	 * <p>Set to null to clear the forced glint
	 * @param glint If the item stack should show an enchantment glint
	 * @return ItemBuilder
	 */
	public ItemBuilder glint(Boolean glint)
	{
		ItemMeta meta = stack.getItemMeta();
		meta.setEnchantmentGlintOverride(glint);
		stack.setItemMeta(meta);
		return this;
	}
	
	/**
	 * Adds item flags to this item stack
	 * @param flags ItemFlags to add
	 * @return ItemBuilder
	 */
	public ItemBuilder flag(ItemFlag... flags)
	{
		Objects.requireNonNull(flags,"Flags cannot be null!");
		ItemMeta meta = stack.getItemMeta();
    	meta.addItemFlags(flags);
    	stack.setItemMeta(meta);
    	return this;
	}
	
	/**
	 * Removes item flags from this item stack
	 * @param flags ItemFlags to remove
	 * @return ItemBuilder
	 */
	public ItemBuilder unflag(ItemFlag... flags)
	{
		Objects.requireNonNull(flags,"Flags cannot be null!");
		ItemMeta meta = stack.getItemMeta();
    	meta.removeItemFlags(flags);
    	stack.setItemMeta(meta);
    	return this;
	}
	
	/**
	 * Clears all flags on this item stack
	 * @return ItemBuilder
	 */
	public ItemBuilder clearFlags()
	{
		ItemMeta meta = stack.getItemMeta();
    	meta.removeItemFlags(ItemFlag.values());
    	stack.setItemMeta(meta);
    	return this;
	}
	
	/**
	 * Makes item stack unbreakable (can't loose durability)
	 * @param unbreak If the item stack should be unbreakable
	 * @return ItemBuilder
	 */
	public ItemBuilder unbreakable(boolean unbreak)
	{
    	ItemMeta meta = stack.getItemMeta();
    	meta.setUnbreakable(unbreak);
     	stack.setItemMeta(meta);
     	return this;
	}
	
	/**
	 * Sets the item stack to a player skull with a custom texture
	 * @param skullTexture base64 encoded texture. Example: 18813764b2abc94ec3c3bc67b9147c21be850cdf996679703157f4555997ea63a
	 * @return ItemBuilder
	 */
	public ItemBuilder skull(String skullTexture)
	{
	    return skull(skullTexture,UUID.randomUUID());
	}
	
	/**
	 * Sets the item stack to a player skull with a custom texture
	 * @param skullTexture base64 encoded texture. Example: 18813764b2abc94ec3c3bc67b9147c21be850cdf996679703157f4555997ea63a
	 * @param customUUID customUUID Uses a specific uuid to prevent skulls of the same texture from not stacking in a players inventory
	 * @return ItemBuilder
	 */
	public ItemBuilder skull(String skullTexture,UUID customUUID)
	{
		Objects.requireNonNull(skullTexture,"Skull texture cannot be null!");
		stack.setType(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) stack.getItemMeta();
		PlayerProfile profile = Bukkit.createPlayerProfile(customUUID);
	    PlayerTextures textures = profile.getTextures();
	    URL urlObject = null;
	    
	    try
	    {
	    	// The URL to the skin, for example: https://textures.minecraft.net/texture/18813764b2abc94ec3c3bc67b9147c21be850cdf996679703157f4555997ea63a
	        urlObject = URI.create("https://textures.minecraft.net/texture/" + skullTexture).toURL();
	        textures.setSkin(urlObject);
		    profile.setTextures(textures);
	    }
	    catch (Exception e)
	    {
	    	Logg.error("An error occured trying to create the texture URL 'https://textures.minecraft.net/texture/" + skullTexture + "'!",e);
	    }
		
	    skullMeta.setOwnerProfile(profile);
	    stack.setItemMeta(skullMeta);
	    return this;
	}
	
	/**
	 * Sets the item stack to a player skull with a custom texture of a players head
	 * @param playerUUID A players UUID
	 * @return ItemBuilder
	 */
	public ItemBuilder skull(UUID playerUUID)
	{
		Objects.requireNonNull(playerUUID);
		stack.setType(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) stack.getItemMeta();
		PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
	    PlayerTextures textures = profile.getTextures();
		MinecraftProfile mcProfile = MinecraftProfileCtrl.getPlayerProfile(playerUUID,false);
		URL urlObject = null;
		
		try
	    {
	    	// The URL to the skin, for example: https://textures.minecraft.net/texture/18813764b2abc94ec3c3bc67b9147c21be850cdf996679703157f4555997ea63a
	        urlObject = URI.create(mcProfile.getTexturesProperty().getSkinURL()).toURL();
			textures.setSkin(urlObject);
		    profile.setTextures(textures);
	    }
	    catch (Exception e)
	    {
	    	Logg.error("An error occured trying to create the texture URL '" + mcProfile.getTexturesProperty().getSkinURL() + "'!",e);
		    return this;
	    }
		
	    skullMeta.setOwnerProfile(profile);
	    stack.setItemMeta(skullMeta);
		return this;
	}
	
	/**
     * Adds an attribute modifier that applies to ALL equipment slots
     * @param att Attribute
     * @param op AttributeModifier Operation
     * @param amount Amount to modify
     * @return ItemBuilder
     */
    public ItemBuilder attmod(Attribute att,AttributeModifier.Operation op,double amount)
    {
    	ItemMeta meta = stack.getItemMeta();
    	meta.addAttributeModifier(att,new AttributeModifier(Dape.getNamespacedKey(),amount,op,EquipmentSlotGroup.ANY));
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Adds an attribute modifier that applies to one equipment slot
     * @param att Attribute
     * @param slot EquipmentSlot this attribute applies to
     * @param op AttributeModifier Operation
     * @param amount Amount to modify
     * @return ItemBuilder
     */
    public ItemBuilder attmod(Attribute att,EquipmentSlotGroup slot,AttributeModifier.Operation op,double amount)
    {
    	ItemMeta meta = stack.getItemMeta();
    	meta.addAttributeModifier(att,new AttributeModifier(Dape.getNamespacedKey(),amount,op,slot));
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Removes all attribute modifiers for an attribute
     * @param att Attribute
     * @return ItemBuilder
     */
    public ItemBuilder removeAttmod(Attribute att)
    {
    	ItemMeta meta = stack.getItemMeta();
    	
    	if(!meta.hasAttributeModifiers()) { return this; }
    	if(!meta.getAttributeModifiers().containsKey(att)) { return this; }
    	
    	meta.removeAttributeModifier(att);
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Removes an attribute modifier that applies to one equipment slot
     * @param att Attribute
     * @param slot EquipmentSlot this attribute applies to
     * @return ItemBuilder
     */
    public ItemBuilder removeAttmod(Attribute att,EquipmentSlotGroup slot)
    {
    	ItemMeta meta = stack.getItemMeta();
    	if(!meta.hasAttributeModifiers()) { return this; }
    	if(!meta.getAttributeModifiers().containsKey(att)) { return this; }
    	
    	for(AttributeModifier mod : meta.getAttributeModifiers().get(att))
		{
    		if(!mod.getSlotGroup().toString().equals(slot.toString())) { continue; }
    		meta.removeAttributeModifier(att,mod);
		}
    	
    	stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Clears attribute modifiers on this item stack
     * @return ItemBuilder
     */
    public ItemBuilder clearAttmods()
    {
    	ItemMeta meta = stack.getItemMeta();
    	if(!meta.hasAttributeModifiers()) { return this; }
    	meta.getAttributeModifiers().forEach((att,attmod) -> meta.removeAttributeModifier(att));    	
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
    public ItemBuilder durability(int dura)
    {
    	if(!(stack.getItemMeta() instanceof Damageable)) { return this; }
    	
		ItemMeta meta = stack.getItemMeta();
		int totalItemDamage = stack.getType().getMaxDurability();
		int clampedDamage = MathUtils.clamp(1,stack.getType().getMaxDurability(),dura);
		
		totalItemDamage = stack.getType().getMaxDurability() - clampedDamage;
		
		((Damageable) meta).setDamage(totalItemDamage);
		stack.setItemMeta(meta);
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
    public ItemBuilder deductDurability(int dura)
    {
    	if(!(stack.getItemMeta() instanceof Damageable damageable)) { return this; }
    	
    	int totalItemDamage = damageable.getDamage();
    	int clampedTotalItemDamage = MathUtils.clamp(1,stack.getType().getMaxDurability(),totalItemDamage + dura);
		
    	ItemMeta meta = stack.getItemMeta();
		((Damageable) meta).setDamage(clampedTotalItemDamage);
		stack.setItemMeta(meta);
    	return this;
    }
    
    /**
     * Sets the custom model data ID used to reference a different model/texture
     * that will be used to render the item with.
     * 
     * <p>Set to -1 to clear/reset the custom model data value
     * @param customModeDataNumber CustomModelData number
     * @return ItemBuilder
     */
    public ItemBuilder customModelData(int customModelDataNumber)
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
     * Sets data onto the item stack
     * @param key Key used to set, access, retrieve, and remove this data
     * @param value Value stored on the item stack
     * @return ItemBuilder
     */
    public ItemBuilder setData(String key,Object value)
    {
    	DataUtils.set(key,value,stack);
    	return this;
    }
    
    /**
     * Removes data from the item stack
     * @param key Key used to set, access, retrieve, and remove this data
     * @return ItemBuilder
     */
    public ItemBuilder removeData(String key)
    {
    	DataUtils.remove(key,stack);
    	return this;
    }
	
	/**
	 * Creates the item stack
	 * @return ItemStack
	 */
	public ItemStack create()
	{
		return stack == null ? new ItemStack(Material.GRASS_BLOCK) : stack;
	}
	
	public class LoreBuilder
	{
		private ItemBuilder builder;
		private List<String> loreList;
		
		/**
		 * Creates a new LoreBuilder
		 * @param builder ItemBuilder instance this lore builder was created from
		 */
		private LoreBuilder(ItemBuilder builder)
		{
			this.builder = builder;
			this.loreList = stack.getItemMeta().hasLore() ? stack.getItemMeta().getLore() : new ArrayList<>();
		}
		
		/**
		 * Creates a new LoreBuilder while appending lore
		 * @param builder ItemBuilder instance this lore builder was created from
		 * @param lore Lore to append
		 */
		private LoreBuilder(ItemBuilder builder,String lore)
		{
			this(builder);
			append(lore);
		}
		
		/**
		 * Creates a new LoreBuilder while appending a list of lore
		 * @param builder ItemBuilder instance this lore builder was created from
		 * @param lore List of lore to append
		 */
		private LoreBuilder(ItemBuilder builder,List<String> lore)
		{
			this(builder);
			appendAll(lore);
		}
		
		/**
		 * Appends list of lore to the item stack
		 * @param lore List of lore to add
		 * @return LoreBuilder
		 */
		public LoreBuilder appendAll(List<String> lore)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			lore.forEach(loreItem -> loreList.add(ColourUtils.translate(loreItem)));
			return this;
		}
		
		/**
		 * Appends list of lore to the item stack
		 * @param lore List of lore to add
		 * @param gradient Gradient to apply to this lore
		 * @return LoreBuilder
		 */
		public LoreBuilder appendAll(List<String> lore,LinearGradient gradient)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			lore.forEach(loreItem -> loreList.add(GradientUtils.applyGradient(loreItem,gradient)));
			return this;
		}
		
		/**
		 * Appends list of lore to the item stack
		 * @param lore List of lore to add
		 * @param colour Colour to apply to this item name
		 * @return LoreBuilder
		 */
		public LoreBuilder appendAll(List<String> lore,Color colour)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			lore.forEach(loreItem -> loreList.add(ColourUtils.applyColour(loreItem,colour)));
			return this;
		}
		
		/**
		 * Append lore to the item stack
		 * @param lore Lore to add
		 * @return LoreBuilder
		 */
		public LoreBuilder append(String lore)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			loreList.add(ColourUtils.translate(lore));
			return this;
		}
		
		/**
		 * Append lore to the item stack
		 * @param lore Lore to add
		 * @param gradient Gradient to apply to this lore
		 * @return LoreBuilder
		 */
		public LoreBuilder append(String lore,LinearGradient gradient)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			loreList.add(GradientUtils.applyGradient(lore,gradient));
			return this;
		}
		
		/**
		 * Append lore to the item stack
		 * @param lore Lore to add
		 * @param colour Colour to apply to this item name
		 * @return LoreBuilder
		 */
		public LoreBuilder append(String lore,Color colour)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			loreList.add(ColourUtils.applyColour(lore,colour));
			return this;
		}
		
		/**
		 * Appends lore to the top of the item stack
		 * @param lore Lore to add
		 * @return LoreBuilder
		 */
		public LoreBuilder appendFirst(String lore)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			loreList.addFirst(ColourUtils.translate(lore));
			return this;
		}
		
		/**
		 * Appends lore to the top of the item stack
		 * @param lore Lore to add
		 * @param gradient Gradient to apply to this lore
		 * @return LoreBuilder
		 */
		public LoreBuilder appendFirst(String lore,LinearGradient gradient)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			loreList.addFirst(GradientUtils.applyGradient(lore,gradient));
			return this;
		}
		
		/**
		 * Appends lore to the top of the item stack
		 * @param lore Lore to add
		 * @param colour Colour to apply to this item name
		 * @return LoreBuilder
		 */
		public LoreBuilder appendFirst(String lore,Color colour)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			loreList.addFirst(ColourUtils.applyColour(lore,colour));
			return this;
		}
		
		/**
		 * Appends lore to the bottom of the item stack
		 * @param lore Lore to add
		 * @return LoreBuilder
		 */
		public LoreBuilder appendLast(String lore)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			loreList.addLast(ColourUtils.translate(lore));
			return this;
		}
		
		/**
		 * Appends lore to the bottom of the item stack
		 * @param lore Lore to add
		 * @param gradient Gradient to apply to this lore
		 * @return LoreBuilder
		 */
		public LoreBuilder appendLast(String lore,LinearGradient gradient)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			loreList.addLast(GradientUtils.applyGradient(lore,gradient));
			return this;
		}
		
		/**
		 * Appends lore to the bottom of the item stack
		 * @param lore Lore to add
		 * @param colour Colour to apply to this item name
		 * @return LoreBuilder
		 */
		public LoreBuilder appendLast(String lore,Color colour)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			loreList.addLast(ColourUtils.applyColour(lore,colour));
			return this;
		}
		
		/**
		 * Wraps and appends lore to the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @return LoreBuilder
		 */
		public LoreBuilder wrap(String lore)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			loreList.addAll(StringUtils.wrapColouredText(ColourUtils.translateNoEscapeRemoval(lore)));
			return this;
		}
		
		/**
		 * Wraps and appends lore to the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param gradient Gradient to apply to this lore
		 * @return LoreBuilder
		 */
		public LoreBuilder wrap(String lore,LinearGradient gradient)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			Objects.requireNonNull(gradient,"Gradient cannot be null!");
			loreList.addAll(StringUtils.wrapColouredText(GradientUtils.applyGradient(lore,gradient)));
			return this;
		}
		
		/**
		 * Wraps and appends lore to the item stack in line lengths of 60
		 * @param lore lore Lore to add
		 * @param colour Colour to apply to this item name
		 * @return LoreBuilder
		 */
		public LoreBuilder wrap(String lore,Color colour)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			Objects.requireNonNull(colour,"Gradient cannot be null!");
			loreList.addAll(StringUtils.wrapColouredText(ColourUtils.applyColour(lore,colour)));
			return this;
		}
		
		/**
		 * Wraps and appends lore to the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param charsPerLine Number of characters to fit on 1 line of lore
		 * @return LoreBuilder
		 */
		public LoreBuilder wrap(String lore,int charsPerLine)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			loreList.addAll(StringUtils.wrapColouredText(ColourUtils.translateNoEscapeRemoval(lore),charsPerLine));
			return this;
		}
		
		/**
		 * Wraps and appends lore to the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param gradient Gradient to apply to this lore
		 * @param charsPerLine Number of characters to fit on 1 line of lore
		 * @return LoreBuilder
		 */
		public LoreBuilder wrap(String lore,LinearGradient gradient,int charsPerLine)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			Objects.requireNonNull(gradient,"Gradient cannot be null!");
			loreList.addAll(StringUtils.wrapColouredText(GradientUtils.applyGradient(lore,gradient),charsPerLine));
			return this;
		}
		
		/**
		 * Wraps and appends lore to the item stack in line lengths of 60
		 * @param lore lore Lore to add
		 * @param colour Colour to apply to this item name
		 * @param charsPerLine Number of characters to fit on 1 line of lore
		 * @return LoreBuilder
		 */
		public LoreBuilder wrap(String lore,Color colour,int charsPerLine)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			Objects.requireNonNull(colour,"Gradient cannot be null!");
			loreList.addAll(StringUtils.wrapColouredText(ColourUtils.applyColour(lore,colour),charsPerLine));
			return this;
		}
		
		/**
		 * Wraps and appends lore to the bottom of the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @return LoreBuilder
		 */
		public LoreBuilder wrapLast(String lore)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			for(String line : StringUtils.wrapColouredText(ColourUtils.translateNoEscapeRemoval(lore)))
			{
				appendLast(line);
			}
			
			return this;
		}
		
		/**
		 * Wraps and appends lore to the bottom of the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param gradient Gradient to apply to this lore
		 * @return LoreBuilder
		 */
		public LoreBuilder wrapLast(String lore,LinearGradient gradient)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			for(String line : StringUtils.wrapColouredText(GradientUtils.applyGradient(lore,gradient)))
			{
				appendLast(line);
			}
			
			return this;
		}
		
		/**
		 * Wraps and appends lore to the bottom of the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param colour Colour to apply to this item name
		 * @return LoreBuilder
		 */
		public LoreBuilder wrapLast(String lore,Color colour)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			for(String line : StringUtils.wrapColouredText(ColourUtils.applyColour(lore,colour)))
			{
				appendLast(line);
			}
			
			return this;
		}
		
		/**
		 * Wraps and appends lore to the bottom of the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param charsPerLine Number of characters to fit on 1 line of lore
		 * @return LoreBuilder
		 */
		public LoreBuilder wrapLast(String lore,int charsPerLine)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			for(String line : StringUtils.wrapColouredText(ColourUtils.translateNoEscapeRemoval(lore)))
			{
				appendLast(line);
			}
			
			return this;
		}
		
		/**
		 * Wraps and appends lore to the bottom of the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param gradient Gradient to apply to this lore
		 * @param charsPerLine Number of characters to fit on 1 line of lore
		 * @return LoreBuilder
		 */
		public LoreBuilder wrapLast(String lore,LinearGradient gradient,int charsPerLine)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			for(String line : StringUtils.wrapColouredText(GradientUtils.applyGradient(lore,gradient)))
			{
				appendLast(line);
			}
			
			return this;
		}
		
		/**
		 * Wraps and appends lore to the bottom of the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param colour Colour to apply to this item name
		 * @param charsPerLine Number of characters to fit on 1 line of lore
		 * @return LoreBuilder
		 */
		public LoreBuilder wrapLast(String lore,Color colour,int charsPerLine)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			for(String line : StringUtils.wrapColouredText(ColourUtils.applyColour(lore,colour)))
			{
				appendLast(line);
			}
			
			return this;
		}
		
		/**
		 * Wraps and appends lore to the top of the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @return LoreBuilder
		 */
		public LoreBuilder wrapFirst(String lore)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			for(String line : StringUtils.wrapColouredText(ColourUtils.translateNoEscapeRemoval(lore)))
			{
				appendFirst(line);
			}
			
			return this;
		}
		
		/**
		 * Wraps and appends lore to the top of the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param gradient Gradient to apply to this lore
		 * @return LoreBuilder
		 */
		public LoreBuilder wrapFirst(String lore,LinearGradient gradient)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			for(String line : StringUtils.wrapColouredText(GradientUtils.applyGradient(lore,gradient)))
			{
				appendFirst(line);
			}
			
			return this;
		}
		
		/**
		 * Wraps and appends lore to the top of the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param colour Colour to apply to this item name
		 * @return LoreBuilder
		 */
		public LoreBuilder wrapFirst(String lore,Color colour)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			for(String line : StringUtils.wrapColouredText(ColourUtils.applyColour(lore,colour)))
			{
				appendFirst(line);
			}
			
			return this;
		}
		
		/**
		 * Wraps and appends lore to the top of the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param charsPerLine Number of characters to fit on 1 line of lore
		 * @return LoreBuilder
		 */
		public LoreBuilder wrapFirst(String lore,int charsPerLine)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			for(String line : StringUtils.wrapColouredText(ColourUtils.translateNoEscapeRemoval(lore)))
			{
				appendFirst(line);
			}
			
			return this;
		}
		
		/**
		 * Wraps and appends lore to the top of the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param gradient Gradient to apply to this lore
		 * @param charsPerLine Number of characters to fit on 1 line of lore
		 * @return LoreBuilder
		 */
		public LoreBuilder wrapFirst(String lore,LinearGradient gradient,int charsPerLine)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			for(String line : StringUtils.wrapColouredText(GradientUtils.applyGradient(lore,gradient)))
			{
				appendFirst(line);
			}
			
			return this;
		}
		
		/**
		 * Wraps and appends lore to the top of the item stack in line lengths of 60
		 * @param lore Lore to add
		 * @param colour Colour to apply to this item name
		 * @param charsPerLine Number of characters to fit on 1 line of lore
		 * @return LoreBuilder
		 */
		public LoreBuilder wrapFirst(String lore,Color colour,int charsPerLine)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			for(String line : StringUtils.wrapColouredText(ColourUtils.applyColour(lore,colour)))
			{
				appendFirst(line);
			}
			
			return this;
		}
		
		/**
		 * Removes lore from the item stack by line index. 0 is top.
		 * @param index Line index to remove lore at
		 * @return LoreBuilder
		 */
		public LoreBuilder remove(int index)
		{
			if(index < 0 || index >= loreList.size())
			{
				Logg.warn("Cannot remove lore from ItemStack as " + index + " is out of range!");
				return this;
			}
			
			loreList.remove(index);
			return this;
		}
		
		/**
		 * Removes lore from the item stack by contents. If contents passed matches a line in lore, it will be removed.
		 * @param lore Lore to remove from item stack
		 * @return LoreBuilder
		 */
		public LoreBuilder remove(String lore)
		{
			Objects.requireNonNull(lore,"Lore cannot be null!");
			Iterator<String> it = loreList.iterator();
			String strippedLore = ColourUtils.strip(lore);
			
			while(it.hasNext())
			{
				String strippedLoreOnItem = ColourUtils.strip(it.next());
				
				if(strippedLore.equals(strippedLoreOnItem))
				{
					it.remove();
					return this;
				}
			}
			
			return this;
		}
		
		/**
		 * Replaces lore from the item stack with new lore by line index.
		 * @param index Line index to replace lore at
		 * @param lore New lore to take place of the existing lore.
		 * @return LoreBuilder
		 */
		public LoreBuilder replace(int index,String newLore)
		{
			Objects.requireNonNull(index,"Index cannot be null!");
			Objects.requireNonNull(newLore,"Lore cannot be null!");
			
			if(index < 0 || index >= loreList.size())
			{
				Logg.warn("Cannot remove lore from ItemStack as " + index + " is out of range!");
				return this;
			}
			
			loreList.set(index,newLore);
			return this;
		}
		
		public LoreBuilder clear()
		{
			loreList.clear();
			return this;
		}
		
		/**
		 * Commit changes made to lore onto the item stack
		 * @return ItemBuilder
		 */
		public ItemBuilder commit()
		{
			ItemMeta meta = stack.getItemMeta();
			meta.setLore(loreList);
			stack.setItemMeta(meta);
			return builder;
		}
		
		/**
		 * Convenient method that commits lore and creates the ItemStack,
		 * avoiding an extra method call
		 * @return ItemStack
		 */
		public ItemStack create()
		{
			return commit().create();
		}
	}
}