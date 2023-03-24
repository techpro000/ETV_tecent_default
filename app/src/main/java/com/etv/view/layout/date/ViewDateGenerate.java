package com.etv.view.layout.date;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
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
public class ViewDateGenerate extends Generator {

    View view;
    Context context;

    @Override
    public void updateTextInfo(Object object) {

    }

    public ViewDateGenerate(Context context, int startX, int StartY, int width, int height) {
        super(context, startX, StartY, width, height);
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.view_date, null);
        initView(view);
    }

    @Override
    public void timeChangeToUpdateView() {

    }

    private TextView tv_date;
    String textColor = "255,255,255";
    RelativeLayout rela_bgg_date;

    private void initView(View view) {
        tv_date = (TextView) view.findViewById(R.id.tv_date);
        rela_bgg_date = (RelativeLayout) view.findViewById(R.id.rela_bgg_date);
        rela_bgg_date.setOnLongClickListener(new View.OnLongClickListener() {
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
        TextInfo textInfo = (TextInfo) object;
        int bggColor = 0x00000000;      //透明
        if (textInfo == null) {
            return;
        }
        try {
            String date = textInfo.getTaContent();
            String backColor = textInfo.getTaBgColor();
            bggColor = TaskDealUtil.getColorFromInToSystem(backColor);
            textColor = textInfo.getTaColor().trim();
            int color = TaskDealUtil.getColorFromInToSystem(textColor);
            float textSize = TaskDealUtil.getTextSize(textInfo.getTaFontSize());
            rela_bgg_date.setBackgroundColor(bggColor);
            tv_date.setTextColor(color);
            tv_date.setTextSize(textSize);
            MyLog.cdl("======显示当前的日期==" + SimpleDateUtil.getDate());
            String typeFace = textInfo.getTaFonType();
            Typeface typeface = TaskDealUtil.getFontTypeFace(typeFace);
            if (typeFace != null) {
                tv_date.setTypeface(typeface);
            }
            tv_date.setGravity(Gravity.CENTER);
            if (date.contains("/")) {
                String[] spits = date.split("/");
                if (spits.length == 2) {
                    // 1个 /
                    tv_date.setText(SimpleDateUtil.getMouthAndDateDate());
                    return;
                } else {
                    // 2个 /
                    tv_date.setText(SimpleDateUtil.getDate());
                    return;
                }
            }
            if (date.contains("-")) {
                String[] spits = date.split("-");
                if (spits.length == 2) {
                    // 1个 -
                    tv_date.setText(SimpleDateUtil.getMouthToDateDate());
                    return;
                } else {
                    // 2个 -
                    tv_date.setText(SimpleDateUtil.getDateSingle());
                    return;
                }
            }
            if (date.contains("年") && date.contains("月") && date.contains("日")){
                tv_date.setText(SimpleDateUtil.getYearToMouthToDateDate());
            }else {
                tv_date.setText(SimpleDateUtil.getMouthDateDate());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playComplet() {

    }

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }


    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

    @Override
    public void pauseDisplayView() {

    }

    @Override
    public void resumePlayView() {

    }

    @Override
    public void moveViewForward(boolean b) {

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
}
