package coffee.dape.playerdata.commands;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import coffee.dape.cmdparsers.astral.annos.CmdPath;
import coffee.dape.cmdparsers.astral.annos.CommandEx;
import coffee.dape.cmdparsers.astral.annos.VMap;
import coffee.dape.cmdparsers.astral.parser.ArgSet;
import coffee.dape.cmdparsers.astral.parser.AstralExecutor;
import coffee.dape.cmdparsers.astral.parser.CommandParser.CmdSender;
import coffee.dape.cmdparsers.astral.suggestions.Suggestions;
import coffee.dape.cmdparsers.astral.types.ArgTypes;
import coffee.dape.exception.MissingAnnotationException;
import coffee.dape.playerdata.PlayerDataCtrl;
import coffee.dape.playerdata.commands.suggestions.PlayerDataSuggestions;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.PrintUtils;


/**
 * 
 * @author Laeven
 *
 */
@CommandEx(name = "playerdata",alias= {"pdata"},description = "A command for viewing and configuring players data")
public class PlayerDataCommand extends AstralExecutor
{
	public PlayerDataCommand() throws MissingAnnotationException
	{
		super(PlayerDataCommand.class);
		
		addPath("view",CmdSender.ANY,new ArgSet().of("view").of("<player>",ArgTypes.PLAYER,Suggestions.onlinePlayerNames()).mapTo("player_uuid"));
		
		addPath("forceSaveData",CmdSender.ANY,new ArgSet().of("force-save").of("<player>",ArgTypes.PLAYER,Suggestions.onlinePlayerNames()).mapTo("player_uuid"));
		
		addPath("forceLoadData",CmdSender.ANY,new ArgSet().of("force-load").of("<player>",ArgTypes.PLAYER,Suggestions.onlinePlayerNames()).mapTo("player_uuid"));
		
		addPath("forceBackup",CmdSender.ANY,new ArgSet().of("force-backup").of("<player>",ArgTypes.PLAYER,Suggestions.onlinePlayerNames()).mapTo("player_uuid"));
		
		addPath("restoreInventory",CmdSender.ANY,new ArgSet().of("restore").of("inventory").of("<player>",ArgTypes.PLAYER,Suggestions.onlinePlayerNames()).mapTo("player_uuid").of(2,PlayerDataSuggestions.playerDataBackupFiles()));
		
		addPath("restoreEnderchest",CmdSender.ANY,new ArgSet().of("restore").of("enderchest").of("<player>",ArgTypes.PLAYER,Suggestions.onlinePlayerNames()).mapTo("player_uuid").of(2,PlayerDataSuggestions.playerDataBackupFiles()));
		
		addPath("restoreEntityData",CmdSender.ANY,new ArgSet().of("restore").of("entitydata").of("<player>",ArgTypes.PLAYER,Suggestions.onlinePlayerNames()).mapTo("player_uuid").of(2,PlayerDataSuggestions.playerDataBackupFiles()));
		
		addPath("restoreAll",CmdSender.ANY,new ArgSet().of("restore").of("all").of("<player>",ArgTypes.PLAYER,Suggestions.onlinePlayerNames()).mapTo("player_uuid").of(2,PlayerDataSuggestions.playerDataBackupFiles()));
	}
	
	@CmdPath(name = "view",description = "View a players data.",syntax = "/playerdata view <player>",usage = "/playerdata view Laeven_")
	public void view(CommandSender sender,@VMap("player_uuid") UUID playerUUID)
	{
		PrintUtils.info(sender,"Displayed player data for " + PlayerUtils.getName(playerUUID) + ". Check Console!");
		PlayerDataCtrl.getPlayerData(playerUUID).printDataToConsole();
	}
	
	@CmdPath(name = "forceSaveData",description = "Force saves a players data.",syntax = "/playerdata force-save <player>",usage = "/playerdata save_data Laeven_")
	public void saveData(CommandSender sender,@VMap("player_uuid") UUID playerUUID)
	{
		PrintUtils.info(sender,"Saving player data for " + PlayerUtils.getName(playerUUID));
		
		if(PlayerDataCtrl.savePlayerData(playerUUID,false))
		{
			PrintUtils.success(sender,"Player data saved!");
		}
		else
		{
			PrintUtils.error(sender,"Player data did not save correctly! View console for further details!");
		}
	}
	
