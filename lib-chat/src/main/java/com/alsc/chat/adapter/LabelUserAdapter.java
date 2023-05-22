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
        final int position = getItemPosition(item);
        ImageView ivAvatar = helper.getView(R.id.ivAvatar);
        ImageView ivDelete = helper.getView(R.id.ivDelete);
        if (position < getItemCount() - 2) {
            helper.setText(R.id.tvName, item.getNickName());
            Utils.displayAvatar(mContext, R.drawable.chat_default_avatar, item.getAvatarUrl(), ivAvatar);
            if (mEditModel == 0) {
                ivDelete.setVisibility(View.GONE);
                ivAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, item);
                        ((ChatBaseActivity) mContext).gotoPager(item.getUserId() == DataManager.getInstance().getUserId() ? MyInfoFragment.class : UserInfoFragment.class, bundle);
                    }
                });
            } else {
                ivDelete.setVisibility(View.VISIBLE);
                ivAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        remove(position);
                        mUsers.remove(position);
                    }
                });
            }
        } else if (position == getItemCount() - 2) {
            helper.setText(R.id.tvName, "");
            ivDelete.setVisibility(View.GONE);
            ivAvatar.setImageResource(R.drawable.icon_user_add);
            ivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, SelectFriendFragment.FROM_ADD_LABEL_OR_GROUP_USER);
                    ArrayList<UserBean> list = (ArrayList<UserBean>) getData();
                    int size = list.size();
                    list.remove(size - 1);
                    list.remove(size - 2);
                    //bundle.putSerializable(Constants.BUNDLE_EXTRA_2, list);
                    DataManager.getInstance().setObject(list.clone());
                    ((ChatBaseActivity) mContext).gotoPager(SelectFriendFragment.class, bundle);
                }
            });
        } else {
            helper.setText(R.id.tvName, "");
            ivDelete.setVisibility(View.GONE);
            ivAvatar.setImageResource(R.drawable.icon_user_delete);
            ivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditModel = mEditModel == 0 ? 1 : 0;
                    notifyDataSetChanged();
                }
            });
        }
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

