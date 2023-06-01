package com.meta.zoom.wallet;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.common.lib.bean.BasicResponse;
import com.common.lib.utils.LogUtil;
import com.google.gson.Gson;
import com.meta.zoom.wallet.bean.TransactionBean;

import java.util.ArrayList;

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.Retrofit;
import retrofit2.http.Query;
import io.reactivex.rxjava3.core.Observable;

public class BlockExplorerClient implements BlockExplorerClientType {

    private final OkHttpClient httpClient;
    private Gson gson;

    private TransactionsApiClient transactionsApiClient;

    public BlockExplorerClient(
            OkHttpClient httpClient) {
        this.httpClient = httpClient;
        this.gson = new Gson();
    }

    public void buildApiClient(String baseUrl) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor =
                new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(@NonNull String s) {
                        LogUtil.LogE(s);
                    }
                });
        builder.addInterceptor(loggingInterceptor);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        transactionsApiClient = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(TransactionsApiClient.class);
    }

    @Override
    public Observable<ArrayList<TransactionBean>> fetchTransactions(String address, String tokenAddress) {
        LogUtil.LogE("fetchTransactions:" + address);
        if (TextUtils.isEmpty(tokenAddress)) {
            return transactionsApiClient
                    .fetchTransactions(address, "account", "txlist", "desc")
                    //   .lift(apiError(gson))
                    .map(r -> r.getResult())
                    .subscribeOn(Schedulers.io());
        } else {
            return transactionsApiClient
                    .fetchTransactions(address, tokenAddress, "account", "tokentx", "desc")
                    // .lift(apiError(gson))
                    .map(r -> r.getResult())
                    .subscribeOn(Schedulers.io());
        }

    }


    // api.etherscan.io
    // TODO: need add apiKey
    private interface TransactionsApiClient {
        @GET("/api")
        Observable<BasicResponse<ArrayList<TransactionBean>>> fetchTransactions(
                @Query("address") String address,
                @Query("module") String module,
                @Query("action") String action,
                @Query("sort") String sort);

        @GET("/api")
        Observable<BasicResponse<ArrayList<TransactionBean>>> fetchTransactions(
                @Query("address") String address,
                @Query("contractaddress") String contract,
                @Query("module") String module,
                @Query("action") String action,
                @Query("sort") String sort);
    }


}

