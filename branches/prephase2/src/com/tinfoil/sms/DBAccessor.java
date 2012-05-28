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

package com.tinfoil.sms;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Creates a database that is read and write and provides methods to facilitate the reading and writing to the database. 
 */
public class DBAccessor {
	
	public static final String KEY_PUBLIC_KEY = "public_key";
	public static final String KEY_PRIVATE_KEY = "private_key";
	public static final String KEY_SIGNATURE = "signature";
	
	public static final String KEY_SHARED_INFO_1 = "shared_info_1";
	public static final String KEY_SHARED_INFO_2 = "shared_info_2";

	public static final String KEY_BOOK_PATH = "book_path";
	public static final String KEY_BOOK_INVERSE_PATH = "book_inverse_path";
	
	public static final String KEY_ID = "id";
	public static final String KEY_NAME = "name";
		
	//public static final String KEY_KEY = "key";
	
	public static final String KEY_REFERENCE = "reference";
	public static final String KEY_NUMBER = "number";
	
	private SQLiteDatabase db;
	private SQLitehelper contactDatabase;
	//private ContentResolver cr;
	
	/**
	 * Creates a database that is read and write
	 * @param c	: Context, where the database is available
	 */
	public DBAccessor (Context c)
	{
		contactDatabase = new SQLitehelper(c);
		db = contactDatabase.getWritableDatabase();
		//cr = c.getContentResolver();
	}
	
	/**
	 * Adds a row to the contacts table, trusted_contact
	 * @param name : String the name of the contact
	 * @param key : String the contact's public key, null if not received
	 */
	public void addRow (String name, String publicKey)
	{
		//Check if name, number or key contain any ';'
		//if (!conflict(number))
		//{
			ContentValues cv = new ContentValues();
				
			//add given values to a row
	        cv.put(KEY_NAME, name);
	        //cv.put(KEY_NUMBER, number);
	        //cv.put(KEY_KEY, key);
	        cv.put(KEY_PUBLIC_KEY, publicKey);
	        //cv.put(KEY_VERIFIED, verified);
	
	        //Insert the row into the database
	        open();
	        db.insert(SQLitehelper.TRUSTED_TABLE_NAME, null, cv);
	        close();
		//}
		
	}

	/**
	 * Add a row to the numbers table.
	 * @param reference : int the reference id of the contact the number belongs to
	 * @param number : String the number 
	 */
	public void addRow (int reference, String number)
	{
		//Check if name, number or key contain any ';'
		//if (!conflict(number))
		//{
			ContentValues cv = new ContentValues();
				
			//add given values to a row
	        cv.put(KEY_REFERENCE, reference);
	        cv.put(KEY_NUMBER, number);
	
	        //Insert the row into the database
	        open();
	        db.insert(SQLitehelper.NUMBERS_TABLE_NAME, null, cv);
	        close();
		//}
		
	}
	
	/**
	 * Adds a row to the contacts table, trusted_contact
	 * @param tc : TrustedContact contains all the required information for the contact
	 */
	public void addRow (TrustedContact tc)
	{
		//Check if name, number or key contain any ';'
		//if (!conflict(tc.getPrimaryNumber()))
		//{
			//tc.setPrimaryNumber(ContactRetriever.format(tc.getPrimaryNumber()));
			ContentValues cv = new ContentValues();
			
			//add given values to a row
	        cv.put(KEY_NAME, tc.getName());
	        //cv.put(KEY_NUMBER, tc.getPrimaryNumber());
	        //cv.put(KEY_KEY, tc.getKey());
	        cv.put(KEY_PUBLIC_KEY, tc.getKey());
	        //cv.put(KEY_KEY, (String) null);
	        //cv.put(KEY_VERIFIED, tc.getVerified());
	        
	        //Insert the row into the database
	        open();
	        int id = (int) db.insert(SQLitehelper.TRUSTED_TABLE_NAME, null, cv);
	        close();
	        if (!tc.isNumbersEmpty())
	        {
	        	//int id = getId(tc.getPrimaryNumber());
	        	//int id = getId(tc.getNumber(0));
	        	for (int i = 0; i< tc.getNumberSize();i++)
	        	{
	        		addRow(id, ContactRetriever.format(tc.getNumber(i)));
	        	}
	        }
	              
	}
	
