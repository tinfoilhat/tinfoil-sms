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

package com.tinfoil.sms.database;

import java.util.ArrayList;
import java.util.List;

import com.tinfoil.sms.dataStructures.Message;
import com.tinfoil.sms.dataStructures.Number;
import com.tinfoil.sms.dataStructures.TrustedContact;
import com.tinfoil.sms.dataStructures.User;
import com.tinfoil.sms.messageQueue.Queue;
import com.tinfoil.sms.sms.ConversationView;
import com.tinfoil.sms.utility.SMSUtility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Creates a database that is read and write and provides methods to 
 * facilitate the reading and writing to the database. Table Names
 * are all from SQLitehelper since they are created in that class.
 */
public class DBAccessor {
	
	public static final String KEY_ID = "id";
	public static final String KEY_NAME = "name";
	public static final String KEY_PUBLIC_KEY = "public_key";
	public static final String KEY_PRIVATE_KEY = "private_key";
	public static final String KEY_SIGNATURE = "signature";
	
	public static final String KEY_SHARED_INFO_1 = "shared_info_1";
	public static final String KEY_SHARED_INFO_2 = "shared_info_2";

	public static final String KEY_BOOK_PATH = "book_path";
	public static final String KEY_BOOK_INVERSE_PATH = "book_inverse_path";

	public static final String KEY_REFERENCE = "reference";
	public static final String KEY_NUMBER = "number";
	public static final String KEY_TYPE = "type";
	public static final String KEY_UNREAD = "unread";
	
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DATE = "date";
	public static final String KEY_SENT = "sent";
	
	public static final String KEY_NUMBER_REFERENCE = "number_reference";
	
	public static final String KEY_SYMMETRIC_KEY = "symmetric_key";
	
	private static final String USER_NAME = "Me";
	
	public static final int LENGTH = 21;
	public static final int OTHER_INDEX = 7;
	
	public static String[] TYPES = new String[] {"", "Home", "Mobile", "Work", "Work Fax",
    	"Home Fax", "Pager", "Other", "Custom", "Callback", "Car", "Company Main", "ISDN", 
    	"Main", "Other Fax", "Telex", "TTY TTD", "Work Mobile", "Work Pager", "Assistant", 
    	"MMS"};
	
	public static final String DEFAULT_BOOK_PATH = "path/path";
	public static final String DEFAULT_BOOK_INVERSE_PATH = "path/inverse";
	
	public static final String DEFAULT_S1 = "Initiator";
	public static final String DEFAULT_S2 = "Receiver";
	
	private SQLiteDatabase db;
	private SQLitehelper contactDatabase;

	/**
	 * Creates a database that is read and write
	 * @param c	: Context, where the database is available
	 */
	public DBAccessor (Context c)
	{
		contactDatabase = new SQLitehelper(c);
		db = contactDatabase.getWritableDatabase();
	}
	
	/**
	 * Add a row to the numbers table.
	 * @param reference : int the reference id of the contact the number belongs to
	 * @param number : Number the object containing the number, last message, type, and date of last sent
	 */
	private long addNumbersRow (long reference, Number number)
	{
		ContentValues cv = new ContentValues();
			
		//add given values to a row
        cv.put(KEY_REFERENCE, reference);
        cv.put(KEY_NUMBER, SMSUtility.format(number.getNumber()));
        cv.put(KEY_TYPE, number.getType());
        cv.put(KEY_UNREAD, number.getUnreadMessageCount());
        cv.put(KEY_PUBLIC_KEY, number.getPublicKey());
        cv.put(KEY_SIGNATURE, number.getSignature());

        //Insert the row into the database
        open();
        long id = db.insert(SQLitehelper.NUMBERS_TABLE_NAME, null, cv);
        close();
        
        updateBookPaths(id, number.getBookPath(), number.getBookInversePath());
        updateSharedInfo(id, number.getSharedInfo1(), number.getSharedInfo2());
        
        return id;
	}
	
	/**
	 * TODO implement passive message deleter, (such that every time a message is added it will
	 * check if the message count is above the limit, if it is it will delete the oldest messages
	 * until it is under the limit
	 * 
	 * Add a message to the database, only a limited number of messages are stored in the database.
	 * @param reference : long, the id of the number that the message came from or was sent to.
	 * @param message : Message, a message object containing all the information for the message.
	 */
	private void addMessageRow (long reference, Message message)
	{
		ContentValues cv = new ContentValues();
			
		//add given values to a row
        cv.put(KEY_REFERENCE, reference);
        cv.put(KEY_MESSAGE, message.getMessage());
        cv.put(KEY_DATE, message.getDate());
        cv.put(KEY_SENT, message.getSent());

        //Insert the row into the database
        open();
        Cursor cur = db.query(SQLitehelper.MESSAGES_TABLE_NAME, new String[]{"COUNT("+KEY_MESSAGE+")"},
        		KEY_REFERENCE + " = " + reference, null, null, null, null);
        
        if (cur.moveToFirst() && cur.getInt(0) >= Integer.valueOf(ConversationView.sharedPrefs.getString
        		("message_limit", String.valueOf(SMSUtility.LIMIT))))
        {
        	Cursor date_cur = db.query(SQLitehelper.MESSAGES_TABLE_NAME, new String[]{"MIN("+KEY_DATE+")"},
            		null, null, null, null, null);
        	
        	if (date_cur.moveToFirst() && date_cur.getLong(0) < message.getDate())
        	{
        		/*
		    	 * Updated the update db sql command to account for messages having the exact same date
		    	 */
		    	db.update(SQLitehelper.MESSAGES_TABLE_NAME, cv, KEY_ID + " = " + 
		        		"(SELECT id FROM " + SQLitehelper.MESSAGES_TABLE_NAME + 
		        		" WHERE "+ KEY_DATE + " = " + "(SELECT MIN("+KEY_DATE+") FROM " 
		        		+ SQLitehelper.MESSAGES_TABLE_NAME + ") LIMIT 1)", null);
        		
        	}
        	date_cur.close();
        }
        else
        {
        	db.insert(SQLitehelper.MESSAGES_TABLE_NAME, null, cv);
        }
        close(cur);
	}
	
