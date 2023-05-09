package com.etv.task.parsener;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

import com.etv.config.ApiInfo;
import com.etv.config.AppInfo;
import com.etv.listener.TaskPlayStateListener;
import com.etv.service.EtvService;
import com.etv.task.db.DBTaskUtil;
import com.etv.task.db.DbTaskManager;
import com.etv.task.entity.CacheMemory;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.MpListEntity;
import com.etv.task.entity.PositionEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TaskWorkEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.model.TaskGetDbListener;
import com.etv.task.model.TaskModelmpl;
import com.etv.task.model.TaskMudel;
import com.etv.task.model.TaskRequestListener;
import com.etv.task.util.TaskDealUtil;
import com.etv.task.view.PlayTaskView;
import com.etv.util.Biantai;
import com.etv.util.SharedPerUtil;
import com.etv.util.system.CpuModel;
import com.etv.view.layout.video.surface.ViewVideoSurfaceGenertrator;
import com.ys.bannerlib.util.GlideCacheUtil;
import com.ys.bannerlib.util.GlideImageUtil;
import com.ys.model.entity.FileEntity;
import com.ys.model.util.FileMatch;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.view.layout.Generator;
import com.etv.view.layout.ViewButtonGenerate;
import com.etv.view.layout.date.ViewDateGenerate;
import com.etv.view.layout.date.ViewTimeOnlyGenerate;
import com.etv.view.layout.date.ViewTimeReduceGenerate;
import com.etv.view.layout.date.ViewWeekGenerate;
import com.etv.view.layout.image.ViewImageGenertrator;
import com.etv.view.layout.mixedswing.ViewImgVideoNetGenerate;
import com.etv.view.layout.music.ViewAudioGenertrator;
import com.etv.view.layout.text.ViewMatQueentestGenerte;
import com.etv.view.layout.text.ViewTexUpGenerate;
import com.etv.view.layout.text.ViewTextSlientGenerate;
import com.etv.view.layout.video.media.ViewVideoGenertrator;
import com.ys.etv.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/***
 * 触发任务播放
 */
public class PlayTaskTriggerParsener {
    Activity context;
    PlayTaskView playTaskView;
    TaskMudel taskModel;
    int currentSencenPosition = 0;

    public PlayTaskTriggerParsener(Activity context, PlayTaskView playTaskView, int playPosition) {
        this.context = context;
        this.playTaskView = playTaskView;
        taskModel = new TaskModelmpl();
        currentSencenPosition = playPosition;
        getView();
    }

    ImageView iv_back_bgg;
    AbsoluteLayout view_abous;

    private void getView() {
        view_abous = playTaskView.getAbsoluteLayout();
        iv_back_bgg = playTaskView.getBggImageView();
    }

    public void setPlayPosition(int playPosition) {
        currentSencenPosition = playPosition;
    }


    public void getTaskToView(String tag) {
        clearMemory();
        taskModel.getPlayTaskTigerFormDb(new TaskGetDbListener() {
            @Override
            public void getTaskFromDb(List<TaskWorkEntity> list) {

            }

            @Override
            public void getTaskTigerFromDb(TaskWorkEntity taskWorkEntity) {
                MyLog.playTask("getTaskTigerFromDb: " + taskWorkEntity);
                if (taskWorkEntity == null) {
                    MyLog.playTask("===获取得任务===null");
                    playTaskView.showViewError("没有需要播放得任务");
                    return;
                }
                MyLog.playTask("===获取得任务===" + taskWorkEntity.toString());
                parsenerTaskInfo(taskWorkEntity);
            }
        }, "====播放界面，这里获取任务数据====");
    }

    /***
     * 解析需要播放得任务
     * @param taskWorkEntity
     */
    List<SceneEntity> sceneEntityListCache = new ArrayList<>(); //需要播放的场景集合
    List<MpListEntity> mpList = new ArrayList<MpListEntity>();     //控件的素材信息
    List<TextInfo> txtList = new ArrayList<TextInfo>();  //控件的文本属性
    int screenWidth = SharedPerUtil.getScreenWidth();
    int screenHeight = SharedPerUtil.getScreenHeight();
    //用来封装播放view的
    List<CacheMemory> genratorViewList = new ArrayList<CacheMemory>();

