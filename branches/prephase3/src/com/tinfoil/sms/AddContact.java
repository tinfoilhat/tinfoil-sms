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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * A class for adding a contact to the tinfoil-sms database
 *
 */
public class AddContact extends Activity {
	public static TrustedContact editTc;
	public static boolean addContact;
	private TrustedContact contactEdit;
	private ListView listView;
	private Button add;
	private EditText contactName;
	private Button addNumber;
	
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
        else
        {
        	contactEdit = new TrustedContact("");
        }
                
        update(null);
        
        addNumber.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				final EditText input = new EditText(getBaseContext());
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
								dialog.cancel();    // Canceled.
							}});
				AlertDialog alert = builder.create();
				alert.show();
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
									dialog.cancel();// Canceled.
								}});
					AlertDialog alert = builder.create();
					alert.show();
        		}
			}
		});
        
        add = (Button) findViewById(R.id.add);
        contactName = (EditText) findViewById(R.id.contact_name);
       
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
				else
				{
					contactEdit.setName(name);
				}
				
				if (!empty && contactEdit.getName().length() > 0 && !contactEdit.isNumbersEmpty())
				{
					if (addContact)
					{
						if (!Prephase3Activity.dba.inDatabase(contactEdit.getANumber()))
						{
							Prephase3Activity.dba.addRow(contactEdit);
							contactEdit = null;
					        editTc = null;
							finish();
						}
						else
						{
							AlertDialog.Builder builder = new AlertDialog.Builder(AddContact.this);
							builder.setMessage("Contact is already in the database")
							       .setCancelable(true)
							       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							        	   
							           }})
							        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										dialog.cancel(); // Canceled.
										}});
							AlertDialog alert = builder.create();
							alert.show();
						}
					}
					else
					{
						Prephase3Activity.dba.updateRow(contactEdit, contactEdit.getNumber(0));
						contactEdit = null;
				        editTc = null;
						finish();
					}
				}else
				{					
					AlertDialog.Builder builder = new AlertDialog.Builder(AddContact.this);
					builder.setMessage("Insufficient information provided")
					       .setCancelable(true)
					       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	   
					           }})
					        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.cancel();// Canceled.
								}});
					AlertDialog alert = builder.create();
					alert.show();
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
        	listView.setAdapter(new ContactAdapter(this, R.layout.add_number, contactEdit));
        }
        else
        {
        	contactEdit = new TrustedContact("");
    		listView.setAdapter(new ContactAdapter(this, R.layout.add_number, contactEdit));
    		
        }

        listView.setItemsCanFocus(false);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}
}
