package com.alsc.chat.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alsc.chat.R;
import com.alsc.chat.hardwrare.CameraManager;
import com.alsc.chat.hardwrare.SensorControler;
import com.alsc.chat.record.OnCameraUseListener;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.alsc.chat.view.CameraGLSurfaceView;
import com.alsc.chat.view.CircleButton;
import com.alsc.chat.view.SquareCameraContainer;
import com.common.lib.activity.BaseActivity;

import java.io.File;
import java.util.HashMap;

public class CameraFragment extends ChatBaseFragment implements View.OnTouchListener {

    public static final int FOR_CHAT_PHOTO = 0;
    public static final int FOR_CHAT_VIDEO = 1;
    public static final int FOR_AVATAR = 2;

    private CameraManager mCameraManager;
    private SquareCameraContainer mCameraContainer;

    private boolean mUsingCamera;

    private int mUseCameraFor;

    private boolean mIsToPreviewPage;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_camera;
    }

    @Override
    protected void onViewCreated(View view) {
        mUseCameraFor = getArguments().getInt(Constants.BUNDLE_EXTRA, FOR_CHAT_PHOTO);
        setViewsOnClickListener(R.id.btnTakePhotoOrRecord, R.id.btnFlashlight, R.id.btnSwitchCamera, R.id.btnClose);
        mIsToPreviewPage = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() == null) {
            return;
        }
        mCameraManager = CameraManager.getInstance(getActivity());
        mCameraManager.setCameraDirection(CameraManager.CameraDirection.CAMERA_BACK);
        initCameraLayout();
        fv(R.id.btnTakePhotoOrRecord).setVisibility(View.VISIBLE);
        ((CircleButton) fv(R.id.btnTakePhotoOrRecord)).resetCircleButton();
        setViewVisible(R.id.recorder_flashlight_parent1, R.id.btnAlbum);
        showOrHideAllBtn(true);
        mUsingCamera = false;
        fv(R.id.btnTakePhotoOrRecord).setOnTouchListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getView() == null) {
            return;
        }
        if (mCameraContainer != null) {
            if (mCameraContainer.getParent() != null) {
                ((ViewGroup) mCameraContainer.getParent()).removeAllViews();
            }
            mCameraContainer.onStop();
        } else {
            if (mCameraManager != null) {
                mCameraManager.releaseActivityCamera();
            }
        }
        mUsingCamera = false;
        mCameraContainer = null;
    }

    private void initCameraLayout() {
        RelativeLayout topLayout = fv(R.id.recorder_surface_parent);
        topLayout.setVisibility(View.VISIBLE);
        fv(R.id.focusView).setOnTouchListener(this);
        if (topLayout.getChildCount() > 0)
            topLayout.removeAllViews();

        if (mCameraContainer == null) {
            if (topLayout.getChildCount() > 0)
                topLayout.removeAllViews();
            mCameraContainer = new SquareCameraContainer(getActivity());
        }
        mCameraContainer.onStart();
        mCameraContainer.bindActivity(getActivity());
        if (mCameraContainer.getParent() == null) {
            RelativeLayout.LayoutParams layoutParam1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParam1.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            topLayout.addView(mCameraContainer, layoutParam1);
        }

        showSwitchCameraIcon();
    }

    private void showOrHideAllBtn(final boolean isShow) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isShow) {
                    setViewVisible(R.id.recorder_flashlight_parent1, R.id.btnAlbum);
                } else {
                    setViewGone(R.id.recorder_flashlight_parent1, R.id.btnAlbum);
                }
            }
        });
    }

    private void showSwitchCameraIcon() {
        if (mCameraManager.getCameraDirection() == CameraManager.CameraDirection.CAMERA_FRONT) {
            fv(R.id.btnFlashlight).setVisibility(View.INVISIBLE);
        } else {
            fv(R.id.btnFlashlight).setVisibility(View.VISIBLE);
            showFlashIcon();
        }
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            fv(R.id.btnSwitchCamera).setVisibility(View.VISIBLE);
        }
    }

    private void showFlashIcon() {
        if (mCameraManager.getLightStatus() == CameraManager.FlashLigthStatus.LIGHT_ON) {
            ((ImageButton) fv(R.id.btnFlashlight)).setImageResource(R.drawable.camera_flashon);
        } else {
            ((ImageButton) fv(R.id.btnFlashlight)).setImageResource(R.drawable.camera_flashoff);
        }
    }

    private long mTapTime, mStartRecordingTime;// mTouchDownTime;
    private boolean mIsStartTimer, mIsFingerUp;
    private Object mLockObject = new Object();
    private int mTouchType; //0表示什么都么做，1表示拍照或录制视频
    private Bitmap mTakeBmp;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mUsingCamera || mCameraContainer == null) {
            return true;
        }
        if (v.getId() == R.id.focusView) {
            mCameraContainer.onTouchEvent(event);
            return true;
        }
        if (v.getId() != R.id.btnTakePhotoOrRecord) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchType = 0;
                ((CircleButton) fv(R.id.btnTakePhotoOrRecord)).startScaleAnim();
                if (!initTakeOrRecord(mUseCameraFor == FOR_CHAT_VIDEO)) {
                    mTouchType = 3;
                } else {
                    mTouchType = 1;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsToPreviewPage) {
                    break;
                }
                if (mTouchType == 1) {
                    mIsFingerUp = true;
                    synchronized (mLockObject) {
                        mIsStartTimer = false;
                        if (mUseCameraFor == FOR_CHAT_VIDEO) {
                            if (!mCameraContainer.isRecording()) {
                                return true;   //在此之前就已经结束
                            }
                            stopRecording();
                        } else {
                            int x = (int) event.getRawX();
                            int y = (int) event.getRawY();
                            int[] location = new int[2];
                            v.getLocationOnScreen(location);
                            if ((x > location[0] && x < location[0] + v.getWidth()) && (y > location[1] && y < location[1] + v.getHeight())) {
                                fv(R.id.btnTakePhotoOrRecord).setVisibility(View.INVISIBLE);
                                mUsingCamera = true;
                                boolean isSuccessful = mCameraContainer.takePicture(new OnCameraUseListener() {
                                    @Override
                                    public void takePicture(final Bitmap bmp) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTakeBmp = bmp;
                                                if (bmp != null) {
                                                    mCameraContainer.stopPreview();
                                                    ImageView showPic = fv(R.id.showPic);
                                                    showPic.setVisibility(View.VISIBLE);
                                                    showPic.setImageBitmap(bmp);
                                                    goPreviewPage(Utils.saveJpeg(bmp, getActivity()), null);
                                                } else {
                                                    SensorControler.getInstance(getActivity()).unlockFocus();
                                                    mUsingCamera = false;
                                                    mCameraContainer.startPreview();
                                                    ((CircleButton) fv(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                                                    showOrHideAllBtn(true);
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void recordingEnd(String videoPath, int width, int height) {

                                    }
                                });
                                if (!isSuccessful) {
                                    mUsingCamera = false;
                                    mCameraContainer.startPreview();
                                    ((CircleButton) fv(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                                    showOrHideAllBtn(true);
                                }
                            } else {
                                mUsingCamera = false;
                                mCameraContainer.startPreview();
                                ((CircleButton) fv(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                                showOrHideAllBtn(true);
                            }
                        }
                    }
                } else if (mTouchType == 3) {
                    mUsingCamera = false;
                    mCameraContainer.startPreview();
                    ((CircleButton) fv(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                    showOrHideAllBtn(true);
                }
                break;
            default:
                break;
        }
        return true;
    }


    private boolean initTakeOrRecord(boolean isRecord) {
        if (isRecord) {
            if (!Utils.isGrantPermission(getActivity(), Manifest.permission.CAMERA)
                    || !Utils.isGrantPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                    || !Utils.isGrantPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ((BaseActivity) getActivity()).requestPermission(null, Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                ((CircleButton) fv(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                return false;
            }
        } else {
            if (!Utils.isGrantPermission(getActivity(), Manifest.permission.CAMERA)
                    || !Utils.isGrantPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ((BaseActivity) getActivity()).requestPermission(null, Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                ((CircleButton) fv(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                return false;
            }
        }
        if (isRecord) {
            mTapTime = System.currentTimeMillis();
            mStartRecordingTime = 0;
            mIsFingerUp = false;
            startTimer();
        }
        return true;
    }

    private void startTimer() {
        if (mIsStartTimer) {
            return;
        }
        mIsStartTimer = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsStartTimer) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mStartRecordingTime == 0 && System.currentTimeMillis() - mTapTime > 500 && !mIsFingerUp) {   //当按下时间超过0.5s时,默认为开始录制视频
                        synchronized (mLockObject) {
                            showOrHideAllBtn(false);
                            mCameraContainer.startRecording();
                            mStartRecordingTime = System.currentTimeMillis();
                        }
                    }
                    if (mIsFingerUp) {
                        mIsStartTimer = false;
                        break;
                    }
                    if (mCameraContainer.isRecording()) {
                        long recordTime = System.currentTimeMillis() - mStartRecordingTime;
                        ((CircleButton) fv(R.id.btnTakePhotoOrRecord)).resetArcAngle(recordTime, CameraGLSurfaceView.MAX_DURATION);
                        if (recordTime >= CameraGLSurfaceView.MAX_DURATION) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stopRecording();
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    private void stopRecording() {
        mIsStartTimer = false;
        mUsingCamera = true;
        fv(R.id.btnTakePhotoOrRecord).setVisibility(View.INVISIBLE);
        mCameraContainer.stopRecording(new OnCameraUseListener() {
            @Override
            public void takePicture(Bitmap bmp) {

            }

            @Override
            public void recordingEnd(final String videoPath, final int width, final int height) {
                final long recordTime = System.currentTimeMillis() - mStartRecordingTime;
                if (recordTime > 2100) {
                    mCameraContainer.stopPreview();
                    fv(R.id.btnTakePhotoOrRecord).setOnTouchListener(null);
                    mCameraContainer.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            final FileBean bean = new FileBean();
//                            bean.setType(MessageType.TYPE_VIDEO);
                            File file = new File(videoPath);
                            final HashMap<String, String> map = new HashMap<>();
                            map.put("time", String.valueOf(recordTime));
                            map.put("width", String.valueOf(width));
                            map.put("height", String.valueOf(height));
                            map.put("fileName", file.getName());
                            map.put("fileSize", String.valueOf(file.length()));
//                            bean.setExtra(map);
//                            bean.setFile(file);
//                            EventBus.getDefault().post(bean);
//                            goBack();
                            goPreviewPage(videoPath, map);
                        }
                    }, 1000);
                } else {
                    mUsingCamera = false;
                    mCameraContainer.startPreview();
                    ((CircleButton) fv(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                    showOrHideAllBtn(true);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        ImageView showPic = fv(R.id.showPic);
        showPic.setImageBitmap(null);
        if (mTakeBmp != null && !mTakeBmp.isRecycled()) {
            mTakeBmp.recycle();
        }
        mTakeBmp = null;
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnFlashlight) {
            if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                return;
            }
            mCameraManager.setLightStatus(mCameraManager.getLightStatus().next());
            showFlashIcon();
        } else if (id == R.id.btnSwitchCamera) {
            mCameraManager.setCameraDirection(mCameraManager.getCameraDirection().next());
            v.setClickable(false);
            mCameraContainer.switchCamera();
            v.postDelayed(new Runnable() {
                @Override
                public void run() {
                    v.setClickable(true);
                }
            }, 500);
            showSwitchCameraIcon();
        } else if (id == R.id.btnClose) {
            finish();
        }
    }

    private void goPreviewPage(String path, HashMap<String, String> map) {
        if (mIsToPreviewPage) {
            return;
        }
        mIsToPreviewPage = true;
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.BUNDLE_EXTRA, mUseCameraFor);
        bundle.putString(Constants.BUNDLE_EXTRA_2, path);
        if (map != null) {
            bundle.putSerializable(Constants.BUNDLE_EXTRA_3, map);
        }
        gotoPager(MediaPreviewFragment.class, bundle);
        finish();
    }
}
