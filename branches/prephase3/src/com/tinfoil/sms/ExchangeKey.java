package com.tinfoil.sms;

import android.content.Context;

public class ExchangeKey implements Runnable {

	private Context c;
	
	public void startThread(Context c)
	{
		this.c = c;
		Thread thread = new Thread(this);
		thread.start();
	}	
	
	public void run() {
		
		/*
		 * This is actually how removing contacts from trusted should look since it is just a
		 * deletion of keys. We don't care if the contact will now fail to decrypt messages that
		 * is the user's problem
		 */
		for(int i = 0; i < ManageContactsActivity.untrustedNumbers.size(); i++)
		{
			ManageContactsActivity.untrustedNumbers.get(i).clearPublicKey();
			MessageService.dba.updateKey(ManageContactsActivity.untrustedNumbers.get(i));
		}
		
		//TODO update to actually use proper key exchange (via sms)
		for(int i = 0; i < ManageContactsActivity.trustedNumbers.size(); i++)
		{
			ManageContactsActivity.trustedNumbers.get(i).setPublicKey();
			MessageService.dba.updateKey(ManageContactsActivity.untrustedNumbers.get(i));
		}
		//Start Key exchanges 1 by 1, using the user specified time out.
		//TODO implement
	}

}
