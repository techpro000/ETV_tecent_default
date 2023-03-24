//package com.etv.service;
//
//import android.app.Activity;
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Message;
//
//import androidx.annotation.NonNull;
//
//import com.EtvApplication;
//import com.etv.activity.ClearCacheActivity;
//import com.etv.activity.MainActivity;
//import com.etv.activity.model.RegisterDevListener;
//import com.etv.config.ApiInfo;
//import com.etv.config.AppConfig;
//import com.etv.config.AppInfo;
//import com.etv.db.DbBggImageUtil;
//import com.etv.db.DbStatiscs;
//import com.etv.service.listener.SysSettingAllViewInfoListener;
//import com.etv.service.parsener.MapLocationParsener;
//import com.etv.service.parsener.TcpParsener;
//import com.etv.service.view.TcpHartStatuesListener;
//import com.etv.service.view.TcpPowerOnOffListener;
//import com.etv.setting.InterestActivity;
//import com.etv.socket.mine.MessageSocketListener;
//import com.etv.socket.mine.SocketStatuesListener;
//import com.etv.socket.mine.SocketUtil;
//import com.etv.socket.online.SocketWebListener;
//import com.etv.task.db.DBTaskUtil;
//import com.etv.udp.util.KeyControl;
//import com.etv.util.Biantai;
//import com.etv.util.FileUtil;
//import com.etv.util.MyLog;
//import com.etv.util.NetWorkUtils;
//import com.etv.util.ScreenUtil;
//import com.etv.util.SharedPerManager;
//import com.etv.util.SharedPerUtil;
//import com.etv.util.SimpleDateUtil;
//import com.etv.util.poweronoff.PowerOnOffManager;
//import com.etv.util.poweronoff.db.PowerDbManager;
//import com.etv.util.rxjava.AppStatuesListener;
//import com.etv.util.system.SystemManagerInstance;
//import com.etv.util.system.SystemManagerUtil;
//import com.etv.util.system.VoiceManager;
//import com.ys.model.dialog.MyToastView;
//
//import org.json.JSONObject;
//
//import java.util.Random;
//import java.util.concurrent.TimeUnit;
//
//import io.reactivex.Observable;
//import io.reactivex.Observer;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.Disposable;
//
//
//public class TcpSocketService_Back extends Service implements SocketStatuesListener {
//
//    public static TcpSocketService_Back instance;
//
//    public static TcpSocketService_Back getInstance() {
//        if (instance == null) {
//            synchronized (TcpSocketService_Back.class) {
//                if (instance == null) {
//                    instance = new TcpSocketService_Back();
//                }
//            }
//        }
//        return instance;
//    }
//
//    private BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            MyLog.e("cdl", "==Tcp广播==action==" + action);
//            if (action.equals(AppInfo.NET_ONLINE)) {
//                MyLog.cdl("==网络连接==取自动连接socket", true);
//                getDevHartStateInfo(1, "网络连接==取自动连接socket");
//            } else if (action.equals(AppInfo.NET_DISONLINE)) {
//                //网络断开，下线
//                MyLog.cdl("==网络断开==下线", true);
//                AppConfig.isOnline = false;
//                dealDisOnlineDev("网络断开，这里也执行断开得操作", false);
//                sendBroadCastToView(AppInfo.STOP_DOWN_TASK_RECEIVER);      //网路断开停止下载
//            } else if (action.equals(Intent.ACTION_TIME_TICK)) {  //时间变化
//                MyLog.cdl("0000时间到了====TcpSockectService");
//                int currentTime = SimpleDateUtil.getHourMin();
//                MyLog.cdl("=======当前时间==" + currentTime);
//                if (currentTime == 235 && SharedPerManager.getAutoRebootDev()) {
//                    MyLog.sleep("=======当前时间=235 设备准时重启软件=", true);
//                    SystemManagerInstance.getInstance(TcpSocketService_Back.this).rebootDev();
//                }
//                startLocationService(1);
//                int numPlus = new Random().nextInt(3000);
//                handler.sendEmptyMessageDelayed(TIMER_ON_NOW_CHECK_SDCARD, 2000 + numPlus); //延迟5秒，因为TaskService中有准时刷新任务
//            } else if (action.equals(AppInfo.SEND_IMAGE_CAPTURE_SUCCESS)) {  //收到截图通知
//                MyLog.update("==截图回来了====");
//                updateImageToWeb(intent);
//            }
//        }
//    };
//
//    //判断server是否启动
//    private boolean isServerStart = false;
//    //当前是否接收到消息
//    private boolean isOnlineCheck = false;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
//            return;
//        }
//        instance = this;
//        initOther();
//        initReceiver();
//        autoSendNullMessageToWeb();
//        handler.sendEmptyMessageDelayed(TIMER_ON_NOW_HEART_TO_WEB, AppConfig.TIME_TO_HART_TO_WEB());
//    }
//
//    Disposable mDisposable;
//
//    private void autoSendNullMessageToWeb() {
//        Observable.interval(AppConfig.MESSAGE_AUTO_SEND_SOCKET, TimeUnit.MILLISECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<Long>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable disposable) {
//                        mDisposable = disposable;
//                    }
//
//                    @Override
//                    public void onNext(@NonNull Long number) {
//                        SocketUtil.getInstance().sendNullMessage();
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//    }
//
//    private static final int SEND_BROAD_TO_VIEW = 5645;    //定时开关机通知界面
//    private static final int CHECK_DEV_IS_ONLINE = 5646;  // 检查这杯是否在线
//    private static final int CHECK_POWER_ON_OFF = 5647;  // 检查定时开关机
//    private static final int UPDATE_SYSTEM_INFO = 5648;  // 同步系统设置信息
//    private static final int STOP_PLAY_TO_MIAN = 5649;
//    private static final int GET_REQUEST_TASK_FROM_WEB = 5650;  //获取任务
//    private static final int CLEAT_SD_CACHE_INFO = 5651;        //清理磁盘
//    private static final int GO_TO_ACTIVITY = 5652;
//    private static final int SHOW_TOAST_VIEW = 5653;
//    private static final int MONITOR_VIEW_TO_WEB = 5654;        //提交屏幕截图到服务器
//    private static final int STOP_DOWN_TASK_AND_CLEAR = 5655;  //清理任务
//    private static final int UPDATE_APK_IMG_INFO = 5656;  //升级APK，固件信息
//    private static final int STOP_TASK_DOWN_INFO = 5657;  //请求任务前把正在下载的中止掉，防止多次重复下载
//    private static final int TIMER_ON_NOW_CHECK_SDCARD = 5658;  //定时到了，检测SD卡信息内存
//    private static final int TIMER_ON_NOW_HEART_TO_WEB = 5659;  //定时到了，检测心跳
//    private static final int UPDATE_DEV_NICK_NAME = 5660;  //定时到了，检测心跳
//    private static final int UPDATE_LOCATION_FROM_WEB = 5661;  //服务器修改定位信息，这里去同步
//    private static final int SYSTEM_CLOSE_SCREEM = 5662;  //休眠
//    private static final int SYSTEM_OPEN_SCREEM = 5663;  //唤醒
//    private static final int CHECK_BGG_IMAGE_STATUES = 5665;  //检测背景图得状态
//    private static final int CHANGE_DEVICE_TO_OTHER = 5666;  //修改设备归属
//    //    private static final int CHANGE_BGG_UPDATE_VIEW = 5667;  //修改背景，延时去通知界面
//    private static final int SOCKET_LINE_SUCCESS_UPDATE_TASK_INFO = 5668;  //服务器连接成功,同步任务信息
//    private static final int MESSAGE_TTS_SPESK = 5669;  //语音TTS
//    private static final int MESSAGE_GET_ALL_DEV_SET_INFO = 5670;  //统一获取所有得设备设置接口
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            handler.removeMessages(msg.what);
//            switch (msg.what) {
//                case MESSAGE_GET_ALL_DEV_SET_INFO:
//                    //获取所有得设备信息
//                    getSytemDevSettingAllInfo();
//                    break;
//                case MESSAGE_TTS_SPESK: //语音TTS
//                    String tts = (String) msg.obj;
//                    MyLog.message("====语音TTS===" + tts);
//                    startToSpeak(tts);
//                    break;
//                case SEND_BROAD_TO_VIEW:  //发广播给界面
//                    AppStatuesListener.getInstance().objectLiveDate.postValue(AppStatuesListener.LIVE_DATA_POWERONOFF);
//
//                    break;
//                case SOCKET_LINE_SUCCESS_UPDATE_TASK_INFO:
//                    //延迟十秒去同步任务信息
//                    sendBroadCastToView(TaskWorkService.GET_TASK_FROM_WEB_NO_DOWN);
//                    break;
//                case CHANGE_DEVICE_TO_OTHER: //修改设备归属
//                    initOther();
//                    tcpParsener.updateDevInformation(TcpSocketService_Back.this, "changeUsername");
//                    break;
//                case CHECK_BGG_IMAGE_STATUES://背景图检测下载相关得
//                    //获取字体信息
//                    initOther();
//                    tcpParsener.getProjectFontInfoFromWeb("TcpSocketService 检测背景图 CHECK_BGG_IMAGE_STATUES");
//                    //检查背景图信息
//                    checkBggImageStatues();
//                    break;
//                case CHECK_POWER_ON_OFF:  //检查定时开关机
//                    dealPowernOff();
//                    break;
//                case UPDATE_SYSTEM_INFO:  //同步系统设置信息
//                    initOther();
//                    //获取后台设备信息
//                    tcpParsener.syncSytemInfoSetting();
//                    break;
//                case CLEAT_SD_CACHE_INFO:   //清理内存信息
//                    Intent intent = new Intent(TcpSocketService_Back.this, ClearCacheActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                    break;
//                case STOP_PLAY_TO_MIAN:  //停止播放，推出到主界面
//                    sendBroadCastToView(AppInfo.RECEIVE_STOP_PLAY_TO_VIEW);
//                    break;
//                case GO_TO_ACTIVITY:
//                    try {
//                        Class<? extends Activity> activity = (Class<? extends Activity>) msg.obj;
//                        Intent intentStop = new Intent(getBaseContext(), activity);
//                        intentStop.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        getApplication().startActivity(intentStop);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                case MONITOR_VIEW_TO_WEB:   //上传截图界面到VIEw
//                    ScreenUtil.getScreenImage(TcpSocketService_Back.this, AppInfo.TAG_UPDATE);
////                    initOther();
////                    tcpServerModule.monitorUpdateImage(getApplicationContext(),AppInfo.CAPTURE_PATH);
//                    break;
//                case SHOW_TOAST_VIEW:          //弹窗提示
//                    String toast = (String) msg.obj;
//                    MyToastView.getInstance().Toast(getApplicationContext(), toast);
//                    break;
//                case UPDATE_APK_IMG_INFO:  //升级固件，apk信息也需要停止下载任务
//                    sendBroadCastToView(TaskWorkService.UPDATE_APK_IMG_INFO);
//                    break;
//                case STOP_DOWN_TASK_AND_CLEAR:
//                    sendBroadCastToView(AppInfo.STOP_DOWN_TASK_RECEIVER);   // 清理下载任务
//                    MyLog.cdl("=========TaskCacheActivity====STOP_DOWN_TASK_AND_CLEAR");
//                    gotoActivity(MainActivity.class);
//                    break;
//                case STOP_TASK_DOWN_INFO:
//                    sendBroadCastToView(AppInfo.STOP_DOWN_TASK_RECEIVER); //请求任务前把正在下载的中止掉，防止多次重复下载
//                    handler.sendEmptyMessageDelayed(GET_REQUEST_TASK_FROM_WEB, 2000);
//                    break;
//                case GET_REQUEST_TASK_FROM_WEB: //准备请求任务
//                    if (!isServerStart) {
//                        MyLog.cdl("server not start,return");
//                        return;
//                    }
//                    MyLog.task("准备请求任务,发送广播");
//                    try {
//                        Intent intentTask = new Intent();
//                        intentTask.setAction(TaskWorkService.GET_TASK_FROM_WEB_TAG);
//                        intentTask.putExtra(TaskWorkService.GET_TASK_FROM_WEB_TAG, "==TCPsOCKETService 准备请求任务操作==");
//                        sendBroadcast(intentTask);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                case TIMER_ON_NOW_CHECK_SDCARD:  //定时检查SD卡内存状态
//                    String time = SimpleDateUtil.getCurrentHourMin();
//                    if (time.contains("0001")) { //每天凌晨的时候取刷新一次定时开关机
//                        dealPowernOff();
//                    }
//                    //检查SD卡剩余的内存
//                    if (tcpParsener != null) {
//                        tcpParsener.checkSystemSettingInfo();
//                    }
//                    break;
//                case UPDATE_DEV_NICK_NAME:
//                    initOther();
//                    tcpParsener.updateDevInformation(TcpSocketService_Back.this, "username");
//                    break;
//                case SYSTEM_CLOSE_SCREEM:  //息屏休眠
//                    sendBroadCastToView(AppInfo.MESSAGE_RECEIVE_SCREEN_CLOSE);  //告诉播放界面需要息屏，关闭副屏界面
//                    gotoActivity(InterestActivity.class);
//                    break;
//                case UPDATE_LOCATION_FROM_WEB:   //服务器修改定位信息，这里去同步
//                    initOther();
//                    tcpParsener.updateDevInformation(TcpSocketService_Back.this, "location");
//                    break;
//                case SYSTEM_OPEN_SCREEM: //唤醒
//                    SystemManagerInstance.getInstance(TcpSocketService_Back.this).turnBackLightTtatues(true);
//                    MainActivity.IS_ORDER_REQUEST_TASK = true;
//                    SharedPerManager.setSleepStatues(false);
//                    gotoActivity(MainActivity.class);
//                    break;
//                case TIMER_ON_NOW_HEART_TO_WEB:   //心跳包,连接服务器
//                    getDevHartStateInfo(1, "时间到了，定时去检查");
//                    handler.sendEmptyMessageDelayed(TIMER_ON_NOW_HEART_TO_WEB, AppConfig.TIME_TO_HART_TO_WEB());
//                    break;
//                case CHECK_DEV_IS_ONLINE: //检查设备是否在线
//                    MyLog.netty("=心跳延时时间到了==" + isOnlineCheck, true);
//                    if (isOnlineCheck) {
//                        //在线
//                        addSocketNum = 0;
//                        AppConfig.isOnline = true;
//                        return;
//                    }
//                    //下面是没有心跳反馈，加的判断信息
//                    addSocketNum++;
//                    if (addSocketNum > 5) {
//                        addSocketNum = 0;
//                        AppConfig.isOnline = false;
//                        lineSockeyWebServer("时间到了,10次心跳无反应，设备不在线，这里去连接服务器");
//                        return;
//                    }
//                    boolean isConnect = SocketUtil.getInstance().getLineSatues();
//                    MyLog.netty("=时间到了,设备是否在线==" + isOnlineCheck + "/ " + isConnect, true);
//                    if (isConnect) {
//                        SocketUtil.getInstance().sendHeartMessage("心跳消息超时，这里再去检查一遍=" + addSocketNum, null);
//                    } else {
//                        //设备不在线，重新去登录
//                        AppConfig.isOnline = false;
//                        lineSockeyWebServer("消息时间返回，设备不在线，这里去连接服务器");
//                    }
//                    break;
//            }
//        }
//    };
//
//
//    int addSocketNum = 0;
//
//
//    /****
//     *     //内存信息
//     *     //定时开关机
//     *     //字体
//     *     //同步系统时间
//     *     //背景图-logo-区间音量
//     *     //系统设置
//     */
//    private void getSytemDevSettingAllInfo() {
//        initOther();
//        tcpParsener.getSystemSettingAllInfo(new SysSettingAllViewInfoListener() {
//
//            @Override
//            public void clearTimer() {
//                MyLog.powerOnOff("=======getSytemDevSettingAllInfo===clearTimer===");
//                clearTimerPowerOnOff("getSytemDevSettingAllInfo 服务器没有数据");
//            }
//
//            @Override
//            public void updateTime() {
//                MyLog.powerOnOff("=======getSytemDevSettingAllInfo==updateTime====");
//                handler.sendEmptyMessageDelayed(SEND_BROAD_TO_VIEW, 2000);
//            }
//        });
//    }
//
//    /***
//     * 处理定时开关机代码
//     */
//    public void dealPowernOff() {
//        initOther();
//        tcpParsener.dealPowernOff(new TcpPowerOnOffListener() {
//            @Override
//            public void clearTimer() {
//                clearTimerPowerOnOff("服务器没有数据");
//            }
//
//            @Override
//            public void updateTime() {
//                handler.sendEmptyMessageDelayed(SEND_BROAD_TO_VIEW, 2000);
//            }
//        });
//    }
//
//    /***===========================================================================================================
//     * int tag_hart
//     * 1：  表示心跳，根据标志位来判断是否要请求设备信息
//     * -1： 表示连接服务器，已经注册成功了额，不用请求服务器设备信息
//     * 用来区别是注册还是心跳
//     */
//    public void getDevHartStateInfo(int tag_hart, String desc) {
//        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
//            return;
//        }
//        initOther();
//        tcpParsener.getDevHartStateInfo(tag_hart, desc, new TcpHartStatuesListener() {
//            @Override
//            public void sendHeartMessage(String message) {
//                MyLog.netty("=service=message==" + message, true);
//                sendSocketHeartMessageToWeb();
//            }
//
//            @Override
//            public void registerDev(String message) {
//                //未注册，这里去注册
//                registerDevToWeb(message);
//            }
//        });
//    }
//
//    /***
//     * 发心跳消息
//     */
//    public void sendSocketHeartMessageToWeb() {
//        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
//            return;
//        }
//        SocketUtil.getInstance().sendHeartMessage("自动发送心跳消息", new MessageSocketListener() {
//            @Override
//            public void messageSendFailed(boolean isSuccess, String errorDesc) {
////                MyLog.netty("=====心跳消息发送状态========" + isSuccess + " / " + errorDesc);
//                if (isSuccess) {
//                    return;
//                }
//                sendBroadToUi(AppInfo.SOCKET_LINE_STATUS_CHANGE, SocketWebListener.SOCKET_ERROR, "Send Socket Message Error：" + errorDesc, null);
//            }
//        });
//        isOnlineCheck = false;
//        handler.sendEmptyMessageDelayed(CHECK_DEV_IS_ONLINE, AppConfig.TIME_TO_HART_MORE_TIME()); // 5秒没有返回消息，表示不在线
//    }
//
//    public void registerDevToWeb(String tag) {
//        MyLog.netty("=====registerDevToWeb========" + tag, true);
//        if (Biantai.isTwoClick()) {
//            return;
//        }
//        String UserName = SharedPerManager.getUserName();
//        if (UserName == null || UserName.length() < 2) {
//            return;
//        }
//        if (UserName.contains("Null")) {
//            return;
//        }
//        registerDev(TcpSocketService_Back.this, UserName, new RegisterDevListener() {
//
//            @Override
//            public void registerDevState(boolean isSuccess, String errorrDesc, int code) {
//                MyLog.message("===注册设备返回==" + isSuccess + "/ " + errorrDesc, true);
//                AppInfo.isDevRegister = isSuccess;
//                if (!isSuccess) {
//                    sendBroadToUi(AppInfo.SOCKET_LINE_STATUS_CHANGE, SocketWebListener.SOCKET_ERROR,
//                            "Registration Failed: " + errorrDesc, errorrDesc);
//                    return;
//                }
//                sendBroadToUi(AppInfo.SOCKET_LINE_STATUS_CHANGE, SocketWebListener.SOCKET_ERROR, "Registration successful, Ready to connect", null);
//                lineSocketWeb();
//            }
//        });
//    }
//
//
//    /**
//     * 老版本得，暂时不删除
//     */
//    public void lineSocketWeb() {
//        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
//            return;
//        }
//        SocketUtil.getInstance().ondestroySocket("准备连接前，先Close以前的");
//        MyLog.netty("===连接Socket服务器中...", true);
//        try {
//            sendBroadToUi(AppInfo.SOCKET_LINE_STATUS_CHANGE, SocketWebListener.SOCKET_ERROR, "Lining Server...", null);
//            MyLog.netty("定时检查状态==正在连接 Socket 服务器", true);
//            SocketUtil.getInstance().initSocketAndReceiveMessage(this);
//        } catch (Exception e) {
//            sendBroadToUi(AppInfo.SOCKET_LINE_STATUS_CHANGE, SocketWebListener.SOCKET_ERROR, "Line Server Error :" + e.toString(), e.toString());
//            MyLog.netty(e.toString(), true);
//        }
//    }
//
//    /***
//     * 这里准备 socket 连接
//     */
//    public void lineSockeyWebServer(String printTag) {
//        if (!isServerStart) {
//            MyLog.cdl("Server 未启动，不去链接服务器==");
//            return;
//        }
//        if (!NetWorkUtils.isNetworkConnected(getBaseContext())) {
//            sendBroadToUi(AppInfo.SOCKET_LINE_STATUS_CHANGE, SocketWebListener.SOCKET_ERROR, "NetWork Error ,Please Check", null);
//            MyLog.message("没有网络，不去连接服务器");
//            return;
//        }
//        if (AppConfig.isOnline) {
//            MyLog.message("=====当前在线，打断操作========");
//            return;
//        }
//        registerDevToWeb("===心跳==去连接服务器====");
//    }
//
//    /***
//     * 断开重连
//     */
//    public void dissOrReconnect() {
//        try {
//            MyLog.message("执行断开重连的方法", true);
//            dealDisOnlineDev("断开重连", false);
//            AppConfig.isOnline = false;
//            //在这里初始化
//            lineSockeyWebServer("断开重连机制");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /***
//     * 注销登陆
//     * 2：取消hanlder活动
//     * 3:下线
//     */
//    public void dealDisOnlineDev(String tag, boolean isSetNull) {
//        MyLog.message("中断和服务器得连接===" + tag, true);
//        try {
//            AppConfig.isOnline = false;
//            SocketUtil.getInstance().ondestroySocket("连接服务器界面，手动断开，重连");
//            sendBroadToUi(AppInfo.SOCKET_LINE_STATUS_CHANGE, -2, "The connection is broken, attempt to reconnect", null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /***
//     * 服务器连接状态
//     */
//    @Override
//    public void backLineStaues(boolean isLineSuccess, String errorDesc) {
//        MyLog.netty("===service 设备Socket 连接状态 = " + errorDesc, true);
//        if (!isLineSuccess) {
//            sendBroadToUi(AppInfo.SOCKET_LINE_STATUS_CHANGE, SocketWebListener.SOCKET_ERROR, "Failed : " + errorDesc, null);
//            return;
//        }
//        AppConfig.isOnline = true;
//        sendBroadToUi(AppInfo.SOCKET_LINE_STATUS_CHANGE, SocketWebListener.SOCKET_OPEN, "Success : ", null);
//        handler.sendEmptyMessage(MESSAGE_GET_ALL_DEV_SET_INFO);
//        handler.sendEmptyMessageDelayed(SOCKET_LINE_SUCCESS_UPDATE_TASK_INFO, 10 * 1000);
//    }
//
//    //心跳服务器返回的消息
//    @Override
//    public void heartMessageBack() {
//        isOnlineCheck = true;
//    }
//
//    /***
//     * 接受消息回调
//     * 里面运行在子线程
//     * @param message
//     */
//    @Override
//    public void receiverMessage(String message) {
//        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
//            return;
//        }
//        isOnlineCheck = true;
//        AppConfig.isOnline = true;
//        MyLog.message("======接收到消息webSocket ：" + message, true);
//        try {
//            if (!message.contains("type")) {
//                return;
//            }
//            JSONObject jsonObject = new JSONObject(message);
//            int type = jsonObject.getInt("type");
//            if (type == AppInfo.MESSAGE_TYPE_ORDER_WEB) {  //简单的指令操作
//                dealOrderMessage(message);
//            } else if (type == AppInfo.MESSAGE_TYPE_SOCKET_DEV) {   //服务器端发送指令
//                dealSysCodeMessage(message);
//            } else if (type == AppInfo.MESSAGE_TYPE_WINCHAT_SOFT) { //小程序指令
//                int code = jsonObject.getInt("code");
//                KeyControl.sendCodeToDev(TcpSocketService_Back.this, code);
//            } else if (type == AppInfo.MESSAGE_TYPE_CUSTOM_MADE) {
//                //  客户定制--方案
//                int code = jsonObject.getInt("code");
//                if (code == AppInfo.ORDER_TTS_MESSAGE) {
//                    String messageTTS = jsonObject.getString("data");
//                    Message msgtts = new Message();
//                    msgtts.what = MESSAGE_TTS_SPESK;
//                    msgtts.obj = messageTTS;
//                    handler.sendMessage(msgtts);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 处理服务器下发得系统消息
//     *
//     * @param message
//     */
//    private void dealSysCodeMessage(String message) {
//        try {
//            JSONObject jsonObject = new JSONObject(message);
//            int code = jsonObject.getInt("code");
//            switch (code) {
//                case AppInfo.SERVER_BACK_DEV_IS_ONLINE:
//                    isOnlineCheck = true;
//                    break;
//                case AppInfo.WET_SCAN_REGISTER_DEV_WEB:
//                    String userName = jsonObject.getString("userName");
//                    SharedPerManager.setUserName(userName, "微信扫码连接服务器");
//                    dealDisOnlineDev("微信扫码连接服务器", false);
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            registerDevToWeb("微信扫码连接服务器");
//                        }
//                    }, 1000);
//                    break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /***
//     * 处理服务器发过来的基本指令
//     * @param message
//     */
//    private void dealOrderMessage(String message) {
//        try {
//            JSONObject jsonObject = new JSONObject(message);
//            int code = jsonObject.getInt("code");
//            if (message.contains("id")) { //过滤掉 指令 ID
//                String messageId = jsonObject.getString("id");
//                byte[] msgOrderBack = ApiInfo.getOrderMessageBack(messageId);
//                SocketUtil.getInstance().sendMessage(msgOrderBack, "收到指令，反馈给服务器", null);
//            }
//            switch (code) {
//                case AppInfo.ORDER_MODIFY_SERVER:
//                    String userName = jsonObject.optString("userName", SharedPerManager.getUserName());
//                    String ip = jsonObject.optString("ip", SharedPerUtil.getWebHostIpAddress());
//                    String port = jsonObject.optString("port", SharedPerUtil.getWebHostPort());
//                    SharedPerManager.setUserName(userName, "服务器修改IP服务器信息");
//                    SharedPerManager.setWebHost(ip);
//                    SharedPerManager.setWebPort(port);
//                    restartLineServer();
//                    break;
//                case AppInfo.ORDER_WEB_DEL_DEV:  //服务器删除设备
//                    PowerDbManager.clearTimeDb("指令清理定时开关机数据");
//                    DBTaskUtil.clearAllDbInfo("服务器删除设备");
//                    handler.sendEmptyMessageDelayed(STOP_PLAY_TO_MIAN, 1500);
//                    break;
//                case AppInfo.ORDER_UPDATE_LOCATION:  //更新设备定位信息
//                    handler.sendEmptyMessage(UPDATE_LOCATION_FROM_WEB);
//                    break;
//                case AppInfo.ORDER_PLAY: //播放
//                    if (!isServerStart) {
//                        MyLog.cdl("server not start,return ,AppInfo.ORDER_PLAY");
//                        return;
//                    }
//                    MyLog.message("=====service==null==" + (TcpSocketService_Back.this == null));
//                    MyLog.task("准备请求任务,发送广播");
//                    try {
//                        Intent intentTask = new Intent();
//                        intentTask.setAction(TaskWorkService.GET_TASK_FROM_WEB_TAG);
//                        intentTask.putExtra(TaskWorkService.GET_TASK_FROM_WEB_TAG, "==TCPSOCKETService 接收播放指令等操作==");
//                        sendBroadcast(intentTask);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                case AppInfo.ORDER_STOP: //停止
//                    handler.sendEmptyMessageDelayed(STOP_PLAY_TO_MIAN, 1500);
//                    break;
//                case AppInfo.ORDER_VOICE: //音量大小
//                    String data = jsonObject.getString("data");
//                    dealVoice(data);
//                    break;
//                case AppInfo.ORDER_CKEAR_SDCARD: //清理磁盘
//                    MyLog.message("==清理内存");
//                    if (TaskWorkService.TASK_CURRENT_TYPE != TaskWorkService.TASK_TYPE_DEFAULT) {
//                        //升级任务，任务正在下载，停止清理
//                        return;
//                    }
//                    sendBroadCastToView(AppInfo.RECEIVE_STOP_PLAY_TO_VIEW);
//                    clearSdcardInnerOut();
//                    break;
//                case AppInfo.ORDER_CLEAR_PROJECT:   //清理任务
//                    sendBroadCastToView(AppInfo.RECEIVE_STOP_PLAY_TO_VIEW);  //关闭播放界面
//                    sendBroadCastToView(AppInfo.STOP_DOWN_TASK_RECEIVER);  //停止下载所有的任务
//                    DBTaskUtil.clearAllDbInfo("   case AppInfo.ORDER_CLEAR_PROJECT:   //清理任务");
//                    String filePath = AppInfo.BASE_TASK_URL();
//                    FileUtil.deleteDirOrFilePath(filePath, "接受指令: ORDER_CLEAR_PROJECT");
//                    MyLog.message("==清理任务");
//                    handler.sendEmptyMessage(STOP_DOWN_TASK_AND_CLEAR);
//                    break;
//                case AppInfo.ORDER_REBOOT_DEV:   //重启设备
//                    SystemManagerInstance.getInstance(getApplicationContext()).rebootDev();
//                    break;
//                case AppInfo.ORDER_UPDATE:   //升级任务
//                    //加入有任务下载，需要停止下载任务
//                    handler.sendEmptyMessage(UPDATE_APK_IMG_INFO);
//                    break;
//                case AppInfo.ORDER_CLEAR_POWER_ON_OFF:   //定时开关机任务。唯一一个
//                    handler.sendEmptyMessage(CHECK_POWER_ON_OFF);
//                    break;
//                case AppInfo.ORDER_MONITOR: //监控
//                    handler.sendEmptyMessage(MONITOR_VIEW_TO_WEB);
//                    break;
//                case AppInfo.ORDER_WORK:   //接受获取任务的指令
//                    //接受新任务，直接停止下载，请求新任务
//                    //重新恢复工作模式
//                    SharedPerManager.setWorkModel(AppInfo.WORK_MODEL_NET, "接受服务器任务指令");
//                    handler.sendEmptyMessage(STOP_TASK_DOWN_INFO);  //停止下载，然后重新请求任务
//                    break;
//                case AppInfo.ORDER_CLEAR_TIMER_TASK: //清理定时开关机任务
//                    clearTimerPowerOnOff("服务器指令清理定时开关机数据");
//                    break;
//                case AppInfo.ORDER_SYSTEM_SETTING:  //去同步设置信息
//                    //停止下载，这里需要同步一次下载速度
//                    sendBroadCastToView(AppInfo.STOP_DOWN_TASK_RECEIVER);
//                    getSystemImfoFormWeb();
//                    //重新去拉任务
//                    handler.sendEmptyMessage(STOP_TASK_DOWN_INFO);  //停止下载，然后重新请求任务
//                    break;
//                case AppInfo.ORDER_CLEAR_UPDATE:    //清理升级任务
//                    sendBroadCastToView(AppInfo.MESSAGE_CLEAR_UPDATE_IMG_APK);
//                    break;
//                case AppInfo.UPDATE_NICK_NAME:  //更新设备昵称
//                    handler.sendEmptyMessage(UPDATE_DEV_NICK_NAME);
//                    break;
//                case AppInfo.ORDER_SHUT_DOWMN: //关机指令
//                    MyLog.message("======0000======准备关机=====");
//                    SystemManagerInstance.getInstance(TcpSocketService_Back.this).shoutDownDev();
//                    break;
//                case AppInfo.ORDER_TURN_OFF_BACKLIGHT:   //休眠
//                    DbStatiscs.clearNullData();  //清理烂数据统计
//                    MyLog.message("======0000======准备休眠=====");
//                    handler.sendEmptyMessage(SYSTEM_CLOSE_SCREEM);
//                    break;
//                case AppInfo.ORDER_TURN_ON_BACKLIGHT:    //唤醒
//                    MyLog.message("======0000======准备唤醒=====");
//                    handler.sendEmptyMessage(SYSTEM_OPEN_SCREEM);
//                    break;
//                case AppInfo.ORDER_MODIFY_BGG_IMAGE:  //修改背景图
//                    handler.sendEmptyMessage(CHECK_BGG_IMAGE_STATUES);
//                    break;
//                case AppInfo.ORDER_CLEAR_BGG_IMAGE:
//                    DbBggImageUtil.clearBggImageInfo();
//                    AppStatuesListener.getInstance().UpdateMainBggEvent.postValue(" TcpSocket 指令，清理壁纸");
//                    break;
//                case AppInfo.ORDER_CHANGE_DEV_TO_OTHER_USER:  //修改设备归属
//                    handler.sendEmptyMessage(CHANGE_DEVICE_TO_OTHER);
//                    break;
//                case AppInfo.ORDER_UPDATE_WORK_INFO_TO_WEB:  //上传日志到服务器
//                    updateFileWorkInfoToWeb();
//                    break;
//            }
//        } catch (Exception e) {
//            MyLog.message(e.toString(), true);
//            e.printStackTrace();
//        }
//    }
//
//    private void sendMessageToServer(String sendCodeToServer) {
//        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
//            return;
//        }
//    }
//
//
//    //===================================================================================================================
//
//    /**
//     * 发送广播给
//     *
//     * @param action
//     */
//    public void sendBroadCastToView(String action) {
//        if (!isServerStart) {
//            MyLog.cdl("server not start,return ,sendBroadCastToView");
//            return;
//        }
//        try {
//            Intent intent = new Intent();
//            intent.setAction(action);
//            EtvApplication.getContext().sendBroadcast(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /***
//     * 同步用户设置的基本信息
//     */
//    private void getSystemImfoFormWeb() {
//        handler.sendEmptyMessage(UPDATE_SYSTEM_INFO);
//    }
//
//
//    TcpParsener tcpParsener;
//
//    private void initOther() {
//        isServerStart = true;
//        if (tcpParsener == null) {
//            tcpParsener = new TcpParsener(TcpSocketService_Back.this);
//        }
//    }
//
//    private void initReceiver() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(AppInfo.NET_ONLINE);
//        filter.addAction(AppInfo.NET_DISONLINE);
//        filter.addAction(Intent.ACTION_TIME_TICK);
//        filter.addAction(AppInfo.SEND_IMAGE_CAPTURE_SUCCESS);
//        registerReceiver(receiver, filter);
//    }
//
//    /***
//     * 发送广播给界面更新
//     * @param socketLineStatusChange
//     * @param errorDesc
//     */
//    private void sendBroadToUi(String socketLineStatusChange, int status, String errorDesc, String appendDesc) {
//        if (!isServerStart) {
//            MyLog.cdl("server not start,return ,sendBroadToUi");
//            return;
//        }
//        String descBac = errorDesc;
//        try {
//            if (appendDesc != null) {
//                descBac = errorDesc + appendDesc;
//            } else {
//                descBac = errorDesc;
//            }
//            Intent intent = new Intent();
//            intent.setAction(socketLineStatusChange);
//            intent.putExtra(AppInfo.SOCKET_LINE_STATUS_CODE, status);
//            intent.putExtra(AppInfo.SOCKET_LINE_STATUS_CHANGE, descBac);
//            sendBroadcast(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
//        return START_STICKY;
//    }
//
//    /***
//     * 注册设备到服务器
//     * @param context
//     * @param userName
//     * @param listener
//     */
//    public void registerDev(Context context, String userName, RegisterDevListener listener) {
//        initOther();
//        tcpParsener.registerDev(context, userName, listener);
//    }
//
//    public void startToSpeak(String s) {
//        initOther();
//        tcpParsener.startToSpeak(s);
//    }
//
//    public void stopToSpeakMessage() {
//        initOther();
//        tcpParsener.stopToSpeakMessage();
//    }
//
//    //=============百度地图定位==============
//    public void startLocationService(int tag) {
//        MapLocationParsener.getInstance(TcpSocketService_Back.this).startLocationService(tag);
//    }
//
//    private void updateImageToWeb(Intent intent) {
//        try {
//            String tag = intent.getStringExtra("tag");
//            MyLog.update("==截图回来了==准备上传==" + tag);
//            initOther();
//            tcpParsener.updateImageToWeb(tag);
//        } catch (Exception e) {
//            MyLog.update("==截图回来了==上传异常==" + e.toString());
//            e.printStackTrace();
//        }
//    }
//
//    /***
//     * 检查背景图的样式
//     */
//    private void checkBggImageStatues() {
//        initOther();
//        tcpParsener.checkBggImageStatues();
//    }
//
//    /**
//     * 清理定时开关机
//     */
//    private void clearTimerPowerOnOff(String printTag) {
//        PowerDbManager.clearTimeDb(printTag);
//        PowerOnOffManager.getInstance().clearPowerOnOffTime("service clearTimerPowerOnOff");
//        handler.sendEmptyMessageDelayed(SEND_BROAD_TO_VIEW, 2500);
//    }
//
//    /***
//     * 清理所有的内存信息
//     */
//    private void clearSdcardInnerOut() {
//        handler.sendEmptyMessageDelayed(CLEAT_SD_CACHE_INFO, 2500);
//    }
//
//    /***
//     * 上传日志到服务器
//     */
//    private void updateFileWorkInfoToWeb() {
//        initOther();
//        tcpParsener.updateFileWorkInfoToWeb();
//    }
//
//
//    public void gotoActivity(Class<? extends Activity> activity) {
//        Message message = new Message();
//        message.what = GO_TO_ACTIVITY;
//        message.obj = activity;
//        handler.sendMessageDelayed(message, 1500);
//    }
//
//    /***
//     * 处理音量
//     * @param data
//     */
//    private void dealVoice(String data) {
//        try {
//            JSONObject jsonObject = new JSONObject(data);
//            String volNum = jsonObject.getString("volNum");
//            MyLog.message("====获取的音量值===" + volNum);
//            VoiceManager.getInstance(TcpSocketService_Back.this).repairDevVoice(volNum);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void showToastView(String toast) {
//        Message message = new Message();
//        message.what = SHOW_TOAST_VIEW;
//        message.obj = toast;
//        handler.sendMessage(message);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        SocketUtil.getInstance().ondestroySocket("onDestroy");
//        if (receiver != null) {
//            unregisterReceiver(receiver);
//        }
//        if (mDisposable != null && !mDisposable.isDisposed()) {
//            mDisposable.dispose();
//        }
//    }
//
//    /***
//     * 服务器切换IP用户名设置
//     */
//    private void restartLineServer() {
//        dealDisOnlineDev("手动点击连接，先断开，后重连", false);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                registerDevToWebOther();
//            }
//        }, 1000);
//    }
//
//    private void registerDevToWebOther() {
//        registerDev(TcpSocketService_Back.this, SharedPerManager.getUserName(), new RegisterDevListener() {
//
//            @Override
//            public void registerDevState(boolean isSuccess, String errorrDesc, int code) {
//                if (isSuccess) {  //注册success
//                    dissOrReconnect();
//                } else { //注册失败
//                    SystemManagerUtil.rebootApp(TcpSocketService_Back.this);
//                }
//            }
//        });
//    }
//
//}
