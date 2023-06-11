package com.zhangke.websocket;

import android.content.ContentValues;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.alsc.chat.http.OkHttpClientManager;
import com.alsc.chat.manager.ChatManager;
import com.alsc.chat.utils.Constants;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.BasicMessage;
import com.common.lib.bean.BasicResponse;
import com.common.lib.bean.ChatGPTMessageBean;
import com.common.lib.bean.MessageBean;
import com.common.lib.bean.MessageType;
import com.common.lib.manager.DataManager;
import com.common.lib.network.HttpMethods;
import com.zhangke.websocket.dispatcher.MainThreadResponseDelivery;
import com.zhangke.websocket.dispatcher.ResponseDelivery;
import com.zhangke.websocket.dispatcher.ResponseProcessEngine;
import com.zhangke.websocket.request.Request;
import com.zhangke.websocket.request.RequestFactory;
import com.zhangke.websocket.response.ErrorResponse;
import com.zhangke.websocket.response.Response;
import com.zhangke.websocket.response.ResponseFactory;
import com.zhangke.websocket.util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;

import io.reactivex.rxjava3.core.Observer;
import okhttp3.Call;
import okhttp3.Callback;

/**
 * WebSocket 管理类
 * <p>
 * Created by ZhangKe on 2019/3/21.
 */
public class WebSocketManager {

    private static final String TAG = "WSManager";

    private WebSocketSetting mSetting;

    private WebSocketWrapper mWebSocket;

    /**
     * 注册的监听器集合
     */
    private ResponseDelivery mDelivery;
    private ReconnectManager mReconnectManager;

    private SocketWrapperListener mSocketWrapperListener;
    /**
     * 当前是否已销毁
     */
    private boolean destroyed = false;
    /**
     * 用户调用了 disconnect 方法后为 true
     */
    private boolean disconnect = false;

    private WebSocketEngine mWebSocketEngine;
    private ResponseProcessEngine mResponseProcessEngine;

    WebSocketManager(WebSocketSetting setting,
                     WebSocketEngine webSocketEngine,
                     ResponseProcessEngine responseProcessEngine) {
        this.mSetting = setting;
        this.mWebSocketEngine = webSocketEngine;
        this.mResponseProcessEngine = responseProcessEngine;

        mDelivery = mSetting.getResponseDelivery();
        if (mDelivery == null) {
            mDelivery = new MainThreadResponseDelivery();
        }
        mSocketWrapperListener = getSocketWrapperListener();
        if (mWebSocket == null) {
            mWebSocket = new WebSocketWrapper(this.mSetting, mSocketWrapperListener);
        }
        start();
    }

    public void resetConnectState() {
        if (mWebSocket != null) {
            mWebSocket.setConnectState(0);
        }
    }

    /**
     * 启动，调用此方法开始连接
     */
    public WebSocketManager start() {
        if (mWebSocket == null) {
            mWebSocket = new WebSocketWrapper(this.mSetting, mSocketWrapperListener);
        }
        if (mWebSocket.getConnectState() == 0) {
            reconnect();
        }
        return this;
    }


    /**
     * WebSocket 是否已连接
     */
    public boolean isConnect() {
        return mWebSocket != null && mWebSocket.getConnectState() == 2;
    }

    /**
     * 设置重连管理类。
     * 用户可根据需求设置自己的重连管理类，只需要实现接口即可
     */
    public void setReconnectManager(ReconnectManager reconnectManager) {
        this.mReconnectManager = reconnectManager;
    }

    /**
     * 通过 {@link ReconnectManager} 开始重接
     */
    public WebSocketManager reconnect() {
        disconnect = false;
        if (mReconnectManager == null) {
            mReconnectManager = getDefaultReconnectManager();
        }
        if (!mReconnectManager.reconnecting()) {
            mReconnectManager.startReconnect();
        }
        return this;
    }

    /**
     * 使用新的 Setting 重新创建连接，同时会销毁之前的连接
     */
    public WebSocketManager reconnect(WebSocketSetting setting) {
        disconnect = false;
        if (destroyed) {
            LogUtil.e(TAG, "This WebSocketManager is destroyed!");
            return this;
        }
        this.mSetting = setting;
        if (mWebSocket != null) {
            mWebSocket.destroy();
            mWebSocket = null;
        }
        start();
        return this;
    }

    /**
     * 断开连接，断开后可使用 {@link this#reconnect()} 方法重新建立连接
     */
    public WebSocketManager disConnect() {
        disconnect = true;
        if (destroyed) {
            LogUtil.e(TAG, "This WebSocketManager is destroyed!");
            return this;
        }
        if (mWebSocket != null && mWebSocket.getConnectState() != 0 && mWebSocketEngine != null) {
            mWebSocketEngine.disConnect(mWebSocket, mSocketWrapperListener);
        }
        return this;
    }

    /**
     * 发送文本数据
     */
    public void send(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        Request<String> request = RequestFactory.createStringRequest();
        request.setRequestData(text);
        sendRequest(request);
    }

