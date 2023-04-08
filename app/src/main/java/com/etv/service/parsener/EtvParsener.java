package com.etv.service.parsener;

import static com.etv.config.AppConfig.APP_TYPE_AD_JH;
import static com.etv.config.AppConfig.APP_TYPE_BEIJING_MG;
import static com.etv.config.AppConfig.APP_TYPE_LK_QRCODE;
import static com.etv.util.FileUtil.TAG;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.etv.config.ApiInfo;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.db.DbStatiscs;
import com.etv.entity.StatisticsEntity;
import com.etv.police.activity.PoliceCacheActivity;
import com.etv.service.EtvService;
import com.etv.service.TaskWorkService;
import com.etv.service.util.EtvServerModule;
import com.etv.service.util.EtvServerModuleImpl;
import com.etv.task.activity.PlayTaskTriggerActivity;
import com.etv.task.activity.PlayerTaskActivity;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.util.SimpleDateUtil;
import com.etv.util.media.AudioPlayerUtil;
import com.etv.util.media.MediaPlayerListener;
import com.ys.rkapi.MyManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import okhttp3.Call;


public class EtvParsener {

    Context context;
    private Handler handler;

    public EtvParsener(Context context) {
        this.context = context;
        initOther();
    }

    /***
     * 处理红外感应，GPIO触发，
     * 人来了得状况
     */
    public void dealRedGpioInfoPeronComeIn() {
        if (!AppInfo.isAppRun) {
            MyLog.phone("IO广播==软件没起来，中断操作==");
            return;
        }
        if (!EtvService.isServerStart) {
            MyLog.phone("=Server还没有起来，这里不往下走了==");
            return;
        }
        MyLog.phone("IO广播==人来了开始处理业务逻辑==");
        switch (AppConfig.APP_TYPE) {
            case AppConfig.APP_TYPE_POLICE_ALERT:
                //一键报警版本
                callNetPolice();
                break;
            case AppConfig.APP_TYPE_CW_GPIO:  //触沃 语音提示
                startToPlayWelcomeAudio();
                break;
            default:
                startToPlayTriggleActivity(0);
                break;
        }
    }

    private void startToPlayTriggleActivity(int playPosition) {
        MyLog.task("进入触发节目模式==" + playPosition);
        if (PlayTaskTriggerActivity.ISVIEW_FORST) {
            return;
        }
        MyLog.task("进入触发节目模式==启动界面");
        //不在前台  启动界面， 传递位置
        Intent intent = new Intent(context, PlayTaskTriggerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    boolean startSpeakPermission = true;

    private void startToPlayWelcomeAudio() {
        if (!startSpeakPermission) {
            MyLog.cdl("=======startToPlayWelcomeAudio===被拦截，等待下一次触发================");
            return;
        }
        if (context != null) {
            context.sendBroadcast(new Intent(AppInfo.TURN_VOICE_ZREO));
        }
        MyLog.cdl("=======startToPlayWelcomeAudio==========onSubscribe开始说话=========");
        String filePath = AppInfo.WELCOME_SAVE_PATH;
        AudioPlayerUtil.getInstance().startToPlayMedia(filePath, new MediaPlayerListener() {

            @Override
            public void playerCompany() {
                MyLog.cdl("=======startToPlayWelcomeAudio======说话完成==========");
                if (context != null) {
                    context.sendBroadcast(new Intent(AppInfo.TURN_VOICE_RESUME));
                }
            }

            @Override
            public void playerError(String errorDesc) {
                MyLog.cdl("=======startToPlayWelcomeAudio=======说话异常============");
                if (context != null) {
                    context.sendBroadcast(new Intent(AppInfo.TURN_VOICE_RESUME));
                }
            }
        });

        int timeDelay = SharedPerManager.getTtsMessageDelay();
        if (timeDelay < 1) {
            MyLog.cdl("=======startToPlayWelcomeAudio=======设定的时间<1不拦截============");
            startSpeakPermission = true;
            return;
        }
        startSpeakPermission = false;
        MyLog.cdl("=======startToPlayWelcomeAudio=======设定的时间 开始拦截============");
        Observable.timer(timeDelay, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                MyLog.cdl("=======startToPlayWelcomeAudio=======倒计时完成，可以触发============");
                startSpeakPermission = true;
            }
        });

    }

