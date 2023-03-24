package com.etv.task.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.AbsoluteLayout;

import com.etv.config.AppInfo;
import com.etv.service.TaskWorkService;
import com.etv.task.parsener.PlaySingleTaskParsener;
import com.etv.task.view.PlaySingleView;
import com.etv.util.MyLog;
import com.ys.etv.R;
import com.ys.model.dialog.WaitDialogUtil;

public class PlaySingleActivity extends TaskActivity implements PlaySingleView {

    @Override
    public void showDownStatuesView(boolean isShow, String desc) {

    }

    private BroadcastReceiver receiverSingle = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TaskWorkService.GET_TASK_FROM_WEB_TAG)) {
                //服务器下发新任务了这里需要中断操作
                if (playTaskSingleParsener != null) {
                    playTaskSingleParsener.clearMemory();
                }
                System.out.println("aaaaaaaaaaaaaaaaaaaaa---000------ " + isFinishing());
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_play_task);
        initView();
        initReceiverSingle();

        System.out.println("aaaaaaaaaaaaaaa------------->onCreate " + getClass());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println("aaaaaaaaaaaaaaa------------->onNewIntent " + getClass());
    }

    private void initReceiverSingle() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TaskWorkService.GET_TASK_FROM_WEB_TAG);
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiverSingle, filter);
    }

    AbsoluteLayout view_abous;
    PlaySingleTaskParsener playTaskSingleParsener;

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        showWaitDialog(true);
        view_abous = (AbsoluteLayout) findViewById(R.id.view_abous);
        view_abous.setBackgroundColor(getResources().getColor(R.color.black));
        playTaskSingleParsener = new PlaySingleTaskParsener(PlaySingleActivity.this, this);
    }

    @Override
    public void retryLoadSource() {
        MyLog.cdl("==========重新加载资源=======================");
        //这里暂时用跳转，界面销毁的方法避免内存溢出
        playTaskSingleParsener.clearMemory();
        Intent intent = new Intent();
        intent.setClass(PlaySingleActivity.this, TaskBlackActivity.class);
        startActivity(intent);
        System.out.println("aaaaaaaaaaaaaaaaaaaaa---111------ " + isFinishing());
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (playTaskSingleParsener != null) {
            playTaskSingleParsener.getDataFromSdcard();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiverSingle != null) {
            unregisterReceiver(receiverSingle);
        }
        if (playTaskSingleParsener != null) {
            playTaskSingleParsener.clearMemory();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyLog.cdl("=========onStop=========onstip====");
        if (playTaskSingleParsener != null) {
            playTaskSingleParsener.clearMemory();
        }
    }

    @Override
    public void checkSdStateFinish() {
        startToMainTaskView();
    }

    @Override
    public void stopOrderToPlay() {
        startToMainTaskView();
    }

    @Override
    public void getTaskInfoNull() {
        startToMainTaskView();
    }

    WaitDialogUtil waitDialog;

    @Override
    public void showWaitDialog(boolean isShow) {
        if (waitDialog == null) {
            waitDialog = new WaitDialogUtil(PlaySingleActivity.this);
        }
        if (isShow) {
            waitDialog.show("加载中");
        } else {
            waitDialog.dismiss();
        }
    }

    @Override
    public void notResourceTip(String descError) {
        showToastView(descError);
        startToMainTaskView();
    }

    @Override
    public AbsoluteLayout getAbsoluLayout() {
        return view_abous;
    }

    @Override
    public void finishView() {
        System.out.println("aaaaaaaaaaaaaaaaaaaaa---222------ " + isFinishing());
        finish();
    }

    @Override
    public void toClickLongViewListener() {
        showBaseSettingDialogNew();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showBaseSettingDialogNew();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
            playTaskSingleParsener.moveViewForward(true);
            MyLog.d("keyCode", "====快进");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
            playTaskSingleParsener.moveViewForward(false);
            MyLog.d("keyCode", "====快退");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            MyLog.d("keyCode", "====暂停");
            playTaskSingleParsener.pauseDisplayView();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            MyLog.d("keyCode", "====播放");
            playTaskSingleParsener.resumePlayView();
            return true;
        } else if (keyCode > 6 && keyCode < 17) {
            //按键事件
            MyLog.d("keyCode", "====点击了数字按键=======");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
