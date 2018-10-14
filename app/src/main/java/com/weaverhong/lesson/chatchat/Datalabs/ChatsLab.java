package com.weaverhong.lesson.chatchat.Datalabs;

import com.weaverhong.lesson.chatchat.ListItem.ChatListItem;

import java.util.ArrayList;
import java.util.List;

public class ChatsLab {
    public static List<ChatListItem> mChatitems;
    static {
        mChatitems = new ArrayList<>();
        mChatitems.add(new ChatListItem("zlearning", "hi homie"));
        mChatitems.add(new ChatListItem("马云", "给你打电话没接啊？"));
        mChatitems.add(new ChatListItem("周琦", "穆雷跟萧华说了，明天就宣布你的特殊顶薪条例，一天一千万"));
    }
}
