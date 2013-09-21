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
import android.os.Bundle;
import android.os.Handler;
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
 * ImportContact activity allows for contacts to be imported from the native
 * database to the tinfoil-sms database. When a contact is imported, a contact's
 * numbers, last message, date of last message, and type is stored. Once a
 * contact is imported they cannot be imported until deleted from tinfoil-sms's
 * database. Changes made in the tinfoil-sms database will not apply to the
 * contact in the native database. An imported contact will appear in the
 * ManageContactsActivity.
 */
public class ImportContacts extends Activity {
    private ListView importList;
    private ArrayList<TrustedContact> tc;
    private boolean disable;
    private ArrayList<Boolean> inDb;
    private ProgressDialog dialog;
    
    public static final String TRUSTED_CONTACTS = "trusted_contact";
    public static final String IN_DATABASE = "in_database";
    public static final int LOAD = 0;
    public static final int FINISH = 1;

    private ImportContactLoader runThread;

    @SuppressWarnings("unchecked")
	@Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.importcontacts);
        
        this.importList = (ListView) this.findViewById(R.id.import_contact_list);
        
        if(savedInstanceState == null)
        {
	        runThread = new ImportContactLoader(this, false, false, inDb, tc, handler);
	        
	        //final Thread thread = new Thread(this);
	        this.dialog = ProgressDialog.show(this, this
	        		.getString(R.string.searching_title), this
	        		.getString(R.string.searching_message), true, true, 
	        		new OnCancelListener() {
	    		
	        	public void onCancel(DialogInterface dialog) {
	    			runThread.setStop(true);
	    			dialog.dismiss();
	    			ImportContacts.this.finish();
	    		}
	        });
      	}
        else
        {
        	tc = (ArrayList<TrustedContact>) savedInstanceState.getSerializable(ImportContacts.TRUSTED_CONTACTS);
        	inDb = (ArrayList<Boolean>) savedInstanceState.getSerializable(ImportContacts.IN_DATABASE);
        	runThread = new ImportContactLoader(this, false, true, inDb, tc, handler);
        	setUpUI();
        }
        
        this.importList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
                //Keep track of the contacts selected.
                if (!ImportContacts.this.disable)
                {
                    ImportContacts.this.change(position);
                }
            }
        });
        
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	outState.putSerializable(ImportContacts.TRUSTED_CONTACTS, tc);
    	outState.putSerializable(ImportContacts.IN_DATABASE, inDb);
    	super.onSaveInstanceState(outState);
    }
    
    /**
     * The onClick action for when the user clicks on the import contact
     * @param view The view that is involved
     */
    public void importSelected(View view)
    {    	
    	//Add Contacts to the tinfoil-sms database from android's database
        if (!ImportContacts.this.disable)
        {
            runThread.setClicked(true);

            runThread.setStart(false);

            ImportContacts.this.dialog = ProgressDialog.show(ImportContacts.this,
            		this.getString(R.string.importing_title),
                    this.getString(R.string.importing_message), true, false);
        }
    }

    /**
     * Unselect the contact from being added to the database
     * @param position The index of the contact
     */
    public void remove(final int position)
    {
        this.inDb.set(position, false);
    }

    /**
     * Select the contact from being added to the database
     * @param position The index of the contact
     */
    public void add(final int position)
    {
        this.inDb.set(position, true);
    }

    /**
     * Toggle the contact from being added or removed from the addition list
     * @param position The index of the contact
     */
    public void change(final int position)
    {
        if (this.tc != null)
        {
            if (this.inDb.get(position))
            {
                this.remove(position);
            }
            else
            {
                this.add(position);
            }
        }
    }

    /**
     * Produces an ArrayList of contact names from the ArrayList of
     * TrustedContacts
     * 
     * @return A list of the names of each person on the list.
     */
    public ArrayList<String> getNames()
    {
        final ArrayList<String> names = new ArrayList<String>();
        if (!this.disable)
        {
            for (int i = 0; i < this.tc.size(); i++)
            {
                names.add(this.tc.get(i).getName());
            }
            return names;
        }
        names.add(this.getString(R.string.empty_import_list));
        return names;
    }
    
    @Override
    protected void onDestroy()
    {
    	if(runThread != null){
    		runThread.setRunner(false);
    	}
	    //tc = null;
	    //dialog = null;
	    super.onDestroy();
	}

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.import_menu, menu);
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
                        this.importList.setItemChecked(i, true);
                        if (this.tc != null)
                        {
                            this.add(i);
                        }
                    }
                }
                return true;
            case R.id.rm_import:
                if (this.tc != null)
                {
                    for (int i = 0; i < this.tc.size(); i++)
                    {
                        this.importList.setItemChecked(i, false);
                        if (this.tc != null)
                        {
                            this.remove(i);
                        }
                    }
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void setUpUI()
    {
    	Button button = (Button)ImportContacts.this.findViewById(R.id.confirm);
    	if (tc != null && tc.size() > 0)
        {
        	button.setEnabled(true);
            ImportContacts.this.disable = false;
            ImportContacts.this.importList.setAdapter(new ArrayAdapter<String>(ImportContacts.this,
                    android.R.layout.simple_list_item_multiple_choice, ImportContacts.this.getNames()));

            ImportContacts.this.importList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        }
        else
        {
        	button.setEnabled(false);
            ImportContacts.this.disable = true;
            ImportContacts.this.importList.setAdapter(new ArrayAdapter<String>(ImportContacts.this,
                    android.R.layout.simple_list_item_1, ImportContacts.this.getNames()));
        }
    }

    /*
     * Please note android.os.Message is needed because tinfoil-sms has another
     * class called Message.
     */
    private final Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
		@Override
        public void handleMessage(final android.os.Message msg)
        {
        	
        	switch (msg.what){
	    		case LOAD:
		        	Bundle b = msg.getData();
		        	tc = (ArrayList<TrustedContact>) b.getSerializable(ImportContacts.TRUSTED_CONTACTS);
		        	inDb = (ArrayList<Boolean>) b.getSerializable(ImportContacts.IN_DATABASE);
		        	
		        	setUpUI();
		            		            
		            if (ImportContacts.this.dialog.isShowing())
		            {
		            	ImportContacts.this.dialog.dismiss();
		            }
		            break;
	    		case FINISH:
	    			ImportContacts.this.finish();
	    			break;
        	}
        	
        }
    };

}
