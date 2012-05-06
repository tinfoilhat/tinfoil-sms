package com.tinfoil.sms;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLitehelper extends SQLiteOpenHelper {

	
	private static final String DATABASE_NAME = "tinfoil-sms.db";
	private static final int DATABASE_VERSION = 2;
    public static final String TABLE_NAME = "trusted_contact";
    private static final String TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + 
                " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, number TEXT, key TEXT, verified INTEGER);";

    public SQLitehelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
		onCreate(db);
		
	}
}