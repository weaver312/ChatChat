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
                item.setFriendtype(e.getIfadded());
                if (e.getIfadded() == OpenfireConnector.FRIENDTYPE_BOTH)
                    item.setIffriend(true);
                else if (e.getIfadded() == OpenfireConnector.FRIENDTYPE_FROM)
                    item.setIffriend(false);
                else
                    item.setIffriend(false);

                // if (e.get)

                templist.add(item);
            }

            list = templist;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void refreshdataonline(Context applicationContext) {
        List<UserEntity> entrieslist = OpenfireConnector.getContactsFromServer();
        // Log.e("ContactLab-entity number: ",entrieslist.size()+"");
        // new ContactDBManager(applicationContext).deleteAllAdded();
        // for (UserEntity e : entrieslist) {
        //     // Log.e("ContactLab-entity from server: ",e.getUsername() + " added: " +e.getIfadded());
        //     if (e.getIfadded() == 0)
        //         entrieslist.remove(e);
        // }
        // new ContactDBManager(applicationContext).addAllinRewriteMode(entrieslist);

        // 完全清空替换掉原先的好友数据即可
        new ContactDBManager(applicationContext).deleteAll();
        new ContactDBManager(applicationContext).addAll(entrieslist);
    }
}
