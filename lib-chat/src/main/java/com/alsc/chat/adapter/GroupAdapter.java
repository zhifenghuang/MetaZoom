package com.alsc.chat.adapter;

import android.content.Context;

import com.alsc.chat.R;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.GroupBean;


public class GroupAdapter extends BaseQuickAdapter<GroupBean, BaseViewHolder> {

    private Context mContext;

    public GroupAdapter(Context context) {
        super(R.layout.item_group);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, GroupBean item) {
        helper.setText(R.id.tvName, item.getName())
                .setGone(R.id.line, getItemPosition(item) == getItemCount() - 1);
        Utils.displayAvatar(mContext, R.drawable.chat_default_group_avatar, item.getIcon(), helper.getView(R.id.ivAvatar));
    }
}
