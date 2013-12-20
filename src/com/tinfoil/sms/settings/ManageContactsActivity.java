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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;

import com.tinfoil.sms.R;
import com.tinfoil.sms.adapter.ManageContactAdapter;
import com.tinfoil.sms.crypto.ExchangeKey;
import com.tinfoil.sms.dataStructures.ContactChild;
import com.tinfoil.sms.dataStructures.ContactParent;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.loader.OnFinishedTaskListener;
import com.tinfoil.sms.sms.ConversationView;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.OnKeyExchangeResolvedListener;

/**
 * ManageContactActivity is an activity that allows the user to exchange keys,
 * edit and delete contacts. A list of contacts will be shown with an check box,
 * if check then the user is either exchanging or have exchanged keys with the
 * contact. To edit a contact's information hold down for a long press. A
 * contact can be added by click 'Add Contact' in the menu. Contacts can
 * be deleted from tinfoil-sms's database by clicking 'Delete Contact' in the
 * menu which will start RemoveContactActivity.
 */
public class ManageContactsActivity extends Activity implements OnFinishedTaskListener, OnKeyExchangeResolvedListener {

	public static final int ADDED_SUCCESS = 100;
	public static final int DELETE_SUCCESS = 101;
	
	public static final int UPDATE = 1;
	
	public static final int POP = 1;
	public static final int EMPTY = 2;	
	
    private ExpandableListView extendableList;
    private ListView listView;
    public static ArrayList<TrustedContact> tc;
    private ProgressDialog dialog;
    public static ArrayAdapter<String> arrayAp;

    public static ArrayList<ContactParent> contacts;
    public static ArrayList<ContactChild> contactNumbers;
    public static ManageContactAdapter adapter;
    
    public static boolean exchange = true;
    
    private static ManageContactsLoader runThread;

    private static ExchangeKey keyThread = new ExchangeKey();

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        exchange = this.getIntent().getExtras().getBoolean(TabSelection.EXCHANGE, true);
       
        this.setContentView(R.layout.contact);
        
        ConversationView.boot.setOnKeyExchangeResolvedListener(this);
        
        if (!exchange)
        {
        	((Button)this.findViewById(R.id.exchange_keys)).setText(R.string.untrust_button_name);
        }
        
        ManageContactsActivity.this.startThread();
        
        this.extendableList = (ExpandableListView) this.findViewById(R.id.contacts_list);
        this.listView = (ListView) this.findViewById(R.id.empty_list);

        this.extendableList.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {

            	/*
            	 * Starts the AddContact activity to edit the contact that was
            	 * selected.
            	 */
            	if (ExpandableListView.getPackedPositionType(id) ==
            			ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                   
                	
                    AddContact.addContact = false;
                    AddContact.editTc = MessageService.dba.getRow(ManageContactsActivity.tc.get(
                    		ExpandableListView.getPackedPositionGroup(id)).getANumber());

                    Intent intent = new Intent(ManageContactsActivity.this,
                    		AddContact.class);
                    
                    ManageContactsActivity.this.startActivityForResult(intent,
                    		AddContact.UPDATED_NUMBER);
                    
                    /*
                     * This stops other on click effects from happening after
                     * this one.
                     */
                    return true;
                }

                //Child long clicked
                return false;
            }
        });

        /*
         * When a contact's number is clicked
         */
        this.extendableList.setOnChildClickListener(new OnChildClickListener() {

            public boolean onChildClick(final ExpandableListView parent, final View v,
                    final int groupPosition, final int childPosition, final long id) {

                final CheckedTextView checked_text = (CheckedTextView) 
                		v.findViewById(R.id.trust_name);

                adapter.getContacts().get(groupPosition)
                	.getNumber(childPosition).toggle();

                checked_text.setChecked(adapter.getContacts().get(groupPosition)
                		.getNumber(childPosition).isSelected());
                
                return true;
            }
        });
    }

    /**
     * The onClick action for when the user clicks on keyExchange
     * @param view The view that is involved
     */
    public void keyExchange(View view)
    {
    	
    	/*
         * Launch Exchange Keys thread.
         */
    	/*dialog = ProgressDialog.show(ManageContactsActivity.this,
        		"Exchanging Keys", "Exchanging. Please wait...", true, false);*/

    	keyThread.setOnFinishedTaskListener(this);
        keyThread.startThread(this, adapter.getContacts());

        /*ExchangeKey.keyDialog.setOnDismissListener(new OnDismissListener() {

            public void onDismiss(final DialogInterface dialog) {
            	updateList();
            }
        });*/
    }

    @Override
	public void onKeyExchangeResolved() {
		updateList();
	}
    
	@Override
	public void onFinishedTaskListener() {
		update();
	}
    
    /**
     * Updates the list of contacts
     */
    private void update()
    {
        if (tc != null)
        {
            this.extendableList.setAdapter(adapter);
            this.listView.setVisibility(ListView.INVISIBLE);
            this.extendableList.setVisibility(ListView.VISIBLE);
        }
        else
        {
            this.listView.setAdapter(arrayAp);
            this.extendableList.setVisibility(ListView.INVISIBLE);
            this.listView.setVisibility(ListView.VISIBLE);
        }

        if (tc != null)
        {
            this.extendableList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        }
    }
    
    protected void onResume()
    {
    	super.onResume();
    	updateList();
    }

    /**
     * Start the thread for loading the contacts.
     */
    private void startThread()
    {
        runThread = new ManageContactsLoader();
        runThread.setOnFinshedTaskListener(this);
        runThread.execute(this);
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
    	runThread = null;
    	//runThread.setRunner(false);
    	super.onDestroy();
	}
    
    
    /**
     * showProgress Displays the progress dialog.
     * 
     * @param show Shows the progress dialog if true. If false, don't show it.
     */
    public void showProgress(boolean show)
    {
        if (show)
        {
            dialog = ProgressDialog.show(this, getString(R.string.loading_dialog_header),
                    getString(R.string.loading_dialog_body), true, true,
                    new OnCancelListener()
                    {
                        public void onCancel(DialogInterface dialog)
                        {

                        }
                    });
        }
        else
        {
            dialog.dismiss();
        }

    }
    
    /**
     * Update the list of contacts
     */
    public void updateList()
    {
        if(runThread != null)
        {
        	Log.v("Run Thread", "Running");
        	
        	//runThread.setStart(false);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	super.onActivityResult(requestCode, resultCode, data);

    	if(resultCode != Activity.RESULT_CANCELED)
    	{
	    	if(requestCode == AddContact.UPDATED_NUMBER || requestCode == AddContact.NEW_NUMBER_CODE)
	    	{
	    		updateList();
	    	}
	    	else if (requestCode == DELETE_SUCCESS)
	    	{
	    		updateList();
	    	}
    	}
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.manage_contacts_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add: {
                AddContact.addContact = true;
                AddContact.editTc = null;
                //TODO make activity handle request code
                this.startActivityForResult(new Intent(this, AddContact.class), AddContact.NEW_NUMBER_CODE);
                return true;
            }
            case R.id.delete: {
                //TODO make activity handle request code
                this.startActivityForResult(new Intent(this.getApplicationContext(),
                		RemoveContactsActivity.class), DELETE_SUCCESS);

                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}