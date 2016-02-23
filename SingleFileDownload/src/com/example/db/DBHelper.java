package com.example.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	public static final String DB_NAME = "download.db";
	public static final int VERSION = 1;
	public static final String SQL_CREATE = "create table thread_info( id integer primary key autoincrement," +
			" thread_id integer, url text, start integer, end integer, finished integer)";
	public static final String SQL_DROP = "drop table if exists thread_info";
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL(SQL_DROP);
		db.execSQL(SQL_CREATE);
	}

}
