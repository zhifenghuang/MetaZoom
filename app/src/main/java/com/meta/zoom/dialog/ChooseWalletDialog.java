package com.meta.zoom.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.manager.ChatManager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.ChainBean;
import com.common.lib.bean.WalletBean;
import com.common.lib.constant.Constants;
import com.common.lib.constant.EventBusEvent;
import com.common.lib.manager.DataManager;
import com.common.lib.utils.BaseUtils;
import com.common.lib.utils.LogUtil;
import com.meta.zoom.R;
import com.meta.zoom.activity.StartWalletActivity;
import com.meta.zoom.adapter.ChooseChainAdapter;
import com.meta.zoom.adapter.WalletAdapter;
import com.meta.zoom.wallet.WalletManager;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

public class ChooseWalletDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private ChainBean mCurrentChain;


    public ChooseWalletDialog(Context context) {
        this(context, com.alsc.chat.R.style.LoadingDialog);
    }


    @SuppressLint("MissingInflatedId")
    public ChooseWalletDialog(Context context, int themeId) {
        super(context, themeId);
        this.mContext = context;
        setContentView(R.layout.layout_switch_address_dialog);

        Window view = getWindow();
        WindowManager.LayoutParams lp = view.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        view.setGravity(Gravity.BOTTOM);

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
        findViewById(R.id.ivClose).setOnClickListener(this);
        findViewById(R.id.tvManageWallet).setOnClickListener(this);
        findViewById(R.id.tvAddWallet).setOnClickListener(this);

        adapter1.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                mCurrentChain = adapter1.getItem(position);
                adapter1.setSelect(position);
                adapter2.setNewInstance(DatabaseOperate.getInstance().getWalletList(mCurrentChain.getChainId()));
                adapter2.notifyDataSetChanged();
            }
        });
        adapter2.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                WalletBean bean = adapter2.getItem(position);
                WalletBean current = DataManager.getInstance().getCurrentWallet();
                if (current.getAddress().equalsIgnoreCase(bean.getAddress())
                        && current.getChainId() == bean.getChainId()) {
                    dismiss();
                    return;
                }
                ChainBean chainBean = DataManager.getInstance().getCurrentChain();
                if (chainBean.getChainId() != bean.getChainId()) {
                    DataManager.getInstance().saveCurrentChain(mCurrentChain);
                    WalletManager.getInstance().resetNetwork(mCurrentChain);
                }
                DataManager.getInstance().saveCurrentWallet(bean);
                HashMap<String, Object> map = new HashMap<>();
                map.put(EventBusEvent.REFRESH_ACCOUNT, "");
                EventBus.getDefault().post(map);
                dismiss();
            }
        });
        adapter2.addChildClickViewIds(R.id.llAddress);
        adapter2.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                switch (view.getId()) {
                    case R.id.llAddress:
                        BaseUtils.StaticParams.copyData(mContext, adapter2.getItem(position).getAddress());
                        ((BaseActivity) mContext).showToast(com.alsc.chat.R.string.chat_copy_successful);
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvManageWallet:
            case R.id.tvAddWallet:
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, DatabaseOperate.getInstance().getChainList().get(0));
                ((BaseActivity) mContext).openActivity(StartWalletActivity.class, bundle);
                dismiss();
                break;
            case R.id.ivClose:
                dismiss();
                break;
        }
    }

}

