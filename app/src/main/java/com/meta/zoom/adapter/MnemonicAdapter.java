package com.meta.zoom.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.meta.zoom.R;
import com.meta.zoom.wallet.bean.MnemonicBean;

import org.jetbrains.annotations.NotNull;

public class MnemonicAdapter extends BaseQuickAdapter<MnemonicBean, BaseViewHolder> {

    private Context mContext;
    private int mType;

    public MnemonicAdapter(Context context, int type) {
        super(type == 0 ? R.layout.item_mnemonic :
                (type == 1 ? R.layout.item_order_mnemonic : R.layout.item_shuffle_mnemonic));
        mContext = context;
        mType = type;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, MnemonicBean bean) {
        helper.setText(R.id.tvMnemonic, bean.getText());
        if (mType == 2) {
            helper.getView(R.id.tvMnemonic).setAlpha(bean.isSelected() ? 0.25f : 1f);
        }
    }


}
