package com.etv.task.parsener;

import android.app.Activity;
import android.util.Log;
import android.view.Display;
import android.widget.AbsoluteLayout;

import com.etv.util.SharedPerUtil;
import com.etv.view.layout.video.media.ViewVideoGenertrator;
import com.ys.bannerlib.util.GlideCacheUtil;
import com.ys.model.entity.FileEntity;
import com.ys.model.util.FileMatch;
import com.EtvApplication;
import com.diff.presentation.DifferentSingleDisplay;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.entity.ScreenEntity;
import com.etv.http.util.GetMediaListFromPathNewRunnable;
import com.etv.listener.TaskPlayStateListener;
import com.etv.service.EtvService;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SingleTaskEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.entity.ViewPosition;
import com.etv.task.util.AbcSortUtil;
import com.etv.task.util.TaskDealUtil;
import com.etv.task.view.PlaySingleView;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.system.SystemManagerUtil;
import com.etv.view.layout.Generator;
import com.etv.view.layout.date.ViewTimeOnlyGenerate;
import com.etv.view.layout.image.ViewImageGenertrator;
import com.etv.view.layout.mixedswing.ViewImgVideoGenerate;
import com.etv.view.layout.mixedswing.ViewImgVideoNetGenerate;
import com.etv.view.layout.music.ViewAudioGenertrator;
import com.ys.etv.R;

import java.util.ArrayList;
import java.util.List;

public class PlaySingleTaskParsener {

    private Activity context;
    private PlaySingleView playSingleView;
    private AbsoluteLayout view_abous;

    public PlaySingleTaskParsener(Activity context, PlaySingleView playSingleView) {
        this.context = context;
        this.playSingleView = playSingleView;
        view_abous = playSingleView.getAbsoluLayout();
    }

    List<MediAddEntity> list_image_cache = new ArrayList<MediAddEntity>();
    List<MediAddEntity> list_video_cache = new ArrayList<MediAddEntity>();
    List<MediAddEntity> list_music_cache = new ArrayList<MediAddEntity>();
    List<MediAddEntity> list_doc_cache = new ArrayList<MediAddEntity>();

    //封装素材得列表
    List<MediAddEntity> listsEntity = new ArrayList<MediAddEntity>();
    SingleTaskEntity currentSingleTaskEntity;

    public void getDataFromSdcard() {
        listsEntity.clear();
        list_image_cache.clear();
        list_video_cache.clear();
        list_music_cache.clear();
        list_doc_cache.clear();
        playSingleView.showWaitDialog(true);
        String path = AppInfo.TASK_SINGLE_PATH();
        GetMediaListFromPathNewRunnable runnable = new GetMediaListFromPathNewRunnable(path, new GetMediaListFromPathNewRunnable.GetSingleTaskEntityListener() {
            @Override
            public void backTaskEntity(boolean isTrue, SingleTaskEntity singleTaskEntity, String errorDesc) {
                playSingleView.showWaitDialog(false);
                currentSingleTaskEntity = singleTaskEntity;
                MyLog.cdl("=====检索文件状态===" + isTrue);
                if (!isTrue) {
                    playSingleView.notResourceTip(context.getString(R.string.current_no_date));
                    return;
                }
                if (singleTaskEntity == null) {
                    playSingleView.notResourceTip(context.getString(R.string.current_no_date));
                    return;
                }
                listsEntity = singleTaskEntity.getListsEntity();
                if (listsEntity == null || listsEntity.size() < 1) {
                    playSingleView.notResourceTip(context.getString(R.string.current_no_date));
                    return;
                }
                list_music_cache = singleTaskEntity.getList_music();
                list_doc_cache = singleTaskEntity.getList_doc();
                list_image_cache = singleTaskEntity.getList_image();
                list_video_cache = singleTaskEntity.getList_video();
                updateLayoutView();
            }
        });
        EtvService.getInstance().executor(runnable);
    }

