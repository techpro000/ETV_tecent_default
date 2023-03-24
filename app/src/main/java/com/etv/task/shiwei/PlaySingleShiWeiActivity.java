package com.etv.task.shiwei;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsoluteLayout;

import com.etv.config.AppInfo;
import com.etv.service.TaskWorkService;
import com.etv.task.activity.TaskActivity;
import com.etv.task.activity.TaskBlackActivity;
import com.etv.task.view.PlaySingleView;
import com.etv.util.MyLog;
import com.ys.model.dialog.WaitDialogUtil;
import com.ys.etv.R;

public class PlaySingleShiWeiActivity extends TaskActivity implements PlaySingleView {

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
    }

    private void initReceiverSingle() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TaskWorkService.GET_TASK_FROM_WEB_TAG);
        registerReceiver(receiverSingle, filter);
    }

    AbsoluteLayout view_abous;
    PlaySingleTaskShiWeiParsener playTaskSingleParsener;
    View view_cleck;

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        showWaitDialog(true);
        view_abous = (AbsoluteLayout) findViewById(R.id.view_abous);
        view_abous.setBackgroundColor(getResources().getColor(R.color.black));
        playTaskSingleParsener = new PlaySingleTaskShiWeiParsener(PlaySingleShiWeiActivity.this, this);
        view_cleck = (View) findViewById(R.id.view_cleck);
        view_cleck.setOnTouchListener(touchTaskListener);
    }

    @Override
    public void retryLoadSource() {
        MyLog.cdl("==========重新加载资源====");
        //这里暂时用跳转，界面销毁的方法避免内存溢出
        playTaskSingleParsener.clearMemory();
        Intent intent = new Intent();
        intent.setClass(PlaySingleShiWeiActivity.this, TaskBlackActivity.class);
        startActivity(intent);
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
            waitDialog = new WaitDialogUtil(PlaySingleShiWeiActivity.this);
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
        finish();
    }

    @Override
    public void toClickLongViewListener() {
        showBaseSettingDialogNew();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            MyLog.d("keyCode", "====返回");
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
        }
        return super.onKeyDown(keyCode, event);
    }
}
