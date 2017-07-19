package com.kince.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;

import com.kince.widget.listenter.ComboClickListener;
import com.kince.widget.listenter.ProgressListener;
import com.kince.widget.listenter.StateChangeListener;
import com.kince.widget.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kince183 on 2017/7/13.
 */
public class MaterialSwiftMenu extends FrameLayout {

    private static final double POSITIVE_QUADRANT = 150;
    private static final double NEGATIVE_QUADRANT = -150;
    private static final double ANGLE_FOR_ONE_SUB_MENU = 0;
    private static final int ANIMATION_TIME = 300;
    private static final int ARC_MENU_NUM = 1;

    public static final int SEND_TYPE_STATIC = 1;
    public static final int SEND_TYPE_DYNAMIC = 2;
    public static final int SEND_TYPE_FACE = 3;

    // Main Button
    private ProgressButton mMainMenuButton;
    // BubbleView
    private BubbleView mBubbleView;
    // position
    private MenuSideEnum mMenuSideEnum;
    // sub menu changed listener
    private StateChangeListener mStateChangeListener;
    // sub menu animation time
    private long mAnimationTime;
    // current radius for sub menu
    private float mCurrentRadius;
    // sub menu radius for final status
    private float mFinalRadius;
    // main menu elevation
    private float mElevation;
    // margin for main button
    private int mMainMenuMargin;
    // sub menu is opened
    private boolean isOpened = false;
    // current quadrant angle
    private double mQuadrantAngle;
    //Represents the center points of the circle whose arc we are considering
    private int cx, cy;
    private int arcX, arcY;
    private Paint mCirclePaint;

    private List<Button> mMenuButtons = new ArrayList<>();
    private int mClickPosition;
    private ComboClickListener mComboClickListener;
    private int mTotalClickCount;

    public MaterialSwiftMenu(Context context) {
        super(context);
    }

