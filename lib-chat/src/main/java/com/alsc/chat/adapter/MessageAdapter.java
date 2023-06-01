package com.alsc.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.activity.ChatBaseActivity;
import com.alsc.chat.fragment.MyInfoFragment;
import com.alsc.chat.utils.BitmapUtil;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.alsc.chat.fragment.UserInfoFragment;
import com.alsc.chat.http.OkHttpClientManager;
import com.alsc.chat.manager.MediaplayerManager;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.alsc.chat.view.RatioImageView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.activity.db.DatabaseOperate;
import com.common.lib.bean.*;
import com.common.lib.utils.LogUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhangke.websocket.WebSocketHandler;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

public class MessageAdapter extends BaseQuickAdapter<BasicMessage, BaseViewHolder> {

    protected Context mContext;
    protected UserBean mMyInfo;
    protected UserBean mChatUser;
    private Gson mGson;

    private boolean mIsEditMode;

    public MessageAdapter(Context context, UserBean myInfo) {
        super(R.layout.item_message);
        mContext = context;
        mMyInfo = myInfo;
        mIsEditMode = false;
    }

    public void setMyInfo(UserBean myInfo) {
        mMyInfo = myInfo;
        notifyDataSetChanged();
    }

    public void resetEditMode() {
        mIsEditMode = !mIsEditMode;
        if (mIsEditMode) {
            List<BasicMessage> list = getData();
            for (BasicMessage msg : list) {
                msg.setCheck(false);
            }
        }
        notifyDataSetChanged();
    }

    public boolean isEditMode() {
        return mIsEditMode;
    }

    public void setChatUser(UserBean chatUser) {
        mChatUser = chatUser;
        notifyDataSetChanged();
    }

