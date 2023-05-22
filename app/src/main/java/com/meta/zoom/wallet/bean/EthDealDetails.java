package com.meta.zoom.wallet.bean;

/**
 * 交易详情
 *
 * @author rainking
 */
public class EthDealDetails {

    /**
     * blockHash : 0x3cc7e79d2825af1d2eb43991e275500bab7e2e3027f1f66c794ca361114f577e
     * contractAddress : 0xe38ab2ad0e3e85983f76fe235b6a9869cda0502d
     * nonce : 9
     * toTokenAddress : 0x2475c0931387ee47aa85a81b30e6794512e83cd3
     * result : true
     * blockNumber : 9563547
     * gas : 60000
     * from : 0xc2127cff5d866f027adc3edac0180b9f71b909cc
     * to : 0xe38ab2ad0e3e85983f76fe235b6a9869cda0502d
     * tokenValue : 1000000000000000000
     * value : 0
     * hash : 0xb2c5dd403dc737556cdb81bffb1121299fc93d8816df7d764b44ed0d6a764afe
     * transIndex : 132
     * gasPrice : 1000000000
     */


    /**
     * 区块hash
     */
    private String blockHash;
    /**
     * 交易随机数
     */
    private int nonce;
    /**
     * 结果
     */
    private boolean result;
    /**
     * 区块编号
     */
    private int blockNumber;
    /**
     * gas值
     */
    private int gas;
    /**
     * 发送地址
     */
    private String from;
    /**
     * 接收地址
     */
    private String to;
    /**
     * 金额
     */
    private int value;
    /**
     * 交易hash
     */
    private String hash;
    /**
     * 交易索引
     */
    private int transIndex;
    /**
     * 手续费
     */
    private int gasPrice;

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }


    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }


    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public int getGas() {
        return gas;
    }

    public void setGas(int gas) {
        this.gas = gas;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }


    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getTransIndex() {
        return transIndex;
    }

    public void setTransIndex(int transIndex) {
        this.transIndex = transIndex;
    }

    public int getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(int gasPrice) {
        this.gasPrice = gasPrice;
    }

    @Override
    public String toString() {
        return "EthDealDetails{" +
                "blockHash='" + blockHash + '\'' +
                ", nonce=" + nonce +
                ", result=" + result +
                ", blockNumber=" + blockNumber +
                ", gas=" + gas +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", value=" + value +
                ", hash='" + hash + '\'' +
                ", transIndex=" + transIndex +
                ", gasPrice=" + gasPrice +
                '}';
    }
}
