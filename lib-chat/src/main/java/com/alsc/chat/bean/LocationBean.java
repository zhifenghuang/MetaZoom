package com.alsc.chat.bean;

import com.amap.api.services.core.PoiItem;

public class LocationBean {
    private boolean isCheck;

    private PoiItem poiItem;


    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public PoiItem getPoiItem() {
        return poiItem;
    }

    public void setPoiItem(PoiItem poiItem) {
        this.poiItem = poiItem;
    }
}
