package com.tinfoil.sms;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class MessageService extends Service {
	
	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}
	
	 @Override
     public void onCreate() {
		 Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		 //super.onCreate();
     }

     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
    	 Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
   	 
    	 return Service.START_STICKY;
     }
}
