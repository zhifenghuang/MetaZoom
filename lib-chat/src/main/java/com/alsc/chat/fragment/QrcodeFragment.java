package com.alsc.chat.fragment;

import android.Manifest;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.alsc.chat.R;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;

public class QrcodeFragment extends ChatBaseFragment {

    public static final int USER_QRCODE = 0;
    public static final int GROUP_QRCODE = 1;

    private UserBean mUserInfo;
    private GroupBean mGroup;

    private int mQrType;

    private Bitmap mQrBmp;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_qrcode;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(view);
        mQrType = getArguments().getInt(Constants.BUNDLE_EXTRA, USER_QRCODE);
        ImageView ivQrcode = view.findViewById(R.id.ivQrcode);
        setViewsOnClickListener(R.id.tvSaveQrcode);
        if (mQrType == USER_QRCODE) {
            mUserInfo = (UserBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA_2);
            setText(R.id.tvTitle, R.string.chat_qr_code);
            setText(R.id.tvLocation, getString(mUserInfo.getGender() == 1 ? R.string.chat_male : R.string.chat_female) + "  " +
                    (TextUtils.isEmpty(mUserInfo.getDistrict()) ? getString(R.string.chat_default_area) : mUserInfo.getDistrict()));
            setText(R.id.tvNick, mUserInfo.getNickName());
            setText(R.id.tvId, getString(R.string.chat_account_2, mUserInfo.getLoginAccount()));
            Utils.displayAvatar(getActivity(), R.drawable.chat_default_avatar, mUserInfo.getAvatarUrl(), view.findViewById(R.id.ivAvatar));

//            if (DataManager.getInstance().getMyUserInfo() == null) {
//                return;
//            }
//     //       setText(R.id.tvInviteCode,getString(R.string.app_name));
//            String lan = "en";
//            if (DataManager.getInstance().getLanguage() == 0) {
//                lan = "zh";
//            }
//            String shareUrl = DataManager.getInstance().getMyUserInfo().getUser_info().getInvite_url() + "?tuijianma=" +
//                    DataManager.getInstance().getUserId() + "&lan=" + lan;
//            mQrBmp = QRCodeUtil.createQRImage(getActivity(), shareUrl, null);
//            ivQrcode.setImageBitmap(mQrBmp);
//        } else {
//            mGroup = (GroupBean) getArguments().getSerializable(Constants.BUNDLE_EXTRA_2);
//            setViewGone(R.id.tvLocation);
//            setText(R.id.tvTitle, R.string.chat_group_qr_code);
//            setText(R.id.tvNick, mGroup.getName());
//            setText(R.id.tvId, getString(R.string.chat_group_member_num, String.valueOf(mGroup.getMemberNum())));
//            mQrBmp = QRCodeUtil.createQRImage(getActivity(), "chat_group_" + mGroup.getGroupId(), null);
//            ivQrcode.setImageBitmap(mQrBmp);
//            Utils.displayAvatar(getActivity(), R.drawable.chat_default_avatar, mGroup.getIcon(), view.findViewById(R.id.ivAvatar));
        }
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvSaveQrcode) {
            if (mQrBmp == null) {
                return;
            }
            if (!Utils.isGrantPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ((BaseActivity) getActivity()).requestPermission(null, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return;
            }
            Utils.saveJpegToAlbum(mQrBmp, getActivity());
            showToast(R.string.chat_save_success_and_look_in_album);
        }
    }
}
