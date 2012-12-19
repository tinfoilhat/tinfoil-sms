package com.tinfoil.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;


public class MessageSender extends BroadcastReceiver{
	public static ServiceChecker sc = new ServiceChecker();
	public static byte success = 0;
	
	@Override
	public void onReceive(Context c, Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle != null && bundle.getString(SMSUtility.NUMBER) != null 
				&& bundle.getString(SMSUtility.MESSAGE) != null)
        {
			int result = getResultCode();
			long id = bundle.getLong(SMSUtility.ID);
			
			Toast.makeText(c, "Unable to send message", Toast.LENGTH_SHORT).show();
			
			/*
			 * Currently this only works for when there is one message in the queue.
			 * ***Note changes have been made but not tested
			 * 
			 * The problem:
			 * ------------
			 * *Please note that tests have consisted only with one contact being messaged multiple times
			 * let n be the number of messages in the queue of a single contact,
			 * n - 1 of the messages would be sent n times to the contact 
			 * current fix attempt: 
			 * retrieve the length of the queue once, before the loop,
			 * *expected results, n messages will be sent, (unknown if it will be n times)
			 * 
			 * ***CURRENTLY NOT SUPPORTED***
			 */
			/*if (result == SmsManager.RESULT_ERROR_NO_SERVICE || result == SmsManager.RESULT_ERROR_RADIO_OFF)
			{
				if(id == 0){
					Toast.makeText(c, "SMS put in queue to send", Toast.LENGTH_SHORT).show();
					MessageService.dba.addMessageToQueue(bundle.getString(SMSUtility.NUMBER), 
	                		bundle.getString(SMSUtility.MESSAGE));
	                
	            	intent.removeExtra(SMSUtility.NUMBER);
	            	intent.removeExtra(SMSUtility.MESSAGE);
					
					//**Temporary fix for no signal problem
					Toast.makeText(c, "No signal", Toast.LENGTH_SHORT).show();
	            	
	            	//Start the Thread to start checking for messages
	            	sc.startThread(c);

				}
				else{
					Toast.makeText(c, "SMS still in queue", Toast.LENGTH_SHORT).show();
					
					success = 1;
				}
				
			}
			else if (result == Activity.RESULT_OK)
			{
				if (id > 0)
				{
					
					Toast.makeText(c, "Queue message sent", Toast.LENGTH_SHORT).show();
					MessageService.dba.deleteQueueEntry(id);
					success = 2;
				}
				//Should make confirmation toast here that the message has been sent.
				//Toast.makeText(c, "Message Sent", Toast.LENGTH_SHORT).show();
			}*/	
		}
    }
}
