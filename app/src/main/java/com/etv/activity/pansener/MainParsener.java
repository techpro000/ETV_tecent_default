package com.etv.activity.pansener;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.etv.activity.MainActivity;
import com.etv.activity.view.MainView;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.listener.TimeChangeListener;
import com.etv.service.EtvService;
import com.etv.service.util.EtvServerModule;
import com.etv.service.util.EtvServerModuleImpl;
import com.etv.service.util.TaskServiceView;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.util.SimpleDateUtil;
import com.ys.etv.R;
import com.ys.model.util.ActivityCollector;

public class MainParsener {

    Context context;
    MainView mainView;
    private static final String TAG = "cdl";

    public MainParsener(Context context, MainView mainView) {
        this.mainView = mainView;
        this.context = context;
        getView();
    }

    TextView tv_time;
    ImageView iv_net_state;
    ImageView iv_line_state;
    ImageView iv_work_model;

    private void getView() {
        if (mainView == null) {
            return;
        }
        try {
            iv_work_model = mainView.getIvWorkModel();
            iv_net_state = mainView.getIvWifiState();
            iv_line_state = mainView.getIvlineState();
            tv_time = mainView.getTimeView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTimeNetLineView() {
        try {
            int workModel = SharedPerManager.getWorkModel();
            if (workModel == AppInfo.WORK_MODEL_NET) {
                iv_work_model.setBackgroundResource(R.mipmap.dialog_net);
            } else if (workModel == AppInfo.WORK_MODEL_NET_DOWN) {
                iv_work_model.setBackgroundResource(R.mipmap.dialog_net_down);
            } else if (workModel == AppInfo.WORK_MODEL_SINGLE) {
                iv_work_model.setBackgroundResource(R.mipmap.dialog_single);
                iv_line_state.setBackgroundResource(R.mipmap.line_web_dis);
            }
            //更新网络状态
            boolean isNetLine = NetWorkUtils.isNetworkConnected(context);
            iv_net_state.setBackgroundResource(isNetLine ? R.mipmap.net_line : R.mipmap.net_disline);
            //更新时间
            String currentTime = SimpleDateUtil.formatCurrentTime();
            tv_time.setText(currentTime);
            if (workModel == AppInfo.WORK_MODEL_SINGLE){
                iv_line_state.setBackgroundResource(R.mipmap.line_web_dis);
            }else {
                if (isNetLine) {
                    boolean isOnline = AppConfig.isOnline;
                    iv_line_state.setBackgroundResource(isOnline ? R.mipmap.line_web : R.mipmap.line_web_dis);
                } else {
                    iv_line_state.setBackgroundResource(R.mipmap.line_web_dis);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startTimerToPlay(String tag) {
        int timeDelay = AppConfig.CHECK_TIMER_TO_PLAY();
        MyLog.i("main", "=====开始计时去播放任务=startTimerToPlay====" + tag + " / " + timeDelay);
        handler.removeMessages(MESSAGE_DELAY_CHANGE);
        handler.sendEmptyMessageDelayed(MESSAGE_DELAY_CHANGE, timeDelay * 1000);
    }

    private static final int MESSAGE_DELAY_CHANGE = 9568;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE_DELAY_CHANGE) {
                toBackMainView();
            }
        }
    };


    private void toBackMainView() {
        MyLog.i("main", "=====开始计时去播放任务=时间到了====");
        if (!MainActivity.isMainForst) {
            MyLog.i("main", "=====开始计时去播放任务=1111=主界面不再前台====");
            return;
        }
        boolean mainForst = ActivityCollector.isForeground(context, MainActivity.class.getName());
        MyLog.i("main", "=====开始计时去播放任务=22222=====" + mainForst);
        if (!mainForst) {
            return;
        }
        MyLog.i("main", "=====开始计时去播放任务=3333=====");
        mainView.startToCheckTaskToActivity("主界面倒计时");
    }

    /***
     * 修改信息给服务器
     */
    EtvServerModule etvServerModule;

    public void updateDevInfoToWeb() {
        long lastTime = SharedPerManager.getLastUpdateDevTime();
        long curreTime = System.currentTimeMillis();
        if (Math.abs(curreTime - lastTime) < (30 * 1000)) {
            MyLog.cdl("======提交设备信息===频率太高了，中断操作=");
            return;
        }
        SharedPerManager.setLastUpdateDevTime(curreTime);
        MyLog.cdl("======提交设备信息===00000=");
        if (etvServerModule == null) {
            etvServerModule = new EtvServerModuleImpl();
        }
        etvServerModule.queryDeviceInfoFromWeb(context, new TaskServiceView() {

            @Override
            public void getDevInfoFromWeb(boolean isSuccess, String errorDesc) {
                if (!isSuccess) {
                    return;
                }
                EtvService.getInstance().updateDevStatuesToWeb(context);
            }
        });
    }
}