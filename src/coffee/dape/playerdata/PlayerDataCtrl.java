package coffee.dape.playerdata;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.gson.JsonObject;

import coffee.dape.Dape;
import coffee.dape.config.Configurable;
import coffee.dape.config.items.ConfigItem;
import coffee.dape.event.PostSentEvent;
import coffee.dape.exception.DeserialiseException;
import coffee.dape.exception.SerialiseException;
import coffee.dape.notifications.NotificationCtrl;
import coffee.dape.playerdata.data.PlayerData;
import coffee.dape.playerdata.data.PlayerDataExtension;
import coffee.dape.playerdata.data.PlayerPostBox.Message;
import coffee.dape.playerdata.data.PlayerPostBox.Parcel;
import coffee.dape.playerdata.data.PlayerServerVariables;
import coffee.dape.playerdata.data.PlayerSettings;
import coffee.dape.postbox.PostItem;
import coffee.dape.utils.ChecksumUtils;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.Cooldown;
import coffee.dape.utils.DelayUtils;
import coffee.dape.utils.FUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.Logg.VerbGroup;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.TimeUtils;
import coffee.dape.utils.json.JUtils;
import coffee.dape.utils.structs.Namespace;
import coffee.dape.utils.toasts.Toast.Frame;
import coffee.dape.utils.toasts.Toasts;

/**
 * 
 * @author Laeven
 *
 */
public class PlayerDataCtrl implements Listener
{
	private static ConcurrentHashMap<UUID,PlayerData> playerData = new ConcurrentHashMap<>();	
	
	private static Map<UUID,Long> playerJoinMili = new HashMap<>();
	
	public static LocalDateTime BACKUP_CUTOFF_DATE = LocalDateTime.now().minusDays(7);
	public static final int MAX_BACKUPS_PER_PLAYER = 300; // 1 backup per 10 minutes
	
	private static final UUID dropItemCooldown = UUID.randomUUID();
	private static final UUID pickupItemCooldown = UUID.randomUUID();
	private static final UUID clickAndDragCooldown = UUID.randomUUID();
	
	private static Map<Namespace,Class<? extends PlayerDataExtension>> extensionRegistry = new HashMap<>();
	
	static
	{
		registerExtension(Namespace.of(Dape.getNamespaceName(),PlayerServerVariables.PLAYER_SERVER_VARIABLES),PlayerServerVariables.class);
		registerExtension(Namespace.of(Dape.getNamespaceName(),PlayerSettings.PLAYER_SETTINGS),PlayerSettings.class);
	}
	
	/**
	 * Loads player data from file (or creates new player data if they're not online)
	 * @param p Player to load data for
	 */
	public static void loadPlayerData(Player p)
	{
		loadPlayerData(p.getUniqueId());
	}
	
	/**
	 * Loads player data from file (or creates new player data if they're not online)
	 * @param uuid UUID of a player to load data for
	 */
	public static void loadPlayerData(UUID uuid)
	{
		// Prevents reloading while already in memory
		if(playerData.containsKey(uuid)) { return; }
		
		// Player data path
		Path playerDataFile = Dape.internalFilePath("playerdata" + File.separator + uuid.toString() + File.separator + "playerdata.json");
		PlayerData data = null;
		
		// Check if an existing player data file exists
		if(!Files.exists(playerDataFile))
		{
			// No existing file exists so create a new one
			data = new PlayerData(uuid);
			playerData.put(uuid,data);
			return;
		}
		
		// Existing player data exists, attempt to load it
		JsonObject dataObj = JUtils.readToObject(playerDataFile);
		
		// If loading did not fail, attempt to deserialise
		if(dataObj != null)
		{
			data = new PlayerData(uuid);
			
			try
			{
				// Attempt to deserialise data. If successful save to map!
				data.deserialise(dataObj);
				data.startDegradedClock();
				playerData.put(uuid,data);
				return;
			}
			catch(DeserialiseException e)
			{
				// Could not deserialise data so treat as corrupted
				Logg.error("Could not deserialise PlayerData " + uuid.toString() + " (" + PlayerUtils.getName(uuid) + ")",e);
			}
		}
		
		// Loading failed, player data may be corrupted?
		Logg.fatal("An error occured attempting to load playerdata for " + PlayerUtils.getName(uuid) + "!");
		Logg.warn("Attempting a temporary data fix...");
		
		// Fix corrupted data by moving the current 'corrupted' data to a folder for later examining and manual fixing.
		// Restore from the most recent good backup
		fixCorruptedData(uuid);
		
		// Check if a backup was able to be retrieved and used as a substitute
		if(Files.exists(playerDataFile))
		{
			// Existing player data exists, attempt to load it
			JsonObject restoredDataObj = JUtils.readToObject(playerDataFile);
			
			// Check if the restored backup was loaded
			if(restoredDataObj != null)
			{
				data = new PlayerData(uuid);
				
				try
				{
					// Attempt to deserialise data. If successful save to map!
					data.deserialise(restoredDataObj);
					data.setDegradedState(true);
					data.startDegradedClock();
					Logg.info("Data restored from backup!");
					playerData.put(uuid,data);
					return;
				}
				catch(DeserialiseException e)
				{
					// Could not deserialise data so treat as corrupted
					Logg.error("Could not deserialise restored PlayerData " + uuid.toString() + " (" + PlayerUtils.getName(uuid) + ")",e);
				}
			}
			
			Logg.fatal("Attempted fix failed! Failed to load playerdata for " + PlayerUtils.getName(uuid) + ", backups may be corrupted! Investigate immediately!");
		}
		
		// Reaching this point means:
		// - Player had data and that data is corrupted
		// - A backup restore was attempted and either a backup does not exist or the backup failed to load or failed to deserialise
		
		// Blank player data is used as a placeholder
		
		Logg.info("Creating placeholder blank player data...");
		data = new PlayerData(uuid);
		Date date = new Date();
		data.setJoinDate(date);
		data.setLastJoin(date);
		data.setDegradedState(true);
		data.startDegradedClock();
		playerData.put(uuid,data);
	}
	
