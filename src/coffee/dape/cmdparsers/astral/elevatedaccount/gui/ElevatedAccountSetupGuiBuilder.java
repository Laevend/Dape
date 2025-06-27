package coffee.dape.cmdparsers.astral.elevatedaccount.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory;
import coffee.dape.chaosui.ChaosFactory.InvTemplate;
import coffee.dape.chaosui.anno.ChaosGUI;
import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.instancedargs.SessionPlayer;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl;
import coffee.dape.cmdparsers.astral.elevatedaccount.ElevatedAccountCtrl.AuthMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.AuthenticationMethod;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.email.EmailSetup;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.staticpin.StaticPinSetup;
import coffee.dape.cmdparsers.astral.elevatedaccount.authmethod.totp.TOTPSetup;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.Logg;
import coffee.dape.utils.PrintUtils;
import coffee.dape.utils.SoundUtils;
import coffee.dape.utils.StdCols;

@ChaosGUI(name = ChaosFactory.Common.AUTHENTICATION_SETUP,template = InvTemplate.CHEST_3)
public class ElevatedAccountSetupGuiBuilder extends ChaosBuilder implements SessionPlayer
{
	public static ItemStack STATIC_PIN_ICON = HeadUtils.getSkull("7e1959dd4a10841dbf5e02749a2f5b09cc47874ec182fc544302decb6232947c");
	public static ItemStack TOTP_ICON = HeadUtils.getSkull("297d6d7be985d0622a48e90698e9073f7ff8813292812ebd1730dba0e01cf18f");
	public static ItemStack EMAIL_OTP_ICON = HeadUtils.getSkull("378a669adef1cad344c60ac94632d295d1389acceab24b5d208a9eba8be45b7c");
	public static ItemStack YUBI_KEY_ICON = HeadUtils.getSkull("6144ccf110bc31ce2424c879567f43ae2f665968447f9fce858f62737f39c10");
	public static ItemStack MICROSOFT_AUTH_ICON = HeadUtils.getSkull("b703a4933c8755b57c19c6a12cfb670a9c054c423c14f35987a0edbc3d4d384e");
	
	@Override
	public void init()
	{
		setOpenSound(SoundUtils.HEAVY_CLINK);		
		setFill(Material.GRAY_STAINED_GLASS_PANE);
	}

