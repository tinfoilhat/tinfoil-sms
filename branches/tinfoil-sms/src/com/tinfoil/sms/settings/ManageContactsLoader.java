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

public class ManageContactsLoader implements Runnable {
	
	public static final String EMPTYLIST = "emptyListValues";
	private boolean loopContinue = true;
	private boolean refresh = false;
	private boolean exchange;
	//private Context context;
	private Handler handler;
	private Thread thread;
	
	public boolean[] trusted;
	
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
    		Log.v("Thread", String.valueOf(refresh));
    		/**/

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
	        	//handler.sendEmptyMessage(ManageContactsActivity.EMPTY);
	        }		        
	        
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
	
    public synchronized void setRefresh(boolean refresh) {
		this.refresh = refresh;
		notifyAll();
	}
    
    public synchronized void setRunner(boolean runner) {
		this.loopContinue = runner;
		notifyAll();
	}
}