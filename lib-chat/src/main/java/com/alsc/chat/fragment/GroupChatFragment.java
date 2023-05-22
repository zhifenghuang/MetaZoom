package com.alsc.chat.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.adapter.GroupMessageAdapter;
import com.alsc.chat.adapter.MessageAdapter;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.utils.Utils;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.utils.Constants;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildLongClickListener;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.manager.DataManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GroupChatFragment extends ChatFragment {

    private GroupBean mGroup;
    private ArrayList<UserBean> mGroupUsers;
    private ArrayList<FilterMsgBean> mFilterMsgs;

    @Override
    protected void onViewCreated(View view) {
        mMyInfo = DataManager.getInstance().getUser();
        mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setText(R.id.tvLeft, mGroup.getName());
        setText(R.id.tvReadDelete, R.string.chat_send_delete);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setStackFromEnd(true);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        init(view);
        getGroupDetail();
        getGroupUsers();
        setViewGone(R.id.llTransfer);
        setViewInvisible(R.id.tabEmptyView);
        getFilterMsg();
        getAdapter().setOnItemChildLongClickListener(new OnItemChildLongClickListener() {
            @Override
            public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.ivLeft) {
                    UserBean user = (UserBean) view.getTag(R.id.chat_id);
                    if (user == null) {
                        return false;
                    }
                    if (mAtGroupMembers == null) {
                        mAtGroupMembers = new ArrayList<>();
                    }
                    mAtGroupMembers.add(user);
                    final EditText etChat = fv(R.id.etChat);
                    String text = etChat.getText().toString();
                    text += ("@" + user.getNickName() + " ");
                    etChat.setText(text);
                    etChat.setSelection(Math.min(2000, text.length()));
                    etChat.setFocusable(true);
                    etChat.setFocusableInTouchMode(true);
                    etChat.requestFocus();
                    showKeyBoard(etChat);
                } else if (!getAdapter().isEditMode()) {
                    showMsgMoreDialog(getAdapter().getItem(position), view);
                }
                return false;
            }
        });
    }

    protected long getChatId() {
        return mGroup.getGroupId();
    }

    @Override
    public void updateUIText() {
        super.updateUIText();
        mGroupUsers = DataManager.getInstance().getGroupUsers(mGroup.getGroupId());
        showForbidView();
        ((GroupMessageAdapter) getAdapter()).setGroupUsers(mGroupUsers);
        ((GroupMessageAdapter) getAdapter()).setGroup(mGroup);
        HashMap<String, ChatSubBean> settings = DataManager.getInstance().getChatSubSettings();
        ChatSubBean chatSubBean = settings.get("group_" + mGroup.getGroupId());
        if (chatSubBean == null) {
            chatSubBean = new ChatSubBean();
        }
        ((GroupMessageAdapter) getAdapter()).setShowNick(chatSubBean.getIsShowMemberNick() == 1);
        mFilterMsgs = DataManager.getInstance().getGroupFilterMsg(mGroup.getGroupId());
    }

    protected void atGroupMember(String text) {
        if (!TextUtils.isEmpty(text)) {
            if (mAtGroupMembers != null && !mAtGroupMembers.isEmpty()) {
                int size = mAtGroupMembers.size();
                String endText = "@" + mAtGroupMembers.get(size - 1).getNickName();
                if (text.endsWith(endText)) {
                    text = text.substring(0, text.length() - endText.length());
                    EditText etChat = fv(R.id.etChat);
                    etChat.setText(text);
                    etChat.setSelection(Math.min(2000, text.length()));
                    mAtGroupMembers.remove(size - 1);
                    return;
                }
            }
            if (text.charAt(text.length() - 1) == '@') {
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.BUNDLE_EXTRA, SelectFriendFragment.FROM_GROUP_CHAT_AT);
                //       bundle.putSerializable(Constants.BUNDLE_EXTRA_2, mGroupUsers);
                DataManager.getInstance().setObject(mGroupUsers.clone());
                gotoPager(SelectFriendFragment.class, bundle);
            }
        }
    }

    @Override
    public void initMsgs() {
        DatabaseOperate.getInstance().setAllGroupMsgRead(mMyInfo.getUserId(), mGroup.getGroupId());

        ArrayList<GroupMessageBean> list;
        if (mSelectedMsg != null) {
            list = DatabaseOperate.getInstance().getAllGroupMsg(mMyInfo.getUserId(), mGroup.getGroupId());
        } else {
            list = DatabaseOperate.getInstance().getGroupMsg(mMyInfo.getUserId(), mGroup.getGroupId(), 0l, Constants.PAGE_NUM);
        }
        Collections.reverse(list);
        ArrayList<BasicMessage> messages = new ArrayList<>();
        if (mSelectedMsg == null) {
            int size = list.size();
            mHasMore = (size == Constants.PAGE_NUM);
            if (size > 0) {
                mLastMsgTime = list.get(0).getCreateTime();
            }
            messages.addAll(list);
            getAdapter().setNewData(messages);
        } else {
            mHasMore = false;
            messages.addAll(list);
            getAdapter().setNewData(messages);
            int position = 0;
            for (BasicMessage msg : messages) {
                if (msg.getMessageId().equals(mSelectedMsg.getMessageId())) {
                    RecyclerView recyclerView = fv(R.id.recyclerView);
                    recyclerView.getLayoutManager().scrollToPosition(position);
                    return;
                }
                ++position;
            }
        }

        final RecyclerView recyclerView = fv(R.id.recyclerView);
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                final LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        hideKeyBoard(fv(R.id.etChat));
                    }

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (mHasMore && manager.findFirstVisibleItemPosition() == 0) {
                            getMoreMessage();
                        }
                    }
                });
            }
        }, 200);
    }

    @Override
    public void getMoreMessage() {
        if (mHasMore) {
            ArrayList<GroupMessageBean> list = DatabaseOperate.getInstance().getGroupMsg(mMyInfo.getUserId(), mGroup.getGroupId(), mLastMsgTime, Constants.PAGE_NUM);
//            int size = list.size();
            getAdapter().addData(0, list);
            mHasMore = false;//(size == Constants.PAGE_NUM);
//            if (size > 0) {
//                mLastMsgTime = list.get(size - 1).getCreateTime();
//                getAdapter().addData(0, list);
//            }
        }

    }

    @Override
    protected MessageAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new GroupMessageAdapter(getActivity(), mMyInfo);
        }
        return mAdapter;
    }

    protected boolean isFilterLetter(String text) {
        if (mFilterMsgs != null && !mFilterMsgs.isEmpty()) {
            for (FilterMsgBean bean : mFilterMsgs) {
                if (!TextUtils.isEmpty(bean.getContent()) && text.contains(bean.getContent())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isFilterUrl(String text) {
        if (mGroup.getDisableLink() == 1 && Utils.isContainUrl(text)) {
            return true;
        }
        return false;
    }

    protected boolean isCanAddFriend() {  //群禁止互加好友后不能点击头像
        if (mGroup.getDisableFriend() == 1) {
            showToast(R.string.chat_group_forbid_add_friend);
            return true;
        }
        return false;
    }

    protected boolean isForbidChat() {  //是否被禁言
        if (mGroup.getGroupRole() == 3) { //群主不需要被禁言
            return false;
        }
        if (mGroup.getAllBlock() == 1) {  //全群被禁言
            return true;
        }
        if (mGroupUsers != null && !mGroupUsers.isEmpty()) {
            for (UserBean bean : mGroupUsers) {
                if (bean.getUserId() == mMyInfo.getUserId()) {
                    if (bean.getBlock() == 1) {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    @Override
    protected void goSendRedPackage() {
        Bundle bundle = new Bundle();
        mGroup.setMemberNum(mGroupUsers == null ? 0 : mGroupUsers.size());
        bundle.putSerializable(Constants.BUNDLE_EXTRA, mGroup);
        gotoPager(SendGroupRedPackageFragment.class, bundle);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMsg(GroupMessageBean message) {
        if (getView() != null && message != null && message.getGroupId() == mGroup.getGroupId()) {
            if (message.getFromId() != mMyInfo.getUserId()) {
                message.readMsg();
            }
            int msgType = message.getMsgType();
            if (msgType == MessageType.TYPE_UPDATE_GROUP_NAME.ordinal()) {
                mGroup.setName(message.getContent());
                setText(R.id.tvLeft, mGroup.getName());
            } else if (msgType == MessageType.TYPE_ALL_FORBID_CHAT.ordinal()) {
                mGroup.setAllBlock(1);
                showForbidView();
                addGroupInList(mGroup);
            } else if (msgType == MessageType.TYPE_ALL_REMOVE_FORBID_CHAT.ordinal()) {
                mGroup.setAllBlock(0);
                showForbidView();
                addGroupInList(mGroup);
            } else if (msgType == MessageType.TYPE_FORBID_CHAT.ordinal()
                    || msgType == MessageType.TYPE_REOMVE_FORBID_CHAT.ordinal()) {
                getGroupUsers();
            }
            getAdapter().addData(message);
            scrollBottom();
        }
    }

    protected BasicMessage getMsg() {
        GroupMessageBean msg = new GroupMessageBean();
        msg.setCmd(2100);
        msg.setFromId(mMyInfo.getUserId());
        msg.setGroupId(mGroup.getGroupId());
        msg.setExtra(getGson().toJson(getUsers()));
        return msg;
    }

    protected void goDetailClass() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.BUNDLE_EXTRA, mGroup);
        DataManager.getInstance().setObject(mGroupUsers.clone());
        gotoPager(GroupDetailFragment.class, bundle);
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
                showForbidView();
                ((GroupMessageAdapter) getAdapter()).setGroupUsers(list);
                DataManager.getInstance().saveGroupUsers(mGroup.getGroupId(), list);
            }
        }, getActivity(), false, (ChatBaseActivity) getActivity()));
    }

    private void showForbidView() {
        if (isForbidChat()) {
            setViewVisible(R.id.tvInForbid);
            setViewGone(R.id.tvRecord, R.id.llChat);
        } else {
            setViewGone(R.id.tvInForbid, R.id.tvRecord);
            setViewVisible(R.id.llChat);
        }
    }

    private void getGroupDetail() {
        ChatHttpMethods.getInstance().getGroupInfo(mGroup.getGroupId(), new HttpObserver(new SubscriberOnNextListener<GroupBean>() {
            @Override
            public void onNext(GroupBean bean, String msg) {
                if (getView() == null) {
                    return;
                }
                addGroupInList(bean);
                mGroup = bean;
                showForbidView();
                ((GroupMessageAdapter) getAdapter()).setGroup(mGroup);
                setText(R.id.tvLeft, mGroup.getName());
                EventBus.getDefault().post(mGroup);
            }
        }, getActivity(), false, (ChatBaseActivity) getActivity()));
    }

    private void getFilterMsg() {
        ChatHttpMethods.getInstance().groupBlockList(String.valueOf(mGroup.getGroupId()),
                new HttpObserver(new SubscriberOnNextListener<ArrayList<FilterMsgBean>>() {
                    @Override
                    public void onNext(ArrayList<FilterMsgBean> list, String msg) {
                        if (getView() == null) {
                            return;
                        }
                        mFilterMsgs = list;
                        DataManager.getInstance().setGroupFilterMsg(mGroup.getGroupId(), list);
                    }
                }, getActivity(), false, (ChatBaseActivity) getActivity()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(GroupBean bean) {
        if (getView() != null) {
            mGroup = bean;
            showForbidView();
            ((GroupMessageAdapter) getAdapter()).setGroup(mGroup);
            setText(R.id.tvLeft, mGroup.getName());
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivSend
                || id == R.id.llAlbum
                || id == R.id.llCamera
                || id == R.id.llVideo
                || id == R.id.llLocation
                || id == R.id.llRedPackage
                || id == R.id.llTransfer
                || id == R.id.llFile
                || id == R.id.ivAdd
                || id == R.id.ivVoice) {
            if (!isForbidChat()) {
                super.onClick(v);
            }
        } else {
            super.onClick(v);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (getView() != null && map != null) {
            if (map.containsKey(Constants.END_GROUP)) {
                long groupId = (long) map.get(Constants.END_GROUP);
                if (mChatUser == null && groupId == mGroup.getGroupId()) {//在群聊中
                    getAdapter().clearMsg();
                    finish();
                }
            } else if (map.containsKey(Constants.REFRESH_GROUP_INFO)) {
                if (mGroup.getGroupId() == (long) map.get(Constants.REFRESH_GROUP_INFO)) {
                    getGroupDetail();
                }
            } else if (map.containsKey(Constants.REFRESH_FORBID_LETTER)) {
                if (mGroup.getGroupId() == (long) map.get(Constants.REFRESH_FORBID_LETTER)) {
                    getFilterMsg();
                }
            } else if (map.containsKey(Constants.REFRESH_GROUP_USERS)) {
                if (mGroup.getGroupId() == (long) map.get(Constants.REFRESH_GROUP_USERS)) {
                    getGroupUsers();
                }
            } else if (map.containsKey(Constants.AT_GROUP_MEMBER)) {
                if (mAtGroupMembers == null) {
                    mAtGroupMembers = new ArrayList<>();
                }
                UserBean user = (UserBean) map.get(Constants.AT_GROUP_MEMBER);
                mAtGroupMembers.add(user);
                EditText etChat = fv(R.id.etChat);
                String text = etChat.getText().toString();
                text += (user.getNickName() + " ");
                etChat.setText(text);
                etChat.setSelection(Math.min(2000, text.length()));
                etChat.setFocusable(true);
                etChat.setFocusableInTouchMode(true);
                etChat.requestFocus();
                etChat.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showKeyBoard(etChat);
                    }
                }, 100);
            } else {
                super.onReceive(map);
            }
        }
    }

}
