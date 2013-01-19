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
import com.tinfoil.sms.utility.MessageService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditNumber extends Activity{
    
	private EditText phoneNumber;
	private EditText sharedInfo1;
	private EditText sharedInfo2;
	private EditText bookPath;
	private EditText bookInverse;
	private Button save;
	private TrustedContact tc;
	private String originalNumber;
	
	@Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.edit_number);
        
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
            this.getIntent().removeExtra(AddContact.EDIT_NUMBER);
        }
        
        
        phoneNumber.setText(originalNumber);
        
        tc = MessageService.dba.getRow(originalNumber);
        
        sharedInfo1.setText(tc.getNumber(originalNumber).getSharedInfo1());
        
        sharedInfo2.setText(tc.getNumber(originalNumber).getSharedInfo2());
        
        bookPath.setText(tc.getNumber(originalNumber).getBookPath());
        
        bookInverse.setText(tc.getNumber(originalNumber).getBookInversePath());
        
        save.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				Number tempNumber = tc.getNumber(originalNumber);
				
			}
        });
	}
}
