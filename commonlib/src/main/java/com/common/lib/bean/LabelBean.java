package com.common.lib.bean;

import java.io.Serializable;
import java.util.List;

public class LabelBean implements Serializable {

    private long tagId;
    private String name;
    private long userId;
    private int contactCount;
    private List<UserBean> labelFriends;

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getContactCount() {
        return contactCount;
    }

    public void setContactCount(int contactCount) {
        this.contactCount = contactCount;
    }

    public List<UserBean> getLabelFriends() {
        return labelFriends;
    }

    public void setLabelFriends(List<UserBean> labelFriends) {
        this.labelFriends = labelFriends;
    }
}
