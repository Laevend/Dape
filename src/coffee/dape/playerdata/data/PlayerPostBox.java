package coffee.dape.playerdata.data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.BundleMeta;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.postbox.PostItem;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.InputUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.ItemUtils;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.json.PersistJson;
import coffee.dape.utils.tools.Deserialise;

/**
 * 
 * @author Laeven
 *
 */
public class PlayerPostBox implements PersistJson
{
	private PlayerData parent;
	
	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm");
	private static final Comparator<PostItem> localDateTimeComparator = (o1,o2) -> o1.getDateSent().compareTo(o2.getDateSent());
	private SortedSet<PostItem> inbox = new TreeSet<>(localDateTimeComparator);
	private SortedSet<PostItem> sent = new TreeSet<>(localDateTimeComparator);
	private SortedSet<PostItem> drafts = new TreeSet<>(localDateTimeComparator);
	
	public PlayerPostBox(PlayerData data)
	{
		this.parent = data;
	}

	public PlayerData getParent()
	{
		return parent;
	}

	public void setParent(PlayerData parent)
	{
		this.parent = parent;
	}

	public SortedSet<PostItem> getInbox()
	{
		return inbox;
	}

	public void setInbox(SortedSet<PostItem> inbox)
	{
		this.inbox = inbox;
	}

	public SortedSet<PostItem> getSent()
	{
		return sent;
	}

	public void setSent(SortedSet<PostItem> sent)
	{
		this.sent = sent;
	}

	public SortedSet<PostItem> getDrafts()
	{
		return drafts;
	}

	public void setDrafts(SortedSet<PostItem> drafts)
	{
		this.drafts = drafts;
	}
	
	public void addInboxPost(PostItem item)
	{
		this.inbox.add(item);
	}
	
	public PostItem getInboxPost(UUID id)
	{
		Iterator<PostItem> it = this.drafts.iterator();
		
		while(it.hasNext())
		{
			PostItem postItem = it.next();
			
			if(postItem.getId().equals(id))
			{
				return postItem;
			}
		}
		
		return null;
	}
	
	public void removeInboxPost(UUID id)
	{
		Iterator<PostItem> it = this.inbox.iterator();
		
		while(it.hasNext())
		{
			if(it.next().getId().equals(id))
			{
				it.remove();
				break;
			}
		}
	}
	
	public void addSentPost(PostItem item)
	{
		this.sent.add(item);
	}
	
	public PostItem getSentPost(UUID id)
	{
		Iterator<PostItem> it = this.sent.iterator();
		
		while(it.hasNext())
		{
			PostItem postItem = it.next();
			
			if(postItem.getId().equals(id))
			{
				return postItem;
			}
		}
		
		return null;
	}
	
	public void removeSentPost(UUID id)
	{
		Iterator<PostItem> it = this.sent.iterator();
		
		while(it.hasNext())
		{
			if(it.next().getId().equals(id))
			{
				it.remove();
				break;
			}
		}
	}
	
	public void addDraftPost(PostItem item)
	{
		this.drafts.add(item);
	}
	
	public PostItem getDraftPost(UUID id)
	{
		Iterator<PostItem> it = this.drafts.iterator();
		
		while(it.hasNext())
		{
			PostItem postItem = it.next();
			
			if(postItem.getId().equals(id))
			{
				return postItem;
			}
		}
		
		return null;
	}
	
	public void removeDraftPost(UUID id)
	{
		Iterator<PostItem> it = this.drafts.iterator();
		
		while(it.hasNext())
		{
			if(it.next().getId().equals(id))
			{
				it.remove();
				break;
			}
		}
	}

	public static final String INBOX = "inbox";
	public static final String SENT = "sent";
	public static final String DRAFTS = "drafts";
	
	public static final String MESSAGES = "messages";
	public static final String PARCELS = "parcels";
	
