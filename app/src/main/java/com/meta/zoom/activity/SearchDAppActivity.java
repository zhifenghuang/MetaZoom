package com.meta.zoom.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.ChainBean;
import com.common.lib.constant.Constants;
import com.common.lib.constant.EventBusEvent;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.meta.zoom.R;
import com.meta.zoom.wallet.WalletManager;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import io.reactivex.Observable;

public class SearchDAppActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {


    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_network;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setText(R.id.tvTitle, R.string.app_custom_network);
        initInputListener();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(Constants.BUNDLE_EXTRA)) {
            ChainBean bean = (ChainBean) bundle.getSerializable(Constants.BUNDLE_EXTRA);
            setText(R.id.etNetworkName, bean.getChainName());
            setText(R.id.etRPC, bean.getRpcUrl());
            setText(R.id.etChainID, String.valueOf(bean.getChainId()));
            setText(R.id.etDefaultToken, bean.getSymbol());
            setText(R.id.etBrowser, bean.getExplore());
            if (bean.getFix() == 1) {
                setViewGone(R.id.tvOk);
            }
        }
        setViewsOnClickListener(R.id.tvOk);
    }


    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvOk:
                int chainID = 0;
                try {
                    chainID = Integer.valueOf(getTextById(R.id.etChainID));
                } catch (NumberFormatException ex) {
                    showToast(R.string.app_error_must_numeric);
                    return;
                }
                ChainBean bean = DatabaseOperate.getInstance().getChain(chainID);
                if (bean != null) {
                    if (bean.getFix() == 1) {  //不可更改
                        showToast(R.string.app_network_had_added);
                        return;
                    }
                    bean.setChainName(getTextById(R.id.etNetworkName));
                    bean.setRpcUrl(getTextById(R.id.etRPC));
                    bean.setSymbol(getTextById(R.id.etDefaultToken));
                    bean.setExplore(getTextById(R.id.etBrowser));
                    DatabaseOperate.getInstance().update(bean);
                    if (bean.getChainId() == DataManager.getInstance().getCurrentChain().getChainId()) {
                        DataManager.getInstance().saveCurrentChain(bean);
                        WalletManager.getInstance().resetNetwork(bean);
                    }
                } else {
                    bean = new ChainBean(chainID, getTextById(R.id.etNetworkName), getTextById(R.id.etRPC)
                            , getTextById(R.id.etDefaultToken), getTextById(R.id.etBrowser), 0);
                    DatabaseOperate.getInstance().insert(bean);
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put(EventBusEvent.REFRESH_NETWORK, "");
                EventBus.getDefault().post(map);
                finish();
                break;
        }
    }

    private void initInputListener() {
        final TextView tvOk = findViewById(R.id.tvOk);
        tvOk.setEnabled(false);
        tvOk.setAlpha(0.25f);
        final EditText etNetworkName = findViewById(R.id.etNetworkName);
        final EditText etRPC = findViewById(R.id.etRPC);
        final EditText etChainID = findViewById(R.id.etChainID);
        Observable.combineLatest(RxTextView.textChanges(etNetworkName).skip(1),
                RxTextView.textChanges(etRPC).skip(1),
                RxTextView.textChanges(etChainID).skip(1),
                (charSequence, charSequence2, charSequence3) ->
                        !TextUtils.isEmpty(etNetworkName.getText().toString().trim())
                                && !TextUtils.isEmpty(etRPC.getText().toString().trim())
                                && !TextUtils.isEmpty(etChainID.getText().toString().trim())).subscribe(aBoolean -> {
            if (aBoolean) {
                tvOk.setAlpha(1.0f);
                tvOk.setEnabled(true);
            } else {
                tvOk.setEnabled(false);
                tvOk.setAlpha(0.25f);
            }
        });
    }
}
