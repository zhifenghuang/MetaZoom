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

public class ImportKeystoreActivity extends BaseActivity<MainContract.Presenter> implements MainContract.View {

    private ChainBean mChain;
    private boolean isShowPsw;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_import_keystore;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setText(R.id.tvTitle, R.string.app_import_wallet);
        setViewsOnClickListener(R.id.tvOk, R.id.ivEye);
        mChain = (ChainBean) getIntent().getExtras().getSerializable(Constants.BUNDLE_EXTRA);
        initInputListener();
        isShowPsw = false;
    }

    @NonNull
    @Override
    protected MainContract.Presenter onCreatePresenter() {
        return new MainPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivEye:
                isShowPsw = !isShowPsw;
                setImage(R.id.ivEye, isShowPsw ? R.drawable.app_eye_open : R.drawable.app_eye_close);
                ((EditText) findViewById(R.id.etPassword)).setInputType(
                        isShowPsw ? InputType.TYPE_TEXT_VARIATION_PASSWORD : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
                break;
            case R.id.tvOk:
                String keystore = getTextById(R.id.etKeystore);
                String psw = getTextById(R.id.etPassword);
                showProgressDialog();
                WalletManager.getInstance().loadWalletByKeystore(keystore, psw).subscribe(this::loadSuccess, this::onError);
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
            DatabaseOperate.getInstance().insert(wallet);
            DataManager.getInstance().saveCurrentWallet(wallet);
        }
        DataManager.getInstance().saveCurrentChain(mChain);
        WalletManager.getInstance().resetNetwork(mChain);
        getPresenter().login(wallet.getAddress());

    }

    public void onError(Throwable e) {
        if (e.toString().contains("Invalid password provided")) {
            showToast(R.string.app_invalid_password_provided);
        } else {
            showToast(R.string.app_invalid_keystore_provided);
        }
        dismissProgressDialog();
    }

    private void initInputListener() {
        final TextView tvOk = findViewById(R.id.tvOk);
        tvOk.setEnabled(false);
        tvOk.setAlpha(0.25f);
        final EditText etKeystore = findViewById(R.id.etKeystore);
        final EditText etPassword = findViewById(R.id.etPassword);
        Observable.combineLatest(RxTextView.textChanges(etKeystore).skip(1),
                RxTextView.textChanges(etPassword).skip(1),
                (charSequence, charSequence2) ->
                        !TextUtils.isEmpty(etKeystore.getText().toString().trim()) &&
                                !TextUtils.isEmpty(etPassword.getText().toString().trim())).subscribe(aBoolean -> {
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
