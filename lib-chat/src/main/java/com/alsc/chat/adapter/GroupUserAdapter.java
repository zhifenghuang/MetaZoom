package com.alsc.chat.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.fragment.GroupInfoFragment;
import com.alsc.chat.fragment.MyInfoFragment;
import com.alsc.chat.fragment.UpdateGroupInfoFragment;
import com.alsc.chat.fragment.UserInfoFragment;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.*;
import com.common.lib.manager.DataManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class GroupUserAdapter extends BaseMultiItemQuickAdapter<GroupInfoFragment.GroupUserItem, BaseViewHolder> {

    private Context mContext;
    private GroupInfoFragment mGroupInfoFragment;
    private GroupBean mGroup;//groupRole我在群角色 1群员 2管理员 3群主
    private long mMyId;

    public GroupUserAdapter(Context context, GroupInfoFragment fragment) {
        super(new ArrayList<GroupInfoFragment.GroupUserItem>());
        mContext = context;
        mGroupInfoFragment = fragment;
        addItemType(0, R.layout.item_group_info_1);
        addItemType(1, R.layout.item_group_info_2);
    }

    public void setGroupRole(GroupBean group, long myId) {
        mGroup = group;
        mMyId = myId;
        notifyDataSetChanged();
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, @Nullable GroupInfoFragment.GroupUserItem groupUserItem) {
        switch (getDefItemViewType(getItemPosition(groupUserItem))) {
            case 0:
                helper.setText(R.id.tvGroupName, mGroup.getName())
                        .setText(R.id.tvGroupMemberNum, mContext.getString(R.string.chat_member_num_xxx, String.valueOf(getItemCount() - 1)))
                        .setText(R.id.tvGroupName2, mGroup.getName())
                        .setText(R.id.tvGroupMemberNum2, mContext.getString(R.string.chat_member_num_xxx_2, String.valueOf(getItemCount() - 1)));
                Utils.displayAvatar(mContext, R.drawable.chat_default_group_avatar, mGroup.getIcon(), helper.getView(R.id.ivGroupCover));
                helper.getView(R.id.llGroupName).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, mGroup);
                        bundle.putInt(Constants.BUNDLE_EXTRA_2, UpdateGroupInfoFragment.UPDATE_GROUP_NAME);
                        ((ChatBaseActivity) mContext).gotoPager(UpdateGroupInfoFragment.class, bundle);
                    }
                });
                helper.getView(R.id.tvGroupCover).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mGroupInfoFragment.showSelectPhotoDialog();
                    }
                });
                break;
            case 1:
                final UserBean userBean = groupUserItem.getUserBean();
                String role = "";
                if (userBean.getGroupRole() == 3) {
                    role = mContext.getString(R.string.chat_group_ower);
                } else if (userBean.getUserId() == mMyId) {
                    role = mContext.getString(R.string.chat_me);
                }
                helper.setText(R.id.tvName, userBean.getNickName())
                        .setText(R.id.tvRole, role);
                Utils.displayAvatar(mContext, 0, userBean.getAvatarUrl(), helper.getView(R.id.ivAvatar));

                helper.getView(R.id.llGroupUser).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mGroup.getDisableFriend() == 1) {
                            ((ChatBaseActivity) mContext).showToast(R.string.chat_group_forbid_add_friend);
                            return;
                        }
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, userBean);
                        mGroupInfoFragment.gotoPager(userBean.getUserId() == DataManager.getInstance().getUserId() ? MyInfoFragment.class : UserInfoFragment.class, bundle);
                    }
                });
                break;
        }
    }


