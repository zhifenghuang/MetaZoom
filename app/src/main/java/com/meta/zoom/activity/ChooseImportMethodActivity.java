package com.meta.zoom.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.ChainBean;
import com.common.lib.constant.Constants;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.meta.zoom.R;

import java.util.ArrayList;

public class ChooseImportMethodActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {

    private ChainBean mChain;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_choose_import_method;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setViewsOnClickListener(R.id.rlMnemonic, R.id.rlPrivateKey, R.id.rlKeystore);
        mChain = (ChainBean) getIntent().getExtras().getSerializable(Constants.BUNDLE_EXTRA);
        setText(R.id.tvTitle, R.string.app_import_wallet);
    }

    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlMnemonic:
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, mChain);
                openActivity(ImportMnemonicActivity.class, bundle);
                break;
            case R.id.rlPrivateKey:
                bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, mChain);
                openActivity(ImportPrivateKeyActivity.class, bundle);
                break;
            case R.id.rlKeystore:
                bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, mChain);
                openActivity(ImportKeystoreActivity.class, bundle);
                break;
        }
    }
}
