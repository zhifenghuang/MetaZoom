package com.common.lib.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.common.lib.activity.BaseActivity

@SuppressLint("AppCompatCustomView")
class ShowPicView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {

    var MIN_SCALE_SIZE = 0.3f
    private var mOriginPoints: FloatArray? = null
    private var mPoints: FloatArray? = null
    private var mOriginContentRect: RectF? = null
    private var mContentRect: RectF? = null
    private var mViewRect: RectF? = null
    private var mDownTime: Long = 0

    private var mLastPointX = 0f
    private var mLastPointY = 0f

    private var mCutLeft = 0
    private var mCutTop = 0
    private var mCutRight = 0
    private var mCutBottom = 0

    private var mBitmap: Bitmap? = null
    private var mMatrix: Matrix? = null

    private var mScaleSize = 1.0f

    private var mFirstScaleSize = 1.0f


    private var mIsDownInStricker = false
    private var mIsCutBmp = false

    private var mCurrentLenght = 0f

    private var mClickNum = 0


    /**
     * 模式 NONE：无 DRAG：拖拽. ZOOM:缩放
     */
    private enum class MODE {
        NONE, DRAG, ZOOM
    }

    private var mode = MODE.NONE // 默认模式

    fun setImageBitmap(bitmap: Bitmap?, isCutBmp: Boolean) {
        mIsCutBmp = isCutBmp
        if (isCutBmp) {
            val dis: DisplayMetrics = (context as BaseActivity<*>).getDisplayMetrics()!!
            mCutLeft = 0
            mCutTop = dis.heightPixels / 10
            mCutRight = dis.widthPixels
            mCutBottom = dis.heightPixels / 10 + dis.widthPixels
        }
        setImageBitmap(bitmap)
        MIN_SCALE_SIZE = mScaleSize
    }

    override fun setImageDrawable(drawable: Drawable?) {
        if (drawable == null) {
            super.setImageDrawable(drawable)
        } else {
            if (drawable is GifDrawable) {
                setImageBitmap(drawable.firstFrame)
            } else if (drawable is BitmapDrawable) {
                setImageBitmap(drawable.bitmap)
            } else {
                super.setImageDrawable(drawable)
            }
        }
    }

