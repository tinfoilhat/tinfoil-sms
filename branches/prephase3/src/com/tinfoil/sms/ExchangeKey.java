/** 
 * Copyright (C) 2011 Tinfoilhat
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

package com.tinfoil.sms;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;

public class ExchangeKey implements Runnable {

	private Context c;
	public static ProgressDialog keyDialog;
	private ArrayList<Number> untrusted;
	private ArrayList<Number> trusted;
	
	public void startThread(Context c, ArrayList<Number> untrusted, ArrayList<Number> trusted)
	{
		this.c = c;
		//TODO generate the trusted and untrusted list by checking if the number has a key
		this.untrusted = untrusted;
		this.trusted = trusted;
		Thread thread = new Thread(this);
		thread.start();
	}	
	
	public void run() {
		
		/*
		 * This is actually how removing contacts from trusted should look since it is just a
		 * deletion of keys. We don't care if the contact will now fail to decrypt messages that
		 * is the user's problem
		 */
		
		for(int i = 0; i < untrusted.size(); i++)
		{
			untrusted.get(i).clearPublicKey();
			MessageService.dba.updateKey(untrusted.get(i));
		}
		
		//TODO update to actually use proper key exchange (via sms)
		//Start Key exchanges 1 by 1, using the user specified time out.
		for(int i = 0; i < trusted.size(); i++)
		{
			trusted.get(i).setPublicKey();
			MessageService.dba.updateKey(trusted.get(i));
		}
		
		keyDialog.dismiss();
	}

}
