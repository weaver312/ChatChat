package com.weaverhong.lesson.chatchat.Entity;

public class MessageEntity {
    private int id;
    // private String owner;
    private String msgtranid;
    private String sendername;
    private String receivername;
    private String createtime;
    private String content;
    private int direction;

    public int getDirection() {
        return direction;
    }

    // 0 is sendmsg, 1 is receivemsg
    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMsgtranid() {
        return msgtranid;
    }

    public void setMsgtranid(String msgtranid) {
        this.msgtranid = msgtranid;
    }

    public String getSendername() {
        return sendername;
    }

    public void setSendername(String sendername) {
        this.sendername = sendername;
    }

    public String getReceivername() {
        return receivername;
    }

    public void setReceivername(String receivername) {
        this.receivername = receivername;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // public String getOwner() {
    //     return owner;
    // }
    //
    // public void setOwner(String owner) {
    //     this.owner = owner;
    // }

    public MessageEntity() {

    }

    public static boolean checkNullable(MessageEntity messageEntity) {
        return messageEntity.getSendername()==null ||
                messageEntity.getReceivername()==null ||
                messageEntity.getCreatetime()==null ||
                messageEntity.getContent()==null ||
                messageEntity.getMsgtranid()==null;
    }

    // public MessageEntity(int id, String owner, String msgtranid, String sendername, String receivername, String createtime, String content, int direction) {
    //     this.id = id;
    //     this.owner = owner;
    //     this.msgtranid = msgtranid;
    //     this.sendername = sendername;
    //     this.receivername = receivername;
    //     this.createtime = createtime;
    //     this.content = content;
    //     this.direction = direction;
    // }
}
