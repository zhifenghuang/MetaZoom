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
        setTopStatusBarStyle(view);
        setText(R.id.tvTitle, R.string.chat_person_info);
        setText(R.id.tvNick, mMyInfo.getNickName());
        setText(R.id.tvGender, mMyInfo.getGender() == 1 ? R.string.chat_male : R.string.chat_female);
        setText(R.id.tvID, String.valueOf(mMyInfo.getUserId()));
        setText(R.id.tvArea, TextUtils.isEmpty(mMyInfo.getDistrict()) ? getString(R.string.chat_default_area) : mMyInfo.getDistrict());
        setViewsOnClickListener(R.id.llAvatar, R.id.llNick, R.id.llGender, R.id.llQrCode, R.id.llArea);
        Utils.loadImage(getActivity(), R.drawable.chat_default_avatar, mMyInfo.getAvatarUrl(), view.findViewById(R.id.ivAvatar));
        if (!TextUtils.isEmpty(mMyInfo.getBindMobile())) {
            setViewVisible(R.id.llPhone, R.id.linePhone);
            setText(R.id.tvPhone, mMyInfo.getBindMobile());
        }
        if (!TextUtils.isEmpty(mMyInfo.getBindEmail())) {
            setViewVisible(R.id.llEmail, R.id.lineEmail);
            setText(R.id.tvEmail, mMyInfo.getBindEmail());
        }
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
        } else if (id == R.id.llGender) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.BUNDLE_EXTRA, ChooseFragment.CHOOSE_GENDER);
            bundle.putInt(Constants.BUNDLE_EXTRA_2, mMyInfo.getGender());
            gotoPager(ChooseFragment.class, bundle);
        } else if (id == R.id.llQrCode) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.BUNDLE_EXTRA, QrcodeFragment.USER_QRCODE);
            bundle.putSerializable(Constants.BUNDLE_EXTRA_2, mMyInfo);
            gotoPager(QrcodeFragment.class, bundle);
        } else if (id == R.id.llArea) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.BUNDLE_EXTRA, ChooseFragment.CHOOSE_COUNTRY);
            gotoPager(ChooseFragment.class, bundle);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceive(HashMap map) {
        if (getView() != null && map != null) {
            if (map.containsKey(ChooseFragment.CHOOSE_COUNTRY)) {
                ChooseFragment.ChooseType type = (ChooseFragment.ChooseType) map.get(ChooseFragment.CHOOSE_COUNTRY);
                setText(R.id.tvArea, type.typeName);
                mMyInfo.setDistrict(type.typeName);
                DataManager.getInstance().saveUser(mMyInfo);
                updateInfo("", "", -1, mMyInfo.getDistrict());
            } else if (map.containsKey(ChooseFragment.CHOOSE_GENDER)) {
                ChooseFragment.ChooseType type = (ChooseFragment.ChooseType) map.get(ChooseFragment.CHOOSE_GENDER);
                mMyInfo.setGender(type.type);
                setText(R.id.tvGender, type.typeName);
                DataManager.getInstance().saveUser(mMyInfo);
                updateInfo("", "", type.type, "");
            } else if (map.containsKey(Constants.UPDATE_NICK)) {
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
                        Utils.displayAvatar(getActivity(), R.drawable.chat_default_avatar, avatarUrl, fv(R.id.ivAvatar));
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
