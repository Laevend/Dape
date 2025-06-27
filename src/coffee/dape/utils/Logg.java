package coffee.dape.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bukkit.Bukkit;

import coffee.dape.Dape;
import coffee.dape.config.Configurable;
import coffee.dape.config.items.ConfigItem;
import coffee.dape.utils.structs.Namespace;

/**
 * @author Laeven
 * @since 1.0.0
 */
public class Logg
{
	public final static String DAPE_PREFIX = "&8[&cD&8]";
	
	private final static String debugPrefix = "&8[&7DBUG&8] ";
	private final static String verbosePrefix = "&8[&bVERB&8] ";
	private final static String infoPrefix = "&8[&9INFO&8] ";
	private final static String warningPrefix = "&8[&eWARN&8] ";
	private final static String errorPrefix = "&8[&cERROR&8] ";
	private final static String fatalPrefix = "&8[&4FATAL&8] ";
	
	private static boolean hideVerbose = false;
	private static boolean hideWarnings = false;
	private static boolean hideErrors = false;
	private static boolean hideFatals = false;
	private static boolean silenceExceptions = false;
	private static boolean writeExceptions = true;
	
	// Grouping of verbose messages to not spam the console and only show certain groups of verbose messages when debugging
	private static Set<Namespace> enabledVerboseGroups = new HashSet<>();
	private static Set<Namespace> verboseGroups = new HashSet<>();
	
	public static synchronized void emptyDivider(int emptyLines)
	{
		raw("\r" + String.format("%" + 400 + "s", ""));
		
		for(int i = 1; i < emptyLines; i++)
		{
			raw("\n");
		}
	}
	
	/**
	 * Gets current time in hrs, minutes, and seconds
	 * @return Formatted current time
	 */
	private static synchronized String getTime()
	{
		return DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
	}
	
	/**
	 * Prints a message to the console with no sub-prefix
	 * @param s Message
	 */
	public static synchronized void raw(String s)
	{
		Bukkit.getServer().getConsoleSender().sendMessage(s);
	}
	
	/**
	 * Prints a message to the console
	 * @param s String to print
	 */
	public static void print(String s)
	{
		// Why the large gap? Well it's to print empty space over the '[00:00:00 INFO]:' print header
		Bukkit.getServer().getConsoleSender().sendMessage("\r                \r[" + getTime() + "] " + ColourUtils.translate(DAPE_PREFIX + " " + s));
	}
	
	/**
	 * Prints a blank message to the console
	 */
	public static void print()
	{
		// Why the large gap? Well it's to print empty space over the '[00:00:00 INFO]:' print header
		Bukkit.getServer().getConsoleSender().sendMessage("\r                \r[" + getTime() + "]");
	}
	
	/**
	 * Prints a message to the console without the time or plugin message prefix
	 * @param s String to print
	 */
	public static void printFromBlank(String s)
	{
		// Why the large gap? Well it's to print empty space over the '[00:00:00 INFO]:' print header
		Bukkit.getServer().getConsoleSender().sendMessage("\r                \r" + ColourUtils.translate(s));
	}
	
	/**
	 * Prints an verbose message
	 * <p>
	 * Use this for showing variables and other messages.
	 * @param s The message
	 * @param verboseGroup Group this message belongs to
	 */
	public static synchronized void verb(String s,Namespace verboseGroup)
	{
		if(hideVerbose) { return; }
		//if(!enabledVerboseGroups.contains(verboseGroup)) { return; }
		
		print(verbosePrefix + getClassAndMethod(Thread.currentThread().getStackTrace()[2]) + "&b" + s);
	}
	
	/**
	 * Prints an temporary debug message.
	 * <p>
	 * Use this for showing variables and other messages.
	 * <p>
	 * These should be removed in a production build and are only meant to exist to help with debugging efforts
	 * @param s The message
	 */
	public static synchronized void debug(String s)
	{
		print(debugPrefix + getClassAndMethod(Thread.currentThread().getStackTrace()[2]) + "&b" + s);
	}
	
	/**
	 * Prints an info message
	 * 
	 * <p>Presents information to the console</p>
	 * @param s The message
	 */
	public static synchronized void info(String s)
	{
		print(infoPrefix + getClassAndMethod(Thread.currentThread().getStackTrace()[2]) + "&9" + s);
	}
	