    override fun setImageBitmap(bitmap: Bitmap?) {
        mBitmap = bitmap
        if (mBitmap == null) {
            invalidate()
            return
        }
        try {
            val px = mBitmap!!.width.toFloat()
            val py = mBitmap!!.height.toFloat()
            mPoints = FloatArray(10)
            if (width == 0 || height == 0) {
                val dis: DisplayMetrics = (context as BaseActivity<*>).getDisplayMetrics()!!
                if (mIsCutBmp) {
                    mScaleSize = Math.max(dis.widthPixels / px, dis.widthPixels / py)
                    mFirstScaleSize = mScaleSize
                    mPoints!![8] = dis.widthPixels * 0.5f
                    mPoints!![9] = dis.heightPixels * 0.1f + dis.widthPixels * 0.5f
                } else {
                    mScaleSize = Math.min(dis.widthPixels / px, dis.heightPixels / py)
                    mPoints!![8] = dis.widthPixels * 0.5f
                    mPoints!![9] = dis.heightPixels * 0.5f
                }
            } else {
                if (mIsCutBmp) {
                    mScaleSize = Math.max(width / px, width / py)
                    mFirstScaleSize = mScaleSize
                    mPoints!![8] = width * 0.5f
                    mPoints!![9] = height * 0.1f + width * 0.5f
                } else {
                    mScaleSize = Math.min(width / px, height / py)
                    mPoints!![8] = width * 0.5f
                    mPoints!![9] = height * 0.5f
                }
            }
            mOriginPoints = floatArrayOf(0f, 0f, px, 0f, px, py, 0f, py, px / 2, py / 2)
            mOriginContentRect = RectF(0f, 0f, px, py)
            mContentRect = RectF()
            mMatrix = Matrix()
            val dy = mPoints!![9] - py / 2
            mMatrix!!.postTranslate(mPoints!![8] - px / 2, dy)
            mMatrix!!.postScale(mScaleSize, mScaleSize, mPoints!![8], mPoints!![9])
            val rectF = RectF()
            mMatrix!!.mapRect(rectF, mOriginContentRect)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        invalidate()
    }


    override fun setFocusable(focusable: Boolean) {
        super.setFocusable(focusable)
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mBitmap == null || mMatrix == null || mContentRect == null) {
            return
        }
        mMatrix!!.mapPoints(mPoints, mOriginPoints)
        mMatrix!!.mapRect(mContentRect, mOriginContentRect)
        canvas.drawBitmap(mBitmap!!, mMatrix!!, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (!mIsCutBmp) {
//            return super.dispatchTouchEvent(event);
//        }
        if (mContentRect == null || visibility != View.VISIBLE) {
            return super.onTouchEvent(event)
        }
        if (event.action == MotionEvent.ACTION_DOWN && mContentRect != null) {
            mIsDownInStricker = mContentRect!!.contains(event.x, event.y)
        }
        if (!mIsDownInStricker) {
            return super.onTouchEvent(event)
        }
        if (mViewRect == null) {
            mViewRect = RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
        }
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mDownTime = System.currentTimeMillis()
                if (mContentRect!!.contains(event.x, event.y)) {
                    mLastPointX = event.x
                    mLastPointY = event.y
                    mode = MODE.DRAG
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> if (event.pointerCount == 2) {
                mClickNum = 0
                mode = MODE.ZOOM
                mCurrentLenght =
                    calculateLength(event.x, event.y, event.getX(1), event.getY(1))
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mode == MODE.DRAG) {
                    if (event.action == MotionEvent.ACTION_UP
                        && System.currentTimeMillis() - mDownTime < 300
                    ) {
                        if (++mClickNum == 1) {
                            postDelayed({
                                if (mClickNum > 1) {
                                    val scale: Float
                                    if (mScaleSize > 1.05f) {
                                        scale = 1.0f / mScaleSize
                                        mScaleSize = 1.0f
                                    } else {
                                        scale = 1.5f / mScaleSize
                                        mScaleSize = 1.5f
                                    }
                                    mMatrix!!.postScale(
                                        scale,
                                        scale,
                                        mPoints!![8],
                                        mPoints!![9]
                                    )
                                    mMatrix!!.postTranslate(
                                        width * 0.5f - mPoints!![8],
                                        height * 0.5f - mPoints!![9]
                                    )
                                    invalidate()
                                } else {
                                    performClick()
                                }
                                mClickNum = 0
                            }, 300)
                        }
                        return true
                    }
                    var cX = event.x - mLastPointX
                    var cY: Float = event.y - mLastPointY
                    if (mContentRect!!.left + cX > 0 || mContentRect!!.right + cX < width) {
                        cX = 0f
                    }
                    if (mContentRect!!.top + cY > 0 || mContentRect!!.bottom + cY < height) {
                        cY = 0f
                    }
                    mMatrix!!.postTranslate(cX, cY)
                    postInvalidate()
                }
                if (mScaleSize < mFirstScaleSize) {
                    mMatrix!!.postScale(
                        mFirstScaleSize / mScaleSize,
                        mFirstScaleSize / mScaleSize,
                        mPoints!![8],
                        mPoints!![9]
                    )
                    //    mMatrix.postTranslate(getWidth() * 0.5f - mPoints[8], getHeight() * 0.5f - mPoints[9]);
                    mScaleSize = mFirstScaleSize
                    invalidate()
                }
                mLastPointX = 0f
                mLastPointY = 0f
                mode = MODE.NONE
            }
            MotionEvent.ACTION_POINTER_UP -> mode = MODE.NONE
            MotionEvent.ACTION_MOVE -> if (mode == MODE.ZOOM) {
                if (event.pointerCount == 2) {
                    val touchLenght =
                        calculateLength(event.x, event.y, event.getX(1), event.getY(1))
                    val scale = touchLenght / mCurrentLenght
                    val nowsc = mScaleSize * scale
                    if (nowsc >= MIN_SCALE_SIZE) {
                        mMatrix!!.postScale(scale, scale, mPoints!![8], mPoints!![9])
                        mScaleSize = nowsc
                    }
                    invalidate()
                    mCurrentLenght = touchLenght
                }
            } else if (mode == MODE.DRAG) { //拖动的操作
                var cX = event.x - mLastPointX
                var cY: Float = event.y - mLastPointY
                if (mIsCutBmp) {
                    if (mContentRect!!.left + cX > mCutLeft || mContentRect!!.right + cX < mCutRight) {
                        cX = 0f
                    }
                    if (mContentRect!!.top + cY > mCutTop || mContentRect!!.bottom + cY < mCutBottom) {
                        cY = 0f
                    }
                } else {
                    if (mContentRect!!.left + cX > 0 || mContentRect!!.right + cX < width) {
                        cX = 0f
                    }
                    if (mContentRect!!.top + cY > 0 || mContentRect!!.bottom + cY < height) {
                        cY = 0f
                    }
                }
                mMatrix!!.postTranslate(cX, cY)
                postInvalidate()
                mLastPointX = event.x
                mLastPointY = event.y
            }
        }
        return true
    }


    private fun calculateLength(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ): Float {
        val ex = x1 - x2
        val ey = y1 - y2
        return Math.sqrt(ex * ex + ey * ey.toDouble()).toFloat()
    }
}