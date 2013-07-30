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

package com.tinfoil.sms.settings;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tinfoil.sms.dataStructures.ContactChild;
import com.tinfoil.sms.dataStructures.ContactParent;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.MessageService;

/**
 * Loads in the contacts from the database and formats them.
 *
 */
public class ManageContactsLoader implements Runnable {
	
	public static final String EMPTYLIST = "emptyListValues";
	private boolean loopContinue = true;
	private boolean refresh = false;
	private boolean exchange;
	private Handler handler;
	private Thread thread;
	
	public boolean[] trusted;
	
	//TODO change to constructor
	/**
	 * TODO comment
	 * @param handler
	 * @param exchange
	 */
	public void startThread(Handler handler, boolean exchange)
	{
		this.handler = handler;
		this.exchange = exchange;
    	thread = new Thread(this);
		thread.start();
	}
	
	public void run() {
    	while(loopContinue)
    	{
    		//TODO comment
    		String emptyListValue = "";
	    	
	    	Log.v("Thread", "In the thread");
	    	if(exchange)
	    	{
	    		ManageContactsActivity.tc = MessageService.dba.getAllRows(DBAccessor.UNTRUSTED);
	    		emptyListValue = "No contacts";
	    	}
	    	else
	    	{
	    		ManageContactsActivity.tc = MessageService.dba.getAllRows(DBAccessor.TRUSTED);
	    		emptyListValue = "No Trusted Contacts";
	    	}		    	
	
	        if (ManageContactsActivity.tc != null)
	        {
	        	ManageContactsActivity.contacts = new ArrayList<ContactParent>();
	            int size = 0;
	
	            for (int i = 0; i < ManageContactsActivity.tc.size(); i++)
	            {
	                size = ManageContactsActivity.tc.get(i).getNumber().size();
	
	                ManageContactsActivity.contactNumbers = new ArrayList<ContactChild>();
	
	                trusted = MessageService.dba.isNumberTrusted(ManageContactsActivity.tc.get(i).getNumber());
	
	                for (int j = 0; j < size; j++)
	                {
	                    //TODO change to use primary key from trusted contact table
	                	ManageContactsActivity.contactNumbers.add(new ContactChild(ManageContactsActivity.tc.get(i).getNumber(j),
	                			trusted[j], false));
	                }
	                ManageContactsActivity.contacts.add(new ContactParent(ManageContactsActivity.tc.get(i).getName(), ManageContactsActivity.contactNumbers));
	            }
	
	            handler.sendEmptyMessage(ManageContactsActivity.POP);
	        }
	        else
	        {
	        	Message msg = new Message();
	        	Bundle b = new Bundle();
	        	b.putString(EMPTYLIST, emptyListValue);
	        	msg.setData(b);
	        	msg.what = ManageContactsActivity.EMPTY;
	        	
	        	handler.sendMessage(msg);
	        }		        
	        
	        /* The wait until the contacts have changed and need to be loaded in
	         * again
	         */
	        while(!refresh && loopContinue)
    		{
    			Log.v("Thread", "Waiting");
    			
    			synchronized(this)
    			{
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    			}
    		}
    		
    		setRefresh(false);
	    }
	}
	
	/**
	 * Set the semaphore to signal the thread to reload the contacts again
	 * @param refresh Whether the contacts need to be reloaded or not
	 */
    public synchronized void setRefresh(boolean refresh) {
		this.refresh = refresh;
		notifyAll();
	}
    
    /**
     * The semaphore for keeping the thread running. This can be left as true
     * until the activity is no longer in use (onDestroy) where it can be set to
     * false.
     * @param runner Whether the thread should be kept running
     */
    public synchronized void setRunner(boolean runner) {
		this.loopContinue = runner;
		notifyAll();
	}
}