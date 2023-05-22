package com.meta.zoom.wallet.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

/**
 * @author rainking
 */
public class JnWallet implements Serializable {


    /**
     * 钱包名称
     */
    private String name;

    /**
     * 助记词
     */
    private List<String> mnemonicCode;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 钱包地址
     */
    private String address;

    /**
     * keystore
     */
    private Object keystore;
    /**
     * 对应币的数量
     */
    private String money;
    /**
     * 行情价格
     */
    private String price;

    private WalletType walletType;

    public JnWallet() {
    }

    public JnWallet(String name, String privateKey, String publicKey, String address, WalletType walletType) {
        this.name = name;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
        this.walletType = walletType;
    }

    public JnWallet(String name, String privateKey, String publicKey, String address, Object keystore, WalletType walletType) {
        this.name = name;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
        this.keystore = keystore;
        this.walletType = walletType;
    }


    public JnWallet(String name, List<String> mnemonicCode, String privateKey, String publicKey, String address, Object keystore, WalletType walletType) {
        this.name = name;
        this.mnemonicCode = mnemonicCode;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
        this.keystore = keystore;
        this.walletType = walletType;
    }


    public JnWallet(String name, List<String> mnemonicCode, String privateKey, String publicKey, String address, WalletType walletType) {
        this.name = name;
        this.mnemonicCode = mnemonicCode;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
        this.walletType = walletType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Object getKeystore() {
        return keystore;
    }

    public void setKeystore(Object keystore) {
        this.keystore = keystore;
    }


    public WalletType getWalletType() {
        return walletType;
    }

    public void setWalletType(WalletType walletType) {
        this.walletType = walletType;
    }

    public List<String> getMnemonicCode() {
        return mnemonicCode;
    }

    public void setMnemonicCode(List<String> mnemonicCode) {
        this.mnemonicCode = mnemonicCode;
    }

    public String getMoney() {
        if (money!=null) {
            return money;
        } else {
            return "";
        }
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @NonNull
    @Override
    public String toString() {
        return "JnWallet{" +
                "name='" + name + '\'' +
//                ", mnemonicCode=" + Objects.requireNonNull(Arrays.toString(mnemonicCode.toArray())) +
                ", privateKey='" + privateKey + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", address='" + address + '\'' +
                ", keystore=" + keystore +
                ", walletType=" + walletType +
                ", money=" + money +
                ", price=" + price +
                '}';
    }
}