    private void callNetPolice() {
        if (PoliceCacheActivity.isViewFront) {
            MyLog.phone("=正在通话中，中断操作==");
            return;
        }
        try {
            MyLog.phone("=开始拨打电话==");
            Intent intent = new Intent(context, PoliceCacheActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void SDorUSBcheckIn(Context context, String path) {
        initOther();
        etvServerModule.SDorUSBcheckIn(context, path);
    }

    /***
     * 删除任务
     * 提交给服务器，
     * 删除本地文件
     *删除本地数据库
     */
    public void deleteEquipmentTaskById(String tag, String taskId) {
        initOther();
        MyLog.del("=======帅选并且删除任务标记==tag");
        try {
            if (etvServerModule != null) {
                etvServerModule.deleteEquipmentTaskServer(taskId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 上传进度给服务器
     *
     * @param taskId
     * @param progress int taskId, int progress, int downKb
     */
    public void updateProgressToWebRegister(String tag, String taskId, String totalNum, int progress, int downKb, String type) {
//        MyLog.cdl("===下载进度标记==" + tag + " /taskId=" + taskId + " /totalNum=" + totalNum + " /progress= " + progress + " /downKb=" + downKb);
        initOther();
        etvServerModule.updateProgressToWebRegister(taskId, totalNum, progress, downKb, type);
    }


    /***
     * 提交设备信息到统计服务器
     */
    public void updateDevInfoToAuthorServer(String version) {
        initOther();
        etvServerModule.upodateDevInfoToAuthorServer(version);
    }

    public void updateDevStatuesToWeb(Context context) {
        initOther();
        etvServerModule.updateDevInfoToWeb(context, "EtvService 界面调用");
    }

    public void updateDownApkImgProgress(int percent, int downKb, String fileName, String tag) {
        initOther();
        etvServerModule.updateDownApkImgProgress(percent, downKb, fileName);
    }

    /**
     * 即时提交统计到服务器
     *
     * @param midId
     * @param addType
     * @param time
     * @param count
     */
    public void addFileNumToTotalOnTime(String midId, String addType, int time, int count) {
        if (!AppInfo.isAppRun) {
            MyLog.update("==统计===软件没起来");
            return;
        }
        int playModel = SharedPerManager.getWorkModel();
        if (playModel != AppInfo.WORK_MODEL_NET) {
            MyLog.update("==统计===非网路模式");
            return;
        }
        if (!AppConfig.isOnline) {
            MyLog.update("==统计===设备不在线");
            return;
        }
        try {
            initOther();
            String timeUpdate = SimpleDateUtil.parsenerSratisDate(System.currentTimeMillis());
            etvServerModule.addFileNumTotal(midId, addType, time, count, timeUpdate);
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    /**
     * 添加统计信息到服务器
     */
    public void addPlayNumToWeb() {
        try {
            if (!AppInfo.isAppRun) {
                MyLog.update("==统计===软件没起来");
                return;
            }
            int playModel = SharedPerManager.getWorkModel();
            if (playModel != AppInfo.WORK_MODEL_NET) {
                MyLog.update("==统计===非网路模式");
                return;
            }
            MyLog.update("==统计===准备提交统计");
            StatisticsEntity statisticsEntity = DbStatiscs.getLastUpdateStaEntity();
            if (statisticsEntity == null) {
                MyLog.update("==统计===提交的参数==null");
                return;
            }
            MyLog.update("=====提交的参数==" + statisticsEntity.toString());
            initOther();
            String stMtId = statisticsEntity.getMtid();
            if (stMtId == null || stMtId.contains("null") || stMtId.length() < 5) {
                DbStatiscs.clearNullData();
                return;
            }
            String addType = statisticsEntity.getAddtype();
            int time = statisticsEntity.getPmtime();
            int count = statisticsEntity.getCount();
            long updteTime = statisticsEntity.getCreatetime();
            String timeUpdate = SimpleDateUtil.parsenerSratisDate(updteTime);
            etvServerModule.addFileNumTotal(stMtId, addType, time, count, timeUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //=============GPIO相关操作===============================================================================================

    EtvServerModule etvServerModule;

    private void initOther() {
        if (handler == null) {
            handler = new Handler();
        }
        if (etvServerModule == null) {
            etvServerModule = new EtvServerModuleImpl();
        }
    }

    public static boolean isDealTime = false;

    public void checkSytemTimeFromWeb(EtvService etvService) {

        if (!NetWorkUtils.isNetworkConnected(context)) {
            Log.e(TAG, "checkSytemTimeFromWeb net no connect");
            return;
        }
        Log.e(TAG, "checkSytemTimeFromWeb net ok" + AppConfig.APP_TYPE);
        //      设置并保存系统的时间 这里的type == 35

        String url = ApiInfo.UPDATE_TIME_FROM_WEB();
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, String s, int i) {
                        //请求失败

                    }

                    @Override
                    public void onResponse(String s, int i) {
                        try {


                            JSONObject jsonObject = new JSONObject(s);
                            JSONObject data = jsonObject.getJSONObject("data");
                            String currentTime = data.getString("currentTime");


                            int year = Integer.parseInt(currentTime.substring(0, 4));
                            int month = Integer.parseInt(currentTime.substring(4, 6));
                            int day = Integer.parseInt(currentTime.substring(6, 8));
                            int hour = Integer.parseInt(currentTime.substring(8, 10));
                            int minute = Integer.parseInt(currentTime.substring(10, 12));
                            int second = Integer.parseInt(currentTime.substring(12, 14));
                            Log.e("liujk", "onResponse: " + year + "--" + month + "--" + day + "--" + hour + "--" + minute + "--" + second);

                            String time = year + "-" + month + "-" + day + " " + hour + "-" + minute;
                            //通知Activity 更新 UI
                            //设置平板的时间
                            MyManager manager = MyManager.getInstance(context);
                            manager.setTime(year, month, day, hour, minute, second);

                            sendSystemTimeChangeBroadCast(time);

                            isDealTime = true;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    /**
     * 发送服务器时间改变，监听广播
     */
    private void sendSystemTimeChangeBroadCast(String time) {
        Intent intent = new Intent(AppInfo.SYSTEM_TIME_CHANGE);
        intent.putExtra("time", time);
        context.sendBroadcast(intent);
    }

}
