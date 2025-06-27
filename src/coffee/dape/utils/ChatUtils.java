package coffee.dape.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import coffee.dape.Dape;
import coffee.dape.event.ChatInputEvent;
import coffee.dape.utils.chat.ChatInputHandler;
import coffee.dape.utils.chat.InputListener;
import coffee.dape.utils.structs.Namespace;
import coffee.dape.utils.tools.ClasspathCollector;

/**
 * 
 * @author Laeven
 * Temporarily uses chat as a way of accepting text input.
 * Listen to {@linkplain coffee.dape.event.ChatInputEvent}
 */
public class ChatUtils
{
	private static ConcurrentHashMap<UUID,ChatInputSession> inputSessions = new ConcurrentHashMap<>();
	
	// Map<Namespace:HandlerName,InputListener>
	private static final Map<Namespace,ChatInputHandler> listeners = new HashMap<>();
	private static final Map<Namespace,ChatInputHandler> tempListeners = new HashMap<>();
	
	/**
	 * Initialises chat input handlers
	 */
	public static void init()
	{
		Logg.title("Collecting Chat Input Handlers...");
		
		try
		{
			ClasspathCollector collector = new ClasspathCollector(Dape.getPluginPath(),Dape.class.getClassLoader());
			Set<String> inputListenerClasses = collector.getClasspathsAssignableFrom(InputListener.class);
			
			for(String clazz : inputListenerClasses)
			{
				Class<?> listenerClass = Class.forName(clazz,false,Dape.class.getClassLoader());
				Constructor<?> listenerConst = listenerClass.getDeclaredConstructor();
				InputListener listener = (InputListener) listenerConst.newInstance();
				
				ChatInputHandler handler = new ChatInputHandler(listenerClass,listener);
				
				for(String handlerName : handler.getHandlers())
				{
					listeners.put(Namespace.of(Dape.getNamespaceName(),handlerName),handler);
				}
			}
		}
		catch (Exception e)
		{
			Logg.fatal("An error occured collecting and initialising input listeners!",e);
		}
	}
	
	/**
	 * Initialises external chat input handlers not part of Dape
	 * @param pathOfPluginJar Path of the plugin jar file containing input listeners
	 * @param pluginClassloader Plugins class loader
	 * @param pluginNamespaceName A suitable namespace name used to initialise these listeners under
	 */
	public static void initExternalInputListeners(Path pathOfPluginJar,ClassLoader pluginClassloader,String pluginNamespaceName)
	{
		Logg.title("Collecting Chat Input Handlers...");
		
		try
		{
			ClasspathCollector collector = new ClasspathCollector(pathOfPluginJar,pluginClassloader);
			Set<String> inputListenerClasses = collector.getClasspathsAssignableFrom(InputListener.class);
			
			for(String clazz : inputListenerClasses)
			{
				Class<?> listenerClass = Class.forName(clazz,false,pluginClassloader);
				Constructor<?> listenerConst = listenerClass.getDeclaredConstructor();
				InputListener listener = (InputListener) listenerConst.newInstance();
				
				ChatInputHandler handler = new ChatInputHandler(listenerClass,listener);
				
				for(String handlerName : handler.getHandlers())
				{
					listeners.put(Namespace.of(pluginNamespaceName,handlerName),handler);
				}
			}
		}
		catch (Exception e)
		{
			Logg.fatal("An error occured collecting and initialising input listeners!",e);
		}
	}
	
	/**
	 * Manually add a chat input listener
	 * @param inputListenerClass Listener class
	 * @param inputListenerInstance Listener instance
	 * @param namespace Namespace for this listener
	 */
	public void addInputListener(Class<?> inputListenerClass,InputListener inputListenerInstance,Namespace namespace)
	{
		Objects.requireNonNull(inputListenerClass,"Listener class cannot be null!");
		Objects.requireNonNull(inputListenerInstance,"Listener instance cannot be null!");
		Objects.requireNonNull(namespace,"Listener namespace cannot be null!");
		
		ChatInputHandler handler = new ChatInputHandler(inputListenerClass,inputListenerInstance);
		listeners.put(namespace,handler);
	}
	
	/**
	 * Removes a chat input listener
	 * @param namespace Namespace for this listener
	 */
	public void removeInputListener(Namespace namespace)
	{
		Objects.requireNonNull(namespace,"Listener namespace cannot be null!");
		
		if(!listeners.containsKey(namespace)) { return; }
		listeners.remove(namespace);
	}
	
