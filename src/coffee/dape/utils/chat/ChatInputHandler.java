package coffee.dape.utils.chat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import coffee.dape.event.ChatInputEvent;
import coffee.dape.utils.ChatUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.StringUtils;
import coffee.dape.utils.structs.Namespace;

public class ChatInputHandler
{
	private Class<?> clazz;
	private InputListener instance;
	private Map<String,Method> handlers = new HashMap<>();
	
	public ChatInputHandler(Class<?> listenerClass,InputListener listenerInstance)
	{
		Objects.requireNonNull(listenerClass,"Listener class cannot be null!");
		Objects.requireNonNull(listenerInstance,"Listener instance cannot be null!");
		
		this.clazz = listenerClass;
		this.instance = listenerInstance;
		collectHandlers();
	}
	
	/**
	 * Collects the handler methods in this listener
	 */
	public void collectHandlers()
	{
		// Search for input handler methods
		for(Method method : clazz.getDeclaredMethods())
		{
			// Lambda and switch cases used in command logic is registered as a method
			if(method.getName().contains("$")) { continue; }
			
			// Ignore methods with no path annotation
			if(!method.isAnnotationPresent(ChatUtils.InputHandler.class)) { continue; }
			
			ChatUtils.InputHandler handlerAnno = method.getAnnotation(ChatUtils.InputHandler.class);
			
			if(!StringUtils.isNotNullEmptyOrBlank(handlerAnno.value()))
			{
				throw new IllegalArgumentException("InputHandler annotations cannot have a null, empty, or blank value! (" + clazz.getSimpleName() + ")");
			}
			
			// Checks if a collision exists
			if(handlers.containsKey(handlerAnno.value()))
			{
				Logg.error("Input listener class " + clazz.getSimpleName() + " has duplicate handler name(s)!");
				Logg.Common.printFail(Logg.Common.Component.INPUT_LISTENER,"Skipping",clazz.getSimpleName() + " -> " + handlerAnno.value());
				continue;
			}
			
			// Check for 1 parameter (ChatInputEvent)
			if(method.getParameters().length != 1)
			{
				Logg.error("Input listener class " + clazz.getSimpleName() + " has a handler (" + handlerAnno.value() + ") with too many parameters!");
				Logg.Common.printFail(Logg.Common.Component.INPUT_LISTENER,"Skipping",clazz.getSimpleName() + " -> " + handlerAnno.value());
				continue;
			}
			
			Class<?> methodParam = method.getParameters()[0].getType();
			
			// Check that parameter is a ChatInputEvent
			if(!methodParam.getCanonicalName().equals(ChatInputEvent.class.getCanonicalName()))
			{
				Logg.error("Input listener class " + clazz.getSimpleName() + " has a handler (" + handlerAnno.value() + ") with a parameter that is not of ChatInputEvent type! Got: " + methodParam.getCanonicalName());
				Logg.Common.printFail(Logg.Common.Component.INPUT_LISTENER,"Skipping",clazz.getSimpleName() + " -> " + handlerAnno.value());
				continue;
			}
			
			handlers.put(handlerAnno.value(),method);
			Logg.Common.printOk(Logg.Common.Component.INPUT_LISTENER,"Collecting",clazz.getSimpleName() + " -> " + handlerAnno.value());
		}
	}
	
	/**
	 * Calls a handler
	 * @param handlerName Name of the handler
	 * @param event ChatInputEvent
	 */
	public void callHandler(Namespace handlerName,ChatInputEvent event)
	{
		Objects.requireNonNull(handlerName,"Handler name cannot be null!");
		Objects.requireNonNull(event,"ChatInputEvent cannot be null!");
		
		if(!handlers.containsKey(handlerName.getKey()))
		{
			Logg.error("Handler '" + handlerName + "' does not exist as a registered input handler for class '" + clazz.getSimpleName() + "'");
			return;
		}
		
		try
		{
			handlers.get(handlerName.getKey()).invoke(instance,event);
		}
		catch (Exception e)
		{
			Logg.error("Could not invoke chat input event!",e);
		}
	}

	public Set<String> getHandlers()
	{
		return handlers.keySet();
	}
}
