package com.tinfoil.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ForgeSMSSendActivity extends Activity {
	Button sendSMS;
	EditText phoneBox;
    EditText messageBox;
    TextView tv;

    /** Called when the activity is first created. */
   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        sendSMS = (Button) findViewById(R.id.send);
        phoneBox = (EditText) findViewById(R.id.phoneNum);
        messageBox = (EditText) findViewById(R.id.message);
        tv = new TextView(this);
        sendSMS.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v) 
			{
				Editable phoneNumber = phoneBox.getText();
		        Editable message = messageBox.getText();
		        String number =  phoneNumber.toString();
				String text = message.toString();
				if (number.length() > 0 && text.length() > 0)
				{

					sendSMS(number, text);
			        
					Toast.makeText(getBaseContext(), "Message sent", Toast.LENGTH_SHORT).show();
				}
				else
				{
					tv.setText("You have failed to provide sufficent information");
					setContentView(tv);
				}
				
			}
        });
			
    }
    
    public void sendSMS (String number, String message)
    {
    	/*Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName("com.android.mms","com.android.mms.ui.ConversationList"));
        startActivity(intent);*/
    	PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, ForgeSMSSendActivity.class), 0);
        SmsManager sms = SmsManager.getDefault();
        ContentValues values = new ContentValues();
        values.put("address", number);
        values.put("body", "DERP");
        getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
    }
}