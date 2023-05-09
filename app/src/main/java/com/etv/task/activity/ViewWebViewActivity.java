package com.etv.task.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.rxjava.AppStatuesListener;
import com.etv.view.layout.Generator;
import com.ys.etv.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 加载网页
 */
public class ViewWebViewActivity extends TaskActivity implements View.OnClickListener {

    public static final String TAG_STR_WEB_VIEW = "TAG_STR_WEB_VIEW";
    public static final String TAG_BACK_TIME = "TAG_BACK_TIME";

    @Override
    public void showDownStatuesView(boolean isShow, String desc) {

    }

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        boolean isDevSpeed = SharedPerManager.getDevSpeedStatues();
        if (isDevSpeed) {
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            //硬件加速
            getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(SharedPerUtil.getScreenWidth(), SharedPerUtil.getScreenHeight());
        View vieWeb = View.inflate(ViewWebViewActivity.this, R.layout.activity_web_touch, null);
        setContentView(vieWeb, params);
        initView();
        initListener();
    }

    private void initListener() {
        AppStatuesListener.getInstance().NetChange.observe(ViewWebViewActivity.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean s) {
                MyLog.message("=======onChanged==NetChange======" + s);
                if (s) {
                    refreshWebView("网络变化，刷新数据");
                }
            }
        });
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

    Generator webViewGen;
    LinearLayout lin_exit;
    TextView tv_exit;
    LinearLayout iv_no_data;
    String urlPath;
    AbsoluteLayout ab_view;
    long backTimePro;
    TextView tv_error_desc;

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        urlPath = getIntent().getStringExtra(TAG_STR_WEB_VIEW);
        if (urlPath == null) {
            tv_error_desc.setText("Url Path == null");
            iv_no_data.setVisibility(View.VISIBLE);
            return;
        }
        if (!urlPath.startsWith("http")) {
            urlPath = "http://" + urlPath;
        }
        String backTime = getIntent().getStringExtra(TAG_BACK_TIME);
        if (backTime == null || backTime.length() < 1) {
            backTime = SharedPerManager.getScene_task_touch_back_time() + "";
//            backTime = "180";
        }
        backTimePro = Long.parseLong(backTime);
        if (backTimePro < 1) {
            backTimePro = 180;
        }
        iv_no_data = (LinearLayout) findViewById(R.id.iv_no_data);
        tv_error_desc = (TextView) findViewById(R.id.tv_error_desc);
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        tv_exit = (TextView) findViewById(R.id.tv_exit);
        lin_exit.setOnClickListener(this);
        tv_exit.setOnClickListener(this);
        ab_view = (AbsoluteLayout) findViewById(R.id.ab_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshWebView("onResume");
    }

    private void refreshWebView(String printTag) {
        if (!NetWorkUtils.isNetworkConnected(ViewWebViewActivity.this)) {
            iv_no_data.setVisibility(View.GONE);
            return;
        }
        if (ab_view == null) {
            ab_view = (AbsoluteLayout) findViewById(R.id.ab_view);
        }
        if (webViewGen != null) {
            webViewGen.clearMemory();
            webViewGen = null;
        }
        ab_view.removeAllViews();
        startTimer(backTimePro);
        int widthShow = SharedPerUtil.getScreenWidth();
        int heightShow = SharedPerUtil.getScreenHeight();
        boolean isDevSpeed = SharedPerManager.getDevSpeedStatues();
        webViewGen = TaskDealUtil.getWebViewBySpeedBoolean(ViewWebViewActivity.this, isDevSpeed, 0, 0, widthShow, heightShow, urlPath);
        View view = webViewGen.getView();
        ab_view.addView(view, webViewGen.getLayoutParams());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lin_exit:
            case R.id.tv_exit:
                finiView();
                break;
        }
    }

    private void finiView() {
        cacelTimer();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finiView();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (webViewGen != null) {
            webViewGen.clearMemory();
        }
        cacelTimer();
    }

    private static final int TIME_BACK_TAG = 564;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == TIME_BACK_TAG) {
                MyLog.playTask("=====点击网页返回===handleMessage");
                finiView();
            }
        }
    };

    private void startTimer(long timerDelay) {
        cacelTimer();
        timer = new Timer(true);
        task = new TimeWebTask();
        MyLog.playTask("=====点击网页返回===" + timerDelay);
        if (timerDelay < 5) {
            return;
        }
        timer.schedule(task, timerDelay * 1000);
    }

    private void cacelTimer() {
        if (timer != null) {
            timer.cancel();
        }
        if (task != null) {
            task.cancel();
        }
    }

    private Timer timer;
    private TimeWebTask task;

    private class TimeWebTask extends TimerTask {
        @Override
        public void run() {
            handler.sendEmptyMessage(TIME_BACK_TAG);
        }
    }


}
