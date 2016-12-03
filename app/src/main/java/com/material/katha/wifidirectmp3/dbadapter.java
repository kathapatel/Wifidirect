package com.material.katha.wifidirectmp3;

/**
 * Created by Milan on 07-Jul-15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class dbadapter {
    private static final String DB_NAME="MY_DATABASE";
    private static final String DB_TABLE="filehistory";
    private static final int DB_VERSION=1;
    private static final String COL_NAME="filename";
    private static final String COL_DEVICE="device";
    private static final String COL_ACTION="action";
    private static final String COL_TIME="time";
    private static final String DB_CREATE="create table filehistory (ID integer primary key autoincrement, filename text, device text,action text,time text);";

    private SQLiteDatabase database;
    private MyDBHelper helper;

    public dbadapter(Context context)
    {
        helper = new MyDBHelper(context,DB_NAME,null,DB_VERSION);
    }

    private static class MyDBHelper extends SQLiteOpenHelper
    {
        public MyDBHelper(Context context, String name, CursorFactory cursorFactory, int version) {
            super(context, name, cursorFactory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("Updation", "Data base version is being updates");
            db.execSQL("DROP TABLE IF EXISTS "+DB_TABLE);
            onCreate(db);
        }
    }

    public dbadapter open(){
        database=helper.getWritableDatabase();
        return this;
    }

    public Cursor getAllEntries() {

        return database.query(DB_TABLE, new String[]{COL_NAME,COL_DEVICE,COL_ACTION,COL_TIME}, null, null, null, null, null);
    }

    public void close() {
        database.close();
    }

    public void insertEntry(String filename, String device, String action,String time) {
        ContentValues contentValues=new ContentValues();
        contentValues.put(COL_NAME,filename);
        contentValues.put(COL_DEVICE,device);
        contentValues.put(COL_ACTION,action);
        contentValues.put(COL_TIME,time);
        database.insert(DB_TABLE, null, contentValues);
    }


    public void delete()
    {
        database.execSQL("delete from "+ DB_TABLE);
    }


}
