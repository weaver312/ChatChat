package com.weaverhong.lesson.chatchat.Datalabs;

import com.weaverhong.lesson.chatchat.ListItem.ContactListItem;
import com.weaverhong.lesson.chatchat.OpenfireConnector;

import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ContactLab {
    public static List<ContactListItem> mContactitems;
    static {
        mContactitems = new ArrayList<>();
        mContactitems.add(new ContactListItem("马化腾"));
        mContactitems.add(new ContactListItem("一位长者"));
        mContactitems.add(new ContactListItem("JackMa"));
    }

    public static void refreshdata() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                List<ContactListItem> Entrieslist = new ArrayList<ContactListItem>();
                RosterGroup rosterGroup = OpenfireConnector.getRoster().getGroup("friends");
                Collection<RosterEntry> rosterEntry = rosterGroup.getEntries();
                
                Iterator<RosterEntry> i = rosterEntry.iterator();
                while (i.hasNext()) {
                    RosterEntry re = i.next();
                    ContactListItem item = new ContactListItem();
                    item.setUsername(re.getJid().toString().split("@")[0]);
                    item.setIffriend(!(re.getName()==null || re.getName().equals("")));
                    Entrieslist.add(item);
                }
                mContactitems = Entrieslist;
            }
        }).start();
    }
}
