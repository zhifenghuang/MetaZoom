package com.common.lib.bean;

public class ChatSettingBean {

    private int isReceiveNewMsg = 1; //是否接收新消息
    private int notInteruptMode = 0;//勿扰模式
    private int startTime = 23 * 60;  //勿扰模式开始时间,用分钟计算，24小时制
    private int endTime = 8 * 60;//勿扰模式结束时间
    private int isNeedVerifyWhenAddMe = 1;  //加我为朋友时需要验证
    private int readDeleteType = 0;//阅后即焚类型
    private int isAddPasswordP2P = 0;//端对端加密


    public int getIsReceiveNewMsg() {
        return isReceiveNewMsg;
    }

    public void setIsReceiveNewMsg(int isReceiveNewMsg) {
        this.isReceiveNewMsg = isReceiveNewMsg;
    }

    public int getNotInteruptMode() {
        return notInteruptMode;
    }

    public void setNotInteruptMode(int notInteruptMode) {
        this.notInteruptMode = notInteruptMode;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getIsNeedVerifyWhenAddMe() {
        return isNeedVerifyWhenAddMe;
    }

    public void setIsNeedVerifyWhenAddMe(int isNeedVerifyWhenAddMe) {
        this.isNeedVerifyWhenAddMe = isNeedVerifyWhenAddMe;
    }

    public int getReadDeleteType() {
        return readDeleteType;
    }

    public void setReadDeleteType(int readDeleteType) {
        this.readDeleteType = readDeleteType;
    }

    public int getIsAddPasswordP2P() {
        return isAddPasswordP2P;
    }

    public void setIsAddPasswordP2P(int isAddPasswordP2P) {
        this.isAddPasswordP2P = isAddPasswordP2P;
    }
}