	/**
	 * Convenient method for manually adding  input listener.
	 * Used for a use case where a static @InputHandler value doesn't work because the handler value needs to be specific to the instance of the object its in OR its temporary 
	 * @param namespace Namespace for this listener
	 * @param handler ChatInputHandler for this listener
	 */
	public void addTempInputListener(Namespace namespace,ChatInputHandler handler)
	{
		listeners.putIfAbsent(namespace,handler);
	}
	
	public Map<Namespace,ChatInputHandler> getListeners()
	{
		return listeners;
	}
	
	/**
	 * Request text input from player and fires 
	 * @param p Player to request input from
	 * @param message Message explaining what input is needed
	 * @param handlerName Value of the handler method marked with the {@linkplain coffee.dape.utils.ChatUtils.InputHandler} annotation. With or without canonical class name prefix. E.g 'MyHandler' or 'com.my.package.MyClass:MyHandler'
	 * @return input session Id to check for when {@linkplain coffee.dape.event.ChatInputEvent} event fires
	 */
	public static UUID requestInput(Player p,String message,Namespace handlerName)
	{
		Objects.requireNonNull(p,"Player cannot be null!");
		Objects.requireNonNull(message,"Message cannot be null!");
		Objects.requireNonNull(handlerName,"Handler name cannot be null!");
		
		// Remove existing session
		if(inputSessions.contains(p.getUniqueId()))
		{
			inputSessions.get(p.getUniqueId()).endSession();
		}
		
		inputSessions.put(p.getUniqueId(),new ChatInputSession(p.getUniqueId(),handlerName));
		
		PrintUtils.info(p,message);
		
		return inputSessions.get(p.getUniqueId()).getInputSessionId();
	}
	
	private static class ChatInputSession implements Listener
	{
		private final UUID inputSessionId = UUID.randomUUID();
		private UUID owner;
		private Namespace handlerToCall;
		
		public ChatInputSession(UUID owner,Namespace handlerToCall)
		{
			this.owner = owner;
			this.handlerToCall = handlerToCall;
			
			// Register this temporary listener
			Bukkit.getPluginManager().registerEvents(this,Dape.instance());
		}
		
		@EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
		public void onLeave(PlayerQuitEvent e)
		{
			if(!e.getPlayer().getUniqueId().equals(owner)) { return; }
			endSession();
		}
		
		@EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
		public void onChat(AsyncPlayerChatEvent e)
		{
			if(!e.getPlayer().getUniqueId().equals(owner)) { return; }
			e.setCancelled(true);
			
			// Call ChatInputEvent synchronously
			DelayUtils.executeDelayedTask(() ->
			{
				if(!listeners.containsKey(handlerToCall) && !(tempListeners.containsKey(handlerToCall)))
				{
					PrintUtils.error(e.getPlayer(),"An error occured attempting to read your input!");
					Logg.error("Could not handle chat input as handler '" + handlerToCall + "' doesn't exist!");
				}
				else
				{
					listeners.get(handlerToCall).callHandler(handlerToCall,new ChatInputEvent(e.getPlayer(),e.getMessage(),this.inputSessionId,this.owner));
				}
			});
			
			endSession();
		}
		
		public void endSession()
		{
			PlayerQuitEvent.getHandlerList().unregister(this);
			AsyncPlayerChatEvent.getHandlerList().unregister(this);
			inputSessions.remove(this.owner);
		}

		public UUID getInputSessionId()
		{
			return inputSessionId;
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface InputHandler
	{
		/**
		 * Name of the chat input method to call
		 * @return Chat input method name
		 */
		public String value();
	}
	
	public class HandlerNames
	{
		public static final String ELEVATED_ACCOUNTS_CONSOLE_SETUP = "ElevatedAccountsConsoleSetup";
		public static final String ELEVATED_ACCOUNTS_VIEW_SECRET = "ElevatedAccountsViewSecret";
		
		public static final String ELEVATED_ACCOUNTS_AUTH_METHOD_SETUP_STATIC_PIN = "ElevatedAccountsAuthMethodSetupStaticPin";
		public static final String ELEVATED_ACCOUNTS_AUTH_METHOD_SETUP_TOTP = "ElevatedAccountsAuthMethodSetupTOTP";
		public static final String ELEVATED_ACCOUNTS_AUTH_METHOD_SETUP_EMAIL_OTP = "ElevatedAccountsAuthMethodSetupEmailOTP";
	}
}
