package com.weaverhong.lesson.chatchat.Datalabs;

import android.content.Context;

import com.weaverhong.lesson.chatchat.DB.ContactDBManager;
import com.weaverhong.lesson.chatchat.Entity.UserEntity;
import com.weaverhong.lesson.chatchat.ListItem.ContactListItem;
import com.weaverhong.lesson.chatchat.OpenfireConnector;

import java.util.ArrayList;
import java.util.List;

public class ContactLab {

    public static List<ContactListItem> list = new ArrayList<>();

    public static void refreshdatalocal(Context context) {
        try {
            List<UserEntity> userlist = new ContactDBManager(context).listAll();
            List<ContactListItem> templist = new ArrayList<>();
            for (UserEntity e : userlist) {
                ContactListItem item = new ContactListItem();
                item.setUsername(e.getUsername());
                item.setIffriend(e.getIfadded() == 0 ? false : true);
                templist.add(item);
            }
            list = templist;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void refreshdataonline(Context applicationContext) {
        List<UserEntity> entrieslist = OpenfireConnector.getContactsFromServer();
        new ContactDBManager(applicationContext).addAll(entrieslist);
    }
}
