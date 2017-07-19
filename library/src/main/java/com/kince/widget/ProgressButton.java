package com.kince.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.kince.widget.listenter.ProgressListener;
import com.kince.widget.util.Utils;

/**
 * Created by Kince183 on 2017/7/13.
 */
public class ProgressButton extends AppCompatButton {

    private static final String TAG = "CProgressButton";
    private final Bitmap windMillItemBitmap;
    // 按钮背景
    private Drawable mBackground;
    // 圆环进度条
    private ProgressDrawable mProgressDrawable;
    private RotateDrawable mRotateDrawable;
    private int mWidth;
    private int mHeight;
    // 状态
    private STATE mState = STATE.NORMAL;
    private boolean morphingCircle; //变形成圆中
    private boolean morphingNormal; //变形成正常状态中
    private float mFromCornerRadius;
    private float mToCornerRadius;
    private int mOriginWidth;
    private int mOriginHeight;
    private int mToWidth;
    private int mToHeight;
    private long mDuration = 500;
    private int mProgress;
    private int mMaxProgress = 100;
    private int mStrokeColor;
    private int mStokeWidth = 0;
    private int mStokeWidthOut = 0;
    private static String[] statusString = new String[]{"发送", "+1", ""};
    private ProgressListener mProgressListener;

    public enum STATE {
        PROGRESS, NORMAL
    }

    public ProgressButton(Context context) {
        this(context, null);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ProgressButton,
                0, 0);
        try {
            mStrokeColor = a.getInteger(R.styleable.ProgressButton_stroke_color, -1);
            mBackground = a.getDrawable(R.styleable.ProgressButton_bg_drawable);
            mStokeWidthOut = (int) a.getDimension(R.styleable.ProgressButton_stroke_width, -1);
            mFromCornerRadius = (int) a.getDimension(R.styleable.ProgressButton_from_corner_radius, -1);
            mToCornerRadius = (int) a.getDimension(R.styleable.ProgressButton_to_corner_radius, -1);
            mToWidth = (int) a.getDimension(R.styleable.ProgressButton_to_width, -1);
            mToHeight = (int) a.getDimension(R.styleable.ProgressButton_to_height, -1);
        } finally {
            a.recycle();
        }

        if (mStrokeColor == -1) {
            mStrokeColor = getResources().getColor(R.color.colorPrimary);
        }

        if (mBackground == null) {
            mBackground = getResources().getDrawable(R.drawable.bounder);
        }

        if (mStokeWidthOut == -1) {
            mStokeWidthOut = Utils.dip2px(getContext(), 1);
        }

        if (mFromCornerRadius == -1) {
            mFromCornerRadius = getResources().getDimensionPixelOffset(R.dimen.default_from_radius);
        }

        if (mToCornerRadius == -1) {
            mToCornerRadius = getResources().getDimensionPixelOffset(R.dimen.default_from_radius);
        }

        if (mToWidth == -1) {
            mToWidth = getResources().getDimensionPixelOffset(R.dimen.default_to_width);
        }

        if (mToHeight == -1) {
            mToHeight = getResources().getDimensionPixelOffset(R.dimen.default_to_height);
        }

        mOriginWidth = getResources().getDimensionPixelOffset(R.dimen.default_from_width);
        mOriginHeight = getResources().getDimensionPixelOffset(R.dimen.default_from_height);

        windMillItemBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_rotate);

        mStokeWidth = mStokeWidthOut * 3;
        setText(statusString[0]);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        mHeight = getHeight() - getPaddingTop() - getPaddingRight();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w - getPaddingLeft() - getPaddingRight();
        mHeight = h - getPaddingTop() - getPaddingRight();
        if (mState == STATE.NORMAL || (mState == STATE.PROGRESS && morphingCircle)) {
            setBound(0);
        } else {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int end = (mWidth - mHeight) / 2;
        if (mState == STATE.NORMAL || (mState == STATE.PROGRESS && morphingCircle)) {
            mBackground.draw(canvas);
        } else if (mState == STATE.PROGRESS && !morphingCircle) {
            if (mProgressDrawable == null || mRotateDrawable == null) {
                int offset = (mWidth - mHeight) / 2 + getPaddingLeft();
                int size = mHeight;
                mProgressDrawable = new ProgressDrawable(getContext(), size, mStokeWidth, mStokeWidthOut, mStrokeColor);
                int left = offset;
                mProgressDrawable.setBounds(left, getPaddingTop(), left + mHeight, getPaddingTop() + mHeight);

                mRotateDrawable = new RotateDrawable(windMillItemBitmap, size);
            }
            setBound(end);

            mBackground.draw(canvas);

            mRotateDrawable.draw(canvas);

            float sweepAngle = (360f / mMaxProgress) * mProgress;
            mProgressDrawable.setSweepAngle(sweepAngle);
            mProgressDrawable.draw(canvas);
        }
        super.onDraw(canvas);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mProgress = mProgress;
        savedState.morphingNormal = morphingNormal;
        savedState.morphingCircle = morphingCircle;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mProgress = savedState.mProgress;
            morphingNormal = savedState.morphingNormal;
            morphingCircle = savedState.morphingCircle;
            super.onRestoreInstanceState(savedState.getSuperState());
            setProgress(mProgress);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    /**
     * order by yourself
     *
     * @param status
     */
    public static void initStatusString(String[] status) {
        if (status != null && status.length > 0) {
            statusString = status;
        }
    }

    public STATE getState() {
        return mState;
    }

    public void setState(STATE state, boolean anim) {
        if (getWidth() == 0 || morphingCircle || morphingNormal)
            return;
        this.mState = state;
        if (anim) {
            if (mState == STATE.PROGRESS) {
                morph2Circle();
            } else if (mState == STATE.NORMAL) {
                morph2Normal();
            }
        } else {
            morphingCircle = morphingNormal = false;
            if (mState == STATE.PROGRESS) {
                setText("");
            } else if (mState == STATE.NORMAL) {
                setText("");
            }
            setBound(0);
        }
    }

    private void setBound(int padding) {
        if (mWidth == 0) {
            mWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        }
        if (mHeight == 0) {
            mHeight = getHeight() - getPaddingTop() - getPaddingRight();
        }
        mBackground.setBounds(getPaddingLeft() + padding, getPaddingTop(), getPaddingLeft() + mWidth - padding, getPaddingTop() + mHeight);
        invalidate();
    }

    private void setProgress(int progress) {
        mProgress = progress;
        mProgressListener.progressStatus(progress);
        if (morphingCircle || morphingNormal)
            return;
        if (mState != STATE.PROGRESS) {
            mState = STATE.PROGRESS;
            setText(statusString[1]);
        }
        if (mProgress >= mMaxProgress) {
            mProgress = mMaxProgress;
        }
        if (mProgress <= 0) {
            mProgress = 0;
        }
        setBound(0);
        invalidate();
    }

    private void morph2Normal() {
        ObjectAnimator cornerAnimation =
                ObjectAnimator.ofFloat(mBackground, "cornerRadius", mToCornerRadius, mFromCornerRadius);

        ValueAnimator heightAnimation = ValueAnimator.ofInt(mToHeight, mOriginHeight);
        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.height = val;
                setLayoutParams(layoutParams);
            }
        });

        ValueAnimator widthAnimation = ValueAnimator.ofInt(mToWidth, mOriginWidth);
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.width = val;
                setLayoutParams(layoutParams);
            }
        });

        final int start = (mWidth - mHeight) / 2;
        ValueAnimator animator = ValueAnimator.ofInt(28, 0);
        animator.setDuration(mDuration)
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        setBound(value);
                    }
                });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(mDuration);
        animatorSet.playTogether(animator, cornerAnimation, heightAnimation, widthAnimation);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mProgressListener.progressEnd();
                morphingNormal = true;
                setText(statusString[2]);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                morphingNormal = false;
                setText(statusString[0]);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                morphingNormal = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    private void morph2Circle() {
        ObjectAnimator cornerAnimation =
                ObjectAnimator.ofFloat(mBackground, "cornerRadius", mFromCornerRadius, mToCornerRadius);

        ValueAnimator heightAnimation = ValueAnimator.ofInt(mHeight, mToHeight);
        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.height = val;
                setLayoutParams(layoutParams);
            }
        });

        ValueAnimator widthAnimation = ValueAnimator.ofInt(mWidth, mToWidth);
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.width = val;
                setLayoutParams(layoutParams);
            }
        });

        final int end = (mWidth - mHeight) / 2;
        ValueAnimator animator = ValueAnimator.ofInt(0, end);
        animator.setDuration(mDuration)
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        setBound(value);
                    }
                });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(mDuration);
        animatorSet.playTogether(animator, cornerAnimation, heightAnimation, widthAnimation);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mProgressListener.progressStart();
                setText(statusString[2]);
                morphingCircle = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setText(statusString[1]);
                morphingCircle = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                morphingCircle = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    static class SavedState extends BaseSavedState {

        private boolean morphingNormal;
        private boolean morphingCircle;
        private int mProgress;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mProgress = in.readInt();
            morphingCircle = in.readInt() == 1;
            morphingNormal = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mProgress);
            out.writeInt(morphingNormal ? 1 : 0);
            out.writeInt(morphingCircle ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);

    public void onButtonClick() {
        if (getState() == STATE.NORMAL) {
            setState(STATE.PROGRESS, true);
            valueAnimator.setDuration(5000);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    setProgress(value + 1);
                    if (value == 100) {
                        setState(STATE.NORMAL, true);
                    }
                }
            });
            valueAnimator.start();
        } else if (getState() == STATE.PROGRESS) {
            valueAnimator.cancel();
            valueAnimator.start();
            scaleTextSize(16);
        }
    }

    public void setProgressListener(ProgressListener mProgressListener) {
        this.mProgressListener = mProgressListener;
    }

    public void scaleTextSize(float textSize) {
        ValueAnimator animator = ValueAnimator.ofFloat(textSize, textSize * 2, textSize);
        animator.setDuration(250);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                setTextSize(animatedValue);
            }
        });
        animator.start();
    }

}
