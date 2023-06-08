package com.meta.zoom.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.activity.BaseActivity;
import com.common.lib.constant.Constants;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.meta.zoom.R;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.ChainBean;

import java.util.ArrayList;

public class StartWalletActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {

    private ChainBean mChain;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_start_wallet;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setViewsOnClickListener(R.id.rlNewWallet, R.id.rlImportWallet);
        mChain= (ChainBean) getIntent().getExtras().getSerializable(Constants.BUNDLE_EXTRA);
    }

    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlNewWallet:
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, mChain);
                openActivity(CreateWalletActivity.class, bundle);
                break;
            case R.id.rlImportWallet:
                bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, mChain);
                openActivity(ChooseImportMethodActivity.class, bundle);
                break;
        }
    }
}
