package com.meta.zoom.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.lib.fragment.BaseFragment;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.meta.zoom.R;
import com.meta.zoom.activity.DappWebActivity;

public class DappWebFragment extends BaseFragment<EmptyContract.Presenter> implements EmptyContract.View {
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_dapp_web;
    }


    @Override
    protected void initView(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setViewsOnClickListener(R.id.ivScan);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivScan:
                openActivity(DappWebActivity.class);
                break;
        }
    }
}
