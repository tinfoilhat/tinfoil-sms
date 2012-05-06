package com.tinfoil.sms;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.util.Log;
import android.os.IBinder;

public class ServiceController extends Service
{
   private Activity mainActivity;
   SMSMonitor sms;
   
   @Override
   public IBinder onBind(Intent intent) {
      // TODO Auto-generated method stub
      return null;
   }
   
   public void onCreate() 
   {
      super.onCreate();
      
      /**** Start Listen to Outgoing SMS ****/
      Log.e("KidSafe","###### ServiceController :: CallSMS Monitor method ######");
      sms = new SMSMonitor(this , mainActivity);
      sms.startSMSMonitoring();
   }
   
   @Override
     public void onDestroy() {
        super.onDestroy();

        /**** Stop Listen to Outgoing SMS ****/
        Log.e("KidSafe","###### ServiceController :: StopSMS Monitor method ######");
        sms.stopSMSMonitoring();
    }
}
