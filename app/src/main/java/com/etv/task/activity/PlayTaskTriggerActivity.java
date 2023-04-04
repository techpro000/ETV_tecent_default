package com.etv.task.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.etv.activity.MainActivity;
import com.etv.service.EtvService;
import com.etv.task.entity.CpListEntity;
import com.etv.task.parsener.PlayTaskParsener;
import com.etv.task.parsener.PlayTaskTriggerParsener;
import com.etv.task.view.PlayTaskView;
import com.etv.util.MyLog;
import com.ys.etv.R;

import java.util.List;

/***
 * 用来播放网络任务的功能界面
 */
public class PlayTaskTriggerActivity extends TaskActivity implements PlayTaskView {
    @Override
    public void showDownStatuesView(boolean isShow, String desc) {

    }
    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_play_task);
        initView();
        getIntentDate();

    }

    int playGpioPosition = 0;

    private void getIntentDate() {
        playGpioPosition = getIntent().getIntExtra("theGpioNotDeskPosition",0);
        MyLog.cdl("======onKeyDown========" + "/////"+playGpioPosition);
    }

    ImageView iv_back_bgg;
    AbsoluteLayout view_abous;
    PlayTaskTriggerParsener playTaskParsener;


    private void initView() {
        iv_back_bgg = (ImageView) findViewById(R.id.iv_back_bgg);
        view_abous = (AbsoluteLayout) findViewById(R.id.view_abous);
        playTaskParsener = new PlayTaskTriggerParsener(PlayTaskTriggerActivity.this, this,playGpioPosition);
    }




    @Override
    protected void onResume() {
        super.onResume();
        MyLog.playTask( this.getClass().getName() + " onResume");
        updateViewInfo("onResume");
    }

    private void updateViewInfo(String tag) {
        if (playTaskParsener == null) {
            return;
        }
        playTaskParsener.getTaskToView(tag);           //获取数据
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showBaseSettingDialogNew();
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_1) {
            playTaskParsener.clearMemory();
            playTaskParsener.setPlayPosition(0);
            playTaskParsener.getTaskToView("keyCode 1");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_2) {
            playTaskParsener.clearMemory();
            playTaskParsener.setPlayPosition(1);
            playTaskParsener.getTaskToView("keyCode 2");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_3) {
            playTaskParsener.clearMemory();
            playTaskParsener.setPlayPosition(2);
            playTaskParsener.getTaskToView("keyCode 3");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_4) {
            playTaskParsener.clearMemory();
            playTaskParsener.setPlayPosition(3);
            playTaskParsener.getTaskToView("keyCode 4");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    /***
     * ===================================================================================================
     */
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

    @Override
    public void playInsertTextTaskToPopWindows(boolean isShow, CpListEntity cpListEntity) {

    }

    @Override
    public void toShowFullScreenView(CpListEntity cpListEntity, List<String> list, int clickPosition) {

    }

    @Override
    public void startViewWebActivty(String webUrl, String backTime) {

    }

    @Override
    public void findTaskNew() {
        MainActivity.IS_ORDER_REQUEST_TASK = true;
        Intent intent = new Intent(PlayTaskTriggerActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showViewError(String errorInfo) {
        MyLog.playTask("===========showViewError==" + errorInfo);
        showToastView(errorInfo);
        startToMainTaskView();
    }

    @Override
    public AbsoluteLayout getAbsoluteLayout() {
        return view_abous;
    }

    @Override
    public ImageView getBggImageView() {
        return iv_back_bgg;
    }


    @Override
    public void startApkView(String coLinkAction, String backTime) {

    }

    @Override
    public void showHdmInViewToActivity(int x, int y, int width, int height) {

    }

    @Override
    public void dissHdmInViewToActivity() {

    }

    @Override
    public void toClickLongViewListener() {
        showBaseSettingDialogNew();
    }

    @Override
    public void playCompanyBack() {
        MainActivity.IS_ORDER_REQUEST_TASK = true;
        startToMainTaskView();
    }

    @Override
    public TextView getM11VideoErrorText() {
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        playTaskParsener.clearMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
