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

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.loader.Loader;

public class RemoveContactsLoader extends Loader{

	private Thread thread;
	private boolean clicked;
	private boolean[] contact;
    private Handler handler;
    private ArrayList<TrustedContact> tc;
    
    /**
     * Create the object and start the thread 
     * @param context The activity context
     * @param update Whether the load is an update or not.
     * @param handler The Handler that takes care of UI setup after the thread
     * has finished
     */
    public RemoveContactsLoader(Context context, boolean clicked, boolean[] contact, ArrayList<TrustedContact> tc, Handler handler)
    {
    	super(context);
    	this.clicked = clicked;
    	this.contact = contact;
    	this.handler = handler;
    	this.tc = tc;
    	thread = new Thread(this);
		thread.start();
		
    }
    
	@Override
	public void execution() {
		if (this.clicked)
        {
            for (int i = 0; i < this.tc.size(); i++)
            {
                if (this.contact[i])
                {
                	loader.removeRow(this.tc.get(i).getANumber());
                }
            }
        }

        String[] names = update();
        
        android.os.Message msg = new android.os.Message();
    	Bundle b = new Bundle();
    	b.putSerializable(RemoveContactsActivity.TRUSTED, (Serializable) tc);
    	b.putBooleanArray(RemoveContactsActivity.CONTACTS, contact);
    	b.putStringArray(RemoveContactsActivity.NAMES, names);
    	msg.setData(b);
    	
    	if (tc != null)
    	{
    		msg.what = RemoveContactsActivity.UPDATE;
    	}
    	else
    	{
    		msg.what = RemoveContactsActivity.EMPTY;
    	}
    	
    	handler.sendMessage(msg);
	}
	
	/**
     * Updates the list of contacts
     */
    private String[] update()
    {
        String[] names;
        this.tc = loader.getAllRows(DBAccessor.ALL);

        if (this.tc != null)
        {
            //The string that is displayed for each item on the list 
            names = new String[this.tc.size()];
            this.contact = new boolean[this.tc.size()];
            
            for (int i = 0; i < this.tc.size(); i++)
            {
                names[i] = this.tc.get(i).getName();
                this.contact[i] = false;
            }
            //this.appAdapt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, names);
        }
        else
        {
            names = new String[] { context.getString(R.string.empty_loader_value) };
            //this.appAdapt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        }
        
        return names;

    }
    
    /**
     * The semaphore for waking the thread up to reload the contacts
     * @param start Whether to start the execution of the thread or not
     */
    public synchronized void setClicked(boolean clicked) {
		this.clicked = clicked;
		notifyAll();
	}
}