	/**
	 * Removes player data from the map
	 * 
	 * <p> @see #savePlayerData(Player) to save player data
	 * before removing it
	 * @param p Player who's data to remove
	 */
	public static void removePlayerData(Player p)
	{
		removePlayerData(p.getUniqueId());
	}
	
	/**
	 * Removes player data from the map
	 * 
	 * <p> @see #savePlayerData(Player) to save player data
	 * before removing it
	 * @param uuid Players UUID who's data to remove
	 */
	public static void removePlayerData(UUID uuid)
	{
		playerData.remove(uuid);
	}
	
	/**
	 * Saves player data
	 * @param p Player who's data to save
	 * @param removeFromMap If player data should be removed from the map after being saved
	 * @return True if data was saved successfully, false otherwise
	 */
	public static boolean savePlayerData(Player p,boolean removeFromMap)
	{
		return savePlayerData(p.getUniqueId(),removeFromMap);
	}
	
	/**
	 * Saves player data
	 * @param uuid Players UUID who's data to save
	 * @param removeFromMap If player data should be removed from the map after being saved
	 * @return True if data was saved successfully, false otherwise
	 */
	public static boolean savePlayerData(UUID uuid,boolean removeFromMap)
	{
		boolean saveSuccessful = false;
		
		if(!playerData.containsKey(uuid))
		{
			Logg.error("Player " + PlayerUtils.getName(uuid) + " does not exist in the playerdata map!");
			return saveSuccessful;
		}
		
		try
		{
			JsonObject data = playerData.get(uuid).serialise();
			String json = JUtils.toJsonString(data,true);
			
			JUtils.write(playerData.get(uuid).getPlayerDataPath(),data,true);
			
			long checksumOfMemory = ChecksumUtils.getChecksum(json.getBytes());
			long checksumOfFile = FUtils.checksumFile(playerData.get(uuid).getPlayerDataPath());
			
			if(checksumOfMemory == checksumOfFile)
			{
				saveSuccessful = true;
			}
			else
			{
				Logg.error("PlayerData " + PlayerUtils.getName(uuid) + " data on disk does not match data in memory! " + checksumOfFile + " != " + checksumOfMemory);
				return false;
			}
		}
		catch(SerialiseException e)
		{
			Logg.error("Could not serialise PlayerData " + uuid.toString() + " (" + PlayerUtils.getName(uuid) + ")",e);
			return false;
		}
		
		if(!removeFromMap) { return saveSuccessful; }
		playerData.get(uuid).stopBackupClockNow();
		playerData.get(uuid).stopSaveClockNow();
		playerData.get(uuid).stopDegradedClockNow();
		removePlayerData(uuid);
		return saveSuccessful;
	}
	
