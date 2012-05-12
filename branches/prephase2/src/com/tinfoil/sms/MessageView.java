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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class MessageView extends Activity {
	Button sendSMS;
	EditText messageBox;
	
  //Change the password here or give a user possibility to change it
    //private static final byte[] PASSWORD = new byte[]{ 0x20, 0x32, 0x34, 0x47, (byte) 0x84, 0x33, 0x58 };
    private static final String PASSWORD = "test123";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.messageviewer);
		
		Prephase2Activity.dba = new DBAccessor(this);
        
        Prephase2Activity.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
		ListView list = (ListView) findViewById(R.id.listView1);
		List<String[]> msgList = getSMS();
		// String []msgList = {"bla", "sasdd"};

		list.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.test_list_item, messageMaker(msgList)));
		list.setItemsCanFocus(false);

		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
			}
		});
		
		sendSMS = (Button) findViewById(R.id.send);
		messageBox = (EditText) findViewById(R.id.message);
		
		sendSMS.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v) 
			{
		        String text = messageBox.getText().toString();
				
				if (Prephase2Activity.selectedNumber.length() > 0 && text.length() > 0)
				{
					Toast.makeText(getBaseContext(), "" + Prephase2Activity.selectedNumber, Toast.LENGTH_SHORT).show();

					//Encrypt the text message before sending it	
					try
					{
                    	//Only expects encrypted messages from trusted contacts in the secure state
						if (Prephase2Activity.dba.isTrustedContact(Prephase2Activity.selectedNumber) && 
								Prephase2Activity.sharedPrefs.getBoolean("enable", true))
						{
							sendSMS(Prephase2Activity.selectedNumber, Encryption.aes_encrypt(PASSWORD, text));
							Toast.makeText(getBaseContext(), "Encrypted Message sent", Toast.LENGTH_SHORT).show();
						}
						else
						{
							sendSMS(Prephase2Activity.selectedNumber, text);
							Toast.makeText(getBaseContext(), "Message sent", Toast.LENGTH_SHORT).show();
						}
						
						messageBox.setText("");
						//phoneBox.setText("");
				    }
			        catch ( Exception e ) 
			        { 
			        	Toast.makeText(getBaseContext(), "FAILED TO SEND", Toast.LENGTH_LONG).show();
			        	e.printStackTrace(); 
			    	}
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(MessageView.this);
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

    public List<String> messageMaker (List<String[]> sms)
	{
		List <String> messageList = new ArrayList<String>();
		for (int i = 0; i < sms.size();i++)
		{
			messageList.add(sms.get(i)[1] + ": " + sms.get(i)[0] + " : "+ sms.get(i)[2]);
		}
		return messageList;
		
	}
	
	public List<String[]> getSMS() {
		List<String[]> sms = new ArrayList<String[]>();
		Uri uriSMSURI = Uri.parse("content://sms/inbox");
		Cursor cur = getContentResolver().query(uriSMSURI, null, "address = ?",
				new String[] {Prephase2Activity.selectedNumber}, null);
		// ContentResolver cr = getContentResolver();
		// Cursor nCur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
		// null, null, null);

		// Used to remove duplication
		//Hashtable<String, Boolean> numbers = new Hashtable<String, Boolean>();

		while (cur.moveToNext()) {
			String address = cur.getString(cur.getColumnIndex("address"));
			String id = cur.getString(cur.getColumnIndex("_id"));
			//if (numbers.isEmpty() || numbers.get(address) == null) {
				//numbers.put(address, true);
				String name = nameHelper(address);
				String body = cur.getString(cur.getColumnIndexOrThrow("body"));
				//msg.add("Number: " + address + " < Name " + name
					//	+ "> Message: " + body);
				//msg2.add(address);
				//sms.add(msg2);
				sms.add(new String[] {address, name, body});
			//}
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
	
	/**
     * Sends the given message to the phone with the given number
     * @param number : String, the number of the phone that the message is sent to
     * @param message : String, the message, encrypted that will be sent to the contact
     */
    public void sendSMS (String number, String message)
    {
    	PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, Object.class), 0);
        SmsManager sms = SmsManager.getDefault();
        
        //this is the function that does all the magic
        sms.sendTextMessage(number, null, message, pi, null);
    	
    }
    
}