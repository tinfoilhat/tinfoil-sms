package com.tinfoil.sms.sms;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.tinfoil.sms.database.DBAccessor;

public class ConversationLoader implements Runnable {
	
    private boolean loopRunner = true;
    private boolean start = true;
    private Context context;
	private Thread thread;
    private boolean update;
    private Handler handler;
    
    public void startThread(Context context, boolean update, Handler handler)
    {
    	this.context = context;
    	//this.msgList = msgList;
    	this.update = update;
    	this.handler = handler;
    	thread = new Thread(this);
		thread.start();
    }

    public void run() {
		while (loopRunner)
		{
			DBAccessor loader = new DBAccessor(context);
			ConversationView.msgList = loader.getConversations();
			if(!update) {
				handler.sendEmptyMessage(ConversationView.LOAD);
			}
			else
			{
				handler.sendEmptyMessage(ConversationView.UPDATE);
			}
			Log.v("Thread", "Just Messaged sent");
			while(loopRunner && start)
			{
				synchronized(this){
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			start = true;
		}
	}
    
    public synchronized void setStart(boolean start) {
		this.start = start;
		notifyAll();
	}
    
    public synchronized void setRunner(boolean runner) {
		this.loopRunner = runner;
		notifyAll();
	}
}