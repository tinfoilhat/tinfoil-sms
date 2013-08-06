package com.tinfoil.sms.settings;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;

import com.tinfoil.sms.dataStructures.Message;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.sms.ConversationView;
import com.tinfoil.sms.utility.MessageService;
import com.tinfoil.sms.utility.SMSUtility;

public class ImportContactLoader implements Runnable{

    private boolean loopRunner = true;
    private boolean start = true;
    private Activity activity;
	private Thread thread;
    private Handler handler;
    private ArrayList<TrustedContact> tc;
    private ArrayList<Boolean> inDb;
    private boolean clicked;   
    private boolean stop;

    /**
     * Create the object and start the thread 
     * @param context The activity context
     * @param update Whether the load is an update or not.
     * @param handler The Handler that takes care of UI setup after the thread
     * has finished
     */
    public ImportContactLoader(Activity activity, boolean clicked, ArrayList<Boolean> inDb, ArrayList<TrustedContact> tc, Handler handler)
    {
    	this.activity = activity;
    	this.handler = handler;
    	this.clicked = clicked;
    	this.inDb = inDb;
    	this.tc = tc;
    	thread = new Thread(this);
		thread.start();
    }	

	@Override
	public void run() {
		
    	while(loopRunner)
    	{
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
	    	 
	        if (!this.clicked)
	        {
	            tc = new ArrayList<TrustedContact>();
	            ArrayList<Number> number;
	            String name;
	            String id;
	
	            final Uri mContacts = ContactsContract.Contacts.CONTENT_URI;
	            final Cursor cur = activity.managedQuery(mContacts, new String[] { Contacts._ID,
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
	                        final Cursor pCur = activity.getContentResolver().query(Phone.CONTENT_URI,
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
	                                mCur = activity.getContentResolver().query(uriSMSURI, new String[]
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
	                        tc.add(new TrustedContact(name, number));
	                        this.inDb.add(false);
	                    }
	
	                    
	                    number = null;
	                } while (cur.moveToNext());
	            }
	            // cur.close();
	
	            final Uri uriSMSURI = Uri.parse("content://sms/conversations/");
	            final Cursor convCur = activity.getContentResolver().query(uriSMSURI,
	                    new String[] { "thread_id" }, null,
	                    null, "date DESC");
	            
	            Cursor nCur = null;
	            Cursor sCur = null;
	
	            Number newNumber = null;
	
	            //Check if the thread has been stopped
	            while (convCur.moveToNext() && !getStop())
	            {
	                id = convCur.getString(convCur.getColumnIndex("thread_id"));

	                nCur = activity.getContentResolver().query(Uri.parse("content://sms/inbox"),
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
	
	                sCur = activity.getContentResolver().query(Uri.parse("content://sms/sent"),
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
	                if (!TrustedContact.isNumberUsed(tc, newNumber.getNumber())
	                        && !MessageService.dba.inDatabase(newNumber.getNumber()) && newNumber.getNumber() != null)
	                {
	                    tc.add(new TrustedContact(newNumber));
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
	                    MessageService.dba.addRow(tc.get(i));
	                }
	            }
	            
	            android.os.Message msg = new android.os.Message();
	        	Bundle b = new Bundle();
	        	msg.setData(b);
	        	msg.what = ImportContacts.FINISH;
	        	
	        	handler.sendMessage(msg);
	        }
	        
	        if(!getStop())
	        {
	        	android.os.Message msg = new android.os.Message();
	        	Bundle b = new Bundle();
	        	b.putSerializable(ImportContacts.TRUSTED_CONTACTS, (Serializable) tc);
	        	b.putSerializable(ImportContacts.IN_DATABASE, (Serializable) inDb);
	        	msg.setData(b);
	        	msg.what = ImportContacts.LOAD;
	        	
	        	handler.sendMessage(msg);
	        }
	        else
	        {
	        	setStop(false);
	        	android.os.Message msg = new android.os.Message();
	        	Bundle b = new Bundle();
	        	msg.setData(b);
	        	msg.what = ImportContacts.FINISH;
	        	
	        	handler.sendMessage(msg);
	        }

	        // Wait for the next time the list needs to be updated/loaded
			while(loopRunner && start)
			{
				synchronized(this){
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			start = true;
		}
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
    
    /**
     * Set the stop flag.
	 * @param stop Whether the thread should be stopped or not.
	 */
    public synchronized void setStop(boolean stop)
	{
    	this.stop = stop;
	}
    
    /**
     * The semaphore for waking the thread up to reload the contacts
     * @param start Whether to start the execution of the thread or not
     */
    public synchronized void setStart(boolean start) {
		this.start = start;
		notifyAll();
	}
    
    /**
     * The semaphore for keeping the thread running. This can be left as true
     * until the activity is no longer in use (onDestroy) where it can be set to
     * false.
     * @param runner Whether the thread should be kept running
     */
    public synchronized void setRunner(boolean runner) {
		this.loopRunner = runner;
		notifyAll();
	}
}
