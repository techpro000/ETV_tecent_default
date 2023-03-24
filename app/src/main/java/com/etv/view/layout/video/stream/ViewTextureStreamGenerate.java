package com.etv.view.layout.video.stream;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etv.config.AppInfo;
import com.etv.listener.VideoPlayListener;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.view.layout.Generator;
import com.etv.view.layout.video.view.VideoStreamViewBitmap;
import com.ys.etv.R;

import java.util.List;

/**
 * 系统的流媒体播放控件
 * TextureView
 * 后边看稳定性，以IJK为主
 */
public class ViewTextureStreamGenerate extends Generator {

    View view;
    Context context;
    CpListEntity cpListEntity;

    @Override
    public void timeChangeToUpdateView() {

    }

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    @Override
    public void updateTextInfo(Object object) {

    }

    public ViewTextureStreamGenerate(Context context, CpListEntity cpListEntity, int startX, int StartY, int width, int height, String streamUrl) {
        super(context, startX, StartY, width, height);
        this.context = context;
        this.cpListEntity = cpListEntity;
        view = View.inflate(context, R.layout.view_stream, null);
        parsenerStreamList(streamUrl);
    }


    String[] streamList = null;

    private void parsenerStreamList(String streamUrl) {
        streamList = streamUrl.split(",");
        if (streamList == null || streamList.length < 1) {
            tv_dialog_wait.setText("Url Is Null ");
            return;
        }
        startToPlayStreamUrl();
    }

    VideoStreamViewBitmap videoStreamViewBitmap;
    LinearLayout iv_no_data;
    LinearLayout lin_progress;   //缓冲等待框
    TextView tv_dialog_wait;     //等待提示
    private int CURRENT_PLAY_POSITION = 0;
    TextView click_view;

    private void startToPlayStreamUrl() {
        iv_no_data = (LinearLayout) view.findViewById(R.id.iv_no_data);
        int workModel = SharedPerManager.getWorkModel();
        if (workModel == AppInfo.WORK_MODEL_SINGLE) {
            iv_no_data.setVisibility(View.VISIBLE);
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(context)) {
            iv_no_data.setVisibility(View.VISIBLE);
            return;
        }
        if (lin_progress == null) {
            lin_progress = (LinearLayout) view.findViewById(R.id.lin_progress);
        }
        if (tv_dialog_wait == null) {
            tv_dialog_wait = (TextView) view.findViewById(R.id.tv_dialog_wait);
        }
        String playUrl = streamList[CURRENT_PLAY_POSITION];
        MyLog.cdl("============流媒体切换====" + playUrl, true);
        if (playUrl == null || playUrl.length() < 5) {
            lin_progress.setVisibility(View.VISIBLE);
            tv_dialog_wait.setText("Url is Null !");
            return;
        }
        if (videoStreamViewBitmap == null) {
            videoStreamViewBitmap = (VideoStreamViewBitmap) view.findViewById(R.id.uvv_vido);
            videoStreamViewBitmap.setVideoPlayListener(new VideoPlayListener() {
                @Override
                public void initOver() {
                    startToPlayStream(playUrl);
                }

                @Override
                public void playCompletion(String tag) {

                }

                @Override
                public void playCompletionSplash(int position, int playTimeCurrent) {

                }

                @Override
                public void playError(String errorDesc) {
                    MyLog.video("流媒体视频播放异常: " + errorDesc, true);

                }

                @Override
                public void playErrorToStop(String errorDesc) {

                }

                @Override
                public void reStartPlayProgram(String errorDesc) {

                }
            }, listener, cpListEntity);
        } else {
            startToPlayStream(playUrl);
        }
        if (click_view == null) {
            click_view = (TextView) view.findViewById(R.id.click_view);
        }
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
    }

    private void startToPlayStream(String streamUrl) {
        if (videoStreamViewBitmap != null) {
            videoStreamViewBitmap.startToPlay(streamUrl);
        }
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
    public void clearMemory() {
        try {
            if (videoStreamViewBitmap != null) {
                videoStreamViewBitmap.clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeCacheView(String tag) {
    }

    @Override
    public void pauseDisplayView() {
        if (videoStreamViewBitmap != null) {
            videoStreamViewBitmap.pauseDisplayView();
        }
    }

    @Override
    public void moveViewForward(boolean b) {
        MyLog.cdl("============流媒体切换====" + b, true);
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
        startToPlayStreamUrl();
    }

    @Override
    public void resumePlayView() {
        if (videoStreamViewBitmap != null) {
            videoStreamViewBitmap.resumePlayView();
        }
    }

}
