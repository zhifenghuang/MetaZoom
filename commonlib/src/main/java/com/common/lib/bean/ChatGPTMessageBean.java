package com.common.lib.bean;

import java.io.Serializable;

public class ChatGPTMessageBean implements Serializable {

    private String role;  //role user 代表自己发的消息  assistant 代表gpt回复的消息
    private String content;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
