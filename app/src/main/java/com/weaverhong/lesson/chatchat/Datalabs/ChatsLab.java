package com.weaverhong.lesson.chatchat.Datalabs;

import android.content.Context;

import com.weaverhong.lesson.chatchat.ListItem.ChatListItem;

import java.util.ArrayList;
import java.util.List;

public class ChatsLab {
    public static List<ChatListItem> list = new ArrayList<>();

    public static void refreshChats(Context context) {
        // try {
        //     List<MessageEntity> userlist = new MessageDBManager(context).listAll();
        //     List<ChatListItem> templist = new ArrayList<>();
        //     for (MessageEntity e : userlist) {
        //         ChatListItem item = new ChatListItem();
        //
        //         templist.add(item);
        //     }
        //     list = templist;
        // } catch (Exception e){
        //     e.printStackTrace();
        // }
    }

}
