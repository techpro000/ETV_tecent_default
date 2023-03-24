package com.etv.task.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
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
    LinearLayout iv_no_data;
    VideoViewBitmap video_single;
    int playPosition = 0;
    LinearLayout lin_exit;
    TextView tv_exit;

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        tv_exit = (TextView) findViewById(R.id.tv_exit);
        lin_exit.setOnClickListener(this);
        tv_exit.setOnClickListener(this);
        iv_no_data = (LinearLayout) findViewById(R.id.iv_no_data);
        videoLists = getIntent().getStringArrayListExtra(TAG_RECEIVE_MESSAGE_VIDEO);
        if (videoLists == null || videoLists.size() < 1) {
            MyLog.cdl("======视频得数量  00===");
            iv_no_data.setVisibility(View.VISIBLE);
            return;
        }
        MyLog.cdl("======视频得数量===" + videoLists.size());
        iv_no_data.setVisibility(View.GONE);
        for (int i = 0; i < videoLists.size(); i++) {
            MyLog.task("======文件的地址==" + videoLists.get(i));
            long fileSize = -1;
            File file = new File(videoLists.get(i));
            if (file.exists()) {
                fileSize = file.length();
            }
            videos.add(new MediAddEntity(videoLists.get(i).toString(), "0", "1", "0", "70", FileEntity.STYLE_FILE_VIDEO, AppInfo.PROGRAM_TOUCH, fileSize));
        }
        video_single = (VideoViewBitmap) findViewById(R.id.video_single);
        video_single.setVideoPlayListener(new VideoPlayListener() {
            @Override
            public void initOver() {
//                video_single.setPlayList(videos);
                video_single.setPlayListPosition(videos, playPosition);
            }

            @Override
            public void playCompletion(String tag) {
                MyLog.video("====视频列表播放完毕了==这里回调====" + tag);
                finish();
            }

            @Override
            public void playCompletionSplash(int position, int playTimeCurrent) {

            }

            @Override
            public void playError(String errorDesc) {
                ErrorToastView.getInstance().Toast(TaskVideoActivity.this, errorDesc);
                finish();
            }

            @Override
            public void playErrorToStop(String errorDesc) {

            }

            @Override
            public void reStartPlayProgram(String errorDesc) {
                startToMainTaskView();
            }
        }, null);
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
        if (video_single != null) {
            video_single.clearMemory();
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
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
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