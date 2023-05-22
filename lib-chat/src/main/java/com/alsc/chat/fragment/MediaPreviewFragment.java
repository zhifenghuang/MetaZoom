package com.alsc.chat.fragment;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.alsc.chat.R;
import com.alsc.chat.manager.MediaplayerManager;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.alsc.chat.utils.BitmapUtil;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.alsc.chat.view.ShowPicView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;

/**
 * Created by gigabud on 16-6-21.
 */
public class MediaPreviewFragment extends ChatBaseFragment {

    private Bitmap mBmp;

    private int mType;

    private String mFilePath;
    private HashMap<String, String> mVideoInfo;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_photo_preview;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        setViewsOnClickListener(R.id.tvCancel, R.id.tvOk);
        mType = getArguments().getInt(Constants.BUNDLE_EXTRA, CameraFragment.FOR_CHAT_PHOTO);
        mFilePath = getArguments().getString(Constants.BUNDLE_EXTRA_2);
        ShowPicView showPicView = view.findViewById(R.id.ivShowPic);
        SurfaceView surfaceView = view.findViewById(R.id.surfaceView);
        if (mType == CameraFragment.FOR_CHAT_VIDEO) {
            showPicView.setVisibility(View.GONE);
            surfaceView.setVisibility(View.VISIBLE);
            File file = new File(mFilePath);
            if (file.exists()) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
                int screenW = ((BaseActivity)getActivity()).getDisplayMetrics().widthPixels;
                int screenH = ((BaseActivity) getActivity()).getDisplayMetrics().heightPixels;
                mVideoInfo = (HashMap<String, String>) getArguments().getSerializable(Constants.BUNDLE_EXTRA_3);
                float ratio1 = Integer.parseInt(mVideoInfo.get("width")) * 1.0f / Integer.parseInt(mVideoInfo.get("height"));
                float ratio2 = screenW * 1.0f / screenH;
                if (ratio1 > ratio2) {
                    lp.width = screenW;
                    lp.height = (int) (screenW / ratio1);
                } else {
                    lp.width = (int) (screenH * ratio1);
                    lp.height = screenH;
                }
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                surfaceView.setLayoutParams(lp);
                MediaplayerManager.getInstance().playVideo(file, surfaceView);
            }
        } else {
            mBmp = BitmapUtil.getBitmapFromFile(mFilePath, ((BaseActivity)getActivity()).getDisplayMetrics().widthPixels, ((BaseActivity)getActivity()).getDisplayMetrics().heightPixels);
            showPicView.setVisibility(View.VISIBLE);
            surfaceView.setVisibility(View.GONE);
            showPicView.setImageBitmap(mBmp, false);
        }

    }

    @Override
    public void updateUIText() {

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvCancel) {
            getActivity().finish();
        } else if (id == R.id.tvOk) {
            if (mType == CameraFragment.FOR_CHAT_PHOTO)
                if (!Utils.isGrantPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ((BaseActivity) getActivity()).requestPermission(null,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    return;
                }
            File file = new File(mFilePath);
            if (mType == CameraFragment.FOR_CHAT_VIDEO) {
                FileBean bean = new FileBean();
                bean.setType(MessageType.TYPE_VIDEO);
                bean.setExtra(mVideoInfo);
                bean.setFile(file);
                EventBus.getDefault().post(bean);
            } else {
                if (mType == CameraFragment.FOR_CHAT_PHOTO) {
                    FileBean bean = new FileBean();
                    bean.setType(MessageType.TYPE_IMAGE);
                    bean.setFile(file);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("fileName", file.getName());
                    map.put("width", String.valueOf(mBmp.getWidth()));
                    map.put("height", String.valueOf(mBmp.getHeight()));
                    map.put("fileSize", String.valueOf(file.length()));
                    bean.setExtra(map);
                    EventBus.getDefault().post(bean);
                } else {
                    EventBus.getDefault().post(file);
                }
                mBmp.recycle();
                mBmp = null;
            }
            getActivity().finish();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        MediaplayerManager.getInstance().releaseMediaPlayer();
        if (mBmp != null && !mBmp.isRecycled()) {
            mBmp.recycle();
        }
        mBmp = null;
    }

}
