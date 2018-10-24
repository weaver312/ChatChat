package com.weaverhong.lesson.chatchat.Entity;

public class UserEntity {
    int id;
    String jid;
    String username;
    int ifadded;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getIfadded() {
        return ifadded;
    }

    public void setIfadded(int ifadded) {
        this.ifadded = ifadded;
    }

    public UserEntity(int id, String jid, String username, int ifadded) {

        this.id = id;
        this.jid = jid;
        this.username = username;
        this.ifadded = ifadded;
    }

    public UserEntity(String jid, String username, int ifadded) {

        this.jid = jid;
        this.username = username;
        this.ifadded = ifadded;
    }

    public UserEntity() {
    }
}