	private int getId(String number)
	{
		open();
		/*Cursor cur = db.query(SQLitehelper.TRUSTED_TABLE_NAME, new String[] {"id"},
				"name = "+ name, null, null, null, null);*/
		Cursor cur = db.query(SQLitehelper.NUMBERS_TABLE_NAME, new String[] {KEY_REFERENCE},
				KEY_NUMBER + " = "+ number, null, null, null, null);
		
		if (cur.moveToFirst())
		{
			int id = cur.getInt(cur.getColumnIndex((KEY_REFERENCE)));
			close(cur);
			return id;
		}
		close(cur);
		return 0;
	}
	
	
	/**
	 * **Note Still a working project
	 * This will be used to sync the contacts in tinfoil-sms's
	 * database with the contacts in the native database.
	 * @param name : String, the name of the contact to be added
	 * @param number : String, the number of the contact to be added
	 * @return : String
	 */
	/*public String nativeContact (String name, String number)
	{
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, 
		null, null, null, null);
		//ContactsContract.Contacts.DISPLAY_NAME +" = " + name,
		while (cur.moveToNext())
		{
			String id = cur.getString(
					cur.getColumnIndex(ContactsContract.Contacts._ID));
			String found_name = cur.getString(
		            cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			if (found_name.equalsIgnoreCase(name))
			{
		        Cursor pCur = cr.query(
		 	 		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
		 	 		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
		 	 		    new String[]{id}, null);
		        if (pCur.moveToNext())
		        {
		        	String tempNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		        	if (tempNumber.equalsIgnoreCase(number))
		        	{
		        		return "Contact is already in dba";
		        	}
		        	else
		        	{
		        		return "Found " + found_name + " X" + tempNumber + "X " + number;
		        	}
		        }
		        else
		        {
		        	return "Found " + found_name;
		        }
			
			}
			else 
			{
				break;
			}
		}
		
		//Need to use Content Provider to add stuff to android's db
		
		return "Found Nothing!";
	}*/
	
