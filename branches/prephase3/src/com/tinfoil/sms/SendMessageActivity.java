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
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SendMessageActivity extends Activity {
	private Button sendSMS;
	private AutoCompleteTextView phoneBox;
	private EditText phone;
    private EditText messageBox;
    private ArrayList<TrustedContact> tc;
    private TrustedContact newCont;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_message);
        
        Prephase3Activity.dba = new DBAccessor(this);
        
        Prephase3Activity.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        newCont = new TrustedContact(null);
        tc = Prephase3Activity.dba.getAllRows();

    	phoneBox = (AutoCompleteTextView) findViewById(R.id.new_message_number);
    	List <String> contact;
    	if (tc != null)
    	{
    		contact =ContactRetriever.contactDisplayMaker(tc);
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
            	
            	if (!info[0].equalsIgnoreCase(s.toString()))
            	{
            		newCont.setName(info[0]);
            		newCont.setNumber(0, info[1]);
            	}
            	else
            	{
            		//**Warning this could be a word, there is nothing protected it from them
            		//entering a name that is not in the database. (message will not send)
            		if (newCont.isNumbersEmpty())
            		{
            			newCont.addNumber(info[0]);
            		}
            		else
            		{
            			newCont.setNumber(0, info[0]);
            		}
            	}
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
      	    
        sendSMS = (Button) findViewById(R.id.new_message_send);
        messageBox = (EditText) findViewById(R.id.new_message_message);
        
        sendSMS.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v) 
			{
				final String number = newCont.getNumber(0);
				String text = messageBox.getText().toString();
				
				if (number.length() > 0 && text.length() > 0)
				{
					//Encrypt the text message before sending it	
					try
					{
                    	//Only expects encrypted messages from trusted contacts in the secure state
						if (Prephase3Activity.dba.isTrustedContact(number) && 
								Prephase3Activity.sharedPrefs.getBoolean("enable", true))
						{
							ContactRetriever.sendSMS(getBaseContext(), number, Encryption.aes_encrypt(
									Prephase3Activity.dba.getRow(ContactRetriever.format(number))
									.getPublicKey(), text));
							Prephase3Activity.sendToSelf(getBaseContext(), number, Encryption.aes_encrypt(
									Prephase3Activity.dba.getRow(ContactRetriever.format(number))
									.getPublicKey(), text), Prephase3Activity.SENT);
							Prephase3Activity.sendToSelf(getBaseContext(), number, text, Prephase3Activity.SENT);
							
							Prephase3Activity.dba.UpdateLastMessage(ContactRetriever.format(number), text);
							Toast.makeText(getBaseContext(), "Encrypted Message sent", Toast.LENGTH_SHORT).show();
						}
						else
						{
							ContactRetriever.sendSMS(getBaseContext(), number, text);
							Prephase3Activity.sendToSelf(getBaseContext(), number, text, Prephase3Activity.SENT);
							Prephase3Activity.dba.UpdateLastMessage(ContactRetriever.format(number), text);
							
							Toast.makeText(getBaseContext(), "Message sent", Toast.LENGTH_SHORT).show();
						}
						if (!Prephase3Activity.dba.inDatabase(number))
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
			        catch ( Exception e ) 
			        { 
			        	Toast.makeText(getBaseContext(), "FAILED TO SEND", Toast.LENGTH_LONG).show();
			        	e.printStackTrace(); 
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