    public void updateLayoutView() {
        genratorViewList.clear();
        clearMemory();
        int layouTag = SharedPerManager.getSingleLayoutTag();
        boolean isScreenForWord = SystemManagerUtil.isScreenHorOrVer(context, AppInfo.PROGRAM_POSITION_MAIN);
        if (isScreenForWord && layouTag > ViewPosition.VIEW_LAYOUT_14) { //横屏
            layouTag = ViewPosition.VIEW_LAYOUT_HRO_VIEW;
            SharedPerManager.setSingleLayoutTag(ViewPosition.VIEW_LAYOUT_HRO_VIEW);
        } else if (!isScreenForWord && layouTag < ViewPosition.VIEW_LAYOUT_VER_VIEW) {
            //竖屏
            layouTag = ViewPosition.VIEW_LAYOUT_VER_VIEW;
            SharedPerManager.setSingleLayoutTag(ViewPosition.VIEW_LAYOUT_VER_VIEW);
        }
        MyLog.cdl("====当前屏幕的布局=====" + layouTag);
        List<ViewPosition> viewList = TaskDealUtil.getViewPositionById(layouTag, AppInfo.PROGRAM_POSITION_MAIN);
        if (viewList == null || viewList.size() < 1) {
            playSingleView.notResourceTip(context.getString(R.string.program_error));
            return;
        }
        for (int i = 0; i < viewList.size(); i++) {
            ViewPosition viewPosition = viewList.get(i);
            addShowView(viewPosition.getViewType(),
                    viewPosition.getLeftPosition(),
                    viewPosition.getTopPosition(),
                    viewPosition.getWidth(),
                    viewPosition.getHeight(),
                    viewPosition.getShowPosition());
        }
        checkMusicFileView();
        //获取双屏节目任务属性
        getDevScreenNum();
        int isShowTime = SharedPerManager.getShowTimeEnable();
        Log.e("cdl", "====isShowTime===" + isShowTime);
        if (isShowTime == 0) {
            return;
        }
        addShowView(AppInfo.VIEW_TIME,
                0,
                0,
                0,
                0,
                1);
    }

    Generator generatorView = null;

