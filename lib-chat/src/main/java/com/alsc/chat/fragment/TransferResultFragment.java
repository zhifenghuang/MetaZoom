package com.alsc.chat.fragment;

import android.view.View;

import com.alsc.chat.R;
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
import com.alsc.chat.utils.Utils;
import com.google.gson.Gson;
import com.zhangke.websocket.WebSocketHandler;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;

public class TransferResultFragment extends ChatBaseFragment {

    private EnvelopeBean mEnvelope;
    private UserBean mChatUser;
    private String mMsgId;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_transfer_result;
    }

    @Override
    protected void onViewCreated(View view) {
        UserBean myInfo = DataManager.getInstance().getUser();
        setText(R.id.tvTitle, R.string.chat_transfer_detail);
        mEnvelope = (EnvelopeBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        mChatUser = (UserBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA_2);
        mMsgId = getArguments().getString(Constants.BUNDLE_EXTRA_3, "");
        setTopStatusBarStyle(view);
        setText(R.id.tvValue, getString(R.string.chat_xxx_alsc, "" + mEnvelope.getAmount()));
        int status = mEnvelope.getStatus();
        if (myInfo.getUserId() == mEnvelope.getUserId()) {
            Utils.displayAvatar(getActivity(), R.drawable.chat_default_avatar, mEnvelope.getAvatarUrl(), view.findViewById(R.id.ivAvatar));
            setText(R.id.tvName, mEnvelope.getNickName());
            if (status == 1) {
                setText(R.id.tvResult, getString(R.string.chat_wait_get_money, mChatUser.getNickName()));
                setText(R.id.tvTip, R.string.chat_transfer_tip_1);
            } else if (status == 2) {
                setText(R.id.tvResult, R.string.chat_transfer_success);
                setText(R.id.tvTip, R.string.chat_had_save_in_peer_wallet);
            } else {
                setText(R.id.tvResult, R.string.chat_had_back);
                setText(R.id.tvTip, "");
            }
        } else {
            Utils.displayAvatar(getActivity(), R.drawable.chat_default_avatar, myInfo.getAvatarUrl(), view.findViewById(R.id.ivAvatar));
            setText(R.id.tvName, myInfo.getNickName());
            if (status == 1) {
                setText(R.id.tvResult, R.string.chat_wait_receive_money);
                setText(R.id.tvTip, R.string.chat_transfer_tip_2);
                setViewVisible(R.id.tvOk);
                setViewsOnClickListener(R.id.tvOk);
            } else if (status == 2) {
                setText(R.id.tvResult, R.string.chat_had_receive_money);
                setText(R.id.tvTip, R.string.chat_had_save_in_my_wallet);
            } else {
                setText(R.id.tvResult, R.string.chat_had_back);
                setText(R.id.tvTip, "");
            }
        }
        setText(R.id.tvStarTime, getString(R.string.chat_transfer_time, mEnvelope.getCreateTime()));
        if (status == 1) {
            setText(R.id.tvEndTime, "");
        } else if (status == 2) {
            ArrayList<EnvelopeBean.Session> sessions = mEnvelope.getSession();
            if (sessions == null || sessions.isEmpty()) {
                setText(R.id.tvEndTime, "");
            } else {
                setText(R.id.tvEndTime, getString(R.string.chat_receive_money_time, sessions.get(0).getCreateTime()));
            }
        } else if (status == 3) {
            setText(R.id.tvEndTime, getString(R.string.chat_back_time, Utils.longToDate3(mEnvelope.getEndTime())));
        }
    }


    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvOk) {
            ChatHttpMethods.getInstance().envelopeDraw(mEnvelope.getEnvelopeId(), new HttpObserver(new SubscriberOnNextListener<EnvelopeBean>() {
                @Override
                public void onNext(EnvelopeBean bean1, String msg) {
                    UserBean myInfo = DataManager.getInstance().getUser();
                    MessageBean msgBean = new MessageBean();
                    msgBean.setCmd(2000);
                    msgBean.setFromId(myInfo.getUserId());
                    msgBean.setToId(mChatUser.getContactId());
                    ArrayList<HashMap<String, Object>> list = new ArrayList<>();
                    list.add(myInfo.toMap());
                    list.add(mChatUser.toMap());
                    msgBean.setExtra(new Gson().toJson(list));
                    msgBean.setMsgType(MessageType.TYPE_RECEIVE_TRANSFER.ordinal());
                    mEnvelope.setStatus(2);
                    HashMap<String, Object> map = mEnvelope.toMap();
                    map.put("messageId", mMsgId);
                    msgBean.setContent(new Gson().toJson(map));
                    WebSocketHandler.getDefault().send(msgBean.toJson());
                    DatabaseOperate.getInstance().insert(msgBean);
                    EventBus.getDefault().post(msgBean);
                    map.clear();
                    map.put(Constants.RECEIVE_ENVELOPE, mMsgId);
                    EventBus.getDefault().post(map);
                    if (getView() == null) {
                        return;
                    }
                    mEnvelope = bean1;
                    setText(R.id.tvResult, R.string.chat_had_receive_money);
                    setText(R.id.tvTip, R.string.chat_had_save_in_my_wallet);
                    ArrayList<EnvelopeBean.Session> sessions = mEnvelope.getSession();
                    if (sessions == null || sessions.isEmpty()) {
                        setText(R.id.tvEndTime, "");
                    } else {
                        setText(R.id.tvEndTime, getString(R.string.chat_receive_money_time, sessions.get(0).getCreateTime()));
                    }
                    setViewGone(R.id.tvOk);
                }
            }, getActivity(), (ChatBaseActivity) getActivity()));
        }
    }
}
