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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.tinfoil.sms.R;
import com.tinfoil.sms.crypto.KeyExchange;
import com.tinfoil.sms.crypto.KeyExchangeHandler;
import com.tinfoil.sms.dataStructures.Entry;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.settings.EditNumber;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;

/**
 * The activity for handling pending key exchanges. If the user does not set
 * the contact's shared secrets or the contact's incoming key exchange messages
 * are set to be handled manually.
 */
public class KeyExchangeManager extends Activity {

	private ArrayAdapter<String> adapter;
	public static final String COMPLETE = "complete";
	public static final String ENTRIES = "entries";
	public static final int FULL = 0;
	public static final int EMPTY = 1;
	
	public static KeyExchangeLoader runThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_key_exchange_manager);
		// Show the Up button in the action bar.
		//setupActionBar();
		
		runThread = new KeyExchangeLoader(this, handler);
	}

	/**
	 * The onClick action for when the exchange key message is pressed. Sends a
	 * key exchange message for each contact that is selected.
	 * @param view The View 
	 */
	public void exchangeKey(View view)
	{
		if(runThread.getEntries() != null)
		{
			ListView list = (ListView)this.findViewById(R.id.key_exchange_list);
			SparseBooleanArray sba = list.getCheckedItemPositions();
			int i = 0;
			
			while(runThread.getEntries() != null && i < runThread.getEntries().size())
			{
				if(sba.get(i))
				{
					
					TrustedContact tc = MessageService.dba.getRow(SMSUtility.
							format(runThread.getEntries().get(0).getNumber()));
					
					final Number number = tc.getNumber(SMSUtility.
							format(runThread.getEntries().get(0).getNumber()));
					
					if(SMSUtility.checksharedSecret(number.getSharedInfo1()) &&
							SMSUtility.checksharedSecret(number.getSharedInfo2()))
					{
						respondMessage(this, number, runThread.getEntries().get(i));
					}
					else
					{
						setAndSend(this, number, tc.getName(), runThread.getEntries().get(i));
					}
				}
				
				i++;
			}
		}		
	}
	
	/**
	 * Set the shared secrets for the contacts.
	 * @param context The context of the setting.
	 * @param number The Number of the contact.
	 * @param name The name of the contact
	 * @param entry The key exchange message.
	 */
	public static void setAndSend(final Context context, final Number number, String name, final Entry entry)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		final EditText sharedSecret1 = new EditText(context);
		sharedSecret1.setHint(context.getString(R.string.shared_secret_hint_1));
		sharedSecret1.setMaxLines(EditNumber.SHARED_INFO_MAX);
		sharedSecret1.setInputType(InputType.TYPE_CLASS_TEXT);
		linearLayout.addView(sharedSecret1);

		final EditText sharedSecret2 = new EditText(context);
		sharedSecret2.setHint(context.getString(R.string.shared_secret_hint_2));
		sharedSecret2.setMaxLines(EditNumber.SHARED_INFO_MAX);
		sharedSecret2.setInputType(InputType.TYPE_CLASS_TEXT);
		linearLayout.addView(sharedSecret2);
		
		builder.setMessage(context.getString(R.string.set_shared_secrets)
				+ " " + name + ", " + number.getNumber())
		   .setTitle(R.string.set_shared_secrets_title)
	       .setCancelable(true)
	       .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
	    	   @Override
	    	   public void onClick(DialogInterface dialog, int id) {
	               //Save the shared secrets
	    		   String s1 = sharedSecret1.getText().toString();
	    		   String s2 = sharedSecret2.getText().toString();
	    		   if(SMSUtility.checksharedSecret(s1) &&
							SMSUtility.checksharedSecret(s2))
	    		   {
	    			   number.setSharedInfo1(s1);
	    			   number.setSharedInfo2(s2);
	    			   MessageService.dba.updateNumberRow(number, number.getNumber(), number.getId());

		               respondMessage(context, number, entry);
	    		   }
	    		   else
	    		   {
	    			   Toast.makeText(context, R.string.invalid_secrets, Toast.LENGTH_LONG).show();
	    		   }
	           }})
	       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    	   @Override
	    	   public void onClick(DialogInterface arg0, int arg1) {
	    		   	//Cancel the key exchange
	    		   Toast.makeText(context, R.string.key_exchange_cancelled, Toast.LENGTH_LONG).show();
	    	   }});
		AlertDialog alert = builder.create();
		
		alert.setView(linearLayout);
		alert.show();
	}
	
	/**
	 * Respond to the key exchange messages 
	 * @param context The context of the activity
	 * @param number The number of the contact for the key exchange
	 * @param entry The key exchange entry received.
	 */
	public static void respondMessage(final Context context, final Number number, final Entry entry)
	{
		// Handles the key exchange received.
		new KeyExchangeHandler(context, number, entry.getMessage(), true){
			
			@Override
			public void accept(){
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
							KeyExchange.sign(number, MessageService.dba,
							SMSUtility.user), true);
				}
				
				if(MessageService.dba.getKeyExchangeMessageCount() == 0)
			    {
					MessageService.mNotificationManager.cancel(MessageService.KEY);
			    }
				if (runThread != null && runThread.getEntries() != null)
				{
					runThread.getEntries().remove(entry);
				}
				super.accept();
			}

			@Override
			public void invalid(){
				Log.v("Key Exchange", "Invalid key exchange");
				 
				 String name = MessageService.dba.getRow(number.getNumber()).getName();
				 
				 final String text = name + ", " + number.getNumber();
				 
				 AlertDialog.Builder builder = new AlertDialog.Builder(context);
				 
				 String message = context.getString
						 (R.string.key_exchange_error_message_1) + " " + text + " " + 
						 context.getString(R.string.key_exchange_error_message_2);
				 
				 builder.setMessage(message)
			       .setCancelable(true)
			       .setTitle(R.string.key_exchange_error_title)
			       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			    	   @Override
			    	   public void onClick(DialogInterface dialog, int id) {
			    		  
			           }})
			       .setNegativeButton(R.string.tell_me_more, new DialogInterface.OnClickListener() {
		    	   @Override
		    	   public void onClick(DialogInterface arg0, int arg1) {
	    		   	
		    		   String url = context.getString(R.string.key_exchange_info_url);
		    		   Intent i = new Intent(Intent.ACTION_VIEW);
		    		   i.setData(Uri.parse(url));
		    		   context.startActivity(i);
		    	   }});
				AlertDialog alert = builder.create();
				
				alert.show();
				
				super.invalid();
			}
			
			@Override
			public void cancel(){
				super.cancel();
			}

			public void finishWith() {
				updateList();
			}
		
		};		
	}
	
	/**
	 * The onClick action for when a for when the reject button has been
	 * clicked. Deletes every key exchange message selected.
	 * @param view The View
	 */
	public void reject(View view)
	{
		if(runThread.getEntries() != null)
		{
			ListView list = (ListView)this.findViewById(R.id.key_exchange_list);
			SparseBooleanArray sba = list.getCheckedItemPositions();
			
			for (int i = 0; i < runThread.getEntries().size(); i++)
			{
				if(sba.get(i))
				{
					MessageService.dba.deleteKeyExchangeMessage(runThread.getEntries().get(i).getNumber());
				
					runThread.getEntries().remove(runThread.getEntries().get(i));
					
				}				
			}
			//finish();
		}
		updateList();
	}
	
	/**
	 * Update the list key exchange messages 
	 */
	public static void updateList()
	{
		if (runThread != null)
		{
			runThread.setStart(false);
			
			if(runThread.getEntries() == null || runThread.getEntries().size() == 0)
			{
				MessageService.mNotificationManager.cancel(MessageService.KEY);
			}
		}	
	}

    @Override
    protected void onDestroy()
    {
	    runThread.setRunner(false);
	    super.onDestroy();
	}
	
	private final Handler handler = new Handler() {
		//@SuppressWarnings("unchecked")
		@Override
        public void handleMessage(final android.os.Message msg)
        {
			Button accept = (Button)KeyExchangeManager.this.findViewById(R.id.accept);
			Button reject = (Button)KeyExchangeManager.this.findViewById(R.id.reject);
			
        	Bundle b = msg.getData();
        	ListView list = null;
        	switch (msg.what){
        	case FULL:
        		
        		accept.setEnabled(true);
        		reject.setEnabled(true);
        		list = (ListView)KeyExchangeManager.this.findViewById(R.id.key_exchange_list);
	    		adapter = new ArrayAdapter<String>(KeyExchangeManager.this, 
	    				android.R.layout.simple_list_item_multiple_choice, (String[]) b.get(KeyExchangeManager.COMPLETE));
	    		//a.setNotifyOnChange(true);
	    		
	    		list.setAdapter(adapter);
	    		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	    		break;
        	case EMPTY:
        		
        		accept.setEnabled(false);
        		reject.setEnabled(false);
        		list = (ListView)KeyExchangeManager.this.findViewById(R.id.key_exchange_list);

	    		adapter = new ArrayAdapter<String>(KeyExchangeManager.this, 
	    				android.R.layout.simple_list_item_1, new String[]
	    				{KeyExchangeManager.this.getString
	    				(R.string.empty_key_exchange_list)});
	    		//a.setNotifyOnChange(true);
	    		
	    		list.setAdapter(adapter);
	    		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        		break;
        	}
        }
    };

}