	/**
	 * Prints an info message
	 * 
	 * <p>Presents information to the console about an action that completed successfully</p>
	 * @param s The message
	 */
	public static synchronized void success(String s)
	{
		print(infoPrefix + getClassAndMethod(Thread.currentThread().getStackTrace()[2]) + "&a" + s);
	}
	
	/**
	 * Prints a warning message
	 * 
	 * <p>Presents information to the console about an action that failed but is not mandatory to succeed to continue operating</p>
	 * @param s The message
	 */
	public static synchronized void warn(String s)
	{
		if(hideWarnings) { return; }
		
		print(warningPrefix + getClassAndMethod(Thread.currentThread().getStackTrace()[2]) + "&e" + s);
	}
	
	/**
	 * Prints a custom message
	 * 
	 * <p>Presents a custom log to the console</p>
	 * @param logName Name of the custom log message
	 * @param logMessage Log message
	 */
	public static synchronized void custom(String logName,String logMessage)
	{
		print("&8[" + logName + "&8] " + getClassAndMethod(Thread.currentThread().getStackTrace()[2]) + "&r" + logMessage);
	}
	
	/**
	 * Prints an error message
	 * 
	 * <p>Presents information to the console about an action that failed. Does not need immediate attention</p>
	 * @param s The message
	 */
	public static synchronized void error(String s)
	{
		if(hideErrors) { return; }
		
		print(errorPrefix + getClassAndMethod(Thread.currentThread().getStackTrace()[2]) + "&c" + s);
	}
	
	/**
	 * Prints an error message with an exception
	 * <p>Presents information to the console about an action that failed. Does not need immediate attention</p>
	 * @param s The message
	 * @param e The exception
	 */
	public static synchronized void error(String s,Exception e)
	{
		if(!hideErrors)
		{
			print(errorPrefix + getClassAndMethod(Thread.currentThread().getStackTrace()[2]) + "&c" + s);
		}
		
		if(!silenceExceptions)
		{
			e.printStackTrace();
		}
		
		if(writeExceptions)
		{
			writeExceptionToDiscord(e);
		}
	}
	
	/**
	 * Prints an fatal message
	 * 
	 * <p>Presents information to the console about an important action that failed. Requires immediate attention of operators</p>
	 * @param s The message
	 */
	public static synchronized void fatal(String s)
	{
		if(hideFatals) { return; }
		
		// This message is designed to get operators attention in the console
		print("&4! ! ! ! ! ! ! ! ! !");
		print(fatalPrefix + getClassAndMethod(Thread.currentThread().getStackTrace()[2]) + "&4" + s);
		print("&4^ ^ ^ ^ ^ ^ ^ ^ ^ ^");
	}
	
	/**
	 * Prints an fatal message with an exception
	 * 
	 * <p>Presents information to the console about an important action that failed. Requires immediate attention of operators</p>
	 * @param s The error message
	 */
	public static synchronized void fatal(String s,Exception e)
	{
		Objects.requireNonNull(s,"Fatal message cannot be null!");
		Objects.requireNonNull(e,"Fatal exception cannot be null!");
		
		if(!hideFatals)
		{
			// This message is designed to get operators attention in the console
			print("&4! ! ! ! ! ! ! ! ! !");
			print(fatalPrefix + getClassAndMethod(Thread.currentThread().getStackTrace()[2]) + "&4" + s);
			print("&4^ ^ ^ ^ ^ ^ ^ ^ ^ ^");
		}
		
		if(!silenceExceptions)
		{
			e.printStackTrace();
		}
		
		if(writeExceptions)
		{
			writeExceptionToDiscord(e);
		}
	}
	
	/**
	 * Throws an illegal argument exception with an error message
	 * 
	 * <p>Presents information to the console about a bad argument.</p>
	 * @param s The error message
	 */
	public static synchronized void throwIllegalArgumentError(String s)
	{
		Objects.requireNonNull(s,"Error message cannot be null!");
		
		print(errorPrefix + getClassAndMethod(Thread.currentThread().getStackTrace()[2]) + "&c" + s);
		throw new IllegalArgumentException(s);
	}
	
	/**
	 * Prints a title in the console (useful for important messages)
	 * @param s The title
	 */
	public static synchronized void title(String s)
	{
		Objects.requireNonNull(s,"Title string cannot be null!");
		
		print();
		print(s);
		print();;
	}
	
