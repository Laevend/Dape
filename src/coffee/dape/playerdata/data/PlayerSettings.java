package coffee.dape.playerdata.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import coffee.dape.chaosui.components.buttons.ChoiceButton;
import coffee.dape.chaosui.components.buttons.IntButton;
import coffee.dape.chaosui.components.buttons.ToggleButton;
import coffee.dape.chaosui.components.buttons.ToggleButton.ToggleStatus;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.interfaces.paginator.PaginatorItem;
import coffee.dape.chaosui.interfaces.paginator.PaginatorPanelItem;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.playerdata.gui.PlayerSettingsGuiBuilder;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.GradientUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.Logg;
import coffee.dape.utils.MaterialUtils;
import coffee.dape.utils.data.DataUtils;
import coffee.dape.utils.structs.Namespace;

/**
 * 
 * @author Laeven
 *
 */
public class PlayerSettings extends PlayerDataExtension
{
	public static final String PLAYER_SETTINGS = "player_settings";
	private static Map<UUID,PlayerSettings> instances = new HashMap<>();
	private JsonObject settingsData;
	
	public PlayerSettings(UUID owner)
	{
		super(owner,Namespace.of(Dape.getNamespaceName(),PLAYER_SETTINGS));
	}
	
	public static PlayerSettings getInstance(Player p)
	{
		return getInstance(p.getUniqueId());
	}
	
	public static PlayerSettings getInstance(UUID playerUUID)
	{
		if(!instances.containsKey(playerUUID))
		{
			instances.put(playerUUID,new PlayerSettings(playerUUID));
		}
		
		return instances.get(playerUUID);
	}
	
	public void set(Setting setting,String value)
	{
		assertCategoryExists(setting);
		settingsData.get(setting.getCategory().getConfigName()).getAsJsonObject().addProperty(setting.getConfigName(),value);
	}
	
	public void set(Setting setting,boolean value)
	{
		assertCategoryExists(setting);
		settingsData.get(setting.getCategory().getConfigName()).getAsJsonObject().addProperty(setting.getConfigName(),value);
	}
	
	public void set(Setting setting,Number value)
	{
		assertCategoryExists(setting);
		settingsData.get(setting.getCategory().getConfigName()).getAsJsonObject().addProperty(setting.getConfigName(),value);
	}
	
	public JsonElement get(Setting setting)
	{
		assertCategoryExists(setting);
		
		if(!settingsData.get(setting.getCategory().getConfigName()).getAsJsonObject().has(setting.getConfigName()))
		{
			return setting.defaultValue;
		}
		
		return settingsData.get(setting.getCategory().getConfigName()).getAsJsonObject().get(setting.getConfigName());
	}
	
	private void assertCategoryExists(Setting setting)
	{
		if(settingsData.has(setting.getCategory().getConfigName())) { return; }
		settingsData.add(setting.getCategory().getConfigName(),new JsonObject());
	}

