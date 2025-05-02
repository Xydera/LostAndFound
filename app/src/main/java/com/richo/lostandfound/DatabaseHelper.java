package com.richo.lostandfound;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lostfound.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ITEMS = "items";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE = "date";
    public static final String COL_LOCATION = "location";
    public static final String COL_CONTACT = "contact";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ITEMS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_LOCATION + " TEXT, " +
                COL_CONTACT + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    public boolean insertItem(String title, String description, String date, String location, String contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_DATE, date);
        values.put(COL_LOCATION, location);
        values.put(COL_CONTACT, contact);
        long result = db.insert(TABLE_ITEMS, null, values);
        return result != -1;
    }

    public List<String> getAllItemsAsStringList() {
        List<String> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                itemList.add(id + ": " + title);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemList;
    }

    public Cursor getItemById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ITEMS + " WHERE " + COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public boolean deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_ITEMS, COL_ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }
}
