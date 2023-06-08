package com.alsc.chat.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.FriendAdapter;
import com.alsc.chat.utils.Constants;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.google.gson.Gson;
import com.zhangke.websocket.WebSocketHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

public class TransferMsgFragment extends ChatBaseFragment {

    private FriendAdapter mAdapter;

    private BasicMessage mMsg;

    private ArrayList<UserBean> mFriendList;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_transfer_msg;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_choose_contact);
        mMsg = (BasicMessage) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        mMsg.setFromId(DataManager.getInstance().getUser().getUserId());
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        mFriendList = DataManager.getInstance().getFriends();
        getAdapter().setNewData(getNewList(mFriendList));

        EditText et = view.findViewById(R.id.etSearch);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (getView() == null) {
                    return;
                }
                searchFriends(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private FriendAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new FriendAdapter(getActivity());
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    if (position == 0) {
                        Bundle bundle = new Bundle();
                        bundle.putInt(Constants.BUNDLE_EXTRA, GroupListFragment.FROM_TRANSFER_MSG);
                        gotoPager(GroupListFragment.class, bundle);
                    } else {
                        showConfirmDialog(0, null, mAdapter.getItem(position).getFriend());
                    }
                }
            });
        }
        return mAdapter;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMsg(HashMap<String, GroupBean> map) {
        if (getView() != null && map != null) {
            if (map.containsKey(Constants.SELECT_A_GROUP)) {
                final GroupBean group = map.get(Constants.SELECT_A_GROUP);
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showConfirmDialog(1, group, null);
                    }
                }, 600);
            }
        }
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {

    }

    private ArrayList<FriendItem> getNewList(ArrayList<UserBean> list) {
        ArrayList<FriendItem> newList = new ArrayList<>();
        FriendItem item = new FriendItem();
        item.setItemType(FriendItem.VIEW_TYPE_4);
        newList.add(item);
        if (list != null) {
            for (UserBean bean : list) {
                if (bean.getBlock() == 1) {
                    continue;
                }
                item = new FriendItem();
                item.setItemType(FriendItem.VIEW_TYPE_3);
                item.setFriend(bean);
                newList.add(item);
            }
        }
        return newList;
    }

    private void showConfirmDialog(final int type, final GroupBean groupBean, final UserBean friend) {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.chat_layout_two_btn_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                ((TextView) view.findViewById(R.id.tv1)).setText(getString(R.string.chat_tip));
                ((TextView) view.findViewById(R.id.tv2)).setText(getString(R.string.chat_are_you_sure_send_msg));
                ((TextView) view.findViewById(R.id.btn1)).setText(getString(R.string.chat_cancel));
                ((TextView) view.findViewById(R.id.btn2)).setText(getString(R.string.chat_ok));
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btn1, R.id.btn2);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.btn2) {
                    BasicMessage msg;
                    UserBean myInfo = DataManager.getInstance().getUser();
                    ArrayList<HashMap<String, Object>> list = new ArrayList<>();
                    list.add(myInfo.toMap());
                    if (type == 0) {
                        msg = MessageBean.toMessage(mMsg, friend.getContactId());
                        list.add(friend.toMap());
                    } else {
                        msg = GroupMessageBean.toGroupMessage(mMsg, groupBean.getGroupId());
                    }
                    msg.setExtra(new Gson().toJson(list));
                    WebSocketHandler.getDefault().send(msg.toJson());
                    DatabaseOperate.getInstance().insert(msg);
                    EventBus.getDefault().post(msg);
                    finish();
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    private void searchFriends(String text) {
        text = text.trim();
        if (TextUtils.isEmpty(text)) {
            getAdapter().setNewInstance(getNewList(mFriendList));
            return;
        }
        if (mFriendList != null && !mFriendList.isEmpty()) {
            ArrayList<UserBean> searchUsers = new ArrayList<>();
            for (UserBean user : mFriendList) {
                if (user.getNickName().toLowerCase().contains(text)
                        || user.getLoginAccount().toLowerCase().contains(text)
                        || user.getPinyinName().contains(text)) {
                    searchUsers.add(user);
                }
            }
            getAdapter().setNewInstance(getNewList(searchUsers));
        }
    }

}
