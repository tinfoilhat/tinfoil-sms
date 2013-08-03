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
import android.os.Bundle;
import android.os.Handler;
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
		
		runThread = new KeyExchangeLoader(handler);
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
			
			for (int i = 0; i < runThread.getEntries().size(); i++)
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
						respondMessage(number, runThread.getEntries().get(i));
						//entries.remove(entries.get(i));
						//updateList();
					}
					else
					{
						setAndSend(this, number, tc.getName(), runThread.getEntries().get(i));
					}
					runThread.getEntries().remove(runThread.getEntries().get(i));
					
					
				//else Item has not be touched leave it alone.
				}
			}
		}
		updateList();
	}
	
	//TODO make sure notification goes away
	public static void setAndSend(final Context context, final Number number, String name, final Entry entry)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		final EditText sharedSecret1 = new EditText(context);
		sharedSecret1.setHint("Shared Secret 1");
		sharedSecret1.setMaxLines(EditNumber.SHARED_INFO_MAX);
		sharedSecret1.setInputType(InputType.TYPE_CLASS_TEXT);
		linearLayout.addView(sharedSecret1);

		final EditText sharedSecret2 = new EditText(context);
		sharedSecret2.setHint("Shared Secret 2");
		sharedSecret2.setMaxLines(EditNumber.SHARED_INFO_MAX);
		sharedSecret2.setInputType(InputType.TYPE_CLASS_TEXT);
		linearLayout.addView(sharedSecret2);
		
		builder.setMessage("Set the shared secret for " + name + ", " + number.getNumber())
	       .setCancelable(true)
	       .setPositiveButton("Save", new DialogInterface.OnClickListener() {
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

		               respondMessage(number, entry);
	    		   }
	    		   else
	    		   {
	    			   Toast.makeText(context, "Invalid secrets", Toast.LENGTH_LONG).show();
	    		   }
	           }})
	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	   @Override
	    	   public void onClick(DialogInterface arg0, int arg1) {
	    		   	//Cancel the key exchange
	    		   Toast.makeText(context, "Key exchange cancelled", Toast.LENGTH_LONG).show();
	    	   }});
		AlertDialog alert = builder.create();
		
		alert.setView(linearLayout);
		alert.show();
	}
	
	/**
	 * Respond to the key exchange messages 
	 * @param number The number of the contact for the key exchange
	 * @param entry The key exchange entry received.
	 */
	public static void respondMessage(Number number,Entry entry)
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
			
			if(MessageService.dba.getKeyExchangeMessageCount() == 0)
		    {
				MessageService.mNotificationManager.cancel(MessageService.KEY);
		    }
			//a.remove(entries.get(i).getNumber());
		}
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
	 * TODO change the view adapter to better represent the key exchange.
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

	//TODO fix menu item  
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
        	Bundle b = msg.getData();
        	ListView list = null;
        	switch (msg.what){
        	case FULL:
        		list = (ListView)KeyExchangeManager.this.findViewById(R.id.key_exchange_list);
	    		adapter = new ArrayAdapter<String>(KeyExchangeManager.this, 
	    				android.R.layout.simple_list_item_multiple_choice, (String[]) b.get(KeyExchangeManager.COMPLETE));
	    		//a.setNotifyOnChange(true);
	    		
	    		list.setAdapter(adapter);
	    		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	    		break;
        	case EMPTY:
        		list = (ListView)KeyExchangeManager.this.findViewById(R.id.key_exchange_list);
	    		adapter = new ArrayAdapter<String>(KeyExchangeManager.this, 
	    				android.R.layout.simple_list_item_1, new String[]{"Empty List"});
	    		//a.setNotifyOnChange(true);
	    		
	    		list.setAdapter(adapter);
	    		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        		break;
        	}
        }
    };

}
