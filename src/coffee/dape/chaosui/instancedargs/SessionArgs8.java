package coffee.dape.chaosui.instancedargs;

import org.bukkit.entity.Player;

/**
 * @author Laeven
 * 
 * I could'nt figure out a way to keep the clean implementation while providing the API user
 * as many arguments as they would like to pass into a GUI. So we're going with this ugly mess.
 * 
 * A class to denote 8 types of argument being passed into a ChaosGUI for use during an instance.
 * {@link coffee.dape.chaosui.ChaosBuilder#initSessionComponents(SessionArgs8)}
 */
public interface SessionArgs8<A1,A2,A3,A4,A5,A6,A7,A8>
{
	public static final int NUM_OF_ARGS = 8;
	
	/**
	 * Initialise the GUIs instanced components with 8 arguments
	 * @param p Player who owns the session
	 * @param arg1 Argument to pass
	 * @param arg2 Argument to pass
	 * @param arg3 Argument to pass
	 * @param arg4 Argument to pass
	 * @param arg5 Argument to pass
	 * @param arg6 Argument to pass
	 * @param arg7 Argument to pass
	 * @param arg8 Argument to pass
	 */
	void initSession(Player p,A1 arg1,A2 arg2,A3 arg3,A4 arg4,A5 arg5,A6 arg6,A7 arg7,A8 arg8);
}
