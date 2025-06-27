package coffee.dape.utils.clocks.absclocks;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;

import coffee.dape.event.AsyncClockTerminatedEvent;
import coffee.dape.utils.DelayUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.security.Bouncer;

/**
 * 
 * @author Laeven
 * An Async Clock designed for semi-frequent saving operations.
 * <p>
 * To prevent massive IO use but also keep things as up to date as possible,
 * when an action is performed that requires writing, it is scheduled to happen
 * after a set amount of time. If no further actions requiring saving are performed
 * during this time, then the save is initiated. If an action is performed, the clock
 * will reset and wait again.
 * <p>
 * A hard limit of 5 minutes is used to force save to prevent the clock never saving
 * from infrequent action use
 * <p>
 * TLDR: Most up-to-date data is always saved with the least IO ops.
 */
public abstract class HybridAsyncClock 
{
	protected String clockName;
	protected boolean enabled = false;
	protected int continueAttempts = 3;		// Number of attempts to re-run the clock cycle if it fails
	protected int attempts = 1;				// Attempt number of running the clock cycle
	protected long interval;				// The interval time in ticks
	protected Thread controlThread;
	
	// Number of milliseconds that must elapse since clock was started before the scheduled save commences (assuming it is not delayed)
	protected long scheduledExecuteInterval = 30_000L; // 30 Seconds
	
	// Number of milliseconds that must elapse since clock was started before no more delays can be made and a force save will commence
	protected long forceExecuteInterval = 300_000L; // 5 Minutes
	
	// Time in milliseconds when the clock was started
	protected long clockStartTime;
	
	// Time in milliseconds when the last delay was performed
	protected long lastDelayTime;
	
	/**
	 * Creates an Asynchronous hybrid clock
	 * @param clockName Name of clock
	 * @param scheduledExecuteInterval Interval in milliseconds until {@link #execute()} is called given no calls to {@link #delay()}
	 * @param forceExecuteInterval Interval in milliseconds until {@link #execute()} is called regardless of further calls to {@link #delay()}
	 */
	public HybridAsyncClock(String clockName)
	{
		Bouncer.requireNotNullOrEmpty(clockName,"clockName cannot be null, empty or blank!");
		
		this.clockName = clockName;
	}
	
	/**
	 * Creates an Asynchronous hybrid clock
	 * @param clockName Name of clock
	 * @param scheduledExecuteInterval Interval in milliseconds until {@link #execute()} is called given no calls to {@link #delay()}
	 * @param forceExecuteInterval Interval in milliseconds until {@link #execute()} is called regardless of further calls to {@link #delay()}
	 */
	public HybridAsyncClock(String clockName,long scheduledExecuteInterval,long forceExecuteInterval)
	{
		Bouncer.requireNotNullOrEmpty(clockName,"clockName cannot be null, empty or blank!");
		Objects.requireNonNull(scheduledExecuteInterval,"Scheduled execute interval cannot be null!");
		Objects.requireNonNull(scheduledExecuteInterval,"Force execute interval cannot be null!");
		
		this.clockName = clockName;
		this.scheduledExecuteInterval = scheduledExecuteInterval;
		this.forceExecuteInterval = forceExecuteInterval;
	}
	
	protected void run()
	{
		clockStartTime = System.currentTimeMillis();
		attempts = 1;
		HybridAsyncClock instance = this;
	
		controlThread = new Thread()
		{
			@Override
			public void run()
			{
				while(enabled)
				{
					if((getDeltaMilliSinceDelay() >= scheduledExecuteInterval) || (getDeltaMilliSinceStart() >= forceExecuteInterval))
					{
						try
				    	{
				    		execute();
				    		instance.stop();
				    		attempts = 1;
			    			return;
				    	}
				    	catch(Exception e)
				    	{
				    		Logg.error("Error! Clock " + clockName + " tripped while trying to execute!",e);
				    		continueAttempts++;
							
							if(attempts > continueAttempts)
					    	{
								instance.stop();
					    		Logg.fatal("Clock " + clockName + " was canceled due to failing " + continueAttempts + " times");
					    		return;
					    	}
							
							return;
				    	}
					}
					
					try
					{
						// Clock speed of 1ms
						Thread.sleep(1L);
					}
					catch (InterruptedException e)
					{
						Logg.error("Error! Clock " + clockName + " was interrupted while trying to sleep!",e);
						instance.stop();
					}
				}
			}
		};
		
		controlThread.start();
	}
	
