package com.alsc.chat.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.adapter.MessageAdapter;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.OkHttpClientManager;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.manager.MediaplayerManager;
import com.alsc.chat.manager.UPYFileUploadManger;
import com.alsc.chat.utils.BitmapUtil;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.SoftKeyBoardListener;
import com.alsc.chat.utils.Utils;
import com.alsc.chat.view.RecorderView;
import com.alsc.chat.view.wheelview.adapter.ArrayWheelAdapter;
import com.alsc.chat.view.wheelview.listener.OnItemSelectedListener;
import com.alsc.chat.view.wheelview.view.WheelView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemChildLongClickListener;
import com.chs.filepicker.filepicker.FilePickerActivity;
import com.chs.filepicker.filepicker.PickerManager;
import com.chs.filepicker.filepicker.model.FileEntity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhangke.websocket.WebSocketHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//https://blog.csdn.net/happy_love1990/article/details/78327161

public class ChatFragment extends ChatBaseFragment {

    private static final String TAG = "Chat";

    private static final int FILE_SELECT_CODE = 10001;
    private static final int ALBUM_PHOTO_REQUEST_CODE = 10002;
    private static final int ALBUM_VIDEO_REQUEST_CODE = 10003;

    private static final long FILE_MAX_SIZE = 50 * 1024 * 1024;  //文件最大size
    private static final long PHOTO_MAX_SIZE = 2 * 1024 * 1024;  //图片最大size

    protected MessageAdapter mAdapter;
    protected UserBean mMyInfo;
    protected UserBean mChatUser;

    protected boolean mHasMore;
    protected long mLastMsgTime;

    private int mInputMode;  //0表示文字，1表示录音

    private long mStartVoiceTime;

    protected int mKeyBoardHeight;
    protected boolean mIsKeyBordShow;

    private HashMap<Long, Integer> mMsgDeleteTypes;

    private int mReadDeleteType;  //0表示关闭，1表示后15分钟，2表示后1小时，3表示1天，4表示1周

    protected BasicMessage mSelectedMsg;

    protected boolean mIsTapAddBtn;

    private long mLastUploadFileTime;

    private Gson mGson;

    private boolean mIsCanCacelRecord;

    private ArrayList<HashMap<String, Object>> mList;

    protected ArrayList<UserBean> mAtGroupMembers;

    protected Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        mMyInfo = DataManager.getInstance().getUser();
        mChatUser = (UserBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA);
        setText(R.id.tvName, mChatUser.getNickName());
        if (mChatUser.getUserId() == -1) {
            setImage(R.id.ivAvatar, R.drawable.chat_chat_gpt);
            setViewGone(R.id.ivVoice, R.id.ivAdd, R.id.ivMore);
        } else {
            int resId = getResources().getIdentifier("chat_default_avatar_" + mChatUser.getUserId() % 6,
                    "drawable", getActivity().getPackageName());
            Utils.loadImage(getActivity(), resId, mChatUser.getAvatarUrl(), fv(R.id.ivAvatar));
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        init(view);
    }

