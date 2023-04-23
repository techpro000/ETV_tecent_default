package com.etv.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.etv.listener.TaskPlayStateListener;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.util.system.CpuModel;

public class AutoScrollTextView extends SurfaceView implements SurfaceHolder.Callback {
    private int mDefaultWidth = 0; // TextView默认宽度

    public static final String SPEED_SLIENT = "0";
    public static final String SPEED_SLOW = "1";
    public static final String SPEED_DEFAULT = "2";
    public static final String SPEED_FAST = "3";

    public static final int CAPTION_TEXT_STOP_SPEED = 0;
    public static final int CAPTION_TEXT_SLOW_SPEED = 2;
    public static final int CAPTION_TEXT_NROMAL_SPEED = 5;
    public static final int CAPTION_TEXT_FAST_SPEED = 8;

    private static final String TAG = AutoScrollTextView.class.getSimpleName();
    private SurfaceHolder mHolder;
    private Thread mScrollThread;
    private Paint mPaintTxt;
    private Canvas mCanvasTxt;
    private Typeface typeFontFace;   //字体

    // 这个值不能太大也不能太小。太大更新太慢，字幕滚动缓慢。太小跟新太快，机器性能更不上
    // 会造成播放视频和播放图片时滚动字幕速度有明显差别。
    private int mSpeed = CAPTION_TEXT_SLOW_SPEED;
    private float mTextLength = 0f;
    private float mSurfaceViewWidth = 0;
    private float mSurfaceViewHeight = 0;
    private boolean isStarting = true;
    float mTextFontSize = 20.0f;
    private int mTextHeight = 0;
    int textColor = Color.RED;
    CpListEntity cpListEntity;


    /***
     * 获取当前的View 状态
     * @return
     */
    public boolean getViewShowStatues() {
        return isStarting;
    }

    /**
     * 内容滚动位置起始坐标
     */
    private float mScrollX = 0;

    /**
     * 移动方向
     */
    private int mScrollorientation = TextInfo.MOVE_LEFT;

    private String mScrollText = "";
    int REFRESH_TIME = 30;

    public AutoScrollTextView(Context context) {
        super(context);
        init();
    }

