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

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * A class for adding a contact to the tinfoil-sms database
 *
 */
public class AddContact extends Activity {
	private ListView listView;
	public static TrustedContact editTc;
	private TrustedContact contactEdit;
	Button add;
	EditText contactName;
	EditText contactNumber;
	Button addNumber;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact);
        
        //contactNumber = new EditText(this);
        listView = (ListView)findViewById(R.id.contact_numbers);
        addNumber = (Button) findViewById(R.id.add_new_number);
        
        contactEdit = editTc;
        editTc = null;
        
        update(null);
        
        addNumber.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				final EditText input = new EditText(getBaseContext());
				AlertDialog.Builder builder = new AlertDialog.Builder(AddContact.this);
				builder.setMessage("Enter the new number:")
				       .setCancelable(true)
				       .setView(input)
				       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   String value = input.getText().toString();
				        	   update(value);
				        	   input.setText("");
				           }})
				        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
									    // Canceled.
							}});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
        
        /*listView.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				
				return false;
			}
		});*/
        
        listView.setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> parent, View view,
        			int position, long id) {
				contactEdit.setPrimaryNumber(contactEdit.getNumber(position));
				Toast.makeText(getBaseContext(), contactEdit.getPrimaryNumber() + " Set to Primary", Toast.LENGTH_SHORT).show();
				update(null);
				return true;
			}
        	
        });
        
        listView.setOnItemClickListener(new OnItemClickListener()
		{
        	public void onItemClick(AdapterView<?> parent, View view,
        			final int position, long id) {
        		
				final EditText input = new EditText(getBaseContext());
				input.setText(contactEdit.getNumber(position));
				
				AlertDialog.Builder builder = new AlertDialog.Builder(AddContact.this);
				builder.setMessage("Edit:")
				       .setCancelable(true)
				       .setView(input)
				       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   contactEdit.setNumber(position, input.getText().toString());
				        	   update(null);
				        	   input.setText("");
				           }})
				        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
									    // Canceled.
							}});
				AlertDialog alert = builder.create();
				alert.show();						
			}
		});
        
        add = (Button) findViewById(R.id.add);
        contactName = (EditText) findViewById(R.id.contact_name); 
        contactNumber = (EditText) findViewById(R.id.contact_number);
        
        if (contactEdit != null)
        {
        	if (contactEdit.getName() != null)
        	{
        		contactName.setText(contactEdit.getName());
        	}
        }
        
        if (SendMessageActivity.newNumber != null)
        {
        	contactNumber.setText(SendMessageActivity.newNumber);
        	SendMessageActivity.newNumber = "";
        }
        
        
        add.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				String name = contactName.getText().toString();
				String number = contactNumber.getText().toString();
				
				if (name.length() > 0 && number.length() > 0)
				{
					//Need to add to android contact's database, and check to see if it isnt already there
					if (!Prephase2Activity.dba.conflict(number))
					{
						Prephase2Activity.dba.addRow(name, ContactRetriever.format(number), null, 0);
						Toast.makeText(getBaseContext(), "Contact Added", Toast.LENGTH_SHORT).show();
						
						contactNumber.setText("");
						contactName.setText("");
					}
					else
					{
						
						//**Note need an alert message here
						Toast.makeText(getBaseContext(), "A contact already has that number", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});       
        
	}
	
	public void update(String newKey)
	{
		if (newKey != null)
		{
			contactEdit.addNumber(newKey);
		}
		
		if (contactEdit != null)
        {
        	//populates listview with the declared strings, an option is also given for it to be multiple choice (check boxes), or single list (radio buttons) 
	        //listView.setAdapter(new ContactAdapter(this, R.layout.add_number, contactEdit.getNumber()));
			listView.setAdapter(new ContactAdapter(this, R.layout.add_number, contactEdit));
        }
        else
        {
        	ArrayList<String> numbers = new ArrayList<String>();
    		numbers.add("");
    			listView.setAdapter(new ContactAdapter(this, R.layout.add_number, 
            			new TrustedContact("", 0, numbers)));
        }

        //Not setting focus on a particular list item, (focus is then left to default at the top of the page)
        listView.setItemsCanFocus(false);

        //Set the mode to single or multiple choice, (should match top choice)
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}
	/*
	 * public class AddContact extends Activity {
		Button add;
		EditText contactName;
		EditText contactNumber; 
		
		public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.addcontact);
	                
	        add = (Button) findViewById(R.id.add);
	        contactName = (EditText) findViewById(R.id.contact_name);
	        contactNumber = (EditText) findViewById(R.id.contact_number);
	        contactNumber.setText(SendMessageActivity.newNumber);
	        SendMessageActivity.newNumber = "";
	        
	        add.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					String name = contactName.getText().toString();
					String number = contactNumber.getText().toString();
					
					if (name.length() > 0 && number.length() > 0)
					{
						//Need to add to android contact's database, and check to see if it isnt already there
						if (!Prephase2Activity.dba.conflict(number))
						{
							Prephase2Activity.dba.addRow(name, ContactRetriever.format(number), null, 0);
							Toast.makeText(getBaseContext(), "Contact Added", Toast.LENGTH_SHORT).show();
							
							contactNumber.setText("");
							contactName.setText("");
						}
						else
						{
							
							//**Note need an alert message here
							Toast.makeText(getBaseContext(), "A contact already has that number", Toast.LENGTH_SHORT).show();
						}
					}
				}
			});       
	        
		}
		
	}
	*/

}