	public enum SettingCategory implements PaginatorItem
	{
		CHAT
		(
			"Chat",
			"The chat interface",
			new Material[]
			{
				Material.GOAT_HORN,
				Material.WRITABLE_BOOK,
				Material.BAMBOO_SIGN
			},
			new Setting[] 
			{
				Setting.CHAT_ENABLED,
				Setting.CHAT_PREFIXES,
				Setting.CHAT_CHAT_COLOUR,
				Setting.CHAT_PREFIX_COLOUR,
				Setting.CHAT_SHORTER_NAME,
				Setting.CHAT_REAL_NAME,
				Setting.CHAT_NAME_COLOUR,
				Setting.CHAT_HIDE_GLOBAL_CHAT_PREFIX,
				Setting.CHAT_IGNORE_PINGS
			}
		),
		CHATBUBBLES
		(
			"Chat Bubbles",
			"Creates bubbles above your head when you talk",
			new Material[]
			{
				Material.SPYGLASS,
				Material.BAMBOO_HANGING_SIGN
			},
			new Setting[] 
			{
				Setting.CHATBUBBLES_ENABLED,
				Setting.CHATBUBBLES_COLOUR
			}
		),
		COMBAT
		(
			"Combat",
			"Combat related settings",
			new Material[]
			{
				Material.DIAMOND_SWORD,
				Material.GOLDEN_AXE,
				Material.SHIELD,
				Material.IRON_SWORD,
				Material.CROSSBOW,
				Material.STONE_AXE,
				Material.BOW
			},
			new Setting[] 
			{
				Setting.COMBAT_SHOW_HIT_SPLATS,
				Setting.COMBAT_SHOW_HEALTH_BAR,
				Setting.COMBAT_SHOW_LOOT_BEAM
			}
		),
		BOMBS
		(
			"Bombs",
			"Server wide effects",
			new Material[]
			{
				Material.TNT,
				Material.REDSTONE_TORCH,
				Material.GUNPOWDER
			},
			new Setting[] 
			{
				Setting.BOMBS_NEW_BOMB_NOTIFICATION,
				Setting.BOMBS_SOUNDS_ENABLED,
			}
		),
		FARMING
		(
			"Farming",
			"Farming utilities",
			new Material[]
			{
				Material.HAY_BLOCK,
				Material.DIAMOND_HOE,
				Material.FARMLAND,
				Material.WHEAT_SEEDS,
				Material.IRON_HOE
			},
			new Setting[] 
			{
				Setting.FARMING_ANTICROP_TRAMPLE,
				Setting.FARMING_AUTOCROP_REPLANT,
				Setting.FARMING_LAZY_HARVEST
			}
		),
		NOTIFICATIONS
		(
			"Notifications",
			"Various settings about specific server notifications",
			MaterialUtils.DYES.toArray(new Material[MaterialUtils.DYES.size()]),
			new Setting[] 
			{
				Setting.NOTIFICATIONS_GREETING,
				Setting.NOTIFICATIONS_TIPS
			}
		),
		MOBS
		(
			"Mobs",
			"Settings that impact mobs",
			new Material[]
			{
				Material.SKELETON_SKULL,
				Material.ZOMBIE_HEAD,
				Material.WITHER_SKELETON_SKULL,
				Material.CREEPER_HEAD,
				Material.PIGLIN_HEAD
			},
			new Setting[] 
			{
				Setting.MOBS_PHANTOM_BUG_LAMP
			}
		),
		;
		
		private String name;
		private String desc;
		private Material[] stackIcons;
		private Setting[] settings;
		
		private ChaosComponent cachedComponent = null;
		
		SettingCategory(String name,String desc,Material[] stackIcons,Setting[] settings)
		{
			this.name = name;
			this.desc = desc;
			this.stackIcons = stackIcons;
			this.settings = settings;
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

		public Setting[] getSettings()
		{
			return settings;
		}
		
		public Setting[] getSettings(Player p)
		{
			Setting[] personalisedSettings = new Setting[settings.length];
			System.arraycopy(settings,0,personalisedSettings,0,settings.length);
			PlayerSettings pSettings = PlayerSettings.getInstance(p);
			
			for(int i = 0; i < settings.length; i++)
			{
//				System.out.println("old -> " + settings[i].name);
//				System.out.println("new -> " + personalisedSettings[i].name);
//				System.out.println("old -> " + settings[i].getCategory().toString());
//				System.out.println("new -> " + personalisedSettings[i].getCategory().toString());
				
				switch(personalisedSettings[i].getPanelButton(p).getType())
				{
					case TOGGLEABLE_BUTTON:
					{
						((ToggleButton) personalisedSettings[i].getPanelButton(p)).setEnabled(pSettings.get(personalisedSettings[i]).getAsBoolean());
						break;
					}
					case CHOICE_BUTTON:
					{
						((ChoiceButton) personalisedSettings[i].getPanelButton(p)).setDefaultValue(pSettings.get(personalisedSettings[i]).getAsString());
						break;
					}
					case INTEGER_INPUT_BUTTON:
					{
						System.out.println("Setting ----> " + personalisedSettings[i].toString());
						((IntButton) personalisedSettings[i].getPanelButton(p)).setDefaultButtonValue(pSettings.get(personalisedSettings[i]).getAsInt());
						break;
					}
					default:
					{
						Logg.fatal("Incorrect button type for panel found when getting settings! " + personalisedSettings[i].getPanelButton(p).getType());
					}
				}
			}
			
			return personalisedSettings;
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
										.create());
					});
					
