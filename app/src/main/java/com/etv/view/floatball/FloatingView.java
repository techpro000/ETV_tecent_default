//package com.etv.view.floatball;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.util.AttributeSet;
//import android.view.View;
//
//public class FloatingView extends View {
//
//    public int height = 100;
//    public int width = 100;
//
//    public FloatingView(Context context) {
//        this(context, null);
//    }
//
//    public FloatingView(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public FloatingView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        initPaint();
//    }
//
//
//    int color_red = 0xffFF4081;
//    private Paint paint;
//    Paint textPaint;
//
//    private void initPaint() {
//        paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setColor(color_red);
//        paint.setStyle(Paint.Style.FILL);
//
//        textPaint = new Paint();
//        textPaint.setColor(0xffffffff);
//        textPaint.setTextSize(20);
//        textPaint.setStyle(Paint.Style.FILL);
//        textPaint.setTextAlign(Paint.Align.CENTER);
//    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        setMeasuredDimension(height, width);
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        drawCircle(canvas);
//        drawText(canvas);
//    }
//
//    private void drawText(Canvas canvas) {
//        Rect rect = new Rect(0, 0, height, height);//画一个矩形
//        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
//        float top = fontMetrics.top;
//        float bottom = fontMetrics.bottom;
//        int baseLineY = (int) (rect.centerY() - top / 2 - bottom / 2);
//        canvas.drawText("返回", rect.centerX(), baseLineY, textPaint);
//    }
//
//    private void drawCircle(Canvas canvas) {
//        canvas.drawCircle(width / 2, width / 2, (float) (width * 1.0 / 4), paint);
//    }
//
//}