//    @Override
//    protected void convert(BaseViewHolder helper, final UserBean item) {
//        final int position = helper.getAdapterPosition();
//        ImageView ivAvatar = helper.getView(R.id.ivAvatar);
//        ImageView ivDelete = helper.getView(R.id.ivDelete);
//        int index = mGroup.getGroupRole() == 3 ? getItemCount() - 2 : getItemCount() - 1;  //群组可以踢人
//        if (position < index || (!mIsShowAddBtn && position == index)) {
//            helper.setText(R.id.tvName, item.getNickName());
//            Utils.displayAvatar(mContext, R.drawable.chat_default_group_avatar, item.getAvatarUrl(), ivAvatar);
//            if (mEditModel == 0 || item.getUserId() == mMyId) {
//                ivDelete.setVisibility(View.GONE);
//                ivAvatar.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (mGroup.getDisableFriend() == 1) {
//                            ((BaseActivity) mContext).showToast(R.string.chat_group_forbid_add_friend);
//                            return;
//                        }
//                        Bundle bundle = new Bundle();
//                        bundle.putSerializable(Constants.BUNDLE_EXTRA, item);
//                        ((BaseActivity) mContext).gotoPager(item.getUserId() == DataManager.getInstance().getUserId() ? MyInfoFragment.class : UserInfoFragment.class, bundle);
//                    }
//                });
//            } else {
//                ivDelete.setVisibility(View.VISIBLE);
//                ivAvatar.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        kickOutUser(item.getUserId(), position);
//                    }
//                });
//            }
//        } else if (position == index) {
//            helper.setText(R.id.tvName, "");
//            ivDelete.setVisibility(View.GONE);
//            ivAvatar.setImageResource(R.drawable.chat_user_add);
//            ivAvatar.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (mEditModel == 1) {
//                        mEditModel = 0;
//                        notifyDataSetChanged();
//                        return;
//                    }
//                    EmptyActivity activity = (EmptyActivity) mContext;
//                    Bundle bundle = new Bundle();
//                    bundle.putSerializable(Constants.BUNDLE_EXTRA, SelectFriendFragment.FROM_GROUP_DETAIL);
//                    ArrayList<UserBean> list = (ArrayList<UserBean>) getData();
//                    int size = list.size();
//                    list.remove(size - 1);
//                    if (mGroup.getGroupRole() == 3) {
//                        list.remove(size - 2);
//                    }
//                    bundle.putSerializable(Constants.BUNDLE_EXTRA_2, list);
//                    bundle.putSerializable(Constants.BUNDLE_EXTRA_3, mGroup);
//                    activity.gotoPager(SelectFriendFragment.class, bundle);
//                }
//            });
//        } else {
//            helper.setText(R.id.tvName, "");
//            ivDelete.setVisibility(View.GONE);
//            ivAvatar.setImageResource(R.drawable.chat_user_delete);
//            ivAvatar.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mEditModel = mEditModel == 0 ? 1 : 0;
//                    notifyDataSetChanged();
//                }
//            });
//        }
//    }
//
//
//    private void kickOutUser(final long userId, int position) {
//        ChatHttpMethods.getInstance().kickOutUser(mGroup.getGroupId(),
//                userId, new HttpObserver(new SubscriberOnNextListener() {
//                    @Override
//                    public void onNext(Object o, String msg) {
//                        remove(position);
//                        UserBean userBean = mUsers.remove(position);
//                        DataManager.getInstance().saveGroupUsers(mGroup.getGroupId(), mUsers);
//                        HashMap<String, Integer> map = new HashMap<>();
//                        map.put(Constants.EDIT_GROUP_MEMBER, mUsers.size());
//                        EventBus.getDefault().post(map);
//
//                        MessageBean messageBean = MessageBean.getSystemMsg(DataManager.getInstance().getUserId(), userId,
//                                Constants.END_GROUP, mGroup.getGroupId());
//                        WebSocketHandler.getDefault().send(messageBean.toJson());
//
//                        sendRemoveGroupMsg(mGroup, userBean);
//                    }
//                }, mContext, (BaseActivity) mContext));
//    }
//
//    private void sendRemoveGroupMsg(GroupBean group, UserBean userBean) {
//        GroupMessageBean groupMessageBean = new GroupMessageBean();
//        groupMessageBean.setCmd(2100);
//        groupMessageBean.setMsgType(MessageType.TYPE_REMOVE_FROM_GROUP.ordinal());
//        UserBean myInfo = DataManager.getInstance().getUser();
//        groupMessageBean.setGroupId(group.getGroupId());
//        groupMessageBean.setContent(new Gson().toJson(myInfo.toMap()));
//        groupMessageBean.setFromId(DataManager.getInstance().getUserId());
//        ArrayList<HashMap<String, Object>> userList = new ArrayList<>();
//        userList.add(userBean.toMap());
//        groupMessageBean.setExtra(new Gson().toJson(userList));
//        if (TextUtils.isEmpty(groupMessageBean.getExtra())) {
//            return;
//        }
//        WebSocketHandler.getDefault().send(groupMessageBean.toJson());
//        groupMessageBean.setSendStatus(1);
//        DatabaseOperate.getInstance().insert(groupMessageBean);
//        EventBus.getDefault().post(groupMessageBean);
//    }

}
