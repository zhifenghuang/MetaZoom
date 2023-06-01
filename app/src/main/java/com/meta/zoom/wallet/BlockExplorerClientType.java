package com.meta.zoom.wallet;

import com.meta.zoom.wallet.bean.TransactionBean;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;

public interface BlockExplorerClientType {
    Observable<ArrayList<TransactionBean>> fetchTransactions(String forAddress, String forToken);
}
