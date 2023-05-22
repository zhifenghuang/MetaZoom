package com.common.lib.bean;

import android.content.ContentValues;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;

public class GroupMessageBean extends BasicMessage {

    private long groupId;

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }


    @Override
    public String getTableName() {
        return "group_message";
    }

    public String toJson() {
        HashMap<String, Object> map = getMap();
        map.put("groupId", groupId);
        return new Gson().toJson(map);
    }

    @Override
    public ContentValues getValues() {
        ContentValues values = super.getValues();
        values.put("groupId", groupId);
        return values;
    }

    public static GroupMessageBean toGroupMessage(BasicMessage basicMessage, long groupId) {
        GroupMessageBean msg = new GroupMessageBean();
        msg.setCmd(2100);
        msg.setMsgType(basicMessage.getMsgType());
        msg.setFromId(basicMessage.getFromId());
        msg.setGroupId(groupId);
        msg.setContent(basicMessage.getContent());
        msg.setUrl(basicMessage.getUrl());
        msg.setExtra(basicMessage.getExtra());
        msg.setExpire(basicMessage.getExpire());
        msg.setSendStatus(basicMessage.getSendStatus());
        return msg;
    }

    public static GroupMessageBean getGroupSystemMsg(long fromId, long groupId, String key, Object value) {
        GroupMessageBean msg = new GroupMessageBean();
        msg.setCmd(2100);
        msg.setMsgType(MessageType.TYPE_GROUP_SYSTEM_MSG.ordinal());
        msg.setFromId(fromId);
        msg.setGroupId(groupId);
        try {
            JSONObject object = new JSONObject();
            object.put(key, value);
            msg.setContent(object.toString());
        } catch (Exception e) {

        }
        return msg;
    }
}
