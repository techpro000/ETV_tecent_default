package com.etv.task.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.listener.TaskPlayStateListener;
import com.etv.task.entity.CpListEntity;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.Biantai;
import com.etv.util.SharedPerUtil;
import com.etv.view.layout.Generator;
import com.ys.model.entity.FileEntity;
import com.etv.listener.VideoPlayListener;
import com.etv.task.entity.MediAddEntity;
import com.etv.util.MyLog;
import com.etv.view.layout.video.media.VideoViewBitmap;
import com.ys.etv.R;
import com.ys.model.dialog.ErrorToastView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/***
 * 视频单独播放控件
 */
public class TaskVideoActivity extends TaskActivity implements View.OnClickListener {

    public static final String TAG_RECEIVE_MESSAGE_VIDEO = "TAG_RECEIVE_MESSAGE_VIDEO";

    @Override
    public void showDownStatuesView(boolean isShow, String desc) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_video);
        MyLog.playTask("===========进入播放视频界面=====");
        initView();
    }

    List<String> videoLists = new ArrayList<String>();
    List<MediAddEntity> videos = new ArrayList<MediAddEntity>();
    LinearLayout lin_exit;
    TextView tv_exit;
    AbsoluteLayout view_abous;
    Generator generatorView = null;

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        videoLists = getIntent().getStringArrayListExtra(TAG_RECEIVE_MESSAGE_VIDEO);
        if (videoLists == null || videoLists.size() < 1) {
            MyLog.cdl("======视频得数量=00===");
            finishView();
            return;
        }
        view_abous = (AbsoluteLayout) findViewById(R.id.view_abous);
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        tv_exit = (TextView) findViewById(R.id.tv_exit);
        lin_exit.setOnClickListener(this);
        tv_exit.setOnClickListener(this);
        MyLog.cdl("======视频得数量===" + videoLists.size());
        for (int i = 0; i < videoLists.size(); i++) {
            MyLog.task("======文件的地址==" + videoLists.get(i));
            long fileSize = -1;
            File file = new File(videoLists.get(i));
            if (file.exists()) {
                fileSize = file.length();
            }
            videos.add(new MediAddEntity(videoLists.get(i).toString(), "0", "1", "0", "70", FileEntity.STYLE_FILE_VIDEO, AppInfo.PROGRAM_TOUCH, fileSize));
        }
        //加载 View
        generatorView = TaskDealUtil.getVideoPlayView(TaskVideoActivity.this, null, 0, 0,
            SharedPerUtil.getScreenWidth(), SharedPerUtil.getScreenHeight(), videos, AppInfo.PROGRAM_POSITION_MAIN, false);
        view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
        generatorView.updateView(null, true);

        generatorView.setPlayStateChangeListener(new TaskPlayStateListener() {
            @Override
            public void playComplete(int playTag) {
                MyLog.video("====视频列表播放完毕了==这里回调====");
            }

            @Override
            public void playCompletePosition(String etLevel, String taskId, int currentPlayPosition, int playTag) {

            }

            @Override
            public void clickTaskView(CpListEntity cpListEntity, List<String> list, int position) {

            }

            @Override
            public void longClickView(CpListEntity cpListEntity, Object object) {

            }

            @Override
            public void reStartPlayProgram(String errorDesc) {
                ErrorToastView.getInstance().Toast(TaskVideoActivity.this, errorDesc);
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lin_exit:
            case R.id.tv_exit:
                finishView();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (generatorView != null) {
            generatorView.clearMemory();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishView();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void finishView() {
        if (Biantai.isOneClick()) {
            return;
        }
        if (generatorView != null) {
            generatorView.clearMemory();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }, 500);
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

}