/** 
 * Copyright (C) 2011 Tinfoilhat
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.tinfoil.sms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddContact extends Activity {
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
					//Need to add to android contact's database, and check to see if it isnt already there
					if (!Prephase2Activity.dba.conflict(number))
					{
						Prephase2Activity.dba.addRow(name, number, null, 0);
						//String message = Prephase1Activity.dba.addRow(name, number, null, 0);
						//Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
						
						
						Toast.makeText(getBaseContext(), "Contact Added", Toast.LENGTH_SHORT).show();
						contactNumber.setText("");
						contactName.setText("");
					}
					else
					{
						Toast.makeText(getBaseContext(), "A contact already has that number", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});       
        
	}
	
}