	@Override
	public JsonObject serialise() throws SerialiseException
	{
		JsonObject obj = new JsonObject();
		
		JsonObject inbox = new JsonObject();
		JsonArray inbox_messages = new JsonArray();
		JsonArray inbox_parcels = new JsonArray();
		for(PostItem item : this.inbox)
		{
			switch(item)
			{
				case Message message -> inbox_messages.add(message.serialise());
				case Parcel parcel -> inbox_parcels.add(parcel.serialise());
				default -> throw new IllegalArgumentException("Unexpected value: " + item);
			}
		}
		
		inbox.add(MESSAGES,inbox_messages);
		inbox.add(PARCELS,inbox_parcels);
		obj.add(INBOX,inbox);
		
		JsonObject sent = new JsonObject();
		JsonArray sent_messages = new JsonArray();
		JsonArray sent_parcels = new JsonArray();
		for(PostItem item : this.sent)
		{
			switch(item)
			{
				case Message message -> sent_messages.add(message.serialise());
				case Parcel parcel -> sent_parcels.add(parcel.serialise());
				default -> throw new IllegalArgumentException("Unexpected value: " + item);
			}
		}
		
		sent.add(MESSAGES,sent_messages);
		sent.add(PARCELS,sent_parcels);
		obj.add(SENT,sent);
		
		JsonObject drafts = new JsonObject();
		JsonArray drafts_messages = new JsonArray();
		JsonArray drafts_parcels = new JsonArray();
		for(PostItem item : this.drafts)
		{
			switch(item)
			{
				case Message message -> drafts_messages.add(message.serialise());
				case Parcel parcel -> drafts_parcels.add(parcel.serialise());
				default -> throw new IllegalArgumentException("Unexpected value: " + item);
			}
		}
		
		drafts.add(MESSAGES,drafts_messages);
		drafts.add(PARCELS,drafts_parcels);
		obj.add(DRAFTS,drafts);
		
		return obj;
	}

	@Override
	public void deserialise(JsonObject obj) throws DeserialiseException
	{
		Deserialise.assertProperty(INBOX,Deserialise.Type.JSON_OBJECT,obj);
		
		this.inbox.clear();
		JsonObject inbox = Deserialise.assertAndGetProperty(INBOX,Deserialise.Type.JSON_OBJECT,obj).getAsJsonObject();
		JsonArray messages = Deserialise.assertAndGetProperty(MESSAGES,Deserialise.Type.JSON_ARRAY,inbox).getAsJsonArray();
		JsonArray parcels = Deserialise.assertAndGetProperty(PARCELS,Deserialise.Type.JSON_ARRAY,inbox).getAsJsonArray();
		
		for(JsonElement message : messages)
		{
			Deserialise.assertType(Deserialise.Type.JSON_OBJECT,message);
			JsonObject nextMessage = message.getAsJsonObject();
			PostItem nextPostItem = new Message(nextMessage);
			this.inbox.add(nextPostItem);
		}
		
		for(JsonElement parcel : parcels)
		{
			Deserialise.assertType(Deserialise.Type.JSON_OBJECT,parcel);
			JsonObject nextParcel = parcel.getAsJsonObject();
			PostItem nextPostItem = new Parcel(nextParcel);
			this.inbox.add(nextPostItem);
		}
		
		this.sent.clear();
		JsonObject sent = Deserialise.assertAndGetProperty(INBOX,Deserialise.Type.JSON_OBJECT,obj).getAsJsonObject();
		messages = Deserialise.assertAndGetProperty(MESSAGES,Deserialise.Type.JSON_ARRAY,sent).getAsJsonArray();
		parcels = Deserialise.assertAndGetProperty(PARCELS,Deserialise.Type.JSON_ARRAY,sent).getAsJsonArray();
		
		for(JsonElement message : messages)
		{
			Deserialise.assertType(Deserialise.Type.JSON_OBJECT,message);
			JsonObject nextMessage = message.getAsJsonObject();
			PostItem nextPostItem = new Message(nextMessage);
			this.sent.add(nextPostItem);
		}
		
		for(JsonElement parcel : parcels)
		{
			Deserialise.assertType(Deserialise.Type.JSON_OBJECT,parcel);
			JsonObject nextParcel = parcel.getAsJsonObject();
			PostItem nextPostItem = new Parcel(nextParcel);
			this.sent.add(nextPostItem);
		}
		
		this.drafts.clear();
		JsonObject drafts = Deserialise.assertAndGetProperty(INBOX,Deserialise.Type.JSON_OBJECT,obj).getAsJsonObject();
		messages = Deserialise.assertAndGetProperty(MESSAGES,Deserialise.Type.JSON_ARRAY,drafts).getAsJsonArray();
		parcels = Deserialise.assertAndGetProperty(PARCELS,Deserialise.Type.JSON_ARRAY,drafts).getAsJsonArray();
		
		for(JsonElement message : messages)
		{
			Deserialise.assertType(Deserialise.Type.JSON_OBJECT,message);
			JsonObject nextMessage = message.getAsJsonObject();
			PostItem nextPostItem = new Message(nextMessage);
			this.drafts.add(nextPostItem);
		}
		
		for(JsonElement parcel : parcels)
		{
			Deserialise.assertType(Deserialise.Type.JSON_OBJECT,parcel);
			JsonObject nextParcel = parcel.getAsJsonObject();
			PostItem nextPostItem = new Parcel(nextParcel);
			this.drafts.add(nextPostItem);
		}
	}
	
