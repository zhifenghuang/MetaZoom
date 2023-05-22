package com.common.lib.bean;

import java.io.File;
import java.util.HashMap;

public class FileBean {
    private MessageType type;
    private File file;
    private HashMap<String, String> extra;

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public HashMap<String, String> getExtra() {
        return extra;
    }

    public void setExtra(HashMap<String, String> extra) {
        this.extra = extra;
    }
}
