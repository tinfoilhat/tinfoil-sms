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
import java.util.Hashtable;
import java.util.List;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Prephase2Activity extends Activity {
	static DBAccessor dba;
	static SharedPreferences sharedPrefs;
	private static List<String[]> msgList;
	static String selectedNumber;

	// Change the password here or give a user possibility to change it
	// private static final byte[] PASSWORD = new byte[]{ 0x20, 0x32, 0x34,
	// 0x47, (byte) 0x84, 0x33, 0x58 };
	private static final String PASSWORD = "test123";

	/** Called when the activity is first created. */
	// @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messages);
		dba = new DBAccessor(this);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		final ListView list = (ListView) findViewById(R.id.listView1);
		msgList = getSMS();
		// String []msgList = {"bla", "sasdd"};

		// Toast.makeText(getApplicationContext(), "Here",
		// Toast.LENGTH_SHORT).show();
		list.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.test_list_item, messageMaker(msgList)));
		list.setItemsCanFocus(false);

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
						messages[i] = SmsMessage
								.createFromPdu((byte[]) pdus[i]);
					}

					if (messages.length > -1) {
						// Shows a Toast with the phone number of the sender,
						// and the message.
						/*
						 * String smsToast = "New SMS received from " +
						 * messages[0].getOriginatingAddress() + "\n'Test" +
						 * messages[0].getMessageBody() + "'";
						 */

						// TrustedContact tc =
						// dba.getRow(messages[0].getOriginatingAddress());
						// Toast.makeText(getBaseContext(),tc.getName() + "\n" +
						// tc.getNumber() + "\n" + tc.getKey() +"\n"+
						// tc.getVerified(), Toast.LENGTH_SHORT).show();
						// Toast.makeText(getBaseContext(),messages[0].getOriginatingAddress(),
						// Toast.LENGTH_SHORT).show();
						
						// Only expects encrypted messages from trusted contacts
						// in the secure state
						String mess = messages[0].getOriginatingAddress();
						//mess = mess.substring(1, mess.length());
						// Toast.makeText(getBaseContext(),mess,
						// Toast.LENGTH_SHORT).show();
						if (dba.isTrustedContact(mess)) {
							Toast.makeText(context,
									"Encrypted Message Received",
									Toast.LENGTH_LONG).show();
							Toast.makeText(context,
									messages[0].getMessageBody(),
									Toast.LENGTH_LONG).show();

							/*
							 * Now send the decrypted message to ourself, set
							 * the source address of the message to the original
							 * sender of the message
							 */
							try {
								sendToSelf(messages[0].getOriginatingAddress(), messages[0].getMessageBody());
								sendToSelf(messages[0].getOriginatingAddress(),	Encryption.aes_decrypt(PASSWORD,
												messages[0].getMessageBody()));
								Toast.makeText(context, "Message Decrypted", Toast.LENGTH_LONG).show();
								msgList = getSMS();
								list.setAdapter(new ArrayAdapter<String>(
										getBaseContext(),
										android.R.layout.test_list_item,
										messageMaker(msgList)));
								list.setItemsCanFocus(false);
							} catch (Exception e) {
								Toast.makeText(context, "FAILED TO DECRYPT",
										Toast.LENGTH_LONG).show();
								e.printStackTrace();
							}
						} else {
							Toast.makeText(context, "Message Received", Toast.LENGTH_LONG).show();
							Toast.makeText(context, messages[0].getMessageBody(), Toast.LENGTH_LONG).show();
							sendToSelf(messages[0].getOriginatingAddress(), messages[0].getMessageBody());
							msgList = getSMS();
							list.setAdapter(new ArrayAdapter<String>(
									getBaseContext(),
									android.R.layout.test_list_item, messageMaker(msgList)));
							list.setItemsCanFocus(false);
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

	public List<String> messageMaker (List<String[]> sms)
	{
		List <String> messageList = new ArrayList<String>();
		for (int i = 0; i < sms.size();i++)
		{
			messageList.add(sms.get(i)[1] + ": " + sms.get(i)[2]);
		}
		return messageList;
		
	}
	
	public List<String[]> getSMS() {
		List<String[]> sms = new ArrayList<String[]>();
		Uri uriSMSURI = Uri.parse("content://sms/inbox");
		Cursor cur = getContentResolver().query(uriSMSURI, null, null, null,
				null);
		// ContentResolver cr = getContentResolver();
		// Cursor nCur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
		// null, null, null);

		// Used to remove duplication
		Hashtable<String, Boolean> numbers = new Hashtable<String, Boolean>();

		while (cur.moveToNext()) {
			String address = cur.getString(cur.getColumnIndex("address"));
			String id = cur.getString(cur.getColumnIndex("_id"));
			if (numbers.isEmpty() || numbers.get(address) == null) {
				numbers.put(address, true);
				String name = nameHelper(address);
				String body = cur.getString(cur.getColumnIndexOrThrow("body"));
				//msg.add("Number: " + address + " < Name " + name
					//	+ "> Message: " + body);
				//msg2.add(address);
				//sms.add(msg2);
				sms.add(new String[] {address, name, body});
			}
		}
		cur.close();
		return sms;
	}

	public String format(String number) {
		if (!number.substring(0, 2).equalsIgnoreCase("+1")) {
			return number;
		}
		return number.substring(2);
	}

	public String nameHelper(String number) {
		String num = findNameByAddress(number);
		if (num.equalsIgnoreCase(number)) {
			return findNameByAddress(format(number));
		}
		return num;
	}

	public String findNameByAddress(String addr) {
		Uri myPerson = Uri.withAppendedPath(
				ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
				Uri.encode(addr));

		String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };

		Cursor cursor = getContentResolver().query(myPerson, projection, null,
				null, null);

		if (cursor.moveToFirst()) {

			String name = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			cursor.close();
			return name;
		}

		cursor.close();

		return addr;
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
	 * @param srcNumber
	 *            : String, the number of the contact that sent the message
	 * @param decMessage
	 *            : String, the message sent from the contact
	 */
	private void sendToSelf(String srcNumber, String decMessage) {
		ContentValues values = new ContentValues();
		values.put("address", srcNumber);
		values.put("body", decMessage);
		getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
	}
}