package com.common.lib.bean;

public class ChatBean {
    public UserBean chatUser;
    public GroupBean group;
    public BasicMessage lastMsg;
    public int unReadNum;
    public ChatSubBean chatSubBean;

    public boolean isTopChat() {
        return chatUser == null ? (group.getTop() == 1) : (chatUser.getTop() == 1);
    }

    public boolean isNotInterupt() {
        return chatUser == null ? (group.getIgnore() == 1) : (chatUser.getIgnore() == 1);
    }
}
