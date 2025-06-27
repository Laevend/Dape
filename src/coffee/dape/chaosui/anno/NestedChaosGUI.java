package coffee.dape.chaosui.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Laeven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NestedChaosGUI
{
	/**
	 * Holds the class of the handler of this GUI
	 * @return GuiHandler or Object.class aka This GUI has no handler!
	 */
	public Class<?> handler() default Object.class;
}