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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Prephase3Activity is the activity that is launched for the start of the program.
 * This activity shows all of the conversations the user has with contacts. The list
 * Will be updated every time a message is received. Upon clicking any of the conversations
 * MessageView activity will be started with selectedNumber = the contacts number
 * 
 * From the menu a user can select 'compose' to start SendMessageActivity to start or continue
 * a conversation with a contact. 
 * The user can also select 'settings' which will take them to the main settings page.
 */
public class Prephase3Activity extends Activity {
	//public static DBAccessor dba;
	public static final String INBOX = "content://sms/inbox";
	public static final String SENT = "content://sms/sent";
	public static SharedPreferences sharedPrefs;
	public static String selectedNumber;
	public static final String selectedNumberIntent = "com.tinfoil.sms.Selected";
	private static ConversationAdapter conversations;
	private static List<String[]> msgList;
	private static ListView list;
	private MessageReceiver boot = new MessageReceiver();

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (MessageService.dba == null)
		{
			MessageService.dba = new DBAccessor(this);
		}
		
		if (this.getIntent().hasExtra(MessageService.multipleNotificationIntent))
		{
			/*
			 * Check if there is the activity has been entered from a notification.
			 * This check specifically is to find out if there are multiple pending
			 * received messages. If there are multiple messages pending the notification
			 * will be removed.
			 */
			if (this.getIntent().getBooleanExtra(MessageService.multipleNotificationIntent, false))
			{
				MessageService.mNotificationManager.cancel(MessageService.INDEX);
			}
			this.getIntent().removeExtra(MessageService.multipleNotificationIntent);
		}
		else if (this.getIntent().hasExtra(MessageService.notificationIntent))
		{
			/*
			 * Check if there is the activity has been entered from a notification.
			 * This check is to find out if there is a single message received pending.
			 * If so then the conversation with that contact will be loaded.
			 */
			
			Intent intent = new Intent(this, MessageView.class);
			intent.putExtra(selectedNumberIntent, this.getIntent().getStringExtra(MessageService.notificationIntent));
			this.getIntent().removeExtra(MessageService.notificationIntent);
			MessageService.mNotificationManager.cancel(MessageService.INDEX);
			startActivity(intent);
		}
		
		setContentView(R.layout.main);
		//dba = new DBAccessor(this);
		
		/*
		 * Load the shared preferences
		 */
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		MessageReceiver.myActivityStarted = true;

		/*
		 * Set the list of conversations
		 */
		list = (ListView) findViewById(R.id.conversation_list);
		
		msgList = MessageService.dba.getConversations();
		conversations = new ConversationAdapter(this, R.layout.listview_item_row, msgList);		
		
		//View header = (View)getLayoutInflater().inflate(R.layout.contact_message, null);
        //list.addHeaderView(header);
		
		list.setAdapter(conversations);
        
		/*
		 * Load the selected conversation thread when clicked
		 */
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = new Intent (getBaseContext(), MessageView.class);
				intent.putExtra(Prephase3Activity.selectedNumberIntent, msgList.get(position)[0]);
				startActivity(intent);
			}
		});
		
	}
		
	/**
	 * Updates the list of the messages in the main inbox and in 
	 * the secondary inbox that the user last viewed, or is viewing
	 * @param list : ListView, the ListView for this activity to update the message list
	 */
	public static void updateList(Context context, boolean messageViewUpdate)
	{
		if (MessageReceiver.myActivityStarted)
		{
			//msgList = ContactRetriever.getSMS(context);
			msgList = MessageService.dba.getConversations();
			conversations.clear();
			conversations.addData(msgList);
			
			if (messageViewUpdate)
			{
				MessageView.updateList(context);
			}
		}
	}
	
	protected void onResume()
	{
		updateList(this, false);
		super.onResume();
	}
	
	protected void onDestroy()
	{
		MessageService.dba.close();
		stopService(new Intent(this, MessageService.class));
		MessageReceiver.myActivityStarted = false;
		super.onDestroy();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.texting_menu, menu);
		return true;
	}

	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.compose:
			startActivity(new Intent(this, SendMessageActivity.class));
			return true;
		case R.id.settings:
			startActivity(new Intent(this, QuickPrefsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	/** 
	 * Drops the message into the in-box of the default SMS program. Tricks the
	 * in-box to think the message was send by the original sender
	 * 
	 * @param srcNumber : String, the number of the contact that sent the message
	 * @param decMessage : String, the message sent from the contact
	 * @param dest : String, the folder in the android database that the message will be stored in
	 */
	public static void sendToSelf(Context c, String srcNumber, String decMessage, String dest) {
		ContentValues values = new ContentValues();
		values.put("address", srcNumber);
		values.put("body", decMessage);
		
		//Stops native sms client from reading messages as new.
		values.put("read", true); 

		/**
		 * Need to:
		 * 1. Make so that messages received from contacts not in database are ignored and sent to native
		 */
		/* Sets used to determine who sent the message, 
		 * if type == 2 then it is sent from the user
		 * if type == 1 it has been sent by the contact
		 */
		if (dest.equalsIgnoreCase(SENT))
		{
			values.put("type", "2");
		}
		else
		{
			values.put("type", "1");
		}
		
		c.getContentResolver().insert(Uri.parse(dest), values);
	}
}