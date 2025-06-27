package coffee.dape.chaosui.instancedargs;

import org.bukkit.entity.Player;

/**
 * @author Laeven
 * 
 * I could'nt figure out a way to keep the clean implementation while providing the API user
 * as many arguments as they would like to pass into a GUI. So we're going with this ugly mess.
 * 
 * A class to denote just the Player type being passed into a ChaosGUI for use during an instance.
 * {@link coffee.dape.chaosui.ChaosBuilder#initSessionComponents(SessionPlayer)}
 */
public interface SessionPlayer
{
	public static final int NUM_OF_ARGS = 0;
	
	/**
	 * Initialise the GUIs instanced components with 0 arguments
	 * @param p Player who owns the session
	 */
	void initSession(Player p);
}
