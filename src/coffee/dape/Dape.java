package coffee.dape;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl;
import coffee.dape.cmdparsers.astral.parser.CommandFactory;
import coffee.dape.config.Configurable;
import coffee.dape.config.DapeConfig;
import coffee.dape.config.YamlConfig;
import coffee.dape.config.items.ConfigItem;
import coffee.dape.feature.vaults.VaultCtrl;
import coffee.dape.feature.wildfires.WildFiresCtrl;
import coffee.dape.listeners.TestLis;
import coffee.dape.playerdata.PlayerDataCtrl;
import coffee.dape.utils.ChatUtils;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.EntityUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.MapUtils.ImageMapper;
import coffee.dape.utils.MaterialUtils;
import coffee.dape.utils.StringUtils;
import coffee.dape.utils.data.DataUtils;
import coffee.dape.utils.structs.Namespace;
import coffee.dape.utils.tools.ClasspathCollector;

public final class Dape extends JavaPlugin
{
	private static Dape INSTANCE;
	private static Path DAPE_PLUGIN_PATH;
	private static final String NAMESPACE_NAME = "DAPE";
	private static final String DT_PLUGIN_MANAGED_ENTITY = "plugin_managed_entity";
	private static DapeConfig config = null;
	
	private static long initStartTime = 0;
	private static long initEndTime = 0;
	
	/* =================== *
	 *   Pre-Bukkit Load
	 * =================== */
	
	@Override
	public void onEnable()
	{
		initStartTime = System.currentTimeMillis();
		INSTANCE = this;
		DAPE_PLUGIN_PATH = Path.of(getFile().getAbsolutePath());
		createConfig();
		printHeader();
		
		configureLogger();
		ElevatedAccountCtrl.init();
		CommandFactory.collectAndInitLocal();
		ChaosFactory.init();
		ChatUtils.init();
		ImageMapper.load();
		
		MaterialUtils.collectBlocksAndItems();
		EntityUtils.collectLivingEntities();
		
		initEndTime = System.currentTimeMillis();
		
		Logg.info("&f&lDape Pre-Bukkit load initialised in " + ((initEndTime - initStartTime) / 1000F) + " seconds");
		
		// For the time being until I built the auto listener register, we manually init
		Bukkit.getPluginManager().registerEvents(new TestLis(),this);
		//Bukkit.getPluginManager().registerEvents(new ElevatedAccountCtrl(),this);
		Bukkit.getPluginManager().registerEvents(new ImageMapper(),this);
		//Bukkit.getPluginManager().registerEvents(new SecretViewWarning(),this);
		Bukkit.getPluginManager().registerEvents(new WildFiresCtrl(),this);
		Bukkit.getPluginManager().registerEvents(new VaultCtrl(),this);
		Bukkit.getPluginManager().registerEvents(new PlayerDataCtrl(),this);
		Bukkit.getPluginManager().registerEvents(new ChaosFactory(),this);
		
		long ll = ElevatedAccountCtrl.Config.AUTH_TIME.get();
		long l = (ll / 50) + 1;
		
//		ConfigItem<List<Long>> longListExample = new ConfigItem<>("test.long_list",List.of(55L,99L));
//		ConfigItem<List<Boolean>> booleanListExample = new ConfigItem<>("test.boolean_list",List.of(true,false));
//		ConfigItem<List<Integer>> intListExample = new ConfigItem<>("test.int_list",List.of(55,99));
//		ConfigItem<List<Double>> doubleListExample = new ConfigItem<>("test.double_list",List.of(55.1d,99.1d));
//		ConfigItem<List<Float>> floatListExample = new ConfigItem<>("test.float_list",List.of(55.55f,99.77f));
//		ConfigItem<List<String>> stringListExample = new ConfigItem<>("test.string_list",List.of("Fuck","you"));
//		
//		longListExample.get().forEach(System.out::println);
//		booleanListExample.get().forEach(System.out::println);
//		intListExample.get().forEach(System.out::println);
//		doubleListExample.get().forEach(System.out::println);
//		floatListExample.get().forEach(System.out::println);
//		stringListExample.get().forEach(System.out::println);
//		
//		ConfigItem<Long> longExample = new ConfigItem<>("test.long_test",30000000000000000L);
//		ConfigItem<Boolean> booleanExample = new ConfigItem<>("test.boolean_test",false);
//		ConfigItem<Integer> intExample = new ConfigItem<>("test.int_test",55);
//		ConfigItem<Double> doubleExample = new ConfigItem<>("test.double_test",55.1d);
//		ConfigItem<Float> floatExample = new ConfigItem<>("test.float_test",99.77f);
//		ConfigItem<String> stringExample = new ConfigItem<>("test.string_test","bork");
//		
//		System.out.println(longExample.get());
//		System.out.println(booleanExample.get());
//		System.out.println(intExample.get());
//		System.out.println(doubleExample.get());
//		System.out.println(floatExample.get());
//		System.out.println(stringExample.get());
	}
	
