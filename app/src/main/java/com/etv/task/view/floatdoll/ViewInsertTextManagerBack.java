package com.etv.task.view.floatdoll;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.view.AutoScrollTextView;
import com.ys.etv.R;

import java.util.List;

public class ViewInsertTextManagerBack {

    WindowManager windowManager;
    Context context;
    private WindowManager.LayoutParams floatBallParams;
    int screenWidth = 1920;
    int screenHeight = 1080;

    CpListEntity cpListEntity;

    public ViewInsertTextManagerBack(Context context) {
        this.context = context;
        screenWidth = SharedPerUtil.getScreenWidth();
        screenHeight = SharedPerUtil.getScreenHeight();
    }

    public void setCpListEntity(CpListEntity cpEntity) {
        this.cpListEntity = cpEntity;
        int topPosition = Integer.parseInt(cpListEntity.getCoRightPosition());
        int height = Integer.parseInt(cpListEntity.getCoHeight());
        initFloatBall(topPosition, height);
    }

    View viewFloat;

    private void initFloatBall(int top, int height) {
        if (windowManager != null) {
            windowManager = null;
        }
        if (floatBallParams != null) {
            floatBallParams = null;
        }
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        floatBallParams = new WindowManager.LayoutParams();
        floatBallParams.width = screenWidth;
        floatBallParams.height = height;
        floatBallParams.gravity = getLayoutPosition(top);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Android 8.0
            floatBallParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            //其他版本
            floatBallParams.type = WindowManager.LayoutParams.TYPE_PHONE;  //TYPE_TOAST
        }
        floatBallParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        floatBallParams.format = PixelFormat.RGBA_8888;
        viewFloat = LayoutInflater.from(context).inflate(R.layout.view_float_insert, null);
        rela_mat_bgg = (RelativeLayout) viewFloat.findViewById(R.id.rela_mat_bgg);
        autoScrollTextView = (AutoScrollTextView) viewFloat.findViewById(R.id.mMarqueeView);
        windowManager.addView(viewFloat, floatBallParams);
    }

    RelativeLayout rela_mat_bgg;
    AutoScrollTextView autoScrollTextView;

    public boolean getViewShowStatues() {
        if (windowManager == null) {
            return false;
        }
        if (viewFloat == null) {
            return false;
        }
        if (autoScrollTextView == null) {
            return false;
        }
        return autoScrollTextView.getViewShowStatues();
    }

    public void showFloatView() {
        if (windowManager == null) {
            return;
        }
        parperToShowView(cpListEntity);
    }

    public void parperToShowView(CpListEntity cpEntity) {
        try {
            List<TextInfo> txtList = cpEntity.getTxList();
            if (txtList == null || txtList.size() < 1) {
                onDestoryBall();
                return;
            }
            TextInfo textInfo = txtList.get(0);
            startToDrawText(textInfo);
        } catch (Exception e) {
            MyLog.ExceptionPrint("播放界面布局异常: " + e.toString());
            e.printStackTrace();
        }
    }

    int taFontSize = 25;
    String content;

    public void startToDrawText(TextInfo textInfo) {
        try {
            if (textInfo == null) {
                onDestoryBall();
                return;
            }
            String textColor = "#ffffff";
            int speend = AutoScrollTextView.CAPTION_TEXT_SLOW_SPEED;
            String taMove = "1";  //右进左出
            String taFont = "default";
            String backBackgroundColor = textInfo.getTaBgColor();  //背景色
            if (backBackgroundColor != null && backBackgroundColor.length() > 1) {
                int bggColor = TaskDealUtil.getColorFromInToSystem(backBackgroundColor);
                rela_mat_bgg.setBackgroundColor(bggColor);
            }
            textColor = textInfo.getTaColor();
            taMove = textInfo.getTaMove();  //运动轨迹
            content = textInfo.getTaContent();
            String speedNum = textInfo.getTaMoveSpeed();
            if (speedNum.contains(AutoScrollTextView.SPEED_SLIENT)) {
                speend = AutoScrollTextView.CAPTION_TEXT_STOP_SPEED;
            } else if (speedNum.contains(AutoScrollTextView.SPEED_SLOW)) {
                speend = AutoScrollTextView.CAPTION_TEXT_SLOW_SPEED;
            } else if (speedNum.contains(AutoScrollTextView.SPEED_DEFAULT)) {
                speend = AutoScrollTextView.CAPTION_TEXT_NROMAL_SPEED;
            } else if (speedNum.contains(AutoScrollTextView.SPEED_FAST)) {
                speend = AutoScrollTextView.CAPTION_TEXT_FAST_SPEED;
            } else {
                speend = AutoScrollTextView.CAPTION_TEXT_SLOW_SPEED;
            }
            //获取字体大小
            String testSize = textInfo.getTaFontSize();
            if (testSize != null || testSize.length() > 0) {
                taFontSize = Integer.parseInt(testSize);
            } else {
                taFontSize = 25;
            }
            taFont = textInfo.getTaFonType();
            MyLog.playTask("==========字幕的属性222===" + speend + " / " + taFontSize + " / " + taMove);
            autoScrollTextView.setTextVules(content);
            taFontSize = TaskDealUtil.px2sp(context, taFontSize);
            autoScrollTextView.setTextSizeColor(taFontSize, textColor, speend, taMove, taFont);
        } catch (Exception e) {
            MyLog.playTask("==========字幕的属性error===" + e.toString());
            e.printStackTrace();
        }
    }

    public void onDestoryBall() {
        try {
            if (windowManager == null) {
                return;
            }
            if (viewFloat != null) {
                windowManager.removeView(viewFloat);
            }
            windowManager = null;
            if (autoScrollTextView != null) {
                autoScrollTextView.stopScroll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getLayoutPosition(int top) {
        MyLog.cdl("============字幕的位置高度坐标===" + top);
        int gravity = Gravity.BOTTOM;
        if (top < (screenHeight / 3)) {
            gravity = Gravity.TOP | Gravity.LEFT;
        } else if (top > (screenHeight / 3) && top < ((screenHeight * 2) / 3)) {
            gravity = Gravity.CENTER | Gravity.LEFT;
        } else if (top > ((screenHeight * 2) / 3)) {
            gravity = Gravity.BOTTOM | Gravity.LEFT;
        }
        return gravity;
    }

}
