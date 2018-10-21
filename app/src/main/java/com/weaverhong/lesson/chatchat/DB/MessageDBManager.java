package com.weaverhong.lesson.chatchat.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.weaverhong.lesson.chatchat.Entity.MessageEntity;

import java.util.ArrayList;
import java.util.List;

public class MessageDBManager {

    private MessageDBHelper mDBHelper;
    private String TBNAME;

    public MessageDBManager(Context context) {
        mDBHelper = new MessageDBHelper(context);
    }

    public void add(MessageEntity item) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("msgtranid", item.getMsgtranid());
        values.put("sendername", item.getSendername());
        values.put("receivername", item.getReceivername());
        values.put("createtime", item.getCreatetime());
        values.put("content", item.getContent());
        db.insert(TBNAME, null, values);
        db.close();
    }

    public void addAll(List<MessageEntity> list) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        for (MessageEntity item : list) {
            ContentValues values = new ContentValues();
            values.put("msgtranid", item.getMsgtranid());
            values.put("sendername", item.getSendername());
            values.put("receivername", item.getReceivername());
            values.put("createtime", item.getCreatetime());
            values.put("content", item.getContent());
            db.insert(TBNAME, null, values);
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

    public void update(MessageEntity item) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("msgtranid", item.getMsgtranid());
        values.put("sendername", item.getSendername());
        values.put("receivername", item.getReceivername());
        values.put("createtime", item.getCreatetime());
        values.put("content", item.getContent());
        db.update(TBNAME, values, "ID=?", new String[]{String.valueOf(item.getId())});
        db.close();
    }

    public List<MessageEntity> listAll() {
        List<MessageEntity> rateList = null;
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.query(TBNAME, null, null, null, null, null, null);
        if (cursor != null) {
            rateList = new ArrayList<MessageEntity>();
            while (cursor.moveToNext()) {
                MessageEntity item = new MessageEntity();
                item.setId(cursor.getInt(cursor.getColumnIndex("ID")));

                item.setMsgtranid(cursor.getString(cursor.getColumnIndex("MSGTRANID")));
                item.setSendername(cursor.getString(cursor.getColumnIndex("SENDERNAME")));
                item.setReceivername(cursor.getString(cursor.getColumnIndex("RECEIVERNAME")));
                item.setCreatetime(cursor.getString(cursor.getColumnIndex("CREATETIME")));
                item.setContent(cursor.getString(cursor.getColumnIndex("CONTENT")));
                rateList.add(item);
            }
            cursor.close();
        }
        db.close();
        return rateList;

    }

    public MessageEntity findById(int id) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.query(TBNAME, null, "ID=?", new String[]{String.valueOf(id)}, null, null, null);
        MessageEntity MessageEntity = null;
        if (cursor != null && cursor.moveToFirst()) {
            MessageEntity item = new MessageEntity();
            MessageEntity.setId(cursor.getInt(cursor.getColumnIndex("ID")));

            item.setMsgtranid(cursor.getString(cursor.getColumnIndex("MSGTRANID")));
            item.setSendername(cursor.getString(cursor.getColumnIndex("SENDERNAME")));
            item.setReceivername(cursor.getString(cursor.getColumnIndex("RECEIVERNAME")));
            item.setCreatetime(cursor.getString(cursor.getColumnIndex("CREATETIME")));
            item.setContent(cursor.getString(cursor.getColumnIndex("CONTENT")));

            cursor.close();
        }
        db.close();
        return MessageEntity;
    }

    public List<MessageEntity> findByRecvAndSender(String receiver, String sender, int direction) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.query(TBNAME, null, "SENDERNAME=?ANDRECEIVERNAME=?",
                new String[]{sender,receiver}, null, null, null);

        List<MessageEntity> list = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            MessageEntity item = new MessageEntity();
            item.setId(cursor.getInt(cursor.getColumnIndex("ID")));

            item.setMsgtranid(cursor.getString(cursor.getColumnIndex("MSGTRANID")));
            item.setSendername(cursor.getString(cursor.getColumnIndex("SENDERNAME")));
            item.setReceivername(cursor.getString(cursor.getColumnIndex("RECEIVERNAME")));
            item.setCreatetime(cursor.getString(cursor.getColumnIndex("CREATETIME")));
            item.setContent(cursor.getString(cursor.getColumnIndex("CONTENT")));
            item.setDirection(direction);

            list.add(item);

            while (!cursor.isLast()) {
                cursor.moveToNext();
                MessageEntity item1 = new MessageEntity();
                item1.setId(cursor.getInt(cursor.getColumnIndex("ID")));

                item1.setMsgtranid(cursor.getString(cursor.getColumnIndex("MSGTRANID")));
                item1.setSendername(cursor.getString(cursor.getColumnIndex("SENDERNAME")));
                item1.setReceivername(cursor.getString(cursor.getColumnIndex("RECEIVERNAME")));
                item1.setCreatetime(cursor.getString(cursor.getColumnIndex("CREATETIME")));
                item1.setContent(cursor.getString(cursor.getColumnIndex("CONTENT")));
                item1.setDirection(direction);

                list.add(item1);
            }
            cursor.close();
        }
        db.close();
        return list;
    }

}
