package com.meta.zoom.wallet.bean;

import java.io.Serializable;

/**
 * 钱包类型
 *
 * @author rainking
 */
public enum WalletType  implements Serializable {

    /**
     * ETH
     */
    ETH("ETH"),
    /**
     * BTC
     */
    BTC("BTC"),
    /**
     * USDT-OMNI
     */
    USDT_OMNI("USDT-OMNI"),
    /**
     * USDT-ERC20
     */
    USDT_ERC20("USDT-ERC20"),

    UTG("UTG");
//    /**
//     * A13
//     */
//    A13("A13");

    private String type;


    WalletType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