    public MaterialSwiftMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.MainMenu, 0, 0);
        init(attr);
    }

    private void init(TypedArray attr) {
        Resources resources = getResources();
        mFinalRadius = attr.getDimension(R.styleable.MainMenu_menu_radius,
                resources.getDimension(R.dimen.default_final_radius));
        mElevation = attr.getDimension(R.styleable.MainMenu_menu_elevation,
                resources.getDimension(R.dimen.default_elevation));
        mMenuSideEnum = MenuSideEnum.fromId(attr.getInt(R.styleable.MainMenu_menu_open,
                ARC_MENU_NUM));
        mAnimationTime = attr.getInteger(R.styleable.MainMenu_menu_animation_time,
                ANIMATION_TIME);
        mMainMenuMargin = attr.getDimensionPixelSize(R.styleable.MainMenu_menu_margin,
                resources.getDimensionPixelSize(R.dimen.fab_margin));
        mCurrentRadius = 0;

        switch (mMenuSideEnum) {
            case ARC_BOTTOM_LEFT:
                mQuadrantAngle = POSITIVE_QUADRANT;
                break;
            case ARC_BOTTOM_RIGHT:
                mQuadrantAngle = NEGATIVE_QUADRANT;
                break;
            case ARC_TOP_LEFT:
                mQuadrantAngle = NEGATIVE_QUADRANT;
                break;
            case ARC_TOP_RIGHT:
                mQuadrantAngle = POSITIVE_QUADRANT;
                break;
        }
        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(getResources().getColor(R.color.colorAccent));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addSubMenus();
        // The bubble view is added as the first child of the view.
        addBubbleView();
        // The main menu is added as the last child of the view.
        addMainMenu();
        toggleVisibilityOfAllChildViews(isOpened);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChild(mMainMenuButton, widthMeasureSpec, heightMeasureSpec);
        measureChild(mBubbleView, widthMeasureSpec, heightMeasureSpec);

        int width = mMainMenuButton.getMeasuredWidth();
        int height = mMainMenuButton.getMeasuredHeight();

        boolean accommodateRadius = false;
        int maxWidth = 0, maxHeight = 0;

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child == mMainMenuButton || child == mBubbleView || child.getVisibility() == GONE)
                continue;
            else {
                accommodateRadius = true;
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                //maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
                //maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
            }
        }

        if (accommodateRadius) {
            int radius = Math.round(mCurrentRadius);
            width += (radius + maxWidth);
            height += (radius + maxHeight);
        }

        width += mMainMenuMargin;
        height += mMainMenuMargin;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutMenu();
        if (!isInEditMode())
            layoutChildrenArc();
    }

    /**
     * add main menu
     */
    private void addMainMenu() {
        mMainMenuButton = new ProgressButton(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMainMenuButton.setElevation(mElevation);
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                Utils.dip2px(getContext(), 60),
                Utils.dip2px(getContext(), 32)
        );
        mMainMenuButton.setLayoutParams(params);
        mMainMenuButton.setGravity(Gravity.CENTER);
        mMainMenuButton.setTextColor(getResources().getColor(android.R.color.white));
        mMainMenuButton.setTextSize(16);
        mMainMenuButton.setOnClickListener(mMainMenuClickListener);
        mMainMenuButton.setProgressListener(new ProgressListener() {

            @Override
            public void progressStart() {
                mBubbleView.start();
                toggleMenu();
            }

            @Override
            public void progressStatus(int progress) {

            }

            @Override
            public void progressEnd() {
                mTotalClickCount = 0;
                if (isOpened) {
                    toggleMenu();
                }
                mBubbleView.stop();
                mComboClickListener.onMenuClosed();
            }
        });
        addView(mMainMenuButton);
    }

    /**
     * add bubble view for background
     */
    private void addBubbleView() {
        mBubbleView = new BubbleView(getContext());
        float density = getResources().getDisplayMetrics().density;
        mBubbleView.setColors(0xFFFF4E00, 0xFFF6A623, 0xFF50E3C2, 0xFFB8E986);
        mBubbleView.setRadiusRange((int) density * 5, (int) (density * 10));
        mBubbleView.setVelocityRange(10, 20);
        mBubbleView.setDuration(1000);
        mBubbleView.setSpeed(100);
        mBubbleView.setInsetRadius((int) (density * 50));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                Utils.dip2px(getContext(), 80),
                Utils.dip2px(getContext(), 80)
        );
        addView(mBubbleView, params);
    }

    private void addSubMenus() {
        for (int i = 0; i < 4; i++) {
            Button button = generateSubMenu(i);
            addView(button);
        }
    }

    /**
     * Lays out the main ProgressButton on the screen.
     * Currently, the library only supports laying out the menu on the bottom right or bottom left of the screen.
     * The proper layout position is directly dependent on the which side the radial arch menu will be show.
     */
    private void layoutMenu() {
        switch (mMenuSideEnum) {
            case ARC_BOTTOM_LEFT:
                cx = getMeasuredWidth() - mMainMenuButton.getMeasuredWidth() - mMainMenuMargin;
                cy = getMeasuredHeight() - mMainMenuButton.getMeasuredHeight() - mMainMenuMargin;

                arcX = getMeasuredWidth() - mMainMenuButton.getMeasuredWidth() * 4 / 5 - mMainMenuMargin;
                arcY = getMeasuredHeight() - mMainMenuButton.getMeasuredHeight() * 4 / 5 - mMainMenuMargin;
                break;
            case ARC_BOTTOM_RIGHT:
                cx = mMainMenuMargin;
                cy = getMeasuredHeight() - mMainMenuButton.getMeasuredHeight() - mMainMenuMargin;

                arcX = mMainMenuButton.getMeasuredWidth() * 2 / 3 - mMainMenuMargin;
                arcY = getMeasuredHeight() - mMainMenuButton.getMeasuredHeight() * 4 / 5 - mMainMenuMargin;
                break;
            case ARC_TOP_LEFT:
                cx = getMeasuredWidth() - mMainMenuButton.getMeasuredWidth() - mMainMenuMargin;
                cy = mMainMenuMargin;
                break;
            case ARC_TOP_RIGHT:
                cx = mMainMenuMargin;
                cy = mMainMenuMargin;
                break;
        }
        mMainMenuButton.layout(cx, cy, cx + mMainMenuButton.getMeasuredWidth(), cy + mMainMenuButton.getMeasuredHeight());
        mBubbleView.layout(cx, cy, cx + mBubbleView.getMeasuredWidth(), cy + mBubbleView.getMeasuredHeight());
    }

    private void layoutChildrenArc() {
        int childCount = getChildCount();
        double eachAngle = getEachArcAngleInDegrees();
        int leftPoint, topPoint, left, top;

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child == mMainMenuButton || child == mBubbleView || child.getVisibility() == GONE)
                continue;
            else {
                double totalAngleForChild = eachAngle * (i);
                leftPoint = (int) (mCurrentRadius * Math.cos(Math.toRadians(totalAngleForChild)));
                topPoint = (int) (mCurrentRadius * Math.sin(Math.toRadians(totalAngleForChild)));

                switch (mMenuSideEnum) {
                    case ARC_BOTTOM_LEFT:
                        left = arcX - leftPoint;
                        top = arcY - topPoint;
                        if (i == 0) {
                            top += 30;
                        }
                        if (i == 1) {
                            top += 20;
                            left -= 10;
                        }
                        if (i == 2) {
                            top += 10;
                            left -= 10;
                        }
                        if (i == 3) {

                        }
                        break;
                    case ARC_BOTTOM_RIGHT:
                        left = arcX + leftPoint;
                        top = arcY + topPoint;
                        if (i == 0) {
                            top += 30;
                            left -= 10;
                        }
                        if (i == 1) {
                            top += 20;
                        }
                        if (i == 2) {
                            top += 10;
                        }
                        if (i == 3) {

                        }
                        break;
                    case ARC_TOP_LEFT:
                        left = cx - leftPoint;
                        top = cy - topPoint;
                        break;
                    case ARC_TOP_RIGHT:
                        left = cx + leftPoint;
                        top = cy + topPoint;
                        break;
                    default:
                        left = cx + leftPoint;
                        top = cy + topPoint;
                        break;
                }
                child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
            }
        }
    }

    /**
     * The number of menu items is the number of menu options added by the user.
     * This is 1 less than the total number of child views, because we manually add one view to the viewgroup which acts as the main menu.
     *
     * @return
     */
    private int getSubMenuCount() {
        return getChildCount() - 1;
    }

    /**
     * If there is only onle sub-menu, then it wil be placed at 45 degress.
     * For the rest, we use 90/(n-1), where n is the number of sub-menus;
     *
     * @return
     */
    private double getEachArcAngleInDegrees() {
        if (getSubMenuCount() == 1)
            return ANGLE_FOR_ONE_SUB_MENU;
        else
            return mQuadrantAngle / ((double) getSubMenuCount() - 1);
    }

    private void toggleVisibilityOfAllChildViews(boolean show) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == mMainMenuButton || child == mBubbleView)
                continue;
            if (show)
                child.setVisibility(VISIBLE);
            else
                child.setVisibility(GONE);
        }
    }

    private void beginOpenAnimation() {
        ValueAnimator openMenuAnimator = ValueAnimator.ofFloat(0, mFinalRadius);
        openMenuAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentRadius = (float) animation.getAnimatedValue();
                requestLayout();
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateInterpolator());

        List<Animator> animationCollection = new ArrayList<>(getSubMenuCount() + 1);
        animationCollection.add(openMenuAnimator);

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == mMainMenuButton)
                continue;

            animationCollection.add(ObjectAnimator.ofFloat(view, "scaleX", 0, 1));
            animationCollection.add(ObjectAnimator.ofFloat(view, "scaleY", 0, 1));
            animationCollection.add(ObjectAnimator.ofFloat(view, "alpha", 0, 1));
        }

        animatorSet.playTogether(animationCollection);
        animatorSet.setDuration(mAnimationTime);
        animatorSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                toggleVisibilityOfAllChildViews(isOpened);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mStateChangeListener != null)
                    mStateChangeListener.onMenuOpened();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animatorSet.start();
    }

    private void beginCloseAnimation() {
        ValueAnimator closeMenuAnimator = ValueAnimator.ofFloat(mFinalRadius, 0);
        closeMenuAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentRadius = (float) animation.getAnimatedValue();
                requestLayout();
            }
        });

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateInterpolator());
        List<Animator> animationCollection = new ArrayList<>(getSubMenuCount() + 1);
        animationCollection.add(closeMenuAnimator);

        AnimatorSet rotateAnimatorSet = new AnimatorSet();
        rotateAnimatorSet.setInterpolator(new AccelerateInterpolator());
        List<Animator> rotateAnimationCollection = new ArrayList<>(getSubMenuCount());

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == mMainMenuButton)
                continue;

            animationCollection.add(ObjectAnimator.ofFloat(view, "scaleX", 1, 0));
            animationCollection.add(ObjectAnimator.ofFloat(view, "scaleY", 1, 0));
            animationCollection.add(ObjectAnimator.ofFloat(view, "alpha", 1, 0));

            rotateAnimationCollection.add(ObjectAnimator.ofFloat(view, "rotation", 0, 360));
        }

        animatorSet.playTogether(animationCollection);
        animatorSet.setDuration(mAnimationTime);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                toggleVisibilityOfAllChildViews(isOpened);
                if (mStateChangeListener != null)
                    mStateChangeListener.onMenuClosed();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        rotateAnimatorSet.playTogether(rotateAnimationCollection);
        rotateAnimatorSet.setDuration(mAnimationTime / 3);
        rotateAnimatorSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animatorSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        rotateAnimatorSet.start();
    }

    private void scaleUpSubMenu(final View view) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.35f, 1.0f);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.35f, 1.0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY);
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(250);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        set.start();
    }

    private OnClickListener mMainMenuClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mComboClickListener != null && !mComboClickListener.isOpenMenu()) {
                return;
            }
            if (mMainMenuButton.getState() == ProgressButton.STATE.PROGRESS) {
                mComboClickListener.onComboClick();
                if (!isOpened) {
                    toggleMenu();
                }
            } else if (mMainMenuButton.getState() == ProgressButton.STATE.NORMAL) {
                mComboClickListener.onSingleClick();
            }
            updateTotalClickCount(1);
            mMainMenuButton.onButtonClick();
        }
    };

    private OnClickListener subMenuClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mComboClickListener != null) {
                scaleUpSubMenu(v);
                mClickPosition = (int) v.getTag();
                updateTotalClickCount(getSpinnerGiftNum());
                mComboClickListener.onMenuClick(getSpinnerGiftNum());
                mMainMenuButton.scaleTextSize(16);
                toggleMenu();
            }
        }

    };

    private void updateTotalClickCount(int num) {
        mTotalClickCount += num;
        String finalText = "x" + String.valueOf(mTotalClickCount);
        mMainMenuButton.setText(finalText);
    }

    /**
     * Toggles the state of the ArcMenu, i.e. closes it if it is open and opens it if it is closed
     */
    public void toggleMenu() {
        isOpened = !isOpened;
        if (isOpened)
            beginOpenAnimation();
        else
            beginCloseAnimation();
    }

    /**
     * Get the state of the ArcMenu, i.e. whether it is open or closed
     *
     * @return true if the menu is open
     */
    public boolean isMenuOpened() {
        return isOpened;
    }

    /**
     * Controls the animation time to transition the menu from close to open state and vice versa.
     * The time is represented in milli-seconds
     *
     * @param animationTime
     */
    public void setAnimationTime(long animationTime) {
        mAnimationTime = animationTime;
    }

    /**
     * Allows you to listen to the state changes of the Menu, i.e.
     * {@link StateChangeListener#onMenuOpened()} and {@link StateChangeListener#onMenuClosed()} events
     *
     * @param stateChangeListener
     */
    public void setStateChangeListener(StateChangeListener stateChangeListener) {
        this.mStateChangeListener = stateChangeListener;
    }

    /**
     * Sets the display radius of the ArcMenu
     */
    public void setRadius(float radius) {
        this.mFinalRadius = radius;
        invalidate();
    }

    public int getSpinnerGiftNum() {
        String tx = mMenuButtons.get(mClickPosition).getText().toString();
        if (!TextUtils.isEmpty(tx)) {
            return Integer.parseInt(tx);
        }
        return 1;
    }

    public Button generateSubMenu(final int position) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                Utils.dip2px(getContext(), 36),
                Utils.dip2px(getContext(), 36)
        );
        final Button button = new Button(getContext());
        button.setBackground(getResources().getDrawable(R.drawable.sub_menu));
        button.setClickable(true);
        button.setTextSize(13);
        button.setTag(position);
        button.setLayoutParams(params);
        button.setGravity(Gravity.CENTER);
        button.setTextColor(Color.WHITE);
        button.setText("");
        button.setOnClickListener(subMenuClickListener);
        mMenuButtons.add(button);
        return button;
    }

    public void setComboClickListener(ComboClickListener comboClickListener) {
        this.mComboClickListener = comboClickListener;
    }

    /**
     * 更新菜单文本
     */
    public void updateCountView(int mode) {
        for (int i = 0; i < mMenuButtons.size(); i++) {
            if (mode == SEND_TYPE_STATIC) {
                if (i == 0) {
                    mMenuButtons.get(i).setText("999");
                } else if (i == 1) {
                    mMenuButtons.get(i).setText("155");
                } else if (i == 2) {
                    mMenuButtons.get(i).setText("55");
                } else if (i == 3) {
                    mMenuButtons.get(i).setText("9");
                }
            } else if (mode == SEND_TYPE_DYNAMIC || mode == SEND_TYPE_FACE) {
                if (i == 0) {
                    mMenuButtons.get(i).setText("49");
                } else if (i == 1) {
                    mMenuButtons.get(i).setText("15");
                } else if (i == 2) {
                    mMenuButtons.get(i).setText("9");
                } else if (i == 3) {
                    mMenuButtons.get(i).setText("5");
                }
            }
        }
    }

    public void resetMenuState(int type) {
        beginCloseAnimation();
        mMainMenuButton.setState(ProgressButton.STATE.NORMAL, true);
        updateCountView(type);
    }

}
