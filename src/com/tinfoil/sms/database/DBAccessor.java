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

package com.tinfoil.sms.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.Entry;
import com.tinfoil.sms.dataStructures.Message;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.dataStructures.User;
import com.tinfoil.sms.dataStructures.WalkthroughStep;
import com.tinfoil.sms.settings.QuickPrefsActivity;
import com.tinfoil.sms.sms.ConversationView;
import com.tinfoil.sms.utility.SMSUtility;
import com.tinfoil.sms.utility.Walkthrough.Step;

/**
 * Creates a database that is read and write and provides methods to 
 * facilitate the reading and writing to the database. Table Names
 * are all from SQLitehelper since they are created in that class.
 */
public class DBAccessor {
	
	public static final int TRUE = 1;
	public static final int FALSE = 0;
	
	private static String USER_NAME = "Me";
	
	public static final int LENGTH = 20;
	public static final int OTHER_INDEX = 7;
	
	public static String[] TYPES = new String[] {"Home", "Mobile", "Work", "Work Fax",
    	"Home Fax", "Pager", "Other", "Custom", "Callback", "Car", "Company Main", "ISDN", 
    	"Main", "Other Fax", "Telex", "TTY TTD", "Work Mobile", "Work Pager", "Assistant", 
    	"MMS"};
	
	public static final String DEFAULT_BOOK_PATH = "path/path";
	public static final String DEFAULT_BOOK_INVERSE_PATH = "path/inverse";
	
	public static final String DEFAULT_S1 = "Initiator";
	public static final String DEFAULT_S2 = "Receiver";
	
	public static final int ALL = 0;
	public static final int TRUSTED = 1;
	public static final int UNTRUSTED = 2;
	
	private SharedPreferences sharedPrefs;
	private Context context;

