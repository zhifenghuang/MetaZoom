package com.common.lib.manager;

import android.text.TextUtils;

import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.ChainBean;
import com.common.lib.bean.ChatSettingBean;
import com.common.lib.bean.ChatSubBean;
import com.common.lib.bean.FilterMsgBean;
import com.common.lib.bean.GroupBean;
import com.common.lib.bean.LabelBean;
import com.common.lib.bean.QuestionBean;
import com.common.lib.bean.TransferFeeBean;
import com.common.lib.bean.UserBean;
import com.common.lib.bean.WalletBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataManager {

    private static final String TAG = "DataManager";
    private static DataManager mDataManager;

    private UserBean mMyInfo;

    private Object mObject;

    private Gson mGson;

    private WalletBean mWallet;

    private ChainBean mChain;

    private DataManager() {

    }

    public static DataManager getInstance() {
        if (mDataManager == null) {
            synchronized (TAG) {
                if (mDataManager == null) {
                    mDataManager = new DataManager();
                }
            }
        }
        return mDataManager;
    }

    public Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }

    public String getToken() {
        return getUser() == null ? "" : getUser().getToken();
    }


    public void saveCurrentChain(ChainBean chain) {
        if (chain == null) {
            mChain = null;
            Preferences.getInstacne().setValues(
                    "current_chain",
                    ""
            );
            return;
        }
        mChain = chain;
        Preferences.getInstacne().setValues(
                "current_chain",
                getGson().toJson(mChain)
        );
    }

    public ChainBean getCurrentChain() {
        if (mChain == null) {
            String str =
                    Preferences.getInstacne().getValues("current_chain", "");
            if (TextUtils.isEmpty(str)) {
                mChain = DatabaseOperate.getInstance().getChainList().get(0);
                saveCurrentChain(mChain);
            } else {
                mChain = getGson().fromJson(str, ChainBean.class);
            }
        }
        return mChain;
    }

    public void saveCurrentWallet(WalletBean wallet) {
        if (wallet == null) {
            mWallet = null;
            Preferences.getInstacne().setValues(
                    "current_wallet",
                    ""
            );
            return;
        }
        mWallet = wallet;
        Preferences.getInstacne().setValues(
                "current_wallet",
                getGson().toJson(wallet)
        );
    }

    public WalletBean getCurrentWallet() {
        if (mWallet == null) {
            String str =
                    Preferences.getInstacne().getValues("current_wallet", "");
            if (TextUtils.isEmpty(str)) {
                return null;
            }
            mWallet = getGson().fromJson(str, WalletBean.class);
        }
        return mWallet;
    }


    public Object getObjectByKey(String key, Type tClass) {
        String str = Preferences.getInstacne().getValues(key, "");
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return getGson().fromJson(str, tClass);
    }

    public void saveObjectByKey(String key, Object object) {
        Preferences.getInstacne().setValues(key, getGson().toJson(object));
    }

    public void saveUser(UserBean userBean) {
        mMyInfo = userBean;
        Preferences.getInstacne().setValues("user", getGson().toJson(userBean));
    }

    public UserBean getUser() {
        if (mMyInfo == null) {
            String str = Preferences.getInstacne().getValues("user", "");
            if (TextUtils.isEmpty(str)) {
                return null;
            }
            mMyInfo = getGson().fromJson(str, UserBean.class);
        }
        return mMyInfo;
    }

    public void setVersionName(String versionName) {
        Preferences.getInstacne().setValues("versionName", versionName);
    }

    public String getVersionName() {
        return Preferences.getInstacne().getValues("versionName", "1.3.6");
    }

    public void setVersionCode(int versionCode) {
        Preferences.getInstacne().setValues("versionCode", versionCode);
    }

    public int getVersionCode() {
        return Preferences.getInstacne().getValues("versionCode", 37);
    }


    public long getUserId() {
        return getUser() == null ? 0 : getUser().getUserId();
    }


    public void saveLanguage(int language) {
        Preferences.getInstacne().setValues("language", language);
    }

    public int getLanguage() {
        return Preferences.getInstacne().getValues("language", 1);
    }


    public void saveNotSeeAsset(int notSeeAsset) {
        Preferences.getInstacne().setValues("notSeeAsset", notSeeAsset);
    }

    public int getNotSeeAsset() {
        return Preferences.getInstacne().getValues("notSeeAsset", 0);
    }

    public void saveArea(int area) {
        Preferences.getInstacne().setValues("area", area);
    }

    public int getArea() {
        return Preferences.getInstacne().getValues("area", 0);
    }

    public void savePayType(int payType) {
        Preferences.getInstacne().setValues("pay_type", payType);
    }

    public int getPayType() {
        return Preferences.getInstacne().getValues("pay_type", 0);
    }

    public void saveTransferFee(float fee) {
        Preferences.getInstacne().setValues("transfer_fee", fee);
    }

    public float getTransferFee() {
        return Preferences.getInstacne().getValues("transfer_fee", 0.0f);
    }

    public void saveUTGPrice(String price) {
        Preferences.getInstacne().setValues("utg_price", price);
    }

    public String getUTGPrice() {
        return Preferences.getInstacne().getValues("utg_price", "0.00");
    }

    public void saveTransferFeeBean(TransferFeeBean fee) {
        Preferences.getInstacne().setValues("transfer_fee_bean", fee == null ? "" : getGson().toJson(fee));
    }

    public TransferFeeBean getTransferFeeBean() {
        String str = Preferences.getInstacne().getValues("transfer_fee_bean", "");
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return getGson().fromJson(str, TransferFeeBean.class);
    }


    public void saveFriends(ArrayList<UserBean> list) {
        Preferences.getInstacne().setValues("friends", list == null ? "" : getGson().toJson(list));
    }

    public ArrayList<UserBean> getFriends() {
        String str = Preferences.getInstacne().getValues("friends", "");
        if (TextUtils.isEmpty(str)) {
            return new ArrayList<>();
        }
        return getGson().fromJson(str, new TypeToken<ArrayList<UserBean>>() {
        }.getType());
    }

    public void saveLoginUsers(HashMap<Long, UserBean> map) {
        Preferences.getInstacne().setValues("login_users", map == null ? "" : getGson().toJson(map));
    }


    public HashMap<Long, UserBean> getLoginUsers() {
        String str = Preferences.getInstacne().getValues("login_users", "");
        HashMap<Long, UserBean> map;
        if (TextUtils.isEmpty(str)) {
            map = new HashMap<>();
        } else {
            map = getGson().fromJson(str, new TypeToken<HashMap<Long, UserBean>>() {
            }.getType());
        }
        UserBean bean = getUser();
        if (bean != null) {
            map.put(bean.getUserId(), bean);
        }
        if (TextUtils.isEmpty(str)) {
            saveLoginUsers(map);
        }
        return map;
    }

    public void saveLabels(List<LabelBean> list) {
        Preferences.getInstacne().setValues("labels", list == null ? "" : getGson().toJson(list));
    }

    public ArrayList<LabelBean> getLabels() {
        String str = Preferences.getInstacne().getValues("labels", "");
        if (TextUtils.isEmpty(str)) {
            return new ArrayList<>();
        }
        return getGson().fromJson(str, new TypeToken<ArrayList<LabelBean>>() {
        }.getType());
    }

    public void saveGroups(ArrayList<GroupBean> list) {
        Preferences.getInstacne().setValues("groups", list == null ? "" : getGson().toJson(list));
    }

    public void saveKeyboardHeight(int keyboardHeight) {
        Preferences.getInstacne().setValues("keyboard_height", keyboardHeight);
    }

    public int getKeyboardHeight() {
        return Preferences.getInstacne().getValues("keyboard_height", 0);
    }

    public void saveExchangeRate(String exchangeRate) {
        Preferences.getInstacne().setValues("exchange_rate", exchangeRate);
    }

    public String getExchangeRate() {
        return Preferences.getInstacne().getValues("exchange_rate", "");
    }


    public boolean isHasNewVerify() {
        return Preferences.getInstacne().getValues("isHadNew", false);
    }

    public void setHadNewVerify() {
        Preferences.getInstacne().setValues("isHadNew", true);
    }

    public void setNoNew() {
        Preferences.getInstacne().setValues("isHadNew", false);
    }

    /**
     * 保存阅后即焚信息
     *
     * @param types
     */
    public void saveMsgDeleteType(HashMap<Long, Integer> types) {
        Preferences.getInstacne().setValues("msg_delete_type", types == null ? "" : getGson().toJson(types));
    }

    public HashMap<Long, Integer> getMsgDeleteType() {
        String str = Preferences.getInstacne().getValues("msg_delete_type", "");
        if (TextUtils.isEmpty(str)) {
            return new HashMap<>();
        }
        return getGson().fromJson(str, new TypeToken<HashMap<Long, Integer>>() {
        }.getType());
    }

    public void saveGroupUsers(long groupId, ArrayList<UserBean> list) {
        Preferences.getInstacne().setValues("group_users_" + groupId, list == null ? "" : getGson().toJson(list));
    }

    public ArrayList<UserBean> getGroupUsers(long groupId) {
        String str = Preferences.getInstacne().getValues("group_users_" + groupId, "");
        if (TextUtils.isEmpty(str)) {
            return new ArrayList<>();
        }
        return getGson().fromJson(str, new TypeToken<ArrayList<UserBean>>() {
        }.getType());
    }

    public void setGroupFilterMsg(long groupId, List<FilterMsgBean> list) {
        Preferences.getInstacne().setValues("group_filter_" + groupId, list == null ? "" : getGson().toJson(list));
    }

    public ArrayList<FilterMsgBean> getGroupFilterMsg(long groupId) {
        String str = Preferences.getInstacne().getValues("group_filter_" + groupId, "");
        if (TextUtils.isEmpty(str)) {
            return new ArrayList<>();
        }
        return getGson().fromJson(str, new TypeToken<ArrayList<FilterMsgBean>>() {
        }.getType());
    }


    public ArrayList<GroupBean> getGroups() {
        String str = Preferences.getInstacne().getValues("groups", "");
        if (TextUtils.isEmpty(str)) {
            return new ArrayList<>();
        }
        return getGson().fromJson(str, new TypeToken<ArrayList<GroupBean>>() {
        }.getType());
    }

    public void saveQuestions(ArrayList<QuestionBean> list) {
        Preferences.getInstacne().setValues("questions", list == null ? "" : getGson().toJson(list));
    }

    public ArrayList<QuestionBean> getQuestions() {
        String str = Preferences.getInstacne().getValues("questions", "");
        if (TextUtils.isEmpty(str)) {
            return new ArrayList<>();
        }
        return getGson().fromJson(str, new TypeToken<ArrayList<QuestionBean>>() {
        }.getType());
    }

    public void saveChatSetting(ChatSettingBean bean) {
        Preferences.getInstacne().setValues("chat_setting", bean == null ? "" : getGson().toJson(bean));
    }

    public ChatSettingBean getChatSetting() {
        String str = Preferences.getInstacne().getValues("chat_setting", "");
        if (TextUtils.isEmpty(str)) {
            return new ChatSettingBean();
        }
        return getGson().fromJson(str, ChatSettingBean.class);
    }

    public void saveChatSubSettings(HashMap<String, ChatSubBean> map) {
        Preferences.getInstacne().setValues("chat_sub_setting", map == null ? "" : getGson().toJson(map));
    }

    public HashMap<String, ChatSubBean> getChatSubSettings() {
        String str = Preferences.getInstacne().getValues("chat_sub_setting", "");
        if (TextUtils.isEmpty(str)) {
            return new HashMap<>();
        }
        return getGson().fromJson(str, new TypeToken<HashMap<String, ChatSubBean>>() {
        }.getType());
    }

    public void saveColdEye(boolean isOpen) {
        Preferences.getInstacne().setValues("coldEyeOpen", isOpen);
    }

    public boolean getColdEye() {
        return Preferences.getInstacne().getValues("coldEyeOpen", true);
    }

    public void saveTotalAssets(String assets) {
        Preferences.getInstacne().setValues("total_assets", assets);
    }

    public String getTotalAssets() {
        return Preferences.getInstacne().getValues("total_assets", "");
    }


    public void loginOut() {
        mMyInfo = null;
        mWallet = null;
        saveCurrentWallet(null);
        saveTotalAssets("");
        saveNotSeeAsset(0);
        saveArea(0);
        savePayType(0);
        Preferences.getInstacne().setValues("my_user_info", "");
        Preferences.getInstacne().setValues("user", "");
        Preferences.getInstacne().setValues("myUserInfoBean", "");
        Preferences.getInstacne().setValues("groups", "");
        Preferences.getInstacne().setValues("friends", "");
        Preferences.getInstacne().setValues("msg_delete_type", "");
        Preferences.getInstacne().setValues("labels", "");
        Preferences.getInstacne().setValues("last_verify_time", 0l);
        Preferences.getInstacne().setValues("isHadNew", false);
        Preferences.getInstacne().setValues("chat_setting", "");
        Preferences.getInstacne().setValues("chat_sub_setting", "");
    }

    public Object getObject() {
        return mObject;
    }

    public void setObject(Object object) {
        this.mObject = object;
    }

}
