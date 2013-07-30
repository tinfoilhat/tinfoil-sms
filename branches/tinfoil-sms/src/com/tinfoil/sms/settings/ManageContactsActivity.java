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
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.tinfoil.sms.utility.MessageService;

/**
 * TODO update when received key exchange.
 * Currently the onClick action is very confusing for people new to the app.
 * Even though items are selected depending on the state of the contact the
 * onClick action will be different. If a contact's number is trusted (has the
 * Tinfoil-sms icon next to their number) then the contact clicking the button
 * will delete the contact's key and set the to untrusted. Where as if the
 * contact's number is not trusted (there is no tinfoil-sms icon next to the
 * number) then clicking the button will initiate a key exchange with that
 * contact.
 * ManageContactActivity is an activity that allows the user to exchange keys,
 * edit and delete contacts. A list of contacts will be shown with an check box,
 * if check then the user is either exchanging or have exchanged keys with the
 * contact. To edit a contact's information hold down for a long press, which
 * will start AddContact activity with addContact == false and editTc != null. A
 * contact can be added by click 'Add Contact' in the menu this will start the
 * AddContact activity with addContact == true and editTc == null. Contacts can
 * be deleted from tinfoil-sms's database by clicking 'Delete Contact' in the
 * menu which will start RemoveContactActivity.
 */
public class ManageContactsActivity extends Activity {

	public static final int UPDATE = 1;
	
	public static final int POP = 1;
	public static final int EMPTY = 2;	
	
    private ExpandableListView extendableList;
    private ListView listView;
    public static ArrayList<TrustedContact> tc;
    private ProgressDialog loadingDialog;
    private ArrayAdapter<String> arrayAp;
    //

    public static ArrayList<ContactParent> contacts;
    public static ArrayList<ContactChild> contactNumbers;
    private static ManageContactAdapter adapter;
    
    public boolean exchange = true;
    
    private static ManageContactsLoader runThread;

    private static ExchangeKey keyThread = new ExchangeKey();

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        exchange = this.getIntent().getExtras().getBoolean(TabSelection.EXCHANGE, true);
       
        //runThread = new ManageContactsLoader();
        this.setContentView(R.layout.contact);
        
        if (!exchange)
        {
        	((Button)this.findViewById(R.id.exchange_keys)).setText("Untrust");
        }
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
                    		UPDATE);
                    
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
     * TODO disable when no contact is selected
     * The onClick action for when the user clicks on keyExchange
     * @param view The view that is involved
     */
    public void keyExchange(View view)
    {
    	/*
         * Launch Exchange Keys thread.
         */
        ExchangeKey.keyDialog = ProgressDialog.show(ManageContactsActivity.this,
        		"Exchanging Keys", "Exchanging. Please wait...", true, false);

        keyThread.startThread(this, adapter.getContacts());

        ExchangeKey.keyDialog.setOnDismissListener(new OnDismissListener() {

            public void onDismiss(final DialogInterface dialog) {
                ManageContactsActivity.this.startThread();
            }
        });
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
            this.listView.setAdapter(this.arrayAp);
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
    	this.startThread();
    }

    /**
     * Start the thread for loading the contacts.
     */
    private void startThread()
    {
        //TODO Override dialog to make so if BACK is pressed it exits the activity if it hasn't finished loading
        this.loadingDialog = ProgressDialog.show(this, "Loading Contacts",
                "Loading. Please wait...", true, false);
        runThread = new ManageContactsLoader();
        //final Thread thread = new Thread(runThread);
        
        runThread.startThread(handler, exchange);
        super.onResume();
    }

    /*@Override
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
                this.startActivity(new Intent(this, AddContact.class));

                return true;
            }
            case R.id.all:
                if (this.tc != null)
                {
                	adapter.setAllSelected(true);
                	update();
                }
                return true;
            case R.id.remove:
                if (this.tc != null)
                {
                	adapter.setAllSelected(false);
                	update();
                }
                return true;
            case R.id.delete: {
                if (this.tc != null)
                {
                    this.startActivity(new Intent(this.getApplicationContext(), RemoveContactsActivity.class));
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/
    @Override
    protected void onDestroy()
    {	  
    	runThread.setRunner(false);
    	super.onDestroy();
	}
    
    public static void updateList()
    {
        if(runThread != null)
        {
        	Log.v("Run Thread", "Running");
            //final Thread thread = new Thread(runThread);
        	runThread.setRefresh(true);
            //thread.start();
        }
    }

    /**
     * The handler class for clean up after the loader thread has finished.
     */
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg)
        {
        	switch (msg.what){
        		case POP:
        			adapter = new ManageContactAdapter(ManageContactsActivity.this, ManageContactsActivity.contacts);
     	            adapter.notifyDataSetChanged();
		            break;
        		case EMPTY:
        			String emptyListValue = msg.getData().getString(ManageContactsLoader.EMPTYLIST);
        			if (emptyListValue == null)
        			{
        				emptyListValue = "Empty list";
        			}
        			arrayAp = new ArrayAdapter<String>(ManageContactsActivity.this, android.R.layout.simple_list_item_1,
    	                    new String[] { emptyListValue });
    	        	
    	        	arrayAp.notifyDataSetChanged();
        			break;
        	}
        	Log.v("Thread", "Thread finished");
            ManageContactsActivity.this.update();
            if(ManageContactsActivity.this.loadingDialog.isShowing())
            {
            	ManageContactsActivity.this.loadingDialog.dismiss();
            }
        }
    };

}