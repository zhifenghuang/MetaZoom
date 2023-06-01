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
import com.common.lib.bean.TokenBean;
import com.common.lib.bean.WalletBean;
import com.common.lib.constant.EventBusEvent;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.meta.zoom.R;
import com.meta.zoom.wallet.bean.Address;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import io.reactivex.Observable;

public class AddNetworkActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {


    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_network;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setText(R.id.tvTitle, R.string.app_custom_network);
//        setViewVisible(R.id.tvRight);
//        setText(R.id.tvRight, R.string.app_easy_add);
        setViewsOnClickListener(R.id.tvRight, R.id.tvOk);
        initInputListener();
    }

    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvRight:
                break;
            case R.id.tvOk:
                int chainID = 0;
                try {
                    chainID = Integer.valueOf(getTextById(R.id.etChainID));
                } catch (NumberFormatException ex) {
                    showToast(R.string.app_error_must_numeric);
                    return;
                }
                if (DatabaseOperate.getInstance().isHadChain(chainID)) {
                    showToast(R.string.app_network_had_added);
                    return;
                }
                ChainBean bean = new ChainBean(chainID, getTextById(R.id.etNetworkName), getTextById(R.id.etRPC)
                        , getTextById(R.id.etDefaultToken), getTextById(R.id.etBrowser));
                DatabaseOperate.getInstance().insert(bean);
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
