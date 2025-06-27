package coffee.dape.utils.tools;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import coffee.dape.utils.ItemUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.structs.Pair;

/**
 * @author Laeven
 * A convenient class to serialise common objects into Json
 */
public class Serialise
{
	public static JsonObject vector(Vector vec)
	{
		Objects.requireNonNull(vec,"Vector cannot be null!");
		
		JsonObject obj = new JsonObject();
		obj.addProperty("x",vec.getX());
		obj.addProperty("y",vec.getY());
		obj.addProperty("z",vec.getZ());
		return obj;
	}
	
	public static JsonObject location(Location loc)
	{
		Objects.requireNonNull(loc,"Location cannot be null!");
		
		JsonObject obj = new JsonObject();
		obj.addProperty("x",loc.getX());
		obj.addProperty("y",loc.getY());
		obj.addProperty("z",loc.getZ());
		obj.addProperty("pitch",loc.getPitch());
		obj.addProperty("yaw",loc.getYaw());
		obj.addProperty("world",loc.getWorld().getName());
		return obj;
	}
	
	@SuppressWarnings("deprecation")
	public static JsonArray potionEffects(Collection<PotionEffect> potionEffects)
	{
		Objects.requireNonNull(potionEffects,"PotionEffects cannot be null!");
		
		JsonArray arr = new JsonArray();
		for(PotionEffect effect : potionEffects)
		{
			JsonObject obj = new JsonObject();
			
			obj.addProperty("type_namespace",effect.getType().getKey().getNamespace());
			obj.addProperty("type_key",effect.getType().getKey().getKey());
			obj.addProperty("duration",effect.getDuration());
			obj.addProperty("amplifier",effect.getAmplifier());
			obj.addProperty("is_ambient",effect.isAmbient());
			obj.addProperty("has_particles",effect.hasParticles());
			obj.addProperty("has_icon",effect.hasIcon());
			
			arr.add(obj);
		}
		
		return arr;
	}
	
	/**
	 * Serialise advancements
	 * <p>
	 * Note that this will not capture the date when an advancement is obtained as there is no way to set this in the Spigot API
	 * @param advancements
	 * @return
	 */
	public static JsonArray advancements(Map<NamespacedKey,Collection<String>> advancements)
	{
		Objects.requireNonNull(advancements,"Advancements cannot be null!");
		
		JsonArray arr = new JsonArray();
		for(Entry<NamespacedKey,Collection<String>> advancement : advancements.entrySet())
		{
			JsonObject obj = new JsonObject();
			Collection<String> awardedCriteria = advancement.getValue();
			
			obj.addProperty("namespace",advancement.getKey().getNamespace());
			obj.addProperty("key",advancement.getKey().getKey());
			
			JsonArray criteriaArr = new JsonArray();
			for(String criteria : awardedCriteria)
			{
				criteriaArr.add(criteria);
			}
			
			obj.add("criteria",criteriaArr);			
			arr.add(obj);
		}
		
		return arr;
	}
	
	public static JsonArray attributes(Map<String,Pair<Double,Collection<AttributeModifier>>> attributes)
	{
		Objects.requireNonNull(attributes,"Advancements cannot be null!");
		
		JsonArray arr = new JsonArray();
		for(Entry<String,Pair<Double,Collection<AttributeModifier>>> attributeEntry : attributes.entrySet())
		{
			JsonObject obj = new JsonObject();
			Pair<Double,Collection<AttributeModifier>> attribute = attributeEntry.getValue();
			
			obj.addProperty("key",attributeEntry.getKey());
			obj.addProperty("base_value",attribute.getValueA());
			
			JsonArray modArr = new JsonArray();
			for(AttributeModifier mod : attribute.getValueB())
			{
				JsonObject modObj = new JsonObject();
				
				modObj.addProperty("namespace",mod.getKey().getNamespace());
				modObj.addProperty("key",mod.getKey().getKey());
				modObj.addProperty("operation",mod.getOperation().toString());
				modObj.addProperty("amount",mod.getAmount());
				
				if(mod.getSlotGroup() != null)
				{
					modObj.addProperty("equipment_group",mod.getSlotGroup().toString());
				}
			}
			
			obj.add("modifiers",modArr);			
			arr.add(obj);
		}
		
		return arr;
	}
	
	public JsonObject inventory(Inventory inv)
	{
		JsonObject obj = new JsonObject();
		
		obj.addProperty("inventory_type",inv.getType().toString());
		obj.addProperty("inventory_size",inv.getSize());
		
		for(int i = 0; i < inv.getSize(); i++)
		{
			// Some slots may contain nothing
			if(ItemUtils.isNullOrAir(inv.getItem(i))) { continue; }
			
			String encodedStack = ItemUtils.toBase64(inv.getItem(i));
			if(encodedStack == null)
			{
				Logg.error("Could not serialise inventory " + inv.getType().toString() + " Item stack " + inv.getItem(i).getType());
				continue;
			}
			
			obj.addProperty(String.valueOf(i),encodedStack);
		}
		
		return obj;
	}
}
