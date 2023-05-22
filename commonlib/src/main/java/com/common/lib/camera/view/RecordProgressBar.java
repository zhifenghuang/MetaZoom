package com.common.lib.camera.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.common.lib.camera.record.CameraUtils;

/**
 * Created by gigabud on 15-12-4.
 */
@SuppressLint("AppCompatCustomView")
public class RecordProgressBar extends View {

    private Paint mPaint;
    private int mPaintWidth;
    private float mProgress;

    public RecordProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * @param current
     * @param total
     */
    public void resetProgress(long current, long total) {
        mProgress = current * 100f / total;
        if (mProgress >= 100) {
            mProgress = 100;
        }
        postInvalidate();
    }

    /**
     * 设置画笔颜色
     *
     * @param color
     */
    private void resetPaintColor(int color) {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaintWidth = CameraUtils.dip2px(getContext(), 5);
            mPaint.setStrokeWidth(mPaintWidth);
            mPaint.setStyle(Paint.Style.STROKE);
        }
        mPaint.setColor(color);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPaint == null) {
            return;
        }

    }
}
