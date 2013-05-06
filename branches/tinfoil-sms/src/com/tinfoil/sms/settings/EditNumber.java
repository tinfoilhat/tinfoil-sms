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

import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.MessageService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

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
	private TrustedContact tc;
	private String originalNumber;
	private static int position;
	
	@Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.edit_number);
        
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        phoneNumber = (EditText)findViewById(R.id.phone_number);
        sharedInfo1 = (EditText)findViewById(R.id.shared_secret_1);
        sharedInfo2 = (EditText)findViewById(R.id.shared_secret_2);
        bookPath = (EditText)findViewById(R.id.book_path);
        bookInverse = (EditText)findViewById(R.id.book_inverse);
        
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
        	tc = MessageService.dba.getRow(originalNumber);
        
	        phoneNumber.setText(originalNumber);        
	        
	        sharedInfo1.setText(tc.getNumber(originalNumber).getSharedInfo1());
	        
	        sharedInfo2.setText(tc.getNumber(originalNumber).getSharedInfo2());
	        
	        bookPath.setText(tc.getNumber(originalNumber).getBookPath());
	        
	        bookInverse.setText(tc.getNumber(originalNumber).getBookInversePath());
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
			Number tempNumber;
			
			if(tc == null)
			{
				/* Number is a new number */						
				if(originalNumber != null)
				{
					/* 
					 * The contact is not new and has another number.
					 * Get the contact from the database
					 */
					tc = MessageService.dba.getRow(originalNumber);
				}
				tempNumber = new Number(phoneNumber.getText().toString());
			}
			else
			{
				/*
				 * Editing the current number. Get the Number row
				 * for the previous number.
				 */
				tempNumber = tc.getNumber(originalNumber);
				tempNumber.setNumber(phoneNumber.getText().toString());
			}
			
			/*
			 * Set the updated information
			 */
			tempNumber.setSharedInfo1(sharedInfo1.getText().toString());
			tempNumber.setSharedInfo2(sharedInfo2.getText().toString());
			tempNumber.setBookPath(bookPath.getText().toString());
			tempNumber.setBookInversePath(bookInverse.getText().toString());
			
			/*
			 * Update/Add the number to the database
			 */
			if(originalNumber != null)
			{
				if(position == AddContact.NEW_NUMBER_CODE)
				{
					tc.addNumber(tempNumber);
					MessageService.dba.updateRow(tc, originalNumber);
				}
				else
				{
					MessageService.dba.updateNumberRow(tempNumber, originalNumber, 0);
				}
			}
			else
			{
				TrustedContact tc = new TrustedContact();
				tc.addNumber(tempNumber);
				MessageService.dba.addRow(tc);
			}
			
			
			Intent data = new Intent();
			
			/*
			 * Return intent with given parameters 
			 */
			if(originalNumber != null && tempNumber.getNumber().equalsIgnoreCase(originalNumber))
			{					
				data.putExtra(EditNumber.UPDATE, false);					
			}
			else
			{
				data.putExtra(EditNumber.UPDATE, true);
			}
			
			data.putExtra(EditNumber.NUMBER, tempNumber.getNumber());
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
    }
    
    //TODO add delete number menu option 
}
