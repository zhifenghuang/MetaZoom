package com.meta.zoom.wallet.bean;

import androidx.annotation.NonNull;

/**
 * 交易信息
 * {\"txhash\":\"交易hash\",\"nonce\":\"交易随机数\"}
 *
 * @author rainking
 */
public class EthSignature {

    private String txhash;
    private String nonce;

    public EthSignature(String txhash, String nonce) {
        this.txhash = txhash;
        this.nonce = nonce;
    }

    public String getTxhash() {
        return txhash;
    }

    public void setTxhash(String txhash) {
        this.txhash = txhash;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @NonNull
    @Override
    public String toString() {
        return "EthTransaction{" +
                "txhash='" + txhash + '\'' +
                ", nonce='" + nonce + '\'' +
                '}';
    }
}
