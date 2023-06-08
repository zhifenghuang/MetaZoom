package com.meta.zoom.adapter;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.WalletBean;
import com.common.lib.manager.DataManager;
import com.meta.zoom.R;

import org.jetbrains.annotations.NotNull;

public class WalletAdapter extends BaseQuickAdapter<WalletBean, BaseViewHolder> {

    private Context mContext;
    private WalletBean mCurrent;

    public WalletAdapter(Context context) {
        super(R.layout.item_wallet);
        mContext = context;
        mCurrent = DataManager.getInstance().getCurrentWallet();
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, WalletBean bean) {
        String address = bean.getAddress();
        boolean isCurrent = bean.getAddress().equalsIgnoreCase(mCurrent.getAddress()) && bean.getChainId() == mCurrent.getChainId();
        helper.setText(R.id.tvName, bean.getWalletName())
                .setBackgroundResource(R.id.rootView,
                        isCurrent ? R.drawable.shape_7a5bd0_12 : R.drawable.shape_stroke_d2c2ff_12)
                .setTextColor(R.id.tvName,
                        ContextCompat.getColor(mContext, isCurrent ? com.common.lib.R.color.text_color_3 : com.common.lib.R.color.text_color_1))
                .setTextColor(R.id.tvAddress,
                        ContextCompat.getColor(mContext, isCurrent ? com.common.lib.R.color.text_color_3 : com.common.lib.R.color.text_color_1))
                .setImageResource(R.id.ivCopy,
                        isCurrent ? R.drawable.app_copy_address : R.drawable.app_copy_address_grey)
                .setText(R.id.tvAddress, address.substring(0, 6) + "..." + address.substring(address.length() - 6));
    }


}
