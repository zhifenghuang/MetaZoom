package com.common.lib.bean;

import com.common.lib.activity.db.IDBItemOperation;
import com.common.lib.manager.DataManager;

import java.util.List;

public class WalletBean extends IDBItemOperation {

    private Integer id;
    private String walletName;
    private int chainId;
    private String mnemonic;
    private String address;
    private String keystore;
    private String password;
    private String walletType;
    private String money;
    private long createTime;

    private String keystorePath;

    /**
     * 助记词
     */
    private List<String> mnemonicCode;

    public WalletBean() {
    }

    public List<String> getMnemonicCode() {
        return mnemonicCode;
    }

    public void setMnemonicCode(List<String> mnemonicCode) {
        this.mnemonicCode = mnemonicCode;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonicCode) {
        this.mnemonic = mnemonicCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWalletType() {
        return walletType;
    }

    public void setWalletType(String walletType) {
        this.walletType = walletType;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    @Override
    public String getPrimaryKeyName() {
        return "id";
    }

    @Override
    public String getTableName() {
        return "wallet";
    }
}
