package coffee.dape.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Laeven (Zack)
 * @since 0.6.0
 * 
 * Called when a {@linkplain FixedRateAsyncClock} is terminated via {@linkplain FixedRateAsyncClock#stop()}
 */
public class AsyncClockTerminatedEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private String clockName;
	
	public AsyncClockTerminatedEvent(String clockName)
	{
		this.clockName = clockName;
	}

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}

	public String getClockName()
	{
		return clockName;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}