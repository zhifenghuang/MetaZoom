package com.meta.zoom.web3;


import com.meta.zoom.web3.entity.Web3Transaction;

public interface OnSignTransactionListener {
    void onSignTransaction(Web3Transaction transaction , String url);
}
