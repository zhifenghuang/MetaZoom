package com.common.lib.bean;

import com.common.lib.activity.db.IDBItemOperation;
import com.common.lib.manager.DataManager;

import java.util.List;

public class TokenBean extends IDBItemOperation {

    private int id;
    private String walletName;
    private int chainId;
    private String mnemonic;
    private String privateKey;
    private String publicKey;
    private String address;
    private String keystore;
    private String password;
    private String walletType;
    private String money;
    private long createTime;

    /**
     * 助记词
     */
    private List<String> mnemonicCode;

    public TokenBean() {
    }

    public TokenBean(String walletName, String privateKey, String publicKey, String address, String keystore, List<String> mnemonicCode) {
        this.walletName = walletName;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
        this.keystore = keystore;
        this.mnemonicCode = mnemonicCode;
        this.mnemonic = DataManager.getInstance().getGson().toJson(mnemonicCode);
        createTime = System.currentTimeMillis();
    }

    public TokenBean(String walletName, String privateKey, String publicKey, String address, String keystore) {
        this.walletName = walletName;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
        this.keystore = keystore;
        createTime = System.currentTimeMillis();
    }

    public List<String> getMnemonicCode() {
        return mnemonicCode;
    }

    public void setMnemonicCode(List<String> mnemonicCode) {
        this.mnemonicCode = mnemonicCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
        this.mnemonic = mnemonic;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
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

    @Override
    public String getPrimaryKeyName() {
        return "id";
    }

    @Override
    public String getTableName() {
        return "wallet";
    }
}
