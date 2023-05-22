package com.common.lib.bean;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class EnvelopeBean implements Serializable {

    private String envelopeId;
    private long userId;
    private String name;
    private double amount; //
    private double sAmount;
    private int totalCount;
    private String remark;
    private int type;  //1群普通红包群2手气红包3私人转转4私人红包5新人红包
    private String toId;
    private String groupId;
    private long endTime;
    private long receiveTime;
    private int status;//1领取中2已领完3已到期
    private String createTime;
    private String avatarUrl;
    private String nickName;
    private String senderName;
    private ArrayList<Session> session;
    private int sessionCount;

    public int getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("envelopeId", envelopeId);
        map.put("userId", String.valueOf(userId));
        map.put("amount", amount);
        map.put("nickName", nickName);
        return map;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public double getsAmount() {
        return sAmount;
    }

    public void setsAmount(double sAmount) {
        this.sAmount = sAmount;
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public ArrayList<Session> getSession() {
        return session;
    }

    public void setSession(ArrayList<Session> session) {
        this.session = session;
    }

    public String getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(String envelopeId) {
        this.envelopeId = envelopeId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getToId() {
        if (TextUtils.isEmpty(toId)) {
            return 0;
        }
        return Long.parseLong(toId);
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public long getGroupId() {
        if (TextUtils.isEmpty(groupId)) {
            return 0;
        }
        return Long.parseLong(groupId);
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public static class Session implements Serializable {

        private String sessionId;
        private String envelopeId;
        private long userId;
        private String nickName;
        private double amount;
        private String createTime;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getEnvelopeId() {
            return envelopeId;
        }

        public void setEnvelopeId(String envelopeId) {
            this.envelopeId = envelopeId;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }
    }
}
