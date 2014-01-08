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

package com.tinfoil.sms.settings;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.TrustedContact;

/**
 * RemoveContactActivity is an activity that allows for contacts to be deleted
 * from tinfoil-sms's database. ***Please note that contacts will not be deleted
 * from the native database.
 */
public class RemoveContactsActivity extends Activity {
    private ListView listView;
    private boolean[] contact;
    private ArrayList<TrustedContact> tc;
    //private ProgressDialog dialog;
    //private boolean clicked = false;
    private ArrayAdapter<String> appAdapt;
    private boolean empty = false;
    private RemoveContactsLoader runThread;
    
    public static final String NAMES = "names";
    public static final String CONTACTS = "contacts";
    public static final String TRUSTED = "trusted_contacts";
    public static final int UPDATE = 0;
    public static final int EMPTY = 1;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.remove_contacts);
        //this.clicked = false;
        
        setupActionBar();

        this.listView = (ListView) this.findViewById(R.id.removeable_contact_list);

        /*this.dialog = ProgressDialog.show(this, "Loading Contacts",
                "Loading. Please wait...", true, false);
		*/
        //update();
        runThread = new RemoveContactsLoader(this, false, contact, tc, handler);

        //Create what happens when you click on a button
        this.listView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {

            	if(!empty)
            	{
                    RemoveContactsActivity.this.toggle(position);
            	}
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
            //RemoveContactsActivity.this.clicked = true;
            /*RemoveContactsActivity.this.dialog = ProgressDialog.show(RemoveContactsActivity.this, "Deleting Contacts",
                    "Deleting. Please wait...", true, false);
            */
            
            runThread.setClicked(true);
            runThread.setStart(false);
            //thread2.start();

            //update();
        }
    }    
    
    @Override
    protected void onDestroy()
    {   
    	runThread.setRunner(false);
    	super.onDestroy();
    }

    /**
     * Toggle the contact's status from to be deleted to not be deleted or from
     * not be deleted to be deleted
     * 
     * @param i The contact that is selected
     */
    public void toggle(final int i)
    {
    	if (this.contact.length > 0)
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
    
    /**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

    
    /**
     * The handler class for cleaning up the loading thread
     */
    private final Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
		@Override
        public void handleMessage(final Message msg)
        {
        	Bundle b = msg.getData();
        	tc = (ArrayList<TrustedContact>) b.getSerializable(RemoveContactsActivity.TRUSTED);
        	contact = b.getBooleanArray(RemoveContactsActivity.CONTACTS);
        	String[] names = b.getStringArray(RemoveContactsActivity.NAMES);
        	
        	Button delete = (Button)RemoveContactsActivity.this.findViewById(R.id.delete_cont);
        	
        	switch(msg.what){
        	case EMPTY:
        		RemoveContactsActivity.this.appAdapt = new ArrayAdapter<String>
        			(RemoveContactsActivity.this, android.R.layout.simple_list_item_1, names);
        		delete.setEnabled(false);
            	empty = true;
        		break;
        	case UPDATE:
        		RemoveContactsActivity.this.appAdapt = new ArrayAdapter<String>
        			(RemoveContactsActivity.this, android.R.layout.simple_list_item_multiple_choice, names);
    			delete.setEnabled(true);
            	RemoveContactsActivity.this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            	empty = false;
        		break;
        	}
            RemoveContactsActivity.this.listView.setAdapter(RemoveContactsActivity.this.appAdapt);
            
            RemoveContactsActivity.this.listView.setItemsCanFocus(false);

            //RemoveContactsActivity.this.dialog.dismiss();
        }
    };

}
