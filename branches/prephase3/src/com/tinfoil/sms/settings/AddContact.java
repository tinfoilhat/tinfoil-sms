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

package com.tinfoil.sms.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tinfoil.sms.R;
import com.tinfoil.sms.adapter.ContactAdapter;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.MessageService;

/**
 * TODO add an option to delete numbers This Activity is used for adding and
 * editing contacts. The activity is able to identify which one it is doing
 * by the information provided to the activity. If the variable addContact ==
 * false then a previously created/imported contact is being edited. Thus editTc
 * != null and will have the contact's information.If addContact == true and
 * editTc == null then a new contact is being added with no previously known
 * information. Finally, if addContact == true and editTc != null then a new
 * contact is being added but information is already know about that contact.
 * ManageContactsActivity will start with either: addContact == true and editTc
 * == null or addContact == false and editTc != null SendMessageActivity will
 * start AddContact with: addContact == true and editTc != null Once the
 * activity has started if need to contactEdit = editTc.
 */
public class AddContact extends Activity {
	
	public static final String EDIT_NUMBER = "edit_number";
	public static final int REQUEST_CODE = 1;
	public static final String POSITION = "position";
	
    public static TrustedContact editTc;
    public static boolean addContact;
    private String originalNumber;
    private TrustedContact contactEdit;
    private ListView listView;
    private Button add;
    private EditText contactName;
    private Button addNumber;
    private static AlertDialog alert;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.add_contact);

        //Sets the keyboard to not pop-up until a text area is selected 
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //contactNumber = new EditText(this);

        this.listView = (ListView) this.findViewById(R.id.contact_numbers);
        this.addNumber = (Button) this.findViewById(R.id.add_new_number);

        /*
         * Check if a user is editing a contact or creating a new contact.
         */
        if (!addContact || editTc != null)
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
         * Add a dialog for when a user clicks on the add new number button.
         * A message dialog pops-up with an input section allowing the user to
         * enter the new number. 
         */
        this.addNumber.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                final EditText input = new EditText(AddContact.this.getBaseContext());
                input.setInputType(InputType.TYPE_CLASS_PHONE);
                AlertDialog.Builder builder = new AlertDialog.Builder(AddContact.this);
                builder.setMessage("Enter the new number:")
                        .setCancelable(true)
                        .setView(input)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                final String value = input.getText().toString();
                                if (value != "" && value.length() > 0)
                                {
                                    AddContact.this.update(value);
                                    input.setText("");
                                }
                                //TODO alert of Invalid Number
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int whichButton) {
                                dialog.cancel(); // Canceled.
                            }
                        });
                alert = builder.create();
                alert.show();
            }

        });

        /*
         * TODO update this to call EditNumber with empty boxes (or default filled)
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
                	
                	//TODO use request code to properly get return data
                	AddContact.this.startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });

        this.add = (Button) this.findViewById(R.id.add);
        this.contactName = (EditText) this.findViewById(R.id.contact_name);

        /*
         * Check if a user is editing a contact and if that contact has a name
         */
        if (this.contactEdit != null && this.contactEdit.getName() != null)
        {
            this.contactName.setText(this.contactEdit.getName());

        }

        /*
         * Add/Save the user to the database and exit the activity.
         */
        this.add.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
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
                        if (!MessageService.dba.inDatabase(AddContact.this.contactEdit.getANumber()))
                        {
                            MessageService.dba.addRow(AddContact.this.contactEdit);
                            AddContact.this.contactEdit = null;
                            editTc = null;
                            AddContact.this.finish();
                        }
                        else
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddContact.this);
                            builder.setMessage("Contact is already in the database")
                                    .setCancelable(true)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int id) {

                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int whichButton) {
                                            dialog.cancel(); // Canceled.
                                        }
                                    });
                            alert = builder.create();
                            alert.show();
                        }
                    }
                    else
                    {
                        MessageService.dba.updateRow(AddContact.this.contactEdit, AddContact.this.originalNumber);
                        AddContact.this.contactEdit = null;
                        editTc = null;
                        AddContact.this.finish();
                    }
                } else
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
        });
    }

    /**
     * Update the list of numbers shown.
     * 
     * @param newNumber
     *            : String a new number to be added to the list, if null no new
     *            number is added
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
    	
    	boolean update = false;
    	String number = null;
    	int position = 0;
    	
    	if(requestCode == resultCode)
    	{
    		update = data.getBooleanExtra(EditNumber.UPDATE, true);
    		number = data.getStringExtra(EditNumber.NEW);
    		position = data.getIntExtra(AddContact.POSITION, 0);
    		
	    	if(update && number != null)
	    	{    		
    			if(position == 0)
    			{
    				update(number);
    			}
    			else
    			{
    				contactEdit.setNumber(position, number);
    				update(null);
    			}
	    	}
    	}
    }
}