	public class Message implements PostItem, PersistJson
	{	
		private UUID messageId = UUID.randomUUID();
		private UUID sender;
		private LocalDateTime dateSent = LocalDateTime.now();
		private String subject;
		private Map<Integer,String> pages = new HashMap<>();
		private boolean read = false;
		
		private ItemStack stackCache = null;
		private ItemStack messageCache = null;
		
		public Message(UUID sender,LocalDateTime dateSent,String subject,Map<Integer,String> pages)
		{
			this.sender = sender;
			this.dateSent = dateSent;
			this.subject = subject;
			this.pages = pages;
		}
		
		public Message(JsonObject obj) throws DeserialiseException
		{
			this.deserialise(obj);
		}

		@Override
		public UUID getId()
		{
			return messageId;
		}
		
		@Override
		public UUID getSender()
		{
			return sender;
		}
		
		@Override
		public void setSender(UUID sender)
		{
			this.sender = sender;
		}
		
		@Override
		public LocalDateTime getDateSent()
		{
			return dateSent;
		}
		
		@Override
		public void setDateSent(LocalDateTime dateSent)
		{
			this.dateSent = dateSent;
		}

		public String getSubject()
		{
			return subject;
		}

		public void setSubject(String subject)
		{
			this.subject = subject;
		}

		public Map<Integer, String> getPages()
		{
			return pages;
		}

		public void setPages(Map<Integer, String> pages)
		{
			this.pages = pages;
		}

		public boolean isRead()
		{
			return read;
		}

		public void setRead(boolean read)
		{
			this.read = read;
		}

		public void readMessage(Player p)
		{
			if(messageCache != null) { p.openBook(messageCache); return; }
			
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			BookMeta meta = (BookMeta) book.getItemMeta();
			meta.setAuthor(PlayerUtils.getName(sender));
			meta.setTitle(subject.substring(0,subject.length() > 32 ? 32 : subject.length()));
			meta.setGeneration(Generation.ORIGINAL);
			
			for(Entry<Integer,String> page : pages.entrySet())
			{
				meta.setPage(page.getKey(),page.getValue());
			}
			
			book.setItemMeta(meta);
			messageCache = book;
			p.openBook(book);
		}
		
		@Override
		public ItemStack getStack()
		{
			if(stackCache != null) { return stackCache; }
			stackCache = ItemBuilder.of(read ? Material.BOOK : Material.ENCHANTED_BOOK)
					.name(subject,read ? ColourUtils.TEXT_SUCCESS : ColourUtils.TEAL)
					.lore()
					.append("by " + PlayerUtils.getName(sender),ColourUtils.TEXT)
					.append("on " + dateFormat.format(dateSent))
					.append("")
					.wrap(InputUtils.LeftClick.describeAction("Read a message"))
					.wrap(InputUtils.ShiftRightClick.describeAction("Delete a message"))
					.wrap(InputUtils.MiddleClick.describeAction("Mark as unread"))
					.commit()
					.setData("id",messageId.toString())
					.setData("type","message")
					.create();
			
			return stackCache;
		}

		@Override
		public boolean isItemComponentType() { return false; }
		@Override
		public ChaosComponent getComponent() { return null; }

