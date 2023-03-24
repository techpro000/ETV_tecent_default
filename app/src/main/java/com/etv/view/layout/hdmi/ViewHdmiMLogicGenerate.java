package com.etv.view.layout.hdmi;

import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContentRating;
import android.media.tv.TvTrackInfo;
import android.media.tv.TvView;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;

import com.etv.config.AppInfo;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.system.CpuModel;
import com.etv.view.layout.Generator;
import com.ys.etv.R;

import java.util.List;

/***
 * MLOGIC hdmi-View
 */
public class ViewHdmiMLogicGenerate extends Generator {
    Context context;
    View view;
    CpListEntity cpEntity;

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ViewHdmiMLogicGenerate(Context context, CpListEntity cpEntity, int startX, int StartY, int width, int height) {
        super(context, startX, StartY, width, height);
        this.context = context;
        this.cpEntity = cpEntity;
        view = LayoutInflater.from(context).inflate(R.layout.view_himi_mlogic, null);
        initView(view);
    }

    Button btn_type_change;
    TvView tv_view;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView(View view) {
        btn_type_change = (Button) view.findViewById(R.id.btn_type_change);
//        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MLOGIC)) {
//            btn_type_change.setVisibility(View.VISIBLE);
//        }
        btn_type_change.setVisibility(SharedPerManager.getShowHdmiButton() ? View.VISIBLE : View.GONE);
        String hdmiInfo = SharedPerManager.getMlogicHdmiPosition();
        if (hdmiInfo.contains(AppInfo.HDMIIN1())) {
            btn_type_change.setText("HDMIIN1");
        } else if (hdmiInfo.contains(AppInfo.HDMIIN2())) {
            btn_type_change.setText("HDMIIN2");
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            btn_type_change.setText("Software not supported");
            return;
        }
        tv_view = (TvView) view.findViewById(R.id.tv_view);
        btn_type_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.clickTaskView(cpEntity, null, 0);
                }
            }
        });

        tv_view.setCallback(new TvView.TvInputCallback() {
            @Override
            public void onConnectionFailed(String inputId) {
                super.onConnectionFailed(inputId);
                MyLog.cdl("====setCallback=======onConnectionFailed====");
            }

            @Override
            public void onDisconnected(String inputId) {
                super.onDisconnected(inputId);
                MyLog.cdl("====setCallback=====onDisconnected======");
            }

            @Override
            public void onChannelRetuned(String inputId, Uri channelUri) {
                super.onChannelRetuned(inputId, channelUri);
                MyLog.cdl("====setCallback=======onChannelRetuned====");
            }

            @Override
            public void onTracksChanged(String inputId, List<TvTrackInfo> tracks) {
                super.onTracksChanged(inputId, tracks);
                MyLog.cdl("====setCallback======onTracksChanged=====");
            }

            @Override
            public void onTrackSelected(String inputId, int type, String trackId) {
                super.onTrackSelected(inputId, type, trackId);
                MyLog.cdl("====setCallback======onTrackSelected=====");
            }

            @Override
            public void onVideoSizeChanged(String inputId, int width, int height) {
                super.onVideoSizeChanged(inputId, width, height);
                MyLog.cdl("====setCallback====onVideoSizeChanged=======");
            }

            @Override
            public void onVideoAvailable(String inputId) {
                super.onVideoAvailable(inputId);
                MyLog.cdl("====setCallback=======onVideoAvailable====");
            }

            @Override
            public void onVideoUnavailable(String inputId, int reason) {
                super.onVideoUnavailable(inputId, reason);
                MyLog.cdl("====setCallback======onVideoUnavailable=====");
            }

            @Override
            public void onContentAllowed(String inputId) {
                super.onContentAllowed(inputId);
                MyLog.cdl("====setCallback======onContentAllowed=====");
            }

            @Override
            public void onContentBlocked(String inputId, TvContentRating rating) {
                super.onContentBlocked(inputId, rating);
                MyLog.cdl("====setCallback=====onContentBlocked======");
            }

            @Override
            public void onTimeShiftStatusChanged(String inputId, int status) {
                super.onTimeShiftStatusChanged(inputId, status);
                MyLog.cdl("====setCallback=====onTimeShiftStatusChanged======");
            }
        });

        tv_view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.longClickView(cpEntity, null);
                }
                return true;
            }
        });
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        String hdmiInfo = SharedPerManager.getMlogicHdmiPosition();
        MyLog.cdl("====updateView=刷新界面==" + hdmiInfo);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            tv_view.reset();
            tv_view.tune(hdmiInfo, Uri.parse(""));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                changeShowType();
            }
        }, 500);
    }

    @Override
    public void timeChangeToUpdateView() {

    }

//
//        :/ # setprop media.omx.display_mode 0
//        setprop media.omx.display_mode 0
//        :/ # setprop sys.meida.omx.vr 1
//        setprop sys.meida.omx.vr 1
//        :/ # getprop media.omx.display_mode
//        getprop media.omx.display_mode  0
//        :/ # getprop sys.meida.omx.vr
//        getprop sys.meida.omx.vr  1
//        :/ #

    /***
     * 修改
     * 显示模式
     *0 自适应
     *1 全景H
     */
    public void changeShowType() {
        int mode = SharedPerManager.getHdmiShowStyle();
        Log.e("cdl", "=======changeShowType===" + mode);
        try {
            Intent intent = new Intent("ys.intent.action.HDMININ_SCREENRATIO");
            if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MLOGIC)) {
                intent.setPackage("com.droidlogic.tv.settings");
            } else if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
                intent.setPackage("com.android.tv.settings");
            }
            intent.putExtra("display_mode", mode);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            if (tv_view != null) {
//                tv_view.removeAllViews();
//                tv_view.reset();
//            }
//        }
    }

    @Override
    public void removeCacheView(String tag) {

    }

    @Override
    public void updateTextInfo(Object object) {

    }

    @Override
    public void playComplet() {

    }

    @Override
    public void pauseDisplayView() {

    }

    @Override
    public void resumePlayView() {

    }

    @Override
    public void moveViewForward(boolean b) {

    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

}
