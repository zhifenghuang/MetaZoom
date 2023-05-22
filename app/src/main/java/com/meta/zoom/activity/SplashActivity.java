package com.meta.zoom.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.UserBean;
import com.common.lib.bean.WalletBean;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.meta.zoom.R;
import com.meta.zoom.contract.MainContract;
import com.meta.zoom.presenter.MainPresenter;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity<MainContract.Presenter> implements MainContract.View {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {

        UserBean myInfo = DataManager.getInstance().getUser();
        WalletBean wallet = DataManager.getInstance().getCurrentWallet();
        if (wallet == null) {
            findViewById(R.id.ll).postDelayed(new Runnable() {
                @Override
                public void run() {
                    openActivity(StartWalletActivity.class);
                    finish();
                }
            }, 1000);
        } else {
            if (myInfo == null || !myInfo.getLoginAccount().equals(wallet.getAddress())) {
                getPresenter().login(wallet.getAddress());
            } else {
                findViewById(R.id.ll).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openActivity(MainActivity.class);
                        finish();
                    }
                }, 1000);
            }
        }
    }

    @NonNull
    @Override
    protected MainContract.Presenter onCreatePresenter() {
        return new MainPresenter(this);
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void loginSuccess() {
        finishAllActivity();
        openActivity(MainActivity.class);
    }
}
