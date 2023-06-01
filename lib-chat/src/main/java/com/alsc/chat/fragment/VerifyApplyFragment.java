package com.alsc.chat.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
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

public class VerifyApplyFragment extends ChatBaseFragment {

    private UserBean mUserInfo;

    private int mAddType;

    public static final int ADD_BY_ID = 0;
    public static final int ADD_BY_QRCODE = 1;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_verify_apply;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_verify_apply);
        mUserInfo = (UserBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        mAddType = getArguments().getInt(Constants.BUNDLE_EXTRA_2, ADD_BY_ID);
        setText(R.id.tvNick, mUserInfo.getNickName());
        String account = mUserInfo.getLoginAccount();
        setText(R.id.tvID, "ID: " + account.substring(0, 6) + "..." + account.substring(account.length() - 6));
        int resId = getResources().getIdentifier("chat_default_avatar_" + mUserInfo.getUserId() % 6,
                "drawable", getActivity().getPackageName());
        Utils.loadImage(getActivity(), resId, mUserInfo.getAvatarUrl(), view.findViewById(R.id.ivAvatar));
        UserBean myInfo = DataManager.getInstance().getUser();
        setText(R.id.etVerifyApply, getString(R.string.chat_i_am, myInfo.getNickName()));
        setViewsOnClickListener(R.id.tvID, R.id.btnSend);
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvID) {
            ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", mUserInfo.getLoginAccount());
            cm.setPrimaryClip(mClipData);
            showToast(R.string.chat_copy_successful);
        } else if (id == R.id.btnSend) {
            String applyText = getTextById(R.id.etVerifyApply).trim();
            ChatHttpMethods.getInstance().addContact(String.valueOf(mUserInfo.getContactId()), "", applyText, String.valueOf(mAddType),
                    new HttpObserver(new SubscriberOnNextListener() {
                        @Override
                        public void onNext(Object o, String msg) {
                            if (mUserInfo.getAllowAdd() == 0) {
                                UserBean myInfo = DataManager.getInstance().getUser();
                                MessageBean bean = new MessageBean();
                                bean.setCmd(2000);
                                bean.setFromId(myInfo.getUserId());
                                bean.setToId(mUserInfo.getUserId());
                                ArrayList<HashMap<String, Object>> list = new ArrayList<>();
                                list.add(myInfo.toMap());
                                list.add(mUserInfo.toMap());
                                bean.setMsgType(MessageType.TYPE_TEXT.ordinal());
                                bean.setContent(getActivity().getString(R.string.chat_add_friend_first_said));
                                bean.setExtra(new Gson().toJson(list));
                                WebSocketHandler.getDefault().send(bean.toJson());
                                DatabaseOperate.getInstance().insert(bean);

                                HashMap<String, String> map = new HashMap<>();
                                map.put(Constants.REDRESH_FRIENDS, "");
                                EventBus.getDefault().post(map);
                            }
                            UserBean myInfo = DataManager.getInstance().getUser();
                            MessageBean bean = MessageBean.getSystemMsg(myInfo.getUserId(), mUserInfo.getContactId(), Constants.NEW_VERIFY,
                                    System.currentTimeMillis());
                            WebSocketHandler.getDefault().send(bean.toJson());
                            if (getView() == null) {
                                return;
                            }
                            ((BaseActivity) getActivity()).finishAllOtherActivity();
                        }
                    }, getActivity(), (ChatBaseActivity) getActivity()));
        }
    }
}
