package com.alsc.chat.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.OnHttpErrorListener;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.manager.ChatManager;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.alsc.chat.dialog.InputPasswordDialog;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.utils.Constants;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.fragment.BaseFragment;
import com.common.lib.manager.DataManager;
import com.common.lib.mvp.IPresenter;
import com.google.gson.Gson;
import com.zhangke.websocket.WebSocketHandler;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Fragment基类提供公共的页面跳转方面，公共弹窗等方法
 *
 * @author xiangwei.ma
 */
public abstract class ChatBaseFragment extends BaseFragment implements View.OnClickListener {

    protected boolean mIsToAnotherPage;

    private boolean mIsGetFriend;
    private boolean mIsGetGroup;
    protected ArrayList<UserBean> mFriendList;
    protected ArrayList<GroupBean> mGroupList;


    @NonNull
    @Override
    protected IPresenter onCreatePresenter() {
        return null;
    }

    @Override
    protected void initView(@NonNull View view, @Nullable Bundle savedInstanceState) {
        onViewCreated(view);
    }

    protected void onViewCreated(View view) {

    }

    public void updateUIText() {
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        mIsToAnotherPage = false;
        updateUIText();
        super.onResume();
    }


    @Override
    public boolean isNeedSetTopStyle() {
        return false;
    }

