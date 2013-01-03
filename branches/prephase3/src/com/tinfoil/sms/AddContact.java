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
import android.widget.AdapterView.OnItemClickListener;

/**
 * TODO add the ability to edit shared informations
 * TODO allow user to initiate key exchange
 * TODO allow user to edit book paths
 * TODO add an option to delete numbers
 * This Activity is used for adding and editing contacts. The activity is able to identify which one it 
 * is doing by the information provided to the activity. If the variable addContact == false then a 
 * previously created/imported contact is being edited. Thus editTc != null and will have the contact's 
 * information.If addContact == true and editTc == null then a new contact is being added with no 
 * previously known information. Finally, if addContact == true and editTc != null then a new contact
 * is being added but information is already know about that contact.
 * 
 * ManageContactsActivity will start with either: addContact == true and editTc == null
 * or addContact == false and editTc != null
 * SendMessageActivity will start AddContact with: addContact == true and editTc != null
 * 
 * Once the activity has started if need to contactEdit = editTc. 
 */
public class AddContact extends Activity {
	public static TrustedContact editTc;
	public static boolean addContact;
	private String originalNumber;
	private TrustedContact contactEdit;
	private ListView listView;
	private Button add;
	private EditText contactName;
	private Button addNumber;
	private static AlertDialog alert;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact);

        //Sets the keyboard to not pop-up until a text area is selected 
      	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
      	
        //contactNumber = new EditText(this);
        
      	listView = (ListView)findViewById(R.id.contact_numbers);
        addNumber = (Button) findViewById(R.id.add_new_number);
        
        /*
         * Check if a user is editing a contact or creating a new contact.
         */
        if (!addContact || editTc != null)
        {
        	contactEdit = editTc;
        	originalNumber = contactEdit.getANumber();
       	}
        else
        {
        	contactEdit = new TrustedContact("");
        }
        
        /*
         * Populates the list of numbers
         */
        update(null);
        
        /*
         * Add a dialog for when a user clicks on the add new number button.
         * A message dialog pops-up with an input section allowing the user to
         * enter the new number. 
         */
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
				        	   if(value != "" && value.length() > 0)
				        	   {
				        		   update(value);
				        		   input.setText("");
				        	   }
				        	   //TODO alert of Invalid Number
				           }})
				        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
								dialog.cancel();    // Canceled.
							}});
				alert = builder.create();
				alert.show();
			}
				
		});
        
        /*
         * When a user clicks on a number for a longer period of time a dialog is started
         * to determine what type of number the number is (mobile, home, ...)
         * 
         * ***Please note: Since this is a task within itself the onclickListener is silenced
         * after fulfilling the declared method.
         * 
         * ***Please note: This does not have a impact on the program. If a user sets the
         * number to pager or anything else and then tries to send a message to the number
         * it will still send.
         */
        listView.setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3) {
				AlertDialog.Builder builder = new AlertDialog.Builder(AddContact.this);
				builder.setTitle("Phone Type:");
				builder.setSingleChoiceItems(DBAccessor.TYPES, contactEdit.getNumber()
						.get(position).getType(), new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	contactEdit.getNumber().get(position).setType(item);
				    	update(null);
				    }
				});
				alert = builder.create();
				if (alert != null)
					alert.show();
				
				return true;
			}
		});
        
        /*
         * When a use clicks on a number in from the list they a dialog will pop-up
         * allowing them to edit the number. Any changes will be discarded if the
         * user cancels the dialog. 
         */
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
					alert = builder.create();
					alert.show();
        		}
			}
		});
        
        add = (Button) findViewById(R.id.add);
        contactName = (EditText) findViewById(R.id.contact_name);
        
        /*
         * Check if a user is editing a contact and if that contact has a name
         */
        if (contactEdit != null && contactEdit.getName() != null)
        {
        		contactName.setText(contactEdit.getName());
        	
        }
        
        /*
         * Add/Save the user to the database and exit the activity.
         */
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
						if (!MessageService.dba.inDatabase(contactEdit.getANumber()))
						{
							MessageService.dba.addRow(contactEdit);
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
							alert = builder.create();
							alert.show();
						}
					}
					else
					{
						MessageService.dba.updateRow(contactEdit, originalNumber);
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
					alert = builder.create();
					alert.show();
				}
			}
		}); 
  	}
	
	/**
	 * Update the list of numbers shown.
	 * @param newNumber : String a new number to be added to the list, if null no new number is added
	 */
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
