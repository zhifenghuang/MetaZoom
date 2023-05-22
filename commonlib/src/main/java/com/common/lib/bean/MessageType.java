package com.common.lib.bean;

public enum MessageType {
    TYPE_TEXT,
    TYPE_IMAGE,
    TYPE_VOICE,
    TYPE_VIDEO,
    TYPE_LOCATION,
    TYPE_FILE,
    TYPE_RED_PACKAGE,  //红包
    TYPE_TRANSFER,    //转账
    TYPE_RECOMAND_USER,  //个人名片
    TYPE_QUESTION,  //客服问题
    TYPE_SELECT_SERVICE, //选择客服
    TYPE_INVITE_TO_GROUP, //邀请进群消息
    TYPE_RECEIVE_RED_PACKAGE,  //红包已收
    TYPE_RECEIVE_TRANSFER,    //转账已收
    TYPE_INVITE_PAY_IN_GROUP,  //付费邀请进群
    TYPE_NEW_MEMBER_RED_PACKAGE,//新人红包
    TYPE_GROUP_SYSTEM_MSG,//群系统消息
    TYPE_REMOVE_FROM_GROUP, //移出群聊
    TYPE_IN_GROUP_BY_QRCODE,//通过扫描二维码加入群
    TYPE_UPDATE_GROUP_NAME,//修改群名字
    TYPE_UPDATE_GROUP_NOTICE,//修改群公告
    TYPE_ALL_FORBID_CHAT,//全体禁言
    TYPE_ALL_REMOVE_FORBID_CHAT,//全体解除禁言
    TYPE_FORBID_CHAT,//禁言个体
    TYPE_REOMVE_FORBID_CHAT,//解除个体禁言
    TYPE_DELETE_MSG,//删除发出去的消息
    TYPE_GROUP_AT_MEMBER_MSG,//群聊@人消息
    TYPE_SYSTEM_MESSAGE,  //系统消息
}
