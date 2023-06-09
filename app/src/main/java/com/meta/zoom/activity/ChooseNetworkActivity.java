package com.meta.zoom.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.constant.Constants;
import com.common.lib.constant.EventBusEvent;
import com.common.lib.interfaces.OnClickCallback;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.meta.zoom.R;
import com.meta.zoom.adapter.ChainAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

public class ChooseNetworkActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {

    private ChainAdapter mAdapter;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_choose_network;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setText(R.id.tvTitle, R.string.app_choose_network);
        setViewsOnClickListener(R.id.tvAddNetwork);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        refreshNetwork();
    }

    private ChainAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new ChainAdapter(this);
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, final int position) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, mAdapter.getItem(position));
                    openActivity(AddNetworkActivity.class, bundle);
                }
            });
            mAdapter.addChildClickViewIds(R.id.ivDelete);
            mAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
                @Override
                public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                    showTwoBtnDialog(getString(R.string.app_are_you_sure_delete_network),
                            getString(R.string.app_cancel),
                            getString(R.string.app_confirm), new OnClickCallback() {
                                @Override
                                public void onClick(int viewId) {
                                    DatabaseOperate.getInstance().delete(mAdapter.getItem(position));
                                    mAdapter.removeAt(position);
                                }
                            });
                }
            });
        }
        return mAdapter;
    }

    private void refreshNetwork() {
        if (isFinish()) {
            return;
        }
        getAdapter().setNewInstance(DatabaseOperate.getInstance().getChainList());
    }

    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvAddNetwork:
                openActivity(AddNetworkActivity.class);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap<String, Object> map) {
        if (isFinish() || map == null) {
            return;
        }
        if (map.containsKey(EventBusEvent.REFRESH_NETWORK)) {
            refreshNetwork();
        }
    }
}
