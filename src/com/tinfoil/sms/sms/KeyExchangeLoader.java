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

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.tinfoil.sms.dataStructures.Entry;
import com.tinfoil.sms.loader.Loader;
import com.tinfoil.sms.utility.MessageService;

public class KeyExchangeLoader extends Loader{

    private Handler handler;
    private ArrayList<Entry> entries;
    
    /**
     * Create the object and start the thread 
     * @param context The activity context
     * @param update Whether the load is an update or not.
     * @param handler The Handler that takes care of UI setup after the thread
     * has finished
     */
    public KeyExchangeLoader(Context context, Handler handler)
    {
    	super(context);
    	this.handler = handler;
    	start();
    }
	
	@Override
	public void execution() {
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
	}

    /**
     * Get the list of entries in the database
     * @return The list of key exchanges currently pending. 
     */
    public synchronized ArrayList<Entry> getEntries() {
   		return entries;
   	}
}