    void init(View view) {
        initListeners();
//        getAdapter().setOnItemChildLongClickListener(new OnItemChildLongClickListener() {
//            @Override
//            public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
//                if (!getAdapter().isEditMode()) {
//                    showMsgMoreDialog(getAdapter().getItem(position), view);
//                }
//                return false;
//            }
//        });
        mSelectedMsg = (BasicMessage) getArguments().getSerializable(Constants.BUNDLE_EXTRA_2);
        setViewsOnClickListener(R.id.ivSend, R.id.ivVoice, R.id.ivAdd,
                R.id.llAlbum, R.id.llCamera, R.id.ivMore,
                R.id.llVideo, R.id.llFile, R.id.llDeleteAfterRead, R.id.ivMsgDeleteType);

        initMsgs();
        initEvent();
        initScrollListener();
        view.findViewById(R.id.tvRecord).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mStartVoiceTime = System.currentTimeMillis();
                        showRecordPopWindow();
                        mIsCanCacelRecord = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mStartVoiceTime == 0) {
                            break;
                        }
                        if (isTouchPointAboveView(v, event.getRawY())) {
                            mIsCanCacelRecord = true;
                            RecorderView recorderView = fv(R.id.recordView);
                            recorderView.setShowAll(true);
                            recorderView.showIcon((int) event.getRawY());
                        } else {
                            RecorderView recorderView = fv(R.id.recordView);
                            recorderView.setShowAll(false);
                            mIsCanCacelRecord = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        long time = System.currentTimeMillis() - mStartVoiceTime;
                        RecorderView recorderView = fv(R.id.recordView);
                        recorderView.stopRecord();
                        if (TextUtils.isEmpty(recorderView.getRecordFile())) {
                            break;
                        }
                        if (mIsCanCacelRecord) {
                            recorderView.deleteRecord();
                            break;
                        }
                        File file = new File(recorderView.getRecordFile());
                        FileBean bean = new FileBean();
                        bean.setFile(file);
                        bean.setType(MessageType.TYPE_VOICE);
                        HashMap<String, String> map = new HashMap<>();
                        map.put("time", String.valueOf(time));
                        map.put("fileSize", String.valueOf(file.length()));
                        map.put("fileName", file.getName());
                        bean.setExtra(map);
                        EventBus.getDefault().post(bean);
                        MediaplayerManager.getInstance().playVoice(getActivity(), R.raw.chat_send_voice_sound, false);
                        break;
                }
                return true;
            }
        });
    }

    protected void initListeners() {
        getAdapter().setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                BasicMessage msg = getAdapter().getItem(position);
                if (msg.getMsgType() == MessageType.TYPE_SELECT_SERVICE.ordinal()
                        || msg.getMsgType() == MessageType.TYPE_QUESTION.ordinal()) {
                    return;
                }
                if (getAdapter().isEditMode()) {
                    msg.setCheck(!msg.isCheck());
                    ImageView iv = view.findViewById(R.id.ivCheck);
                    iv.setImageResource(msg.isCheck() ? R.drawable.icon_box_selected : R.drawable.icon_box_unselected);
                } else {
                    if (view.getId() == R.id.ivResend) {
                        getAdapter().deleteMsg(msg);
                        msg.setSendStatus(0);
                        msg.setCreateTime(System.currentTimeMillis());
                        if (msg.getExpire() > 0) {
                            msg.setExpire(msg.getCreateTime() + getExpreTimeByType());
                        }
                        int fileType = msg.getMsgType();
                        File file = null;
                        if (fileType == MessageType.TYPE_IMAGE.ordinal()
                                || fileType == MessageType.TYPE_VOICE.ordinal()
                                || fileType == MessageType.TYPE_VIDEO.ordinal()
                                || fileType == MessageType.TYPE_LOCATION.ordinal()
                                || fileType == MessageType.TYPE_FILE.ordinal()) {
                            if (!TextUtils.isEmpty(msg.getContent())) {
                                try {
                                    JSONObject jsonObject = new JSONObject(msg.getContent());
                                    String fileName = jsonObject.optString("fileName");
                                    file = new File(Utils.getSaveFilePath(getActivity(), fileName));
                                } catch (Exception e) {

                                }
                            }
                        }
                        if (file != null && file.exists()) {
                            FileBean fileBean = new FileBean();
                            fileBean.setFile(file);
                            UPYFileUploadManger.getInstance().formUpload(msg, fileBean);
                        } else {
                            if (mChatUser != null && mChatUser.getUserId() == -1) {
                                WebSocketHandler.getDefault().sendChatGPT(msg);
                            } else {
                                WebSocketHandler.getDefault().send(msg.toJson());
                            }
                        }
                        DatabaseOperate.getInstance().insert(msg);
                        EventBus.getDefault().post(msg);
                    } else {
                        showMsg(view, getAdapter().getItem(position));
                    }
                }
            }
        });
    }


    private boolean isTouchPointAboveView(View view, float rawY) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int top = location[1];
        return rawY < top;
    }

    protected long getChatId() {
        return mChatUser.getContactId();
    }

    private void showRecordPopWindow() {
        if (!Utils.isGrantPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || !Utils.isGrantPermission(getActivity(), Manifest.permission.RECORD_AUDIO)) {
            ((BaseActivity) getActivity()).requestPermission(null,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
            mStartVoiceTime = 0l;
            return;
        }
        RecorderView recorderView = fv(R.id.recordView);
        recorderView.startRecode(Utils.getSaveFilePath(getActivity(), UUID.randomUUID().toString() + ".mp3"));
    }

    protected void initEvent() {
        mInputMode = 0;
        mIsTapAddBtn = false;
        mIsKeyBordShow = false;
        EditText etChat = getView().findViewById(R.id.etChat);
        View ivSend = fv(R.id.ivSend);
        ivSend.setAlpha(0.5f);
        ivSend.setEnabled(false);
        etChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (getView() == null) {
                    return;
                }
                String text = s.toString();
                if (text.trim().length() > 0) {
                    ivSend.setAlpha(1f);
                    ivSend.setEnabled(true);
                } else {
                    ivSend.setAlpha(0.5f);
                    ivSend.setEnabled(false);
                }
                atGroupMember(text);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        SoftKeyBoardListener.setListener(getActivity(), new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {

            public void keyBoardShow(int height) {
                DataManager.getInstance().saveKeyboardHeight(height);
                mIsKeyBordShow = true;
                mKeyBoardHeight = height;
                if (getView() == null) {
                    return;
                }
                View llBottomTab = fv(R.id.llBottomTab);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) llBottomTab.getLayoutParams();
                lp.height = height;
                llBottomTab.setLayoutParams(lp);
                mIsTapAddBtn = false;
                scrollBottom();
            }

            public void keyBoardHide(int height) {
                mIsKeyBordShow = false;
                if (getView() == null) {
                    return;
                }
                if (!mIsTapAddBtn) {
                    View llBottomTab = fv(R.id.llBottomTab);
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) llBottomTab.getLayoutParams();
                    lp.height = 0;
                    llBottomTab.setLayoutParams(lp);
                }
                mIsTapAddBtn = false;
            }
        });
    }

    protected void atGroupMember(String text) {

    }


    protected void initMsgs() {
        DatabaseOperate.getInstance().setAllMsgRead(mMyInfo.getUserId(), mChatUser.getContactId());
        ArrayList<MessageBean> list;
        if (mSelectedMsg != null) {
            list = DatabaseOperate.getInstance().getAllUserChatMsg(mMyInfo.getUserId(), mChatUser.getContactId());
        } else {
            list = DatabaseOperate.getInstance().getUserChatMsg(mMyInfo.getUserId(), mChatUser.getContactId(), 0l, Constants.PAGE_NUM);
        }
        Collections.reverse(list);
        if (mSelectedMsg == null) {
            int size = list.size();
            mHasMore = (size == Constants.PAGE_NUM);
            if (size > 0) {
                mLastMsgTime = list.get(0).getCreateTime();
            }
            ArrayList<BasicMessage> messages = new ArrayList<>();
            messages.addAll(list);
            getAdapter().setNewData(messages);
        } else {
            mHasMore = false;
            ArrayList<BasicMessage> messages = new ArrayList<>();
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
    }

    protected void initScrollListener() {
        final RecyclerView recyclerView = fv(R.id.recyclerView);
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                final LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
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
        View llBottomTab = fv(R.id.llBottomTab);
        llBottomTab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mIsKeyBordShow) {
                    hideKeyBoard(fv(R.id.etChat));
                } else {
                    View llBottomTab = fv(R.id.llBottomTab);
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) llBottomTab.getLayoutParams();
                    lp.height = 0;
                    llBottomTab.setLayoutParams(lp);
                }
                return false;
            }
        });
    }

    protected void getMoreMessage() {
        if (mHasMore) {
            ArrayList<MessageBean> list = DatabaseOperate.getInstance().getUserChatMsg(mMyInfo.getUserId(), mChatUser.getContactId(),
                    mLastMsgTime, Constants.PAGE_NUM);
            getAdapter().addData(0, list);
            mHasMore = false;
        }
    }

    protected MessageAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new MessageAdapter(getActivity(), mMyInfo);
            mAdapter.setChatUser(mChatUser);
        }
        return mAdapter;
    }

    void showMsg(View view, BasicMessage message) {
        int type = message.getMsgType();
        if (type == MessageType.TYPE_TEXT.ordinal()
                || type == MessageType.TYPE_VOICE.ordinal()) {
            return;
        }
        if (type == MessageType.TYPE_RED_PACKAGE.ordinal()
                || type == MessageType.TYPE_NEW_MEMBER_RED_PACKAGE.ordinal()) {
            EnvelopeBean bean = getGson().fromJson(message.getContent(), EnvelopeBean.class);
            getRedPackageDetail(message, bean.getEnvelopeId());
        } else if (type == MessageType.TYPE_TRANSFER.ordinal() || type == MessageType.TYPE_RECEIVE_TRANSFER.ordinal()) {
            EnvelopeBean bean = getGson().fromJson(message.getContent(), EnvelopeBean.class);
            getRedPackageDetail(message, bean.getEnvelopeId());
        } else if (type == MessageType.TYPE_RECOMAND_USER.ordinal()) {
            UserBean bean = getGson().fromJson(message.getContent(), UserBean.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, bean);
            gotoPager(bean.getUserId() == DataManager.getInstance().getUserId() ? MyInfoFragment.class : UserInfoFragment.class, bundle);
        } else if (type == MessageType.TYPE_RECEIVE_RED_PACKAGE.ordinal()) {
            if (!isCanAddFriend()) {  //禁止加好友，不能查看群员信息
                return;
            }
            int id = view.getId();
            if (id == R.id.tvName1 || id == R.id.tvName2) {
                ArrayList<UserBean> users = getGson().fromJson(message.getExtra(), new TypeToken<ArrayList<UserBean>>() {
                }.getType());

                for (UserBean bean : users) {
                    if (bean.getUserId() != mMyInfo.getUserId()) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, bean);
                        gotoPager(UserInfoFragment.class, bundle);
                        break;
                    }
                }
            } else if (view.getId() == R.id.tvRedPackage) {
                EnvelopeBean bean = getGson().fromJson(message.getContent(), EnvelopeBean.class);
                getRedPackageDetail(message, bean.getEnvelopeId());
            }
        } else if (type == MessageType.TYPE_INVITE_PAY_IN_GROUP.ordinal()) {
            if (!TextUtils.isEmpty(message.getExtra())) {
                GroupInviteBean bean = getGson().fromJson(message.getExtra(), GroupInviteBean.class);
                if (isInGroup(bean.getGroup().getGroupId())) {
                    showToast(R.string.chat_you_had_in_group);
                    return;
                }
                showPayInGroupDialog(bean.getGroup(), mChatUser);
            }
        } else {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, ShowMessageFragment.TYPE_SHOW_CHAT_MESSAGE);
            bundle.putSerializable(Constants.BUNDLE_EXTRA_2, message);
            if (message.getMsgType() == MessageType.TYPE_IMAGE.ordinal()
                    || message.getMsgType() == MessageType.TYPE_VIDEO.ordinal()) {
                bundle.putSerializable(Constants.BUNDLE_EXTRA_3, getAdapter().getAllMediaMsgs());
            }
            gotoPager(ShowMessageFragment.class, bundle);
        }
    }

    @Override
    public void updateUIText() {
        mMyInfo = DataManager.getInstance().getUser();
        getAdapter().setMyInfo(mMyInfo);
        mMsgDeleteTypes = DataManager.getInstance().getMsgDeleteType();
        if (mMsgDeleteTypes.containsKey(getChatId())) {
            mReadDeleteType = mMsgDeleteTypes.get(getChatId());
            if (mReadDeleteType == 0) {
                ChatSettingBean bean = DataManager.getInstance().getChatSetting();
                mReadDeleteType = bean.getReadDeleteType();
            }
        }

        ImageView ivMsgDeleteType = fv(R.id.ivMsgDeleteType);
        if (mReadDeleteType == 0) {
            ivMsgDeleteType.setVisibility(View.GONE);
        } else {
            ivMsgDeleteType.setVisibility(View.VISIBLE);
            int drawableId = getResources().getIdentifier("chat_delete_type_" + mReadDeleteType, "drawable", getActivity().getPackageName());
            ivMsgDeleteType.setImageResource(drawableId);
        }
    }

    private void saveDeleteMsgType() {
        mMsgDeleteTypes.put(getChatId(), mReadDeleteType);
        DataManager.getInstance().saveMsgDeleteType(mMsgDeleteTypes);
    }

    protected void showMsgMoreDialog(final BasicMessage msg, final View locationView) {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_msg_more_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                int msgType = msg.getMsgType();
                if (msgType != MessageType.TYPE_TEXT.ordinal()
                        && msgType != MessageType.TYPE_GROUP_AT_MEMBER_MSG.ordinal()) {
                    view.findViewById(R.id.tvTranslate).setVisibility(View.GONE);
                    view.findViewById(R.id.tvCopy).setVisibility(View.GONE);
                    if (msgType == MessageType.TYPE_IMAGE.ordinal()
                            || msgType == MessageType.TYPE_VIDEO.ordinal()) {
                        view.findViewById(R.id.tvSave).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.tvCollect).setVisibility(View.VISIBLE);
                    } else if (msgType == MessageType.TYPE_NEW_MEMBER_RED_PACKAGE.ordinal() ||
                            msgType == MessageType.TYPE_RED_PACKAGE.ordinal() ||
                            msgType == MessageType.TYPE_TRANSFER.ordinal() ||
                            msgType == MessageType.TYPE_RECEIVE_RED_PACKAGE.ordinal() ||
                            msgType == MessageType.TYPE_RECEIVE_TRANSFER.ordinal()) {
                        view.findViewById(R.id.tvSendToFriend).setVisibility(View.GONE);
                        view.findViewById(R.id.tvMutiChoose).setVisibility(View.GONE);
                    } else if (msgType == MessageType.TYPE_VOICE.ordinal()
                            || msgType == MessageType.TYPE_FILE.ordinal()) {
                        view.findViewById(R.id.tvCollect).setVisibility(View.VISIBLE);
                    }
                } else {
                    view.findViewById(R.id.tvCollect).setVisibility(View.VISIBLE);
                }
                if (TextUtils.isEmpty(msg.getMessageId()) || TextUtils.isEmpty(msg.getExtra())) {
                    view.findViewById(R.id.tvCollect).setVisibility(View.GONE);
                }
                dialogFragment.setDialogViewsOnClickListener(view, R.id.tvCopy, R.id.tvSendToFriend,
                        R.id.tvMutiChoose, R.id.tvTranslate, R.id.tvDelete, R.id.ll, R.id.tvSave, R.id.view,
                        R.id.tvCollect);
                view.findViewById(R.id.paddingView).setVisibility(msg.isMySendMsg(mMyInfo.getUserId()) ? View.VISIBLE : View.GONE);
                View llRoot = view.findViewById(R.id.llRoot);
                int heigth = Utils.dip2px(getActivity(), 237);
                int screenHeight = ((BaseActivity) getActivity()).getDisplayMetrics().heightPixels;
                int bottom = Utils.dip2px(getActivity(), 50);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) llRoot.getLayoutParams();
                int[] location = new int[2];
                locationView.getLocationOnScreen(location);
                if (heigth + location[1] > screenHeight - bottom) {
                    lp.topMargin = location[1] - heigth;
                } else {
                    lp.topMargin = location[1];
                }
                llRoot.setLayoutParams(lp);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.tvCopy) {
                    ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("Label", msg.getContent());
                    cm.setPrimaryClip(mClipData);
                    showToast(R.string.chat_copy_successful);
                } else if (viewId == R.id.tvSendToFriend) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, msg);
                    gotoPager(TransferMsgFragment.class, bundle);
                } else if (viewId == R.id.tvMutiChoose) {
                    getAdapter().resetEditMode();
                    setViewGone(R.id.ivRight);
                    setViewVisible(R.id.btnRight);
                } else if (viewId == R.id.tvDelete) {
                    if (msg.isMySendMsg(DataManager.getInstance().getUserId())) {
                        showDeleteMsgDialog(msg);
                    } else {
                        getAdapter().deleteMsg(msg);
                    }
                } else if (viewId == R.id.tvSave) {
                    try {
                        final JSONObject jsonObject = new JSONObject(msg.getContent());
                        String fileName = jsonObject.optString("fileName");
                        String filePath = Utils.getSaveFilePath(getActivity(), fileName);
                        File file = new File(filePath);
                        if (file.exists()) {
                            Utils.copyMediaToAlbum(getActivity(), msg.getMsgType() == MessageType.TYPE_IMAGE.ordinal() ? 0 : 1, filePath);
                            showToast(R.string.chat_save_success_and_look_in_album);
                        } else {
                            if (msg.getMsgType() == MessageType.TYPE_VIDEO.ordinal()) {
                                showToast(R.string.chat_save_success_and_look_in_album);
                                return;
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    FutureTarget future = Glide.with(getActivity())
                                            .load(msg.getUrl())
                                            .downloadOnly(jsonObject.optInt("width"), jsonObject.optInt("height"));
                                    try {
                                        File cacheFile = (File) future.get();
                                        String path = cacheFile.getAbsolutePath();
                                        Utils.copyMediaToAlbum(getActivity(), 0, path);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showToast(R.string.chat_save_success_and_look_in_album);
                                        }
                                    });
                                }
                            }).start();
                        }
                    } catch (Exception e) {
                    }
                } else if (viewId == R.id.tvCollect) {
                    ChatHttpMethods.getInstance().addCollection(msg.getMessageId(), getGson().toJson(msg),
                            new HttpObserver(new SubscriberOnNextListener<Object>() {
                                @Override
                                public void onNext(Object o, String msg) {
                                    if (getView() == null) {
                                        return;
                                    }
                                    showToast(R.string.chat_collect_success);
                                }
                            }, getActivity(), false, (ChatBaseActivity) getActivity()));
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    private void showDeleteMsgDialog(final BasicMessage msg) {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_delete_msg_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                view.findViewById(R.id.ivCheck).setTag(false);
                ((TextView) view.findViewById(R.id.tv3)).setText(getString(mChatUser == null ? R.string.chat_delete_msg_tip_3 : R.string.chat_delete_msg_tip_2));
                int msgType = msg.getMsgType();
                view.findViewById(R.id.ll).setVisibility((msgType == MessageType.TYPE_RED_PACKAGE.ordinal()
                        || msgType == MessageType.TYPE_TRANSFER.ordinal()
                        || msgType == MessageType.TYPE_RECEIVE_TRANSFER.ordinal()
                        || msgType == MessageType.TYPE_RECEIVE_RED_PACKAGE.ordinal()
                        || msgType == MessageType.TYPE_NEW_MEMBER_RED_PACKAGE.ordinal()) ? View.GONE : View.VISIBLE);
                dialogFragment.setDialogViewsOnClickListener(view,
                        R.id.ll, R.id.btn1, R.id.btn2);
                dialogFragment.setClickDismiss(false);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.ll) {
                    ImageView ivCheck = dialogFragment.getView().findViewById(R.id.ivCheck);
                    boolean isCheck = !((boolean) ivCheck.getTag());
                    ivCheck.setTag(isCheck);
                    ivCheck.setImageResource(isCheck ? R.drawable.icon_box_selected : R.drawable.icon_box_unselected);
                } else if (viewId == R.id.btn1) {
                    getAdapter().deleteMsg(msg);
                    ImageView ivCheck = dialogFragment.getView().findViewById(R.id.ivCheck);
                    if ((boolean) ivCheck.getTag()) {
                        BasicMessage bean = getMsg();
                        bean.setMsgType(MessageType.TYPE_DELETE_MSG.ordinal());
                        bean.setContent(msg.getMessageId());
                        WebSocketHandler.getDefault().send(bean.toJson());
                    }
                    dialogFragment.dismiss();
                } else if (viewId == R.id.btn2) {
                    dialogFragment.dismiss();
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }


    protected boolean isFilterLetter(String text) {
        return false;
    }

    protected boolean isFilterUrl(String text) {
        return false;
    }

    protected boolean isCanAddFriend() {  //群禁止互加好友后不能点击头像
        return false;
    }

    protected boolean isForbidChat() {  //是否被禁言
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivSend) {
            String text = getTextById(R.id.etChat);
            if (TextUtils.isEmpty(text.trim())) {
                return;
            }
            BasicMessage msg = getMsg();
//            if (isFilterLetter(text)) {
//                msg.setMsgType(MessageType.TYPE_TEXT.ordinal());
//                msg.setContent(getString(R.string.chat_include_illeage_letter));
//            } else if (isFilterUrl(text)) {
//                msg.setMsgType(MessageType.TYPE_TEXT.ordinal());
//                msg.setContent(getString(R.string.chat_include_illeage_url));
//            } else {
            if (mAtGroupMembers == null || mAtGroupMembers.isEmpty()) {
                msg.setMsgType(MessageType.TYPE_TEXT.ordinal());
            } else {
                msg.setMsgType(MessageType.TYPE_GROUP_AT_MEMBER_MSG.ordinal());
                //               msg.setExtra(getGson().toJson(mAtGroupMembers));
            }
            msg.setContent(text);
            //          }
            msg.setExpire(mReadDeleteType == 0 ? 0l : msg.getCreateTime() + getExpreTimeByType());
            if (WebSocketHandler.getDefault() != null) {
                if (mChatUser != null && mChatUser.getUserId() == -1) {
                    WebSocketHandler.getDefault().sendChatGPT(msg);
                } else {
                    WebSocketHandler.getDefault().send(msg.toJson());
                }
            }
            setText(R.id.etChat, "");
            DatabaseOperate.getInstance().insert(msg);
            EventBus.getDefault().post(msg);
        } else if (id == R.id.ivMore) {
            goDetailClass();
        } else if (id == R.id.btnRight) {
            getAdapter().resetEditMode();
            setViewGone(R.id.btnRight);
            setViewVisible(R.id.ivRight);
            getAdapter().deleteCheckMsg();
        } else if (id == R.id.ivMsgDeleteType) {
            View llBottomTab = fv(R.id.llBottomTab);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) llBottomTab.getLayoutParams();
            if (mIsKeyBordShow || lp.height == 0) {
                if (mKeyBoardHeight <= 0) {
                    mKeyBoardHeight = (int) (((BaseActivity) getActivity()).getDisplayMetrics().heightPixels * 0.4);
                }
                hideKeyBoard(fv(R.id.etChat));
                lp.height = mKeyBoardHeight;
                setViewVisible(R.id.llSelectDeleteTime);
                setViewGone(R.id.ll1, R.id.ll2);
                showSelectDeleteMsgTimeView();
            } else {
                lp.height = 0;
            }
            llBottomTab.setLayoutParams(lp);
            saveDeleteMsgType();
        } else if (id == R.id.ivAdd) {
            mIsTapAddBtn = true;
            mInputMode = 0;
            setViewVisible(R.id.llChat);
            setViewGone(R.id.tvRecord);
            if (mIsKeyBordShow) {
                hideKeyBoard(fv(R.id.etChat));
            } else {
                View llBottomTab = fv(R.id.llBottomTab);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) llBottomTab.getLayoutParams();
                View llSelectDeleteTime = fv(R.id.llSelectDeleteTime);
                if (lp.height == 0) {
                    if (mKeyBoardHeight <= 0) {
                        mKeyBoardHeight = DataManager.getInstance().getKeyboardHeight();
                    }
                    if (mKeyBoardHeight <= 0) {
                        mKeyBoardHeight = (int) (((BaseActivity) getActivity()).getDisplayMetrics().heightPixels * 0.4);
                    }
                    lp.height = mKeyBoardHeight;
                    setViewGone(R.id.llSelectDeleteTime);
                    setViewVisible(R.id.ll1, R.id.ll2);
                    llBottomTab.setLayoutParams(lp);
                } else {
                    if (llSelectDeleteTime.getVisibility() == View.VISIBLE) {
                        setViewGone(R.id.llSelectDeleteTime);
                        setViewVisible(R.id.ll1, R.id.ll2);
                    } else {
                        EditText et = fv(R.id.etChat);
                        et.setFocusable(true);
                        et.setFocusableInTouchMode(true);
                        et.requestFocus();
                        showKeyBoard(et);
                        String text = et.getText().toString();
                        et.setSelection(text.length());
                        et.setCursorVisible(true);
                        et.requestFocus();
                    }
                }
            }
            saveDeleteMsgType();
        } else if (id == R.id.llAlbum) {
            if (!Utils.isGrantPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ((BaseActivity) getActivity()).requestPermission(null, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                showSelectMediaDialog();
            }
        } else if (id == R.id.llCamera) {
            if (!Utils.isGrantPermission(getActivity(),
                    Manifest.permission.CAMERA)) {
                ((BaseActivity) getActivity()).requestPermission(null, Manifest.permission.CAMERA);
            } else {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, CameraFragment.FOR_CHAT_PHOTO);
                gotoPager(CameraFragment.class, bundle);
            }
        } else if (id == R.id.llVideo) {
            if (!Utils.isGrantPermission(getActivity(),
                    Manifest.permission.CAMERA)) {
                ((BaseActivity) getActivity()).requestPermission(null, Manifest.permission.CAMERA);
            } else {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, CameraFragment.FOR_CHAT_VIDEO);
                gotoPager(CameraFragment.class, bundle);
            }
        } else if (id == R.id.llFile) {
            showFileChooser();
        } else if (id == R.id.llDeleteAfterRead) {
            setViewVisible(R.id.llSelectDeleteTime);
            setViewGone(R.id.ll1, R.id.ll2);
            showSelectDeleteMsgTimeView();
        } else if (id == R.id.ivVoice) {
            if (mInputMode == 0) {
                mInputMode = 1;
                setViewGone(R.id.llChat);
                setViewVisible(R.id.tvRecord);
                hideKeyBoard(fv(R.id.etChat));
                View llBottomTab = fv(R.id.llBottomTab);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) llBottomTab.getLayoutParams();
                lp.height = 0;
                llBottomTab.setLayoutParams(lp);
            } else {
                mInputMode = 0;
                setViewVisible(R.id.llChat);
                setViewGone(R.id.tvRecord);
                EditText et = fv(R.id.etChat);
                et.setFocusable(true);
                et.setFocusableInTouchMode(true);
                et.requestFocus();
                showKeyBoard(et);
                String text = et.getText().toString();
                et.setSelection(text.length());
                et.setCursorVisible(true);
                et.requestFocus();
            }
        }
    }

    private void showSelectMediaDialog() {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_select_media_type);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btnVideo, R.id.btnPhoto, R.id.btnCancel);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.btnPhoto) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, ALBUM_PHOTO_REQUEST_CODE);
                } else if (viewId == R.id.btnVideo) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("video/*");
                        startActivityForResult(intent, ALBUM_VIDEO_REQUEST_CODE);
                    } catch (Exception e) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("video/*");
                        startActivityForResult(intent, ALBUM_VIDEO_REQUEST_CODE);
                    }
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    protected void goSendRedPackage() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.BUNDLE_EXTRA, mChatUser);
        gotoPager(SendRedPackageFragment.class, bundle);
    }

    private void showFileChooser() {
        PickerManager.getInstance().files.clear();
        Intent intent = new Intent(getActivity(), FilePickerActivity.class);
        startActivityForResult(intent, FILE_SELECT_CODE);
    }

    protected BasicMessage getMsg() {
        MessageBean msg = new MessageBean();
        msg.setCmd(mMyInfo.isService() ? 2200 : 2000);  //客服发的消息全是2200
        msg.setFromId(mMyInfo.getUserId());
        msg.setToId(mChatUser.getContactId());
        msg.setExtra(getGson().toJson(getUsers()));
        return msg;
    }

    protected ArrayList<HashMap<String, Object>> getUsers() {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        if (mList.isEmpty()) {
            mList.add(mMyInfo.toMap());
            if (mChatUser != null) {
                mList.add(mChatUser.toMap());
            }
        }
        return mList;
    }

    protected void goDetailClass() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.BUNDLE_EXTRA, mChatUser);
        gotoPager(ChatDetailFragment.class, bundle);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMsg(MessageBean message) {
        if (getView() != null && message != null && isChatMsg(message)) {
            if (message.getFromId() == mChatUser.getContactId()) {
                message.readMsg();
            }
            getAdapter().addData(message);
            scrollBottom();
        }
    }

    protected boolean isChatMsg(MessageBean message) {
        return (message.getFromId() == mMyInfo.getUserId() && message.getToId() == mChatUser.getContactId()) ||
                (message.getToId() == mMyInfo.getUserId() && message.getFromId() == mChatUser.getContactId());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveFile(FileBean fileBean) {
        if (System.currentTimeMillis() - mLastUploadFileTime < 1000) {
            return;
        }
        mLastUploadFileTime = System.currentTimeMillis();
        if (getView() != null && fileBean != null) {
            BasicMessage msg = getMsg();
            MessageType fileType = fileBean.getType();
            File file = fileBean.getFile();
            if (fileType == MessageType.TYPE_IMAGE) {
                msg.setMsgType(MessageType.TYPE_IMAGE.ordinal());
                msg.setUrl(Constants.IMAGE_HTTP_HOST + String.format(Constants.IMAGE_SAVE_PATH, file.getName()));
            } else if (fileType == MessageType.TYPE_VOICE) {
                msg.setMsgType(MessageType.TYPE_VOICE.ordinal());
                msg.setUrl(Constants.IMAGE_HTTP_HOST + String.format(Constants.VOICE_SAVE_PATH, file.getName()));
            } else if (fileType == MessageType.TYPE_VIDEO) {
                msg.setMsgType(MessageType.TYPE_VIDEO.ordinal());
                msg.setUrl(Constants.IMAGE_HTTP_HOST + String.format(Constants.VIDEO_SAVE_PATH, file.getName()));
            } else if (fileType == MessageType.TYPE_LOCATION) {
                msg.setMsgType(MessageType.TYPE_LOCATION.ordinal());
                msg.setUrl(Constants.IMAGE_HTTP_HOST + String.format(Constants.MAP_SAVE_PATH, file.getName()));
            } else if (fileType == MessageType.TYPE_FILE) {
                msg.setMsgType(MessageType.TYPE_FILE.ordinal());
                msg.setUrl(Constants.IMAGE_HTTP_HOST + String.format(Constants.FILE_SAVE_PATH, file.getName()));
            }
            msg.setContent(getGson().toJson(fileBean.getExtra()));
            msg.setExpire(mReadDeleteType == 0 ? 0l : msg.getCreateTime() + getExpreTimeByType());
            DatabaseOperate.getInstance().insert(msg);
            EventBus.getDefault().post(msg);
            UPYFileUploadManger.getInstance().formUpload(msg, fileBean);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (getView() != null && map != null) {
            if (map.containsKey(Constants.RED_PACKAGE)
                    || map.containsKey(Constants.NEW_MEMBER_RED_PACKAGE)
                    || map.containsKey(Constants.TRANSFER)) {
                if (System.currentTimeMillis() - mLastUploadFileTime < 1000) {
                    return;
                }
                mLastUploadFileTime = System.currentTimeMillis();
            }
            if (map.containsKey(Constants.CLEAR_MESSAGE)) {
                getAdapter().clearMsg();
            } else if (map.containsKey(Constants.RED_PACKAGE)) {
                String content = (String) map.get(Constants.RED_PACKAGE);
                BasicMessage msg = getMsg();
                msg.setMsgType(MessageType.TYPE_RED_PACKAGE.ordinal());
                msg.setContent(content);
                if (WebSocketHandler.getDefault() != null) {
                    WebSocketHandler.getDefault().send(msg.toJson());
                }
                DatabaseOperate.getInstance().insert(msg);
                EventBus.getDefault().post(msg);
            } else if (map.containsKey(Constants.NEW_MEMBER_RED_PACKAGE)) {
                String content = (String) map.get(Constants.NEW_MEMBER_RED_PACKAGE);
                BasicMessage msg = getMsg();
                msg.setMsgType(MessageType.TYPE_NEW_MEMBER_RED_PACKAGE.ordinal());
                msg.setContent(content);
                //               msg.setExpire(mReadDeleteType == 0 ? 0l : msg.getCreateTime() + getExpreTimeByType());
                if (WebSocketHandler.getDefault() != null) {
                    WebSocketHandler.getDefault().send(msg.toJson());
                }
                DatabaseOperate.getInstance().insert(msg);
                EventBus.getDefault().post(msg);
            } else if (map.containsKey(Constants.TRANSFER)) {
                String content = (String) map.get(Constants.TRANSFER);
                BasicMessage msg = getMsg();
                msg.setMsgType(MessageType.TYPE_TRANSFER.ordinal());
                msg.setContent(content);
                if (WebSocketHandler.getDefault() != null) {
                    WebSocketHandler.getDefault().send(msg.toJson());
                }
                DatabaseOperate.getInstance().insert(msg);
                EventBus.getDefault().post(msg);
            } else if (map.containsKey(Constants.EDIT_FRIEND)) {
                UserBean userBean = (UserBean) map.get(Constants.EDIT_FRIEND);
                mChatUser = userBean;
                getAdapter().setChatUser(mChatUser);
            } else if (map.containsKey(Constants.REMOVE_FRIEND)) {
                long userId = (long) map.get(Constants.REMOVE_FRIEND);
                if (userId == mChatUser.getContactId()) {
                    finish();
                }
            } else if (map.containsKey(Constants.SEND_MSG_FAILED)) {
                String msgId = (String) map.get(Constants.SEND_MSG_FAILED);
                getAdapter().setMsgFailed(msgId);
            } else if (map.containsKey(Constants.SEND_MSG_SUCCESS)) {
                String msgId = (String) map.get(Constants.SEND_MSG_SUCCESS);
                getAdapter().setMsgSuccess(msgId);
            } else if (map.containsKey(Constants.RECEIVE_ENVELOPE)) {
                String msgId = (String) map.get(Constants.RECEIVE_ENVELOPE);
                getAdapter().receiveEnvelope(msgId);
            } else if (map.containsKey(Constants.END_GROUP)) {
                long groupId = (long) map.get(Constants.END_GROUP);
                if (mChatUser == null && groupId == getChatId()) {//在群聊中
                    getAdapter().clearMsg();
                    finish();
                }
            } else if (map.containsKey(Constants.DELETE_CHAT_MESSAGE)) {
                String messageId = (String) map.get(Constants.DELETE_CHAT_MESSAGE);
                getAdapter().deleteMsg(messageId);
            }
        }
    }


    protected void scrollBottom() {
        if (getView() != null && getAdapter().getItemCount() > 0) {
            RecyclerView recyclerView = getView().findViewById(R.id.recyclerView);
            recyclerView.smoothScrollToPosition(getAdapter().getItemCount() - 1);
        }
    }

    public void onPause() {
        super.onPause();
        if (mMsgDeleteTypes != null) {
            saveDeleteMsgType();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MediaplayerManager.getInstance().releaseMediaPlayer();
    }

    private void showSelectDeleteMsgTimeView() {
        WheelView wheelView = fv(R.id.wheelView);
        if (wheelView.getAdapter() == null) {
            wheelView.setCyclic(false);
            final ArrayList<String> mOptionsItems = new ArrayList<>();
            int strId;
            for (int i = 0; i <= 4; ++i) {
                strId = getResources().getIdentifier("chat_delete_after_read_type_" + i, "string", getActivity().getPackageName());
                mOptionsItems.add(getString(strId));
            }
            wheelView.setAdapter(new ArrayWheelAdapter(mOptionsItems));
            wheelView.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(int index) {
                    mReadDeleteType = index;
                    ImageView ivMsgDeleteType = fv(R.id.ivMsgDeleteType);
                    if (mReadDeleteType == 0) {
                        ivMsgDeleteType.setVisibility(View.GONE);
                    } else {
                        ivMsgDeleteType.setVisibility(View.VISIBLE);
                        int drawableId = getResources().getIdentifier("chat_delete_type_" + mReadDeleteType, "drawable", getActivity().getPackageName());
                        ivMsgDeleteType.setImageResource(drawableId);
                    }
                }
            });
        }
        wheelView.setCurrentItem(mReadDeleteType);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ALBUM_PHOTO_REQUEST_CODE || requestCode == ALBUM_VIDEO_REQUEST_CODE) {
                try {
                    String filePath;
                    int sdkVersion = Build.VERSION.SDK_INT;
                    if (sdkVersion >= 19) { // api >= 19
                        filePath = ((ChatBaseActivity) getActivity()).getRealPathFromUriAboveApi19(data.getData());
                    } else { // api < 19
                        filePath = ((ChatBaseActivity) getActivity()).getRealPathFromUriBelowAPI19(data.getData());
                    }
                    String newPath;
                    if (requestCode == ALBUM_PHOTO_REQUEST_CODE) {
                        File file = new File(filePath);
                        if (file.length() > PHOTO_MAX_SIZE) {   //大于2M压缩处理
                            Bitmap bmp = BitmapUtil.getBitmapFromFile(filePath, ((BaseActivity) getActivity()).getDisplayMetrics().widthPixels,
                                    ((BaseActivity) getActivity()).getDisplayMetrics().heightPixels);
                            newPath = Utils.saveJpeg(bmp, getActivity());
                        } else {
                            newPath = Utils.getSaveFilePath(getActivity(), UUID.randomUUID().toString() + ".jpg");
                            Utils.copyFile(filePath, newPath);
                        }
                    } else {
                        newPath = Utils.getSaveFilePath(getActivity(), UUID.randomUUID().toString() + ".mp4");
                        Utils.copyFile(filePath, newPath);
                    }
                    File file = new File(newPath);
                    FileBean bean = new FileBean();
                    bean.setFile(file);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("fileName", file.getName());
                    map.put("fileSize", String.valueOf(file.length()));
                    MessageType type;
                    if (requestCode == ALBUM_VIDEO_REQUEST_CODE) {
                        type = MessageType.TYPE_VIDEO;
                        String[] infos = BitmapUtil.getVideoInfo(filePath);
                        map.put("time", infos[0]);
                        map.put("width", infos[1]);
                        map.put("height", infos[2]);
                    } else {
                        type = MessageType.TYPE_IMAGE;
                        int[] wh = BitmapUtil.getBitmapBound(newPath);
                        map.put("width", String.valueOf(wh[0]));
                        map.put("height", String.valueOf(wh[1]));
                    }
                    bean.setType(type);
                    bean.setExtra(map);
                    EventBus.getDefault().post(bean);
                } catch (Exception e) {

                }
            } else if (requestCode == FILE_SELECT_CODE) {
                if (PickerManager.getInstance().files.isEmpty()) {
                    return;
                }
                FileEntity fileEntity = PickerManager.getInstance().files.get(0);
                String filePath = fileEntity.getPath();
                if (TextUtils.isEmpty(filePath)) {
                    return;
                }
                File file = new File(filePath);
                if (!file.exists()) {
                    return;
                }
                if (file.length() > FILE_MAX_SIZE) {
                    ((ChatBaseActivity) getActivity()).showToast(R.string.chat_file_max_size);
                    return;
                }
                String newPath = Utils.getSaveFilePath(getActivity(), file.getName());
                Utils.copyFile(filePath, newPath);
                file = new File(newPath);
                FileBean bean = new FileBean();
                MessageType type;
                HashMap<String, String> map = new HashMap<>();
                map.put("fileName", file.getName());
                map.put("fileSize", String.valueOf(file.length()));
                String mimeType = fileEntity.getMimeType().toLowerCase();
                if (TextUtils.isEmpty(mimeType)) {
                    type = MessageType.TYPE_FILE;
                    map.put("mimeType", "");
                } else if (mimeType.contains("image/")) {
                    type = MessageType.TYPE_IMAGE;
                    int[] wh = BitmapUtil.getBitmapBound(newPath);
                    map.put("width", String.valueOf(wh[0]));
                    map.put("height", String.valueOf(wh[1]));
                } else if (mimeType.contains("video/")) {
                    type = MessageType.TYPE_VIDEO;
                    String[] infos = BitmapUtil.getVideoInfo(filePath);
                    map.put("time", infos[0]);
                    map.put("width", infos[1]);
                    map.put("height", infos[2]);
                } else {
                    type = MessageType.TYPE_FILE;
                    map.put("mimeType", mimeType);
                }
                bean.setType(type);
                bean.setFile(file);
                bean.setExtra(map);
                EventBus.getDefault().post(bean);
            }
        }
    }

    private long getExpreTimeByType() {
        if (mReadDeleteType == 1) {  //15分钟
            return 900000;
        } else if (mReadDeleteType == 2) {  //1小时
            return 3600000;
        } else if (mReadDeleteType == 3) {  //1天
            return 24 * 3600000;
        } else if (mReadDeleteType == 4) {  //1周
            return 7 * 24 * 3600000;
        }
        return 0;
    }


    private void getRedPackageDetail(final BasicMessage msgBean, String id) {
        ChatHttpMethods.getInstance().envelopeDetail(id, new HttpObserver(new SubscriberOnNextListener<EnvelopeBean>() {
            @Override
            public void onNext(EnvelopeBean bean, String msg) {
                if (getView() == null) {
                    return;
                }
                if (bean.getType() == 3) {
                    if (bean.getStatus() != msgBean.getReceiveStatus()) {
                        msgBean.sureReceiveStatus(bean.getStatus());
                        getAdapter().notifyDataSetChanged();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, bean);
                    bundle.putSerializable(Constants.BUNDLE_EXTRA_2, mChatUser);
                    bundle.putSerializable(Constants.BUNDLE_EXTRA_3, msgBean.getMessageId());
                    gotoPager(TransferResultFragment.class, bundle);
                    return;
                }
                if (bean.getToId() > 0 && bean.getUserId() == mMyInfo.getUserId()) {
                    if (bean.getStatus() != msgBean.getReceiveStatus()) {
                        msgBean.sureReceiveStatus(bean.getStatus());
                        getAdapter().notifyDataSetChanged();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, bean);
                    gotoPager(RedPackageResultFragment.class, bundle);
                } else if (bean.getGroupId() > 0 && isHadGetRedPackage(bean)) {  //群红包已领
                    msgBean.sureReceiveStatus(2);
                    getAdapter().notifyDataSetChanged();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, bean);
                    gotoPager(RedPackageResultFragment.class, bundle);
                } else if (bean.getType() == 5 && bean.getUserId() == mMyInfo.getUserId()) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, bean);
                    gotoPager(RedPackageResultFragment.class, bundle);
                } else {
                    if (bean.getStatus() != msgBean.getReceiveStatus()) {
                        msgBean.sureReceiveStatus(bean.getStatus());
                        getAdapter().notifyDataSetChanged();
                    }
                    showRedPackage(msgBean, bean);
                }
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }

    private boolean isHadGetRedPackage(EnvelopeBean bean) {
        ArrayList<EnvelopeBean.Session> list = bean.getSession();
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (EnvelopeBean.Session session : list) {
            if (session.getUserId() == DataManager.getInstance().getUserId()) {
                return true;
            }
        }
        return false;
    }

    private void showRedPackage(final BasicMessage msg, final EnvelopeBean bean) {
        final MyDialogFragment dialogFragment = new MyDialogFragment(bean.getType() == 5 ? R.layout.layout_new_member_red_package_dialog : R.layout.layout_red_package_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                ((TextView) view.findViewById(R.id.tvXXXRedPackage)).setText(getString(R.string.chat_xxx_red_package, bean.getNickName()));
                int status = bean.getStatus();
                if (status == 1) {
                    if (bean.getType() == 5) {
                        ((TextView) view.findViewById(R.id.tvRedPackageState)).setText(getString(R.string.chat_new_member_special_red_package));
                    } else {
                        ((TextView) view.findViewById(R.id.tvRedPackageState)).setText(bean.getRemark() == null ? "" : bean.getRemark());
                    }
                    ((TextView) view.findViewById(R.id.tvOpenRedPackage)).setText(getString(R.string.chat_open_red_package));
                } else if (status == 2) {
                    ((TextView) view.findViewById(R.id.tvRedPackageState)).setText(getString(R.string.chat_all_red_package_had_complete));
                    ((TextView) view.findViewById(R.id.tvOpenRedPackage)).setText(getString(R.string.chat_look_red_package));
                } else {
                    ((TextView) view.findViewById(R.id.tvRedPackageState)).setText(getString(R.string.chat_red_package_had_expire));
                    ((TextView) view.findViewById(R.id.tvOpenRedPackage)).setText(getString(R.string.chat_look_red_package));
                }
                dialogFragment.setDialogViewsOnClickListener(view, R.id.tvOpenRedPackage, R.id.ivClose);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.tvOpenRedPackage) {
                    boolean isHadGet = false;  //自己是否已经领取
                    ArrayList<EnvelopeBean.Session> list = bean.getSession();
                    if (list != null) {
                        for (EnvelopeBean.Session session : list) {
                            if (session.getUserId() == mMyInfo.getUserId()) {
                                isHadGet = true;
                                break;
                            }
                        }
                    }
                    if (!isHadGet && bean.getStatus() == 1) {
                        drawRedPackage(msg, bean.getEnvelopeId());
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, bean);
                        gotoPager(RedPackageResultFragment.class, bundle);
                    }
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    private void drawRedPackage(final BasicMessage itemMsg, String id) {
        ChatHttpMethods.getInstance().envelopeDraw(id, new HttpObserver(new SubscriberOnNextListener<EnvelopeBean>() {
            @Override
            public void onNext(EnvelopeBean bean1, String m) {
                itemMsg.sureReceiveStatus(2);
                getAdapter().notifyDataSetChanged();

                MessageBean msg = new MessageBean();
                msg.setCmd(2000);
                msg.setFromId(mMyInfo.getUserId());
                msg.setToId(bean1.getUserId());
                msg.setMsgType(MessageType.TYPE_RECEIVE_RED_PACKAGE.ordinal());
                HashMap<String, Object> map = bean1.toMap();
                map.put("messageId", itemMsg.getMessageId());
                map.put("nickName2", mMyInfo.getNickName());
                if (itemMsg.getCmd() == 2100) {
                    map.put("groupId", ((GroupMessageBean) itemMsg).getGroupId());
                    ArrayList<HashMap<String, Object>> list = getGson().fromJson(itemMsg.getExtra(), new TypeToken<ArrayList<HashMap<String, Object>>>() {
                    }.getType());
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(mMyInfo.toMap());
                    msg.setExtra(getGson().toJson(list));
                } else {
                    msg.setExtra(itemMsg.getExtra());
                }
                msg.setContent(getGson().toJson(map));
                if (mMyInfo.getUserId() != bean1.getUserId()) {  //自己的红包不需要发送到服务器
                    WebSocketHandler.getDefault().send(msg.toJson());
                } else {
                    msg.setSendStatus(1);
                    msg.setIsRead(1);
                }
                if (itemMsg.getCmd() == 2100) {
                    GroupMessageBean groupMessageBean = GroupMessageBean.toGroupMessage(msg, ((GroupMessageBean) itemMsg).getGroupId());
                    groupMessageBean.setMessageId(msg.getMessageId());
                    DatabaseOperate.getInstance().insert(groupMessageBean);
                    EventBus.getDefault().post(groupMessageBean);
                } else {
                    DatabaseOperate.getInstance().insert(msg);
                    EventBus.getDefault().post(msg);
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.BUNDLE_EXTRA, bean1);
                gotoPager(RedPackageResultFragment.class, bundle);
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }
}
