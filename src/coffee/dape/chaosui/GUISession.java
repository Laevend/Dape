package coffee.dape.chaosui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coffee.dape.chaosui.components.ChaosComponent;
import coffee.dape.chaosui.components.ChaosMultiComponent;
import coffee.dape.chaosui.slots.ChaosSlot;
import coffee.dape.utils.TimeUtils;
import coffee.dape.utils.clocks.RefillableIntervalClock;

public class GUISession
{
	private Player owner;
	private JsonObject guiData = new JsonObject();
	private SessionExpireClock expireClock = new SessionExpireClock();
	
	// Prevents navigation event adding previous GUI when using back buttons as it will cause a loop
	private boolean navigatingUsingBackButton = false;
	
	// Session Data about slots
	private Map<Integer,ChaosSlot> sessionSlots = new HashMap<>();
	
	// Not used in most cases
	private Map<String,Object> globalData = null;
	
	private static final String PREVIOUS_GUI = "previous_gui";
	private static final String GUI_PAGE_NUM = "gui_page_num";
	
	private static final String INPUT_BUTTON_SLOT = "input_button_slot";
	
	public void setInputButtonSlotClicked(ChaosBuilder builder,int inputButtonSlot)
	{
		setData(builder,INPUT_BUTTON_SLOT,inputButtonSlot);
	}
	
	public int getInputButtonSlotClicked(ChaosBuilder builder)
	{
		return getData(builder,INPUT_BUTTON_SLOT).getAsInt();
	}
	
	public void setGlobalData(String key,Object obj)
	{
		if(globalData == null) { globalData = new HashMap<>(); }
		globalData.put(key,obj);
	}
	
	public Object getGlobalData(String key)
	{
		if(globalData == null) { return null; }
		return globalData.get(key);
	}
	
	public boolean hasGlobalData(String key)
	{
		if(globalData == null) { return false; }
		return globalData.containsKey(key);
	}
	
	public Object removeGlobalData(String key)
	{
		if(globalData == null) { return null; }
		return globalData.remove(key);
	}
	
	public void setData(ChaosBuilder builder,String key,boolean data)
	{
		assertExist(builder);
		guiData.get(builder.getName()).getAsJsonObject().addProperty(key,data);
	}
	
	public void setData(ChaosBuilder builder,String key,char data)
	{
		assertExist(builder);
		guiData.get(builder.getName()).getAsJsonObject().addProperty(key,data);
	}
	
	public void setData(ChaosBuilder builder,String key,Number data)
	{
		assertExist(builder);
		guiData.get(builder.getName()).getAsJsonObject().addProperty(key,data);
	}
	
	public void setData(ChaosBuilder builder,String key,String data)
	{
		assertExist(builder);
		guiData.get(builder.getName()).getAsJsonObject().addProperty(key,data);
	}
	
	public void setPreviousGUI(ChaosBuilder builder,ChaosBuilder guiNavigatedFrom)
	{
		assertExist(builder);
		guiData.get(builder.getName()).getAsJsonObject().addProperty(PREVIOUS_GUI,guiNavigatedFrom.getName());
	}
	
	public String getPreviousGUI(ChaosBuilder builder)
	{
		return guiData.get(builder.getName()).getAsJsonObject().get(PREVIOUS_GUI).getAsString();
	}
	
	public boolean hasPreviousGUI(ChaosBuilder builder)
	{
		return guiData.get(builder.getName()).getAsJsonObject().has(PREVIOUS_GUI);
	}
	
	public void setPageNumber(ChaosBuilder builder,int pageNumber)
	{
		assertExist(builder);
		guiData.get(builder.getName()).getAsJsonObject().addProperty(GUI_PAGE_NUM,pageNumber);
	}
	
	public int getPageNumber(ChaosBuilder builder)
	{
		assertExist(builder);
		return guiData.get(builder.getName()).getAsJsonObject().get(GUI_PAGE_NUM).getAsInt();
	}
	
