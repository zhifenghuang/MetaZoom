package com.meta.zoom.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.ChainBean;
import com.common.lib.bean.WalletBean;
import com.common.lib.constant.EventBusEvent;
import com.common.lib.manager.DataManager;
import com.meta.zoom.R;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

public class AddNetworkDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private ChainBean mCurrentChain;
    private OnAddClickListener mOnAddClickListener;


    public AddNetworkDialog(Context context, ChainBean chainBean, OnAddClickListener listener) {
        this(context, com.alsc.chat.R.style.LoadingDialog, chainBean);
        mOnAddClickListener = listener;
    }


    @SuppressLint("MissingInflatedId")
    public AddNetworkDialog(Context context, int themeId, ChainBean chainBean) {
        super(context, themeId);
        this.mContext = context;
        mCurrentChain = chainBean;
        setContentView(R.layout.layout_add_network);

        Window view = getWindow();
        WindowManager.LayoutParams lp = view.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        view.setGravity(Gravity.BOTTOM);

        findViewById(R.id.ivClose).setOnClickListener(this);
        findViewById(R.id.tvOk).setOnClickListener(this);
        ((TextView) findViewById(R.id.tvNetworkName)).setText(chainBean.getChainName());
        ((TextView) findViewById(R.id.tvRPC)).setText(chainBean.getRpcUrl());
        ((TextView) findViewById(R.id.tvChainID)).setText(String.valueOf(chainBean.getChainId()));
        ((TextView) findViewById(R.id.tvDefaultToken)).setText(chainBean.getSymbol());
        ((TextView) findViewById(R.id.tvBrowser)).setText(chainBean.getExplore());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvOk:
                if (mOnAddClickListener != null) {
                    mOnAddClickListener.onClick();
                }
                dismiss();
                break;
            case R.id.ivClose:
                dismiss();
                break;
        }
    }

    public interface OnAddClickListener {
        public void onClick();
    }

}

