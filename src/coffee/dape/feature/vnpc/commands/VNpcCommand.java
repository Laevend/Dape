package coffee.dape.feature.vnpc.commands;

import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.VMap;
import coffee.dape.cmdparsers.astral.parser.ArgSet;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.cmdparsers.astral.types.ArgTypes;
import coffee.dape.exception.MissingAnnotationException;
import coffee.dape.feature.vnpc.VNpc;
import coffee.dape.feature.vnpc.VNpc.InteractionType;
import coffee.dape.feature.vnpc.VNpcCtrl;
import coffee.dape.feature.vnpc.commands.suggestions.VNpcSuggestions;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.structs.UID4;

/**
 * 
 * @author Laeven
 *
 */
@CommandEx(name = "vnpc",description = "A command for managing villager npcs.")
public class VNpcCommand extends AstralExecutor
{
	public VNpcCommand() throws MissingAnnotationException
	{
		super(VNpcCommand.class);
		
		addPath("create",CmdSender.PLAYER,new ArgSet().of("create").of("<name>",ArgTypes.STRING).mapTo("npc_name"));
		
		addPath("move",CmdSender.PLAYER,new ArgSet().of("move").of("<uid4>",ArgTypes.UID4,VNpcSuggestions.vnpcs()).mapTo("uid"));
		
		addPath("remove",CmdSender.PLAYER,new ArgSet().of("remove").of("<uid4>",ArgTypes.UID4,VNpcSuggestions.vnpcs()).mapTo("uid"));
		
		addPath("modifyAddTrade",CmdSender.PLAYER,new ArgSet().of("modify").of("<uid4>",ArgTypes.UID4,VNpcSuggestions.vnpcs()).mapTo("uid").of("trades").of("add"));
		
		addPath("modifyRemoveTrade",CmdSender.PLAYER,new ArgSet().of("modify").of("<uid4>",ArgTypes.UID4,VNpcSuggestions.vnpcs()).mapTo("uid").of("trades").of("remove").of("<index>",ArgTypes.INT).mapTo("trade_index"));
		
		addPath("nudgeX",CmdSender.PLAYER,new ArgSet().of("nudge").of("<uid4>",ArgTypes.UID4,VNpcSuggestions.vnpcs()).mapTo("uid").of("x").of("<x>",ArgTypes.DOUBLE).mapTo("relative_x"));
		
		addPath("nudgeY",CmdSender.PLAYER,new ArgSet().of("nudge").of("<uid4>",ArgTypes.UID4,VNpcSuggestions.vnpcs()).mapTo("uid").of("y").of("<y>",ArgTypes.DOUBLE).mapTo("relative_y"));
		
		addPath("nudgeZ",CmdSender.PLAYER,new ArgSet().of("nudge").of("<uid4>",ArgTypes.UID4,VNpcSuggestions.vnpcs()).mapTo("uid").of("z").of("<z>",ArgTypes.DOUBLE).mapTo("relative_z"));
		
		addPath("modifyProfession",CmdSender.PLAYER,new ArgSet().of("modify").of("<uid4>",ArgTypes.UID4,VNpcSuggestions.vnpcs()).mapTo("uid").of("profession").of("<profession>",ArgTypes.REGISTRY(Registry.VILLAGER_PROFESSION),VNpcSuggestions.villagerProfessions()).mapTo("profession"));
		
		addPath("modifyType",CmdSender.PLAYER,new ArgSet().of("modify").of("<uid4>",ArgTypes.UID4,VNpcSuggestions.vnpcs()).mapTo("uid").of("type").of("<type>",ArgTypes.REGISTRY(Registry.VILLAGER_TYPE),VNpcSuggestions.villagerTypes()).mapTo("type"));
		
		addPath("modifyInterationType",CmdSender.PLAYER,new ArgSet().of("modify").of("<uid4>",ArgTypes.UID4,VNpcSuggestions.vnpcs()).mapTo("uid").of("interaction").of("<interaction>",ArgTypes.ENUM(InteractionType.class),VNpcSuggestions.vnpcInteractionTypes()).mapTo("interaction"));
		
		addPath("modifyCustomName",CmdSender.PLAYER,new ArgSet().of("modify").of("<uid4>",ArgTypes.UID4,VNpcSuggestions.vnpcs()).mapTo("uid").of("name").of("<name>",ArgTypes.STRING).mapTo("custom_name"));
		
		addPath("info",CmdSender.PLAYER,new ArgSet().of("info").of("<uid4>",ArgTypes.UID4,VNpcSuggestions.vnpcs()).mapTo("uid"));
		
		addPath("forceRefresh",CmdSender.PLAYER,new ArgSet().of("refresh"));
	}
	
