package coffee.dape.commands;

import org.bukkit.entity.Player;

import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.parser.ArgSet;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.exception.MissingAnnotationException;

/**
 * 
 * @author Laeven
 * 
 */
@CommandEx(name = "chaosui",description = "A command for testing the chaos ui framework",group = "default")
public class ChaosUICommand extends AstralExecutor
{
	public ChaosUICommand() throws MissingAnnotationException
	{
		super(ChaosUICommand.class);
		
		addPath("test lore wrapping",CmdSender.PLAYER,new ArgSet().of("lore-wrapping"));
	}
	
	@CmdPath(name = "test lore wrapping",description = "Opens lore wrapping testing gui",syntax = "/chaosui lore-wrapping",usage = "/chaosui lore-wrapping")
	public boolean openLoreWrappingTest(Player sender,String[] args)
	{
		ChaosFactory.open(sender,"LoreWrappingTest");
		return true;
	}
}