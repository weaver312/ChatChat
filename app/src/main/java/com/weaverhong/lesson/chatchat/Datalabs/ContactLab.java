package com.weaverhong.lesson.chatchat.Datalabs;

import android.util.Log;

import com.weaverhong.lesson.chatchat.ListItem.ContactListItem;
import com.weaverhong.lesson.chatchat.OpenfireConnector;

import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ContactLab {
    public static List<ContactListItem> mContactitems;
    public static Set<String> mContactItems_nogroup;
    static {
        mContactitems = new ArrayList<>();
        mContactItems_nogroup = new HashSet<>();
        // mContactitems.add(new ContactListItem("init"));
    }

    public static void refreshdata() {
        // 获取朋友
        List<ContactListItem> entrieslist = new ArrayList<ContactListItem>();
        RosterGroup rosterGroup = OpenfireConnector.getRoster().getGroup("friends");
        Collection<RosterEntry> rosterEntry;
        if (rosterGroup!=null) {
            rosterEntry = rosterGroup.getEntries();

            Iterator<RosterEntry> i = rosterEntry.iterator();
            while (i.hasNext()) {
                RosterEntry re = i.next();
                ContactListItem item = new ContactListItem();
                item.setUsername(re.getJid().toString().split("@")[0]);
                // item.setIffriend(!(re.getName()==null || re.getName().equals("")));
                item.setIffriend(true);
                entrieslist.add(item);
            }
        }

        mContactitems = entrieslist;

        refreshdate_nogroup();

        Log.e("Refreshdatafinish", "debug");
    }

    public static void refreshdate_nogroup() {
        for (String str : mContactItems_nogroup) {
            mContactitems.add(new ContactListItem(str, false));
        }
    }

    public static void clear() {
        mContactitems = null;
        mContactItems_nogroup = null;
    }
}
