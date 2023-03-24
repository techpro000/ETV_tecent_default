package com.etv.view.layout.date;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etv.config.AppInfo;
import com.etv.listener.TimeChangeListener;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SimpleDateUtil;
import com.etv.util.TimerDealUtil;
import com.etv.view.layout.Generator;
import com.ys.etv.R;

import org.apache.poi.ss.formula.functions.T;

import java.util.List;

/**
 * 展示天气的控件
 * Created by 定龙
 */
public class ViewTimeOnlyGenerate extends Generator {

    View view;
    Context context;
    boolean isShowYMD;

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    @Override
    public void updateTextInfo(Object object) {

    }

    public ViewTimeOnlyGenerate(Context context, int startX, int StartY, int width, int height, boolean isShowYMD) {
        super(context, startX, StartY, width, height);
        this.isShowYMD = isShowYMD;
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.view_time_only, null);
        initView(view);
    }

    private TextView tv_time;
    String textColor = "255,255,255";
    RelativeLayout rela_time_bgg;

    private void initView(View view) {
        tv_time = (TextView) view.findViewById(R.id.tv_time);
        rela_time_bgg = (RelativeLayout) view.findViewById(R.id.rela_time_bgg);
        TimerDealUtil.getInstance().addGeneratorToList(this);
        rela_time_bgg.setOnLongClickListener(new View.OnLongClickListener() {
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
            String backColor = textInfo.getTaBgColor();
            bggColor = TaskDealUtil.getColorFromInToSystem(backColor);
            textColor = textInfo.getTaColor().trim();
            int color = TaskDealUtil.getColorFromInToSystem(textColor);
            float textSize = TaskDealUtil.getTextSize(textInfo.getTaFontSize());
            rela_time_bgg.setBackgroundColor(bggColor);
            tv_time.setTextColor(color);
            tv_time.setTextSize(textSize);
            String typeFace = textInfo.getTaFonType();
            Typeface typeface = TaskDealUtil.getFontTypeFace(typeFace);
            if (typeface != null) {
                tv_time.setTypeface(typeface);
            }
            MyLog.cdl("=====星期得字幕===" + typeface + " /" + color + " / " + textSize);
            tv_time.setText(SimpleDateUtil.getCurrentHourMinSec(isShowYMD));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void timeChangeToUpdateView() {
        if (tv_time != null) {
            String currenTime = SimpleDateUtil.getCurrentHourMinSec(isShowYMD);
            tv_time.setText(currenTime);
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
        TimerDealUtil.getInstance().removeGeneratorToList(this);
        if (tv_time != null) {
            tv_time = null;
        }
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
