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

package com.tinfoil.sms.sms;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.tinfoil.sms.R;
import com.tinfoil.sms.adapter.MessageBoxWatcher;
import com.tinfoil.sms.dataStructures.Message;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;

/**
 * SendMessageActivity is an activity that allows a user to create a new or
 * continue an old conversation. If the message is sent to a Trusted Contact (a
 * contact that has exchanged their key with the user) then it will be
 * encrypted. If the message is sent to a new contact a pop-up dialog will ask
 * the user if they would like to add the contact to tinfoil-sms's database. If
 * they user accepts AddContact will be started with addContact == true and
 * editTc != null
 */
public class SendMessageActivity extends Activity {
    private static MessageBoxWatcher messageEvent;
    private AutoCompleteTextView phoneBox;
    private EditText messageBox;
    private ArrayList<TrustedContact> tc;
    private TrustedContact newCont;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.new_message);

        //String a = null;
        //Toast.makeText(this, a.length(), Toast.LENGTH_LONG).show();
        MessageService.dba = new DBAccessor(this);

        ConversationView.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.newCont = new TrustedContact();
        
        //Do in thread.
        this.tc = MessageService.dba.getAllRows(DBAccessor.ALL);

        //Since the number is being entered cant really set a limit on the size...
        //Defaults to a trusted contact just to be safe
        final boolean isTrusted = true;//MessageService.dba.isTrustedContact(Prephase3Activity.selectedNumber);

        messageEvent = new MessageBoxWatcher(this, R.id.send_word_count, isTrusted);
        
        this.phoneBox = (AutoCompleteTextView) this.findViewById(R.id.new_message_number);
        List<String> contact;
        if (this.tc != null)
        {
            contact = SMSUtility.contactDisplayMaker(this.tc);
        }
        else
        {
            contact = null;
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.auto_complete_list_item, contact);

        Uri uri = this.getIntent().getData();
        if (uri != null)
        {
        	//Toast.makeText(this, uri.toString().split(":")[1], Toast.LENGTH_LONG).show();
        	String[] value = uri.toString().split(":");
        	if(value.length == 2)
        	{
        		this.phoneBox.setText(value[1]);
        	}
        	else
        	{
        		this.phoneBox.setText(value[0]);
        	}
        	
        	//Toast.makeText(this, , Toast.LENGTH_LONG).show();
        }
        
        this.phoneBox.setAdapter(adapter);

        this.phoneBox.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(final Editable s) {

                final String[] info = s.toString().split(", ");

                if (!info[0].trim().equals(""))
                {
                    if (info.length > 1 && !info[0].trim().equalsIgnoreCase(s.toString()))
                    {
                        SendMessageActivity.this.newCont.setName(info[0].trim());
                        SendMessageActivity.this.newCont.setNumber(info[1].trim());
                    }
                    else
                    {
                        //**Warning this could be a word, there is nothing protected it from them
                        //entering a name that is not in the database. (message will not send)
                        if (SMSUtility.isANumber(info[0].trim()))
                        {
                            if (SendMessageActivity.this.newCont.isNumbersEmpty())
                            {
                                SendMessageActivity.this.newCont.addNumber(info[0].trim());
                            }
                            else
                            {
                                SendMessageActivity.this.newCont.setNumber(info[0].trim());
                            }
                        }
                        else
                        {
                        	
                        	//TODO make this reminder part of the onClick action rather then in the text box listener onChange.
                            Toast.makeText(SendMessageActivity.this.getBaseContext(), R.string.invalid_number_message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            }
        });
        
        //this.sendSMS = (Button) this.findViewById(R.id.new_message_send);
        this.messageBox = (EditText) this.findViewById(R.id.new_message_message);

        final InputFilter[] FilterArray = new InputFilter[1];

        if (isTrusted)
        {
            FilterArray[0] = new InputFilter.LengthFilter(SMSUtility.ENCRYPTED_MESSAGE_LENGTH);
        }
        else
        {
            FilterArray[0] = new InputFilter.LengthFilter(SMSUtility.MESSAGE_LENGTH);
        }

        this.messageBox.addTextChangedListener(messageEvent);

        /*this.sendSMS.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(final View v)
            {
                
            }
        });*/
    }
    
    public void sendMessage (View view)
    {
    	if (!SendMessageActivity.this.newCont.getNumber().isEmpty()) {
            final String number = SendMessageActivity.this.newCont.getNumber(0);
            final String text = SendMessageActivity.this.messageBox.getText().toString();

            if (number.length() > 0 && text.length() > 0)
            {
            	/*
                 * Numbers are now automatically added upon sending a message to
                 * them (if they are not already in the database). The user can
                 * then go and edit their information as they please.
                 */
            	
            	//Add contact to the database
            	if(!MessageService.dba.inDatabase(number))
            	{
            		MessageService.dba.addRow(new TrustedContact(new Number(number)));
            	}
            	
            	//Add the message to the database
            	if(MessageService.dba.isTrustedContact(number))
            	{
            		MessageService.dba.addNewMessage(new Message(text, true, Message.SENT_ENCRYPTED), number, true);
            	}
            	else
            	{
            		MessageService.dba.addNewMessage(new Message(text, true, Message.SENT_DEFAULT), number, true);
            	}

                //Add the message to the queue to send it
                MessageService.dba.addMessageToQueue(number, text, false);         
                
                SendMessageActivity.this.messageBox.setText("");
                SendMessageActivity.this.phoneBox.setText("");
            }
            else
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SendMessageActivity.this);
                builder.setMessage(R.string.insufficent_information_provided)
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }

        }
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