	@CmdPath(name = "forceLoadData",description = "Force loads a players data.",syntax = "/playerdata force-load <player>",usage = "/playerdata load_data Laeven_")
	public void loadData(CommandSender sender,@VMap("player_uuid") UUID playerUUID)
	{
		PrintUtils.info(sender,"Loading player data for " + PlayerUtils.getName(playerUUID));
		
		PlayerDataCtrl.loadPlayerData(playerUUID);
		
		if(PlayerDataCtrl.playerDataExistsInMap(playerUUID))
		{
			PrintUtils.success(sender,"Player data loaded!");
		}
		else
		{
			PrintUtils.error(sender,"Player data did not load! View console for further details!");
		}
	}
	
	@CmdPath(name = "forceBackup",description = "Force backups a players data.",syntax = "/playerdata backup <player>",usage = "/playerdata backup Laeven_")
	public void backup(CommandSender sender,@VMap("player_uuid") UUID playerUUID)
	{
		PrintUtils.info(sender,"Creating backup for " + PlayerUtils.getName(playerUUID));
		Path backupPath = PlayerDataCtrl.backup(PlayerDataCtrl.getPlayerData(playerUUID));
		
		if(PlayerDataCtrl.verifyBackup(backupPath,PlayerDataCtrl.getPlayerData(playerUUID)))
		{
			PrintUtils.success(sender,"Backup created at: " + backupPath.toString());
		}
		else
		{
			PrintUtils.error(sender,"Backup failed to create at: " + backupPath.toString());
		}
	}
	
	@CmdPath(name = "restoreInventory",description = "Restore a players inventory from a backup.",syntax = "/playerdata restore inventory <player> <file>",usage = "/playerdata restore inventory Laeven_ backups/22-01-2077 03:33:21")
	public void restoreInventory(CommandSender sender,@VMap("player_uuid") UUID playerUUID,String[] args)
	{
		/**
		 * 
		 * TODO Fix how path is given in arguments! Instead of 'backups/2025-01-07-03:33:21' use something like '2025 01-07 3-32(1)' as 3 arguments instead of 1. YEAR, MONTH, TIME, INCREMENT
		 * 
		 */
		
		String playerName = PlayerUtils.getName(playerUUID);
		String[] pathParts = args[3].split("[/]");
		Path backupPath = Paths.get(PlayerDataCtrl.getPlayerData(playerUUID).getPlayerDataBackupDirectory() + File.separator + pathParts[0] + File.separator + pathParts[1]);
		
		PrintUtils.info(sender,"Restoring inventory for " + playerName);
		
		if(PlayerDataCtrl.restoreInventoryOnly(PlayerDataCtrl.getPlayerData(playerUUID),backupPath))
		{
			PrintUtils.success(sender,"Inventory restored for " + PlayerUtils.getName(playerUUID));
			
			if(!PlayerUtils.isOnline(playerUUID)) { return; }
			
			Player p = PlayerUtils.getPlayer(playerUUID);
			String commandUser = sender instanceof Player user ? user.getName() : "[Server]";
			PrintUtils.success(p,"Your inventory was restored from a backup by " + commandUser);
		}
		else
		{
			PrintUtils.error(sender,"Inventory could not be restored for " + PlayerUtils.getName(playerUUID) + "!");
		}
	}
	
	@CmdPath(name = "restoreEnderchest",description = "Restore a players enderchest from a backup.",syntax = "/playerdata restore enderchest <player> <file>",usage = "/playerdata restore enderchest Laeven_ backups/22-01-2077 03:33:21")
	public void restoreEnderchest(CommandSender sender,@VMap("player_uuid") UUID playerUUID,String[] args)
	{
		/**
		 * 
		 * TODO Fix how path is given in arguments! Instead of 'backups/2025-01-07-03:33:21' use something like '2025 01-07 3-32(1)' as 3 arguments instead of 1. YEAR, MONTH, TIME, INCREMENT
		 * 
		 */
		
		String playerName = PlayerUtils.getName(playerUUID);
		String[] pathParts = args[3].split("[/]");
		Path backupPath = Paths.get(PlayerDataCtrl.getPlayerData(playerUUID).getPlayerDataBackupDirectory() + File.separator + pathParts[0] + File.separator + pathParts[1]);
		
		PrintUtils.info(sender,"Restoring enderchest for " + playerName);
		
		if(PlayerDataCtrl.restoreEnderchestOnly(PlayerDataCtrl.getPlayerData(playerUUID),backupPath))
		{
			PrintUtils.success(sender,"Enderchest restored for " + PlayerUtils.getName(playerUUID));
			
			if(!PlayerUtils.isOnline(playerUUID)) { return; }
			
			Player p = PlayerUtils.getPlayer(playerUUID);
			String commandUser = sender instanceof Player user ? user.getName() : "[Server]";
			PrintUtils.success(p,"Your enderchest was restored from a backup by " + commandUser);
		}
		else
		{
			PrintUtils.error(sender,"Enderchest could not be restored for " + PlayerUtils.getName(playerUUID) + "!");
		}
	}
	
