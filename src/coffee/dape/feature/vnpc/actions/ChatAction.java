package coffee.dape.feature.vnpc.actions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import coffee.dape.feature.vnpc.VNpcCtrl;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.GradientUtils;
import coffee.dape.utils.GradientUtils.LinearGradient;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.clocks.RefillableIntervalClock;

public class ChatAction extends VNpcAction
{
	private List<VNpcDialogue> dialogue = new ArrayList<>();
	private DialoguePauseType pauseType;
	private int intervalDuration = 5;
	public static String ACTION_TAG = "chat_action_index";
	
	public ChatAction()
	{
		super(VNpcAction.ActionType.CHAT);
	}

	@Override
	public void execute(Player p)
	{
		if(!VNpcCtrl.hasSessionData(p,ACTION_TAG))
		{
			VNpcCtrl.setSessionData(p,ACTION_TAG,0);
			
			if(pauseType == DialoguePauseType.FIXED)
			{
				VNpcCtrl.getSession(p).setTalkClock(new ChatAction.ChatClock(intervalDuration,this,p));
				VNpcCtrl.getSession(p).getTalkClock().start();
			}
			else if(pauseType == DialoguePauseType.DYNAMIC)
			{
				// Clock initially set with a high duration that will be lowered
				VNpcCtrl.getSession(p).setTalkClock(new ChatAction.ChatClock(9999,this,p));
				VNpcCtrl.getSession(p).getTalkClock().start();
			}
		}
		
		int index = VNpcCtrl.getSessionData(p,ACTION_TAG).getAsInt();
		
		if(index >= dialogue.size())
		{
			PrintUtils.error(p,"Null dialouge [" + index + "]");
			
			if(VNpcCtrl.getSession(p).getTalkClock() != null)
			{
				VNpcCtrl.getSession(p).getTalkClock().stop();
				VNpcCtrl.getSession(p).setTalkClock(null);
			}
		}
		else
		{
			PrintUtils.raw(p,ColourUtils.translate(dialogue.get(index).getFormattedDialogue()));
			int length = ChatColor.stripColor(ColourUtils.translate(dialogue.get(index).getFormattedDialogue())).length();
			
			if((index + 1) >= dialogue.size())
			{
				VNpcCtrl.getSession(p).nextAction();
				VNpcCtrl.removeSessionData(p,ACTION_TAG);
				VNpcCtrl.getSession(p).getTalkClock().stop();
				VNpcCtrl.getSession(p).setTalkClock(null);
				Bukkit.getPluginManager().callEvent(new PlayerInteractEntityEvent(p,VNpcCtrl.getVNpc(VNpcCtrl.getSession(p).getCurrentVNpcId()).getEntity(),EquipmentSlot.HAND));
			}
			else
			{
				VNpcCtrl.setSessionData(p,ACTION_TAG,index + 1);
				
				if(pauseType != DialoguePauseType.INTERACT_ONLY)
				{
					if(pauseType == DialoguePauseType.DYNAMIC)
					{
						VNpcCtrl.getSession(p).getTalkClock().setMaxDuration(length * 2);
						VNpcCtrl.getSession(p).getTalkClock().refill();
					}
				}
			}
		}
	}
	
	public enum DialoguePauseType
	{
		DYNAMIC,			// Pause durations based on length of dialogue
		FIXED,				// Pause durations are fixed
		INTERACT_ONLY		// No pause durations, player must interact again to progress dialogue
	}
	
	public class VNpcDialogue
	{
		private String name;
		private String dialogue;
		
		public VNpcDialogue(String name,String dialogue)
		{
			this.name = name;
			this.dialogue = dialogue;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getDialogue()
		{
			return dialogue;
		}

		public void setDialogue(String dialogue)
		{
			this.dialogue = dialogue;
		}
		
		public String getFormattedDialogue()
		{
			return ColourUtils.translate("&8[" + GradientUtils.applyGradient(name,LinearGradient.of("#56ab2f","#a8e063")) + "&8] " + ColourUtils.applyColour(dialogue,"#cfdef3"));
		}
	}
	
	public static class ChatClock extends RefillableIntervalClock
	{
		private Player listener;
		private ChatAction action;
		
		public ChatClock(long durationInTicks,ChatAction action,Player listener)
		{
			super("VNpc Chat Clock",durationInTicks);
			this.action = action;
			this.listener = listener;
		}

		@Override
		public void execute() throws Exception
		{
			// TODO Auto-generated method stub
			VNpcCtrl.getSession(this.listener).resetDuration();
			action.execute(this.listener);
		}		
	}
}
