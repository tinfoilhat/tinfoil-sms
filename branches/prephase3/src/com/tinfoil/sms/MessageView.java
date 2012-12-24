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

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * MessageView activity allows the user to view through all the messages 
 * from or to the defined contact. selectedNumber will equal the contact
 * that the messages belong to. If a message is sent or received the list
 * of messages will be updated and Prephase3Activity's messages will be
 * updated as well.
 */
public class MessageView extends Activity {
	private static Button sendSMS;
	private EditText messageBox;
	private static ListView list2;
	private static List<String[]> msgList2;
	private static MessageAdapter messages;
	private static MessageBoxWatcher messageEvent;
	   
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Finds the number of the recently sent message attached to the notification
        if (this.getIntent().hasExtra(MessageService.notificationIntent))
		{
			Prephase3Activity.selectedNumber = this.getIntent().getStringExtra(MessageService.notificationIntent);
		}
        else if(this.getIntent().hasExtra(Prephase3Activity.selectedNumberIntent))
        {
        	Prephase3Activity.selectedNumber = this.getIntent().getStringExtra(Prephase3Activity.selectedNumberIntent);
        }
        else 
        {
        	finish();
        }
        
        setContentView(R.layout.messageviewer);
		
		//Sets the keyboard to not pop-up until a text area is selected 
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		MessageService.dba = new DBAccessor(this);
		boolean isTrusted = MessageService.dba.isTrustedContact(Prephase3Activity.selectedNumber);
		
		messageEvent = new MessageBoxWatcher(this, R.id.word_count, isTrusted);
	
		/*
		 * Create a list of messages sent between the user and the contact
		 */
		list2 = (ListView) findViewById(R.id.message_list);

		msgList2 = MessageService.dba.getSMSList(Prephase3Activity.selectedNumber);
		messages = new MessageAdapter(this, R.layout.listview_full_item_row, msgList2,
			MessageService.dba.getUnreadMessageCount(Prephase3Activity.selectedNumber));
		list2.setAdapter(messages);
		list2.setItemsCanFocus(false);	
		
		/*
		 * Reset the number of unread messages for the contact to 0
		 */
		if (MessageService.dba.getUnreadMessageCount(Prephase3Activity.selectedNumber) > 0)
        {
        	//All messages are now read since the user has entered the conversation.
        	MessageService.dba.updateMessageCount(Prephase3Activity.selectedNumber, 0);
        	if (MessageService.mNotificationManager != null)
        	{
        		MessageService.mNotificationManager.cancel(MessageService.INDEX);
        	}
        }

		//Set an action for when a user clicks on a message
		list2.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				/** TODO implement
				 * Going to add a menu of things the user can do with the messages:
				 * 1. Re-send the message
				 * 2. Delete the message
				 * 3. Copy the details
				 * 	i.  Message
				 * 	ii. Number
				 * 4. View Information
				 * 	i.  Time Received
				 * 	ii. Number
				 * 5. Forward message
				 */
			}
		});
		
		//TODO link messageBox to send button so if it is empty it will be disabled 
		
		/*
		 * Link the GUI items to the xml layout
		 */
		sendSMS = (Button) findViewById(R.id.send);
		messageBox = (EditText) findViewById(R.id.message);
		
		InputFilter[] FilterArray = new InputFilter[1];
		
		
		if(isTrusted)
		{
			FilterArray[0] = new InputFilter.LengthFilter(SMSUtility.ENCRYPTED_MESSAGE_LENGTH);
		}
		else
		{
			FilterArray[0] = new InputFilter.LengthFilter(SMSUtility.MESSAGE_LENGTH);
		}
		
		messageBox.setFilters(FilterArray);
		
		messageBox.addTextChangedListener(messageEvent);
		/*
		 * Set an action for when the user clicks on the sent button
		 */
		sendSMS.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v) 
			{
		        String text = messageBox.getText().toString();
				
				if (Prephase3Activity.selectedNumber.length() > 0 && text.length() > 0)
				{
					//Sets so that a new message sent from the user will not show up as bold
					messages.setCount(0);
					messageBox.setText("");
					messageEvent.resetCount();
					
					//Encrypt the text message before sending it	
					SMSUtility.SendMessage(Prephase3Activity.selectedNumber, text, getBaseContext());
					updateList(getBaseContext());
				}
			}
        });
		
    }   
    
    public static void updateList(Context context)
    {
    	if (Prephase3Activity.selectedNumber != null)
    	{
    		msgList2 = MessageService.dba.getSMSList(Prephase3Activity.selectedNumber);
    		messages.clear();
    		messages.addData(msgList2);
    		MessageService.dba.updateMessageCount(Prephase3Activity.selectedNumber, 0);
    	}
    }
    
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.exchange).setChecked(true);
        return true;
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.message_view_menu, menu);
		return true;
	}

	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.exchange:
			//TODO ensure this is working
			TrustedContact tc = MessageService.dba.getRow(SMSUtility.format
					(Prephase3Activity.selectedNumber));
			if (tc != null)
			{
				if (MessageService.dba.isTrustedContact(SMSUtility.format
						(Prephase3Activity.selectedNumber)))
				{
					tc.clearPublicKey();
					MessageService.dba.updateKey(tc, Prephase3Activity.selectedNumber);
				}
				else
				{
					tc.setPublicKey();
					MessageService.dba.updateKey(tc, Prephase3Activity.selectedNumber);
				}
			}
			
			return true;
		case R.id.delete:
			/*
			 * TODO add Delete Thread and another option to delete groups of messages within the thread
			 */
			return true;
	
		default:
			return super.onOptionsItemSelected(item);
		}

	}	   
}
      