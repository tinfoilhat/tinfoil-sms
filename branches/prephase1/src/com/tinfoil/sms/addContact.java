package com.tinfoil.sms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class addContact extends Activity {
	Button add;
	EditText contactName;
	EditText contactNumber; 
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addcontact);
        
        
        add = (Button) findViewById(R.id.add);
        contactName = (EditText) findViewById(R.id.contact_name);
        contactNumber = (EditText) findViewById(R.id.contact_number);

        
        add.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				String name = contactName.getText().toString();
				String number = contactNumber.getText().toString();
				
				if (name.length() > 0 && number.length() > 0)
				{
					Prephase1Activity.dba.addRow(name, number, null, 0);
					Toast.makeText(getBaseContext(), "Contact Added", Toast.LENGTH_SHORT).show();
									
					contactNumber.setText("");
					contactName.setText("");
				}
			}
		});       
        
	}
	
}