    private void addShowView(String viewType, int leftPosition, int topPosition, int width, int height, int showPosition) {
        switch (viewType) {
            case AppInfo.VIEW_TIME:
                //#FF0000
                TextInfo textInfo = new TextInfo("#FFFFFF", "40", "");
                int screenWidth = SharedPerUtil.getScreenWidth();
                int viewSize = 430;
                generatorView = new ViewTimeOnlyGenerate(context, screenWidth - viewSize,
                        0, viewSize, 100, true);
                view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                generatorView.updateView(textInfo, true);
                addViewToList(generatorView);
                break;
            case AppInfo.VIEW_IMAGE_VIDEO:
                if (listsEntity == null || listsEntity.size() < 1) {
                    return;
                }
                List<MediAddEntity> list_entity_cache = getListAllCache(showPosition);
                if (list_entity_cache == null || list_entity_cache.size() < 1) {
                    return;
                }
                //视威使用触摸版本的，其他的使用混播模式
                if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_SHI_WEI) {
                    generatorView = new ViewImgVideoGenerate(context, null, leftPosition, topPosition, width, height, list_entity_cache, AppInfo.PROGRAM_POSITION_MAIN);
                } else {
                    generatorView = new ViewImgVideoNetGenerate(context, null, null, leftPosition, topPosition, width, height, list_entity_cache, true, 0, AppInfo.PROGRAM_POSITION_MAIN, false);
                }
                addViewToList(generatorView);
                view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                break;
            case AppInfo.VIEW_DOC:
                List<MediAddEntity> list_doc = getShowDocData(showPosition);
                if (list_doc == null || list_doc.size() < 1) {
                    MyLog.playTask("====准备展示图片==NULL==");
                    return;
                }
                MediAddEntity mediAddEntity = list_doc.get(0);
                String fileUrl = mediAddEntity.getUrl();
                int fileType = FileMatch.fileMatch(fileUrl);
                generatorView = TaskDealUtil.getPdfShowView(context, fileType, null, leftPosition, topPosition, width, height, mediAddEntity, list_doc);
                if (generatorView != null) {
                    addViewToList(generatorView);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(mediAddEntity, true);
                }
                break;
            case AppInfo.VIEW_IMAGE:
                List<MediAddEntity> list_image = getShowImageData(showPosition);
                if (list_image == null || list_image.size() < 1) {
                    MyLog.playTask("====准备展示图片==NULL==");
                    return;
                }
                MyLog.playTask("====准备展示图片==" + list_image.size());
//                //图片需要添加点击事件，所以addViewToList放在前面，切记，updateView用来刷新界面得，需要放在后边
                generatorView = new ViewImageGenertrator(context, null, leftPosition, topPosition, width, height, list_image, false);
                addViewToList(generatorView);
                view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                generatorView.updateView(null, true);
                break;
            case AppInfo.VIEW_VIDEO:
                List<MediAddEntity> list_video = getShowVideoData(showPosition);
                if (list_video == null || list_video.size() < 1) {
                    MyLog.playTask("==========video=========1111");
                    return;
                }
                generatorView = TaskDealUtil.getVideoPlayView(context, null, leftPosition, topPosition, width, height, list_video, AppInfo.PROGRAM_POSITION_MAIN, false);
                addViewToList(generatorView);
                view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                generatorView.updateView(null, true);
                break;
        }
    }

    /**
     * 检测有没有音频
     */
    private void checkMusicFileView() {
        if (list_music_cache == null || list_music_cache.size() < 1) {
            MyLog.playTask("===当前没有音频====");
            return;
        }
        MyLog.playTask("===当前没有音频====" + list_music_cache.size());
        generatorView = new ViewAudioGenertrator(context, 0, 0, 1, 1, list_music_cache);
        addViewToList(generatorView);
        view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
        generatorView.updateView(null, true);
    }

    /***
     * 根据位置获取素材
     * -1 获取全部图片和视频
     * > 根据位置获取
     * @param showPosition
     * @return
     */
    private List<MediAddEntity> getListAllCache(int showPosition) {
        if (listsEntity == null || listsEntity.size() < 1) {
            return null;
        }
        String urlCompair = "/" + showPosition + "/";
        List<MediAddEntity> listVideoShow = new ArrayList<>();
        for (int i = 0; i < listsEntity.size(); i++) {
            MediAddEntity mediAddEntity = listsEntity.get(i);
            String filePath = mediAddEntity.getUrl();
            int fileType = mediAddEntity.getFileType();
            if (fileType == FileEntity.STYLE_FILE_IMAGE || fileType == FileEntity.STYLE_FILE_VIDEO) {
                if (filePath.contains(urlCompair)) {
                    listVideoShow.add(mediAddEntity);
                }
            }
        }
        listVideoShow = AbcSortUtil.sortFile(listVideoShow);
        return listVideoShow;
    }


    /**
     * @param showPosition
     * @return
     */
    private List<MediAddEntity> getShowDocData(int showPosition) {
        if (list_doc_cache == null || list_doc_cache.size() < 1) {
            return null;
        }
        String urlCompair = "/" + showPosition + "/";
        List<MediAddEntity> listDocShow = new ArrayList<>();
        for (int i = 0; i < list_doc_cache.size(); i++) {
            MediAddEntity mediAddEntity = list_doc_cache.get(i);
            String filePath = mediAddEntity.getUrl();
            if (filePath.contains(urlCompair)) {
                listDocShow.add(mediAddEntity);
            }
        }
        listDocShow = AbcSortUtil.sortFile(listDocShow);
        return listDocShow;
    }

    private List<MediAddEntity> getShowVideoData(int showPosition) {
        if (list_video_cache == null || list_video_cache.size() < 1) {
            return null;
        }
        String urlCompair = "/" + showPosition + "/";
        List<MediAddEntity> listVideoShow = new ArrayList<>();
        for (int i = 0; i < list_video_cache.size(); i++) {
            MediAddEntity mediAddEntity = list_video_cache.get(i);
            String filePath = mediAddEntity.getUrl();
            if (filePath.contains(urlCompair)) {
                listVideoShow.add(mediAddEntity);
            }
        }
        listVideoShow = AbcSortUtil.sortFile(listVideoShow);
        return listVideoShow;
    }


    /**
     * 获取需要展示得图片
     *
     * @param showPosition
     * @return
     */
    private List<MediAddEntity> getShowImageData(int showPosition) {
        if (list_image_cache == null || list_image_cache.size() < 1) {
            return null;
        }
        String urlCompair = "/" + showPosition + "/";
        List<MediAddEntity> listImageShow = new ArrayList<>();
        for (int i = 0; i < list_image_cache.size(); i++) {
            MediAddEntity mediAddEntity = list_image_cache.get(i);
            String filePath = mediAddEntity.getUrl();
            if (filePath.contains(urlCompair)) {
                listImageShow.add(mediAddEntity);
            }
        }
        listImageShow = AbcSortUtil.sortFile(listImageShow);
        return listImageShow;
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
                if (playSingleView != null) {
                    playSingleView.toClickLongViewListener();
                }
            }

            @Override
            public void reStartPlayProgram(String errorDesc) {
                playSingleView.retryLoadSource();
            }
        });
    }

    /***
     * 清理View缓存
     */
    public void clearMemory() {
        try {
            if (singleDisplay != null) {
                singleDisplay.clearMemory();
                singleDisplay.dismiss();
            }
            if (view_abous != null) {
                view_abous.removeAllViews();
            }
            GlideCacheUtil.getInstance().clearImageAllCache(context);
            if (genratorViewList == null || genratorViewList.size() < 1) {
                return;
            }

            //System.out: com.etv.view.layout.video.media.ViewVideoGenertrator@1d74f68
            //System.out: com.etv.view.layout.video.media.ViewVideoGenertrator@1d74f68

            //ViewVideoGenertrator
            for (Generator genView : genratorViewList) {
                System.out.println(genView);
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

    /***
     * 双屏界面显示
     * 1：单品节目两个屏幕显示一样
     * 2：双屏幕节目 显示双节目
     */
    public void getDevScreenNum() {
        try {
            List<ScreenEntity> screenEntityList = EtvApplication.getInstance().getListScreen();
            if (screenEntityList == null || screenEntityList.size() < 2) {
                MyLog.diff("33333====haha==当前屏幕得个数= 0 ");
                return;
            }
            MyLog.diff("33333====haha==当前屏幕得个数=" + screenEntityList.size());
            ScreenEntity screenEntity = screenEntityList.get(1);
            Display display = screenEntity.getDisplay();
            int width = screenEntity.getScreenWidth();
            int height = screenEntity.getScreenHeight();

            MyLog.diff("33333====haha==当前屏幕得个数=" + currentSingleTaskEntity);
            if (currentSingleTaskEntity == null) {
                return;
            }
            List<MediAddEntity> listsEntity_double = currentSingleTaskEntity.getListsEntity_double();

            if (listsEntity_double == null || listsEntity_double.size() < 1) {
                MyLog.diff("33333====haha==当前屏幕得个数 listsEntity_double=" + listsEntity_double);
                return;
            }
            //不联动效果
            if (singleDisplay == null) {
                singleDisplay = new DifferentSingleDisplay(context, display, width, height);
                singleDisplay.show();
                singleDisplay.setPlayMediaList(currentSingleTaskEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    DifferentSingleDisplay singleDisplay;

}
