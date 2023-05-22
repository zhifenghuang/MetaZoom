package com.meta.zoom.wallet.bean;

/**
 * {"fastestFee":22,"halfHourFee":20,"hourFee":4}
 * 有3种速度，自己选择即可,选快一点
 *
 * @author rainking
 */
public class BtcFee {


    /**
     * fastestFee : 16
     * halfHourFee : 16
     * hourFee : 6
     */

    /**
     * 最快速
     */

    private long fastestFee;
    /**
     * 30分钟
     */
    private long halfHourFee;
    /**
     * 60分钟
     */
    private long hourFee;

    public long getFastestFee() {
        return fastestFee;
    }

    public void setFastestFee(long fastestFee) {
        this.fastestFee = fastestFee;
    }

    public long getHalfHourFee() {
        return halfHourFee;
    }

    public void setHalfHourFee(long halfHourFee) {
        this.halfHourFee = halfHourFee;
    }

    public long getHourFee() {
        return hourFee;
    }

    public void setHourFee(long hourFee) {
        this.hourFee = hourFee;
    }
}
