package com.etv.service;

import static com.etv.config.AppConfig.APP_TYPE;
import static com.etv.config.AppConfig.APP_TYPE_JIANGJUN_YUNCHENG;
import static com.etv.util.FileUtil.TAG;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.etv.activity.MainActivity;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.service.parsener.EtvParsener;
import com.etv.setting.InterestActivity;
import com.etv.socket.online.SocketWebListener;
import com.etv.udp.UdpParnsener;
import com.etv.util.CodeUtil;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.ProjectorUtil;
import com.etv.util.SharedPerManager;
import com.etv.util.TimerDealUtil;
import com.etv.util.rxjava.AppStatuesListener;
import com.etv.util.system.LeaderBarUtil;
import com.etv.util.system.LightUtil;
import com.ys.etv.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EtvService extends Service {

    public static EtvService instance;
    public static final String UPDATE_STA_TOTAL_TO_WEB = "com.etv.service.UPDATE_STA_TOTAL_TO_WEB";
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    ExecutorService executor = Executors.newFixedThreadPool(CPU_COUNT * 2);

    public void executor(Runnable runnable) {
        executor.execute(runnable);
    }

    public static EtvService getInstance() {
        if (instance == null) {
            synchronized (EtvService.class) {
                if (instance == null) {
                    instance = new EtvService();
                }
            }
        }
        return instance;
    }

    private BroadcastReceiver receiverEtv = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MyLog.d("cdl", "==============接收到广播====service====" + action);
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                //网络变化广播
                dealNetChange(context, intent);
            } else if (action.equals(AppInfo.RECEIVE_BROAD_CAST_LIVE)) {
                //守护进程发过来检测当前是否在前端的广播
                MyLog.cdl("=======软件接受到守护进程的广播===RECEIVE_BROAD_CAST_LIVE");
                try {
                    boolean isLive = LeaderBarUtil.isAppRunBackground(EtvService.this, "守护进程检测");
                    MyLog.cdl("判断软件是否运行在后台true=后台，false=前台=====" + isLive);
                    Intent intentSend = new Intent();
                    intentSend.setAction(AppInfo.SEND_BROAD_CAST_LIVE);
                    intentSend.putExtra(AppInfo.SEND_BROAD_CAST_LIVE, isLive);
                    sendBroadCastToViewToView(intentSend);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (action.equals(AppInfo.SOCKET_LINE_STATUS_CHANGE)) {
                //服务器连接状态
                int lineCode = intent.getIntExtra(AppInfo.SOCKET_LINE_STATUS_CODE, SocketWebListener.SOCKET_ERROR);
                if (lineCode != SocketWebListener.SOCKET_OPEN) {  //服务器连接成功
                    return;
                }
                //30秒之后检测升级功能
                int workModel = SharedPerManager.getWorkModel();
                if (workModel == AppInfo.WORK_MODEL_NET) {//网络模式
                    handler.sendEmptyMessageDelayed(UPDATE_INFO, 30 * 1000);
                }
            } else if (action.equals(UPDATE_STA_TOTAL_TO_WEB)) {  //提交统计信息给服务器
                MyLog.d("cdl", "===========添加统计请求===广播==========");
                addPlayNumToWeb();
            } else if (action.equals(AppInfo.RED_LINE_LISTENER_IN)) {
                GPIO_STATUES_CURRENT = GPIO_STATUES_COME;
                MyLog.phone("IO广播==人来了==");
                if (etvParsener == null) {
                    return;
                }
                etvParsener.dealRedGpioInfoPeronComeIn();
            } else if (action.equals(AppInfo.RED_LINE_LISTENER_OUT)) {
                MyLog.phone("IO广播==人走了==");
                GPIO_STATUES_CURRENT = GPIO_STATUES_LIVE;
            }
        }
    };

    public static int GPIO_STATUES_COME = 0;
    public static int GPIO_STATUES_LIVE = 1;
    public static int GPIO_STATUES_CURRENT = GPIO_STATUES_LIVE;

    private BroadcastReceiver receiverSdUsb = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MyLog.d("cdl", "==============USB插播========" + action);
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) { //插入SD卡USB
                try {
                    String sdUsbPath = intent.getData().getPath();
                    MyLog.cdl("===检测到U盘插入==USB path====" + sdUsbPath, true);
                    sendBroadCastToViewToView(new Intent(AppInfo.STOP_DOWN_TASK_RECEIVER));  //停止下载任务
                    Message message = new Message();
                    message.what = SEARCH_MESSAGE_SDCARD_IN;
                    message.obj = sdUsbPath;
                    handler.sendMessageDelayed(message, 2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (action.equals(Intent.ACTION_MEDIA_EJECT)) { //拔出来.SD usb
                sendBroadCastToViewToView(new Intent(AppInfo.STOP_DOWN_TASK_RECEIVER));  //停止下载任务
                Message message = new Message();
                message.what = SEARCH_MESSAGE_SDCARD_OUT;
                handler.sendMessageDelayed(message, 2000);
            }
        }
    };
    public static boolean isServerStart = false;

    @Override
    public void onCreate() {
        super.onCreate();
        if (APP_TYPE == APP_TYPE_JIANGJUN_YUNCHENG) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationChannel channel = manager.getNotificationChannel(getPackageName());
                if (channel == null) {
                    channel = new NotificationChannel(getPackageName(), "etv", NotificationManager.IMPORTANCE_HIGH);
                    manager.createNotificationChannel(channel);
                }
                Notification notification = new NotificationCompat.Builder(this, channel.getId())
                        .setSmallIcon(R.mipmap.radio_chooice)
                        .setAutoCancel(true)
                        .build();
                startForeground(1, notification);
            }
        }
        initUdp();
        initParsener();
        initReceiver();
        initTimeClick();
        Message msgm = new Message();
        msgm.what = SET_SYSTEM_TIME;
        handler.sendMessageDelayed(msgm, 2000);
    }

    private void initTimeClick() {
        TimerDealUtil.getInstance().startToTimerMinRxJava();
    }

    /***
     * @param isBackOn
     * true  亮屏
     * false 息屏
     */
    private void toDoLightChangeInfo(boolean isBackOn) {
        if (isBackOn) {
            //亮屏
            MainActivity.IS_ORDER_REQUEST_TASK = true;
            gotoActivity(MainActivity.class);
        } else {
            //息屏
            if (InterestActivity.isMainView) {
                return;
            }
            gotoActivity(InterestActivity.class);
        }
    }

    private void gotoActivity(Class<? extends Activity> activity) {
        Intent intentStop = new Intent(getBaseContext(), activity);
        intentStop.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intentStop);
    }

    EtvParsener etvParsener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServerStart = true;
        return super.onStartCommand(intent, flags, startId);
    }

    UdpParnsener udpParsener = null;

    private void initUdp() {
        if (udpParsener == null) {
            udpParsener = new UdpParnsener(EtvService.this);
            MyLog.udp("========parsenerJsonInfo== isTrue" + EtvService.this);
            udpParsener.receiveUdpMessage();
        }
    }


    /**
     * 发送Udp消息
     *
     * @param message
     * @param sendIp
     */
    public void sendUdpMessage(String message, String sendIp) {
        if (udpParsener == null) {
            initUdp();
        }
        udpParsener.sendUdpMessage(message, sendIp);
    }

    /***
     * 初始化相应的属性
     */
    private void initParsener() {
        if (etvParsener == null) {
            etvParsener = new EtvParsener(EtvService.this);
        }
    }

    private static final int SEARCH_MESSAGE_SDCARD_IN = 2345;
    private static final int SEARCH_MESSAGE_SDCARD_OUT = 2346;
    private static final int UPDATE_INFO = 2347;
    private static final int SET_SYSTEM_TIME = 2348;
    Boolean isOk = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler.removeMessages(msg.what);
            switch (msg.what) {
                case UPDATE_INFO:  //自动检测升级
                    MyLog.cdl("=======软件启动检测升级功能====", true);
                    checkAppUpdate();
                    break;
                case SEARCH_MESSAGE_SDCARD_IN:   //SD in
                    initParsener();
                    ProjectorUtil.setProjectorSavePath(EtvService.this, "外置存储设备插入");
                    String path = (String) msg.obj;
                    initParsener();
                    MyLog.usb("=======准备读取USB 内容====" + path);
                    etvParsener.SDorUSBcheckIn(EtvService.this, path);
                    break;
                case SEARCH_MESSAGE_SDCARD_OUT:  //SD out
                    initParsener();
                    ProjectorUtil.setProjectorSavePath(EtvService.this, "外置存储设备拔出");
                    break;
                case SET_SYSTEM_TIME:
                    if (EtvParsener.isDealTime) {
                        return;
                    }
                    initParsener();
                    //获取系统时间
                    etvParsener.checkSytemTimeFromWeb(EtvService.this);

                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (receiverEtv != null) {
            unregisterReceiver(receiverEtv);
        }
        if (receiverSdUsb != null) {
            unregisterReceiver(receiverSdUsb);
        }
        TimerDealUtil.getInstance().onDestroyTimer();
        LightUtil.StopLightUtil();
        if (udpParsener != null) {
            udpParsener.stopReceiveMessage();
        }
    }

    private void initReceiver() {
        IntentFilter fileter = new IntentFilter();
        fileter.addAction(AppInfo.RECEIVE_BROAD_CAST_LIVE);               //守护进程广播
        fileter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);   //网络状态改变
        fileter.addAction(AppInfo.SOCKET_LINE_STATUS_CHANGE);  //服务器连接状态广播
        fileter.addAction(UPDATE_STA_TOTAL_TO_WEB);
        fileter.addAction(AppInfo.RED_LINE_LISTENER_IN);
        fileter.addAction(AppInfo.RED_LINE_LISTENER_OUT);
        registerReceiver(receiverEtv, fileter);

        IntentFilter fileterSd = new IntentFilter();
        fileterSd.addAction(Intent.ACTION_MEDIA_MOUNTED);
        fileterSd.addAction(Intent.ACTION_MEDIA_EJECT);
        fileterSd.addDataScheme("file");
        registerReceiver(receiverSdUsb, fileterSd);
    }

    /***
     *  检测软件升级
     */
    private void checkAppUpdate() {
        int workModel = SharedPerManager.getWorkModel();
        if (workModel != AppInfo.WORK_MODEL_NET) {
            MyLog.cdl("===单机模式");
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(EtvService.this)) {
            MyLog.cdl("===网络连接失败，不去检测升级任务");
            return;
        }
        sendBroadCastToViewToView(new Intent(TaskWorkService.UPDATE_APK_IMG_INFO));
    }

    /**
     * 发送广播给
     */
    public void sendBroadCastToViewToView(Intent intent) {
        try {
            sendBroadcast(intent);
        } catch (Exception e) {
            MyLog.cdl("EtvService发送广播异常: = " + e.toString(), true);
            e.printStackTrace();
        }
    }

    /***
     * 删除任务
     * 提交给服务器，
     * 删除本地文件
     *删除本地数据库
     */
    public void deleteEquipmentTaskById(String tag, String taskId) {
        initParsener();
        etvParsener.deleteEquipmentTaskById(tag, taskId);
    }

    /**
     * 上传进度给服务器
     *
     * @param taskId
     * @param progress int taskId, int progress, int downKb
     */
    public void updateProgressToWebRegister(String tag, String taskId, String totalNum, int progress, int downKb, String type) {
        initParsener();
        etvParsener.updateProgressToWebRegister(tag, taskId, totalNum, progress, downKb, type);
    }

    /***
     * 处理网络变化
     * @param context
     * @param intent
     */
    private void dealNetChange(Context context, Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        if (info == null) {
            MyLog.cdl("====获取网络对象===null。检测不熬网络变化了");
            return;
        }
        if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
            MyLog.cdl("===NetBroadCastReciver网络连接", true);
            AppStatuesListener.getInstance().NetChange.setValue(true);
            sendBroadCastToViewToView(new Intent(AppInfo.NET_ONLINE));
            String clVersion = CodeUtil.getSystCodeVersion(EtvService.this);
            updateDevInfoToAuthorServer(clVersion);
        } else {
            AppStatuesListener.getInstance().NetChange.setValue(false);
            MyLog.cdl("===NetBroadCastReciver网络断开", true);
            AppConfig.isOnline = false;
            sendBroadCastToViewToView(new Intent(AppInfo.NET_DISONLINE));
        }
    }

    /**
     * 添加统计信息到服务器
     */
    public void addPlayNumToWeb() {
        if (etvParsener != null) {
            etvParsener.addPlayNumToWeb();
        }
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
        if (etvParsener != null) {
            etvParsener.addFileNumToTotalOnTime(midId, addType, time, count);
        }
    }

    /**
     * 更新Apk，img的下载进度
     *
     * @param percent
     * @param downKb
     * @param fileName
     */
    public void updateDownApkImgProgress(String tag, int percent, int downKb, String fileName) {
        initParsener();
        etvParsener.updateDownApkImgProgress(percent, downKb, fileName, tag);
    }

    public void updateDevStatuesToWeb(Context context) {
        initParsener();
        etvParsener.updateDevStatuesToWeb(context);
    }

    /***
     * 提交设备信息到统计服务器
     */
    public void updateDevInfoToAuthorServer(String version) {
        initParsener();
        etvParsener.updateDevInfoToAuthorServer(version);
    }

    /**
     * 发送广播给
     *
     * @param action
     */
    public void sendBroadCastToView(String action) {
        if (!isServerStart) {
            MyLog.cdl("server not start,return ,sendBroadCastToView");
            return;
        }
        try {
            Intent intent = new Intent();
            intent.setAction(action);
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
