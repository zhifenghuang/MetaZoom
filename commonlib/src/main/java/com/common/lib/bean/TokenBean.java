package com.common.lib.bean;

import com.common.lib.activity.db.IDBItemOperation;

public class TokenBean extends IDBItemOperation {

    private Integer id;
    private int chainId;
    private String contractAddress;
    private String symbol;
    private int tokenPrecision;

    private String walletAddress;
    private String balance;

    public TokenBean() {

    }


    public TokenBean(int chainId, String walletAddress, String contractAddress, String symbol,
                     int tokenPrecision) {
        this.chainId = chainId;
        this.contractAddress = contractAddress;
        this.symbol = symbol;
        this.tokenPrecision = tokenPrecision;
        this.walletAddress = walletAddress;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getTokenPrecision() {
        return tokenPrecision;
    }

    public void setTokenPrecision(int tokenPrecision) {
        this.tokenPrecision = tokenPrecision;
    }

    @Override
    public String getPrimaryKeyName() {
        return "id";
    }

    @Override
    public String getTableName() {
        return "token";
    }
}
