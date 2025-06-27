package coffee.dape.feature.vaults;

import java.util.EnumSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Vault;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import coffee.dape.utils.DelayUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.ServerProps;

public class VaultCtrl implements Listener
{
	private static final String DT_VAULT_REUSE = "resuable_vault";
	private static final EnumSet<org.bukkit.block.data.type.Vault.State> unInteractableStates = EnumSet.of
	(
		org.bukkit.block.data.type.Vault.State.EJECTING,
		org.bukkit.block.data.type.Vault.State.INACTIVE,
		org.bukkit.block.data.type.Vault.State.UNLOCKING
	);
	
	@EventHandler
	public void onInteractWithVault(PlayerInteractEvent e)
	{
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if(e.getClickedBlock().getType() != Material.VAULT) { return; }
		if(e.getItem() == null) { return; }
		if(e.getItem().getType() != Material.TRIAL_KEY && e.getItem().getType() != Material.OMINOUS_TRIAL_KEY) { return; }
		
		Block vaultBlock = e.getClickedBlock();
		Vault vaultData = (Vault) vaultBlock.getBlockData();
		
		if(unInteractableStates.contains(vaultData.getVaultState())) { return; }
		
		//if(!DataUtils.has(DT_VAULT_REUSE,vault)) { return; }
		String commandWorldName;
		
		if(vaultBlock.getWorld().getName().startsWith(ServerProps.level_name()))
		{
			switch(vaultBlock.getWorld().getEnvironment())
			{
				case NORMAL -> commandWorldName = "overword";
				case NETHER -> commandWorldName = "the_nether";
				case THE_END -> commandWorldName = "the_end";
				default ->
				{
					Logg.error("Unkown environment! " + vaultBlock.getWorld().getEnvironment() + " for world " + vaultBlock.getWorld().getName());
					commandWorldName = "overword";
				}
			}
		}
		else
		{
			commandWorldName = vaultBlock.getWorld().getName();
		}
		
		DelayUtils.executeDelayedTask(() ->
		{
			// Dumb long command executed by console to remove nbt data from a vault that holds a list of players it has rewarded
			// This nbt list is removed so that the player can re-use the same vault
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"minecraft:execute in minecraft:" + commandWorldName + " run data remove block " + vaultBlock.getX() + " " + vaultBlock.getY() + " " + vaultBlock.getZ() + " server_data.rewarded_players");
			Logg.verb("Command send to console: \n" +
					"minecraft:execute in minecraft:" + commandWorldName + " run data remove block " + vaultBlock.getX() + " " + vaultBlock.getY() + " " + vaultBlock.getZ() + " server_data.rewarded_players",Logg.VerbGroup.FEATURE_VAULT);
		});
	}
}
