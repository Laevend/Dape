package coffee.dape.chaosui.components.buttons;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.events.ChaosToggleButtonClickEvent;
import coffee.dape.chaosui.handler.ToggleButtonHandler;
import coffee.dape.chaosui.listeners.ChaosActionListener;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.Logg;
import coffee.dape.utils.SoundUtils;
import coffee.dape.utils.data.DataUtils;

/**
 * 
 * @author Laeven
 * Button that changed between green or red (to indicate if its outright disabled or enabled)
 */
public class ToggleButton extends ChaosComponent
{
	private ItemStack enabledStack;
	private ItemStack disabledStack;
	public static final String DTAG = "toggle_button_status";
	
	public ToggleButton(int slot,boolean enabled)
	{
		super(slot,null,Type.TOGGLEABLE_BUTTON);
		initToggleButton(enabled,null,null);
	}
	
	public ToggleButton(boolean enabled)
	{
		super(null,Type.TOGGLEABLE_BUTTON);
		initToggleButton(enabled,null,null);
	}
	
	public ToggleButton(int slot,boolean enabled,ItemStack enabledStack,ItemStack disabledStack)
	{
		super(slot,null,Type.TOGGLEABLE_BUTTON);
		initToggleButton(enabled,enabledStack,disabledStack);
	}
	
	public ToggleButton(boolean enabled,ItemStack enabledStack,ItemStack disabledStack)
	{
		super(null,Type.TOGGLEABLE_BUTTON);
		initToggleButton(enabled,enabledStack,disabledStack);
	}
	
	public ToggleButton(int slot,boolean enabled,String title)
	{
		super(slot,null,Type.TOGGLEABLE_BUTTON);
		initToggleButton(enabled,
				ItemBuilder.of(HeadUtils.REDSTONE_GREEN.clone())
				.name(title,ColourUtils.TEXT)
				.lore()
				.append("Enabled",ColourUtils.TEXT_SUCCESS)
				.append("")
				.wrap(ColourUtils.applyColour("(Click me to toggle)",ColourUtils.TEXT))
				.commit()
				.setData(ToggleButton.DTAG,ToggleStatus.ENABLED.toString())
				.create(),
			ItemBuilder.of(HeadUtils.REDSTONE_RED.clone())
				.name(title,ColourUtils.TEXT)
				.lore()
				.append("Disabled",ColourUtils.TEXT_ERROR)
				.append("")
				.wrap(ColourUtils.applyColour("(Click me to toggle)",ColourUtils.TEXT))
				.commit()
				.setData(ToggleButton.DTAG,ToggleStatus.DISABLED.toString())
				.create());
	}
	
	public ToggleButton(boolean enabled,String title)
	{
		super(null,Type.TOGGLEABLE_BUTTON);
		initToggleButton(enabled,
				ItemBuilder.of(HeadUtils.REDSTONE_GREEN.clone())
				.name(title,ColourUtils.TEXT)
				.lore()
				.append("Enabled",ColourUtils.TEXT_SUCCESS)
				.append("")
				.wrap(ColourUtils.applyColour("(Click me to toggle)",ColourUtils.TEXT))
				.commit()
				.setData(ToggleButton.DTAG,ToggleStatus.ENABLED.toString())
				.create(),
			ItemBuilder.of(HeadUtils.REDSTONE_RED.clone())
				.name(title,ColourUtils.TEXT)
				.lore()
				.append("Disabled",ColourUtils.TEXT_ERROR)
				.append("")
				.wrap(ColourUtils.applyColour("(Click me to toggle)",ColourUtils.TEXT))
				.commit()
				.setData(ToggleButton.DTAG,ToggleStatus.DISABLED.toString())
				.create());
	}
	
