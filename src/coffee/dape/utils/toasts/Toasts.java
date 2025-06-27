package coffee.dape.utils.toasts;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import coffee.dape.utils.clocks.RepeatingClock;
import coffee.dape.utils.toasts.Toast.Frame;

/**
 * @author Laeven
 * @since 1.0.0
 * 
 * Toasts are little UI elements that appear at the top right of a players screen.
 * They usually appear in vanilla when a player meets the criteria for an advancement.
 * This class allows custom toasts to be sent to a player or many players.
 * 
 * Use the {@link #queueToast(String, Material, Frame, Player)} method to queue a new toast for sending
 */
public class Toasts
{
	private static Deque<Toast> toastQueue = new ArrayDeque<>();
	@SuppressWarnings("unused")
	private static ToastSendClock toastClock = new ToastSendClock();
	
	public static void queueToast(String text,Material icon,Frame frame,Player player)
	{
		queueToast(text,icon,frame,Arrays.asList(player));
	}
	
	public static void queueToast(String text,Material icon,Frame frame,Collection<? extends Player> players)
	{
		toastQueue.addLast(new Toast(text,icon,frame,players));
	}
	
	public static Deque<Toast> getToastQueue()
	{
		return toastQueue;
	}
	
	/**
	 * Due to toasts needing an extra tick to be sent, we need a queue to prevent a case where the same toast is sent multiple times.
	 * The same toast being sent multiple times will cause the toast to be re-reigstered before it gets removed on the same tick.
	 */
	private static class ToastSendClock extends RepeatingClock
	{
		public ToastSendClock()
		{
			super("Toast Sender Clock",40);
			start();
		}

		@Override
		public void execute() throws Exception
		{
			if(toastQueue.isEmpty()) { return; }
			toastQueue.pollFirst().send();
		}
	}
}
