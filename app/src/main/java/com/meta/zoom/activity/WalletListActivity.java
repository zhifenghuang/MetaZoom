package com.meta.zoom.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.ChainBean;
import com.common.lib.constant.Constants;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.contract.EmptyContract;
import com.common.lib.mvp.presenter.EmptyPresenter;
import com.common.lib.utils.BaseUtils;
import com.meta.zoom.R;
import com.meta.zoom.adapter.ChooseChainAdapter;
import com.meta.zoom.adapter.WalletAdapter;

public class WalletListActivity extends BaseActivity<EmptyContract.Presenter> implements EmptyContract.View {

    private ChainBean mCurrentChain;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_wallet_list;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setText(R.id.tvTitle, R.string.app_manage_wallet);
        RecyclerView recyclerView = findViewById(R.id.recyclerView1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        final ChooseChainAdapter adapter1 = new ChooseChainAdapter(context);
        adapter1.onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter1);
        adapter1.setNewInstance(DatabaseOperate.getInstance().getChainList());
        mCurrentChain = DataManager.getInstance().getCurrentChain();
        adapter1.reset(mCurrentChain);

        recyclerView = findViewById(R.id.recyclerView2);
        layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        WalletAdapter adapter2 = new WalletAdapter(context);
        adapter2.onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter2);
        adapter2.setNewInstance(DatabaseOperate.getInstance().getWalletList(mCurrentChain.getChainId()));

        ((TextView) findViewById(R.id.tvChainName)).setText(mCurrentChain.getChainName());

        adapter1.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                mCurrentChain = adapter1.getItem(position);
                ((TextView) findViewById(R.id.tvChainName)).setText(mCurrentChain.getChainName());
                adapter1.setSelect(position);
                adapter2.setNewInstance(DatabaseOperate.getInstance().getWalletList(mCurrentChain.getChainId()));
                adapter2.notifyDataSetChanged();
            }
        });
        adapter2.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, adapter2.getItem(position));
                openActivity(ManageWalletActivity.class, bundle);
                finish();
            }
        });
        adapter2.addChildClickViewIds(R.id.llAddress);
        adapter2.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                switch (view.getId()) {
                    case R.id.llAddress:
                        BaseUtils.StaticParams.copyData(WalletListActivity.this, adapter2.getItem(position).getAddress());
                        showToast(com.alsc.chat.R.string.chat_copy_successful);
                        break;
                }
            }
        });
    }

    @NonNull
    @Override
    protected EmptyContract.Presenter onCreatePresenter() {
        return new EmptyPresenter(this);
    }

    @Override
    public void onClick(View v) {

    }
}
