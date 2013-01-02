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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLitehelper is used to create the database to store all needed
 * information for tinfoil-sms.
 */
public class SQLitehelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "tinfoil-sms.db";
	private static final int DATABASE_VERSION = 1;
	
	//Table Names
	public static final String USER_TABLE_NAME = "user";
    public static final String TRUSTED_TABLE_NAME = "trusted_contact";
    public static final String NUMBERS_TABLE_NAME = "numbers";
    public static final String SHARED_INFO_TABLE_NAME = "shared_information";
    public static final String BOOK_PATHS_TABLE_NAME = "book_paths";
    public static final String MESSAGES_TABLE_NAME = "messages";
    public static final String QUEUE_TABLE_NAME = "queue";
    
    //TODO document db create schema
    private static final String SHARED_INFO_TABLE_CREATE =
            "CREATE TABLE " + SHARED_INFO_TABLE_NAME + 
            " (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " reference INTEGER REFERENCES numbers (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE, " +
            " shared_info_1 TEXT," +
            " shared_info_2 TEXT);";
    
    private static final String BOOK_PATHS_TABLE_CREATE =
            "CREATE TABLE " + BOOK_PATHS_TABLE_NAME + 
            " (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " reference INTEGER REFERENCES numbers (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE, " +
            " book_path TEXT," +
            " book_inverse_path TEXT);";
    
    
    private static final String USER_TABLE_CREATE =
            "CREATE TABLE " + USER_TABLE_NAME + 
            " (public_key BLOB," +
            " private_key BLOB," +
            " signature BLOB);";
    
    private static final String TRUSTED_TABLE_CREATE =
            "CREATE TABLE " + TRUSTED_TABLE_NAME + 
            " (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " name TEXT );";
    
    private static final String NUMBERS_TABLE_CREATE =
            "CREATE TABLE " + NUMBERS_TABLE_NAME + 
            " (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " reference INTEGER REFERENCES trusted_contact (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE, " +
            " number TEXT," +
            " type INTEGER," +
            " unread INTEGER," +
            " public_key BLOB," +
            " signature BLOB);";
    
    private static final String MESSAGES_TABLE_CREATE =
            "CREATE TABLE " + MESSAGES_TABLE_NAME + 
            " (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " reference INTEGER REFERENCES numbers (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE," +
            " message TEXT," +
            " date INTEGER," +
            " sent INTEGER);";
    
    private static final String QUEUE_TABLE_CREATE =
            "CREATE TABLE " + QUEUE_TABLE_NAME + 
            " (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " number_reference INTEGER REFERENCES numbers (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE," +
            " message TEXT);";

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
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+ USER_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ TRUSTED_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ NUMBERS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ SHARED_INFO_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ BOOK_PATHS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ MESSAGES_TABLE_CREATE);
		db.execSQL("DROP TABLE IF EXISTS "+ QUEUE_TABLE_CREATE);
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