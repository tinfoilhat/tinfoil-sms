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
	
	//private static final String PATH = "/data/data/com.tinfoil.sms/databases/" + DATABASE_NAME;
	
	/**
	 * Beta release v1.4.0+
	 */
	private static final int DB_V4 = 4;
	
	/**
	 * Beta release v1.3.0 - v1.3.2
	 */
	private static final int DB_V3 = 3;

	/**
	 * Beta release v1.0.0 - v1.2.6
	 */
	private static final int DB_V2 = 2;

	/**
	 * Pre-beta release of Tinfoil-SMS
	 */
	private static final int DB_V1 = 1;
	
	/**
	 * Upgraded the version of the database since signature was removed from the
	 * user database.
	 */
	private static final int DATABASE_VERSION = DB_V4;
	
	/* Table Names */
	public static final String USER_TABLE_NAME = "user";
    public static final String TRUSTED_TABLE_NAME = "trusted_contact";
    public static final String NUMBERS_TABLE_NAME = "numbers";
    public static final String SHARED_INFO_TABLE_NAME = "shared_information";
    public static final String BOOK_PATHS_TABLE_NAME = "book_paths";
    public static final String MESSAGES_TABLE_NAME = "messages";
    public static final String QUEUE_TABLE_NAME = "queue";
    public static final String EXCHANGE_TABLE_NAME = "exchange_messages";
    public static final String WALKTHROUGH_TABLE_NAME = "walkthrough";
    
    public static final String EXISTS_CLAUSE = "IF NOT EXISTS";
    
    /* Column Names */
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
	
	public static final String KEY_NONCE_ENCRYPT = "nonce_encrypt";
	public static final String KEY_NONCE_DECRYPT = "nonce_decrypt";
	
	public static final String KEY_INITIATOR = "initiator";
	public static final String KEY_EXCHANGE_SETTING = "exchange_setting";
	
	public static final String KEY_EXCHANGE = "exchange";
	public static final String KEY_EXCHANGE_MESSAGE = "key_message";
	
	public static final String KEY_INTRO = "intro";
	public static final String KEY_START_IMPORT = "start_import";
	public static final String KEY_IMPORT = "import";
	public static final String KEY_START_EXCHANGE = "start_exchange";
	public static final String KEY_SET_SECRET = "set_secret";
	public static final String KEY_KEY_SENT = "key_sent";
	public static final String KEY_PENDING = "pending";
	public static final String KEY_ACCEPT = "accept";
	public static final String KEY_SUCCESS = "success";
	public static final String KEY_CLOSE = "close";
	
	public static final String KEY_DRAFT = "draft";
	
	private static final String ALTER_NUMBERS_TABLE_DRAFT_UPDATE =
			"ALTER TABLE " + NUMBERS_TABLE_NAME + " ADD COLUMN "
			+ KEY_DRAFT + " TEXT DEFAULT \"\";";
    
    /* Create statements */
    private static final String SHARED_INFO_TABLE_CREATE =
            "CREATE TABLE " + EXISTS_CLAUSE + " " + SHARED_INFO_TABLE_NAME + 
            " ("+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " " + KEY_REFERENCE + " INTEGER REFERENCES numbers (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE, " +
            " " + KEY_SHARED_INFO_1 + " TEXT," + 
            " " + KEY_SHARED_INFO_2 + " TEXT);";
    
    private static final String BOOK_PATHS_TABLE_CREATE =
            "CREATE TABLE " + EXISTS_CLAUSE + " " + BOOK_PATHS_TABLE_NAME + 
            " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " " + KEY_REFERENCE + " INTEGER REFERENCES numbers (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE, " +
            " " + KEY_BOOK_PATH + " TEXT," +
            " " + KEY_BOOK_INVERSE_PATH + " TEXT);";
    
    private static final String USER_TABLE_CREATE =
            "CREATE TABLE " + EXISTS_CLAUSE + " " + USER_TABLE_NAME + 
            " (" + KEY_PUBLIC_KEY + " BLOB," +
            " " + KEY_PRIVATE_KEY + " BLOB);";
    
    private static final String TRUSTED_TABLE_CREATE =
            "CREATE TABLE " + EXISTS_CLAUSE + " " + TRUSTED_TABLE_NAME + 
            " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " " + KEY_NAME + " TEXT );";
    
    private static final String NUMBERS_TABLE_CREATE =
            "CREATE TABLE " + EXISTS_CLAUSE + " " + NUMBERS_TABLE_NAME + 
            " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " " + KEY_REFERENCE + " INTEGER REFERENCES trusted_contact (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE, " +
            " " + KEY_NUMBER + " TEXT UNIQUE," +
            " " + KEY_DRAFT + "TEXT DEFAULT \"\"," +
            " " + KEY_TYPE + " INTEGER," +
            " " + KEY_UNREAD + " INTEGER," +
            " " + KEY_PUBLIC_KEY + " BLOB," +
            " " + KEY_SIGNATURE + " BLOB," +
            " " + KEY_NONCE_ENCRYPT + " BLOB," +
            " " + KEY_NONCE_DECRYPT + " BLOB," +
            " " + KEY_INITIATOR + " INTEGER," +
            " " + KEY_EXCHANGE_SETTING + " INTEGER);";
    
    private static final String MESSAGES_TABLE_CREATE =
            "CREATE TABLE " + EXISTS_CLAUSE + " " + MESSAGES_TABLE_NAME + 
            " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " " + KEY_REFERENCE + " INTEGER REFERENCES numbers (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE," +
            " " + KEY_MESSAGE + " TEXT," +
            " " + KEY_DATE + " INTEGER," +
            " " + KEY_SENT + " INTEGER);";
    
    private static final String QUEUE_TABLE_CREATE =
            "CREATE TABLE " + EXISTS_CLAUSE + " " + QUEUE_TABLE_NAME + 
            " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
            " " + KEY_NUMBER_REFERENCE + " INTEGER REFERENCES numbers (id)" +
            " ON DELETE CASCADE ON UPDATE CASCADE," +
            " " + KEY_MESSAGE + " TEXT," +
            " " + KEY_EXCHANGE + " INTEGER);";
    
    private static final String EXCHANGE_TABLE_CREATE =
    		"CREATE TABLE " + EXISTS_CLAUSE + " " + EXCHANGE_TABLE_NAME + 
    		" (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL," +
    		" " + KEY_NUMBER_REFERENCE + " INTEGER REFERENCES numbers (id)" +
    		" ON DELETE CASCADE ON UPDATE CASCADE," +
    		" " + KEY_EXCHANGE_MESSAGE + " TEXT);";
    
    private static final String WALKTHROUGH_TABLE_CREATE =
    		"CREATE TABLE " + EXISTS_CLAUSE + " " + WALKTHROUGH_TABLE_NAME + 
    		" (" + KEY_ID + " INTEGER UNIQUE," +
    		" " + KEY_INTRO + " INTEGER NOT NULL," +
    		" " + KEY_START_IMPORT + " INTEGER NOT NULL," +
    		" " + KEY_IMPORT + " INTEGER NOT NULL," +
    		" " + KEY_START_EXCHANGE + " INTEGER NOT NULL," +
    		" " + KEY_SET_SECRET + " INTEGER NOT NULL," +
    		" " + KEY_KEY_SENT + " INTEGER NOT NULL," +
    		" " + KEY_PENDING + " INTEGER NOT NULL," +
    		" " + KEY_ACCEPT + " INTEGER NOT NULL," +
    		" " + KEY_SUCCESS + " INTEGER NOT NULL," +
    		" " + KEY_CLOSE + " INTEGER NOT NULL);";

    private static final String INSERT_WALKTHROUGH = "INSERT OR IGNORE INTO "
    		+ WALKTHROUGH_TABLE_NAME + " (" + KEY_ID + ", " + KEY_INTRO + ", "
    		+ KEY_START_IMPORT + ", " + KEY_IMPORT + ", " + KEY_START_EXCHANGE
    		+ ", " + KEY_SET_SECRET + ", " + KEY_KEY_SENT + "," + KEY_PENDING + ", " + KEY_ACCEPT + ","
    		+ KEY_SUCCESS + ", " + KEY_CLOSE + ") VALUES (0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);";
    
	public SQLitehelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        
    }
	
	public SQLiteDatabase getDB()
	{
		//TODO look into localization error	
		return this.getWritableDatabase();
		//SQLiteDatabase.openDatabase(PATH, null, SQLiteDatabase.CREATE_IF_NECESSARY
				//| SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);
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
        db.execSQL(WALKTHROUGH_TABLE_CREATE);        
        db.execSQL(INSERT_WALKTHROUGH);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v("Database Update", "tables are being deleted to update from version "
				+ oldVersion + " to version " + newVersion);
		
		if(oldVersion <= DB_V1)
		{
			// Using an unreleased version of the database. Clear and re-create.
			db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + TRUSTED_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + NUMBERS_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + SHARED_INFO_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + BOOK_PATHS_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + QUEUE_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + EXCHANGE_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + WALKTHROUGH_TABLE_NAME);
			onCreate(db);
		}
		else {
		
			// Tweak the database to be consistent with the current database version.
			if(oldVersion == DB_V2)
			{
				db.execSQL(WALKTHROUGH_TABLE_CREATE);
				db.execSQL(INSERT_WALKTHROUGH);
			}
			
			if(oldVersion <= DB_V3)
			{
				db.execSQL(ALTER_NUMBERS_TABLE_DRAFT_UPDATE);
			}
		}
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