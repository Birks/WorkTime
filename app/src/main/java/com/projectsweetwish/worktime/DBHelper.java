package com.projectsweetwish.worktime;

/**
 * Project WorkTime
 * Created by Tamás on 11/15/2014.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String TIMES_TABLE_NAME = "times";
    public static final String TIMES_COLUMN_ID = "id";
    public static final String TIMES_COLUMN_WORKLENGTH = "worklength";
    public static final String TIMES_COLUMN_REMAINING = "remaining";

    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "CREATE TABLE times " +
                        "(id integer primary key, worklength text, remaining text)"
        );
        ContentValues contentValues = new ContentValues();
        contentValues.put("worklength",0);
        contentValues.put("remaining", 72000);
        db.insert("times", null, contentValues);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS times");
        onCreate(db);
    }

    public boolean insertTime(String worklength, String remaining) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("worklength", worklength);
        contentValues.put("remaining", remaining);
        db.insert("times", null, contentValues);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM times WHERE id=" + id + "", null);
        return res;
    }



    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TIMES_TABLE_NAME);
        return numRows;
    }


    public void resetTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("times",null,null);
        ContentValues contentValues = new ContentValues();
        contentValues.put("worklength",0);
        contentValues.put("remaining", 72000);
        db.insert("times", null, contentValues);

    }



    public Integer deleteTime(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("times",
                "id = ? ",
                new String[]{Integer.toString(id)});
    }

}


