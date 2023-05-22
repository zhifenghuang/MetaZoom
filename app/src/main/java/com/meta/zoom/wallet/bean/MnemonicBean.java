package com.meta.zoom.wallet.bean;


import java.util.ArrayList;
import java.util.List;

public class MnemonicBean {
    private int position;
    private String text;
    private boolean isSelected;

    public MnemonicBean(int position, String text, boolean isSelected) {
        this.position = position;
        this.text = text;
        this.isSelected = isSelected;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public static List<MnemonicBean> transfer(List<String> strings) {
        List<MnemonicBean> list = new ArrayList<>();
        int index = 0;
        for (String str : strings) {
            list.add(new MnemonicBean(index++, str, false));
        }
        return list;
    }
}
