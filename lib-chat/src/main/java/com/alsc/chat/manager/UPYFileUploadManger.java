package com.alsc.chat.manager;

import com.alsc.chat.utils.Constants;
import com.common.lib.bean.BasicMessage;
import com.common.lib.bean.FileBean;
import com.common.lib.bean.MessageType;
import com.common.lib.bean.UploadAvatarEvent;
import com.upyun.library.common.Params;
import com.upyun.library.common.UploadEngine;
import com.upyun.library.listener.UpCompleteListener;
import com.upyun.library.utils.UpYunUtils;
import com.zhangke.websocket.WebSocketHandler;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class UPYFileUploadManger {

    private static final String TAG = "UPYFileUploadManger";

    private static UPYFileUploadManger mUPYFileUploadManger;

    private HashMap<String, Object> mMap = new HashMap<>();

    private HashMap<String, Integer> mRetryMap = new HashMap<>();


    private UPYFileUploadManger() {
    }

    public static UPYFileUploadManger getInstance() {
        if (mUPYFileUploadManger == null) {
            synchronized (TAG) {
                if (mUPYFileUploadManger == null) {
                    mUPYFileUploadManger = new UPYFileUploadManger();
                }
            }
        }
        return mUPYFileUploadManger;
    }


    public void formUpload(final BasicMessage msg, final FileBean fileBean) {
        final File file = fileBean.getFile();
        if (file == null || !file.exists()) {
            return;
        }
        if (!mRetryMap.containsKey(msg.getMessageId())) {
            mRetryMap.put(msg.getMessageId(), 1);
        }
        final Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(Params.BUCKET, Constants.SPACE);
        int fileType = msg.getMsgType();
        if (fileType == MessageType.TYPE_IMAGE.ordinal()) {
            paramsMap.put(Params.SAVE_KEY, String.format(Constants.IMAGE_SAVE_PATH, file.getName()));
        } else if (fileType == MessageType.TYPE_VOICE.ordinal()) {
            paramsMap.put(Params.SAVE_KEY, String.format(Constants.VOICE_SAVE_PATH, file.getName()));
        } else if (fileType == MessageType.TYPE_VIDEO.ordinal()) {
            paramsMap.put(Params.SAVE_KEY, String.format(Constants.VIDEO_SAVE_PATH, file.getName()));
        } else if (fileType == MessageType.TYPE_LOCATION.ordinal()) {
            paramsMap.put(Params.SAVE_KEY, String.format(Constants.MAP_SAVE_PATH, file.getName()));
        } else if (fileType == MessageType.TYPE_FILE.ordinal()) {
            paramsMap.put(Params.SAVE_KEY, String.format(Constants.FILE_SAVE_PATH, file.getName()));
        }
        paramsMap.put(Params.CONTENT_LENGTH, file.length());
        paramsMap.put(Params.RETURN_URL, "httpbin.org/post");
//        UpProgressListener progressListener = new UpProgressListener() {
//            @Override
//            public void onRequestProgress(long bytesWrite, long contentLength) {
//                int progress = (int) ((100 * bytesWrite) / contentLength);
//                msg.setFileProgress(progress);
//                EventBus.getDefault().post(getRefreshProgressMap(msg));
//            }
//        };

        //结束回调，不可为空
        UpCompleteListener completeListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, Response response, Exception error) {
                try {
                    if (isSuccess) {
                        mRetryMap.remove(msg.getMessageId());
//                        msg.setFileProgress(100);
//                        mMap.clear();
//                        mMap.put(Constants.REFRESH_PROGRESS, msg);
//                        EventBus.getDefault().post(mMap);
                        WebSocketHandler.getDefault().send(msg.toJson());
                    } else {
                        int retry = 1;
                        if (mRetryMap.containsKey(msg.getMessageId())) {
                            retry = mRetryMap.get(msg.getMessageId());
                        }
                        if (retry < 3) {
                            mRetryMap.put(msg.getMessageId(), ++retry);
                            formUpload(msg, fileBean);
                            return;
                        }
                        mRetryMap.remove(msg.getMessageId());
                        msg.sendFileFailed();
                        mMap.clear();
                        mMap.put(Constants.SEND_MSG_FAILED, msg.getMessageId());
                        EventBus.getDefault().post(mMap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    int retry = 1;
                    if (mRetryMap.containsKey(msg.getMessageId())) {
                        retry = mRetryMap.get(msg.getMessageId());
                    }
                    if (retry < 3) {
                        mRetryMap.put(msg.getMessageId(), ++retry);
                        formUpload(msg, fileBean);
                        return;
                    }
                    mRetryMap.remove(msg.getMessageId());
                    msg.sendFileFailed();
                    mMap.clear();
                    mMap.put(Constants.SEND_MSG_FAILED, msg.getMessageId());
                    EventBus.getDefault().post(mMap);
                }

            }
        };
        UploadEngine.getInstance().formUpload(file, paramsMap, Constants.OPERATER, UpYunUtils.md5(Constants.PASSWORD), completeListener, null);
    }


    public void uploadFile(File file) {

        final Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(Params.BUCKET, Constants.SPACE);
        paramsMap.put(Params.SAVE_KEY, String.format(Constants.AVATAR_SAVE_PATH, file.getName()));
        paramsMap.put(Params.CONTENT_LENGTH, file.length());
        paramsMap.put(Params.RETURN_URL, "httpbin.org/post");

        if (!mRetryMap.containsKey(file.getName())) {
            mRetryMap.put(file.getName(), 1);
        }

        //结束回调，不可为空
        UpCompleteListener completeListener = new UpCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, Response response, Exception error) {
                try {
                    String result = null;
                    if (response != null) {
                        result = response.body().string();
                    } else if (error != null) {
                        result = error.toString();
                    }
                    if (isSuccess) {
                        mRetryMap.remove(file.getName());
                        JSONObject jsonObject = new JSONObject(result);
                        String url = Constants.IMAGE_HTTP_HOST + jsonObject.getString("url");
                        EventBus.getDefault().post(new UploadAvatarEvent(isSuccess, url, ""));
                    } else {
                        int retry = 1;
                        if (mRetryMap.containsKey(file.getName())) {
                            retry = mRetryMap.get(file.getName());
                        }
                        if (retry < 3) {
                            mRetryMap.put(file.getName(), ++retry);
                            uploadFile(file);
                            return;
                        }
                        EventBus.getDefault().post(new UploadAvatarEvent(isSuccess, "", error.getMessage()));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    int retry = 1;
                    if (mRetryMap.containsKey(file.getName())) {
                        retry = mRetryMap.get(file.getName());
                    }
                    if (retry < 3) {
                        mRetryMap.put(file.getName(), ++retry);
                        uploadFile(file);
                        return;
                    }
                    EventBus.getDefault().post(new UploadAvatarEvent(false, "", e.toString()));
                }

            }
        };
        UploadEngine.getInstance().formUpload(file, paramsMap, Constants.OPERATER, UpYunUtils.md5(Constants.PASSWORD), completeListener, null);
    }
}
