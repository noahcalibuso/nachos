package nachos.threads;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
	waitQueue = ThreadedKernel.scheduler.newThreadQueue(false);
	
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    //  wait method 
    public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
		boolean intStatus = Machine.interrupt().disable(); // atomically (disable interruptions)
		waitQueue.waitForAccess(KThread.currentThread()); // enqueue
		conditionLock.release();
		KThread.sleep();		// suspend the current thread
		
		conditionLock.acquire();
		Machine.interrupt().restore(intStatus); // restore interruption
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() 
    {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		// if waitQueue is not empty, a process is removed from the waitQueue
		// and placed in the ready queue
		boolean intStatus = Machine.interrupt().disable();
		KThread nextThread = waitQueue.nextThread();
		if (nextThread != null) {
			nextThread.ready();
			Machine.interrupt().restore(intStatus);
		}
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
	// Wakes up every process in the waitQueue
    public void wakeAll() 
    {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		boolean intStatus = Machine.interrupt().disable();
		KThread nextThread = waitQueue.nextThread();
		while (nextThread != null) {
			nextThread.ready();
			nextThread = waitQueue.nextThread();
		}
		Machine.interrupt().restore(intStatus);
    }

    private Lock conditionLock;
    private ThreadQueue waitQueue;
}
   