	/**
	 * TODO Rename and refactor
	 * Sends an error/fatal message to discord with its exception
	 * @param s Error message
	 * @param e Exception
	 */
	public static synchronized void writeExceptionToDiscord(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		//String sStackTrace = sw.toString(); // stack trace as a string
		
		//CSVUtils.writeCSV(DataPaths.ARC_EXCEPTIONS.getPath() + File.separator + StringUtils.getDateAsString() + ".txt",sStackTrace);
	}
	
	/**
	 * Grabs the class name, line number, and method the logged message was printed from
	 * @param stackTrace stack trace
	 * @return Class name, line number and method name
	 */
	private static synchronized String getClassAndMethod(StackTraceElement stackTrace)
	{		
		String[] classpath  = stackTrace.getClassName().replace(".","-").split("-");
		return "&5" + classpath[classpath.length - 1] + " &8> &d" + stackTrace.getMethodName() + "&8 : &f";
	}

	public static synchronized boolean isHideVerbose()
	{
		return hideVerbose;
	}

	public static synchronized void setHideVerbose(boolean hideVerbose)
	{
		Logg.hideVerbose = hideVerbose;
	}

	public static synchronized boolean isHideWarnings()
	{
		return hideWarnings;
	}

	public static synchronized void setHideWarnings(boolean hideWarnings)
	{
		Logg.hideWarnings = hideWarnings;
	}

	public static synchronized boolean isHideErrors()
	{
		return hideErrors;
	}

	public static synchronized void setHideErrors(boolean hideErrors)
	{
		Logg.hideErrors = hideErrors;
	}

	public static synchronized boolean isHideFatals()
	{
		return hideFatals;
	}

	public static synchronized void setHideFatals(boolean hideFatals)
	{
		Logg.hideFatals = hideFatals;
	}

	public static synchronized boolean isSilenceExceptions()
	{
		return silenceExceptions;
	}

	public static synchronized void setSilenceExceptions(boolean silenceExceptions)
	{
		Logg.silenceExceptions = silenceExceptions;
	}

	public static synchronized boolean isWriteExceptions()
	{
		return writeExceptions;
	}

	public static synchronized void setWriteExceptions(boolean writeExceptions)
	{
		Logg.writeExceptions = writeExceptions;
	}
	
	public static synchronized Set<Namespace> getVerboseGroups()
	{
		return verboseGroups;
	}
	
	public static synchronized void toggleVerboseGroup(Namespace verboseGroupName)
	{
		if(enabledVerboseGroups.contains(verboseGroupName)) { enabledVerboseGroups.remove(verboseGroupName); return; }
		enabledVerboseGroups.add(verboseGroupName);
	}
	
	public static synchronized void setVerboseGroupEnabled(Namespace verboseGroupName,boolean enabled)
	{
		if(enabled)
		{
			enabledVerboseGroups.add(verboseGroupName);
			return;
		}
		
		enabledVerboseGroups.remove(verboseGroupName);
	}
	
	public static class Common
	{
		private static String PREFIX_OK = "&8[&a OK &8] ";
		private static String PREFIX_FAIL = "&8[&cFAIL&8] ";
		
		public static class Component
		{
			public static final String CONFIG = "CFG";
			public static final String LISTENER = "LIS";
			public static final String INPUT_LISTENER = "INP";
			public static final String COMMAND = "CMD";
			public static final String GUI = "GUI";
			public static final String LIBRARY = "LIB";
			public static final String MODULE = "MOD";
		}
		
		/**
		 * Prints a success message showing that this component initialised successfully
		 * @param componentType The type of component being initialised
		 * @param action The action (registering/building/constructing/initialising)
		 * @param componentBeingInitialisedName The name of the component that succeeded in initialising (Usually the name or class name of the component)
		 */
		public static synchronized void printOk(String componentType,String action,String componentBeingInitialisedName)
		{
			// Why the large gap? Well it's to print empty space over the '[00:00:00 INFO]:' print header
			Bukkit.getServer().getConsoleSender().sendMessage("\r                \r[" + getTime() + "] " + ColourUtils.translate(PREFIX_OK + "&3" + componentType + " &8> &6" + action + " &8> &r" + componentBeingInitialisedName));
		}
		
