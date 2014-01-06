package com.tinfoil.sms.sms;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import com.tinfoil.sms.R;
import com.tinfoil.sms.crypto.ExchangeKey;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.SMSUtility;

public class NewKeyExchange extends Activity {
	
    private AutoCompleteTextView phoneBox;
	private ArrayList<TrustedContact> tc;
	private DBAccessor dba;
	private TrustedContact newCont;
	
	private static ExchangeKey keyThread = new ExchangeKey();	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_message);
		
		setupInterface();
		
		dba = new DBAccessor(this);
		
		tc = dba.getAllRows(DBAccessor.ALL);
		
		newCont = new TrustedContact();
		
		phoneBox = (AutoCompleteTextView) this.findViewById(R.id.new_message_number);
		
		List<String> contact;
        if (this.tc != null)
        {
            contact = SMSUtility.contactDisplayMaker(this.tc);
        }
        else
        {
            contact = null;
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.auto_complete_list_item, contact);
        
        phoneBox.setAdapter(adapter);
        
        this.phoneBox.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(final Editable s) {

                final String[] info = s.toString().split(", ");

                if (!info[0].trim().equals(""))
                {
                    if (info.length > 1 && !info[0].trim().equalsIgnoreCase(s.toString()))
                    {
                        NewKeyExchange.this.newCont.setName(info[0].trim());
                        NewKeyExchange.this.newCont.setNumber(info[1].trim());
                    }
                    else
                    {
                        if (SMSUtility.isANumber(info[0].trim()))
                        {
                            if (NewKeyExchange.this.newCont.isNumbersEmpty())
                            {
                            	NewKeyExchange.this.newCont.addNumber(info[0].trim());
                            }
                            else
                            {
                            	NewKeyExchange.this.newCont.setNumber(info[0].trim());
                            }
                        }
                    }
                }
            }

            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            }
        });
		
		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	public void setupInterface() 
	{
		LinearLayout et = (LinearLayout)findViewById(R.id.new_message_field);
		
		et.setVisibility(LinearLayout.INVISIBLE);
		
		LinearLayout layout = (LinearLayout)findViewById(R.id.key_exchange_field);
		layout.setVisibility(LinearLayout.VISIBLE);
		
		//Button exchange = (Button)findViewById(R.id.key_exchange);
	}

	public void sendKeyExchange(View view)
	{
		
		String[] temp = SendMessageActivity.checkValidNumber(this, newCont, null, false, true);
		
		if(temp != null)
		{
			SMSUtility.handleKeyExchange(keyThread, dba, this, temp[0]);

			//TODO Give user feedback.
			//Toast.makeText(this, R.string.key_exchange_sent, Toast.LENGTH_SHORT).show();
			
			finish();
		}
		else
		{
			//TODO Handle bad number
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
		}
		return super.onOptionsItemSelected(item);
	}

}
