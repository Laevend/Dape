package coffee.dape.playerdata.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import coffee.dape.Dape;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.components.buttons.AnimatedButton;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.components.buttons.TextInputButton;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.interfaces.paginator.PaginatorItem;
import coffee.dape.chaosui.interfaces.paginator.PaginatorPanelItem;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.playerdata.gui.PlayerServerVarsGuiBuilder;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.GradientUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.Logg;
import coffee.dape.utils.MaterialUtils;
import coffee.dape.utils.structs.Namespace;

/**
 * 
 * @author Laeven
 *
 */
public class PlayerServerVariables extends PlayerDataExtension
{
	public static final String PLAYER_SERVER_VARIABLES = "player_server_variables";
	private static Map<UUID,PlayerServerVariables> instances = new HashMap<>();
	private JsonObject serverVarData;
	
	public PlayerServerVariables(UUID owner)
	{
		super(owner,Namespace.of(Dape.getNamespaceName(),PLAYER_SERVER_VARIABLES));
		serverVarData = new JsonObject();
	}
	
	public static PlayerServerVariables getInstance(Player p)
	{
		return getInstance(p.getUniqueId());
	}
	
	public static PlayerServerVariables getInstance(UUID playerUUID)
	{
		if(!instances.containsKey(playerUUID))
		{
			instances.put(playerUUID,new PlayerServerVariables(playerUUID));
		}
		
		return instances.get(playerUUID);
	}
	
	public void set(ServerVar serverVar,String value)
	{
		assertCategoryExists(serverVar);
		serverVarData.get(serverVar.getCategory().getConfigName()).getAsJsonObject().addProperty(serverVar.getConfigName(),value);
	}
	
	public void set(ServerVar serverVar,boolean value)
	{
		assertCategoryExists(serverVar);
		serverVarData.get(serverVar.getCategory().getConfigName()).getAsJsonObject().addProperty(serverVar.getConfigName(),value);
	}
	
	public void set(ServerVar serverVar,Number value)
	{
		assertCategoryExists(serverVar);
		serverVarData.get(serverVar.getCategory().getConfigName()).getAsJsonObject().addProperty(serverVar.getConfigName(),value);
	}
	
	public void set(ServerVar serverVar,JsonElement value)
	{
		assertCategoryExists(serverVar);
		serverVarData.get(serverVar.getCategory().getConfigName()).getAsJsonObject().add(serverVar.getConfigName(),value);
	}
	
	public JsonElement get(ServerVar serverVar)
	{
		assertCategoryExists(serverVar);
		
		if(!serverVarData.get(serverVar.getCategory().getConfigName()).getAsJsonObject().has(serverVar.getConfigName()))
		{
			return serverVar.defaultValue;
		}
		
		return serverVarData.get(serverVar.getCategory().getConfigName()).getAsJsonObject().get(serverVar.getConfigName());
	}
	
	public void reset(ServerVar serverVar)
	{
		set(serverVar,serverVar.getDefaultValue());
	}
	
	private void assertCategoryExists(ServerVar serverVar)
	{
		if(serverVarData.has(serverVar.getCategory().getConfigName())) { return; }
		serverVarData.add(serverVar.getCategory().getConfigName(),new JsonObject());
	}
	
	/**
	 * Checks if the current server variable is the same as its default value
	 * @param serverVar Server variable to check
	 * @return True if the value stored is the same as the default value for this variable, false otherwise
	 */
	public boolean isDefault(ServerVar serverVar)
	{
		switch(serverVar.getType())
		{
			case BOOLEAN:
			{
				return get(serverVar).getAsBoolean() == serverVar.getDefaultValue().getAsBoolean();
			}
			case CHAT_COLOUR_CODES:
			{
				return get(serverVar).getAsString().equals(serverVar.getDefaultValue().getAsString());
			}
			case DOUBLE:
			{
				return get(serverVar).getAsDouble() == serverVar.getDefaultValue().getAsDouble();
			}
			case FLOAT:
			{
				return get(serverVar).getAsFloat() == serverVar.getDefaultValue().getAsFloat();
			}
			case HEXADECIMAL:
			{
				return get(serverVar).getAsString().equals(serverVar.getDefaultValue().getAsString());
			}
			case INTEGER:
			{
				return get(serverVar).getAsInt() == serverVar.getDefaultValue().getAsInt();
			}
			case LONG:
			{
				return get(serverVar).getAsLong() == serverVar.getDefaultValue().getAsLong();
			}
			case STRING:
			{
				return get(serverVar).getAsString().equals(serverVar.getDefaultValue().getAsString());
			}
			case UUID:
			{
				return get(serverVar).getAsString().equals(serverVar.getDefaultValue().getAsString());
			}
			default:
			{
				Logg.error("Cannot check default for type " + serverVar.getType().toString() + " because it has not been implemented!");
				return false;
			}
		}
	}

