package com.tinfoil.sms;

import android.content.Context;

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
		while(!signal)
		{
			//Do nothing, just wait for their to be signal
		}
		for (int i = 0; i < MessageService.dba.queueLength(); i++)
    	{
			MessageSender.success = 0;
			Queue messageInfo = MessageService.dba.getFirstInQueue();
    		SMSUtility.sendSMS(c, messageInfo.getNumber(), messageInfo.getMessage(), messageInfo.getId());
    		
    		while (!signal && MessageSender.success == 0){
    			//Do nothing, just wait for their to be signal
    		}
    		if (MessageSender.success == 1) 
    		{
    			i--;
    		}
    	}
		//handler.sendEmptyMessage(0);
	}

	/*private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
        	
        }
	};*/
}
