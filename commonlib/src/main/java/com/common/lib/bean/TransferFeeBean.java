package com.common.lib.bean;

import java.io.Serializable;
import java.util.List;

public class TransferFeeBean implements Serializable {
    private float fee;
    private int type;

    public float getFee() {
        return fee;
    }

    public void setFee(float fee) {
        this.fee = fee;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}