	public boolean inDatabase(ArrayList<String> number)
	{
		for (int i = 0; i<number.size(); i++)
		{
			TrustedContact tc = getRow(ContactRetriever.format(number.get(i)));
			if (tc != null)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean inDatabase(String number)
	{
		if (getRow(ContactRetriever.format(number)) == null)
		{
			return false;
		}
		return true;
	}
	
    /**
     * Open the database to be used
     */
	public void open()
	{
		db = contactDatabase.getWritableDatabase();
	}
	
	/**
	 * Close the database
	 */
	public void close(Cursor cur)
	{
		cur.close();
		//contactDatabase.close();
		db.close();
	}
	
	public void close()
	{
		//contactDatabase.close();
		db.close();
	}
	
	/**
	 * Access the information stored in the database of a contact who has a certain number
	 * with the columns: name, number, key, verified.
	 * @param number : String the primary number of the contact to retrieve 
	 * @return TrustedContact, the row of data.
	 */
	/*public TrustedContact getRow(String primaryNumber)
	{		
		open();
		Cursor cur = db.query(SQLitehelper.TRUSTED_TABLE_NAME, null,
				KEY_NUMBER +" = " + primaryNumber, null, null, null, null);
		
		if (cur.moveToFirst())
        { 	
			TrustedContact tc = new TrustedContact (cur.getString(cur.getColumnIndex(KEY_NAME)),
					cur.getString(cur.getColumnIndex(KEY_NUMBER)), cur.getString(cur.getColumnIndex(KEY_KEY)),
					cur.getInt(cur.getColumnIndex(KEY_VERIFIED)));
			
			int id = cur.getInt(cur.getColumnIndex(KEY_ID));
			Cursor pCur = db.query(SQLitehelper.TRUSTED_TABLE_NAME + ", " + SQLitehelper.NUMBERS_TABLE_NAME, 
					new String[] {SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_NUMBER},
					SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_ID + " = " + 
					SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_REFERENCE + " AND " + 
					SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_ID + " = " + id,
					null, null, null, null);

			if (pCur.moveToFirst())
			{
				do
				{
					tc.addNumber(pCur.getString(pCur.getColumnIndex(KEY_NUMBER)));
				}while(pCur.moveToNext());
				pCur.close();
			}
			close(cur);
			return tc;
        }
		close(cur);
		return null;
	}*/
	
	/**
	 * Access the information stored in the database of a contact who has a certain number
	 * with the columns: name, number, key, verified.
	 * @param number : String the number of the contact to retrieve 
	 * @return TrustedContact, the row of data.
	 */
	public TrustedContact getRow(String number)
	{		
		open();
		
		/* If the number isn't found a primary number, might be better to just search this 
		 * db first then find the reference number, then search other db for id, thus searching
		 * through all numbers, cause primary numbers can change quite easily.
		 */
		Cursor idCur = db.query(SQLitehelper.NUMBERS_TABLE_NAME, 
				new String[] {KEY_REFERENCE, KEY_NUMBER}, KEY_NUMBER + " = " + number,
				null, null, null, null);
		int id = 0;
		if (idCur.moveToFirst())
		{
			id = idCur.getInt(idCur.getColumnIndex(KEY_REFERENCE));
		}
		idCur.close();
		Cursor cur = db.query(SQLitehelper.TRUSTED_TABLE_NAME, null,
				KEY_ID +" = " + id, null, null, null, null);
		
		if (cur.moveToFirst())
        { 	
			/*TrustedContact tc = new TrustedContact (cur.getString(cur.getColumnIndex(KEY_NAME)),
					cur.getString(cur.getColumnIndex(KEY_NUMBER)), cur.getString(cur.getColumnIndex(KEY_KEY)),
					cur.getInt(cur.getColumnIndex(KEY_VERIFIED)));
			TrustedContact tc = new TrustedContact (cur.getString(cur.getColumnIndex(KEY_NAME)),
					cur.getString(cur.getColumnIndex(KEY_KEY)),
					cur.getInt(cur.getColumnIndex(KEY_VERIFIED)));*/
			TrustedContact tc = new TrustedContact (cur.getString(cur.getColumnIndex(KEY_NAME)),
					cur.getString(cur.getColumnIndex(KEY_PUBLIC_KEY)));
			
			//id = cur.getInt(cur.getColumnIndex(KEY_ID));
			Cursor pCur = db.query(SQLitehelper.TRUSTED_TABLE_NAME + ", " + SQLitehelper.NUMBERS_TABLE_NAME, 
					new String[] {SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_NUMBER},
					SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_ID + " = " + 
					SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_REFERENCE + " AND " + 
					SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_ID + " = " + id,
					null, null, null, null);

			if (pCur.moveToFirst())
			{
				do
				{
					tc.addNumber(pCur.getString(pCur.getColumnIndex(KEY_NUMBER)));
				}while(pCur.moveToNext());
				pCur.close();
			}
			close(cur);
			return tc;
        }
		close(cur);
		return null;
	}
	
	/**
	 * Get all of the rows in the database with the columns
	 * name, number, key, verified.	
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
			do
			{
				/*tc.add(new TrustedContact (cur.getString(cur.getColumnIndex(KEY_NAME)),
						cur.getString(cur.getColumnIndex(KEY_NUMBER)), cur.getString(cur.getColumnIndex(KEY_KEY)),
						cur.getInt(cur.getColumnIndex(KEY_VERIFIED))));
				tc.add(new TrustedContact (cur.getString(cur.getColumnIndex(KEY_NAME)),
						cur.getString(cur.getColumnIndex(KEY_KEY)),
						cur.getInt(cur.getColumnIndex(KEY_VERIFIED))));*/
				String key = cur.getString(cur.getColumnIndex(KEY_PUBLIC_KEY));
				tc.add(new TrustedContact (cur.getString(cur.getColumnIndex(KEY_NAME)),
						key));
				
				int id = cur.getInt(cur.getColumnIndex(KEY_ID));
				Cursor pCur = db.query(SQLitehelper.TRUSTED_TABLE_NAME + ", " + SQLitehelper.NUMBERS_TABLE_NAME, 
						new String[] {SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_NUMBER},
						SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_ID + " = " + 
						SQLitehelper.NUMBERS_TABLE_NAME + "." + KEY_REFERENCE + " AND " + 
						SQLitehelper.TRUSTED_TABLE_NAME + "." + KEY_ID + " = " + id,
						null, null, null, null);

				if (pCur.moveToFirst())
				{
					do
					{
						tc.get(i).addNumber(pCur.getString(pCur.getColumnIndex(KEY_NUMBER)));
					}while(pCur.moveToNext());
					//pCur.close();
					//return tc;
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
	
	public TrustedContact getUserRow()
	{
		open();
		Cursor cur = db.query(SQLitehelper.USER_TABLE_NAME, null,
				null, null, null, null, null);
		if (cur.moveToFirst())
		{
			//get public, private and fingerprint, and set the name to "ME" 
		}
		
		close(cur);
		return null;
	}
	
	
	/**
	 * Update all of the values in a row
	 * @param tc : Trusted Contact, the new values for the row
	 * @param number : the number of the contact in the database
	 */
	public void updateRow (TrustedContact tc, String number)
	{
		open();
		removeRow(ContactRetriever.format(number));
		addRow(tc);
		close();
	}
		
	/**
	 * Deletes the rows with the given number
	 * @param number : String, the primary number of the contact to be deleted 
	 */
	public void removeRow(String number)
	{
		number = ContactRetriever.format(number);
		int id = getId(number);
		open();
		//db.delete(SQLitehelper.TRUSTED_TABLE_NAME, KEY_NUMBER + " = " + number, null);
		db.delete(SQLitehelper.TRUSTED_TABLE_NAME, KEY_ID + " = " + id, null);
		db.delete(SQLitehelper.NUMBERS_TABLE_NAME, KEY_REFERENCE + " = " + id, null);
		close();
	}

	/**
	 * Checks if the given number is a trusted contact's number
	 * @param number : String, the number of the potential trusted contact
	 * @return : boolean
	 * true, if the contact is found in the database and is in the trusted state.
	 * false, if the contact is not found in the database or is not the trusted state.
	 * 
	 * A contact is in the trusted state if they have a key (!= null) and
	 * they have send their public key the contact (verified = 2)
	 */
	public boolean isTrustedContact (String number)
	{
		//TrustedContact tc = getRow(number);
		
		//if (tc == null)
		//{
		TrustedContact tc = getRow(ContactRetriever.format(number));
		//}
		if (tc != null)
		{
			if (!tc.isKeyNull())// && tc.getVerified() == 2)
			{
				return true;
			}
		}
		return false;
	}
	
	
}
