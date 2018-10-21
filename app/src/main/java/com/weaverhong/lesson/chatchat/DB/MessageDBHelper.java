package com.weaverhong.lesson.chatchat.DB;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageDBHelper extends SQLiteOpenHelper {

    // DBOpenHelper主要是三个字段：版本、数据库名、表名
    private static final int VERSION = 1;
    private static final String DB_NAME = "chatchat.db";
    private static final String TB_NAME = "table_chats";

    public MessageDBHelper(Context context) {
        this(context, DB_NAME, null, VERSION, null);
    }

    public MessageDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MessageDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TB_NAME+"" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "MSGTRANSID TEXT UNIQUE," +
                "SENDERNAME TEXT," +
                "CREATETIME TEXT," +
                "CONTENT TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
