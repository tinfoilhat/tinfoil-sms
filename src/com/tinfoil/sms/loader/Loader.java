/** 
 * Copyright (C) 2013 Jonathan Gillett, Joseph Heron
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tinfoil.sms.loader;

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;
import com.tinfoil.sms.database.DBAccessor;

public abstract class Loader implements Runnable {
	
    private boolean loopRunner = true;
    private boolean start = true;
    private Thread thread;
    
    protected Context context;
    
    protected DBAccessor loader;
    
    public Loader(Context context)
    {
    	this.context = context;
    }
        
    public void start()
    {
    	thread = new Thread(this);
    	thread.start();
    }

	@Override
	public void run() {
		while (loopRunner)
		{
			loader = DBAccessor.createNewConnection(context);
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
