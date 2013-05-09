package com.tinfoil.sms.sms;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.Entry;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.encryption.Encryption;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;

@SuppressLint("all")
public class KeyExchangeManager extends Activity {

	private ArrayList<Entry> entries;
	private ArrayList<Integer> checked;
	//private  numbers;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_key_exchange_manager);
		// Show the Up button in the action bar.
		//setupActionBar();
		
		
		entries = MessageService.dba.getAllKeyExchangeMessages();
		
		
		if(entries != null)
		{
			checked = new ArrayList<Integer>(entries.size());
			String[] numbers = new String[entries.size()];
			
			for(int i = 0; i < entries.size(); i++)
			{
				numbers[i] = entries.get(i).getNumber();
			}
			
			ListView list = (ListView)this.findViewById(R.id.key_exchange_list);
			
			ArrayAdapter<String> a = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, numbers);
			list.setAdapter(a);
			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			
			/*list.setOnItemClickListener(new OnItemClickListener(){

				public void onItemClick(final AdapterView<?> parent, final View view,
                final int position, final long id) {
					
					//ListView list = (ListView)KeyExchangeManager.this.findViewById(R.id.key_exchange_list);
					
					
					//checked.add(position);
				}
			});*/
		}
		
	}

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
					Number number = MessageService.dba.getNumber(SMSUtility.format(entries.get(0).getNumber()));
					
					// Will do it off of entries.get(position).getMessage() once keys are implemented
					number.setPublicKey();
					
					MessageService.dba.updateNumberRow(number, number.getNumber(), number.getId());
					
					if(!number.isInitiator())
					{
						MessageService.dba.addMessageToQueue(number.getNumber(),
								new String(Encryption.generateKey()), true);
					}
					//Remove element from list
				}
				//else
					// Item has not be touched leave it alone.
			}
		}
	}
	
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
				}
			}
		}
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	
	@SuppressLint("NewApi")
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
