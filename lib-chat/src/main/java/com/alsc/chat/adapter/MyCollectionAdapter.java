package com.alsc.chat.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alsc.chat.R;
import com.alsc.chat.http.OkHttpClientManager;
import com.alsc.chat.manager.ChatManager;
import com.alsc.chat.manager.MediaplayerManager;
import com.alsc.chat.utils.Utils;
import com.common.lib.bean.*;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.manager.DataManager;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class MyCollectionAdapter extends BaseQuickAdapter<CollectionBean, BaseViewHolder> {

    private Context mContext;

    public MyCollectionAdapter(Context context) {
        super(R.layout.item_my_collection);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, final CollectionBean bean) {
        try {
            final BasicMessage msg;
            if (TextUtils.isEmpty(bean.getGroupId())) {
                msg = ChatManager.getInstance().getGson().fromJson(bean.getContent(), MessageBean.class);
            } else {
                msg = ChatManager.getInstance().getGson().fromJson(bean.getContent(), GroupMessageBean.class);
            }
            final UserBean myInfo = DataManager.getInstance().getUser();
            if (myInfo.getUserId() == msg.getFromId()) {
                helper.setText(R.id.tvInfo, myInfo.getNickName() + "  " + bean.getCreateTime());
            } else {
                ArrayList<UserBean> list = ChatManager.getInstance().getGson().fromJson(msg.getExtra(), new TypeToken<ArrayList<UserBean>>() {
                }.getType());
                for (UserBean userBean : list) {
                    if (userBean.getUserId() != myInfo.getUserId()) {
                        helper.setText(R.id.tvInfo, userBean.getNickName() + "  " + bean.getCreateTime());
                        break;
                    }
                }
            }
            int msgType = msg.getMsgType();
            if (msgType == MessageType.TYPE_TEXT.ordinal()) {
                helper.setGone(R.id.tvText, false)
                        .setText(R.id.tvText, msg.getContent())
                        .setGone(R.id.ivPic, true)
                        .setGone(R.id.llVoice, true)
                        .setGone(R.id.rlVideo, true)
                        .setGone(R.id.llFile, true);
            } else if (msgType == MessageType.TYPE_IMAGE.ordinal()) {
                helper.setGone(R.id.tvText, true)
                        .setGone(R.id.ivPic, false)
                        .setGone(R.id.llVoice, true)
                        .setGone(R.id.rlVideo, true)
                        .setGone(R.id.llFile, true);
                Utils.loadImage(mContext, 0, msg.getUrl(), helper.getView(R.id.ivPic));
            } else if (msgType == MessageType.TYPE_VOICE.ordinal()) {
                helper.setGone(R.id.tvText, true)
                        .setGone(R.id.ivPic, true)
                        .setGone(R.id.llVoice, false)
                        .setGone(R.id.rlVideo, true)
                        .setGone(R.id.llFile, true);
                LinearLayout llVoice = helper.getView(R.id.llVoice);
                String fileName = null;
                long time = 0l;
                if (!TextUtils.isEmpty(msg.getContent())) {
                    try {
                        JSONObject jsonObject = new JSONObject(msg.getContent());
                        time = jsonObject.optLong("time") + 500;
                        if (time < 1000) {
                            time = 1000;
                        }
                        helper.setText(R.id.tvVoiceTime, String.valueOf(time / 1000));
                        fileName = jsonObject.optString("fileName");
                    } catch (Exception e) {

                    }
                }

                ImageView ivVoiceAnim = helper.getView(R.id.ivRecordIcon);
                if (msg.isPlayingVoice()) {
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
                    OkHttpClientManager.getInstance().downloadAsyn(msg.getUrl(), file, null);
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
                            msg.setPlayingVoice(true);
                            notifyDataSetChanged();
                            v.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    msg.setPlayingVoice(false);
                                    notifyDataSetChanged();
                                }
                            }, voiceTime);
                        }
                    }
                });
            } else if (msgType == MessageType.TYPE_VIDEO.ordinal()) {
                helper.setGone(R.id.tvText, true)
                        .setGone(R.id.ivPic, true)
                        .setGone(R.id.llVoice, true)
                        .setGone(R.id.rlVideo, false)
                        .setGone(R.id.llFile, true);

                String fileName = null;
                if (!TextUtils.isEmpty(msg.getContent())) {
                    try {
                        JSONObject jsonObject = new JSONObject(msg.getContent());
                        fileName = jsonObject.optString("fileName");
                        helper.setText(R.id.tvVideoTime, Utils.getNewTime(jsonObject.optInt("time")));
                    } catch (Exception e) {
                    }
                }
                File file = new File(Utils.getSaveFilePath(mContext, fileName));
                final ImageView ivVideo = helper.getView(R.id.ivVideo);
                ivVideo.setTag(R.id.chat_id, fileName);
                if (file.exists()) {
                    Utils.loadImage(mContext, 0, Uri.fromFile(file), ivVideo);
                } else {
                    OkHttpClientManager.getInstance().downloadAsyn(msg.getUrl(), file, new OkHttpClientManager.HttpCallBack() {
                        @Override
                        public void successful() {
                            String tag = (String) ivVideo.getTag(R.id.chat_id);
                            if (tag.equals(file.getName())) {
                                ivVideo.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.loadImage(mContext, 0, Uri.fromFile(file), ivVideo);
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
            } else if (msgType == MessageType.TYPE_FILE.ordinal()) {
                helper.setGone(R.id.tvText, true)
                        .setGone(R.id.ivPic, true)
                        .setGone(R.id.llVoice, true)
                        .setGone(R.id.rlVideo, true)
                        .setGone(R.id.llFile, false);
                if (!TextUtils.isEmpty(msg.getContent())) {
                    try {
                        JSONObject jsonObject = new JSONObject(msg.getContent());
                        helper.setText(R.id.tvFileName, jsonObject.optString("fileName"));
                        helper.setText(R.id.tvFileSize, Utils.getFileSize(jsonObject.optLong("fileSize")));
                    } catch (Exception e) {

                    }
                }
            }
        } catch (Exception e) {

        }
    }
}
