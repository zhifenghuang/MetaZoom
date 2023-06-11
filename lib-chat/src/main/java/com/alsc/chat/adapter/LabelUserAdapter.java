package com.alsc.chat.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.fragment.MyInfoFragment;
import com.alsc.chat.fragment.UserInfoFragment;
import com.alsc.chat.fragment.SelectFriendFragment;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.UserBean;
import com.common.lib.manager.DataManager;
import com.common.lib.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class LabelUserAdapter extends BaseQuickAdapter<UserBean, BaseViewHolder> {

    protected Context mContext;

    protected int mEditModel;  //0为普通，1为delete

    protected ArrayList<UserBean> mUsers;


    public LabelUserAdapter(Context context) {
        super(R.layout.item_label_user);
        mContext = context;
    }

    public void setUsers(ArrayList<UserBean> list) {
        mUsers = list;
    }

    @Override
    protected void convert(BaseViewHolder helper, UserBean item) {
        helper.setText(R.id.tvName, item.getNickName());
        int resId = mContext.getResources().getIdentifier("chat_default_avatar_" + item.getUserId() % 6,
                "drawable", mContext.getPackageName());
        Utils.loadImage(mContext, resId, item.getAvatarUrl(), helper.getView(R.id.ivAvatar));
    }

    public ArrayList<UserBean> getSelectedUsers() {
        List<UserBean> list = getData();
        ArrayList<UserBean> users = new ArrayList<>();
        for (UserBean user : list) {
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }
}

