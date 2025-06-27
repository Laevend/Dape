package coffee.dape.cmdparsers.astral.annos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Laeven
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CmdPath
{
	/**
	 * Name of the path
	 * 
	 * <p>This is used to identify this method
	 * @return name of the path
	 */
	public String name();
	
	/**
	 * Description of the path
	 * @return Syntax description
	 */
	public String description();
	
	/**
	 * Path permission of this command required to execute it
	 * 
	 * <p>If base permission is blank ("") then command permissions becomes
	 * <\plugin / module name\>.command.<\command name\>.<\path name\>
	 * 
	 * <p>Example dape.command.help.gui
	 * @return Main permission of this command
	 */
	public String permission() default "";
	
	/**
	 * Syntax of sub command
	 * @return Syntax's syntax
	 */
	public String syntax();
	
	/**
	 * A usage example of the path command being used
	 * @return A example of how this command can be typed
	 */
	public String usage();
	
	/**
	 * If this path should be hidden and not show tab-complete when typing
	 * @return 
	 */
	public boolean hidden() default false;
}