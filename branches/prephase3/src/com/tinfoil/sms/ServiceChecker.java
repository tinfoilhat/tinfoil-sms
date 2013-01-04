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
				//Do nothing, just waWit for their to be signal
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
