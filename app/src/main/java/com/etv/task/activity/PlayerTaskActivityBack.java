//package com.etv.task.activity;
//
//import android.content.BroadcastReceiver;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.ServiceConnection;
//import android.graphics.PixelFormat;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.AbsoluteLayout;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import androidx.lifecycle.Observer;
//
//import com.EtvApplication;
//import com.etv.activity.MainActivity;
//import com.etv.config.AppConfig;
//import com.etv.config.AppInfo;
//import com.etv.entity.RawSourceEntity;
//import com.etv.service.EtvService;
//import com.etv.service.TaskInsertTextService;
//import com.etv.service.TaskWorkService;
//import com.etv.task.entity.CpListEntity;
//import com.etv.task.entity.SceneEntity;
//import com.etv.task.parsener.PlayTaskParsener;
//import com.etv.task.view.PlayTaskView;
//import com.etv.util.APKUtil;
//import com.etv.util.MyLog;
//import com.etv.util.SharedPerManager;
//import com.etv.util.guardian.InstallApkBackUtil;
//import com.etv.util.permission.PermissionUtil;
//import com.etv.util.rxjava.AppStatuesListener;
//import com.etv.util.rxjava.RxLifecycle;
//import com.etv.util.rxjava.SchedulerTransformer;
//import com.ys.etv.R;
//import com.ys.model.dialog.MyToastView;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//import io.reactivex.Observable;
//import io.reactivex.functions.Consumer;
//import teaonly.rk.droidipcam.HdmiInterface;
//
///***
// * 用来播放网络任务的功能界面
// */
//public class PlayerTaskActivityBack extends TaskActivity implements PlayTaskView {
//
//    AbsoluteLayout view_abous;
//    PlayTaskParsener playTaskParsener;
//    //触摸，从别个界面回来的.需要恢复播放
//    private static final int ONRESUME_PLAY_VIEW = 2356;
//
//    private BroadcastReceiver receiverPlayTask = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            MyLog.playTask("=======播放界面广播===" + action);
//            if (action.equals(AppInfo.DOWN_TASK_SUCCESS)) {
//                //任务下载完成，准备刷新界面
//                updateViewInfo("======任务下载完毕，去布局绘制界面");
//            } else if (action.equals(Intent.ACTION_TIME_TICK)) {
//                if (!isPlayForst) {
//                    MyLog.playTask("播放界面不再前台,打断操作");
//                    return;
//                }
//                if (playTaskParsener != null) {
//                    playTaskParsener.updateWeatherView();
//                    playTaskParsener.updateMediaVoiceNum();
//                    //检查插播任务
//                    playTaskParsener.checkTextInsertTaskInfoByTime();
//                }
//            } else if (action.equals(AppInfo.MESSAGE_RECEIVE_SCREEN_CLOSE)) {
//                //停止任务自动检查
//                if (playTaskParsener != null) {
//                    playTaskParsener.shutDownDiffScreenLight();
//                }
//                MainActivity.IS_ORDER_REQUEST_TASK = false;
//                startToMainTaskView();
//                AppInfo.startCheckTaskTag = false;
//            } else if (action.equals(AppInfo.TURN_VOICE_ZREO)) {
//                //静音
//                if (playTaskParsener != null) {
//                    playTaskParsener.setMediaVoiceMute(true);
//                }
//            } else if (action.equals(AppInfo.TURN_VOICE_RESUME)) {
//                //恢复音量
//                if (playTaskParsener != null) {
//                    playTaskParsener.setMediaVoiceMute(false);
//                }
//            }
//        }
//    };
//
//    @Override
//    protected void onCreate(Bundle paramBundle) {
//        super.onCreate(paramBundle);
//        boolean isDevSpeed = SharedPerManager.getDevSpeedStatues();
//        if (isDevSpeed) {
//            getWindow().setFormat(PixelFormat.TRANSLUCENT);
//            //硬件加速
//            getWindow().setFlags(
//                    android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
//                    android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
//        }
//        setContentView(R.layout.activity_play_task);
//        initView();
//        initListener();
//        initTaskReceiver();
//    }
//
//    private void initListener() {
//        AppStatuesListener.getInstance().NetChange.observe(PlayerTaskActivityBack.this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean s) {
//                MyLog.message("=======onChanged==NetChange======" + s);
//                //网络已连接，通知需要刷新得节目，刷新一次
//                if (playTaskParsener != null && s) {
//                    playTaskParsener.updateGenWebViewRefresh(AppInfo.NET_ONLINE);
//                }
//            }
//        });
//
//        //这里需要更新声音
//        AppStatuesListener.getInstance().UpdateMainMediaVoiceEvent.observe(PlayerTaskActivityBack.this, new Observer<String>() {
//            @Override
//            public void onChanged(String s) {
//                MyLog.cdl("==========界面更新音量值大小========" + s);
//                //背景图下载完成，这里修改音量大小
//                if (playTaskParsener != null) {
//                    playTaskParsener.updateMediaVoiceNum();
//                    playTaskParsener.modifyLogoShowInfo("背景图下载完毕，更新logo");
//                }
//            }
//        });
//    }
//
//
//    @Override
//    public void checkSdStateFinish() {
//        startToMainTaskView();
//    }
//
//    @Override
//    public void stopOrderToPlay() {
//        startToMainTaskView();
//    }
//
//    //这里表示不去任务==null
//    @Override
//    public void getTaskInfoNull() {
//        MyLog.cdl("=======获取的节目==null==playActivity==");
//        startToMainTaskView();
//    }
//
//    ImageView iv_back_bgg;
//    RelativeLayout rela_down_tag;
//    TextView tv_down_desc;
//
//    private void initView() {
//        EtvApplication.getInstance().setSendIpList(null);  //清理一次数据
//        tv_down_desc = (TextView) findViewById(R.id.tv_down_desc);
//        rela_down_tag = (RelativeLayout) findViewById(R.id.rela_down_tag);
//        iv_back_bgg = (ImageView) findViewById(R.id.iv_back_bgg);
//        view_abous = (AbsoluteLayout) findViewById(R.id.view_abous);
//        playTaskParsener = new PlayTaskParsener(PlayerTaskActivityBack.this, this);
//        playTaskParsener.updateMediaVoiceNum();
//        updateViewInfo("onResume");
//    }
//
//    @Override
//    public void showDownStatuesView(boolean isShow, String desc) {
//        rela_down_tag.setVisibility(isShow ? View.VISIBLE : View.GONE);
//        tv_down_desc.setText(desc);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        bindTextInsertService();
//        JUMP_TO_VIEW = JUMP_DEFAULT;
//        TaskWorkService.isStartApk = false;  //startApk  归位
//        AppInfo.startCheckTaskTag = true;
//        if (playTaskParsener == null) {
//            return;
//        }
//        SceneEntity currentSencent = playTaskParsener.getCurrentSencenEntity();
//        Log.e("cdl", "=======currentSencent==" + (currentSencent == null));
//        if (currentSencent != null) {
//            playTaskParsener.resumePlayView();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        MyLog.playTask("========生命周期=======onPause=======");
//        if (playTaskParsener != null) {
//            playTaskParsener.pauseDisplayView();
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (mService != null) {
//            mService.ondestoryPopWindow();
//        }
//        unBindService();
//        isPlayForst = false;
//        MyLog.playTask("========生命周期==========onStop====000===" + JUMP_TO_VIEW + " / " + JUMP_TO_APK);
////        if (JUMP_TO_VIEW == JUMP_DEFAULT) {//什么都没做，直接退出
////            if (playTaskParsener != null) {
////                playTaskParsener.clearMemory();
////                playTaskParsener.clearLastView(PlayTaskParsener.TAG_CLEARVIEW_ONDESTORY);
////            }
////            return;
////        }
//        if (JUMP_TO_VIEW != JUMP_TO_APK) {
//            return;
//        }
//        if (playTaskParsener != null) {
//            playTaskParsener.clearMemory();
//            playTaskParsener.clearLastView(PlayTaskParsener.TAG_CLEARVIEW_ONDESTORY);
//        }
//    }
//
//    /**
//     * 跟新界面数据
//     *
//     * @param tag
//     */
//    private void updateViewInfo(String tag) {
//        if (playTaskParsener == null) {
//            return;
//        }
//        if (mService != null) {
//            mService.ondestoryPopWindow();
//        }
//        MyLog.playTask("===========刷新界面===" + tag + "  /时间==" + System.currentTimeMillis());
//        EtvService.getInstance().updateDevStatuesToWeb(PlayerTaskActivityBack.this);
//        playTaskParsener.getTaskToView(tag);           //获取数据
//    }
//
//    /**
//     * 加载WebView
//     *
//     * @param showWbeUrl
//     */
//    @Override
//    public void startViewWebActivty(String showWbeUrl, String backTime) {
//        if (playTaskParsener != null) {
//            playTaskParsener.pauseDisplayView();
//        }
//        JUMP_TO_VIEW = JUMP_TO_OTHER;
//        //url  触摸返回时间
//        Intent intent = new Intent(PlayerTaskActivityBack.this, ViewWebViewActivity.class);
//        intent.putExtra(ViewWebViewActivity.TAG_STR_WEB_VIEW, showWbeUrl);
//        intent.putExtra(ViewWebViewActivity.TAG_BACK_TIME, backTime);
//        startActivityForResult(intent, ONRESUME_PLAY_VIEW);
//    }
//
//    private static final int JUMP_DEFAULT = -1;
//    private static final int JUMP_TO_APK = 0;
//    private static final int JUMP_TO_OTHER = 1;
//    private int JUMP_TO_VIEW = JUMP_DEFAULT;
//
//    @Override
//    public void startApkView(final String coLinkAction, String backTime) {
//        MyLog.apk("======添砖APK===" + backTime + " / " + coLinkAction);
//        boolean isPermission = PermissionUtil.checkFloatPermission(PlayerTaskActivityBack.this);
//        MyLog.apk("判断是否有权限==" + isPermission);
//        if (!isPermission) {
//            PermissionUtil.openWinPopToast(PlayerTaskActivityBack.this);
//            return;
//        }
//        boolean isInstall = APKUtil.ApkState(PlayerTaskActivityBack.this, coLinkAction);
//        if (!isInstall) {
//            showToastView("跳转软件没有安装");
//            return;
//        }
//        JUMP_TO_VIEW = JUMP_TO_APK;
//        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_DAXIANG_TECH) {
//            //李凤睿---翔达智能
//            goToTaskApkBackActivity(coLinkAction);
//            return;
//        }
//        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_XINGMA) {
//            //星马
//            goToTaskApkBackActivity(coLinkAction);
//            return;
//        }
//        RawSourceEntity rawSourceEntity = InstallApkBackUtil.getResourceEntity();
//        if (rawSourceEntity == null) {  //不是7.1  3288的
//            goToTaskApkBackActivity(coLinkAction);
//            return;
//        }
//        int timeApk = getApkBackTime(backTime);
//        if (timeApk < AppConfig.APP_BACK_TIME_MIX) {
//            showToastView(getString(R.string.apk_back_time));
//            return;
//        }
//        //启动--APK
//        APKUtil.startApp(PlayerTaskActivityBack.this, coLinkAction);
//        //通知service开始计时
//        Intent intent = new Intent();
//        intent.setAction(TaskWorkService.BACK_OTHER_APK_TO_ETV_MAIN);
//        intent.putExtra(TaskWorkService.BACK_OTHER_APK_TO_ETV_MAIN, timeApk);
//        sendBroadcast(intent);
//    }
//
//    /**
//     * 跳转APK不返回
//     */
//    private void goToTaskApkBackActivity(String coLinkAction) {
//        Intent intentApk = new Intent();
//        intentApk.setClass(PlayerTaskActivityBack.this, TaskApkBackActivity.class);
//        intentApk.putExtra(TaskApkBackActivity.TAG_GO_TO_APK_PACKAGENAME, coLinkAction);
//        startActivity(intentApk);
//        finish();
//    }
//
//    private int getApkBackTime(String time) {
//        time = time.trim();
//        int appBackTime = 0;
//        try {
//            if (time == null || time.length() < 1) {
//                return 0;
//            }
//            appBackTime = Integer.parseInt(time);
//            if (appBackTime < 1) {  //没有设置返回时间，这里就不需要计时返回
//                return 0;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return appBackTime;
//    }
//
//    /**
//     * 全屏展示View
//     *
//     * @param cpListEntity
//     * @param list
//     */
//    @Override
//    public void toShowFullScreenView(CpListEntity cpListEntity, List<String> list, int clickPositon) {
//        if (cpListEntity == null) {
//            MyLog.touch("====展示全屏wei=null,不操作");
//            return;
//        }
//        if (list == null || list.size() < 1) {
//            MyLog.touch("====展示全屏list=null,不操作");
//            return;
//        }
//        String cpType = cpListEntity.getCoType(); //控件类型
//        MyLog.touch("====展示全屏w==" + cpType);
//        Intent intent = new Intent();
//        switch (cpType) {
//            case AppInfo.VIEW_IMAGE: //图片
//                intent.setClass(PlayerTaskActivityBack.this, TaskImageActivity.class);
//                intent.putExtra("clickPositon", clickPositon);
//                intent.putStringArrayListExtra(TaskImageActivity.TAG_RECEIVE_MESSAGE, (ArrayList<String>) list);
//                break;
//            case AppInfo.VIEW_VIDEO: //视频
//                intent.setClass(PlayerTaskActivityBack.this, TaskVideoActivity.class);
//                intent.putStringArrayListExtra(TaskVideoActivity.TAG_RECEIVE_MESSAGE_VIDEO, (ArrayList<String>) list);
//                break;
//            case AppInfo.VIEW_IMAGE_VIDEO: //混播
//                intent.setClass(PlayerTaskActivityBack.this, PlayImaVideoActivity.class);
//                intent.putStringArrayListExtra(PlayImaVideoActivity.TAG_RECEIVE_MESSAGE, (ArrayList<String>) list);
//                break;
//        }
//        JUMP_TO_VIEW = JUMP_TO_OTHER;
//        startActivityForResult(intent, ONRESUME_PLAY_VIEW);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        MyLog.playTask("========生命周期==========界面回来了====" + resultCode);
//        if (resultCode != RESULT_OK) {
//            return;
//        }
//        if (requestCode == ONRESUME_PLAY_VIEW) {
//            if (playTaskParsener != null) {
//                playTaskParsener.resumePlayView();
//            }
//        }
//    }
//
//    /**
//     * 所有的任务已经播放完毕，
//     * 去检查新的播放任务
//     */
//    @Override
//    public void findTaskNew() {
//        MyLog.cdl("=========TaskCacheActivity====findTaskNew=任务播放完毕了===，去默认播放界面");
//        MainActivity.IS_ORDER_REQUEST_TASK = true;
//        Intent intent = new Intent(PlayerTaskActivityBack.this, MainActivity.class);
//        startActivity(intent);
//        finish();
//    }
//
//    /***
//     * 界面异常:直接停止界面
//     * @param errorInfo
//     */
//    @Override
//    public void showViewError(String errorInfo) {
//        MyLog.playTask("===========showViewError==" + errorInfo);
//        startToMainTaskView();
//    }
//
//    @Override
//    public void showToastView(String toast) {
//        MyToastView.getInstance().Toast(PlayerTaskActivityBack.this, toast);
//    }
//
//    @Override
//    public AbsoluteLayout getAbsoluteLayout() {
//        return view_abous;
//    }
//
//    @Override
//    public ImageView getBggImageView() {
//        return iv_back_bgg;
//    }
//
//    private void initTaskReceiver() {
//        IntentFilter fileter = new IntentFilter();
//        fileter.addAction(AppInfo.DOWN_TASK_SUCCESS);  //任务下载完毕，这里需要刷新界面
//        fileter.addAction(Intent.ACTION_TIME_TICK);  //时间变化得广播监听
//        fileter.addAction(AppInfo.MESSAGE_RECEIVE_SCREEN_CLOSE);  //息屏休眠得功能
//        fileter.addAction(AppInfo.TURN_VOICE_ZREO);  //静音
//        fileter.addAction(AppInfo.TURN_VOICE_RESUME);   //恢复音量
//        registerReceiver(receiverPlayTask, fileter);
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        MyLog.cdl("======onKeyDown========" + keyCode);
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            showBaseSettingDialogNew();
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
//            //快进 87
//            boolean isSameScreen = playTaskParsener.isLinkDoubleScreen();
//            if (!isSameScreen) {
//                return true;
//            }
//            playTaskParsener.moveViewForward(true);
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
//            //快退 88
//            boolean isSameScreen = playTaskParsener.isLinkDoubleScreen();
//            if (!isSameScreen) {
//                return true;
//            }
//            playTaskParsener.moveViewForward(false);
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
//            //暂停 86
//            boolean isSameScreen = playTaskParsener.isLinkDoubleScreen();
//            if (!isSameScreen) {
//                return true;
//            }
//            playTaskParsener.pauseDisplayView();
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
//            //播放 85
//            boolean isSameScreen = playTaskParsener.isLinkDoubleScreen();
//            if (!isSameScreen) {
//                return true;
//            }
//            playTaskParsener.resumePlayView();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    //用来监听触摸，跳转屏保场景的
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                //有按下动作时取消定时
//                break;
//            case MotionEvent.ACTION_UP:
//                //抬起时启动定时
//                if (playTaskParsener == null) {
//                    return true;
//                }
//                SceneEntity sceneEntity = getCurrentPlayScenEntity();
//                if (sceneEntity == null) {
//                    return true;
//                }
//                String pmType = sceneEntity.getPmType();
//                if (pmType == null || pmType.length() < 1) {
//                    return true;
//                }
//                if (pmType.contains(AppInfo.PROGRAM_TOUCH)) { //互动节目,才执行触摸返回
//                    playTaskParsener.autoJumpToNextSencentProjector();
//                }
//
//                break;
//        }
//        return super.dispatchTouchEvent(ev);
//    }
//
//    private SceneEntity getCurrentPlayScenEntity() {
//        SceneEntity sceneEntity = playTaskParsener.getCurrentSencenEntity();
//        if (sceneEntity == null) {
//            return null;
//        }
//        return sceneEntity;
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        try {
//            if (playTaskParsener != null) {
//                playTaskParsener.clearMemory();
//                playTaskParsener.clearLastView(PlayTaskParsener.TAG_CLEARVIEW_ONDESTORY);
//            }
//            MyLog.playTask("========生命周期==========onDestroy====");
//            EtvApplication.getInstance().setTaskWorkEntityList(null);
//            if (receiverPlayTask != null) {
//                unregisterReceiver(receiverPlayTask);
//            }
//            unBindService();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    //=======插播消息================================================================================
//    @Override
//    public void playInsertTextTaskToPopWindows(boolean isShow, CpListEntity cpListEntity) {
//        MyLog.cdl("=====playInsertTextTaskToPopWindows===" + isShow);
//        bindTextInsertService();
//        if (mService == null) {
//            MyLog.cdl("=====playInsertTextTaskToPopWindows==mService == null=");
//            return;
//        }
//        if (isShow) {
//            MyLog.cdl("=====playInsertTextTaskToPopWindows==mService == showPopWindows=");
//            mService.showPopWindows(cpListEntity);
//        } else {
//            MyLog.cdl("=====playInsertTextTaskToPopWindows==mService == ondestoryPopWindow=");
//            mService.ondestoryPopWindow();
//        }
//    }
//
//    private void bindTextInsertService() {
//        if (mService != null) {
//            return;
//        }
//        Intent intent = new Intent(PlayerTaskActivityBack.this, TaskInsertTextService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    private void unBindService() {
//        try {
//            if (hdmiInterface != null) {
//                unbindService(conn_hdmi);
//            }
//            if (mService != null) {
//                unbindService(mConnection);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    TaskInsertTextService mService;
//    private ServiceConnection mConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            MyLog.playTask("==========服务器连接成功");
//            TaskInsertTextService.TaskInsertBinder binder = (TaskInsertTextService.TaskInsertBinder) service;
//            mService = binder.getService();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            MyLog.playTask("==========服务器连接断开");
//            mService = null;
//        }
//    };
//
//    int hdmiX;
//    int hdmiY;
//    int hdmiWidth;
//    int hdmiHeight;
//
//    /**
//     * 加载HdmiIn的功能
//     */
//    @Override
//    public void showHdmInViewToActivity(int x, int y, int width, int height) {
//        Observable.timer(2, TimeUnit.SECONDS)
//                .compose(new SchedulerTransformer())
//                .compose(RxLifecycle.bindRxLifecycle(this))
//                .subscribe(new Consumer<Long>() {
//                    @Override
//                    public void accept(Long aLong) throws Exception {
//                        MyLog.i("main", "=====开始计时=倒计时完成=====");
//                        loadHdmiView(x, y, width, height);
//                    }
//                });
//    }
//
//    private void loadHdmiView(int x, int y, int width, int height) {
//        boolean isSuportHdmi = SharedPerManager.getIfHdmiInSuport();
//        if (!isSuportHdmi) {
//            showToastView(getString(R.string.hdmi_in_support_not));
//            return;
//        }
//        boolean isLinAidl = lineHdmInAidl();
//        if (!isLinAidl) {
//            showToastView("Line Hdmi_in Failed ");
//            return;
//        }
//        this.hdmiX = x;
//        this.hdmiY = y;
//        this.hdmiWidth = width;
//        this.hdmiHeight = height;
////        if (hdmiInterface == null) {
////            showToastView("Line Hdmi_in Failed : hdmiInterface is null");
////            return;
////        }
////        try {
////            hdmiInterface.showSuspensionwindow(x, y, width, height);
////        } catch (Exception e) {
////            MyLog.hdmi("加载Hdmin View error : " + e.toString(), true);
////            e.printStackTrace();
////        }
//    }
//
//
//    /**
//     * 隐藏HDMIiN的界面
//     */
//    @Override
//    public void dissHdmInViewToActivity() {
//        MyLog.hdmi("dissHdmInViewToActivity");
//        if (hdmiInterface == null) {
//            return;
//        }
//        try {
//            hdmiInterface.dissSuspensionwindow();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void toClickLongViewListener() {
//        showBaseSettingDialogNew();
//    }
//
//    /**
//     * 连接HDMI_IN的连接
//     *
//     * @return
//     */
//    private boolean lineHdmInAidl() {
//        String packageName = AppInfo.HDMI_IN_PACKAGE_NAME;
//        boolean isApkInstall = APKUtil.ApkState(PlayerTaskActivityBack.this, packageName);
//        if (!isApkInstall) {
//            showToastView("HDMI_IN not install");
//            return false;
//        }
//        try {
//            Intent intent = new Intent();
//            intent.setComponent(new ComponentName(packageName, AppInfo.HDMI_IN_SERVICE_NAME));
//            bindService(intent, conn_hdmi, Context.BIND_AUTO_CREATE);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//
//    HdmiInterface hdmiInterface;
//    private ServiceConnection conn_hdmi = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className, IBinder binder) {
//            Log.e("cdl", "====服务器连接=======");
//            hdmiInterface = HdmiInterface.Stub.asInterface(binder);
//            if (hdmiInterface == null) {
//                showToastView("Line Hdmi_in Failed : hdmiInterface is null");
//                return;
//            }
//            try {
//                hdmiInterface.showSuspensionwindow(hdmiX, hdmiY, hdmiWidth, hdmiHeight);
//            } catch (Exception e) {
//                MyLog.hdmi("加载Hdmin View error : " + e.toString(), true);
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            Log.e("cdl", "====服务器断开======");
//            hdmiInterface = null;
//        }
//    };
//
//    //判断播放界面是否在前台
//    public static boolean isPlayForst = false;
//
//    @Override
//    protected void onPostResume() {
//        super.onPostResume();
//        isPlayForst = true;
//    }
//
//
//}