	public enum ServerVarCategory implements PaginatorItem
	{
		CHAT
		(
			"Chat",
			"Manage variables for chat",
			new Material[]
			{
				Material.GOAT_HORN,
				Material.WRITABLE_BOOK,
				Material.BAMBOO_SIGN
			},
			new ServerVar[] 
			{
				ServerVar.CHAT_PREFIX_DECOR_ITEMSTORE_NAME,
				ServerVar.CHAT_PREFIX_DECOR_ITEMSTORE_CATEGORY,
				ServerVar.CHAT_PREFIX_DECOR,
				ServerVar.CHAT_PREFIX_ITEMSTORE_NAME,
				ServerVar.CHAT_PREFIX,
				ServerVar.CHAT_NAME_DECOR_ITEMSTORE_NAME,
				ServerVar.CHAT_NAME_DECOR_ITEMSTORE_CATEGORY,
				ServerVar.CHAT_NAME_DECOR,
				ServerVar.CHAT_NICK_NAME,
				ServerVar.CHAT_DECOR_ITEMSTORE_NAME,
				ServerVar.CHAT_DECOR_ITEMSTORE_CATEGORY,
				ServerVar.CHAT_DECOR,
				ServerVar.CHAT_BUBBLE_DECOR,
				ServerVar.CHAT_CHANNEL,
				ServerVar.PARTY
			}
		),
		WORLD_VARS
		(
			"World Variables",
			"Manage variables for world mechanics",
			new Material[]
			{
				Material.LIGHT_BLUE_CONCRETE,
				Material.LIME_CONCRETE,
			},
			new ServerVar[] 
			{
				ServerVar.SPOOKY_CAVES_SECONDS_SPENT_IN_DARK,
				ServerVar.CURRENT_BOSS_INSTANCE,
			}
		),
		RANKS
		(
			"Ranks",
			"Manage variables for ranks",
			new Material[]
			{
				Material.NAME_TAG,
				Material.DIAMOND,
				Material.PAPER
			},
			new ServerVar[] 
			{
				ServerVar.STAFF_RANK,
				ServerVar.PATRON_RANK,
				ServerVar.RANK_PREFIX_INITIAL,
			}
		),
		GUIS
		(
			"GUIS",
			"Manage variables for GUIs",
			new Material[]
			{
				Material.CHEST,
				Material.ENDER_CHEST,
				Material.BARREL,
				Material.HOPPER
			},
			new ServerVar[] 
			{
				ServerVar.FORCE_HOME_TP,
				ServerVar.OVERRIDE_HOME,
				ServerVar.EMPTY_TRASH_WARNING,
			}
		),
		;
		
		private String name;
		private String desc;
		private Material[] stackIcons;
		private ServerVar[] serverVars;
		
		private ChaosComponent cachedComponent = null;
		
		ServerVarCategory(String name,String desc,Material[] stackIcons,ServerVar[] serverVars)
		{
			this.name = name;
			this.desc = desc;
			this.stackIcons = stackIcons;
			this.serverVars = serverVars;
		}
		
		public String getName()
		{
			return name;
		}

		public String getDesc()
		{
			return desc;
		}
		
		public Material[] getStackIcons()
		{
			return stackIcons;
		}

		public ServerVar[] getServerVars()
		{
			return serverVars;
		}
		
		public ServerVar[] getServerVars(Player p)
		{
			return getServerVars(p.getUniqueId());
		}
		
