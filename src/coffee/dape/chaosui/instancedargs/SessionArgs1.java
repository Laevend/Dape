package coffee.dape.chaosui.instancedargs;

import org.bukkit.entity.Player;

/**
 * @author Laeven
 * 
 * I could'nt figure out a way to keep the clean implementation while providing the API user
 * as many arguments as they would like to pass into a GUI. So we're going with this ugly mess.
 * 
 * A class to denote 1 type of argument being passed into a ChaosGUI for use during an instance.
 * {@link coffee.dape.chaosui.ChaosBuilder#initSessionComponents(SessionArgs1)}
 */
public interface SessionArgs1<A1>
{
	public static final int NUM_OF_ARGS = 1;
	
	/**
	 * Initialise the GUIs instanced components with 1 argument
	 * @param p Player who owns the session
	 * @param arg1 Argument to pass
	 */
	void initSession(Player p,A1 arg1);
}
