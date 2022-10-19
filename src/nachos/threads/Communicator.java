package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */

// Use a linked list (queue) to queue up the things (threads) that are waiting 

public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	cLock = new Lock();
    	speaker = new Condition2(cLock);
    	listener = new Condition2(cLock);
    	waitingThreads = new LinkedList<Condition2>();
    	messageQueue = new LinkedList<Integer>();
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    // thread calls speak if no threat there just wait (threads are going to sleep) use condition variables
    // if another thread calls listen then speak
    public void speak(int word) {
    	cLock.acquire();
    	if (!messageQueue.isEmpty() && !waitingThreads.isEmpty()) {
    		waitingThreads.add(speaker);
    		messageQueue.add(word);
    		speaker.sleep();
    	}
    	else {
    		if (!waitingThreads.isEmpty() && messageQueue.isEmpty()) {
    			messageQueue.add(word);
    			Condition2 waitingListener = waitingThreads.remove();
    			waitingListener.wake();
    		}
    		else {
    			waitingThreads.add(speaker);
    			messageQueue.add(word);
    			speaker.sleep();
    		}
    	}    	
    	cLock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	cLock.acquire();
    	int word = -1;
    	if (!waitingThreads.isEmpty() && messageQueue.isEmpty()) {
    		waitingThreads.add(listener);
    		listener.sleep();
    		word = messageQueue.remove();
    	}
    	else {
    		if (!waitingThreads.isEmpty() && !messageQueue.isEmpty()) {
    			waitingThreads.remove().wake();
    			word = messageQueue.remove();
    		}
    		else if (waitingThreads.isEmpty() && !messageQueue.isEmpty()) {
    			word = messageQueue.remove();
    		}
    		else {
    			waitingThreads.add(listener);
    			listener.sleep();
    			word = messageQueue.remove();
    		}
    	}
    	cLock.release();
    	return word;
    }
    
    private Lock cLock;
    private Condition2 speaker;
    private Condition2 listener;
    LinkedList<Condition2> waitingThreads;
    LinkedList<Integer> messageQueue;
    
    
}
