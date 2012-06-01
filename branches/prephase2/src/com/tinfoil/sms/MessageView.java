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
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

//**Might be a good idea for this activity to extend the main activity, prephase2Activity. 
public class MessageView extends Activity {
	
	private Button sendSMS;
	private EditText messageBox;
	public static ListView list2;
	public static List<String[]> msgList2;
	   
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
					//Encrypt the text message before sending it	
					try
					{
						messageBox.setText("");
																		
						//Only expects encrypted messages from trusted contacts in the secure state
						if (Prephase2Activity.dba.isTrustedContact(Prephase2Activity.selectedNumber) && 
								Prephase2Activity.sharedPrefs.getBoolean("enable", true))
						{
							ContactRetriever.sendSMS(getBaseContext(), Prephase2Activity.selectedNumber, 
									Encryption.aes_encrypt(Prephase2Activity.dba.getRow(
									ContactRetriever.format(Prephase2Activity.selectedNumber))
									.getPublicKey(), text));							
							
							Prephase2Activity.sendToSelf(getBaseContext(), Prephase2Activity.selectedNumber,
									Encryption.aes_encrypt(Prephase2Activity.dba.getRow(ContactRetriever.format
									(Prephase2Activity.selectedNumber)).getPublicKey(), text), Prephase2Activity.SENT);
							Prephase2Activity.sendToSelf(getBaseContext(), Prephase2Activity.selectedNumber,
									 text, Prephase2Activity.SENT);
							Toast.makeText(getBaseContext(), "Encrypted Message sent", Toast.LENGTH_SHORT).show();
						}
						else
						{
							ContactRetriever.sendSMS(getBaseContext(), Prephase2Activity.selectedNumber, text);
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
					tc.clearPublicKey();
					Prephase2Activity.dba.updateRow(tc, Prephase2Activity.selectedNumber);
				}
				else
				{
					tc.setPublicKey();
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
}
      