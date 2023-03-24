package com.etv.view.layout.date;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.util.SimpleDateUtil;
import com.etv.view.layout.Generator;
import com.ys.etv.R;

import java.util.List;

/**
 * 展示天气的控件
 * Created by 定龙
 */
public class ViewWeekGenerate extends Generator {

    View view;
    Context context;

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    @Override
    public void updateTextInfo(Object object) {

    }

    @Override
    public void timeChangeToUpdateView() {

    }

    public ViewWeekGenerate(Context context, int startX, int StartY, int width, int height) {
        super(context, startX, StartY, width, height);
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.view_week, null);
        initView(view);
    }

    private TextView tv_week;
    String textColor = "255,255,255";
    RelativeLayout rela_week_bgg;

    private void initView(View view) {
        tv_week = (TextView) view.findViewById(R.id.tv_date);
        rela_week_bgg = (RelativeLayout) view.findViewById(R.id.rela_week_bgg);
        rela_week_bgg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.longClickView(null, null);
                }
                return true;
            }
        });
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        int bggColor = 0x00000000;      //透明
        TextInfo textInfo = (TextInfo) object;
        if (textInfo == null) {
            return;
        }
        try {
            String typeFace = textInfo.getTaFonType();
            MyLog.cdl("=====星期得字幕===" + typeFace);
            Typeface typeface = TaskDealUtil.getFontTypeFace(typeFace);
            if (typeface != null) {
                tv_week.setTypeface(typeface);
            }
            String backColor = textInfo.getTaBgColor();
            bggColor = TaskDealUtil.getColorFromInToSystem(backColor);
            textColor = textInfo.getTaColor().trim();
            int color = TaskDealUtil.getColorFromInToSystem(textColor);
            float textSize = TaskDealUtil.getTextSize(textInfo.getTaFontSize());
            tv_week.setTextColor(color);
            rela_week_bgg.setBackgroundColor(bggColor);
            tv_week.setTextSize(textSize);
            tv_week.setText(SimpleDateUtil.getWeek());
        } catch (Exception e) {
            MyLog.cdl("=====星期得字幕=Exception==" + e.toString());
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
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {

    }

    @Override
    public void removeCacheView(String tag) {

    }

    @Override
    public void moveViewForward(boolean b) {

    }

    @Override
    public void pauseDisplayView() {

    }

    @Override
    public void resumePlayView() {

    }

}
