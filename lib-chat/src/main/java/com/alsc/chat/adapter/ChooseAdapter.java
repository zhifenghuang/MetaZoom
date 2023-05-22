package com.alsc.chat.adapter;

import android.content.Context;

import com.alsc.chat.R;
import com.alsc.chat.fragment.ChooseFragment;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

public class ChooseAdapter extends BaseQuickAdapter<ChooseFragment.ChooseType, BaseViewHolder> {

    private Context mContext;

    private int mChooseType;


    public ChooseAdapter(Context context, int chooseType) {
        super(R.layout.item_choose);
        mContext = context;
        mChooseType = chooseType;
    }

    @Override
    protected void convert(BaseViewHolder helper, ChooseFragment.ChooseType item) {
        helper.setText(R.id.tvType, item.typeName)
                .setVisible(R.id.ivSwitch, item.isSelect);
        if (item.drawableId != -1) {
            helper.setGone(R.id.ivIcon, false)
                    .setGone(R.id.paddingView, true)
                    .setImageResource(R.id.ivIcon, item.drawableId);
        }
    }
}
