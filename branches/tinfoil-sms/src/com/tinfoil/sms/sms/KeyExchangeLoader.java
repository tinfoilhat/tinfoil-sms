package com.tinfoil.sms.sms;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.tinfoil.sms.dataStructures.Entry;
import com.tinfoil.sms.utility.MessageService;

public class KeyExchangeLoader implements Runnable{

	private boolean loopRunner = true;
    private boolean start = true;
	private Thread thread;
    private Handler handler;
    private ArrayList<Entry> entries;
    
    /**
     * Create the object and start the thread 
     * @param context The activity context
     * @param update Whether the load is an update or not.
     * @param handler The Handler that takes care of UI setup after the thread
     * has finished
     */
    public KeyExchangeLoader(Handler handler)
    {
    	this.handler = handler;
    	thread = new Thread(this);
		thread.start();
    }
	
	@Override
	public void run() {
		while (loopRunner)
		{
			entries = MessageService.dba.getAllKeyExchangeMessages();
			if(entries != null)
			{
				String[] numbers = new String[entries.size()];
				
				for(int i = 0; i < entries.size(); i++)
				{
					numbers[i] = MessageService.dba.getRow(
							entries.get(i).getNumber()).getName() 
							+ ", " + entries.get(i).getNumber();
				}
				

				Message msg = new Message();
	        	Bundle b = new Bundle();
	        	b.putStringArray(KeyExchangeManager.COMPLETE, numbers);
	        	b.putSerializable(KeyExchangeManager.ENTRIES, entries);
	        	msg.setData(b);
	        	msg.what = KeyExchangeManager.FULL;
		        
	        	this.handler.sendMessage(msg);
			}
			else
			{
				Message msg = new Message();
	        	Bundle b = new Bundle();
	        	//b.putSerializable(KeyExchangeManager.ENTRIES, entries);
	        	msg.setData(b);
	        	msg.what = KeyExchangeManager.EMPTY;
		        
	        	this.handler.sendMessage(msg);
			}

			// Wait for the next time the list needs to be updated/loaded
			while(loopRunner && start)
			{
				synchronized(this){
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			setStart(true);
		}
	}

    /**
     * Get the list of entries in the database
     * @return The list of key exchanges currently pending. 
     */
    public synchronized ArrayList<Entry> getEntries() {
   		return entries;
   	}
    
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
