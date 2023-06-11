package com.meta.zoom.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.ChainBean;
import com.common.lib.constant.Constants;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.LogUtil;
import com.meta.zoom.R;
import com.meta.zoom.adapter.MnemonicAdapter;
import com.meta.zoom.wallet.bean.MnemonicBean;
import com.common.lib.bean.WalletBean;

public class BackUpMnemonicActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {

    private WalletBean mWallet;
    private ChainBean mChain;

    private MnemonicAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_back_up_mnemonic;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setText(R.id.tvTitle,R.string.app_back_up);
        setViewsOnClickListener(R.id.tvBackedUp);
        Bundle bundle = getIntent().getExtras();
        mWallet = (WalletBean) bundle.getSerializable(Constants.BUNDLE_EXTRA);
        mChain = (ChainBean) getIntent().getExtras().getSerializable(Constants.BUNDLE_EXTRA_2);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());

        LogUtil.LogE(mWallet.getMnemonicCode());
        getAdapter().setNewInstance(MnemonicBean.transfer(mWallet.getMnemonicCode()));
    }

    private MnemonicAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new MnemonicAdapter(this, 0);
        }
        return mAdapter;
    }

    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvBackedUp:
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, mWallet);
                bundle.putSerializable(Constants.BUNDLE_EXTRA_2, mChain);
                openActivity(VerifyMnemonicActivity.class, bundle);
                break;
        }
    }
}