    private void parsenerTaskInfo(TaskWorkEntity taskWorkEntity) {
        List<MpListEntity> mpListEntities = DBTaskUtil.getMpListInfoAll();
        boolean isFileExict = TaskDealUtil.compairMpListFileExict(mpListEntities);
        MyLog.playTask("==========比对数据库文件是否存在==比对完成，是否全部存在" + isFileExict);
        if (!isFileExict) {
            playTaskView.showViewError("没有素材信息需要播放");
            return;
        }
        sceneEntityListCache.clear();
        List<SceneEntity> listCacheSenc = DbTaskManager.getSencenEntityFormDbByTask(taskWorkEntity);
        if (listCacheSenc != null && listCacheSenc.size() > 0) {
            sceneEntityListCache.addAll(listCacheSenc);
        }
        if (sceneEntityListCache == null || sceneEntityListCache.size() < 1) {
            MyLog.playTask("===== 获取任务场景失败 1");
            playTaskView.showViewError("获取任务场景失败");
            return;
        }
        getPmFromTask(currentSencenPosition, "parsenerTaskFromDb");
    }

    private void getPmFromTask(int position, String tag) {
        MyLog.task("getPmFromTask: " + tag);
        SceneEntity currentSceneEntity = sceneEntityListCache.get(position);
        if (currentSceneEntity == null) {
            playTaskView.showViewError("获取场景信息异常");
            return;
        }
        String sencenId = currentSceneEntity.getSenceId();   // 场景得ID
        List<CpListEntity> cpList = DbTaskManager.getComptionFromDbBySenId(sencenId);
        if (cpList == null || cpList.size() < 1) {
            playTaskView.showViewError("获取控件异常");
            return;
        }
        List<CpListEntity> cpCacheList = TaskDealUtil.mathCpListOrder(cpList);
        addBackImageInfo(currentSceneEntity); //添加背景图片
        for (int i = 0; i < cpCacheList.size(); i++) {
            CpListEntity cpListEntity = cpCacheList.get(i);
            parperToShowView(cpListEntity);
        }
    }

