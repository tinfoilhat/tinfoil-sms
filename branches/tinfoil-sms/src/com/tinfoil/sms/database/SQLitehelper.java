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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLitehelper is used to create the database to store all needed
 * information for tinfoil-sms.
 */
public class SQLitehelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "tinfoil-sms.db";
	
	/*
	 * Upgraded the version of the database since signature was removed from the
	 * user database.
	 */
	private static final int DATABASE_VERSION = 2;
	
	/* Table Names */
	public static final String USER_TABLE_NAME = "user";
    public static final String TRUSTED_TABLE_NAME = "trusted_contact";
    public static final String NUMBERS_TABLE_NAME = "numbers";
    public static final String SHARED_INFO_TABLE_NAME = "shared_information";
    public static final String BOOK_PATHS_TABLE_NAME = "book_paths";
    public static final String MESSAGES_TABLE_NAME = "messages";
    public static final String QUEUE_TABLE_NAME = "queue";
    public static final String EXCHANGE_TABLE_NAME = "exchange_messages";
    
    /* Create statements */
    private static final String SHARED_INFO_TABLE_CREATE =
            "CREATE TABLE " + SHARED_INFO_TABLE_NAME + 
            " ("+ DBAccessor.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " " + DBAccessor.KEY_REFERENCE + " INTEGER REFERENCES numbers (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE, " +
            " " + DBAccessor.KEY_SHARED_INFO_1 + " TEXT," + 
            " " + DBAccessor.KEY_SHARED_INFO_2 + " TEXT);";
    
    private static final String BOOK_PATHS_TABLE_CREATE =
            "CREATE TABLE " + BOOK_PATHS_TABLE_NAME + 
            " (" + DBAccessor.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " " + DBAccessor.KEY_REFERENCE + " INTEGER REFERENCES numbers (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE, " +
            " " + DBAccessor.KEY_BOOK_PATH + " TEXT," +
            " " + DBAccessor.KEY_BOOK_INVERSE_PATH + " TEXT);";
    
    
    private static final String USER_TABLE_CREATE =
            "CREATE TABLE " + USER_TABLE_NAME + 
            " (" + DBAccessor.KEY_PUBLIC_KEY + " BLOB," +
            " " + DBAccessor.KEY_PRIVATE_KEY + " BLOB);";
    
    private static final String TRUSTED_TABLE_CREATE =
            "CREATE TABLE " + TRUSTED_TABLE_NAME + 
            " (" + DBAccessor.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " " + DBAccessor.KEY_NAME + " TEXT );";
    
    private static final String NUMBERS_TABLE_CREATE =
            "CREATE TABLE " + NUMBERS_TABLE_NAME + 
            " (" + DBAccessor.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " " + DBAccessor.KEY_REFERENCE + " INTEGER REFERENCES trusted_contact (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE, " +
            " " + DBAccessor.KEY_NUMBER + " TEXT," +
            " " + DBAccessor.KEY_TYPE + " INTEGER," +
            " " + DBAccessor.KEY_UNREAD + " INTEGER," +
            " " + DBAccessor.KEY_PUBLIC_KEY + " BLOB," +
            " " + DBAccessor.KEY_SIGNATURE + " BLOB," +
            " " + DBAccessor.KEY_NONCE_ENCRYPT + " BLOB," +
            " " + DBAccessor.KEY_NONCE_DECRYPT + " BLOB," +
            " " + DBAccessor.KEY_INITIATOR + " INTEGER," +
            " " + DBAccessor.KEY_EXCHANGE_SETTING + " INTEGER);";
    
    private static final String MESSAGES_TABLE_CREATE =
            "CREATE TABLE " + MESSAGES_TABLE_NAME + 
            " (" + DBAccessor.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " " + DBAccessor.KEY_REFERENCE + " INTEGER REFERENCES numbers (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE," +
            " " + DBAccessor.KEY_MESSAGE + " TEXT," +
            " " + DBAccessor.KEY_DATE + " INTEGER," +
            " " + DBAccessor.KEY_SENT + " INTEGER);";
    
    private static final String QUEUE_TABLE_CREATE =
            "CREATE TABLE " + QUEUE_TABLE_NAME + 
            " (" + DBAccessor.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " " + DBAccessor.KEY_NUMBER_REFERENCE + " INTEGER REFERENCES numbers (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE," +
            " " + DBAccessor.KEY_MESSAGE + " TEXT," +
            " " + DBAccessor.KEY_EXCHANGE + " INTEGER);";
    
    private static final String EXCHANGE_TABLE_CREATE =
    		"CREATE TABLE " + EXCHANGE_TABLE_NAME + 
    		" (" + DBAccessor.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
    		" " + DBAccessor.KEY_NUMBER_REFERENCE + " INTEGER REFERENCES numbers (id)" +
    		" ON DELETE CASCADE ON UPDATE CASCADE," +
    		" " + DBAccessor.KEY_EXCHANGE_MESSAGE + " TEXT);";

    public SQLitehelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	db.execSQL(USER_TABLE_CREATE);
        db.execSQL(TRUSTED_TABLE_CREATE);
        db.execSQL(NUMBERS_TABLE_CREATE);
        db.execSQL(SHARED_INFO_TABLE_CREATE);
        db.execSQL(BOOK_PATHS_TABLE_CREATE);
        db.execSQL(MESSAGES_TABLE_CREATE);
        db.execSQL(QUEUE_TABLE_CREATE);
        db.execSQL(EXCHANGE_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v("Database Update", "tables are being deleted to update from version "
				+ oldVersion + " to version " + newVersion); 
		db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + TRUSTED_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + NUMBERS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + SHARED_INFO_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + BOOK_PATHS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + QUEUE_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + EXCHANGE_TABLE_NAME);
		onCreate(db);		
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);		
		if (!db.isReadOnly())
		{
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}
}