					cachedComponent = new AnimatedButton(20,icons);
				}
				
				SettingCategory cat = this;
				
				cachedComponent.addActionListener(new ChaosActionListener()
				{
					public void onClick(ChaosClickEvent e)
					{
						Player p = (Player) e.getWhoClicked();
						PlayerSettingsGuiBuilder.instance.setSettingCategoryInUse(p,cat);
						ChaosFactory.open(p,ChaosFactory.Common.PLAYER_SETTINGS_CATEGORY);
					}
				});
			}
						
			return cachedComponent;
		}
	}
	
	private static final String SETCAT_CHAT = "CHAT";
	private static final String SETCAT_CHATBUBBLES = "CHATBUBBLES";
	private static final String SETCAT_COMBAT = "COMBAT";
	private static final String SETCAT_BOMBS = "BOMBS";
	private static final String SETCAT_FARMING = "FARMING";
	private static final String SETCAT_NOTIFICATIONS = "NOTIFICATIONS";
	private static final String SETCAT_MOBS = "MOBS";

	public enum Setting implements PaginatorPanelItem
	{
		// Examples
//		CHAT_ENABLED
//		(
//				"Chat Enabled",
//				"If I want to see other players talking in global chat.",
//				1,
//				new Material[] {Material.GOAT_HORN},
//				List.of(-20,20),
//				SETCAT_CHAT,
//				ChaosComponent.Type.INTEGER_INPUT_BUTTON
//		),
//		CHAT_PREFIXES
//		(
//				"Prefixes",
//				"If I want to see players prefixs in the chat.",
//				"true",
//				new Material[] {Material.NAME_TAG},
//				List.of("true","false","something","idk","idrc"),
//				SETCAT_CHAT,
//				ChaosComponent.Type.CHOICE_BUTTON
//		),
		CHAT_ENABLED
		(
				"Chat Enabled",
				"If I want to see other players talking in global chat.",
				true,
				new Material[] {Material.GOAT_HORN},
				List.of(true,false),
				SETCAT_CHAT,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		CHAT_PREFIXES
		(
				"Prefixes",
				"If I want to see players prefixs in the chat.",
				true,
				new Material[] {Material.NAME_TAG},
				List.of(true,false),
				SETCAT_CHAT,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		CHAT_CHAT_COLOUR
		(
				"Chat Message Colour",
				"If I want to see chat messages with colour.",
				true,
				MaterialUtils.DYES.toArray(new Material[MaterialUtils.DYES.size()]),
				List.of(true,false),
				SETCAT_CHAT,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		CHAT_PREFIX_COLOUR
		(
				"Prefix Colour",
				"If I want to see chat prefixs with colour.",
				true,
				MaterialUtils.DYES.toArray(new Material[MaterialUtils.DYES.size()]),
				List.of(true,false),
				SETCAT_CHAT,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		CHAT_SHORTER_NAME
		(
				"Shorter Player Names",
				"If I want players names to be shorter.",
				false,
				new Material[] {Material.LEAD},
				List.of(true,false),
				SETCAT_CHAT,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		CHAT_REAL_NAME
		(
				"Display Players Real Name",
				"If I want to see a players real name rather than their nickname (if they have one).",
				false,
				new Material[] {Material.PLAYER_HEAD},
				List.of(true,false),
				SETCAT_CHAT,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		CHAT_NAME_COLOUR
		(
				"Player Name Colour",
				"If I want to see players name with colour.",
				true,
				MaterialUtils.DYES.toArray(new Material[MaterialUtils.DYES.size()]),
				List.of(true,false),
				SETCAT_CHAT,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		CHAT_HIDE_GLOBAL_CHAT_PREFIX
		(
				"Hide Global Chat Prefix",
				"If I want to see the prefix for global chat 'G >'.",
				false,
				new Material[] {Material.BRUSH},
				List.of(true,false),
				SETCAT_CHAT,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		CHAT_IGNORE_PINGS
		(
				"Ingore Pings",
				"If I want to prevent people from pinging me in chat.",
				false,
				new Material[] {Material.NOTE_BLOCK},
				List.of(true,false),
				SETCAT_CHAT,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		
		/* =-=-=-=-=-=-=-=-=-=-= */
		
		CHATBUBBLES_ENABLED
		(
				"Chat Bubbles Enabled",
				"If chat bubbles are enabled.",
				true,
				new Material[] {Material.SPYGLASS},
				List.of(true,false),
				SETCAT_CHATBUBBLES,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		CHATBUBBLES_COLOUR
		(
				"Chatbubble Colour",
				"If I want the chat bubbles that I see to have their colour (only applies to messages sent in global chat).",
				true,
				MaterialUtils.DYES.toArray(new Material[MaterialUtils.DYES.size()]),
				List.of(true,false),
				SETCAT_CHATBUBBLES,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		
		/* =-=-=-=-=-=-=-=-=-=-= */
		
		COMBAT_SHOW_HIT_SPLATS
		(
				"Hit Splats",
				"Show the damage I deal to an mob/player.",
				true,
				new Material[] {Material.STONE_AXE,Material.DIAMOND_SWORD,Material.IRON_PICKAXE,Material.BOW,Material.CROSSBOW},
				List.of(true,false),
				SETCAT_COMBAT,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		COMBAT_SHOW_HEALTH_BAR
		(
				"Health bar",
				"Show the health bar of a mob/player when attacking.",
				true,
				new Material[] {Material.REDSTONE},
				List.of(true,false),
				SETCAT_COMBAT,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		COMBAT_SHOW_LOOT_BEAM
		(
				"Loot Beam",
				"Show a loot beam over rare/expensive items when a mob or player drops them on death.",
				false,
				new Material[] {Material.BEACON},
				List.of(true,false),
				SETCAT_COMBAT,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		
		/* =-=-=-=-=-=-=-=-=-=-= */
		
		BOMBS_NEW_BOMB_NOTIFICATION
		(
				"New Bomb Notifications",
				"If you want to recieve a notification in chat when someone deploys a bomb.",
				false,
				new Material[] {Material.TNT,Material.GUNPOWDER},
				List.of(true,false),
				SETCAT_BOMBS,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		BOMBS_SOUNDS_ENABLED
		(
				"Bomb Sounds Enabled",
				"If you want to hear sound effects when a new bomb is deployed and expired.",
				false,
				new Material[] {Material.NOTE_BLOCK},
				List.of(true,false),
				SETCAT_BOMBS,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		
		/* =-=-=-=-=-=-=-=-=-=-= */
		
		FARMING_ANTICROP_TRAMPLE
		(
				"Anti-Crop Trample",
				"When enabled, it will prevent you from trampling crops.",
				true,
				new Material[] {Material.FARMLAND,Material.IRON_HOE},
				List.of(true,false),
				SETCAT_FARMING,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		FARMING_AUTOCROP_REPLANT
		(
				"Autocrop Replant",
				"When enabled, it will automatically re-plant seeds for the crop you harvested. Only applies to crops placed on farmland!",
				false,
				new Material[] {Material.WHEAT,Material.CARROT,Material.POTATO,Material.BEETROOT},
				List.of(true,false),
				SETCAT_FARMING,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		FARMING_LAZY_HARVEST
		(
				"Lazy Harvest",
				"When enabled, it will prevent you from destroying stems and crops that are not ready to be harvested while holding a hoe in your main hand.",
				false,
				new Material[] {Material.GOLDEN_HOE,Material.STONE_HOE,Material.IRON_HOE,Material.WOODEN_HOE,Material.DIAMOND_HOE,Material.NETHERITE_HOE},
				List.of(true,false),
				SETCAT_FARMING,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		
		/* =-=-=-=-=-=-=-=-=-=-= */
		
		NOTIFICATIONS_GREETING
		(
				"Greeting Notification",
				"Displays the greeting when you join the server.",
				true,
				new Material[] {Material.CHERRY_SIGN,Material.ACACIA_SIGN,Material.BAMBOO_SIGN,Material.BIRCH_SIGN,Material.WARPED_SIGN,Material.SPRUCE_SIGN,Material.OAK_SIGN,Material.MANGROVE_SIGN,Material.JUNGLE_SIGN,Material.DARK_OAK_SIGN,Material.CHERRY_SIGN},
				List.of(true,false),
				SETCAT_NOTIFICATIONS,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		NOTIFICATIONS_TIPS
		(
				"Tip Notifications",
				"Displays tips in the chat for various features you may not know about.",
				true,
				new Material[] {Material.KNOWLEDGE_BOOK},
				List.of(true,false),
				SETCAT_NOTIFICATIONS,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		
		/* =-=-=-=-=-=-=-=-=-=-= */
		
		MOBS_PHANTOM_BUG_LAMP
		(
				"Phantom Bug Lamp",
				"If I want phantoms to be buzzed and killed when they attack me.",
				false,
				new Material[] {Material.PHANTOM_MEMBRANE},
				List.of(true,false),
				SETCAT_MOBS,
				ChaosComponent.Type.TOGGLEABLE_BUTTON
		),
		;
		
		private String name;
		private String desc;
		private JsonElement defaultValue;
		private Material[] stackIcons;
		private List<Object> options;
		private String category;
		private ChaosComponent.Type buttonType;
		
		private ChaosComponent cachedPanelIcon = null;
		private ChaosComponent cachedPanelButton = null;
		
		Setting(String name,String desc,Object defaultValue,Material[] stackIcons,List<Object> options,String category,ChaosComponent.Type buttonType)
		{
			this.name = name;
			this.desc = desc;
			this.stackIcons = stackIcons;
			this.options = options;
			this.category = category;
			this.buttonType = buttonType;
			
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
			
			Setting sett = this;
			
			switch(buttonType)
			{
				case TOGGLEABLE_BUTTON:
				{
					cachedPanelButton = new ToggleButton(this.defaultValue.getAsBoolean());
					cachedPanelButton.addActionListener(new ChaosActionListener()
					{
						public void onClick(ChaosClickEvent e)
						{
							Player p = (Player) e.getWhoClicked();
							String toggle = DataUtils.get(ToggleButton.DTAG,e.getCurrentItem()).asString();
							PlayerSettings.getInstance(p).set(sett,toggle.equals(ToggleStatus.ENABLED.toString()) ? true : false);
						}
					});
					break;
				}
				case CHOICE_BUTTON:
				{
					String defaultChoice = this.defaultValue.getAsString();
					String[] choices = new String[options.size()];
					
					int counter = 0;
					for(Object obj : options)
					{
						choices[counter] = (String) obj;
						counter++;
					}
					
					cachedPanelButton = new ChoiceButton(ColourUtils.applyColour("Choice input",ColourUtils.DARK_WHITE),Material.HOPPER,choices);					
					((ChoiceButton) cachedPanelButton).setDefaultValue(defaultChoice);
					cachedPanelButton.addActionListener(new ChaosActionListener()
					{
						public void onClick(ChaosClickEvent e)
						{
							Player p = (Player) e.getWhoClicked();
							String choice = DataUtils.get("choice",e.getCurrentItem()).asString();
							PlayerSettings.getInstance(p).set(sett,choice);
						}
					});
					break;
				}
				// It's expected that List<Object> options has MIN in index 0 and MAX in index 1
				case INTEGER_INPUT_BUTTON:
				{
					int min;
					int max;
					
					if(options.size() == 2)
					{
						min = (int) options.get(0);
						max = (int) options.get(1);
					}
					else
					{
						min = -100;
						max = 100;
					}
					
					cachedPanelButton = new IntButton(ColourUtils.applyColour("Number input",ColourUtils.DARK_WHITE),this.defaultValue.getAsInt(),max,min);
					cachedPanelButton.addActionListener(new ChaosActionListener()
					{
						public void onClick(ChaosClickEvent e)
						{
							Player p = (Player) e.getWhoClicked();
							int val = DataUtils.get("button_value",e.getCurrentItem()).asInt();
							PlayerSettings.getInstance(p).set(sett,val);
						}
					});
					break;
				}
				default:
				{
					Logg.fatal("Unsuitable button type for panel! " + buttonType.toString());
				}
			}
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

		public List<Object> getOptions()
		{
			return options;
		}
		
		public String getConfigName()
		{
			return this.toString().toLowerCase();
		}
		
		public SettingCategory getCategory()
		{
			return SettingCategory.valueOf(category.toUpperCase());
		}

		public ChaosComponent.Type getButtonType()
		{
			return buttonType;
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
							.wrap(ColourUtils.applyColour("Default value is ",ColourUtils.TEXT) + ColourUtils.applyColour(defaultValue.toString(),ColourUtils.TEXT_WARNING))
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
										.wrap(ColourUtils.applyColour("Default value is ",ColourUtils.TEXT) + ColourUtils.applyColour(defaultValue.toString(),ColourUtils.TEXT_WARNING))
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

	@Override
	public JsonObject serialise() throws SerialiseException
	{
		return settingsData;
	}

	@Override
	public void deserialise(JsonObject obj) throws DeserialiseException
	{
		settingsData = obj;
	}
}
