package com.common.lib.bean;

import android.content.ContentValues;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;

public class MessageBean extends BasicMessage {

    private long toId;
    private long groupId;


    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getToId() {
        return toId;
    }

    public void setToId(long toId) {
        this.toId = toId;
    }

    @Override
    public String getTableName() {
        return "message";
    }

    public String toJson() {
        HashMap<String, Object> map = super.getMap();
        map.put("toId", toId);
        return new Gson().toJson(map);
    }

    @Override
    public ContentValues getValues() {
        ContentValues values = super.getValues();
        values.put("toId", toId);
        String tag = fromId < toId ? (fromId + "_" + toId) : (toId + "_" + fromId);
        values.put("tag", tag);
        return values;
    }

    public static MessageBean getSystemMsg(long fromId, long toId, String key, Object value) {
        MessageBean msg = new MessageBean();
        msg.setCmd(2010);
        msg.setMsgType(MessageType.TYPE_SYSTEM_MESSAGE.ordinal());
        msg.setFromId(fromId);
        msg.setToId(toId);
        try {
            JSONObject object = new JSONObject();
            object.put(key, value);
            msg.setContent(object.toString());
        } catch (Exception e) {

        }
        return msg;
    }

    public static MessageBean toMessage(BasicMessage basicMessage, long toId) {
        MessageBean msg = new MessageBean();
        msg.setCmd(2000);
        msg.setMsgType(basicMessage.getMsgType());
        msg.setFromId(basicMessage.getFromId());
        msg.setToId(toId);
        msg.setContent(basicMessage.getContent());
        msg.setUrl(basicMessage.getUrl());
        return msg;
    }
}
