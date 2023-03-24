//package com.etv.view.layout.date;
//
//import android.content.Context;
//import android.graphics.Typeface;
//import android.os.Handler;
//import android.os.Message;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.etv.task.entity.MediAddEntity;
//import com.etv.task.entity.SceneEntity;
//import com.etv.task.entity.TextInfo;
//import com.etv.task.util.TaskDealUtil;
//import com.etv.util.MyLog;
//import com.etv.util.SimpleDateUtil;
//import com.etv.view.layout.Generator;
//import com.ys.etv.R;
//
//import java.util.List;
//
///**
// * 展示天气的控件
// * Created by 定龙
// */
//public class ViewTimeOnlyGenerateBack extends Generator {
//
//    View view;
//    Context context;
//    boolean isShowYMD;
//
//    @Override
//    public void updateTextInfo(Object object) {
//
//    }
//
//    public ViewTimeOnlyGenerateBack(Context context, int startX, int StartY, int width, int height, boolean isShowYMD) {
//        super(context, startX, StartY, width, height);
//        this.isShowYMD = isShowYMD;
//        this.context = context;
//        view = LayoutInflater.from(context).inflate(R.layout.view_time_only, null);
//        initView(view);
//    }
//
//    private TextView tv_time;
//    String textColor = "255,255,255";
//    RelativeLayout rela_time_bgg;
//
//
//    private void initView(View view) {
//        tv_time = (TextView) view.findViewById(R.id.tv_time);
//        rela_time_bgg = (RelativeLayout) view.findViewById(R.id.rela_time_bgg);
//    }
//
//    @Override
//    public void updateView(Object object, boolean isShowBtn) {
//        TextInfo textInfo = (TextInfo) object;
//        int bggColor = 0x00000000;      //透明
//        if (textInfo == null) {
//            return;
//        }
//        try {
//            String backColor = textInfo.getTaBgColor();
//            bggColor = TaskDealUtil.getColorFromInToSystem(backColor);
//            textColor = textInfo.getTaColor().trim();
//            int color = TaskDealUtil.getColorFromInToSystem(textColor);
//            float textSize = TaskDealUtil.getTextSize(textInfo.getTaFontSize());
//            rela_time_bgg.setBackgroundColor(bggColor);
//            tv_time.setTextColor(color);
//            tv_time.setTextSize(textSize);
//            String typeFace = textInfo.getTaFonType();
//            Typeface typeface = TaskDealUtil.getFontTypeFace(typeFace);
//            if (typeface != null) {
//                tv_time.setTypeface(typeface);
//            }
//            MyLog.cdl("=====星期得字幕===" + typeface + " /" + color + " / " + textSize);
//            tv_time.setText(SimpleDateUtil.getCurrentHourMinSec(isShowYMD));
//            startTimer();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private boolean isStart = true;
//
//    private void startTimer() {
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                while (isStart) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    handler.sendEmptyMessage(MESSAGE_UPDATE_TIME);
//                }
//            }
//        }.start();
//    }
//
//    private static final int MESSAGE_UPDATE_TIME = 23564;
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == MESSAGE_UPDATE_TIME) {
//                String currenTime = SimpleDateUtil.getCurrentHourMinSec(isShowYMD);
//                tv_time.setText(currenTime);
//            }
//        }
//    };
//
//    @Override
//    public void timeChangeToUpdateView() {
//        MyLog.cdl("======TimerDealUtil==时间控件===timeChangeMin====回调==");
//    }
//
//    @Override
//    public void playComplet() {
//
//    }
//
//    @Override
//    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {
//
//    }
//
//    @Override
//    public View getView() {
//        return view;
//    }
//
//    @Override
//    public void clearMemory() {
//        isStart = false;
//    }
//
//    @Override
//    public void removeCacheView() {
//
//    }
//
//    @Override
//    public void moveViewForward(boolean b) {
//
//    }
//
//    @Override
//    public void pauseDisplayView() {
//
//    }
//
//    @Override
//    public void resumePlayView() {
//
//    }
//
//}
