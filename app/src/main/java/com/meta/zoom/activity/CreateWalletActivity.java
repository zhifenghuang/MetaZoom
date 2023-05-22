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
import com.common.lib.constant.Constants;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.LogUtil;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.meta.zoom.R;
import com.common.lib.bean.ChainBean;
import com.common.lib.bean.WalletBean;
import com.meta.zoom.wallet.WalletManager;

import io.reactivex.Observable;

public class CreateWalletActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {

    private ChainBean mChain;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_wallet;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setViewsOnClickListener(R.id.tvOk);
        mChain = (ChainBean) getIntent().getExtras().getSerializable(Constants.BUNDLE_EXTRA);
        setText(R.id.tvTitle, getString(R.string.app_create_xxx_wallet, mChain.getSymbol()));
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
            case R.id.tvOk:
                String walletName = getTextById(R.id.etWalletName);
                String psw = getTextById(R.id.etPassword);
                String confirmPsw = getTextById(R.id.etConfirmPassword);
                boolean verifyWalletInfo = verifyInfo(walletName, psw, confirmPsw);
                if (verifyWalletInfo) {
                    showProgressDialog();
                    WalletManager.getInstance().create(walletName, psw, confirmPsw, "")
                            .subscribe(this::jumpToWalletBackUp, this::showError);
                }
                break;
        }
    }

    public void showError(Throwable errorInfo) {
        dismissProgressDialog();
        LogUtil.LogE( errorInfo);
        showToast(errorInfo.toString());
    }

    private void jumpToWalletBackUp(WalletBean wallet){
        dismissProgressDialog();
        wallet.setWalletType(mChain.getSymbol());
        wallet.setChainId(mChain.getChainId());
        wallet.setMoney("0");
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.BUNDLE_EXTRA, wallet);
        openActivity(BackUpMnemonicActivity.class, bundle);
    }

    private boolean verifyInfo(String walletName, String walletPwd, String confirmPwd) {
        if (DatabaseOperate.getInstance().walletNameChecking(walletName)) {
            showToast(R.string.create_wallet_name_repeat_tips);
            // 同时不可重复
            return false;
        } else if (TextUtils.isEmpty(walletName)) {
            showToast(R.string.create_wallet_name_input_tips);
            // 同时不可重复
            return false;
        } else if (TextUtils.isEmpty(walletPwd)) {
            showToast(R.string.create_wallet_pwd_input_tips);
            // 同时判断强弱
            return false;
        } else if (TextUtils.isEmpty(confirmPwd) || !TextUtils.equals(confirmPwd, walletPwd)) {
            showToast(R.string.create_wallet_pwd_confirm_input_tips);
            return false;
        }
        return true;
    }

    private void initInputListener() {
        final TextView tvOk = findViewById(R.id.tvOk);
        tvOk.setEnabled(false);
        tvOk.setAlpha(0.25f);
        final EditText etWalletName = findViewById(R.id.etWalletName);
        final EditText etPassword = findViewById(R.id.etPassword);
        final EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Observable.combineLatest(RxTextView.textChanges(etWalletName).skip(1),
                RxTextView.textChanges(etPassword).skip(1),
                RxTextView.textChanges(etConfirmPassword).skip(1),
                (charSequence, charSequence2, charSequence3) -> !TextUtils.isEmpty(etWalletName.getText().toString().trim()) &&
                        !TextUtils.isEmpty(etPassword.getText().toString().trim()) &&
                        !TextUtils.isEmpty(etConfirmPassword.getText().toString().trim())).subscribe(aBoolean -> {
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
