package coffee.dape.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.craftbukkit.v1_21_R4.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ChatBuilder
{
	private List<BaseComponent> messages = new ArrayList<>();
	private TextComponent currentMessage = new TextComponent();
	
	public ChatBuilder(String msg)
	{
		setMessage(msg);
	}
	
	public ChatBuilder() {}
	
	public ChatBuilder addComponents(BaseComponent[] components)
	{
		messages.addAll(Arrays.asList(components));
		return this;
	}
	
	public ChatBuilder addComponents(List<BaseComponent> components)
	{
		messages.addAll(components);
		return this;
	}
	
	public ChatBuilder setMessage(String msg)
	{
		// Legacy is used for gradients and old-school & colours
		BaseComponent msgComponent = TextComponent.fromLegacy(ColourUtils.translate(msg));
		currentMessage.addExtra(msgComponent);
		return this;
	}
	
	public ChatBuilder setClickRunCommandEvent(String command)
	{
		currentMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,command));
		return this;
	}
	
	public ChatBuilder setClickCopyToClipboardEvent(String text)
	{
		currentMessage.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,text));
		return this;
	}
	
	public ChatBuilder setClickOpenURLEvent(String url)
	{
		currentMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,url));
		return this;
	}
	
	public ChatBuilder setClickSuggestCommandEvent(String command)
	{
		currentMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,command));
		return this;
	}
	
	public ChatBuilder setHoverShowTextEvent(String hoverText)
	{
		ComponentBuilder comp = new ComponentBuilder();
		comp.appendLegacy(ColourUtils.translate(hoverText));
		currentMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new Text(comp.create())));
		return this;
	}
	
	public ChatBuilder setHoverShowTextEvent(List<String> hoverTextList)
	{
		ComponentBuilder comp = new ComponentBuilder();
		
		for(String s : hoverTextList)
		{
			comp.appendLegacy(ColourUtils.translate(s));
		}
		
		currentMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new Text(comp.create())));
		return this;
	}
	
	public ChatBuilder setHoverShowEntityEvent(Entity e)
	{
		BaseComponent name = (e.getCustomName() != null ? TextComponent.fromLegacy(ColourUtils.translate(e.getCustomName())) : TextComponent.fromLegacy(StringUtils.capitaliseFirstLetter(e.getType().toString().toLowerCase().replace("_"," "))));
		String type = "minecraft:" + StringUtils.toSnakecase(e.getType().toString());
		String id = e.getUniqueId().toString();
		
		currentMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY,new net.md_5.bungee.api.chat.hover.content.Entity(type,id,name)));
		return this;
	}
	
	// Does not fucking work, idk why it's so damn hard to show an item in chat -.-
	// You're suppose to get ItemTag.ofNbt(JSON STRING) but idk how to get json string of an ItemStack
	// Thanks md5... :/
	@Deprecated
	public ChatBuilder setHoverShowItemEvent(ItemStack stack)
	{
		String materialId = "minecraft:" + StringUtils.toSnakecase(stack.getType().toString());
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
		//NBTTagCompound nbtTag = nmsStack.;
		//String t = s.q(); (1.20.4)
		String nbt = nmsStack.toString();
		//System.out.println(t);
		//System.out.println(s.toString());
		ItemTag tag = ItemTag.ofNbt(nbt);
		
		currentMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM,new net.md_5.bungee.api.chat.hover.content.Item(materialId,1,tag)));
		
		return this;
	}
	
	public ChatBuilder nextComp()
	{
		messages.add(currentMessage);
		currentMessage = new TextComponent();
		return this;
	}
	
	public BaseComponent[] getResult()
	{
		List<BaseComponent> finalMessage = new ArrayList<>(messages);
		if(currentMessage.toPlainText().length() > 0) { finalMessage.add(currentMessage); }
		return finalMessage.toArray(new BaseComponent[0]);
	}
}
