package com.alsc.chat.http;

import android.text.TextUtils;
import android.util.Log;

import com.alsc.chat.utils.MD5Utils;
import com.alsc.chat.utils.Utils;
import com.alsc.chat.manager.ChatManager;
import com.alsc.chat.utils.NetUtil;
import com.common.lib.bean.*;
import com.common.lib.manager.DataManager;
import com.google.gson.GsonBuilder;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by gigabud on 17-5-3.
 */

public class ChatHttpMethods {

    private static final String TAG = "ChatHttpMethods";
    private Retrofit mRetrofit;
    private static final int DEFAULT_TIMEOUT = 30;
    private static ChatHttpMethods INSTANCE;
    private String mToken;
    private OkHttpClient.Builder mBuilder;

    private ChatHttpMethods() {
        //手动创建一个OkHttpClient并设置超时时间
        mBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.i(TAG, message);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request;
                String token = DataManager.getInstance().getToken();
                if (TextUtils.isEmpty(token)) {
                    token = mToken;
                }
                if (!TextUtils.isEmpty(token)) {
                    request = chain.request()
                            .newBuilder()
                            .addHeader("Authorization", token)
                            .build();
                } else {
                    request = chain.request()
                            .newBuilder()
                            .build();
                }
                long currentTime = System.currentTimeMillis() / 1000;
                String notice = UUID.randomUUID().toString().replace("-", "").toLowerCase();
                String authToken = "80a343d36784dfa87c76869ca320fe38";
                String url = request.url().toString();
                String reqToken = currentTime + "|" + notice + "|" + MD5Utils.encryptMD5(currentTime + notice + authToken + url);
                request.newBuilder()
                        .addHeader("X-YD-Req-Token", reqToken);
                return chain.proceed(request);
            }
        };

        mBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .addInterceptor(loggingInterceptor);
        resetRetrofit();
    }


    public static ChatHttpMethods getInstance() {
        if (INSTANCE == null) {
            synchronized (TAG) {
                if (INSTANCE == null) {
                    INSTANCE = new ChatHttpMethods();
                }
            }
        }
        return INSTANCE;
    }

    public void setToken(String token) {
        mToken = token;
    }


    public void resetRetrofit() {
        mRetrofit = new Retrofit.Builder()
                .client(mBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .setLenient()
                        .create()))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .baseUrl("http://im.metazoom.pro")//"http://utg-im.yunfanke.cn/")
                .build();
    }


    public void operateContact(long contactId, int star, String memo, int block, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("contactId", contactId);
        if (star >= 0) {
            map.put("star", star);
        }
        if (!TextUtils.isEmpty(memo)) {
            map.put("memo", memo);
        }
        if (block >= 0) {
            map.put("block", block);
        }
        Observable observable = httpService.operateContact(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    public void operateContact(long contactId, int top, int ignore, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("contactId", contactId);
        if (top >= 0) {
            map.put("top", top);
        }
        if (ignore >= 0) {
            map.put("ignore", ignore);
        }
        Observable observable = httpService.operateContact(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    public void removeContact(long contactId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("contactId", contactId);
        Observable observable = httpService.removeContact(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * 联系人列表
     *
     * @param star
     * @param observer
     */
    public void getFriends(int star, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.getFriends(
                Utils.getLanguageStr(DataManager.getInstance().getLanguage()),
                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(1)),
                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(Integer.MAX_VALUE - 1)),
                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(star)));
        toSubscribe(observable, observer);
    }

    public void getFriends(HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.getFriends(
                Utils.getLanguageStr(DataManager.getInstance().getLanguage()),
                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(1)),
                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(Integer.MAX_VALUE - 1)));
        toSubscribe(observable, observer);
    }

    /**
     * 获取黑名单
     *
     * @param block
     * @param observer
     */
    public void getBlockUsers(int block, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.getBlockUsers(
                Utils.getLanguageStr(DataManager.getInstance().getLanguage()),
                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(1)),
                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(Integer.MAX_VALUE - 1)),
                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(block)));
        toSubscribe(observable, observer);
    }


    /**
     * 申请添加联系人
     *
     * @param contactId 添加的联系人Id
     * @param memo      备注
     * @param remark    申请留言
     * @param addType
     * @param observer
     */
    public void addContact(String contactId, String memo, String remark, String addType, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.addContact(
                Utils.getLanguageStr(DataManager.getInstance().getLanguage())
                , RequestBody.create(MediaType.parse("text/plain"), contactId)
                , RequestBody.create(MediaType.parse("text/plain"), memo)
                , RequestBody.create(MediaType.parse("text/plain"), remark)
                , RequestBody.create(MediaType.parse("text/plain"), addType));
        toSubscribe(observable, observer);
    }

    /**
     * 查找指定联系人
     *
     * @param mobile   用户名
     * @param observer
     */
    public void searchContact(String mobile, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.searchContact(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), mobile));
        toSubscribe(observable, observer);
    }

    /**
     * 同意或拒绝添加联系人
     *
     * @param contactId 申请用户ID
     * @param status    1同意2拒绝3忽略
     * @param memo      备注信息
     * @param observer
     */
    public void replayContact(String contactId, String status, String memo, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.replayContact(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), contactId)
                , RequestBody.create(MediaType.parse("text/plain"), status)
                , RequestBody.create(MediaType.parse("text/plain"), memo));
        toSubscribe(observable, observer);
    }

    /**
     * 待处理联系人申请列表
     *
     * @param observer
     */
    public void reviewContact(HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.reviewContact(Utils.getLanguageStr(DataManager.getInstance().getLanguage()));
        toSubscribe(observable, observer);
    }

    /**
     * 退出群
     *
     * @param groupId  群ID
     * @param observer
     */
    public void exitGroup(String groupId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.exitGroup(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId));
        toSubscribe(observable, observer);
    }

    /**
     * 获取群好友列表
     *
     * @param groupId  群ID
     * @param observer
     */
    public void getGroupUsers(String groupId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.getGroupUsers(
                Utils.getLanguageStr(DataManager.getInstance().getLanguage()),
                RequestBody.create(MediaType.parse("text/plain"), groupId));
        toSubscribe(observable, observer);
    }

    /**
     * 禁言成员列表
     *
     * @param groupId  群ID
     * @param block
     * @param observer
     */
    public void getGroupUsers(String groupId, String block, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.getGroupBlockUsers(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId),
                RequestBody.create(MediaType.parse("text/plain"), block));
        toSubscribe(observable, observer);
    }

    /**
     * 禁言成员列表
     *
     * @param groupId  群ID
     * @param observer
     */
    public void dismissGroup(String groupId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.dismissGroup(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId));
        toSubscribe(observable, observer);
    }

    /**
     * 屏蔽词列表
     *
     * @param groupId  群ID
     * @param observer
     */
    public void groupBlockList(String groupId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.groupBlockList(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId));
        toSubscribe(observable, observer);
    }

    /**
     * 删除屏蔽词
     *
     * @param groupId  群ID
     * @param blockId
     * @param observer
     */
    public void groupBlockDelete(String groupId, String blockId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.groupBlockDelete(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId),
                RequestBody.create(MediaType.parse("text/plain"), blockId));
        toSubscribe(observable, observer);
    }

    /**
     * 添加屏蔽词
     *
     * @param groupId  群ID
     * @param content
     * @param observer
     */
    public void groupBlockCreate(String groupId, String content, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.groupBlockCreate(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId),
                RequestBody.create(MediaType.parse("text/plain"), content));
        toSubscribe(observable, observer);
    }

    /**
     * 修改群聊信息
     *
     * @param groupId      群ID
     * @param name         群名称
     * @param ownerId      群所有者ID（默认0为不修改)
     * @param notice       群公告
     * @param introduction 群介绍
     * @param observer
     */
    public void updateGroup(String groupId, String name, String ownerId, String notice, String introduction, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.updateGroup(
                Utils.getLanguageStr(DataManager.getInstance().getLanguage()),
                RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), name)
                , RequestBody.create(MediaType.parse("text/plain"), ownerId)
                , RequestBody.create(MediaType.parse("text/plain"), notice)
                , RequestBody.create(MediaType.parse("text/plain"), introduction));
        toSubscribe(observable, observer);
    }

    public void updateGroupName(String groupId, String name, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.updateGroupName(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), name));
        toSubscribe(observable, observer);
    }

    public void updateGroupNotice(String groupId, String notice, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.updateGroupNotice(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), notice));
        toSubscribe(observable, observer);
    }

    public void updateGroupOwner(String groupId, String ownerId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.updateGroupOwner(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), ownerId));
        toSubscribe(observable, observer);
    }

    public void updateEnterGroupPay(String groupId, String payinState, String payAmount, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.updateEnterGroupPay(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), payinState)
                , RequestBody.create(MediaType.parse("text/plain"), payAmount));
        toSubscribe(observable, observer);
    }

    public void updateEnterGroupType(String groupId, String joinType, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.updateEnterGroupType(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), joinType));
        toSubscribe(observable, observer);
    }

    public void updateEnterGroupStint(String groupId, String joinStint, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.updateEnterGroupStint(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), joinStint));
        toSubscribe(observable, observer);
    }


    public void updateGroupIcon(String groupId, String iconUrl, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.updateGroupIcon(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), iconUrl));
        toSubscribe(observable, observer);
    }

    public void allBlock(String groupId, String allBlock, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.allBlock(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), allBlock));
        toSubscribe(observable, observer);
    }

    public void disableFriend(String groupId, String disableFriend, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.disableFriend(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), disableFriend));
        toSubscribe(observable, observer);
    }

    public void disableLink(String groupId, String disableLink, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.disableLink(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), disableLink));
        toSubscribe(observable, observer);
    }

    /**
     * 创建群聊
     *
     * @param name     群聊名称
     * @param users
     * @param observer
     */
    public void createGroup(String name, ArrayList<Long> users, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("userIds", users);
        Observable observable = httpService.createGroup(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * 群详细信息
     *
     * @param groupId
     * @param observer
     */
    public void getGroupInfo(long groupId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        Observable observable = httpService.getGroupInfo(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    public void getGroups(int currentPage, int pageSize, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.getGroups(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentPage))
                , RequestBody.create(MediaType.parse("text/plain"), String.valueOf(pageSize)));
        toSubscribe(observable, observer);
    }

    public void newcomerList(String groupId, int type, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.newcomerList(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId),
                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(type)));
        toSubscribe(observable, observer);
    }

    /**
     * 修改群成员群内备注
     *
     * @param groupId
     * @param memo
     * @param editUserId
     * @param observer
     */
    public void updateGroupMemo(String groupId, String memo, String editUserId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable;
        if (TextUtils.isEmpty(editUserId)) {
            observable = httpService.updateGroupMemo(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                    , RequestBody.create(MediaType.parse("text/plain"), memo));
        } else {
            observable = httpService.updateGroupMemo(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                    , RequestBody.create(MediaType.parse("text/plain"), memo),
                    RequestBody.create(MediaType.parse("text/plain"), editUserId));
        }
        toSubscribe(observable, observer);
    }

    public void updateGroupTop(String groupId, int top, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.groupTop(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), String.valueOf(top)));
        toSubscribe(observable, observer);
    }

    public void updateGroupIgnore(String groupId, int ignore, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.groupIgnore(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), groupId)
                , RequestBody.create(MediaType.parse("text/plain"), String.valueOf(ignore)));
        toSubscribe(observable, observer);
    }

    /**
     * 邀请加入群组
     *
     * @param groupId
     * @param users
     * @param observer
     */
    public void inviteToGroup(long groupId, ArrayList<Long> users, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("inviteId", users);
        Observable observable = httpService.inviteToGroup(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * 添加/解除 群成员禁言
     *
     * @param groupId
     * @param blockId
     * @param blockType
     * @param observer
     */
    public void addOrRemoveGroupBlock(long groupId, ArrayList<Long> blockId, int blockType, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("blockId", blockId);
        map.put("blockType", blockType);
        Observable observable = httpService.addOrRemoveGroupBlock(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * @param groupId
     * @param observer
     */
    public void groupQrcode(long groupId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        Observable observable = httpService.groupQrcode(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * 群主踢人
     *
     * @param groupId
     * @param userIds
     * @param observer
     */
    public void kickOutUser(long groupId, ArrayList<Long> userIds, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("kickUserId", userIds);
        Observable observable = httpService.kickout(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * 红包/转账详情
     *
     * @param envelopeId
     * @param observer
     */
    public void envelopeDetail(String envelopeId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("envelopeId", envelopeId);
        Observable observable = httpService.envelopeDetail(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * 领取红包/转账
     *
     * @param envelopeId
     * @param observer
     */
    public void envelopeDraw(String envelopeId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("envelopeId", envelopeId);
        Observable observable = httpService.envelopeDraw(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * 撤回新人红包
     *
     * @param envelopeId
     * @param observer
     */
    public void unLockNewEnvelope(String envelopeId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("envelopeId", envelopeId);
        Observable observable = httpService.unLockNewEnvelope(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * 转账或者发红包
     *
     * @param amount     总金额
     * @param totalCount 数量
     * @param remark     备注
     * @param type       类型 1普通红包2手气红包3转转
     * @param toId       转账收取人Id
     * @param groupId    群Id
     * @param observer
     */
    public void envelopeSend(float amount, int totalCount, String remark, int type,
                             long toId, long groupId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("amount", amount);
        if (totalCount > 0) {
            map.put("totalCount", totalCount);
        }
        if (!TextUtils.isEmpty(remark)) {
            map.put("remark", remark);
        }
        map.put("type", type);
        if (toId > 0) {
            map.put("toId", toId);
        }
        if (groupId > 0) {
            map.put("groupId", groupId);
        }
        Observable observable = httpService.envelopeSend(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * @param type     1我收到的红包2我发出的红包
     * @param observer
     */
    public void envelopeList(int type, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("currentPage", 1);
        map.put("pageSize", Integer.MAX_VALUE - 1);
        Observable observable = httpService.envelopeList(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * @param name
     * @param users
     * @param observer
     */
    public void createLabel(String name, ArrayList<UserBean> users, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        HashMap<String, Object> itemMap;
        for (UserBean userBean : users) {
            itemMap = new HashMap<>();
            itemMap.put("contactId", userBean.getContactId());
            list.add(itemMap);
        }
        map.put("contacts", list);
        Observable observable = httpService.createTag(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * @param tagId
     * @param name
     * @param users
     * @param observer
     */
    public void editLabel(long tagId, String name, ArrayList<UserBean> users, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("tagId", tagId);
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        HashMap<String, Object> itemMap;
        for (UserBean userBean : users) {
            itemMap = new HashMap<>();
            itemMap.put("contactId", userBean.getContactId());
            list.add(itemMap);
        }
        map.put("contacts", list);
        Observable observable = httpService.editTag(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * @param observer
     */
    public void getLabels(HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("currentPage", 1);
        map.put("pageSize", Integer.MAX_VALUE - 1);
        Observable observable = httpService.getTagList(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * @param tagId
     * @param observer
     */
    public void deleteLabel(long tagId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("tagId", tagId);
        Observable observable = httpService.deleteTag(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * @param observer
     */
    public void getLabelFriends(long tagId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("tagId", tagId);
        map.put("currentPage", 1);
        map.put("pageSize", Integer.MAX_VALUE - 1);
        Observable observable = httpService.tagContacts(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * @param nickName
     * @param avatarUrl
     * @param gender
     * @param district
     * @param observer
     */
    public void updateUserProfile(String nickName, String avatarUrl, int gender, String district, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        if (!TextUtils.isEmpty(nickName)) {
            map.put("nickName", nickName);
        }
        if (!TextUtils.isEmpty(avatarUrl)) {
            map.put("avatarUrl", avatarUrl);
        }
        if (gender >= 0) {
            map.put("gender", gender);
        }
        if (!TextUtils.isEmpty(district)) {
            map.put("district", district);
        }
        Observable observable = httpService.updateUserProfile(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    public void updateUserAllowAdd(int allowAdd, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("allowAdd", allowAdd);
        Observable observable = httpService.updateUserProfile(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * @param observer
     */
    public void getUserProfile(HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.getUserProfile(Utils.getLanguageStr(DataManager.getInstance().getLanguage()));
        toSubscribe(observable, observer);
    }

    /**
     * @param contactId
     * @param observer
     */
    public void getContactProfile(String contactId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.getContactProfile(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), RequestBody.create(MediaType.parse("text/plain"), contactId));
        toSubscribe(observable, observer);
    }


    /**
     * @param content
     * @param contact
     * @param pic
     * @param observer
     */
    public void suggest(String content, String contact, ArrayList<String> pic, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("content", content);
        if (!TextUtils.isEmpty(contact)) {
            map.put("contact", contact);
        }
        if (pic != null && !pic.isEmpty()) {
            map.put("pic", pic);
        }
        Observable observable = httpService.suggest(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * 自动回复问题列表
     *
     * @param observer
     */
    public void questionList(HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.questionList(Utils.getLanguageStr(DataManager.getInstance().getLanguage()));
        toSubscribe(observable, observer);
    }

    /**
     * 自动回复问题详情
     *
     * @param questionId
     * @param observer
     */
    public void questionDetail(long questionId, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("questionId", questionId);
        Observable observable = httpService.questionDetail(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    /**
     * 在线客服列表
     *
     * @param observer
     */
    public void questionOnline(HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.questionOnline(Utils.getLanguageStr(DataManager.getInstance().getLanguage()));
        toSubscribe(observable, observer);
    }

    /**
     * 转账手续费
     *
     * @param observer
     */
    public void transferFee(HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        Observable observable = httpService.transferFee(Utils.getLanguageStr(DataManager.getInstance().getLanguage()));
        toSubscribe(observable, observer);
    }

    /**
     * 推送
     *
     * @param registrationId
     * @param deviceName
     * @param observer
     */
    public void userDevice(String registrationId, String deviceName, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("registrationId", registrationId);
        map.put("deviceName", deviceName);
        Observable observable = httpService.userDevice(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    public void addCollection(String msgId, String content, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("msgId", msgId);
        map.put("content", content);
        Observable observable = httpService.addCollection(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    public void delCollection(long id, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        Observable observable = httpService.delCollection(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }

    public void getCollections(int currentPage, HttpObserver observer) {
        ChatHttpService httpService = mRetrofit.create(ChatHttpService.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("currentPage", currentPage);
        map.put("pageSize", 20);
        Observable observable = httpService.getCollections(Utils.getLanguageStr(DataManager.getInstance().getLanguage()), map);
        toSubscribe(observable, observer);
    }


    private <T> void toSubscribe(Observable<T> o, HttpObserver s) {
        o.retry(2, new Predicate<Throwable>() {
                    @Override
                    public boolean test(@NonNull Throwable throwable) throws Exception {
                        return NetUtil.isConnected(ChatManager.getInstance().getContext()) &&
                                (throwable instanceof SocketTimeoutException ||
                                        throwable instanceof ConnectException ||
                                        throwable instanceof ConnectTimeoutException ||
                                        throwable instanceof TimeoutException);
                    }
                }).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s);
    }

}
