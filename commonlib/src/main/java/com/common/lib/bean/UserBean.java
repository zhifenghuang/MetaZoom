package com.common.lib.bean;

import android.content.ContentValues;
import android.text.TextUtils;

import com.common.lib.activity.db.IDBItemOperation;

import java.util.HashMap;

public class UserBean extends IDBItemOperation {
    /**
     * 客户端数据主键
     */
    private long userId = 0;
    private String avatarUrl;
    private String nickName;
    private String loginAccount;
    private String password;
    private int userState;
    private String bindMobile;
    private String bindEmail;
    private int gender;
    private String token;
    private String district;
    private int userType;
    private long createTime;
    private int allowAdd = 1;  //加好友是否需要验证
    private String privateKey;
    /**
     * 冷钱包加密内容
     */
    private String walletContentMD;

    private long id;
    private long contactId = 0;
    private String remark;
    private String memo;
    private int status;
    private int star;
    private int block;
    private boolean isFix;//选人时是否固定不能修改
    private int groupRole;
    private int addType;
    private int top;
    private int ignore;
    /**
     * 钱包类型 0热钱包 1 冷钱包
     */
    private int walletType;
    /**
     * 用户等级
     */
    private int level;
    private int userLevel;
    /**
     * 登录账号
     */
    private String account;
    /**
     * 备注
     */
    private int remarks;

    private boolean isCheck;

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getIgnore() {
        return ignore;
    }

    public void setIgnore(int ignore) {
        this.ignore = ignore;
    }

    public String getBindEmail() {
        return bindEmail;
    }

    public void setBindEmail(String bindEmail) {
        this.bindEmail = bindEmail;
    }

    public int getAllowAdd() {
        return allowAdd;
    }

    public void setAllowAdd(int allowAdd) {
        this.allowAdd = allowAdd;
    }

    public int getGroupRole() {
        return groupRole;
    }

    public void setGroupRole(int groupRole) {
        this.groupRole = groupRole;
    }

    public boolean isFix() {
        return isFix;
    }

    public void setFix(boolean isFix) {
        this.isFix = isFix;
    }

    private String pinyinName;

    public int getAddType() {
        return addType;
    }

    public void setAddType(int addType) {
        this.addType = addType;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    /**
     * 是否是客服
     *
     * @return
     */
    public boolean isService() {
        return userType > 0;
    }

    public long getUserId() {
        if (userId == 0) {
            userId = contactId;
        }
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickName() {
        if (!TextUtils.isEmpty(memo)) {
            return memo;
        }
        if (nickName == null) {
            nickName = getLoginAccount();
        }
        return nickName;
    }

    public String getNickName2() {
        if (nickName == null) {
            nickName = getLoginAccount();
        }
        return nickName;
    }

    public String getPinyinName() {
//        if (TextUtils.isEmpty(pinyinName)) {
//            String nick = getNickName();
//            if (TextUtils.isEmpty(nick)) {
//                return "#";
//            }
//            try {
//                pinyinName = Pinyin.toPinyin(nick, "").toLowerCase();
//                char c = pinyinName.charAt(0);
//                if (c < 'a' || c > 'z') {
//                    pinyinName = "#" + pinyinName;
//                }
//            } catch (Exception e) {
//                pinyinName = "#" + nick.toLowerCase();
//            }
//        }
//        return pinyinName;
        return getNickName();
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getLoginAccount() {
        if (TextUtils.isEmpty(loginAccount)) {
            loginAccount = bindEmail;
        }
        if (TextUtils.isEmpty(loginAccount)) {
            loginAccount = bindMobile;
        }
        return loginAccount;
    }

    public void setLoginAccount(String loginAccount) {
        this.loginAccount = loginAccount;
    }

    public int getUserState() {
        return userState;
    }

    public void setUserState(int userState) {
        this.userState = userState;
    }

    public String getBindMobile() {
        return bindMobile;
    }

    public void setBindMobile(String bindMobile) {
        this.bindMobile = bindMobile;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getContactId() {
        if (contactId == 0) {
            contactId = userId;
        }
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getMemo() {
        if (TextUtils.isEmpty(memo)) {
            if (nickName == null) {
                nickName = getLoginAccount();
            }
            return nickName;
        }
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public int getWalletType() {
        return walletType;
    }

    public void setWalletType(int walletType) {
        this.walletType = walletType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRemarks() {
        return remarks;
    }

    public void setRemarks(int remarks) {
        this.remarks = remarks;
    }

    public void setPinyinName(String pinyinName) {
        this.pinyinName = pinyinName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWalletContentMD() {
        return walletContentMD;
    }

    public void setWalletContentMD(String walletContentMD) {
        this.walletContentMD = walletContentMD;
    }

    public String getAccount() {
        if (TextUtils.isEmpty(account)) {
            account = getLoginAccount();
        }
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String getPrimaryKeyName() {
        return "userId";
    }

    @Override
    public String getTableName() {
        return "User";
    }

    @Override
    public ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put("userId", userId);
        values.put("password", password);
        values.put("userLevel", userLevel);
        values.put("userType", userType);
        values.put("walletType", walletType);
        values.put("avatarUrl", avatarUrl);
        values.put("nickName", getNickName());
        values.put("loginAccount", getLoginAccount());
        values.put("account", getAccount());
        values.put("userState", userState);
        values.put("bindMobile", bindMobile);
        values.put("gender", gender);
        values.put("token", token);
        values.put("district", district);
        values.put("createTime", createTime);
        values.put("walletContentMD", walletContentMD);
        values.put("remarks", remarks);
        return values;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", getUserId());
        map.put("avatarUrl", avatarUrl);
        map.put("nickName", nickName);
        map.put("loginAccount", loginAccount);
        map.put("gender", gender);
        if (!TextUtils.isEmpty(district)) {
            map.put("district", district);
        }
        return map;
    }
}