	public abstract void execute() throws Exception;
	
	/**
	 * Resets the duration for a scheduled save to 0
	 * <p>
	 * Delayed duration is increased by the elapsed duration before this delay is made.
	 * <p>
	 * Clock will delay the execute operation by MAX_MILLI_BEFORE_SCHEDULED_SAVE (by default this is 30 seconds).
	 * <p>
	 * If delayedDuration exceeds MAX_MILLI_BEFORE_FORCE_SAVE (by default this is 5 minutes) from too many delays,
	 * the execute operation takes place regardless of further delays.
	 */
	public synchronized void delay()
	{
		Logg.verb("Delaying...",Logg.VerbGroup.CLOCKS);
		Logg.verb("Delta T Before (" + getDeltaMilliSinceDelay() + ") Delta T since creation (" + getDeltaMilliSinceStart() + ")",Logg.VerbGroup.CLOCKS);	
		lastDelayTime = System.currentTimeMillis();
	}
	
	/**
	 * Get the delta milliseconds between now and when the clock was started
	 * @return Delta T
	 */
	private long getDeltaMilliSinceStart()
	{
		return System.currentTimeMillis() - clockStartTime;
	}
	
	/**
	 * Get the delta milliseconds between now and when the clock was last delayed (Gandalf: It was delayed)
	 * @return Delta T
	 */
	private long getDeltaMilliSinceDelay()
	{
		return System.currentTimeMillis() - lastDelayTime;
	}
	
	/**
	 * Starts this clock
	 * <p>
	 * Thread pool is re-initialised with the number of threads specified by this clock.
	 */
	public void start()
	{
		if(enabled) { return; }
		enabled = true;
		run();
	}
	
	/**
	 * Stops this clock
	 * <p>
	 * If the thread pool is null or this clock is not enabled, this method does nothing.
	 * <p>
	 * This method returns immediately and schedules the thread pool to shutdown after the executing task finishes.
	 * <p>
	 * The {@linkplain AsyncClockTerminatedEvent} event is called upon the thread pool terminating.
	 */
	public void stop()
	{
		if(controlThread == null || !enabled) { return; }
		enabled = false;
		
		// Send out an event when the thread pool has finished terminating
		ExecutorService service = Executors.newSingleThreadExecutor();
		service.execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					controlThread.join();
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				
				DelayUtils.executeDelayedBukkitTask(() ->
				{
					Bukkit.getPluginManager().callEvent(new AsyncClockTerminatedEvent(clockName));
				},1);
			}
		});
	}
	
	/**
	 * Kills this clock
	 */
	public void kill()
	{
		if(controlThread == null) { return; }
		enabled = false;
		controlThread.interrupt();
		return;
	}
	
	public boolean isEnabled()
	{
		return controlThread == null ? false : !enabled ? false : !controlThread.isAlive() ? false : true;
	}

	public String getClockName()
	{
		return clockName;
	}

	public void setClockName(String clockName)
	{
		Objects.requireNonNull(clockName,"clockName cannot be null!");
		this.clockName = clockName;
	}

	public int getContinueAttempts()
	{
		return continueAttempts;
	}
	
	/**
	 * Set the number of attempts this clock will attempt to run {@link #execute()} before it will timeout
	 * @param continueAttempts Number of attempts to restart the clock (should an exception be thrown) before it will timeout
	 */
	public void setContinueAttempts(int continueAttempts)
	{
		Objects.requireNonNull(continueAttempts,"continueAttempts cannot be null!");
		this.continueAttempts = continueAttempts;
	}

	public int getAttempts()
	{
		return attempts;
	}

	public long getInterval()
	{
		return interval;
	}

	public void setInterval(long interval)
	{
		Objects.requireNonNull(interval,"interval cannot be null!");
		this.interval = interval;
		this.stop();
		this.start();
	}
}
