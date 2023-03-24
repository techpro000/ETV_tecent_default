package com.etv.task.shiwei;

import android.content.Context;
import android.widget.AbsoluteLayout;

import com.etv.config.AppInfo;
import com.etv.listener.TaskPlayStateListener;
import com.etv.service.EtvService;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.ViewPosition;
import com.etv.task.util.TaskDealUtil;
import com.etv.task.view.PlaySingleView;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.system.SystemManagerUtil;
import com.etv.view.layout.Generator;
import com.etv.view.layout.image.ViewImageGenertrator;
import com.etv.view.layout.mixedswing.ViewImgVideoGenerate;
import com.etv.view.layout.video.media.ViewVideoGenertrator;
import com.ys.bannerlib.util.GlideCacheUtil;
import com.ys.etv.R;

import java.util.ArrayList;
import java.util.List;

public class PlaySingleTaskShiWeiParsener {

    Context context;
    PlaySingleView playSingleView;
    AbsoluteLayout view_abous;

    public PlaySingleTaskShiWeiParsener(Context context, PlaySingleView playSingleView) {
        this.context = context;
        this.playSingleView = playSingleView;
        view_abous = playSingleView.getAbsoluLayout();
    }

    public void getDataFromSdcard() {
        playSingleView.showWaitDialog(true);
        String path = AppInfo.TASK_SINGLE_PATH();
        GetMediaShiWeiRunnable runnable = new GetMediaShiWeiRunnable(path, new GetMediaFileListener() {
            @Override
            public void backMediaFileList(boolean isTrue, SingleTaskShiWeiEntity singleTaskShiWeiEntity) {
                playSingleView.showWaitDialog(false);
                if (!isTrue) {
                    playSingleView.notResourceTip(context.getString(R.string.current_no_date));
                    return;
                }
                dealFileListInfo(singleTaskShiWeiEntity);
            }
        });
        EtvService.getInstance().executor(runnable);
    }

    List<MediAddEntity> list_image = new ArrayList<MediAddEntity>();
    List<MediAddEntity> list_video = new ArrayList<MediAddEntity>();
    List<MediAddEntity> listsEntity = new ArrayList<MediAddEntity>();

    private void dealFileListInfo(SingleTaskShiWeiEntity singleTaskShiWeiEntity) {
        if (singleTaskShiWeiEntity == null) {
            playSingleView.notResourceTip(context.getString(R.string.current_no_date));
            return;
        }
        listsEntity = singleTaskShiWeiEntity.getListsEntity();
        list_image = singleTaskShiWeiEntity.getList_image();
        list_video = singleTaskShiWeiEntity.getList_video();
        if (listsEntity == null || listsEntity.size() < 1) {
            playSingleView.notResourceTip(context.getString(R.string.current_no_date));
            return;
        }
        playSingleView.showWaitDialog(false);
        updateLayoutView();
    }

    public void updateLayoutView() {
        genratorViewList.clear();
        clearMemory();
        int layouTag = SharedPerManager.getSingleLayoutTag();
        boolean isScreenForWord = SystemManagerUtil.isScreenHorOrVer(context, AppInfo.PROGRAM_POSITION_MAIN);
        MyLog.playTask("========判断当前屏幕得方向===" + isScreenForWord);
        if (isScreenForWord && layouTag > (ViewPosition.VIEW_LAYOUT_VER_VIEW - 1)) { //横屏
            layouTag = ViewPosition.VIEW_LAYOUT_HRO_VIEW;
            SharedPerManager.setSingleLayoutTag(ViewPosition.VIEW_LAYOUT_HRO_VIEW);
        } else if (!isScreenForWord && layouTag < ViewPosition.VIEW_LAYOUT_VER_VIEW) { //竖屏
            layouTag = ViewPosition.VIEW_LAYOUT_VER_VIEW;
            SharedPerManager.setSingleLayoutTag(ViewPosition.VIEW_LAYOUT_VER_VIEW);
        }
        List<ViewPosition> viewList = TaskDealUtil.getViewPositionById(layouTag, AppInfo.PROGRAM_POSITION_MAIN);
        if (viewList == null || viewList.size() < 1) {
            playSingleView.notResourceTip("节目数据异常,请联系售后");
            return;
        }
        for (int i = 0; i < viewList.size(); i++) {
            ViewPosition viewPosition = viewList.get(i);
            addShowView(viewPosition.getViewType(),
                    viewPosition.getLeftPosition(),
                    viewPosition.getTopPosition(),
                    viewPosition.getWidth(),
                    viewPosition.getHeight());
        }
    }

