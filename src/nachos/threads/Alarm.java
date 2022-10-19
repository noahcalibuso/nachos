package nachos.threads;

import java.util.PriorityQueue;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
    	Machine.timer().setInterruptHandler(new Runnable() {
    		public void run() { timerInterrupt(); }
	    });
	
    	aLock = new Lock();
    	alarmCondition = new Condition2(aLock);
    	waitingThreads = new PriorityQueue<threadWakeTime>();
		
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    
   
    //  
    
    public void timerInterrupt() {
    	Lib.assertTrue(Machine.interrupt().disabled());
    	
    	while (!waitingThreads.isEmpty() && 
    			waitingThreads.peek().wakeTime <= Machine.timer().getTime()) 
    	{
    		alarmCondition = waitingThreads.poll().alarmCondition;
    		
    		if (alarmCondition != null) {
	    		aLock.acquire();
	    		alarmCondition.wake();
	    		aLock.release();
    		}
    	}
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) { 
    	boolean intStatus = Machine.interrupt().disable();
    	
    	long wakeTime = Machine.timer().getTime() + x;
    	Condition2 alarmCondition = new Condition2(aLock);
    	threadWakeTime threadWakeTime = new threadWakeTime(alarmCondition, wakeTime);
    	
    	waitingThreads.add(threadWakeTime);
    	
    	aLock.acquire();
    	alarmCondition.sleep();
    	aLock.release();
    	
    	Machine.interrupt().restore(intStatus);
    }
    
    private class threadWakeTime implements Comparable<threadWakeTime> 
    {
    	 // one condition var per thread used to put it to sleep and wake it up
    	public threadWakeTime(Condition2 alarmCondition, long wakeTime) 
    	{
    		this.alarmCondition = alarmCondition;
    		this.wakeTime = wakeTime;
    	}
    	
		@Override
		public int compareTo(threadWakeTime threadWakeTime) 
		{
			if (this.wakeTime > threadWakeTime.wakeTime) 
				return 1;
			else if (this.wakeTime < threadWakeTime.wakeTime)
				return -1;
			else 
				return 0;
		}
		private Condition2 alarmCondition;
    	private long wakeTime;
    }
    
    private Lock aLock;
    Condition2 alarmCondition;
    private PriorityQueue<threadWakeTime> waitingThreads;
}
