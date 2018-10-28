package com.weaverhong.lesson.chatchat.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.weaverhong.lesson.chatchat.Entity.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class ContactDBManager {

    private ContactDBHelper mDBHelper;
    private static final String TBNAME = "table_contacts";

    public ContactDBManager(Context context) {
        mDBHelper = new ContactDBHelper(context);
    }

    public void add(UserEntity item) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("jid", item.getJid());
        values.put("username", item.getUsername());
        values.put("ifadded", item.getIfadded());
        try {
            db.insert(TBNAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
    }

    public void addAllinRewriteMode(List<UserEntity> list) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        for (UserEntity item : list) {
            ContentValues values = new ContentValues();
            values.put("jid", item.getJid());
            values.put("username", item.getUsername());
            values.put("ifadded", item.getIfadded());
            try {
                db.insert(TBNAME, null, values);
            } catch (Exception e) {
                e.printStackTrace();
                // 1可以覆盖0，0不能覆盖1
                if (item.getIfadded()==1)
                    db.update(TBNAME, values, "USERNAME=?", new String[]{item.getUsername()});
            }
        }
        db.close();
    }

    public void addAll(List<UserEntity> list) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        for (UserEntity item : list) {
            ContentValues values = new ContentValues();
            values.put("jid", item.getJid());
            values.put("username", item.getUsername());
            values.put("ifadded", item.getIfadded());
            try {
                db.insert(TBNAME, null, values);
            } catch (Exception e) {
                e.printStackTrace();
                db.update(TBNAME, values, "USERNAME=?", new String[]{item.getUsername()});
            }
        }
        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.delete(TBNAME, null, null);
        db.close();
    }

    public void delete(int id) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.delete(TBNAME, "ID=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void delete(String username) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.delete(TBNAME, "USERNAME=?", new String[]{username});
        db.close();
    }

    public void update(UserEntity item) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("jid", item.getJid());
        values.put("username", item.getUsername());
        values.put("ifadded", item.getIfadded());
        db.update(TBNAME, values, "ID=?", new String[]{String.valueOf(item.getId())});
        db.close();
    }

    public List<UserEntity> listAll() {
        List<UserEntity> rateList = null;
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.query(TBNAME, null, null, null, null, null, null);
        if (cursor != null) {
            rateList = new ArrayList<UserEntity>();
            while (cursor.moveToNext()) {
                UserEntity item = new UserEntity();
                item.setId(cursor.getInt(cursor.getColumnIndex("ID")));

                item.setJid(cursor.getString(cursor.getColumnIndex("JID")));
                item.setUsername(cursor.getString(cursor.getColumnIndex("USERNAME")));
                item.setIfadded(cursor.getInt(cursor.getColumnIndex("IFADDED")));
                rateList.add(item);
            }
            cursor.close();
        }
        db.close();
        return rateList;

    }

    public UserEntity findById(int id) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.query(TBNAME, null, "ID=?", new String[]{String.valueOf(id)}, null, null, null);
        UserEntity UserEntity = null;
        if (cursor != null && cursor.moveToFirst()) {
            UserEntity item = new UserEntity();
            UserEntity.setId(cursor.getInt(cursor.getColumnIndex("ID")));

            item.setJid(cursor.getString(cursor.getColumnIndex("JID")));
            item.setUsername(cursor.getString(cursor.getColumnIndex("USERNAME")));
            item.setIfadded(cursor.getInt(cursor.getColumnIndex("IFADDED")));

            cursor.close();
        }
        db.close();
        return UserEntity;
    }

    public UserEntity findByUsername(String username) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.query(TBNAME, null, "USERNAME=?",
                new String[]{username}, null, null, null);

        UserEntity item = null;
        if (cursor != null && cursor.moveToFirst()) {
            item = new UserEntity();
            item.setId(cursor.getInt(cursor.getColumnIndex("ID")));

            item.setJid(cursor.getString(cursor.getColumnIndex("JID")));
            item.setUsername(cursor.getString(cursor.getColumnIndex("USERNAME")));
            item.setIfadded(cursor.getInt(cursor.getColumnIndex("IFADDED")));

            cursor.close();
        }
        db.close();
        return item;
    }

    public void deleteAllAdded() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.delete(TBNAME, "IFADDED=?", new String[]{String.valueOf(1)});
        db.close();
    }
}
