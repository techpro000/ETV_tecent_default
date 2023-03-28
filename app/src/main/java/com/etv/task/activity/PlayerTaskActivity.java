package com.etv.task.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Bundle;
 import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.EtvApplication;
import com.etv.activity.MainActivity;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.entity.RawSourceEntity;
import com.etv.service.EtvService;
import com.etv.service.TaskWorkService;
import com.etv.service.parsener.EtvParsener;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.parsener.PlayTaskParsener;
import com.etv.task.parsener.PlayTaskTriggerParsener;
import com.etv.task.view.PlayTaskView;
import com.etv.task.view.floatdoll.ViewInsertTextManager;
import com.etv.udp.util.KeyControl;
import com.etv.util.APKUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.guardian.InstallApkBackUtil;
import com.etv.util.permission.PermissionUtil;
import com.etv.util.rxjava.AppStatuesListener;
import com.etv.util.rxjava.RxLifecycle;
import com.etv.util.rxjava.SchedulerTransformer;
import com.etv.util.system.SystemManagerInstance;
import com.ys.etv.R;
import com.ys.etv.databinding.ActivityPlayTaskBinding;
import com.ys.model.dialog.MyToastView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import teaonly.rk.droidipcam.HdmiInterface;

/***
 * 用来播放网络任务的功能界面
 */
public class PlayerTaskActivity extends TaskActivity implements PlayTaskView {

    PlayTaskParsener playTaskParsener;
    //触摸，从别个界面回来的.需要恢复播放
    private static final int ONRESUME_PLAY_VIEW = 2356;



