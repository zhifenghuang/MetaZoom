package com.alsc.chat.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.manager.UPYFileUploadManger;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupDetailFragment extends ChatBaseFragment {

    private GroupBean mGroup;
    private ArrayList<UserBean> mGroupUsers;

    private HashMap<String, ChatSubBean> mSettings;
    private ChatSubBean mChatSubBean;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_group_detail;
    }

    @Override
    protected void onViewCreated(View view) {
        setText(R.id.tvTitle, getString(R.string.chat_detail));
        mSettings = DataManager.getInstance().getChatSubSettings();
        setTopStatusBarStyle(view);
        setViewsOnClickListener(R.id.llGroupInfo, R.id.llGroupQrCode,
                R.id.tvGroupManager, R.id.tvGroupNotice,
                R.id.llGroupMsgSwitch, R.id.llTopChat,
                R.id.llInGroupNick, R.id.llShowNickInGroup,
                R.id.tvSearchMsg, R.id.tvClearMsg,
                R.id.tvDeleteQuit, R.id.tvAddMember);
        mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);

        if (DataManager.getInstance().getObject() != null) {
            mGroupUsers = (ArrayList<UserBean>) DataManager.getInstance().getObject();
        }
        DataManager.getInstance().setObject(null);
        mChatSubBean = mSettings.get("group_" + mGroup.getGroupId());
        if (mChatSubBean == null) {
            mChatSubBean = new ChatSubBean();
        }
        setImage(R.id.ivGroupMsgSwitch, mGroup.getIgnore() == 1 ? R.drawable.icon_switch_on : R.drawable.icon_switch_off);
        setImage(R.id.ivTopChatSwitch, mGroup.getTop() == 1 ? R.drawable.icon_switch_on : R.drawable.icon_switch_off);
        setImage(R.id.ivShowNickInGroup, mChatSubBean.getIsShowMemberNick() == 1 ? R.drawable.icon_switch_on : R.drawable.icon_switch_off);

        if (mGroupUsers == null) {
            mGroupUsers = new ArrayList<>();
        }
        getGroupUsers();
        UserBean myInfo = DataManager.getInstance().getUser();
        String myNickInGroup = myInfo.getNickName();
        for (UserBean user : mGroupUsers) {
            if (user.getUserId() == myInfo.getUserId()) {
                myNickInGroup = user.getNickName();
                break;
            }
        }
        mGroup.setMyNickInGroup(myNickInGroup);
    }

    private void showGroupDetail() {
        boolean isShowAddButton = false;
        if (mGroup.getJoinStint() == 0) {
            if (mGroup.getJoinType() == 0 && mGroup.getGroupRole() == 3) {  //只有群主能拉人
                isShowAddButton = true;
            } else if (mGroup.getJoinType() == 1) {
                isShowAddButton = true;
            }
        }
        fv(R.id.tvAddMember).setVisibility(isShowAddButton ? View.VISIBLE : View.GONE);
        setText(R.id.tvGroupName, mGroup.getName());
        setText(R.id.tvInGroupNick, mGroup.getMyNickInGroup());
        Utils.displayAvatar(getActivity(), R.drawable.chat_default_group_avatar, mGroup.getIcon(), fv(R.id.ivGroupCover));
    }


    @Override
    public void updateUIText() {
        if (mGroup.getGroupRole() == 3) {
            setViewVisible(R.id.tvGroupManager, R.id.lineGroupManager);
        } else {
            setViewGone(R.id.tvGroupManager, R.id.lineGroupManager);
        }
        Object object = DataManager.getInstance().getObject();
        if (object instanceof HashMap) {
            HashMap<String, ArrayList<UserBean>> map = (HashMap<String, ArrayList<UserBean>>) object;
            if (map.containsKey("add_member")) {
                mGroupUsers.addAll(map.get("add_member"));
            }
            DataManager.getInstance().saveGroupUsers(mGroup.getGroupId(), mGroupUsers);
        }
        DataManager.getInstance().setObject(null);
        setText(R.id.tvGroupMemberNum, getString(R.string.chat_member_num_xxx, String.valueOf(mGroupUsers.size())));
        showGroupDetail();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llGroupInfo) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mGroup);
            // bundle.putSerializable(Constants.BUNDLE_EXTRA_2, mGroupUsers);
            DataManager.getInstance().setObject(mGroupUsers.clone());
            gotoPager(GroupInfoFragment.class, bundle);
        } else if (id == R.id.llGroupQrCode) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, QrcodeFragment.GROUP_QRCODE);
            mGroup.setMemberNum(mGroupUsers.size());
            bundle.putSerializable(Constants.BUNDLE_EXTRA_2, mGroup);
            gotoPager(QrcodeFragment.class, bundle);
        } else if (id == R.id.tvGroupNotice) {
            if (mGroup.getGroupRole() != 3) {
                ((BaseActivity) getActivity()).showOneBtnDialog(
                        getString(R.string.chat_only_ower_can_update_notice),
                        getString(R.string.chat_ok), null);
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mGroup);
            bundle.putInt(Constants.BUNDLE_EXTRA_2, UpdateGroupInfoFragment.UPDATE_GROUP_NOTICE);
            gotoPager(UpdateGroupInfoFragment.class, bundle);
        } else if (id == R.id.tvGroupManager) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mGroup);
            //bundle.putSerializable(Constants.BUNDLE_EXTRA_2, mGroupUsers);
            DataManager.getInstance().setObject(mGroupUsers.clone());
            gotoPager(GroupManagerFragment.class, bundle);
        } else if (id == R.id.llGroupMsgSwitch) {
            int msgSwitch = mGroup.getIgnore() == 1 ? 0 : 1;
            operatorGroupIgnore(msgSwitch);
        } else if (id == R.id.llTopChat) {
            int topSwitch = mGroup.getTop() == 1 ? 0 : 1;
            operatorGroupTop(topSwitch);
        } else if (id == R.id.llInGroupNick) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mGroup);
            bundle.putInt(Constants.BUNDLE_EXTRA_2, UpdateGroupInfoFragment.UPDATE_IN_GROUP_NICK);
            gotoPager(UpdateGroupInfoFragment.class, bundle);
        } else if (id == R.id.llShowNickInGroup) {
            int isShowMemberNick = mChatSubBean.getIsShowMemberNick() == 1 ? 0 : 1;
            mChatSubBean.setIsShowMemberNick(isShowMemberNick);
            setImage(R.id.ivShowNickInGroup, mChatSubBean.getIsShowMemberNick() == 1 ? R.drawable.icon_switch_on : R.drawable.icon_switch_off);
            mSettings.put("group_" + mGroup.getGroupId(), mChatSubBean);
            DataManager.getInstance().saveChatSubSettings(mSettings);
        } else if (id == R.id.tvSearchMsg) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.BUNDLE_EXTRA, SearchFragment.SEARCH_GROUP_CHAT_RECORD);
            bundle.putSerializable(Constants.BUNDLE_EXTRA_2, mGroup);
            gotoPager(SearchFragment.class, bundle);
        } else if (id == R.id.tvClearMsg) {
            showDeleteDialog(0);
        } else if (id == R.id.tvDeleteQuit) {
            showDeleteDialog(1);
        } else if (id == R.id.tvAddMember) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, SelectFriendFragment.FROM_GROUP_DETAIL);
            // bundle.putSerializable(Constants.BUNDLE_EXTRA_2, mGroupUsers);
            bundle.putSerializable(Constants.BUNDLE_EXTRA_3, mGroup);
            DataManager.getInstance().setObject(mGroupUsers.clone());
            gotoPager(SelectFriendFragment.class, bundle);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(GroupBean bean) {
        if (getView() != null) {
            mGroup = bean;
            saveNewGroup(mGroup);
            setText(R.id.tvGroupName, mGroup.getName());
            if (TextUtils.isEmpty(mGroup.getMyNickInGroup())) {
                return;
            }
            UserBean myInfo = DataManager.getInstance().getUser();
            for (UserBean user : mGroupUsers) {
                if (user.getUserId() == myInfo.getUserId()) {
                    user.setMemo(mGroup.getMyNickInGroup());
                    break;
                }
            }
            DataManager.getInstance().saveGroupUsers(mGroup.getGroupId(), mGroupUsers);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveInfo(HashMap<String, Object> map) {
        if (getView() != null && map != null) {
            if (map.containsKey(Constants.EDIT_GROUP_MEMBER)) {
                setText(R.id.tvGroupMemberNum, getString(R.string.chat_member_num_xxx, String.valueOf(mGroupUsers.size())));
            } else if (map.containsKey(Constants.END_GROUP)) {
                long groupId = (long) map.get(Constants.END_GROUP);
                if (groupId == mGroup.getGroupId()) {
                    finish();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveAvatarFile(File file) {
        if (getView() != null) {
            UPYFileUploadManger.getInstance().uploadFile(file);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveAvatarUrl(UploadAvatarEvent avatar) {
        if (getView() != null && avatar.isSuccess()) {
            mGroup.setIcon(avatar.getUrl());
            saveNewGroup(mGroup);
            ChatHttpMethods.getInstance().updateGroupIcon(String.valueOf(mGroup.getGroupId()), avatar.getUrl(), new HttpObserver(new SubscriberOnNextListener() {
                @Override
                public void onNext(Object o, String msg) {

                }
            }, getActivity(), (ChatBaseActivity) getActivity()));
        }
    }

    private void saveNewGroup(GroupBean bean) {
        ArrayList<GroupBean> list = DataManager.getInstance().getGroups();
        if (list == null || list.isEmpty()) {
            return;
        }
        int index = 0;
        for (GroupBean group : list) {
            if (group.getGroupId() == bean.getGroupId()) {
                list.set(index, bean);
                DataManager.getInstance().saveGroups(list);
                return;
            }
            ++index;
        }
    }

    private void showDeleteDialog(int type) {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_two_btn_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                ((TextView) view.findViewById(R.id.tv1)).setText(getString(R.string.chat_tip));
                ((TextView) view.findViewById(R.id.tv2)).setText(getString(type == 0 ?
                        R.string.chat_are_you_sure_delete_group_chat_record : R.string.chat_are_you_sure_delete_group));
                ((TextView) view.findViewById(R.id.btn1)).setText(getString(R.string.chat_cancel));
                ((TextView) view.findViewById(R.id.btn2)).setText(getString(R.string.chat_ok));
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btn1, R.id.btn2);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.btn2) {
                    if (type == 0) {
                        UserBean myInfo = DataManager.getInstance().getUser();
                        DatabaseOperate.getInstance().deleteGroupChatRecord(myInfo.getUserId(), mGroup.getGroupId());
                        HashMap<String, String> map = new HashMap<>();
                        map.put(Constants.CLEAR_MESSAGE, "");
                        EventBus.getDefault().post(map);
                    } else if (type == 1) {
                        deleteGroup();
                    }
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    private void deleteGroup() {
        ChatHttpMethods.getInstance().exitGroup(String.valueOf(mGroup.getGroupId()), new HttpObserver(new SubscriberOnNextListener() {
            @Override
            public void onNext(Object o, String msg) {
                UserBean myInfo = DataManager.getInstance().getUser();
                DatabaseOperate.getInstance().deleteGroupChatRecord(myInfo.getUserId(), mGroup.getGroupId());
                removeGroup();
                HashMap<String, Object> map = new HashMap<>();
                map.put(Constants.END_GROUP, mGroup.getGroupId());
                EventBus.getDefault().post(map);
                finish();
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }

    private void removeGroup() {
        ArrayList<GroupBean> list = DataManager.getInstance().getGroups();
        for (GroupBean bean : list) {
            if (bean.getGroupId() == mGroup.getGroupId()) {
                list.remove(bean);
                DataManager.getInstance().saveGroups(list);
                return;
            }
        }
    }


    /**
     * 获取群用户
     */
    private void getGroupUsers() {
        ChatHttpMethods.getInstance().getGroupUsers(String.valueOf(mGroup.getGroupId()), new HttpObserver(new SubscriberOnNextListener<ArrayList<UserBean>>() {
            @Override
            public void onNext(ArrayList<UserBean> list, String msg) {
                if (getView() == null || list == null) {
                    return;
                }
                mGroupUsers = list;
                setText(R.id.tvGroupMemberNum, getString(R.string.chat_member_num_xxx, String.valueOf(mGroupUsers.size())));
                DataManager.getInstance().saveGroupUsers(mGroup.getGroupId(), list);
            }
        }, getActivity(), false, (ChatBaseActivity) getActivity()));
    }

    private void operatorGroupTop(final int top) {
        ChatHttpMethods.getInstance().updateGroupTop(String.valueOf(mGroup.getGroupId()), top, new HttpObserver(new SubscriberOnNextListener() {
            @Override
            public void onNext(Object o, String msg) {
                if (getView() == null) {
                    return;
                }
                mGroup.setTop(top);
                setImage(R.id.ivTopChatSwitch, mGroup.getTop() == 1 ? R.drawable.icon_switch_on : R.drawable.icon_switch_off);
                EventBus.getDefault().post(mGroup);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }

    private void operatorGroupIgnore(final int ignore) {
        ChatHttpMethods.getInstance().updateGroupIgnore(String.valueOf(mGroup.getGroupId()), ignore, new HttpObserver(new SubscriberOnNextListener() {
            @Override
            public void onNext(Object o, String msg) {
                if (getView() == null) {
                    return;
                }
                mGroup.setIgnore(ignore);
                setImage(R.id.ivGroupMsgSwitch, mGroup.getIgnore() == 1 ? R.drawable.icon_switch_on : R.drawable.icon_switch_off);
                EventBus.getDefault().post(mGroup);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }
}
