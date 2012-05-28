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
import android.app.AlertDialog;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class MessageView extends Activity {
	
	Button sendSMS;
	EditText messageBox;
	public static ListView list2;
	public static List<String[]> msgList2;
	
	//Change the password here or give a user possibility to change it
    //private static final byte[] PASSWORD = new byte[]{ 0x20, 0x32, 0x34, 0x47, (byte) 0x84, 0x33, 0x58 };
    //private static final String PASSWORD = "test123";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.messageviewer);
		
		//Sets the keyboard to not pop-up until a text area is selected 
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		Prephase2Activity.dba = new DBAccessor(this);
	
		Prephase2Activity.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
		list2 = (ListView) findViewById(R.id.message_list);
		msgList2 = ContactRetriever.getPersonSMS(this);
		
		//for (int i =0;i<1;i++)
		//	Toast.makeText(this, msgList2.get(0)[i],Toast.LENGTH_LONG);
		list2.setAdapter(new MessageAdapter(this,
				R.layout.listview_full_item_row, msgList2));
		list2.setItemsCanFocus(false);

		list2.setOnItemClickListener(new OnItemClickListener() {
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
					//Toast.makeText(getBaseContext(), "" + Prephase2Activity.selectedNumber, Toast.LENGTH_SHORT).show();

					//Encrypt the text message before sending it	
					try
					{
						messageBox.setText("");
						//Toast.makeText(getBaseContext(), ""+Prephase2Activity.selectedNumber.substring(1), Toast.LENGTH_SHORT).show();
						//Toast.makeText(getBaseContext(), ""+Prephase2Activity.selectedNumber.substring(2), Toast.LENGTH_SHORT).show();
												
						//Only expects encrypted messages from trusted contacts in the secure state
						if (Prephase2Activity.dba.isTrustedContact(Prephase2Activity.selectedNumber) && 
								Prephase2Activity.sharedPrefs.getBoolean("enable", true))
						{
							sendSMS(Prephase2Activity.selectedNumber, Encryption.aes_encrypt(
									Prephase2Activity.dba.getRow(ContactRetriever.format(
											Prephase2Activity.selectedNumber)).getKey(), text));							
							
							Prephase2Activity.sendToSelf(getBaseContext(), Prephase2Activity.selectedNumber,
									Encryption.aes_encrypt(Prephase2Activity.dba.getRow(ContactRetriever.format
									(Prephase2Activity.selectedNumber)).getKey(), text), Prephase2Activity.SENT);
							Prephase2Activity.sendToSelf(getBaseContext(), Prephase2Activity.selectedNumber,
									 text, Prephase2Activity.SENT);
							Toast.makeText(getBaseContext(), "Encrypted Message sent", Toast.LENGTH_SHORT).show();
						}
						else
						{
							sendSMS(Prephase2Activity.selectedNumber, text);
							Prephase2Activity.sendToSelf(getBaseContext(), Prephase2Activity.selectedNumber,
									text, Prephase2Activity.SENT);
							Toast.makeText(getBaseContext(), "Message sent", Toast.LENGTH_SHORT).show();
						}
						updateList();
						
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
    
    public void updateList()
    {
    	MessageView.msgList2 = ContactRetriever.getPersonSMS(this);
		MessageView.list2.setAdapter(new MessageAdapter(this,
				R.layout.listview_full_item_row, MessageView.msgList2));
    }
    
    protected void onStart()
    {
		super.onStart();
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
			//Add to trusted Contact list
			TrustedContact tc = Prephase2Activity.dba.getRow(ContactRetriever.format
					(Prephase2Activity.selectedNumber));
			if (tc != null)
			{
				if (Prephase2Activity.dba.isTrustedContact(ContactRetriever.format
						(Prephase2Activity.selectedNumber)))
				{
					//tc.setKey(null);
					tc.clearKey();
					Prephase2Activity.dba.updateRow(tc, Prephase2Activity.selectedNumber);
				}
				else
				{
					//tc.setKey(KEY);
					tc.setKey();
					Prephase2Activity.dba.updateRow(tc, Prephase2Activity.selectedNumber);
				}
			}
			
			return true;
		case R.id.delete:
			//Not sure if we should have it delete the contact or delete the conversation
			return true;
	
		default:
			return super.onOptionsItemSelected(item);
		}

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