		public ServerVar[] getServerVars(UUID player)
		{
			ServerVar[] personalisedServerVars = new ServerVar[serverVars.length];
			System.arraycopy(serverVars,0,personalisedServerVars,0,serverVars.length);
			PlayerServerVariables pServerVars = PlayerServerVariables.getInstance(player);
			
			for(int i = 0; i < serverVars.length; i++)
			{
				((TextInputButton) personalisedServerVars[i].getPanelButton(null)).setInputValue(pServerVars.get(personalisedServerVars[i]).getAsString());
			}
			
			return serverVars;
		}
		
		public String getConfigName()
		{
			return this.toString().toLowerCase();
		}

		@Override
		public ItemStack getStack()
		{
			return getComponent().getStack();
		}

		@Override
		public boolean isItemComponentType()
		{
			return true;
		}

		@Override
		public ChaosComponent getComponent()
		{
			if(cachedComponent == null)
			{
				if(stackIcons.length == 1)
				{
					cachedComponent = new Button(ItemBuilder.of(stackIcons[0])
							.name(name,GradientUtils.GREY_TO_DARK)
							.lore()
							.wrap(ColourUtils.applyColour(desc,ColourUtils.TEXT))
							.commit()
							.create());
				}
				else
				{
					LinkedList<ItemStack> icons = new LinkedList<>();
					Arrays.asList(stackIcons).forEach(icon ->
					{
						icons.add(ItemBuilder.of(icon)
										.name(name,GradientUtils.GREY_TO_DARK)
										.lore()
										.wrap(ColourUtils.applyColour(desc,ColourUtils.TEXT))
										.commit()
										.create());
					});
					
					cachedComponent = new AnimatedButton(20,icons);
				}
				
				ServerVarCategory cat = this;
				
				cachedComponent.addActionListener(new ChaosActionListener()
				{
					public void onClick(ChaosClickEvent e)
					{
						Player p = (Player) e.getWhoClicked();
						PlayerServerVarsGuiBuilder.instance.setServerVarCategoryInUse(p,cat);
						ChaosFactory.open(p,ChaosFactory.Common.PLAYER_SERVER_VARIABLES_CATEGORY);
					}
				});
			}
						
			return cachedComponent;
		}
	}
	
	// These exist because enums don't like being called when they're still being initialised
	private static final String VAR_CATEGORY_CHAT = "CHAT";
	private static final String VAR_CATEGORY_WORLD_VARS = "WORLD_VARS";
	private static final String VAR_CATEGORY_RANKS = "RANKS";
	private static final String VAR_CATEGORY_GUIS = "GUIS";

