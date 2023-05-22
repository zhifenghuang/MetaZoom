package com.common.lib.bean;

import com.common.lib.activity.db.IDBItemOperation;

public class ChainBean extends IDBItemOperation {

    private int chainId;
    private String chainName;
    private String rpcUrl;
    private String symbol;

    private String explore;

    public ChainBean() {

    }

    public ChainBean(int chainId, String chainName, String rpcUrl, String symbol, String explore) {
        this.chainId = chainId;
        this.chainName = chainName;
        this.rpcUrl = rpcUrl;
        this.symbol = symbol;
        this.explore = explore;
    }

    public String getExplore() {
        return explore;
    }

    public void setExplore(String explore) {
        this.explore = explore;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public String getRpcUrl() {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String getPrimaryKeyName() {
        return "chainId";
    }

    @Override
    public String getTableName() {
        return "chain";
    }
}
