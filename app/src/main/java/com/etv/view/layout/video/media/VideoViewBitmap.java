package com.etv.view.layout.video.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.http.util.BitmapWriteLocalRunnable;
import com.etv.listener.TaskPlayStateListener;
import com.etv.listener.VideoPlayListener;
import com.etv.listener.WriteBitmapToLocalListener;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.Biantai;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.system.CpuModel;
import com.ys.bannerlib.BannerConfig;
import com.ys.etv.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频播放控件
 */
public class VideoViewBitmap extends RelativeLayout implements
        TextureView.SurfaceTextureListener {

    View view;
    private MediaPlayer mMediaPlayer;
    private Surface surface;
    private TextureView textureView;
//    private boolean isSetSurface;

    VideoPlayListener listener;
    CpListEntity cpListEntity;
    List<MediAddEntity> playList = new ArrayList<MediAddEntity>();
    int currentPlayIndex = 0;
    Context context;

    /**
     * 直接播放，从0开始
     *
     * @param playUrlList
     */
    public void setPlayList(List<MediAddEntity> playUrlList) {
        setPlayListPosition(playUrlList, 0);
    }

    /**
     * 携带位置，从位置处开始播放
     *
     * @param playUrlList
     * @param playPosition
     */
    public void setPlayListPosition(List<MediAddEntity> playUrlList, int playPosition) {
        currentPlayIndex = playPosition;
        this.playList = playUrlList;
        if (playUrlList.size() < 1) {
            return;
        }
        MediAddEntity mediAddEntity = playList.get(currentPlayIndex);
        MyLog.video("====当前播放的地址==" + mediAddEntity.toString());
        startToPlay(mediAddEntity);
    }

    public void setVideoPlayListener(VideoPlayListener listener, CpListEntity cpListEntity) {
        this.listener = listener;
        this.cpListEntity = cpListEntity;
    }

    public VideoViewBitmap(Context context) {
        this(context, null);
    }

    public VideoViewBitmap(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoViewBitmap(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        MyLog.video("==========init");
        this.context = context;
        view = View.inflate(context, R.layout.view_video, null);
        initView(view);
        addView(view);
        getMediaPlayerInstance();
    }

    private void getMediaPlayerInstance() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
    }

    RelativeLayout rela_no_data;

    TaskPlayStateListener taskPlayStateListener;
    boolean ifViewZero = false;   //点击事件拦截

    public void setVideoClickListen(TaskPlayStateListener taskPlayStateListener, boolean ifViewZero) {
        this.taskPlayStateListener = taskPlayStateListener;
        this.ifViewZero = ifViewZero;
    }

    ImageView video_image;

    private void initView(View view) {
        video_image = (ImageView) view.findViewById(R.id.video_image);
        rela_no_data = (RelativeLayout) view.findViewById(R.id.rela_no_data);
        textureView = (TextureView) view.findViewById(R.id.textureview);
        textureView.setSurfaceTextureListener(this);  //设置监听函数  重写4个方法
        textureView.setOnTouchListener(onTouchListener);
    }

    public void startToPlay(MediAddEntity mediAddEntity) {
        if (mMediaPlayer == null) {
            MyLog.video("==========开始播放 startToPlay " + mMediaPlayer);
            return;
        }
        boolean isFileRight = jujleVideoFileISAll(mediAddEntity);
        if (!isFileRight && playList.size() < 2) {
            if (listener != null) {
                listener.playErrorToStop("播放文件不合法,请检查");
            }
            return;
        }
        String playUrl = mediAddEntity.getUrl();
        rela_no_data.setVisibility(View.GONE);
        int volNum = TaskDealUtil.getMediaVolNum(playList, currentPlayIndex);
        float volNumChangfe = (float) (volNum * 1.0 / 100);
        MyLog.video("==========开始播放 volNum==" + volNum + " / " + currentPlayIndex + " / " + playUrl + "   mMediaPlayer:" + mMediaPlayer);
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(playUrl);
//            mMediaPlayer.setSurface(surface);      //1  隐藏-默认版本   2 显示
            MyLog.video("==========开始播放 volNum==" + volNum + "   mMediaPlayer:" + mMediaPlayer + "     " + this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setVolume(volNumChangfe, volNumChangfe);
            mMediaPlayer.setOnPreparedListener(onPreparedListener);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setOnErrorListener(onErrorListener);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            MyLog.video("播放异常:" + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 判断播放文件是否合法
     *
     * @return
     */
    private boolean jujleVideoFileISAll(MediAddEntity mediAddEntity) {
        int workModel = SharedPerManager.getWorkModel();
        if (workModel == AppInfo.WORK_MODEL_SINGLE) {
            return true;
        }
        try {
            String playUrl = mediAddEntity.getUrl();
            File file = new File(playUrl);
            if (!file.exists()) {
                MyLog.video("===测试视频===播放文件不存在");
                return false;
            }
            long fileSzie = mediAddEntity.getFileSize();
            long fileLocalSize = file.length();
            long disLength = Math.abs(fileSzie - fileLocalSize);
            MyLog.video("===测试视频===播放文件存在" + fileSzie + " / " + fileLocalSize + " / " + (fileSzie - fileLocalSize));
            if (disLength > 1024 * 10) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private OnCompletionListener onCompletionListener = new OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            String playUrl = playList.get(currentPlayIndex).getUrl();
            MyLog.video("0000===========播放结束了======" + currentPlayIndex + " / " + playList.size() + " / " + playUrl);
            try {
                if (listener != null) {
                    listener.playCompletionSplash(currentPlayIndex, playTimeCurrent);  //用来给Splach界面回调的
                    MyLog.video("0000===========播放结束了,执行回调==playCompletionSplash======");
                    if (currentPlayIndex == (playList.size() - 1) || currentPlayIndex > (playList.size() - 1)) {
                        MyLog.d("over", "===over==播放结束了,执行回调==playCompletion======");
                        listener.playCompletion("全部播放结束了，这里回调");
                    }
                }
                currentFrameBitmap = textureView.getBitmap();
                MyLog.video("==currentFrameBitmap==" + (currentFrameBitmap == null));
                if (currentFrameBitmap != null) {
                    MyLog.video("==currentFrameBitmap==" + currentFrameBitmap.getWidth() + " / " + currentFrameBitmap.getHeight());
                    video_image.setVisibility(View.VISIBLE);
                    video_image.setImageBitmap(currentFrameBitmap);
//                    BitmapWriteLocalRunnable bitmapWriteLocalRunnable = new BitmapWriteLocalRunnable(currentFrameBitmap, "/sdcard/cache.jpg", new WriteBitmapToLocalListener() {
//                        @Override
//                        public void writeStatues(boolean isSuccess, String path) {
//                            MyLog.video("==writeStatues==" + isSuccess);
//                        }
//                    });
//                    Thread thread = new Thread(bitmapWriteLocalRunnable);
//                    thread.start();
                }
            } catch (Exception e) {
                MyLog.video("error==onCompletionInfo==" + e.toString(), true);
                e.printStackTrace();
            }
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    playNextVideo();
                }
            }, 50);
        }
    };


    int playTimeCurrent = 15;
    Bitmap currentFrameBitmap = null;


    OnPreparedListener onPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            try {
                playTimeCurrent = (mediaPlayer.getDuration()) / 1000;
                MyLog.video("=====当前播放时长 ：" + playTimeCurrent);
            } catch (Exception e) {
                e.printStackTrace();
                MyLog.video("error==onPreparedListener==" + e.toString(), true);
            }
            int VideoSizeChnage = SharedPerManager.geVideoSingleShowTYpe();
            if (VideoSizeChnage == BannerConfig.SCREEN_SHOW_TYPE_PROPROTIONAL) {  //比例缩放
                LinearLayout.LayoutParams params = TaskDealUtil.getLayoutSize(mediaPlayer, viewSizeWidth, viewSizeHeight);
                textureView.setLayoutParams(params);
            }
            mMediaPlayer.setSurface(surface);
            mediaPlayer.start();
            MyLog.video("开始播放视频文件");
            mHandler.removeCallbacks(runnableClearCache);
            mHandler.postDelayed(runnableClearCache, 800);
        }
    };

    private Handler mHandler = new Handler();
    private Runnable runnableClearCache = new Runnable() {
        @Override
        public void run() {
            video_image.setVisibility(GONE);
            MyLog.video("开始销毁上一个bitmap");
            if (currentFrameBitmap != null) {
                currentFrameBitmap.recycle();
                currentFrameBitmap = null;
            }
        }
    };

    /**
     * 播放异常回调
     */
    MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            MyLog.video("视频播放异常: what= " + what + " /extra =" + extra);
            if (listener != null) {
                listener.playError("播放异常");
            }
            return false;
        }
    };

    public void clearMemory() {
        MyLog.video("======执行销毁进程===mediapLayer==clearMemory " + this);
        try {
            if (currentFrameBitmap != null) {
                video_image.setImageBitmap(null);
                currentFrameBitmap.recycle();
                currentFrameBitmap = null;
            }
            if (mMediaPlayer == null) {
                return;
            }
            MyLog.video("==========mediaplay release clearMemory================== " + hashCode() + "  " + mMediaPlayer.hashCode());
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        } catch (Exception e) {
            MyLog.video("======执行销毁进程===mediapLayer==clearMemory==" + e);
            e.printStackTrace();
        }
    }

    //清理缓存得View
    public void removeCacheView() {
        MyLog.video("======执行销毁进程===mediapLayer==removeCacheView");
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
            }
        } catch (Exception e) {
            MyLog.video("======执行销毁进程===mediapLayer==" + e);
            e.printStackTrace();
        }

        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                MyLog.video("==========mediaplay reset================== " + hashCode() + "   " + mMediaPlayer.hashCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (mMediaPlayer != null) {
                MyLog.video("==========mediaplay release=removeCacheView================= " + hashCode() + "   " + mMediaPlayer.hashCode());
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (Exception e) {
            MyLog.video("======执行销毁进程===mediapLayer==" + e);
            e.printStackTrace();
        }

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        surface = new Surface(surfaceTexture);
        if (listener != null) {
            listener.initOver();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        MyLog.video("======执行销毁进程===onSurfaceTextureDestroyed==clearMemory");
        surface = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    /***
     * 播放下一个
     */
    private void playNextVideo() {
        try {
            if (playList == null || playList.size() < 1) {
                listener.reStartPlayProgram("播放下一个,出错了，这里执行播放完成,跳过这里");
                return;
            }

            currentPlayIndex++;
            if (currentPlayIndex > (playList.size() - 1)) {
                currentPlayIndex = 0;
            }
            MediAddEntity mediAddEntity = playList.get(currentPlayIndex);
            startToPlay(mediAddEntity);
        } catch (Exception e) {
            MyLog.video(e.toString(), true);
            e.printStackTrace();
        }
    }

    /**
     * 播放上一个视频
     */
    private void playProVideo() {
        try {
            if (playList == null || playList.size() < 1) {
                listener.reStartPlayProgram("播放上一个,出错了，这里执行播放完成,跳过这里");
                return;
            }
            currentPlayIndex--;
            if (currentPlayIndex < 0) {
                currentPlayIndex = playList.size() - 1;
            }
            MediAddEntity mediAddEntity = playList.get(currentPlayIndex);
            startToPlay(mediAddEntity);
        } catch (Exception e) {
            MyLog.video("播放上一个视频： " + e.toString(), true);
            e.printStackTrace();
        }
    }

    /**
     * 暂停播放
     */
    public void pausePlayVideo() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    /**
     * 恢复播放
     */
    public void resumePlayVideo() {
        if (mMediaPlayer == null) {
            return;
        }
        if (playList == null || playList.size() < 1) {
            return;
        }
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    /**
     * 快进或者快退
     *
     * @param b
     */
    public void moveViewForward(boolean b) {
        if (mMediaPlayer == null) {
            return;
        }
        if (playList == null || playList.size() < 2) {
            return;
        }
        if (b) {  //快进
            playNextVideo();
        } else {//快退
            playProVideo();
        }
    }


    float downX, upX;
    long touchDownTime = 0;
    private OnTouchListener onTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    touchDownTime = System.currentTimeMillis();
                    downX = motionEvent.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    upX = motionEvent.getX();
                    if (downX - upX > 200) { //向左滑动
                        if (playList.size() == 1) {
                            MyLog.cdl("===滑动==消费掉=");
                            return true;
                        }
                        if (SharedPerManager.getWorkModel() == AppInfo.WORK_MODEL_SINGLE) { //处理单机模式
                            playNextVideo();
                            return true;
                        }
                        String pmType = playList.get(currentPlayIndex).getPmType();
                        if (pmType.contains(AppInfo.PROGRAM_TOUCH)) {
                            playNextVideo();
                        }
                    } else if (upX - downX > 200) { //向右滑动
                        if (playList.size() == 1) {
                            MyLog.cdl("===滑动==消费掉=");
                            return true;
                        }
                        if (SharedPerManager.getWorkModel() == AppInfo.WORK_MODEL_SINGLE) { //处理单机模式
                            playProVideo();
                            return true;
                        }
                        MyLog.video("===播放上一个=" + currentPlayIndex + " / " + playList.size());
                        String pmType = playList.get(currentPlayIndex).getPmType();
                        if (pmType.contains(AppInfo.PROGRAM_TOUCH)) {
                            playProVideo();
                        }
                    } else { //执行点击事件
                        MyLog.cdl("===滑动=点击了视频=点击事件=");
                        long currentTime = System.currentTimeMillis();
                        long distanceTime = currentTime - touchDownTime;
                        clickApEntityBack(distanceTime);
                    }
                    break;
            }
            return true;
        }
    };

    /**
     * 执行点击事件
     */
    private void clickApEntityBack(long distanceTime) {
        if (ifViewZero) {
            return;
        }
        if (taskPlayStateListener == null) {
            return;
        }
        if (distanceTime > 2000) {
            //这里执行长按事件
            MyLog.playTask("=====点击了视频===长按事件拦截");
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

    /**
     * 播放异常了，去播放下一个视频
     * 1:先判断当前节目里面有几个视频
     * 2：如果只有一个，就中断播放，显示UI，2秒后执行播放完成的操作
     * 3：如果有多个，就移除当前的这个，
     * 4：再次判断，如果仍有多个就播放下一个
     * 5：没有下一个的话，就停止播放
     */
//    private void errorToPlayNextVideo() {
//        try {
//            MyLog.video("===播放异常===playList。size=" + playList.size());
//            if (playList == null || playList.size() < 2) {
//                rela_no_data.setVisibility(View.VISIBLE);
//                handler.sendEmptyMessageDelayed(VIDEO_OVER_TO_NEXT, 2000);
//                return;
//            }
//            MyLog.video("===播放异常===数据有多的==");
//            playList.remove(currentPlayIndex);
//            currentPlayIndex--;
//            MyLog.video("===播放异常===移除数据后位置==" + currentPlayIndex + "/" + playList.size());
//            //移除数据后再次判断
//            if (playList == null || playList.size() < 1) {
//                MyLog.video("===播放异常===移除数据后再次判断==显示图片");
//                rela_no_data.setVisibility(View.VISIBLE);
//                handler.sendEmptyMessageDelayed(VIDEO_OVER_TO_NEXT, 2000);
//                return;
//            }
//            MyLog.video("===播放异常===去播放下一个");
//            playNextVideo();
//        } catch (Exception e) {
//            MyLog.video("===播放异常===去播放下一个" + e.toString(), true);
//            e.printStackTrace();
//        }
//    }
}


