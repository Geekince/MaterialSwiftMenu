package com.kince.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Created by Kince183 on 2017/7/13.
 */

public class ProgressDrawable extends Drawable {

    private static final String TAG = "CircularProgressDrawable";

    private float mSweepAngle;
    private float mStartAngle;
    private int mSize;
    private int mStrokeWidth;
    private int mStrokeColor;
    private Context mContext;
    private RectF mMiddleRect;

    public ProgressDrawable(Context context, int size, int strokeWidth, int stokenWidthOut, int strokeColor) {
        this.mSize = size;
        this.mContext = context;
        this.mStrokeWidth = strokeWidth;
        this.mStrokeColor = strokeColor;
        this.mStartAngle = -90;
        this.mSweepAngle = 0;
        this.mPaint =  createPaint(mStrokeWidth);
    }

    public void setSweepAngle(float sweepAngle) {
        mSweepAngle = sweepAngle;
    }

    public int getSize() {
        return mSize;
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect bounds = getBounds();
        if (mPath == null) {
            mPath = new Path();
        }
        mPath.reset();
        mPath.addArc(getRect(mStrokeWidth), mStartAngle, mSweepAngle);
        mPath.offset(bounds.left, bounds.top);
        canvas.drawPath(mPath, mPaint);

        //canvas.drawArc(getRect(mStrokeWidth), -90, mSweepAngle, false,mPaint);
    }

    private RectF getRectInMiddle() {
        int size = getSize();
        mMiddleRect = new RectF(size/3, size/3, size - size/3, size - size/3);
        return mMiddleRect;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return 1;
    }

    private RectF mRectF;
    private Paint mPaint;
    private Path mPath;

    private RectF getRect(int stoken) {
        int size = getSize();
        int index = stoken / 2;
        mRectF = new RectF(index, index, size - index, size - index);
        return mRectF;
    }

    private Paint createPaint(int stokenWidth) {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.WHITE);
        }
        if(stokenWidth == 0){
            mPaint.setStyle(Paint.Style.FILL);
        }else{
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(stokenWidth);
        }
        return mPaint;
    }

}