	@CmdPath(name = "create",description = "Creates a new vnpc",syntax = "/villagers create <name>",usage = "/villagers create myvillager")
	public void create(Player p,@VMap("npc_name") String name)
	{
		UID4 uid = VNpcCtrl.addVNpc(name,p.getLocation());
		VNpcCtrl.save(uid);
		VNpcCtrl.getVNpc(uid).respawn();
		
		PrintUtils.success(p,"Created villager " + uid.toString());
	}
	
	@CmdPath(name = "move",description = "Moves a villager to where you're standing",syntax = "/villagers move <uid>",usage = "/villagers move mynpc#0493")
	public void move(Player p,@VMap("uid") UID4 uid)
	{
		if(!VNpcCtrl.contains(uid))
		{
			PrintUtils.error(p,"This npc does not exist!");
			return;
		}
		
		VNpc npc = VNpcCtrl.getVNpc(uid);
		npc.setLocation(p.getLocation());
		npc.respawn();
		
		VNpcCtrl.save(uid);
		PrintUtils.success(p,"Villager moved to players location");
	}
	
	@CmdPath(name = "remove",description = "Removes this villager",syntax = "/villagers remove <key>",usage = "/villagers remove mynpc#0493")
	public void remove(Player p,@VMap("uid") UID4 uid)
	{
		if(!VNpcCtrl.contains(uid))
		{
			PrintUtils.error(p,"This npc does not exist!");
			return;
		}
		
		VNpcCtrl.remove(uid);
		PrintUtils.success(p,"Removed villager " + uid.toString());
	}
	
	@CmdPath(name = "modifyAddTrade",description = "Add text to this villager",syntax = "/villagers modify <key> trade add",usage = "/villagers modify mynpc#0493 trade add")
	public void modifyAddTrade(Player p,@VMap("uid") UID4 uid)
	{
		if(!VNpcCtrl.contains(uid))
		{
			PrintUtils.error(p,"This npc does not exist!");
			return;
		}
		
		//GUISession sess = ChaosFactory.getSession(p);
		//sess.setData(ChaosFactory.getGUI(ChaosFactory.Common.ADD_TRADE),AddTradeGuiBuilder.DT_VILLAGER_NPC,uid);
		
		//ChaosFactory.open(p,ChaosFactory.Common.ADD_TRADE);
	}
	
	@CmdPath(name = "modifyRemoveTrade",description = "Remove text on this villager",syntax = "/villagers modify <key> trade remove <index>",usage = "/villagers modify mynpc#0493 trade remove 0")
	public void modifyRemoveTrade(Player p,@VMap("uid") UID4 uid,@VMap("trade_index")int tradeIndex)
	{
		if(!VNpcCtrl.contains(uid))
		{
			PrintUtils.error(p,"This npc does not exist!");
			return;
		}
		
		VNpc npc = VNpcCtrl.getVNpc(uid);
		npc.removeTrade(tradeIndex);
		npc.respawn();
		VNpcCtrl.save(uid);
		PrintUtils.success(p,"VNpc" + uid.toString() + " updated");
	}
	
	@CmdPath(name = "nudgeX",description = "Set the x axis position of this villager",syntax = "/villagers modify <key> x <x>",usage = "/villagers nudge mynpc#0493 x 57.0")
	public void nudgeX(Player p,@VMap("uid") UID4 uid,@VMap("relative_x") double x)
	{
		if(!VNpcCtrl.contains(uid))
		{
			PrintUtils.error(p,"This npc does not exist!");
			return;
		}
		
		VNpc npc = VNpcCtrl.getVNpc(uid);
		npc.setX(npc.getX() + x);
		npc.respawn();
		VNpcCtrl.save(uid);
		PrintUtils.success(p,"VNpc " + uid.toString() + " updated");
	}
	
	@CmdPath(name = "nudgeY",description = "Set the y axis position of this villager",syntax = "/villagers modify <key> y <y>",usage = "/villagers nudge mynpc#0493 y 120.5")
	public void nudgeY(Player p,@VMap("uid") UID4 uid,@VMap("relative_y") double y)
	{
		if(!VNpcCtrl.contains(uid))
		{
			PrintUtils.error(p,"This villager does not exist!");
			return;
		}
		
		VNpc npc = VNpcCtrl.getVNpc(uid);
		npc.setY(npc.getY() + y);
		npc.respawn();
		VNpcCtrl.save(uid);
		PrintUtils.success(p,"VNpc " + uid.toString() + " updated");
	}
	
