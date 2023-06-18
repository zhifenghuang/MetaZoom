package com.meta.zoom.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.meta.zoom.R;
import com.meta.zoom.wallet.WalletManager;
import com.meta.zoom.wallet.bean.Address;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class AddTokenActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {

    private boolean mIsAddressCorrect;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_token;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setText(R.id.tvTitle, R.string.app_add_token);
        setViewsOnClickListener(R.id.tvOk);
        initInputListener();
        mIsAddressCorrect = false;
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
                String address = getTextById(R.id.etAddress);
                String symbol = getTextById(R.id.etSymbol);
                String decimalStr = getTextById(R.id.etDecimal);
                int decimal = 0;
                try {
                    decimal = Integer.valueOf(decimalStr);
                } catch (NumberFormatException ex) {
                    showToast(R.string.app_error_must_numeric);
                    return;
                }
                if (!Address.isAddress(address)) {
                    showToast(R.string.app_error_invalid_address);
                    return;
                }
                if (!mIsAddressCorrect) {
                    showProgressDialog();
                    WalletManager.getInstance().getTokenByAddress(address).subscribeOn(Schedulers.io())
                            .subscribe(AddTokenActivity.this::onGetTokenInfo,
                                    AddTokenActivity.this::onError);
                    return;
                }
                WalletBean wallet = DataManager.getInstance().getCurrentWallet();
                if (DatabaseOperate.getInstance().isHadToken(wallet.getChainId(), address, wallet.getAddress())) {
                    showToast(R.string.app_token_had_added);
                    return;
                }
                TokenBean bean = new TokenBean(wallet.getChainId(), wallet.getAddress(),
                        address, symbol, decimal);
                DatabaseOperate.getInstance().insert(bean);
                HashMap<String, Object> map = new HashMap<>();
                map.put(EventBusEvent.REFRESH_ASSETS, "");
                EventBus.getDefault().post(map);
                finish();
                break;
        }
    }

    private void initInputListener() {
        final TextView tvOk = findViewById(R.id.tvOk);
        tvOk.setEnabled(false);
        tvOk.setAlpha(0.25f);
        final EditText etAddress = findViewById(R.id.etAddress);
        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String address = s.toString().toLowerCase().trim();
                if (Address.isAddress(address)) {
                    showProgressDialog();
                    WalletManager.getInstance().getTokenByAddress(address).subscribeOn(Schedulers.io())
                            .subscribe(AddTokenActivity.this::onGetTokenInfo,
                                    AddTokenActivity.this::onError);
                } else {
                    mIsAddressCorrect = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        final EditText etSymbol = findViewById(R.id.etSymbol);
        final EditText etDecimal = findViewById(R.id.etDecimal);
        Observable.combineLatest(RxTextView.textChanges(etAddress).skip(1),
                RxTextView.textChanges(etSymbol).skip(1),
                RxTextView.textChanges(etDecimal).skip(1),
                (charSequence, charSequence2, charSequence3) ->
                        !TextUtils.isEmpty(etAddress.getText().toString().trim()) && !TextUtils.isEmpty(etDecimal.getText().toString().trim())
                                && !TextUtils.isEmpty(etSymbol.getText().toString().trim())).subscribe(aBoolean -> {
            if (aBoolean) {
                tvOk.setAlpha(1.0f);
                tvOk.setEnabled(true);
            } else {
                tvOk.setEnabled(false);
                tvOk.setAlpha(0.25f);
            }
        });
    }

    private void onGetTokenInfo(TokenBean bean) {
        LogUtil.LogE(bean.getSymbol() + ", " + bean.getTokenPrecision());
        dismissProgressDialog();
        setText(R.id.etSymbol, bean.getSymbol());
        setText(R.id.etDecimal, String.valueOf(bean.getTokenPrecision()));
        mIsAddressCorrect = true;
    }

    private void onError(Throwable throwable) {
        LogUtil.LogE("throwable: " + throwable.getMessage());
        dismissProgressDialog();
        mIsAddressCorrect = false;
        showToast(R.string.app_error_invalid_address);
        setText(R.id.etSymbol, "");
        setText(R.id.etDecimal, "");
    }
}
