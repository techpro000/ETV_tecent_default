package com;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.etv.ActivityCallback;
import com.etv.config.AppInfo;
import com.etv.entity.ScreenEntity;
import com.etv.task.entity.TaskWorkEntity;
import com.etv.util.AppLinkSer;
import com.etv.util.CrashExceptionHandler;
import com.etv.util.MyLog;
import com.etv.util.RootCmd;
import com.etv.util.SharedManagerModel;
import com.etv.util.SharedPerManager;
import com.etv.util.poweronoff.PowerOnOffManager;
import com.etv.util.system.CpuModel;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;
import com.tencent.mmkv.MMKV;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.zhy.http.okhttp.OkHttpUtils;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class EtvApplication extends Application {

    public static EtvApplication instance;
    static Context context;
    public List<ScreenEntity> listScreen;   //缓存屏幕得相关属性
    public List<TaskWorkEntity> taskWorkEntityList;   //缓存任务得相关属性
    public TaskWorkEntity TaskWorkEntityInsert;      //缓存字幕插播消息

    public static EtvApplication getInstance() {
        return instance;
    }

    SharedManagerModel sharedPerManager;
    public int count;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = this;
        initSharedPerence();
        //数据库初始化
        LitePal.initialize(this); //数据库初始化
        initOther();

        registerActivityLifecycleCallbacks(new ActivityCallback() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                count++;
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                count--;
                System.out.println("aaaaaaaaaaaaaaaa-----ActivityCollector---- " + count);
            }
        });

        if (CpuModel.IsRK3566() || CpuModel.IsRK3568()) {
            RootCmd.setProperty("use_nuplayer", "true");
        }

    }

    private void initSharedPerence() {
        MyLog.shared("===initSharedPerence==000");
        String rootDir = MMKV.initialize(EtvApplication.this);
        MyLog.shared("===initSharedPerence==1111==" + rootDir);
        sharedPerManager = new SharedManagerModel(this, "ETV_SHARE");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Context getContext() {
        return context;
    }

    private void initOther() {
        PowerOnOffManager.getInstance().initPowerOnOffManager(EtvApplication.this);
        initExcelSo();
        CrashExceptionHandler crashExceptionHandler = CrashExceptionHandler.getCrashInstance();
        crashExceptionHandler.init();
//        CrashReport.initCrashReport(this, "6a63b5ad10", false);
        taskWorkEntityList = new ArrayList<TaskWorkEntity>();
        listScreen = new ArrayList<ScreenEntity>();
        initX5View();
        initOkHttp();
        initIJKPlayer();

        AppLinkSer.setSocketType(SharedPerManager.getSocketType());
    }

    /***
     * 初始化EXCEL加载控件
     */
    private void initExcelSo() {
        if (!SharedPerManager.getWpsShowEnable()) {
            Log.e("CDL", "===========文档初始化状态======拦截========");
            return;
        }
        Log.e("CDL", "===========文档初始化状态==============");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //EXCEL加载
                System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
                System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
                System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void initIJKPlayer() {
        PlayerFactory.setPlayManager(SystemPlayerManager.class);
    }

    private void initOkHttp() {
        int workModel = SharedPerManager.getWorkModel();
        if (workModel == AppInfo.WORK_MODEL_SINGLE) {
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
//                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
//                .hostnameVerifier(new HostnameVerifier() {
//                    @Override
//                    public boolean verify(String hostname, SSLSession session) {
//                        return true;
//                    }
//                })
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

    //用来判断
    public boolean isInitWebX5Statues = false;

    /**
     * 初始化浏览器内核
     */
    public void initX5View() {
        int workModel = SharedPerManager.getWorkModel();
        if (workModel == AppInfo.WORK_MODEL_SINGLE) {
            return;
        }
        boolean isDevSpeed = SharedPerManager.getDevSpeedStatues();
        if (!isDevSpeed) {
            return;
        }
        try {
            QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
                @Override
                public void onViewInitFinished(boolean isSuccess) {
                    isInitWebX5Statues = isSuccess;
                    MyLog.cdl("======设置浏览器内核====isSuccess=" + isSuccess, true);
                    //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                }

                @Override
                public void onCoreInitFinished() {
                    MyLog.cdl("======设置浏览器内核====onCoreInitFinished");
                    // TODO Auto-generated method stub
                }
            };
            QbSdk.setTbsListener(new TbsListener() {
                @Override
                public void onDownloadFinish(int i) {
                    MyLog.cdl("======设置浏览器内核====onDownloadFinish=" + i, true);
                }

                @Override
                public void onInstallFinish(int i) {
                    MyLog.cdl("======设置浏览器内核====onInstallFinish=" + i, true);
                }

                @Override
                public void onDownloadProgress(int i) {
                    MyLog.cdl("======设置浏览器内核====onDownloadProgress=" + i, true);
                }
            });
            //x5内核初始化接口
            QbSdk.initX5Environment(getApplicationContext(), cb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TaskWorkEntity getTaskWorkEntityInsert() {
        return TaskWorkEntityInsert;
    }

    public void setTaskWorkEntityInsert(TaskWorkEntity taskWorkEntityInsert) {
        MyLog.task("====插播消息====application==保存处理=======" + (taskWorkEntityInsert == null));
        this.TaskWorkEntityInsert = taskWorkEntityInsert;
    }

    //获取屏幕得个数
    public List<ScreenEntity> getListScreen() {
        return listScreen;
    }

    //保存屏幕得个数
    public void setListScreen(List<ScreenEntity> listScreen) {
        this.listScreen = listScreen;
    }

    //获取当前播放任务
    public List<TaskWorkEntity> getTaskWorkEntityList() {
        return taskWorkEntityList;
    }

    //设备当前播放得任务续航经
    public void setTaskWorkEntityList(List<TaskWorkEntity> taskWorkEntityList) {
        this.taskWorkEntityList = taskWorkEntityList;
    }

    public void saveData(String key, Object data) {
        sharedPerManager.saveData(key, data);
    }

    public Object getData(String key, Object defaultObject) {
        return sharedPerManager.getData(key, defaultObject);
    }
}
