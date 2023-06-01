package com.alsc.chat.adapter;

import android.content.Context;

import com.alsc.chat.R;
import com.alsc.chat.fragment.ContactFragment;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.UserBean;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ContactAdapter extends BaseMultiItemQuickAdapter<ContactFragment.ContactItem, BaseViewHolder> {

    private Context mContext;
    private boolean mIsHadNewVerify;

    public ContactAdapter(Context context) {
        super(new ArrayList<>());
        addItemType(0, R.layout.item_contact_0);
        addItemType(1, R.layout.item_contact_1);
        mContext = context;
    }

    public void setNew(boolean isHadNewVerify) {
        mIsHadNewVerify = isHadNewVerify;
        notifyDataSetChanged();
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, ContactFragment.ContactItem item) {
        int position=getItemPosition(item);
        switch (getDefItemViewType(position)) {
            case 0:
                helper.setText(R.id.tv, item.name)
                        .setVisible(R.id.tvNew, false);
                if (position == 0 && mIsHadNewVerify) {
                    helper.setVisible(R.id.ivNew, true);
                }
                break;
            case 1:
                UserBean friend = item.getFriend();
//                char c = friend.getPinyinName().charAt(0);
//                if (position == 4) {
//                    helper.setText(R.id.tvLetter, String.valueOf(c))
//                            .setGone(R.id.tvLetter, false);
//                } else {
//                    char prevC = getItem(position - 1).getFriend().getPinyinName().charAt(0);
//                    helper.setText(R.id.tvLetter, String.valueOf(c))
//                            .setGone(R.id.tvLetter, prevC == c);
//                }
                helper.setText(R.id.tvName, friend.getNickName());
                int resId = mContext.getResources().getIdentifier("chat_default_avatar_" + friend.getUserId() % 6,
                        "drawable", mContext.getPackageName());
                Utils.loadImage(mContext, resId, friend.getAvatarUrl(), helper.getView(R.id.ivAvatar));
                break;
        }
    }

}
