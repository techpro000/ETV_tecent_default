package com.etv.view.layout.video.stream;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.util.MyLog;
import com.etv.util.TimerDealUtil;
import com.etv.view.layout.Generator;
import com.etv.view.layout.video.view.EmptyControlVideo;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;
import com.ys.etv.R;

import java.util.List;

/**
 * 采用IJK播放框架
 * 直播流媒体播放工具类
 */
public class ViewIJKStreamGenerate extends Generator {

    View view;
    CpListEntity cpListEntity;

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    public ViewIJKStreamGenerate(Context context, CpListEntity cpListEntity, int x, int y, int width, int height,
                                 String streamUrl) {
        super(context, x, y, width, height);
        this.cpListEntity = cpListEntity;
        view = View.inflate(context, R.layout.view_video_udp_player, null);
        TimerDealUtil.getInstance().addGeneratorToList(this);
        parsenerStreamList(streamUrl);
    }

    String[] streamList = null;

    private void parsenerStreamList(String streamUrl) {
        streamList = streamUrl.split(",");
        if (streamList == null || streamList.length < 1) {
            showWaitDialog(true, "Url Is Null ");
            return;
        }
        initView(view);
    }

    EmptyControlVideo videoPlayer;
    TextView click_view;
    LinearLayout lin_progress;   //缓冲等待框
    TextView tv_dialog_wait;     //等待提示
    private int CURRENT_PLAY_POSITION = 0;

    private void initView(View view) {
        lin_progress = (LinearLayout) view.findViewById(R.id.lin_progress);
        tv_dialog_wait = (TextView) view.findViewById(R.id.tv_dialog_wait);
        showWaitDialog(true, "Loading...");
        videoPlayer = (EmptyControlVideo) view.findViewById(R.id.video_player);
        click_view = (TextView) view.findViewById(R.id.click_view);
        click_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener == null) {
                    return;
                }
                listener.clickTaskView(cpListEntity, null, 0);
            }
        });

        click_view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.longClickView(cpListEntity, null);
                }
                return true;
            }
        });
        startPlayVideo(streamList[CURRENT_PLAY_POSITION]);
    }

    public void startPlayVideo(String videoUrl) {
        showWaitDialog(true, "Loading...");
        MyLog.playTask("======流媒体===startPlayVideo===" + CURRENT_PLAY_POSITION + " / " + streamList.length);
        videoPlayer.setUp(videoUrl, true, "");
        videoPlayer.startPlayLogic();
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
    }

    @Override
    public void playComplet() {

    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

    @Override
    public void removeCacheView(String tag) {
        if (videoPlayer != null) {
            videoPlayer.release();
        }
    }

    @Override
    public void clearMemory() {
        TimerDealUtil.getInstance().removeGeneratorToList(this);
        MyLog.playTask("======流媒体=====clearMemory=");
        try {
            pausePlayVideo();
            if (videoPlayer != null) {
                videoPlayer.setVideoAllCallBack(null);
            }
            GSYVideoManager.releaseAllVideos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停播放
     */
    private void pausePlayVideo() {
        MyLog.playTask("======流媒体=====pausePlayVideo=");
        if (videoPlayer == null) {
            return;
        }
        try {
            if (videoPlayer.isInPlayingState()) {
                videoPlayer.onVideoPause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void timeChangeToUpdateView() {
        if (videoPlayer == null) {
            return;
        }
        int playStatuesCurrent = videoPlayer.getCurrentState();
        MyLog.playTask("==========流媒体播放状态检测=====" + playStatuesCurrent);
        switch (playStatuesCurrent) {
            case GSYVideoView.CURRENT_STATE_NORMAL:  //正常
                break;
            case GSYVideoView.CURRENT_STATE_PREPAREING:    //准备中
                showWaitDialog(true, "Loading...");
                break;
            case GSYVideoView.CURRENT_STATE_PLAYING:     //播放中
                showWaitDialog(false, "Loading...");
                break;
            case GSYVideoView.CURRENT_STATE_PLAYING_BUFFERING_START:     //开始缓冲
                showWaitDialog(true, "Loading...");
                break;
            case GSYVideoView.CURRENT_STATE_PAUSE:     //暂停
                break;
            case GSYVideoView.CURRENT_STATE_AUTO_COMPLETE:     //自动播放结束
                break;
            case GSYVideoView.CURRENT_STATE_ERROR:     //错误状态
                showWaitDialog(true, "ERROR");
                break;
        }
    }

    private void showWaitDialog(boolean isShow, String desc) {
        tv_dialog_wait.setText(desc);
        lin_progress.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void moveViewForward(boolean b) {
        if (streamList == null || streamList.length < 2) {
            return;
        }
        if (b) {
            CURRENT_PLAY_POSITION++;
            if (CURRENT_PLAY_POSITION > (streamList.length - 1)) {
                CURRENT_PLAY_POSITION = 0;
            }
        } else {
            CURRENT_PLAY_POSITION--;
            if (CURRENT_PLAY_POSITION < 0) {
                CURRENT_PLAY_POSITION = streamList.length - 1;
            }
        }
        startPlayVideo(streamList[CURRENT_PLAY_POSITION]);
    }

    @Override
    public void pauseDisplayView() {
//        TimerDealUtil.getInstance().pauseOrResumeTimerInfo(false);
//        MyLog.playTask("=====流媒体IJK=====暂停");
//        pausePlayVideo();
    }

    @Override
    public void resumePlayView() {
//        TimerDealUtil.getInstance().pauseOrResumeTimerInfo(true);
//        MyLog.playTask("=====流媒体IJK=====恢复000");
//        if (videoPlayer == null) {
//            MyLog.playTask("=====流媒体IJK=====恢复111");
//            return;
//        }
//        try {
//            MyLog.playTask("=====流媒体IJK=====恢复222==" + videoPlayer.isInPlayingState());
//        if (!videoPlayer.isInPlayingState()) {
//            MyLog.playTask("=====流媒体IJK=====恢复333");
//            videoPlayer.onVideoResume();
//        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void updateTextInfo(Object object) {

    }

}