	public boolean hasPageNumber(ChaosBuilder builder)
	{
		assertExist(builder);
		return guiData.get(builder.getName()).getAsJsonObject().has(GUI_PAGE_NUM);
	}
	
	public boolean hasData(ChaosBuilder builder,String key)
	{
		if(!guiData.has(builder.getName())) { return false; }
		return guiData.get(builder.getName()).getAsJsonObject().has(key);
	}
	
	public JsonElement getData(ChaosBuilder builder,String key)
	{
		assertExist(builder);
		return guiData.get(builder.getName()).getAsJsonObject().get(key);
	}	
	
	private void assertExist(ChaosBuilder builder)
	{
		if(guiData.has(builder.getName())) { return; }
		guiData.add(builder.getName(),new JsonObject());
	}
	
	public ChaosBuilder getBuilder()
	{
		if(!ChaosFactory.isGUI(owner.getOpenInventory().getTitle())) { return null; }
		return ChaosFactory.getGUI(owner.getOpenInventory().getTitle());
	}
	
	public InventoryView getView()
	{
		if(!ChaosFactory.isGUI(owner.getOpenInventory().getTitle())) { return null; }
		return owner.getOpenInventory();
	}
	
	public boolean isNavigatingUsingBackButton()
	{
		return navigatingUsingBackButton;
	}

	public void setNavigatingUsingBackButton(boolean navigatingUsingBackButton)
	{
		this.navigatingUsingBackButton = navigatingUsingBackButton;
	}

	public void refresh()
	{
		expireClock.refill();
	}
	
	// Invalidates a session when its been at least 5 minutes since interacting with a GUI
	private class SessionExpireClock extends RefillableIntervalClock
	{
		private static final int secondsBeforeSessionExpires = 300;
		
		public SessionExpireClock()
		{
			super("GUI session clock",TimeUtils.secondsToTicks(secondsBeforeSessionExpires));
		}

		@Override
		public void execute() throws Exception
		{
			// Extend the session if the player is still inside a Chaos GUI or they're in input mode
			if(ChaosFactory.isGUI(owner.getOpenInventory().getTitle()) || ChaosFactory.isPlayerInTextInputMode(owner))
			{
				this.refill();
				return;
			}
			
			ChaosFactory.invalidateSession(owner);
		}
	}
	
	/**
	 * Wipes all data from the session
	 */
	public void wipeData()
	{
		guiData = new JsonObject();
	}
	
	/**
	 * Set a temporary GUI component that only exists for a specific player in an instance of a GUI
	 * @param InventoryView The players view of the inventory
	 * @param com Chaos Component to set
	 */
	public void setSessionComponent(ChaosComponent com) throws IllegalStateException
	{
		if(!sessionSlots.containsKey(com.getOccupyingSlot()))
		{
			sessionSlots.put(com.getOccupyingSlot(),new ChaosSlot(com.getOccupyingSlot()));
		}
		
		sessionSlots.get(com.getOccupyingSlot()).setSlotComponent(com);
	}
	
	/**
	 * Set a temporary GUI multi component that only exists for a specific player in an instance of a GUI
	 * @param mcom ChaosMultiComponent to set
	 */
	public void setSessionMultiComponent(ChaosMultiComponent mcom) throws IllegalStateException
	{
		for(ChaosComponent com : mcom.getComponents().values())
		{
			setSessionComponent(com);
		}
	}
	
	/**
	 * Checks if a temp slot has a chaos component inside it
	 * @param slot rawslot to check
	 * @return
	 */
	public boolean isSessionSlotOccupied(int slot)
	{
		if(!sessionSlots.containsKey(slot)) { return false; }
		return sessionSlots.get(slot).isOccupied();
	}
	
	public Map<Integer,ChaosSlot> getSessionSlots()
	{
		return sessionSlots;
	}
}
