package com.meta.zoom.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.lib.activity.BaseActivity;
import com.common.lib.constant.Constants;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.meta.zoom.R;
import com.meta.zoom.adapter.MnemonicAdapter;
import com.common.lib.activity.db.DatabaseOperate;
import com.meta.zoom.contract.MainContract;
import com.meta.zoom.presenter.MainPresenter;
import com.meta.zoom.wallet.bean.MnemonicBean;
import com.common.lib.bean.WalletBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VerifyMnemonicActivity extends BaseActivity<MainContract.Presenter> implements MainContract.View {

    private WalletBean mWallet;
    private MnemonicAdapter mOrderAdapter, mShuffleAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_verification_mnemonic;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setText(R.id.tvTitle, R.string.app_verification);
        setViewsOnClickListener(R.id.tvBackedUp);
        Bundle bundle = getIntent().getExtras();
        mWallet = (WalletBean) bundle.getSerializable(Constants.BUNDLE_EXTRA);
        RecyclerView recyclerView = findViewById(R.id.recyclerView1);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        getOrderAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getOrderAdapter());
        resetBtn();

        recyclerView = findViewById(R.id.recyclerView2);
        layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        getShuffleAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getShuffleAdapter());
        List<String> list = new ArrayList<>();
        list.addAll(mWallet.getMnemonicCode());
        Collections.shuffle(list);
        getShuffleAdapter().setNewInstance(MnemonicBean.transfer(list));
    }

    private MnemonicAdapter getOrderAdapter() {
        if (mOrderAdapter == null) {
            mOrderAdapter = new MnemonicAdapter(this, 1);
            mOrderAdapter.addChildClickViewIds(R.id.ivDelete);
            mOrderAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
                @Override
                public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                    switch (view.getId()) {
                        case R.id.ivDelete:
                            MnemonicBean bean = mOrderAdapter.getItem(position);
                            mOrderAdapter.removeAt(position);
                            bean.setSelected(false);
                            mShuffleAdapter.notifyDataSetChanged();
                            resetBtn();
                            setViewInvisible(R.id.tvTips);
                            break;
                    }
                }
            });
        }
        return mOrderAdapter;
    }

    private MnemonicAdapter getShuffleAdapter() {
        if (mShuffleAdapter == null) {
            mShuffleAdapter = new MnemonicAdapter(this, 2);
            mShuffleAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    MnemonicBean bean = mShuffleAdapter.getItem(position);
                    if (!bean.isSelected()) {
                        bean.setSelected(true);
                        mShuffleAdapter.notifyDataSetChanged();
                        mOrderAdapter.addData(bean);
                        resetBtn();
                    }
                }
            });
        }
        return mShuffleAdapter;
    }

    @NonNull
    @Override
    protected MainContract.Presenter onCreatePresenter() {
        return new MainPresenter(this);
    }

    private void resetBtn() {
        View tvBackedUp = findViewById(R.id.tvBackedUp);
        if (mOrderAdapter.getItemCount() == 12) {
            tvBackedUp.setAlpha(1.0f);
            tvBackedUp.setEnabled(true);
        } else {
            tvBackedUp.setAlpha(0.25f);
            tvBackedUp.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvBackedUp:
                List<MnemonicBean> orderList = getOrderAdapter().getData();
                List<String> list = mWallet.getMnemonicCode();
                int length = list.size();
                if (orderList.size() != length) {
                    return;
                }
                for (int i = 0; i < length; ++i) {
                    if (!list.get(i).equals(orderList.get(i).getText())) {
                        setViewVisible(R.id.tvTips);
                        return;
                    }
                }
                DatabaseOperate.getInstance().insert(mWallet);
                DataManager.getInstance().saveCurrentWallet(mWallet);
                getPresenter().login(mWallet.getAddress());
                break;
        }
    }

    @Override
    public void loginSuccess() {
        finishAllActivity();
        openActivity(MainActivity.class);
    }
}
