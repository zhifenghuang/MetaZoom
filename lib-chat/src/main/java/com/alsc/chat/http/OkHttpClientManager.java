package com.alsc.chat.http;

import android.text.TextUtils;
import android.util.Log;

import com.alsc.chat.manager.ChatManager;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by zhy on 15/8/17.
 */
public class OkHttpClientManager {

    private static final String TAG = "OkHttpClientManager";

    private static OkHttpClientManager mInstance;
    private OkHttpClient mOkHttpClient;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType XML = MediaType.parse("application/xml; charset=utf-8");

    private HashMap<String, Long> mDownloadingUrl = new HashMap<>();
    private ArrayList<String> mNeedDownLoadingUrl = new ArrayList<>();

    private OkHttpClientManager() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

    }

    public static OkHttpClientManager getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpClientManager.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpClientManager();
                }
            }
        }
        return mInstance;
    }

    public void post(String url, RequestBody body, final Callback callback) {
        //RequestBody body = new FormBody.Builder().add("useName", "addd").add("pwd", "123").build();
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public void post(String url, String json, final Callback callback) {
        //RequestBody body = new FormBody.Builder().add("useName", "addd").add("pwd", "123").build();
        RequestBody body = RequestBody.create(JSON, json);
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public void postXml(String url, String xmlStr, final Callback callback) {
        //RequestBody body = new FormBody.Builder().add("useName", "addd").add("pwd", "123").build();
        RequestBody body = RequestBody.create(XML, xmlStr);
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public void get(String url, final Callback callback) {

        CacheControl cc = new CacheControl.Builder()
                .noCache()
                .noStore()
                .maxStale(5, TimeUnit.SECONDS)
                .build();

        final Request request = new Request.Builder()
                .cacheControl(cc)
                .url(url)
                .build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(callback);
    }


    /**
     * 异步下载文件
     *
     * @param url
     * @param file 本地文件存储的文件夹
     */
    public synchronized void downloadAsyn(final String url, final File file, final HttpCallBack callBack) {
        if (file.exists() || TextUtils.isEmpty(url)) {
            return;
        }
        if (mDownloadingUrl.size() < 5) {
            if (!mDownloadingUrl.containsKey(url)) {
                mDownloadingUrl.put(url, System.currentTimeMillis());
            } else {
                if (System.currentTimeMillis() - mDownloadingUrl.get(url) > 10 * 60 * 1000) {  //超过10分钟还没下载完重新下载
                    mDownloadingUrl.put(url, System.currentTimeMillis());
                } else {
                    return;
                }
            }
        } else {
            mNeedDownLoadingUrl.add(url);
            return;
        }
        final Request request = new Request.Builder()
                .url(url)
                .build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                if (callBack != null) {
                    callBack.failed(e);
                }
                mDownloadingUrl.remove(url);
                file.delete();
                downloadNext(file.getName());
            }

            @Override
            public void onResponse(Call call, Response response) {
                InputStream is = null;
                FileOutputStream out = null;
                try {
                    is = response.body().byteStream();
                    File tempFile = new File(Utils.getSaveFilePath(ChatManager.getInstance().getContext(), file.getName() + ".download"));
                    out = new FileOutputStream(tempFile);
                    byte[] buf = new byte[4096];
                    int len = 0;
                    while ((len = is.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    out.flush();
                    tempFile.renameTo(file);
                    if (callBack != null) {
                        callBack.successful();
                    }
                    HashMap<String, String> map = new HashMap<>();
                    map.put(Constants.DOWNLOAD_FILE, url);
                    EventBus.getDefault().post(map);
                } catch (Exception e) {
                    if (callBack != null) {
                        callBack.failed(e);
                    }
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                    } catch (Exception e) {
                        if (callBack != null) {
                            callBack.failed(e);
                        }
                    }
                    mDownloadingUrl.remove(url);
                    downloadNext(file.getName());
                }
            }
        });
    }

    /**
     * 异步下载文件
     *
     * @param url
     * @param file 本地文件存储的文件夹
     */
    public synchronized void downloadAsynWithProgress(final String url, final File file, final HttpCallBack callBack) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        final Request request = new Request.Builder()
                .url(url)
                .build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "e: " + e);
                if (callBack != null) {
                    callBack.failed(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                InputStream is = null;
                FileOutputStream out = null;
                try {
                    ResponseBody body = response.body();
                    long total = body.contentLength();
                    if (total == 0) {
                        if (callBack != null) {
                            callBack.failed(null);
                        }
                        return;
                    }
                    is = body.byteStream();
                    File tempFile = new File(Utils.getSaveFilePath(ChatManager.getInstance().getContext(), file.getName() + ".download"));
                    out = new FileOutputStream(tempFile);
                    byte[] buf = new byte[4096];
                    int len = 0;
                    long progress = 0;
                    while ((len = is.read(buf)) != -1) {
                        out.write(buf, 0, len);
                        progress += len;
                        if (callBack != null) {
                            callBack.progress((int) (progress * 100 / total + 0.5));
                        }
                    }
                    out.flush();
                    tempFile.renameTo(file);
                    if (callBack != null) {
                        callBack.successful();
                    }
                } catch (Exception e) {
                    if (callBack != null) {
                        callBack.failed(e);
                    }
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                    } catch (Exception e) {
                        if (callBack != null) {
                            callBack.failed(e);
                        }
                    }
                }
            }
        });
    }

    private synchronized void downloadNext(String fileName) {
        if (mNeedDownLoadingUrl.isEmpty()) {
            return;
        }
        String url = mNeedDownLoadingUrl.remove(mNeedDownLoadingUrl.size() - 1);
        final File path = new File(Utils.getSaveFilePath(ChatManager.getInstance().getContext(), fileName));
        if (!path.exists()) {
            downloadAsyn(url, path, null);
        }
    }


    public void baiduTranslate(String text, String to, Callback callback) {
        long time = System.currentTimeMillis();
        String sign = Utils.encryptMD5(Constants.BAIDU_TRANSLATE_APPID + text + time + Constants.BAIDU_TRANSLATE_SECRET);
        StringBuilder sb = new StringBuilder("http://api.fanyi.baidu.com/api/trans/vip/translate?q=");
        sb.append(text);
        sb.append("&from=auto&to=");
        sb.append(to);
        sb.append("&appid=");
        sb.append(Constants.BAIDU_TRANSLATE_APPID);
        sb.append("&salt=");
        sb.append(time);
        sb.append("&sign=");
        sb.append(sign);
        String url = sb.toString();
        get(url, callback);
    }

    public interface HttpCallBack {
        public void successful();

        public void progress(int progress);

        public void failed(Exception e);
    }

}
