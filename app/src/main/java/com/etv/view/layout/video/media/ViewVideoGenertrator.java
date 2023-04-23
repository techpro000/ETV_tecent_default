package com.etv.view.layout.video.media;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.db.DbStatiscs;
import com.etv.entity.StatisticsEntity;
import com.etv.listener.TaskPlayStateListener;
import com.etv.listener.VideoPlayListener;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.util.MyLog;
import com.etv.view.layout.Generator;
import com.ys.etv.R;

import java.util.List;

/***
 * 播放视频资源
 */
public class ViewVideoGenertrator extends Generator {

    VideoViewBitmap videoView = null;
    List<MediAddEntity> videoLists = null;
    View view;
    CpListEntity cpListEntity;
    String screenPosition;
    int width;
    int height;
    boolean ifViewZero = false;

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
     * @param x
     * 起点坐标
     * @param y
     * 起点坐标
     * @param width
     * 控件宽度
     * @param height
     * 控件高度
     * @param videoLists
     * 素材集合
     */
    public ViewVideoGenertrator(Context context, CpListEntity cpListEntity, int x, int y, int width, int height, List<MediAddEntity> videoLists,
                                String screenPosition, boolean ifViewZero) {
        super(context, x, y, width, height);
        this.width = width;
        this.height = height;
        this.videoLists = videoLists;
        this.ifViewZero = ifViewZero;
        this.cpListEntity = cpListEntity;
        MyLog.playTask("=控件选择=" + (cpListEntity == null));
        this.screenPosition = screenPosition;
        view = View.inflate(context, R.layout.view_video_play_new, null);
    }

    @Override
    public void timeChangeToUpdateView() {

    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        MyLog.playTask("==使用的是全屏拉伸=========");
        initView(view);
    }

    /***
     * 播放完毕
     */
    @Override
    public void playComplet() {
        if (listener != null) {
            listener.playComplete(TaskPlayStateListener.TAG_PLAY_VIDEO);
        }
    }

    int playTimeUpdate = 15;

    public void toUpdatePlayNum(int position) {
        if (listener == null) {
            return;
        }
        addUpdateToWeb(videoLists.get(position).getMidId(), playTimeUpdate);
    }

    private void addUpdateToWeb(String midId, int timeDisplay) {
        String resourType = AppInfo.VIEW_VIDEO;
        StatisticsEntity statisticsEntity = new StatisticsEntity(midId, resourType, timeDisplay, 1, System.currentTimeMillis());
        DbStatiscs.saveStatiseToLocal(statisticsEntity, "Video视频添加统计");
    }

    RelativeLayout rela_no_data;
    TextView tv_desc;

    private void initView(View view) {
        tv_desc = (TextView) view.findViewById(R.id.tv_desc);
        rela_no_data = (RelativeLayout) view.findViewById(R.id.rela_no_data);
        videoView = (VideoViewBitmap) view.findViewById(R.id.video_view);
        videoView.setViewSize(width, height);
        videoView.setVideoClickListen(listener, ifViewZero);
        videoView.setVideoPlayListener(new VideoPlayListener() {

            @Override
            public void initOver() {
                videoView.setPlayList(videoLists);
            }

            @Override
            public void playCompletion(String tag) {
                MyLog.video("====视频列表播放完毕了==这里回调====" + tag);
                playComplet();
            }

            @Override
            public void playCompletionSplash(int position, int playTime) {  //每次播放完毕都会调用这里
                playTimeUpdate = playTime;
                toUpdatePlayNum(position);
            }

            @Override
            public void playError(String errorDesc) {
                MyLog.video("error==startToPlay==" + errorDesc, true);
            }

            @Override
            public void playErrorToStop(String errorDesc) {
                MyLog.video("error==playErrorToStop==" + errorDesc, true);
                rela_no_data.setVisibility(View.VISIBLE);
                tv_desc.setText(errorDesc + "");
            }

            @Override
            public void reStartPlayProgram(String errorDesc) {
                MyLog.video("error==reStartPlayProgram==" + errorDesc, true);
                if (listener != null) {
                    listener.reStartPlayProgram(errorDesc);
                }
            }
        }, cpListEntity);
    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {
        if (videoView != null) {
            videoView.clearMemory();
        }
    }

    @Override
    public void removeCacheView(String tag) {
        if (videoView != null) {
            videoView.removeCacheView();
        }
    }

    @Override
    public void pauseDisplayView() {
        MyLog.playTask("=====pauseDisplayView===video=====");
        if (videoView != null) {
            videoView.pausePlayVideo();
        }
    }

    @Override
    public void moveViewForward(boolean b) {
        if (videoView != null) {
            videoView.moveViewForward(b);
        }
    }

    @Override
    public void resumePlayView() {
        MyLog.playTask("=====resumePlayView===video=====");
        if (videoView != null) {
            videoView.resumePlayVideo();
        }
    }

}
