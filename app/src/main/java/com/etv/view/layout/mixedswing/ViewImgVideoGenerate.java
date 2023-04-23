//package com.etv.view.layout.mixedswing;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.AbsoluteLayout;
//
//import com.ys.bannerlib.util.GlideCacheUtil;
//import com.ys.model.entity.FileEntity;
//import com.etv.listener.TaskPlayStateListener;
//import com.etv.task.entity.CpListEntity;
//import com.etv.task.entity.MediAddEntity;
//import com.etv.task.entity.SceneEntity;
//import com.etv.util.MyLog;
//import com.etv.view.layout.Generator;
//import com.etv.view.layout.image.ViewImageGenertrator;
//import com.etv.view.layout.video.media.ViewVideoGenertrator;
//import com.ys.etv.R;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 视威专用版本
// * <p>
// * 混播模式View
// * 单机模式
// * 先播放图片，完毕在播放视频
// */
//public class ViewImgVideoGenerate extends Generator {
//
//    View view;
//    List<MediAddEntity> listsEntity;
//    Context context;
//    int screenWidth = 0;
//    int screenHeight = 0;
//    CpListEntity cpListEntity;
//    String screenPosition;
//
//    @Override
//    public int getVideoPlayCurrentDuartion() {
//        return 0;
//    }
//
//    @Override
//    public void updateTextInfo(Object object) {
//
//    }
//
//    public ViewImgVideoGenerate(Context context, CpListEntity cpListEntity, int x, int y, int width, int height, List<MediAddEntity> listsEntity, String screenPosition) {
//        super(context, x, y, width, height);
//        this.cpListEntity = cpListEntity;
//        this.screenWidth = width;
//        this.screenHeight = height;
//        this.listsEntity = listsEntity;
//        this.context = context;
//        this.screenPosition = screenPosition;
//        view = LayoutInflater.from(context).inflate(R.layout.view_image_video, null);
//        ab_view = (AbsoluteLayout) view.findViewById(R.id.ab_view);
//        parsenerUpdateList(TaskPlayStateListener.TAG_PLAY_VIDEO);
//    }
//
//    AbsoluteLayout ab_view;
//    Generator generatorView;
//
//    static final int PLAY_TAG_IMAGE = 0;
//    static final int PLAY_TAG_VIDEO = 1;
//    int playTag;
//
//    public void parpreViewToShow(int tag) {
//        MyLog.cdl("======加载imageView==" + listsImage.size());
//        playTag = tag;
//        switch (tag) {
//            case PLAY_TAG_IMAGE:
//                MyLog.cdl("======加载imageView==" + listsImage.size());
//                generatorView = new ViewImageGenertrator(context, cpListEntity, 0, 0, screenWidth, screenHeight, listsImage, false);
//                addViewListener(generatorView);
//                ab_view.addView(generatorView.getView(), generatorView.getLayoutParams());
//                generatorView.updateView(null, true);
//                break;
//            case PLAY_TAG_VIDEO:
//                MyLog.cdl("======加载videoView==" + listsVideo.size() + " / " + (cpListEntity == null));
//                generatorView = new ViewVideoGenertrator(context, cpListEntity, 0, 0, screenWidth, screenHeight, listsVideo, screenPosition, false);
//                addViewListener(generatorView);
//                ab_view.addView(generatorView.getView(), generatorView.getLayoutParams());
//                generatorView.updateView(null, true);
//                break;
//        }
//    }
//
//    List<Generator> genratorViewList = new ArrayList<Generator>();  //用来封装播放view的
//
//    private void addViewListener(Generator generatorView) {
//        if (generatorView == null) {
//            return;
//        }
//        genratorViewList.add(generatorView);
//        generatorView.setPlayStateChangeListener(new TaskPlayStateListener() {
//
//            @Override
//            public void playComplete(int playTag) {
//                MyLog.cdl("===========播放完毕=====" + playTag);
//                clearMemory();
//                parsenerUpdateList(playTag);
//            }
//
//            @Override
//            public void playCompletePosition(String etLevel, String taskId, int currentPlayPosition, int playTag) {
//
//            }
//
//
//            @Override
//            public void clickTaskView(CpListEntity cpListEntity, List<String> list, int position) {
//                if (listener != null) {
//                    listener.clickTaskView(cpListEntity, list, position);
//                }
//            }
//
//            @Override
//            public void longClickView(CpListEntity cpListEntity, Object object) {
//                if (listener != null) {
//                    listener.longClickView(cpListEntity, null);
//                }
//            }
//
//            @Override
//            public void reStartPlayProgram(String errorDesc) {
//
//            }
//        });
//    }
//
//    @Override
//    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {
//
//    }
//
//    private List<MediAddEntity> listsImage = new ArrayList<>();
//    private List<MediAddEntity> listsVideo = new ArrayList<>();
//
//    private void parsenerUpdateList(int playTag) {
//        listsImage.clear();
//        listsVideo.clear();
//        MyLog.cdl("===listsEntity====" + listsEntity.size());
//        if (listsEntity == null || listsEntity.size() < 1) {
//            return;
//        }
//        for (int i = 0; i < listsEntity.size(); i++) {
//            MediAddEntity mediaEntity = listsEntity.get(i);
//            int fileType = mediaEntity.getFileType();
//            if (fileType == FileEntity.STYLE_FILE_IMAGE) {
//                listsImage.add(mediaEntity);
//            } else if (fileType == FileEntity.STYLE_FILE_VIDEO) {
//                listsVideo.add(mediaEntity);
//            }
//        }
//        MyLog.cdl("===listsImage====" + listsImage.size());
//        MyLog.cdl("===listsVideo====" + listsVideo.size());
//        if (playTag == TaskPlayStateListener.TAG_PLAY_PICTURE) {
//            if (listsVideo == null || listsVideo.size() < 1) {
//                MyLog.cdl("=====播放完毕了==没有视频直接播放图片====");
//                parpreViewToShow(PLAY_TAG_IMAGE);
//                return;
//            }
//            MyLog.cdl("=====播放完毕了==播放视频====");
//            parpreViewToShow(PLAY_TAG_VIDEO);
//        } else if (playTag == TaskPlayStateListener.TAG_PLAY_VIDEO) {
//            if (listsImage == null || listsImage.size() < 1) {
//                MyLog.cdl("=====播放完毕了==没有图片，继续播放视频====");
//                parpreViewToShow(PLAY_TAG_VIDEO);
//                return;
//            }
//            parpreViewToShow(PLAY_TAG_IMAGE);
//        }
//    }
//
//    @Override
//    public View getView() {
//        return view;
//    }
//
//    @Override
//    public void clearMemory() {
//        try {
//            if (ab_view != null) {
//                ab_view.removeAllViews();
//            }
//            GlideCacheUtil.getInstance().clearImageAllCache(context);
//            if (genratorViewList == null || genratorViewList.size() < 1) {
//                return;
//            }
//            for (int i = 0; i < genratorViewList.size(); i++) {
//                Generator genView = genratorViewList.get(i);
//                genView.clearMemory();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    @Override
//    public void removeCacheView(String tag) {
//
//    }
//
//    @Override
//    public void timeChangeToUpdateView() {
//
//    }
//
//    @Override
//    public void updateView(Object object, boolean isShow) {
//
//    }
//
//    @Override
//    public void playComplet() {
//
//    }
//
//    @Override
//    public void pauseDisplayView() {
//
//    }
//
//    @Override
//    public void resumePlayView() {
//
//    }
//
//    @Override
//    public void moveViewForward(boolean b) {
//
//    }
//}
