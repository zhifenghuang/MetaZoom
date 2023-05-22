package com.alsc.chat.fragment;

import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.MessageAdapter;
import com.alsc.chat.adapter.ServiceMessageAdapter;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.OnHttpErrorListener;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildLongClickListener;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.alsc.chat.manager.ChatManager;
import com.alsc.chat.utils.Constants;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ServiceFragment extends ChatFragment {


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_service;
    }

    @Override
    protected void onViewCreated(View view) {
        mMyInfo = DataManager.getInstance().getUser();
        setText(R.id.tvTitle, R.string.chat_service);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        init(view);
 //       questionOnline();
        getQuestionList();
    }

    void init(View view) {
        setTopStatusBarStyle(view);
        setViewsOnClickListener(R.id.ivSend, R.id.tvService, R.id.ivAdd,
                R.id.llAlbum, R.id.llCamera, R.id.llVideo, R.id.llLeaveMsg, R.id.llFile);
        initEvent();
        initListeners();
        initScrollListener();

        getAdapter().setOnItemChildLongClickListener(new OnItemChildLongClickListener() {
            @Override
            public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
                if (!getAdapter().isEditMode()) {
                    showMsgMoreDialog(getAdapter().getItem(position), view);
                }
                return false;
            }
        });
    }

    @Override
    public void updateUIText() {
        mMyInfo = DataManager.getInstance().getUser();
        getAdapter().setMyInfo(mMyInfo);
    }

    @Override
    protected MessageAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new ServiceMessageAdapter(getActivity(), mMyInfo);
        }
        return mAdapter;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @Override
    public void onReceive(HashMap map) {
        if (getView() != null && map != null) {
            if (map.containsKey(Constants.SELECT_SERVICE)) {
                mChatUser = (UserBean) map.get(Constants.SELECT_SERVICE);
                initMsgs();
            } else {
                super.onReceive(map);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvService) {
            if (mChatUser == null) {
                v.setEnabled(false);
                v.setAlpha(0.3f);
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v.setEnabled(true);
                        v.setAlpha(1.0f);
                    }
                }, 10000);
                questionOnline();
            }
        } else if (id == R.id.llLeaveMsg) {
            gotoPager(LeaveMsgFragment.class);
        } else if (id == R.id.ivAdd) {
            mIsTapAddBtn = true;
            if (mIsKeyBordShow) {
                hideKeyBoard(fv(R.id.etChat));
            } else {
                View llBottomTab = fv(R.id.llBottomTab);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) llBottomTab.getLayoutParams();
                View llSelectDeleteTime = fv(R.id.llSelectDeleteTime);
                if (lp.height == 0) {
                    if (mKeyBoardHeight <= 0) {
                        mKeyBoardHeight = (int) (((BaseActivity)getActivity()).getDisplayMetrics().heightPixels * 0.4);
                    }
                    lp.height = mKeyBoardHeight;
                    setViewGone(R.id.llSelectDeleteTime);
                    setViewVisible(R.id.ll1, R.id.ll2);
                    llBottomTab.setLayoutParams(lp);
                } else {
                    if (llSelectDeleteTime != null && llSelectDeleteTime.getVisibility() == View.VISIBLE) {
                        setViewGone(R.id.llSelectDeleteTime);
                        setViewVisible(R.id.ll1, R.id.ll2);
                    } else {
                        EditText et = fv(R.id.etChat);
                        et.setFocusable(true);
                        et.setFocusableInTouchMode(true);
                        et.requestFocus();
                        showKeyBoard(et);
                    }
                }
            }
        } else {
            if (mChatUser == null) {
                showToast(R.string.chat_please_select_service_first);
                questionOnline();
                return;
            }
            super.onClick(v);
        }
    }

    private void getQuestionList() {
        ChatHttpMethods.getInstance().questionList(new HttpObserver(new SubscriberOnNextListener<ArrayList<QuestionBean>>() {
            @Override
            public void onNext(ArrayList<QuestionBean> list, String msg) {
                if (getView() == null || list == null || list.isEmpty()) {
                    return;
                }
                DataManager.getInstance().saveQuestions(list);
                ServiceMessageBean bean = new ServiceMessageBean();
                bean.setMsgType(MessageType.TYPE_QUESTION.ordinal());
                bean.setQuestions(list);
                getAdapter().clearMsg();
                getAdapter().addData(bean);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }

    protected BasicMessage getMsg() {
        MessageBean msg = new MessageBean();
        msg.setCmd(2200);  //客服发的消息全是2200
        msg.setFromId(mMyInfo.getUserId());
        msg.setToId(mChatUser.getContactId());
        msg.setExtra(getGson().toJson(getUsers()));
        return msg;
    }

    private void questionOnline() {
        ChatHttpMethods.getInstance().questionOnline(new HttpObserver(new SubscriberOnNextListener<ArrayList<UserBean>>() {
            @Override
            public void onNext(ArrayList<UserBean> list, String msg) {
                if (getView() == null) {
                    return;
                }
                if (list == null || list.isEmpty()) {
                    showToast(R.string.chat_service_time);
                    return;
                }
                ServiceMessageBean bean = new ServiceMessageBean();
                bean.setMsgType(MessageType.TYPE_SELECT_SERVICE.ordinal());
                bean.setServices(list);
                getAdapter().addData(bean);
                if (list.size() == 1) {
                    mChatUser = list.get(0);
                    initMsgs();
                    getAdapter().setChatUser(mChatUser);
                    ServiceMessageBean sMsg = new ServiceMessageBean();
                    sMsg.setCmd(2200);
                    sMsg.setMsgType(MessageType.TYPE_TEXT.ordinal());
                    sMsg.setFromId(mChatUser == null ? 0 : mChatUser.getUserId());
                    sMsg.setToId(mMyInfo.getUserId());
                    sMsg.setContent(getActivity().getString(R.string.chat_service_first_msg));
                    getAdapter().addData(sMsg);
                }
                scrollBottom();
            }
        }, getActivity(), new OnHttpErrorListener() {
            @Override
            public void onConnectError(Throwable e) {
                showToast(R.string.chat_net_work_error);
            }

            @Override
            public void onServerError(int errorCode, String errorMsg) {
                showToast(R.string.chat_service_time);
                if (errorCode == 401) {
                    ChatManager.getInstance().showLoginOutDialog();
                }
            }
        }));
    }

    protected void initMsgs() {
        DatabaseOperate.getInstance().setAllMsgRead(mMyInfo.getUserId(), mChatUser.getContactId());
        ArrayList<MessageBean> list = DatabaseOperate.getInstance().getAllUserChatMsg(mMyInfo.getUserId(), mChatUser.getContactId());
        Collections.reverse(list);
        ArrayList<BasicMessage> messages = (ArrayList<BasicMessage>) getAdapter().getData();
        List<BasicMessage> message = (List<BasicMessage>) messages.clone();
        messages.clear();
        messages.addAll(list);
        messages.addAll(message);
        getAdapter().notifyDataSetChanged();
    }
}
