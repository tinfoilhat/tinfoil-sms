package com.example.SMS;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
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
					Toast.makeText(getBaseContext(), "Message sent", Toast.LENGTH_SHORT).show();
				}
				else
				{
					tv.setText("You have failed to provide sufficent information");
				}
				setContentView(tv);
			}
        });
			
    }
}