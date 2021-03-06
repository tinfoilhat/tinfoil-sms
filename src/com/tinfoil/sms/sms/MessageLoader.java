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

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.loader.Loader;

public class MessageLoader extends Loader{
	
    private boolean update;
    private Handler handler;
    private String number;
    
    /**
     * Create the object and start the thread 
     * @param context The activity context
     * @param update Whether the load is an update or not.
     * @param handler The Handler that takes care of UI setup after the thread
     * has finished
     */
    public MessageLoader(String number, Context context, boolean update, Handler handler)
    {
    	super(context);
    	this.update = update;
    	this.handler = handler;
    	this.number = number;
    	start();
    }

    @Override
	public void execution() {
    	
		if(!update)
		{
	        final boolean isTrusted = loader.isTrustedContact(number);
	        
			List<String[]> msgList2 = loader.getSMSList(number);
			final int unreadCount = loader.getUnreadMessageCount(number);

	        //Retrieve the name of the contact from the database
			
			TrustedContact tc = loader.getRow(number);
			
			if(tc != null)
			{
		        String contact_name = tc.getName();
				
		        Message msg = new Message();
	        	Bundle b = new Bundle();
	        	b.putString(SendMessageActivity.CONTACT_NAME, contact_name);
	        	b.putBoolean(SendMessageActivity.IS_TRUSTED, isTrusted);
	        	b.putSerializable(SendMessageActivity.MESSAGE_LIST, (Serializable)msgList2);
	        	b.putInt(SendMessageActivity.UNREAD_COUNT, unreadCount);
	        	msg.setData(b);
	        	msg.what = SendMessageActivity.LOAD;
		        
		        this.handler.sendMessage(msg);
			}
			else
			{
				this.handler.sendEmptyMessage(SendMessageActivity.FINISH);
			}
		}
		else
		{
			List<String[]> msgList2 = loader.getSMSList(number);
			loader.updateMessageCount(number, 0);
			setUpdate(false);
			
			Message msg = new Message();
        	Bundle b = new Bundle();
        	b.putSerializable(SendMessageActivity.MESSAGE_LIST, (Serializable)msgList2);
        	msg.setData(b);
        	msg.what = SendMessageActivity.UPDATE;
	        
	        this.handler.sendMessage(msg);
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
}