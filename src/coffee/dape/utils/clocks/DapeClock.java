package coffee.dape.utils.clocks;

public interface DapeClock
{
	public void start();
	
	public void stop();
	
	public boolean isEnabled();
	
	public String getClockName();
}
