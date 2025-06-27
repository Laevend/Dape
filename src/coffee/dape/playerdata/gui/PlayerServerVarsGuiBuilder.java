package coffee.dape.playerdata.gui;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.ChaosFactory.InvTemplate;
import coffee.dape.chaosui.GUISession;
import coffee.dape.chaosui.anno.ChaosGUI;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.interfaces.paginator.Paginator;
import coffee.dape.playerdata.data.PlayerServerVariables;
import coffee.dape.playerdata.data.PlayerServerVariables.ServerVarCategory;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;

@ChaosGUI(name = ChaosFactory.Common.PLAYER_SERVER_VARIABLES,template = InvTemplate.CHEST_6)
public class PlayerServerVarsGuiBuilder extends ChaosBuilder
{
	public static final String DT_SERVER_VAR_CAT = "server_var_cat";
	public static PlayerServerVarsGuiBuilder instance;
	
	@Override
	public void init()
	{
		setInterface(new Paginator(List.of(PlayerServerVariables.ServerVarCategory.values())));
		
		putStaticComponent(new Button(4,ItemBuilder.of(HeadUtils.INFO_ICON.clone())
				.name("Information",ColourUtils.VISTA_BLUE)
				.lore()
				.wrap(ColourUtils.applyColour("This is a debug menu meant for manually changing server variables for a player",ColourUtils.TEXT))
				.commit().create()));
		
		instance = this;
	}

	@Override
	public void buildGUI(InventoryView view) {}
	
	public void setServerVarCategoryInUse(Player p,ServerVarCategory cat)
	{
		GUISession sess = ChaosFactory.getSession(p);
		sess.setData(this,DT_SERVER_VAR_CAT,cat.toString());
	}
	
	public ServerVarCategory getServerVarCategoryInUse(Player p)
	{
		GUISession sess = ChaosFactory.getSession(p);
		
		if(!sess.hasData(this,DT_SERVER_VAR_CAT)) { return null; }
		
		return ServerVarCategory.valueOf(sess.getData(this,DT_SERVER_VAR_CAT).getAsString().toUpperCase());
	}
	
	public void setPlayerInUse(Player p,UUID playerInUse)
	{
		GUISession sess = ChaosFactory.getSession(p);
		sess.setData(this,"player_in_use",playerInUse.toString());
	}
	
	public UUID getPlayerInUse(Player p)
	{
		GUISession sess = ChaosFactory.getSession(p);
		
		if(!sess.hasData(this,"player_in_use")) { return null; }
		
		return UUID.fromString(sess.getData(this,"player_in_use").getAsString());
	}
}