	public enum ServerVar implements PaginatorPanelItem
	{
		CHAT_PREFIX_DECOR_ITEMSTORE_NAME
		(
				"Prefix Decor Itemstore Name",
				"Itemstore name of the colour used for the prefix.",
				"",
				new Material[] {Material.CRIMSON_SIGN},
				ServerVarDataType.STRING,
				VAR_CATEGORY_CHAT
		),
		CHAT_PREFIX_DECOR_ITEMSTORE_CATEGORY
		(
				"Prefix Decor Itemstore Category Name",
				"Itemstore category name of the colour used for the prefix.",
				"",
				new Material[] {Material.CRIMSON_HANGING_SIGN},
				ServerVarDataType.STRING,
				VAR_CATEGORY_CHAT
		),
		CHAT_PREFIX_DECOR
		(
				"Prefix Decor",
				"The colour used to decorate the prefix.",
				"#00abab",
				MaterialUtils.DYES.toArray(new Material[MaterialUtils.DYES.size()]),
				ServerVarDataType.HEXADECIMAL,
				VAR_CATEGORY_CHAT
		),
		CHAT_PREFIX_ITEMSTORE_NAME
		(
				"Prefix Itemstore Name",
				"Itemstore name of the prefix.",
				"",
				new Material[] {Material.BAMBOO_SIGN},
				ServerVarDataType.STRING,
				VAR_CATEGORY_CHAT
		),
		CHAT_PREFIX
		(
				"Prefix",
				"The text that displays before a players name.",
				"Member",
				new Material[] {Material.NAME_TAG},
				ServerVarDataType.STRING,
				VAR_CATEGORY_CHAT
		),
		CHAT_NAME_DECOR_ITEMSTORE_NAME
		(
				"Player Name Decor Itemstore Name",
				"Itemstore name of the colour used for the players name.",
				"",
				new Material[] {Material.WARPED_SIGN},
				ServerVarDataType.STRING,
				VAR_CATEGORY_CHAT
		),
		CHAT_NAME_DECOR_ITEMSTORE_CATEGORY
		(
				"Player Name Decor Itemstore Category Name",
				"Itemstore category name of the colour used for the players name.",
				"",
				new Material[] {Material.WARPED_HANGING_SIGN},
				ServerVarDataType.STRING,
				VAR_CATEGORY_CHAT
		),
		CHAT_NAME_DECOR
		(
				"Player Name Decor",
				"The colour used to decorate the players name.",
				"#00abab",
				MaterialUtils.DYES.toArray(new Material[MaterialUtils.DYES.size()]),
				ServerVarDataType.HEXADECIMAL,
				VAR_CATEGORY_CHAT
		),
		CHAT_NICK_NAME
		(
				"Player Nick Name",
				"The text that overrides the players real username.",
				"",
				new Material[] {Material.NAME_TAG},
				ServerVarDataType.STRING,
				VAR_CATEGORY_CHAT
		),
		CHAT_DECOR_ITEMSTORE_NAME
		(
				"Chat Message Decor Itemstore Name",
				"Itemstore name of the colour used for the chat message.",
				"",
				new Material[] {Material.OAK_SIGN},
				ServerVarDataType.STRING,
				VAR_CATEGORY_CHAT
		),
		CHAT_DECOR_ITEMSTORE_CATEGORY
		(
				"Chat Message Decor Itemstore Category Name",
				"Itemstore category name of the colour used for the chat message.",
				"",
				new Material[] {Material.OAK_HANGING_SIGN},
				ServerVarDataType.STRING,
				VAR_CATEGORY_CHAT
		),
		CHAT_DECOR
		(
				"Chat Message Decor",
				"The colour used to decorate the chat message.",
				"#FFFFFF",
				MaterialUtils.DYES.toArray(new Material[MaterialUtils.DYES.size()]),
				ServerVarDataType.HEXADECIMAL,
				VAR_CATEGORY_CHAT
		),
		CHAT_BUBBLE_DECOR
		(
				"Chat Bubble Decor",
				"The colour used to decorate the chat bubble message.",
				"e",
				MaterialUtils.DYES.toArray(new Material[MaterialUtils.DYES.size()]),
				ServerVarDataType.CHAT_COLOUR_CODES,
				VAR_CATEGORY_CHAT
		),
		CHAT_CHANNEL
		(
				"Chat Channel",
				"The name of the channel the player is talking in.",
				"global",
				new Material[] {Material.GOAT_HORN,Material.WRITABLE_BOOK,Material.PAPER},
				ServerVarDataType.STRING,
				VAR_CATEGORY_CHAT
		),
		PARTY
		(
				"Party",
				"The UUID of the party the player is in.",
				"none",
				new Material[] {Material.PIGLIN_HEAD},
				ServerVarDataType.UUID,
				VAR_CATEGORY_CHAT
		),
		
		/* =-=-=-=-=-=-=-=-=-=-= */
		
		SPOOKY_CAVES_SECONDS_SPENT_IN_DARK
		(
				"Spooky Caves Time In Dark",
				"The number of seconds a player has spent in a light level 0 area.",
				0,
				new Material[] {Material.DEEPSLATE},
				ServerVarDataType.INTEGER,
				VAR_CATEGORY_WORLD_VARS
		),
		CURRENT_BOSS_INSTANCE
		(
				"Current Boss Instance",
				"The UUID of the boss instance the player is apart of",
				"none",
				new Material[] {Material.DRAGON_HEAD},
				ServerVarDataType.UUID,
				VAR_CATEGORY_WORLD_VARS
		),
		
