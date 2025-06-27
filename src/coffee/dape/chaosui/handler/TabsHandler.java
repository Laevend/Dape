package coffee.dape.chaosui.handler;

import coffee.dape.chaosui.events.ChaosClickEvent;
import coffee.dape.chaosui.interfaces.tab.TabButton;

public interface TabsHandler
{	
	public void onClickTab(ChaosClickEvent e,TabButton tabButtonClicked);
}