    Generator generatorView = null;

    private void addShowView(String viewType, int leftPosition, int topPosition, int width, int height) {
        MyLog.playTask("====播放布局坐标===" + viewType + " /" + leftPosition + " / " + topPosition + "/" + width + " / " + height);
        switch (viewType) {
            case AppInfo.VIEW_IMAGE_VIDEO:
                if (listsEntity.size() < 1 || listsEntity == null) {
                    return;
                }
                generatorView = new ViewImgVideoGenerate(context, null, leftPosition, topPosition, width, height, listsEntity, AppInfo.PROGRAM_POSITION_MAIN);
                addViewToList(generatorView);
                view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                break;
            case AppInfo.VIEW_IMAGE:
                if (list_image.size() < 1 || list_image == null) {
                    MyLog.playTask("====准备展示图片==NULL==");
                    return;
                }
                MyLog.playTask("====准备展示图片==" + list_image.size());
                //图片需要添加点击事件，所以addViewToList放在前面，切记，updateView用来刷新界面得，需要放在后边
                generatorView = new ViewImageGenertrator(context, null, leftPosition, topPosition, width, height, list_image,false);
                addViewToList(generatorView);
                view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                generatorView.updateView(null, true);
                break;
            case AppInfo.VIEW_VIDEO:
                if (list_video.size() < 1 || list_video == null) {
                    MyLog.playTask("==========video=========1111");
                    return;
                }
                MyLog.playTask("====视频的坐标的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height);
                generatorView = new ViewVideoGenertrator(context, null, leftPosition, topPosition, width, height, list_video, AppInfo.PROGRAM_POSITION_MAIN,false);
//                generatorView = new ViewIjkVideoGenertrator(context, null, leftPosition, topPosition, width, height, list_video, AppInfo.PROGRAM_POSITION_MAIN);
                addViewToList(generatorView);
                view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                generatorView.updateView(null, true);
                break;
        }
    }

    List<Generator> genratorViewList = new ArrayList<Generator>();  //用来封装播放view的

    public void addViewToList(final Generator generatorView) {
        if (generatorView == null) {
            return;
        }
        genratorViewList.add(generatorView);
        generatorView.setPlayStateChangeListener(new TaskPlayStateListener() {
            @Override
            public void playComplete(int playTag) {

            }

            @Override
            public void playCompletePosition(String etLevel, String taskId, int currentPlayPosition, int playTag) {

            }

            @Override
            public void clickTaskView(CpListEntity cpListEntity, List<String> list, int position) {
            }

            @Override
            public void longClickView(CpListEntity cpListEntity, Object object) {

            }

            @Override
            public void reStartPlayProgram(String errorDesc) {

            }
        });
    }

    /***
     * 清理View缓存
     */
    public void clearMemory() {
        try {
            if (view_abous != null) {
                view_abous.removeAllViews();
            }
            GlideCacheUtil.getInstance().clearImageAllCache(context);
            if (genratorViewList == null || genratorViewList.size() < 1) {
                return;
            }
            for (int i = 0; i < genratorViewList.size(); i++) {
                Generator genView = genratorViewList.get(i);
                genView.clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resumePlayView() {
        if (view_abous == null) {
            return;
        }
        if (genratorViewList == null || genratorViewList.size() < 1) {
            return;
        }
        for (int i = 0; i < genratorViewList.size(); i++) {
            Generator genView = genratorViewList.get(i);
            genView.resumePlayView();
        }
    }

    public void pauseDisplayView() {
        if (view_abous == null) {
            return;
        }
        if (genratorViewList == null || genratorViewList.size() < 1) {
            return;
        }
        for (int i = 0; i < genratorViewList.size(); i++) {
            Generator genView = genratorViewList.get(i);
            genView.pauseDisplayView();
        }
    }

    public void moveViewForward(boolean b) {
        if (view_abous == null) {
            return;
        }
        if (genratorViewList == null || genratorViewList.size() < 1) {
            return;
        }
        for (int i = 0; i < genratorViewList.size(); i++) {
            Generator genView = genratorViewList.get(i);
            genView.moveViewForward(b);
        }
    }
}
