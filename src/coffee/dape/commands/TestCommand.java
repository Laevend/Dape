package coffee.dape.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import coffee.dape.Dape;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.annos.VMap;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl;
import coffee.dape.cmdparsers.astral.parser.ArgSet;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.cmdparsers.astral.suggestions.Suggestions;
import coffee.dape.cmdparsers.astral.types.ArgTypes;
import coffee.dape.exception.MissingAnnotationException;
import coffee.dape.utils.PrintUtils;

/**
 * 
 * @author Laeven
 * 
 */
@CommandEx(name = "test",description = "A command for testing the parser",permission = "arc.command.test",group = "default")
public class TestCommand extends AstralExecutor
{
	public TestCommand() throws MissingAnnotationException
	{
		super(TestCommand.class);
		
		addPath("dunk no arg",CmdSender.PLAYER);
		
		addPath("print message",CmdSender.PLAYER,new ArgSet().of("add").of("something").of("else"));
		
		addPath("say yeet",CmdSender.PLAYER,new ArgSet().of("yeet"));
		
		addPath("hidden",CmdSender.PLAYER,new ArgSet().of("hidden"));
		
		addPath("elevated",CmdSender.ANY,new ArgSet().of("elevated"));
		
		addPath("all worlds",CmdSender.PLAYER,
				new ArgSet()
				.of("extra")
				.of("dynamic")
				.of("<worlds>",ArgTypes.STRING,Suggestions.allWorlds())
				.of("<item>",ArgTypes.STRING,Suggestions.minecraftMaterials())
				.of("<amount>",ArgTypes.INT));
		
		addPath("long string test",CmdSender.PLAYER,
				new ArgSet()
				.of("large")
				.of("strings")
				.of("<full name>",ArgTypes.STRING)
				.of("<description>",ArgTypes.STRING)
				.of("<age>",ArgTypes.INT));
		
		addPath("repeat msg",CmdSender.PLAYER,
				new ArgSet()
				.of("<msg>",ArgTypes.STRING)
				.of("<repeats>",ArgTypes.INT));
		
		addPath("suggestion based on arg",CmdSender.PLAYER,
				new ArgSet()
				.of("help")
				.of("<command>",ArgTypes.STRING,Suggestions.commandNames())
				.of(1,Suggestions.commandPathIndexes()));
		
		addPath("mapped args",CmdSender.PLAYER,
				new ArgSet()
				.of("mappedArgs")
				.of("<material>",ArgTypes.MATERIAL,Suggestions.minecraftMaterials()).mapTo("mat")
				.of("<amount>",ArgTypes.INT).mapTo("amount")
				.of("<message>",ArgTypes.STRING).mapTo("msg")
				.of("<say message>",ArgTypes.BOOLEAN).mapTo("sayMsg"));
	}
	
	@CmdPath(name = "dunk no arg",description = "A command to test no arguments",syntax = "/test",usage = "/test")
	public boolean first(CommandSender sender,String[] args)
	{
		PrintUtils.info(sender,"Hello there! NO ARGS!");
		
		try
		{
			ElevatedAccountCtrl.getConsoleAccount().execute();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	@CmdPath(name = "print message",description = "A command to test static arguments",syntax = "/test add something else",usage = "/test add something else")
	public boolean printMessage(CommandSender sender,String[] args)
	{
		System.out.println("sender null? " + sender == null);
		PrintUtils.info(sender,"Hello there!");
		return true;
	}
	
	@CmdPath(name = "say yeet",description = "A command to test a single static argument",syntax = "/test yeet",usage = "/test yeet")
	public boolean sayYeet(CommandSender sender,String[] args)
	{
		PrintUtils.info(sender,"Yeet!");
		return true;
	}
	
	@CmdPath(name = "hidden",description = "A command that should be hidden",syntax = "/test hidden",usage = "/test hidden",hidden = true)
	public boolean hiddenCommand(CommandSender sender,String[] args)
	{
		PrintUtils.info(sender,"You found a hidden command!");
		return true;
	}
	
	@CmdPath(name = "elevated",description = "A command that should request TOTP code",syntax = "/test elevated",usage = "/test elevated")
	public boolean elevatedCommand(CommandSender sender,String[] args)
	{
		PrintUtils.info(sender,"This is an elevated command!");
		return true;
	}
	
	@CmdPath(name = "all worlds",description = "Displays a list of worlds for testing.",syntax = "/test extra dynamic <worlds> <item> <amount>",usage = "/test extra dynamic world1 oak_planks 5")
	public boolean allWorlds(CommandSender sender,String[] args)
	{
		PrintUtils.info(sender,"A list of worlds goes here or somethin'");
		return true;
	}
	
	@CmdPath(name = "long string test",description = "A test command for multiple long string arguments.",syntax = "/test large strings <fullname> <description> <age>",usage = "/test large strings \"Zackary Stacks\" \"Some dude who is tired all the time\" 12")
	public boolean longStringTest(CommandSender sender,String[] args)
	{
		PrintUtils.info(sender,"Fullname is " + args[2]);
		PrintUtils.info(sender,"Description is " + args[3]);
		PrintUtils.info(sender,"Age is " + args[4]);
		return true;
	}
	
	@CmdPath(name = "repeat msg",description = "A test for string var arguments in the same suggestion list as static arguments. Prints your message <amount> number of times.",syntax = "/test <message> <amount>",usage = "/test \"Some random message for testing\" 3")
	public boolean repeatMsg(CommandSender sender,String[] args)
	{
		for(int i = 0; i < Integer.parseInt(args[1]); i++)
		{
			PrintUtils.info(sender,args[0]);
		}
		
		return true;
	}
	
	@CmdPath(name = "suggestion based on arg",description = "A test for dependant arguments.",syntax = "/test item <number> <?> hum this boi",usage = "/test item 1 NETHER hum this boi")
	public boolean suggestionBasedOnArg(CommandSender sender,String[] args)
	{
		PrintUtils.info(sender,"Suggestions... so many");
		return true;
	}
	
	@CmdPath(name = "mapped args",description = "A test for mapped args",syntax = "/test mappedArgs <material> <amount> <message> <say message>",usage = "/test mappedArgs sand 5 \"Hi there\" true")
	public boolean mapArgs(Player p,@VMap("sayMsg") boolean say,@VMap("amount") int a,@VMap("mat") Material m,@VMap("msg") String s)
	{
		PrintUtils.info(p,"Aye!");
		PrintUtils.info(p,"Material: " + m.toString());
		PrintUtils.info(p,"Amount: " + a);
		PrintUtils.info(p,"Message: " + s);
		PrintUtils.info(p,"Say: " + say);
		return true;
	}
}