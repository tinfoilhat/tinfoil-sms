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
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.Message;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.sms.ConversationView;
import com.tinfoil.sms.sms.MessageView;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;

/**
 * TODO implement on Dialog finish to check if the user exited the load (to then finish the task)
 * ImportContact activity allows for contacts to be imported from the native
 * database to the tinfoil-sms database. When a contact is imported, a contacts'
 * numbers, last message, date of last message, and type is stored. Once a
 * contact is imported they cannot be imported until deleted from tinfoil-sms's
 * database. Changes made in the tinfoil-sms database will not apply to the
 * contact in the native database. An imported contact will appear in the
 * ManageContactsActivity.
 */
public class ImportContacts extends Activity implements Runnable {
    private ListView importList;
    private ArrayList<TrustedContact> tc;
    private boolean disable;
    private ArrayList<Boolean> inDb;
    private ProgressDialog dialog;
    private boolean clicked = false;

    private boolean stop = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.importcontacts);
        this.clicked = false;
        this.importList = (ListView) this.findViewById(R.id.import_contact_list);
        
        final Thread thread = new Thread(this);
        this.dialog = ProgressDialog.show(this, "Searching",
                "Locating Contacts...", true, true, new OnCancelListener() {
    		
        	public void onCancel(DialogInterface dialog) {
    			stop();
    			dialog.dismiss();
    			ImportContacts.this.finish();
    		}
        });
        
        thread.start();
        
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
    
    /**
     * Set the stop flag to to true
     */
    public synchronized void stop() 
    {
    	this.stop = true;
    }
    
    /**
     * Get the stop flag
     * @return The stop flag
     */
    public synchronized boolean getStop()
    {
    	return this.stop;
    }
    
    /**
     * Set the stop flag.
     * @param stop Whether the thread should be stopped or not.
     */
    public synchronized void setStop(boolean stop)
    {
    	this.stop = stop;
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
            ImportContacts.this.clicked = true;
            final Thread thread2 = new Thread(ImportContacts.this);
            thread2.start();

            ImportContacts.this.dialog = ProgressDialog.show(ImportContacts.this, "Importing",
                    "Saving Contacts...", true, false);
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
        names.add("No Contacts to Import");
        return names;

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

    public void run() {
    	
    	/*
    	 * Note throughout this thread checks are made to a variable 'stop'.
    	 * This variable identifies if the user has pressed the back button. If
    	 * they have the thread will break each loop until it is at the end of
    	 * run method and then will the dialog will be dismissed and the user
    	 * will go back to the previous activity.
    	 * This allows the user to interrupt the import contacts loading thread
    	 * so that if the user does not actually want to wait for the all the
    	 * contacts to be found then it will terminate the search. This is
    	 * because the method of reading in the contacts from the native's
    	 * database can be quite time consuming. This time increases as the
    	 * number of contacts increases, of course this also has to do with the
    	 * users phone.
    	 */
    	
    	/* 
    	 * TODO find a better way to check, it seems bad to have dozens of
    	 * checks if a variable is true
    	 */
    
        if (!this.clicked)
        {
            this.tc = new ArrayList<TrustedContact>();
            ArrayList<Number> number;
            String name;
            String id;

            final Uri mContacts = ContactsContract.Contacts.CONTENT_URI;
            final Cursor cur = this.managedQuery(mContacts, new String[] { Contacts._ID,
                    Contacts.DISPLAY_NAME, Contacts.HAS_PHONE_NUMBER },
                    null, null, Contacts.DISPLAY_NAME);

            this.inDb = new ArrayList<Boolean>();

            if (cur != null && cur.moveToFirst()) {
                do {
                	
                	//Check if the thread has been stopped
                	if(getStop())
                	{
                		break;
                	}
                	
                    number = new ArrayList<Number>();
                    name = cur.getString(cur.getColumnIndex(Contacts.DISPLAY_NAME));
                    id = cur.getString(cur.getColumnIndex(Contacts._ID));

                    if (cur.getString(cur.getColumnIndex(Contacts.HAS_PHONE_NUMBER)).equalsIgnoreCase("1"))
                    {
                    	Cursor mCur = null;
                        final Cursor pCur = this.getContentResolver().query(Phone.CONTENT_URI,
                                new String[] { Phone.NUMBER, Phone.TYPE }, Phone.CONTACT_ID + " = ?",
                                new String[] { id }, null);

                        if (pCur != null && pCur.moveToFirst())
                        {
                            do
                            {
                            
                            	//Check if the thread has been stopped
                            	if(getStop())
                            	{
                            		break;
                            	}
                            	
                                final String numb = pCur.getString(pCur.getColumnIndex(Phone.NUMBER));
                                final int type = pCur.getInt(pCur.getColumnIndex(Phone.TYPE));
                                final Uri uriSMSURI = Uri.parse("content://sms/");

                                number.add(new Number(SMSUtility.format(numb), type));

                                //This now takes into account the different formats of the numbers. 
                                mCur = this.getContentResolver().query(uriSMSURI, new String[]
                                { "body", "date", "type" }, "address = ? or address = ? or address = ?",
                                        new String[] { SMSUtility.format(numb),
                                                "+1" + SMSUtility.format(numb),
                                                "1" + SMSUtility.format(numb) },
                                        "date DESC LIMIT " +
                                                Integer.valueOf(ConversationView.sharedPrefs.getString
                                                        ("message_limit", String.valueOf(SMSUtility.LIMIT))));
                                if (mCur != null && mCur.moveToFirst())
                                {
                                    do
                                    {
                                    	//Check if the thread has been stopped
                                    	if(getStop())
                                    	{
                                    		break;
                                    	}
                                    	
                                        //Toast.makeText(this, ContactRetriever.millisToDate(mCur.getLong(mCur.getColumnIndex("date"))), Toast.LENGTH_LONG);
                                        final Message myM = new Message(mCur.getString(mCur.getColumnIndex("body")),
                                                mCur.getLong(mCur.getColumnIndex("date")), mCur.getInt(mCur.getColumnIndex("type")));
                                        number.get(number.size() - 1).addMessage(myM);
                                        
                                        //Check if the thread has been stopped
                                    	if(getStop())
                                    	{
                                    		break;
                                    	}
                                    } while (mCur.moveToNext());
                                    
                                }

                                //Check if the thread has been stopped
                            	if(getStop())
                            	{
                            		break;
                            	}
                                
                            } while (pCur.moveToNext());
                        }
                        if(mCur != null)
                        {
                        	mCur.close();
                        }                        
                        pCur.close();
                    }

                    //Check if the thread has been stopped
                	if(getStop())
                	{
                		break;
                	}
                	
                    /*
                     * Added a check to see if the number array is empty
                     * if a contact has no number they can not be texted
                     * therefore there is no point allowing them to be
                     * added.
                     */
                    if (number != null && !number.isEmpty() &&
                            !MessageService.dba.inDatabase(number))
                    {
                        this.tc.add(new TrustedContact(name, number));
                        this.inDb.add(false);
                    }

                    
                    number = null;
                } while (cur.moveToNext());
            }
            cur.close();

            final Uri uriSMSURI = Uri.parse("content://sms/conversations/");
            final Cursor convCur = this.getContentResolver().query(uriSMSURI,
                    new String[] { "thread_id" }, null,
                    null, "date DESC");
            
            Cursor nCur = null;
            Cursor sCur = null;

            Number newNumber = null;

            //Check if the thread has been stopped
            while (convCur.moveToNext() && !getStop())
            {
                id = convCur.getString(convCur.getColumnIndex("thread_id"));

                /*
                 * TODO possibly come up with a more efficient method, since if the conversation 
                 * has a lot of messages then limit 2 messages will be taken and then will be inserted (or attempted
                 * until there is only user specified limited amount)
                 */
                nCur = this.getContentResolver().query(Uri.parse("content://sms/inbox"),
                        new String[] { "body", "address", "date", "type" }, "thread_id = ?",
                        new String[] { id }, "date DESC LIMIT " +
                                Integer.valueOf(ConversationView.sharedPrefs.getString
                                        ("message_limit", String.valueOf(SMSUtility.LIMIT))));

                if (nCur != null && nCur.moveToFirst())
                {
                    newNumber = new Number(SMSUtility.format(
                            nCur.getString(nCur.getColumnIndex("address"))));
                    do
                    {
                    	//Check if the thread has been stopped
                    	if(getStop())
                    	{
                    		break;
                    	}
                        
                        newNumber.addMessage(new Message(nCur.getString(nCur.getColumnIndex("body")),
                                nCur.getLong(nCur.getColumnIndex("date")), nCur.getInt(nCur.getColumnIndex("type"))));
                        //newNumber.setDate(nCur.getLong(nCur.getColumnIndex("date")));
                    } while (nCur.moveToNext());
                }

                sCur = this.getContentResolver().query(Uri.parse("content://sms/sent"),
                        new String[] { "body", "address", "date", "type" }, "thread_id = ?",
                        new String[] { id }, "date DESC LIMIT " +
                                Integer.valueOf(ConversationView.sharedPrefs.getString
                                        ("message_limit", String.valueOf(SMSUtility.LIMIT))));

                if (sCur != null && sCur.moveToFirst())
                {
                    if (newNumber == null)
                    {
                        newNumber = new Number(SMSUtility.format(
                                sCur.getString(sCur.getColumnIndex("address"))));
                    }

                    do
                    {
                    	//Check if the thread has been stopped
                        if(getStop())
                    	{
                    		break;
                    	}
                        newNumber.addMessage(new Message(sCur.getString(sCur.getColumnIndex("body")),
                                sCur.getLong(sCur.getColumnIndex("date")), sCur.getInt(sCur.getColumnIndex("type"))));
                        //newNumber.setDate(nCur.getLong(nCur.getColumnIndex("date")));
                    } while (sCur.moveToNext());
                }
                if (!TrustedContact.isNumberUsed(this.tc, newNumber.getNumber())
                        && !MessageService.dba.inDatabase(newNumber.getNumber()) && newNumber.getNumber() != null)
                {
                    this.tc.add(new TrustedContact(newNumber));
                    this.inDb.add(false);
                }
            }
            if(nCur != null)
            {
            	nCur.close();
            }
            if(sCur != null)
            {
            	sCur.close();
            }
            convCur.close();
        }
        else
        {
            for (int i = 0; i < this.tc.size(); i++)
            {
                if (this.inDb.get(i))
                {
                    MessageService.dba.addRow(this.tc.get(i));
                }
            }
            this.dialog.dismiss();
            this.finish();
        }
        
        if(!getStop())
        {
        	this.handler.sendEmptyMessage(0);
        }
        else
        {        	
        	setStop(false);
        }
    }

    /*
     * Please note android.os.Message is needed because tinfoil-sms has another
     * class called Message.
     */
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(final android.os.Message msg)
        {
            if (ImportContacts.this.tc != null && ImportContacts.this.tc.size() > 0)
            {
                ImportContacts.this.disable = false;
                ImportContacts.this.importList.setAdapter(new ArrayAdapter<String>(ImportContacts.this,
                        android.R.layout.simple_list_item_multiple_choice, ImportContacts.this.getNames()));

                ImportContacts.this.importList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
            else
            {
                ImportContacts.this.disable = true;
                ImportContacts.this.importList.setAdapter(new ArrayAdapter<String>(ImportContacts.this,
                        android.R.layout.simple_list_item_1, ImportContacts.this.getNames()));
            }
            ImportContacts.this.dialog.dismiss();
        }
    };

}
