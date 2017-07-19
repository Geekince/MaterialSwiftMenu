package com.kince.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.LinearInterpolator;

import com.kince.widget.util.Utils;

/**
 * Created by Kince183 on 2017/7/14.
 */
public class RotateDrawable extends Drawable {

    private float sweepDegrees = 360f;

    private Bitmap mRotateBitmap;
    private int mDrawableSize;
    private float currentDegrees;
    private Paint drawBitmapPaint;

    public RotateDrawable(Bitmap bitmap, int size) {
        this.mDrawableSize = size;
        this.mRotateBitmap = Utils.scaleBitmap(bitmap, size, size);

        this.drawBitmapPaint = new Paint();
        this.drawBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.drawBitmapPaint.setStyle(Paint.Style.FILL);

        startRotateAnimation();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.rotate(currentDegrees, mDrawableSize / 2, mDrawableSize / 2);
        canvas.drawBitmap(mRotateBitmap, 0, 0, drawBitmapPaint);
        canvas.restore();
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int i) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public void startRotateAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, sweepDegrees);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentDegrees = (float) animation.getAnimatedValue();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.start();
    }

}
