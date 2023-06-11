package com.alsc.chat.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.adapter.ChatUserAdapter;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.common.lib.utils.LogUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatListFragment extends ChatBaseFragment {

    private ChatUserAdapter mAdapter;

    private ArrayList<ChatBean> mChatList;
    private HashMap<String, ChatSubBean> mSettings;
    private Gson mGson;

    private Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_list;
    }

    @Override
    protected void onViewCreated(View view) {
        SwipeMenuRecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.color_f7_f7_f7));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setSwipeMenuCreator(swipeMenuCreator);
        recyclerView.setSwipeMenuItemClickListener(swipeMenuItemClickListener);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());

        mChatList = new ArrayList<>();
        mSettings = DataManager.getInstance().getChatSubSettings();
        initChatList();
    }

    private ChatUserAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new ChatUserAdapter(getActivity(), DataManager.getInstance().getUser());
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    Bundle bundle = new Bundle();
                    ChatBean bean = getAdapter().getItem(position);
                    if (bean.chatUser != null) {
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, bean.chatUser);
                        gotoPager(ChatFragment.class, bundle);
                    } else {
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, bean.group);
                        gotoPager(GroupChatFragment.class, bundle);
                    }
                }
            });
            mAdapter.setNewInstance(mChatList);
        }
        return mAdapter;
    }


    private void initChatList() {
        UserBean myInfo = DataManager.getInstance().getUser();
        if (myInfo == null) {
            return;
        }
        DatabaseOperate.getInstance().deleteAllExprieMsg();
        mChatList.clear();
        if (mFriendList == null) {
            mFriendList = new ArrayList<>();
        }

        ArrayList<MessageBean> list = DatabaseOperate.getInstance().getUserChatList(myInfo.getUserId());
        boolean isAddChatGPT = false;
        if (!list.isEmpty()) {
            ChatBean chatBean;
            for (MessageBean bean : list) {
                if (bean.getFromId() == 0 || TextUtils.isEmpty(bean.getTag()) || bean.getTag().startsWith("0_") || bean.getTag().equals("_0")) {
                    continue;
                }
                if (bean.getToId() == -1
                        || bean.getFromId() == -1) {
                    chatBean = new ChatBean();
                    chatBean.chatUser = UserBean.createChatGPTUser();
                    chatBean.lastMsg = bean;
                    chatBean.chatSubBean = mSettings.get("user_-1");
                    chatBean.unReadNum = 0;//bean.getUnReadNum();
                    mChatList.add(chatBean);
                    isAddChatGPT = true;
                } else {
                    for (UserBean friend : mFriendList) {
                        if (friend.getBlock() == 1) {
                            continue;
                        }
                        if (friend.getContactId() == bean.getToId()
                                || friend.getContactId() == bean.getFromId()) {
                            chatBean = new ChatBean();
                            chatBean.chatUser = friend;
                            chatBean.lastMsg = bean;
                            chatBean.chatSubBean = mSettings.get("user_" + friend.getContactId());
                            chatBean.unReadNum = 0;//bean.getUnReadNum();//DatabaseOperate.getInstance().getUnReadNum(myInfo.getUserId(), friend.getContactId());
                            mChatList.add(chatBean);
                            break;
                        }
                    }
                }
            }
        }
        if (!isAddChatGPT) {
            ChatBean chatBean = new ChatBean();
            chatBean.chatUser = UserBean.createChatGPTUser();
            chatBean.chatSubBean = mSettings.get("user_-1");
            chatBean.unReadNum = 0;//bean.getUnReadNum();
            mChatList.add(chatBean);
        }
        initGroupChatList(myInfo.getUserId());
        getAdapter().setNewInstance(mChatList);
        getAdapter().resortList();
        getAdapter().notifyDataSetChanged();
        resetTotalUnReadNum();
    }

    private void initGroupChatList(long myId) {
        if (mGroupList == null) {
            return;
        }
        ArrayList<GroupMessageBean> list = DatabaseOperate.getInstance().getChatGroupList(myId);
        if (list != null && !list.isEmpty()) {
            for (GroupMessageBean bean : list) {
                for (GroupBean group : mGroupList) {
                    if (group.getGroupId() == bean.getGroupId()) {
                        ChatBean chatBean = new ChatBean();
                        chatBean.group = group;
                        chatBean.lastMsg = bean;
                        chatBean.chatSubBean = mSettings.get("group_" + group.getGroupId());
                        chatBean.unReadNum = 0;//bean.getUnReadNum();//DatabaseOperate.getInstance().getGroupUnReadNum(myId, group.getGroupId());
                        mChatList.add(chatBean);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void updateUIText() {
        mSettings = DataManager.getInstance().getChatSubSettings();
    }

    @Override
    public void onClick(View v) {
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMsg(MessageBean message) {
        if (getView() != null && message != null) {
            for (ChatBean chatBean : mChatList) {
                if (chatBean.chatUser == null) {
                    continue;
                }
                if (chatBean.chatUser.getContactId() == message.getFromId()
                        || chatBean.chatUser.getContactId() == message.getToId()) {
                    chatBean.lastMsg = message;
                    //  chatBean.unReadNum += 1;
                    mChatList.remove(chatBean);
                    getAdapter().receiveNewMsg(chatBean);
                    getAdapter().notifyDataSetChanged();
                    resetTotalUnReadNum();
                    return;
                }
            }
            ChatBean chatBean;
            if (mFriendList != null) {
                for (UserBean bean : mFriendList) {
                    if (bean.getContactId() == message.getFromId()
                            || bean.getContactId() == message.getToId()) {
                        chatBean = new ChatBean();
                        chatBean.chatUser = bean;
                        chatBean.lastMsg = message;
                        //    chatBean.unReadNum += 1;
                        chatBean.chatSubBean = mSettings.get("user_" + bean.getContactId());
                        getAdapter().receiveNewMsg(chatBean);
                        getAdapter().notifyDataSetChanged();
                        resetTotalUnReadNum();
                        return;
                    }
                }
            } else {
                getFriendFromServer();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveGroupMsg(GroupMessageBean message) {
        if (getView() != null && message != null) {
            for (ChatBean chatBean : mChatList) {
                if (chatBean.group == null) {
                    continue;
                }
                if (chatBean.group.getGroupId() == message.getGroupId()) {
                    if (message.getMsgType() == MessageType.TYPE_UPDATE_GROUP_NAME.ordinal()) {
                        chatBean.group.setName(message.getContent());
                        updateGroup(chatBean.group);
                    } else if (message.getMsgType() == MessageType.TYPE_REMOVE_FROM_GROUP.ordinal()) {
                        ArrayList<UserBean> list = getGson().fromJson(message.getExtra(), new TypeToken<ArrayList<UserBean>>() {
                        }.getType());
                        for (UserBean bean : list) {
                            if (bean.getUserId() == DataManager.getInstance().getUserId()) {
                                getAdapter().removeGroupChat(chatBean.group.getGroupId());
                                removeFromGroupInList(chatBean.group);
                                resetTotalUnReadNum();
                                return;
                            }
                        }
                    }
                    chatBean.lastMsg = message;
                    //   chatBean.unReadNum += 1;
                    mChatList.remove(chatBean);
                    getAdapter().receiveNewMsg(chatBean);
                    getAdapter().notifyDataSetChanged();
                    resetTotalUnReadNum();
                    return;
                }
            }
            if (mGroupList != null) {
                for (GroupBean bean : mGroupList) {
                    if (bean.getGroupId() == message.getGroupId()) {
                        if (message.getMsgType() == MessageType.TYPE_REMOVE_FROM_GROUP.ordinal()) {
                            ArrayList<UserBean> list = getGson().fromJson(message.getExtra(), new TypeToken<ArrayList<UserBean>>() {
                            }.getType());
                            for (UserBean userBean : list) {
                                if (userBean.getUserId() == DataManager.getInstance().getUserId()) {
                                    removeFromGroupInList(bean);
                                    return;
                                }
                            }
                        }
                        ChatBean chatBean = new ChatBean();
                        chatBean.group = bean;
                        chatBean.lastMsg = message;
                        //       chatBean.unReadNum += 1;
                        chatBean.chatSubBean = mSettings.get("group_" + bean.getGroupId());
                        getAdapter().receiveNewMsg(chatBean);
                        getAdapter().notifyDataSetChanged();
                        resetTotalUnReadNum();
                        return;
                    }
                }
            }
            getGroupFromServer();
        }
    }

    private void resetTotalUnReadNum() {
        int totalNum = 0;
        for (ChatBean bean : mChatList) {
            if (!bean.isNotInterupt()) {
                totalNum += bean.unReadNum;
            }
        }
        HashMap<String, Integer> map = new HashMap<>();
        map.put(Constants.NEW_MSG_NUM_CHANGE, totalNum);
        EventBus.getDefault().post(map);
    }

    public void setData(ArrayList<UserBean> friendList, ArrayList<GroupBean> groupList) {
        mFriendList = friendList;
        mGroupList = groupList;
        LogUtil.LogE(mFriendList + ", " + mGroupList);
        if (getView() != null) {
            initChatList();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveAvatarUrl(CaptureEvent event) {
        if (getView() != null && !TextUtils.isEmpty(event.getUrl())) {
            final String text = event.getUrl();
            getView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (text.contains("tuijianma=")) {
                        String strs = text.split("tuijianma=")[1];
                        String id = strs.split("&")[0];
                        searchContact(id);
                    } else if (text.startsWith("chat_group_")) {
                        String id = text.substring(11);
                        addToGroup(Long.parseLong(id));
                    }
                }
            }, 200);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(GroupBean group) {
        if (getView() == null || group == null) {
            return;
        }
        for (ChatBean bean : mChatList) {
            if (bean.group != null && group.getGroupId() == bean.group.getGroupId()) {
                bean.group.setTop(group.getTop());
                bean.group.setIgnore(group.getIgnore());
                mAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveInfo(HashMap<String, Object> map) {
        if (getView() != null && map != null) {
            if (map.containsKey(Constants.SEND_MSG_FAILED)) {
                String msgId = (String) map.get(Constants.SEND_MSG_FAILED);
                for (ChatBean bean : mChatList) {
                    if (bean.lastMsg != null && bean.lastMsg.getMessageId().equals(msgId)) {
                        bean.lastMsg.setSendStatus(-1);
                        getAdapter().notifyDataSetChanged();
                        break;
                    }
                }
            } else if (map.containsKey(Constants.END_GROUP)) {
                long groupId = (long) map.get(Constants.END_GROUP);
                getAdapter().removeGroupChat(groupId);
                resetTotalUnReadNum();
            } else if (map.containsKey(Constants.SEND_MSG_SUCCESS)) {
                String msgId = (String) map.get(Constants.SEND_MSG_SUCCESS);
                for (ChatBean bean : mChatList) {
                    if (bean.lastMsg != null && bean.lastMsg.getMessageId().equals(msgId)) {
                        bean.lastMsg.setSendStatus(1);
                        mAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            } else if (map.containsKey(Constants.EDIT_FRIEND)) {
                UserBean userBean = (UserBean) map.get(Constants.EDIT_FRIEND);
                for (ChatBean bean : mChatList) {
                    if (bean.chatUser != null && userBean.getContactId() == bean.chatUser.getContactId()) {
                        bean.chatUser.setTop(userBean.getTop());
                        bean.chatUser.setIgnore(userBean.getIgnore());
                        mAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            }
        }
    }

    private void addToGroup(long groupId) {
        ChatHttpMethods.getInstance().groupQrcode(groupId, new HttpObserver(new SubscriberOnNextListener<GroupBean>() {
            @Override
            public void onNext(GroupBean bean, String msg) {
                if (getView() == null || bean == null) {
                    return;
                }
                if (bean.getPayinState() == 1) {
                    showPayInGroupDialog(bean, null);
                } else {
                    ArrayList<UserBean> list = new ArrayList<>();
                    list.add(DataManager.getInstance().getUser());
                    sendInviteToGroupMsg(bean, null, list);
                    showToast(R.string.chat_add_group_success);
                    addGroupInList(bean);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, bean);
                    gotoPager(GroupChatFragment.class, bundle);
                }
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }

    private void searchContact(String text) {
        ChatHttpMethods.getInstance().searchContact(text, new HttpObserver(new SubscriberOnNextListener<UserBean>() {
            @Override
            public void onNext(UserBean user, String msg) {
                if (getView() == null || user == null) {
                    return;
                }
                user.setContactId(user.getUserId());
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, user);
                boolean isFriend = false;
                ArrayList<UserBean> list = DataManager.getInstance().getFriends();
                if (list != null && !list.isEmpty()) {
                    for (UserBean bean : list) {
                        if (bean.getContactId() == user.getUserId()) {
                            isFriend = true;
                            break;
                        }
                    }
                }
                if (!isFriend) {
                    bundle.putInt(Constants.BUNDLE_EXTRA_2, VerifyApplyFragment.ADD_BY_QRCODE);
                }
                gotoPager(isFriend ? UserInfoFragment.class : VerifyApplyFragment.class, bundle);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }


    private void showDeleteChatRecord(final ChatBean chatBean) {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.chat_layout_two_btn_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                ((TextView) view.findViewById(R.id.tv1)).setText(getString(R.string.chat_tip));
                ((TextView) view.findViewById(R.id.tv2)).setText(getString(R.string.chat_delete_chat_tip));
                ((TextView) view.findViewById(R.id.btn1)).setText(getString(R.string.chat_cancel));
                ((TextView) view.findViewById(R.id.btn2)).setText(getString(R.string.chat_ok));
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btn1, R.id.btn2);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.btn2) {
                    UserBean myInfo = DataManager.getInstance().getUser();
                    if (chatBean.chatUser != null) {
                        DatabaseOperate.getInstance().deleteUserChatRecord(myInfo.getUserId(), chatBean.chatUser.getContactId());
                        if (chatBean.chatUser.getUserId() != -1) {
                            getAdapter().removeChatUser(chatBean);
                        } else {
                            chatBean.lastMsg = null;
                            getAdapter().notifyDataSetChanged();
                        }
                    } else {
                        DatabaseOperate.getInstance().deleteGroupChatRecord(myInfo.getUserId(), chatBean.group.getGroupId());
                        getAdapter().removeChatUser(chatBean);
                    }
                    resetTotalUnReadNum();
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }


    // 设置菜单监听器。
    SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        // 创建菜单：
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            int width = Utils.dip2px(getActivity(), 70);
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity())
                    .setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.color_f9_65_59))
                    .setTextColor(Color.WHITE)
                    .setText(R.string.wallet_delete)
                    .setWidth(width)
                    .setHeight(height);
            swipeRightMenu.addMenuItem(deleteItem);
        }
    };

    SwipeMenuItemClickListener swipeMenuItemClickListener = new SwipeMenuItemClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge) {
            // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
            menuBridge.closeMenu();

            int direction = menuBridge.getDirection();//左边还是右边菜单
            int adapterPosition = menuBridge.getAdapterPosition();//    ecyclerView的Item的position。

            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                try {
                    showDeleteChatRecord(getAdapter().getItem(adapterPosition));
                } catch (Exception e) {

                }
            }

        }
    };
}
