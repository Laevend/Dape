package coffee.dape.cmdparsers.astral.types;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Keyed;
import org.bukkit.Registry;

import coffee.dape.Dape;
import coffee.dape.utils.Logg;
import coffee.dape.utils.interfaces.Initialiser;
import coffee.dape.utils.tools.ClasspathCollector;

/**
 * @author Laeven
 * 
 * This class is a class for quickly accessing existing types
 * for reference when defining new paths in a command
 */
public class ArgTypes implements Initialiser
{
	private static Map<String,ArgumentType> argTypeMap = new HashMap<>();
	
	// Yeah I could just remove these, but for now they're too convenient to use when building new commands.
	// Might remove later if I fully transition over to text only arg set building
	public static final BooleanType BOOLEAN = new BooleanType();
	public static final DoubleType DOUBLE = new DoubleType();
	public static final FloatType FLOAT = new FloatType();
	public static final HexType HEX = new HexType();
	public static final IntegerType INT = new IntegerType();
	public static final LongType LONG = new LongType();
	public static final StringType STRING = new StringType();
	public static final SecureStringType SECURE_STRING = new SecureStringType();
	public static final MaterialType MATERIAL = new MaterialType();
	public static final UUIDType UUID = new UUIDType();
	public static final UID4Type UID4 = new UID4Type();
	public static final NamespaceType NAMESPACE = new NamespaceType();
	public static final OnlinePlayerType ONLINE_PLAYER = new OnlinePlayerType();
	public static final OfflinePlayerType OFFLINE_PLAYER = new OfflinePlayerType();
	public static final PlayerType PLAYER = new PlayerType();
	
	// Converts an enum into its enum ArgumentType counterpart
	public static <T extends Enum<T>> EnumType<T> ENUM(Class<T> enumObj)
	{
		return EnumType.of(enumObj);
	}
	
	// Converts a Registry into its Registry ArgumentType counterpart
	public static <R extends Keyed> RegistryType<R> REGISTRY(Registry<R> enumObj)
	{
		return RegistryType.of(enumObj);
	}
	
	static
	{
		init();
	}
	
	private static void init()
	{
		ClasspathCollector cc = new ClasspathCollector();
		
		try
		{
			Set<String> classpaths = cc.getClasspathsAssignableFrom(ArgumentType.class);
			String argumentTypeClass = ArgumentType.class.getPackageName() + "." + ArgumentType.class.getSimpleName();
			
			// Special case as enums can't be initialised via empty constructor. Enum class must be passed on init
			String enumTypeClass = EnumType.class.getPackageName() + "." + EnumType.class.getSimpleName();
			
			// Special case as registry types can't be initialised via empty constructor. Registry class must be passed on init
			String regTypeClass = RegistryType.class.getPackageName() + "." + RegistryType.class.getSimpleName();
			
			for(String argTypeClasspath : classpaths)
			{
				if(argTypeClasspath.equals(argumentTypeClass)) { continue; }
				if(argTypeClasspath.equals(enumTypeClass)) { continue; }
				if(argTypeClasspath.equals(regTypeClass)) { continue; }
				
				Class<?> argTypeClass = Class.forName(argTypeClasspath,true,Dape.class.getClassLoader());
				Constructor<?> argTypeConstructor = argTypeClass.getConstructor();
				ArgumentType argType = (ArgumentType) argTypeConstructor.newInstance();
				
				argTypeMap.put(argType.getTypeName(),argType);
			}
		}
		catch(Exception e)
		{
			Logg.error("Unable to collect argument type classes!",e);
		}
	}
	
	/**
	 * Get an argument type via its name
	 * @param argumentTypeName
	 * @return
	 */
	public static ArgumentType get(String argumentTypeName)
	{
		if(!argTypeMap.containsKey(argumentTypeName)) { return null; }
		return argTypeMap.get(argumentTypeName);
	}
	
	protected static void addArgumentType(ArgumentType argType)
	{
		if(argTypeMap.containsKey(argType.getTypeName())) { return; }
		argTypeMap.put(argType.getTypeName(),argType);
	}
}
