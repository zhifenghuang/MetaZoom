package com.meta.zoom.wallet.util;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 网络请求类
 *
 * @author rainking
 */
public class HttpUtil {
    private static final String TAG = HttpUtil.class.getSimpleName();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static HttpUtil mHttpUtil;
    private static OkHttpClient client = new OkHttpClient();

    public static HttpUtil getInstance() {
        if (mHttpUtil == null) {
            synchronized (TAG) {
                if (mHttpUtil == null) {
                    mHttpUtil = new HttpUtil();
                }
            }
        }
        return mHttpUtil;
    }


    /**
     * 同步请求方法 get
     *
     * @param url 请求地址
     * @return
     */
    public Response getRequest(String url) throws IOException {
        OkHttpClient client = new OkHttpClient(new OkHttpClient.Builder());
        Request request = new Request.Builder().url(url).build();
        return client.newCall(request).execute();
    }

    /**
     * 同步请求方法 get
     *
     * @param url 请求地址
     * @return String
     */
    public String get(String url) throws Exception {
        try {
            Response response = getRequest(url);
            //响应成功
            if (response != null && response.isSuccessful() && response.body() != null) {
                return Objects.requireNonNull(response.body()).string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new Exception("接口请求失败");
    }

    /**
     * 同步请求post
     *
     * @param url
     * @param json
     * @return
     * @throws Exception
     */
    public String post(String url, String json) throws Exception {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        //响应成功
        if (response.isSuccessful() && response.body() != null) {
            return Objects.requireNonNull(response.body()).string();
        }
        throw new Exception("接口请求失败");
    }

    public String post(String url, HashMap<String, String> map) throws Exception {
        FormBody.Builder builder = new FormBody.Builder();
        Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();
        Map.Entry<String, String> entry;
        while (entries.hasNext()) {
            entry = entries.next();
            builder.add(entry.getKey(), entry.getValue());
        }
        RequestBody body = builder.build();
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        //响应成功
        if (response.isSuccessful() && response.body() != null) {
            return Objects.requireNonNull(response.body()).string();
        }
        throw new Exception("接口请求失败");
    }

    private final String[] userAgent = new String[]{"Mozilla/4.0 (compatible; MSIE 7.0; Windows 7)",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0",
            "Mozilla/5.0 (Windows NT 8.0; WOW64; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (Windows NT 9.0; WOW64; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko"};

    /**
     * 同步请求方法 get
     *
     * @param url 请求地址
     * @return String
     */
    public String getRandom(String url) throws Exception {
        try {
            Random random = new Random();
            int number = random.nextInt(userAgent.length);
            OkHttpClient client = new OkHttpClient(new OkHttpClient.Builder());
            Request request = new Request.Builder().url(url)
                    .removeHeader("User-Agent")
                    .addHeader("User-Agent", userAgent[number]).build();
            Response response = client.newCall(request).execute();
            //响应成功
            if (response.isSuccessful() && response.body() != null) {
                return Objects.requireNonNull(response.body()).string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new Exception("接口请求失败");
    }
}
