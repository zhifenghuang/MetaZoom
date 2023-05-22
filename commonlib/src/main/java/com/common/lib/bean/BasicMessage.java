package com.common.lib.bean;

import android.content.ContentValues;
import android.text.TextUtils;

import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.activity.db.IDBItemOperation;
import com.common.lib.manager.DataManager;

import java.util.HashMap;
import java.util.UUID;

public abstract class BasicMessage extends IDBItemOperation {

    private int cmd;
    private String messageId;
    protected long fromId;
    private int msgType;   //0表示文字消息，1表示图片消息，2表示语音消息,3表示小视频消息,4表示位置信息
    private String content;
    private String url;
    private int status;
    private long expire;
    private long createTime;
    private int sendStatus;
    private int receiveStatus;
    private int isRead;
    private int fileProgress;  //文件进度，100表示完成,-1表示失败
    private String extra;  //额外补充字段，目前用于存放发送消息者用户信息，json

    private String translate; //翻译

    private String tag;


    private int unReadNum;  //未读消息数目

    private boolean isCheck;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }


    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public BasicMessage() {
        UserBean myInfo = DataManager.getInstance().getUser();
        messageId = UUID.randomUUID().toString() + "_" + myInfo.getUserId();
        createTime = System.currentTimeMillis();
    }

    public int getUnReadNum() {
        return unReadNum;
    }

    public void setUnReadNum(int unReadNum) {
        this.unReadNum = unReadNum;
    }

    public boolean isMySendMsg(long myUserId) {
        return fromId == myUserId;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getFromId() {
        return fromId;
    }

    public void setFromId(long fromId) {
        this.fromId = fromId;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public int getReceiveStatus() {
        return receiveStatus;
    }

    public void setReceiveStatus(int receiveStatus) {
        this.receiveStatus = receiveStatus;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public int getFileProgress() {
        return fileProgress;
    }

    public void setFileProgress(int fileProgress) {
        this.fileProgress = fileProgress;
    }

    @Override
    public String getPrimaryKeyName() {
        return "messageId";
    }

    public HashMap<String, Object> getMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("cmd", cmd);
        map.put("messageId", messageId);
        map.put("fromId", fromId);
        map.put("msgType", msgType);
        map.put("content", content);
        map.put("createTime", createTime);
        map.put("extra", extra);
        map.put("expire", expire);
        if (!TextUtils.isEmpty(url)) {
            map.put("url", url);
        }
        return map;
    }

    /**
     * 消息接收情况
     */
    public void sureReceiveStatus(int status) {
        receiveStatus = status;
        ContentValues contentValues = new ContentValues();
        contentValues.put(getPrimaryKeyName(), messageId);
        contentValues.put("receiveStatus", receiveStatus);
        DatabaseOperate.getInstance().update(this, contentValues);
    }

    private boolean isPlayingVoice;

    public void setPlayingVoice(boolean isPlayingVoice) {
        this.isPlayingVoice = isPlayingVoice;
    }

    public boolean isPlayingVoice() {
        return isPlayingVoice;
    }


    public void updateTranslate(String translate) {
        this.translate = translate;
        ContentValues contentValues = new ContentValues();
        contentValues.put(getPrimaryKeyName(), messageId);
        contentValues.put("translate", translate);
        DatabaseOperate.getInstance().update(this, contentValues);
    }


    /**
     * 消息已发送成功
     */
    public void sureSendMsg() {
        sendStatus = 1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(getPrimaryKeyName(), messageId);
        contentValues.put("sendStatus", sendStatus);
        DatabaseOperate.getInstance().update(this, contentValues);
    }

    public void sendMsgFailed() {
        sendStatus = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(getPrimaryKeyName(), messageId);
        contentValues.put("sendStatus", sendStatus);
        DatabaseOperate.getInstance().update(this, contentValues);
    }

    public void readMsg() {
        if (isRead == 1) {
            return;
        }
        isRead = 1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(getPrimaryKeyName(), messageId);
        contentValues.put("isRead", 1);
        DatabaseOperate.getInstance().update(this, contentValues);
    }

    public void sendFileSuccess() {
        fileProgress = 100;
        ContentValues contentValues = new ContentValues();
        contentValues.put(getPrimaryKeyName(), messageId);
        contentValues.put("fileProgress", fileProgress);
        DatabaseOperate.getInstance().update(this, contentValues);
    }

    public void sendFileFailed() {
        fileProgress = -1;
        sendStatus = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(getPrimaryKeyName(), messageId);
        contentValues.put("fileProgress", fileProgress);
        contentValues.put("sendStatus", sendStatus);
        DatabaseOperate.getInstance().update(this, contentValues);
    }

    @Override
    public ContentValues getValues() {
        UserBean myInfo = DataManager.getInstance().getUser();
        ContentValues values = new ContentValues();
        values.put("cmd", cmd);
        values.put("messageId", messageId);
        values.put("msgType", msgType);
        values.put("fromId", fromId);
        values.put("sendStatus", sendStatus);
        values.put("receiveStatus", receiveStatus);
        values.put("content", content);
        values.put("url", url);
        values.put("createTime", createTime);
        values.put("isRead", isRead);
        values.put("isDel", 0);
        values.put("expire", expire);
        values.put("owerId", myInfo.getUserId());
        values.put("extra", extra);
        return values;
    }

    public abstract String toJson();
}
