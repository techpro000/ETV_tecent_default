package com.etv.view.layout.text;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.etv.config.AppInfo;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.view.AutoScrollTextView;
import com.etv.view.layout.Generator;
import com.ys.etv.R;

import java.util.List;

/**
 * 跑马灯位置
 */
public class ViewMatQueentestGenerte extends Generator {

    View view;
    AutoScrollTextView autoScrollTextView;
    int screenViewHeight = 18;
    int taFontSize = 25;
    String content;
    CpListEntity cpListEntity;
    Context context;

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    @Override
    public void updateTextInfo(Object object) {

    }

    public ViewMatQueentestGenerte(Context context, CpListEntity cpListEntity, int x, int y, int width, int height, String text) {
        super(context, x, y, width, height);
        this.screenViewHeight = height;
        this.context = context;
        this.content = text;
        this.cpListEntity = cpListEntity;
        taFontSize = screenViewHeight * 4 / 5;
        view = LayoutInflater.from(context).inflate(R.layout.view_marqueentest, null);
        initView(view);
    }

    @Override
    public View getView() {
        return view;
    }

    RelativeLayout rela_mat_bgg;

    private void initView(View view) {
        rela_mat_bgg = (RelativeLayout) view.findViewById(R.id.rela_mat_bgg);
        autoScrollTextView = (AutoScrollTextView) view.findViewById(R.id.mMarqueeView);
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        try {
            TextInfo textInfo = (TextInfo) object;
            String textColor = "#ffffff";
            int speend = AutoScrollTextView.CAPTION_TEXT_SLOW_SPEED;
            String taMove = "1";  //右进左出
            String taFont = AppInfo.TEXT_FONT_DEFAULT;
            if (textInfo != null) {
                String backBackgroundColor = textInfo.getTaBgColor();  //背景色
                if (backBackgroundColor != null && backBackgroundColor.length() > 1) {
                    int bggColor = TaskDealUtil.getColorFromInToSystem(backBackgroundColor);
                    rela_mat_bgg.setBackgroundColor(bggColor);
                }
                textColor = textInfo.getTaColor();
                taMove = textInfo.getTaMove();  //运动轨迹
                //运动速度====
                String speedNum = textInfo.getTaMoveSpeed();
//                MyLog.playTask("==========字幕的属性speedNum===" + speedNum + " / " + backBackgroundColor + " / " + bggColor);
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
                taFont = textInfo.getTaFonType();
                String testSize = textInfo.getTaFontSize();
                if (testSize != null || testSize.length() > 0) {
                    taFontSize = Integer.parseInt(testSize);
                } else {
                    taFontSize = 25;
                }
            }            MyLog.playTask("==========字幕的属性222===" + speend + " / " + taFontSize + " / " + taMove);
            autoScrollTextView.setTextVules(content);
            taFontSize = TaskDealUtil.px2sp(context, taFontSize);
            MyLog.playTask("==========字幕的属性222===" +  taFontSize );
            autoScrollTextView.setTextSizeColor(taFontSize, textColor, speend, taMove, taFont);
            //添加点击事件
            autoScrollTextView.setClickCpEntity(listener, cpListEntity);
        } catch (Exception e) {
            MyLog.playTask("==========字幕的属性error===" + e.toString());
            e.printStackTrace();
        }
    }


    @Override
    public void playComplet() {

    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

    @Override
    public void clearMemory() {
        if (autoScrollTextView != null) {
            autoScrollTextView.stopScroll();
        }
    }

    @Override
    public void removeCacheView(String tag) {

    }

    @Override
    public void pauseDisplayView() {
        if (autoScrollTextView != null) {
            autoScrollTextView.pauseDisplayView();
        }
    }

    @Override
    public void timeChangeToUpdateView() {

    }

    @Override
    public void moveViewForward(boolean b) {

    }

    @Override
    public void resumePlayView() {
        if (autoScrollTextView != null) {
            autoScrollTextView.resumePlayView();
        }
    }

}
