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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.MessageService;

public class EditNumber extends Activity {

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

        /* TODO comment
         * TODO consider how to handle the saving aspect
         * - have multiple save buttons (current)
         * - have a single save button (in addContact) and have this as temp passing back the object (not as ideal)
         */

        this.phoneNumber = (EditText) this.findViewById(R.id.phone_number);
        this.sharedInfo1 = (EditText) this.findViewById(R.id.shared_secret_1);
        this.sharedInfo2 = (EditText) this.findViewById(R.id.shared_secret_2);
        this.bookPath = (EditText) this.findViewById(R.id.book_path);
        this.bookInverse = (EditText) this.findViewById(R.id.book_inverse);
        this.save = (Button) this.findViewById(R.id.save);

        final Intent intent = this.getIntent();

        if (intent != null)
        {
            this.originalNumber = intent.getStringExtra(AddContact.EDIT_NUMBER);
            position = intent.getIntExtra(AddContact.POSITION, AddContact.NEW_NUMBER_CODE);
            this.getIntent().removeExtra(AddContact.EDIT_NUMBER);
            this.getIntent().removeExtra(AddContact.POSITION);
        }
        else
        {
            this.finish();
        }

        if (position != AddContact.NEW_NUMBER_CODE)
        {
            this.tc = MessageService.dba.getRow(this.originalNumber);

            this.phoneNumber.setText(this.originalNumber);

            this.sharedInfo1.setText(this.tc.getNumber(this.originalNumber).getSharedInfo1());

            this.sharedInfo2.setText(this.tc.getNumber(this.originalNumber).getSharedInfo2());

            this.bookPath.setText(this.tc.getNumber(this.originalNumber).getBookPath());

            this.bookInverse.setText(this.tc.getNumber(this.originalNumber).getBookInversePath());
        }
        else
        {
            this.tc = new TrustedContact();

            this.sharedInfo1.setText(DBAccessor.DEFAULT_S1);

            this.sharedInfo2.setText(DBAccessor.DEFAULT_S2);

            this.bookPath.setText(DBAccessor.DEFAULT_BOOK_PATH);

            this.bookInverse.setText(DBAccessor.DEFAULT_BOOK_INVERSE_PATH);
        }

        this.save.setOnClickListener(new OnClickListener() {

            public void onClick(final View v) {

                if (EditNumber.this.phoneNumber.getText().toString() != null &&
                        EditNumber.this.phoneNumber.getText().toString().length() > 0)
                {
                    final Number tempNumber = EditNumber.this.tc.getNumber(EditNumber.this.originalNumber);
                    tempNumber.setNumber(EditNumber.this.phoneNumber.getText().toString());
                    tempNumber.setSharedInfo1(EditNumber.this.sharedInfo1.getText().toString());
                    tempNumber.setSharedInfo2(EditNumber.this.sharedInfo2.getText().toString());
                    tempNumber.setBookPath(EditNumber.this.bookPath.getText().toString());
                    tempNumber.setBookInversePath(EditNumber.this.bookInverse.getText().toString());

                    MessageService.dba.updateNumberRow(tempNumber, EditNumber.this.originalNumber, 0);

                    final Intent data = new Intent();

                    if (tempNumber.getNumber().equalsIgnoreCase(EditNumber.this.originalNumber))
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
