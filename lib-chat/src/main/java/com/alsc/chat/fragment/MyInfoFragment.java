package com.alsc.chat.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.alsc.chat.R;
import com.alsc.chat.utils.BitmapUtil;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;

import com.alsc.chat.manager.UPYFileUploadManger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashMap;


public class MyInfoFragment extends ChatBaseFragment {

    private UserBean mMyInfo;

    private static final int ALBUM_REQUEST_CODE = 10002;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_my_info;
    }

    @Override
    protected void onViewCreated(View view) {
        mMyInfo = DataManager.getInstance().getUser();
        setTopStatusBarStyle(R.id.topView);
        setText(R.id.tvTitle, R.string.chat_person_info);
        setText(R.id.tvNick, mMyInfo.getNickName());
        String account = mMyInfo.getLoginAccount();
        setText(R.id.tvID, account.substring(0, 6) + "..." + account.substring(account.length() - 6));
        setViewsOnClickListener(R.id.llAvatar, R.id.llNick);
        int resId = getResources().getIdentifier("chat_default_avatar_" + mMyInfo.getUserId() % 6,
                "drawable", getActivity().getPackageName());
        Utils.loadImage(getActivity(), resId, mMyInfo.getAvatarUrl(), fv(R.id.ivAvatar));
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llAvatar) {
            showSelectPhotoDialog();
        } else if (id == R.id.llNick) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, mMyInfo);
            gotoPager(UpdateNickFragment.class, bundle);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (getView() != null && map != null) {
            if (map.containsKey(Constants.UPDATE_NICK)) {
                String nick = (String) map.get(Constants.UPDATE_NICK);
                mMyInfo.setNickName(nick);
                setText(R.id.tvNick, mMyInfo.getNickName());
                DataManager.getInstance().saveUser(mMyInfo);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveAvatarFile(File file) {
        if (getView() != null) {
            ((BaseActivity) getActivity()).showProgressDialog();
            UPYFileUploadManger.getInstance().uploadFile(file);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveAvatarUrl(UploadAvatarEvent avatar) {
        if (getView() == null) {
            return;
        }
        if (avatar.isSuccess()) {
            updateInfo("", avatar.getUrl(), -1, "");
        } else {
            ((BaseActivity) getActivity()).dismissProgressDialog();
            ((BaseActivity) getActivity()).showToast(getString(R.string.chat_upload_avatar_failed));
        }
    }

    private void updateInfo(String nick, String avatarUrl, int gender, String area) {
        ChatHttpMethods.getInstance().updateUserProfile(nick, avatarUrl, gender, area, new HttpObserver(new SubscriberOnNextListener() {
            @Override
            public void onNext(Object o, String msg) {
                if (getView() != null) {
                    if (!TextUtils.isEmpty(avatarUrl)) {
                        int resId = getResources().getIdentifier("chat_default_avatar_" + mMyInfo.getUserId() % 6,
                                "drawable", getActivity().getPackageName());
                        Utils.loadImage(getActivity(), resId, avatarUrl, fv(R.id.ivAvatar));
                        mMyInfo.setAvatarUrl(avatarUrl);
                        DataManager.getInstance().saveUser(mMyInfo);
                    }
                    ((BaseActivity) getActivity()).dismissProgressDialog();
                }
            }
        }, getActivity(), (ChatBaseActivity) getActivity()));
    }


    private void showSelectPhotoDialog() {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_select_photo_type);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btnTakePhoto, R.id.btnAlbum, R.id.btnCancel);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.btnTakePhoto) {
                    if (!Utils.isGrantPermission(getActivity(),
                            Manifest.permission.CAMERA)) {
                        ((BaseActivity) getActivity()).requestPermission(null, Manifest.permission.CAMERA);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.BUNDLE_EXTRA, CameraFragment.FOR_AVATAR);
                        gotoPager(CameraFragment.class, bundle);
                    }
                } else if (viewId == R.id.btnAlbum) {
                    if (!Utils.isGrantPermission(getActivity(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        ((BaseActivity) getActivity()).requestPermission(null, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");//相片类型
                        startActivityForResult(intent, ALBUM_REQUEST_CODE);
                    }
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ALBUM_REQUEST_CODE) {
                try {
                    String filePath;
                    int sdkVersion = Build.VERSION.SDK_INT;
                    if (sdkVersion >= 19) { // api >= 19
                        filePath = ((ChatBaseActivity) getActivity()).getRealPathFromUriAboveApi19(data.getData());
                    } else { // api < 19
                        filePath = ((ChatBaseActivity) getActivity()).getRealPathFromUriBelowAPI19(data.getData());
                    }
                    String newPath;
                    File file = new File(filePath);
                    if (file.length() > 2 * 1024 * 1024) {  //大于2M压缩处理
                        Bitmap bmp = BitmapUtil.getBitmapFromFile(filePath, ((ChatBaseActivity) getActivity()).getDisplayMetrics().widthPixels,
                                ((ChatBaseActivity) getActivity()).getDisplayMetrics().heightPixels);
                        newPath = Utils.saveJpeg(bmp, getActivity());
                    } else {
                        newPath = filePath;
                    }
                    onReceiveAvatarFile(new File(newPath));
                } catch (Exception e) {

                }
            }
        }
    }

}
