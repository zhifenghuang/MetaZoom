package com.meta.zoom.wallet.bean;

/**
 * 描述结果
 *
 * @author xyx on 2020/3/6 0006
 * @e-mail 384744573@qq.com
 * @see [相关类/方法](可选)
 */
public class UtxoBean {


    /**
     * block_no : 619648
     * output_no : 0
     * index : 655
     * txid : 43251af661dadc4677da2f803dbe13069cba930c87ffa9b244ef095519cd95e4
     * confirmations : 791
     * value : 0.0001
     */

    private String block_no;
    private long output_no;
    private String index;
    private String txid;
    private String confirmations;
    private String value;
    private long valueAmount;

    public String getBlock_no() {
        return block_no;
    }

    public void setBlock_no(String block_no) {
        this.block_no = block_no;
    }

    public long getOutput_no() {
        return output_no;
    }

    public void setOutput_no(long output_no) {
        this.output_no = output_no;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(String confirmations) {
        this.confirmations = confirmations;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getValueAmount() {
        return valueAmount;
    }

    public void setValueAmount(long valueAmount) {
        this.valueAmount = valueAmount;
    }
}
