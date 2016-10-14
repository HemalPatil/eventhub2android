package com.hemal.eventhub2.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Hemal on 10-Oct-16.
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "eventhub_local";

	public DatabaseHelper(Context c)
	{
		super(c, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE IF NOT EXISTS club (id integer NOT NULL PRIMARY KEY AUTOINCREMENT, name varchar(128) NOT NULL, alias varchar(128) NOT NULL, followed integer not null)");
		db.execSQL("CREATE TABLE IF NOT EXISTS event (id integer NOT NULL PRIMARY KEY AUTOINCREMENT, type varchar(128) NOT NULL, subtype varchar(128) NOT NULL, name varchar(128) NOT NULL UNIQUE, date_time datetime NOT NULL, contact_name_1 varchar(128) NOT NULL, contact_number_1 varchar(10) NOT NULL, contact_name_2 varchar(128) NOT NULL, contact_number_2 varchar(10) NOT NULL, venue varchar(128) NOT NULL, alias varchar(128) NOT NULL UNIQUE, club_id integer NULL REFERENCES club (id), created_on timestamp not null, followed integer not null, image_downloaded integer not null, description text not null)");
		Log.v("database", "create database called");
	}

	@Override
	public void onOpen(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE IF NOT EXISTS club (id integer NOT NULL PRIMARY KEY AUTOINCREMENT, name varchar(128) NOT NULL, alias varchar(128) NOT NULL, followed integer not null)");
		db.execSQL("CREATE TABLE IF NOT EXISTS event (id integer NOT NULL PRIMARY KEY AUTOINCREMENT, type varchar(128) NOT NULL, subtype varchar(128) NOT NULL, name varchar(128) NOT NULL UNIQUE, date_time datetime NOT NULL, contact_name_1 varchar(128) NOT NULL, contact_number_1 varchar(10) NOT NULL, contact_name_2 varchar(128) NOT NULL, contact_number_2 varchar(10) NOT NULL, venue varchar(128) NOT NULL, alias varchar(128) NOT NULL UNIQUE, club_id integer NULL REFERENCES club (id), created_on timestamp not null, followed integer not null, image_downloaded integer not null, description text not null)");
		Log.v("database", "open database called");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		onCreate(db);
		Log.v("database", "upgrade database called");
	}
}
