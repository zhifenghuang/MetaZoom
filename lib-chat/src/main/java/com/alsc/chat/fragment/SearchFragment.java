package com.alsc.chat.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.SearchChatRecordAdapter;
import com.alsc.chat.adapter.SearchContactAdapter;
import com.alsc.chat.utils.Utils;
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

import java.util.ArrayList;

public class SearchFragment extends ChatBaseFragment {

    public static final int SEARCH_LOCAL_FRIEND = 0;
    public static final int SEARCH_SERVER_FRIEND = 1;
    public static final int SEARCH_CHAT_RECORD = 2;
    public static final int SEARCH_GROUP_CHAT_RECORD = 3;

    private int mSearchType;

    private UserBean mChatUser;
    private GroupBean mGroup;

    private String mSearchText;

    private ArrayList<BasicMessage> mMessageList;

    private SearchContactAdapter mAdapter;
    private SearchChatRecordAdapter mAdapter2;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search;
    }

    @Override
    protected void onViewCreated(View view) {
        mSearchType = getArguments().getInt(Constants.BUNDLE_EXTRA, SEARCH_LOCAL_FRIEND);
        setTopStatusBarStyle(view);
        EditText et = view.findViewById(R.id.etSearch);
        if (mSearchType == SEARCH_LOCAL_FRIEND) {
            setViewGone(R.id.topView);
            setViewVisible(R.id.llMyInfo);
            UserBean myInfo = DataManager.getInstance().getUser();
            Utils.displayAvatar(getActivity(), R.drawable.chat_default_avatar, myInfo.getAvatarUrl(), fv(R.id.ivMyAvatar));
            setText(R.id.tvMyName, myInfo.getNickName());
            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            getAdapter().onAttachedToRecyclerView(recyclerView);
            recyclerView.setAdapter(getAdapter());
            getAdapter().setFriends(DataManager.getInstance().getFriends(), DataManager.getInstance().getGroups());
            et.setHint(R.string.chat_search_contact_record);
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (getView() == null) {
                        return;
                    }
                    mSearchText = s.toString().trim().toLowerCase();
                    getAdapter().searchFriend(mSearchText);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } else if (mSearchType == SEARCH_SERVER_FRIEND) {
            setViewGone(R.id.topView);
            setViewGone(R.id.llMyInfo);
            setViewsOnClickListener(R.id.llSearchContent);
            et.setHint(R.string.chat_id_phone_email);
            final TextView tvText = view.findViewById(R.id.tvText);
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (getView() == null) {
                        return;
                    }
                    mSearchText = s.toString().trim();
                    if (TextUtils.isEmpty(mSearchText)) {
                        setViewGone(R.id.llSearchContent);
                    } else {
                        setViewVisible(R.id.llSearchContent);
                        tvText.setText(mSearchText);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_UNSPECIFIED) {
                        String text = textView.getText().toString();
                        searchContact(text);
                    }
                    return false;
                }
            });
        } else if (mSearchType == SEARCH_CHAT_RECORD || mSearchType == SEARCH_GROUP_CHAT_RECORD) {
            setViewGone(R.id.llMyInfo);
            setText(R.id.tvTitle, R.string.chat_search_chat_record);
            DatabaseOperate.getInstance().deleteAllExprieMsg();
            mMessageList = new ArrayList<>();
            et.setHint(R.string.chat_search_chat_record);
            if (mSearchType == SEARCH_CHAT_RECORD) {
                mChatUser = (UserBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA_2);
            } else {
                mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA_2);
            }
            mSearchText = getArguments().getString(Constants.BUNDLE_EXTRA_3, "");
            if (!TextUtils.isEmpty(mSearchText)) {
                setText(R.id.etSearch, mSearchText);
                et.setSelection(mSearchText.length());
                searchChatRecord(mSearchText);
            }
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (getView() == null) {
                        return;
                    }
                    searchChatRecord(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            getAdapter2().onAttachedToRecyclerView(recyclerView);
            recyclerView.setAdapter(getAdapter2());
        }
        setViewsOnClickListener(R.id.tvCancel);
    }

    private SearchContactAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new SearchContactAdapter(getActivity());
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    ContactItem contactItem = mAdapter.getItem(position);
                    if (contactItem == null) {
                        return;
                    }
                    if (contactItem.getItemType() == ContactItem.VIEW_TYPE_1) {
                        Bundle bundle = new Bundle();
                        if (contactItem.getFriend() != null) {
                            bundle.putSerializable(Constants.BUNDLE_EXTRA, contactItem.getFriend());
                            gotoPager(ChatFragment.class, bundle);
                        } else {
                            bundle.putSerializable(Constants.BUNDLE_EXTRA, contactItem.getGroup());
                            gotoPager(GroupChatFragment.class, bundle);
                        }
                        ((BaseActivity) getActivity()).finishAllOtherActivity();
                    } else if (contactItem.getItemType() == ContactItem.VIEW_TYPE_2) {
                        Bundle bundle = new Bundle();
                        if (contactItem.getFriend() != null) {
                            bundle.putInt(Constants.BUNDLE_EXTRA, SearchFragment.SEARCH_CHAT_RECORD);
                            bundle.putSerializable(Constants.BUNDLE_EXTRA_2, contactItem.getFriend());
                            bundle.putString(Constants.BUNDLE_EXTRA_3, mSearchText);
                            gotoPager(SearchFragment.class, bundle);
                        } else {
                            bundle.putInt(Constants.BUNDLE_EXTRA, SearchFragment.SEARCH_GROUP_CHAT_RECORD);
                            bundle.putSerializable(Constants.BUNDLE_EXTRA_2, contactItem.getGroup());
                            bundle.putString(Constants.BUNDLE_EXTRA_3, mSearchText);
                            gotoPager(SearchFragment.class, bundle);
                        }
                    }
                }
            });
        }
        return mAdapter;
    }

    private SearchChatRecordAdapter getAdapter2() {
        if (mAdapter2 == null) {
            mAdapter2 = new SearchChatRecordAdapter(getActivity());
            mAdapter2.setSearchObject(mSearchType, mSearchType == SEARCH_CHAT_RECORD ? mChatUser : mGroup);
            mAdapter2.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    Bundle bundle = new Bundle();
                    if (mSearchType == SEARCH_CHAT_RECORD) {
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, mChatUser);
                        bundle.putSerializable(Constants.BUNDLE_EXTRA_2, getAdapter2().getItem(position));
                        gotoPager(ChatFragment.class, bundle);
                    } else {
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, mGroup);
                        bundle.putSerializable(Constants.BUNDLE_EXTRA_2, getAdapter2().getItem(position));
                        gotoPager(GroupChatFragment.class, bundle);
                    }
                    ((BaseActivity) getActivity()).finishAllOtherActivity();
                }
            });
        }
        return mAdapter2;
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvCancel) {
            finish();
        } else if (id == R.id.llSearchContent) {
            searchContact(mSearchText);
        }
    }

    private void searchChatRecord(String text) {
        mMessageList.clear();
        if (!TextUtils.isEmpty(text)) {
            UserBean myInfo = DataManager.getInstance().getUser();
            if (mSearchType == SEARCH_CHAT_RECORD) {
                mMessageList.addAll(DatabaseOperate.getInstance().searchChatRecordByText(myInfo.getUserId(),
                        mChatUser.getContactId(), text, MessageType.TYPE_TEXT.ordinal()));
            } else {
                mMessageList.addAll(DatabaseOperate.getInstance().searchGroupChatRecordByText(myInfo.getUserId(),
                        mGroup.getGroupId(), text, MessageType.TYPE_TEXT.ordinal()));
            }
        }
        getAdapter2().setSearchText(text);
        getAdapter2().setNewData(mMessageList);
        getAdapter2().notifyDataSetChanged();
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

                UserBean myInfo = DataManager.getInstance().getUser();
                if (myInfo.isService()) {  //如果是客服直接让客服搜索发消息
                    gotoPager(UserInfoFragment.class, bundle);
                    return;
                }

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
                    bundle.putInt(Constants.BUNDLE_EXTRA_2, VerifyApplyFragment.ADD_BY_ID);
                }
                gotoPager(isFriend ? UserInfoFragment.class : VerifyApplyFragment.class, bundle);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }
}
