package com.alsc.chat.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.SelectFriendAdapter;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.alsc.chat.utils.Constants;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.lib.utils.LogUtil;
import com.google.gson.Gson;
import com.lzy.imagepicker.util.Utils;
import com.zhangke.websocket.WebSocketHandler;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectFriendFragment extends ChatBaseFragment {

    private SelectFriendAdapter mAdapter;

    public static final int FROM_GROUP = 0;
    public static final int FROM_LABEL = 1;
    public static final int FROM_GROUP_DETAIL_ADD_MEMBER = 2;
    public static final int FROM_TRANSFER_GROUP = 4;

    public static final int FROM_GROUP_ADD_BLOCK = 5;  //添加禁言
    public static final int FROM_GROUP_REMOVE_BLOCK = 6;  //解除禁言

    public static final int FROM_GROUP_CHAT_AT = 7;  //@群里人

    public static final int DELETE_GROUP_USER = 8;  //删除群成员

    private GroupBean mGroup;

    private int mFromType;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_select_friend;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(R.id.topView);
        mFromType = getArguments().getInt(Constants.BUNDLE_EXTRA, FROM_GROUP);
        if (mFromType == FROM_GROUP_DETAIL_ADD_MEMBER
                || mFromType == FROM_TRANSFER_GROUP
                || mFromType == FROM_GROUP_ADD_BLOCK
                || mFromType == FROM_GROUP_REMOVE_BLOCK
                || mFromType == DELETE_GROUP_USER) {
            mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA_3);
        }
        ArrayList<UserBean> list;
        if (DataManager.getInstance().getObject() != null) {
            list = (ArrayList<UserBean>) DataManager.getInstance().getObject();
        } else {
            list = new ArrayList<>();
        }
        DataManager.getInstance().setObject(null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        if (mFromType == DELETE_GROUP_USER) {
            if (list != null && !list.isEmpty()) {
                UserBean myInfo = DataManager.getInstance().getUser();
                for (UserBean bean : list) {
                    if (bean.getUserId() == myInfo.getUserId()) {
                        list.remove(bean);
                        break;
                    }
                }
            }
            for (UserBean userBean : list) {
                userBean.setCheck(false);
                userBean.setFix(false);
            }
            getAdapter().setNewData(list);
            setText(R.id.tvTitle, R.string.chat_remove_member);
            setImage(R.id.ivNext, R.drawable.chat_complete_red);
            view.findViewById(R.id.topView).setBackgroundColor(ContextCompat.getColor(getActivity(), com.common.lib.R.color.text_color_8));
            int num = getAdapter().getCheckNum();
            View ivNext = fv(R.id.ivNext);
            ivNext.setAlpha(num == 0 ? 0.25f : 1.0f);
            setViewGone(R.id.tvDesc);
            ivNext.setOnClickListener(this);
        } else {
            getAdapter().setNewData(DataManager.getInstance().getFriends());
            getAdapter().resetSelectedUser(list);
            int num = getAdapter().getCheckNum();
            if (mFromType == FROM_GROUP) {
                setText(R.id.tvTitle, R.string.chat_new_group);
                setText(R.id.tvDesc, getDesc(num));
            } else if (mFromType == FROM_GROUP_DETAIL_ADD_MEMBER) {
                setText(R.id.tvTitle, R.string.chat_add_members);
                setViewGone(R.id.tvDesc);
                setImage(R.id.ivNext, R.drawable.chat_complete);
            }
            View ivNext = fv(R.id.ivNext);
            ivNext.setAlpha(num == 0 ? 0.25f : 1.0f);
            ivNext.setOnClickListener(this);
        }
    }

    private SelectFriendAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new SelectFriendAdapter(getActivity(), mFromType);
            if (mFromType == FROM_TRANSFER_GROUP || mFromType == FROM_GROUP_CHAT_AT) {
                mAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                        if (mFromType == FROM_TRANSFER_GROUP) {
                            showTransferGroupDialog(getAdapter().getItem(position));
                        } else {
                            HashMap<String, UserBean> map = new HashMap<>();
                            map.put(Constants.AT_GROUP_MEMBER, getAdapter().getItem(position));
                            EventBus.getDefault().post(map);
                            finish();
                        }
                    }
                });
            } else {
                mAdapter.setOnItemCheckListener(new SelectFriendAdapter.OnItemCheckListener() {
                    @Override
                    public void checkNum(int num) {
                        View ivNext = fv(R.id.ivNext);
                        ivNext.setAlpha(num == 0 ? 0.25f : 1.0f);
                        setText(R.id.tvDesc, getDesc(num));
                    }
                });
            }
        }
        return mAdapter;
    }

    private String getDesc(int num) {
        if (num == 0) {
            return getString(R.string.chat_up_to_200_members);
        }
        return getString(R.string.chat_xxx_200_selected, String.valueOf(num));
    }

    @Override
    public void updateUIText() {
        if (getAdapter().getItemCount() == 0) {
            setViewGone(R.id.recyclerView);
            setViewVisible(R.id.tvNoContacts, R.id.tvNoContactsDesc);
        } else {
            setViewVisible(R.id.recyclerView);
            setViewGone(R.id.tvNoContacts, R.id.tvNoContactsDesc);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivNext) {
            ArrayList<UserBean> list = getAdapter().getSelectUsers();
            if (list.isEmpty()) {
                return;
            }
            if (mFromType == FROM_GROUP || mFromType == FROM_LABEL) {
                DataManager.getInstance().setObject(list);
                Bundle bundle = new Bundle();
                if (mFromType == FROM_LABEL) {
                    bundle.putInt(Constants.BUNDLE_EXTRA, EditLabelFragment.SAVE_LABEL);
                }
                gotoPager(mFromType == FROM_GROUP ? AddGroupFragment.class :
                        EditLabelFragment.class, bundle);
                finish();
            } else if (mFromType == FROM_GROUP_DETAIL_ADD_MEMBER) {
                inviteToGroup(list);
            } else if (mFromType == FROM_GROUP_REMOVE_BLOCK
                    || mFromType == FROM_GROUP_ADD_BLOCK) {
                resetGroupBlockUser(list);
            } else if (mFromType == DELETE_GROUP_USER) {
                deleteGroupMember(list);
            }
        }
    }

    private void showTransferGroupDialog(final UserBean userBean) {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.chat_layout_two_btn_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                ((TextView) view.findViewById(R.id.tv1)).setText(getString(R.string.chat_tip));
                ((TextView) view.findViewById(R.id.tv2)).setText(getString(R.string.chat_chat_are_you_sure_give_group_to_other,
                        userBean.getNickName()));
                ((TextView) view.findViewById(R.id.btn1)).setText(getString(R.string.chat_cancel));
                ((TextView) view.findViewById(R.id.btn2)).setText(getString(R.string.chat_ok));
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btn1, R.id.btn2);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.btn2) {
                    ChatHttpMethods.getInstance().updateGroupOwner(String.valueOf(mGroup.getGroupId()), String.valueOf(userBean.getUserId()),
                            new HttpObserver(new SubscriberOnNextListener<GroupBean>() {
                                @Override
                                public void onNext(GroupBean bean, String msg) {
                                    if (getView() == null) {
                                        return;
                                    }
                                    mGroup.setGroupRole(1);
                                    EventBus.getDefault().post(mGroup);
                                    finish();
                                }
                            }, getActivity(), (ChatBaseActivity) getActivity()));
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    private void deleteGroupMember(final ArrayList<UserBean> users) {
        ArrayList<Long> list = new ArrayList<>();
        for (UserBean user : users) {
            list.add(user.getUserId());
        }
        ChatHttpMethods.getInstance().kickOutUser(mGroup.getGroupId(), list, new HttpObserver(new SubscriberOnNextListener() {
            @Override
            public void onNext(Object o, String msg) {
                if (getView() == null) {
                    return;
                }
                HashMap<String, ArrayList<UserBean>> map = new HashMap<>();
                map.put("delete_member", users);
                DataManager.getInstance().setObject(map);
                finish();
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }

    private void inviteToGroup(final ArrayList<UserBean> users) {
        ArrayList<Long> list = new ArrayList<>();
        for (UserBean user : users) {
            list.add(user.getUserId());
        }
        ChatHttpMethods.getInstance().inviteToGroup(mGroup.getGroupId(), list, new HttpObserver(new SubscriberOnNextListener() {
            @Override
            public void onNext(Object o, String msg) {
                if (getView() == null) {
                    return;
                }
                if (mGroup.getPayinState() == 1) {
                    showToast(R.string.chat_group_pay_in_invite_had_sent);
                } else {
                    sendInviteToGroupMsg(mGroup, DataManager.getInstance().getUser(), users);
                    HashMap<String, ArrayList<UserBean>> map = new HashMap<>();
                    map.put("add_member", users);
                    DataManager.getInstance().setObject(map);
                }
                finish();
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }


    private void resetGroupBlockUser(final ArrayList<UserBean> users) {
        ArrayList<Long> list = new ArrayList<>();
        for (UserBean user : users) {
            list.add(user.getUserId());
        }
        ChatHttpMethods.getInstance().addOrRemoveGroupBlock(mGroup.getGroupId(), list, mFromType == FROM_GROUP_ADD_BLOCK ? 1 : 0,
                new HttpObserver(new SubscriberOnNextListener() {
                    @Override
                    public void onNext(Object o, String msg) {
                        GroupMessageBean groupMsg = new GroupMessageBean();
                        groupMsg.setCmd(2100);
                        groupMsg.setMsgType(mFromType == FROM_GROUP_ADD_BLOCK ? MessageType.TYPE_FORBID_CHAT.ordinal() : MessageType.TYPE_REOMVE_FORBID_CHAT.ordinal());
                        groupMsg.setFromId(DataManager.getInstance().getUserId());
                        groupMsg.setGroupId(mGroup.getGroupId());

                        ArrayList<HashMap<String, Object>> userList = new ArrayList<>();
                        for (UserBean bean : users) {
                            if (bean != null) {
                                userList.add(bean.toMap());
                            }
                        }
                        if (userList.isEmpty()) {
                            return;
                        }
                        groupMsg.setExtra(new Gson().toJson(userList));
                        WebSocketHandler.getDefault().send(groupMsg.toJson());

                        groupMsg.setSendStatus(1);
                        DatabaseOperate.getInstance().insert(groupMsg);
                        EventBus.getDefault().post(groupMsg);
                        if (getView() == null) {
                            return;
                        }
                        finish();
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
    }
}
