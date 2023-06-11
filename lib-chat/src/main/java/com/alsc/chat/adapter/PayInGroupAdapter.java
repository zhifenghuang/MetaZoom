package com.alsc.chat.adapter;

import android.content.Context;

import com.alsc.chat.R;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.EnvelopeBean;

public class PayInGroupAdapter extends BaseQuickAdapter<EnvelopeBean, BaseViewHolder> {

    private Context mContext;

    public PayInGroupAdapter(Context context) {
        super(R.layout.item_pay_in_group);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, final EnvelopeBean item) {
        int resId = mContext.getResources().getIdentifier("chat_default_avatar_" + item.getUserId() % 6,
                "drawable", mContext.getPackageName());
        Utils.loadImage(mContext, resId, item.getAvatarUrl(), helper.getView(R.id.ivAvatar));
        helper.setText(R.id.tvName, item.getNickName())
                .setText(R.id.tvTime, item.getCreateTime())
                .setText(R.id.tvNum, String.format("%.8f", item.getAmount()));
    }
}
