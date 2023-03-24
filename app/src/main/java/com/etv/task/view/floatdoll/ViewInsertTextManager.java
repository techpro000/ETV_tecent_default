package com.etv.task.view.floatdoll;

import android.content.Context;
import android.widget.RelativeLayout;

import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.util.ViewSizeChange;
import com.etv.view.AutoScrollTextView;

import java.util.List;

public class ViewInsertTextManager {

    Context context;

    public ViewInsertTextManager(Context context) {
        this.context = context;
    }

    AutoScrollTextView autoScrollTextView;
    CpListEntity cpListEntity;
    RelativeLayout rela_mat_bgg;

    public void setCpListEntity(CpListEntity cpEntity, AutoScrollTextView autoScrollTextView, RelativeLayout rela_mat_bgg) {
        if (cpEntity == null) {
            return;
        }
        this.cpListEntity = cpEntity;
        this.autoScrollTextView = autoScrollTextView;
        this.rela_mat_bgg = rela_mat_bgg;
        int topPosition = Integer.parseInt(cpListEntity.getCoRightPosition());  //2顶端 5居中 8底部对齐
        int height = Integer.parseInt(cpListEntity.getCoHeight());
        MyLog.cdl("=======ViewInsertTextManager=======" + topPosition + " / " + height);
        ViewSizeChange.setTextInsertViewPosition(rela_mat_bgg, topPosition, height);
    }

    public void showFloatView() {
        try {
            List<TextInfo> txtList = cpListEntity.getTxList();
            if (txtList == null || txtList.size() < 1) {
                onDestoryBall();
                return;
            }
            TextInfo textInfo = txtList.get(0);
            startToDrawText(textInfo);
        } catch (Exception e) {
            MyLog.cdl("播放界面布局异常: " + e.toString());
            e.printStackTrace();
        }
    }

    int taFontSize = 25;
    String content;

    public void startToDrawText(TextInfo textInfo) {
        MyLog.cdl("=======ViewInsertTextManager=======开始绘制");
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
            MyLog.cdl("=======ViewInsertTextManager======字幕的属性222===" + speend + " / " + taFontSize + " / " + taMove);
            autoScrollTextView.setTextVules(content);
            taFontSize = TaskDealUtil.px2sp(context, taFontSize);
            autoScrollTextView.setTextSizeColor(taFontSize, textColor, speend, taMove, taFont);
            MyLog.cdl("=======ViewInsertTextManager======字幕的属性333===");
        } catch (Exception e) {
            MyLog.cdl("=======ViewInsertTextManager========字幕的属性error===" + e.toString());
            e.printStackTrace();
        }
    }

    public void onDestoryBall() {
        if (autoScrollTextView != null) {
            autoScrollTextView.onDestroyScrool();
        }
    }

}
