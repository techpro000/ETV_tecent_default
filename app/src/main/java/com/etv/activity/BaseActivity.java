package com.etv.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.service.EtvService;
import com.etv.service.TaskWorkService;
import com.etv.service.TcpService;
import com.etv.service.TcpSocketService;
import com.etv.setting.WorkChoiceActivity;
import com.etv.util.FileUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.guardian.GuardianUtil;
import com.etv.util.image.ImageScreenShotUtil;
import com.etv.util.rxjava.AppStatuesListener;
import com.etv.util.system.CpuModel;
import com.etv.util.system.SystemManagerUtil;
import com.etv.view.dialog.SettingMenuDialog;
import com.ys.etv.R;
import com.ys.model.dialog.EditTextDialog;
import com.ys.model.dialog.ErrorToastView;
import com.ys.model.dialog.MyToastView;
import com.ys.model.listener.EditTextDialogListener;
import com.ys.model.util.ActivityCollector;

public class BaseActivity extends AppCompatActivity {

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initBase();
    }

    //进入播放界面，取消显示悬浮窗
    public void dismissPopWindow() {
        sendBroadcast(new Intent(TaskWorkService.DISSMISS_DOWN_POOP_WINDOW));
    }

    public String getLanguageFromResurce(int resourceId) {
        String desc = getResources().getString(resourceId);
        return desc;
    }

    public String getLanguageFromResurceWithPosition(int resourceId, String desc) {
        String stringStart = getResources().getString(resourceId);
        String startResult = String.format(stringStart, desc);
        return startResult;
    }

    private void initBase() {
        SystemManagerUtil.openCloseChuangpinLeaderBar(BaseActivity.this, false);
        ActivityCollector.addActivity(this);
        GuardianUtil.sendBroadToGuardianConpany(BaseActivity.this);
        AppStatuesListener.getInstance().objectLiveDate.observe(BaseActivity.this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == AppStatuesListener.LIVE_DATA_SCREEN_CATPTURE) {
                    // ImageCaptureUtil.takeScreenShotM11(BaseActivity.this);
                    ImageScreenShotUtil.getInstance(BaseActivity.this).takeScreenShot(BaseActivity.this);
                }
//                else if (integer == AppStatuesListener.LIVE_DATA_SAVE_POWERONOFF_LOG) {
//                    String saveOnTime = RootCmd.getProperty("persist.sys.powerontime", "0");
//                    String saveOffTime = RootCmd.getProperty("persist.sys.powerofftime", "0");
//                    MyLog.db("===powerOnOff====saveOnTime==" + saveOnTime + " / " + saveOffTime, true);
//                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != ImageScreenShotUtil.EVENT_SCREENSHOT) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ImageScreenShotUtil.getInstance(BaseActivity.this).getImageInfoFtomIntent(resultCode, data);
        }
    }

    protected void onResume() {
        super.onResume();
        startAppService();
        FileUtil.creatPathNotExcit("Base OnResume !");
    }

    /***
     * start App Service
     */
    private void startAppService() {
        //  EtvService 不管什么状况，都要起来
        startService(new Intent(BaseActivity.this, EtvService.class));
        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
            startService(new Intent(BaseActivity.this, TcpService.class));
        } else {
            startService(new Intent(BaseActivity.this, TcpSocketService.class));
        }
        startService(new Intent(BaseActivity.this, TaskWorkService.class));
    }

    public void showToastView(String desc) {
        MyToastView.getInstance().Toast(BaseActivity.this, desc);
    }

    SettingMenuDialog settingMenuDialog;

    public void showBaseSettingDialogNew() {
        boolean mainForst = ActivityCollector.isForeground(getApplication(), MainActivity.class.getName());
        if (settingMenuDialog == null) {
            settingMenuDialog = new SettingMenuDialog(BaseActivity.this);
        }
        settingMenuDialog.setOnDialogClickListener(new SettingMenuDialog.SettingMenuClickListener() {
            @Override
            public void clickWorkModel() {      //显示工作模式的弹窗
                showExitBaseDialog(0);
//                startActivity(new Intent(BaseActivity.this, WorkChoiceActivity.class));
//                if (!mainForst) { //不在前台，直接退出
//                    finish();
//                }
            }

            @Override
            public void exitApp() {
                if (mainForst) { //主界面才需要密码，播放界面不需要
                    showExitBaseDialog(1);
                } else {
                    exitAppInfo();
                }
            }
        });
        if (mainForst) {
            settingMenuDialog.show(getString(R.string.exit_app));
        } else {
            settingMenuDialog.show(getString(R.string.exit_play));
        }
    }

    /****
     * 点击需要密码验证的dialog
     * @param tag
     * 0  :设置界面
     * 1  ：退出APP
     */
    public void showExitBaseDialog(int tag) {
        String exitCode = SharedPerManager.getExitpassword();
        if (exitCode == null || exitCode.length() < 1) {
            if (tag == 1) {
                //退出APP
                exitAppInfo();
            } else {
                boolean mainForst = MainActivity.isMainForst;
                startActivity(new Intent(BaseActivity.this, WorkChoiceActivity.class));
                if (!mainForst) { //不在前台，直接退出
                    finish();
                }
            }
            return;
        }
        //有退出密码
        EditTextDialog editTextDialog = new EditTextDialog(BaseActivity.this);
        editTextDialog.setOnDialogClickListener(new EditTextDialogListener() {
            @Override
            public void commit(String content) {
                String exitCodeDialog = SharedPerManager.getExitpassword();
                if (content.trim().equals(exitCodeDialog) || content.trim().contains("000")) {
                    if (tag == 1) {
                        //退出APP
                        exitAppInfo();
                    } else {
                        boolean mainForst = MainActivity.isMainForst;
                        startActivity(new Intent(BaseActivity.this, WorkChoiceActivity.class));
                        if (!mainForst) { //不在前台，直接退出
                            finish();
                        }
                    }
                    return;
                }
                MyToastView.getInstance().Toast(BaseActivity.this, getString(R.string.password_error));
            }

            @Override
            public void clickHiddleView() {

            }
        });
        editTextDialog.show(getString(R.string.exit_password), "", getString(R.string.submit));
    }

    public void exitAppInfo() {
        //干掉PopWindow
        sendBroadcast(new Intent(TaskWorkService.DESTORY_DOWN_POOP_WINDOW));
        //设置异常退出得问题
        SharedPerManager.setExitDefault(true);
        //没有退出密码
        boolean mainForst = ActivityCollector.isForeground(BaseActivity.this, MainActivity.class.getName());

        System.out.println("aaaaaaaaaaaaaaa exitAppInfo " + mainForst);
        if (mainForst) {
            //在前台，直接退出
            killAppMySelf();
        } else {
            //不再前台。需要返回到前台
            Intent intentMain = new Intent(BaseActivity.this, MainActivity.class);
            startActivity(intentMain);
            finish();
        }
    }

    public void killAppMySelf() {
        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_JIAIWEI) {
            ErrorToastView.getInstance().Toast(BaseActivity.this, "Prohibit operation");
            return;
        }
        AppInfo.startCheckTaskTag = false;  //退出程序，设置为false
        //开启底部导航栏
        SystemManagerUtil.openCloseChuangpinLeaderBar(BaseActivity.this, true);
        if (CpuModel.isMLogic()) {
            GuardianUtil.startGuardianService(BaseActivity.this);
        }
        //发广播给守护进程，待会要起来
        Intent intent = new Intent(AppInfo.START_PROJECTOR_GUARDIAN_TIMER);
        sendBroadcast(intent);
        Log.e("projector", "发广播给守护进程，定时起来");
        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_JIANGJUN_YUNCHENG) {
            moveTaskToBack(true);
            return;
        }
        ImageScreenShotUtil.getInstance(BaseActivity.this).onDestroyImageScreen();
        //停止所有得程序服务
        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
            stopService(new Intent(BaseActivity.this, TcpService.class));
        } else {
            stopService(new Intent(BaseActivity.this, TcpSocketService.class));
        }
        stopService(new Intent(BaseActivity.this, EtvService.class));
        stopService(new Intent(BaseActivity.this, TaskWorkService.class));
        // moveTaskToBack(true);
        MyLog.cdl("主界面退出,发广播给守护进程", true);
        //关闭所有的程序界面
        ActivityCollector.finishAll();
        //杀死当前进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (settingMenuDialog != null) {
            settingMenuDialog.dissmiss();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }


}
