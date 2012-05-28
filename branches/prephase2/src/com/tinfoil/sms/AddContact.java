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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
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
	public static boolean addContact;
	private TrustedContact contactEdit;
	Button add;
	EditText contactName;
	//EditText contactNumber;
	Button addNumber;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact);
        
        
        
        //Sets the keyboard to not pop-up until a text area is selected 
      	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
      	
        //contactNumber = new EditText(this);
        listView = (ListView)findViewById(R.id.contact_numbers);
        addNumber = (Button) findViewById(R.id.add_new_number);
        if (!addContact)
        {
        	contactEdit = editTc;
       	}
        
        editTc = null;
        
        update(null);
        
        addNumber.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				final EditText input = new EditText(getBaseContext());
				//android:inputType="phone"
				input.setInputType(InputType.TYPE_CLASS_PHONE);
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
				/*contactEdit.setPrimaryNumber(contactEdit.getNumber(position));
				update(null);*/
				return true;
			}
        	
        });
        
        listView.setOnItemClickListener(new OnItemClickListener()
		{
        	public void onItemClick(AdapterView<?> parent, View view,
        			final int position, long id) {
        		if (contactEdit != null)
        		{
        			final EditText input = new EditText(getBaseContext());
					input.setText(contactEdit.getNumber(position));
					input.setInputType(InputType.TYPE_CLASS_PHONE);
					
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
			}
		});
        
        add = (Button) findViewById(R.id.add);
        contactName = (EditText) findViewById(R.id.contact_name); 
        //contactNumber = (EditText) findViewById(R.id.contact_number);
        
        if (contactEdit != null)
        {
        	if (contactEdit.getName() != null)
        	{
        		contactName.setText(contactEdit.getName());
        	}
        }
        
        add.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				String name = contactName.getText().toString();
				boolean empty = false;
				if (name == null)
				{
					if (!contactEdit.isNumbersEmpty())
					{
						name = contactEdit.getANumber();
					}
					else
					{
						empty = true;
					}
				}
				{
					contactEdit.setName(name);
				}

				
				if (!empty && contactEdit.getName().length() > 0 && !contactEdit.isNumbersEmpty())
				{
					//Need to add to android contact's database, and check to see if it isnt already there
					//Need to figure a way to update when it is a contact that is being updated and to 
					//add when it is a contact that is being added
					
					if (addContact)
					{
						//if (!Prephase2Activity.dba.inDatabase(contactEdit.getPrimaryNumber()))
						if (!Prephase2Activity.dba.inDatabase(contactEdit.getANumber()))
						{
							//Prephase2Activity.dba.addRow(name, ContactRetriever.format(number), null, 0);
							
							Prephase2Activity.dba.addRow(contactEdit);
							contactEdit = null;
							finish();
							//contactNumber.setText("");
							//contactName.setText("");
						}
						else
						{
							
							//**Note need an alert message here
							Toast.makeText(getBaseContext(), "A contact already has that number", Toast.LENGTH_SHORT).show();
						}
					}
					else
					{
						Prephase2Activity.dba.updateRow(contactEdit, contactEdit.getNumber(0));
						Toast.makeText(getBaseContext(), "Contact Added", Toast.LENGTH_SHORT).show();
						contactEdit = null;
						finish();
					}
					
					//if (!Prephase2Activity.dba.inDatabase(contactEdit.getPrimaryNumber()))
					//{
						//Prephase2Activity.dba.addRow(name, ContactRetriever.format(number), null, 0);
						//Prephase2Activity.dba.addRow(contactEdit);
						
						//contactNumber.setText("");
						//contactName.setText("");
					//}
					//else
					//{
						
						//**Note need an alert message here
					//	Toast.makeText(getBaseContext(), "A contact already has that number", Toast.LENGTH_SHORT).show();
					//}
				}else
				{					
					//**Note need an alert message here
					Toast.makeText(getBaseContext(), "Insufficient information provided", Toast.LENGTH_SHORT).show();
				}
			}
		});       
        
	}
	
	public void update(String newNumber)
	{
		if (newNumber != null)
		{
			contactEdit.addNumber(newNumber);
		}
		
		if (contactEdit != null)
        {
        	//populates listview with the declared strings, an option is also given for it to be multiple choice (check boxes), or single list (radio buttons) 
	        //listView.setAdapter(new ContactAdapter(this, R.layout.add_number, contactEdit.getNumber()));
			listView.setAdapter(new ContactAdapter(this, R.layout.add_number, contactEdit));
        }
        else
        {
        	//ArrayList<String> numbers = new ArrayList<String>();
    		contactEdit = new TrustedContact("");
    			listView.setAdapter(new ContactAdapter(this, R.layout.add_number, 
            			contactEdit));
    		
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
