package com.etv.view.layout.music;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;

import com.etv.config.AppInfo;
import com.etv.db.DbStatiscs;
import com.etv.entity.StatisticsEntity;
import com.etv.listener.TaskPlayStateListener;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.util.MyLog;
import com.etv.util.system.CpuModel;
import com.etv.util.system.VoiceManager;
import com.etv.view.layout.Generator;
import com.ys.etv.R;

import java.util.List;

/***
 * 播放音频资源资源
 */
public class ViewAudioGenertrator extends Generator implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    List<MediAddEntity> audioLists = null;
    View view;
    private MediaPlayer mediaPlayer;

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    @Override
    public void updateTextInfo(Object object) {

    }

    /***
     *
     * @param context
     * Context
     * @param x
     * 起始点X坐标
     * @param y
     * 起始点Y坐标
     * @param width
     * 控件的宽度
     * @param height
     * 控件的高度
     * @param audioLists
     * 音频集合
     */
    Context context;

    public ViewAudioGenertrator(Context context, int x, int y, int width, int height, List<MediAddEntity> audioLists) {
        super(context, x, y, width, height);
        this.audioLists = audioLists;
        this.context = context;
        view = View.inflate(context, R.layout.view_audio_play, null);
        initView(view);
    }

    int currentPosition = 0;

    private void initView(View view) {
        mediaPlayer = new MediaPlayer();
        startPlayMusic();
    }

    public void startOrPause() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    /**
     * 获取当前媒体的音量
     *
     * @param playList
     * @return
     */
    private int getMediaVolNum(List<MediAddEntity> playList) {
        int volNum = 70;
        if (playList != null && playList.size() > 0) {
            String volNumString = playList.get(currentPosition).getVolNum().trim();
            if (volNumString == null || volNumString.length() < 1) {
                volNumString = "70";
            }
            volNum = Integer.parseInt(volNumString);
        }
        return volNum;
    }

    public void startPlayMusic() {
        String musicPath = audioLists.get(currentPosition).getUrl();
        if (musicPath == null || musicPath.length() < 5) {
            return;
        }
        int volNum = getMediaVolNum(audioLists);
        float volNumChangfe = (float) (volNum * 1.0 / 100);
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();//进行重置
            mediaPlayer.setDataSource(musicPath);
//            setMediaVoiceNum(volNum);
            mediaPlayer.setVolume(volNumChangfe, volNumChangfe);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMediaVoiceNum(int volNum) {
        float volNumChangfe = (float) (volNum * 1.0 / 100);
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            mediaPlayer.setVolume(1.0f, 1.0f);
            VoiceManager.getInstance(context).repairDevVoice(volNum + "");
            MyLog.video("========setMediaVoiceNum==CPU_MODEL_MTK_M11==" + volNum);
        } else {
            mediaPlayer.setVolume(volNumChangfe, volNumChangfe);
            MyLog.video("========setMediaVoiceNum====" + volNumChangfe);
        }
    }

    @Override
    public void timeChangeToUpdateView() {

    }

    public void nextPlay() {
        if (mediaPlayer == null) {
            return;
        }
        currentPosition++;
        if (currentPosition > (audioLists.size() - 1)) {
            currentPosition = 0;
        }
        startPlayMusic();
    }

    public void proPlay() {
        currentPosition--;
        if (currentPosition < 0) {
            currentPosition = (audioLists.size() - 1);
        }
        startPlayMusic();
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {
        try {
            MyLog.playTask("========clearMemory===音乐清理clearMemory清理View");
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeCacheView(String tag) {
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {

    }

    @Override
    public void playComplet() {
        //播放完毕，添加统计
        toUpdatePlayNum();
        if (listener != null) {
            listener.playComplete(TaskPlayStateListener.TAG_PLAY_AUDIO);
        }
    }

    public void toUpdatePlayNum() {
        if (listener == null) {
            return;
        }
        String midId = audioLists.get(currentPosition).getMidId();
        int playTime = 50 * 1000;
        if (mediaPlayer != null) {
            playTime = mediaPlayer.getDuration();
        }
        addUpdateToWeb(midId, playTime / 1000);
    }

    private void addUpdateToWeb(String midId, int timeDisplay) {
        String resourType = AppInfo.VIEW_AUDIO;
        StatisticsEntity statisticsEntity = new StatisticsEntity(midId, resourType, timeDisplay, 1, System.currentTimeMillis());
        boolean isSave = DbStatiscs.saveStatiseToLocal(statisticsEntity, "音频添加统计");
        MyLog.db("====音频统计====" + isSave);
    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

    @Override
    public void pauseDisplayView() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void resumePlayView() {
        if (mediaPlayer == null) {
            return;
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    public void moveViewForward(boolean b) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.e("music", "=====播放的位置==" + currentPosition + " / " + (audioLists.size() - 1));
        if (currentPosition == (audioLists.size() - 1)) {
            playComplet();
        }
        nextPlay();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        clearMemory();
        return false;
    }

}
