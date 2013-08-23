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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.ContactChild;
import com.tinfoil.sms.dataStructures.ContactParent;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.loader.Loader;

/**
 * Loads in the contacts from the database and formats them.
 *
 */
public class ManageContactsLoader extends Loader{
	
	public static final String EMPTYLIST = "emptyListValues";
	private boolean exchange;
	private Handler handler;
	public boolean[] trusted;
	
	/**
	 * TODO comment
	 * @param handler
	 * @param exchange
	 */
	public ManageContactsLoader(Context context, Handler handler, boolean exchange)
	{
		super(context);
		this.handler = handler;
		this.exchange = exchange;
    	start();
	}
	
	@Override
	public void execution() {

		/*
		 * Load the list of contacts
		 */
		String emptyListValue = "";
		
    	if(exchange)
    	{
    		ManageContactsActivity.tc = loader.getAllRows(DBAccessor.UNTRUSTED);
    		emptyListValue = context.getString(R.string.empty_loader_value);
    	}
    	else
    	{
    		ManageContactsActivity.tc = loader.getAllRows(DBAccessor.TRUSTED);
    		emptyListValue = context.getString(R.string.empty_loader_trusted_value);
    	}

        if (ManageContactsActivity.tc != null)
        {
        	ManageContactsActivity.contacts = new ArrayList<ContactParent>();
            int size = 0;

            for (int i = 0; i < ManageContactsActivity.tc.size(); i++)
            {
                size = ManageContactsActivity.tc.get(i).getNumber().size();

                ManageContactsActivity.contactNumbers = new ArrayList<ContactChild>();

                trusted = loader.isNumberTrusted(ManageContactsActivity.tc.get(i).getNumber());

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
	}
}