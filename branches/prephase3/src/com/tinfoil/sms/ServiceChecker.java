package com.tinfoil.sms;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

public class ServiceChecker implements Runnable{
	private Context c;
	//private ServiceState s = new ServiceState();
	
	public static boolean signal = false;
	public void startThread(Context c)
	{
		this.c = c;
		Thread thread = new Thread(this);
		thread.start();
	}	
	
	public void run() {
		
		/*
		 * TODO remove busy wait
		 */
		while(!signal)
		{
			//Do nothing, just wait for their to be signal
		}
		//long length = MessageService.dba.queueLength();
		//for (int i = 0; i < length; i++)
		
		/*
		 * Change to keep running while messages are in the queue. 
		 * TODO get queue working.
		 * TODO change message service to only send messages through queue
		 */
		Looper.prepare();
		
		//TODO Add semaphores to synchronously access the database for the message queue thread
		while(MessageService.dba.queueLength() > 0)
    	{
			/*
			 * TODO remove busy wait
			 */
			while(!signal)
			{
				//Do nothing, just wait for their to be signal
			}
			MessageSender.success = 0;
			Queue messageInfo = MessageService.dba.getFirstInQueue();
			
    		SMSUtility.sendSMS(c, messageInfo.getNumber(), messageInfo.getMessage(), messageInfo.getId());
    		
    		/*
    		 * TODO remove busy wait
    		 */
    		/*while (!signal || MessageSender.success == 0){
    			//Do nothing, just wait for their to be signal
    			Log.v("ServiceChecker", "" + MessageSender.success);
    		}*/
    		/*if (MessageSender.success == 1) 
    		{
    			i--;
    		}*/
    	}
	}
}
