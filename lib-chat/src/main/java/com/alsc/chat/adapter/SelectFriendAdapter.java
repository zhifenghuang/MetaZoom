package com.alsc.chat.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.alsc.chat.R;
import com.alsc.chat.fragment.SelectFriendFragment;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.UserBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SelectFriendAdapter extends BaseQuickAdapter<UserBean, BaseViewHolder> {

    private Context mContext;

    private OnItemCheckListener mListener;

    private int mChectNum;

    private int mFromType;


    public SelectFriendAdapter(Context context, int type) {
        super(R.layout.item_select_friend);
        mContext = context;
        mChectNum = 0;
        mFromType = type;
    }

    @Override
    protected void convert(BaseViewHolder helper, UserBean item) {

        int pos = getItemPosition(item);
        if (pos == 0 || getItem(pos - 1).getPinyinName().charAt(0) != item.getPinyinName().charAt(0)) {
            helper.setGone(R.id.tvLetter, false);
            helper.setText(R.id.tvLetter, String.valueOf(item.getPinyinName().charAt(0)).toUpperCase());
        } else {
            helper.setGone(R.id.tvLetter, true);
        }
        ImageView checkFriend = helper.getView(R.id.ivCheckFriend);
        if (mFromType == SelectFriendFragment.FROM_TRANSFER_GROUP
                || mFromType == SelectFriendFragment.FROM_GROUP_CHAT_AT) {
            checkFriend.setVisibility(View.GONE);
        } else {
            checkFriend.setVisibility(View.VISIBLE);
            checkFriend.setImageResource(item.isCheck() ? R.drawable.icon_box_selected : R.drawable.icon_box_unselected);
            View ll = helper.getView(R.id.ll);
            if (item.isFix()) {
                checkFriend.setAlpha(0.5f);
                ll.setOnClickListener(null);
            } else {
                checkFriend.setAlpha(1.0f);
                ll.setTag(item);
                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserBean item = (UserBean) v.getTag();
                        item.setCheck(!item.isCheck());
                        if (item.isCheck()) {
                            ++mChectNum;
                            ((ImageView) v.findViewById(R.id.ivCheckFriend)).setImageResource(R.drawable.icon_box_selected);
                        } else {
                            --mChectNum;
                            ((ImageView) v.findViewById(R.id.ivCheckFriend)).setImageResource(R.drawable.icon_box_unselected);
                        }
                        if (mListener != null) {
                            mListener.checkNum(mChectNum);
                        }
                    }
                });
            }
        }
        helper.setText(R.id.tvName, item.getNickName());
        Utils.displayAvatar(mContext, R.drawable.chat_default_avatar, item.getAvatarUrl(), helper.getView(R.id.ivAvatar));
    }

    @Override
    public void setNewData(List<UserBean> list) {
        resort(list);
        super.setNewData(list);
    }

    public int getCheckNum() {
        return mChectNum;
    }

    public ArrayList<UserBean> getSelectUsers() {
        ArrayList<UserBean> list = new ArrayList<>();
        for (UserBean bean : getData()) {
            if (bean.isCheck() && !bean.isFix()) {
                list.add(bean);
            }
        }
        return list;
    }

    public void setOnItemCheckListener(OnItemCheckListener listener) {
        mListener = listener;
    }

    public void resetSelectedUser(ArrayList<UserBean> list) {
        if (list == null) {
            return;
        }
        ArrayList<UserBean> datas = (ArrayList<UserBean>) getData();
        for (UserBean userBean : datas) {
            userBean.setCheck(false);
            userBean.setFix(false);
        }
        for (UserBean userBean : list) {
            for (UserBean bean : datas) {
                if (userBean.getContactId() == bean.getUserId()) {
                    bean.setCheck(true);
                    bean.setFix(true);
                    break;
                }
            }
        }
        list.clear();
        list = null;
        notifyDataSetChanged();
    }

    public interface OnItemCheckListener {
        public void checkNum(int num);
    }

    public int getIndexByLetter(String letter) {
        List<UserBean> list = getData();
        int index = 0;
        for (UserBean bean : list) {
            if (bean.getPinyinName().startsWith(letter)) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    private void resort(List<UserBean> list) {
        Collections.sort(list, new Comparator<UserBean>() {
            @Override
            public int compare(UserBean o1, UserBean o2) {
                String name1 = o1.getPinyinName();
                String name2 = o2.getPinyinName();
                if (name1.startsWith("#") && name2.startsWith("#")) {
                    return name1.compareTo(name2);
                }
                if (name1.startsWith("#")) {
                    return 1;
                }
                if (name2.startsWith("#")) {
                    return -1;
                }
                return name1.compareTo(name2);
            }
        });
    }
}
