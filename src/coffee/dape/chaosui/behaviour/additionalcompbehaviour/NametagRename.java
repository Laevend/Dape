package coffee.dape.chaosui.behaviour.additionalcompbehaviour;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.events.ChaosChangeNameEvent;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.handler.ChangeNameHandler;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.ItemUtils;
import coffee.dape.utils.MaterialUtils;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.SoundUtils;

public class NametagRename
{
	/**
	 * Changes the name of a paginator item
	 * @param e ChaosClickEvent
	 * @return 
	 */
	public static void onChangeName(ChaosClickEvent e)
	{		
		if(e.getBuilder().getHandler() instanceof ChangeNameHandler handler)
		{
			if(ItemUtils.isNullOrAir(e.getWhoClicked().getItemOnCursor())) { SoundUtils.playSound((Player) e.getWhoClicked(),Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,1.0f,0.1f); return; }
			ItemStack nameTag = e.getWhoClicked().getItemOnCursor();
			
			if(nameTag.getType() != Material.NAME_TAG) { return; }
			if(!nameTag.hasItemMeta()) { PrintUtils.actionBar(e.getWhoClicked(),"&cThis name tag has no name!"); return; }
			if(!nameTag.getItemMeta().hasDisplayName()) { PrintUtils.actionBar(e.getWhoClicked(),"&cThis name tag has no name!"); return; }
			
			ItemStack stack = e.getView().getItem(e.getRawSlot());
			String newName = ColourUtils.translate(nameTag.getItemMeta().getDisplayName());
			String oldName = stack.getItemMeta().hasDisplayName() ? stack.getItemMeta().getDisplayName() : MaterialUtils.getNameFromMaterial(stack.getType());
			
			ChaosChangeNameEvent ccne = new ChaosChangeNameEvent(e.getView(),e.getBuilder(),stack,oldName,newName,(Player) e.getWhoClicked(),e.getRawSlot());
			handler.onNameChange(ccne);
			
			if(ccne.isCancelled()) { return; }
			e.getView().setItem(e.getRawSlot(),ItemBuilder.of(stack).name(newName).create());
			SoundUtils.playSound((Player) e.getWhoClicked(),Sound.BLOCK_FUNGUS_BREAK,2.0f);
		}
	}
}