	@Override
	public void initSession(Player p)
	{
		int minAuthMethods = ElevatedAccountCtrl.Config.MIN_AUTH_METHODS.get();
		
		Button info = new Button(4,ItemBuilder.of(HeadUtils.INFO_ICON.clone())
				.name("Information",ColourUtils.VISTA_BLUE)
				.lore().wrap(ColourUtils.applyColour("Setup your elevated account by selecting " + minAuthMethods + " or more authentication methods",StdCols.TEXT_DEFAULT))
				.create());		
		
		List<Button> setupButtons = new ArrayList<>();
		
		// Static pin is available for setup
		if(ElevatedAccountCtrl.Config.STATIC_PIN.get())
		{
			setupButtons.add(getAuthButton(p,AuthMethod.STATIC_PIN,"Static Pin Authentication",STATIC_PIN_ICON.clone(),new ChaosActionListener()
			{
				public void onClick(ChaosClickEvent e)
				{
					SoundUtils.playSound((Player) e.getWhoClicked(),Sound.ENTITY_BOAT_PADDLE_LAND,1.0f);
					e.getWhoClicked().closeInventory();
					StaticPinSetup.getInstance((Player) e.getWhoClicked()).startSetup((Player) e.getWhoClicked());
				}
			}));
		}
		
		// TOTP is available for setup
		if(ElevatedAccountCtrl.Config.TOTP.get())
		{
			setupButtons.add(getAuthButton(p,AuthMethod.TIMED_OTP,"Timed One Time Passcode Authentication",TOTP_ICON.clone(),new ChaosActionListener()
			{
				public void onClick(ChaosClickEvent e)
				{
					SoundUtils.playSound((Player) e.getWhoClicked(),Sound.ENTITY_BOAT_PADDLE_LAND,1.0f);
					e.getWhoClicked().closeInventory();
					TOTPSetup.getInstance((Player) e.getWhoClicked()).startSetup((Player) e.getWhoClicked());
				}
			}));
		}
		
		// Email OTP is available for setup
		if(ElevatedAccountCtrl.Config.EMAIL_OTP.get())
		{
			setupButtons.add(getAuthButton(p,AuthMethod.EMAIL_OTP,"Email One Time Passcode Authentication",EMAIL_OTP_ICON.clone(),new ChaosActionListener()
			{
				public void onClick(ChaosClickEvent e)
				{
					SoundUtils.playSound((Player) e.getWhoClicked(),Sound.ENTITY_BOAT_PADDLE_LAND,1.0f);
					e.getWhoClicked().closeInventory();
					EmailSetup.getInstance((Player) e.getWhoClicked()).startSetup((Player) e.getWhoClicked());
				}
			}));
		}
		
		// Yubi Key is available for setup
		if(ElevatedAccountCtrl.Config.YUBI_KEY.get())
		{
			setupButtons.add(getAuthButton(p,AuthMethod.YUBI_KEY,"Yubi Key Authentication",YUBI_KEY_ICON.clone(),new ChaosActionListener()
			{
				public void onClick(ChaosClickEvent e)
				{
					PrintUtils.actionBar(e.getWhoClicked(),"&cNot implemented");
					SoundUtils.playErrorSound((Location) e.getWhoClicked());
					e.getWhoClicked().closeInventory();
				}
			}));
		}
		
		if(ElevatedAccountCtrl.Config.MICROSOFT_AUTH.get())
		{
			setupButtons.add(getAuthButton(p,AuthMethod.MICROSOFT_AUTH,"Microsoft Authentication",MICROSOFT_AUTH_ICON.clone(),new ChaosActionListener()
			{
				public void onClick(ChaosClickEvent e)
				{
					PrintUtils.actionBar(e.getWhoClicked(),"&cNot implemented");
					SoundUtils.playErrorSound((Location) e.getWhoClicked());
					e.getWhoClicked().closeInventory();
				}
			}));
		}
		
		if(setupButtons.size() == 0)
		{
			PrintUtils.error(p,"An error occured, no authentication methods are available to setup!");
			return;
		}
		
		switch(setupButtons.size())
		{
			case 1 -> setupButtons.get(0).setOccupyingSlot(13);
			case 2 ->
			{
				setupButtons.get(0).setOccupyingSlot(11);
				setupButtons.get(1).setOccupyingSlot(15);
			}
			case 3 ->
			{
				setupButtons.get(0).setOccupyingSlot(10);
				setupButtons.get(1).setOccupyingSlot(13);
				setupButtons.get(2).setOccupyingSlot(16);
			}
			case 4 ->
			{
				setupButtons.get(0).setOccupyingSlot(10);
				setupButtons.get(1).setOccupyingSlot(12);
				setupButtons.get(2).setOccupyingSlot(14);
				setupButtons.get(3).setOccupyingSlot(16);
			}
			case 5 ->
			{
				setupButtons.get(0).setOccupyingSlot(9);
				setupButtons.get(1).setOccupyingSlot(11);
				setupButtons.get(2).setOccupyingSlot(13);
				setupButtons.get(3).setOccupyingSlot(15);
				setupButtons.get(4).setOccupyingSlot(17);
			}
			default ->
			{
				Logg.error("AuthButtons list is larger than expected!");
			}
		}
		
		putSessionComponent(p,info);
		putAllSessionComponents(p,setupButtons.toArray(ChaosComponent[]::new));
	}
	
	@Override
	public void buildGUI(InventoryView view) {}
	
	private Button getAuthButton(Player p,AuthMethod authMeth,String buttonTitle,ItemStack buttonIcon,ChaosActionListener action)
	{
		boolean hasSetupMethod = false;
		
		for(AuthenticationMethod meth : ElevatedAccountCtrl.getAccount(p).getAuthMethods())
		{
			if(meth.getAuthType() == authMeth) { hasSetupMethod = true; }
		}
		
		return new Button(0,ItemBuilder.of(buttonIcon)
			.name(buttonTitle,hasSetupMethod ? ColourUtils.DARK_WHITE : ColourUtils.TEXT_SUCCESS)
			.lore().wrap(hasSetupMethod ? ColourUtils.applyColour("Click to reset this authentication",StdCols.TEXT_DEFAULT) : ColourUtils.applyColour("Click to start setup",StdCols.TEXT_DEFAULT))
			.create(),action);
	}
}
