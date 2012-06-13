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
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Prephase3Activity extends Activity {
	//public static DBAccessor dba;
	public static final String INBOX = "content://sms/inbox";
	public static final String SENT = "content://sms/sent";
	public static SharedPreferences sharedPrefs;
	public static String selectedNumber;
	private static MessageAdapter conversations;
	private static List<String[]> msgList;
	private static ListView list;
	private MessageReceiver boot = new MessageReceiver();
	
	//private NotificationManager nm;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//dba = new DBAccessor(this);
		MessageService.dba = new DBAccessor(this);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		MessageReceiver.myActivityStarted = true;

		list = (ListView) findViewById(R.id.conversation_list);
		
		/*TextView name = (TextView)findViewById(R.id.c_name);
		TextView message = (TextView)findViewById(R.id.c_message);
		
		name.setTextColor(color.white);
		message.setTextColor(color.white);
		list.setBackgroundColor(color.black);
		*/
		
		msgList = ContactRetriever.getSMS(this);
		conversations = new MessageAdapter(this, R.layout.listview_item_row, msgList);		
		
		//View header = (View)getLayoutInflater().inflate(R.layout.contact_message, null);
        //list.addHeaderView(header);
		
		list.setAdapter(conversations);
        
		//Load up the conversation with the contact selected
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectedNumber = msgList.get(position)[0];
				startActivity(new Intent (getBaseContext(), MessageView.class));
			}
		});
	}
		
	/**
	 * Updates the list of the messages in the main inbox and in 
	 * the secondary inbox that the user last viewed, or is viewing
	 * @param list : ListView, the ListView for this activity to update the message list
	 */
	public static void updateList(Context context)
	{
		if (MessageReceiver.myActivityStarted)
		{
			msgList = ContactRetriever.getSMS(context);
			conversations.clear();
			conversations.addData(msgList);
			if (Prephase3Activity.selectedNumber != null)
			{
				MessageView.updateList(context);
			}
		}
	}
	
	protected void onResume()
	{
		Prephase3Activity.selectedNumber = null;
		updateList(this);
		super.onResume();
	}
	
	protected void onDestroy()
	{
		MessageService.dba.close();
		stopService(new Intent(this, MessageService.class));
		MessageReceiver.myActivityStarted = false;
		//unregisterReceiver(SMSbr);
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
		//***Note this is temporarily commented until we implement our own notification system
		//values.put("read", true); 

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