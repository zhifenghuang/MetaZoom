package com.meta.zoom.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.fragment.AddFriendFragment;
import com.alsc.chat.fragment.MyCollectionFragment;
import com.alsc.chat.fragment.SelectFriendFragment;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.ChainBean;
import com.common.lib.bean.TokenBean;
import com.common.lib.bean.UserBean;
import com.common.lib.bean.WalletBean;
import com.common.lib.constant.Constants;
import com.common.lib.constant.EventBusEvent;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.fragment.BaseFragment;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.LogUtil;
import com.meta.zoom.R;
import com.meta.zoom.activity.AddTokenActivity;
import com.meta.zoom.activity.ChooseNetworkActivity;
import com.meta.zoom.activity.MainActivity;
import com.meta.zoom.activity.WalletDetailActivity;
import com.meta.zoom.adapter.ChooseChainAdapter;
import com.meta.zoom.adapter.MnemonicAdapter;
import com.meta.zoom.adapter.SymbolAdapter;
import com.meta.zoom.dialog.ChooseWalletDialog;
import com.meta.zoom.wallet.WalletManager;
import com.meta.zoom.wallet.bean.MnemonicBean;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

public class WalletFragment extends BaseFragment<EmptyContract.Presenter> implements EmptyContract.View {

    private SymbolAdapter mAdapter;

    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_wallet;
    }


    @Override
    protected void initView(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setTopStatusBarStyle(view);
        setViewsOnClickListener(R.id.tvAdd, R.id.ivSwitchWallet, R.id.ivAddNetWork);
        getTokens();
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
    }

    private SymbolAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new SymbolAdapter(getActivity());
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, mAdapter.getItem(position));
                    openActivity(WalletDetailActivity.class, bundle);
                }
            });
        }
        return mAdapter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivSwitchWallet:
                openActivity(ChooseNetworkActivity.class);
                break;
            case R.id.ivAddNetWork:
                ChooseWalletDialog dialog = new ChooseWalletDialog(getActivity());
                dialog.show();
                break;
            case R.id.tvAdd:
                openActivity(AddTokenActivity.class);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap<String, Object> map) {
        if (getView() == null) {
            return;
        }
        if (map.containsKey(EventBusEvent.REFRESH_ASSETS)) {
            getTokens();
        }
    }


    public void onRefresh() {
        if (getView() == null) {
            return;
        }
        getTokens();
    }

    private void getTokens() {
        WalletBean current = DataManager.getInstance().getCurrentWallet();
        WalletManager.getInstance().getTokens(current.getAddress()).subscribe(this::onTokens, this::onError);
    }

    private void onTokens(ArrayList<TokenBean> tokens) {
        LogUtil.LogE("onTokens");
        if (getView() == null) {
            return;
        }
        getAdapter().setNewInstance(tokens);
        getAdapter().notifyDataSetChanged();
    }

    private void onError(Throwable throwable) {
        LogUtil.LogE("onError" + throwable.toString());
    }
}