	@Override
	public void onDisable()
	{
		ImageMapper.save();
		PlayerDataCtrl.saveAllPlayerData(true);
	}
	
	private void createConfig()
	{
		Map<String,Object> defaults = new HashMap<>();
		
		Logg.title("Collecting Default Configuration Values...");
		
		try
		{
			ClasspathCollector collector = new ClasspathCollector(DAPE_PLUGIN_PATH,Dape.class.getClassLoader());
			Set<String> configurableClasses = collector.getClasspathsAssignableFrom(Configurable.class);
			
			for(String clazz : configurableClasses)
			{
				Class<?> configurableClass = Class.forName(clazz,false,Dape.class.getClassLoader());
				String className;
				
				/**
				 * Dape has a pattern practice of config items going in a nested 'Config' class.
				 * 
				 * This makes it easier to access from an IDE when typing and this method spends less
				 * time combing through fields that are not of ConfigItem type.
				 * 
				 * Unfortunately it's not useful to have every configuration class called 'config' in the logs.
				 * 
				 * So we attempt to get the nested class name instead.
				 */
				if(configurableClass.getSimpleName().equals("Config"))
				{
					String[] canonicalNameSplit = configurableClass.getCanonicalName().split("[.]");
					className = StringUtils.capitaliseFirstLetter(canonicalNameSplit[canonicalNameSplit.length - 2]);
				}
				else
				{
					className = configurableClass.getSimpleName() + " S I M P L E";
				}
				
				for(Field field : configurableClass.getDeclaredFields())
				{
					// Ignore fields that are not of ConfigItem
					if(!field.getType().equals(ConfigItem.class)) { continue; }
					
					try
					{
						ConfigItem<?> configItem = (ConfigItem<?>) field.get(null);
						defaults.put(configItem.getKey(),configItem.getDefaultValue());
						Logg.Common.printOk(Logg.Common.Component.CONFIG,"Collecting","(" + className + ") " + configItem.getKey());
					}
					catch(Exception e)
					{
						Logg.error("Configurable class " + configurableClass.getSimpleName());
						Logg.Common.printFail(Logg.Common.Component.CONFIG,"Collecting","(" + className + ") " + "???");
					}
				}
			}
		}
		catch (Exception e)
		{
			Logg.fatal("Configurables could not be initialised!",e);
		}
		
		/**
		 * If swapping to YAML, comment out TomlConfig initialiser and uncomment YamlConfig
		 */
		
		//config = new TomlConfig(internalFilePath("config.toml"),defaults,"Dape config file");
		
		config = new YamlConfig(internalFilePath("config.yml"),defaults,"Dape config file");
	}
	