	/**
	 * Saves all player data in the map to disk
	 * @param removeFromMap If player data should be removed from the map after being saved
	 * @return True if all data was saved successfully, false otherwise
	 */
	public static boolean saveAllPlayerData(boolean removeFromMap)
	{
		boolean allSavesSuccessful = true;
		
		Logg.verb("Size " + playerData.size(),Logg.VerbGroup.MISC);
		
		for(UUID uuid : playerData.keySet())
		{
			if(!savePlayerData(uuid,removeFromMap))
			{
				allSavesSuccessful = false;
			}
		}
		
		return allSavesSuccessful;
	}
	
	/**
	 * Checks if PlayerData for this player is loaded in the map
	 * @param p Player
	 * @return True if their PlayerData is loaded in the map, false otherwise
	 */
	public static boolean playerDataExistsInMap(Player p)
	{
		return playerDataExistsInMap(p.getUniqueId());
	}
	
	/**
	 * Checks if PlayerData for this player is loaded in the map
	 * @param uuid Players UUID
	 * @return True if their PlayerData is loaded in the map, false otherwise
	 */
	public static boolean playerDataExistsInMap(UUID uuid)
	{
		return playerData.containsKey(uuid);
	}
	
	/**
	 * Retrieves a players data.
	 * @param p Player who's data to retrieve
	 * @return PlayerData when retrieved successfully, otherwise null
	 */
	public static PlayerData getPlayerData(Player p)
	{
		return getPlayerData(p.getUniqueId());
	}
	
	/**
	 * Retrieves a players data.
	 * @param uuid Players UUID who's data to retrieve
	 * @return PlayerData when retrieved successfully, otherwise null
	 */
	public static PlayerData getPlayerData(UUID uuid)
	{
		if(!playerData.containsKey(uuid))
		{			
			loadPlayerData(uuid);
			
			if(!playerData.containsKey(uuid))
			{
				Logg.error("An autoload was performed as " + PlayerUtils.getName(uuid) + "'s data did not exist. This autoload failed!");
				return null;
			}
		}
		
		return playerData.get(uuid);
	}
	
	/**
	 * Sets the current system time when the player joins the server
	 * @param p Player
	 */
	public static void setTimePlayerJoined(Player p)
	{
		playerJoinMili.put(p.getUniqueId(),System.currentTimeMillis());
	}
	
	/**
	 * Retrieves a players system time when they joined the server
	 * @param p Player
	 * @return System time when player joined server
	 */
	public static long getTimePlayerJoined(Player p)
	{
		return playerJoinMili.get(p.getUniqueId());
	}
	
	/**
	 * Removes a players system time when they joined the server
	 * @param p Player
	 */
	public static void removeTimePlayerJoined(Player p)
	{
		playerJoinMili.remove(p.getUniqueId());
	}
	
	/**
	 * Get total play time including play time stored in players data file + time on the server currently
	 * @param playerUUID Player UUID
	 * @return total play time
	 */
	public static long getTotalPlayTime(UUID playerUUID)
	{
		long totalPlayTime = getPlayerData(playerUUID).getPlayTime() + (PlayerUtils.isOnline(playerUUID) ? (System.currentTimeMillis() - getTimePlayerJoined(PlayerUtils.getPlayer(playerUUID))) : 0);
		return totalPlayTime;
	}
	
