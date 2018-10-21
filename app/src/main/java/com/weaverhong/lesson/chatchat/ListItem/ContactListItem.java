package com.weaverhong.lesson.chatchat.ListItem;

public class ContactListItem {
    String username;
    boolean iffriend;

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