    public AutoScrollTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public AutoScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        setOnClickListener(clickListener);
    }

    OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener == null) {
                return;
            }
            listener.clickTaskView(cpListEntity, null, 0);
        }
    };

    private void init() {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mDisplayMetrics);
        this.mDefaultWidth = mDisplayMetrics.widthPixels;
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSPARENT); // 顶层绘制SurfaceView设成透明
        this.setZOrderOnTop(true);
        mPaintTxt = new Paint();
    }

    /***
     * 设置字体显示的文字
     * @param text
     */
    public void setTextVules(String text) {
        mSurfaceViewWidth = getWidth();
        if (mSurfaceViewWidth <= 0) {
            mSurfaceViewWidth = mDefaultWidth;
        }
        mScrollText = text;
        mTextLength = mPaintTxt.measureText(text.toString());
    }

    /**
     * 设置跑马灯相关的属性
     *
     * @param textSize 字体大小
     * @param taColor  跑马灯的颜色
     * @param speed、   跑马灯的速度
     * @param tvMov    运动的方向
     */

    public void setTextSizeColor(int textSize, String taColor, int speed, String tvMov, String taFont) {
        this.mTextFontSize = (float) (textSize);
        Log.e(TAG, "setTextSizeColor: " + mTextFontSize);
        this.textColor = TaskDealUtil.getColorFromInToSystem(taColor);
        this.mSpeed = speed;
        if (tvMov.contains("0")) {  //0：左进右出
            mScrollorientation = TextInfo.MOVE_RIGHT;
        } else if (tvMov.contains("1")) {  //   1：右进左出
            mScrollorientation = TextInfo.MOVE_LEFT;
        }
        taFont = taFont.trim();
        typeFontFace = TaskDealUtil.getFontTypeFace(taFont);
        resetPaint();
    }

    public void resetPaint() {
        mPaintTxt.reset();
        mPaintTxt.setAntiAlias(true);  // 锯齿
        mPaintTxt.setTypeface(Typeface.SANS_SERIF);    // 字体
        MyLog.cdl("==ViewInsertTextManager==绘制字体大小===" + mTextFontSize);
        mPaintTxt.setTextSize(mTextFontSize);   // 字体大小
        mPaintTxt.setColor(textColor);    // 字体颜色
        mTextLength = mPaintTxt.measureText(mScrollText.toString());
        Rect rect = new Rect();
        mPaintTxt.getTextBounds("test", 0, 1, rect);// 为了提高计算速度，自定义一个字符串来计算，由于是纯英文(占一个字符串)，只截取前1个字符即可
        mTextHeight = rect.height();
    }

    TaskPlayStateListener listener;

    public void setClickCpEntity(TaskPlayStateListener listener, CpListEntity cpListEntity) {
        this.listener = listener;
        this.cpListEntity = cpListEntity;
    }

    /**
     * 暂停播放
     */
    public void pauseDisplayView() {
        isStarting = false;
    }

    /**
     * 恢复播放
     */
    public void resumePlayView() {
        if (isStarting) {
            return;
        }
        isStarting = true;
        startThread();
    }

    /**
     * 停止滚动
     */
    public void stopScroll() {
        isStarting = false;
    }

    public void onDestroyScrool() {
        stopScroll();
        if (mCanvasTxt != null) {
            mCanvasTxt.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
    }


    public void startScroll() {
        if (!isStarting) {
            startThread();
        }
    }

    /* 自定义线程 */
    class AutoScrollRunnable implements Runnable {

        public void run() {
            mSurfaceViewWidth = getWidth();
            while (isStarting) {
                try {
                    synchronized (mHolder) {
                        draw();
                    }
                    if (mSpeed == CAPTION_TEXT_STOP_SPEED) {
                        isStarting = false;
                    }
                    Thread.sleep(REFRESH_TIME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void draw() {
        try {
            // 锁定画布
            mCanvasTxt = mHolder.lockCanvas();
            if (mHolder == null || mCanvasTxt == null) {
                return;
            }
            // 清屏操作或者直接绘制背景
            mCanvasTxt.drawColor(Color.TRANSPARENT, Mode.CLEAR);
            if (typeFontFace != null) {
                mPaintTxt.setTypeface(typeFontFace);
            }
            mCanvasTxt.drawText(mScrollText, mScrollX, (mSurfaceViewHeight - mTextHeight) / 2 + mTextHeight, mPaintTxt);
            // 解锁显示
            mHolder.unlockCanvasAndPost(mCanvasTxt);
            // 方向
            if (mScrollorientation == TextInfo.MOVE_LEFT) {
                // 向左
                if (mScrollX < -mTextLength) {
                    mScrollX = mSurfaceViewWidth;
                } else {
                    mScrollX -= mSpeed;
                }
            } else if (mScrollorientation == TextInfo.MOVE_RIGHT) {
                // 向右
                if (mScrollX >= mSurfaceViewWidth) {
                    mScrollX = -mTextLength;
                } else {
                    mScrollX += mSpeed;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 当控件创建时自动执行的方法
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        MyLog.cdl("=========surfaceCreated 初始化完成====");
        startThread();
    }

    private void startThread() {
        mScrollThread = null;
        mScrollThread = new Thread(new AutoScrollRunnable());
        mScrollThread.start();
        isStarting = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceViewHeight = height;
        mSurfaceViewWidth = width;
    }

    /**
     * 当控件销毁时自动执行的方法
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 终止自定义线程
        isStarting = false;
        mScrollThread.interrupt();
    }

    public void setScrollOrientation(int orientation) {
        mScrollorientation = orientation;
    }


}