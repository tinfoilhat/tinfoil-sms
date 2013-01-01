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
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * SendMessageActivity is an activity that allows a user to create a new or
 * continue an old conversation. If the message is sent to a Trusted Contact
 * (a contact that has exchanged their key with the user) then it will be
 * encrypted. If the message is sent to a new contact a pop-up dialog will 
 * ask the user if they would like to add the contact to tinfoil-sms's 
 * database. If they user accepts AddContact will be started with
 * addContact == true and editTc != null
 */
public class SendMessageActivity extends Activity {
	private Button sendSMS;
	private AutoCompleteTextView phoneBox;
    private EditText messageBox;
    private ArrayList<TrustedContact> tc;
    private TrustedContact newCont;
    private static MessageBoxWatcher messageEvent;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_message);
        
        MessageService.dba = new DBAccessor(this);
        
        Prephase3Activity.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        newCont = new TrustedContact();
        tc = MessageService.dba.getAllRows();
        
        //Since the number is being entered cant really set a limit on the size...
        //Defaults to a trusted contact just to be safe
        boolean isTrusted = true;//MessageService.dba.isTrustedContact(Prephase3Activity.selectedNumber);
		
		messageEvent = new MessageBoxWatcher(this, R.id.send_word_count, isTrusted);

    	phoneBox = (AutoCompleteTextView) findViewById(R.id.new_message_number);
    	List <String> contact;
    	if (tc != null)
    	{
    		contact =SMSUtility.contactDisplayMaker(tc);
    	}
    	else
    	{
    		contact = null;
    	}
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.auto_complete_list_item, contact);
    		
    	phoneBox.setAdapter(adapter);
    	
    	phoneBox.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {

            	String []info = s.toString().split(", ");

            	if(!info[0].equals(""))
            	{
            		if (!info[0].equalsIgnoreCase(s.toString()))
	            	{
	            		newCont.setName(info[0]);
	            		newCont.setNumber(0, info[1]);
	            	}
	            	else
	            	{
	            		//**Warning this could be a word, there is nothing protected it from them
	            		//entering a name that is not in the database. (message will not send)
	            		if(SMSUtility.isANumber(info[0]))
	            		{
		            		if (newCont.isNumbersEmpty())
		            		{
		            			newCont.addNumber(info[0]);
		            		}
		            		else
		            		{
		            			newCont.setNumber(0, info[0]);
		            		}
	            		}
	            		else
	            		{
	            			Toast.makeText(getBaseContext(), "Invaild number", Toast.LENGTH_SHORT).show();
	            		}
	            	}
            	}
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
    	
    	//TODO link messageBox to sms send so that when the message box is empty it will disable the send button.
      	    
        sendSMS = (Button) findViewById(R.id.new_message_send);
        messageBox = (EditText) findViewById(R.id.new_message_message);
        
        InputFilter[] FilterArray = new InputFilter[1];
		
		
		if(isTrusted)
		{
			FilterArray[0] = new InputFilter.LengthFilter(SMSUtility.ENCRYPTED_MESSAGE_LENGTH);
		}
		else
		{
			FilterArray[0] = new InputFilter.LengthFilter(SMSUtility.MESSAGE_LENGTH);
		}
		
		messageBox.addTextChangedListener(messageEvent);
        
        sendSMS.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v) 
			{
				final String number = newCont.getNumber(0);
				String text = messageBox.getText().toString();
				
				if (number.length() > 0 && text.length() > 0)
				{
					//Send the message
					boolean sent = SMSUtility.sendMessage(newCont.getNumber(0), text, getBaseContext());
					
					//Check if the message was successful at sending
					if (sent)
					{
						if (!MessageService.dba.inDatabase(number))
						{

							AlertDialog.Builder builder = new AlertDialog.Builder(SendMessageActivity.this);
							builder.setMessage("Would you like to add " + number + " to your contacts list?")
							       .setCancelable(false)
							       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							        	   	AddContact.editTc = new TrustedContact("");
							        	   	AddContact.editTc.addNumber(number);
							        	   	AddContact.addContact = true;
							        	   	SendMessageActivity.this.startActivity(new Intent(
							        	   			SendMessageActivity.this, AddContact.class));
							        	   	finish();
							        	   	}})
							       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							               public void onClick(DialogInterface dialog, int id) {
							                   dialog.cancel();
							              }});
							AlertDialog alert = builder.create();
							alert.show();
						}
						messageBox.setText("");
						phoneBox.setText("");
					}				
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(SendMessageActivity.this);
					builder.setMessage("You have failed to provide sufficient information")
					       .setCancelable(false)
					       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {}});
					AlertDialog alert = builder.create();
					alert.show();
				}
				
			}
        });
    }
    
    /*public boolean onCreateOptionsMenu(Menu menu) {
    	 
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.texting_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	        case R.id.add:
	        startActivity(new Intent(this, AddContact.class));
	        return true;
	        default:
	        return super.onOptionsItemSelected(item);
    	}
    }*/   
}
