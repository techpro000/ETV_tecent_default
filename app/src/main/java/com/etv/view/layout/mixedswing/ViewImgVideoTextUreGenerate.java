package com.etv.view.layout.mixedswing;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
 * 混播控件
 * textureView
 * 播放视频资源
 */
public class ViewImgVideoTextUreGenerate extends Generator {

    List<MediAddEntity> videoLists = null;
    View view;
    CpListEntity cpListEntity;
    int width, x;
    int height, y;

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    @Override
    public void timeChangeToUpdateView() {

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
    public ViewImgVideoTextUreGenerate(Context context, CpListEntity cpListEntity, int x, int y, int width, int height, List<MediAddEntity> videoLists) {
        super(context, x, y, width, height);
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        MyLog.video("==textureView===视频区域得坐标====" + x + " / " + y + " / " + width + " /" + height);
        this.videoLists = videoLists;
        this.cpListEntity = cpListEntity;
        view = View.inflate(context, R.layout.view_mix_media_textureview, null);
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        initView(view);
    }

    /***
     * 播放完毕
     */
    @Override
    public void playComplet() {
        if (listener != null) {
            listener.playComplete(TaskPlayStateListener.TAG_PLAY_VIDEO_IMAGE);
        }
    }

    RelativeLayout rela_no_data;
    TextView tv_desc;
    VideoTextUreImageVideoView video_view_surface;

    private void initView(View view) {
        tv_desc = (TextView) view.findViewById(R.id.tv_desc);
        rela_no_data = (RelativeLayout) view.findViewById(R.id.rela_no_data);
        video_view_surface = (VideoTextUreImageVideoView) view.findViewById(R.id.video_view_surface);
        video_view_surface.setVideoClickListen(listener, cpListEntity);
        video_view_surface.setVideoPlayListener(new VideoPlayListener() {
            @Override
            public void initOver() {
                MyLog.video("====初始化完成===");
                video_view_surface.setPlayList(videoLists);
            }

            @Override
            public void playCompletion(String tag) {
                MyLog.video("====视频列表播放完毕了==这里回调00====" + tag);
                playComplet();
            }

            @Override
            public void playCompletionSplash(int position, int playTimeCurrent) {

            }

            @Override
            public void playError(String errorDesc) {
                MyLog.video("error==startToPlay==" + errorDesc, true);
            }

            @Override
            public void playErrorToStop(String errorDesc) {
                MyLog.video("error==playErrorToStop55==" + errorDesc, true);
                listener.reStartPlayProgram(errorDesc);
            }

            @Override
            public void reStartPlayProgram(String errorDesc) {
                MyLog.video("error==reStartPlayProgram==" + errorDesc, true);
                if (listener != null) {
                    listener.reStartPlayProgram(errorDesc);
                }
            }
        });
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
        if (video_view_surface != null) {
            video_view_surface.clearMemory();
        }

    }

    @Override
    public void removeCacheView(String tag) {
        if (video_view_surface != null) {
            video_view_surface.removeCacheView();
        }
    }

    @Override
    public void pauseDisplayView() {
        MyLog.playTask("=====pauseDisplayView===video=====");
        if (video_view_surface != null) {
            video_view_surface.pausePlayVideo();
        }
    }

    @Override
    public void moveViewForward(boolean b) {

    }

    @Override
    public void resumePlayView() {
        MyLog.playTask("=====resumePlayView===video=====");
        if (video_view_surface != null) {
            video_view_surface.resumePlayVideo();
        }
    }

}