package com.meta.zoom.wallet.bean;

/**
 * BTC 签名
 *
 * @author rainking
 */
public class BtcTransaction {

    private String txHash;
    private String hash;

    public BtcTransaction(String txHash, String hash) {
        this.txHash = txHash;
        this.hash = hash;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