    protected Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }

    @Override
    protected void convert(BaseViewHolder helper, BasicMessage item) {
        int msgType = item.getMsgType();
        showUserInfo(helper, item);
        if (mIsEditMode) {
            helper.setGone(R.id.ivCheck, false)
                    .setImageResource(R.id.ivCheck, item.isCheck() ? R.drawable.icon_box_selected : R.drawable.icon_box_unselected);
        } else {
            helper.setGone(R.id.ivCheck, true);
        }
        int position = getItemPosition(item);
        if (position == 0) {
            helper.setGone(R.id.tvTime, false)
                    .setText(R.id.tvTime, Utils.longToChatTime(item.getCreateTime()));
        } else {
            if (Utils.isShowTime(getItem(position - 1).getCreateTime(), item.getCreateTime())) {
                helper.setGone(R.id.tvTime, false)
                        .setText(R.id.tvTime, Utils.longToChatTime(item.getCreateTime()));
            } else {
                helper.setGone(R.id.tvTime, true);
            }
        }
        if (msgType == MessageType.TYPE_RECEIVE_RED_PACKAGE.ordinal()) {
            helper.setGone(R.id.llParent, true)
                    .setGone(R.id.llReceiveRedPackageInfo, false)
                    .setGone(R.id.tvGroupInfo, true)
                    .setGone(R.id.tvRestTime, true);
            String name1 = "", name2 = "";
            HashMap<String, String> map = getGson().fromJson(item.getContent(), new TypeToken<HashMap<String, String>>() {
            }.getType());
            TextView tvName1 = helper.getView(R.id.tvName1);
            TextView tvName2 = helper.getView(R.id.tvName2);
            if (item.isMySendMsg(mMyInfo.getUserId())) {
                name1 = mContext.getString(R.string.chat_you);
                if (mMyInfo.getUserId() == Long.parseLong(map.get("userId"))) {
                    name2 = mContext.getString(R.string.chat_mine);
                    tvName2.setTextColor(ContextCompat.getColor(mContext, R.color.color_7c_84_98));
                } else {
                    name2 = map.get("nickName");
                    tvName2.setTextColor(ContextCompat.getColor(mContext, R.color.color_ff_c5_00));
                }
                tvName1.setTextColor(ContextCompat.getColor(mContext, R.color.color_7c_84_98));
            } else {
                name1 = map.get("nickName2");
                if (mMyInfo.getUserId() == Long.parseLong(map.get("userId"))) {
                    name2 = mContext.getString(R.string.chat_you);
                    tvName2.setTextColor(ContextCompat.getColor(mContext, R.color.color_7c_84_98));
                } else {
                    name2 = map.get("nickName");
                    tvName2.setTextColor(ContextCompat.getColor(mContext, R.color.color_ff_c5_00));
                }
                tvName1.setTextColor(ContextCompat.getColor(mContext, R.color.color_ff_c5_00));
            }
            tvName1.setText(name1);
            tvName2.setText(name2);
            TextView tvRedPackage = helper.getView(R.id.tvRedPackage);
            tvName1.setTag(R.id.chat_id, position);
            tvName2.setTag(R.id.chat_id, position);
            tvRedPackage.setTag(R.id.chat_id, position);
            if (!isEditMode()) {
                tvName1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getOnItemChildClickListener().onItemChildClick(null, v, (int) v.getTag(R.id.chat_id));
                    }
                });
                tvName2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getOnItemChildClickListener().onItemChildClick(null, v, (int) v.getTag(R.id.chat_id));
                    }
                });
                tvRedPackage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getOnItemChildClickListener().onItemChildClick(null, v, (int) v.getTag(R.id.chat_id));
                    }
                });
            } else {
                tvRedPackage.setOnClickListener(null);
                tvName1.setOnClickListener(null);
                tvName2.setOnClickListener(null);
            }
            return;
        } else if (msgType == MessageType.TYPE_INVITE_TO_GROUP.ordinal()) {
            helper.setGone(R.id.llParent, true)
                    .setGone(R.id.llReceiveRedPackageInfo, true)
                    .setGone(R.id.tvGroupInfo, false)
                    .setGone(R.id.tvRestTime, true);
            UserBean user1 = getGson().fromJson(item.getContent(), UserBean.class);
            ArrayList<UserBean> list = getGson().fromJson(item.getExtra(), new TypeToken<ArrayList<UserBean>>() {
            }.getType());
            String name1 = user1.getUserId() == mMyInfo.getUserId() ? mContext.getString(R.string.chat_you) : user1.getNickName();
            if (list == null) {
                helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_xxx_invite_xxx_add_group, name1, ""));
                return;
            }
            String name2 = "";
            int index = 0;
            for (UserBean bean : list) {
                if (bean.getUserId() == mMyInfo.getUserId()) {
                    name2 += mContext.getString(bean.getUserId() == user1.getUserId() ? R.string.chat_mine : R.string.chat_you);
                } else {
                    name2 += bean.getNickName();
                }
                if (index < list.size() - 1) {
                    name2 += "、";
                }
                ++index;
            }
            helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_xxx_invite_xxx_add_group, name1, name2));
            return;
        } else if (msgType == MessageType.TYPE_REMOVE_FROM_GROUP.ordinal()) {
            helper.setGone(R.id.llParent, true)
                    .setGone(R.id.llReceiveRedPackageInfo, true)
                    .setGone(R.id.tvGroupInfo, false)
                    .setGone(R.id.tvRestTime, true);
            ArrayList<UserBean> list = getGson().fromJson(item.getExtra(), new TypeToken<ArrayList<UserBean>>() {
            }.getType());
            if (list == null) {
                helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_xxx_remove_from_group, ""));
                return;
            }
            String name = "";
            int index = 0;
            for (UserBean bean : list) {
                if (bean.getUserId() == mMyInfo.getUserId()) {
                    name += mContext.getString(R.string.chat_you);
                } else {
                    name += bean.getNickName();
                }
                if (index < list.size() - 1) {
                    name += "、";
                }
                ++index;
            }
            helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_xxx_remove_from_group, name));
            return;
        } else if (msgType == MessageType.TYPE_IN_GROUP_BY_QRCODE.ordinal()) {
            helper.setGone(R.id.llParent, true)
                    .setGone(R.id.llReceiveRedPackageInfo, true)
                    .setGone(R.id.tvGroupInfo, false)
                    .setGone(R.id.tvRestTime, true);
            ArrayList<UserBean> list = getGson().fromJson(item.getExtra(), new TypeToken<ArrayList<UserBean>>() {
            }.getType());
            if (list == null) {
                helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_xxx_in_group_by_qrcode, ""));
                return;
            }
            String name = "";
            int index = 0;
            for (UserBean bean : list) {
                if (bean.getUserId() == mMyInfo.getUserId()) {
                    name += mContext.getString(R.string.chat_you);
                } else {
                    name += bean.getNickName();
                }
                if (index < list.size() - 1) {
                    name += "、";
                }
                ++index;
            }
            helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_xxx_in_group_by_qrcode, name));
            return;
        } else if (msgType == MessageType.TYPE_UPDATE_GROUP_NAME.ordinal()) {
            helper.setGone(R.id.llParent, true)
                    .setGone(R.id.llReceiveRedPackageInfo, true)
                    .setGone(R.id.tvGroupInfo, false)
                    .setGone(R.id.tvRestTime, true);
            helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_group_ower_update_group_name));
            return;
        } else if (msgType == MessageType.TYPE_UPDATE_GROUP_NOTICE.ordinal()) {
            helper.setGone(R.id.llParent, true)
                    .setGone(R.id.llReceiveRedPackageInfo, true)
                    .setGone(R.id.tvGroupInfo, false)
                    .setGone(R.id.tvRestTime, true);
            helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_group_ower_update_group_notice));
            return;
        } else if (msgType == MessageType.TYPE_ALL_FORBID_CHAT.ordinal()) {
            helper.setGone(R.id.llParent, true)
                    .setGone(R.id.llReceiveRedPackageInfo, true)
                    .setGone(R.id.tvGroupInfo, false)
                    .setGone(R.id.tvRestTime, true);
            helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_all_forbid_chat));
            return;
        } else if (msgType == MessageType.TYPE_ALL_REMOVE_FORBID_CHAT.ordinal()) {
            helper.setGone(R.id.llParent, true)
                    .setGone(R.id.llReceiveRedPackageInfo, true)
                    .setGone(R.id.tvGroupInfo, false)
                    .setGone(R.id.tvRestTime, true);
            helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_remove_all_forbid_chat));
            return;
        } else if (msgType == MessageType.TYPE_FORBID_CHAT.ordinal()) {
            helper.setGone(R.id.llParent, true)
                    .setGone(R.id.llReceiveRedPackageInfo, true)
                    .setGone(R.id.tvGroupInfo, false)
                    .setGone(R.id.tvRestTime, true);
            ArrayList<UserBean> list = getGson().fromJson(item.getExtra(), new TypeToken<ArrayList<UserBean>>() {
            }.getType());
            if (list == null) {
                helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_xxx_forbid_chat, ""));
                return;
            }
            String name = "";
            int index = 0;
            for (UserBean bean : list) {
                if (bean.getUserId() == mMyInfo.getUserId()) {
                    name += mContext.getString(R.string.chat_you);
                } else {
                    name += bean.getNickName();
                }
                if (index < list.size() - 1) {
                    name += "、";
                }
                ++index;
            }
            helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_xxx_forbid_chat, name));
            return;
        } else if (msgType == MessageType.TYPE_REOMVE_FORBID_CHAT.ordinal()) {
            helper.setGone(R.id.llParent, true)
                    .setGone(R.id.llReceiveRedPackageInfo, true)
                    .setGone(R.id.tvGroupInfo, false)
                    .setGone(R.id.tvRestTime, true);
            ArrayList<UserBean> list = getGson().fromJson(item.getExtra(), new TypeToken<ArrayList<UserBean>>() {
            }.getType());
            if (list == null) {
                helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_reomve_xxx_forbid_chat, ""));
                return;
            }
            String name = "";
            int index = 0;
            for (UserBean bean : list) {
                if (bean.getUserId() == mMyInfo.getUserId()) {
                    name += mContext.getString(R.string.chat_you);
                } else {
                    name += bean.getNickName();
                }
                if (index < list.size() - 1) {
                    name += "、";
                }
                ++index;
            }
            helper.setText(R.id.tvGroupInfo, mContext.getString(R.string.chat_reomve_xxx_forbid_chat, name));
            return;
        }
        helper.setGone(R.id.llParent, false)
                .setGone(R.id.llReceiveRedPackageInfo, true)
                .setGone(R.id.tvGroupInfo, true);
        TextView tvRestTime = helper.getView(R.id.tvRestTime);
        long currentTime = System.currentTimeMillis();
        boolean isShowRestTime = item.getExpire() > 0 && currentTime < item.getExpire();
        if (isShowRestTime) {
            tvRestTime.setVisibility(View.VISIBLE);
            tvRestTime.setText(Utils.getRestTime(item.getExpire(), currentTime, mContext));
        } else {
            tvRestTime.setVisibility(View.GONE);
        }
        if (item.isMySendMsg(mMyInfo.getUserId())) {
            if (item.getSendStatus() == 0 && System.currentTimeMillis() - item.getCreateTime() > 30000) {   //消息30秒还没发送结果重发
                item.setCreateTime(System.currentTimeMillis());
                if (WebSocketHandler.getDefault() != null) {
                    WebSocketHandler.getDefault().send(item.toJson());
                }
            }
            helper.setGone(R.id.llLeft, true)
                    .setGone(R.id.llRight, false)
                    .setGone(R.id.ivResend, item.getSendStatus() >= 0)
                    .setGone(R.id.sendStateBar, item.getSendStatus() != 0);
            setOnResendClick(helper.getView(R.id.ivResend), position);
            if (msgType == MessageType.TYPE_TEXT.ordinal() || msgType == MessageType.TYPE_GROUP_AT_MEMBER_MSG.ordinal()) {
                helper.setGone(R.id.llTextRight, false)
                        .setGone(R.id.ivPicRight, true)
                        .setGone(R.id.llVoiceRight, true)
                        .setGone(R.id.flLocationRight, true)
                        .setGone(R.id.rlVideoRight, true)
                        .setGone(R.id.llFileRight, true)
                        .setGone(R.id.rlRedPackageRight, true)
                        .setGone(R.id.rlTransferRight, true)
                        .setGone(R.id.llRecommendRight, true)
                        .setText(R.id.tvRight, item.getContent());
                if (TextUtils.isEmpty(item.getTranslate())) {
                    helper.setGone(R.id.tvRightTranslate, true);
                } else {
                    helper.setGone(R.id.tvRightTranslate, false)
                            .setText(R.id.tvRightTranslate, item.getTranslate());
                }
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.tvRight), position);
            } else if (msgType == MessageType.TYPE_IMAGE.ordinal()) {
                helper.setGone(R.id.llTextRight, true)
                        .setGone(R.id.ivPicRight, false)
                        .setGone(R.id.flLocationRight, true)
                        .setGone(R.id.rlVideoRight, true)
                        .setGone(R.id.llFileRight, true)
                        .setGone(R.id.llVoiceRight, true)
                        .setGone(R.id.rlTransferRight, true)
                        .setGone(R.id.llRecommendRight, true)
                        .setGone(R.id.rlRedPackageRight, true);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.ivPicRight), position);
                showImageMsg(helper.getView(R.id.ivPicRight), null, item);
            } else if (msgType == MessageType.TYPE_VOICE.ordinal()) {
                helper.setGone(R.id.llTextRight, true)
                        .setGone(R.id.ivPicRight, true)
                        .setGone(R.id.llVoiceRight, false)
                        .setGone(R.id.rlVideoRight, true)
                        .setGone(R.id.llFileRight, true)
                        .setGone(R.id.flLocationRight, true)
                        .setGone(R.id.rlTransferRight, true)
                        .setGone(R.id.rlRedPackageRight, true)
                        .setGone(R.id.llRecommendRight, true);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.llVoiceRight), position);
                showVoiceMsg(helper.getView(R.id.llVoiceRight), helper.getView(R.id.tvRightVoiceTime), helper.getView(R.id.ivRightRecordIcon), item);
            } else if (msgType == MessageType.TYPE_VIDEO.ordinal()) {
                helper.setGone(R.id.llTextRight, true)
                        .setGone(R.id.ivPicRight, true)
                        .setGone(R.id.llVoiceRight, true)
                        .setGone(R.id.flLocationRight, true)
                        .setGone(R.id.llFileRight, true)
                        .setGone(R.id.rlVideoRight, false)
                        .setGone(R.id.rlTransferRight, true)
                        .setGone(R.id.rlRedPackageRight, true)
                        .setGone(R.id.llRecommendRight, true);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.rlVideoRight), position);
                showVideoMsg(helper.getView(R.id.ivVideoRight), helper.getView(R.id.ivPlayVideoRight), null, helper.getView(R.id.tvVideoTimeRight), item);
            } else if (msgType == MessageType.TYPE_LOCATION.ordinal()) {
                helper.setGone(R.id.llTextRight, true)
                        .setGone(R.id.ivPicRight, true)
                        .setGone(R.id.llVoiceRight, true)
                        .setGone(R.id.rlVideoRight, true)
                        .setGone(R.id.llFileRight, true)
                        .setGone(R.id.flLocationRight, false)
                        .setGone(R.id.rlTransferRight, true)
                        .setGone(R.id.rlRedPackageRight, true)
                        .setGone(R.id.llRecommendRight, true);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.flLocationRight), position);
                showLocationMsg(helper.getView(R.id.ivMapRight), helper.getView(R.id.tvTitleRight), helper.getView(R.id.tvLocationRight), item);
            } else if (msgType == MessageType.TYPE_FILE.ordinal()) {
                helper.setGone(R.id.llTextRight, true)
                        .setGone(R.id.ivPicRight, true)
                        .setGone(R.id.llVoiceRight, true)
                        .setGone(R.id.rlVideoRight, true)
                        .setGone(R.id.llFileRight, false)
                        .setGone(R.id.flLocationRight, true)
                        .setGone(R.id.rlTransferRight, true)
                        .setGone(R.id.rlRedPackageRight, true)
                        .setGone(R.id.llRecommendRight, true);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.llFileRight), position);
                showFileMsg(helper.getView(R.id.tvFileNameRight), helper.getView(R.id.tvFileSizeRight), item);
            } else if (msgType == MessageType.TYPE_RED_PACKAGE.ordinal()
                    || msgType == MessageType.TYPE_NEW_MEMBER_RED_PACKAGE.ordinal()) {
                helper.setGone(R.id.llTextRight, true)
                        .setGone(R.id.ivPicRight, true)
                        .setGone(R.id.llVoiceRight, true)
                        .setGone(R.id.rlVideoRight, true)
                        .setGone(R.id.llFileRight, true)
                        .setGone(R.id.flLocationRight, true)
                        .setGone(R.id.rlTransferRight, true)
                        .setGone(R.id.llRecommendRight, true)
                        .setGone(R.id.rlRedPackageRight, false);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.rlRedPackageRight), position);
                showRedPackageMsg(helper.getView(R.id.ivRPBgRight), helper.getView(R.id.ivRPIconRight), helper.getView(R.id.tvRPRemarkRight), helper.getView(R.id.tvRPTitleRight), item, true);
            } else if (msgType == MessageType.TYPE_TRANSFER.ordinal() || msgType == MessageType.TYPE_RECEIVE_TRANSFER.ordinal()) {
                helper.setGone(R.id.llTextRight, true)
                        .setGone(R.id.ivPicRight, true)
                        .setGone(R.id.llVoiceRight, true)
                        .setGone(R.id.rlVideoRight, true)
                        .setGone(R.id.llFileRight, true)
                        .setGone(R.id.flLocationRight, true)
                        .setGone(R.id.rlTransferRight, false)
                        .setGone(R.id.rlRedPackageRight, true)
                        .setGone(R.id.llRecommendRight, true);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.rlTransferRight), position);
                showTransferMsg(helper.getView(R.id.ivTransferBgRight), helper.getView(R.id.ivTransferIconRight), helper.getView(R.id.tvTransferValueRight), helper.getView(R.id.tvTransferToRight), item, true);
            } else if (msgType == MessageType.TYPE_RECOMAND_USER.ordinal()) {
                helper.setGone(R.id.llTextRight, true)
                        .setGone(R.id.ivPicRight, true)
                        .setGone(R.id.llVoiceRight, true)
                        .setGone(R.id.rlVideoRight, true)
                        .setGone(R.id.llFileRight, true)
                        .setGone(R.id.flLocationRight, true)
                        .setGone(R.id.rlTransferRight, true)
                        .setGone(R.id.rlRedPackageRight, true)
                        .setGone(R.id.llRecommendRight, false);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.llRecommendRight), position);
                showRecommendMsg(helper.getView(R.id.ivRecommendRight), helper.getView(R.id.tvRecommendNickRight), helper.getView(R.id.tvRecommendIdRight), item);
            } else {
                helper.setGone(R.id.llRight, true);
            }
            if (isShowRestTime) {
                tvRestTime.setGravity(Gravity.RIGHT);
            }
        } else {
            helper.setGone(R.id.llLeft, false)
                    .setGone(R.id.llRight, true);
            if (msgType == MessageType.TYPE_TEXT.ordinal() || msgType == MessageType.TYPE_GROUP_AT_MEMBER_MSG.ordinal()) {
                helper.setGone(R.id.llTextLeft, false)
                        .setGone(R.id.rlPicLeft, true)
                        .setGone(R.id.llVoiceLeft, true)
                        .setGone(R.id.flLocationLeft, true)
                        .setGone(R.id.rlVideoLeft, true)
                        .setGone(R.id.llFileLeft, true)
                        .setGone(R.id.rlRedPackageLeft, true)
                        .setGone(R.id.rlTransferLeft, true)
                        .setGone(R.id.llRecommendLeft, true)
                        .setGone(R.id.llQuestion, true)
                        .setGone(R.id.llService, true)
                        .setGone(R.id.rlInviteLeft, true)
                        .setText(R.id.tvLeft, item.getContent());
                if (TextUtils.isEmpty(item.getTranslate())) {
                    helper.setGone(R.id.tvTranslateLeft, true);
                } else {
                    helper.setGone(R.id.tvTranslateLeft, false)
                            .setText(R.id.tvTranslateLeft, item.getTranslate());
                }
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.llTextLeft), position);
            } else if (msgType == MessageType.TYPE_IMAGE.ordinal()) {
                helper.setGone(R.id.llTextLeft, true)
                        .setGone(R.id.llVoiceLeft, true)
                        .setGone(R.id.rlVideoLeft, true)
                        .setGone(R.id.flLocationLeft, true)
                        .setGone(R.id.llFileLeft, true)
                        .setGone(R.id.rlRedPackageLeft, true)
                        .setGone(R.id.rlTransferLeft, true)
                        .setGone(R.id.llRecommendLeft, true)
                        .setGone(R.id.llQuestion, true)
                        .setGone(R.id.llService, true)
                        .setGone(R.id.rlInviteLeft, true)
                        .setGone(R.id.rlPicLeft, false);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.ivPicLeft), position);
                showImageMsg(helper.getView(R.id.ivPicLeft), helper.getView(R.id.picLeftProgressBar), item);
            } else if (msgType == MessageType.TYPE_VOICE.ordinal()) {
                helper.setGone(R.id.llTextLeft, true)
                        .setGone(R.id.llVoiceLeft, false)
                        .setGone(R.id.flLocationLeft, true)
                        .setGone(R.id.rlVideoLeft, true)
                        .setGone(R.id.llFileLeft, true)
                        .setGone(R.id.rlTransferLeft, true)
                        .setGone(R.id.rlRedPackageLeft, true)
                        .setGone(R.id.llRecommendLeft, true)
                        .setGone(R.id.llQuestion, true)
                        .setGone(R.id.llService, true)
                        .setGone(R.id.rlInviteLeft, true)
                        .setGone(R.id.rlPicLeft, true);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.llVoiceLeft), position);
                showVoiceMsg(helper.getView(R.id.llVoiceLeft), helper.getView(R.id.tvLeftVoiceTime), helper.getView(R.id.ivLeftRecordIcon), item);
                helper.setGone(R.id.ivVoiceReadLeft, item.getReceiveStatus() > 1);
            } else if (msgType == MessageType.TYPE_VIDEO.ordinal()) {
                helper.setGone(R.id.llTextLeft, true)
                        .setGone(R.id.llVoiceLeft, true)
                        .setGone(R.id.flLocationLeft, true)
                        .setGone(R.id.rlVideoLeft, false)
                        .setGone(R.id.llFileLeft, true)
                        .setGone(R.id.rlRedPackageLeft, true)
                        .setGone(R.id.rlTransferLeft, true)
                        .setGone(R.id.llRecommendLeft, true)
                        .setGone(R.id.llQuestion, true)
                        .setGone(R.id.llService, true)
                        .setGone(R.id.rlInviteLeft, true)
                        .setGone(R.id.rlPicLeft, true);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.rlVideoLeft), position);
                showVideoMsg(helper.getView(R.id.ivVideoLeft), helper.getView(R.id.ivPlayVideoLeft), helper.getView(R.id.progressBarVideoLeft), helper.getView(R.id.tvVideoTimeLeft), item);
            } else if (msgType == MessageType.TYPE_LOCATION.ordinal()) {
                helper.setGone(R.id.llTextLeft, true)
                        .setGone(R.id.llVoiceLeft, true)
                        .setGone(R.id.flLocationLeft, false)
                        .setGone(R.id.rlVideoLeft, true)
                        .setGone(R.id.rlTransferLeft, true)
                        .setGone(R.id.llFileLeft, true)
                        .setGone(R.id.rlPicLeft, true)
                        .setGone(R.id.llRecommendLeft, true)
                        .setGone(R.id.llQuestion, true)
                        .setGone(R.id.llService, true)
                        .setGone(R.id.rlInviteLeft, true)
                        .setGone(R.id.rlRedPackageLeft, true);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.flLocationLeft), position);
                showLocationMsg(helper.getView(R.id.ivMapLeft), helper.getView(R.id.tvTitleLeft), helper.getView(R.id.tvLocationLeft), item);
            } else if (msgType == MessageType.TYPE_FILE.ordinal()) {
                helper.setGone(R.id.llTextLeft, true)
                        .setGone(R.id.llVoiceLeft, true)
                        .setGone(R.id.flLocationLeft, true)
                        .setGone(R.id.rlVideoLeft, true)
                        .setGone(R.id.llFileLeft, false)
                        .setGone(R.id.rlTransferLeft, true)
                        .setGone(R.id.rlPicLeft, true)
                        .setGone(R.id.llRecommendLeft, true)
                        .setGone(R.id.llQuestion, true)
                        .setGone(R.id.llService, true)
                        .setGone(R.id.rlInviteLeft, true)
                        .setGone(R.id.rlRedPackageLeft, true);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.llFileLeft), position);
                showFileMsg(helper.getView(R.id.tvFileNameLeft), helper.getView(R.id.tvFileSizeLeft), item);
            } else if (msgType == MessageType.TYPE_RED_PACKAGE.ordinal()
                    || msgType == MessageType.TYPE_NEW_MEMBER_RED_PACKAGE.ordinal()) {
                helper.setGone(R.id.llTextLeft, true)
                        .setGone(R.id.llVoiceLeft, true)
                        .setGone(R.id.flLocationLeft, true)
                        .setGone(R.id.rlVideoLeft, true)
                        .setGone(R.id.llFileLeft, true)
                        .setGone(R.id.rlPicLeft, true)
                        .setGone(R.id.rlTransferLeft, true)
                        .setGone(R.id.llRecommendLeft, true)
                        .setGone(R.id.llQuestion, true)
                        .setGone(R.id.llService, true)
                        .setGone(R.id.rlInviteLeft, true)
                        .setGone(R.id.rlRedPackageLeft, false);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.rlRedPackageLeft), position);
                showRedPackageMsg(helper.getView(R.id.ivRPBgLeft), helper.getView(R.id.ivRPIconLeft), helper.getView(R.id.tvRPRemarkLeft), helper.getView(R.id.tvRPTitleLeft), item, false);
            } else if (msgType == MessageType.TYPE_TRANSFER.ordinal() || msgType == MessageType.TYPE_RECEIVE_TRANSFER.ordinal()) {
                helper.setGone(R.id.llTextLeft, true)
                        .setGone(R.id.llVoiceLeft, true)
                        .setGone(R.id.flLocationLeft, true)
                        .setGone(R.id.rlVideoLeft, true)
                        .setGone(R.id.llFileLeft, true)
                        .setGone(R.id.rlPicLeft, true)
                        .setGone(R.id.rlTransferLeft, false)
                        .setGone(R.id.llRecommendLeft, true)
                        .setGone(R.id.rlRedPackageLeft, true)
                        .setGone(R.id.llService, true)
                        .setGone(R.id.rlInviteLeft, true)
                        .setGone(R.id.llQuestion, true);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.rlTransferLeft), position);
                showTransferMsg(helper.getView(R.id.ivTransferBgLeft), helper.getView(R.id.ivTransferIconLeft), helper.getView(R.id.tvTransferValueLeft), helper.getView(R.id.tvTransferToLeft), item, false);
            } else if (msgType == MessageType.TYPE_RECOMAND_USER.ordinal()) {
                helper.setGone(R.id.llTextLeft, true)
                        .setGone(R.id.llVoiceLeft, true)
                        .setGone(R.id.flLocationLeft, true)
                        .setGone(R.id.rlVideoLeft, true)
                        .setGone(R.id.llFileLeft, true)
                        .setGone(R.id.rlPicLeft, true)
                        .setGone(R.id.rlTransferLeft, true)
                        .setGone(R.id.llRecommendLeft, false)
                        .setGone(R.id.rlRedPackageLeft, true)
                        .setGone(R.id.llService, true)
                        .setGone(R.id.rlInviteLeft, true)
                        .setGone(R.id.llQuestion, true);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.llRecommendLeft), position);
                showRecommendMsg(helper.getView(R.id.ivRecommendLeft), helper.getView(R.id.tvRecommendNickLeft), helper.getView(R.id.tvRecommendIdLeft), item);
            } else if (msgType == MessageType.TYPE_QUESTION.ordinal()) {
                helper.setGone(R.id.llTextLeft, true)
                        .setGone(R.id.rlPicLeft, true)
                        .setGone(R.id.llVoiceLeft, true)
                        .setGone(R.id.flLocationLeft, true)
                        .setGone(R.id.rlVideoLeft, true)
                        .setGone(R.id.llFileLeft, true)
                        .setGone(R.id.rlRedPackageLeft, true)
                        .setGone(R.id.rlTransferLeft, true)
                        .setGone(R.id.llRecommendLeft, true)
                        .setGone(R.id.llService, true)
                        .setGone(R.id.rlInviteLeft, true)
                        .setGone(R.id.llQuestion, false);
                setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.llQuestion), position);
                showQuestion(helper.getView(R.id.questionRecyclerView), item);
            } else if (msgType == MessageType.TYPE_SELECT_SERVICE.ordinal()) {
                helper.setGone(R.id.llTextLeft, true)
                        .setGone(R.id.rlPicLeft, true)
                        .setGone(R.id.llVoiceLeft, true)
                        .setGone(R.id.flLocationLeft, true)
                        .setGone(R.id.rlVideoLeft, true)
                        .setGone(R.id.llFileLeft, true)
                        .setGone(R.id.rlRedPackageLeft, true)
                        .setGone(R.id.rlTransferLeft, true)
                        .setGone(R.id.llRecommendLeft, true)
                        .setGone(R.id.llService, false)
                        .setGone(R.id.rlInviteLeft, true)
                        .setGone(R.id.llQuestion, true);
                //  setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.llService), position);
                showServices(helper.getView(R.id.serviceRecyclerView), helper.getView(R.id.tvOnlineNum), item);
            } else if (msgType == MessageType.TYPE_INVITE_PAY_IN_GROUP.ordinal()) {
                helper.setGone(R.id.llTextLeft, true)
                        .setGone(R.id.rlPicLeft, true)
                        .setGone(R.id.llVoiceLeft, true)
                        .setGone(R.id.flLocationLeft, true)
                        .setGone(R.id.rlVideoLeft, true)
                        .setGone(R.id.llFileLeft, true)
                        .setGone(R.id.rlRedPackageLeft, true)
                        .setGone(R.id.rlTransferLeft, true)
                        .setGone(R.id.llRecommendLeft, true)
                        .setGone(R.id.llQuestion, true)
                        .setGone(R.id.llService, true)
                        .setGone(R.id.rlInviteLeft, false);
                if (!TextUtils.isEmpty(item.getExtra())) {
                    GroupInviteBean bean = getGson().fromJson(item.getExtra(), GroupInviteBean.class);
                    Utils.displayAvatar(mContext, R.drawable.chat_default_avatar, bean.getGroup().getIcon(), helper.getView(R.id.ivInviteGroupIconLeft));
                    helper.setText(R.id.tvInviteMsgLeft, "\"" + mChatUser.getNickName() + "\"" + mContext.getString(R.string.chat_invite_you_in_group_xxx, bean.getGroup().getName()));
                    setOnClickListener(helper.getView(R.id.llParent), helper.getView(R.id.rlInviteLeft), position);
                }
            } else {
                helper.setGone(R.id.llLeft, true);
            }
            if (isShowRestTime) {
                tvRestTime.setGravity(Gravity.LEFT);
            }
        }
    }


    protected void setOnClickListener(View rootView, View view, int position) {
        view.setTag(R.id.chat_id, position);
        rootView.setTag(R.id.chat_id, position);
        if (!isEditMode()) {
            rootView.setOnClickListener(null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getOnItemChildClickListener().onItemChildClick(null, v, (int) v.getTag(R.id.chat_id));
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    getOnItemChildLongClickListener().onItemChildLongClick(null, v, (int) v.getTag(R.id.chat_id));
                    return false;
                }
            });
        } else {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getOnItemChildClickListener().onItemChildClick(null, v, (int) v.getTag(R.id.chat_id));
                }
            });
        }
    }

    private void setOnResendClick(View view, int position) {
        view.setTag(R.id.chat_id, position);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnItemChildClickListener().onItemChildClick(null, view, (int) v.getTag(R.id.chat_id));
            }
        });
    }

    protected void showServices(RecyclerView recyclerView, TextView tvNum, BasicMessage item) {

    }

    protected void showQuestion(RecyclerView recyclerView, BasicMessage item) {

    }

    private void showRecommendMsg(ImageView iv, TextView tvNick, TextView tvId, BasicMessage item) {
        if (!TextUtils.isEmpty(item.getContent())) {
            try {
                UserBean bean = getGson().fromJson(item.getContent(), UserBean.class);
                Utils.displayAvatar(mContext, R.drawable.chat_default_avatar, bean.getAvatarUrl(), iv);
                tvNick.setText(bean.getNickName());
                tvId.setText(bean.getLoginAccount());
            } catch (Exception e) {
            }
        }
    }

    private void showTransferMsg(ImageView ivBg, ImageView ivTransferIcon, TextView tvTransferValue, TextView tvTransferTo, BasicMessage item, boolean isMine) {
        if (!TextUtils.isEmpty(item.getContent())) {
            try {
                JSONObject jsonObject = new JSONObject(item.getContent());
                tvTransferValue.setText(String.format("%.2f", jsonObject.optDouble("amount")));
            } catch (Exception e) {
            }
        }
        int msgType = item.getMsgType();
        if (msgType == MessageType.TYPE_TRANSFER.ordinal()) {
            int receiveStatus = item.getReceiveStatus();
            ivTransferIcon.setImageResource(R.drawable.chat_transfer_icon);
            if (receiveStatus == 2) {
                tvTransferTo.setText(mContext.getString(R.string.chat_peer_had_receive_money));
                ivBg.setImageResource(isMine ? R.drawable.icon_red_package_right_2 : R.drawable.icon_red_package_left_2);
            } else if (receiveStatus == 3) {
                tvTransferTo.setText(mContext.getString(R.string.chat_had_back));
                ivBg.setImageResource(isMine ? R.drawable.icon_red_package_right_2 : R.drawable.icon_red_package_left_2);
            } else {
                tvTransferTo.setText(mContext.getString(R.string.chat_transfer_to_xxx,
                        isMine ? mChatUser.getNickName() : mContext.getString(R.string.chat_you)));
                ivBg.setImageResource(isMine ? R.drawable.icon_red_package_right : R.drawable.icon_red_package_left);
            }
        } else {
            ivTransferIcon.setImageResource(R.drawable.chat_transfer_icon_2);
            tvTransferTo.setText(mContext.getString(R.string.chat_had_receive_money));
            ivBg.setImageResource(isMine ? R.drawable.icon_red_package_right_2 : R.drawable.icon_red_package_left_2);
        }
    }

    private void showRedPackageMsg(ImageView ivBg, ImageView ivIcon, TextView tvRPRemark, TextView tvTitle, BasicMessage item, boolean isMine) {
        if (!TextUtils.isEmpty(item.getContent())) {
            try {
                JSONObject jsonObject = new JSONObject(item.getContent());
                int type = jsonObject.optInt("type");
                if (type == 5) {
                    tvRPRemark.setText(mContext.getString(R.string.chat_new_member_special_red_package));
                    tvTitle.setText(mContext.getString(R.string.chat_new_member_red_package));
                } else {
                    String remark = jsonObject.optString("remark");
                    tvRPRemark.setText(TextUtils.isEmpty(remark) ? mContext.getString(R.string.chat_red_package_default_remark) : remark);
                    tvTitle.setText(mContext.getString(R.string.chat_money_package));
                }
            } catch (Exception e) {
                tvRPRemark.setText("");
            }
        }
        int msgType = item.getMsgType();
        if (msgType == MessageType.TYPE_RED_PACKAGE.ordinal()) {
            int receiveStatus = item.getReceiveStatus();
            if (receiveStatus > 1) {
                ivIcon.setImageResource(R.drawable.icon_red_package_icon_3);
                ivBg.setImageResource(isMine ? R.drawable.icon_red_package_right_2 : R.drawable.icon_red_package_left_2);
            } else {
                ivIcon.setImageResource(R.drawable.icon_red_package_icon_3);
                ivBg.setImageResource(isMine ? R.drawable.icon_red_package_right : R.drawable.icon_red_package_left);
            }
        } else if (msgType == MessageType.TYPE_NEW_MEMBER_RED_PACKAGE.ordinal()) {
            int receiveStatus = item.getReceiveStatus();
            if (receiveStatus > 1) {
                ivIcon.setImageResource(R.drawable.icon_red_package_icon_3);
                ivBg.setImageResource(isMine ? R.drawable.icon_new_member_red_package_right_2 : R.drawable.icon_new_member_red_package_left_2);
            } else {
                ivIcon.setImageResource(R.drawable.icon_red_package_icon_3);
                ivBg.setImageResource(isMine ? R.drawable.icon_new_member_red_package_right : R.drawable.icon_new_member_red_package_left);
            }
        } else {
            ivIcon.setImageResource(R.drawable.icon_red_package_icon_3);
            ivBg.setImageResource(isMine ? R.drawable.icon_red_package_right_2 : R.drawable.icon_red_package_left_2);
        }
    }

    protected void showUserInfo(BaseViewHolder helper, BasicMessage item) {
        if (item.isMySendMsg(mMyInfo.getUserId())) {

            int resId = mContext.getResources().getIdentifier("chat_default_avatar_" + mMyInfo.getUserId() % 6,
                    "drawable", mContext.getPackageName());
            Utils.loadImage(mContext, resId, mMyInfo.getAvatarUrl(), helper.getView(R.id.ivRight));
            helper.getView(R.id.ivRight).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ChatBaseActivity) mContext).gotoPager(MyInfoFragment.class);
                }
            });
        } else {
            int resId = mContext.getResources().getIdentifier("chat_default_avatar_" + mChatUser.getUserId() % 6,
                    "drawable", mContext.getPackageName());
            Utils.loadImage(mContext, resId, mChatUser.getAvatarUrl(), helper.getView(R.id.ivLeft));
            helper.getView(R.id.ivLeft).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.BUNDLE_EXTRA, mChatUser);
                    ((ChatBaseActivity) mContext).gotoPager(UserInfoFragment.class, bundle);
                }
            });
        }
    }

    private void showFileMsg(TextView tvName, TextView tvSize, BasicMessage item) {
        if (!TextUtils.isEmpty(item.getContent())) {
            try {
                JSONObject jsonObject = new JSONObject(item.getContent());
                tvName.setText(jsonObject.optString("fileName"));
                tvSize.setText(Utils.getFileSize(jsonObject.optLong("fileSize")));
            } catch (Exception e) {

            }
        }
    }

    private void showLocationMsg(RatioImageView iv, TextView tvTitle, TextView tvLocation, BasicMessage item) {
        String fileName = null;
        if (!TextUtils.isEmpty(item.getContent())) {
            try {
                JSONObject jsonObject = new JSONObject(item.getContent());
                iv.setRatio(jsonObject.getInt("height") * 1.0f / jsonObject.getInt("width"));
                fileName = jsonObject.optString("fileName");
                tvLocation.setText(jsonObject.optString("address"));
                tvTitle.setText(jsonObject.optString("title"));
            } catch (Exception e) {

            }
        }
        String filePath = Utils.getSaveFilePath(mContext, fileName);
        Utils.loadImage(mContext, 0, new File(filePath), item.getUrl(), iv);
    }

    private void showVideoMsg(final RatioImageView iv, ImageView ivPlay, View progressBar, TextView tv, BasicMessage item) {
        String fileName = null;
        if (!TextUtils.isEmpty(item.getContent())) {
            try {
                JSONObject jsonObject = new JSONObject(item.getContent());
                iv.setRatio(jsonObject.getInt("height") * 1.0f / jsonObject.getInt("width"));
                fileName = jsonObject.optString("fileName");
                tv.setText(Utils.getNewTime(jsonObject.optInt("time")));
            } catch (Exception e) {

            }
        }
        File file = new File(Utils.getSaveFilePath(mContext, fileName));
        iv.setTag(R.id.chat_id, fileName);
        if (file.exists()) {
            ivPlay.setVisibility(View.VISIBLE);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            Bitmap bmp = BitmapUtil.getVideoThumbnail(file.getAbsolutePath(), MINI_KIND);
            if (bmp != null || file.length() > 1024 * 1024) {
                iv.setImageBitmap(BitmapUtil.getVideoThumbnail(file.getAbsolutePath(), MINI_KIND));
                return;
            }
        }
        ivPlay.setVisibility(View.GONE);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        OkHttpClientManager.getInstance().downloadAsyn(item.getUrl(), file, new OkHttpClientManager.HttpCallBack() {
            @Override
            public void successful() {
                String tag = (String) iv.getTag(R.id.chat_id);
                if (tag.equals(file.getName())) {
                    iv.post(new Runnable() {
                        @Override
                        public void run() {
                            ivPlay.setVisibility(View.VISIBLE);
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            iv.setImageBitmap(BitmapUtil.getVideoThumbnail(file.getAbsolutePath(), MINI_KIND));
                        }
                    });
                }
            }

            public void progress(int progress) {
            }

            @Override
            public void failed(Exception e) {

            }
        });
    }

    private void showImageMsg(final RatioImageView iv, final ProgressBar bar, BasicMessage item) {
        String fileName = null;
        int width = Utils.dip2px(mContext, 120);
        int height = width;
        if (!TextUtils.isEmpty(item.getContent())) {
            try {
                JSONObject jsonObject = new JSONObject(item.getContent());
                float ratio = jsonObject.getInt("height") * 1.0f / jsonObject.getInt("width");
                iv.setRatio(ratio);
                height = (int) (width * ratio);
                fileName = jsonObject.optString("fileName");
            } catch (Exception e) {
                iv.setRatio(1.0f);
            }
            if (height == 0) {
                height = width;
            }
        }
        String filePath = Utils.getSaveFilePath(mContext, fileName);
        if (bar != null) {
            bar.setVisibility(View.VISIBLE);
        }
        iv.setTag(R.id.chat_id_2, item.getUrl());
        SimpleTarget<Drawable> simpleTarget = new SimpleTarget<Drawable>(width, height) {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                String url = (String) iv.getTag(R.id.chat_id_2);
                if (url.equals(item.getUrl())) {
                    if (bar != null) {
                        bar.setVisibility(View.GONE);
                    }
                    iv.setImageDrawable(resource);
                }
            }
        };
        Utils.loadImage(mContext, 0, new File(filePath), item.getUrl(), simpleTarget);
    }

    private void showVoiceMsg(View llVoice, TextView tvVoiceTime, ImageView ivVoiceAnim, final BasicMessage item) {
        String fileName = null;
        long time = 0l;
        if (!TextUtils.isEmpty(item.getContent())) {
            try {
                JSONObject jsonObject = new JSONObject(item.getContent());
                time = jsonObject.optLong("time") + 500;
                if (time < 1000) {
                    time = 1000;
                }
                tvVoiceTime.setText(String.valueOf(time / 1000));
                fileName = jsonObject.optString("fileName");
            } catch (Exception e) {

            }
        }

        if (item.isPlayingVoice()) {
            ivVoiceAnim.setImageResource(R.drawable.chat_record_animation);
            AnimationDrawable animationDrawable = (AnimationDrawable) ivVoiceAnim.getDrawable();
            animationDrawable.start();
        } else {
            if (ivVoiceAnim.getDrawable() instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) ivVoiceAnim.getDrawable();
                animationDrawable.stop();
            }
            ivVoiceAnim.setImageResource(R.drawable.chat_record_0);
        }
        String filePath = Utils.getSaveFilePath(mContext, fileName);
        File file = new File(filePath);
        if (!file.exists()) {
            OkHttpClientManager.getInstance().downloadAsyn(item.getUrl(), file, null);
        }
        llVoice.setTag(filePath);
        final long voiceTime = time;
        llVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filePath = (String) v.getTag();
                File file = new File(filePath);
                if (file.exists()) {
                    MediaplayerManager.getInstance().playVoice(file);
                    if (!item.isMySendMsg(mMyInfo.getUserId())) {
                        item.sureReceiveStatus(2);
                    }
                    List<BasicMessage> list = getData();
                    for (BasicMessage bean : list) {
                        bean.setPlayingVoice(false);
                    }
                    item.setPlayingVoice(true);
                    notifyDataSetChanged();
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            item.setPlayingVoice(false);
                            notifyDataSetChanged();
                        }
                    }, voiceTime);
                }
            }
        });
    }

    public void setMsgFailed(String msgId) {
        List<BasicMessage> list = getData();
        int size = list.size();
        BasicMessage message;
        for (int i = size - 1; i >= 0; --i) {
            message = list.get(i);
            if (msgId.equals(message.getMessageId())) {
                message.setSendStatus(-1);
                notifyDataSetChanged();
                return;
            }
        }
    }

    public void setMsgSuccess(String msgId) {
        List<BasicMessage> list = getData();
        int size = list.size();
        BasicMessage message;
        for (int i = size - 1; i >= 0; --i) {
            message = list.get(i);
            if (msgId.equals(message.getMessageId())) {
                message.setSendStatus(1);
                notifyDataSetChanged();
                return;
            }
        }
    }

    public void receiveEnvelope(String msgId) {
        List<BasicMessage> list = getData();
        int size = list.size();
        BasicMessage message;
        for (int i = size - 1; i >= 0; --i) {
            message = list.get(i);
            if (msgId.equals(message.getMessageId())) {
                message.sureReceiveStatus(2);
                notifyDataSetChanged();
                return;
            }
        }
    }

    public void clearMsg() {
        List<BasicMessage> list = getData();
        list.clear();
        notifyDataSetChanged();
    }

    public void deleteMsg(BasicMessage msg) {
        List<BasicMessage> list = getData();
        list.remove(msg);
        notifyDataSetChanged();
        DatabaseOperate.getInstance().deleteOne(msg, mMyInfo.getUserId());
    }

    public void deleteMsg(String msgId) {
        List<BasicMessage> list = getData();
        for (BasicMessage msg : list) {
            if (msg.getMessageId().equals(msgId)) {
                list.remove(msg);
                notifyDataSetChanged();
                return;
            }
        }
    }

    public void deleteCheckMsg() {
        List<BasicMessage> list = getData();
        ArrayList<BasicMessage> items = new ArrayList<>();
        for (int i = 0; i < list.size(); ) {
            if (list.get(i).isCheck()) {
                items.add(list.get(i));
                list.remove(i);
                continue;
            }
            ++i;
        }
        notifyDataSetChanged();
        DatabaseOperate.getInstance().deleteAll(items, mMyInfo.getUserId());
    }

    public ArrayList<BasicMessage> getAllMediaMsgs() {
        List<BasicMessage> list = getData();
        ArrayList<BasicMessage> items = new ArrayList<>();
        for (BasicMessage msg : list) {
            if (msg.getMsgType() == MessageType.TYPE_IMAGE.ordinal() || msg.getMsgType() == MessageType.TYPE_VIDEO.ordinal()) {
                items.add(msg);
            }
        }
        return items;
    }
}
