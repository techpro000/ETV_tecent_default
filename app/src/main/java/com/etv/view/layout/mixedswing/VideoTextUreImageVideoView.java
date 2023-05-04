package com.etv.view.layout.mixedswing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.etv.config.AppInfo;
import com.etv.db.DbStatiscs;
import com.etv.entity.StatisticsEntity;
import com.etv.listener.TaskPlayStateListener;
import com.etv.listener.VideoPlayListener;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.ys.bannerlib.util.GlideImageUtil;
import com.ys.etv.R;
import com.ys.model.entity.FileEntity;

import java.util.List;

/***
 * 混播控件
 * textUreView
 */
public class VideoTextUreImageVideoView extends RelativeLayout implements TextureView.SurfaceTextureListener {

    public VideoTextUreImageVideoView(Context context) {
        this(context, null);
    }

    public VideoTextUreImageVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoTextUreImageVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        view = View.inflate(context, R.layout.view_img_video_texture, null);
        initView(view);
        addView(view);
    }

    View view;
    Context context;
    private TextureView textureView;
    private MediaPlayer mediaPlayer;
    ImageView iv_view_video;
    ImageView iv_view_image;
    long startDownTime = 0;
    long startUpTime = 0;
    private Surface surface;


    private void initView(View view) {
        mediaPlayer = new MediaPlayer();
        iv_view_image = (ImageView) view.findViewById(R.id.iv_view_image);
        iv_view_video = (ImageView) view.findViewById(R.id.iv_view_video);
        textureView = (TextureView) view.findViewById(R.id.surface_view_video);
        textureView.setSurfaceTextureListener(this);  //设置监听函数  重写4个方法
        textureView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        startDownTime = System.currentTimeMillis();
                        MyLog.task("====setOnTouchListener====ACTION_DOWN=======");
                        break;
                    case MotionEvent.ACTION_UP:
                        startUpTime = System.currentTimeMillis();
                        MyLog.task("=====setOnTouchListener==ACTION_UP========");
                        if (startUpTime - startDownTime > 1000) {
                            MyLog.task("=====setOnTouchListener==ACTION_UP====deal====");
                            if (taskPlayStateListener != null) {
                                taskPlayStateListener.longClickView(cpListEntity, null);
                            }
                        }
                        startDownTime = 0;
                        startUpTime = 0;
                        break;
                }
                return true;
            }
        });
        if (currentFrameBitmap != null) {
            iv_view_video.setImageBitmap(currentFrameBitmap);
        }
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
        currentPlayIndex = 0;
        if (playList == null || playList.size() < 1) {
            MyLog.video("setPlayList==000");
            if (listener != null) {
                listener.playError("没有需要播放的信息");
            }
            return;
        }
        MyLog.video("setPlayList==111==" + playUrlList.size());
        MediAddEntity mediAddEntity = playList.get(currentPlayIndex);
        startToPlayMedia(mediAddEntity);
    }

    private void startToPlayMedia(MediAddEntity mediAddEntity) {
        int mediaType = mediAddEntity.getFileType();
        MyLog.video("startToPlayMedia==mediaType=" + mediaType);
        if (mediaType == FileEntity.STYLE_FILE_IMAGE) {
            MyLog.video("startToPlayMedia==准备播放图片");
            startToPlayImage(mediAddEntity);
        } else if (mediaType == FileEntity.STYLE_FILE_VIDEO) {
            MyLog.video("startToPlayMedia==准备播放视屏");
            mHandler.removeMessages(TIME_DELAY);
            startToPlayVideo(mediAddEntity);
        }
    }

    public void toUpdatePlayNum(MediAddEntity mediAddEntity, int duartionTime) {
        int fileType = mediAddEntity.getFileType();
        String midId = mediAddEntity.getMidId();
        String resourType = AppInfo.VIEW_IMAGE_VIDEO;
        String playTime = mediAddEntity.getPlayParam();
        MyLog.video("toUpdatePlayNum=" + fileType + " / " + playTime);
        if (fileType == FileEntity.STYLE_FILE_IMAGE) {
            resourType = AppInfo.VIEW_IMAGE;
        } else if (fileType == FileEntity.STYLE_FILE_VIDEO) {
            resourType = AppInfo.VIEW_VIDEO;
        }
        StatisticsEntity statisticsEntity = new StatisticsEntity(midId, resourType, duartionTime, 1, System.currentTimeMillis());
        DbStatiscs.saveStatiseToLocal(statisticsEntity, "Video视频添加统计");
    }

    int playTime = 10;

    private void startToPlayImage(MediAddEntity mediAddEntity) {
        String delayTime = "10";
        int workModel = SharedPerManager.getWorkModel();
        if (workModel == AppInfo.WORK_MODEL_SINGLE) {
            delayTime = SharedPerManager.getPicDistanceTime() + "";
        } else {
            delayTime = mediAddEntity.getPlayParam();
        }
        try {
            playTime = Integer.parseInt(delayTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (playTime < 3) {
            playTime = 3;
        }
        toUpdatePlayNum(mediAddEntity, playTime);
        MyLog.video("==setVisibility==加载图片====VISIBLE===");
        iv_view_video.setVisibility(VISIBLE);
        iv_view_image.setVisibility(VISIBLE);
        String imagePath = mediAddEntity.getUrl();
        try {
            GlideImageUtil.loadImageByPath(context, imagePath, iv_view_image);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    GlideImageUtil.loadImageByPath(context, imagePath, iv_view_video);
                }
            }, 650);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MyLog.video("========单张图片开始播放=====" + playTime + "/ imagePath = " + imagePath, true);
        mHandler.removeMessages(TIME_DELAY);
        mHandler.sendEmptyMessageDelayed(TIME_DELAY, playTime * 1000);
    }

    public void startToPlayVideo(MediAddEntity mediAddEntity) {
        if (mediAddEntity == null) {
            if (listener != null) {
                listener.playError("被播放的素材==NULL");
            }
            return;
        }
        int volNum = TaskDealUtil.getMediaVolNum(playList, currentPlayIndex);
        float volNumChangfe = (float) (volNum * 1.0 / 100);
        String playUrl = mediAddEntity.getUrl();
        MyLog.video("开始播放volNum==" + volNum + " / " + currentPlayIndex + " / " + playUrl);
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(playUrl);
            MyLog.video("开始播放==" + (surface == null));
            mediaPlayer.setSurface(surface);      //1  隐藏-默认版本   2 显示
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(volNumChangfe, volNumChangfe);
            mediaPlayer.setOnPreparedListener(onPreparedListener);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.setOnErrorListener(onErrorListener);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            MyLog.video("播放异常:" + e.toString());
        }
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            MyLog.video("播放完成====onCompletion=");
            currentFrameBitmap = textureView.getBitmap();
            if (currentFrameBitmap != null) {
                iv_view_video.setVisibility(View.VISIBLE);
                iv_view_video.setImageBitmap(currentFrameBitmap);
                MyLog.video("==setVisibility==播放视频完成====VISIBLE===");
                iv_view_image.setVisibility(View.GONE);
            }
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    toPlayNextMediaInfo("視屏播放完成回調");
                }
            }, 50);
        }
    };

    Bitmap currentFrameBitmap = null;

    /**
     * 播放异常回调
     */
    MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            MyLog.video("视频播放异常:1 what= " + what + " /extra =" + extra);
            if (listener != null) {
                listener.playError("播放视频异常了:" + what + " / " + extra);
            }
            return false;
        }
    };

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            MyLog.video("onPrepared==准备播放节目==");
            mediaPlayer.start();
            mHandler.removeCallbacks(runnableClearCache);
            mHandler.postDelayed(runnableClearCache, 200);

            //提交播放统计
            long videoDuartion = mediaPlayer.getDuration();
            MyLog.video("视频播放时长=" + videoDuartion);
            MediAddEntity mediAddEntity = playList.get(currentPlayIndex);
            toUpdatePlayNum(mediAddEntity, (int) (videoDuartion / 1000));
        }
    };

    private Runnable runnableClearCache = new Runnable() {
        @Override
        public void run() {
            iv_view_image.setVisibility(GONE);
            iv_view_video.setVisibility(GONE);
            MyLog.video("==setVisibility==開始刷新視屏界面====GONE===");
            if (currentFrameBitmap != null) {
                currentFrameBitmap.recycle();
                currentFrameBitmap = null;
            }
            //显示完成之后，清理缓存。防止图片加载太多，无销毁
            GlideImageUtil.clearViewCache(context, iv_view_image);
            GlideImageUtil.clearViewCache(context, iv_view_video);
        }
    };
    private static final int TIME_DELAY = 78954;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            mHandler.removeMessages(msg.what);
            if (msg.what == TIME_DELAY) {
                toPlayNextMediaInfo("圖片播放完成");
            }
        }
    };

    private void toPlayNextMediaInfo(String printTag) {
        currentPlayIndex++;
        MyLog.video("toPlayNextMediaInfo-==播放下一個====" + currentPlayIndex + " / " + (playList.size()));
        if (currentPlayIndex > playList.size() - 1) {
            MyLog.video("toPlayNextMediaInfo-==整體播放完成====" + printTag);
            if (listener != null) {
                listener.playCompletion("播放完毕了");
            }
            currentPlayIndex = 0;
        } else {
            MyLog.video("toPlayNextMediaInfo-==播放单个完成====" + printTag);
        }
        MediAddEntity mediAddEntity = playList.get(currentPlayIndex);
        startToPlayMedia(mediAddEntity);
    }


    private void stopPlay() {
        MyLog.video("========混播=====停止播放=====");
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void clearMemory() {
        if (mHandler != null) {
            mHandler.removeCallbacks(runnableClearCache);
            mHandler.removeMessages(TIME_DELAY);
        }
        MyLog.video("========混播=====清理内存=====");
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

    TaskPlayStateListener taskPlayStateListener;
    CpListEntity cpListEntity;

    public void setVideoClickListen(TaskPlayStateListener taskPlayStateListener, CpListEntity cpListEntity) {
        this.taskPlayStateListener = taskPlayStateListener;
        this.cpListEntity = cpListEntity;
    }

    //清理缓存得View
    public void removeCacheView() {
        try {
            if (mHandler != null) {
                mHandler.removeCallbacks(runnableClearCache);
                mHandler.removeMessages(TIME_DELAY);
            }
            MyLog.video("======执行销毁进程===mediapLayer==removeCacheView");
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            GlideImageUtil.clearViewCache(context, iv_view_image);
            GlideImageUtil.clearViewCache(context, iv_view_video);
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

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        MyLog.video("=viewChange=onSurfaceTextureAvailable====");
        surface = new Surface(surfaceTexture);
        if (listener != null) {
            listener.initOver();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        MyLog.video("=viewChange=onSurfaceTextureSizeChanged====");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        MyLog.video("=viewChange=onSurfaceTextureDestroyed====");
        surface = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        MyLog.video("=viewChange=onSurfaceTextureUpdated====");
    }

//    2023-04-24 18:43:04.788 11693-11693/com.ys.etv E/videoPlay: ==textureView===视频区域得坐标====0 / 0 / 1280 /800
//        2023-04-24 18:43:04.815 11693-11693/com.ys.etv E/videoPlay: setPlayList==111==2
//        2023-04-24 18:43:04.815 11693-11693/com.ys.etv E/videoPlay: startToPlayMedia==mediaType=2
//        2023-04-24 18:43:04.815 11693-11693/com.ys.etv E/videoPlay: startToPlayMedia==准备播放视屏
//2023-04-24 18:43:04.815 11693-11693/com.ys.etv E/videoPlay: 开始播放volNum==70 / 0 / /storage/emulated/0/etv/task/1593067305618202624.mp4
//2023-04-24 18:43:04.851 11693-11693/com.ys.etv E/videoPlay: =viewChange=onSurfaceTextureAvailable====
//        2023-04-24 18:43:04.852 11693-11693/com.ys.etv E/videoPlay: ====初始化完成===
//        2023-04-24 18:43:04.963 11693-11693/com.ys.etv E/videoPlay: onPrepared==准备播放节目==
//        2023-04-24 18:43:04.965 11693-11693/com.ys.etv E/videoPlay: 视频播放时长=26123
//        2023-04-24 18:43:04.966 11693-11693/com.ys.etv E/videoPlay: toUpdatePlayNum=2 / 10
//        2023-04-24 18:43:05.165 11693-11693/com.ys.etv E/videoPlay: ==setVisibility==開始刷新視屏界面====GONE===

}
