package com.etv.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
//import android.support.v7.widget.AppCompatTextView;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * 从下到上的运动字幕
 */
public class TextViewScroll extends AppCompatTextView {
    private int x, y;
    // 滚动速度
    private int speed = 5;
    // 字幕从那边出来
    public static final int FROM_RIGHT = 0;
    public static final int FROM_LEFT = 1;
    public static final int FROM_TOP = 2;
    public static final int FROM_BOTTOM = 3;
    private int scrollType = FROM_RIGHT;
    public static final boolean START = true;
    public static final boolean STOP = false;
    private boolean scrollStatus = START;


    public TextViewScroll(Context context) {
        super(context);
    }

    public TextViewScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
        x = getTextWidth();
        y = getTextHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //scrollType(scrollType);
    }

    private void scrollType(int type) {
        if (scrollStatus) {
            switch (type) {
                case FROM_RIGHT:
                    // 右到左
                    if (x >= getTextWidth()) {
                        x = -getWidth();
                    }
                    scrollXY(x, 0);
                    x = x + speed;
                    break;
                case FROM_LEFT:
                    // 左到右
                    if (x <= -getWidth()) {
                        x = getTextWidth();
                    }
                    scrollXY(x, 0);
                    x = x - speed;
                    break;
                case FROM_TOP:
                    // 上到下
                    if (y <= -getHeight()) {
                        y = getTextHeight();
                    }
                    scrollXY(0, y);
                    y = y - speed;
                    break;
                case FROM_BOTTOM:
                    // 下到上
                    if (y >= getTextHeight()) {
                        y = -getHeight();
                    }
                    scrollXY(0, y);
                    y = y + speed;
                    break;

                default:
                    break;
            }
        }
    }

    Thread thread;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start();
    }

    private void start() {
        thread = new Thread(() -> {
            while (isAttachedToWindow()) {
                scrollType(scrollType);
                SystemClock.sleep(50);
            }
        });
        thread.start();
    }

    private void scrollXY(int x, int y) {
        post(() -> scrollTo(x, y));
    }

    // 获取字体行宽度
    private int getTextWidth() {
        int mTextWidth;
        Paint mPaint = getPaint();
        if (getLineCount() > 1) {
            // 如果有多行文字，则获取最长的一行文字宽度
            String[] lineContent = getText().toString().split("\n");
            int maxLine = 0, maxLineNumber = 0;
            for (int i = 0; i < lineContent.length; i++) {
                if (lineContent[i].length() > maxLine) {
                    maxLine = lineContent[i].length();
                    maxLineNumber = i;
                }
            }
            mTextWidth = (int) mPaint.measureText(lineContent[maxLineNumber]);
        } else {
            mTextWidth = (int) mPaint.measureText(getText().toString());
        }
        return mTextWidth;
    }

    // 获取字体总高度
    private int getTextHeight() {
        return getLineHeight() * getLineCount();
    }

    public int getScrollType() {
        return scrollType;
    }

    public void setScrollType(int scrollType) {
        this.scrollType = scrollType;
        if (scrollType == FROM_BOTTOM) {
            y = -getHeight();
        }
        setScrollStatus(START);
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed + 3;
    }

    public boolean isScrollStatus() {
        return scrollStatus;
    }

    public void setScrollStatus(boolean scrollStatus) {
        this.scrollStatus = scrollStatus;
        postInvalidate();
    }
}
