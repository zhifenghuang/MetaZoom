package com.alsc.chat.fragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alsc.chat.R;
import com.blankj.utilcode.util.AppUtils;
import com.alsc.chat.http.OkHttpClientManager;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.alsc.chat.view.ShowPicView;
import com.chs.filepicker.filepicker.util.OpenFile;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.xiao.nicevideoplayer.NiceVideoPlayer;
import com.xiao.nicevideoplayer.NiceVideoPlayerManager;
import com.xiao.nicevideoplayer.TxVideoPlayerController;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ShowMessageFragment extends ChatBaseFragment {

    public static final int TYPE_SHOW_CHAT_MESSAGE = 0;
    public static final int TYPE_SHOW_IMAGE_URL = 1;

    private int mType;
    private BasicMessage mMessage;
    private String mUrl;

    private ArrayList<BasicMessage> mMsgList;
    private ArrayList<View> mViewList;
    private int mCurrentPos, mLastPos;

    private final int MAX_VIEW_SIZE = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt(Constants.BUNDLE_EXTRA, TYPE_SHOW_CHAT_MESSAGE);
        if (mType == TYPE_SHOW_CHAT_MESSAGE) {
            mMessage = (BasicMessage) getArguments().getSerializable(Constants.BUNDLE_EXTRA_2);
            if (mMessage.getMsgType() == MessageType.TYPE_IMAGE.ordinal()
                    || mMessage.getMsgType() == MessageType.TYPE_VIDEO.ordinal()) {
                mMsgList = (ArrayList<BasicMessage>) getArguments().getSerializable(Constants.BUNDLE_EXTRA_3);
            }
        } else {
            mUrl = getArguments().getString(Constants.BUNDLE_EXTRA_2, "");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        int resId = 0;
        if (mType == TYPE_SHOW_IMAGE_URL) {
            resId = R.layout.item_message_media;
        } else {
            int msgType = mMessage.getMsgType();
            if (msgType == MessageType.TYPE_IMAGE.ordinal() || msgType == MessageType.TYPE_VIDEO.ordinal()) {
                resId = R.layout.layout_message_media;
            } else if (msgType == MessageType.TYPE_LOCATION.ordinal()) {
                resId = R.layout.layout_message_location;
            } else if (msgType == MessageType.TYPE_FILE.ordinal()) {
                resId = R.layout.layout_message_file;
            }
        }
        return resId;
    }

    @Override
    protected void onViewCreated(View view) {
        if (mType == TYPE_SHOW_IMAGE_URL) {
            showImage(view);
        } else {
            int msgType = mMessage.getMsgType();
            if (msgType == MessageType.TYPE_IMAGE.ordinal() || msgType == MessageType.TYPE_VIDEO.ordinal()) {
                showMedia(view);
            } else if (msgType == MessageType.TYPE_FILE.ordinal()) {
                showFileMsg(view);
            }
        }
    }

    private void showMedia(View view) {
        final ViewPager viewPager = view.findViewById(R.id.viewPager);
        mViewList = new ArrayList<>();
        for (int i = 0; i < MAX_VIEW_SIZE; ++i) {
            mViewList.add(LayoutInflater.from(getActivity()).inflate(R.layout.item_message_media, null));
        }
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mMsgList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View item = mViewList.get(position % MAX_VIEW_SIZE);
                container.addView(item);
                return item;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mViewList.get(position % MAX_VIEW_SIZE));
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mCurrentPos = viewPager.getCurrentItem();
                if (state == ViewPager.SCROLL_STATE_IDLE && mLastPos != mCurrentPos) {
                    mLastPos = mCurrentPos;
                    if (mCurrentPos > 0) {
                        goneView(mViewList.get((mCurrentPos - 1) % MAX_VIEW_SIZE), mMsgList.get(mCurrentPos - 1));
                    }
                    if (mCurrentPos < mMsgList.size() - 1) {
                        goneView(mViewList.get((mCurrentPos + 1) % MAX_VIEW_SIZE), mMsgList.get(mCurrentPos + 1));
                    }
                    mMessage = mMsgList.get(mCurrentPos);
                    if (mMessage.getMsgType() == MessageType.TYPE_IMAGE.ordinal()) {
                        showImage(mViewList.get(mCurrentPos % MAX_VIEW_SIZE));
                    } else {
                        playVideo(mViewList.get(mCurrentPos % MAX_VIEW_SIZE));
                    }
                }
            }
        });
        mCurrentPos = 0;
        for (BasicMessage msg : mMsgList) {
            if (msg.getMessageId().equals(mMessage.getMessageId())) {
                break;
            }
            ++mCurrentPos;
        }
        viewPager.setCurrentItem(mCurrentPos);
        if (mMessage.getMsgType() == MessageType.TYPE_IMAGE.ordinal()) {
            showImage(mViewList.get(mCurrentPos % MAX_VIEW_SIZE));
        } else {
            playVideo(mViewList.get(mCurrentPos % MAX_VIEW_SIZE));
        }
        mLastPos = mCurrentPos;
        if (mCurrentPos > 0) {
            goneView(mViewList.get((mCurrentPos - 1) % MAX_VIEW_SIZE), mMsgList.get(mCurrentPos - 1));
        }
        if (mCurrentPos < mMsgList.size() - 1) {
            goneView(mViewList.get((mCurrentPos + 1) % MAX_VIEW_SIZE), mMsgList.get(mCurrentPos + 1));
        }
    }

    private void goneView(View view, BasicMessage msg) {
        NiceVideoPlayer niceVideoPlayer = view.findViewById(R.id.videoView);
        niceVideoPlayer.setVisibility(View.GONE);
        view.findViewById(R.id.ivClose).setVisibility(View.GONE);
        ShowPicView showPicView = view.findViewById(R.id.ivShowPic);
        showPicView.setVisibility(View.VISIBLE);
        String fileName = null;
        if (!TextUtils.isEmpty(msg.getContent())) {
            try {
                JSONObject jsonObject = new JSONObject(msg.getContent());
                fileName = jsonObject.optString("fileName");
            } catch (Exception e) {

            }
        }
        String filePath = Utils.getSaveFilePath(getActivity(), fileName);

        Utils.loadImage(getActivity(), 0, new File(filePath), msg.getUrl(), showPicView);
    }

    private void showFileMsg(View view) {
        setTopStatusBarStyle(view);
        TextView tvLeft = view.findViewById(R.id.tvTitle);
        tvLeft.setText(R.string.chat_file);
        tvLeft.setVisibility(View.VISIBLE);
        tvLeft.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_00_00_00));
        if (!TextUtils.isEmpty(mMessage.getContent())) {
            try {
                JSONObject jsonObject = new JSONObject(mMessage.getContent());
                String fileName = jsonObject.optString("fileName");
                setText(R.id.tvFileName, fileName);
                TextView tvFileOperator = view.findViewById(R.id.tvFileOperator);
                final File file = new File(Utils.getSaveFilePath(getActivity(), fileName));
                tvFileOperator.setVisibility(View.VISIBLE);
                if (!file.exists()) {
                    tvFileOperator.setText(getString(R.string.chat_download));
                    tvFileOperator.setTag(0);
                } else {
                    tvFileOperator.setText(getString(R.string.chat_open_file));
                    tvFileOperator.setTag(1);
                }
                tvFileOperator.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int tag = (int) v.getTag();
                        if (tag == 0) {
                            v.setEnabled(false);
                            OkHttpClientManager.getInstance().downloadAsyn(mMessage.getUrl(), file, new OkHttpClientManager.HttpCallBack() {
                                @Override
                                public void successful() {
                                    if (getActivity() == null || getView() == null) {
                                        return;
                                    }
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            v.setTag(1);
                                            v.setEnabled(true);
                                            tvFileOperator.setText(getString(R.string.chat_open_file));
                                        }
                                    });

                                }

                                public void progress(int progress) {

                                }

                                @Override
                                public void failed(Exception e) {
                                    if (getActivity() == null || getView() == null) {
                                        return;
                                    }
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            v.setEnabled(true);
                                        }
                                    });
                                }
                            });
                        } else {
                            String path = file.getAbsolutePath();
                            if (path.endsWith(".apk")) {
                                installApk(file);
                            } else {
                                startActivity(Intent.createChooser(OpenFile.openFile(file.getAbsolutePath(), getActivity().getApplicationContext()), getString(R.string.chat_select_application)));
                            }
                        }
                    }
                });
            } catch (Exception e) {
            }
        }

    }

    private void installApk(File apkFile) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(getActivity(), AppUtils.getAppPackageName() + ".fileprovider", apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromFile(apkFile);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            }
            getActivity().startActivity(intent);
            ((BaseActivity) getActivity()).finishAllActivity();
        } catch (Exception e) {

        }
    }

    private void showImage(View view) {
        ShowPicView picView = view.findViewById(R.id.ivShowPic);
        picView.setVisibility(View.VISIBLE);
        view.findViewById(R.id.videoView).setVisibility(View.GONE);
        view.findViewById(R.id.ivClose).setVisibility(View.GONE);
        picView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (mType == TYPE_SHOW_IMAGE_URL) {
            Utils.loadImage(getActivity(), 0, mUrl, picView);
            return;
        }
        String fileName = null;
        if (!TextUtils.isEmpty(mMessage.getContent())) {
            try {
                JSONObject jsonObject = new JSONObject(mMessage.getContent());
                fileName = jsonObject.optString("fileName");
            } catch (Exception e) {

            }
        }
        String filePath = Utils.getSaveFilePath(getActivity(), fileName);
        Utils.loadImage(getActivity(), 0, new File(filePath), mMessage.getUrl(), picView);
    }

    private void playVideo(View view) {
        NiceVideoPlayerManager.instance().releaseNiceVideoPlayer();
        view.findViewById(R.id.ivShowPic).setVisibility(View.GONE);
        ImageView ivClose = view.findViewById(R.id.ivClose);
        ivClose.setVisibility(View.VISIBLE);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        NiceVideoPlayer niceVideoPlayer = view.findViewById(R.id.videoView);
        niceVideoPlayer.setVisibility(View.VISIBLE);
        niceVideoPlayer.setPlayerType(NiceVideoPlayer.TYPE_IJK);
        TxVideoPlayerController controller = new TxVideoPlayerController(getActivity());
        controller.setTitle("");
        niceVideoPlayer.setController(controller);
        String fileName = null;
        int width = 0, height = 0;
        if (!TextUtils.isEmpty(mMessage.getContent())) {
            try {
                JSONObject jsonObject = new JSONObject(mMessage.getContent());
                fileName = jsonObject.optString("fileName");
                width = jsonObject.optInt("width");
                height = jsonObject.optInt("height");
            } catch (Exception e) {

            }
        }
//        Log.e("aaaaaaaaaaa", mMessage.getUrl() + "\n" + mMessage.getContent());
        String filePath = Utils.getSaveFilePath(getActivity(), fileName);
        File file = new File(filePath);
//        Log.e("aaaaaaaaaaa", mMessage.getUrl() + "\n" + file.exists() + "\n" + file.getAbsolutePath());
        if (file.exists()) {
            view.findViewById(R.id.progressBar).setVisibility(View.GONE);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) niceVideoPlayer.getLayoutParams();
            int screenW = ((BaseActivity) getActivity()).getDisplayMetrics().widthPixels;
            int screenH = ((BaseActivity) getActivity()).getDisplayMetrics().heightPixels;
            float ratio1 = width * 1.0f / height;
            float ratio2 = screenW * 1.0f / screenH;
            if (ratio1 > ratio2) {
                lp.width = screenW;
                lp.height = (int) (screenW / ratio1);
            } else {
                lp.width = (int) (screenH * ratio1);
                lp.height = screenH;
            }
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            niceVideoPlayer.setLayoutParams(lp);
            niceVideoPlayer.setUp(filePath, null);
            niceVideoPlayer.start();
        } else {
            view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        }
    }




    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (getView() != null && map != null) {
            if (map.containsKey(Constants.DOWNLOAD_FILE)) {
                String url = (String) map.get(Constants.DOWNLOAD_FILE);
                if (mMessage != null && mMessage.getMsgType() == MessageType.TYPE_VIDEO.ordinal() &&
                        mMessage.getUrl().equals(url)) {
                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playVideo(mViewList.get(mCurrentPos % MAX_VIEW_SIZE));
                        }
                    }, 300);
                }
            }
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        NiceVideoPlayerManager.instance().releaseNiceVideoPlayer();
    }
}
