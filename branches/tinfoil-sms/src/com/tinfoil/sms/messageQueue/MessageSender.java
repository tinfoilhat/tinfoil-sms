package com.tinfoil.sms.messageQueue;

import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.SMSUtility;

import android.content.Context;
import android.os.Looper;

public class MessageSender implements Runnable{

	private Context c;
	private boolean empty = true;
	//private String number;
	//private ConcurrentLinkedQueue<QueueEntry> message = new ConcurrentLinkedQueue<QueueEntry>();
	private Thread thread;
	private DBAccessor sender;
	
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
			
			synchronized(this){
				empty = true;
			}
			
			if(mes != null) {
				SMSUtility.sendMessage(this.sender, c, mes.getNumber(), mes.getMessage());
			}
		}
		
	}
	
	public void threadNotify()
	{
		empty = false;
		synchronized (this) {
			notifyAll();
		}
	}
}
	