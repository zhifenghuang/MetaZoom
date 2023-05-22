package com.common.lib.bean;

import android.text.TextUtils;

import java.io.Serializable;


public class GroupBean implements Serializable {
    private long id;
    private long groupId;
    private int groupRole;  //角色 1群员 2管理员 3群主
    private String name;
    private String icon;
    private int status;
    private String notice;
    private String introduction;
    private long ownerId;
    private int memberNum;
    private int top;
    private int ignore;

    private int allBlock;  //全群禁言
    private int joinType;  //加群方式 0群主邀请 1群员邀请
    private int joinStint; // 加群限制 0允许任何人加群 1不允许任何人加群
    private int payinState; // 付费进群 0正常 1付费
    private float payAmount; // 付费金额
    private int disableLink;
    private int disableFriend;
    private String memo;
    private String pinyinName;

    private int isQuit;

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getIgnore() {
        return ignore;
    }

    public void setIgnore(int ignore) {
        this.ignore = ignore;
    }

    public int getIsQuit() {
        return isQuit;
    }

    public void setIsQuit(int isQuit) {
        this.isQuit = isQuit;
    }

    public int getAllBlock() {
        return allBlock;
    }

    public void setAllBlock(int allBlock) {
        this.allBlock = allBlock;
    }

    public int getJoinType() {
        return joinType;
    }

    public void setJoinType(int joinType) {
        this.joinType = joinType;
    }

    public int getJoinStint() {
        return joinStint;
    }

    public void setJoinStint(int joinStint) {
        this.joinStint = joinStint;
    }

    public int getPayinState() {
        return payinState;
    }

    public void setPayinState(int payinState) {
        this.payinState = payinState;
    }

    public float getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(float payAmount) {
        this.payAmount = payAmount;
    }

    public int getDisableLink() {
        return disableLink;
    }

    public void setDisableLink(int disableLink) {
        this.disableLink = disableLink;
    }

    public int getDisableFriend() {
        return disableFriend;
    }

    public void setDisableFriend(int disableFriend) {
        this.disableFriend = disableFriend;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    private String myNickInGroup;

    public String getMyNickInGroup() {
        return myNickInGroup;
    }

    public void setMyNickInGroup(String myNickInGroup) {
        this.myNickInGroup = myNickInGroup;
    }

    public int getMemberNum() {
        return memberNum;
    }

    public void setMemberNum(int memberNum) {
        this.memberNum = memberNum;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public int getGroupRole() {
        return groupRole;
    }

    public void setGroupRole(int groupRole) {
        this.groupRole = groupRole;
    }

    public String getName() {
        return name;
    }

    public String getPinyinName() {
//        if (TextUtils.isEmpty(pinyinName)) {
//            try {
//                pinyinName = Pinyin.toPinyin(getName(), "").toLowerCase();
//            } catch (Exception e) {
//                pinyinName = name;
//            }
//        }
        return pinyinName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
