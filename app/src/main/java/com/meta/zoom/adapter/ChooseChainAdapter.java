package com.meta.zoom.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.ChainBean;
import com.meta.zoom.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChooseChainAdapter extends BaseQuickAdapter<ChainBean, BaseViewHolder> {

    private Context mContext;
    private int mSelectIndex;

    public ChooseChainAdapter(Context context) {
        super(R.layout.item_choose_chain);
        mContext = context;
    }

    public void setSelect(int index) {
        mSelectIndex = index;
        notifyDataSetChanged();
    }

    public void reset(ChainBean current) {
        List<ChainBean> list = getData();
        int pos = 0;
        for (ChainBean bean : list) {
            if (bean.getChainId() == current.getChainId()) {
                break;
            }
            ++pos;
        }
        mSelectIndex = pos;
        notifyDataSetChanged();
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, ChainBean bean) {
        int drawableId = 0;
        int pos = getItemPosition(bean);

        boolean isSelect = mSelectIndex == pos;
        try {
            drawableId = mContext.getResources().getIdentifier(
                    "app_" + bean.getChainId() + (isSelect ? "_on" : "_off"), "drawable", mContext.getPackageName());
        } catch (Exception e) {
        }
        if (drawableId == 0) {
            drawableId = (isSelect ? R.drawable.app_unknow_on : R.drawable.app_unkonw_off);
        }
        helper.setImageResource(R.id.ivSymbol, drawableId)
                .setGone(R.id.line, !isSelect);
    }


}
