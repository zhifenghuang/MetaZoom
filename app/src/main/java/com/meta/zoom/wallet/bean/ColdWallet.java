package com.meta.zoom.wallet.bean;


import androidx.annotation.NonNull;

import com.common.lib.bean.WalletBean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 多链钱包
 *
 * @author rainking
 */
public class ColdWallet implements Serializable {


    /**
     * 助记词
     */
    private List<String> mnemonicCode;

    /**
     * 钱包列表
     */
    private WalletBean wallet;


    public ColdWallet(List<String> mnemonicCode, WalletBean wallet) {
        this.mnemonicCode = mnemonicCode;
        this.wallet = wallet;
    }

    public List<String> getMnemonicCode() {
        return mnemonicCode;
    }

    public void setMnemonicCode(List<String> mnemonicCode) {
        this.mnemonicCode = mnemonicCode;
    }

    public WalletBean getWallet() {
        return wallet;
    }

    public void setWallet(WalletBean wallet) {
        this.wallet = wallet;
    }

    @NonNull
    @Override
    public String toString() {
        return "ColdWallet{" +
                "mnemonicCode='" + Arrays.toString(mnemonicCode.toArray()) + '\'' +
                ", wallets=" + wallet +
                '}';
    }
}
