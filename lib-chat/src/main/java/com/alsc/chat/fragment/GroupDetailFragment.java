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
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.interfaces.OnClickCallback;
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
        setTopStatusBarStyle(R.id.topView);
        mSettings = DataManager.getInstance().getChatSubSettings();
        setViewsOnClickListener(R.id.ivEdit,
                R.id.tvGroupManager,
                R.id.llGroupMsgSwitch,
                R.id.tvRemoveMember,
                R.id.tvClearChatHistory,
                R.id.tvFindChatHistory,
                R.id.llDisband, R.id.tvAddMember);
        mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);

        if (DataManager.getInstance().getObject() != null) {
            mGroupUsers = (ArrayList<UserBean>) DataManager.getInstance().getObject();
        }
        DataManager.getInstance().setObject(null);
        mChatSubBean = mSettings.get("group_" + mGroup.getGroupId());
        if (mChatSubBean == null) {
            mChatSubBean = new ChatSubBean();
        }
        setImage(R.id.ivGroupMsgSwitch, mGroup.getIgnore() == 1 ? R.drawable.chat_switch_on : R.drawable.chat_switch_off);

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
        if (mGroup.getGroupRole() == 3) {
            setViewGone(R.id.tvRemoveMember);
            setText(R.id.tvDisband, R.string.chat_delete_and_quit);
        }
        setText(R.id.tvGroupName, mGroup.getName());
        Utils.loadImage(getActivity(), R.drawable.chat_default_group_avatar, mGroup.getIcon(), fv(R.id.ivGroupCover));
    }


    @Override
    public void updateUIText() {
        if (mGroup.getGroupRole() == 3) {
            setViewVisible(R.id.tvGroupManager);
        } else {
            setViewGone(R.id.tvGroupManager);
        }
        Object object = DataManager.getInstance().getObject();
        if (object instanceof HashMap) {
            HashMap<String, ArrayList<UserBean>> map = (HashMap<String, ArrayList<UserBean>>) object;
            if (map.containsKey("add_member")) {
                mGroupUsers.addAll(map.get("add_member"));
            } else if (map.containsKey("delete_member")) {
                ArrayList<UserBean> list = map.get("delete_member");
                for (UserBean bean : list) {
                    for (UserBean user : mGroupUsers) {
                        if (bean.getUserId() == user.getUserId()) {
                            mGroupUsers.remove(user);
                            break;
                        }
                    }
                }
            }
            DataManager.getInstance().saveGroupUsers(mGroup.getGroupId(), mGroupUsers);
        }
        DataManager.getInstance().setObject(null);
        setText(R.id.tvGroupMemberNum, getString(R.string.chat_xxx_members, String.valueOf(mGroupUsers.size())));
        showGroupDetail();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivEdit) {
            showUpdateGroupNameDialog();
        } else if (id == R.id.tvGroupManager) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mGroup);
            gotoPager(AddGroupWayFragment.class, bundle);
        } else if (id == R.id.llGroupMsgSwitch) {
            int msgSwitch = mGroup.getIgnore() == 1 ? 0 : 1;
            operatorGroupIgnore(msgSwitch);
        } else if (id == R.id.tvAddMember) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, SelectFriendFragment.FROM_GROUP_DETAIL_ADD_MEMBER);
            bundle.putSerializable(Constants.BUNDLE_EXTRA_3, mGroup);
            DataManager.getInstance().setObject(mGroupUsers.clone());
            gotoPager(SelectFriendFragment.class, bundle);
        } else if (id == R.id.tvRemoveMember) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, SelectFriendFragment.DELETE_GROUP_USER);
            bundle.putSerializable(Constants.BUNDLE_EXTRA_3, mGroup);
            DataManager.getInstance().setObject(mGroupUsers.clone());
            gotoPager(SelectFriendFragment.class, bundle);
        } else if (id == R.id.llDisband) {
            if (mGroup.getGroupRole() == 3) {
                disbandGroup();
            } else {
                exitGroup();
            }
        } else if (id == R.id.tvClearChatHistory) {
            showTwoBtnDialog(getString(R.string.chat_are_you_sure_delete_group),
                    getString(R.string.chat_cancel),
                    getString(R.string.chat_ok), new OnClickCallback() {
                        @Override
                        public void onClick(int viewId) {
                            UserBean myInfo = DataManager.getInstance().getUser();
                            DatabaseOperate.getInstance().deleteGroupChatRecord(myInfo.getUserId(), mGroup.getGroupId());
                            HashMap<String, String> map = new HashMap<>();
                            map.put(Constants.CLEAR_MESSAGE, "");
                            EventBus.getDefault().post(map);
                        }
                    });
        } else if (id == R.id.tvFindChatHistory) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.BUNDLE_EXTRA, SearchFragment.SEARCH_GROUP_CHAT_RECORD);
            bundle.putSerializable(Constants.BUNDLE_EXTRA_2, mGroup);
            gotoPager(SearchFragment.class, bundle);
        }
    }

    private void disbandGroup() {
        showTwoBtnDialog(getString(R.string.chat_chat_are_you_sure_end_group),
                getString(R.string.chat_cancel),
                getString(R.string.chat_ok), new OnClickCallback() {
                    @Override
                    public void onClick(int viewId) {
                        ChatHttpMethods.getInstance().dismissGroup(String.valueOf(mGroup.getGroupId()),
                                new HttpObserver(new SubscriberOnNextListener() {
                                    @Override
                                    public void onNext(Object o, String msg) {
                                        removeGroup();
                                        UserBean myInfo = DataManager.getInstance().getUser();
                                        DatabaseOperate.getInstance().deleteGroupChatRecord(myInfo.getUserId(), mGroup.getGroupId());
                                        HashMap<String, Object> map = new HashMap<>();
                                        map.put(Constants.END_GROUP, mGroup.getGroupId());
                                        EventBus.getDefault().post(map);
                                        finish();
                                    }
                                }, getActivity(), (ChatBaseActivity) getActivity()));
                    }
                });

    }

    private void quitGroup() {
        showTwoBtnDialog(getString(R.string.chat_are_you_sure_delete_group),
                getString(R.string.chat_cancel),
                getString(R.string.chat_ok), new OnClickCallback() {
                    @Override
                    public void onClick(int viewId) {
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
                });
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
                setText(R.id.tvGroupMemberNum, getString(R.string.chat_xxx_members, String.valueOf(mGroupUsers.size())));
            }
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

    private void showUpdateGroupNameDialog() {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.dialog_update_group_name);
        dialogFragment.setClickDismiss(false);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                dialogFragment.setDialogViewsOnClickListener(view, R.id.tvOk);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.tvOk) {
                    final String name = getTextById(dialogFragment.getView().findViewById(R.id.etGroupName));
                    if (TextUtils.isEmpty(name)) {
                        showToast(R.string.chat_please_enter);
                        return;
                    }
                    ChatHttpMethods.getInstance().updateGroupName(String.valueOf(mGroup.getGroupId()), name,
                            new HttpObserver(new SubscriberOnNextListener<GroupBean>() {
                                @Override
                                public void onNext(GroupBean bean, String msg) {
                                    sendUpdateGroupMsg(mGroup, MessageType.TYPE_UPDATE_GROUP_NAME.ordinal(), name);
                                    if (getView() == null) {
                                        return;
                                    }
                                    mGroup.setName(name);
                                    EventBus.getDefault().post(mGroup);
                                    setText(R.id.tvGroupName, name);
                                    dialogFragment.dismiss();
                                }
                            }, getActivity(), (ChatBaseActivity) getActivity()));
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    private void exitGroup() {
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
                setText(R.id.tvGroupMemberNum, getString(R.string.chat_xxx_members, String.valueOf(mGroupUsers.size())));
                DataManager.getInstance().saveGroupUsers(mGroup.getGroupId(), list);
            }
        }, getActivity(), false, (ChatBaseActivity) getActivity()));
    }

    private void operatorGroupIgnore(final int ignore) {
        ChatHttpMethods.getInstance().updateGroupIgnore(String.valueOf(mGroup.getGroupId()), ignore, new HttpObserver(new SubscriberOnNextListener() {
            @Override
            public void onNext(Object o, String msg) {
                if (getView() == null) {
                    return;
                }
                mGroup.setIgnore(ignore);
                setImage(R.id.ivGroupMsgSwitch, mGroup.getIgnore() == 1 ? R.drawable.chat_switch_on : R.drawable.chat_switch_off);
                EventBus.getDefault().post(mGroup);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }
}
