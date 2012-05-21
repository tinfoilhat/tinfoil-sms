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

public class Prephase2Activity extends Activity {
	static DBAccessor dba;
	public static final String INBOX = "content://sms/inbox";
	public static final String SENT = "content://sms/sent";
	static SharedPreferences sharedPrefs;
	private static List<String[]> msgList;
	static String selectedNumber;
	private ListView list;
	
	// Change the password here or give a user possibility to change it
	// private static final byte[] PASSWORD = new byte[]{ 0x20, 0x32, 0x34,
	// 0x47, (byte) 0x84, 0x33, 0x58 };
	//private static final String PASSWORD = "test123";

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		dba = new DBAccessor(this);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		list = (ListView) findViewById(R.id.conversation_list);
		
		/*TextView name = (TextView)findViewById(R.id.c_name);
		TextView message = (TextView)findViewById(R.id.c_message);
		
		name.setTextColor(color.white);
		message.setTextColor(color.white);
		list.setBackgroundColor(color.black);
		*/
		
		//msgList = ContactRetriever.getSMS(this, 10);
		msgList = ContactRetriever.getSMS(this);

		MessageAdapter adapter = new MessageAdapter(this, R.layout.listview_item_row, msgList);
		
		
		//View header = (View)getLayoutInflater().inflate(R.layout.contact_message, null);
        //list.addHeaderView(header);
		list.setAdapter(adapter);
        
		//Load up the conversation with the contact selected
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectedNumber = msgList.get(position)[0];
				startActivity(new Intent (getBaseContext(), MessageView.class));
			}
		});
		
		// String actualNumber = "5555215556";
		// dba.addRow("billy", actualNumber, "12345", 2);

		final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
		BroadcastReceiver SMSbr = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// Called every time a new sms is received
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					// This will put every new message into a array of
					// SmsMessages. The message is received as a pdu,
					// and needs to be converted to a SmsMessage, if you want to
					// get information about the message.
					Object[] pdus = (Object[]) bundle.get("pdus");
					final SmsMessage[] messages = new SmsMessage[pdus.length];
					for (int i = 0; i < pdus.length; i++) {
						messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					}

					if (messages.length > -1) {
						
						/* Shows a Toast with the phone number of the sender, and the message.
						 * String smsToast = "New SMS received from " +
						 * messages[0].getOriginatingAddress() + "\n'Test" +
						 * messages[0].getMessageBody() + "'";
						 */

						/*TrustedContact tc =
						dba.getRow(messages[0].getOriginatingAddress());
						Toast.makeText(getBaseContext(),tc.getName() + "\n" +
						tc.getNumber() + "\n" + tc.getKey() +"\n"+
						tc.getVerified(), Toast.LENGTH_SHORT).show();
						Toast.makeText(getBaseContext(),messages[0].getOriginatingAddress(),
						Toast.LENGTH_SHORT).show();
						*/
						
						
						String address = messages[0].getOriginatingAddress();
						/*mess = mess.substring(1);
						Toast.makeText(getBaseContext(),address.substring(1), Toast.LENGTH_SHORT).show();
						Toast.makeText(getBaseContext(),dba.getRow(address.substring(1)).getNumber(), Toast.LENGTH_SHORT).show();
						*/
						
						// Only expects encrypted messages from trusted contacts in the secure state
						if (dba.isTrustedContact((address))) {
							Toast.makeText(context,	"Encrypted Message Received", Toast.LENGTH_SHORT).show();
							Toast.makeText(context,	messages[0].getMessageBody(), Toast.LENGTH_LONG).show();
							
							//TrustedContact tc = dba.getRow(address);
							
							/*
							 * Now send the decrypted message to ourself, set
							 * the source address of the message to the original
							 * sender of the message
							 */
							try {
								sendToSelf(getBaseContext(), messages[0].getOriginatingAddress(), 
										messages[0].getMessageBody(), INBOX);
								sendToSelf(getBaseContext(), messages[0].getOriginatingAddress(),	
										Encryption.aes_decrypt(dba.getRow(ContactRetriever.format
										(address)).getKey(), messages[0].getMessageBody()), INBOX);
								Toast.makeText(context, "Message Decrypted", Toast.LENGTH_SHORT).show();
								updateList();
							} catch (Exception e) {
								Toast.makeText(context, "FAILED TO DECRYPT", Toast.LENGTH_LONG).show();
								e.printStackTrace();
							}
						} else {
							Toast.makeText(context, "Message Received", Toast.LENGTH_LONG).show();
							Toast.makeText(context, messages[0].getMessageBody(), Toast.LENGTH_LONG).show();
							sendToSelf(getBaseContext(), messages[0].getOriginatingAddress(),
									messages[0].getMessageBody(), INBOX);
							updateList();
						}
					}
				}

				// Prevent other applications from seeing the message received
				this.abortBroadcast();

			}
		};
		// The BroadcastReceiver needs to be registered before use.
		IntentFilter SMSfilter = new IntentFilter(SMS_RECEIVED);
		this.registerReceiver(SMSbr, SMSfilter);
	}
	
	

	
	/**
	 * Updates the list of the messages in the main inbox and in 
	 * the secondary inbox that the user last viewed, or is viewing
	 * @param list : ListView, the ListView for this activity to update the message list
	 */
	private void updateList()
	{
		//msgList = ContactRetriever.getSMS(this, 0);
		msgList = ContactRetriever.getSMS(this);
		list.setAdapter(new MessageAdapter(this, R.layout.listview_item_row, msgList));
		if (Prephase2Activity.selectedNumber != null)
		{
			MessageView.msgList2 = ContactRetriever.getPersonSMS(this);
			MessageView.list2.setAdapter(new MessageAdapter(this,
					R.layout.listview_full_item_row, MessageView.msgList2));
		}
		
	}
	
	protected void onResume()
	{
		Prephase2Activity.selectedNumber = null;
		updateList();
		super.onResume();
	}
	
	protected void onDestroy()
	{
		dba.close();
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