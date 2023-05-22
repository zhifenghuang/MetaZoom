package com.common.lib.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public class ContactItem implements MultiItemEntity {


    public static final int VIEW_TYPE_0 = 0;
    public static final int VIEW_TYPE_1 = 1;
    public static final int VIEW_TYPE_2 = 2;

    private int itemType = VIEW_TYPE_1;
    private String name;
    private UserBean friend;
    private GroupBean group;
    private int msgNum;

    public int getMsgNum() {
        return msgNum;
    }

    public void setMsgNum(int msgNum) {
        this.msgNum = msgNum;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public GroupBean getGroup() {
        return group;
    }

    public void setGroup(GroupBean group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    public UserBean getFriend() {
        return friend;
    }

    public void setFriend(UserBean friend) {
        this.friend = friend;
    }
}