	private void configureLogger()
	{
		try
		{
			// Register all verbose groups
			for(Field f : Logg.VerbGroup.class.getDeclaredFields())
			{
				Namespace verboseGroup = (Namespace) f.get(null);
				Logg.registerVerboseLogGroup(verboseGroup);
			}
		}
		catch(Exception e)
		{
			Logg.error("Error occured attempting to register logger verbose groups!",e);
		}
		
		Logg.setHideVerbose(config.getBoolean("logger.hide_verbose"));
		Logg.setHideWarnings(config.getBoolean("logger.hide_warnings"));
		Logg.setHideErrors(config.getBoolean("logger.hide_errors"));
		Logg.setHideFatals(config.getBoolean("logger.hide_fatals"));
		Logg.setSilenceExceptions(config.getBoolean("logger.hide_exceptions"));
		Logg.setWriteExceptions(config.getBoolean("logger.write_warnings"));
		
		List<String> enabledVerboseGroups = (List<String>) config.getStringList("logger.verbose.enabled_groups");
		if(enabledVerboseGroups == null) { return; }
		
		for(String groupName : enabledVerboseGroups)
		{
			Logg.setVerboseGroupEnabled(Namespace.fromString(groupName),true);
		}
	}
	
	/**
	 * Used to shutdown the server in times when the server is left in a state that cannot be recovered
	 * Shutting down the server prevents further data degradation and unpredictable server states
	 */
	public static void forceShutdown()
	{
		Logg.error("A fatal error has occured. The server will be forcefully shutdown to prevent further damage.");
		Bukkit.getServer().shutdown();
	}
	
	/**
	 * Used to shutdown the server in times when the server is left in a state that cannot be recovered
	 * Shutting down the server prevents further data degradation and unpredictable server states
	 */
	public static void forceShutdown(String reason)
	{
		Logg.error("The server is being forcefully shutdown. Reason: " + reason);
		Bukkit.getServer().shutdown();
	}
	
	/**
	 * Prints header for Dape
	 */
	private final void printHeader()
	{
		// String builder necessary to create a single string otherwise the logger prints the time for each line
		StringBuilder sb = new StringBuilder();
		
		sb.append("\r" + String.format("%" + 400 + "s", "") + "\n\n\n");
		
		for(String s : logo)
		{
			sb.append(s);
		}
		
		sb.append("\n\n\n");
		
		Logg.raw(ColourUtils.translate(sb.toString()));
	}
	
	public static final Dape instance()
	{
		return INSTANCE;
	}
	
	public static Path featureFilePath(String path)
	{
		Path p = Paths.get(Dape.instance().getDataFolder().getPath() + File.separator + "feature" + File.separator + path);		
		return p; 
	}
	
	/**
	 * Returns an internal file path for dapes plugin data folder with an appended directory path
	 * @param path Appended directory path starting from ./plugins/Dape/
	 * @return Path of directory or file internal to dapes plugin data directory
	 */
	public static Path internalFilePath(String path)
	{
		Path p = Paths.get(Dape.instance().getDataFolder().getPath() + File.separator + path);		
		return p; 
	}
	
	public static final Path getPluginPath()
	{
		return DAPE_PLUGIN_PATH;
	}
	
	public static String getNamespaceName()
	{
		return NAMESPACE_NAME;
	}
	
	public static NamespacedKey getNamespacedKey()
	{
		return new NamespacedKey(Dape.instance(),getNamespaceName());
	}

	public static int getMajorVersion()
	{
		String major = INSTANCE.getDescription().getVersion().split("[.]")[0];
		return major.length() == 0 ? 0 : Integer.parseInt(major);
	}

	public static int getMinorVersion()
	{
		String minor = INSTANCE.getDescription().getVersion().split("[.]")[1];
		return minor.length() == 0 ? 0 : Integer.parseInt(minor);
	}

	public static int getRevision()
	{
		String patch = INSTANCE.getDescription().getVersion().split("[.]")[2];
		return patch.length() == 0 ? 0 : Integer.parseInt(patch);
	}
	
	public static int getHotfix()
	{
		String hotfix = INSTANCE.getDescription().getVersion().split("[.]")[3];
		return hotfix.length() == 0 ? 0 : Integer.parseInt(hotfix);
	}

	public static String getVersion()
	{
		return INSTANCE.getDescription().getVersion();
	}
	
