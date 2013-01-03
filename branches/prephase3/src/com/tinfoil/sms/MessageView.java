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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
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
	private static final String[] options = new String[]{"Delete message", "Copy message", "Forward message"};
	private static String contact_name;
	private ArrayList<TrustedContact> tc;
	private static AutoCompleteTextView phoneBox;
	private AlertDialog popup_alert;
	   
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
		Prephase3Activity.messageViewActive = true;
		boolean isTrusted = MessageService.dba.isTrustedContact(Prephase3Activity.selectedNumber);
		
		messageEvent = new MessageBoxWatcher(this, R.id.word_count, isTrusted);
	
		/*
		 * Create a list of messages sent between the user and the contact
		 */
		list2 = (ListView) findViewById(R.id.message_list);

		msgList2 = MessageService.dba.getSMSList(Prephase3Activity.selectedNumber);
		int unreadCount = MessageService.dba.getUnreadMessageCount(Prephase3Activity.selectedNumber);
		Toast.makeText(this, String.valueOf(unreadCount), Toast.LENGTH_SHORT).show();
		messages = new MessageAdapter(this, R.layout.listview_full_item_row, msgList2,
			unreadCount);
		
		
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
		
		//Retrieve the name of the contact from the database
		contact_name = MessageService.dba.getRow(Prephase3Activity.selectedNumber).getName();
		
		//Set an action for when a user clicks on a message
		list2.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				final int item_num = position;
				
				
				AlertDialog.Builder popup_builder = new AlertDialog.Builder(MessageView.this);
				popup_builder.setTitle(contact_name)
					   .setItems(options, new DialogInterface.OnClickListener() {
						   
						public void onClick(DialogInterface dialog, int which) {
							
							final String[] messageValue = (String[])list2.getItemAtPosition(item_num);
							
							//Toast.makeText(MessageView.this, messageValue[1], Toast.LENGTH_SHORT).show();
							if(which == 0)
							{
								//option = Delete
								MessageService.dba.deleteMessage(Long.valueOf(messageValue[3]));
								updateList(MessageView.this);
							}
							else if(which == 1)
							{
								//TODO implement
								//option = Copy message
								Toast.makeText(getBaseContext(), "implement me", Toast.LENGTH_SHORT).show();
							}
							else if(which == 2)
							{
								
								//option = Forward message
								phoneBox = new AutoCompleteTextView(getBaseContext());
								
								List <String> contact = null;
								if(tc == null)
								{
									tc = MessageService.dba.getAllRows();
								}
								
						    	if (tc != null)
						    	{
						    		if(contact == null)
						    		{
						    			contact =SMSUtility.contactDisplayMaker(tc);
						    		}
						    	}
						    	else
						    	{
						    		contact = null;
						    	}
						    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.auto_complete_list_item, contact);
						    	
						    	phoneBox.setAdapter(adapter);
								
								AlertDialog.Builder contact_builder = new AlertDialog.Builder(MessageView.this);
								
								contact_builder.setTitle("Input contact number")
									.setCancelable(true)
									.setView(phoneBox)
									.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

										public void onClick(DialogInterface dialog, int which) {
											String []info = phoneBox.getText().toString().split(", ");
											
											boolean invalid = false;
											//TODO identify whether a forwarded message has a special format
											if(info != null)
											{
												
												if(info.length == 2 && info[1] != null)
												{
													if(SMSUtility.isANumber(info[1]))
													{
														sendMessage(info[1], messageValue[1]);
													}
													else
													{
														invalid = true;
													}
												}
												else
												{
													String num = phoneBox.getText().toString();
													if(SMSUtility.isANumber(num))
													{
														sendMessage(num, messageValue[1]);
													}
													else
													{
														invalid = true;
													}
												}
											}
											
											if(invalid)
											{
												Toast.makeText(getBaseContext(), "Invalid number", Toast.LENGTH_SHORT).show();
											}
										}
										
									});
								AlertDialog contact_alert = contact_builder.create();
								
								popup_alert.cancel();
								contact_alert.show();
							}
						}
						   
					   })
				       .setCancelable(true);
				popup_alert = popup_builder.create();
				popup_alert.show();
				/** TODO implement
				 * Going to add a menu of things the user can do with the messages:
				 * 2. Copy the details
				 * 	i.  Message
				 * 	ii. Number
				 * 4. View Information
				 * 	i.  Time Received/Sent
				 * 	ii. Number
				 */
			}
		});
		
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
				sendMessage(Prephase3Activity.selectedNumber, messageBox.getText().toString());
			}
        });
		
    }   
    
    public void sendMessage(String number, String text)
    {
		if (number.length() > 0 && text.length() > 0)
		{
			//Sets so that a new message sent from the user will not show up as bold
			messages.setCount(0);
			messageBox.setText("");
			messageEvent.resetCount();
			
			//Encrypt the text message before sending it	
			SMSUtility.sendMessage(number, text, getBaseContext());
			updateList(getBaseContext());
		}
    }
    
    protected void onResume()
    {
    	super.onResume();
   }
        
    public static void updateList(Context context)
    {
    	if (Prephase3Activity.selectedNumber != null)
    	{
    		msgList2 = MessageService.dba.getSMSList(Prephase3Activity.selectedNumber);
    		messages.clear();
    		messages.addData(msgList2);
    		
    		//TODO fix so when entering MessageView from any state the new messages will show in bold. (until exited message view or user sends out a message)
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
			/*
			 * TODO update so that this launches the exchange keys thread.
			 */
			TrustedContact tc = MessageService.dba.getRow(SMSUtility.format
					(Prephase3Activity.selectedNumber));
			if (tc != null)
			{
				if (MessageService.dba.isTrustedContact(SMSUtility.format
						(Prephase3Activity.selectedNumber)))
				{
					tc.getNumber(Prephase3Activity.selectedNumber).clearPublicKey();
					MessageService.dba.updateKey(tc.getNumber(Prephase3Activity.selectedNumber));
				}
				else
				{
					tc.getNumber(Prephase3Activity.selectedNumber).setPublicKey();
					MessageService.dba.updateKey(tc.getNumber(Prephase3Activity.selectedNumber));
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
      