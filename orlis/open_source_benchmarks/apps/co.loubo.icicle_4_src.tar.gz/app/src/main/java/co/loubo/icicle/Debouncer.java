package co.loubo.icicle;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.Handler;


public class Debouncer {
	  private final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
	  private final ConcurrentHashMap<Runnable, TimerTask> delayedMap = new ConcurrentHashMap<>();
	  private final Handler mFreenetHandler;
	  private final int interval;

	  public Debouncer(Handler handler, int interval) { 
	    this.mFreenetHandler = handler;
	    this.interval = interval;
	  }

	  public void call(Runnable key) {
	    TimerTask task = new TimerTask(key);

	    TimerTask prev;
	    do {
	      prev = delayedMap.putIfAbsent(key, task);
	      if (prev == null)
	        sched.schedule(task, interval, TimeUnit.MILLISECONDS);
	    } while (prev != null && !prev.extend()); // Exit only if new task was added to map, or existing task was extended successfully
	  }

	  public void terminate() {
	    sched.shutdownNow();
	  }

	  // The task that wakes up when the wait time elapses
	  private class TimerTask implements Runnable {
	    private final Runnable key;
	    private long dueTime;    
	    private final Object lock = new Object();

	    public TimerTask(Runnable key) {        
	      this.key = key;
	      extend();
	    }

	    public boolean extend() {
	      synchronized (lock) {
	        if (dueTime < 0) // Task has been shutdown
	          return false;
	        dueTime = System.currentTimeMillis() + interval;
	        return true;
	      }
	    }

	    public void run() {
	      synchronized (lock) {
	        long remaining = dueTime - System.currentTimeMillis();
	        if (remaining > 0) { // Re-schedule task
	          sched.schedule(this, remaining, TimeUnit.MILLISECONDS);
	        } else { // Mark as terminated and invoke callback
	          dueTime = -1;
	          try {
	        	  mFreenetHandler.post(key);
	          } finally {
	            delayedMap.remove(key);
	          }
	        }
	      }
	    }
	  }
}