    protected void showPayInGroupDialog(final GroupBean group, final UserBean inviteUser) {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.chat_layout_two_btn_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                ((TextView) view.findViewById(R.id.tv1)).setText(getString(R.string.chat_tip));
                ((TextView) view.findViewById(R.id.tv2)).setText(getString(R.string.chat_the_group_need_pay_xxx_alsc, String.valueOf(group.getPayAmount())));
                ((TextView) view.findViewById(R.id.btn1)).setText(getString(R.string.chat_i_think));
                ((TextView) view.findViewById(R.id.btn2)).setText(getString(R.string.chat_pay_enter_group_1));
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btn1, R.id.btn2);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.btn2) {
                    showInputPayPasswordDialog(group, inviteUser);
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    private void showInputPayPasswordDialog(final GroupBean group, final UserBean inviteUser) {
        InputPasswordDialog dialog = new InputPasswordDialog(getActivity(), -1, null);
        dialog.setOnInputPasswordListener(new InputPasswordDialog.OnInputPasswordListener() {
            @Override
            public void afterCheckPassword() {
                if (getView() == null) {
                    return;
                }
                payEnterGroup(group, inviteUser);
            }
        });
        dialog.show();
    }

    private void payEnterGroup(final GroupBean group, final UserBean inviteUser) {
        ChatHttpMethods.getInstance().envelopeSend(group.getPayAmount(), 1, "", 6, -1, group.getGroupId(),
                new HttpObserver(new SubscriberOnNextListener<EnvelopeBean>() {
                    @Override
                    public void onNext(EnvelopeBean bean, String msg) {
                        ArrayList<UserBean> list = new ArrayList<>();
                        list.add(DataManager.getInstance().getUser());
                        sendInviteToGroupMsg(group, inviteUser, list);
                        if (getView() == null || bean == null) {
                            return;
                        }
                        showToast(R.string.chat_pay_enter_group_success);
                        addGroupInList(group);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, group);
                        gotoPager(GroupChatFragment.class, bundle);
                        ((ChatBaseActivity) getActivity()).finishAllOtherActivity();
                    }
                }, getActivity(), (ChatBaseActivity) getActivity()));
    }

    protected void removeFromGroupInList(GroupBean group) {
        ArrayList<GroupBean> groups = DataManager.getInstance().getGroups();
        for (GroupBean bean : groups) {
            if (bean.getGroupId() == group.getGroupId()) {
                groups.remove(bean);
                DataManager.getInstance().saveGroups(groups);
                break;
            }
        }
    }


    protected void addGroupInList(GroupBean group) {
        ArrayList<GroupBean> groups = DataManager.getInstance().getGroups();
        boolean isHad = false;
        for (GroupBean bean : groups) {
            if (bean.getGroupId() == group.getGroupId()) {
                isHad = true;
                break;
            }
        }
        if (!isHad) {
            groups.add(group);
            DataManager.getInstance().saveGroups(groups);
        }
    }

    protected void updateGroup(GroupBean group) {
        ArrayList<GroupBean> groups = DataManager.getInstance().getGroups();
        for (GroupBean bean : groups) {
            if (bean.getGroupId() == group.getGroupId()) {
                groups.remove(bean);
                groups.add(group);
                DataManager.getInstance().saveGroups(groups);
                break;
            }
        }
    }

    protected boolean isInGroup(long groupId) {
        ArrayList<GroupBean> groups = DataManager.getInstance().getGroups();
        for (GroupBean bean : groups) {
            if (bean.getGroupId() == groupId) {
                return true;
            }
        }
        return false;
    }

    public void sendInviteToGroupMsg(GroupBean group, UserBean inviteUser, List<UserBean> list) {
        GroupMessageBean groupMessageBean = new GroupMessageBean();
        groupMessageBean.setCmd(2100);
        groupMessageBean.setMsgType(inviteUser == null ? MessageType.TYPE_IN_GROUP_BY_QRCODE.ordinal() : MessageType.TYPE_INVITE_TO_GROUP.ordinal());
        groupMessageBean.setGroupId(group.getGroupId());
        if (inviteUser != null) {
            groupMessageBean.setContent(new Gson().toJson(inviteUser.toMap()));
        }
        groupMessageBean.setFromId(DataManager.getInstance().getUserId());
        ArrayList<HashMap<String, Object>> userList = new ArrayList<>();
        for (UserBean bean : list) {
            if (bean != null) {
                userList.add(bean.toMap());
            }
        }
        groupMessageBean.setExtra(new Gson().toJson(userList));
        if (TextUtils.isEmpty(groupMessageBean.getExtra())) {
            return;
        }
        WebSocketHandler.getDefault().send(groupMessageBean.toJson());
        groupMessageBean.setSendStatus(1);
        DatabaseOperate.getInstance().insert(groupMessageBean);
        EventBus.getDefault().post(groupMessageBean);
    }


    public void sendUpdateGroupMsg(GroupBean group, int msgType, String text) {
        GroupMessageBean groupMessageBean = new GroupMessageBean();
        groupMessageBean.setCmd(2100);
        groupMessageBean.setMsgType(msgType);
        groupMessageBean.setGroupId(group.getGroupId());
        groupMessageBean.setContent(text);
        groupMessageBean.setFromId(DataManager.getInstance().getUserId());
        ArrayList<HashMap<String, Object>> userList = new ArrayList<>();
        userList.add(DataManager.getInstance().getUser().toMap());
        groupMessageBean.setExtra(new Gson().toJson(userList));
        WebSocketHandler.getDefault().send(groupMessageBean.toJson());
        groupMessageBean.setSendStatus(1);
        DatabaseOperate.getInstance().insert(groupMessageBean);
        EventBus.getDefault().post(groupMessageBean);
    }

    /**
     * 跳转到新的界面
     *
     * @param pagerClass
     */
    public void gotoPager(final Class<?> pagerClass) {
        gotoPager(pagerClass, null);
    }

    /**
     * 跳转到新的界面
     *
     * @param pagerClass
     * @param bundle
     */
    public void gotoPager(final Class<?> pagerClass, final Bundle bundle) {
        if (mIsToAnotherPage) {
            return;
        }
        if (getActivity() instanceof ChatBaseActivity) {
            mIsToAnotherPage = true;
            ((ChatBaseActivity) getActivity()).gotoPager(pagerClass, bundle);
        }
    }

    protected <VT extends View> VT fv(View parent, int id) {
        return (VT) parent.findViewById(id);
    }

    protected <VT extends View> VT fv(int id) {
        return (VT) getView().findViewById(id);
    }

    public void hideKeyBoard(View view) {
        if (getActivity() == null) {
            return;
        }
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view == null) {
            if (getActivity().getCurrentFocus() != null) {
                in.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } else {
            in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showKeyBoard(View view) {
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, 0);
        }
    }


    public void getFriendFromServer() {
        if (mIsGetFriend) {
            return;
        }
        UserBean userBean = DataManager.getInstance().getUser();
        if (userBean == null) {
            return;
        }
        mIsGetFriend = true;
        ChatHttpMethods.getInstance().getFriends(new HttpObserver(new SubscriberOnNextListener<ArrayList<UserBean>>() {
            @Override
            public void onNext(ArrayList<UserBean> list, String msg) {
                DataManager.getInstance().saveFriends(list);
                mFriendList = list;

                setData(mFriendList, DataManager.getInstance().getGroups());
                mIsGetFriend = false;
            }
        }, getActivity(), false, new OnHttpErrorListener() {

            @Override
            public void onConnectError(Throwable e) {
                mIsGetFriend = false;
            }

            @Override
            public void onServerError(int errorCode, String errorMsg) {
                mIsGetFriend = false;
                if (errorCode == 401) {
                    ChatManager.getInstance().showLoginOutDialog();
                }
            }
        }));
    }

    public void getGroupFromServer() {
        if (mIsGetGroup) {
            return;
        }
        UserBean userBean = DataManager.getInstance().getUser();
        if (userBean == null) {
            return;
        }
        mIsGetGroup = true;
        ChatHttpMethods.getInstance().getGroups(1, Integer.MAX_VALUE - 1,
                new HttpObserver(new SubscriberOnNextListener<ArrayList<GroupBean>>() {
                    @Override
                    public void onNext(ArrayList<GroupBean> list, String msg) {
                        DataManager.getInstance().saveGroups(list);
                        mGroupList = list;
                        setData(DataManager.getInstance().getFriends(), mGroupList);
                        mIsGetGroup = false;
                    }
                }, getActivity(), false, new OnHttpErrorListener() {
                    @Override
                    public void onConnectError(Throwable e) {
                        mIsGetGroup = false;
                    }

                    @Override
                    public void onServerError(int errorCode, String errorMsg) {
                        mIsGetGroup = false;
                        if (errorCode == 401) {
                            ChatManager.getInstance().showLoginOutDialog();
                        }
                    }
                }));
    }

    public void setData(ArrayList<UserBean> friendList, ArrayList<GroupBean> groupList) {
    }

}
