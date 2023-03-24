package com.etv.view.layout.video.stream;

import android.content.Context;
import android.text.TextUtils;
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
import com.etv.view.layout.video.surface.VideoSurfaceStreamView;
import com.ys.etv.R;

import java.util.List;

/***
 * 播放流媒体
 * 采用SurfaceView 渲染
 */
public class ViewSurfaceStreamGenertrator extends Generator {

    String streamUrl;
    View view;
    CpListEntity cpListEntity;
    int width;
    int height;

    public ViewSurfaceStreamGenertrator(Context context, CpListEntity cpListEntity,
                                        int x, int y, int width, int height,
                                        String streamUrl) {
        super(context, x, y, width, height);
        this.width = width;
        this.height = height;
        MyLog.cdl("===播放流媒体==视频区域得坐标====" + x + " / " + y + " / " + width + " /" + height);
        this.streamUrl = streamUrl;
        this.cpListEntity = cpListEntity;
        parsenerStreamList(streamUrl);
        view = View.inflate(context, R.layout.view_stream_surface, null);
    }


    String[] streamList = null;

    private void parsenerStreamList(String streamUrl) {
        if (TextUtils.isEmpty(streamUrl)) {
            return;
        }
        streamList = streamUrl.split(",");
        if (streamList == null || streamList.length < 1) {
            return;
        }
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        startToPlayStreamUrl();
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

    RelativeLayout rela_no_data;
    TextView tv_desc;
    VideoSurfaceStreamView video_view_surface;
    private int CURRENT_PLAY_POSITION = 0;

    private void startToPlayStreamUrl() {
        if (tv_desc == null) {
            tv_desc = (TextView) view.findViewById(R.id.tv_desc);
        }
        if (rela_no_data == null) {
            rela_no_data = (RelativeLayout) view.findViewById(R.id.rela_no_data);
        }
        if (video_view_surface == null) {
            video_view_surface = (VideoSurfaceStreamView) view.findViewById(R.id.video_view_surface);
            video_view_surface.setViewSize(width, height);
            video_view_surface.setVideoClickListen(listener, cpListEntity);
            video_view_surface.setVideoPlayListener(new VideoPlayListener() {
                @Override
                public void initOver() {
                    video_view_surface.startToPlayVideo(streamList[CURRENT_PLAY_POSITION]);
                }

                @Override
                public void playCompletion(String tag) {
                    MyLog.video("==播放流媒体==视频列表播放完毕了==这里回调====" + tag);
                }

                @Override
                public void playCompletionSplash(int position, int playTime) {

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
            });
        } else {
            if (video_view_surface != null) {
                video_view_surface.startToPlayVideo(streamList[CURRENT_PLAY_POSITION]);
            }
        }
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
        MyLog.cdl("============流媒体切换====" + b);
        if (streamList == null || streamList.length < 2) {
            MyLog.cdl("============流媒体切换==streamList null==");
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
        startToPlayStreamUrl();
    }

    @Override
    public void resumePlayView() {
        MyLog.playTask("=====resumePlayView===video=====");
        if (video_view_surface != null) {
            video_view_surface.resumePlayVideo();
        }
    }


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

}
