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
		while(true)
		{
			QueueEntry mes = null;
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
			
			if(mes != null) {
				SMSUtility.sendMessage(this.sender, c, mes.getNumber(), mes.getMessage());
			}
		}		
	}
	
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

	public synchronized boolean isSignal() {
		return signal;
	}

	public synchronized void setSignal(boolean signal) {
		this.signal = signal;
	}
}
	