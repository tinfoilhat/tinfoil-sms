package com.tinfoil.sms.settings;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;

import com.tinfoil.sms.dataStructures.Message;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.loader.OnFinishedImportingListener;
import com.tinfoil.sms.utility.SMSUtility;

public class ImportTask extends AsyncTask<Context, Void, Integer>{

	private OnFinishedImportingListener listener;
	private ArrayList<TrustedContact> tc;
	private ArrayList<Boolean> inDb;
	private boolean clicked;
	private DBAccessor loader;
	
	private boolean doNothing;
	
	private SharedPreferences sharedPrefs;
	
	public ImportTask(OnFinishedImportingListener listener, boolean doNothing)
	{
		super();
		this.listener = listener;
		this.clicked = false;
		this.doNothing = doNothing;
	}
	
	public ImportTask(OnFinishedImportingListener listener, boolean clicked, boolean doNothing,
			ArrayList<TrustedContact> tc, ArrayList<Boolean> inDb)
	{
		super();
		this.listener = listener;
		this.clicked = clicked;
		this.doNothing = doNothing;
		this.tc = tc;
		this.inDb = inDb;
	}
	
	/*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
	@Override
	protected Integer doInBackground(Context... params) {
		
		Context context = params[0];
		loader = new DBAccessor(context);
		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
   	 
		if (!doNothing)
		{
	        if (!this.clicked)
	        {
	            tc = new ArrayList<TrustedContact>();
	            ArrayList<Number> number;
	            String name;
	            String id;
	
	            final Uri mContacts = ContactsContract.Contacts.CONTENT_URI;
	            final Cursor cur = context.getContentResolver().query(mContacts, new String[] 
	            		{ Contacts._ID, Contacts.DISPLAY_NAME, Contacts.HAS_PHONE_NUMBER }, 
	                    null, null, Contacts.DISPLAY_NAME);
	
	            this.inDb = new ArrayList<Boolean>();
	
	            if (cur != null && cur.moveToFirst()) {
	                do {

	                    number = new ArrayList<Number>();
	                    name = cur.getString(cur.getColumnIndex(Contacts.DISPLAY_NAME));
	                    id = cur.getString(cur.getColumnIndex(Contacts._ID));
	
	                    if (cur.getString(cur.getColumnIndex(Contacts.HAS_PHONE_NUMBER)).equalsIgnoreCase("1"))
	                    {
	                    	Cursor mCur = null;
	                        final Cursor pCur = context.getContentResolver().query(Phone.CONTENT_URI,
	                                new String[] { Phone.NUMBER, Phone.TYPE }, Phone.CONTACT_ID + " = ?",
	                                new String[] { id }, null);
	
	                        if (pCur != null && pCur.moveToFirst())
	                        {
	                            do
	                            {                           	
	                                final String numb = pCur.getString(pCur.getColumnIndex(Phone.NUMBER));
	                                final int type = pCur.getInt(pCur.getColumnIndex(Phone.TYPE));
	                                final Uri uriSMSURI = Uri.parse("content://sms/");
	
	                                // Ensure that the number retrieved is not null
	                                if(numb != null)
	                                {
	                                	number.add(new Number(SMSUtility.format(numb), type));
	                                
	
		                                //This now takes into account the different formats of the numbers. 
		                                mCur = context.getContentResolver().query(uriSMSURI, new String[]
		                                { "body", "date", "type" }, "address = ? or address = ? or address = ?",
		                                        new String[] { SMSUtility.format(numb),
		                                                "+1" + SMSUtility.format(numb),
		                                                "1" + SMSUtility.format(numb) },
		                                        "date DESC LIMIT " +
                                                Integer.valueOf(sharedPrefs.getString
                                                (QuickPrefsActivity.MESSAGE_LIMIT_SETTING_KEY,
                                                String.valueOf(SMSUtility.LIMIT))));
		                                if (mCur != null && mCur.moveToFirst())
		                                {
		                                    do
		                                    {
		                                    	
		                                        //Toast.makeText(this, ContactRetriever.millisToDate(mCur.getLong(mCur.getColumnIndex("date"))), Toast.LENGTH_LONG);
		                                        final Message myM = new Message(mCur.getString(mCur.getColumnIndex("body")),
		                                                mCur.getLong(mCur.getColumnIndex("date")), mCur.getInt(mCur.getColumnIndex("type")));
		                                        number.get(number.size() - 1).addMessage(myM);
		                                        
		                                    } while (mCur.moveToNext());
		                                    
		                                }
	                                }
	                                
	                            } while (pCur.moveToNext());
	                        }
	                        if(mCur != null)
	                        {
	                        	mCur.close();
	                        }                        
	                        pCur.close();
	                    }
	                	
	                    /*
	                     * Added a check to see if the number array is empty
	                     * if a contact has no number they can not be texted
	                     * therefore there is no point allowing them to be
	                     * added.
	                     */
	                    if (number != null && !number.isEmpty() &&
	                            !loader.inDatabase(number))
	                    {
	                        tc.add(new TrustedContact(name, number));
	                        this.inDb.add(false);
	                    }
	
	                    
	                    number = null;
	                } while (cur.moveToNext());
	            }
	            // cur.close();
	
	            final Uri uriSMSURI = Uri.parse("content://sms/conversations/");
	            final Cursor convCur = context.getContentResolver().query(uriSMSURI,
	                    new String[] { "thread_id" }, null,
	                    null, "date DESC");
	            
	            Cursor nCur = null;
	            Cursor sCur = null;
	
	            Number newNumber = null;
	
	            //Check if the thread has been stopped
	            while (convCur != null && convCur.moveToNext())
	            {
	                id = convCur.getString(convCur.getColumnIndex("thread_id"));
	
	                nCur = context.getContentResolver().query(Uri.parse("content://sms/inbox"),
	                        new String[] { "body", "address", "date", "type" }, "thread_id = ?",
	                        new String[] { id }, "date DESC LIMIT " +
	                                Integer.valueOf(sharedPrefs.getString
	                                        (QuickPrefsActivity.MESSAGE_LIMIT_SETTING_KEY, String.valueOf(SMSUtility.LIMIT))));
	
	                if (nCur != null && nCur.moveToFirst())
	                {
	                    newNumber = new Number(SMSUtility.format(
	                            nCur.getString(nCur.getColumnIndex("address"))));
	                    do
	                    {
	                        newNumber.addMessage(new Message(nCur.getString(nCur.getColumnIndex("body")),
	                                nCur.getLong(nCur.getColumnIndex("date")), nCur.getInt(nCur.getColumnIndex("type"))));
	                        //newNumber.setDate(nCur.getLong(nCur.getColumnIndex("date")));
	                    } while (nCur.moveToNext());
	                }
	
	                sCur = context.getContentResolver().query(Uri.parse("content://sms/sent"),
	                        new String[] { "body", "address", "date", "type" }, "thread_id = ?",
	                        new String[] { id }, "date DESC LIMIT " +
	                        Integer.valueOf(sharedPrefs.getString
	                        (QuickPrefsActivity.MESSAGE_LIMIT_SETTING_KEY, String.valueOf(SMSUtility.LIMIT))));

	                if (sCur != null && sCur.moveToFirst())
	                {
	                    if (newNumber == null)
	                    {
	                        newNumber = new Number(SMSUtility.format(
	                                sCur.getString(sCur.getColumnIndex("address"))));
	                    }
	
	                    do
	                    {
	                        newNumber.addMessage(new Message(sCur.getString(sCur.getColumnIndex("body")),
	                                sCur.getLong(sCur.getColumnIndex("date")), sCur.getInt(sCur.getColumnIndex("type"))));
	                        //newNumber.setDate(nCur.getLong(nCur.getColumnIndex("date")));
	                    } while (sCur.moveToNext());
	                }
	                
	                int [] ent = null;
	                
	                if(newNumber != null)
	                {
	                	ent = TrustedContact.isNumberUsed(tc, newNumber.getNumber());
	                }
	                
	                
	                if (ent == null && !loader.inDatabase(newNumber.getNumber())
	                         && newNumber.getNumber() != null)
	                {
	                    tc.add(new TrustedContact(newNumber));
	                    this.inDb.add(false);
	                }
	                else if(newNumber != null && newNumber.getNumber() != null && 
	                		ent != null && !loader.inDatabase(newNumber.getNumber()))
	                {

	                	//Add messages to list
	                	//newNumber.getNumber();
	                	Number num = tc.get(ent[0]).getNumberOb(ent[1]);
	                	if(num != null)
	                	{
	                		for(int i = 0; i < newNumber.getMessages().size(); i++)
	                		{
	                			if(!num.getMessages().contains(newNumber.getMessages().get(i)))
	                			{
	                				num.getMessages().add(newNumber.getMessages().get(i));
	                			}
	                		}
	                		//num.setMessage(newNumber.getMessages());
	                	}
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
	                    loader.addRow(tc.get(i));
	                }
	            }

	        	return ImportContacts.FINISH;
	        }
	        
	        return ImportContacts.LOAD;
	        	
		}
		return ImportContacts.NOTHING;
	}
	
	/*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(final Integer success)
    {
    	// Call the listener if this is successful.
        if (listener != null)
        {

            listener.onFinishedImportingListener(success, tc, inDb);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onCancelled()
     */
    @Override
    protected void onCancelled()
    {
        // Not implemented. Provided to satisfy interface.
    }
    
    /**
     * setOnFinishedTaskListener Used to set the task listener.
     * 
     * @param listener
     *            The listener to set to.
     */
    public void setOnFinshedTaskListener(OnFinishedImportingListener listener)
    {
        this.listener = listener;
    }
    
    /**
     * Set the stop flag to to true
     */
    public synchronized void setClicked(boolean clicked) 
    {
    	this.clicked = clicked;
    }
    
    /**
     * Get the stop flag
     * @return The stop flag
     */
    public synchronized boolean getClicked()
    {
    	return this.clicked;
    }
}
