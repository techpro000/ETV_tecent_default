package com.diff.presentation;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.widget.AbsoluteLayout;

import com.diff.entity.DiffShowEntity;
import com.diff.util.DiffSizeUtil;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.listener.TaskPlayStateListener;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SingleTaskEntity;
import com.etv.task.entity.ViewPosition;
import com.etv.task.util.AbcSortUtil;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.system.CpuModel;
import com.etv.util.system.SystemManagerUtil;
import com.etv.view.layout.Generator;
import com.etv.view.layout.image.ViewImageGenertrator;
import com.etv.view.layout.mixedswing.ViewImgVideoGenerate;
import com.etv.view.layout.mixedswing.ViewImgVideoNetGenerate;
import com.etv.view.layout.music.ViewAudioGenertrator;
import com.etv.view.layout.video.media.ViewVideoGenertrator;
import com.ys.bannerlib.util.GlideCacheUtil;
import com.ys.etv.R;
import com.ys.model.entity.FileEntity;
import com.ys.model.util.FileMatch;

import java.util.ArrayList;
import java.util.List;

/**
 * 单机版本的副屏任务
 */
public class DifferentSingleDisplay extends Presentation {

    Activity context;
    int screenWidth;          //副屏得宽度
    int screenHeight;          //副屏得高度
    float widthChSize = 1;    //屏幕压缩比例
    float heightChSize = 1;   //屏幕压缩比例

    public DifferentSingleDisplay(Activity outerContext, Display display, int width, int height) {
        super(outerContext, display);
        this.context = outerContext;
        this.screenHeight = height;
        this.screenWidth = width;
        DiffShowEntity diffShowEntity = DiffSizeUtil.getDiffScreenSizeShow(screenWidth, screenHeight);
        widthChSize = diffShowEntity.getWidthChSize();
        heightChSize = diffShowEntity.getHeightChSize();
        MyLog.diff("===副屏的比例尺寸==" + widthChSize + " / " + heightChSize);
    }

    List<MediAddEntity> list_image_cache = new ArrayList<MediAddEntity>();
    List<MediAddEntity> list_video_cache = new ArrayList<MediAddEntity>();
    List<MediAddEntity> listsEntity = new ArrayList<MediAddEntity>();
    List<MediAddEntity> list_doc_cache = new ArrayList<MediAddEntity>();
    List<MediAddEntity> list_music_cache = new ArrayList<MediAddEntity>();

    public void setPlayMediaList(SingleTaskEntity singleTaskEntity) {
        listsEntity = singleTaskEntity.getListsEntity_double();
        list_image_cache = singleTaskEntity.getList_image_double();
        list_video_cache = singleTaskEntity.getList_video_double();
        list_doc_cache = singleTaskEntity.getList_doc_double();
        list_music_cache = singleTaskEntity.getList_music_double();
        updateLayoutView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_layout);
        MyLog.diff("===双屏初始化==onCreate");
        initView();
    }

    AbsoluteLayout view_abous;

    private void initView() {
        view_abous = (AbsoluteLayout) findViewById(R.id.view_diff);
    }

    List<Generator> genratorViewList = new ArrayList<Generator>();  //用来封装播放view的

    public void updateLayoutView() {
        genratorViewList.clear();
        clearMemory();
        int layouTag = SharedPerManager.getSingleSecondLayoutTag();
        boolean isScreenForWord = SystemManagerUtil.isScreenHorOrVer(context, AppInfo.PROGRAM_POSITION_SECOND);
        if (isScreenForWord && layouTag > ViewPosition.VIEW_LAYOUT_14) { //横屏
            layouTag = ViewPosition.VIEW_LAYOUT_HRO_VIEW;
            SharedPerManager.setSingleLayoutTag(ViewPosition.VIEW_LAYOUT_HRO_VIEW);
        } else if (!isScreenForWord && layouTag < ViewPosition.VIEW_LAYOUT_VER_VIEW) { //竖屏
            layouTag = ViewPosition.VIEW_LAYOUT_VER_VIEW;
            SharedPerManager.setSingleLayoutTag(ViewPosition.VIEW_LAYOUT_VER_VIEW);
        }
        List<ViewPosition> viewList = TaskDealUtil.getViewPositionById(layouTag, AppInfo.PROGRAM_POSITION_SECOND);
        if (viewList == null || viewList.size() < 1) {
            return;
        }
        for (int i = 0; i < viewList.size(); i++) {
            ViewPosition viewPosition = viewList.get(i);
            addShowView(viewPosition.getViewType(),
                    viewPosition.getLeftPosition(),
                    viewPosition.getTopPosition(),
                    viewPosition.getWidth(),
                    viewPosition.getHeight(), viewPosition.getShowPosition());
        }
        checkMusicFileView();
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

    Generator generatorView = null;

    private void addShowView(String viewType, int leftPosition, int topPosition, int width, int height, int showPosition) {
        leftPosition = (int) (leftPosition * widthChSize);
        topPosition = (int) (topPosition * heightChSize);
        width = (int) (width * widthChSize);
        height = (int) (height * heightChSize);
        MyLog.diff("====diff布局的坐标点0000==>>" + viewType + " / " + leftPosition + " / " + topPosition + " / " + width + " / " + height);
        switch (viewType) {
            case AppInfo.VIEW_IMAGE_VIDEO:
                MyLog.diff("====副屏混播控件====");
                if (listsEntity == null || listsEntity.size() < 1) {
                    MyLog.diff("====副屏混播控件==listsEntity==null==");
                    return;
                }
                List<MediAddEntity> list_entity_cache = getListAllCache(showPosition);
                if (list_entity_cache == null || list_entity_cache.size() < 1) {
                    MyLog.diff("====副屏混播控件==list_entity_cache==null==");
                    return;
                }
                MyLog.diff("====副屏混播控件==list_entity_cache===" + list_entity_cache.size());
                generatorView = new ViewImgVideoNetGenerate(context, null, null, leftPosition, topPosition, width, height, list_entity_cache, true, 0, AppInfo.PROGRAM_POSITION_SECOND, false);
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
                if (generatorView == null) {
                    return;
                }
                view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                generatorView.updateView(mediAddEntity, true);
                break;
            case AppInfo.VIEW_IMAGE:
                List<MediAddEntity> list_image = getShowImageData(showPosition);
                if (list_image == null || list_image.size() < 1) {
                    MyLog.diff("====准备展示图片==NULL==");
                    return;
                }
                MyLog.diff("====显示图片得坐标==>>" + viewType + " / " + leftPosition + " / " + topPosition + " / " + width + " / " + height);
                MyLog.playTask("====准备展示图片==" + list_image.size());
                //图片需要添加点击事件，所以addViewToList放在前面，切记，updateView用来刷新界面得，需要放在后边
                generatorView = new ViewImageGenertrator(context, null, leftPosition, topPosition, width, height, list_image, false);
                addViewToList(generatorView);
                view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                generatorView.updateView(null, true);
                break;
            case AppInfo.VIEW_VIDEO:
                List<MediAddEntity> list_video = getShowVideoData(showPosition);
                if (list_video == null || list_video.size() < 1) {
                    MyLog.diff("==========video=========1111");
                    return;
                }
                MyLog.diff("====视频的坐标的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height);
                generatorView = new ViewVideoGenertrator(context, null, leftPosition, topPosition, width, height, list_video, AppInfo.PROGRAM_POSITION_SECOND, false);
                addViewToList(generatorView);
                view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                generatorView.updateView(null, true);
                break;
        }
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


}
