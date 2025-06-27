package coffee.dape.utils.clocks.absclocks;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;

import coffee.dape.event.AsyncClockTerminatedEvent;
import coffee.dape.utils.DelayUtils;
import coffee.dape.utils.Logg;
import coffee.dape.utils.security.Bouncer;

/**
 * 
 * @author Laeven
 * Represents a asynchronous clock that can run a task repeatedly
 */
public abstract class VariableRateAsyncClock 
{
	protected String clockName;
	protected boolean enabled = false;
	protected int continueAttempts = 3;		// Number of attempts to re-run the clock cycle if it fails
	protected int attempts = 1;				// Attempt number of running the clock cycle
	protected long interval;				// The interval time in ticks
	protected long minInterval;
	protected long maxInterval;
	protected Thread controlThread;
	protected ScheduledThreadPoolExecutor threadPool;
	protected int threads = 1;
	
	// timeout to stop a task if this time is reached
	protected long timeout = -1;
	
	// Time in milliseconds when the last interval was performed
	protected long lastTaskDuration = System.currentTimeMillis();
	
	/**
	 * Creates an Async clock
	 * @param clockName Name of clock
	 * @param minIntervalInMilliseconds Minimum interval in milliseconds
	 * @param maxIntervalInMilliseconds Maximum interval in milliseconds
	 */
	public VariableRateAsyncClock(String clockName,long minIntervalInMilliseconds,long maxIntervalInMilliseconds)
	{
		Bouncer.requireNotNullOrEmpty(clockName,"clockName cannot be null, empty or blank!");
		Objects.requireNonNull(minIntervalInMilliseconds,"Min interval cannot be null!");
		Objects.requireNonNull(maxIntervalInMilliseconds,"Max interval cannot be null!");
		
		this.clockName = clockName;
		this.minInterval = minIntervalInMilliseconds;
		this.maxInterval = maxIntervalInMilliseconds;
		this.threadPool = new ScheduledThreadPoolExecutor(threads);
	}
	
	/**
	 * Creates an Async clock
	 * @param clockName Name of clock
	 * @param minIntervalInMilliseconds Minimum interval in milliseconds
	 * @param maxIntervalInMilliseconds Maximum interval in milliseconds
	 * @param threadCount Number of threads to initialise this pool with
	 */
	public VariableRateAsyncClock(String clockName,long minIntervalInMilliseconds,long maxIntervalInMilliseconds,int threadCount)
	{
		Bouncer.requireNotNullOrEmpty(clockName,"clockName cannot be null, empty or blank!");
		Objects.requireNonNull(minIntervalInMilliseconds,"Min interval cannot be null!");
		Objects.requireNonNull(maxIntervalInMilliseconds,"Max interval cannot be null!");
		Objects.requireNonNull(threadCount,"threadcount cannot be null!");
		
		this.clockName = clockName;
		this.minInterval = minIntervalInMilliseconds;
		this.maxInterval = maxIntervalInMilliseconds;
		this.threads = threadCount;
		this.threadPool = new ScheduledThreadPoolExecutor(threads);
	}
	
	protected void run()
	{
		VariableRateAsyncClock instance = this;
		
		controlThread = new Thread()
		{
			@Override
			public void run()
			{
				while(enabled)
				{
					ScheduledFuture<?> future = threadPool.schedule(new Runnable()
					{
						@Override
						public void run()
						{
							if(!enabled) { return; }
							
							if(attempts > continueAttempts)
					    	{
								instance.stop();
					    		Logg.fatal("Clock " + clockName + " was canceled due to failing " + continueAttempts + " times");
					    		return;
					    	}
							
							try
					    	{
								instance.execute();
					    		attempts = 1;
					    	}
					    	catch(Exception e)
					    	{
					    		Logg.error(clockName + " tripped and threw an exception!",e);
					    		attempts++;
					    	}
						}
					},interval,TimeUnit.MILLISECONDS);
					
					try
					{
						future.get();
					}
					catch (InterruptedException e)
					{
						Logg.error("Clock " + clockName + " was interrupted waiting for task completion!",e);
					}
					catch (ExecutionException e)
					{
						Logg.error("Clock " + clockName + " threw an Execution Exception!",e);
					}
				}
			}
		};
		
		controlThread.start();
	}
	
	public abstract void execute() throws Exception;
	
	/**
	 * Starts this clock
	 * <p>
	 * Thread pool is re-initialised with the number of threads specified by this clock.
	 */
	public void start()
	{
		if(threadPool != null && enabled) { return; }
		this.threadPool = new ScheduledThreadPoolExecutor(threads);
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
		if(threadPool == null || !enabled) { return; }
		enabled = false;
		threadPool.shutdown();
		
		// Send out an event when the thread pool has finished terminating
		ExecutorService service = Executors.newSingleThreadExecutor();
		service.execute(new Runnable()
		{
			@Override
			public void run()
			{
				while(threadPool != null && !threadPool.isTerminated())
				{
					try
					{
						Thread.sleep(500);
					}
					catch(InterruptedException e)
					{
						Logg.error("Clock " + clockName + " was interrupted waiting for termination!",e);
						kill();
						return;
					}
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
	 * @return List of tasks that never commenced execution.
	 */
	public List<Runnable> kill()
	{
		if(threadPool == null) { return Collections.emptyList(); }
		enabled = false;
		return threadPool.shutdownNow();
	}
	
	public boolean isEnabled()
	{
		return threadPool == null ? false : !enabled ? false : true;
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
