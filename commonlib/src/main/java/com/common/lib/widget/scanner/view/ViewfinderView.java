package com.common.lib.widget.scanner.view;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.common.lib.R;
import com.common.lib.utils.DensityUtil;
import com.common.lib.widget.scanner.camera.CameraManager;
import com.google.zxing.ResultPoint;

import java.util.ArrayList;
import java.util.List;

public final class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192,
            128, 64};
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final long ANIMATION_DELAY = 80L;
    private CameraManager cameraManager;
    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private int scannerAlpha;
    private final List<ResultPoint> possibleResultPoints;

    private int i = 0;
    private final Rect mRect;
    private final GradientDrawable mDrawable;
    private final Drawable lineDrawable;
    private Paint textPaint = null;
    private Paint net_work_textPaint = null;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Align.CENTER);
        Resources resources = getResources();
        textPaint.setColor(resources.getColor(R.color.white));
        textPaint.setTextSize(DensityUtil.dip2px(context, 15));


        net_work_textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        net_work_textPaint.setTextAlign(Align.CENTER);
        net_work_textPaint.setColor(resources.getColor(R.color.white));
        net_work_textPaint.setTextSize(DensityUtil.dip2px(context, 18));


        maskColor = resources.getColor(R.color.zxing_viewfinder_mask);
        resultColor = resources.getColor(R.color.zxing_result_view);

        mRect = new Rect();
        int left = getResources().getColor(R.color.zxing_lightgreen);
        int center = getResources().getColor(R.color.zxing_green);
        int right = getResources().getColor(R.color.zxing_lightgreen);
        lineDrawable = getResources().getDrawable(R.drawable.zx_code_line);
        mDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{left,
                left, center, right, right});

        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<ResultPoint>(5);
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return;
        }

        Rect frame = cameraManager.getFramingRect();
        if (frame == null) {
            return;
        }

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
        canvas.drawRect(0, frame.bottom, width, height, paint);
        canvas.drawText(getContext().getString(R.string.zxing_top_hint), (frame.left + frame.right) / 2, frame.bottom + DensityUtil.dip2px(getContext(), 23), textPaint);
        if (drawNetWork) {
            canvas.drawText(getContext().getString(R.string.network_excption), (frame.left + frame.right) / 2, (frame.top + frame.bottom) / 2 - DensityUtil.dip2px(getContext(), 20), net_work_textPaint);
            canvas.drawText(getContext().getString(R.string.network_excption), (frame.left + frame.right) / 2, (frame.top + frame.bottom) / 2 + DensityUtil.dip2px(getContext(), 20), net_work_textPaint);
        } else {
            // �����ĸ��
            paint.setColor(getResources().getColor(R.color.zxing_green));
            // ���Ͻ�
            canvas.drawRect(frame.left, frame.top, frame.left + 15,
                    frame.top + 5, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + 5,
                    frame.top + 15, paint);
            // ���Ͻ�
            canvas.drawRect(frame.right - 15, frame.top, frame.right,
                    frame.top + 5, paint);
            canvas.drawRect(frame.right - 5, frame.top, frame.right,
                    frame.top + 15, paint);
            // ���½�
            canvas.drawRect(frame.left, frame.bottom - 5, frame.left + 15,
                    frame.bottom, paint);
            canvas.drawRect(frame.left, frame.bottom - 15, frame.left + 5,
                    frame.bottom, paint);
            // ���½�
            canvas.drawRect(frame.right - 15, frame.bottom - 5, frame.right,
                    frame.bottom, paint);
            canvas.drawRect(frame.right - 5, frame.bottom - 15, frame.right,
                    frame.bottom, paint);

            // ��ɨ����л���ģ��ɨ�������
            // ����ɨ��������ɫΪ��ɫ
            paint.setColor(getResources().getColor(R.color.zxing_green));
            // ������ɫ�����͸��ֵ
            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            // ͸��ȱ仯
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;

            // ����̶����в�������
            // int middle = frame.height() / 2 + frame.top;
            // canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1,
            // middle + 2, paint);

            // ��ɨ�����޸�Ϊ�����ߵ���
            if ((i += 5) < frame.bottom - frame.top) {
                /* ����Ϊ�ý���������Ϊɨ���� */
                // ����ͼΪ����
                // mDrawable.setShape(GradientDrawable.RECTANGLE);
                // ����ͼΪ����
                // mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                // ���;��ε��ĸ�Բ�ǰ뾶
                // mDrawable
                // .setCornerRadii(new float[] { 8, 8, 8, 8, 8, 8, 8, 8 });
                // λ�ñ߽�
                // mRect.set(frame.left + 10, frame.top + i, frame.right - 10,
                // frame.top + 1 + i);
                // ���ý���ͼ���߽�
                // mDrawable.setBounds(mRect);
                // ���������
                // mDrawable.draw(canvas);

                /* ����ΪͼƬ��Ϊɨ���� */
                mRect.set(frame.left - 6, frame.top + i - 6, frame.right + 6,
                        frame.top + 6 + i);
                lineDrawable.setBounds(mRect);
                lineDrawable.draw(canvas);

                // ˢ��
                invalidate();
            } else {
                i = 0;
            }


            // �ظ�ִ��ɨ����������(���ĸ�Ǽ�ɨ����)
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
                    frame.right, frame.bottom);
        }
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    private boolean drawNetWork = false;

    public void drawResultBitmap(boolean drawNetWork) {
        this.drawNetWork = drawNetWork;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

    public void recycleLineDrawable() {
        if (mDrawable != null) {
            mDrawable.setCallback(null);
        }
        if (lineDrawable != null) {
            lineDrawable.setCallback(null);
        }
    }
}
