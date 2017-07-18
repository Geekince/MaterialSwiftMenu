package com.kince.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

import com.kince.widget.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by wind on 2016/12/5.
 */
public class BubbleView extends View {

    private static final String TAG = BubbleView.class.getSimpleName();

    private int[] colors = new int[]{0x88FF7000};
    private final int[] radiusRange = new int[]{10, 20};
    private final int[] velocityRange = new int[]{10, 20};
    private List<Bubble> bubbles = new ArrayList<>();
    private int duration = 1000;
    private int insetRadius = 100;

    Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    long mSpeed = 100;

    private boolean mIsRunning;

    private Point midpoint = new Point();

    private Random random = new Random();

    private Runnable mCreateBubble = new Runnable() {
        @Override
        public void run() {
            if (mIsRunning) {
                newBubble();
                postDelayed(mCreateBubble, mSpeed);
            }
        }
    };

    public BubbleView(Context context) {
        super(context);
    }

    public BubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Utils.dip2px(getContext(),64);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        midpoint.set(w / 2, h / 2);
    }

    Rect drawRect = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(drawRect);
        drawRect.inset(-insetRadius, -insetRadius);  //make the rect larger
        canvas.clipRect(drawRect, Region.Op.REPLACE);
        Iterator<Bubble> iterator = bubbles.iterator();
        while (iterator.hasNext()) {
            Bubble bubble = iterator.next();
            if (bubble.isAlive()) {
                mPaint.setColor(bubble.color);
                mPaint.setAlpha(bubble.getAlpha());
                Point p = bubble.getPoint();
                int dis = (int) Math.sqrt(Math.pow(Math.abs(midpoint.x - p.x), 2) + Math.pow(Math.abs(midpoint.y - p.y), 2));
                if (dis < drawRect.width() / 2 - bubble.radius) {
                    canvas.drawCircle(p.x, p.y, bubble.radius, mPaint);
                }
            } else {
                iterator.remove();
            }
        }
        if (bubbles.size() > 0) {
            postInvalidateDelayed(16, drawRect.left, drawRect.top, drawRect.right, drawRect.bottom);
        }
    }

    private void newBubble() {
        int color = colors[random.nextInt(colors.length)];
        int radius = random.nextInt(radiusRange[1] - radiusRange[0]) + radiusRange[0];
        int velocity = random.nextInt(velocityRange[1] - velocityRange[0]) + velocityRange[0];
        Bubble bubble = new Bubble(color, radius, velocity, random.nextDouble() * 2 * Math.PI, duration);
        bubbles.add(bubble);
        invalidate(drawRect);
    }

    public void setColors(int... colors) {
        this.colors = colors;
    }

    public void setRadiusRange(int min, int max) {
        radiusRange[0] = min;
        radiusRange[1] = max;
    }

    public void setVelocityRange(int min, int max) {
        velocityRange[0] = min;
        velocityRange[1] = max;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setSpeed(long mSpeed) {
        this.mSpeed = mSpeed;
    }

    public void setInsetRadius(int insetRadius) {
        this.insetRadius = insetRadius;
    }

    public void start() {
        if (!mIsRunning) {
            mIsRunning = true;
            mCreateBubble.run();
        }
    }

    public void stop() {
        mIsRunning = false;
        removeCallbacks(mCreateBubble);
    }

    public void stopImmediately() {
        mIsRunning = false;
        removeCallbacks(mCreateBubble);
        bubbles.clear();
        invalidate(drawRect);
    }

    private class Bubble {

        int color = 0x88FF7000;
        int radius = 20; //px
        int velocity = 10;  // px / 100 millisecond
        double angle = 0;
        int duration = 1000; //millisecond

        long createTime;

        int initRadius;

        public Bubble(int color, int radius, int velocity, double angle, int duration) {
            this.color = color;
            this.radius = radius;
            this.velocity = velocity;
            this.angle = angle;
            this.duration = duration;
            createTime = System.currentTimeMillis();
            initRadius = (int) Math.sqrt(midpoint.x * midpoint.x + midpoint.y * midpoint.y) - radius * 2;
        }

        public Point getPoint() {
            long distance = (System.currentTimeMillis() - createTime) * velocity / 100 + initRadius;
            int x = (int) (Math.cos(angle) * distance);
            int y = (int) (Math.sin(angle) * distance);
            return new Point(midpoint.x + x, midpoint.y + y);
        }

        public int getAlpha() {
            float percent = (float) (System.currentTimeMillis() - createTime) / duration;
            return (int) ((1 - percent) * 255);
        }

        boolean isAlive() {
            return System.currentTimeMillis() - createTime <= duration;
        }
    }

}
