package com.etv.view.layout.date;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etv.config.AppInfo;
import com.etv.listener.TaskPlayStateListener;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.util.TimerDealUtil;
import com.etv.view.layout.Generator;
import com.ys.etv.R;

import java.util.List;

/**
 * 时间倒计时
 */
public class ViewTimeReduceGenerate extends Generator {

    View view;
    CpListEntity cpListEntity;
    Context context;

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    @Override
    public void updateTextInfo(Object object) {

    }

    public ViewTimeReduceGenerate(Context context, CpListEntity cpListEntity, int x, int y, int width, int height) {
        super(context, x, y, width, height);
        this.context = context;
        this.cpListEntity = cpListEntity;
        view = LayoutInflater.from(context).inflate(R.layout.view_time_reduce, null);
        initView(view);
        TimerDealUtil.getInstance().addGeneratorToList(this);
    }

    TextView tv_time_show;
    RelativeLayout rela_text_bgg;
    long countDown = 0L;             //用来展示倒计时的 变量
    String taAlignment = "5";
    public static final String TIME_REDUS = "1";
    public static final String TIME_ADD = "2";
    String timeType = TIME_REDUS;   // 1 -倒计时   2 正计时

    private void initView(View view) {
        Log.e("cdl", "=============倒计时=====initView======");
        taAlignment = "5";
        rela_text_bgg = (RelativeLayout) view.findViewById(R.id.rela_text_bgg);
        tv_time_show = (TextView) view.findViewById(R.id.tv_time_show);

        rela_text_bgg.setOnLongClickListener(new View.OnLongClickListener() {
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
        Log.e("cdl", "=============倒计时=====updateView======");
        TextInfo textInfo = (TextInfo) object;
        int bggColor = 0x00000000;      //透明
        int textColorInt = 0xffffffff;  //默认白色
        String taMove = "1";       //是否显示单位
        String taMoveSpeed = "1";   //单位名称
        String typeFace = AppInfo.TEXT_FONT_DEFAULT;
        float fontSzie = 16;
        try {
            if (textInfo != null) {
                String textColor = textInfo.getTaColor();
                textColorInt = TaskDealUtil.getColorFromInToSystem(textColor);
                String backColor = textInfo.getTaBgColor();
                bggColor = TaskDealUtil.getColorFromInToSystem(backColor);
                String sizeString = textInfo.getTaFontSize();
                timeType = textInfo.getTaBgImage();
                MyLog.cdl("===========过去计时信息=====timeType====" + timeType);
                if (timeType == null || TextUtils.isEmpty(timeType)) {
                    timeType = TIME_REDUS; //默认倒计时
                }
                fontSzie = TaskDealUtil.getTextSize(sizeString);
                String taCountDown = textInfo.getTaCountDown();
                //根据类型解析时间
                countDown = getTimeChange(taCountDown);
                typeFace = textInfo.getTaFonType();
                taAlignment = textInfo.getTaAlignment();
                taMove = textInfo.getTaMove();   //是否显示
                taMoveSpeed = textInfo.getTaMoveSpeed();  //单位
            }
            setViewColorSize(bggColor, fontSzie, textColorInt, taMove, taMoveSpeed, typeFace);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 解析获取时间
     * @param taCountDown
     * @return
     */
    private long getTimeChange(String taCountDown) {
        long backCountDown = 30;
        MyLog.cdl("===========过去计时信息===getTimeChange======" + timeType);
        if (timeType.contains(TIME_REDUS)) {
            //倒计时
            long currentTime = System.currentTimeMillis();
            if (taCountDown == null || taCountDown.length() < 1) {
                backCountDown = 30;
            } else {
                try {
                    backCountDown = Long.parseLong(taCountDown);
                    backCountDown = (backCountDown - currentTime) / 1000;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (backCountDown < 3) {
                backCountDown = 0;
            }
            MyLog.cdl("===========过去计时信息===倒计时======" + backCountDown);
            return backCountDown;
        }
        //正计时的功能
        if (taCountDown == null || taCountDown.length() < 1) {
            backCountDown = System.currentTimeMillis();
        } else {
            try {
                backCountDown = Long.parseLong(taCountDown);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (backCountDown < 3) {
                backCountDown = System.currentTimeMillis();
            }
        }
        MyLog.cdl("===========过去计时信息=====正计时====" + backCountDown);
        return backCountDown;
    }

    boolean isShowDev;  //是否显示单位
    String dWei;        //单位

    /**
     * @param bggColor
     * @param textSize
     * @param textColorInt
     * @param isShowDw     是否显示单位 ，不显示单位  0天 23:10:10
     * @param dWei         单位
     */
    private void setViewColorSize(int bggColor, float textSize, int textColorInt, String isShowDw, String dWei, String typeFace) {
        if (isShowDw.contains("2")) {
            isShowDev = true;
        } else {
            isShowDev = false;
        }
        this.dWei = dWei;
        rela_text_bgg.setBackgroundColor(bggColor);
//        int showPosition = TaskDealUtil.getShowPosition(taAlignment);
//        tv_time_show.setGravity(showPosition);
//        textSize = TaskDealUtil.px2sp(context, textSize);
        tv_time_show.setTextSize(textSize);
        tv_time_show.setTextColor(textColorInt);
        Typeface typeface = TaskDealUtil.getFontTypeFace(typeFace);
        if (typeface != null) {
            tv_time_show.setTypeface(typeface);
        }
    }

    @Override
    public void timeChangeToUpdateView() {
        if (timeType.contains("1")) {  //倒计时
            countDown--;
            if (countDown < 0) {
                tv_time_show.setText("00:00:00");
                playComplet();
                return;
            }
            //获取倒计时的时间
            String showTimRedues = TaskDealUtil.getReduiceTime(countDown, isShowDev, dWei);
            tv_time_show.setText(showTimRedues);
            return;
        }
        //正计时
        long addTime = System.currentTimeMillis() - countDown;
        String showTimeAdd = TaskDealUtil.getReduiceTime(addTime / 1000, isShowDev, dWei);
        tv_time_show.setText(showTimeAdd);
    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

    @Override
    public void playComplet() {
        if (listener == null) {
            return;
        }
        listener.playComplete(TaskPlayStateListener.TAG_PLAY_TIME_REDUCE);
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {
        TimerDealUtil.getInstance().removeGeneratorToList(this);
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

    public int px2sp(float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) ((pxValue * 1.0) / fontScale + 0.5f);
    }

}