		public static final String SENDER = "sender";
		public static final String DATE_SENT = "date_sent";
		public static final String SUBJECT = "subject";
		public static final String READ = "read";
		public static final String PAGES = "pages";
		
		@Override
		public JsonObject serialise() throws SerialiseException
		{
			JsonObject obj = new JsonObject();
			
			obj.addProperty(SENDER,sender.toString());
			obj.addProperty(DATE_SENT,dateSent.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
			obj.addProperty(SUBJECT,subject);
			obj.addProperty(READ,read);
			
			JsonObject pages = new JsonObject();
			
			for(Entry<Integer,String> pageEntry : this.pages.entrySet())
			{
				pages.addProperty(String.valueOf(pageEntry.getKey()),pageEntry.getValue());
			}
			
			obj.add(PAGES,pages);
			return obj;
		}

		@Override
		public void deserialise(JsonObject obj) throws DeserialiseException
		{
			Deserialise.assertProperty(SENDER,Deserialise.Type.STRING,obj);
			Deserialise.assertProperty(DATE_SENT,Deserialise.Type.NUMBER,obj);
			Deserialise.assertProperty(SUBJECT,Deserialise.Type.STRING,obj);
			Deserialise.assertProperty(READ,Deserialise.Type.BOOLEAN,obj);
			Deserialise.assertProperty(PAGES,Deserialise.Type.JSON_OBJECT,obj);
			
			sender = Deserialise.uuid(obj.get(SENDER));
			dateSent = LocalDateTime.ofInstant(Instant.ofEpochMilli(obj.get(DATE_SENT).getAsLong()),ZoneId.systemDefault());
			subject = obj.get(SUBJECT).getAsString();
			read = obj.get(READ).getAsBoolean();
			pages.clear();
			
			for(Entry<String,JsonElement> pageEntry : obj.get(PAGES).getAsJsonObject().entrySet())
			{
				JsonElement pageNumber = JsonParser.parseString("{\"page_number\": " + pageEntry.getKey() + "}").getAsJsonObject().get("page_number");
				Deserialise.assertType(Deserialise.Type.NUMBER,pageNumber);
				Deserialise.assertType(Deserialise.Type.STRING,pageEntry.getValue());
				pages.put(Integer.valueOf(pageEntry.getKey()),pageEntry.getValue().getAsString());
			}
		}
	}
	
	public class Parcel implements PostItem, PersistJson
	{
		private UUID parcelId = UUID.randomUUID();
		private UUID sender;
		private LocalDateTime dateSent = LocalDateTime.now();
		private String title;
		private BundleMeta bundle;
		private Material bundleType;
		
		private ItemStack stackCache = null;
		
		public Parcel(UUID sender,LocalDateTime dateSent,String title,ItemStack parcel)
		{
			if(!bundleTypes.contains(parcel.getType()))
			{
				throw new IllegalArgumentException("Parcel is not a bundle type!");
			}
			
			bundleType = parcel.getType();
			bundle = (BundleMeta) parcel.getItemMeta();
		}
		
		public Parcel(JsonObject obj) throws DeserialiseException
		{
			this.deserialise(obj);
		}
		
		@Override
		public UUID getId()
		{
			return parcelId;
		}

		@Override
		public UUID getSender()
		{
			return sender;
		}

		@Override
		public void setSender(UUID sender)
		{
			this.sender = sender;
		}

		@Override
		public LocalDateTime getDateSent()
		{
			return dateSent;
		}

		@Override
		public void setDateSent(LocalDateTime dateSent)
		{
			this.dateSent = dateSent;
		}

		public String getTitle()
		{
			return title;
		}

		public void setTitle(String title)
		{
			this.title = title;
		}
		
		@Override
		public ItemStack getStack()
		{
			if(stackCache != null) { return stackCache; }
			stackCache = ItemBuilder.of(bundleType)
					.name(title,ColourUtils.VISTA_BLUE)
					.lore()
					.append("by " + PlayerUtils.getName(sender),ColourUtils.TEXT)
					.append("on " + dateFormat.format(dateSent))
					.append("")
					.wrap(InputUtils.LeftClick.describeAction("Open parcel"))
					.wrap(InputUtils.ShiftRightClick.describeAction("Delete parcel"))
					.commit()
					.setData("id",parcelId.toString())
					.setData("type","parcel")
					.create();
			
			return stackCache;
		}

