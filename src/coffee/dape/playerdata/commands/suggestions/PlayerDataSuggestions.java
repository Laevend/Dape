package coffee.dape.playerdata.commands.suggestions;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.bukkit.Bukkit;

import coffee.dape.Dape;
import coffee.dape.cmdparsers.astral.parser.Comparators;
import coffee.dape.cmdparsers.astral.suggestions.ConditionalSuggestionList;
import coffee.dape.cmdparsers.astral.suggestions.Suggestions;
import coffee.dape.playerdata.PlayerDataCtrl;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PlayerUtils;
import coffee.dape.utils.structs.Namespace;

public class PlayerDataSuggestions
{
	public static ConditionalSuggestionList playerDataBackupFiles()
	{
		Namespace listNamespace = Namespace.of(Dape.getNamespaceName(),"player_data_backup_files");
		if(Suggestions.hasSuggestionList(listNamespace)) { return Suggestions.get(listNamespace).asConditional(); }
		
		Suggestions.addSuggestionList(new ConditionalSuggestionList(listNamespace,Comparators.NUMERICALLY_BY_STRING)
		{
		    @Override
		    protected void build(String playerName)
		    {
		    	if(!PlayerUtils.isOnline(playerName)) { return; }
				
				UUID uuid = Bukkit.getPlayer(playerName).getUniqueId();
				Path dataBackups = PlayerDataCtrl.getPlayerData(uuid).getPlayerDataBackupDirectory();
				
				if(!Files.exists(dataBackups)) { add("<none>"); return; }
				
				try(DirectoryStream<Path> stream = Files.newDirectoryStream(dataBackups))
				{
					for(Path entry : stream)
					{
						if(!Files.isDirectory(entry)) { continue; }
						
						try(DirectoryStream<Path> backupFilesStream = Files.newDirectoryStream(entry))
						{
							for(Path backupFilePath : backupFilesStream)
							{
								if(Files.isDirectory(backupFilePath)) { continue; }
								
								add(entry.getFileName().toString() + "/" + backupFilePath.getFileName());
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
					Logg.error("Could not stream directory " + dataBackups.toString(),e);
					Logg.fatal("Could not clean up old backup files!");
					return;
				}
		    }
		});
		
		return Suggestions.get(listNamespace).asConditional();
	}
}