    HashMap<String, Object> mMap = new HashMap<>();

    public void sendChatGPT(BasicMessage msg) {
        if (msg == null || TextUtils.isEmpty(msg.getContent())) {
            return;
        }
//        Observer observer = new Observer<String>() {
//
//            @Override
//            public void onSubscribe(io.reactivex.rxjava3.disposables.@NonNull Disposable d) {
//
//            }
//
//            @Override
//            public void onNext(String response) {
//                com.common.lib.utils.LogUtil.LogE("response: "+response);
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        };
//        HttpMethods.Companion.getInstance().chatGPT(
//                "user", msg.getContent(),
//                observer);

        try {
            JSONObject object = new JSONObject();
            object.put("role", "user");
            object.put("content", msg.getContent());
            OkHttpClientManager.getInstance().post("http://im.metazoom.pro/api/v1/chat/send", object.toString(), new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    com.common.lib.utils.LogUtil.LogE(e);
                    msg.sendMsgFailed();
                    mMap.clear();
                    mMap.put(Constants.SEND_MSG_FAILED, msg.getMessageId());
                    EventBus.getDefault().post(mMap);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (response.body() == null) {
                        return;
                    }
                    String text = response.body().string();
                    com.common.lib.utils.LogUtil.LogE(text);
                    if (TextUtils.isEmpty(text)) {
                        return;
                    }
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("messageId", msg.getMessageId());
                    contentValues.put("sendStatus", 1);
                    DatabaseOperate.getInstance().update(msg, contentValues);
                    mMap.clear();
                    mMap.put(Constants.SEND_MSG_SUCCESS, msg.getMessageId());
                    EventBus.getDefault().post(mMap);
                    text = text
                            .replaceAll("data: \\[DONE\\]", "")
                            .replaceAll("data: ", ",")
                            .replaceAll("\n", "")
                            .substring(1);
                    com.common.lib.utils.LogUtil.LogE("response: " + text);
                    try {
                        String content = "";
                        JSONArray array = new JSONArray("[" + text + "]");
                        int length = array.length();
                        for (int i = 0; i < length; ++i) {
                            JSONObject ob = array.optJSONObject(i);
                            if (ob == null) {
                                continue;
                            }
                            JSONArray choices = ob.optJSONArray("choices");
                            if (choices == null || choices.length() == 0) {
                                continue;
                            }
                            int size = choices.length();
                            for (int j = 0; j < size; ++j) {
                                ob = choices.optJSONObject(j);
                                if (ob == null) {
                                    continue;
                                }
                                ob = ob.optJSONObject("delta");
                                if (ob == null) {
                                    continue;
                                }
                                String str = ob.optString("content");
                                if (!TextUtils.isEmpty(str)) {
                                    content += str;
                                }
                            }
                        }
                        MessageBean bean = new MessageBean();
                        bean.setCmd(2000);
                        bean.setFromId(-1);
                        bean.setToId(msg.getFromId());
                        bean.setExtra(msg.getExtra());
                        bean.setMsgType(MessageType.TYPE_TEXT.ordinal());
                        bean.setContent(content);
                        bean.setReceiveStatus(2);
                        DatabaseOperate.getInstance().insert(bean);
                        EventBus.getDefault().post(bean);
                    } catch (Exception e) {
                    }
                }
            });
        } catch (JSONException e) {
        }


    }

    /**
     * 发送 byte[] 数据
     */
    public void send(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        Request<byte[]> request = RequestFactory.createByteArrayRequest();
        request.setRequestData(bytes);
        sendRequest(request);
    }

    /**
     * 发送 ByteBuffer 数据
     */
    public void send(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return;
        }
        Request<ByteBuffer> request = RequestFactory.createByteBufferRequest();
        request.setRequestData(byteBuffer);
        sendRequest(request);
    }

    /**
     * 发送 Ping
     */
    public void sendPing() {
        sendRequest(RequestFactory.createPingRequest());
    }

    /**
     * 发送 Pong
     */
    public void sendPong() {
        sendRequest(RequestFactory.createPongRequest());
    }

    /**
     * 发送 Pong
     */
    public void sendPong(PingFrame pingFrame) {
        if (pingFrame == null) {
            return;
        }
        Request<PingFrame> request = RequestFactory.createPongRequest();
        request.setRequestData(pingFrame);
        sendRequest(request);
    }

    /**
     * 发送 {@link Framedata}
     */
    public void sendFrame(Framedata framedata) {
        if (framedata == null) {
            return;
        }
        Request<Framedata> request = RequestFactory.createFrameDataRequest();
        request.setRequestData(framedata);
        sendRequest(request);
    }

    /**
     * 发送 {@link Framedata} 集合
     */
    public void sendFrame(Collection<Framedata> frameData) {
        if (frameData == null) {
            return;
        }
        Request<Collection<Framedata>> request = RequestFactory.createCollectionFrameRequest();
        request.setRequestData(frameData);
        sendRequest(request);
    }

    /**
     * 添加一个监听器，使用完成之后需要调用
     * {@link #removeListener(SocketListener)} 方法移除监听器
     */
    public WebSocketManager addListener(SocketListener listener) {
        mDelivery.addListener(listener);
        return this;
    }

    /**
     * 移除一个监听器
     */
    public WebSocketManager removeListener(SocketListener listener) {
        mDelivery.removeListener(listener);
        return this;
    }

    /**
     * 获取配置类，
     * 部分参数支持动态设定。
     */
    public WebSocketSetting getSetting() {
        return mSetting;
    }

    /**
     * 彻底销毁该连接，销毁后改连接完全失效，
     * 请勿使用其他方法。
     */
    public void destroy() {
        destroyed = true;
        if (mWebSocket != null) {
            mWebSocketEngine.destroyWebSocket(mWebSocket);
            mWebSocketEngine = null;
            mWebSocket = null;
        }
        if (mDelivery != null) {
            if (!mDelivery.isEmpty()) {
                mDelivery.clear();
            }
            mDelivery = null;
        }
        if (mReconnectManager != null) {
            if (mReconnectManager.reconnecting()) {
                mReconnectManager.stopReconnect();
            }
            mReconnectManager = null;
        }
    }

    /**
     * 重新连接一次,
     * for {@link ReconnectManager}
     */
    void reconnectOnce() {
        if (destroyed) {
            LogUtil.e(TAG, "This WebSocketManager is destroyed!");
            return;
        }
        if (mWebSocket.getConnectState() == 0) {
            mWebSocketEngine.connect(mWebSocket, mSocketWrapperListener);
        } else {
            if (mReconnectManager != null) {
                mReconnectManager.onConnected();
            }
            LogUtil.e(TAG, "WebSocket 已连接，请勿重试。");
        }
    }

    /**
     * 发送数据
     */
    private void sendRequest(Request request) {
        if (destroyed) {
            LogUtil.e(TAG, "This WebSocketManager is destroyed!");
            return;
        }
        mWebSocketEngine.sendRequest(mWebSocket, request, mSocketWrapperListener);
    }

    /**
     * 获取默认的重连器
     */
    private ReconnectManager getDefaultReconnectManager() {
        return new DefaultReconnectManager(this, new ReconnectManager.OnConnectListener() {
            @Override
            public void onConnected() {
                LogUtil.i(TAG, "重连成功");
            }

            @Override
            public void onDisconnect() {
                LogUtil.i(TAG, "重连失败");
                mSetting.getResponseDispatcher()
                        .onDisconnect(mDelivery);
            }
        });
    }

    /**
     * 获取监听器
     */
    private SocketWrapperListener getSocketWrapperListener() {
        return new SocketWrapperListener() {
            @Override
            public void onConnected() {
                if (mReconnectManager != null) {
                    mReconnectManager.onConnected();
                }
                mSetting.getResponseDispatcher()
                        .onConnected(mDelivery);
            }

            @Override
            public void onConnectFailed(Throwable e) {
                //if reconnecting,interrupt this event for ReconnectManager.
                if (mReconnectManager != null &&
                        mReconnectManager.reconnecting()) {
                    mReconnectManager.onConnectError(e);
                }
                mSetting.getResponseDispatcher()
                        .onConnectFailed(e, mDelivery);
            }

            @Override
            public void onDisconnect() {
                mSetting.getResponseDispatcher()
                        .onDisconnect(mDelivery);
                if (mReconnectManager != null &&
                        mReconnectManager.reconnecting()) {
                    if (disconnect) {
                        mSetting.getResponseDispatcher()
                                .onDisconnect(mDelivery);
                    } else {
                        mReconnectManager.onConnectError(null);
                    }
                } else {
                    if (!disconnect) {
                        if (mReconnectManager == null) {
                            mReconnectManager = getDefaultReconnectManager();
                        }
                        mReconnectManager.onConnectError(null);
                        mReconnectManager.startReconnect();
                    }
                }
            }

            @Override
            public void onSendDataError(Request request, int type, Throwable tr) {
                ErrorResponse errorResponse = ResponseFactory.createErrorResponse();
                errorResponse.init(request, type, tr);
                if (mSetting.processDataOnBackground()) {
                    mResponseProcessEngine
                            .onSendDataError(errorResponse,
                                    mSetting.getResponseDispatcher(),
                                    mDelivery);
                } else {
                    mSetting.getResponseDispatcher().onSendDataError(errorResponse, mDelivery);
                }
                if (!disconnect && type == ErrorResponse.ERROR_NO_CONNECT) {
                    LogUtil.e(TAG, "数据发送失败，网络未连接，开始重连。。。");
                    reconnect();
                }
                //todo 使用完注意释放资源 request.release();
            }

            @Override
            public void onMessage(Response message) {
                if (mSetting.processDataOnBackground()) {
                    mResponseProcessEngine
                            .onMessageReceive(message,
                                    mSetting.getResponseDispatcher(),
                                    mDelivery);
                } else {
                    message.onResponse(mSetting.getResponseDispatcher(), mDelivery);
                }
            }
        };
    }
}
