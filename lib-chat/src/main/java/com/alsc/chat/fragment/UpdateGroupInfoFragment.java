package com.alsc.chat.fragment;


import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alsc.chat.R;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;

import org.greenrobot.eventbus.EventBus;

public class UpdateGroupInfoFragment extends ChatBaseFragment {

    private GroupBean mGroup;

    public static final int UPDATE_GROUP_NAME = 0;
    public static final int UPDATE_GROUP_NOTICE = 1;
    public static final int UPDATE_IN_GROUP_NICK = 2;

    private int mUpdateGroupType;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_update_group_info;
    }

    @Override
    protected void onViewCreated(View view) {
        mUpdateGroupType = getArguments().getInt(Constants.BUNDLE_EXTRA_2, UPDATE_GROUP_NAME);
        mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setTopStatusBarStyle(view);

        final TextView tvLeft = fv(R.id.tvLeft);
        tvLeft.setVisibility(View.VISIBLE);
        tvLeft.setOnClickListener(this);
        setText(tvLeft, getString(R.string.chat_finish));

        EditText etNotice = view.findViewById(R.id.etNotice);
        if (mUpdateGroupType == UPDATE_GROUP_NAME) {
            setText(R.id.tvTitle, R.string.chat_update_group_name);
            setViewGone(R.id.tvLetterNum);
            etNotice.setMaxLines(1);
            etNotice.setHint(R.string.chat_group_name);
            etNotice.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
            if (!TextUtils.isEmpty(mGroup.getName())) {
                etNotice.setText(mGroup.getName());
                etNotice.setSelection(Math.min(mGroup.getName().length(), 20));
                etNotice.setCursorVisible(true);
                etNotice.requestFocus();
            }
        } else if (mUpdateGroupType == UPDATE_GROUP_NOTICE) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) etNotice.getLayoutParams();
            setText(R.id.tvTitle, R.string.chat_update_group_notice);
            etNotice.setHint(R.string.chat_group_notice);
            lp.height = Utils.dip2px(getActivity(), 180);
            etNotice.setLayoutParams(lp);
            etNotice.setFilters(new InputFilter[]{new InputFilter.LengthFilter(150)});
            if (!TextUtils.isEmpty(mGroup.getNotice())) {
                etNotice.setText(mGroup.getNotice());
                etNotice.setSelection(Math.min(mGroup.getNotice().length(), 150));
                etNotice.setCursorVisible(true);
                etNotice.requestFocus();
            }
            etNotice.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (getView() == null) {
                        return;
                    }
                    setText(R.id.tvLetterNum, s.toString().length() + "/150");
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } else if (mUpdateGroupType == UPDATE_IN_GROUP_NICK) {
            setText(R.id.tvTitle, R.string.chat_nick_in_group);
            setViewGone(R.id.tvLetterNum);
            etNotice.setMaxLines(1);
            etNotice.setHint(R.string.chat_nick_in_group);
            etNotice.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
            if (!TextUtils.isEmpty(mGroup.getMyNickInGroup())) {
                etNotice.setText(mGroup.getMyNickInGroup());
                etNotice.setSelection(Math.min(mGroup.getMyNickInGroup().length(), 20));
                etNotice.setCursorVisible(true);
                etNotice.requestFocus();
            }
        }
    }


    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvLeft) {
            String text = getTextById(R.id.etNotice);
            if (TextUtils.isEmpty(text)) {
                return;
            }
            if (mUpdateGroupType == UPDATE_GROUP_NAME) {
                ChatHttpMethods.getInstance().updateGroupName(String.valueOf(mGroup.getGroupId()), text,
                        new HttpObserver(new SubscriberOnNextListener<GroupBean>() {
                            @Override
                            public void onNext(GroupBean bean, String msg) {
                                sendUpdateGroupMsg(mGroup, MessageType.TYPE_UPDATE_GROUP_NAME.ordinal(), text);
                                if (getView() == null) {
                                    return;
                                }
                                mGroup.setName(text);
                                EventBus.getDefault().post(mGroup);
                                finish();
                            }
                        }, getActivity(), (ChatBaseActivity) getActivity()));
            } else if (mUpdateGroupType == UPDATE_GROUP_NOTICE) {
                ChatHttpMethods.getInstance().updateGroupNotice(String.valueOf(mGroup.getGroupId()), text,
                        new HttpObserver(new SubscriberOnNextListener<GroupBean>() {
                            @Override
                            public void onNext(GroupBean bean, String msg) {
                                sendUpdateGroupMsg(mGroup, MessageType.TYPE_UPDATE_GROUP_NOTICE.ordinal(), text);
                                if (getView() == null) {
                                    return;
                                }
                                mGroup.setNotice(text);
                                EventBus.getDefault().post(mGroup);
                                finish();
                            }
                        }, getActivity(), (ChatBaseActivity) getActivity()));
            } else if (mUpdateGroupType == UPDATE_IN_GROUP_NICK) {
                ChatHttpMethods.getInstance().updateGroupMemo(String.valueOf(mGroup.getGroupId()), text, "",
                        new HttpObserver(new SubscriberOnNextListener() {
                            @Override
                            public void onNext(Object o, String msg) {
                                if (getView() == null) {
                                    return;
                                }
                                mGroup.setMyNickInGroup(text);
                                EventBus.getDefault().post(mGroup);
                                finish();
                            }
                        }, getActivity(), (ChatBaseActivity) getActivity()));
            }
        } else if (id == R.id.ivAddMsgFilter) {
            String content = getTextById(R.id.etNotice).trim();
            if (TextUtils.isEmpty(content)) {
                return;
            }
            ChatHttpMethods.getInstance().groupBlockCreate(String.valueOf(mGroup.getGroupId()), content,
                    new HttpObserver(new SubscriberOnNextListener() {
                        @Override
                        public void onNext(Object o, String msg) {
                            if (getView() == null) {
                                return;
                            }
                            setText(R.id.etNotice, "");
                        }
                    }, getActivity(), (ChatBaseActivity) getActivity()));
        }
    }


}
