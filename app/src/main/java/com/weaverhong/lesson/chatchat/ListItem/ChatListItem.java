package com.weaverhong.lesson.chatchat.ListItem;

public class ChatListItem {
    String user;
    String content;

    public ChatListItem() {

    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ChatListItem(String user, String content) {
        this.user = user;
        this.content = content;
    }
}
