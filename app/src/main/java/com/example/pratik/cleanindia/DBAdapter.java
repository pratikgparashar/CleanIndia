package com.example.pratik.cleanindia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	public static final String KEY_ROWID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_PHONE = "phone";
	private static final String TAG = "DBAdapter";
	private static final String DATABASE_NAME = "MyDB";
	private static final String DATABASE_TABLE = "requests";
	private static final int DATABASE_VERSION = 6;

	private static final String DATABASE_CREATE =
	"create table requests (_id integer primary key, "
	+ "name text not null, date text not null, image blob not null ,timeAcc text not null);";
	private final Context context;
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	
	public DBAdapter(Context ctx)
	{
	this.context = ctx;
	DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);//SQLiteOpenHelper (Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
		} // SQLiteDatabase.CursorFactory is null by default
	
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			try 
			{
				db.execSQL(DATABASE_CREATE);
			} 
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS requests");
		onCreate(db);
		}
	}

	//---opens the database---
	public DBAdapter open() throws SQLException
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}

	//---closes the database---
	public void close()
	{
		DBHelper.close();
	}

	//---insert a contact into the database---
	public long insertRequest(int id,String name, String date1, String encodedimage,String timeAcc)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put("_id",id);
        initialValues.put("name", name);
		initialValues.put("date", date1);
		initialValues.put("image", encodedimage);
        initialValues.put("timeAcc", timeAcc);
		return db.insert(DATABASE_TABLE, null, initialValues); // 2nd parameter -  null indicates all column values. 
	}

	//---retrieves all the contacts---
	public Cursor getAllContacts()
	{
		return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME,KEY_EMAIL,KEY_PHONE}, null, null, null, null, null);
	}

	//---retrieves a particular contact---
	public Cursor getRequest(long rowId) throws SQLException
	{
	Cursor mCursor =
	db.query(DATABASE_TABLE, new String[] {KEY_ROWID,
	"name","date","image"}, KEY_ROWID + "=" + rowId, null,
	null, null, null, null);
	if (mCursor != null) {
	mCursor.moveToFirst();
	}
	return mCursor;
	}
     public Cursor lastRequest()throws  SQLException{
      Cursor mCursor = db.rawQuery("SELECT * FROM requests WHERE _id = (SELECT MAX(_id) FROM requests);",null);
         return mCursor;
     }


}

