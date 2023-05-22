package com.alsc.chat.view;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alsc.chat.R;
import com.blankj.utilcode.util.BarUtils;

/**
 * 录音时弹出来的
 *
 * @author gigabud
 */
@SuppressLint("HandlerLeak")
public class RecorderView extends LinearLayout implements OnErrorListener {
    private static final String TAG = "RecorderView";
    private MediaRecorder mediaRecorder;
    private String recordAudioFilePath;
    private long recordStartTime = 0;
    private static final int HANDLE_UPDATE_VOICE_LEVEL = 0;
    private static final long RECORD_MIN_TIME = 1000;
    private static final long RECORD_MAX_TIME = 60 * 1000;
    private OnRecordControl onRecordControl;

    private boolean isShowRedIcon;

    public RecorderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.record_view, this);
    }


    public void setOnRecordControl(OnRecordControl onRecordControl) {
        this.onRecordControl = onRecordControl;
    }

    public void startRecode(String filePath) {
        setVisibility(View.VISIBLE);
        this.recordAudioFilePath = filePath;
        setShowAll(false);
        recording();
        showRecordAnimation(true);
    }

    public void setShowAll(boolean isShowAll) {
        if (isShowAll) {
            findViewById(R.id.bg).setVisibility(View.VISIBLE);
            findViewById(R.id.ivCancel).setVisibility(View.VISIBLE);
            findViewById(R.id.ll).setBackgroundResource(0);
            ((TextView) findViewById(R.id.tv)).setText(getContext().getString(R.string.chat_up_cancel_send));
        } else {
            findViewById(R.id.bg).setVisibility(View.GONE);
            findViewById(R.id.ivCancel).setVisibility(View.GONE);
            findViewById(R.id.ll).setBackgroundResource(R.drawable.bg_chat_record);
            ((TextView) findViewById(R.id.tv)).setText(getContext().getString(R.string.chat_slide_up_cancel_send));
            isShowRedIcon = false;
            ((ImageView) findViewById(R.id.ivCancel)).setImageResource(R.drawable.chat_record_not_cancel);
        }
    }

    public void showIcon(int y) {
        if (isShowRedIcon) {
            return;
        }
        ImageView ivCancel = findViewById(R.id.ivCancel);
        if (y < ivCancel.getBottom() + BarUtils.getStatusBarHeight()) {
            isShowRedIcon = true;
            ivCancel.setImageResource(R.drawable.chat_record_cancel);
        }
    }

    public void deleteRecord() {
        if (!TextUtils.isEmpty(recordAudioFilePath)) {
            new File(recordAudioFilePath).delete();
        }
    }

    private void showRecordAnimation(boolean isShow) {
        ImageView ivRecord = findViewById(R.id.ivRecordIcon);
        AnimationDrawable animationDrawable = (AnimationDrawable) ivRecord.getDrawable();
        if (isShow) {
            animationDrawable.start();
        } else {
            animationDrawable.stop();
        }
    }

//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case HANDLE_UPDATE_VOICE_LEVEL:
//                    if (System.currentTimeMillis() - recordStartTime >= RECORD_MAX_TIME) {
//                        stopRecord();
//                        if (onRecordControl != null)
//                            onRecordControl.recordTimeReachMax();
//                        return;
//                    }
////				updateVoieLevelFunc();
//                    mHandler.sendEmptyMessageDelayed(HANDLE_UPDATE_VOICE_LEVEL, 100);
//                    break;
//            }
//        }
//    };

//	private void updateVoieLevelFunc() {
//		int volume = getVolumeValue(mediaRecorder);
//		recordView.setVolume(volume);
//	}
//
//    private int getVolumeValue(MediaRecorder mediaRecorder) {
//        int result = 0;
//        if (null != mediaRecorder) {
//            try {
//                result = mediaRecorder.getMaxAmplitude() / 800 * 3;
//            } catch (Exception e) {
//                result = new Random().nextInt(8) + 1;
//            }
//            if (result > 100) {
//                result = new Random().nextInt(8) + 1;
//            } else if (result > 90) {
//                result = 8;
//            } else if (result > 60) {
//                result = 7;
//            } else if (result > 50) {
//                result = 6;
//            } else if (result > 40) {
//                result = 5;
//            } else if (result > 30) {
//                result = 4;
//            } else if (result > 20) {
//                result = 3;
//            } else if (result > 10) {
//                result = 2;
//            } else {
//                result = 1;
//            }
//        }
//        return result;
//    }

    public void stopRecord() {
        showRecordAnimation(false);
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
            }
        } catch (Exception ex) {

        }
        mediaRecorder = null;
        setVisibility(View.GONE);
    }

    private void recording() {
        try {
            mediaRecorder = new MediaRecorder();
            // 设置音频来源(一般为麦克风)
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置音频输出格式（默认的输出格式）
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            // mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // 设置音频编码方式（默认的编码方式）
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            // mediaRecorder.setAudioChannels(2);// 1 单声道 2 立体声
            // mediaRecorder.setAudioEncodingBitRate(160000); // 设置比特率
            // mediaRecorder.setAudioSamplingRate(9000); // 音频采样率。AAC audio
            // coding standard ranges from 8 to 96 kHz

            mediaRecorder.setOnErrorListener(this);

            mediaRecorder.setOutputFile(recordAudioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            recordStartTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                mediaRecorder.stop();
            } catch (Exception ex) {

            }
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            recordStartTime = 0;
        }
    }

    public String getRecordFile() {
        return recordAudioFilePath;
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }

    public interface OnRecordControl {
        public void recordTimeReachMax();
    }

}
