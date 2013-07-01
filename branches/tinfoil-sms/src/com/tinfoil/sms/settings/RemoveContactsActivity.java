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
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.utility.MessageService;

/**
 * RemoveContactActivity is an activity that allows for contacts to be deleted
 * from tinfoil-sms's database. ***Please note that contacts will not be deleted
 * from the native database.
 */
public class RemoveContactsActivity extends Activity implements Runnable {
    private ListView listView;
    private boolean[] contact;
    private ArrayList<TrustedContact> tc;
    private ProgressDialog dialog;
    private boolean clicked = false;
    private ArrayAdapter<String> appAdapt;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.remove_contacts);
        this.clicked = false;

        this.listView = (ListView) this.findViewById(R.id.removeable_contact_list);

        this.dialog = ProgressDialog.show(this, "Loading Contacts",
                "Loading. Please wait...", true, false);

        //update();
        final Thread thread = new Thread(this);
        thread.start();

        //Create what happens when you click on a button
        this.listView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {

                RemoveContactsActivity.this.toggle(position);

            }
        });
    }
    
    /**
     * The onClick action for when the user clicks the delete selected
     * @param view The view that is involved
     */
    public void deleteSelected(View view)
    {
    	if (RemoveContactsActivity.this.tc != null)
        {
            RemoveContactsActivity.this.clicked = true;
            RemoveContactsActivity.this.dialog = ProgressDialog.show(RemoveContactsActivity.this, "Deleting Contacts",
                    "Deleting. Please wait...", true, false);
            final Thread thread2 = new Thread(RemoveContactsActivity.this);
            thread2.start();

            //update();
        }
    }

    /**
     * Toggle the contact's status from to be deleted to not be deleted or from
     * not be deleted to be deleted
     * 
     * @param i The contact that is selected
     */
    public void toggle(final int i)
    {
        if (!this.contact[i])
        {
            this.contact[i] = true;
        }
        else
        {
            this.contact[i] = false;
        }
    }

    /**
     * Updates the list of contacts
     */
    private void update()
    {
        String[] names;
        this.tc = MessageService.dba.getAllRows(DBAccessor.ALL);

        if (this.tc != null)
        {
            //The string that is displayed for each item on the list 
            names = new String[this.tc.size()];
            for (int i = 0; i < this.tc.size(); i++)
            {
                names[i] = this.tc.get(i).getName();
            }
            this.appAdapt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, names);
        }
        else
        {
            names = new String[] { "No Contacts" };
            this.appAdapt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.remove_contacts_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.all:
                if (this.tc != null)
                {
                    for (int i = 0; i < this.tc.size(); i++)
                    {
                        this.listView.setItemChecked(i, true);
                        this.contact[i] = true;
                    }
                }
                return true;
            case R.id.remove:
                if (this.tc != null)
                {
                    for (int i = 0; i < this.tc.size(); i++)
                    {
                        this.listView.setItemChecked(i, false);
                        this.contact[i] = false;
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void run() {
        if (this.clicked)
        {
            for (int i = 0; i < this.tc.size(); i++)
            {
                if (this.contact[i])
                {
                    MessageService.dba.removeRow(this.tc.get(i).getANumber());
                }
            }
        }

        this.update();
        if (this.tc != null)
        {

            this.contact = new boolean[this.tc.size()];
            for (int i = 0; i < this.tc.size(); i++)
            {
                this.contact[i] = false;
            }
        }
        this.handler.sendEmptyMessage(0);
    }

    /**
     * The handler class for cleaning up the loading thread
     */
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg)
        {
            RemoveContactsActivity.this.listView.setAdapter(RemoveContactsActivity.this.appAdapt);
            if (RemoveContactsActivity.this.tc != null)
            {

                RemoveContactsActivity.this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
            RemoveContactsActivity.this.listView.setItemsCanFocus(false);

            RemoveContactsActivity.this.dialog.dismiss();
        }
    };

}
