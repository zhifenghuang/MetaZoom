package com.common.lib.manager;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;

public class MediaplayerManager {

    private static final String TAG = "MediaplayerManager";
    private static MediaplayerManager mMediaplayerManager;

    private MediaPlayer mPlayer;
    private SoundPool mSoundPool;
    private int mSoundId;


    private MediaplayerManager() {

    }


    public static MediaplayerManager getInstance() {
        if (mMediaplayerManager == null) {
            synchronized (TAG) {
                if (mMediaplayerManager == null) {
                    mMediaplayerManager = new MediaplayerManager();
                }
            }
        }
        return mMediaplayerManager;
    }

    public void playVoice(File file) {
        if (file.exists()) {
            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
            }
            mPlayer.reset();
            try {
                mPlayer.setDataSource(ConfigurationManager.Companion.getInstance().getContext(), Uri.fromFile(file));
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.prepare();
                mPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void pauseVoice() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    public void resumeVoice() {
        if (mPlayer != null) {
            mPlayer.start();
        }
    }

    public void playVoice(Context context, int rawId, boolean isLoop) {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        mPlayer.reset();
        try {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(rawId);
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setLooping(isLoop);
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadSound(Context context, int rawId) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(16)
                .setAudioAttributes(audioAttributes)
                .build();
        mSoundId = mSoundPool.load(context.getApplicationContext(), rawId, 1);
    }

    public void playSound(Context context, int rawId) {
        if (mSoundPool == null) {
            loadSound(context, rawId);
        }
        mSoundPool.play(mSoundId, 1.0f, 1.0f, 16, 1, 1.0f);
    }


    public void playVideo(File file, SurfaceView surfaceView) {
        if (file.exists()) {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();

            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        if (mPlayer == null) {
                            mPlayer = new MediaPlayer();
                        }
                        mPlayer.reset();
                        mPlayer.setDataSource(ConfigurationManager.Companion.getInstance().getContext(), Uri.fromFile(file));
                        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                        mPlayer.setDisplay(holder);
                        mPlayer.setLooping(true);
                        mPlayer.prepare();
                        mPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            });
        }
    }

    public void releaseMediaPlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
        mPlayer = null;
    }

    public void stopSoundPool() {
        if (mSoundPool != null) {
            mSoundPool.pause(mSoundId);
        }
    }

    public void releaseSoundPool() {
        if (mSoundPool != null) {
            mSoundPool.autoPause();
            mSoundPool.unload(mSoundId);
            mSoundId = -1;
            mSoundPool.release();
            mSoundPool = null;
        }
    }
}