		/**
		 * Prints a failed message showing that this component failed to initialised correctly
		 * @param componentType The type of component being initialised
		 * @param action The action (registering/building/constructing/initialising)
		 * @param componentBeingInitialisedName The name of the component that failed to initialise (Usually the name or class name of the component)
		 */
		public static synchronized void printFail(String componentType,String action,String componentBeingInitialisedName)
		{
			// Why the large gap? Well it's to print empty space over the '[00:00:00 INFO]:' print header
			Bukkit.getServer().getConsoleSender().sendMessage("\r                \r[" + getTime() + "] " + ColourUtils.translate(PREFIX_FAIL + "&3" + componentType + " &8> &6" + action + " &8> &c" + componentBeingInitialisedName));
		}
	}
	
	public static class Config implements Configurable
	{
		public static final ConfigItem<Boolean> HIDE_ALL_VERBOSE = new ConfigItem<>("logger.hide_verbose.all",false,"If all verbose messages should be hidden.");
		public static final ConfigItem<Boolean> HIDE_WARNINGS = new ConfigItem<>("logger.hide_warnings",false,"If all warning messages should be hidden.");
		public static final ConfigItem<Boolean> HIDE_ERRORS = new ConfigItem<>("logger.hide_errors",false,"If all errors messages should be hidden.");
		public static final ConfigItem<Boolean> HIDE_FATALS = new ConfigItem<>("logger.hide_fatals",false,"If all fatal messages should be hidden.");
		public static final ConfigItem<Boolean> HIDE_EXCEPTIONS = new ConfigItem<>("logger.hide_exceptions",false,"If all caught exceptions should be hidden.");
		public static final ConfigItem<Boolean> WRITE_EXCEPTIONS = new ConfigItem<>("logger.write_exceptions",false,"If exceptions should be written to disk.");
		public static final ConfigItem<Boolean> USE_SHORTER_PRINT_PREFIX = new ConfigItem<>("logger.use_shorter_print_prefix",true,"If the shorter print prefix should be used." + 
				" Spigot does not respect \r, as such we can't achieve a message only displaying the time and not the [Server thread/INFO]: crap that's not needed");
		public static final ConfigItem<List<String>> ENABLED_VERBOSE_GROUPS = new ConfigItem<>("logger.verbose.enabled_groups",Collections.emptyList(),"List of verbose groups that are enabled.");
	}
	
	public static void registerVerboseLogGroup(Namespace logGroup)
	{
		verboseGroups.add(logGroup);
	}
	
	public static class VerbGroup
	{
		public static final Namespace MISC = Namespace.of(Dape.getNamespaceName(),"Misc");
		
		public static final Namespace NMSPLAYER = Namespace.of(Dape.getNamespaceName(),"NMSPlayer");
		public static final Namespace ASTRAL_PARSER = Namespace.of(Dape.getNamespaceName(),"Astral_Parser");
		public static final Namespace MINECRAFT_PROFILE = Namespace.of(Dape.getNamespaceName(),"Minecraft_Profile");
		public static final Namespace ELEVATED_ACCOUNT = Namespace.of(Dape.getNamespaceName(),"Elevated_Account");
		public static final Namespace CHAOS_UI = Namespace.of(Dape.getNamespaceName(),"Chaos_UI");
		public static final Namespace COMMANDS = Namespace.of(Dape.getNamespaceName(),"Commands");
		public static final Namespace CLOCKS = Namespace.of(Dape.getNamespaceName(),"Clocks");
		public static final Namespace PLAYER_DATA = Namespace.of(Dape.getNamespaceName(),"Player_Data");
		
		public static final Namespace MAP_UTILS = Namespace.of(Dape.getNamespaceName(),"Map_Utils");
		public static final Namespace TIME_UTILS = Namespace.of(Dape.getNamespaceName(),"Time_Utils");
		public static final Namespace COLOUR_UTILS = Namespace.of(Dape.getNamespaceName(),"Colour_Utils");
		public static final Namespace JSON_UTILS = Namespace.of(Dape.getNamespaceName(),"Json_Utils");
		
		public static final Namespace FEATURE_VAULT = Namespace.of(Dape.getNamespaceName(),"Feature_Vault");
		public static final Namespace FEATURE_VNPC = Namespace.of(Dape.getNamespaceName(),"Feature_VNPC");
	}
}