    public void parperToShowView(CpListEntity cpEntity) {
        MyLog.task("getPmFromTask: " + 99999999);
        if (cpEntity == null) {
            playTaskView.showViewError("控件解析失败");
            return;
        }
        try {
            if (mpList == null) {
                mpList = new ArrayList<>();
            }
            mpList.clear();
            if (txtList == null) {
                txtList = new ArrayList<>();
            }
            txtList.clear();
            PositionEntity positionEntity = TaskDealUtil.getCpListPosition(cpEntity);
            int leftPosition = positionEntity.getLeftPosition();
            int topPosition = positionEntity.getTopPosition();
            int width = positionEntity.getWidth();
            int height = positionEntity.getHeight();
            String coType = cpEntity.getCoType();             //控件类型
            int viewWidth = leftPosition + width;
            int viewHeight = topPosition + height;
            if (leftPosition < 8 && leftPosition > 0) {
                leftPosition = 0;
            }
            if (topPosition < 8 && topPosition > 0) {
                topPosition = 0;
            }
            //控件的宽度 - 屏幕的宽度
            int distanceWidth = Math.abs(viewWidth - screenWidth);
            if (distanceWidth < 8) {
                width = screenWidth - leftPosition;
            }
            int distanceHeight = Math.abs(viewHeight - screenHeight);
            if (distanceHeight < 8) {
                height = screenHeight - topPosition;
            }
            MyLog.playTask("====布局的坐标点1111==>>" + coType + "/cpEntityId=" + cpEntity.getCpidId() + " / " + " / " + leftPosition + " / " + topPosition + " / " + width + " / " + height);
            if (TaskDealUtil.isResourceType(coType)) {   //资源类型
                MyLog.playTask("====布局的坐标点1111==>>资源类型");
                mpList = cpEntity.getMpList();
            } else if (TaskDealUtil.isTxtType(coType)) {  //文本类型
                MyLog.playTask("====布局的坐标点1111==>>文本类型");
                txtList = cpEntity.getTxList();
            }
            final Generator generatorView;
            switch (coType) {
                case AppInfo.VIEW_IMAGE:  //图片格式
                    List<MediAddEntity> imageList = TaskDealUtil.getResourceListPath(mpList);
                    if (imageList == null || imageList.size() < 1) {
                        MyLog.playTask("====准备展示图片==NULL==");
                        return;
                    }
                    MyLog.playTask("====图标的布局的坐标点==>>" + leftPosition + " / " + topPosition + " / " + width + " / " + height);
                    MyLog.playTask("====准备展示图片==" + imageList.size());
                    //图片需要添加点击事件，所以addViewToList放在前面，切记，updateView用来刷新界面得，需要放在后边
                    generatorView = TaskDealUtil.getImageGenertorViewParsener(context, cpEntity, leftPosition, topPosition, width, height, imageList, false);
                    addViewToList(generatorView, coType, false);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(null, true);
                    break;
                case AppInfo.VIEW_VIDEO: //视频格式
                    List<MediAddEntity> videoList = TaskDealUtil.getResourceListPath(mpList);
                    if (videoList == null || videoList.size() < 1) {
                        return;
                    }
                    MyLog.playTask("====视频的坐标的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height + " / videoList=" + videoList.size());
                    generatorView = TaskDealUtil.getVideoPlayView(context, cpEntity, leftPosition, topPosition, width, height, videoList, AppInfo.PROGRAM_POSITION_MAIN, false);
                    addViewToList(generatorView, coType, false);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(null, true);
                    break;
                case AppInfo.VIEW_IMAGE_VIDEO: //混播模式
                    List<MediAddEntity> mixtureList = TaskDealUtil.getResourceListPath(mpList);
                    if (mixtureList == null || mixtureList.size() < 1) {
                        return;
                    }
                    SceneEntity currentScentity = getCurrentSencenEntity();
                    MyLog.playTask("混播===列表数量===" + mixtureList.size());
                    generatorView = new ViewImgVideoNetGenerate(context, cpEntity, currentScentity, leftPosition, topPosition, width, height, mixtureList, true, 0, AppInfo.PROGRAM_POSITION_MAIN, false);
                    addViewToList(generatorView, coType, false);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    break;
                case AppInfo.VIEW_AUDIO:                         //音频
                    List<MediAddEntity> audioList = TaskDealUtil.getResourceListPath(mpList);
                    if (audioList == null || audioList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewAudioGenertrator(context, 0, 0, 1, 1, audioList);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    addViewToList(generatorView, coType, false);
                    break;
                case AppInfo.VIEW_WEB_PAGE:                      //网页
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    TextInfo textInfo = txtList.get(0);
                    if (textInfo == null) {
                        return;
                    }
                    String txtContentWeb = textInfo.getTaContent();
                    MyLog.playTask("网络网址=000==" + txtContentWeb);
                    //加一个判断， H5 类型的节目
                    String pmType = textInfo.getPmType();
                    if (pmType.contains(AppInfo.PROGRAM_HTML_5)) {
                        txtContentWeb = "http://" + ApiInfo.getWebIpHost() + txtContentWeb;
                        MyLog.playTask("网络网址=111==" + txtContentWeb);
                    }
                    String moveWeb = textInfo.getTaMove();
                    generatorView = TaskDealUtil.getWebViewBySpeedString(context, moveWeb, leftPosition, topPosition, width, height, txtContentWeb);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    addViewToList(generatorView, coType, false);
                    //网页不用这个方法，这个是刷新功能
                    generatorView.updateView(textInfo, true);
                    break;
                case AppInfo.VIEW_DOC:    //文档
                    List<MediAddEntity> docList = TaskDealUtil.getResourceListPath(mpList);
                    if (docList == null || docList.size() < 1) {
                        return;
                    }
                    MediAddEntity mediAddEntity = docList.get(0);
                    String fileUrl = mediAddEntity.getUrl();
                    int fileType = FileMatch.fileMatch(fileUrl);
                    MyLog.playTask("====文档地址===" + fileUrl + " /fileType =  " + fileType);
                    MyLog.playTask("====文档地址===" + " /fileType =  " + fileType);
                    generatorView = TaskDealUtil.getPdfShowView(context, fileType, cpEntity, leftPosition, topPosition, width, height, mediAddEntity, docList);
                    if (generatorView != null) {
                        view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                        generatorView.updateView(mediAddEntity, true);
                        addViewToList(generatorView, coType, false);
                    }
                    break;
//                case AppInfo.VIEW_STREAM_VIDEO:  //流媒体
//                    MyLog.playTask("====加载流媒体控件==VIEW_STREAM_VIDEO==" + leftPosition + " / " + topPosition + " / " + width + " / " + height);
//                    if (txtList == null || txtList.size() < 1) {
//                        return;
//                    }
//                    TextInfo textInfoStream = txtList.get(0);
//                    String streamUrl = textInfoStream.getTaContent();
//                    MyLog.playTask("====加载流媒体控件==streamUrl==" + streamUrl);
//                    String moveStream = textInfoStream.getTaMove();
//                    generatorView = TaskDealUtil.getStreamGenViewBySpeed(context, moveStream, cpEntity, leftPosition, topPosition,
//                            width, height, streamUrl, coType);
//                    addViewToList(generatorView, coType, false);
//                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
//                    generatorView.updateView(null, true);
//                    break;
                case AppInfo.VIEW_DATE:   //日期
                    MyLog.playTask("====时间的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height);
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewDateGenerate(context, leftPosition, topPosition, width, height);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    addViewToList(generatorView, coType, false);
                    break;
                case AppInfo.VIEW_WEEK:   //星期
                    MyLog.playTask("====时间的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height);
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewWeekGenerate(context, leftPosition, topPosition, width, height);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    addViewToList(generatorView, coType, false);
                    break;
                case AppInfo.VIEW_TIME:   //时间
                    MyLog.playTask("====时间的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height);
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewTimeOnlyGenerate(context, leftPosition, topPosition, width, height, false);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    addViewToList(generatorView, coType, false);
                    break;
                case AppInfo.VIEW_SUBTITLE:   //字幕
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    TextInfo textInfo1 = null;
                    for (int i = 0; i < txtList.size(); i++) {
                        int parsenId = txtList.get(i).getParentCoId();
                        if (parsenId == DBTaskUtil.MP_DEFAULT) {
                            textInfo1 = txtList.get(i);
                        }
                    }
                    StringBuilder builder = new StringBuilder();
                    String txtContentword = textInfo1.getTaContent();
                    builder.append(txtContentword + "");
                    String taMoveSpeed = textInfo1.getTaMoveSpeed();
                    String taMove = textInfo1.getTaMove();
                    if (taMove == null || taMove.length() < 1) {
                        taMove = TextInfo.MOVE_LEFT + "";
                    }
                    MyLog.playTask("===字幕数值===" + textInfo1.toString());
                    MyLog.playTask("====字幕的坐标===" + leftPosition + "/" + topPosition + "/" + width + "/" + height + " /taMoveSpeed=" + taMoveSpeed);
                    if (taMoveSpeed.contains("0")) {
                        //静止状态
                        generatorView = new ViewTextSlientGenerate(context, cpEntity, leftPosition, topPosition, width, height, builder.toString());
                    } else { //运动状态
                        if (taMove.contains(TextInfo.MOVE_UP + "")) {      //从下到上
                            generatorView = new ViewTexUpGenerate(context, cpEntity, leftPosition, topPosition, width, height, builder.toString());
                        } else {  //左右位 移动
                            generatorView = new ViewMatQueentestGenerte(context, cpEntity, leftPosition, topPosition, width, height, builder.toString());
                        }
                    }
                    addViewToList(generatorView, coType, false);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(textInfo1, true);
                    break;
                case AppInfo.VIEW_BUTTON:
                    MyLog.playTask("button的坐标===" + leftPosition + "/" + topPosition + "/" + width + "/" + height);
                    if (txtList == null || txtList.size() < 1) {
                        MyLog.playTask("button的坐标===没有数据，终端操作");
                        return;
                    }
                    StringBuilder builderButton = new StringBuilder();
                    for (int i = 0; i < txtList.size(); i++) {
                        String txtContent = txtList.get(i).getTaContent();
                        builderButton.append(txtContent + "");
                    }
                    MyLog.playTask("button显示===" + builderButton.toString());
                    generatorView = new ViewButtonGenerate(context, cpEntity, leftPosition, topPosition, width, height, builderButton.toString());
                    addViewToList(generatorView, coType, false);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    if (txtList != null && txtList.size() > 0) {
                        generatorView.updateView(txtList.get(0), true);
                    } else {
                        generatorView.updateView(null, true);
                    }
                    break;
                case AppInfo.VIEW_COUNT_DOWN://倒计时
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewTimeReduceGenerate(context, cpEntity, leftPosition, topPosition, width, height);
                    addViewToList(generatorView, coType, false);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    break;
                case AppInfo.VIEW_HDMI:
                    MyLog.playTask("====Hdmi的坐标点==>>" + leftPosition + " / " + topPosition + " / " + width + " / " + height);
                    playTaskView.showHdmInViewToActivity(leftPosition, topPosition, width, height);
                    break;
            }
        } catch (Exception e) {
            MyLog.ExceptionPrint("播放界面布局异常: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 获取当前正在播放的场景
     *
     * @return
     */
    public SceneEntity getCurrentSencenEntity() {
        if (sceneEntityListCache == null || sceneEntityListCache.size() < 1) {
            return null;
        }
        MyLog.task("getPmFromTask: " + 10101010);
        return sceneEntityListCache.get(currentSencenPosition);
    }

    /***
     *增加到管理View中，统一清理缓存
     * @param generatorView
     * View
     * @param coType
     * View类型
     * @param isRelation
     * 是否是关联控件
     */
    public void addViewToList(Generator generatorView, String coType, boolean isRelation) {
        MyLog.task("getPmFromTask: " + 888888888);
        MyLog.playTask("======添加view到集合中，类型=" + coType + " /是否是关联==" + isRelation);
        if (generatorView == null) {
            return;
        }
        try {
            genratorViewList.add(new CacheMemory(generatorView, coType, isRelation));
            generatorView.setPlayStateChangeListener(new TaskPlayStateListener() {

                @Override
                public void playComplete(int playTag) {
                    MyLog.playTask("播放结束回调==" + playTag + " / " + EtvService.GPIO_STATUES_CURRENT);
                    //触发节目不需要切换节目
                    changeProjectView(playTag);
                }

                /**
                 * 这是同步播放的单个节目播放完毕回调
                 * @param playTag
                 */
                @Override
                public void playCompletePosition(String etLevel, String taskId, int currentPlayPosition, int playTag) {

                }

                @Override
                public void clickTaskView(CpListEntity cpListEntity, List<String> list, int clickPosition) {
                }

                @Override
                public void longClickView(CpListEntity cpListEntity, Object object) {
                    if (playTaskView != null) {
                        playTaskView.toClickLongViewListener();
                    }
                }

                @Override
                public void reStartPlayProgram(String errorDesc) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 获取任务
     * 获取节目，获取节目中的控件
     * 视频优先。音频次之，图片次之
     * @param playTag
     * 这是测试代码
     */
    private void changeProjectView(int playTag) {
        SceneEntity sceneEntity = sceneEntityListCache.get(currentSencenPosition);
        if (sceneEntity == null) {
            playTaskView.playCompanyBack();
            return;
        }
        MyLog.playTask("=====播放完成回调====" + playTag);
        taskModel.playNextProgram(sceneEntityListCache, currentSencenPosition, playTag, AppInfo.TASK_TYPE_TRIGGER, new TaskRequestListener() {
            @Override
            public void modifyTxtInfoStatues(boolean isSuccess, String desc) {

            }

            @Override
            public void playNextProgram(boolean isBack, List<SceneEntity> sceneEntities, int tag) {
                MyLog.playTask("=====播放完成回调==playNextProgram==" + isBack);
                if (!isBack) {
                    return;
                }
                MyLog.playTask("=====播放完成回调==准备切换下一个状态==");
//                if (EtvService.GPIO_STATUES_CURRENT == EtvService.GPIO_STATUES_COME) {
//                    //人还在前台，继续轮询播放
//                    MyLog.playTask("=====播放完成回调==人还没有走，继续播放一轮==");
//                    return;
//                }
                MyLog.playTask("=====播放完成回调==停止播放触发节目==");
                playTaskView.playCompanyBack();
            }

            @Override
            public void finishMySelf(String errorDesc) {

            }

            @Override
            public void parserJsonOver(String tag, List<TaskWorkEntity> list) {

            }
        });
    }

//    /**
//     * 去播放下一个节目
//     *
//     * @param sceneEntities
//     */
//    private void toPlayNextProject(List<SceneEntity> sceneEntities) {
//        try {
//            currentSencenPosition++;
//            if (currentSencenPosition > (sceneEntities.size() - 1)) {
//                currentSencenPosition = 0;
//            }
//            MyLog.playTask("====播放结束了，切换节目===" + currentSencenPosition + " / " + sceneEntities.size());
//            MyLog.playTask("当前只有" + sceneEntities.size() + "个节目,执行下一步操作");
//            getPmFromTask(currentSencenPosition, "==去播放下一个节目==");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    //获取当前场景得播放时间
    private int getCurrentSencenPlayTime() {
        MyLog.task("getPmFromTask: " + 777777777);
        int backTime = 0;
        if (sceneEntityListCache == null || sceneEntityListCache.size() < 2) {
            return backTime;
        }
        SceneEntity sceneEntity = sceneEntityListCache.get(currentSencenPosition);
        if (sceneEntity == null) {
            return backTime;
        }
        String scenTime = sceneEntity.getScTime();
        if (scenTime == null || scenTime.length() < 1) {
            return backTime;
        }
        try {
            backTime = Integer.parseInt(scenTime);
            if (backTime < 5) {
                backTime = 0;
            }
        } catch (Exception e) {
            backTime = 0;
        }
        return backTime;
    }


    /**
     * 添加背景图片
     *
     * @param sceneEntity
     */
    private void addBackImageInfo(SceneEntity sceneEntity) {
        if (sceneEntity == null) {
            return;
        }
        String backFilePath = sceneEntity.getScBackImg();
        backFilePath = TaskDealUtil.getSavePath(backFilePath);
        String sencenType = sceneEntity.getPmType();
        if (backFilePath == null || backFilePath.length() < 3) {
            setDefaultBackColor(sencenType);
            return;
        }
        File file = new File(backFilePath);
        if (!file.exists()) {
            setDefaultBackColor(sencenType);
            return;
        }
        GlideImageUtil.loadImageByPath(context, backFilePath, iv_back_bgg);
    }


    /**
     * 设置默认的背景色
     *
     * @param sencenType
     */
    private void setDefaultBackColor(String sencenType) {
        try {
            GlideImageUtil.clearViewCache(context, iv_back_bgg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        iv_back_bgg.setBackgroundColor(context.getResources().getColor(R.color.white));
    }


    /***
     * 清理View缓存
     */
    public void clearMemory() {
        try {
            GlideCacheUtil.getInstance().clearImageAllCache(context);
            //去判断一下，如果是互动节目,就直接切换不做无缝，普通节目得话需要无缝切换操作
            String taskType = "1";
            SceneEntity currScenty = getCurrentSencenEntity();
            if (currScenty != null) {
                taskType = currScenty.getPmType();
            }
            MyLog.playTask("=====清理缓存一次==任务类型==" + taskType);
            view_abous.removeAllViews();
            if (genratorViewList != null || genratorViewList.size() > 0) {
                for (int i = 0; i < genratorViewList.size(); i++) {
                    Generator genView = genratorViewList.get(i).getGenerator();
                    genView.clearMemory();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
