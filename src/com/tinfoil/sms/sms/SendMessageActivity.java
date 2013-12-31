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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.tinfoil.sms.R;
import com.tinfoil.sms.adapter.MessageBoxWatcher;
import com.tinfoil.sms.crypto.ExchangeKey;
import com.tinfoil.sms.dataStructures.Message;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
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
    
    private DBAccessor dba;
    private static ExchangeKey keyThread = new ExchangeKey();
    

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.new_message);

        //String a = null;
        //Toast.makeText(this, a.length(), Toast.LENGTH_LONG).show();
        dba = new DBAccessor(this);

        ConversationView.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.newCont = new TrustedContact();
        
        //Do in thread.
        this.tc = dba.getAllRows(DBAccessor.ALL);

        //Since the number is being entered cant really set a limit on the size...
        //Defaults to a trusted contact just to be safe
        final boolean isTrusted = true;

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
        	String[] value = uri.toString().split(":");
        	if(value.length == 2)
        	{
        		this.phoneBox.setText(value[1]);
        	}
        	else
        	{
        		this.phoneBox.setText(value[0]);
        	}
        	
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
    }
    
    public void sendMessage (View view)
    {
    	String[] temp = checkValidNumber(true, true);
    	
    	if(temp != null)
    	{
    		String number = temp[0];
    		String text = temp[1];
    		/*
             * Numbers are now automatically added upon sending a message to
             * them (if they are not already in the database). The user can
             * then go and edit their information as they please.
             */
        	
        	//Add contact to the database
        	if(!dba.inDatabase(number))
        	{
        		dba.addRow(new TrustedContact(new Number(number)));
        	}
        	
        	//Add the message to the database
        	if(dba.isTrustedContact(number))
        	{
        		dba.addNewMessage(new Message(text, true, Message.SENT_ENCRYPTED), number, true);
        	}
        	else
        	{
        		dba.addNewMessage(new Message(text, true, Message.SENT_DEFAULT), number, true);
        	}

            //Add the message to the queue to send it
            dba.addMessageToQueue(number, text, false);         
            
            SendMessageActivity.this.messageBox.setText("");
            SendMessageActivity.this.phoneBox.setText("");
    	}
    	       	
            
    }

    public boolean onCreateOptionsMenu(Menu menu) {

    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_message_menu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        String[] values = checkValidNumber(false, false);
        if(values != null)
        {
	        if(dba.isTrustedContact(values[0]))
	        {
	        	menu.findItem(R.id.exchange)
	        		.setTitle(R.string.untrust_contact_menu_full)
	        		.setTitleCondensed(this.getString(R.string.untrust_contact_menu_short))
	        		.setEnabled(true);
	        }
	        else
	        {
	        	if(dba.getKeyExchangeMessage(values[0]) != null)
	        	{
	        		menu.findItem(R.id.exchange)
	        			.setTitle(R.string.resolve_key_exchange_full)
	        			.setTitleCondensed(this.getString(R.string.resolve_key_exchange_short))
	        			.setEnabled(true);
	        	}
	        	else
	        	{
	        		menu.findItem(R.id.exchange)
	        			.setTitle(R.string.exchange_key_full)
	        			.setTitleCondensed(this.getString(R.string.exchange_key_short))
	        			.setEnabled(true);
	        	}
	        }
        }
        else
        {
        	menu.findItem(R.id.exchange).setEnabled(false);
        }
        return true;
    }

    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
            case R.id.exchange:
            	
            	//This is a bit of a redundant check
            	String[] value = checkValidNumber(false, false);
            	if(value!= null) {
            		
            		//Add contact to the database
                	if(!dba.inDatabase(value[0]))
                	{
                		dba.addRow(new TrustedContact(new Number(value[0])));
                	}
                	                	
                	SMSUtility.handleKeyExchange(keyThread, dba, this, value[0]);
            	}
            	
            	return true;
            default:
            return super.onOptionsItemSelected(item);
    	}
    }
    
    private String[] checkValidNumber(boolean checkText, boolean showError)
    {
    	if (!SendMessageActivity.this.newCont.getNumber().isEmpty()) {
            final String number = SendMessageActivity.this.newCont.getNumber(0);
            final String text = SendMessageActivity.this.messageBox.getText().toString();

            if (number.length() > 0 && (!checkText || text.length() > 0))
            {
            	return new String[]{number, text};

            }
            else if(showError)
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
    	else if(showError)
    	{
    		Toast.makeText(SendMessageActivity.this.getBaseContext(), R.string.invalid_number_message, Toast.LENGTH_SHORT).show();
    	}
    	return null;
    }
}
