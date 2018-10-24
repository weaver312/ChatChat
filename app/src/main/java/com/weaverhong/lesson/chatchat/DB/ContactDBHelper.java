package com.weaverhong.lesson.chatchat.DB;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDBHelper extends SQLiteOpenHelper {

    // DBOpenHelper主要是三个字段：版本、数据库名、表名
    private static final int VERSION = 1;
    private static final String DB_NAME = "chatchat.db";
    private static final String TB_NAME_CONTACTS = "table_contacts";
    private static final String TB_NAME_MESSAGE = "table_chats";


    public ContactDBHelper(Context context) {
        this(context, DB_NAME, null, VERSION, null);
    }

    public ContactDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public ContactDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ TB_NAME_CONTACTS +"" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                // "MSGTRANID TEXT UNIQUE," +
                "JID TEXT UNIQUE," +
                "USERNAME TEXT UNIQUE," +
                "IFADDED INTEGER)");
        db.execSQL("CREATE TABLE "+ TB_NAME_MESSAGE +"" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                // "MSGTRANID TEXT UNIQUE," +
                "MSGTRANID TEXT," +
                "SENDERNAME TEXT," +
                "RECEIVERNAME TEXT," +
                "CREATETIME TEXT," +
                "CONTENT TEXT," +
                "DIRECTION INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
