package coffee.dape.postbox;

import java.time.LocalDateTime;
import java.util.UUID;

import coffee.dape.chaosui.interfaces.paginator.PaginatorItem;

/**
 * Interface to denote a post box item
 */
public interface PostItem extends PaginatorItem
{
	public UUID getId();

	public UUID getSender();

	public void setSender(UUID sender);

	public LocalDateTime getDateSent();

	public void setDateSent(LocalDateTime dateSent);
}
