package com.etv.view.layout.video.surface;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.listener.TaskPlayStateListener;
import com.etv.listener.VideoPlayListener;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.Biantai;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.system.CpuModel;
import com.etv.view.layout.video.rtsp.HolderCallback;
import com.ys.bannerlib.BannerConfig;
import com.ys.etv.R;

import java.util.ArrayList;
import java.util.List;

public class VideoSurfaceView extends RelativeLayout implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, OnErrorListener {

    public VideoSurfaceView(Context context) {
        this(context, null);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        view = View.inflate(context, R.layout.view_video_surface, null);
        initView(view);
        addView(view);
    }

    View view;
    Context context;
    private SurfaceView surface_view_video;
    private MediaPlayer mediaPlayer;
    private RelativeLayout lin_surface_view;

    private void initView(View view) {
        mediaPlayer = new MediaPlayer();
        lin_surface_view = (RelativeLayout) view.findViewById(R.id.lin_surface_view);
        surface_view_video = (SurfaceView) view.findViewById(R.id.surface_view_video);
//        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //surfaceHolder.setKeepScreenOn(true);
        surface_view_video.setOnTouchListener(onTouchListener);
    }


    float downX, upX;
    long clickDownTime = 0;
    long clickUpTime = 0;
    private OnTouchListener onTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    clickDownTime = System.currentTimeMillis();
                    clickUpTime = System.currentTimeMillis();
                    downX = motionEvent.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    clickUpTime = System.currentTimeMillis();
                    upX = motionEvent.getX();
                    if (downX - upX > 200) { //向左滑动
                        if (playList.size() == 1) {
                            MyLog.cdl("===滑动==消费掉=");
                            return true;
                        }
                        if (SharedPerManager.getWorkModel() == AppInfo.WORK_MODEL_SINGLE) { //处理单机模式
                            playNextPositionVideoPlayer();
                            return true;
                        }
                        String pmType = playList.get(currentPlayIndex).getPmType();
                        if (pmType.contains(AppInfo.PROGRAM_TOUCH)) {
                            playNextPositionVideoPlayer();
                        }
                    } else if (upX - downX > 200) { //向右滑动
                        if (playList.size() == 1) {
                            MyLog.cdl("===滑动==消费掉=");
                            return true;
                        }
                        if (playList.size() == 1) {
                            MyLog.cdl("===滑动==消费掉=");
                            return true;
                        }
                        if (SharedPerManager.getWorkModel() == AppInfo.WORK_MODEL_SINGLE) { //处理单机模式
                            playPrePositionVideoPlayer();
                            return true;
                        }
                        MyLog.video("===播放上一个=" + currentPlayIndex + " / " + playList.size());
                        String pmType = playList.get(currentPlayIndex).getPmType();
                        if (pmType.contains(AppInfo.PROGRAM_TOUCH)) {
                            playPrePositionVideoPlayer();
                        }
                    } else { //执行点击事件
                        clickApEntityBack();
                    }
                    break;
            }
            return true;
        }
    };

    /**
     * 执行点击事件
     */
    private void clickApEntityBack() {
        if (ifViewZero) {
            return;
        }
        if (taskPlayStateListener == null) {
            return;
        }
        if (clickUpTime - clickDownTime > 3000) {
            //这里执行长按事件
            taskPlayStateListener.longClickView(cpListEntity, null);
            return;
        }
        if (Biantai.isOneClick()) {
            MyLog.playTask("=====点击了视频===000暴力测试拦截");
            return;
        }
        MyLog.playTask("=====点击了视频===000");

        if (playList == null || playList.size() < 1) {
            return;
        }
        List<String> listsShow = new ArrayList<String>();
        for (int i = 0; i < playList.size(); i++) {
            listsShow.add(playList.get(i).getUrl());
        }
        taskPlayStateListener.clickTaskView(cpListEntity, listsShow, 0);
    }


    private VideoPlayListener listener;

    public void setVideoPlayListener(VideoPlayListener listener) {
        this.listener = listener;
    }

    List<MediAddEntity> playList;
    private int currentPlayIndex = 0;

    /**
     * 直接播放，从0开始
     *
     * @param playUrlList
     */
    public void setPlayList(List<MediAddEntity> playUrlList) {
        this.playList = playUrlList;
        if (playList == null) {
            if (listener != null) {
                listener.playError("没有需要播放的信息");
            }
            return;
        }
        currentPlayIndex = 0;
        MediAddEntity mediAddEntity = playList.get(currentPlayIndex);
        startToPlayVideo(mediAddEntity);
    }

    public void startToPlayVideo(MediAddEntity mediAddEntity) {
        if (mediAddEntity == null) {
            if (listener != null) {
                listener.playError("被播放的素材==NULL");
            }
            return;
        }
        MyLog.video("==========开始播放 volNum==" +  mediAddEntity.toString());
        int volNum = TaskDealUtil.getMediaVolNum(playList, currentPlayIndex);
        float volNumChangfe = (float) (volNum * 1.0 / 100);
        String playUrl = mediAddEntity.getUrl();
        MyLog.video("==========开始播放 volNum==" + volNum + " / " + currentPlayIndex + " / " + playUrl);
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(playUrl);
            mediaPlayer.setDisplay(null);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(volNumChangfe, volNumChangfe);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            MyLog.video("播放异常:" + e.toString());
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (listener != null) {
            listener.playError("播放视频异常了");
        }
        return false;
    }

    int Width = 0;
    int Height = 0;
    boolean isChange = true;
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (surface_view_video == null) {
            MyLog.video("========onPrepared===surface_view_video null====", true);
            return;
        }
        SurfaceHolder holder = surface_view_video.getHolder();
        if (!holder.getSurface().isValid()) {
            MyLog.video("========onPrepared===surface_view_video is invalid====", true);
            return;
        }
        int VideoSizeChnage = SharedPerManager.geVideoSingleShowTYpe();
        if (VideoSizeChnage == BannerConfig.SCREEN_SHOW_TYPE_PROPROTIONAL) {  //比例缩放
            Log.e("TAG", "onPrepared: "+VideoSizeChnage );
            if (CpuModel.isMLogic()){
                if (isChange){
                    int layoutWidth = lin_surface_view.getWidth();
                    int layoutHeight = lin_surface_view.getHeight();
                    Width = layoutWidth;
                    Height = layoutHeight;
                    Log.e("TAG", "onPrepared888: " + layoutWidth + "////" + layoutHeight);
                    LinearLayout.LayoutParams params = TaskDealUtil.getLayoutSize(mediaPlayer, layoutWidth, layoutHeight);
                    surface_view_video.setLayoutParams(params);
                    mp.setDisplay(holder);
                    mp.start();
                    isChange = false;
                }else {
                    Log.e("TAG", "onPrepared999: " + Width + "////" + Height);
                    LinearLayout.LayoutParams params = TaskDealUtil.getLayoutSize(mediaPlayer, Width, Height);
                    surface_view_video.setLayoutParams(params);
                    mp.setDisplay(holder);
                    mp.start();
                }
                return;
            }
//            int layoutWidth = lin_surface_view.getWidth();
//            int layoutHeight = lin_surface_view.getHeight();
            int layoutWidth = viewSizeWidth;
            int layoutHeight = viewSizeHeight;
            Log.e("TAG", "onPrepared666: "+layoutWidth+"////"+layoutHeight );
            LinearLayout.LayoutParams params = TaskDealUtil.getLayoutSize(mediaPlayer, layoutWidth, layoutHeight);
            surface_view_video.setLayoutParams(params);
        }
        mp.setDisplay(holder);
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        MyLog.video("========单机视频=====播放完成=====");
        playNextPositionVideoPlayer();
    }

    private void playNextPositionVideoPlayer() {
        MyLog.video("========单机视频======播放下一个====");
        Log.e("TAG", "playNextPositionVideoPlayer: "+viewSizeWidth+"/////"+viewSizeHeight );
        currentPlayIndex++;
        if (currentPlayIndex > playList.size() - 1) {
            if (listener != null) {
                listener.playCompletion("全部播放完毕了");
            }
            currentPlayIndex = 0;
        } else {
            if (listener != null) {
                listener.playCompletionSplash(currentPlayIndex, 15);
            }
        }
        MediAddEntity mediAddEntity = playList.get(currentPlayIndex);
        startToPlayVideo(mediAddEntity);
    }

    private void stopPlay() {
        MyLog.video("========单机视频=====停止播放=====");
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void clearMemory() {
        MyLog.video("========单机视频=====清理内存=====");
        if (mediaPlayer == null) {
            return;
        }
        stopPlay();
        mediaPlayer.release();
    }

    /**
     * 恢复播放的功能
     */
    public void resumePlayView() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            return;
        }
        mediaPlayer.start();
    }

    public void pauseDisplayView() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void moveViewForward(boolean b) {
        if (b) {
            playNextPositionVideoPlayer();
        } else {
            playPrePositionVideoPlayer();
        }
    }

    private void playPrePositionVideoPlayer() {
        try {
            if (playList == null || playList.size() < 1) {
                listener.reStartPlayProgram("播放上一个,出错了，这里执行播放完成,跳过这里");
                return;
            }
            currentPlayIndex--;
            if (currentPlayIndex < 0) {
                currentPlayIndex = playList.size();
            }
            MediAddEntity mediAddEntity = playList.get(currentPlayIndex);
            startToPlayVideo(mediAddEntity);
        } catch (Exception e) {
            MyLog.video("播放上一个视频： " + e.toString(), true);
            e.printStackTrace();
        }
    }

    int viewSizeWidth = 0;
    int viewSizeHeight = 0;

    /***
     * 计算显示区域得尺寸
     * @param width
     * @param height
     */
    public void setViewSize(int width, int height) {
        viewSizeWidth = width;
        viewSizeHeight = height;
        Log.e("TAG", "setViewSize: "+viewSizeWidth+"/////;"+viewSizeHeight );
    }

    TaskPlayStateListener taskPlayStateListener;
    CpListEntity cpListEntity;
    boolean ifViewZero = false;

    public void setVideoClickListen(TaskPlayStateListener taskPlayStateListener, CpListEntity cpListEntity, boolean ifViewZero) {
        this.taskPlayStateListener = taskPlayStateListener;
        this.cpListEntity = cpListEntity;
        this.ifViewZero = ifViewZero;
    }


    //清理缓存得View
    public void removeCacheView() {
        try {
            MyLog.video("======执行销毁进程===mediapLayer==removeCacheView");
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
        } catch (Exception e) {
            MyLog.video("======执行销毁进程===mediapLayer==" + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 暂停播放
     */
    public void pausePlayVideo() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    /**
     * 恢复播放
     */
    public void resumePlayVideo() {
        if (mediaPlayer == null) {
            return;
        }
        if (playList == null || playList.size() < 1) {
            return;
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

}
