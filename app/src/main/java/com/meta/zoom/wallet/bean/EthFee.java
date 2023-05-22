package com.meta.zoom.wallet.bean;

import java.math.BigDecimal;

/**
 * {\"fastest\":\"最快\",\"fast\":\"快\",\"average\":\"平均\",\"safeLow\":\"慢\"}
 *
 * @author rainking
 */
public class EthFee {


    /**
     * fast : 80
     * fastest : 100
     * safeLow : 10
     * average : 20
     * block_time : 10.93939393939394
     * blockNum : 9579003
     * speed : 0.7060470473575439
     * safeLowWait : 5.6
     * avgWait : 3.4
     * fastWait : 0.4
     * fastestWait : 0.4
     * gasPriceRange : {"4":182.3,"6":182.3,"8":182.3,"10":5.6,"15":5.2,"20":3.4,"25":3.4,"30":1.4,"35":1.3,"40":1,"45":1,"50":0.6,"55":0.6,"60":0.6,"65":0.6,"70":0.6,"75":0.6,"80":0.4,"85":0.4,"90":0.4,"95":0.4,"100":0.4}
     */

    private int fast;
    private int fastest;
    private int safeLow;
    private int average;
    private double block_time;
    private int blockNum;
    private double speed;
    private double safeLowWait;
    private double avgWait;
    private double fastWait;
    private double fastestWait;


    public int getFast() {
        return fast;
    }

    public BigDecimal getBigDecimalFast() {
        return new BigDecimal(getFast()).setScale(0, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal("10"));
    }


    public void setFast(int fast) {
        this.fast = fast;
    }

    public int getFastest() {
        return fastest;
    }


    public BigDecimal getBigDecimalFastest() {
        return new BigDecimal(getFastest()).setScale(0, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal("10"));
    }

    public void setFastest(int fastest) {
        this.fastest = fastest;
    }

    public int getSafeLow() {
        return safeLow;
    }

    public BigDecimal getBigDecimalSafeLow() {
        return new BigDecimal(getSafeLow()).setScale(0, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal("10"));
    }


    public void setSafeLow(int safeLow) {
        this.safeLow = safeLow;
    }

    public int getAverage() {
        return average;
    }


    public BigDecimal getBigDecimalAverage() {
        return new BigDecimal(getAverage()).setScale(0, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal("10"));
    }

    public void setAverage(int average) {
        this.average = average;
    }

    public double getBlock_time() {
        return block_time;
    }

    public void setBlock_time(double block_time) {
        this.block_time = block_time;
    }

    public int getBlockNum() {
        return blockNum;
    }

    public void setBlockNum(int blockNum) {
        this.blockNum = blockNum;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSafeLowWait() {
        return safeLowWait;
    }

    public void setSafeLowWait(double safeLowWait) {
        this.safeLowWait = safeLowWait;
    }

    public double getAvgWait() {
        return avgWait;
    }

    public void setAvgWait(double avgWait) {
        this.avgWait = avgWait;
    }

    public double getFastWait() {
        return fastWait;
    }

    public void setFastWait(double fastWait) {
        this.fastWait = fastWait;
    }

    public double getFastestWait() {
        return fastestWait;
    }

    public void setFastestWait(double fastestWait) {
        this.fastestWait = fastestWait;
    }

}
