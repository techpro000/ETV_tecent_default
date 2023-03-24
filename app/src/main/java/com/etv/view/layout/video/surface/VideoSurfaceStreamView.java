package com.etv.view.layout.video.surface;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.etv.listener.TaskPlayStateListener;
import com.etv.listener.VideoPlayListener;
import com.etv.task.entity.CpListEntity;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.ys.bannerlib.BannerConfig;
import com.ys.etv.R;


/***
 * 流媒体，采用SurfaceView 进行渲染
 */
public class VideoSurfaceStreamView extends RelativeLayout implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, OnErrorListener {

    public VideoSurfaceStreamView(Context context) {
        this(context, null);
    }

    public VideoSurfaceStreamView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoSurfaceStreamView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    private SurfaceHolder surfaceHolder;
    private RelativeLayout lin_surface_view;
    LinearLayout lin_progress;

    private void initView(View view) {
        lin_progress = (LinearLayout) view.findViewById(R.id.lin_progress);
        mediaPlayer = new MediaPlayer();
        lin_surface_view = (RelativeLayout) view.findViewById(R.id.lin_surface_view);
        surface_view_video = (SurfaceView) view.findViewById(R.id.surface_view_video);
        surfaceHolder = surface_view_video.getHolder();
//        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.addCallback(this);
    }


    private VideoPlayListener listener;

    public void setVideoPlayListener(VideoPlayListener listener) {
        this.listener = listener;
    }

    public void startToPlayVideo(String playUrl) {
        if (TextUtils.isEmpty(playUrl)) {
            if (listener != null) {
                listener.playError("被播放的素材==NULL");
            }
            return;
        }
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(playUrl);
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {

                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            MyLog.video("=========缓冲进度==start==");
                            if (lin_progress != null) {
                                lin_progress.setVisibility(VISIBLE);
                            }
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            MyLog.video("=========缓冲进度==end==");
                            if (lin_progress != null) {
                                lin_progress.setVisibility(GONE);
                            }
                            break;
                    }
                    return false;
                }
            });
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

    @Override
    public void onPrepared(MediaPlayer mp) {
        int VideoSizeChnage = SharedPerManager.geVideoSingleShowTYpe();
        Log.e("TAG", "onPrepared: "+VideoSizeChnage);
        if (VideoSizeChnage == BannerConfig.SCREEN_SHOW_TYPE_PROPROTIONAL) {  //比例缩放
            int layoutWidth = lin_surface_view.getWidth();
            int layoutHeight = lin_surface_view.getHeight();
            Log.e("TAG", "onPrepared1111: "+layoutWidth+"////"+layoutHeight );
            LinearLayout.LayoutParams params = TaskDealUtil.getLayoutSize(mediaPlayer, layoutWidth, layoutHeight);
            surface_view_video.setLayoutParams(params);
        }
        mp.start();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        MyLog.video("========单机视频=====播放完成=====");
    }

    public void clearMemory() {
        MyLog.video("========单机视频=====清理内存=====");
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (listener != null) {
            listener.initOver();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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
    }

    TaskPlayStateListener taskPlayStateListener;
    CpListEntity cpListEntity;

    public void setVideoClickListen(TaskPlayStateListener taskPlayStateListener, CpListEntity cpListEntity) {
        this.taskPlayStateListener = taskPlayStateListener;
        this.cpListEntity = cpListEntity;
    }

    //清理缓存得View
    public void removeCacheView() {
        try {
            MyLog.video("======执行销毁进程===mediapLayer==removeCacheView");
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
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

}
