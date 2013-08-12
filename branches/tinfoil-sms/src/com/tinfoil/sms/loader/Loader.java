package com.tinfoil.sms.loader;

import com.bugsense.trace.BugSenseHandler;

public abstract class Loader implements Runnable {
	
    private boolean loopRunner = true;
    private boolean start = true;
    private Thread thread;
    
    public void start()
    {
    	thread = new Thread(this);
    	thread.start();
    }

	@Override
	public void run() {
		while (loopRunner)
		{
			execution();
			// Wait for the next time the list needs to be updated/loaded
			while(loopRunner && start)
			{
				synchronized(this){
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
			            BugSenseHandler.sendExceptionMessage("Type", "Loader Concurrency Issue", e);
					}
				}
			}
			
			setStart(true);
		}
	}
	
	public abstract void execution();
	
    /**
     * The semaphore for waking the thread up to reload the contacts
     * @param start Whether to start the execution of the thread or not
     */
    public synchronized void setStart(boolean start) {
		this.start = start;
		notifyAll();
	}
    
    /**
     * The semaphore for keeping the thread running. This can be left as true
     * until the activity is no longer in use (onDestroy) where it can be set to
     * false.
     * @param runner Whether the thread should be kept running
     */
    public synchronized void setRunner(boolean runner) {
		this.loopRunner = runner;
		notifyAll();
	}
}
