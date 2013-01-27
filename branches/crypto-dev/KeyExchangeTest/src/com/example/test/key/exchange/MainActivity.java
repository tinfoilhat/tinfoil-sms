package com.example.test.key.exchange;

import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button key_exchange;
	private static final SmsManager sms = SmsManager.getDefault();
	
	private static final String NUMBER = "5555215556";
	private static  String message = "blah";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        message = "blah";
        
        key_exchange = (Button)findViewById(R.id.exchange_button);
        
        key_exchange.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				
				sendSMS(getBaseContext(), NUMBER, message);
			}
        	
        });

    }
    
    /**
     * Sends the given message to the phone with the given number
     * 
     * @param number The number of the phone that the message is sent to
     * @param message The message, encrypted that will be sent to the
     *            contact
     */
    public static void sendSMS(final Context c, final String number, final String message)
    {
        final String SENT = "SMS_SENT";

        final Intent intent = new Intent(SENT);
        final PendingIntent sentPI = PendingIntent.getBroadcast(c, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);


        //TODO use deliver intent
        sms.sendTextMessage(number, null, message, sentPI, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
