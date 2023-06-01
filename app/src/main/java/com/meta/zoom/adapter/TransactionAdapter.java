package com.meta.zoom.adapter;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.manager.DataManager;
import com.common.lib.utils.BaseUtils;
import com.common.lib.utils.LogUtil;
import com.meta.zoom.R;
import com.meta.zoom.wallet.bean.TransactionBean;

import org.jetbrains.annotations.NotNull;
import org.web3j.utils.Convert;

public class TransactionAdapter extends BaseQuickAdapter<TransactionBean, BaseViewHolder> {

    private Context mContext;
    private String mMyAddress;

    public TransactionAdapter(Context context) {
        super(R.layout.item_transaction);
        mContext = context;
        mMyAddress = DataManager.getInstance().getCurrentWallet().getAddress();
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, TransactionBean bean) {
        boolean isOut = mMyAddress.equalsIgnoreCase(bean.getFrom());
        helper.setImageResource(R.id.iv, isOut ? R.drawable.app_send : R.drawable.app_receive)
                .setText(R.id.tvType, mContext.getString(isOut ? R.string.app_send : R.string.app_received))
                .setText(R.id.tvTime, BaseUtils.StaticParams.longToDate3(bean.getTimeStamp() * 1000))
                .setTextColor(R.id.tvValue, ContextCompat.getColor(mContext, isOut ? com.common.lib.R.color.text_color_1 : com.common.lib.R.color.text_color_11))
                .setText(R.id.tvValue, (isOut ? "-" : "+") + Convert.fromWei(bean.getValue(), Convert.Unit.ETHER).toPlainString());
    }
}
