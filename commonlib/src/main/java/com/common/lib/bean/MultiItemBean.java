package com.common.lib.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public class MultiItemBean implements MultiItemEntity {

    private int itemType;
    private Object bean;
    private Object bean2;

    public MultiItemBean(int itemType, Object bean) {
        this.itemType = itemType;
        this.bean = bean;
    }

    public MultiItemBean(int itemType, Object bean, Object bean2) {
        this.itemType = itemType;
        this.bean = bean;
        this.bean2=bean2;
    }

    public Object getBean2() {
        return bean2;
    }

    public void setBean2(Object bean2) {
        this.bean2 = bean2;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
