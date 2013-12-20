package com.tinfoil.sms.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class DatabaseProvider extends ContentProvider {
	
	/**
     * The fully qualified name of the authority.
     */
    public static final String AUTHORITY = "com.tinfoil.sms.database.provider";
    
    private static final String TRUST_NUMBER_TABLE = SQLitehelper.TRUSTED_TABLE_NAME + ", "
    		+ SQLitehelper.NUMBERS_TABLE_NAME;
    private static final String TRUST_NUM_MESS_TABLE = SQLitehelper.TRUSTED_TABLE_NAME + ", "
    		+ SQLitehelper.NUMBERS_TABLE_NAME + ", " + SQLitehelper.MESSAGES_TABLE_NAME;
    
    private static final String QUERY_STRING = "(SELECT " + 
			SQLitehelper.TRUSTED_TABLE_NAME + "." + SQLitehelper.KEY_NAME + ", " +
			SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_NUMBER + ", " + 
			SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_UNREAD + ", " + 
			SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_MESSAGE + ", " + 
			SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_REFERENCE + ", " + 
			SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_DATE + " FROM " + 
			SQLitehelper.TRUSTED_TABLE_NAME + ", " + 
			SQLitehelper.NUMBERS_TABLE_NAME + ", " + 
			SQLitehelper.MESSAGES_TABLE_NAME + " WHERE " + 
			SQLitehelper.TRUSTED_TABLE_NAME + "." + SQLitehelper.KEY_ID + " = " + 
			SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_REFERENCE + " AND " + 
			SQLitehelper.NUMBERS_TABLE_NAME + "." + SQLitehelper.KEY_ID + " = " +
			SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_REFERENCE + " AND " + 
			SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_MESSAGE + " IS NOT NULL " +
			"ORDER BY " + SQLitehelper.MESSAGES_TABLE_NAME + "." + SQLitehelper.KEY_DATE + ")";
    
    private static final String BASE_QUERY_PATH = "QUERY";

    public static final Uri USER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + SQLitehelper.USER_TABLE_NAME);
    public static final Uri TRUSTED_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + SQLitehelper.TRUSTED_TABLE_NAME);
    public static final Uri NUMBER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + SQLitehelper.NUMBERS_TABLE_NAME);
    public static final Uri SHARED_INFO_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + SQLitehelper.SHARED_INFO_TABLE_NAME);
    public static final Uri BOOK_PATHS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + SQLitehelper.BOOK_PATHS_TABLE_NAME);
    public static final Uri MESSAGE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + SQLitehelper.MESSAGES_TABLE_NAME);
    public static final Uri QUEUE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + SQLitehelper.QUEUE_TABLE_NAME);
    public static final Uri EXCHANGE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + SQLitehelper.EXCHANGE_TABLE_NAME);
    

    public static final Uri TN_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + SQLitehelper.TRUSTED_TABLE_NAME + "/" + SQLitehelper.NUMBERS_TABLE_NAME);
    public static final Uri TNM_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + SQLitehelper.TRUSTED_TABLE_NAME + "/" + SQLitehelper.NUMBERS_TABLE_NAME
            + "/" + SQLitehelper.MESSAGES_TABLE_NAME);
    public static final Uri QUERY_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
            + BASE_QUERY_PATH);
    
    /**
     * A helper object for accessing the SQLite database.
     */
    private SQLitehelper db;
    
    /**
     * A helper object for storing the SQLite database instance.
     */
    private SQLiteDatabase dba;
    
    private static final int USER = 0;
    private static final int TRUSTED = 1;
    private static final int NUMBERS = 2;
    private static final int SHARED_INFO = 3;
    private static final int BOOK_PATH = 4;
    private static final int MESSAGE = 5;
    private static final int QUEUE = 6;
    private static final int EXCHANGE = 7;
    private static final int TRUST_NUMBERS = 8;
    private static final int TRUST_NUM_MESS = 9;
    private static final int QUERY = 10;
    
    //SQLitehelper.TRUSTED_TABLE_NAME + ", " + SQLitehelper.NUMBERS_TABLE_NAME + ", " + SQLitehelper.MESSAGES_TABLE_NAME
    //A select Statement
    //SQLitehelper.TRUSTED_TABLE_NAME + ", " + SQLitehelper.NUMBERS_TABLE_NAME

    /**
     * A URI matcher object to associate URIs with pertinent data types.
     */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
    	//sURIMatcher.a
        sURIMatcher.addURI(AUTHORITY, SQLitehelper.USER_TABLE_NAME, USER);
        sURIMatcher.addURI(AUTHORITY, SQLitehelper.TRUSTED_TABLE_NAME, TRUSTED);
        sURIMatcher.addURI(AUTHORITY, SQLitehelper.NUMBERS_TABLE_NAME, NUMBERS);
        sURIMatcher.addURI(AUTHORITY, SQLitehelper.SHARED_INFO_TABLE_NAME, SHARED_INFO);
        sURIMatcher.addURI(AUTHORITY, SQLitehelper.BOOK_PATHS_TABLE_NAME, BOOK_PATH);
        sURIMatcher.addURI(AUTHORITY, SQLitehelper.MESSAGES_TABLE_NAME, MESSAGE);
        sURIMatcher.addURI(AUTHORITY, SQLitehelper.QUEUE_TABLE_NAME, QUEUE);
        sURIMatcher.addURI(AUTHORITY, SQLitehelper.EXCHANGE_TABLE_NAME, EXCHANGE);
        
        sURIMatcher.addURI(AUTHORITY, SQLitehelper.TRUSTED_TABLE_NAME + 
        		"/" + SQLitehelper.NUMBERS_TABLE_NAME, TRUST_NUMBERS);
        sURIMatcher.addURI(AUTHORITY, SQLitehelper.TRUSTED_TABLE_NAME +
        		"/" + SQLitehelper.NUMBERS_TABLE_NAME + "/" + SQLitehelper.MESSAGES_TABLE_NAME,
        		TRUST_NUM_MESS);
        sURIMatcher.addURI(AUTHORITY, BASE_QUERY_PATH, QUERY);
    }
	
	public DatabaseProvider() {
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		int choose = sURIMatcher.match(uri);

		dba = db.getWritableDatabase();
		
        // Delete either user data, or booking data.
        switch (choose)
        {
        case TRUSTED:
            // Delete trusted data.
            int count = dba.delete(SQLitehelper.TRUSTED_TABLE_NAME, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case NUMBERS:
            // Delete number data.
            count = dba.delete(SQLitehelper.NUMBERS_TABLE_NAME, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case SHARED_INFO:
            // Delete shared info data.
            count = dba.delete(SQLitehelper.SHARED_INFO_TABLE_NAME, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case BOOK_PATH:
            // Delete book path data.
            count = dba.delete(SQLitehelper.BOOK_PATHS_TABLE_NAME, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case MESSAGE:
            // Delete message data.
        	count = dba.delete(SQLitehelper.MESSAGES_TABLE_NAME, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case QUEUE:
            // Delete queue data.
            count = dba.delete(SQLitehelper.QUEUE_TABLE_NAME, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case EXCHANGE:
            // Delete exchange data.
            count = dba.delete(SQLitehelper.EXCHANGE_TABLE_NAME, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
	}

	@Override
	public String getType(Uri uri) {
		
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int choose = sURIMatcher.match(uri);

		dba = db.getWritableDatabase();
		
        // Delete either user data, or booking data.
        switch (choose)
        {
        case USER:
            // Insert user data.
            long id = dba.insert(SQLitehelper.USER_TABLE_NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(SQLitehelper.USER_TABLE_NAME + "/" + id);
        case TRUSTED:
            // Insert trusted data.
            id = dba.insert(SQLitehelper.TRUSTED_TABLE_NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(SQLitehelper.TRUSTED_TABLE_NAME + "/" + id);
        case NUMBERS:
            // Insert number data.
            id = dba.insert(SQLitehelper.NUMBERS_TABLE_NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(SQLitehelper.NUMBERS_TABLE_NAME + "/" + id);
        case SHARED_INFO:
            // Insert shared info data.
        	id = dba.insert(SQLitehelper.SHARED_INFO_TABLE_NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(SQLitehelper.SHARED_INFO_TABLE_NAME + "/" + id);
        case BOOK_PATH:
            // Insert book path data.
        	id = dba.insert(SQLitehelper.BOOK_PATHS_TABLE_NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(SQLitehelper.BOOK_PATHS_TABLE_NAME + "/" + id);
        case MESSAGE:
            // Insert message data.
        	id = dba.insert(SQLitehelper.MESSAGES_TABLE_NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(SQLitehelper.MESSAGES_TABLE_NAME + "/" + id);
        case QUEUE:
            // Insert queue data.
        	id = dba.insert(SQLitehelper.QUEUE_TABLE_NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(SQLitehelper.QUEUE_TABLE_NAME + "/" + id);
        case EXCHANGE:
            // Insert exchange data.
        	id = dba.insert(SQLitehelper.EXCHANGE_TABLE_NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse(SQLitehelper.EXCHANGE_TABLE_NAME + "/" + id);
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
	}

	@Override
	public boolean onCreate() {
		db = new SQLitehelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		// Create proper SQL syntax for our database.
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();

        Cursor c = null;
		
		int choose = sURIMatcher.match(uri);

		dba = db.getWritableDatabase();
		
        // Delete either user data, or booking data.
        switch (choose)
        {
        case USER:
        	
        	//Select user data
        	qBuilder.setTables(SQLitehelper.USER_TABLE_NAME);

            c = qBuilder.query(db.getWritableDatabase(), projection, selection, selectionArgs, "",
                    "", sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        case TRUSTED:
        	//Select trusted data
        	qBuilder.setTables(SQLitehelper.TRUSTED_TABLE_NAME);

            c = qBuilder.query(db.getWritableDatabase(), projection, selection, selectionArgs, "",
                    "", sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        case NUMBERS:
        	//Select number data
        	qBuilder.setTables(SQLitehelper.NUMBERS_TABLE_NAME);

            c = qBuilder.query(db.getWritableDatabase(), projection, selection, selectionArgs, "",
                    "", sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        case SHARED_INFO:
        	//Select shared info data
        	qBuilder.setTables(SQLitehelper.SHARED_INFO_TABLE_NAME);

            c = qBuilder.query(db.getWritableDatabase(), projection, selection, selectionArgs, "",
                    "", sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        case BOOK_PATH:
        	//Select book path data
        	qBuilder.setTables(SQLitehelper.BOOK_PATHS_TABLE_NAME);

            c = qBuilder.query(db.getWritableDatabase(), projection, selection, selectionArgs, "",
                    "", sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        case MESSAGE:
        	//Select message data
        	qBuilder.setTables(SQLitehelper.MESSAGES_TABLE_NAME);

            c = qBuilder.query(db.getWritableDatabase(), projection, selection, selectionArgs, "",
                    "", sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        case QUEUE:
        	//Select queue data
        	qBuilder.setTables(SQLitehelper.QUEUE_TABLE_NAME);

            c = qBuilder.query(db.getWritableDatabase(), projection, selection, selectionArgs, "",
                    "", sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        case EXCHANGE:
        	//Select exchange data
        	qBuilder.setTables(SQLitehelper.EXCHANGE_TABLE_NAME);

            c = qBuilder.query(db.getWritableDatabase(), projection, selection, selectionArgs, "",
                    "", sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        case TRUST_NUMBERS:
        	//Select trusted and numbers data
        	qBuilder.setTables(TRUST_NUMBER_TABLE);

            c = qBuilder.query(db.getWritableDatabase(), projection, selection, selectionArgs, "",
                    "", sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        case TRUST_NUM_MESS:
        	//Select trusted, numbers and mssage data
        	qBuilder.setTables(TRUST_NUM_MESS_TABLE);

            c = qBuilder.query(db.getWritableDatabase(), projection, selection, selectionArgs, "",
                    "", sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        case QUERY:
        	//Select special inner query data
        	qBuilder.setTables(QUERY_STRING);

        	/* This groups by SQLitehelper.KEY_REFERENCE, since it is only used in 1 place
        	 * If this query is used in more than 1 place than it will also use group by so
        	 * be wary.
        	 */
            c = qBuilder.query(db.getWritableDatabase(), projection, selection, selectionArgs,
            		SQLitehelper.KEY_REFERENCE, "", sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int choose = sURIMatcher.match(uri);

		dba = db.getWritableDatabase();
		
        // Delete either user data, or booking data.
        switch (choose)
        {
        case USER:
            // Delete user data.
            int count = dba.update(SQLitehelper.USER_TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case TRUSTED:
            // Delete trusted data.
            count = dba.update(SQLitehelper.TRUSTED_TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case NUMBERS:
            // Delete number data.
            count = dba.update(SQLitehelper.NUMBERS_TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case SHARED_INFO:
            // Delete shared info data.
            count = dba.update(SQLitehelper.SHARED_INFO_TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case BOOK_PATH:
            // Delete book path data.
            count = dba.update(SQLitehelper.BOOK_PATHS_TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case MESSAGE:
            // Delete message data.
        	count = dba.update(SQLitehelper.MESSAGES_TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case QUEUE:
            // Delete queue data.
            count = dba.update(SQLitehelper.QUEUE_TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        case EXCHANGE:
            // Delete exchange data.
            count = dba.update(SQLitehelper.EXCHANGE_TABLE_NAME, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
	}
}
