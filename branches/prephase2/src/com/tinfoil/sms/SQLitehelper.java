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

public class SQLitehelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "tinfoil-sms.db";
	private static final int DATABASE_VERSION = 2;
	public static final String USER_TABLE_NAME = "user";
    public static final String TRUSTED_TABLE_NAME = "trusted_contact";
    public static final String NUMBERS_TABLE_NAME = "numbers";
    private static final String USER_TABLE_CREATE =
            "CREATE TABLE " + USER_TABLE_NAME + 
            " (public_key BLOB, private_key BLOB, fingerprint BLOB);";
    private static final String TRUSTED_TABLE_CREATE =
                "CREATE TABLE " + TRUSTED_TABLE_NAME + 
                " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, number TEXT, key TEXT, verified INTEGER);";
    private static final String NUMBERS_TABLE_CREATE =
            "CREATE TABLE " + NUMBERS_TABLE_NAME + 
            " (id INTEGER PRIMARY KEY AUTOINCREMENT, reference INTEGER, number TEXT);";

    public SQLitehelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TRUSTED_TABLE_CREATE);
        db.execSQL(NUMBERS_TABLE_CREATE);
        db.execSQL(USER_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+ USER_TABLE_CREATE);
		db.execSQL("DROP TABLE IF EXISTS "+ TRUSTED_TABLE_CREATE);
		db.execSQL("DROP TABLE IF EXISTS "+ NUMBERS_TABLE_CREATE);
		onCreate(db);
		
	}
}