	public static DapeConfig getConfigFile()
	{
		return config;
	}
	
	@Override
	public void saveConfig()
	{
		config.saveConfig();
	}
	
	public void reloadConfig()
	{
		config.reloadConfig();
	}
	
	@Override
	public void saveDefaultConfig()
	{
		saveConfig();
	}
	
	@Override
	public FileConfiguration getConfig()
	{
		throw new UnsupportedOperationException("The default configuration is not supported! Please use 'Dape.getConfigFile()");
	}
	
	/**
	 * Signs an entity making it identify as plugin managed
	 * @param e Entity
	 */
	public static void setAsPluginManaged(Entity e)
	{
		DataUtils.set(DT_PLUGIN_MANAGED_ENTITY,1,e);
	}
	
	/**
	 * Checks if this entity is managed by this plugin by looking for a data tag
	 * @param e Entity to check
	 * @return True if this a plugin managed entity, false otherwise
	 */
	public static boolean isPluginManaged(Entity e)
	{
		return DataUtils.has(NAMESPACE_NAME + ":" + DT_PLUGIN_MANAGED_ENTITY,e);
	}

	private final String[] logo = new String[]
	{
		"          &c                 ++++++-+++++++++-                 &r\r\n",
		"          &c             --+##++-++###########++--             &r\r\n",
		"          &c          +-++###     ++########## +---+-          &r\r\n",
		"          &c        -++######++-   -+#########+   +#+++        &r\r\n",
		"          &c      -++##########++-  ++########-   -#####+      &r\r\n",
		"          &c     -+##############+++ ++#######-   +### #+-               &8[ &cD A P E &8]&r\r\n",
		"          &c   -+++---++###########++ ########-  -+######++-   &r\r\n",
		"          &c  -++-     +----++######++ #######-  +#########+-            &9Version &8> &e" + getDescription().getVersion() + "&r\r\n",
		"          &c -+#+            ++++############+  -+#######++++-           &9Message Prefix &8> &8[&cD&8]&r\r\n",
		"          &c +##++----------++###############+ ++#######+- -++           &9Spigot API &8> &e" + getDescription().getAPIVersion() + "&r\r\n",
		"          &c.+############################### ++#######+-   ++.          &9Contributors &8> &8[&e" + String.join(",",getDescription().getAuthors()) + "&8]&r\r\n",
		"          &c-+##+############++++#######+#+###+#####++--    ++-          &9Bukkit Ver &8> &e" + Bukkit.getBukkitVersion() + "&r\r\n",
		"          &c-+#++######+###+####+#+++++++##++++###++--   .-+#+-&r\r\n",
		"          &c-######+--++#  +##############+####++++#   --+####-&r\r\n",
		"          &c-+#+++-.   +###+#############+#++##+  #+--+####+#+-&r\r\n",
		"          &c-++---   .-++++++++++++++++++####+#+++++++++++++#+-&r\r\n",
		"          &c-++    .-+++++++++#+###+###+++++++++++++++++++++++.&r\r\n",
		"          &c.++   -++++++++++++++++++++++++++++++++++++++++ ++.&r\r\n",
		"          &c -++#-+++++++++- ++++++#+++++++###+-.........--++- &r\r\n",
		"          &c .++++++++++++. .+++++++++++++++++            +++. &r\r\n",
		"          &c  --+++++++++-  .+++++++ -+++++++++-....     .+--  &r\r\n",
		"          &c   .-++++++++.  .++++++++ --+++++++++++++...---.   &r\r\n",
		"          &c     .++ +++-   .++++++++. +-+++++++++++++ ++.     &r\r\n",
		"          &c      .-++#+.   .++++++++++  .-+++++++++#++-.      &r\r\n",
		"          &c        .--+-   -+++++++++-.   ..--+++++--.        &r\r\n",
		"          &c          ...... ++++++++++-.      ++--..          &r\r\n",
		"          &c             ...-+++++++++++--........             &r\r\n",
		"          &c                 .................                 &r\r\n",
	};	
}