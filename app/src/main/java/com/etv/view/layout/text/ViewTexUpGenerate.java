package com.etv.view.layout.text;


import android.content.Context;
import android.graphics.Typeface;
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
import com.etv.view.TextViewScroll;
import com.etv.view.layout.Generator;
import com.ys.etv.R;

import java.util.List;

/**
 * 上下滚动字幕
 */
public class ViewTexUpGenerate extends Generator {

    View view;
    String textShow;
    int viewWidth;
    Context context;
    int viewHeight;
    CpListEntity cpListEntity;
    int startY;

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    @Override
    public void updateTextInfo(Object object) {

    }

    public ViewTexUpGenerate(Context context, CpListEntity cpListEntity, int x, int y, int width, int height, String text) {
        super(context, x, y, width, height);
        this.context = context;
        this.cpListEntity = cpListEntity;
        this.startY = y;
        this.textShow = text;
        this.viewWidth = width;
        this.viewHeight = height;
        view = LayoutInflater.from(context).inflate(R.layout.view_text_auto, null);
        initView(view);
    }

    TextViewScroll tv_scrool_view;
    View view_click;
    RelativeLayout rela_bgg;

    private void initView(View view) {
        tv_scrool_view = (TextViewScroll) view.findViewById(R.id.tv_scrool_view);
        rela_bgg = (RelativeLayout) view.findViewById(R.id.rela_bgg);
        view_click = (View) view.findViewById(R.id.view_click);
        tv_scrool_view.setTextSize(fontSzie);
        tv_scrool_view.setText(textShow);
        view_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyLog.cdl("========点击了控件===========");
                if (listener == null) {
                    return;
                }
                listener.clickTaskView(cpListEntity, null, 0);
            }
        });
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {
        if (tv_scrool_view != null) {
            tv_scrool_view.setScrollStatus(false);
        }
    }

    @Override
    public void removeCacheView(String tag) {
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        TextInfo textInfo = (TextInfo) object;
        try {
            rela_bgg.setBackgroundResource(0x0000000000);
            String taFont = AppInfo.TEXT_FONT_DEFAULT;
            if (textInfo != null) {
                String backBackgroundColor = textInfo.getTaBgColor();  //背景色
                if (backBackgroundColor != null && backBackgroundColor.length() > 1) {
                    int bggColor = TaskDealUtil.getColorFromInToSystem(backBackgroundColor);
                    rela_bgg.setBackgroundColor(bggColor);
                } else {
                    rela_bgg.setBackgroundResource(0x0000000000);
                }
                //===========================================================================
                String txtColor = textInfo.getTaColor();
                textColor = TaskDealUtil.getColorFromInToSystem(txtColor);
                tv_scrool_view.setTextColor(textColor);
                taFonts = textInfo.getTaFonType();
                fontSzie = TaskDealUtil.getTextSize(textInfo.getTaFontSize());
                tv_scrool_view.setTextSize(fontSzie);
                String intspeed = textInfo.getTaMoveSpeed().trim();
                if (intspeed == null || intspeed.length() < 1) {
                    txtSpeed = 1;
                } else {
                    txtSpeed = Integer.parseInt(intspeed);
                }
                taFont = textInfo.getTaFonType();
            }
            Typeface typeFace = TaskDealUtil.getFontTypeFace(taFont);
            if (typeFace != null) {
                tv_scrool_view.setTypeface(typeFace);
            }
            MyLog.playTask("自下向上得运动txtSpeed==" + txtSpeed + " /textSize=" + fontSzie + " /textColor=" + textColor);
            tv_scrool_view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tv_scrool_view.setSpeed(txtSpeed);
                    tv_scrool_view.setScrollType(TextViewScroll.FROM_BOTTOM);
                }
            }, 50);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int textColor = 0xff000000;  //默认黑色
    float fontSzie = 25;  //字体大小
    int txtSpeed = 1;  //速度
    String taFonts = "default";

    @Override
    public void playComplet() {

    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

    @Override
    public void moveViewForward(boolean b) {

    }

    @Override
    public void timeChangeToUpdateView() {

    }

    @Override
    public void pauseDisplayView() {
        if (tv_scrool_view != null) {
            tv_scrool_view.setScrollStatus(false);
        }
    }

    @Override
    public void resumePlayView() {
        if (tv_scrool_view != null) {
            tv_scrool_view.setScrollStatus(true);
        }
    }

}