	/**
	 * Creates a database that is read and write
	 * @param c	: Context, where the database is available
	 */
	public DBAccessor (Context c)
	{
		this.context = c;
		localizeStrings(c);
		
		this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);		
	}
	
	public static void localizeStrings(Context c)
	{
		USER_NAME = c.getString(R.string.user);
		TYPES = c.getResources().getStringArray(R.array.phone_types);
	}
	
	/**
	 * Get the pending key exchange message for the contact with the given
	 * number.
	 * @param number The number of the contact whose key exchange message is
	 * needed.
	 * @return The Entry in the key exchange db that contains the number and the
	 * key exchange message.
	 */
	public Entry getKeyExchangeMessage(String number)
	{
		long id = this.getNumberId(SMSUtility.format(number));
		
		//open();
		
		Cursor cur = context.getContentResolver().query(DatabaseProvider.EXCHANGE_CONTENT_URI, new String[]{
				SQLitehelper.KEY_ID, SQLitehelper.KEY_EXCHANGE_MESSAGE}, SQLitehelper.KEY_NUMBER_REFERENCE + " = " + id,
				null, null);
		if(cur.moveToFirst())
		{
			Entry exchangeMessage = new Entry(number,
					cur.getString(cur.getColumnIndex(SQLitehelper.KEY_EXCHANGE_MESSAGE)),
					cur.getLong(cur.getColumnIndex(SQLitehelper.KEY_ID)), TRUE);
			
			cur.close();
			return exchangeMessage;
		}
		
		if(cur != null)
		{
			cur.close();
		}
		
		return null;
	}
	
	/**
	 * Get all of the pending key exchange messages.
	 */
	public ArrayList<Entry> getAllKeyExchangeMessages()
	{
		Cursor cur = context.getContentResolver().query(DatabaseProvider.EXCHANGE_CONTENT_URI,
				new String[]{ SQLitehelper.KEY_ID, SQLitehelper.KEY_NUMBER_REFERENCE,
				SQLitehelper.KEY_EXCHANGE_MESSAGE}, null, null, null);
		
		ArrayList<Entry> exchangeMessage = null;
		
		if(cur.moveToFirst())
		{
			exchangeMessage = new ArrayList<Entry>();
			do
			exchangeMessage.add(new Entry(
					getNumber(cur.getLong(cur.getColumnIndex(SQLitehelper.KEY_NUMBER_REFERENCE))),
					cur.getString(cur.getColumnIndex(SQLitehelper.KEY_EXCHANGE_MESSAGE)),
					cur.getLong(cur.getColumnIndex(SQLitehelper.KEY_ID)), TRUE));
			
			
			while(cur.moveToNext());
		}
		cur.close();
		return exchangeMessage;
	}
	
	/**
	 * Get all of the pending key exchange messages.
	 * @return The number of key exchanges pending
	 */
	public int getKeyExchangeMessageCount()
	{
		Cursor cur = context.getContentResolver().query(DatabaseProvider.EXCHANGE_CONTENT_URI,
				new String[]{ SQLitehelper.KEY_ID}, null, null, null);
		
		int count = 0;
		if(cur.moveToFirst())
		{
			count =  cur.getCount();		
		}
		cur.close();
		return count;
	}
	
	/**
	 * Add a pending key exchange message.
	 * @param keyExchange The pending key exchange message
	 * @return The error message, if null the addition was successful.
	 */
	public String addKeyExchangeMessage(Entry keyExchange)
	{
		Entry prev = getKeyExchangeMessage(keyExchange.getNumber());
		if(prev == null)
		{
			ContentValues cv = new ContentValues();
			
			cv.put(SQLitehelper.KEY_NUMBER_REFERENCE, getNumberId(SMSUtility.format(keyExchange.getNumber())));
			cv.put(SQLitehelper.KEY_EXCHANGE_MESSAGE, keyExchange.getMessage());
			
			context.getContentResolver().insert(DatabaseProvider.EXCHANGE_CONTENT_URI, cv);
			
			return null;
		}
		else
		{
			if(prev.getMessage().compareTo(keyExchange.getMessage()) == 0)
			{
				//Contact sent another message
				return keyExchange.getNumber() + " " + context.getString(R.string.duplicate_key_exchange);
			}
			else
			{
				return context.getString(R.string.duplicate_key_exchange_warning_1)
						+ " " + keyExchange.getNumber() + " " +
						context.getString(R.string.duplicate_key_exchange_warning_2);
			}
		}		
	}
	
	/**
	 * Delete the pending key exchange message. Calling this method means the
	 * user has either rejected the key exchange or accepted.
	 * @param number The number of the contact who had a pending key exchange
	 * that has now been dealt with.
	 */
	public void deleteKeyExchangeMessage(String number)
	{
		long id = getNumberId(number);
		context.getContentResolver().delete(DatabaseProvider.EXCHANGE_CONTENT_URI,
				SQLitehelper.KEY_NUMBER_REFERENCE + " = " + id, null);
	}
	
	/**
	 * Add a row to the numbers table.
	 * @param reference The reference id of the contact the number belongs to.
	 * @param number The Number that contains all the information to be stored.
	 */
	private long addNumbersRow (long reference, Number number)
	{
		ContentValues cv = new ContentValues();
			
		//add given values to a row
        cv.put(SQLitehelper.KEY_REFERENCE, reference);
        cv.put(SQLitehelper.KEY_NUMBER, SMSUtility.format(number.getNumber()));
        cv.put(SQLitehelper.KEY_TYPE, number.getType());
        cv.put(SQLitehelper.KEY_UNREAD, number.getUnreadMessageCount());
        cv.put(SQLitehelper.KEY_PUBLIC_KEY, number.getPublicKey());
        cv.put(SQLitehelper.KEY_SIGNATURE, number.getSignature());
        cv.put(SQLitehelper.KEY_NONCE_ENCRYPT, number.getNonceEncrypt());
        cv.put(SQLitehelper.KEY_NONCE_DECRYPT, number.getNonceDecrypt());
        cv.put(SQLitehelper.KEY_INITIATOR, number.getInitiatorInt());
        cv.put(SQLitehelper.KEY_EXCHANGE_SETTING, number.getKeyExchangeFlag());

        //Insert the row into the database

        long id = Long.valueOf(context.getContentResolver().insert(DatabaseProvider
        		.NUMBER_CONTENT_URI, cv).getLastPathSegment());
        
        updateBookPaths(id, number.getBookPath(), number.getBookInversePath());
        addSharedInfo(id, number.getSharedInfo1(), number.getSharedInfo2());
        
        return id;
	}
	
	/**
	 * TODO implement passive message deleter, (such that every time a message is added it will
	 * check if the message count is above the limit, if it is it will delete the oldest messages
	 * until it is under the limit
	 * 
	 * Add a message to the database, only a limited number of messages are stored in the database.
	 * @param reference The id of the number that the message came from or was sent to.
	 * @param message A message object containing all the information for the message.
	 */
	private void addMessageRow (long reference, Message message)
	{
		ContentValues cv = new ContentValues();
			
		//add given values to a row
        cv.put(SQLitehelper.KEY_REFERENCE, reference);
        cv.put(SQLitehelper.KEY_MESSAGE, message.getMessage());
        cv.put(SQLitehelper.KEY_DATE, message.getDate());
        cv.put(SQLitehelper.KEY_SENT, message.getSent());

        //Insert the row into the database
        Cursor cur = context.getContentResolver().query(DatabaseProvider.MESSAGE_CONTENT_URI,
        		new String[]{SQLitehelper.KEY_ID}, SQLitehelper.KEY_REFERENCE
        		+ " = " + reference, null, null);
        
        if (cur.moveToFirst() && cur.getCount() >= Integer.valueOf(sharedPrefs.getString
        		(QuickPrefsActivity.MESSAGE_LIMIT_SETTING_KEY, String.valueOf(SMSUtility.LIMIT))))
        {
        	Cursor date_cur = context.getContentResolver().query(
        			DatabaseProvider.MESSAGE_CONTENT_URI, new String[]
        			{"MIN("+SQLitehelper.KEY_DATE+")"},
            		null, null, null);
        	
        	if (date_cur.moveToFirst() && date_cur.getLong(0) < message.getDate())
        	{
        		/*
		    	 * Updated the update db sql command to account for messages having the exact same date
		    	 */
        		context.getContentResolver().update(DatabaseProvider.MESSAGE_CONTENT_URI,
        				cv, SQLitehelper.KEY_ID + " = " + "(SELECT id FROM " +
        				SQLitehelper.MESSAGES_TABLE_NAME + " WHERE "+ SQLitehelper.KEY_DATE
        				+ " = " + "(SELECT MIN("+SQLitehelper.KEY_DATE+") FROM " 
		        		+ SQLitehelper.MESSAGES_TABLE_NAME + ") LIMIT 1)", null);
        		
        	}
        	date_cur.close();
        }
        else
        {
        	context.getContentResolver().insert(DatabaseProvider.MESSAGE_CONTENT_URI, cv);
        }
        cur.close();
	}
	
	/**
	 * Delete messages that are stored in the database
	 * @param id The id of the message in the database.
	 * @return Whether the message was deleted or not.
	 */
	public boolean deleteMessage(long id)
	{
		int num = context.getContentResolver().delete(DatabaseProvider.MESSAGE_CONTENT_URI,
				SQLitehelper.KEY_ID + " = " + id, null);
		
		if(num == 0)
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Delete the messages that this number has stored in the database.
	 * @param number The number of the contact that is in the database.
	 * @return Whether the messages are deleted or not.
	 */
	public boolean deleteMessage(String number)
	{
		number = SMSUtility.format(number);
		long id = getNumberId(number);
		
		return deleteMessage(id);
	}
	
	/**
	 * Updates the message count for the particular given number to the given
	 * new count.
	 * @param number A number from the contact.
	 * @param unreadMessageCount The new number of unread messages.
	 */
	public void updateMessageCount(String number, int unreadMessageCount)
	{
		ContentValues cv = new ContentValues();
		cv.put(SQLitehelper.KEY_UNREAD, unreadMessageCount);
		context.getContentResolver().update(DatabaseProvider.NUMBER_CONTENT_URI,
				cv, "number = ?", new String[] {number});
	}
	
	/**
	 * Add a new message to the database.
	 * @param message The Message that contains all the information to be
	 * stored.
	 * @param number The number of the contact the message was sent to or
	 * received from.
	 * @param unread Whether the message has been read or has not, true the
	 * message is unread, false otherwise.
	 */
	public void addNewMessage(Message message, String number, boolean unread)
	{
		number = SMSUtility.format(number);
		addMessageRow(getNumberId(number), message);
		if (unread)
		{
			updateMessageCount(number, getUnreadMessageCount(number)+1);
		}
	}
	
	/**
	 * Add a row to the shared_information table.
	 * @param reference : int the id of the contact
	 * @param s1 The first shared information
	 * @param s2 The second shared information
	 */
	private void addSharedInfo (long reference, String s1, String s2)
	{
		ContentValues cv = new ContentValues();
			
		//add given values to a row
        cv.put(SQLitehelper.KEY_REFERENCE, reference);
        cv.put(SQLitehelper.KEY_SHARED_INFO_1, s1);
        cv.put(SQLitehelper.KEY_SHARED_INFO_2, s2);
        
        //Insert the row into the database
        context.getContentResolver().insert(
        		DatabaseProvider.SHARED_INFO_CONTENT_URI, cv);
	}
	
	/** 
	 * Used for updating the shared information
	 * @param reference : int the id of the contact
	 * @param s1 The first shared information
	 * @param s2 The second shared information
	 */
	public void updateSharedInfo(long reference, String s1, String s2)
	{
		ContentValues cv = new ContentValues();
		
		//add given values to a row
        cv.put(SQLitehelper.KEY_REFERENCE, reference);
        cv.put(SQLitehelper.KEY_SHARED_INFO_1, s1);
        cv.put(SQLitehelper.KEY_SHARED_INFO_2, s2);
        
        //Insert the row into the database
        context.getContentResolver().update(DatabaseProvider.SHARED_INFO_CONTENT_URI,
        		cv, SQLitehelper.KEY_REFERENCE + " = " + reference, null);
	}
	
	/**
	 * Used to retrieve the shared information
	 * @param reference The reference id for the contact
	 * @return Both pieces of shared information for that contact (s1 and s2)
	 */
	public String[] getSharedInfo(long reference)
	{
		Cursor cur = context.getContentResolver().query(DatabaseProvider.SHARED_INFO_CONTENT_URI, 
				new String[] {SQLitehelper.KEY_REFERENCE, SQLitehelper.KEY_SHARED_INFO_1, SQLitehelper.KEY_SHARED_INFO_2},
				SQLitehelper.KEY_REFERENCE + " = " + reference, null, null);
		
		if (cur.moveToFirst())
		{
			//Found the reference number in the database
			String sharedInfo[] = new String[] {cur.getString(cur.getColumnIndex
					(SQLitehelper.KEY_SHARED_INFO_1)), cur.getString(cur.
					getColumnIndex(SQLitehelper.KEY_SHARED_INFO_2))};

			cur.close();
			
			return sharedInfo;
		}
		cur.close();
		return new String[] { null, null };
	}
		
	/**
	 * Add a row to the shared_information table.
	 * @param reference The id of the contact
	 * @param bookPath The path for looking up the book source
	 * @param bookInversePath The path for looking up the inverse book source
	 */
	private void addBookPath (long reference, String bookPath, String bookInversePath)
	{
		ContentValues cv = new ContentValues();
			
		//add given values to a row
        cv.put(SQLitehelper.KEY_REFERENCE, reference);
        
        if(bookPath.equalsIgnoreCase("") || 
        		bookPath.equalsIgnoreCase(DEFAULT_BOOK_PATH))
        {
        	bookPath = DBAccessor.DEFAULT_BOOK_PATH;
        }
        cv.put(SQLitehelper.KEY_BOOK_PATH, bookPath);
        
        if(bookInversePath.equalsIgnoreCase("") ||
        		bookInversePath.equalsIgnoreCase(DEFAULT_BOOK_INVERSE_PATH))
        {
        	bookInversePath = DBAccessor.DEFAULT_BOOK_INVERSE_PATH;
        }

        cv.put(SQLitehelper.KEY_BOOK_INVERSE_PATH, bookInversePath);
        
        //Insert the row into the database
        context.getContentResolver().insert(DatabaseProvider.BOOK_PATHS_CONTENT_URI, cv);
	}
	
	/**
	 * Sets the book path back to the default path
	 * @param reference The id of the contact
	 */
	public void resetBookPath (long reference)
	{
		if (!bookIsDefault(reference))
		{
			context.getContentResolver().delete(DatabaseProvider.BOOK_PATHS_CONTENT_URI,
					SQLitehelper.KEY_REFERENCE + " = " + reference, null);
		}
	}
	
	/** 
	 * Used for updating the book paths.
	 * @param reference The id of the contact.
	 * @param bookPath The path for looking up the book source.
	 * @param bookInversePath The path for looking up the inverse book source.
	 */
	public void updateBookPaths(long reference, String bookPath, String bookInversePath)
	{
		resetBookPath(reference);
		if(bookPath != null && bookInversePath != null)
		{
			if(((!bookPath.equalsIgnoreCase("") || !bookInversePath.equalsIgnoreCase("")) &&
					(!bookPath.equalsIgnoreCase(DEFAULT_BOOK_PATH)) ||
					!bookInversePath.equalsIgnoreCase(DEFAULT_BOOK_INVERSE_PATH)))
	        {
				addBookPath(reference, bookPath, bookInversePath);
	        }
		}
	}
	
	/**
	 * Finds out whether the contact has an entry in the book path database.
	 * @param reference The id of the contact.
	 * @return True if the book path is the default, false otherwise.
	 */
	private boolean bookIsDefault(long reference)
	{
		Cursor cur = context.getContentResolver().query(DatabaseProvider.BOOK_PATHS_CONTENT_URI, 
				new String[] {SQLitehelper.KEY_REFERENCE, SQLitehelper.KEY_BOOK_PATH,
				SQLitehelper.KEY_BOOK_INVERSE_PATH}, SQLitehelper.KEY_REFERENCE
				+ " = " + reference, null, null);
		if (cur.moveToFirst())
		{
			cur.close();
			return false;
		}
		cur.close();
		return true;
	}
	
	/**
	 * Used to retrieve the book paths
	 * @param reference The id of the contact
	 * @return The book path, and the book inverse path 
	 */
	public String[] getBookPath(long reference)
	{

		Cursor cur = context.getContentResolver().query(DatabaseProvider.BOOK_PATHS_CONTENT_URI, 
				new String[] {SQLitehelper.KEY_REFERENCE, SQLitehelper.KEY_BOOK_PATH,
				SQLitehelper.KEY_BOOK_INVERSE_PATH}, SQLitehelper.KEY_REFERENCE
				+ " = " + reference, null, null);
		
		if (cur.moveToFirst())
		{
			//Found the reference number in the database
			String bookPaths[] = new String[] {cur.getString(cur.getColumnIndex(SQLitehelper.KEY_BOOK_PATH)),
					cur.getString(cur.getColumnIndex(SQLitehelper.KEY_BOOK_INVERSE_PATH))};

			cur.close();
			return bookPaths;
		}

		cur.close();
		return new String[] { DEFAULT_BOOK_PATH, DEFAULT_BOOK_INVERSE_PATH };
	}
	
	/**
	 * Adds a trusted contact to the database
	 * @param tc The TrustedContact that contains all the information about the
	 * contact.
	 */
	public void addRow (TrustedContact tc)
	{
		if (!inDatabase(tc.getNumber()))
		{
			ContentValues cv = new ContentValues();
			
			//add given values to a row
	        cv.put(SQLitehelper.KEY_NAME, tc.getName());

	        //Insert the row into the database

	        long id = Long.valueOf(context.getContentResolver().insert(DatabaseProvider
	        		.TRUSTED_CONTENT_URI, cv).getLastPathSegment());

	        if (!tc.isNumbersEmpty())
	        {
	        	for (int i = 0; i< tc.getNumber().size();i++)
	        	{
	        		long id2 = addNumbersRow(id, tc.getNumber().get(i));
	        		
	        		for (int j = 0; j < tc.getNumber().get(i).getMessages().size(); j++)
	        		{
	        			addMessageRow(id2, tc.getNumber().get(i).getMessage(j));
	        		}
	        	}
	        }
		}	              
	}
	
	/**
	 * Returns the id of the contact with the given number
	 * @param number The number of the contact
	 * @return :The id for the contact with the given number
	 */
	private long getId(String number)
	{
		Cursor cur = context.getContentResolver().query(DatabaseProvider.NUMBER_CONTENT_URI,
				new String[]{SQLitehelper.KEY_REFERENCE}, SQLitehelper.KEY_NUMBER
				+ " = ?", new String[] {number}, null);

		if (cur.moveToFirst())
		{
			long id = cur.getInt(cur.getColumnIndex((SQLitehelper.KEY_REFERENCE)));
			cur.close();
			return id;
		}
		cur.close();
		return 0;
	}
	
	/**
	 * Get the number's id to use as a reference for the message table.
	 * @param number The number.
	 * @return The id number of the number.
	 * If there is no number in the database it will return 0
	 */
	private long getNumberId(String number)
	{
		Cursor cur = context.getContentResolver().query(DatabaseProvider.NUMBER_CONTENT_URI,
				new String[]{SQLitehelper.KEY_ID}, SQLitehelper.KEY_NUMBER + " = ?",
				new String[] {number}, null);

		if (cur.moveToFirst())
		{
			long id = cur.getInt(cur.getColumnIndex((SQLitehelper.KEY_ID)));
			cur.close();
			return id;
		}
		cur.close();
		return 0;
	}
	
	/**
	 * Get the number give the id for the that number in the database. 
	 * @param id The id of the number in the database.
	 * @return The number that is stored at the given id.
	 */
	private String getNumber(long id)
	{
		Cursor cur = context.getContentResolver().query(DatabaseProvider.NUMBER_CONTENT_URI,
				new String[]{SQLitehelper.KEY_NUMBER}, SQLitehelper.KEY_ID + " = " + id,
				null, null);

		if (cur.moveToFirst())
		{
			//long id = cur.getInt(cur.getColumnIndex((KEY_ID)));
			String number = cur.getString(cur.getColumnIndex(SQLitehelper.KEY_NUMBER));
			cur.close();
			return number;
		}
		cur.close();
		return null;
	}
	
	/**
	 * Check to see if any of the given numbers is already in the database
	 * @param number The list of numbers to check of numbers
	 * @return  true if at least one of the numbers is already in the database,
	 * false otherwise.
	 */
	public boolean inDatabase(ArrayList<Number> number)
	{
		for (int i = 0; i < number.size(); i++)
		{
			if (inDatabase(number.get(i).getNumber()))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the contact is already in the database.
	 * @param number The number that is to look for.
	 * @return True if the number is in the database already, false otherwise.
	 */
	public boolean inDatabase(String number)
	{
		Cursor idCur = context.getContentResolver().query(DatabaseProvider.NUMBER_CONTENT_URI,
				new String[]{SQLitehelper.KEY_REFERENCE, SQLitehelper.KEY_NUMBER}, 
				SQLitehelper.KEY_NUMBER + " = ?", new String[] {SMSUtility.format(number)},
				null);
		
		long id = 0;
		if (idCur.moveToFirst())
		{
			id = idCur.getInt(idCur.getColumnIndex(SQLitehelper.KEY_REFERENCE));
		}
		idCur.close();
		Cursor cur = context.getContentResolver().query(DatabaseProvider.TRUSTED_CONTENT_URI,
				new String[]{SQLitehelper.KEY_NAME}, SQLitehelper.KEY_ID
				+ " = " + id, null, null);

			if (cur.moveToFirst())
		{
			cur.close();
			return true;
		}
		cur.close();
		return false;
	}
	
	/**
	 * Get all of the messages sent and received from the given number.
	 * @param number The number whose messages are going to be retrieved.
	 * @return A list of containing all the important information about the
	 * messages.
	 */
	public List<String[]> getSMSList(String number)
	{
		List<String[]> smsList = new ArrayList<String[]>();
		
		String reversed = "";
		
		/* Reverse the order of the list when loading. */
		if (sharedPrefs.getBoolean(QuickPrefsActivity.REVERSE_MESSAGE_ORDERING_KEY, false))
		{
			reversed = " DESC";
		}	
		
		Cursor cur = context.getContentResolver().query(DatabaseProvider.TNM_CONTENT_URI,
				new String[]{
				SQLitehelper.TRUSTED_TABLE_NAME + "." + SQLitehelper.KEY_NAME, 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_MESSAGE, 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_SENT, 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_DATE,
				SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_ID},
				SQLitehelper.TRUSTED_TABLE_NAME + "." + SQLitehelper.KEY_ID + " = " + 
				SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_REFERENCE + " AND " + 
				SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_ID + " = " +
				SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_REFERENCE + " AND " + 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_MESSAGE + " IS NOT NULL AND " +
				SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_NUMBER + " = ?", new String[]{
				SMSUtility.format(number)}, 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_DATE + reversed);
		
		if (cur.moveToFirst())
		{
			do
			{
				String name = USER_NAME;
				int sentFlag = cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_SENT)); 
				if (sentFlag >= Message.RECEIVED_DEFAULT && sentFlag <= Message.RECEIVED_ENC_OBF_FAIL
						|| (sentFlag >= Message.RECEIVED_KEY_EXCHANGE_INIT 
						&& sentFlag <= Message.RECEIVED_KEY_EXCHANGE_INIT_RESP))
				{
					name = cur.getString(cur.getColumnIndex(SQLitehelper.KEY_NAME));
				}
				//Locale a = ;
				String message = cur.getString(cur.getColumnIndex(SQLitehelper.KEY_MESSAGE));
				String date = Message.millisToDate(cur.getLong(cur.getColumnIndex(SQLitehelper.KEY_DATE)), 
						context.getResources().getConfiguration().locale);
				String id = String.valueOf(cur.getLong(cur.getColumnIndex(SQLitehelper.KEY_ID)));
				String sent = String.valueOf(sentFlag);
				//String count = cur.getString(cur.getColumnIndex(KEY_UNREAD));
				smsList.add(new String[]{name, message, date, id, sent});
			}while(cur.moveToNext());
		}
		cur.close();
		return smsList;
	}
	
	/**
	 * Get all of the last messages sent from every contact.
	 * @return The list of information needed to display the conversations.
	 */
	public List<String[]>  getConversations()
	{
		Cursor cur = context.getContentResolver().query(DatabaseProvider.QUERY_CONTENT_URI,
				new String[]{SQLitehelper.KEY_NAME, SQLitehelper.KEY_NUMBER,
				SQLitehelper.KEY_UNREAD, SQLitehelper.KEY_MESSAGE,
				SQLitehelper.KEY_SENT}, null, null, SQLitehelper.KEY_DATE + " DESC");
		
		List<String[]> sms = new ArrayList<String[]>();
		
		while (cur.moveToNext())
		{
			String address = cur.getString(cur.getColumnIndex(SQLitehelper.KEY_NUMBER));
			String count = cur.getString(cur.getColumnIndex(SQLitehelper.KEY_UNREAD));
			String name = cur.getString(cur.getColumnIndex(SQLitehelper.KEY_NAME));
			int type = cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_SENT));
			String message = cur.getString(cur.getColumnIndex(SQLitehelper.KEY_MESSAGE));
			sms.add(new String[] {address, name, message, count, String.valueOf(type)});
		}
		cur.close();
		return sms;
	}
	
	/**
	 * Get a Number from the database given the number. This method is met to
	 * simplify transactions that require only the contact's Number and no other
	 * information. It is a less expensive query
	 * @param number The contact's number
	 * @return The Number containing all the relevant information about the
	 * contact's number
	 */
	public Number getNumber(String number)
	{
		
		Cursor cur = context.getContentResolver().query(DatabaseProvider.NUMBER_CONTENT_URI,
				new String[]{SQLitehelper.KEY_ID, SQLitehelper.KEY_NUMBER, SQLitehelper.KEY_DRAFT,
				SQLitehelper.KEY_TYPE, SQLitehelper.KEY_UNREAD, SQLitehelper.KEY_PUBLIC_KEY,
				SQLitehelper.KEY_SIGNATURE, SQLitehelper.KEY_NONCE_ENCRYPT, SQLitehelper.KEY_NONCE_DECRYPT,
				SQLitehelper.KEY_INITIATOR, SQLitehelper.KEY_EXCHANGE_SETTING},
				SQLitehelper.KEY_NUMBER + " = ?", new String[]{number}, null);
		
		if(cur.moveToFirst())
		{
			Number returnNumber = new Number(cur.getLong(cur.getColumnIndex(SQLitehelper.KEY_ID)),
					cur.getString(cur.getColumnIndex(SQLitehelper.KEY_NUMBER)),
					cur.getString(cur.getColumnIndex(SQLitehelper.KEY_DRAFT)),
					cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_TYPE)),
					cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_UNREAD)),
					cur.getBlob(cur.getColumnIndex(SQLitehelper.KEY_PUBLIC_KEY)),
					cur.getBlob(cur.getColumnIndex(SQLitehelper.KEY_SIGNATURE)),
					cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_NONCE_ENCRYPT)),
					cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_NONCE_DECRYPT)),
					cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_INITIATOR)),
					cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_EXCHANGE_SETTING)));
			
			//Retrieve the book paths
			returnNumber.setBookPaths(getBookPath(returnNumber.getId()));
			
			//Retrieve the shared information
			returnNumber.setSharedInfo(getSharedInfo(returnNumber.getId()));
			
			cur.close();
			
			return returnNumber;
		}
		cur.close();
		return null;
		
	}
	
	/**
	 * Retrieve the information relating to the contact who has the given
	 * number. This does not however retrieve the messages of each particular
	 * contact's number.  
	 * @param number The number of the contact to retrieve 
	 * @return The TrustedContact containing all the information in the database
	 * about that contact.
	 */ 
	public TrustedContact getRow(String number)
	{
		// Get the id of the number to look up the contact.
		Cursor idCur = context.getContentResolver().query(DatabaseProvider.NUMBER_CONTENT_URI,
				new String[]{SQLitehelper.KEY_REFERENCE}, SQLitehelper.KEY_NUMBER + " = ?",
				new String[] {number}, null);

		long id = 0;
		long num_id = 0;
		int i = 0;
		if (idCur.moveToFirst())
		{
			id = idCur.getInt(idCur.getColumnIndex(SQLitehelper.KEY_REFERENCE));
		}
		idCur.close();
		
		// Find the contact from the TrustedContact table
		Cursor cur = context.getContentResolver().query(DatabaseProvider.TRUSTED_CONTENT_URI,
				new String[]{SQLitehelper.KEY_NAME}, SQLitehelper.KEY_ID +" = " + id, null, null);
		
		if (cur.moveToFirst())
        { 	
			TrustedContact tc = new TrustedContact (cur.getString(cur.getColumnIndex(SQLitehelper.KEY_NAME)));
			cur.close();
			
			// Query the number table to access the number information.
			Cursor pCur = context.getContentResolver().query(DatabaseProvider.TN_CONTENT_URI,
					null, SQLitehelper.TRUSTED_TABLE_NAME + "." + SQLitehelper.KEY_ID + " = " + 
					SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_REFERENCE + " AND " + 
					SQLitehelper.TRUSTED_TABLE_NAME + "." + SQLitehelper.KEY_ID + " = " + id,
					null, null);

			if (pCur.moveToFirst())
			{
				i = 0;
				do
				{
					num_id = pCur.getLong(pCur.getColumnIndex(SQLitehelper.KEY_ID));
					tc.addNumber(new Number (num_id, 
							pCur.getString(pCur.getColumnIndex(SQLitehelper.KEY_NUMBER)),
							pCur.getString(pCur.getColumnIndex(SQLitehelper.KEY_DRAFT)),
							pCur.getInt(pCur.getColumnIndex(SQLitehelper.KEY_TYPE)),
							pCur.getInt(pCur.getColumnIndex(SQLitehelper.KEY_UNREAD)),
							pCur.getBlob(pCur.getColumnIndex(SQLitehelper.KEY_PUBLIC_KEY)),
							pCur.getBlob(pCur.getColumnIndex(SQLitehelper.KEY_SIGNATURE)),
							pCur.getInt(pCur.getColumnIndex(SQLitehelper.KEY_NONCE_ENCRYPT)),
							pCur.getInt(pCur.getColumnIndex(SQLitehelper.KEY_NONCE_DECRYPT)),
							pCur.getInt(pCur.getColumnIndex(SQLitehelper.KEY_INITIATOR)),
							pCur.getInt(pCur.getColumnIndex(SQLitehelper.KEY_EXCHANGE_SETTING))));

					//Retrieve the book paths
					tc.getNumber().get(i).setBookPaths(getBookPath(num_id));
					
					//Retrieve the shared information
					tc.getNumber().get(i).setSharedInfo(getSharedInfo(num_id));

					i++;
				}while(pCur.moveToNext());
			}
			pCur.close();
			
			
			return tc;
        }
		cur.close();
		return null;
	}
	
	public boolean anyTrusted()
	{
		Cursor cur = context.getContentResolver().query(DatabaseProvider.NUMBER_CONTENT_URI,
				new String[]{SQLitehelper.KEY_ID}, SQLitehelper.KEY_PUBLIC_KEY 
				+ " IS NOT NULL" , null, null);
		
		if(cur != null)
		{
			if (cur.moveToFirst())
			{
				if(cur.getCount()> 0)
				{
					cur.close();
					return true;
				}
				cur.close();
			}
		}
		return false;
		
	}
	
	/**
	 * Get all of the rows in the database with the columns
	 * @param select Whether to get trusted, untrusted or all contacts.
	 * @return The list of all the contacts in the database with all relevant
	 * information about them.
	 */
	public ArrayList<TrustedContact> getAllRows(int select)
	{		
		String selectString = "";
		if (select == TRUSTED)
		{
			selectString = " AND " + SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_PUBLIC_KEY + " NOT NULL";
		}
		else if (select == UNTRUSTED)
		{
			selectString = " AND " + SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_PUBLIC_KEY + " IS NULL";
		}
		
		Cursor cur = context.getContentResolver().query(DatabaseProvider.TN_CONTENT_URI,
				new String[]{ SQLitehelper.TRUSTED_TABLE_NAME + ".*",
				SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_NUMBER,
				SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_PUBLIC_KEY},
				SQLitehelper.TRUSTED_TABLE_NAME + "." + SQLitehelper.KEY_ID + " = " + 
				SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_REFERENCE +
				selectString, null, 
				SQLitehelper.TRUSTED_TABLE_NAME + "." + SQLitehelper.KEY_ID);
		
		ArrayList<TrustedContact> tc = new ArrayList<TrustedContact>();
		
		if (cur.moveToFirst())
        {
			// Set to 0 so that the first contact's id will be unqiue.
			long prevContId = 0;
			// Since the prevContId is setting to increment i by 1 the index
			// must be set to -1 to all for the index to properly be used
			int i = -1;
			do
			{
				long curContId = cur.getLong(cur.getColumnIndex(SQLitehelper.KEY_ID));
				
				// Only add the contact as a new trusted contact if they are not already in the database.
				if (prevContId != curContId)
				{
					tc.add(new TrustedContact (cur.getString(cur.getColumnIndex(SQLitehelper.KEY_NAME))));
					prevContId = curContId;
					i++;
				}
				
				tc.get(i).addNumber(new Number (cur.getString(cur.getColumnIndex(SQLitehelper.KEY_NUMBER)),
						cur.getBlob(cur.getColumnIndex(SQLitehelper.KEY_PUBLIC_KEY))));

			}while (cur.moveToNext());
			
			cur.close();
			return tc;
        }
		cur.close();
		return null;
	}
	
	/**
	 * Get number of messages that are unread for all numbers
	 * @return The number of messages unread for all numbers
	 */
	public int getUnreadMessageCount() {
		Cursor cur = context.getContentResolver().query(DatabaseProvider.NUMBER_CONTENT_URI,
				new String[]{"SUM("+SQLitehelper.KEY_UNREAD+")"},
				null, null, SQLitehelper.KEY_ID);
		int count = 0;
		if (cur.moveToFirst())
		{
			count = cur.getInt(0);
		}
		cur.close();
		return count;
	}
	
	/**
	 * Get the unread message count for a given number
	 * @param number A number
	 * @return The number of unread messages
	 */
	public int getUnreadMessageCount(String number) {
		if(number != null)
		{
			Cursor cur = context.getContentResolver().query(DatabaseProvider.NUMBER_CONTENT_URI,
					new String[]{SQLitehelper.KEY_UNREAD}, SQLitehelper.KEY_NUMBER + " = ?",
					new String[]{SMSUtility.format(number)}, SQLitehelper.KEY_ID);
			int count = 0;
			if (cur != null && cur.moveToFirst())
			{
				count = cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_UNREAD));
			}
			cur.close();
			return count;
		}
		return -1;
	}
	
	/**
	 * Store the user's public key, private key and signature.
	 * @param user The user object that contains the public and private key, and
	 * the signature.
	 */
	public void setUser(User user)
	{
		//if (!isKeyGen())
		//{
			ContentValues cv = new ContentValues();
			
			//add given values to a row
	        cv.put(SQLitehelper.KEY_PUBLIC_KEY, user.getPublicKey());
	        cv.put(SQLitehelper.KEY_PRIVATE_KEY, user.getPrivateKey());       
	        
	        //Insert the row into the database
	        context.getContentResolver().insert(DatabaseProvider.USER_CONTENT_URI, cv);
		//}
	}
	
	/**
	 * Get the user's public key, private key and signature
	 * @return The user that contains the public and private key, and the
	 * signature.
	 */
	public User getUserRow()
	{
		Cursor cur = context.getContentResolver().query(DatabaseProvider.USER_CONTENT_URI, 
				new String[] {SQLitehelper.KEY_PUBLIC_KEY, SQLitehelper.KEY_PRIVATE_KEY},
				null, null, null);
		
		if(cur == null)
		{
			return null;
		}
		
		if (cur.moveToFirst())
		{
			User user = new User(cur.getBlob(cur.getColumnIndex(SQLitehelper.KEY_PUBLIC_KEY)),
					cur.getBlob(cur.getColumnIndex(SQLitehelper.KEY_PRIVATE_KEY)));
			cur.close();
			return user;
		}
		
		cur.close();
		return null;
	}

	/**
	 * Update all of the values in a row
	 * @param tc The new values for the row
	 * @param number The number of the contact in the database
	 */
	public void updateRow (TrustedContact tc, String number)
	{
		long id = getId(SMSUtility.format(number));
		updateTrustedRow(tc, number, id);
		
		updateNumberRow(tc.getNumber(), id);
		
	}
	
	/**
	 * Update all of the values in a row
	 * @param tc The new values for the row
	 * @param number The number of the contact in the database
	 */
	public void updateContactInfo (TrustedContact tc, String number)
	{
		long id = getId(SMSUtility.format(number));
		updateTrustedRow(tc, number, id);
		
		updateNumberRowType(tc.getNumber(), id);
		
	}
	
	/**
	 * Update a TrustedContact row
	 * @param tc The new information to be stored
	 * @param number A number owned by the contact
	 * @param id The id for the contact's database row
	 */
	public void updateTrustedRow (TrustedContact tc, String number, long id)
	{
		ContentValues cv = new ContentValues();
		if (id == 0)
		{
			id = getId(SMSUtility.format(number));
		}
		
		//Trusted Table
        cv.put(SQLitehelper.KEY_NAME, tc.getName());

        context.getContentResolver().update(DatabaseProvider.TRUSTED_CONTENT_URI,
        		cv, SQLitehelper.KEY_ID + " = " + id, null);
	}
	
	/**
	 * Update a row from the Numbers table key
	 * @param Number the number object with the new key
	 */
	public void updateKey (Number number)
	{
		ContentValues cv = new ContentValues();
		
		long id = getId(number.getNumber());
		
        cv.put(SQLitehelper.KEY_PUBLIC_KEY, number.getPublicKey());
        cv.put(SQLitehelper.KEY_SIGNATURE, number.getSignature());
        cv.put(SQLitehelper.KEY_INITIATOR, number.getInitiatorInt());
        cv.put(SQLitehelper.KEY_NONCE_ENCRYPT, number.getNonceEncrypt());
        cv.put(SQLitehelper.KEY_NONCE_DECRYPT, number.getNonceDecrypt());
        
        context.getContentResolver().update(DatabaseProvider.NUMBER_CONTENT_URI,
        		cv, SQLitehelper.KEY_REFERENCE + " = " + id + " AND "
        		+ SQLitehelper.KEY_NUMBER + " LIKE ?" , new String[]{number.getNumber()});
	}
	
	/**
	 * Update a row from the Numbers table initiator
	 * @param Number the number object with the new initiator value
	 */
	public void updateInitiator (Number number)
	{
		ContentValues cv = new ContentValues();
		
		long id = getId(number.getNumber());
		
        cv.put(SQLitehelper.KEY_INITIATOR, number.getInitiatorInt());
        
        context.getContentResolver().update(DatabaseProvider.NUMBER_CONTENT_URI,
        		cv, SQLitehelper.KEY_REFERENCE + " = " + id + " AND " +
        		SQLitehelper.KEY_NUMBER + " LIKE ?" , new String[]{number.getNumber()});
	}
	
	/**
	 * Update the Decrypt Nonce count in the database.
	 * @param numb The Number that contains all the contact's security
	 * information with the new decryption nonce to add to the database.
	 */
	public void updateDecryptNonce(Number numb)
	{
		ContentValues cv = new ContentValues();
		
		cv.put(SQLitehelper.KEY_NONCE_DECRYPT, numb.getNonceDecrypt());
		
		context.getContentResolver().update(DatabaseProvider.NUMBER_CONTENT_URI,
				cv, SQLitehelper.KEY_NUMBER + " LIKE ? ",
				new String[]{SMSUtility.format(numb.getNumber())});
	}
	
	/**
	 * Update the Encrypt Nonce count in the database.
	 * @param numb The Number that contains all the contact's security
	 * information with the new encryption nonce to add to the database.
	 */
	public void updateEncryptNonce(Number numb)
	{
		ContentValues cv = new ContentValues();
		
		cv.put(SQLitehelper.KEY_NONCE_ENCRYPT, numb.getNonceEncrypt());
		
		context.getContentResolver().update(DatabaseProvider.NUMBER_CONTENT_URI,
				cv, SQLitehelper.KEY_NUMBER + " LIKE ?",
				new String[]{SMSUtility.format(numb.getNumber())});
	}
	
	public boolean deleteNumber(String number)
	{
		int count = context.getContentResolver().delete(DatabaseProvider.NUMBER_CONTENT_URI,
				SQLitehelper.KEY_NUMBER + " LIKE ?", new String[]{number});
		
		if (count > 0)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Update a row from the Numbers table
	 * @param tc The new information to be stored
	 * @param number A number owned by the contact
	 * @param id The id for the contact's database row
	 */
	public void updateNumberRow(Number numb, String number, long id)
	{
		number = SMSUtility.format(number);
		ContentValues cv = new ContentValues();
		if (id == 0)
		{
			id = getId(number);
		}
		long num_id = getNumberId(number);
		
		cv.put(SQLitehelper.KEY_REFERENCE, id);
        cv.put(SQLitehelper.KEY_NUMBER, numb.getNumber());
        cv.put(SQLitehelper.KEY_DRAFT, numb.getDraft());
        cv.put(SQLitehelper.KEY_TYPE, numb.getType());
        cv.put(SQLitehelper.KEY_UNREAD, numb.getUnreadMessageCount());
        cv.put(SQLitehelper.KEY_PUBLIC_KEY, numb.getPublicKey());
        cv.put(SQLitehelper.KEY_SIGNATURE, numb.getSignature());
        cv.put(SQLitehelper.KEY_NONCE_ENCRYPT, numb.getNonceEncrypt());
        cv.put(SQLitehelper.KEY_NONCE_DECRYPT, numb.getNonceDecrypt());
        cv.put(SQLitehelper.KEY_INITIATOR, numb.getInitiatorInt());
        cv.put(SQLitehelper.KEY_EXCHANGE_SETTING, numb.getKeyExchangeFlag());
        
        context.getContentResolver().update(DatabaseProvider.NUMBER_CONTENT_URI,
        		cv, SQLitehelper.KEY_REFERENCE + " = " + id + " AND " +
        		SQLitehelper.KEY_NUMBER + " LIKE ?" , new String[]{number});
		
		updateBookPaths(num_id, numb.getBookPath(), numb.getBookInversePath());
		updateSharedInfo(num_id, numb.getSharedInfo1(), numb.getSharedInfo2());
	}
	
	/**
	 * Update the draft in the database.
	 * @param number The number of the contact the draft is for.
	 * @param draft The draft of the message.
	 */
	public void updateDraft(String number, String draft)
	{
		ContentValues cv = new ContentValues();
		cv.put(SQLitehelper.KEY_DRAFT, draft);
		context.getContentResolver().update(DatabaseProvider.NUMBER_CONTENT_URI,
        		cv, SQLitehelper.KEY_NUMBER + " LIKE ?" , new String[]{number});
	}
		
	/**
	 * Update a row from the Numbers table
	 * @param tc The new information to be stored
	 * @param number A number owned by the contact
	 * @param id The id for the contact's database row
	 */
	public void updateNumberRowType (ArrayList<Number> number, long id)
	{
		ContentValues cv = new ContentValues();
	
		for(int i = 0; i < number.size(); i++)
		{	
	        cv.put(SQLitehelper.KEY_TYPE, number.get(i).getType());
	        
	        context.getContentResolver().update(DatabaseProvider.NUMBER_CONTENT_URI,
	        		cv, SQLitehelper.KEY_REFERENCE + " = " + id + " AND " +
	        		SQLitehelper.KEY_NUMBER + " = ?", new String[]{number.get(i).getNumber()});
			
			cv.clear();
		}
		
	}
	
	/**
	 * Update a row from the Numbers table
	 * @param tc The new information to be stored
	 * @param number A number owned by the contact
	 * @param id The id for the contact's database row
	 */
	public void updateNumberRow (ArrayList<Number> number, long id)
	{
		ContentValues cv = new ContentValues();
	
		for(int i = 0; i < number.size(); i++)
		{
			cv.put(SQLitehelper.KEY_REFERENCE, id);
	        cv.put(SQLitehelper.KEY_NUMBER, number.get(i).getNumber());
	        cv.put(SQLitehelper.KEY_DRAFT, number.get(i).getDraft());
	        cv.put(SQLitehelper.KEY_TYPE, number.get(i).getType());
	        cv.put(SQLitehelper.KEY_UNREAD, number.get(i).getUnreadMessageCount());
	        cv.put(SQLitehelper.KEY_PUBLIC_KEY, number.get(i).getPublicKey());
	        cv.put(SQLitehelper.KEY_SIGNATURE, number.get(i).getSignature());
	        cv.put(SQLitehelper.KEY_NONCE_ENCRYPT, number.get(i).getNonceEncrypt());
	        cv.put(SQLitehelper.KEY_NONCE_DECRYPT, number.get(i).getNonceDecrypt());
	        cv.put(SQLitehelper.KEY_INITIATOR, number.get(i).getInitiatorInt());
	        cv.put(SQLitehelper.KEY_EXCHANGE_SETTING, number.get(i).getKeyExchangeFlag());
	        
	        int num = context.getContentResolver().update(DatabaseProvider.NUMBER_CONTENT_URI,
	        		cv, SQLitehelper.KEY_REFERENCE + " = " + id + " AND " +
	        		SQLitehelper.KEY_ID + " = " + number.get(i).getId(), null);
			
			cv.clear();
			if(num == 0)
			{
				addNumbersRow(id, number.get(i));
			}
		}
		
	}
	
	/**
	 * Deletes the rows with the given number
	 * @param number The primary number of the contact to be deleted
	 * @return True if the contacts were deleted properly, false otherwise.
	 */
	public boolean removeRow(String number)
	{
		number = SMSUtility.format(number);
		long id = getId(number);
		
		int num = 0;
		num = context.getContentResolver().delete(DatabaseProvider.TRUSTED_CONTENT_URI,
				SQLitehelper.KEY_ID + " = " + id, null);
		
		if (num == 0)
		{
			return false;
		}
		return true;
	}

	/**
	 * Checks if the given number is a trusted contact's number
	 * @param number The number of the potential trusted contact
	 * @return True if the contact is found in the database and is in the
	 * trusted state, false otherwise. A contact is in the trusted state if they
	 * have a key (!= null)
	 */
	public boolean isTrustedContact (String number)
	{
		Number trustedNumber = this.getNumber(SMSUtility.format(number));
		if (trustedNumber != null)
		{
			if (!trustedNumber.isPublicKeyNull())
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param tc The contact to check for a trusted number
	 * @return True if the contact is trusted, false otherwise.
	 * (NOTE: see isTrustedContact(String number) for more details)
	 */
	public boolean isTrustedContact (TrustedContact tc)
	{
		for (int i=0;i<tc.getNumber().size();i++)
		{
			if(isTrustedContact(tc.getNumber().get(i).getNumber()))
			{
				return true;
			}
			
		}
		return false;
	}
	
	/**
	 * @param contacts The contact to check for a trusted number
	 * @return True if the contact is trusted, false otherwise.
	 * 
	 * (NOTE: see isTrustedContact(String number) for more details)
	 */
	public boolean[] isNumberTrusted (ArrayList<Number> number)
	{
		boolean[] trusted = new boolean[number.size()];
		for (int i=0;i<number.size();i++)
		{
			trusted[i] = isTrustedContact(number.get(i).getNumber());
		}
		return trusted;
	}
	
	public void updateWalkthrough(WalkthroughStep ws)
	{
		ContentValues cv = new ContentValues();
		
		if(ws.get(Step.INTRO) != null)
			cv.put(SQLitehelper.KEY_INTRO, ws.get(Step.INTRO));
		
		if(ws.get(Step.START_IMPORT) != null)
			cv.put(SQLitehelper.KEY_START_IMPORT, ws.get(Step.START_IMPORT));
		
		if(ws.get(Step.IMPORT) != null)
			cv.put(SQLitehelper.KEY_IMPORT, ws.get(Step.IMPORT));
		
		if(ws.get(Step.START_EXCHANGE) != null)
			cv.put(SQLitehelper.KEY_START_EXCHANGE, ws.get(Step.START_EXCHANGE));
		
		if(ws.get(Step.SET_SECRET) != null)
			cv.put(SQLitehelper.KEY_SET_SECRET, ws.get(Step.SET_SECRET));
		
		if(ws.get(Step.KEY_SENT) != null)
			cv.put(SQLitehelper.KEY_KEY_SENT, ws.get(Step.KEY_SENT));
		
		if(ws.get(Step.PENDING) != null)
			cv.put(SQLitehelper.KEY_PENDING, ws.get(Step.PENDING));
		
		if(ws.get(Step.ACCEPT) != null)
			cv.put(SQLitehelper.KEY_ACCEPT, ws.get(Step.ACCEPT));
		
		if(ws.get(Step.SUCCESS) != null)
			cv.put(SQLitehelper.KEY_SUCCESS, ws.get(Step.SUCCESS));
		
		if(ws.get(Step.CLOSE) != null)
			cv.put(SQLitehelper.KEY_CLOSE, ws.get(Step.CLOSE));
		
		context.getContentResolver().update(DatabaseProvider.WALKTHROUGH_CONTENT_URI,
        		cv, null, null);
	}
	
	public WalkthroughStep getWalkthrough()
	{
		Cursor cur = context.getContentResolver().query(DatabaseProvider.WALKTHROUGH_CONTENT_URI,
        		new String[]{SQLitehelper.KEY_INTRO, SQLitehelper.KEY_START_IMPORT,
				SQLitehelper.KEY_IMPORT, SQLitehelper.KEY_START_EXCHANGE,
				SQLitehelper.KEY_SET_SECRET, SQLitehelper.KEY_KEY_SENT,
				SQLitehelper.KEY_PENDING, SQLitehelper.KEY_ACCEPT,
				SQLitehelper.KEY_SUCCESS, SQLitehelper.KEY_CLOSE}, null, null, null);

		WalkthroughStep ws = new WalkthroughStep();
		if(cur.moveToFirst())
		{
			ws.set(Step.INTRO, cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_INTRO)));
			ws.set(Step.START_IMPORT, cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_START_IMPORT)));
			ws.set(Step.IMPORT, cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_IMPORT)));
			ws.set(Step.START_EXCHANGE, cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_START_EXCHANGE)));
			ws.set(Step.SET_SECRET, cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_SET_SECRET)));
			ws.set(Step.KEY_SENT, cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_KEY_SENT)));
			ws.set(Step.PENDING, cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_PENDING)));
			ws.set(Step.ACCEPT, cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_ACCEPT)));
			ws.set(Step.SUCCESS, cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_SUCCESS)));
			ws.set(Step.CLOSE, cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_CLOSE)));
			cur.close();
			return ws;
		}
		cur.close();
		return null;
	}
	
	/**
	 * Adding a message to the queue to be sent when there is service to send
	 * the message. Once the message has been sent it will be removed from the
	 * queue.
	 * @param number The number for the contact that the message will be sent
	 * to.
	 * @param message The message that will be sent to the contact with the
	 * given number.
	 */
	public synchronized void addMessageToQueue (String number, String message, boolean keyExchange)
	{
		long numberReference = getNumberId(number);
		ContentValues cv = new ContentValues();

        cv.put(SQLitehelper.KEY_NUMBER_REFERENCE, numberReference);
        cv.put(SQLitehelper.KEY_MESSAGE, message);
        
        if(keyExchange)
        {
        	cv.put(SQLitehelper.KEY_EXCHANGE, TRUE);
        }
        else
        {
        	cv.put(SQLitehelper.KEY_EXCHANGE, FALSE);
        }
        
        context.getContentResolver().insert(DatabaseProvider.QUEUE_CONTENT_URI, cv);
	
		ConversationView.messageSender.threadNotify(true);
	}
	
	/**
	 * Get the first element within the queue.
	 * *Note the entry is also removed from the queue.
	 * @return The first element in the queue stored in the database. If the
	 * queue is empty then the return is null.
	 */
	public synchronized Entry getFirstInQueue ()
	{
		Cursor cur = context.getContentResolver().query(DatabaseProvider.QUEUE_CONTENT_URI,
				new String[]{SQLitehelper.KEY_ID, SQLitehelper.KEY_NUMBER_REFERENCE,
				SQLitehelper.KEY_MESSAGE, SQLitehelper.KEY_EXCHANGE}, SQLitehelper.KEY_ID + 
				" = (SELECT MIN(" + SQLitehelper.KEY_ID + ") FROM " + SQLitehelper.QUEUE_TABLE_NAME +")",
				null, null);
		
		if(cur == null)
		{
			return null;
		}
		
		
		if (cur.moveToFirst())
		{
			long id = cur.getLong(cur.getColumnIndex(SQLitehelper.KEY_ID));
			String number = getNumber(cur.getLong(cur.getColumnIndex(SQLitehelper.KEY_NUMBER_REFERENCE)));

			if(number != null)
			{
				Entry entry = new Entry(number,
						cur.getString(cur.getColumnIndex(SQLitehelper.KEY_MESSAGE)),
						id, cur.getInt(cur.getColumnIndex(SQLitehelper.KEY_EXCHANGE)));
				cur.close();
				
				deleteQueueEntry(id);
				return entry;
			}
		}
		cur.close();		
		return null;
	}
	
	/**
	 * Delete a given entry from the queue
	 * @param id The private key
	 */
	public void deleteQueueEntry (long id)
	{
		context.getContentResolver().delete(DatabaseProvider.QUEUE_CONTENT_URI,
				SQLitehelper.KEY_ID + " = " + id, null);
	}
	
	/**
	 * Delete the first entry in the queue
	 */
	public synchronized void deleteFirstQueueEntry()
	{
		context.getContentResolver().delete(DatabaseProvider.QUEUE_CONTENT_URI,
				SQLitehelper.KEY_ID + " = (SELECT MIN(" + SQLitehelper.KEY_ID + ") FROM " + 
				SQLitehelper.QUEUE_TABLE_NAME +")", null);
	}
	
	
}