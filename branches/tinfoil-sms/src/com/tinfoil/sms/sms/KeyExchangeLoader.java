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
    //private Context context;
	private Thread thread;
    //private boolean update;
    private Handler handler;
    //private ArrayList<Entry> entries;
    
    /**
     * Create the object and start the thread 
     * @param context The activity context
     * @param update Whether the load is an update or not.
     * @param handler The Handler that takes care of UI setup after the thread
     * has finished
     */
    public KeyExchangeLoader(Handler handler)
    {
    	//this.context = context;
    	//this.update = update;
    	this.handler = handler;
    	//this.entries = entries;
    	thread = new Thread(this);
		thread.start();
    }
	
	@Override
	public void run() {
		while (loopRunner)
		{
			KeyExchangeManager.entries = MessageService.dba.getAllKeyExchangeMessages();
			if(KeyExchangeManager.entries != null)
			{
				String[] numbers = new String[KeyExchangeManager.entries.size()];
				
				for(int i = 0; i < KeyExchangeManager.entries.size(); i++)
				{
					numbers[i] = MessageService.dba.getRow(
							KeyExchangeManager.entries.get(i).getNumber()).getName() 
							+ ", " + KeyExchangeManager.entries.get(i).getNumber();
				}
				

				Message msg = new Message();
	        	Bundle b = new Bundle();
	        	b.putStringArray(KeyExchangeManager.COMPLETE, numbers);
	        	msg.setData(b);
		        
		        this.handler.sendMessage(msg);
			}
			else
			{		        
		        this.handler.sendEmptyMessage(KeyExchangeManager.EMPTY);
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
     * Update whether the thread is running to update the list of contacts or
     * load from scratch, updating takes slightly less time and should be used
     * when possible.
     * @param update Whether the list needs to be updated or not.
     */
    /*public synchronized void setUpdate(boolean update) {
		this.update = update;
	}*/
    
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
