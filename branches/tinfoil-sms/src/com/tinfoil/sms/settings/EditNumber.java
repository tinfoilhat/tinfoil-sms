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

package com.tinfoil.sms.settings;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.MessageService;

/**
 * An activity used to edit a single number from a given contact. 
 * 
 * This activity returns a AddContact.RESULT_CODE if changes have
 * been made and the list needs to be update, else returns default
 * result code.
 * The Extras are included are:
 * EditNumber.UPDATE, whether the number has been updated or not
 * EditNumber.NUMBER, the number that has been edited
 * AddContact.POSITION, the position of the given number in the
 * contact's numbers array list, -1 if it is a new number
 * EditNumber.ADD, whether the number is to update or deleted
 */
public class EditNumber extends Activity{
    
	public static final String UPDATE = "update";
	public static final String NUMBER = "number";
	public static final String ADD = "add";
	
	private EditText phoneNumber;
	private EditText sharedInfo1;
	private EditText sharedInfo2;
	private EditText bookPath;
	private EditText bookInverse;
	private EditText pubKey;
	private Number number;
	//private TrustedContact tc;
	private String originalNumber;
	private static int position;
	
	private ArrayList<RadioButton> keyExchangeSetting;
	
	@Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.edit_number);
        
        //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        keyExchangeSetting = new ArrayList<RadioButton>();
        
        keyExchangeSetting.add((RadioButton)this.findViewById(R.id.auto_exchange));
        keyExchangeSetting.add((RadioButton)this.findViewById(R.id.manual_exchange));
        keyExchangeSetting.add((RadioButton)this.findViewById(R.id.ignore_exchange));
        
        phoneNumber = (EditText)findViewById(R.id.phone_number);
        sharedInfo1 = (EditText)findViewById(R.id.shared_secret_1);
        sharedInfo2 = (EditText)findViewById(R.id.shared_secret_2);
        bookPath = (EditText)findViewById(R.id.book_path);
        bookInverse = (EditText)findViewById(R.id.book_inverse);
        
        
        pubKey = (EditText)findViewById(R.id.contact_pub_key);
        boolean trusted = false;
        
        Intent intent = this.getIntent();
        
        /* Check if there is intent */
        if(intent != null)
        {
        	/*
        	 * Get the extra values in the intent.
        	 */
        	originalNumber = intent.getStringExtra(AddContact.EDIT_NUMBER);
        	position = intent.getIntExtra(AddContact.POSITION, AddContact.NEW_NUMBER_CODE);   	
            this.getIntent().removeExtra(AddContact.EDIT_NUMBER);
            this.getIntent().removeExtra(AddContact.POSITION);
        }
        else
        {
        	finish();
        }
        
        /*
         * Is the number a new number
         */
        if(position != AddContact.NEW_NUMBER_CODE)
        {
        	/*
        	 * Initialize the values to be adjusted
        	 */
        	number = MessageService.dba.getNumber(originalNumber);
        
	        phoneNumber.setText(originalNumber);        
	        
	        sharedInfo1.setText(number.getSharedInfo1());
	        
	        sharedInfo2.setText(number.getSharedInfo2());
	        
	        bookPath.setText(number.getBookPath());
	        
	        bookInverse.setText(number.getBookInversePath());

	        keyExchangeSetting.get(number.getKeyExchangeFlag()).setChecked(true);
	        
	        if(MessageService.dba.isTrustedContact(originalNumber))
	        {
	        	trusted = true;
	        }
        }
        else
        {
        	/*
        	 * Initialize the values to the default values
        	 */
	        sharedInfo1.setText(DBAccessor.DEFAULT_S1);
	        
	        sharedInfo2.setText(DBAccessor.DEFAULT_S2);
	        
	        bookPath.setText(DBAccessor.DEFAULT_BOOK_PATH);
	        
	        bookInverse.setText(DBAccessor.DEFAULT_BOOK_INVERSE_PATH);
        }
        
        if(trusted)
        {
        	TextView pubKeyTitle = (TextView)findViewById(R.id.contact_pub_key_text);
        	pubKeyTitle.setVisibility(TextView.INVISIBLE);
        	pubKey.setVisibility(EditText.INVISIBLE);
        	
        	// Find the radio button group view
        	View rg = (View)findViewById(R.id.radioGroup1);
        	
        	/*
        	 * Set it so now it is below book_inverse since the other two
        	 * elements are invisible.
        	 */
        	RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        	        ViewGroup.LayoutParams.WRAP_CONTENT);
        	p.addRule(RelativeLayout.BELOW,R.id.book_inverse);
        	rg.setLayoutParams(p);
        }
	}
	
	/**
     * The onClick action for when the user clicks on save information
     * @param view The view that is involved
     */
	public void saveNumberInfo(View view)
	{
		/* Is there a valid number */
		if(phoneNumber.getText().toString() != null &&
				phoneNumber.getText().toString().length() > 0)
		{
			//Number tempNumber;
			TrustedContact tc = null;
			
			if(number == null)
			{
				/* Number is a new number */						
				if(originalNumber != null)
				{
					/* 
					 * The contact is not new and has another number.
					 * Get the contact from the database
					 */
					
					//number = MessageService.dba.getNumber(originalNumber);
				}
				number = new Number(phoneNumber.getText().toString());
			}
			else
			{
				/*
				 * Editing the current number. Get the Number row
				 * for the previous number.
				 */
				
				number.setNumber(phoneNumber.getText().toString());
			}
			
			/*
			 * Set the updated information
			 */
			number.setSharedInfo1(sharedInfo1.getText().toString());
			number.setSharedInfo2(sharedInfo2.getText().toString());
			number.setBookPath(bookPath.getText().toString());
			number.setBookInversePath(bookInverse.getText().toString());
			
			if(pubKey.getVisibility() == EditText.VISIBLE)
			{
				String key = pubKey.getText().toString().trim();
				if(key != "" && key.length() > 0)
				{
					number.setPublicKey(key.getBytes());
				}
			}
			
			int index = 0;
			if(keyExchangeSetting.get(Number.AUTO).isChecked())
			{
				index = Number.AUTO;
			}
			else if(keyExchangeSetting.get(Number.MANUAL).isChecked())
			{
				index = Number.MANUAL;
			}
			else
			{
				index = Number.IGNORE;
			}
			number.setKeyExchangeFlag(index);
			
			/*
			 * Update/Add the number to the database
			 */
			if(originalNumber != null)
			{
				if(position == AddContact.NEW_NUMBER_CODE)
				{
					tc = MessageService.dba.getRow(originalNumber);
					tc.addNumber(number);
					MessageService.dba.updateRow(tc, originalNumber);
				}
				else
				{
					MessageService.dba.updateNumberRow(number, originalNumber, 0);
				}
			}
			else
			{
				tc = new TrustedContact();
				tc.addNumber(number);
				MessageService.dba.addRow(tc);
			}
			
			
			Intent data = new Intent();
			
			/*
			 * Return intent with given parameters 
			 */
			if(originalNumber != null && number.getNumber().equalsIgnoreCase(originalNumber))
			{					
				data.putExtra(EditNumber.UPDATE, false);					
			}
			else
			{
				data.putExtra(EditNumber.UPDATE, true);
			}
			
			data.putExtra(EditNumber.NUMBER, number.getNumber());
			data.putExtra(AddContact.POSITION, position);
			data.putExtra(EditNumber.ADD, true);
			
			/*
			 * Set result code to identify that whether the list in
			 * ManageContactsActivity.
			 */
			EditNumber.this.setResult(AddContact.REQUEST_CODE, data);

			EditNumber.this.finish();
		}
	}

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        return super.onCreateOptionsMenu(menu);
        
        //TODO add import public key to the menu if the contact does not have a key.
    }
    
}
