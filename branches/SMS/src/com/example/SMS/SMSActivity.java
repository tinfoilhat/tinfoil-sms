package com.example.SMS;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SMSActivity extends Activity {
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
					//tv.setText("Message sent");
					//Toast.makeText(getBaseContext(), "Message sending", Toast.LENGTH_SHORT).show();
					// Encrypt the text message using ROT13 before sending it
					sendSMS(number, Cipher.rot13(text));
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
    	PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, SMSActivity.class), 0);
        SmsManager sms = SmsManager.getDefault();
        // this is the function that does all the magic
        sms.sendTextMessage(number, null, message, pi, null);
    	//Intent nint = new Intent(this, SMSActivity.class);
    	//PendingIntent pInt = PendingIntent.getActivity(this, 0, new Intent(this, SMSActivity.class), 0);
    	//SmsManager middleMan = SmsManager.getDefault();
    	//middleMan.sendTextMessage(number, null, message, pInt, null);
    	
    	//middleMan.sendTextMessage(number, null, message, pInt, null); 	//This line is making the program fail, 
    																	//most likely because the last 2 parameters
    }
}
