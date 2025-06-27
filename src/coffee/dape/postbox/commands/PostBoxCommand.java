package coffee.dape.postbox.commands;

import org.bukkit.entity.Player;

import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.exception.MissingAnnotationException;

/**
 * 
 * @author Laeven
 *
 */
@CommandEx(name = "postbox",alias = {"pb"},description = "Open your post box")
public class PostBoxCommand extends AstralExecutor
{
	public PostBoxCommand() throws MissingAnnotationException
	{
		super(PostBoxCommand.class);
		
		addPath("open",CmdSender.PLAYER);
	}
	
	@CmdPath(name = "open",description = "Opens the message area interface",syntax = "/messagearea",usage = "/messagearea")
	public void open(Player p)
	{
		ChaosFactory.open(p,ChaosFactory.Common.POST_BOX);
	}
}