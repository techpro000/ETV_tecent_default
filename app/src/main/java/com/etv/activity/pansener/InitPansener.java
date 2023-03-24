package com.etv.activity.pansener;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.etv.activity.TimerReduceActivity;
import com.etv.activity.view.InitView;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.service.EtvService;
import com.etv.service.listener.EtvServerListener;
import com.etv.service.util.EtvServerModule;
import com.etv.service.util.EtvServerModuleImpl;
import com.etv.service.util.TaskServiceView;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.util.poweronoff.CheckTimeRunnable;
import com.etv.util.poweronoff.PowerOnOffManager;

public class InitPansener {

    Context context;
    InitView initView;

    public InitPansener(Context context, InitView initView) {
        this.context = context;
        this.initView = initView;
    }
    public InitPansener(Context context) {
        this.context = context;
    }

    /***
     * 检查定时开关机
     * 1：有网的情况下联网获取数据
     * 2：没有网络的情况下，从数据库中获取数据
     */
    public void checkMainPowerOnOff() {
        handler.sendEmptyMessageDelayed(TIME_TO_JUMP_TO_ACTIVITY, 10 * 1000);
        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_KING_LAM) {
            MyLog.powerOnOff("0000==============金浪漫客户，不检查定时开关机");
            return;
        }
        checkTimerShotDown("延时2500秒后执行");
    }

    private static final int TIME_TO_JUMP_TO_ACTIVITY = 5643;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler.removeMessages(msg.what);
            switch (msg.what) {
                case TIME_TO_JUMP_TO_ACTIVITY:
                    MyLog.cdl("===========时间到了，跳转界面====");
                    jumpMainActivity();
                    break;
            }
        }
    };


    /**
     * 检查当前是否是关机时间
     */
    public void checkTimerShotDown(String tag) {
        MyLog.powerOnOff("0000==============检查是否是关机时间===" + tag);
        CheckTimeRunnable runnable = new CheckTimeRunnable(context, new EtvServerListener() {

            @Override
            public void jujleCurrentIsShutDownTime(boolean isShutDown) {
                if (isShutDown) {
                    MyLog.powerOnOff("0000======当前是开机时间");
                    if (initView == null) {
                        return;
                    }
                    jumpMainActivity();  //当前是开机时间，直接跳转
                } else {
                    showAlertDialog();
                    MyLog.powerOnOff("0000======当前是关机时间");
                }
            }
        });
        EtvService.getInstance().executor(runnable);
    }


    private void jumpMainActivity() {
        //进入界面前，设置一次定时开关机，下次使用
        PowerOnOffManager.getInstance().changePowerOnOffByWorkModel("单机模式，修改定时开关机");   //修改定时开关机
        if (initView == null) {
            return;
        }
        if (handler != null) {
            handler.removeMessages(TIME_TO_JUMP_TO_ACTIVITY);
        }
        initView.jumpActivity();
    }


    private void showAlertDialog() {
        handler.removeMessages(TIME_TO_JUMP_TO_ACTIVITY);
        Intent intent = new Intent();
        intent.setClass(context, TimerReduceActivity.class);
        context.startActivity(intent);
    }

    public void onDestroy() {
        if (handler != null) {
            handler.removeMessages(TIME_TO_JUMP_TO_ACTIVITY);
        }
    }

    public void queryNickName() {
        int workModel = SharedPerManager.getWorkModel();
        if (workModel != AppInfo.WORK_MODEL_NET) {
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(context)) {
            return;
        }
        EtvServerModule etvServerModule = new EtvServerModuleImpl();
        etvServerModule.queryDeviceInfoFromWeb(context, new TaskServiceView() {

            @Override
            public void getDevInfoFromWeb(boolean isSuccess, String errorDesc) {
                if (!isSuccess) {
                    return;
                }
            }
        });
    }
}
