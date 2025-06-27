package coffee.dape.event;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import coffee.dape.postbox.PostItem;

/**
 * @author Laeven
 * 
 * Called when post is sent to a player
 */
public class PostSentEvent extends PlayerEvent implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private UUID reciever;
	private PostItem itemPosted;
	private boolean cancelled = false;
	
	public PostSentEvent(Player sender,Player reciever,PostItem itemPosted)
	{
		super(sender);
		this.reciever = reciever.getUniqueId();
		this.itemPosted = itemPosted;
	}
	
	public PostSentEvent(Player sender,UUID reciever,PostItem itemPosted)
	{
		super(sender);
		this.reciever = reciever;
		this.itemPosted = itemPosted;
	}
	
	public Player getSender()
	{
		return getPlayer();
	}
	
	public UUID getReciever()
	{
		return reciever;
	}

	public void setReciever(UUID reciever)
	{
		this.reciever = reciever;
	}

	public PostItem getItemPosted()
	{
		return itemPosted;
	}

	public void setItemPosted(PostItem itemPosted)
	{
		this.itemPosted = itemPosted;
	}

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel)
	{
		this.cancelled = cancel;
	}
}