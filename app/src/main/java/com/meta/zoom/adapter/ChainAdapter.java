package com.meta.zoom.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.ChainBean;
import com.meta.zoom.R;

import org.jetbrains.annotations.NotNull;

public class ChainAdapter extends BaseQuickAdapter<ChainBean, BaseViewHolder> {

    private Context mContext;

    public ChainAdapter(Context context) {
        super(R.layout.item_chain);
        mContext = context;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, ChainBean bean) {
        int drawableId = 0;
        try {
            drawableId = mContext.getResources().getIdentifier("app_symbol_" + bean.getChainId(), "drawable", mContext.getPackageName());
        } catch (Exception e) {
        }
        helper.setText(R.id.tvSymbol, bean.getSymbol())
                .setImageResource(R.id.ivSymbol, drawableId == 0 ? R.drawable.app_unknow_symbol : drawableId)
                .setText(R.id.tvName, bean.getChainName());
    }


}