    private BroadcastReceiver receiverPlayTask = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MyLog.playTask("=======播放界面广播===" + action);
            if (action.equals(AppInfo.DOWN_TASK_SUCCESS)) {
                //任务下载完成，准备刷新界面

                updateViewInfo("======任务下载完毕，去布局绘制界面");
            } else if (action.equals(Intent.ACTION_TIME_TICK)) {
                MyLog.cdl("0000时间到了====PlayTaskActivity");
                if (!isPlayForst) {
                    MyLog.playTask("播放界面不再前台,打断操作");

                    return;
                }

                if (playTaskParsener == null) {
                    return;
                }
                playTaskParsener.updateWeatherView();
                playTaskParsener.updateMediaVoiceNum();
                //检查插播任务
                playTaskParsener.checkTextInsertTaskInfoByTime();
            } else if (action.equals(AppInfo.MESSAGE_RECEIVE_SCREEN_CLOSE)) {
                //停止任务自动检查
                if (playTaskParsener != null) {
                    playTaskParsener.shutDownDiffScreenLight();
                }
                MainActivity.IS_ORDER_REQUEST_TASK = false;
                startToMainTaskView();
                AppInfo.startCheckTaskTag = false;
            } else if (action.equals(AppInfo.TURN_VOICE_ZREO)) {
                //静音
                if (playTaskParsener != null) {
                    playTaskParsener.setMediaVoiceMute(true);
                }
            } else if (action.equals(AppInfo.TURN_VOICE_RESUME)) {
                //恢复音量
                if (playTaskParsener != null) {
                    playTaskParsener.setMediaVoiceMute(false);
                }
            }
        }
    };

    ActivityPlayTaskBinding mBingding;



    private EtvParsener etvParsener;
    private EtvService etvService;
    private IntentFilter intentFilter;


    class EtvParsener extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            playGpioPosition = intent.getIntExtra("theGpioDeskPosition", 0);
            Log.e("TAG", "onReceive+theGpioDeskPosition: " + playGpioPosition);
            startGpioPosition(playGpioPosition);
        }
    }

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        MyLog.message("PlayerTaskAct onCreate");
        boolean isDevSpeed = SharedPerManager.getDevSpeedStatues();
        if (isDevSpeed) {
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            //硬件加速
            getWindow().setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
        mBingding = ActivityPlayTaskBinding.inflate(getLayoutInflater());
        setContentView(mBingding.getRoot());
        getIntentDate();
        initView();
        initListener();
        initTaskReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("thePositionGpio");
        etvParsener = new EtvParsener();
        registerReceiver(etvParsener, intentFilter);
    }

    int playGpioPosition = 0;

    private void getIntentDate() {
        playGpioPosition = getIntent().getIntExtra("theGpioNotDeskPosition",0);
        startGpioPosition(playGpioPosition);
        MyLog.cdl("======onKeyDown========" + "/////"+playGpioPosition);
    }

    private void initListener() {
        AppStatuesListener.getInstance().NetChange.observe(PlayerTaskActivity.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean s) {
                MyLog.message("=======onChanged==NetChange======" + s);
                //网络已连接，通知需要刷新得节目，刷新一次
                if (playTaskParsener != null && s) {
                    playTaskParsener.updateGenWebViewRefresh(AppInfo.NET_ONLINE);
                }
            }
        });

        //这里需要更新声音
        AppStatuesListener.getInstance().UpdateMainMediaVoiceEvent.observe(PlayerTaskActivity.this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                MyLog.cdl("==========界面更新音量值大小========" + s);
                //背景图下载完成，这里修改音量大小
                if (playTaskParsener != null) {
                    playTaskParsener.updateMediaVoiceNum();
                    playTaskParsener.modifyLogoShowInfo("背景图下载完毕，更新logo");
                }
            }
        });

        mBingding.viewClickRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        startDownTime = System.currentTimeMillis();
                        MyLog.task("====setOnTouchListener====ACTION_DOWN=======");
                        break;
                    case MotionEvent.ACTION_UP:
                        startUpTime = System.currentTimeMillis();
                        MyLog.task("=====setOnTouchListener==ACTION_UP========");
                        if (startUpTime - startDownTime > 59 * 1000) {
                            MyLog.task("=====setOnTouchListener==ACTION_UP====deal====");
                            showBaseSettingDialogNew();
                        }
                        startDownTime = 0;
                        startUpTime = 0;
                        break;
                }
                return true;
            }
        });
    }

    long startDownTime = 0;
    long startUpTime = 0;

    @Override
    public void checkSdStateFinish() {
        startToMainTaskView();
    }

    @Override
    public void stopOrderToPlay() {
        startToMainTaskView();
    }

    //这里表示不去任务==null
    @Override
    public void getTaskInfoNull() {
        MyLog.cdl("=======获取的节目==null==playActivity==");
        startToMainTaskView();
    }

    public static boolean ISVIEW_FORST = false;

    private void initView() {
        playTaskParsener = new PlayTaskParsener(PlayerTaskActivity.this, this);
        playTaskParsener.updateMediaVoiceNum();
        updateViewInfo("onResume");
        if(AppConfig.APP_TYPE==AppConfig.APP_TYPE_CHUNYN){
            mBingding.viewClickRight.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showDownStatuesView(boolean isShow, String desc) {
        mBingding.downSyatues.relaDownTag.setVisibility(isShow ? View.VISIBLE : View.GONE);
        mBingding.downSyatues.tvDownDesc.setText(desc);
    }
    @Override
    protected void onResume() {
        super.onResume();
        ISVIEW_FORST = true;
        int widthScreen = SharedPerUtil.getScreenWidth();
        int screenheight = SharedPerUtil.getScreenHeight();
        Log.e("cdl", "=======currentSencent==" + widthScreen + " / " + screenheight);
        JUMP_TO_VIEW = JUMP_DEFAULT;
        TaskWorkService.isStartApk = false;  //startApk  归位
        AppInfo.startCheckTaskTag = true;
        if (playTaskParsener == null) {
            return;
        }
        SceneEntity currentSencent = playTaskParsener.getCurrentSencenEntity();
        Log.e("cdl", "=======currentSencent==" + (currentSencent == null));
        if (currentSencent != null) {
            playTaskParsener.resumePlayView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyLog.playTask("========生命周期=======onPause=======");
        if (playTaskParsener != null) {
            playTaskParsener.pauseDisplayView();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unBindService();
        ISVIEW_FORST = false;
        if (viewInsertTextManager != null) {
            viewInsertTextManager.onDestoryBall();
        }
        isPlayForst = false;
        MyLog.playTask("========生命周期==========onStop====000===" + JUMP_TO_VIEW + " / " + JUMP_TO_APK);
        if (JUMP_TO_VIEW != JUMP_TO_APK) {
            return;
        }
        if (playTaskParsener != null) {
            playTaskParsener.clearMemory();
            playTaskParsener.clearLastView(PlayTaskParsener.TAG_CLEARVIEW_ONDESTORY);
        }
    }


    /**
     * 跟新界面数据
     *
     * @param tag
     */
    private void updateViewInfo(String tag) {
        if (playTaskParsener == null) {
            return;
        }
        MyLog.playTask("===========刷新界面===" + tag + "  /时间==" + System.currentTimeMillis());
        EtvService.getInstance().updateDevStatuesToWeb(PlayerTaskActivity.this);
        playTaskParsener.getTaskToView(tag);           //获取数据

    }

    /**
     * 加载WebView
     *
     * @param showWbeUrl
     */
    @Override
    public void startViewWebActivty(String showWbeUrl, String backTime) {
        if (playTaskParsener != null) {
            playTaskParsener.pauseDisplayView();
        }
        JUMP_TO_VIEW = JUMP_TO_OTHER;
        //url  触摸返回时间
        Intent intent = new Intent(PlayerTaskActivity.this, ViewWebViewActivity.class);
        intent.putExtra(ViewWebViewActivity.TAG_STR_WEB_VIEW, showWbeUrl);
        intent.putExtra(ViewWebViewActivity.TAG_BACK_TIME, backTime);
        startActivityForResult(intent, ONRESUME_PLAY_VIEW);
    }

    private static final int JUMP_DEFAULT = -1;
    private static final int JUMP_TO_APK = 0;
    private static final int JUMP_TO_OTHER = 1;
    private int JUMP_TO_VIEW = JUMP_DEFAULT;

    @Override
    public void startApkView(final String coLinkAction, String backTime) {
        MyLog.apk("======添砖APK===" + backTime + " / " + coLinkAction);
        boolean isPermission = PermissionUtil.checkFloatPermission(PlayerTaskActivity.this);
        MyLog.apk("判断是否有权限==" + isPermission);
        if (!isPermission) {
            PermissionUtil.openWinPopToast(PlayerTaskActivity.this);
            return;
        }
        boolean isInstall = APKUtil.ApkState(PlayerTaskActivity.this, coLinkAction);
        if (!isInstall) {
            showToastView("跳转软件没有安装");
            return;
        }
        JUMP_TO_VIEW = JUMP_TO_APK;
        RawSourceEntity rawSourceEntity = InstallApkBackUtil.getResourceEntity();
        if (rawSourceEntity == null) {  //不是7.1  3288的
            goToTaskApkBackActivity(coLinkAction);
            return;
        }
        int timeApk = getApkBackTime(backTime);
        if (timeApk < AppConfig.APP_BACK_TIME_MIX) {
            showToastView(getString(R.string.apk_back_time));
            return;
        }
        //启动--APK
        APKUtil.startApp(PlayerTaskActivity.this, coLinkAction);
        //通知service开始计时
        Intent intent = new Intent();
        intent.setAction(TaskWorkService.BACK_OTHER_APK_TO_ETV_MAIN);
        intent.putExtra(TaskWorkService.BACK_OTHER_APK_TO_ETV_MAIN, timeApk);
        sendBroadcast(intent);
    }

    /**
     * 跳转APK不返回
     */
    private void goToTaskApkBackActivity(String coLinkAction) {
        Intent intentApk = new Intent();
        intentApk.setClass(PlayerTaskActivity.this, TaskApkBackActivity.class);
        intentApk.putExtra(TaskApkBackActivity.TAG_GO_TO_APK_PACKAGENAME, coLinkAction);
        startActivity(intentApk);
        finish();
    }

    private int getApkBackTime(String time) {
        time = time.trim();
        int appBackTime = 0;
        try {
            if (time == null || time.length() < 1) {
                return 0;
            }
            appBackTime = Integer.parseInt(time);
            if (appBackTime < 1) {  //没有设置返回时间，这里就不需要计时返回
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appBackTime;
    }

    /**
     * 全屏展示View
     *
     * @param cpListEntity
     * @param list
     */
    @Override
    public void toShowFullScreenView(CpListEntity cpListEntity, List<String> list, int clickPositon) {
        if (cpListEntity == null) {
            MyLog.touch("====展示全屏wei=null,不操作");
            return;
        }
        if (list == null || list.size() < 1) {
            MyLog.touch("====展示全屏list=null,不操作");
            return;
        }
        String cpType = cpListEntity.getCoType(); //控件类型
        MyLog.touch("====展示全屏w==" + cpType);
        Intent intent = new Intent();
        switch (cpType) {
            case AppInfo.VIEW_IMAGE: //图片
                MyLog.touch("====展示全屏w==1" + cpType);
                intent.setClass(PlayerTaskActivity.this, TaskImageActivity.class);
                intent.putExtra("clickPositon", clickPositon);
                intent.putStringArrayListExtra(TaskImageActivity.TAG_RECEIVE_MESSAGE, (ArrayList<String>) list);
                break;
            case AppInfo.VIEW_VIDEO: //视频
                MyLog.touch("====展示全屏w==2" + cpType);
                intent.setClass(PlayerTaskActivity.this, TaskVideoActivity.class);
                intent.putStringArrayListExtra(TaskVideoActivity.TAG_RECEIVE_MESSAGE_VIDEO, (ArrayList<String>) list);
                break;
            case AppInfo.VIEW_IMAGE_VIDEO: //混播
                MyLog.touch("====展示全屏w==3" + cpType);
                intent.setClass(PlayerTaskActivity.this, PlayImaVideoActivity.class);
                intent.putStringArrayListExtra(PlayImaVideoActivity.TAG_RECEIVE_MESSAGE, (ArrayList<String>) list);
                break;
        }
        JUMP_TO_VIEW = JUMP_TO_OTHER;
        startActivityForResult(intent, ONRESUME_PLAY_VIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyLog.playTask("========生命周期==========界面回来了====" + resultCode);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == ONRESUME_PLAY_VIEW) {
            if (playTaskParsener != null) {
                playTaskParsener.resumePlayView();
            }
        }
    }

    /**
     * 所有的任务已经播放完毕，
     * 去检查新的播放任务
     */
    @Override
    public void findTaskNew() {
        MyLog.cdl("=========TaskCacheActivity====findTaskNew=任务播放完毕了===，去默认播放界面");
        MainActivity.IS_ORDER_REQUEST_TASK = true;
        Intent intent = new Intent(PlayerTaskActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /***
     * 界面异常:直接停止界面
     * @param errorInfo
     */
    @Override
    public void showViewError(String errorInfo) {
        MyLog.playTask("===========showViewError==" + errorInfo);
        showToastView(errorInfo);
        startToMainTaskView();
    }

    @Override
    public void showToastView(String toast) {
        MyToastView.getInstance().Toast(PlayerTaskActivity.this, toast);
    }

    @Override
    public AbsoluteLayout getAbsoluteLayout() {
        return mBingding.viewAbous;
    }

    @Override
    public ImageView getBggImageView() {
        return mBingding.ivBackBgg;
    }

    private void initTaskReceiver() {
        IntentFilter fileter = new IntentFilter();
        fileter.addAction(AppInfo.DOWN_TASK_SUCCESS);  //任务下载完毕，这里需要刷新界面
        fileter.addAction(Intent.ACTION_TIME_TICK);  //时间变化得广播监听
        fileter.addAction(AppInfo.MESSAGE_RECEIVE_SCREEN_CLOSE);  //息屏休眠得功能
        fileter.addAction(AppInfo.TURN_VOICE_ZREO);  //静音
        fileter.addAction(AppInfo.TURN_VOICE_RESUME);   //恢复音量
        registerReceiver(receiverPlayTask, fileter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        MyLog.cdl("======onKeyDown===666=====" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.e("TAG", "onKeyDown: "+"快进" );
            showBaseSettingDialogNew();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
            //快进 87
            Log.e("TAG", "onKeyDown: "+"快进" );
            playTaskParsener.moveViewForward(true);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
            //快退 88
            Log.e("TAG", "onKeyDown: "+"快进" );
            playTaskParsener.moveViewForward(false);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            //暂停 86
            playTaskParsener.pauseDisplayView();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            //播放 85
            playTaskParsener.resumePlayView();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_1) {
            Intent intent = new Intent(PlayerTaskActivity.this,PlayTaskTriggerActivity.class);
            intent.putExtra("playPosition",0);
            startActivity(intent);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_2) {
            Intent intent = new Intent(PlayerTaskActivity.this,PlayTaskTriggerActivity.class);
            intent.putExtra("playPosition",1);
            startActivity(intent);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_3) {
            Intent intent = new Intent(PlayerTaskActivity.this,PlayTaskTriggerActivity.class);
            intent.putExtra("playPosition",2);
            startActivity(intent);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_4) {
            Intent intent = new Intent(PlayerTaskActivity.this,PlayTaskTriggerActivity.class);
            intent.putExtra("playPosition",3);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void startGpioPosition(int position) {
        playGpioPosition = position ;
        Log.e("TAG", "startGpioPosition 333333333333: "+playGpioPosition );
        if (playTaskParsener == null) {
            return;
        }
        Log.e("TAG", "startGpioPosition 111111: "+ playTaskParsener );
        playTaskParsener.clearMemory();
        playTaskParsener.playNextSencenView(true);
    }


    //用来监听触摸，跳转屏保场景的
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //有按下动作时取消定时
                break;
            case MotionEvent.ACTION_UP:
                //抬起时启动定时
                if (playTaskParsener == null) {
                    return true;
                }
                SceneEntity sceneEntity = getCurrentPlayScenEntity();
                if (sceneEntity == null) {
                    return true;
                }
                String pmType = sceneEntity.getPmType();
                if (pmType == null || pmType.length() < 1) {
                    return true;
                }
                if (pmType.contains(AppInfo.PROGRAM_TOUCH)) { //互动节目,才执行触摸返回
                    playTaskParsener.autoJumpToNextSencentProjector();
                }

                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private SceneEntity getCurrentPlayScenEntity() {
        SceneEntity sceneEntity = playTaskParsener.getCurrentSencenEntity();
        if (sceneEntity == null) {
            return null;
        }
        return sceneEntity;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(etvParsener);
        try {
            if (viewInsertTextManager != null) {
                viewInsertTextManager.onDestoryBall();
            }
            if (playTaskParsener != null) {
                playTaskParsener.clearMemory();
                playTaskParsener.clearLastView(PlayTaskParsener.TAG_CLEARVIEW_ONDESTORY);
            }
            MyLog.playTask("========生命周期==========onDestroy====");
            EtvApplication.getInstance().setTaskWorkEntityList(null);
            if (receiverPlayTask != null) {
                unregisterReceiver(receiverPlayTask);
            }
            unBindService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //=======插播消息================================================================================

    ViewInsertTextManager viewInsertTextManager;

    @Override
    public void playInsertTextTaskToPopWindows(boolean isShow, CpListEntity cpListEntity) {
        try {
            MyLog.cdl("=====playInsertTextTaskToPopWindows===" + isShow);
            if (viewInsertTextManager == null) {
                viewInsertTextManager = new ViewInsertTextManager(PlayerTaskActivity.this);
            }
            if (isShow) {
                mBingding.relaMatBgg.setVisibility(View.VISIBLE);
                mBingding.autoScroolView.setVisibility(View.VISIBLE);
                viewInsertTextManager.setCpListEntity(cpListEntity, mBingding.autoScroolView, mBingding.relaMatBgg);
                viewInsertTextManager.showFloatView();
            } else {
                mBingding.relaMatBgg.setVisibility(View.GONE);
                mBingding.autoScroolView.setVisibility(View.GONE);
                viewInsertTextManager.onDestoryBall();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unBindService() {
        try {
            if (hdmiInterface != null) {
                unbindService(conn_hdmi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int hdmiX;
    int hdmiY;
    int hdmiWidth;
    int hdmiHeight;
    

    /**
     * 加载HdmiIn的功能
     */
    @Override
    public void showHdmInViewToActivity(int x, int y, int width, int height) {
        Observable.timer(2, TimeUnit.SECONDS)
                .compose(new SchedulerTransformer())
                .compose(RxLifecycle.bindRxLifecycle(this))
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        MyLog.i("main", "=====开始计时=倒计时完成=====");
                        loadHdmiView(x, y, width, height);
                    }
                });
    }

    private void loadHdmiView(int x, int y, int width, int height) {
        boolean isSuportHdmi = SharedPerManager.getIfHdmiInSuport();
        Log.e("TAG", "loadHdmiView: "+isSuportHdmi );
        if (!isSuportHdmi) {
            showToastView(getString(R.string.hdmi_in_support_not));
            return;
        }
        boolean isLinAidl = lineHdmInAidl();
        if (!isLinAidl) {
            showToastView("Line Hdmi_in Failed ");
            return;
        }
        this.hdmiX = x;
        this.hdmiY = y;
        this.hdmiWidth = width;
        this.hdmiHeight = height;
        if (hdmiInterface == null) {
            showToastView("Line Hdmi_in Failed : hdmiInterface is null");
            return;
        }
        try {
            hdmiInterface.showSuspensionwindow(x, y, width, height);
        } catch (Exception e) {
            MyLog.hdmi("加载Hdmin View error : " + e.toString(), true);
            e.printStackTrace();
        }
    }

    /**
     * 隐藏HDMIiN的界面
     */
    @Override
    public void dissHdmInViewToActivity() {
        MyLog.hdmi("dissHdmInViewToActivity");
        if (hdmiInterface == null) {
            return;
        }
        try {
            hdmiInterface.dissSuspensionwindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toClickLongViewListener() {
        showBaseSettingDialogNew();
    }

    @Override
    public void playCompanyBack() {

    }

    @Override
    public TextView getM11VideoErrorText() {
        return mBingding.tvErrorDesc;
    }

    /**
     * 连接HDMI_IN的连接
     *
     * @return
     */
    private boolean lineHdmInAidl() {
        String packageName = AppInfo.HDMI_IN_PACKAGE_NAME;
        boolean isApkInstall = APKUtil.ApkState(PlayerTaskActivity.this, packageName);
        if (!isApkInstall) {
            showToastView("HDMI_IN not install");
            return false;
        }
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, AppInfo.HDMI_IN_SERVICE_NAME));
            bindService(intent, conn_hdmi, Context.BIND_AUTO_CREATE);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    HdmiInterface hdmiInterface;
    private ServiceConnection conn_hdmi = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.e("cdl", "====服务器连接=======");
            hdmiInterface = HdmiInterface.Stub.asInterface(binder);
            if (hdmiInterface == null) {
                showToastView("Line Hdmi_in Failed : hdmiInterface is null");
                return;
            }
            try {
                hdmiInterface.showSuspensionwindow(hdmiX, hdmiY, hdmiWidth, hdmiHeight);
            } catch (Exception e) {
                MyLog.hdmi("加载Hdmin View error : " + e.toString(), true);
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e("cdl", "====服务器断开======");
            hdmiInterface = null;
        }
    };

    //判断播放界面是否在前台
    public static boolean isPlayForst = false;

    @Override
    protected void onPostResume() {
        super.onPostResume();
        isPlayForst = true;
    }


}