	/**
	 * Get formatted total play time including play time stored in players data file + time on the server currently
	 * @param p Player
	 * @return total play time
	 */
	public static String getTotalPlayTimeFormatted(UUID playerUUID)
	{
		long totalPlayTime = getTotalPlayTime(playerUUID);
		
		long hours = TimeUnit.MILLISECONDS.toHours(totalPlayTime);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(totalPlayTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalPlayTime));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(totalPlayTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalPlayTime));
		
		String formatted = String.format("%d %s %d %s %d %s",hours,hours == 1 ? "hr" : "hrs",minutes,minutes == 1 ? "min" : "mins",seconds,seconds == 1 ? "sec" : "secs");
		return formatted;
	}
	
	private static DateFormat fileFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
	private static DateFormat folderFormat = new SimpleDateFormat("yyyy-MM");
	
	/**
	 * Attempts to restore players data to a known working version
	 * @param player Player who's data to restore
	 */
	public static void fixCorruptedData(UUID player)
	{
		Objects.requireNonNull(player,"Player uuid cannot be null!");
		
		String playerName = PlayerUtils.getName(player);
		Path dataBackups = Dape.internalFilePath("playerdata" + File.separator + player.toString() + File.separator + "backups");
		Path playerDataFile = Dape.internalFilePath("playerdata" + File.separator + player.toString() + File.separator + "playerdata.json");
		Path corruptDataDir = Dape.internalFilePath("playerdata" + File.separator + player.toString() + File.separator + "corrupted");
		
		// Make copy of corrupted player data to 'corrupted' directory
		if(!FUtils.createDirectories(corruptDataDir))
		{
			Logg.error("Could not create directories for corrupted player data location!");
			
			if(PlayerUtils.isOnline(player))
			{
				Player p = PlayerUtils.getPlayer(player);
				p.kickPlayer(ColourUtils.translate("&cYou have been auto-kicked because we had problems loading your player data!"));
			}
			
			return;
		}
		
		Path archivedCorruptedPlayedData = Path.of(corruptDataDir.toString() + File.separator + "corrupted_playerdata_" + UUID.randomUUID().toString() + ".json");
		
		// Copy corrupted player data to corrupted dir
		FUtils.copyFile(playerDataFile,archivedCorruptedPlayedData);
		
		if(!Files.exists(archivedCorruptedPlayedData))
		{
			Logg.error("Could not archive corrupted player data! Please check file permissions!");
			
			if(PlayerUtils.isOnline(player))
			{
				Player p = PlayerUtils.getPlayer(player);
				p.kickPlayer(ColourUtils.translate("&cYou have been auto-kicked because we had problems loading your player data!"));
			}
			
			return;
		}
		
		// Delete original
		FUtils.delete(playerDataFile);
		
		if(!FUtils.createDirectoriesForFile(dataBackups))
		{
			Logg.error("Could not create directories for player data backup location!");
			return;
		}
		
		if(!Files.exists(dataBackups))
		{
			Logg.info("No backups exist for " + playerName);
			return;
		}
		
		// Attempt to find a backup that is not corrupted
		TreeMap<Long,Path> backupFiles = FUtils.getPathsInDirectorySortedByAge(dataBackups);
		
		// Check no more than 24 backup files as we could be here for a while
		// If more than 24 are corrupted I'm sure there is something very wrong with the code...
		int MAX_FILES_TO_CHECK = 24;
		int filesChecked = 0;
		
		for(Path backupFile : backupFiles.values())
		{
			if(filesChecked > MAX_FILES_TO_CHECK) { return; }
			
			// Check that the backup can be read from (if it cannot be read it is corrupted)
			if(JUtils.readToObject(backupFile) != null)
			{
				Logg.info("Attempting to restore from backup: " + backupFile.getFileName());
				FUtils.copyFile(backupFile,playerDataFile);
				
				if(PlayerUtils.isOnline(player))
				{
					Player p = PlayerUtils.getPlayer(player);
					PrintUtils.error(p,"Your playerdata was auto-restored from a backup as your current one could not be read! Please contact an admin immediately!");
					
					DelayUtils.executeDelayedTask(() ->
					{
						NotificationCtrl.sendAlert(p,"Alert! Your PlayerData was found to be corrupted and could not be read correctly!"
								+ " An attempt has been made to fix your data via a backup."
								+ " Please contact an administrator immediately!");
					},20L);
				}
				
				Logg.fatal("Player " + PlayerUtils.getName(player) + " has degraded player data!");
				return;
			}
		}
	}
	
	/**
	 * Backups player data
	 * @param data PlayerData to backup
	 * @return Path to this backup file
	 */
	public static Path backup(PlayerData data)
	{
		if(!FUtils.createDirectoriesForFile(data.getPlayerDataBackupDirectory()))
		{
			Logg.error("Could not create directories for player data backup location!");
			return null;
		}
		
		// Prevents backing up data that's already in a degraded state
		if(data.isInDegradedState()) { return null; }
		
		Date backupDate = new Date();
		Path backupPath = Paths.get(data.getPlayerDataBackupDirectory().toString() + File.separator + folderFormat.format(backupDate) + File.separator + fileFormat.format(backupDate) + ".json");
		
		try
		{
			JUtils.write(backupPath,data.serialise(),true);
		}
		catch(SerialiseException e)
		{
			Logg.error("Could not serialise PlayerData " + data.getOwner().toString() + " (" + PlayerUtils.getName(data.getOwner()) + ")",e);
		}
		
		return backupPath;
	}
	
	/**
	 * Verifies the backup that just took place
	 * This should be called immediately after {@link #backup(PlayerData)}
	 * @param pathToBackup Path to backup file
	 * @param data PlayerData held in memory to compare against
	 * @return True if backup was saved successfully, false otherwise
	 */
	public static boolean verifyBackup(Path pathToBackup,PlayerData data)
	{
		if(data.getMostRecentSerialisedData() == null)
		{
			Logg.error("No data has been serialised!");
			return false;
		}
		
		String json = JUtils.toJsonString(data.getMostRecentSerialisedData(),false);
		
		long checksumOfMemory = ChecksumUtils.getChecksum(json.getBytes());
		long checksumOfFile = FUtils.checksumFile(pathToBackup);
		
		if(checksumOfMemory == checksumOfFile)
		{
			return true;
		}
		else
		{
			Logg.error("Backup of playerData " + PlayerUtils.getName(data.getOwner()) + " saved on disk does not match data in memory! " + checksumOfFile + " != " + checksumOfMemory);
		}
		
		return false;
	}
	
	/**
	 * Deletes backup files older than a week
	 * @param data PlayerData to check the backup files of
	 */
	public static void checkAndDeleteOldBackups(PlayerData data)
	{
		if(!FUtils.createDirectoriesForFile(data.getPlayerDataBackupDirectory()))
		{
			Logg.error("Could not create directories for player data backup location!");
			return;
		}
		
		Path backupPath = Paths.get(data.getPlayerDataBackupDirectory().toString());
		TreeMap<Long,Path> backupFiles = new TreeMap<>();
		BACKUP_CUTOFF_DATE = LocalDateTime.now().minusDays(7);
		
		// NOTE:
		// If backups have exactly the same creation date, then they will not be picked up
		// They will however be eventually cleared with enough backup clear checks
		
		// Cull files that are older than 'BACKUP_CUTOFF_DATE'		
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(backupPath))
		{
			for(Path entry : stream)
			{
				if(!Files.isDirectory(entry)) { continue; }
				
				try(DirectoryStream<Path> backupFilesStream = Files.newDirectoryStream(entry))
				{
					for(Path backupFilePath : backupFilesStream)
					{
						if(Files.isDirectory(backupFilePath)) { continue; }
						
						LocalDateTime date = FUtils.getFileCreationDate(backupFilePath);
						
						// If file is older than 7 days, delete
						if(BACKUP_CUTOFF_DATE.isAfter(date))
						{
							FUtils.delete(backupFilePath);
							continue;
						}
						
						long timeEpoch = TimeUtils.getMilliFromLocalDateTime(date);
						backupFiles.put(timeEpoch,backupFilePath);
					}
				}
				catch(Exception e)
				{
					Logg.error("Could not stream directory " + entry.toString(),e);
					Logg.fatal("Could not clean up old backup files!");
					return;
				}
			}
		}
		catch(Exception e)
		{
			Logg.error("Could not stream directory " + backupPath.toString(),e);
			Logg.fatal("Could not clean up old backup files!");
			return;
		}
		
		// We don't need to delete extra backup files if there are less than 'MAX_BACKUPS_PER_PLAYER' 
		if(backupFiles.size() <= MAX_BACKUPS_PER_PLAYER)
		{
			// Delete empty directories left over from culling
			deleteEmptyBackupDirs(backupPath);
			return;
		}
		
		// Oldest file is at the top of the list (has the smallest epoch time)
		while(backupFiles.size() > MAX_BACKUPS_PER_PLAYER)
		{
			Path backupFilePath = backupFiles.remove(backupFiles.firstKey());
			FUtils.delete(backupFilePath);
		}
		
		// Delete empty directories left over from culling
		deleteEmptyBackupDirs(backupPath);
	}
	
	private static void deleteEmptyBackupDirs(Path backupPath)
	{
		if(!Files.exists(backupPath)) { return; }
		
		// Delete empty directories left over from culling
		for(File backupDir : backupPath.toFile().listFiles())
		{
			if(backupDir.isDirectory())
			{
				if(backupDir.listFiles().length == 0)
				{
					Path backupDirPath = Paths.get(backupDir.getAbsolutePath());
					FUtils.delete(backupDirPath);
				}
			}
		}
	}
	
	/**
	 * Reads a player data file or player data backup file
	 * @param path Path to player data file / player data backup file
	 * @return JsonObject representing the serialised player data
	 */
	private static JsonObject readPlayerData(Path path)
	{
		return JUtils.readToObject(path);
	}
	
	/**
	 * Restores a players PlayerData from an existing backup
	 * @param data PlayerData to overwrite
	 * @param backupPath Backup path to restore from
	 * @return True if restore was successful, false otherwise
	 */
	public static boolean restore(PlayerData data,Path backupPath)
	{
		data.stopBackupClock();
		data.stopSaveClock();
		data.stopDegradedClock();
		
		UUID owner = data.getOwner();
		String playerName = PlayerUtils.getName(owner);
		JsonObject dataObj = readPlayerData(backupPath);
		PlayerData backupData = null;
		
		if(dataObj == null)
		{
			Logg.fatal("Failed to restore player data from backup for player " + playerName);
			Logg.fatal("Could not read from " + backupPath.toAbsolutePath().toString());			
			return false;
		}
		
		try
		{
			backupData = new PlayerData(owner);
			backupData.deserialise(dataObj);;
		}
		catch(DeserialiseException e)
		{
			Logg.error("Could not deserialise restored PlayerData " + owner.toString() + " (" + PlayerUtils.getName(owner) + ")",e);
			Logg.error("Backup " + backupPath.toString() + " is possibly corrupt or missing attributes!");
			return false;
		}
		
		FUtils.delete(data.getPlayerDataPath());
		playerData.remove(owner);
		playerData.put(owner,backupData);
		backupData = playerData.get(owner);
		
		backupData.getInventory().loadInventoryToPlayer();
		backupData.getEnderchest().loadEnderchestToPlayer();
		backupData.getEntityData().loadEntityDataToPlayer();
		backupData.startBackupClock();

		savePlayerData(owner,false);
		return true;
	}
	
	/**
	 * Restores a players inventory from an existing backup
	 * @param data PlayerData to overwrite
	 * @param backupPath Backup path to restore from
	 * @return True if restore was successful, false otherwise
	 */
	public static boolean restoreInventoryOnly(PlayerData data,Path backupPath)
	{
		Objects.requireNonNull(data,"PlayerData cannot be null!");
		Objects.requireNonNull(backupPath,"Backup path cannot be null!");
		
		UUID owner = data.getOwner();
		String playerName = PlayerUtils.getName(owner);
		JsonObject dataObj = readPlayerData(backupPath);
		
		if(dataObj == null)
		{
			Logg.fatal("Failed to restore player data from backup for player " + playerName);
			Logg.fatal("Could not read from " + backupPath.toAbsolutePath().toString());			
			return false;
		}
		
		try
		{
			data.getInventory().deserialise(dataObj.get(PlayerData.PLAYER_INVENTORY).getAsJsonObject());
		}
		catch(DeserialiseException e)
		{
			Logg.error("Could not deserialise restored Inventory PlayerData " + owner.toString() + " (" + PlayerUtils.getName(owner) + ")",e);
			Logg.error("Backup " + backupPath.toString() + " is possibly corrupt or missing attributes!");
			return false;
		}
		
		data.getInventory().loadInventoryToPlayer();
		if(!savePlayerData(owner,false))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Restores a players enderchest from an existing backup
	 * @param data PlayerData to overwrite
	 * @param backupPath Backup path to restore from
	 * @return True if restore was successful, false otherwise
	 */
	public static boolean restoreEnderchestOnly(PlayerData data,Path backupPath)
	{
		Objects.requireNonNull(data,"PlayerData cannot be null!");
		Objects.requireNonNull(backupPath,"Backup path cannot be null!");
		
		UUID owner = data.getOwner();
		String playerName = PlayerUtils.getName(owner);
		JsonObject dataObj = readPlayerData(backupPath);
		
		if(dataObj == null)
		{
			Logg.fatal("Failed to restore player data from backup for player " + playerName);
			Logg.fatal("Could not read from " + backupPath.toAbsolutePath().toString());			
			return false;
		}
		
		try
		{
			data.getEnderchest().deserialise(dataObj.get(PlayerData.PLAYER_ENDERCHEST).getAsJsonObject());
		}
		catch(DeserialiseException e)
		{
			Logg.error("Could not deserialise restored Enderchest PlayerData " + owner.toString() + " (" + PlayerUtils.getName(owner) + ")",e);
			Logg.error("Backup " + backupPath.toString() + " is possibly corrupt or missing attributes!");
			return false;
		}
		
		data.getEnderchest().loadEnderchestToPlayer();
		if(!savePlayerData(owner,false))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Restores a players entity data from an existing backup
	 * @param data PlayerData to overwrite
	 * @param backupPath Backup path to restore from
	 * @return True if restore was successful, false otherwise
	 */
	public static boolean restoreEntityDataOnly(PlayerData data,Path backupPath)
	{
		Objects.requireNonNull(data,"PlayerData cannot be null!");
		Objects.requireNonNull(backupPath,"Backup path cannot be null!");
		
		UUID owner = data.getOwner();
		String playerName = PlayerUtils.getName(owner);
		JsonObject dataObj = readPlayerData(backupPath);
		
		if(dataObj == null)
		{
			Logg.fatal("Failed to restore player data from backup for player " + playerName);
			Logg.fatal("Could not read from " + backupPath.toAbsolutePath().toString());			
			return false;
		}
		
		try
		{
			data.getEntityData().deserialise(dataObj.get(PlayerData.PLAYER_ENTITY).getAsJsonObject());
		}
		catch(DeserialiseException e)
		{
			Logg.error("Could not deserialise restored Enderchest PlayerData " + owner.toString() + " (" + PlayerUtils.getName(owner) + ")",e);
			Logg.error("Backup " + backupPath.toString() + " is possibly corrupt or missing attributes!");
			return false;
		}
		
		data.getEntityData().loadEntityDataToPlayer();
		if(!savePlayerData(owner,false))
		{
			return false;
		}
		
		return true;
	}
	
	@EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = false)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		
		Logg.verb("Loading account for " + p.getName(),Logg.VerbGroup.PLAYER_DATA);
		setTimePlayerJoined(p);
		PlayerData pData = getPlayerData(p);
		
		pData.setLastJoin(new Date());
		pData.addIpAddress(p.getAddress().getHostName());
		pData.addUsername(p.getName());
		pData.getInventory().loadInventoryToPlayer();
		pData.getEnderchest().loadEnderchestToPlayer();
		pData.getEntityData().loadEntityDataToPlayer();
		pData.startBackupClock();

		savePlayerData(p,false);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = false)
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		backup(getPlayerData(e.getPlayer()));
		onPlayerQuit(e.getPlayer());
	}
	
	/**
	 * Saves player data when they quit the server or
	 * when the server is shutdown.
	 * @param p Player
	 */
	public static void onPlayerQuit(Player p)
	{
		Logg.verb("Saving and unloading account for " + p.getName(),VerbGroup.PLAYER_DATA);
		
		PlayerData pData = getPlayerData(p);
		
		pData.stopBackupClock();
		pData.stopSaveClock();
		pData.setPlayTime(getTotalPlayTime(p.getUniqueId()));
		savePlayerData(p,true);
		removeTimePlayerJoined(p);
	}
	
	// =========================================
	// Events to trigger a scheduled save
	// =========================================
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent e)
	{
		// Drop item events happen a lot, this prevents spam scheduling
		if(Cooldown.isCooling(e.getPlayer(),dropItemCooldown,true)) { return; }
		
		PlayerData data = getPlayerData(e.getPlayer());
		if(data == null) { return; }
		data.scheduleSave();
		
		Cooldown.setCooldown(e.getPlayer(),dropItemCooldown,10000);
	}
	
	@EventHandler
	public void onItemBreak(PlayerItemBreakEvent e)
	{
		PlayerData data = getPlayerData(e.getPlayer());
		if(data == null) { return; }
		data.scheduleSave();
	}
	
	@EventHandler
	public void onConsume(PlayerItemConsumeEvent e)
	{
		PlayerData data = getPlayerData(e.getPlayer());
		if(data == null) { return; }
		data.scheduleSave();
	}
	
	@EventHandler
	public void onPickupItem(EntityPickupItemEvent e)
	{
		if(!(e.getEntity() instanceof Player p)) { return; }
		
		// Pickup item events happen a lot, this prevents spam scheduling
		if(Cooldown.isCooling(p,pickupItemCooldown,true)) { return; }
		
		Logg.verb("Scheduled Save -> " + p.getName(),Logg.VerbGroup.PLAYER_DATA);
		PlayerData data = getPlayerData(p);
		if(data == null) { return; }
		data.scheduleSave();
		
		Cooldown.setCooldown(p,pickupItemCooldown,10000);
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e)
	{
		PlayerData data = getPlayerData(e.getEntity());
		if(data == null) { return; }
		data.scheduleSave();
	}
	
	@EventHandler
	public void onClickInInventory(InventoryClickEvent e)
	{
		if(e.getView().getBottomInventory().getType() != InventoryType.PLAYER) { return; }
		if(e.getRawSlot() == -999 || e.getRawSlot() == -1) { return; }
		
		// Click and Drag events happen a lot, this prevents spam scheduling
		if(Cooldown.isCooling((Player) e.getWhoClicked(),clickAndDragCooldown,true)) { return; }
		
		PlayerData data = getPlayerData((Player) e.getWhoClicked());
		if(data == null) { return; }
		data.scheduleSave();
		
		Cooldown.setCooldown((Player) e.getWhoClicked(),clickAndDragCooldown,10000);
	}
	
	@EventHandler
	public void onDragInInventory(InventoryDragEvent e)
	{
		if(e.getView().getBottomInventory().getType() != InventoryType.PLAYER) { return; }
		
		// Click and Drag events happen a lot, this prevents spam scheduling
		if(Cooldown.isCooling((Player) e.getWhoClicked(),clickAndDragCooldown,true)) { return; }
		
		PlayerData data = getPlayerData((Player) e.getWhoClicked());
		if(data == null) { return; }
		data.scheduleSave();
		
		Cooldown.setCooldown((Player) e.getWhoClicked(),clickAndDragCooldown,10000);
	}
	
	@EventHandler
	public void onLevelChange(PlayerLevelChangeEvent e)
	{
		PlayerData data = getPlayerData(e.getPlayer());
		if(data == null) { return; }
		data.scheduleSave();
	}
	
	/**
	 * Registers a PlayerDataExtension class to be initialised when deserialising data for it
	 * @param <T> A data class extension of PlayerDataExtension
	 * @param extensionName Namespace of the extension
	 * @param extensionClass PlayerDataExtension class
	 */
	public static <T extends PlayerDataExtension> void registerExtension(Namespace extensionName,Class<T> extensionClass)
	{
		extensionRegistry.put(extensionName,extensionClass);
	}
	
	/**
	 * Retrieves an extension class
	 * @param <T> A data class extension of PlayerDataExtension
	 * @param extensionName Namespace of the extension
	 * @return Extension class if it exists in the registry, otherwise null
	 */
	public static <T extends PlayerDataExtension> Class<? extends PlayerDataExtension> getExtensionClass(Namespace extensionName)
	{
		return extensionRegistry.get(extensionName);
	}
	
	/**
	 * Checks if an extension class is registered
	 * @param extensionName Namespace of the extension
	 * @return True if this extension class has been registered, false otherwise
	 */
	public static boolean hasExtensionClass(Namespace extensionName)
	{
		return extensionRegistry.containsKey(extensionName);
	}
	
	/**
	 * Send a PostItem to a player
	 * <p>
	 * This post item can be either a {@link coffee.dape.playerdata.data.PlayerPostBox.Message} or {@link coffee.dape.playerdata.data.PlayerPostBox.Parcel}
	 * @param p Player to send post to
	 * @param postItem PostItem to send
	 * @return True if PostItem sent, false otherwise
	 */
	public static boolean sendPostItem(UUID recepient,PostItem postItem)
	{
		PostSentEvent pse = new PostSentEvent(PlayerUtils.getPlayer(postItem.getSender()),recepient,postItem);
		Bukkit.getPluginManager().callEvent(pse);
		
		if(pse.isCancelled()) { return false; }
		
		postItem.setDateSent(LocalDateTime.now());
		PlayerDataCtrl.getPlayerData(postItem.getSender()).getPostData().addSentPost(postItem);
		PlayerDataCtrl.getPlayerData(recepient).getPostData().addInboxPost(postItem);
		
		if(!PlayerUtils.isOnline(recepient)) { return true; }
		Player onlineRecepient = PlayerUtils.getPlayer(recepient);
		
		switch(postItem)
		{
			case Message message -> Toasts.queueToast("New post recieved!",message.getStack().getType(),Frame.TASK,onlineRecepient);
			case Parcel parcel -> Toasts.queueToast("New post recieved!",parcel.getStack().getType(),Frame.TASK,onlineRecepient);
			default -> {}
		}
		
		return true;
	}
	
	public static class Config implements Configurable
	{
		public static final ConfigItem<Boolean> SAVE_ADVANCEMENTS = new ConfigItem<>("player_data.save_advancements",true,"If a players advancements should be saved.");
		public static final ConfigItem<Boolean> SAVE_STATISTICS = new ConfigItem<>("player_data.save_statistics",true,"If a players statistics should be saved.");
	}
}