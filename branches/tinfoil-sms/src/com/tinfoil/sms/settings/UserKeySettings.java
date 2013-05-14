package com.tinfoil.sms.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.tinfoil.sms.R;
import com.tinfoil.sms.utility.SMSUtility;

public class UserKeySettings extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_key_settings);
		
		TextView keyView = (TextView)findViewById(R.id.public_key);
		
		keyView.setText(new String(SMSUtility.user.getPublicKey()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_key_settings, menu);
		return true;
	}
	
	public void exportKey(View view)
	{
		//TODO create export action
	}

}
