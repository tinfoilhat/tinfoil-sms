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