	public boolean deleteMessage(long id)
	{
		open();
		int num = db.delete(SQLitehelper.MESSAGES_TABLE_NAME, KEY_ID + " = " + id, null);
		close();
		if(num == 0)
		{
			return false;
		}
		return true;
	}
	
	public boolean deleteMessage(String number)
	{
		number = SMSUtility.format(number);
		long id = getNumberId(number);
		
		open();
		int num = db.delete(SQLitehelper.MESSAGES_TABLE_NAME, KEY_REFERENCE + " = " + id, null);
		close();
		
		if(num > 0)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Updates the message count for the particular given number to the given new count.
	 * @param number : String a number.
	 * @param unreadMessageCount : int the new number of unread messages.
	 */
	public void updateMessageCount(String number, int unreadMessageCount)
	{
		ContentValues cv = new ContentValues();
		cv.put(KEY_UNREAD, unreadMessageCount);
		open();
        db.update(SQLitehelper.NUMBERS_TABLE_NAME, cv, "number = ?", new String[] {number});
        close();
	}
	
	/**
	 * Add a new message to the database.
	 * @param message : Message, the object containing all the information to be stored.
	 * @param number : String, the number of the contact the message was sent to or received from.
	 * @param unread : boolean, whether the message has been read or has not.
	 * if true the message is unread
	 * if false the message is read
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
	 * @param s1 : String the first shared information
	 * @param s2 : String the second shared information
	 */
	private void addSharedInfo (long reference, String s1, String s2)
	{
		ContentValues cv = new ContentValues();
			
		//add given values to a row
        cv.put(KEY_REFERENCE, reference);
        
        if(s1.equalsIgnoreCase("") || s1.equalsIgnoreCase(DEFAULT_S1))
        {
        	s1 = DBAccessor.DEFAULT_S1;
        }
        cv.put(KEY_SHARED_INFO_1, s1);
        
        if(s2.equalsIgnoreCase("") || s2.equalsIgnoreCase(DEFAULT_S2))
        {
        	s2 = DBAccessor.DEFAULT_S2;
        }
        cv.put(KEY_SHARED_INFO_2, s2);
        
        //Insert the row into the database
        open();
        db.insert(SQLitehelper.SHARED_INFO_TABLE_NAME, null, cv);
        close();
	}
	
	/** 
	 * Used for updating the shared information, will not delete the default row
	 * @param reference : int the id of the contact
	 * @param s1 : String the first shared information
	 * @param s2 : String the second shared information
	 */
	public void updateSharedInfo(long reference, String s1, String s2)
	{
		resetSharedInfo(reference);
		if(s1 != null && s2 != null)
		{
			if((!s1.equalsIgnoreCase("") || !s2.equalsIgnoreCase("")) &&
					(!s1.equalsIgnoreCase(DEFAULT_S1) || !s2.equalsIgnoreCase(DEFAULT_S2)))
	        {
				addSharedInfo(reference, s1, s2);
	        }
		}
	}
	
	/**
	 * Resets the shared information to the default shared information
	 * @param reference : int the reference id for the contact
	 */
	public void resetSharedInfo (long reference)
	{
		if (reference != 0 && !sharedInfoIsDefault(reference) )
		{
			open();
			db.delete(SQLitehelper.SHARED_INFO_TABLE_NAME, KEY_REFERENCE + " = " + reference, null);
			close();
		}
	}
	
	/**
	 * Check if the shared info is the default shared info
	 * @param reference : int the id of the contact
	 * @return : boolean
	 * true if the shared info is the default
	 * false if the shared info is not the default
	 */
	private boolean sharedInfoIsDefault(long reference)
	{
		open();
		Cursor cur = db.query(SQLitehelper.SHARED_INFO_TABLE_NAME, 
				new String[] {KEY_REFERENCE, KEY_SHARED_INFO_1, KEY_SHARED_INFO_2},
				KEY_REFERENCE + " = " + reference, null, null, null, null);
		if (cur.moveToFirst())
		{
			close(cur);
			return false;
		}
		close(cur);
		return true;
	}
	
	/**
	 * Used to retrieve the shared information
	 * @param reference : int the reference id for the contact
	 * @return : String[2] s1 and s2
	 */
	public String[] getSharedInfo(long reference)
	{
		boolean open = true;
		if(!db.isOpen())
		{
			open = false;
			open();
		}
		Cursor cur = db.query(SQLitehelper.SHARED_INFO_TABLE_NAME, 
				new String[] {KEY_REFERENCE, KEY_SHARED_INFO_1, KEY_SHARED_INFO_2},
				KEY_REFERENCE + " = " + reference, null, null, null, null);
		
		if (cur.moveToFirst())
		{
			//Found the reference number in the database
			String sharedInfo[] = new String[] {cur.getString(cur.getColumnIndex(KEY_SHARED_INFO_1)),
					cur.getString(cur.getColumnIndex(KEY_SHARED_INFO_2))};
			if (open)
			{
				cur.close();
			}
			else
			{
				close(cur);
			}
			return sharedInfo;
		}
		cur.close();
		return new String[] { DEFAULT_S1, DEFAULT_S2 };
	}
		
	/**
	 * Add a row to the shared_information table.
	 * @param reference : int the id of the contact
	 * @param bookPath : String the path for looking up the book source
	 * @param bookInversePath : String the path for looking up the inverse book source
	 */
	private void addBookPath (long reference, String bookPath, String bookInversePath)
	{
		ContentValues cv = new ContentValues();
			
		//add given values to a row
        cv.put(KEY_REFERENCE, reference);
        
        if(bookPath.equalsIgnoreCase("") || 
        		bookPath.equalsIgnoreCase(DEFAULT_BOOK_PATH))
        {
        	bookPath = DBAccessor.DEFAULT_BOOK_PATH;
        }
        cv.put(KEY_BOOK_PATH, bookPath);
        
        if(bookInversePath.equalsIgnoreCase("") ||
        		bookInversePath.equalsIgnoreCase(DEFAULT_BOOK_INVERSE_PATH))
        {
        	bookInversePath = DBAccessor.DEFAULT_BOOK_INVERSE_PATH;
        }

        cv.put(KEY_BOOK_INVERSE_PATH, bookInversePath);
        
        //Insert the row into the database
        open();
        db.insert(SQLitehelper.BOOK_PATHS_TABLE_NAME, null, cv);
        close();
	}
	
	/**
	 * Sets the book path back to the default path
	 * @param reference : int the id of the contact
	 */
	public void resetBookPath (long reference)
	{
		if (!bookIsDefault(reference))
		{
			open();
			db.delete(SQLitehelper.BOOK_PATHS_TABLE_NAME, KEY_REFERENCE + " = " + reference, null);
			close();
		}
	}
	
	/** 
	 * Used for updating the book paths, will not delete the default row
	 * @param reference : int the id of the contact
	 * @param bookPath : String the path for looking up the book source
	 * @param bookInversePath : String the path for looking up the inverse book source
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
	 * @param reference : int the id of the contact
	 * @return : boolean 
	 * true if the book path is the default
	 * false if the book path is not the default
	 */
	private boolean bookIsDefault(long reference)
	{
		open();
		Cursor cur = db.query(SQLitehelper.BOOK_PATHS_TABLE_NAME, 
				new String[] {KEY_REFERENCE, KEY_BOOK_PATH, KEY_BOOK_INVERSE_PATH},
				KEY_REFERENCE + " = " + reference, null, null, null, null);
		if (cur.moveToFirst())
		{
			close(cur);
			return false;
		}
		close(cur);
		return true;
	}
	
	/**
	 * Used to retrieve the book paths
	 * @param reference : int the id of the contact
	 * @return : String[2] the book path, and the book inverse path 
	 */
	public String[] getBookPath(long reference)
	{
		boolean open = true;
		if (!db.isOpen())
		{
			open = false;
			open();
		}
		Cursor cur = db.query(SQLitehelper.BOOK_PATHS_TABLE_NAME, 
				new String[] {KEY_REFERENCE, KEY_BOOK_PATH, KEY_BOOK_INVERSE_PATH}, 
				KEY_REFERENCE + " = " + reference, null, null, null, null);
		
		if (cur.moveToFirst())
		{
			//Found the reference number in the database
			String bookPaths[] = new String[] {cur.getString(cur.getColumnIndex(KEY_BOOK_PATH)),
					cur.getString(cur.getColumnIndex(KEY_BOOK_INVERSE_PATH))};
			if (open)
			{
				cur.close();
			}
			else
			{
				close(cur);
			}
			return bookPaths;
		}

		cur.close();
		return new String[] { DEFAULT_BOOK_PATH, DEFAULT_BOOK_INVERSE_PATH };
	}
	
	/**
	 * Adds a trusted contact to the database
	 * @param tc : TrustedContact contains all the required information for the contact
	 */
	public void addRow (TrustedContact tc)
	{
		if (!inDatabase(tc.getNumber()))
		{
			ContentValues cv = new ContentValues();
			
			//add given values to a row
	        cv.put(KEY_NAME, tc.getName());

	        //Insert the row into the database
	        open();
	        long id = db.insert(SQLitehelper.TRUSTED_TABLE_NAME, null, cv);
	        close();
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
	 * @param number : String the number of the contact
	 * @return : int the id for the contact with the given number
	 */
	private long getId(String number)
	{
		open();
		Cursor cur = db.rawQuery("SELECT " + KEY_REFERENCE + " FROM " + 
		SQLitehelper.NUMBERS_TABLE_NAME  + " WHERE " + KEY_NUMBER + " = ?", new String[] {number});

		if (cur.moveToFirst())
		{
			long id = cur.getInt(cur.getColumnIndex((KEY_REFERENCE)));
			close(cur);
			return id;
		}
		close(cur);
		return 0;
	}
	
	/**
	 * Get the number's id to use as a reference for the message table
	 * @param number : String, the number
	 * @return : long the id number of the number
	 * if there is no number in the database it will return 0
	 */
	private long getNumberId(String number)
	{
		open();
		Cursor cur = db.rawQuery("SELECT " + KEY_ID + " FROM " + 
		SQLitehelper.NUMBERS_TABLE_NAME  + " WHERE " + KEY_NUMBER + " = ?", new String[] {number});

		if (cur.moveToFirst())
		{
			long id = cur.getInt(cur.getColumnIndex((KEY_ID)));
			close(cur);
			return id;
		}
		close(cur);
		return 0;
	}
	
	private String getNumber(long id)
	{
		open();
		Cursor cur = db.rawQuery("SELECT " + KEY_NUMBER + " FROM " + 
		SQLitehelper.NUMBERS_TABLE_NAME  + " WHERE " + KEY_ID + " = " + id, null);

		if (cur.moveToFirst())
		{
			//long id = cur.getInt(cur.getColumnIndex((KEY_ID)));
			String number = cur.getString(cur.getColumnIndex(KEY_NUMBER));
			close(cur);
			return number;
		}
		close(cur);
		return null;
	}
	
	/**
	 * Check to see if any of the given numbers is already in the database
	 * @param number : ArrayList<String> of numbers
	 * @return : boolean 
	 * true if the number is in the database already 
	 * false if the number is not found the database
	 */
	public boolean inDatabase(ArrayList<Number> number)
	{
		for (int i = 0; i<number.size(); i++)
		{
			if (inDatabase(number.get(i).getNumber()))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the contact is already in the database
	 * @param number : String a number for the contact
	 * @return: boolean 
	 * true if the number is in the database already 
	 * false if the number is not found the database
	 */
	public boolean inDatabase(String number)
	{
		open();
		Cursor idCur = db.rawQuery("SELECT " + KEY_REFERENCE + ", " + KEY_NUMBER + " FROM "
				+ SQLitehelper.NUMBERS_TABLE_NAME + " WHERE " + KEY_NUMBER + " = ?", 
				new String[] {SMSUtility.format(number)});
		
		long id = 0;
		if (idCur.moveToFirst())
		{
			id = idCur.getInt(idCur.getColumnIndex(KEY_REFERENCE));
		}
		idCur.close();
		Cursor cur = db.query(SQLitehelper.TRUSTED_TABLE_NAME, new String[]
				{KEY_NAME}, KEY_ID +" = " + id, null, null, null, null);

			if (cur.moveToFirst())
		{
			close(cur);
			return true;
		}
		close(cur);
		return false;
		
	}
	
    /**
     * Open the database to be used
     */
	public synchronized void open()
	{
		db = contactDatabase.getWritableDatabase();
	}
	
	/**
	 * Close the database
	 * @param cur : Cursor, the cursor to close 
	 */
	public synchronized void close(Cursor cur)
	{
		cur.close();
		db.close();
	}
	
	/**
	 * Close the database
	 */
	public synchronized void close()
	{
		db.close();
	}
	
	/**
	 * Get all of the messages sent and received from the given number.
	 * @param number : String, a number
	 * @return : List<String[]> with the name and the message stored in the
	 * array. 
	 */
	public List<String[]> getSMSList(String number)
	{
		List<String[]> smsList = new ArrayList<String[]>();
		open();
		Cursor cur = db.query(SQLitehelper.TRUSTED_TABLE_NAME + ", " + 
				SQLitehelper.NUMBERS_TABLE_NAME + ", " +
				SQLitehelper.MESSAGES_TABLE_NAME, new String[]{
				SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_NAME, 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_MESSAGE, 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_SENT, 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_DATE,
				SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_ID},
				SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_ID + " = " + 
				SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_REFERENCE + " AND " + 
				SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_ID + " = " +
				SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_REFERENCE + " AND " + 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_MESSAGE + " IS NOT NULL AND " +
				SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_NUMBER + " = ?", new String[]{
				SMSUtility.format(number)}, null, null, 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_DATE + " DESC");
		
		if (cur.moveToFirst())
		{
			do
			{
				String name = USER_NAME;
				if (cur.getInt(cur.getColumnIndex(KEY_SENT)) == 1)
				{
					name = cur.getString(cur.getColumnIndex(KEY_NAME));
				}
				String message = cur.getString(cur.getColumnIndex(KEY_MESSAGE));
				String date = Message.millisToDate(cur.getLong(cur.getColumnIndex(KEY_DATE)));
				String id = String.valueOf(cur.getLong(cur.getColumnIndex(KEY_ID)));
				//String count = cur.getString(cur.getColumnIndex(KEY_UNREAD));
				smsList.add(new String[]{name, message, date, id});
			}while(cur.moveToNext());
		}
		close(cur);
		return smsList;
	}
	
	/**
	 * Get all of the last messages sent from every contact.
	 * @return : List<String[]> the information needed to 
	 * display the conversations.
	 */
	public List<String[]>  getConversations()
	{
		
		String orderQuery = "(SELECT " + 
				SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_NAME + ", " +
				SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_NUMBER + ", " + 
				SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_UNREAD + ", " + 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_MESSAGE + ", " + 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_REFERENCE + ", " + 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_DATE + " FROM " + 
				SQLitehelper.TRUSTED_TABLE_NAME + ", " + 
				SQLitehelper.NUMBERS_TABLE_NAME + ", " + 
				SQLitehelper.MESSAGES_TABLE_NAME + " WHERE " + 
				SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_ID + " = " + 
				SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_REFERENCE + " AND " + 
				SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_ID + " = " +
				SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_REFERENCE + " AND " + 
				SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_MESSAGE + " IS NOT NULL " +
				"ORDER BY " + SQLitehelper.MESSAGES_TABLE_NAME + "." + KEY_DATE + ")";
		open();
		Cursor cur = db.query(orderQuery, new String[]{
				KEY_NAME, KEY_NUMBER, KEY_UNREAD, KEY_MESSAGE},
				null, null, KEY_REFERENCE, null, KEY_DATE + " DESC");
		
		List<String[]> sms = new ArrayList<String[]>();
		
		while (cur.moveToNext())
		{
			String address = cur.getString(cur.getColumnIndex(KEY_NUMBER));
			String count = cur.getString(cur.getColumnIndex(KEY_UNREAD));
			String name = cur.getString(cur.getColumnIndex(KEY_NAME));
			String message = cur.getString(cur.getColumnIndex(KEY_MESSAGE));
			sms.add(new String[] {address, name, message, count});
		}
		close(cur);
		return sms;
	}
	
	/**
	 * Access the information stored in the database of a contact who has a certain number
	 * with the columns: name, number, key, verified.
	 * @param number : String the number of the contact to retrieve 
	 * @return TrustedContact, the row of data.
	 */
	public TrustedContact getRow(String number)
	{		
		open();
		Cursor idCur = db.rawQuery("SELECT " + KEY_REFERENCE + ", " + KEY_NUMBER + " FROM "
				+ SQLitehelper.NUMBERS_TABLE_NAME + " WHERE " + KEY_NUMBER + " = ?", new String[] {number});

		long id = 0;
		long num_id = 0;
		int i = 0;
		if (idCur.moveToFirst())
		{
			id = idCur.getInt(idCur.getColumnIndex(KEY_REFERENCE));
		}
		idCur.close();
		Cursor cur = db.query(SQLitehelper.TRUSTED_TABLE_NAME, new String[]
				{KEY_NAME /*, KEY_PUBLIC_KEY, KEY_SIGNATURE*/},
				KEY_ID +" = " + id, null, null, null, null);
		
		if (cur.moveToFirst())
        { 	
			TrustedContact tc = new TrustedContact (cur.getString(cur.getColumnIndex(KEY_NAME)));
			cur.close();
			Cursor pCur = db.query(SQLitehelper.TRUSTED_TABLE_NAME + ", " + SQLitehelper.NUMBERS_TABLE_NAME,
					null, SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_ID + " = " + 
					SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_REFERENCE + " AND " + 
					SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_ID + " = " + id,
					null, null, null, null);

			if (pCur.moveToFirst())
			{
				i = 0;
				do
				{
					num_id = pCur.getLong(pCur.getColumnIndex(KEY_ID));
					tc.addNumber(new Number (num_id, pCur.getString(pCur.getColumnIndex(KEY_NUMBER)),
							pCur.getInt(pCur.getColumnIndex(KEY_TYPE)),
							pCur.getInt(pCur.getColumnIndex(KEY_UNREAD)),
							pCur.getBlob(pCur.getColumnIndex(KEY_PUBLIC_KEY)),
							pCur.getBlob(pCur.getColumnIndex(KEY_SYMMETRIC_KEY)),
							pCur.getBlob(pCur.getColumnIndex(KEY_SIGNATURE))));

					//Retrieve the book paths
					String columns[] = getBookPath(num_id);
					tc.getNumber().get(i).setBookPath(columns[0]);
					tc.getNumber().get(i).setBookInversePath(columns[1]);
					
					//Retrieve the shared information
					columns = getSharedInfo(num_id);
					tc.getNumber().get(i).setSharedInfo1(columns[0]);
					tc.getNumber().get(i).setSharedInfo2(columns[1]);
					i++;
				}while(pCur.moveToNext());
			}
			close(pCur);
			
			
			return tc;
        }
		close(cur);
		return null;
	}
	
	/**
	 * Get all of the rows in the database with the columns
	 * @return : ArrayList<TrustedContact>, a list of all the
	 * contacts in the database
	 */
	public ArrayList<TrustedContact> getAllRows()
	{		
		open();
		Cursor cur = db.query(SQLitehelper.TRUSTED_TABLE_NAME, null,
				null, null, null, null, KEY_ID);
		
		ArrayList<TrustedContact> tc = new ArrayList<TrustedContact>();
				
		if (cur.moveToFirst())
        {
			int i = 0;
			int j = 0;
			long id = 0;
			long num_id = 0;
			do
			{
				tc.add(new TrustedContact (cur.getString(cur.getColumnIndex(KEY_NAME))));
				
				id = cur.getInt(cur.getColumnIndex(KEY_ID));
				Cursor pCur = db.query(SQLitehelper.TRUSTED_TABLE_NAME + ", " + 
						SQLitehelper.NUMBERS_TABLE_NAME, null,
						SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_ID + " = " + 
						SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_REFERENCE + " AND " + 
						SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_ID + " = " + id,
						null, null, null, null);

				if (pCur.moveToFirst())
				{
					j = 0;
					do
					{
						num_id = pCur.getLong(pCur.getColumnIndex(KEY_ID));
						
						tc.get(i).addNumber(new Number (num_id, pCur.getString(pCur.getColumnIndex(KEY_NUMBER)),
								pCur.getInt(pCur.getColumnIndex(KEY_TYPE)),
								pCur.getInt(pCur.getColumnIndex(KEY_UNREAD)),
								pCur.getBlob(pCur.getColumnIndex(KEY_PUBLIC_KEY)),
								pCur.getBlob(pCur.getColumnIndex(KEY_SYMMETRIC_KEY)),
								pCur.getBlob(pCur.getColumnIndex(KEY_SIGNATURE))));

						//Retrieve the book paths
						String columns[] = getBookPath(num_id);
						tc.get(i).getNumber().get(j).setBookPath(columns[0]);
						tc.get(i).getNumber().get(j).setBookInversePath(columns[1]);
						
						//Retrieve the shared information
						columns = getSharedInfo(num_id);
						tc.get(i).getNumber().get(j).setSharedInfo1(columns[0]);
						tc.get(i).getNumber().get(j).setSharedInfo2(columns[1]);
						j++;
						
					}while(pCur.moveToNext());
				}
				pCur.close();
				
				i++;
			}while (cur.moveToNext());
			
			close(cur);
			return tc;
        }
		close(cur);
		return null;
	}
	
	/**
	 * Get number of messages that are unread for all numbers
	 * @return : int, the number of messages unread for all numbers
	 */
	public int getUnreadMessageCount() {
		open();
		Cursor cur = db.query(SQLitehelper.NUMBERS_TABLE_NAME, new String[]{"SUM("+KEY_UNREAD+")"},
				null, null, null, null, KEY_ID);
		int count = 0;
		if (cur.moveToFirst())
		{
			count = cur.getInt(0);
		}
		close(cur);
		return count;
	}
	
	/**
	 * Get the unread message count for a given number
	 * @param number : String a number
	 * @return : int the number of unread messages
	 */
	public int getUnreadMessageCount(String number) {
		open();
		Cursor cur = db.query(SQLitehelper.NUMBERS_TABLE_NAME, new String[]{KEY_UNREAD},
				KEY_NUMBER + " = ?", new String[]{SMSUtility.format(number)}, null, null, KEY_ID);
		int count = 0;
		if (cur.moveToFirst())
		{
			count = cur.getInt(cur.getColumnIndex(KEY_UNREAD));
		}
		close(cur);
		return count;
	}
	
	/**
	 * Store the user's public key, private key and signature.
	 * ***Can only be set Once
	 * @param user : User 
	 */
	public void setUser(User user)
	{
		if (!isKeyGen())
		{
			ContentValues cv = new ContentValues();
			
			//add given values to a row
	        cv.put(KEY_PUBLIC_KEY, user.getPublicKey());
	        cv.put(KEY_PRIVATE_KEY, user.getPrivateKey());
	        cv.put(KEY_SIGNATURE, user.getSignature());
	        
	        //Insert the row into the database
	        open();
	        db.insert(SQLitehelper.USER_TABLE_NAME, null, cv);
	        close();
		}
	}
	
	/**
	 * Get the user's public key, private key and signature
	 * @return : User
	 */
	public User getUserRow()
	{
		open();
		Cursor cur = db.query(SQLitehelper.USER_TABLE_NAME, 
				new String[] {KEY_PUBLIC_KEY, KEY_PRIVATE_KEY, KEY_SIGNATURE},
				null, null, null, null, null);
		if (cur.moveToFirst())
		{
			User user = new User(cur.getBlob(cur.getColumnIndex(KEY_PUBLIC_KEY)),
					cur.getBlob(cur.getColumnIndex(KEY_PRIVATE_KEY)), 
					cur.getBlob(cur.getColumnIndex(KEY_SIGNATURE)));
			close(cur);
			return user;
		}
		
		close(cur);
		return null;
	}
	
	/**
	 * Used to determine if the user's key has been generated
	 * @return : boolean
	 * true if there is a key already in the database,
	 * false if there is no key in the database.
	 */
	public boolean isKeyGen()
	{
		Cursor cur = db.query(SQLitehelper.USER_TABLE_NAME, new String[]
				{KEY_PUBLIC_KEY, KEY_PUBLIC_KEY, KEY_SIGNATURE}, null, null, null, null, null);
		if (cur.moveToFirst())
		{
			close(cur);
			return true;
		}
		close(cur);
		return false;
	}

	/**
	 * Update all of the values in a row
	 * @param tc : Trusted Contact, the new values for the row
	 * @param number : the number of the contact in the database
	 */
	public void updateRow (TrustedContact tc, String number)
	{
		long id = getId(SMSUtility.format(number));
		updateTrustedRow(tc, number, id);
		
		updateNumberRow(tc.getNumber(), id);
		
	}
	
	/**
	 * Update all of the values in a row
	 * @param tc : Trusted Contact, the new values for the row
	 * @param number : the number of the contact in the database
	 */
	public void updateNumberType (TrustedContact tc, String number)
	{
		long id = getId(SMSUtility.format(number));
		updateTrustedRow(tc, number, id);
		
		updateNumberRowType(tc.getNumber(), id);
		
	}
	
	/**
	 * Update a TrustedContact row
	 * @param tc : TrustedContact the new information to be stored
	 * @param number : String a number owned by the contact
	 * @param id : long the id for the contact's database row
	 */
	public void updateTrustedRow (TrustedContact tc, String number, long id)
	{
		ContentValues cv = new ContentValues();
		if (id == 0)
		{
			id = getId(SMSUtility.format(number));
		}
		
		//Trusted Table
        cv.put(KEY_NAME, tc.getName());
        
        open();
		db.update(SQLitehelper.TRUSTED_TABLE_NAME, cv, KEY_ID + " = " + id, null);
		close();
	}
	
	/**
	 * Update a row from the Numbers table key
	 * @param Number the number object with the new key
	 */
	public void updateKey (Number number)
	{
		ContentValues cv = new ContentValues();
		
		long id = getId(number.getNumber());
		
        cv.put(KEY_PUBLIC_KEY, number.getPublicKey());
        cv.put(KEY_SIGNATURE, number.getSignature());
        
        open();
		db.update(SQLitehelper.NUMBERS_TABLE_NAME, cv, KEY_REFERENCE + " = " + id 
				+ " AND " + KEY_NUMBER + " LIKE ?" , new String[]{number.getNumber()});
		close();
	}
	
	/**
	 * Update a row from the Numbers table
	 * @param tc : TrustedContact the new information to be stored
	 * @param number : String a number owned by the contact
	 * @param id : long the id for the contact's database row
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
		
		cv.put(KEY_REFERENCE, id);
        cv.put(KEY_NUMBER, numb.getNumber());
        cv.put(KEY_TYPE, numb.getType());
        cv.put(KEY_UNREAD, numb.getUnreadMessageCount());
        cv.put(KEY_PUBLIC_KEY, numb.getPublicKey());
        cv.put(KEY_SIGNATURE, numb.getSignature());
        
        open();
        db.update(SQLitehelper.NUMBERS_TABLE_NAME, cv, KEY_REFERENCE + " = " + id 
				+ " AND " + KEY_NUMBER + " LIKE ?" , new String[]{number});
		close();
		
		updateBookPaths(num_id, numb.getBookPath(), numb.getBookInversePath());
		updateSharedInfo(num_id, numb.getSharedInfo1(), numb.getSharedInfo2());
	}
	
	/**
	 * Update a row from the Numbers table
	 * @param tc : TrustedContact the new information to be stored
	 * @param number : String a number owned by the contact
	 * @param id : long the id for the contact's database row
	 */
	public void updateNumberRowType (ArrayList<Number> number, long id)
	{
		ContentValues cv = new ContentValues();
	
		for(int i = 0; i < number.size(); i++)
		{	
	        cv.put(KEY_TYPE, number.get(i).getType());
	        
	        open();
	        db.update(SQLitehelper.NUMBERS_TABLE_NAME, cv, KEY_REFERENCE + " = " + id 
	        		+ " AND " + KEY_NUMBER + " = " + number.get(i).getNumber(), null);
			close();
			
			cv.clear();
		}
		
	}
	
	/**
	 * Update a row from the Numbers table
	 * @param tc : TrustedContact the new information to be stored
	 * @param number : String a number owned by the contact
	 * @param id : long the id for the contact's database row
	 */
	public void updateNumberRow (ArrayList<Number> number, long id)
	{
		ContentValues cv = new ContentValues();
	
		for(int i = 0; i < number.size(); i++)
		{
			cv.put(KEY_REFERENCE, id);
	        cv.put(KEY_NUMBER, number.get(i).getNumber());
	        cv.put(KEY_TYPE, number.get(i).getType());
	        cv.put(KEY_UNREAD, number.get(i).getUnreadMessageCount());
	        cv.put(KEY_PUBLIC_KEY, number.get(i).getPublicKey());
	        cv.put(KEY_SIGNATURE, number.get(i).getSignature());
	        open();
	        int num = db.update(SQLitehelper.NUMBERS_TABLE_NAME, cv, KEY_REFERENCE + " = " + id 
	        		+ " AND " + KEY_ID + " = " + number.get(i).getId(), null);
			close();
			
			cv.clear();
			if(num == 0)
			{
				addNumbersRow(id, number.get(i));
			}
		}
		
	}
	
	/**
	 * Deletes the rows with the given number
	 * @param number : String, the primary number of the contact to be deleted
	 * @return : boolean
	 * true if the contacts were deleted properly
	 * false if the contacts were not deleted properly
	 */
	public boolean removeRow(String number)
	{
		number = SMSUtility.format(number);
		long id = getId(number);
		
		int num = 0;
		open();
		num = db.delete(SQLitehelper.TRUSTED_TABLE_NAME, KEY_ID + " = " + id, null);
		close();
		
		if (num == 0)
		{
			return false;
		}
		return true;
	}

	/**
	 * Checks if the given number is a trusted contact's number
	 * @param number String, the number of the potential trusted contact
	 * @return boolean
	 * true, if the contact is found in the database and is in the trusted state.
	 * false, if the contact is not found in the database or is not the trusted state.
	 * 
	 * A contact is in the trusted state if they have a key (!= null)
	 */
	public boolean isTrustedContact (String number)
	{
		TrustedContact tc = getRow(SMSUtility.format(number));
		if (tc != null)
		{
			if (!tc.getNumber(number).isPublicKeyNull())
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param tc : TrustedContact the contact to check for a trusted number
	 * @return : boolean whether the contact has a trusted number or not
	 * true if the contact is trusted
	 * false if the contact is not trusted.
	 * 
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
	 * @param contacts : TrustedContact the contact to check for a trusted number
	 * @return : boolean whether the contact has a trusted number or not
	 * true if the contact is trusted
	 * false if the contact is not trusted.
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
	
	/**
	 * Adding a message to the queue to be sent when there is service to send the message. 
	 * Once the message has been sent it will be removed from the queue.
	 * @param number : String the number for the contact that the message will be sent to
	 * @param message : String the message that will be sent to the contact with the given number.
	 * **Please note the message is not changed, it will be stored and sent as is.  
	 */
	public void addMessageToQueue (String number, String message)
	{
		long numberReference = getNumberId(number);
		ContentValues cv = new ContentValues();

        cv.put(KEY_NUMBER_REFERENCE, numberReference);
        cv.put(KEY_MESSAGE, message);
        
        open();
        db.insert(SQLitehelper.QUEUE_TABLE_NAME, null, cv);
		close();
	}
	
	/**
	 * Get the first element within the queue.
	 * @return : Queue the number and message stored in the queue
	 */
	public Queue getFirstInQueue ()
	{
		open();
		Cursor cur = db.query(SQLitehelper.QUEUE_TABLE_NAME, new String[]{KEY_ID, 
				KEY_NUMBER_REFERENCE, KEY_MESSAGE}, KEY_ID + 
				" = (SELECT MIN(" + KEY_ID + ") FROM " + SQLitehelper.QUEUE_TABLE_NAME +")",
				null, null, null, null);
		
		if (cur.moveToFirst())
		{
			long id = cur.getLong(cur.getColumnIndex(KEY_ID));
			Queue entry = new Queue(getNumber(cur.getLong(cur.getColumnIndex(KEY_NUMBER_REFERENCE))),
					cur.getString(cur.getColumnIndex(KEY_MESSAGE)), id);
			close(cur);
			//deleteQueueEntry(id);
			return entry;
		}
		close(cur);
		return null;
	}
	
	/**
	 * Delete a given entry from the queue
	 * @param id : long, the private key
	 */
	public void deleteQueueEntry (long id)
	{
		open();
		db.delete(SQLitehelper.QUEUE_TABLE_NAME, KEY_ID + " = " + id, null);
		close();
	}
	
	/**
	 * Delete the first entry in the queue
	 */
	public void deleteFirstQueueEntry()
	{
		open();
		db.delete(SQLitehelper.QUEUE_TABLE_NAME, KEY_ID + " = (SELECT MIN(" + KEY_ID + ") FROM " + 
				SQLitehelper.QUEUE_TABLE_NAME +")", null);
		close();
	}
	
	/**
	 * Get the current length of the queue
	 * @return : long the length of the queue
	 */
	public int queueLength()
	{
		open();
		Cursor cur = db.query(SQLitehelper.QUEUE_TABLE_NAME, 
				new String[]{"COUNT("+KEY_ID+")"},
				null, null, null, null, null);
		
		if (cur.moveToFirst())
		{
			int count = cur.getInt(0);
			close(cur);
			return count;
		}
		close(cur);
		return 0;
	}
	
	//TODO remove
	/*public void placeOnTop(String number, String message, long id)
	{
		long numberReference = getNumberId(number);
		ContentValues cv = new ContentValues();

        cv.put(KEY_NUMBER_REFERENCE, numberReference);
        cv.put(KEY_MESSAGE, message);
        
        open();
        db.insert(SQLitehelper.QUEUE_TABLE_NAME, null, cv);
		close();
	}*/
}
