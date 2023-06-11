package com.meta.zoom.activity;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.ChainBean;
import com.common.lib.bean.WalletBean;
import com.common.lib.constant.Constants;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.LogUtil;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.meta.zoom.R;
import com.meta.zoom.contract.MainContract;
import com.meta.zoom.presenter.MainPresenter;
import com.meta.zoom.wallet.WalletManager;

import io.reactivex.Observable;

public class ImportPrivateKeyActivity extends BaseActivity<MainContract.Presenter> implements MainContract.View {

    private ChainBean mChain;
    private boolean isShowPsw1;
    private boolean isShowPsw2;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_import_private_key;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setText(R.id.tvTitle, R.string.app_import_wallet);
        setViewsOnClickListener(R.id.tvOk, R.id.ivPasswordEye, R.id.ivSurePasswordEye);
        mChain = (ChainBean) getIntent().getExtras().getSerializable(Constants.BUNDLE_EXTRA);
        initInputListener();
        isShowPsw1 = false;
        isShowPsw2 = false;
    }

    @NonNull
    @Override
    protected MainContract.Presenter onCreatePresenter() {
        return new MainPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivPasswordEye:
                isShowPsw1 = !isShowPsw1;
                setImage(R.id.ivPasswordEye, isShowPsw1 ? R.drawable.app_eye_open : R.drawable.app_eye_close);
                ((EditText) findViewById(R.id.etPassword)).setInputType(
                        isShowPsw1 ? InputType.TYPE_TEXT_VARIATION_PASSWORD : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
                break;
            case R.id.ivSurePasswordEye:
                isShowPsw2 = !isShowPsw2;
                setImage(R.id.ivSurePasswordEye, isShowPsw2 ? R.drawable.app_eye_open : R.drawable.app_eye_close);
                ((EditText) findViewById(R.id.etConfirmPassword)).setInputType(
                        isShowPsw2 ? InputType.TYPE_TEXT_VARIATION_PASSWORD : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
                break;
            case R.id.tvOk:
                String privateKey = getTextById(R.id.etPrivateKey);
                if (privateKey.length() != 64) {
                    showToast(R.string.app_private_key_is_illegal);
                    return;
                }
                String walletName = getTextById(R.id.etWalletName);
//                if (DatabaseOperate.getInstance().walletNameChecking(walletName)) {
//                    showToast(R.string.create_wallet_name_repeat_tips);
//                    return;
//                }
                if (walletName.length() > 12) {
                    showToast(R.string.app_1_12_characters);
                    return;
                }
                String psw = getTextById(R.id.etPassword);
                String confirmPsw = getTextById(R.id.etConfirmPassword);
                if (!psw.equals(confirmPsw)) {
                    showToast(R.string.app_password_not_equal_confirm_password);
                    return;
                }
                showProgressDialog();
                WalletManager.getInstance().loadWalletByPrivateKey(walletName, privateKey, psw).subscribe(this::loadSuccess, this::onError);
                break;
        }
    }

    public void loadSuccess(WalletBean wallet) {
        dismissProgressDialog();
        WalletBean bean = DatabaseOperate.getInstance().getWallet(wallet.getAddress(), mChain.getChainId());
        if (bean != null) {
            DataManager.getInstance().saveCurrentWallet(bean);
        } else {
            wallet.setWalletType(mChain.getSymbol());
            wallet.setChainId(mChain.getChainId());
            wallet.setMoney("0");
            wallet.setId((int) DatabaseOperate.getInstance().insert(wallet));
            DataManager.getInstance().saveCurrentWallet(wallet);
        }
        DataManager.getInstance().saveCurrentChain(mChain);
        WalletManager.getInstance().resetNetwork(mChain);
        getPresenter().login(wallet.getAddress());
    }

    private void onError(Throwable error) {
        showToast(R.string.load_wallet_by_private_key_input_tip);
        dismissProgressDialog();
    }

    private void initInputListener() {
        final TextView tvOk = findViewById(R.id.tvOk);
        tvOk.setEnabled(false);
        tvOk.setAlpha(0.25f);
        final EditText etPrivateKey = findViewById(R.id.etPrivateKey);
        final EditText etWalletName = findViewById(R.id.etWalletName);
        final EditText etPassword = findViewById(R.id.etPassword);
        final EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Observable.combineLatest(RxTextView.textChanges(etPrivateKey).skip(1),
                RxTextView.textChanges(etWalletName).skip(1),
                RxTextView.textChanges(etPassword).skip(1),
                RxTextView.textChanges(etConfirmPassword).skip(1),
                (charSequence, charSequence2, charSequence3, charSequence4) ->
                        !TextUtils.isEmpty(etPrivateKey.getText().toString().trim()) &&
                                !TextUtils.isEmpty(etWalletName.getText().toString().trim()) &&
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

    @Override
    public void loginSuccess() {
        finishAllActivity();
        openActivity(MainActivity.class);
    }
}
