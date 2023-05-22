package com.alsc.chat.manager;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alsc.chat.R;
import com.alsc.chat.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.alsc.chat.utils.Constants;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhangke.websocket.SimpleListener;
import com.zhangke.websocket.SocketListener;
import com.zhangke.websocket.WebSocketHandler;
import com.zhangke.websocket.WebSocketManager;
import com.zhangke.websocket.WebSocketSetting;
import com.zhangke.websocket.response.ErrorResponse;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ChatManager {

    private static final String TAG = "ChatManager";

    private static ChatManager mConfigManager;

    private Context mContext;

    private Gson mGson;

    private int mConnectState; //0未连接 1已连接 2连接中

    private boolean mIsAppRunning;

    private int mNotificationID = 0;

    private static final long DELAY_TIME = 20 * 1000;

    private HashMap<String, Object> mMap = new HashMap<>();

    private IntentFilter mIntentFilter;
    private NetworkChangeReciver mNetwork;

    private MyDialogFragment mLoginoutDialog;

    private Activity mCurrentActivity;

    private boolean mIsSendingUnSendMsg = false;

    private WebSocketSetting mWebSocketSetting;

    private long mLastSendMsgSuccessTime;  //上次发消息成功时间，socket长时间连着会出现收得到消息发不出去现象，如果检测到1个小时没发送成功消息，则重连socket

    private long mLastGetHeartTime;

    private long oneHourTime = 1800 * 1000;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    long dTime1 = System.currentTimeMillis() - mLastSendMsgSuccessTime;
                    long dTime2 = System.currentTimeMillis() - mLastGetHeartTime;
                    if (mConnectState == 0 || dTime1 > oneHourTime || dTime2 > 180000) {  //有半个小时未发送成功消息则重连或者200秒没有心跳
                        mConnectState = 0;
                        String token = DataManager.getInstance().getToken();
                        if (!TextUtils.isEmpty(token)) {
                            initWebSocket(token);
                        }
                        mLastSendMsgSuccessTime = System.currentTimeMillis();
                        mLastGetHeartTime = System.currentTimeMillis();
                    }
                    Log.i(TAG, "20s loop");
                    mHandler.sendEmptyMessageDelayed(0, DELAY_TIME);
                    break;
            }
        }
    };

    private ChatManager() {
        mConnectState = 0;
        mLastSendMsgSuccessTime = System.currentTimeMillis();
        mLastGetHeartTime = System.currentTimeMillis();
        mHandler.sendEmptyMessageDelayed(0, DELAY_TIME);
    }

    public static ChatManager getInstance() {
        if (mConfigManager == null) {
            synchronized (TAG) {
                if (mConfigManager == null) {
                    mConfigManager = new ChatManager();
                }
            }
        }
        return mConfigManager;
    }

    public Context getContext() {
        return mContext;
    }

    public Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }

    public void setAppRunning(boolean isAppRunning) {
        mIsAppRunning = isAppRunning;
    }

    public void setCurrentActivity(Activity currentActivity) {
        mCurrentActivity = currentActivity;
    }

    public void setCurrentActivityNull(Activity currentActivity) {
        if (mCurrentActivity == currentActivity) {
            mCurrentActivity = null;
        }
    }

    public boolean isAppRunning() {
        return mIsAppRunning;
    }

    public void setContext(Context context) {
        mContext = context;
        String token = DataManager.getInstance().getToken();
        if (!TextUtils.isEmpty(token)) {
            initWebSocket(token);
        }
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mNetwork = new NetworkChangeReciver();
        context.registerReceiver(mNetwork, mIntentFilter);
    }

    class NetworkChangeReciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub'
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                String token = DataManager.getInstance().getToken();
                if (!ChatManager.getInstance().isConnected()) {
                    if (!TextUtils.isEmpty(token)) {
                        ChatManager.getInstance().initWebSocket(token);
                    }
                }
            } else {
                //       ConfigManager.getInstance().disSocketConnect();
            }

        }

    }

    public boolean isConnected() {
        return mConnectState == 1;
    }

    public void initWebSocket(String token) {
        if (TextUtils.isEmpty(token) || mConnectState == 1 || mConnectState == 2) {
            return;
        }
        Log.e(TAG, "initWebSocket:" + mConnectState);
        disSocketConnect();
        mConnectState = 2;
        mWebSocketSetting = new WebSocketSetting();

        //设置连接超时时间
        mWebSocketSetting.setConnectTimeout(15 * 1000);

        //设置心跳间隔时间
        mWebSocketSetting.setConnectionLostTimeout(30);

        //设置断开后的重连次数，可以设置的很大，不会有什么性能上的影响
        mWebSocketSetting.setReconnectFrequency(10);

        //网络状态发生变化后是否重连，
        //需要调用 WebSocketHandler.registerNetworkChangedReceiver(context) 方法注册网络监听广播
        mWebSocketSetting.setReconnectWithNetworkChanged(true);
        //注意，需要在 AndroidManifest 中配置网络状态获取权限
        //注册网路连接状态变化广播
        //连接地址，必填，例如 wss://echo.websocket.org
        String url = String.format("ws://im.metazoom.pro:2348?token=%s&channel=im", token);

        Log.e(TAG, url);
        //     String url = String.format("ws://chat.blokbase.de:2348?token=%s&channel=im", token);
        mWebSocketSetting.setConnectUrl(url);//必填

        //   WebSocketHandler.setNull();
        //通过 init 方法初始化默认的 WebSocketManager 对象
        WebSocketManager manager = WebSocketHandler.init(mWebSocketSetting);
        manager.resetConnectState();
        //启动连接
        manager.start();

        WebSocketHandler.registerNetworkChangedReceiver(mContext);
        WebSocketHandler.getDefault().addListener(socketListener);
        DatabaseOperate.getInstance().handleNotSendMsg(DataManager.getInstance().getUserId());
    }

    public void disSocketConnect() {
        mWebSocketSetting = null;
        try {
            mConnectState = 0;
            if (WebSocketHandler.getDefault() == null) {
                return;
            }
            WebSocketHandler.getDefault().disConnect();
            WebSocketHandler.getDefault().destroy();
            WebSocketHandler.setNull();
        } catch (Exception e) {

        }
    }

    private SocketListener socketListener = new SimpleListener() {
        @Override
        public void onConnected() {
            Log.e(TAG, "onConnected");
            mConnectState = 1;
            mLastSendMsgSuccessTime = System.currentTimeMillis();
            mLastGetHeartTime = System.currentTimeMillis();
        }

        @Override
        public void onConnectFailed(Throwable e) {
            mConnectState = 0;
            if (e != null) {
                Log.e(TAG, "onConnectFailed:" + e.toString());
            } else {
                Log.e(TAG, "onConnectFailed:null");
            }
        }

        @Override
        public void onDisconnect() {
            Log.e(TAG, "onDisconnect");
            mConnectState = 0;
            DatabaseOperate.getInstance().handleNotSendMsg(DataManager.getInstance().getUserId());
        }

        @Override
        public void onSendDataError(ErrorResponse errorResponse) {
            Log.e(TAG, "error:" + errorResponse.getRequestData().getRequestData().toString());
            String text = errorResponse.getRequestData().getRequestData().toString();
            if (!TextUtils.isEmpty(text)) {
                try {
                    JSONObject object = new JSONObject(text);
                    int cmd = object.optInt("cmd");
                    BasicMessage msg = null;
                    if (cmd == 2000 || cmd == 2200) {
                        msg = getGson().fromJson(text, MessageBean.class);
                    } else if (cmd == 2100) {
                        msg = getGson().fromJson(text, GroupMessageBean.class);
                    }
                    if (msg != null && !TextUtils.isEmpty(msg.getMessageId())
                            && msg.getFromId() == DataManager.getInstance().getUserId()) {
                        msg.sendMsgFailed();
                        mMap.clear();
                        mMap.put(Constants.SEND_MSG_FAILED, msg.getMessageId());
                        EventBus.getDefault().post(mMap);
                    }
                } catch (Exception e) {
                }
            }
            errorResponse.release();
            mConnectState = 0;
            initWebSocket(DataManager.getInstance().getToken());
        }

        @Override
        public <T> void onMessage(String message, T data) {
            //      Log.e(TAG, "message:" + message);
            progressMsg(message);
        }
    };

    private void sendUnSendMsg() {
        if (mIsSendingUnSendMsg) {
            return;
        }
        mIsSendingUnSendMsg = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<BasicMessage> list = DatabaseOperate.getInstance().getAllUnSendMsg(DataManager.getInstance().getUser().getUserId());
                if (!list.isEmpty()) {
                    for (BasicMessage msg : list) {
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
                                    file = new File(Utils.getSaveFilePath(mContext, fileName));
                                } catch (Exception e) {

                                }
                            }
                        }
                        if (file != null && file.exists()) {
                            FileBean fileBean = new FileBean();
                            fileBean.setFile(file);
                            UPYFileUploadManger.getInstance().formUpload(msg, fileBean);
                        } else {
                            WebSocketHandler.getDefault().send(msg.toJson());
                        }

                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {

                        }
                    }
                }
                mIsSendingUnSendMsg = false;
            }
        }).start();

    }

    public void progressMsg(String message) {
        if (TextUtils.isEmpty(message) || DataManager.getInstance().getUser() == null) {
            return;
        }
        try {
            JSONObject object = new JSONObject(message);
            int cmd = object.optInt("cmd");
            if (message.contains("\"code\":")) {
                int code = object.optInt("code", 200);
                if (code != 200 && cmd == 9999) {
                    disSocketConnect();
                    return;
                }
            }
            switch (cmd) {
                case 0:
                    sendUnSendMsg();  //将未发出去的消息重新发出去
                    break;
                case 9999:  //token失效需要重新登录
                    //             case 4444:  //其他设备登录该账号挤下线
                    Log.e(TAG, "cmd: " + cmd);
                    disSocketConnect();
                    showLoginOutDialog();
                    break;
                case 2000: //接收消息
                    MessageBean msgBean = getGson().fromJson(message, MessageBean.class);
                    if (TextUtils.isEmpty(msgBean.getMessageId())) {
                        return;
                    }
                    ArrayList<String> msgIds = new ArrayList<>();
                    msgIds.add(msgBean.getMessageId());
                    sendReceiveMsg(2050, msgIds);
                    if (msgBean.getMsgType() == MessageType.TYPE_RECEIVE_TRANSFER.ordinal()) {
                        HashMap<String, String> map = getGson().fromJson(msgBean.getContent(), new TypeToken<HashMap<String, String>>() {
                        }.getType());
                        String messageId = map.get("messageId");
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("messageId", messageId);
                        contentValues.put("receiveStatus", 2);
                        DatabaseOperate.getInstance().update(new MessageBean(), contentValues);
                        map.clear();
                        map.put(Constants.RECEIVE_ENVELOPE, messageId);
                        EventBus.getDefault().post(map);
                    } else if (msgBean.getMsgType() == MessageType.TYPE_RECEIVE_RED_PACKAGE.ordinal()) {
                        HashMap<String, String> map = getGson().fromJson(msgBean.getContent(), new TypeToken<HashMap<String, String>>() {
                        }.getType());
                        if (!map.containsKey("groupId")) {
                            String messageId = map.get("messageId");
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("messageId", messageId);
                            contentValues.put("receiveStatus", 2);
                            DatabaseOperate.getInstance().update(new GroupMessageBean(), contentValues);
                            map.clear();
                            map.put(Constants.RECEIVE_ENVELOPE, messageId);
                            EventBus.getDefault().post(map);
                            if (DatabaseOperate.getInstance().insert(msgBean) == -1) {
                                return;
                            }
                            EventBus.getDefault().post(msgBean);
                        } else {
                            GroupMessageBean groupMessage = GroupMessageBean.toGroupMessage(msgBean, Long.parseLong(map.get("groupId")));
                            groupMessage.setMessageId(msgBean.getMessageId());
                            if (DatabaseOperate.getInstance().insert(groupMessage) == -1) {
                                return;
                            }
                            EventBus.getDefault().post(groupMessage);
                        }
                    } else if (msgBean.getMsgType() == MessageType.TYPE_NEW_MEMBER_RED_PACKAGE.ordinal()) {
                        if (msgBean.getGroupId() > 0) {
                            HashMap<String, Object> mapG = new HashMap<>();
                            if (!TextUtils.isEmpty(msgBean.getContent())) {
                                ArrayList<HashMap<String, Object>> list = new ArrayList<>();
                                JSONObject object1 = new JSONObject(msgBean.getContent());
                                mapG.put("userId", object1.optLong("userId"));
                                mapG.put("avatarUrl", object1.optLong("avatarUrl"));
                                mapG.put("nickName", object1.optLong("nickName"));
                                list.add(mapG);
                                list.add(DataManager.getInstance().getUser().toMap());
                                msgBean.setFromId(object1.optLong("userId"));
                                msgBean.setExtra(getGson().toJson(list));
                            }
                            GroupMessageBean groupMessage = GroupMessageBean.toGroupMessage(msgBean, msgBean.getGroupId());
                            groupMessage.setMessageId(msgBean.getMessageId());
                            if (DatabaseOperate.getInstance().insert(groupMessage) == -1) {
                                return;
                            }
                            EventBus.getDefault().post(groupMessage);
                        }
                    } else if (msgBean.getMsgType() == MessageType.TYPE_DELETE_MSG.ordinal()) {
                        String messageId = msgBean.getContent();
                        if (TextUtils.isEmpty(messageId)) {
                            return;
                        }
                        BasicMessage msg = new MessageBean();
                        msg.setMessageId(messageId);
                        DatabaseOperate.getInstance().deleteOne(msg, DataManager.getInstance().getUserId());
                        mMap.clear();
                        mMap.put(Constants.DELETE_CHAT_MESSAGE, messageId);
                        EventBus.getDefault().post(mMap);
                        return;
                    } else {
                        if (DatabaseOperate.getInstance().insert(msgBean) == -1) {
                            return;
                        }
                        EventBus.getDefault().post(msgBean);
                    }
                    showNotification(msgBean);
                    break;
                case 2001: //确认消息发送成功
                case 2201: //客服确认发送成功消息
                    mLastSendMsgSuccessTime = System.currentTimeMillis();
                    String messageId = object.optString("result");
                    if (TextUtils.isEmpty(messageId)) {
                        return;
                    }
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("messageId", messageId);
                    contentValues.put("sendStatus", 1);
                    DatabaseOperate.getInstance().update(new MessageBean(), contentValues);
                    mMap.clear();
                    mMap.put(Constants.SEND_MSG_SUCCESS, messageId);
                    EventBus.getDefault().post(mMap);
                    break;
                case 2100: //接收群消息
                    GroupMessageBean groupMsg = getGson().fromJson(message, GroupMessageBean.class);
                    if (TextUtils.isEmpty(groupMsg.getMessageId())) {
                        return;
                    }
                    msgIds = new ArrayList<>();
                    msgIds.add(groupMsg.getMessageId());
                    sendReceiveMsg(2051, msgIds);
                    if (groupMsg.getMsgType() == MessageType.TYPE_GROUP_SYSTEM_MSG.ordinal()) {
                        HashMap<String, Long> map = getGson().fromJson(groupMsg.getContent(), new TypeToken<HashMap<String, Long>>() {
                        }.getType());
                        EventBus.getDefault().post(map);
                        return;
                    } else if (groupMsg.getMsgType() == MessageType.TYPE_DELETE_MSG.ordinal()) {
                        messageId = groupMsg.getContent();
                        if (TextUtils.isEmpty(messageId)) {
                            return;
                        }
                        BasicMessage msg = new GroupMessageBean();
                        msg.setMessageId(messageId);
                        DatabaseOperate.getInstance().deleteOne(msg, DataManager.getInstance().getUserId());
                        mMap.clear();
                        mMap.put(Constants.DELETE_CHAT_MESSAGE, messageId);
                        EventBus.getDefault().post(mMap);
                        return;
                    }
                    if (DatabaseOperate.getInstance().insert(groupMsg) == -1) {  //已经重复
                        return;
                    }
                    EventBus.getDefault().post(groupMsg);
                    showNotification(groupMsg);
                    break;
                case 2101: //确认群消息发送成功
                    mLastSendMsgSuccessTime = System.currentTimeMillis();
                    messageId = object.optString("result");
                    if (TextUtils.isEmpty(messageId)) {
                        return;
                    }
                    contentValues = new ContentValues();
                    contentValues.put("messageId", messageId);
                    contentValues.put("sendStatus", 1);
                    DatabaseOperate.getInstance().update(new GroupMessageBean(), contentValues);
                    mMap.clear();
                    mMap.put(Constants.SEND_MSG_SUCCESS, messageId);
                    EventBus.getDefault().post(mMap);
                    break;
                case 2102: //群解散
                    long groupId = object.optLong("result");
                    if (groupId <= 0) {
                        return;
                    }
                    removeGroup(groupId);
                    break;
                case 2200:  //客服消息
                    msgBean = getGson().fromJson(message, MessageBean.class);
                    if (TextUtils.isEmpty(msgBean.getMessageId())) {
                        return;
                    }
                    msgIds = new ArrayList<>();
                    msgIds.add(msgBean.getMessageId());
                    sendReceiveMsg(2050, msgIds);
                    if (DatabaseOperate.getInstance().insert(msgBean) == -1) {
                        return;
                    }
                    EventBus.getDefault().post(msgBean);
                    showNotification(msgBean);
                    break;
                case 1000: //发送心跳
                    mLastGetHeartTime = System.currentTimeMillis();
                    JSONObject jb = new JSONObject();
                    jb.put("cmd", 1000);
                    WebSocketHandler.getDefault().send(jb.toString());
                    mConnectState = 1;
                    break;
                case 1010: //离线群组消息
                    MessageResponse<GroupMessageBean> groupResponse = getGson().fromJson(message, new TypeToken<MessageResponse<GroupMessageBean>>() {
                    }.getType());
                    if (groupResponse != null) {
                        ArrayList<GroupMessageBean> list = groupResponse.getMessages();
                        if (list == null || list.isEmpty()) {
                            return;
                        }
                        msgIds = new ArrayList<>();
                        for (GroupMessageBean bean : list) {
                            if (TextUtils.isEmpty(bean.getMessageId())) {
                                continue;
                            }
                            msgIds.add(bean.getMessageId());
                        }
                        sendReceiveMsg(2051, msgIds);
                        for (GroupMessageBean bean : list) {
                            if (TextUtils.isEmpty(bean.getMessageId())
                                    || bean.getMsgType() == MessageType.TYPE_GROUP_SYSTEM_MSG.ordinal()) {
                                continue;
                            }
                            if (bean.getMsgType() == MessageType.TYPE_DELETE_MSG.ordinal()) {
                                messageId = bean.getContent();
                                BasicMessage msg = new GroupMessageBean();
                                msg.setMessageId(messageId);
                                DatabaseOperate.getInstance().deleteOne(msg, DataManager.getInstance().getUserId());
                                mMap.clear();
                                mMap.put(Constants.DELETE_CHAT_MESSAGE, messageId);
                                EventBus.getDefault().post(mMap);
                                continue;
                            }
                            if (DatabaseOperate.getInstance().insert(bean) == -1) {
                                continue;
                            }
                            EventBus.getDefault().post(bean);
                        }
                        //                    playDefaultMediaPlayer();
                    }
                    break;
                case 1020:  //离线个人消息
                    MessageResponse<MessageBean> response = getGson().fromJson(message, new TypeToken<MessageResponse<MessageBean>>() {
                    }.getType());
                    if (response != null) {
                        ArrayList<MessageBean> list = response.getMessages();
                        if (list == null || list.isEmpty()) {
                            return;
                        }
                        msgIds = new ArrayList<>();
                        for (MessageBean bean : list) {
                            if (TextUtils.isEmpty(bean.getMessageId())) {
                                continue;
                            }
                            msgIds.add(bean.getMessageId());
                        }
                        sendReceiveMsg(2050, msgIds);
                        for (MessageBean bean : list) {
                            if (TextUtils.isEmpty(bean.getMessageId())) {
                                continue;
                            }
                            if (bean.getMsgType() == MessageType.TYPE_RECEIVE_TRANSFER.ordinal()) {
                                HashMap<String, String> map = getGson().fromJson(bean.getContent(), new TypeToken<HashMap<String, String>>() {
                                }.getType());
                                messageId = map.get("messageId");
                                contentValues = new ContentValues();
                                contentValues.put("messageId", messageId);
                                contentValues.put("receiveStatus", 2);
                                DatabaseOperate.getInstance().update(new MessageBean(), contentValues);
                                map.clear();
                                map.put(Constants.RECEIVE_ENVELOPE, messageId);
                                EventBus.getDefault().post(map);
                                if (DatabaseOperate.getInstance().insert(bean) == -1) {
                                    continue;
                                }
                                EventBus.getDefault().post(bean);
                            } else if (bean.getMsgType() == MessageType.TYPE_RECEIVE_RED_PACKAGE.ordinal()) {
                                HashMap<String, String> map = getGson().fromJson(bean.getContent(), new TypeToken<HashMap<String, String>>() {
                                }.getType());
                                if (!map.containsKey("groupId")) {
                                    messageId = map.get("messageId");
                                    contentValues = new ContentValues();
                                    contentValues.put("messageId", messageId);
                                    contentValues.put("receiveStatus", 2);
                                    DatabaseOperate.getInstance().update(new MessageBean(), contentValues);
                                    map.clear();
                                    map.put(Constants.RECEIVE_ENVELOPE, messageId);
                                    EventBus.getDefault().post(map);
                                } else {
                                    GroupMessageBean groupMessage = GroupMessageBean.toGroupMessage(bean, Long.parseLong(map.get("groupId")));
                                    groupMessage.setMessageId(bean.getMessageId());
                                    if (DatabaseOperate.getInstance().insert(groupMessage) == -1) {
                                        continue;
                                    }
                                    EventBus.getDefault().post(groupMessage);
                                }
                            } else if (bean.getMsgType() == MessageType.TYPE_NEW_MEMBER_RED_PACKAGE.ordinal()) {
                                if (bean.getGroupId() > 0) {
                                    HashMap<String, Object> mapG = new HashMap<>();
                                    if (!TextUtils.isEmpty(bean.getContent())) {
                                        ArrayList<HashMap<String, Object>> l = new ArrayList<>();
                                        JSONObject object1 = new JSONObject(bean.getContent());
                                        mapG.put("userId", object1.optLong("userId"));
                                        mapG.put("avatarUrl", object1.optLong("avatarUrl"));
                                        mapG.put("nickName", object1.optLong("nickName"));
                                        l.add(mapG);
                                        l.add(DataManager.getInstance().getUser().toMap());
                                        bean.setFromId(object1.optLong("userId"));
                                        bean.setExtra(getGson().toJson(l));
                                    }
                                    GroupMessageBean groupMessage = GroupMessageBean.toGroupMessage(bean, bean.getGroupId());
                                    groupMessage.setMessageId(bean.getMessageId());
                                    if (DatabaseOperate.getInstance().insert(groupMessage) == -1) {
                                        continue;
                                    }
                                    EventBus.getDefault().post(groupMessage);
                                }
                            } else if (bean.getMsgType() == MessageType.TYPE_DELETE_MSG.ordinal()) {
                                messageId = bean.getContent();
                                BasicMessage msg = new MessageBean();
                                msg.setMessageId(messageId);
                                DatabaseOperate.getInstance().deleteOne(msg, DataManager.getInstance().getUserId());
                                mMap.clear();
                                mMap.put(Constants.DELETE_CHAT_MESSAGE, messageId);
                                EventBus.getDefault().post(mMap);
                            } else if (bean.getMsgType() == MessageType.TYPE_SYSTEM_MESSAGE.ordinal()) {
                                HashMap<String, Long> map = getGson().fromJson(bean.getContent(), new TypeToken<HashMap<String, Long>>() {
                                }.getType());
                                if (map.containsKey(Constants.END_GROUP)) {
                                    removeGroup(map.get(Constants.END_GROUP));
                                    continue;
                                }
                                EventBus.getDefault().post(map);
                            } else {
                                if (DatabaseOperate.getInstance().insert(bean) == -1) {
                                    continue;
                                }
                                EventBus.getDefault().post(bean);
                            }
                        }
                        //                       playDefaultMediaPlayer();
                    }
                    break;
                case 2010: //系统消息
                    msgBean = getGson().fromJson(message, MessageBean.class);
                    if (TextUtils.isEmpty(msgBean.getContent())) {
                        return;
                    }
                    msgIds = new ArrayList<>();
                    msgIds.add(msgBean.getMessageId());
                    sendReceiveMsg(2050, msgIds);
                    HashMap<String, Long> map = getGson().fromJson(msgBean.getContent(), new TypeToken<HashMap<String, Long>>() {
                    }.getType());
                    if (map.containsKey(Constants.END_GROUP)) {
                        removeGroup(map.get(Constants.END_GROUP));
                        break;
                    } else if (map.containsKey(Constants.REDRESH_FRIENDS)
                            || map.containsKey(Constants.NEW_VERIFY)) {
                        playDefaultMediaPlayer();
                    }
                    EventBus.getDefault().post(map);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendReceiveMsg(int cmd, ArrayList<String> msgIds) {
        if (msgIds == null || msgIds.isEmpty()) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("cmd", cmd);
        map.put("messageId", msgIds);
        WebSocketHandler.getDefault().send(getGson().toJson(map));
    }

    private void removeGroup(long groupId) {
        ArrayList<GroupBean> groups = DataManager.getInstance().getGroups();
        for (GroupBean bean : groups) {
            if (bean.getGroupId() == groupId) {
                groups.remove(bean);
                DataManager.getInstance().saveGroups(groups);
                break;
            }
        }
        UserBean myInfo = DataManager.getInstance().getUser();
        DatabaseOperate.getInstance().deleteGroupChatRecord(myInfo.getUserId(), groupId);
        HashMap<String, Object> map1 = new HashMap<>();
        map1.put(Constants.END_GROUP, groupId);
        EventBus.getDefault().post(map1);
    }

    public void showNotification(BasicMessage msg) {
        int msgType = msg.getMsgType();
        if (msgType == MessageType.TYPE_INVITE_TO_GROUP.ordinal()
                || msgType == MessageType.TYPE_REMOVE_FROM_GROUP.ordinal()
                || msgType == MessageType.TYPE_IN_GROUP_BY_QRCODE.ordinal()
                || msgType == MessageType.TYPE_FORBID_CHAT.ordinal()
                || msgType == MessageType.TYPE_REOMVE_FORBID_CHAT.ordinal()) {
            UserBean myInfo = DataManager.getInstance().getUser();
            ArrayList<UserBean> list = getGson().fromJson(msg.getExtra(), new TypeToken<ArrayList<UserBean>>() {
            }.getType());
            if (list == null) {
                return;
            }
            boolean isHad = false;
            for (UserBean bean : list) {
                if (myInfo.getUserId() == bean.getUserId()) {
                    isHad = true;
                    break;
                }
            }
            if (!isHad) {  //邀请入群或者踢人信息中没有自己则不需要推送
                return;
            }
        }

//        HashMap<String, ChatSubBean> settings = DataManager.getInstance().getChatSubSettings();
//        ChatSubBean subBean = null;
//        if (msg instanceof MessageBean) {
//            subBean = settings.get("user_" + msg.getFromId());
//        } else if (msg instanceof GroupMessageBean) {
//            subBean = settings.get("group_" + ((GroupMessageBean) msg).getGroupId());
//        }
//        if (subBean == null) {
//            subBean = new ChatSubBean();
//        }

        UserBean userBean = null;
        GroupBean groupBean = null;
        if (msg instanceof MessageBean) {
            if (!TextUtils.isEmpty(msg.getExtra())) {
                ArrayList<UserBean> users = getGson().fromJson(msg.getExtra(), new TypeToken<ArrayList<UserBean>>() {
                }.getType());
                if (users != null && !users.isEmpty()) {
                    for (UserBean bean : users) {
                        if (bean.getUserId() == msg.getFromId()) {
                            userBean = bean;
                            break;
                        }
                    }
                }
            }
        } else if (msg instanceof GroupMessageBean) {
            long groupId = ((GroupMessageBean) msg).getGroupId();
            ArrayList<GroupBean> list = DataManager.getInstance().getGroups();
            if (list != null && !list.isEmpty()) {
                for (GroupBean group : list) {
                    if (group.getGroupId() == groupId) {
                        groupBean = group;
                        break;
                    }
                }
            }
        }

        if (userBean == null && groupBean == null) {
            return;
        }
        if ((userBean != null && userBean.getIgnore() == 1) || (groupBean != null && groupBean.getIgnore() == 1)) {
            return;
        }

        ChatSettingBean chatSetting = DataManager.getInstance().getChatSetting();
        if (chatSetting.getNotInteruptMode() == 1) {  //勿扰模式打开
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int total = hour * 60 + minute;
            int startHour = chatSetting.getStartTime() / 60;
            int endHour = chatSetting.getEndTime() / 60;
            if (total > chatSetting.getStartTime()) {
                if (endHour > startHour) {
                    if (total < chatSetting.getEndTime()) {
                        return;
                    }
                } else {
                    return;
                }
            } else {
                if (endHour < startHour) {
                    if (total < chatSetting.getEndTime()) {
                        return;
                    }
                }
            }
        }

        if (mIsAppRunning) {
            playDefaultMediaPlayer();
            return;
        }


        final String url;
        if (userBean == null) {
            url = groupBean.getIcon();
        } else {
            url = userBean.getAvatarUrl();
        }
        if (!TextUtils.isEmpty(url)) {
            final UserBean user = userBean;
            final GroupBean group = groupBean;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int with = Utils.dip2px(mContext, 40);
                    FutureTarget future = Glide.with(mContext)
                            .load(url)
                            .downloadOnly(with, with);
                    try {
                        File cacheFile = (File) future.get();
                        final Bitmap bmp = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showNotification(bmp, user, group, msg);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showNotification(null, user, group, msg);
                            }
                        });
                    }
                }
            }).start();
        } else {
            showNotification(null, userBean, groupBean, msg);
        }
    }

    private void showNotification(Bitmap bmp, UserBean userBean, GroupBean groupBean, BasicMessage msg) {
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1",
                    "my_channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(
                    mContext, "1").setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true)
                    .setWhen(System.currentTimeMillis());
        } else {
            builder = new Notification.Builder(
                    mContext).setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true)
                    .setWhen(System.currentTimeMillis());
        }
        builder.setContentTitle(mContext.getString(R.string.app_name));
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_ALL;
        notification.when = System.currentTimeMillis();
        notification.icon = mContext.getResources().getIdentifier("app_logo", "mipmap", mContext.getPackageName());
        final RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.layout_notification);
        remoteViews.setImageViewResource(R.id.ivAvatar, R.drawable.chat_default_avatar);
        remoteViews.setTextViewText(R.id.tvMessage, getContentByType(msg, mContext));
        remoteViews.setTextViewText(R.id.tvTime, Utils.getTimeStrOnlyHour(msg.getCreateTime()));
        if (userBean == null) {
            remoteViews.setTextViewText(R.id.tvName, groupBean.getName());
        } else {
            remoteViews.setTextViewText(R.id.tvName, userBean.getNickName());
        }
        if (bmp == null) {
            remoteViews.setImageViewResource(R.id.ivAvatar, R.drawable.chat_default_avatar);
        } else {
            remoteViews.setImageViewBitmap(R.id.ivAvatar, bmp);
        }

        Intent intent = new Intent();
        if (userBean == null) {
            intent.putExtra(Constants.BUNDLE_EXTRA, 1);
            intent.putExtra(Constants.BUNDLE_EXTRA_2, groupBean);
        } else {
            intent.putExtra(Constants.BUNDLE_EXTRA, 0);
            intent.putExtra(Constants.BUNDLE_EXTRA_2, userBean);
        }
        intent.setComponent(new ComponentName(mContext.getPackageName(), "io.netflow.walletpro.activity.MainActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, ++mNotificationID,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.llNotification, contentIntent);
        notification.contentView = remoteViews;//显示布局
        notificationManager.notify(TAG, mNotificationID, notification);
    }

    public String getContentByType(BasicMessage msg, Context context) {
        String content = "";
        try {
            int msgType = msg.getMsgType();
            if (msgType == MessageType.TYPE_TEXT.ordinal()
                    || msgType == MessageType.TYPE_INVITE_PAY_IN_GROUP.ordinal()
                    || msgType == MessageType.TYPE_GROUP_AT_MEMBER_MSG.ordinal()) {
                content = msg.getContent();
            } else if (msgType == MessageType.TYPE_IMAGE.ordinal()) {
                content = context.getString(R.string.chat_type_image);
            } else if (msgType == MessageType.TYPE_VOICE.ordinal()) {
                content = context.getString(R.string.chat_type_voice);
            } else if (msgType == MessageType.TYPE_VIDEO.ordinal()) {
                content = context.getString(R.string.chat_type_video);
            } else if (msgType == MessageType.TYPE_LOCATION.ordinal()) {
                content = context.getString(R.string.chat_type_location);
            } else if (msgType == MessageType.TYPE_FILE.ordinal()) {
                content = context.getString(R.string.chat_type_file);
            } else if (msgType == MessageType.TYPE_RED_PACKAGE.ordinal() || msgType == MessageType.TYPE_RECEIVE_RED_PACKAGE.ordinal()) {
                content = context.getString(R.string.chat_type_red_package);
            } else if (msgType == MessageType.TYPE_TRANSFER.ordinal() || msgType == MessageType.TYPE_RECEIVE_TRANSFER.ordinal()) {
                content = context.getString(R.string.chat_type_transfer);
            } else if (msgType == MessageType.TYPE_RECOMAND_USER.ordinal()) {
                content = context.getString(R.string.chat_type_personal_profile);
            } else if (msgType == MessageType.TYPE_NEW_MEMBER_RED_PACKAGE.ordinal()) {
                content = context.getString(R.string.chat_type_new_member_red_package);
            } else if (msgType == MessageType.TYPE_INVITE_TO_GROUP.ordinal()) {
                UserBean myInfo = DataManager.getInstance().getUser();
                UserBean user1 = getGson().fromJson(msg.getContent(), UserBean.class);
                ArrayList<UserBean> list = getGson().fromJson(msg.getExtra(), new TypeToken<ArrayList<UserBean>>() {
                }.getType());
                String name1 = user1.getUserId() == myInfo.getUserId() ? context.getString(R.string.chat_you) : user1.getNickName();
                String name2 = "";
                int index = 0;
                if (list == null) {
                    content = context.getString(R.string.chat_xxx_invite_xxx_add_group, name1, "");
                } else {
                    for (UserBean bean : list) {
                        if (bean.getUserId() == myInfo.getUserId()) {
                            name2 += context.getString(bean.getUserId() == user1.getUserId() ? R.string.chat_mine : R.string.chat_you);
                        } else {
                            name2 += bean.getNickName();
                        }
                        if (index < list.size() - 1) {
                            name2 += "、";
                        }
                        ++index;
                    }
                    content = context.getString(R.string.chat_xxx_invite_xxx_add_group, name1, name2);
                }

            } else if (msgType == MessageType.TYPE_REMOVE_FROM_GROUP.ordinal()) {
                UserBean myInfo = DataManager.getInstance().getUser();
                ArrayList<UserBean> list = getGson().fromJson(msg.getExtra(), new TypeToken<ArrayList<UserBean>>() {
                }.getType());
                String name = "";
                int index = 0;
                for (UserBean bean : list) {
                    if (bean.getUserId() == myInfo.getUserId()) {
                        name += context.getString(R.string.chat_you);
                    } else {
                        name += bean.getNickName();
                    }
                    if (index < list.size() - 1) {
                        name += "、";
                    }
                    ++index;
                }
                content = context.getString(R.string.chat_xxx_remove_from_group, name);
            } else if (msgType == MessageType.TYPE_IN_GROUP_BY_QRCODE.ordinal()) {
                UserBean myInfo = DataManager.getInstance().getUser();
                ArrayList<UserBean> list = getGson().fromJson(msg.getExtra(), new TypeToken<ArrayList<UserBean>>() {
                }.getType());
                String name = "";
                int index = 0;
                for (UserBean bean : list) {
                    if (bean.getUserId() == myInfo.getUserId()) {
                        name += context.getString(R.string.chat_you);
                    } else {
                        name += bean.getNickName();
                    }
                    if (index < list.size() - 1) {
                        name += "、";
                    }
                    ++index;
                }
                content = context.getString(R.string.chat_xxx_in_group_by_qrcode, name);
            } else if (msgType == MessageType.TYPE_UPDATE_GROUP_NAME.ordinal()) {
                content = context.getString(R.string.chat_group_ower_update_group_name);
            } else if (msgType == MessageType.TYPE_UPDATE_GROUP_NOTICE.ordinal()) {
                content = context.getString(R.string.chat_group_ower_update_group_notice);
            } else if (msgType == MessageType.TYPE_ALL_FORBID_CHAT.ordinal()) {
                content = context.getString(R.string.chat_all_forbid_chat);
            } else if (msgType == MessageType.TYPE_ALL_REMOVE_FORBID_CHAT.ordinal()) {
                content = context.getString(R.string.chat_remove_all_forbid_chat);
            } else if (msgType == MessageType.TYPE_FORBID_CHAT.ordinal()) {
                UserBean myInfo = DataManager.getInstance().getUser();
                ArrayList<UserBean> list = getGson().fromJson(msg.getExtra(), new TypeToken<ArrayList<UserBean>>() {
                }.getType());
                String name = "";
                int index = 0;
                for (UserBean bean : list) {
                    if (bean.getUserId() == myInfo.getUserId()) {
                        name += context.getString(R.string.chat_you);
                    } else {
                        name += bean.getNickName();
                    }
                    if (index < list.size() - 1) {
                        name += "、";
                    }
                    ++index;
                }
                content = context.getString(R.string.chat_xxx_forbid_chat, name);
            } else if (msgType == MessageType.TYPE_REOMVE_FORBID_CHAT.ordinal()) {
                UserBean myInfo = DataManager.getInstance().getUser();
                ArrayList<UserBean> list = getGson().fromJson(msg.getExtra(), new TypeToken<ArrayList<UserBean>>() {
                }.getType());
                String name = "";
                int index = 0;
                for (UserBean bean : list) {
                    if (bean.getUserId() == myInfo.getUserId()) {
                        name += context.getString(R.string.chat_you);
                    } else {
                        name += bean.getNickName();
                    }
                    if (index < list.size() - 1) {
                        name += "、";
                    }
                    ++index;
                }
                content = context.getString(R.string.chat_reomve_xxx_forbid_chat, name);
            }
        } catch (Exception e) {
            content = "";
        }
        return content;
    }

    public void playDefaultMediaPlayer() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(mContext, notification);
        r.play();
    }


    public synchronized void showLoginOutDialog() {
        if (mLoginoutDialog != null || mCurrentActivity == null) {
            return;
        }
        try {
            mLoginoutDialog = new MyDialogFragment(R.layout.layout_one_btn_dialog);
            mLoginoutDialog.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
                @Override
                public void initView(View view) {
                    view.findViewById(R.id.tv1).setVisibility(View.GONE);
                    ((TextView) view.findViewById(R.id.tv2)).setText(mCurrentActivity.getString(R.string.chat_login_end));
                    ((TextView) view.findViewById(R.id.btn2)).setText(mCurrentActivity.getString(R.string.chat_ok));
                    mLoginoutDialog.setDialogViewsOnClickListener(view, R.id.btn2);
                }

                @Override
                public void onViewClick(int viewId) {
                    if (mCurrentActivity != null && !mCurrentActivity.isDestroyed()) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put(Constants.GO_LOGIN, "");
                        EventBus.getDefault().post(map);
                    }
                }
            });
            mLoginoutDialog.show(((AppCompatActivity) mCurrentActivity).getSupportFragmentManager(), "MyDialogFragment");
            mLoginoutDialog.setOnDismiss(new MyDialogFragment.IDismissListener() {
                @Override
                public void onDismiss() {
                    ChatManager.getInstance().disSocketConnect();
                    mLoginoutDialog = null;
                }
            });
        } catch (Exception e) {

        }
    }

}