	@CmdPath(name = "nudgeZ",description = "Set the z axis position of this villager",syntax = "/villagers modify <key> z <z>",usage = "/villagers nudge mynpc#0493 z -54.4")
	public void nudgeZ(Player p,@VMap("uid") UID4 uid,@VMap("relative_z") double z)
	{
		if(!VNpcCtrl.contains(uid))
		{
			PrintUtils.error(p,"This villager does not exist!");
			return;
		}
		
		VNpc npc = VNpcCtrl.getVNpc(uid);
		npc.setZ(npc.getZ() + z);
		npc.respawn();
		VNpcCtrl.save(uid);
		PrintUtils.success(p,"VNpc " + uid.toString() + " updated");
	}
	
	@CmdPath(name = "modifyProfession",description = "Change the villagers profession",syntax = "/villagers modify <key> profession <profession>",usage = "/villagers modify mynpc#0493 profession butcher")
	public void modifyProfession(Player p,@VMap("uid") UID4 uid,@VMap("profession") Villager.Profession villagerProfession)
	{
		if(!VNpcCtrl.contains(uid))
		{
			PrintUtils.error(p,"This villager does not exist!");
			return;
		}
		
		VNpc npc = VNpcCtrl.getVNpc(uid);
		npc.setProfession(villagerProfession);
		npc.respawn();
		VNpcCtrl.save(uid);
		PrintUtils.success(p,"VNpc " + uid.toString() + " updated");
	}
	
	@CmdPath(name = "modifyType",description = "Change the villagers type",syntax = "/villagers modify <key> type <type>",usage = "/villagers modify mynpc#0493 type plains")
	public void modifyType(Player p,@VMap("uid") UID4 uid,@VMap("type") Villager.Type type)
	{
		if(!VNpcCtrl.contains(uid))
		{
			PrintUtils.error(p,"This villager does not exist!");
			return;
		}
		
		VNpc npc = VNpcCtrl.getVNpc(uid);
		npc.setVillagerType(type);
		npc.respawn();
		VNpcCtrl.save(uid);
		PrintUtils.success(p,"VNpc " + uid.toString() + " updated");
	}
	
	@CmdPath(name = "modifyInterationType",description = "Change the villagers interaction type",syntax = "/villagers modify <key> interaction <interaction>",usage = "/villagers modify mynpc#0493 interaction none")
	public void modifyInterationType(Player p,@VMap("uid") UID4 uid,@VMap("interaction") InteractionType interaction)
	{
		if(!VNpcCtrl.contains(uid))
		{
			PrintUtils.error(p,"This villager does not exist!");
			return;
		}
		
		VNpc npc = VNpcCtrl.getVNpc(uid);
		npc.setInteractionType(interaction);
		npc.respawn();
		VNpcCtrl.save(uid);
		PrintUtils.success(p,"VNpc " + uid.toString() + " updated");
	}
	
	@CmdPath(name = "modifyCustomName",description = "Change the villagers name",syntax = "/villagers modify <key> name <name>",usage = "/villagers modify mynpc#0493 name \"The Doom Slayer\"")
	public void modifyCustomName(Player p,@VMap("uid") UID4 uid,@VMap("custom_name") String name)
	{
		if(!VNpcCtrl.contains(uid))
		{
			PrintUtils.error(p,"This villager does not exist!");
			return;
		}
		
		VNpc npc = VNpcCtrl.getVNpc(uid);
		npc.setCustomName(name);
		npc.respawn();
		VNpcCtrl.save(uid);
		PrintUtils.success(p,"VNpc " + uid.toString() + " updated");
	}
	
	@CmdPath(name = "info",description = "Display information about this villager",syntax = "/villagers info <key>",usage = "/villagers info mynpc#0493")
	public void info(Player p,@VMap("uid") UID4 uid)
	{
		if(!VNpcCtrl.contains(uid))
		{
			PrintUtils.error(p,"This villager does not exist!");
			return;
		}
		
		VNpc npc = VNpcCtrl.getVNpc(uid);
		npc.printInfo(p);
	}
	
	@CmdPath(name = "forceRefresh",description = "Force refreshes all villagers",syntax = "/villagers refresh",usage = "/villagers refresh")
	public void forceRefresh(Player p)
	{
		VNpcCtrl.respawnAll();
		PrintUtils.info(p,"All villagers have been refreshed!");
	}
}