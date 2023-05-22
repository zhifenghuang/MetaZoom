package com.common.lib.bean;

import java.io.Serializable;
import java.util.List;

public class FilterMsgBean implements Serializable {

    private long blockId;
    private String content;

    public long getBlockId() {
        return blockId;
    }

    public void setBlockId(long blockId) {
        this.blockId = blockId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
