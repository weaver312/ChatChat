package com.weaverhong.lesson.chatchat.ListItem;

public class ContactListItem {
    String username;
    boolean iffriend;
    int friendtype;

    public int getFriendtype() {
        return friendtype;
    }

    public void setFriendtype(int friendtype) {
        this.friendtype = friendtype;
    }

    public ContactListItem(String username, boolean iffriend, int friendtype) {

        this.username = username;
        this.iffriend = iffriend;
        this.friendtype = friendtype;
    }

    public ContactListItem(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ContactListItem() {
    }

    public boolean isIffriend() {

        return iffriend;
    }

    public void setIffriend(boolean iffriend) {
        this.iffriend = iffriend;
    }

    public ContactListItem(String username, boolean iffriend) {

        this.username = username;
        this.iffriend = iffriend;
    }

}
