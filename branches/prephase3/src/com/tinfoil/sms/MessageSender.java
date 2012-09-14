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
	//private ServiceChecker sc = new ServiceChecker();
	
	
	@Override
	public void onReceive(Context c, Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle != null && bundle.getString(SMSUtility.NUMBER) != null 
				&& bundle.getString(SMSUtility.MESSAGE) != null)
        {
			int result = getResultCode();
			long id = bundle.getLong(SMSUtility.ID);
			
			if (result == SmsManager.RESULT_ERROR_NO_SERVICE || result == SmsManager.RESULT_ERROR_RADIO_OFF)
			{
				if(id == 0){
					/*Toast.makeText(c, "SMS put in queue to send", Toast.LENGTH_SHORT).show();
					MessageService.dba.addMessageToQueue(bundle.getString(SMSUtility.NUMBER), 
	                		bundle.getString(SMSUtility.MESSAGE));
	                
	            	intent.removeExtra(SMSUtility.NUMBER);
	            	intent.removeExtra(SMSUtility.MESSAGE);*/
					
					//Temporary fix for no signal problem
					Toast.makeText(c, "No signal", Toast.LENGTH_SHORT).show();
	            	
	            	//Start the Thread to start checking for messages
	            	
	            	//sc.startThread();
				}
				else{
					Toast.makeText(c, "SMS still in queue", Toast.LENGTH_SHORT).show();
					//Found service but then lost it. Don't do anything just go back to waiting.
				}
				
			}
			else if (result == Activity.RESULT_OK)
			{
				if (id > 0)
				{
					Toast.makeText(c, "Queue message sent", Toast.LENGTH_SHORT).show();
					MessageService.dba.deleteQueueEntry(id);
				}
				//Should make confirmation toast here that the message has been sent.
				//Toast.makeText(c, "Message Sent", Toast.LENGTH_SHORT).show();
			}
			/*switch (getResultCode())
	        {
	            case Activity.RESULT_OK:
	                Toast.makeText(c, "SMS sent", Toast.LENGTH_SHORT).show();
	                //For testing purposes
	                //Toast.makeText(c, "here", Toast.LENGTH_SHORT).show();
	                MessageService.dba.addMessageToQueue(bundle.getString(SMSUtility.NUMBER), 
	                	bundle.getString(SMSUtility.MESSAGE));
	                	
	                intent.removeExtra(SMSUtility.NUMBER);
	                intent.removeExtra(SMSUtility.MESSAGE);
	                
	                break;
	            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	                Toast.makeText(c, "Generic failure", Toast.LENGTH_SHORT).show();
	                break;
	            case SmsManager.RESULT_ERROR_NO_SERVICE:
	            	//Move Message to the Queue
            	   
                	MessageService.dba.addMessageToQueue(bundle.getString(SMSUtility.NUMBER), 
                		bundle.getString(SMSUtility.MESSAGE));
                	
                	intent.removeExtra(SMSUtility.NUMBER);
                	intent.removeExtra(SMSUtility.MESSAGE);
                    Toast.makeText(c, "No service", Toast.LENGTH_SHORT).show();
	                break;
	            case SmsManager.RESULT_ERROR_NULL_PDU:
	                Toast.makeText(c, "Null PDU", Toast.LENGTH_SHORT).show();
	                break;
	            case SmsManager.RESULT_ERROR_RADIO_OFF:
	            	//Move Message to the Queue
	            	
                	MessageService.dba.addMessageToQueue(bundle.getString(SMSUtility.NUMBER), 
                		bundle.getString(SMSUtility.MESSAGE));
                	
                	intent.removeExtra(SMSUtility.NUMBER);
                	intent.removeExtra(SMSUtility.MESSAGE);
	                
	                Toast.makeText(c, "Radio off", Toast.LENGTH_SHORT).show();
	                break;
	        }*/
		}
    }
}
