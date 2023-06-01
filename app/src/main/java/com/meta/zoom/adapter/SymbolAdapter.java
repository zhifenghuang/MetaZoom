package com.meta.zoom.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.TokenBean;
import com.common.lib.utils.LogUtil;
import com.meta.zoom.R;

import org.jetbrains.annotations.NotNull;

public class SymbolAdapter extends BaseQuickAdapter<TokenBean, BaseViewHolder> {

    private Context mContext;

    public SymbolAdapter(Context context) {
        super(R.layout.item_symbol);
        mContext = context;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, TokenBean bean) {
        int drawableId = 0;
        if (TextUtils.isEmpty(bean.getContractAddress())) {
            try {
                drawableId = mContext.getResources().getIdentifier("app_symbol_" + bean.getChainId(), "drawable", mContext.getPackageName());
            } catch (Exception e) {
            }
        }
        helper.setText(R.id.tvSymbol, bean.getSymbol())
                .setImageResource(R.id.ivSymbol, drawableId == 0 ? R.drawable.app_unknow_symbol : drawableId)
                .setText(R.id.tvBalance, TextUtils.isEmpty(bean.getBalance()) ? "0" : bean.getBalance());
    }


}
