package com.common.lib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.cardview.widget.CardView;

import com.common.lib.R;

public class RatioCardView extends CardView {

    protected float mRatio;

    public RatioCardView(Context context) {
        super(context);
    }

    public RatioCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mRatio = 1.0f;
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CardViewRatio);
            mRatio = array.getFloat(R.styleable.CardViewRatio_cardView_ratio, 1.0f);
            array.recycle();
        }
    }


    public void setRatio(float ratio) {
        mRatio = ratio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        int childWidthSize = getMeasuredWidth();
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (childWidthSize * mRatio + 0.5f), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
