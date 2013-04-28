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

package com.tinfoil.sms.messageQueue;

import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.SMSUtility;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

public class MessageSender implements Runnable{

	private Context c;
	private boolean empty = true;
	//private String number;
	//private ConcurrentLinkedQueue<QueueEntry> message = new ConcurrentLinkedQueue<QueueEntry>();
	private Thread thread;
	private DBAccessor sender;
	private boolean signal = false;
	
	public void startThread(Context c) {
		this.c = c;
		this.sender = new DBAccessor(c);
		thread = new Thread(this);
		thread.start();
	}
	
	//@Override
	public void run() {
		
		Looper.prepare();
		
		/*
		 * Keep the thread running
		 */
		while(true)
		{
			QueueEntry mes = null;
			
			/*
			 * TODO change the queue to wait until the broadcast receiver
			 * notifies that the message has been sent or that the message.
			 */

			/*
			 * Get the next element in the queue. If there is no more elements
			 * wait until notified that there are more in the queue
			 */
			while(empty && mes == null)
			{
				mes = sender.getFirstInQueue();
				if(mes != null)
				{
					break;
				}
				
				synchronized(this){
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			/*
			 * Check that the signal has not changed to have no signal to send
			 * messages. If there is no service, wait till the service state
			 * changes to signal.
			 */
			while(!signal)
			{
				Log.v("Signal", "none");
				synchronized(this){
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			Log.v("Signal", "some");
			
			synchronized(this){
				empty = true;
			}
			
			/*
			 * Send the message 
			 */
			if(mes != null) {
				SMSUtility.sendMessage(this.sender, c, mes.getNumber(), mes.getMessage());
			}
		}		
	}
	
	/**
	 * Set whether the queue has been emptied and notifies all the threads to
	 * wake up. 
	 * @param setEmpty Whether the queue is empty or not.
	 */
	public void threadNotify(boolean setEmpty)
	{
		if(setEmpty)
		{
			empty = false;
		}
		synchronized (this) {
			notifyAll();
		}
	}

	/**
	 * Check whether the phone has signal.
	 * @return Whether the phone has signal or not.
	 */
	public synchronized boolean isSignal() {
		return signal;
	}

	/**
	 * Set whether the phone has signal. This should really only be used by the
	 * signalListener.
	 * @param signal Whether the phone has signal or not.
	 */
	public synchronized void setSignal(boolean signal) {
		this.signal = signal;
	}
}
	