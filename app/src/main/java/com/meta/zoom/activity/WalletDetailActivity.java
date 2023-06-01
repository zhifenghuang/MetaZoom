package com.meta.zoom.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.TokenBean;
import com.common.lib.bean.WalletBean;
import com.common.lib.constant.Constants;
import com.common.lib.constant.EventBusEvent;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.LogUtil;
import com.google.gson.Gson;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.meta.zoom.R;
import com.meta.zoom.adapter.TransactionAdapter;
import com.meta.zoom.wallet.WalletManager;
import com.meta.zoom.wallet.bean.Address;
import com.meta.zoom.wallet.bean.TransactionBean;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import io.reactivex.Observable;

public class WalletDetailActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {

    private TokenBean mToken;

    private TransactionAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_wallet_detail;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        mToken = (TokenBean) getIntent().getExtras().getSerializable(Constants.BUNDLE_EXTRA);
        setText(R.id.tvTitle, mToken.getSymbol());
        setViewsOnClickListener(R.id.tvTransfer, R.id.tvReceive);
        int drawableId = 0;
        if (TextUtils.isEmpty(mToken.getContractAddress())) {
            try {
                drawableId = getResources().getIdentifier("app_symbol_" + mToken.getChainId(), "drawable", getPackageName());
            } catch (Exception e) {
            }
        }
        setImage(R.id.iv, drawableId == 0 ? R.drawable.app_unknow_symbol : drawableId);
        setText(R.id.tvBalance, mToken.getBalance() + " " + mToken.getSymbol());
        setText(R.id.tvToUsdt, "");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
    }

    public void onResume() {
        super.onResume();
        WalletManager.getInstance()
                .getTransactions(mToken.getWalletAddress(), mToken.getContractAddress())
                .subscribe(this::onTransactions, this::onError);
    }

    private TransactionAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new TransactionAdapter(this);
        }
        return mAdapter;
    }

    private void onTransactions(ArrayList<TransactionBean> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        for (TransactionBean bean : transactions) {
            try {
                DatabaseOperate.getInstance().insert(bean);
            } catch (Exception e) {

            }
        }
        getAdapter().setNewInstance(transactions);
        getAdapter().notifyDataSetChanged();
    }

    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvTransfer:
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, mToken);
                openActivity(TransferActivity.class, bundle);
                break;
            case R.id.tvReceive:
                bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, mToken);
                openActivity(ReceiveActivity.class, bundle);
                break;
        }
    }

    private void onError(Throwable e) {
        LogUtil.LogE("onError: " + e);
    }

}
