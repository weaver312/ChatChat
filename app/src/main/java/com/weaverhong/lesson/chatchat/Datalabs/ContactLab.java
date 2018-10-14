package com.weaverhong.lesson.chatchat.Datalabs;

import com.weaverhong.lesson.chatchat.ListItem.ContactListItem;

import java.util.ArrayList;
import java.util.List;

public class ContactLab {
    public static List<ContactListItem> mContactitems;
    static {
        mContactitems = new ArrayList<>();
        mContactitems.add(new ContactListItem("马化腾"));
        mContactitems.add(new ContactListItem("一位长者"));
        mContactitems.add(new ContactListItem("JackMa"));
    }
}
