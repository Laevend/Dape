package coffee.dape.playerdata.gui;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.ChaosFactory.InvTemplate;
import coffee.dape.chaosui.anno.ChaosGUI;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.instancedargs.SessionArgs1;
import coffee.dape.playerdata.PlayerDataCtrl;
import coffee.dape.playerdata.data.PlayerData;
import coffee.dape.utils.GradientUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.MathUtils;
import coffee.dape.utils.TimeUtils;

/**
 * @author Laeven
 * @since 1.0.0
 */
@ChaosGUI(name = ChaosFactory.Common.PLAYER_PROFILE,template = InvTemplate.CHEST_3)
public class PlayerProfileGUIBuilder extends ChaosBuilder implements SessionArgs1<UUID>
{	
	public static ItemStack ONLINE_ICON = HeadUtils.getSkull("f5be49bbdd1db35def04ad11f06deaaf45c9666c05bc02bc8bf1444e99c7e");
	public static ItemStack JOINDATE_ICON = HeadUtils.getSkull("bf6395008f5a3df106d68f520d91a1a07b7393687f9fcbea7c281a882ae1db3e");
	public static ItemStack LASTJOINDATE_ICON = HeadUtils.getSkull("dd3c2be4eba9f104700fb1a45028aed9420b0f0ed8ce33a04efc221103ae0015");
	
	@Override
	public void init()
	{
		setFill(Material.WHITE_STAINED_GLASS_PANE);
	}

	@Override
	public void initSession(Player p,UUID playerProfileUUID)
	{
		PlayerData pd = PlayerDataCtrl.getPlayerData(playerProfileUUID);
		
		Button profileHead = new Button(4,ItemBuilder.ofSkull(playerProfileUUID).create());
		profileHead.setSound(Sound.ENTITY_VEX_CHARGE,1.0f);
		
		Button joinDate = new Button(10,ItemBuilder.of(JOINDATE_ICON.clone()).name("Join Date",GradientUtils.VERTEX_GREEN)
				.lore()
				.wrap(TimeUtils.getDateFormat(pd.getJoinDate(),TimeUtils.PATTERN_SLASH_dd_MM_yyyy) + 
						" " + 
						TimeUtils.getDateFormat(pd.getJoinDate(),TimeUtils.PATTERN_COLON_HH_mm_ss),GradientUtils.GOLDY).create());
		joinDate.setSound(Sound.ITEM_LODESTONE_COMPASS_LOCK,MathUtils.getRandom(1.0f,2.0f));
		
		Button lastJoinDate = new Button(13,ItemBuilder.of(LASTJOINDATE_ICON.clone()).name("Last Join Date",GradientUtils.VERTEX_GREEN)
				.lore()
				.wrap(TimeUtils.getDateFormat(pd.getLastJoin(),TimeUtils.PATTERN_SLASH_dd_MM_yyyy) +
						" " +
						TimeUtils.getDateFormat(pd.getLastJoin(),TimeUtils.PATTERN_COLON_HH_mm_ss),GradientUtils.GOLDY).create());
		lastJoinDate.setSound(Sound.ITEM_LODESTONE_COMPASS_LOCK,MathUtils.getRandom(1.0f,2.0f));
		
		Button playTime = new Button(16,ItemBuilder.of(ONLINE_ICON.clone()).name("Play Time",GradientUtils.VERTEX_GREEN)
				.lore()
				.wrap(PlayerDataCtrl.getTotalPlayTimeFormatted(playerProfileUUID),GradientUtils.GOLDY).create());
		playTime.setSound(Sound.ITEM_LODESTONE_COMPASS_LOCK,MathUtils.getRandom(1.0f,2.0f));
		
		putAllSessionComponents(p,profileHead,joinDate,lastJoinDate,playTime);
	}
	
	@Override
	public void buildGUI(InventoryView view) {}
}