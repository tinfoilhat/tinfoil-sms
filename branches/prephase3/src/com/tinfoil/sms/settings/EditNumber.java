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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditNumber extends Activity{
    
	public static final String UPDATE = "update";
	public static final String NEW = "new_number";
	
	private EditText phoneNumber;
	private EditText sharedInfo1;
	private EditText sharedInfo2;
	private EditText bookPath;
	private EditText bookInverse;
	private Button save;
	private TrustedContact tc;
	private String originalNumber;
	private static int position;
	
	@Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.edit_number);
        
        
        /*TODO consider how to handle the saving aspect
         * - have multiple save buttons (current)
         * - have a single save button (in addContact) and have this as temp passing back the object (not as ideal)
         */
       
        phoneNumber = (EditText)findViewById(R.id.phone_number);
        sharedInfo1 = (EditText)findViewById(R.id.shared_secret_1);
        sharedInfo2 = (EditText)findViewById(R.id.shared_secret_2);
        bookPath = (EditText)findViewById(R.id.book_path);
        bookInverse = (EditText)findViewById(R.id.book_inverse);
        save = (Button)findViewById(R.id.save);
        
        Intent intent = this.getIntent();
        
        if(intent != null)
        {
        	originalNumber = intent.getStringExtra(AddContact.EDIT_NUMBER);
        	position = intent.getIntExtra(AddContact.POSITION, AddContact.NEW_NUMBER_CODE);
            this.getIntent().removeExtra(AddContact.EDIT_NUMBER);
            this.getIntent().removeExtra(AddContact.POSITION);
        }
        else
        {
        	finish();
        }
        
        if(position != AddContact.NEW_NUMBER_CODE)
        {
        	tc = MessageService.dba.getRow(originalNumber);
        
	        phoneNumber.setText(originalNumber);        
	        
	        sharedInfo1.setText(tc.getNumber(originalNumber).getSharedInfo1());
	        
	        sharedInfo2.setText(tc.getNumber(originalNumber).getSharedInfo2());
	        
	        bookPath.setText(tc.getNumber(originalNumber).getBookPath());
	        
	        bookInverse.setText(tc.getNumber(originalNumber).getBookInversePath());
        }
        else
        {
        	tc = new TrustedContact();
        	//phoneNumber.setText();        
	        
	        sharedInfo1.setText(DBAccessor.DEFAULT_S1);
	        
	        sharedInfo2.setText(DBAccessor.DEFAULT_S2);
	        
	        bookPath.setText(DBAccessor.DEFAULT_BOOK_PATH);
	        
	        bookInverse.setText(DBAccessor.DEFAULT_BOOK_INVERSE_PATH);
        }
        
        save.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				
				if(phoneNumber.getText().toString() != null &&
						phoneNumber.getText().toString().length() > 0)
				{
					Number tempNumber = tc.getNumber(originalNumber);
					tempNumber.setNumber(phoneNumber.getText().toString());
					tempNumber.setSharedInfo1(sharedInfo1.getText().toString());
					tempNumber.setSharedInfo2(sharedInfo2.getText().toString());
					tempNumber.setBookPath(bookPath.getText().toString());
					tempNumber.setBookInversePath(bookInverse.getText().toString());
					
					MessageService.dba.updateNumberRow(tempNumber, originalNumber, 0);
					
					Intent data = new Intent();
					
					if(tempNumber.getNumber().equalsIgnoreCase(originalNumber))
					{					
						data.putExtra(EditNumber.UPDATE, false);					
					}
					else
					{
						data.putExtra(EditNumber.UPDATE, true);
					}
					
					data.putExtra(EditNumber.NEW, tempNumber.getNumber());
					data.putExtra(AddContact.POSITION, position);
					
					EditNumber.this.setResult(AddContact.REQUEST_CODE, data);
	
					EditNumber.this.finish();
				}
			}
        });
	}
}