		/* =-=-=-=-=-=-=-=-=-=-= */
		
		STAFF_RANK
		(
				"Staff Rank",
				"The staff rank of the player",
				"",
				new Material[] {Material.PAPER},
				ServerVarDataType.STRING,
				VAR_CATEGORY_RANKS
		),
		PATRON_RANK
		(
				"Patron Rank",
				"The patron rank of the player",
				"",
				new Material[] {Material.MAP},
				ServerVarDataType.STRING,
				VAR_CATEGORY_RANKS
		),
		RANK_PREFIX_INITIAL
		(
				"Rank Prefix Initial",
				"The letter(s) displayed before a players prefix to indicate their rank. Be it Patron or Staff",
				"",
				new Material[] {Material.NAME_TAG},
				ServerVarDataType.STRING,
				VAR_CATEGORY_RANKS
		),
		
		/* =-=-=-=-=-=-=-=-=-=-= */
		
		FORCE_HOME_TP
		(
				"Force Home Teleport",
				"Persistent variable for forcing home teleports. Forcing meaning not checking for safety.",
				false,
				new Material[] {Material.BELL},
				ServerVarDataType.BOOLEAN,
				VAR_CATEGORY_GUIS
		),
		OVERRIDE_HOME
		(
				"Override Home",
				"Persistent variable for overriding a home location when setting a new home with the same name.",
				false,
				new Material[] {Material.NAME_TAG},
				ServerVarDataType.BOOLEAN,
				VAR_CATEGORY_GUIS
		),
		EMPTY_TRASH_WARNING
		(
				"Empty Trash Warning",
				"Persistent variable for checking if a warning confirmation GUI should be shown before emptying trash.",
				true,
				new Material[] {Material.LAVA_BUCKET},
				ServerVarDataType.BOOLEAN,
				VAR_CATEGORY_GUIS
		)
		;
		
		private String name;
		private String desc;
		private JsonElement defaultValue;
		private Material[] stackIcons;
		private ServerVarDataType type;
		private String category;
		
		private ChaosComponent cachedPanelIcon = null;
		private ChaosComponent cachedPanelButton = null;
		
		ServerVar(String name,String desc,Object defaultValue,Material[] stackIcons,ServerVarDataType type,String category)
		{
			this.name = name;
			this.desc = desc;
			this.stackIcons = stackIcons;
			this.type = type;
			this.category = category;
			
			if(defaultValue.getClass().equals(String.class))
			{
				String val = (String) defaultValue;
				this.defaultValue = JsonParser.parseString("{\"val\": \"" + val + "\"}").getAsJsonObject().get("val");
			}
			else if(defaultValue.getClass().equals(Integer.class))
			{
				Integer val = (Integer) defaultValue;
				this.defaultValue = JsonParser.parseString("{\"val\": " + val + "}").getAsJsonObject().get("val");
			}
			else if(defaultValue.getClass().equals(Boolean.class))
			{
				Boolean val = (Boolean) defaultValue;
				this.defaultValue = JsonParser.parseString("{\"val\": " + val + "}").getAsJsonObject().get("val");
			}
			else if(defaultValue.getClass().equals(Double.class))
			{
				Double val = (Double) defaultValue;
				this.defaultValue = JsonParser.parseString("{\"val\": " + val + "}").getAsJsonObject().get("val");
			}
			else if(defaultValue.getClass().equals(Float.class))
			{
				Float val = (Float) defaultValue;
				this.defaultValue = JsonParser.parseString("{\"val\": " + val + "}").getAsJsonObject().get("val");
			}
			else if(defaultValue.getClass().equals(Long.class))
			{
				Long val = (Long) defaultValue;
				this.defaultValue = JsonParser.parseString("{\"val\": " + val + "}").getAsJsonObject().get("val");
			}
			
			cachedPanelButton = new TextInputButton(
					ColourUtils.applyColour("Modify Variable",ColourUtils.DARK_WHITE),
					ColourUtils.applyColour("Modify this variable",ColourUtils.TEXT),
					ColourUtils.applyColour("Enter new variable",ColourUtils.TEXT),
					this.defaultValue.getAsString().equals("") ? ColourUtils.applyColour("null",ColourUtils.TEXT_ERROR) : ColourUtils.applyColour(this.defaultValue.getAsString(),ColourUtils.TEXT_ERROR),
					Material.CHAIN_COMMAND_BLOCK);
		}
		