	@CmdPath(name = "restoreEntityData",description = "Restore a players entity data from a backup.",syntax = "/playerdata restore entitydata <player> <file>",usage = "/playerdata restore entitydata Laeven_ backups/22-01-2077 03:33:21")
	public void restoreEntityData(CommandSender sender,@VMap("player_uuid") UUID playerUUID,String[] args)
	{
		/**
		 * 
		 * TODO Fix how path is given in arguments! Instead of 'backups/2025-01-07-03:33:21' use something like '2025 01-07 3-32(1)' as 3 arguments instead of 1. YEAR, MONTH, TIME, INCREMENT
		 * 
		 */
		
		String playerName = PlayerUtils.getName(playerUUID);
		String[] pathParts = args[3].split("[/]");
		Path backupPath = Paths.get(PlayerDataCtrl.getPlayerData(playerUUID).getPlayerDataBackupDirectory() + File.separator + pathParts[0] + File.separator + pathParts[1]);
		
		PrintUtils.info(sender,"Restoring entity data for " + playerName);
		
		if(PlayerDataCtrl.restoreEntityDataOnly(PlayerDataCtrl.getPlayerData(playerUUID),backupPath))
		{
			PrintUtils.success(sender,"Entity data restored for " + PlayerUtils.getName(playerUUID));
			
			if(!PlayerUtils.isOnline(playerUUID)) { return; }
			
			Player p = PlayerUtils.getPlayer(playerUUID);
			String commandUser = sender instanceof Player user ? user.getName() : "[Server]";
			PrintUtils.success(p,"Your entity data was restored from a backup by " + commandUser);
		}
		else
		{
			PrintUtils.error(sender,"Entity data could not be restored for " + PlayerUtils.getName(playerUUID) + "!");
		}
	}
	
	@CmdPath(name = "restoreAll",description = "Restore a players data from a backup.",syntax = "/playerdata restore all <player> <file>",usage = "/playerdata restore all Laeven_ backups/22-01-2077 03:33:21")
	public void restoreAll(CommandSender sender,@VMap("player_uuid") UUID playerUUID,String[] args)
	{
		/**
		 * 
		 * TODO Fix how path is given in arguments! Instead of 'backups/2025-01-07-03:33:21' use something like '2025 01-07 3-32(1)' as 3 arguments instead of 1. YEAR, MONTH, TIME, INCREMENT
		 * 
		 */
		
		String playerName = PlayerUtils.getName(playerUUID);
		String[] pathParts = args[3].split("[/]");
		Path backupPath = Paths.get(PlayerDataCtrl.getPlayerData(playerUUID).getPlayerDataBackupDirectory() + File.separator + pathParts[0] + File.separator + pathParts[1]);
		
		PrintUtils.info(sender,"Restoring PlayerData for " + playerName);
		
		if(PlayerDataCtrl.restore(PlayerDataCtrl.getPlayerData(playerUUID),backupPath))
		{
			PrintUtils.success(sender,"PlayerData restored for " + PlayerUtils.getName(playerUUID));
			
			if(!PlayerUtils.isOnline(playerUUID)) { return; }
			
			Player p = PlayerUtils.getPlayer(playerUUID);
			String commandUser = sender instanceof Player user ? user.getName() : "[Server]";
			PrintUtils.success(p,"Your player data was restored from a backup by " + commandUser);
		}
		else
		{
			PrintUtils.error(sender,"PlayerData could not be restored for " + PlayerUtils.getName(playerUUID) + "!");
		}
	}
}