		@Override
		public boolean isItemComponentType() { return false; }
		@Override
		public ChaosComponent getComponent() { return null; }
		
		public static final String SENDER = "sender";
		public static final String DATE_SENT = "date_sent";
		public static final String TITLE = "title";
		public static final String PARCEL = "parcel";
		public static final String BUNDLE_TYPE = "bundle_type";
		public static final String PARCEL_CONTENTS = "parcel_contents";
		
		@Override
		public JsonObject serialise() throws SerialiseException
		{
			JsonObject obj = new JsonObject();
			
			obj.addProperty(SENDER,sender.toString());
			obj.addProperty(DATE_SENT,dateSent.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
			obj.addProperty(TITLE,title);
			
			JsonObject parcel = new JsonObject();
			
			parcel.addProperty(BUNDLE_TYPE,bundleType.toString());
			
			JsonArray parcelContents = new JsonArray();
			
			for(ItemStack item : bundle.getItems())
			{
				String encodedItem = ItemUtils.toBase64(item);
				parcelContents.add(encodedItem);
			}
			
			parcel.add(PARCEL_CONTENTS,parcelContents);
			obj.add(PARCEL,parcel);
			return obj;
		}

		@Override
		public void deserialise(JsonObject obj) throws DeserialiseException
		{
			Deserialise.assertProperty(SENDER,Deserialise.Type.STRING,obj);
			Deserialise.assertProperty(DATE_SENT,Deserialise.Type.NUMBER,obj);
			Deserialise.assertProperty(TITLE,Deserialise.Type.STRING,obj);
			Deserialise.assertProperty(PARCEL,Deserialise.Type.JSON_OBJECT,obj);
			
			sender = Deserialise.uuid(obj.get(SENDER));
			dateSent = LocalDateTime.ofInstant(Instant.ofEpochMilli(obj.get(DATE_SENT).getAsLong()),ZoneId.systemDefault());
			title = obj.get(TITLE).getAsString();			
			JsonObject parcel = obj.get(PARCEL).getAsJsonObject();
			
			Deserialise.assertProperty(BUNDLE_TYPE,Deserialise.Type.STRING,parcel);
			Deserialise.assertProperty(PARCEL_CONTENTS,Deserialise.Type.JSON_ARRAY,parcel);
			Deserialise.assertEnum(BUNDLE_TYPE,Material.class);
			
			Material bundleType = Material.valueOf(parcel.get(BUNDLE_TYPE).getAsString());
			
			if(!bundleTypes.contains(bundleType))
			{
				throw new DeserialiseException("Parcel is not a bundle type!");
			}
			
			ItemStack bundleItem = new ItemStack(bundleType);
			bundle = (BundleMeta) bundleItem.getItemMeta();
			
			JsonArray parcelContents = parcel.get(PARCEL_CONTENTS).getAsJsonArray();
			
			for(JsonElement encodedItem : parcelContents)
			{
				Deserialise.assertType(Deserialise.Type.STRING,encodedItem);
				ItemStack stack = ItemUtils.fromBase64(encodedItem.getAsString());
				if(stack == null) { continue; }
				bundle.addItem(stack);
			}
		}
		
		private static final Set<Material> bundleTypes = Set.of(
			    Material.BUNDLE,
			    Material.WHITE_BUNDLE,
			    Material.ORANGE_BUNDLE,
			    Material.MAGENTA_BUNDLE,
			    Material.LIGHT_BLUE_BUNDLE,
			    Material.YELLOW_BUNDLE,
			    Material.LIME_BUNDLE,
			    Material.PINK_BUNDLE,
			    Material.GRAY_BUNDLE,
			    Material.LIGHT_GRAY_BUNDLE,
			    Material.CYAN_BUNDLE,
			    Material.PURPLE_BUNDLE,
			    Material.BLUE_BUNDLE,
			    Material.BROWN_BUNDLE,
			    Material.GREEN_BUNDLE,
			    Material.RED_BUNDLE,
			    Material.BLACK_BUNDLE);
	}
}
