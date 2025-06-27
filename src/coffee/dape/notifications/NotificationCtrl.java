package coffee.dape.notifications;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import coffee.dape.playerdata.data.PlayerSettings;
import coffee.dape.playerdata.data.PlayerSettings.Setting;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.DelayUtils;
import coffee.dape.utils.GradientUtils;
import coffee.dape.utils.MathUtils;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.SoundUtils;
import coffee.dape.utils.TimeUtils;
import coffee.dape.utils.clocks.RepeatingClock;

public class NotificationCtrl implements Listener
{
	private static TipClock clock;
	
	public static void initTipClock()
	{
		clock = new NotificationCtrl().new TipClock();
		clock.start();
	}
	
	public static void sendAlert(Player player,String message)
	{
		printAlertDivider(player);
		PrintUtils.raw(player,ColourUtils.translate("&c" + message));
		printAlertDivider(player);
		SoundUtils.playSound(player,Sound.ENTITY_BREEZE_DEATH,1.0f);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		PlayerSettings sett = PlayerSettings.getInstance(e.getPlayer());
		
		if(sett.get(Setting.NOTIFICATIONS_GREETING).getAsBoolean())
		{
			DelayUtils.executeDelayedTask(() ->
			{
				sendGreetingMessage(e.getPlayer());
			},10);
		}
	}
	
	private static List<String> greetings = List.of
	(
		"Welcome back, %player%!",
		"G'day to you %player%!",
		"Mornin' %player%!",
		"Aye %player%, you're back!",
		"Doooooooom. I mean, hello there %player%! Haha..",
		"Good day %player%, welcome back!",
		"Howdy %player%!",
		"Hello there %player%!",
		"Back so soon are we %player%?",
		"Nice to see you %player%!",
		"It's a pleasure having you here %player%!",
		"Hey %player%, good to see ya! Hope you stick around!"
	);
	
	private static void sendGreetingMessage(Player p)
	{
		printInfoDivider(p);
		PrintUtils.raw(p,ColourUtils.applyColour(MathUtils.getRandom(greetings).replace("%player%",p.getName()),ColourUtils.LIGHT_GREEN));
		PrintUtils.raw(p,"");
		PrintUtils.raw(p,ColourUtils.applyColour("Type /help for a collection of commands.",ColourUtils.LIGHT_GREEN));
		PrintUtils.raw(p,ColourUtils.applyColour("Type /menu to open your main menu.",ColourUtils.LIGHT_GREEN));
		PrintUtils.raw(p,ColourUtils.applyColour("Type /settings to open your preferences menu.",ColourUtils.LIGHT_GREEN));
		printInfoDivider(p);
	}
	
	public static void sendTipMessage(Player p,String tipMessage)
	{
		PrintUtils.raw(p,"&8[" + ColourUtils.applyColour("Tip",ColourUtils.LIGHT_GREEN) + "&8] &r" + ColourUtils.applyColour(tipMessage,ColourUtils.VISTA_BLUE));
	}
	
	/**
	 * Prints a divider for notifications
	 * @param p
	 */
	public static void printAlertDivider(Player p)
	{
		PrintUtils.sendCenteredMessage(p,
				GradientUtils.generateGradientLine(20,GradientUtils.SAND_TO_BLUE) + 
				ColourUtils.translate("&r &8[") + 
				ColourUtils.applyColour("!",ColourUtils.TEXT_ERROR) + 
				ColourUtils.translate("&8] &r") + 
				GradientUtils.generateGradientLine(20,GradientUtils.SAND_TO_BLUE_REVERSE));
	}
	
	/**
	 * Prints a divider for info
	 * @param p
	 */
	public static void printInfoDivider(Player p)
	{
		PrintUtils.sendCenteredMessage(p,
				GradientUtils.generateGradientLine(20,GradientUtils.SAND_TO_BLUE) + 
				ColourUtils.translate("&r &8[") + 
				GradientUtils.applyGradient("v",GradientUtils.VERTEX_GREEN) + 
				ColourUtils.translate("&8] &r") + 
				GradientUtils.generateGradientLine(20,GradientUtils.SAND_TO_BLUE_REVERSE));
	}
	
	private class TipClock extends RepeatingClock
	{
		private List<String> tips = List.of
		(
			"You can store all your blocks in your block bank! Type '/bb' to open your block bank.",
			"Blueprints can be used to copy and paste large structures. Type '/bp create' to create your blueprint.",
			"Remember to vote to receive vote tokens to spend in the vote shop! Type '/voting' to find out more.",
			"When you die you can type '/back' to get back to your death spot!",
			"Typing '/tpa' on its own will allow you to configure teleport requests for players who teleport to you.",
			"Typing '/homemenu' will display all the homes you can teleport to.",
			"To claim land from the wilderness, type '/axis claim'. You must have at least 1 crying obsidian to do this.",
			"Want to party up? Type '/party create' to create a party. Invite your friends to talk in a private chat channel.",
			"'/chatchannel' can be used to switch between global and party chat.",
			"'/home' will teleport you to whatever home you've assigned as your default home in the '/homemenu'",
			"Need to dump some items you don't need? Typing '/trash' or '/dispose' will open a menu that will allow you to destroy items you no longer need.",
			"Want to get creative with building? '/bwand' might be your thing. It allows you to change certain properties of a block like its shape.",
			"You can customise your experience on the server. Typing '/playersettings' will show a menu that allows you to customise various aspects of the server for yourself.",
			"You can click on a players name in chat to view their join date, time played, and their collection of badges!",
			"You can sit on a block using '/pose sit'",
			"Visit the server store with '/store'. View your purchases in '/purchases'. You can access these menus and more from '/menu'"
		);
		
		private int tipIndex = 0;
		
		public TipClock()
		{
			super("Tip Clock",TimeUtils.minutesToTicks(20));
		}

		@Override
		public void execute() throws Exception
		{
			for(Player p : Bukkit.getOnlinePlayers())
			{
				if(!PlayerSettings.getInstance(p).get(Setting.NOTIFICATIONS_TIPS).getAsBoolean()) { continue; }
				
				sendTipMessage(p,tips.get(tipIndex));
				
				SoundUtils.playSound(p,Sound.BLOCK_NOTE_BLOCK_COW_BELL,2.0f);
				DelayUtils.executeDelayedTask(() ->
				{
					SoundUtils.playSound(p,Sound.BLOCK_NOTE_BLOCK_COW_BELL,1.5f);
				},5);
			}
			
			tipIndex = MathUtils.clampAndRoll(0,(tips.size() - 1),tipIndex,1);
		}
	}
}
