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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.tinfoil.sms.R;
import com.tinfoil.sms.crypto.KeyExchange;
import com.tinfoil.sms.dataStructures.Entry;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.settings.EditNumber;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;

//TODO comment
public class KeyExchangeManager extends Activity {

	private ArrayList<Entry> entries;
	private ArrayAdapter<String> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_key_exchange_manager);
		// Show the Up button in the action bar.
		//setupActionBar();
		
		entries = MessageService.dba.getAllKeyExchangeMessages();
		
		if(entries != null)
		{
			updateList();
		}
		
	}

	/**
	 * The onClick action for when the exchange key message is pressed. Sends a
	 * key exchange message for each contact that is selected.
	 * @param view The View 
	 */
	public void exchangeKey(View view)
	{
		if(entries != null)
		{
			ListView list = (ListView)this.findViewById(R.id.key_exchange_list);
			SparseBooleanArray sba = list.getCheckedItemPositions();
			
			for (int i = 0; i < entries.size(); i++)
			{
				if(sba.get(i))
				{
					
					TrustedContact tc = MessageService.dba.getRow(SMSUtility.
							format(entries.get(0).getNumber()));
					
					final Number number = tc.getNumber(SMSUtility.
							format(entries.get(0).getNumber()));
					
					if(SMSUtility.checksharedSecret(number.getSharedInfo1()) &&
							SMSUtility.checksharedSecret(number.getSharedInfo2()))
					{
						respondMessage(number, entries.get(i));
					}
					else
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	    		LinearLayout linearLayout = new LinearLayout(this);
        	    		linearLayout.setOrientation(LinearLayout.VERTICAL);

        	    		final EditText sharedSecret1 = new EditText(this);
        	    		sharedSecret1.setHint("Shared Secret 1");
        	    		sharedSecret1.setMaxLines(EditNumber.SHARED_INFO_MAX);
        	    		sharedSecret1.setInputType(InputType.TYPE_CLASS_TEXT);
        	    		linearLayout.addView(sharedSecret1);

        	    		final EditText sharedSecret2 = new EditText(this);
        	    		sharedSecret2.setHint("Shared Secret 2");
        	    		sharedSecret2.setMaxLines(EditNumber.SHARED_INFO_MAX);
        	    		sharedSecret2.setInputType(InputType.TYPE_CLASS_TEXT);
        	    		linearLayout.addView(sharedSecret2);
        	    		
        	    		final int value = i;
        	    		
        	    		builder.setMessage("Set the shared secret for " + tc.getName() + ", " + number.getNumber())
        	    		       .setCancelable(false)
        	    		       .setPositiveButton("Save", new DialogInterface.OnClickListener() {
        	    		    	   @Override
        	    		    	   public void onClick(DialogInterface dialog, int id) {
        	    		                //Save the shared secrets
        	    		    		   String s1 = sharedSecret1.getText().toString();
        	    		    		   String s2 = sharedSecret2.getText().toString();
        	    		    		   if(SMSUtility.checksharedSecret(s1) &&
        	    								SMSUtility.checksharedSecret(s2))
        	    		    		   {
        	    		    			   //Toast.makeText(activity, "Valid secrets", Toast.LENGTH_LONG).show();
        	    		    			   number.setSharedInfo1(s1);
        	    		    			   number.setSharedInfo2(s2);
        	    		    			   MessageService.dba.updateNumberRow(number, number.getNumber(), number.getId());
        	    		    			   //number.setInitiator(true);					
       	                                
        	    			               //MessageService.dba.updateInitiator(number);
        	    			                
        	    			               //String keyExchangeMessage = KeyExchange.sign(number);
        	    			                
        	    			               //MessageService.dba.addMessageToQueue(number.getNumber(), keyExchangeMessage, true);
        	    			               respondMessage(number, entries.get(value));
        	    		    		   }
        	    		    		   else
        	    		    		   {
        	    		    			   Toast.makeText(KeyExchangeManager.this, "Invalid secrets", Toast.LENGTH_LONG).show();
        	    		    		   }
        	    		           }})
        	    		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	    		    	   @Override
        	    		    	   public void onClick(DialogInterface arg0, int arg1) {
        	    		    		   	//Cancel the key exchange
        	    		    		   Toast.makeText(KeyExchangeManager.this, "Key exchange cancelled", Toast.LENGTH_LONG).show();
        	    		    	   }});
        	    		AlertDialog alert = builder.create();
        	    		
        	    		alert.setView(linearLayout);
        	    		alert.show();
					}
					
					
				//else Item has not be touched leave it alone.
				}
			}
		}
	}
	
	/**
	 * TODO comment
	 * @param number
	 * @param entry
	 */
	public void respondMessage(Number number,Entry entry)
	{
		if(KeyExchange.verify(number, entry.getMessage()))
		{
			Log.v("Key Exchange", "Exchange Key Message Received");
			
			number.setPublicKey(KeyExchange.encodedPubKey(entry.getMessage()));
			number.setSignature(KeyExchange.encodedSignature(entry.getMessage()));
			
			MessageService.dba.deleteKeyExchangeMessage(entry.getNumber());
			
			MessageService.dba.updateNumberRow(number,
					number.getNumber(), 0);
			
			if(!number.isInitiator())
			{
				Log.v("Key Exchange", "Not Initiator");
				MessageService.dba.addMessageToQueue(number.getNumber(),
						KeyExchange.sign(number), true);
			}
			//a.remove(entries.get(i).getNumber());
			entries.remove(entry);
			updateList();
		}
	}
	
	/**
	 * The onClick action for when a for when the reject button has been
	 * clicked. Deletes every key exchange message selected.
	 * @param view The View
	 */
	public void reject(View view)
	{
		if(entries != null)
		{
			ListView list = (ListView)this.findViewById(R.id.key_exchange_list);
			SparseBooleanArray sba = list.getCheckedItemPositions();
			
			for (int i = 0; i < entries.size(); i++)
			{
				if(sba.get(i))
				{
					MessageService.dba.deleteKeyExchangeMessage(entries.get(i).getNumber());
				
					entries.remove(entries.get(i));
					updateList();
				}				
			}
			//finish();
		}
	}
	
	/**
	 * TODO change the view adapter to better represent the key exchange.
	 * Update the list key exchange messages 
	 */
	private void updateList()
	{
		String[] numbers = new String[entries.size()];
		
		for(int i = 0; i < entries.size(); i++)
		{
			numbers[i] = entries.get(i).getNumber();
		}
		
		ListView list = (ListView)this.findViewById(R.id.key_exchange_list);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, numbers);
		//a.setNotifyOnChange(true);
		
		list.setAdapter(adapter);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	
	//@SuppressLint("NewApi")
	/*@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.key_exchange_manager, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}*/
		return super.onOptionsItemSelected(item);
	}

}