		public String getName()
		{
			return name;
		}
		
		public String getDescription()
		{
			return desc;
		}
		
		public JsonElement getDefaultValue()
		{
			return defaultValue;
		}
		
		public Material[] getStackIcons()
		{
			return stackIcons;
		}
		
		public ServerVarDataType getType()
		{
			return type;
		}

		public String getConfigName()
		{
			return this.toString().toLowerCase();
		}
		
		public ServerVarCategory getCategory()
		{
			return ServerVarCategory.valueOf(category.toUpperCase());
		}

		@Override
		public ItemStack getStack()
		{
			return getComponent().getStack();
		}

		@Override
		public boolean isItemComponentType()
		{
			return true;
		}

		@Override
		public ChaosComponent getComponent()
		{
			return getPanelIcon();
		}

		@Override
		public ChaosComponent getPanelIcon()
		{
			if(cachedPanelIcon == null)
			{
				if(stackIcons.length == 1)
				{
					cachedPanelIcon = new Button(ItemBuilder.of(stackIcons[0])
							.name(name,GradientUtils.GOLDY)
							.lore()
							.wrap(ColourUtils.applyColour(desc,ColourUtils.COLUMBIA_BLUE))
							.append("")
							.append(ColourUtils.applyColour("DataType: ",ColourUtils.TEXT) + getType().getLore())
							.wrap(ColourUtils.applyColour("Default value is ",ColourUtils.TEXT) + (defaultValue.getAsString().equals("") ? ColourUtils.applyColour("null*",ColourUtils.TEXT_WARNING) : ColourUtils.applyColour(defaultValue.getAsString(),ColourUtils.TEXT_WARNING)))
							.commit()
							.setData("server_var",this.toString())
							.create());
				}
				else
				{
					LinkedList<ItemStack> icons = new LinkedList<>();
					Arrays.asList(stackIcons).forEach(icon ->
					{
						icons.add(ItemBuilder.of(icon)
										.name(name,GradientUtils.GOLDY)
										.lore()
										.wrap(ColourUtils.applyColour(desc,ColourUtils.COLUMBIA_BLUE))
										.append("")
										.append(ColourUtils.applyColour("DataType: ",ColourUtils.TEXT) + getType().getLore())
										.wrap(ColourUtils.applyColour("Default value is ",ColourUtils.TEXT) + (defaultValue.getAsString().equals("") ? ColourUtils.applyColour("null*",ColourUtils.TEXT_WARNING) : ColourUtils.applyColour(defaultValue.getAsString(),ColourUtils.TEXT_WARNING)))
										.commit()
										.setData("server_var",this.toString())
										.create());
					});
					
					cachedPanelIcon = new AnimatedButton(20,icons);
				}
			}
			
			return cachedPanelIcon;
		}

		@Override
		public ChaosComponent getPanelButton(Player p)
		{
			return cachedPanelButton;
		}
	}
	
	private enum ServerVarDataType
	{
		STRING("&8[&9String&8]"),
		INTEGER("&8[&cInt&8]"),
		BOOLEAN("&8[&eBool&8]"),
		DOUBLE("&8[&dDouble&8]"),
		FLOAT("&8[&aFloat&8]"),
		LONG("&8[&6Long&8]"),
		
		// Subtypes of string
		HEXADECIMAL("&8[&1Hex&8]"),
		CHAT_COLOUR_CODES("&8[&bChat Colour Codes&8]"),
		UUID("&8[&3UUID&8]"),
		;
		
		private String lore;
		
		ServerVarDataType(String lore)
		{
			this.lore = lore;
		}

		public String getLore()
		{
			return lore;
		}
	}

	@Override
	public JsonObject serialise() throws SerialiseException
	{
		return serverVarData;
	}

	@Override
	public void deserialise(JsonObject obj) throws DeserialiseException
	{
		serverVarData = obj;
	}
}
