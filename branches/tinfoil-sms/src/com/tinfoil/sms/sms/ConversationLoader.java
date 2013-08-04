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

package com.tinfoil.sms.sms;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.tinfoil.sms.crypto.KeyGenerator;
import com.tinfoil.sms.dataStructures.User;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;

/**
 * Manages the thread used to query for the contacts' information.
 */
public class ConversationLoader implements Runnable {
	
    private boolean loopRunner = true;
    private boolean start = true;
    private Context context;
	private Thread thread;
    private boolean update;
    private Handler handler;
    
    /**
     * Create the object and start the thread 
     * @param context The activity context
     * @param update Whether the load is an update or not.
     * @param handler The Handler that takes care of UI setup after the thread
     * has finished
     */
    public ConversationLoader(Context context, boolean update, Handler handler)
    {
    	this.context = context;
    	this.update = update;
    	this.handler = handler;
    	thread = new Thread(this);
		thread.start();
    }

    public void run() {
		while (loopRunner)
		{
			MessageService.dba = new DBAccessor(context);
			DBAccessor loader = new DBAccessor(context);

	        SMSUtility.user = loader.getUserRow();
	         
	        if(SMSUtility.user == null)
	        {
	        	//Toast.makeText(context, "New key pair is generating...", Toast.LENGTH_SHORT).show();
	        	Log.v("First Launch", "keys are generating...");
	        	//Create the keyGenerator
		        KeyGenerator keyGen = new KeyGenerator();
		        
		        SMSUtility.user = new User(keyGen.generatePubKey(), keyGen.generatePriKey());
		        //Set the user's 
		        loader.setUser(SMSUtility.user);
	        }
	        
	        Log.v("public key", new String(SMSUtility.user.getPublicKey()));
	        //Toast.makeText(context, "Public Key " + new String(SMSUtility.user.getPublicKey()), Toast.LENGTH_LONG).show();
	        
	        Log.v("private key", new String(SMSUtility.user.getPrivateKey()));
	        //Toast.makeText(context, "Private Key " + new String(SMSUtility.user.getPrivateKey()), Toast.LENGTH_LONG).show();			
			
			ConversationView.msgList = loader.getConversations();
			if(!update) {
				handler.sendEmptyMessage(ConversationView.LOAD);
			}
			else
			{
				handler.sendEmptyMessage(ConversationView.UPDATE);
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
			
			start = true;
		}
	}
    
    /**
     * Update whether the thread is running to update the list of contacts or
     * load from scratch, updating takes slightly less time and should be used
     * when possible.
     * @param update Whether the list needs to be updated or not.
     */
    public synchronized void setUpdate(boolean update) {
		this.update = update;
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