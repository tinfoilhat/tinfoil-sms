/** 
 * Copyright (C) 2013 Jonathan Gillett, Joseph Heron
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

package com.tinfoil.sms.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.tinfoil.sms.R;
import com.tinfoil.sms.adapter.ContactAdapter;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.MessageService;

/**
 * Add or Edit contacts of the user.
 */
public class AddContact extends Activity {

	public static final String EDIT_NUMBER = "edit_number";
	public static final int REQUEST_CODE = 1;
	public static final String POSITION = "position";
	public static final int NEW_NUMBER_CODE = -1;
	public static final int UPDATED_NUMBER = 2;
	public static final int DELETED_NUMBER = 3;
	
    public static TrustedContact editTc;
    public static boolean addContact;
    private String originalNumber;
    private TrustedContact contactEdit;
    private ListView listView;
    private EditText contactName;
    private static AlertDialog alert;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.add_contact);

        //Sets the keyboard to not pop-up until a text area is selected 
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        this.listView = (ListView) this.findViewById(R.id.contact_numbers);
        //this.addNumber = (Button) this.findViewById(R.id.add_new_number);

        /*
         * Check if a user is editing a contact or creating a new contact.
         */
        if (!addContact && editTc != null)
        {
            this.contactEdit = editTc;
            this.originalNumber = this.contactEdit.getANumber();
        }
        else
        {
            this.contactEdit = new TrustedContact("");
        }

        /*
         * Populates the list of numbers
         */
        this.update(null);
        
        /*
         * When a user clicks on a number for a longer period of time a dialog is started
         * to determine what type of number the number is (mobile, home, ...)
         * 
         * ***Please note: This does not have a impact on the program. If a user sets the
         * number to pager or anything else and then tries to send a message to the number
         * it will still send.
         */
        this.listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    final int position, long arg3) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddContact.this);
                builder.setTitle("Phone Type:");
                builder.setSingleChoiceItems(DBAccessor.TYPES, AddContact.this.contactEdit.getNumber()
                        .get(position).getType(), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int item) {
                        AddContact.this.contactEdit.getNumber().get(position).setType(item);
                        AddContact.this.update(null);
                    }
                });
                alert = builder.create();
                if (alert != null) {
                    alert.show();
                }
                return true;
            }
        });

        /*
         * When a use clicks on a number in from the list they a dialog will pop-up
         * allowing them to edit the number. Any changes will be discarded if the
         * user cancels the dialog. 
         */
        this.listView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if (AddContact.this.contactEdit != null)
                {
                	Intent intent = new Intent(AddContact.this, EditNumber.class);
                	intent.putExtra(AddContact.EDIT_NUMBER, AddContact.this.contactEdit.getNumber(position));
                	intent.putExtra(AddContact.POSITION, position);
                	AddContact.this.startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });

        //this.add = (Button) this.findViewById(R.id.add);
        this.contactName = (EditText) this.findViewById(R.id.contact_name);

        /*
         * Check if a user is editing a contact and if that contact has a name
         */
        if (this.contactEdit != null && this.contactEdit.getName() != null)
        {
            this.contactName.setText(this.contactEdit.getName());
        }
    }
    
    /**
     * The onClick action for when the add new number button is clicked.
     * @param view The view that is involved
     */
    public void addNewNumber(View view)
    {
    	Intent intent = new Intent(AddContact.this, EditNumber.class);
    	
    	if(!contactEdit.isNumbersEmpty())
    	{
    		intent.putExtra(AddContact.EDIT_NUMBER, contactEdit.getANumber());
    		intent.putExtra(AddContact.POSITION, -1);
    	}
    	AddContact.this.startActivityForResult(intent, REQUEST_CODE);
    }
    
    /**
     * The onClick action for when the user clicks on save information
     * @param view The view that is involved
     */
    public void saveInformation(View view)
    {
    	//Could add to Thread
    	String name = AddContact.this.contactName.getText().toString();
        boolean empty = false;
        
        if (name == null)
        {
            if (!AddContact.this.contactEdit.isNumbersEmpty())
            {
                name = AddContact.this.contactEdit.getANumber();
            }
            else
            {
                empty = true;
            }
        }
        else
        {
            AddContact.this.contactEdit.setName(name);
        }

        if (!empty && AddContact.this.contactEdit.getName().length() > 0 && !AddContact.this.contactEdit.isNumbersEmpty())
        {
            if (addContact)
            {
            	MessageService.dba.updateContactInfo(AddContact.this.contactEdit, contactEdit.getANumber());
            }
            else
            {
                MessageService.dba.updateContactInfo(AddContact.this.contactEdit, AddContact.this.originalNumber);
            }
            
            if (AddContact.this.originalNumber != null && AddContact.this.contactEdit.getNumber(AddContact.this.originalNumber) == null)
            {
            	AddContact.this.contactEdit = null;
                editTc = null;
                AddContact.this.setResult(AddContact.DELETED_NUMBER);
                AddContact.this.finish();
            }
            else
            {
            	AddContact.this.contactEdit = null;
                editTc = null;
                AddContact.this.setResult(AddContact.UPDATED_NUMBER);
                AddContact.this.finish();
            }
            
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddContact.this);
            builder.setMessage("Insufficient information provided")
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int whichButton) {
                            dialog.cancel();// Canceled.
                        }
                    });
            alert = builder.create();
            alert.show();
        }
    }

    /**
     * Update the list of numbers shown.
     * 
     * @param newNumber String a new number to be added to the list,
     * if null no new number is added
     */
    public void update(String newNumber)
    {
        if (newNumber != null)
        {
            this.contactEdit.addNumber(newNumber);
        }

        if (this.contactEdit != null)
        {
            this.listView.setAdapter(new ContactAdapter(this, R.layout.add_number, this.contactEdit));
        }
        else
        {
            this.contactEdit = new TrustedContact("");
            
            this.listView.setAdapter(new ContactAdapter(this, R.layout.add_number, this.contactEdit));
        }

        this.listView.setItemsCanFocus(false);

        this.listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	super.onActivityResult(requestCode, resultCode, data);
    	
        /*
         * Get the result data
         */
    	boolean update = false;
    	String number = null;
    	int position = 0;
    	boolean addNumber = false;
    	
    	if(requestCode == resultCode && resultCode == AddContact.REQUEST_CODE)
    	{
    		update = data.getBooleanExtra(EditNumber.UPDATE, true);
    		number = data.getStringExtra(EditNumber.NUMBER);
    		position = data.getIntExtra(AddContact.POSITION, 0);
    		addNumber = data.getBooleanExtra(EditNumber.ADD, true);
    		
    		/*
    		 * Add the new number to the list of numbers
    		 */
	    	if(update && number != null)
	    	{    	
	    		if(addNumber)
	    		{
	    			if(position == AddContact.NEW_NUMBER_CODE)
	    			{
	    				if(contactEdit.getNumbers().size() <= 1)
	    				{
	    					if(contactEdit.getName() != "")
	    					{
	    						this.contactName.setText(number);
	    						contactEdit.setName(number);
	    					}
	    					else if (contactName.getText().toString() != "")
	    					{
	    						contactEdit.setName(contactName.getText().toString());
	    					}
	    					contactEdit.setNumber(number);
	    					MessageService.dba.updateContactInfo(contactEdit, number);
	    					update(null);
	    				}
	    				else
	    				{
	    					update(number);
	    				}
	    			}
	    			else
	    			{
	    				contactEdit.setNumber(position, number);
	    				update(null);
	    			}
	    		}
	    	}
	    	else if (update)
	    	{
	    		/*
	    		 * Remove the number or the contact from the list
	    		 */
	    		boolean isDeleted = data.getBooleanExtra(EditNumber.IS_DELETED, false);
	    		
	    		String temp = data.getStringExtra(EditNumber.DELETE);    		
	    		
	    		if (!isDeleted)
	    		{
		    		contactEdit.deleteNumber(temp);
		    		AddContact.this.setResult(AddContact.DELETED_NUMBER);
		    		update(null);
	    		}
	    		else
	    		{
	    			MessageService.dba.removeRow(temp);
	    			AddContact.this.setResult(AddContact.DELETED_NUMBER);
	    			finish();
	    		}
	    	}
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.add_contact_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_contact: {
            
            	MessageService.dba.removeRow(contactEdit.getANumber());
            	
            	finish();
            	
            	return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    
}
