package com.common.lib.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.app.ActivityCompat;

import com.common.lib.R;
import com.common.lib.camera.CameraManager;
import com.common.lib.camera.SensorControler;
import com.common.lib.camera.record.CameraUtils;
import com.common.lib.camera.record.OnCameraUseListener;
import com.common.lib.camera.view.CameraGLSurfaceView;
import com.common.lib.camera.view.SquareCameraContainer;
import com.common.lib.constant.Constants;
import com.common.lib.constant.EventBusEvent;
import com.common.lib.interfaces.OnClickCallback;
import com.common.lib.mvp.IPresenter;
import com.common.lib.utils.MediaStoreUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CameraActivity extends BaseActivity implements View.OnTouchListener {


    private CameraManager mCameraManager;
    private SquareCameraContainer mCameraContainer;

    private boolean mUsingCamera;

    private boolean mIsToPreviewPage;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_camera;
    }

    @Override
    protected void onCreated(@Nullable Bundle savedInstanceState) {
        setViewsOnClickListener(R.id.btnTakePhotoOrRecord, R.id.btnFlashlight, R.id.btnSwitchCamera, R.id.btnAlbum);
        mIsToPreviewPage = false;
    }

    @NotNull
    @Override
    protected IPresenter onCreatePresenter() {
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraManager = CameraManager.getInstance(this);
        mCameraManager.setCameraDirection(CameraManager.CameraDirection.CAMERA_BACK);
        initCameraLayout();
        findViewById(R.id.btnTakePhotoOrRecord).setVisibility(View.VISIBLE);
        setViewVisible(R.id.recorder_flashlight_parent1, R.id.btnAlbum);
        showOrHideAllBtn(true);
        mUsingCamera = false;
        findViewById(R.id.btnTakePhotoOrRecord).setOnTouchListener(this);
        mIsToPreviewPage = false;
    }

    @Override
    public void onPause() {
        super.onPause();
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
        RelativeLayout topLayout = findViewById(R.id.recorder_surface_parent);
        topLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.focusView).setOnTouchListener(this);
        if (topLayout.getChildCount() > 0)
            topLayout.removeAllViews();

        if (mCameraContainer == null) {
            if (topLayout.getChildCount() > 0)
                topLayout.removeAllViews();
            mCameraContainer = new SquareCameraContainer(this);
        }
        mCameraContainer.onStart();
        mCameraContainer.bindActivity(this);
        if (mCameraContainer.getParent() == null) {
            RelativeLayout.LayoutParams layoutParam1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParam1.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            topLayout.addView(mCameraContainer, layoutParam1);
        }

        showSwitchCameraIcon();
    }

    private void showOrHideAllBtn(final boolean isShow) {
        runOnUiThread(new Runnable() {
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
            findViewById(R.id.btnFlashlight).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.btnFlashlight).setVisibility(View.VISIBLE);
            showFlashIcon();
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            findViewById(R.id.btnSwitchCamera).setVisibility(View.VISIBLE);
        }
    }

    private void showFlashIcon() {
        if (mCameraManager.getLightStatus() == CameraManager.FlashLigthStatus.LIGHT_ON) {
            ((ImageView) findViewById(R.id.btnFlashlight)).setImageResource(R.drawable.camera_flashon);
        } else {
            ((ImageView) findViewById(R.id.btnFlashlight)).setImageResource(R.drawable.camera_flashoff);
        }
    }

    private long mTapTime, mStartRecordingTime, mTouchDownTime;
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
                mTouchDownTime = System.currentTimeMillis();
                mTouchType = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchType == 0 && System.currentTimeMillis() - mTouchDownTime > 200) {
                    mTouchType = 1;
                    if (!initTakeOrRecord(true)) {
                        mTouchType = 3;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchType == 0) {
                    mTouchType = 1;
                    if (!initTakeOrRecord(false)) {
                        mTouchType = 3;
                    }
                }
                if (mTouchType == 1) {
                    mIsFingerUp = true;
                    synchronized (mLockObject) {
                        mIsStartTimer = false;
                        if (mStartRecordingTime > 0) {
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
                                findViewById(R.id.btnTakePhotoOrRecord).setVisibility(View.INVISIBLE);
                                mUsingCamera = true;
                                boolean isSuccessful = mCameraContainer.takePicture(new OnCameraUseListener() {
                                    @Override
                                    public void takePicture(final Bitmap bmp) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTakeBmp = bmp;
                                                if (bmp != null) {
                                                    mCameraContainer.stopPreview();
                                                    ImageView showPic = findViewById(R.id.showPic);
                                                    showPic.setVisibility(View.VISIBLE);
                                                    showPic.setImageBitmap(bmp);
                                                    String srcPath = CameraUtils.saveJpeg(bmp, CameraActivity.this);
                                                    toEditPostActivity(srcPath);
                                                } else {
                                                    SensorControler.getInstance(CameraActivity.this).unlockFocus();
                                                    mUsingCamera = false;
                                                    mCameraContainer.startPreview();
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
                                    showOrHideAllBtn(true);
                                }
                            } else {
                                mUsingCamera = false;
                                mCameraContainer.startPreview();
                                showOrHideAllBtn(true);
                            }
                        }
                    }
                } else if (mTouchType == 3) {
                    mUsingCamera = false;
                    mCameraContainer.startPreview();
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
            if (!CameraUtils.isGrantPermission(this, Manifest.permission.CAMERA)
                    || !CameraUtils.isGrantPermission(this, Manifest.permission.RECORD_AUDIO)
                    || !CameraUtils.isGrantPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                showRequestPermissionDialog(1, "相机,录音,存储", new OnClickCallback() {
                    @Override
                    public void onClick(int viewId) {
                        requestPermission(null, Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                });
                return false;
            }
        } else {
            if (!CameraUtils.isGrantPermission(this, Manifest.permission.CAMERA)
                    || !CameraUtils.isGrantPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showRequestPermissionDialog(1, "相机,存储", new OnClickCallback() {
                    @Override
                    public void onClick(int viewId) {
                        requestPermission(null, Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                });
                return false;
            }
        }
        mTapTime = System.currentTimeMillis();
        mStartRecordingTime = 0;
        mIsFingerUp = false;
        if (isRecord) {
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
                        if (recordTime >= CameraGLSurfaceView.MAX_DURATION) {
                            runOnUiThread(new Runnable() {
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
        findViewById(R.id.btnTakePhotoOrRecord).setVisibility(View.INVISIBLE);
        mCameraContainer.stopRecording(new OnCameraUseListener() {
            @Override
            public void takePicture(Bitmap bmp) {

            }

            @Override
            public void recordingEnd(String videoPath, int width, int height) {
                if (System.currentTimeMillis() - mStartRecordingTime > 1100) {
                    mCameraContainer.stopPreview();
                    toEditPostActivity(videoPath);
                    findViewById(R.id.btnTakePhotoOrRecord).setOnTouchListener(null);
                } else {
                    mUsingCamera = false;
                    mCameraContainer.startPreview();
                    showOrHideAllBtn(true);
                }
            }
        });
    }


    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id == R.id.btnFlashlight) {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
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
        } else if (id == R.id.btnAlbum) {
            openGallery();
        }
    }

    @Override
    public void onGetImageWithUri(@NotNull Uri uri) {
        toEditPostActivity(MediaStoreUtil.INSTANCE.getRealPathFromUri(CameraActivity.this, uri));
    }

    private synchronized void toEditPostActivity(String srcPath) {
        if (mIsToPreviewPage) {
            return;
        }
        mIsToPreviewPage = true;

    }

    @Override
    public void onStop() {
        super.onStop();
        ImageView showPic = findViewById(R.id.showPic);
        showPic.setImageBitmap(null);
        if (mTakeBmp != null && !mTakeBmp.isRecycled()) {
            mTakeBmp.recycle();
        }
        mTakeBmp = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMsg(String str) {
        if (TextUtils.isEmpty(str)) {
            return;
        }
        if (str.equals(EventBusEvent.FINISH_ONE_ACTIVITY)) {
            finish();
        }
    }
}
