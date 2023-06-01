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
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.alsc.chat.utils.Constants;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
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
    public static final int FROM_GROUP_DETAIL = 2;
    public static final int FROM_ADD_LABEL_OR_GROUP_USER = 3;
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
        mFromType = getArguments().getInt(Constants.BUNDLE_EXTRA, FROM_GROUP);
        if (mFromType == FROM_GROUP_DETAIL
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
        setTopStatusBarStyle(view);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        if (mFromType == FROM_TRANSFER_GROUP || mFromType == FROM_GROUP_CHAT_AT
                || mFromType == DELETE_GROUP_USER) {
            setText(R.id.tvTitle, mFromType == FROM_TRANSFER_GROUP ? R.string.chat_select_transfer_group_user :
                    (mFromType == FROM_GROUP_CHAT_AT ? R.string.chat_select_chat_at : R.string.chat_group_member));
            if (list != null && !list.isEmpty()) {
                UserBean myInfo = DataManager.getInstance().getUser();
                for (UserBean bean : list) {
                    if (bean.getUserId() == myInfo.getUserId()) {
                        list.remove(bean);
                        break;
                    }
                }
            }
            getAdapter().setNewData(list);
            if (mFromType == FROM_TRANSFER_GROUP || mFromType == FROM_GROUP_CHAT_AT) {
                setViewGone(R.id.llBottom);
            } else {
                int num = getAdapter().getCheckNum();
                TextView btnOk = fv(R.id.btnOk);
                btnOk.setAlpha(num == 0 ? 0.5f : 1.0f);
                btnOk.setText(getString(R.string.chat_ok_1, String.valueOf(num)));
                btnOk.setOnClickListener(this);
            }
        } else {
            if (mFromType == FROM_GROUP_ADD_BLOCK) {
                ArrayList<UserBean> forbidUsers=(ArrayList<UserBean>)getArguments().getSerializable(Constants.BUNDLE_EXTRA_2);
                setText(R.id.tvTitle, R.string.chat_add_forbid_say);
                if (list != null && !list.isEmpty()) {
                    UserBean myInfo = DataManager.getInstance().getUser();
                    for (UserBean bean : list) {
                        if (bean.getUserId() == myInfo.getUserId()) {
                            list.remove(bean);
                            break;
                        }
                    }
                }
                getAdapter().setNewData(list);
                getAdapter().resetSelectedUser(forbidUsers);
            } else if (mFromType == FROM_GROUP_REMOVE_BLOCK) {
                setText(R.id.tvTitle, R.string.chat_remove_forbid_say);
                ArrayList<UserBean> forbidUsers=(ArrayList<UserBean>)getArguments().getSerializable(Constants.BUNDLE_EXTRA_2);
                getAdapter().setNewData(forbidUsers);
            } else {
                if (mFromType == FROM_GROUP) {
                    setText(R.id.tvTitle, R.string.chat_new_group);
                } else {
                    setText(R.id.tvTitle, R.string.chat_select_friend);
                }
                getAdapter().setNewData(DataManager.getInstance().getFriends());
                getAdapter().resetSelectedUser(list);
            }
            int num = getAdapter().getCheckNum();
            TextView btnOk = fv(R.id.btnOk);
            btnOk.setAlpha(num == 0 ? 0.5f : 1.0f);
            btnOk.setText(getString(R.string.chat_ok_1, String.valueOf(num)));
            btnOk.setOnClickListener(this);
        }

        int startLetter = 'A';
        int count = 'Z' - 'A';
        char ch;
        ArrayList<String> letters = new ArrayList<>();
        for (int i = 0; i <= count; ++i) {
            ch = (char) (startLetter + i);
            letters.add(String.valueOf(ch));
        }
        letters.add("#");
        initLetters(letters);
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
                        TextView btnOk = fv(R.id.btnOk);
                        btnOk.setAlpha(num == 0 ? 0.5f : 1.0f);
                        btnOk.setText(getString(R.string.chat_ok_1, String.valueOf(num)));
                    }
                });
            }
        }
        return mAdapter;
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnOk) {
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
            } else if (mFromType == FROM_ADD_LABEL_OR_GROUP_USER) {
                DataManager.getInstance().setObject(list);
                finish();
            } else if (mFromType == FROM_GROUP_DETAIL) {
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
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_two_btn_dialog);
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

    private void initLetters(ArrayList<String> letters) {
        LinearLayout llLetters = fv(R.id.llLetters);
        int count = letters.size();
        TextView tvLetter;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int padding = (int) Utils.dp2px(getActivity(), 10);
        for (int i = 0; i < count; ++i) {
            tvLetter = new TextView(getActivity());
            tvLetter.setTag(letters.get(i));
            tvLetter.setText(letters.get(i));
            tvLetter.setPadding(padding, 0, padding, 0);
            tvLetter.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_00_00_00));
            tvLetter.setGravity(Gravity.CENTER_HORIZONTAL);
            llLetters.addView(tvLetter, lp);
            tvLetter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String letter = (String) v.getTag();
                    RecyclerView recyclerView = fv(R.id.recyclerView);
                    int pos = mAdapter.getIndexByLetter(letter);
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    linearLayoutManager.scrollToPositionWithOffset(pos, 0);
                }
            });
        }
    }
}