	private void initToggleButton(boolean enabled,ItemStack enabledStack_,ItemStack disabledStack_)
	{
		if(enabledStack_ == null)
		{
			this.enabledStack = ItemBuilder.of(HeadUtils.REDSTONE_GREEN.clone())
					.name("Enabled",ColourUtils.TEXT_SUCCESS)
					.lore("")
					.wrap(ColourUtils.applyColour("(Click me to toggle)",ColourUtils.TEXT))
					.commit()
					.setData(DTAG,ToggleStatus.ENABLED.toString())
					.create();
		}
		else
		{
			this.enabledStack = enabledStack_;
			DataUtils.set(DTAG,ToggleStatus.ENABLED.toString(),this.enabledStack);
		}
		
		if(disabledStack_ == null)
		{
			this.disabledStack = ItemBuilder.of(HeadUtils.REDSTONE_RED.clone())
					.name("Disabled",ColourUtils.TEXT_ERROR)
					.lore("")
					.wrap(ColourUtils.applyColour("(Click me to toggle)",ColourUtils.TEXT))
					.commit()
					.setData(DTAG,ToggleStatus.DISABLED.toString())
					.create();
		}
		else
		{
			this.disabledStack = disabledStack_;
			DataUtils.set(DTAG,ToggleStatus.DISABLED.toString(),this.disabledStack);
		}
		
		this.addActionListener(new ChaosActionListener()
		{
			@Override
			public void onClick(ChaosClickEvent e)
			{
				if(!DataUtils.has(DTAG,e.getView().getItem(e.getRawSlot())))
				{
					Logg.error("ChaosGUI '" + e.getBuilder().getName() + "' has a toggle button that has missing data tag for " + DTAG + "!");
					return;
				}
				
				// Check current item displayed and swap to the other
				if(DataUtils.get(DTAG,e.getView().getItem(e.getRawSlot())).asString().equals(ToggleStatus.DISABLED.toString()))
				{
					Logg.verb("Is Disabled",Logg.VerbGroup.CHAOS_UI);
					e.getView().setItem(e.getRawSlot(),enabledStack);
					SoundUtils.playSound((Player) e.getWhoClicked(),Sound.ENTITY_PUFFER_FISH_BLOW_UP,2.0f);
					
					if(e.getBuilder().getHandler() instanceof ToggleButtonHandler handler)
					{
						handler.onToggleButtonClick(new ChaosToggleButtonClickEvent(e.getView(),e.getRawSlot(),e.getBuilder(),(Player) e.getWhoClicked(),ToggleButton.ToggleStatus.DISABLED,ToggleButton.ToggleStatus.ENABLED));
					}
				}
				else
				{
					Logg.verb("Is Enabled",Logg.VerbGroup.CHAOS_UI);
					e.getView().setItem(e.getRawSlot(),disabledStack);
					SoundUtils.playSound((Player) e.getWhoClicked(),Sound.ENTITY_PUFFER_FISH_BLOW_OUT,0.1f);
					
					if(e.getBuilder().getHandler() instanceof ToggleButtonHandler handler)
					{
						handler.onToggleButtonClick(new ChaosToggleButtonClickEvent(e.getView(),e.getRawSlot(),e.getBuilder(),(Player) e.getWhoClicked(),ToggleButton.ToggleStatus.ENABLED,ToggleButton.ToggleStatus.DISABLED));
					}
				}
			}
		});
		
		setEnabled(enabled);
	}
	
	public enum ToggleStatus
	{
		ENABLED,
		DISABLED
	}

	public ItemStack getEnabledStack()
	{
		return enabledStack;
	}

	public ItemStack getDisabledStack()
	{
		return disabledStack;
	}
	
	public void setEnabledStack(ItemStack enabledStack)
	{
		this.enabledStack = enabledStack;
	}

	public void setDisabledStack(ItemStack disabledStack)
	{
		this.disabledStack = disabledStack;
	}

	public void setEnabled(boolean enabled)
	{
		if(enabled) { setAppearance(enabledStack); return; }
		setAppearance(disabledStack);
	}
	
	public boolean isEnabled()
	{
		ItemStack stack = getAppearance();
		return DataUtils.get(DTAG,stack).asString().equals(ToggleStatus.ENABLED.toString());
	}

	@Override
	public ItemStack getStack()
	{
		return getAppearance();
	}
	
	@Override
	public boolean isItemComponentType()
	{
		return true;
	}

	@Override
	public ChaosComponent getComponent()
	{
		return this;
	}
}