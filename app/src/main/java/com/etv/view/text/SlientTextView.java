package com.etv.view.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;

import com.etv.task.entity.TextInfo;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;


/**
 * 静态汉字的绘制方法
 * 调用方法
 * SlientTextView seek_light = (SlientTextView) findViewById(R.id.seek_light);
 * seek_light.setdrawRect(0xff031d8e);
 * seek_light.setTextColorSize(0xffffffff, 150);
 * seek_light.setText("哈哈哈哈");
 */
public class SlientTextView extends AppCompatTextView {
    public SlientTextView(Context context) {
        this(context, null);
    }

    public SlientTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlientTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    Paint textPaint;

    private void initPaint() {
        textPaint = new Paint();
    }

    /**
     * 设置背景颜色
     *
     * @param bggColor
     * @return
     */
    public SlientTextView setdrawRect(int bggColor) {
        this.bggColor = bggColor;
        return this;
    }

    int bggColor;
    int textColor;
    int textSize;
    String textShow;
    int showPosition;


    /**
     * 设置字体颜色大小
     *
     * @param textColor
     * @param textSize
     * @return
     */
    public SlientTextView setTextColorSize(int textColor, int textSize) {
        this.textColor = textColor;
        this.textSize = textSize;
        return this;
    }

    public SlientTextView setGraintPosition(String position) {
        showPosition = TaskDealUtil.getShowPosition(position);
        return this;
    }


    public void setText(String textShow) {
        this.textShow = textShow;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPaintTextBgg(canvas);
    }

    private void drawPaintTextBgg(Canvas canvas) {
        int leftRect = 0;
        int topRect = 0;
        int rightRect = getWidth();
        int bottomRect = getHeight();
        Rect rect = new Rect(leftRect, topRect, rightRect, bottomRect);
        Paint rectPaint = new Paint();
        rectPaint.setColor(bggColor);
        rectPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rect, rectPaint);
        drawTextShow(canvas, rect);
    }

    /**
     * 绘制文本
     *
     * @param canvas
     * @param rect
     */
    private void drawTextShow(Canvas canvas, Rect rect) {
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.LEFT);
//        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
//        float top = fontMetrics.top;  //为基线到字体上边框的距离,即上图中的top
//        float bottom = fontMetrics.bottom;   //为基线到字体下边框的距离,即上图中的bottom
//        int baseLineY = (int) (rect.centerY() - top / 2 - bottom / 2);//基线中间点的y轴计算公式
//        canvas.drawText(textShow, rect.centerX(), baseLineY, textPaint);\
        float textWidth = textPaint.measureText(textShow);
        canvas.drawText(textShow, 0, textSize / 2, textPaint);
    }

    public static int getShowPosition(String taAlignment) {
        MyLog.cdl("=====显示得位置===" + taAlignment);
        int backPosition = Gravity.CENTER;
        taAlignment = taAlignment.trim();
        if (taAlignment == null || taAlignment.length() < 1) {
            return backPosition;
        }
        try {
            int defaultPoi = Integer.parseInt(taAlignment);
            switch (defaultPoi) {
                case TextInfo.POI_LEFT_TOP:
                    backPosition = Gravity.LEFT | Gravity.TOP;
                    break;
                case TextInfo.POI_TOP_CENTER:
                    backPosition = Gravity.CENTER | Gravity.TOP;
                    break;
                case TextInfo.POI_RIGHT_TOP:
                    backPosition = Gravity.RIGHT | Gravity.TOP;
                    break;
                case TextInfo.POI_LEFT_CENTER:
                    backPosition = Gravity.LEFT | Gravity.CENTER;
                    break;
                case TextInfo.POI_CENTER:
                    backPosition = Gravity.CENTER;
                    break;
                case TextInfo.POI_RIGHT_CENTER:
                    backPosition = Gravity.RIGHT | Gravity.CENTER;
                    break;
                case TextInfo.POI_LEFT_BOTTOM:
                    backPosition = Gravity.LEFT | Gravity.BOTTOM;
                    break;
                case TextInfo.POI_BOTTOM_CENTER:
                    backPosition = Gravity.BOTTOM | Gravity.CENTER;
                    break;
                case TextInfo.POI_RIGHT_BOTTOM:
                    backPosition = Gravity.RIGHT | Gravity.BOTTOM;
                    break;
                default:
                    backPosition = Gravity.CENTER;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return backPosition;
    }

}
