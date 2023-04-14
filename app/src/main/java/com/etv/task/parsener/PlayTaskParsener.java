package com.etv.task.parsener;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.EtvApplication;
import com.diff.presentation.DifferentDislay;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.db.DbBggImageUtil;
import com.etv.db.DbDevMedia;
import com.etv.entity.BggImageEntity;
import com.etv.entity.ScreenEntity;
import com.etv.http.util.GetFileFromPathForRunnable;
import com.etv.listener.TaskPlayStateListener;
import com.etv.service.EtvService;
import com.etv.setting.GeneralSetActivity;
import com.etv.task.db.DBTaskUtil;
import com.etv.task.db.DbTaskManager;
import com.etv.task.entity.CacheMemory;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.MpListEntity;
import com.etv.task.entity.PositionEntity;
import com.etv.task.entity.ProjectJumpEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TaskWorkEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.model.TaskGetDbListener;
import com.etv.task.model.TaskModelUtil;
import com.etv.task.model.TaskModelmpl;
import com.etv.task.model.TaskMudel;
import com.etv.task.model.TaskRequestListener;
import com.etv.task.util.TaskDealUtil;
import com.etv.task.util.TaskGetTxtInsertRunnable;
import com.etv.task.view.PlayTaskView;
import com.etv.util.FileUtil;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.SimpleDateUtil;
import com.etv.util.TimerDealUtil;
import com.etv.util.system.CpuModel;
import com.etv.util.system.SystemManagerInstance;
import com.etv.util.system.VoiceManager;
import com.etv.util.weather.WeatherEntity;
import com.etv.util.weather.WeatherHttpRequest;
import com.etv.view.layout.Generator;
import com.etv.view.layout.ViewButtonGenerate;
import com.etv.view.layout.date.ViewDateGenerate;
import com.etv.view.layout.date.ViewTimeOnlyGenerate;
import com.etv.view.layout.date.ViewTimeReduceGenerate;
import com.etv.view.layout.date.ViewWeekGenerate;
import com.etv.view.layout.hdmi.ViewHdmiMLogicGenerate;
import com.etv.view.layout.image.ViewImageGenertrator;
import com.etv.view.layout.image.ViewLogoImageGenertrator;
import com.etv.view.layout.mixedswing.ViewImgVideoNetGenerate;
import com.etv.view.layout.music.ViewAudioGenertrator;
import com.etv.view.layout.text.ViewMatQueentestGenerte;
import com.etv.view.layout.text.ViewTexUpGenerate;
import com.etv.view.layout.text.ViewTextSlientGenerate;
import com.ys.bannerlib.util.GlideCacheUtil;
import com.ys.bannerlib.util.GlideImageUtil;
import com.ys.etv.R;
import com.ys.model.dialog.EditTextDialog;
import com.ys.model.listener.EditTextDialogListener;
import com.ys.model.util.FileMatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PlayTaskParsener {

    PlayTaskView playTaskView;
    Activity context;
    TaskMudel taskModel;
    AbsoluteLayout view_abous;
    ImageView iv_back_bgg;
    List<SceneEntity> sceneEntityListMain = new ArrayList<>();  //需要播放的场景集合
    List<SceneEntity> sceneEntityListSecond = new ArrayList<>(); //需要播放的场景集合


    public PlayTaskParsener(Activity context, PlayTaskView playTaskView) {
        this.context = context;
        this.playTaskView = playTaskView;
        taskModel = new TaskModelmpl();
        getView();
    }

    public PlayTaskParsener(Activity context, PlayTaskView playTaskView, int playPosition) {
        this.context = context;
        this.playTaskView = playTaskView;
        taskModel = new TaskModelmpl();
        currentSencenPosition = playPosition + currentSencenPosition;
        Log.e("TAG", "PlayTaskTriggerParsener: " + currentSencenPosition);
        getView();
    }

    TextView tv_video_error;

    public void setPlayPosition(int playPosition) {
        currentSencenPosition = currentSencenPosition + playPosition;
        Log.e("TAG", "setPlayPosition: " + currentSencenPosition);
    }

    private void getView() {
        //这里需要判断加载模式
        view_abous = playTaskView.getAbsoluteLayout();
        iv_back_bgg = playTaskView.getBggImageView();
        tv_video_error = playTaskView.getM11VideoErrorText();
    }

    public void getTaskToView(String tag) {
        lastUpdateWeatherTime = 0;
        //播放之前先去清理多余得素材
        checkTaskDownFile();
        MyLog.playTask("========任务播放界面刷新=getTaskToView=====" + tag);
        try {
            sceneEntityListMain.clear();
            sceneEntityListSecond.clear();
            projectJumpEntities.clear();
            currentSencenPosition = 0;
            taskModel.getPlayTaskListFormDb(new TaskGetDbListener() {
                @Override
                public void getTaskFromDb(List<TaskWorkEntity> list) {
                    if (list == null || list.size() < 1) {
                        MyLog.playTask("======播放界面===没有获取到需要播放的任务，去检查插播消息");
                        checkTxtInsertTask(false, "当前没有任务，去检查字幕插播", false);
                        return;
                    }
                    MyLog.playTask("======播放界面===获取到需要播放的任务==" + list.size());
                    //当前有任务，直接去检测插播任务
                    checkTxtInsertTask(true, "当前有任务。取检查字幕插播", false);
                    if (view_abous != null) {
                        view_abous.removeAllViews();
                    }
                    //清理副屏播放得信息
                    clearLastDoubleScreenView(TAG_CLEARVIEW_ONDESTORY);
                    parsenerTaskFromDb(list);
                }

                @Override
                public void getTaskTigerFromDb(TaskWorkEntity taskWorkEntity) {

                }
            }, "====播放界面，这里获取任务数据====", TaskModelUtil.DEL_LASTDATE_AND_AFTER_NOW);
        } catch (Exception e) {
            MyLog.playTask("==== getPlayTaskListFormDb " + e.toString());
            e.printStackTrace();
        }
    }

    /***
     * 解析通用版本得 TASK
     * @param taskWorkEntityList
     */
    private void parsenerTaskFromDb(List<TaskWorkEntity> taskWorkEntityList) {
        List<MpListEntity> mpListEntities = DBTaskUtil.getMpListInfoAll();
        MyLog.playTask("校验素材信息==000=" + mpListEntities);
        boolean isFileExict = TaskDealUtil.compairMpListFileExict(mpListEntities);
        MyLog.playTask("校验素材信息==111=" + isFileExict);
        if (!isFileExict) {
            playTaskView.showViewError(context.getString(R.string.no_resource_need_play));
            return;
        }
        List<SceneEntity> sceneEntityListCache = new ArrayList<>(); //需要播放的场景集合
        for (int i = 0; i < taskWorkEntityList.size(); i++) {
            TaskWorkEntity taskWorkEntity = taskWorkEntityList.get(i);
            String taskId = taskWorkEntity.getTaskId();
            EtvService.getInstance().updateProgressToWebRegister("进入界面，提交一次", taskId, "", 100, 0, "-1");
            if (taskWorkEntity == null) {
                break;
            }
            List<SceneEntity> listCacheSenc = DbTaskManager.getSencenEntityFormDbByTask(taskWorkEntity);
            if (listCacheSenc != null && listCacheSenc.size() > 0) {
                sceneEntityListCache.addAll(listCacheSenc);
            }
        }
        //这里获取任务场景失效，
        MyLog.d("DDD", "sceneEntityListCache size: " + sceneEntityListCache.size());
        if (sceneEntityListCache == null || sceneEntityListCache.size() < 1) {
            MyLog.playTask("==== 获取任务场景失败 2");
            playTaskView.showViewError("获取任务场景失败");
            return;
        }
        EtvApplication.getInstance().setTaskWorkEntityList(taskWorkEntityList);
        MyLog.playTask("====当前节目由多少个场景==" + sceneEntityListCache.size());
        for (int i = 0; i < sceneEntityListCache.size(); i++) {
            SceneEntity sceneEntity = sceneEntityListCache.get(i);
            MyLog.playTask("====遍历场景==" + "position=" + i + " / " + sceneEntity.toString());
            String disPosition = sceneEntity.getDisplayPos();
            if (disPosition == null || disPosition.length() < 1) { //防止NULL的情况
                sceneEntityListMain.add(sceneEntity);
            } else if (disPosition.contains(AppInfo.PROGRAM_POSITION_MAIN)) {
                sceneEntityListMain.add(sceneEntity);
                MyLog.playTask("====遍历场景==主频添加===" + sceneEntity.toString());
            } else if (disPosition.contains(AppInfo.PROGRAM_POSITION_SECOND)) {
                sceneEntityListSecond.add(sceneEntity);
                MyLog.playTask("====遍历场景==副频添加===" + sceneEntity.toString());
            }
        }
        //这里为了防止客户主副平下发一摸一样得节目，导致数据库保存得信息一致得问题
        if (sceneEntityListMain == null || sceneEntityListMain.size() < 1) {
            MyLog.playTask("====主屏没有素材=======0000=====");
            if (sceneEntityListSecond != null && sceneEntityListSecond.size() > 0) {
                MyLog.playTask("====主屏没有素材=====11111=======");
                sceneEntityListMain.addAll(sceneEntityListSecond);
                sceneEntityListSecond.clear();
            }
        }
        if (sceneEntityListMain == null || sceneEntityListMain.size() < 1) {
            playTaskView.showViewError("获取主界面任务场景失败");
            return;
        }
        Log.e("TAG", "startGpioPosition 2222: " + this);
        MyLog.playTask("=========主界面的场景个数===" + sceneEntityListMain.size() + "/" + sceneEntityListMain.get(0).getTaskid());
        SceneEntity sceneEntity = sceneEntityListMain.get(0);
        String scId = sceneEntity.getSenceId();
        MyLog.playTask("====TEXT getPmFromTask 1");
        getPmFromTask(currentSencenPosition, scId, false, "parsenerTaskFromDb");
    }

    //从任务中获取节目
    List<CpListEntity> cpCacheList = new ArrayList<>();
    //获取播放任务,任务检测，从这里开始
    int currentSencenPosition = 0;  //播放场景的位置

    /****
     *
     * @param position
     * 播放的位置
     * @param senceid
     * 场景的ID
     * @param isTouch
     * 释放时触摸任务
     * @param printTag
     * 打印标签
     * ea202fcdeba542e5a3af
     * 用户触摸类型=2 /coLinkAction= 2 / 10 / 46416cfc566e44d29be3
     */
    private void getPmFromTask(int position, String senceid, boolean isTouch, String printTag) {
        MyLog.task("==getPmFromTask==" + position + " /senceid= " + senceid + "/isTouch= " + isTouch);
        //清除时间变化观察者模式
        TimerDealUtil.getInstance().removeAllGenerator();
        //每次回到第一个场景,重新计时
        if (position == 0) {
            projectJumpEntities.clear();
            SceneEntity sceneEntityFrom = sceneEntityListMain.get(0);
            if (sceneEntityFrom != null) {
                ProjectJumpEntity projectJumpEntity = new ProjectJumpEntity(sceneEntityFrom, null, 0);
                addProJumpSencenToList(projectJumpEntity, "parsenerTaskFromDb");
            }
        }
        SceneEntity currentSceneEntity = sceneEntityListMain.get(position);
        if (isTouch) {
            //互动节目
            if (senceid == null || senceid.length() < 1) {
                MyLog.playTask("====谁在切换节目==根据位置获取场景 senceid==null");
                currentSceneEntity = sceneEntityListMain.get(position);
            } else {
                MyLog.playTask("====谁在切换节目==根据场景ID获取场景==" + senceid);
                currentSceneEntity = DbTaskManager.getSceneEntityBySeId(senceid);
            }
        } else {
            //正常普通的节目
            MyLog.playTask("====谁在切换节目000==根据位置获取场景");
            currentSceneEntity = sceneEntityListMain.get(position);
        }
        currentSencenPosition = position;
        try {
            clearMemory();    //这里会涉及到重复调用，所以会先清理一次内存
            MyLog.playTask("=====准备更新View===清空View===" + " / " + genratorViewList);
            cpListEntityArea = null;
            genratorViewList.clear();   //清理控件集合
            if (currentSceneEntity == null) {
                MyLog.playTask("====谁在切换节目==获取场景信息异常=null=");
                playTaskView.showViewError("获取场景信息异常");
                return;
            }
            String sencenId = currentSceneEntity.getSenceId();   // 场景得ID
            MyLog.playTask("===== 场景ID " + sencenId);
            List<CpListEntity> cpList = DbTaskManager.getComptionFromDbBySenId(sencenId);
            MyLog.playTask("===== 场景cpList " + (cpList == null ? "无法获取到" : cpList.size()));
            if (cpList == null || cpList.size() < 1) {
                playTaskView.showViewError("获取控件异常");
                return;
            }
            MyLog.playTask("====添加到集合的顺序==" + cpList.size());
            //对控件进行排序
            cpCacheList.clear();
            cpCacheList = TaskDealUtil.mathCpListOrder(cpList);
            //如果是互动节目，就要检查触摸返回得时间
            String pmType = currentSceneEntity.getPmType();
            if (pmType.contains(AppInfo.PROGRAM_TOUCH)) {
                //检查屏保时间
                autoJumpToNextSencentProjector();
            } else if (pmType.contains(AppInfo.PROGRAM_DEFAULT)) { //普通节目
                startTimerAutoJumpSencen();
            }
            boolean ifHasVideo = false;
            int videoDesc = 0;   //0 视频   1 HDMI   2流媒体
            tv_video_error.setVisibility(View.GONE);
            for (int i = 0; i < cpCacheList.size(); i++) {
                CpListEntity cpListEntity = cpCacheList.get(i);
                String coType = cpListEntity.getCoType();
                Log.e("TAG", "getPmFromTask: " + cpListEntity.getCoWidth() + "/" + cpListEntity.getCoHeight() + " 数据相关：" + cpListEntity.toString());
                if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11) && coType.equals(AppInfo.VIEW_STREAM_VIDEO)) {
                    tv_video_error.setText(context.getString(R.string.support_stream_current));
                    tv_video_error.setVisibility(View.VISIBLE);
                    continue;
                }
                if (coType.equals(AppInfo.VIEW_VIDEO) || coType.equals(AppInfo.VIEW_HDMI) || coType.equals(AppInfo.VIEW_STREAM_VIDEO)) {
                    if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11) && ifHasVideo) {
                        tv_video_error.setText(TaskDealUtil.getM11VideoErrorDesc(videoDesc, coType, context));
                        tv_video_error.setVisibility(View.VISIBLE);
                        continue;
                    }
                }
                if (coType.equals(AppInfo.VIEW_VIDEO)) {
                    videoDesc = 0;
                    ifHasVideo = true;
                }
                if (coType.equals(AppInfo.VIEW_HDMI)) {
                    videoDesc = 1;
                    ifHasVideo = true;
                }
                if (coType.equals(AppInfo.VIEW_STREAM_VIDEO)) {
                    videoDesc = 2;
                    ifHasVideo = true;
                }
                parperToShowView(cpListEntity);
            }
            //检测双屏任务
            if (sceneEntityListSecond != null && sceneEntityListSecond.size() > 0) {
                getDevScreenNum();
            } else { //单屏任务，这里需要dissmiss 副屏，防止双屏，单屏任务切换
                if (myPresentation != null) {
                    myPresentation.dismiss();
                    myPresentation = null;
                }
            }
            handler.sendEmptyMessageDelayed(CLEAR_LAST_VIEW_FROM_LIST, 2000);

            //加载logo
            loadLogoImage();
            //放后边，防止第一次加载失败
            addBackImageInfo(currentSceneEntity); //添加背景图片
            handler.sendEmptyMessageDelayed(UPDATE_VIDEO_FILR_TO_WEB, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 修改logo
     * @param printTag
     */
    public void modifyLogoShowInfo(String printTag) {
        MyLog.playTask("======modifyLogoShowInfo======" + printTag);
        if (genratorViewList == null || genratorViewList.size() < 1) {
            return;
        }
        try {
            for (CacheMemory cacheMemory : genratorViewList) {
                if (cacheMemory == null) {
                    continue;
                }
                String cpType = cacheMemory.getCoType();
                System.out.println("aaaaaaaaaaaaaaaaaa----------> " + cpType);
                Generator generator = cacheMemory.getGenerator();
                if (cpType.contains(AppInfo.VIEW_LOGO)) {
                    genratorViewList.remove(cacheMemory);
                    view_abous.removeView(generator.getView());
                    generator.clearMemory();
                    loadLogoImage();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 加载 logo
     */
    private void loadLogoImage() {
        try {
            BggImageEntity bggImageEntity = DbBggImageUtil.getLogoInfoFromDb();
            if (bggImageEntity == null) {
                MyLog.bgg("=======节目加载的路径===bggImageEntity == null");
                return;
            }
            String filePath = bggImageEntity.getSavePath();
            String fileName = bggImageEntity.getImageName();
            String allPath = filePath + "/" + fileName;
            MyLog.bgg("=======节目加载的路径===" + allPath);
            File file = new File(allPath);
            if (!file.exists()) {
                MyLog.bgg("=======节目加载的路径==logo文件不存在=");
                return;
            }
            String showStyle = bggImageEntity.getFileType();
            MyLog.bgg("=======节目加载的路径==111=" + showStyle);
            PositionEntity positionEntity = TaskDealUtil.getLogoShowPosition(showStyle, allPath);
            if (positionEntity == null) {
                MyLog.bgg("=======节目加载的路径==222=");
                return;
            }
            MyLog.bgg("=======节目加载的路径==333=" + positionEntity.toString() + " / " + allPath);
            clearLogoView();
            Generator generatorView = new ViewLogoImageGenertrator(context, positionEntity.getLeftPosition(),
                    positionEntity.getTopPosition(), positionEntity.getWidth(), positionEntity.getHeight(), allPath);
            addViewToList(generatorView, AppInfo.VIEW_LOGO, false, null);
            generatorView.getView().setTag(generatorView);
            view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
            generatorView.updateView(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearLogoView() {
        int index = 0;
        while (index < view_abous.getChildCount()) {
            View child = view_abous.getChildAt(index);
            if (child.getTag() instanceof ViewLogoImageGenertrator) {
                view_abous.removeView(child);
            }
            index++;
        }
    }

    //获取当前场景得播放时间
    private int getCurrentSencenPlayTime() {
        int backTime = 0;
        if (sceneEntityListMain == null || sceneEntityListMain.size() < 2) {
            return backTime;
        }
        SceneEntity sceneEntity = sceneEntityListMain.get(currentSencenPosition);
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
     * 检查插播消息
     *
     * @param ifHasTask 是否有正常的任务
     */
    private void checkTxtInsertTask(boolean ifHasTask, String printTag, boolean timeToUpdate) {
        MyLog.playTask("====检查插播消息===checkTxtInsert==000==" + printTag);
        TaskGetTxtInsertRunnable runnable = new TaskGetTxtInsertRunnable(new TaskGetTxtInsertRunnable.GetTaskTxtInsertListener() {
            @Override
            public void backCpEntity(String taskId, CpListEntity cpListEntity, String errorDesc) {
                MyLog.playTask("====检查插播消息===checkTxtInsert==000==" + " /cpListEntity== " + (cpListEntity == null) + " /errorDesc =" + errorDesc);
                if (cpListEntity == null) {
                    MyLog.playTask("===检查插播消息===当前没有播放的任务,errorDesc  false==" + errorDesc);
                    playTaskView.playInsertTextTaskToPopWindows(false, null);
                    if (ifHasTask) {
                        return;
                    }
                    playTaskView.showViewError("当前没有播放的任务:" + errorDesc);
                    return;
                }
                if (!timeToUpdate) {
                    //开始播放的时候提交一次，时间变化，不提交
                    EtvService.getInstance().updateProgressToWebRegister("进入界面，提交一次", taskId, "", 100, 0, "-1");
                }
                playTaskView.playInsertTextTaskToPopWindows(true, cpListEntity);
            }
        });
        EtvService.getInstance().executor(runnable);
    }

    /**
     * 添加背景图片
     *
     * @param sceneEntity
     */
    private void addBackImageInfo(SceneEntity sceneEntity) {
        if (sceneEntity == null) {
            iv_back_bgg.setImageResource(R.color.white);
            return;
        }
        String backFilePathShow = TaskDealUtil.getSavePath(sceneEntity.getScBackImg());
        if (backFilePathShow == null || backFilePathShow.length() < 3) {
            iv_back_bgg.setImageResource(R.color.white);
            return;
        }
        File file = new File(backFilePathShow);
        if (!file.exists()) {
            iv_back_bgg.setImageResource(R.color.white);
            return;
        }
        GlideImageUtil.loadImageByPath(context, backFilePathShow, iv_back_bgg);
    }

    //用来封装播放view的
    List<CacheMemory> genratorViewList = new ArrayList<CacheMemory>();
    //屏幕得宽高d'd
    int screenWidth = SharedPerUtil.getScreenWidth();
    int screenHeight = SharedPerUtil.getScreenHeight();
    List<MpListEntity> mpList = new ArrayList<MpListEntity>();     //控件的素材信息
    List<TextInfo> txtList = new ArrayList<TextInfo>();  //控件的文本属性
    CpListEntity cpListEntityArea;    //区域控件属性封装

    /***
     * 加载区域控件
     * @param cpListEntityShow
     * @param parentCoId
     * 如果==-1  表示只查询欧通素材
     * 如果等于其他得数据，表示查询关联素材
     */
    public void parperToShowAreaView(CpListEntity cpListEntityShow, int parentCoId) {
        MyLog.playTask("=======parperToShowAreaView，重新加载===" + parentCoId + " / " + cpListEntityShow.toString());
        //移除上一次添加得view,防止添加过多得View
        if (genratorViewList != null && genratorViewList.size() > 0) {
            for (int i = 0; i < genratorViewList.size(); i++) {
                boolean isRelation = genratorViewList.get(i).isRelation();
                if (isRelation) {
                    Generator genView = genratorViewList.get(i).getGenerator();
                    String taType = genratorViewList.get(i).getCoType();
                    MyLog.playTask("====parperToShowAreaView===这里需要移除区域类，重新加载===" + taType);
                    genratorViewList.remove(i);
                    MyLog.playTask("===parperToShowAreaView===remouve===" + genratorViewList);
                    view_abous.removeView(genView.getView());
                    genView.clearMemory();
                }
            }
        }
        //这里才是开始得逻辑
        if (cpListEntityArea == null) {
            playTaskView.showViewError("控件解析失败");
            Log.e("TAG", "parperToShowAreaView: " + 123456);
            return;
        }
        try {
            int leftPosition = TaskDealUtil.StringToFloat(cpListEntityArea.getCoLeftPosition());
            int topPosition = TaskDealUtil.StringToFloat(cpListEntityArea.getCoRightPosition());
            int width = TaskDealUtil.StringToFloat(cpListEntityArea.getCoWidth());
            int height = TaskDealUtil.StringToFloat(cpListEntityArea.getCoHeight());

            Log.e("liujk", "获取区域控件： leftPosition :" + leftPosition + " topPosition: " + topPosition + " width：" + width + " height: " + height);

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

            /**
             * 针对4K 显示屏幕，多个控件设置区域，不会在指定区域显示
             */
            if (SharedPerUtil.getScreenWidth() == 3840 || SharedPerUtil.getScreenWidth() == 2160) {
                leftPosition = leftPosition * 2;
                topPosition = topPosition * 2;
                width = width * 2;
                height = height * 2;
            }
            String cpId = cpListEntityShow.getCpidId();
            MyLog.playTask("======parperToShowAreaView===区域跳转====" + cpId + " / " + parentCoId + " /leftPosition = "
                    + leftPosition + "/" + topPosition + " / " + width + " / " + height);
            if (parentCoId == DBTaskUtil.MP_DEFAULT) {
                //加载第一张图得默认效果
                loadMixImgVideoView(cpId, parentCoId, leftPosition, topPosition, width, height, true);
                return;
            }
            List<TextInfo> textInfos = DBTaskUtil.getTxtParsentListInfoById(cpId, DBTaskUtil.MP_RELATION);
            if (textInfos != null && textInfos.size() > 0) {
                MyLog.playTask("======parperToShowAreaView===查询有网页需要加载====" + cpId);
                //这里去添加网页
                TextInfo textInfo = textInfos.get(0);
                if (textInfo == null) {
                    return;
                }
                String txtContentWeb = textInfo.getTaContent();
                String moveWeb = textInfo.getTaMove();
                Generator generatorView = TaskDealUtil.getWebViewBySpeedString(context, moveWeb, leftPosition, topPosition, width, height, txtContentWeb);
                view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                addViewToList(generatorView, AppInfo.VIEW_WEB_PAGE, true, null);
                return;
            }
            MyLog.playTask("======parperToShowAreaView===去加载素材资源====" + cpId);
            //去加载素材资源
            loadMixImgVideoView(cpId, parentCoId, leftPosition, topPosition, width, height, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 加载混播view得属性 View
     * @param cpId
     * @param parentCoId
     * @param leftPosition
     * @param topPosition
     * @param width
     * @param height
     */
    private void loadMixImgVideoView(String cpId, int parentCoId, int leftPosition, int topPosition, int width, int height, boolean isTrue) {
        MyLog.playTask("===loadMixImgVideoView");
        List<MpListEntity> mpListEntities = DBTaskUtil.getMpListInfoById(cpId, parentCoId, "parperToShowAreaView");
        if (mpListEntities == null || mpListEntities.size() < 1) {
            MyLog.playTask("====parperToShowAreaView=混播播放的素材 =null===" + parentCoId);
            return;
        }
        MyLog.playTask("====parperToShowAreaView=混播播放的素材 ====" + mpListEntities.size() + " / " + parentCoId);
        String mpType = mpListEntities.get(0).getType();
        List<MediAddEntity> mixtureList = TaskDealUtil.getResourceListPath(mpListEntities);
        if (mixtureList == null || mixtureList.size() < 1) {
            return;
        }
        MyLog.playTask("====parperToShowAreaView====加载控件类型==" + mixtureList.size());
        Generator generatorView;
        if (mpType.equals(AppInfo.VIEW_IMAGE)) {  //图片类型素材
            generatorView = new ViewImageGenertrator(context, null, leftPosition, topPosition, width, height, mixtureList, isTrue);
            MyLog.playTask("=====parperToShowAreaView===加载控件类型====image===" + leftPosition + " / " + topPosition + " / " + width + " / " + height);
            addViewToList(generatorView, AppInfo.VIEW_IMAGE, true, null);
            view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
            generatorView.updateView(null, true);
        } else if (mpType.equals(AppInfo.VIEW_VIDEO)) {  //视频类型控件
            MyLog.playTask("======parperToShowAreaView==加载控件类型====video===" + leftPosition + " / " + topPosition + " / " + width + " / " + height);
            generatorView = TaskDealUtil.getVideoPlayView(context, null, leftPosition, topPosition, width, height, mixtureList, AppInfo.PROGRAM_POSITION_MAIN, isTrue);
            addViewToList(generatorView, AppInfo.VIEW_VIDEO, true, null);
            view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
            generatorView.updateView(null, true);
        } else if (mpType.equals(AppInfo.VIEW_DOC)) { //文档
            MediAddEntity mediAddEntity = mixtureList.get(0);
            String fileUrl = mediAddEntity.getUrl();
            String deaution = mediAddEntity.getPlayParam();
            MyLog.playTask("文档得切换时间 ： " + deaution);
            int fileType = FileMatch.fileMatch(fileUrl);
            generatorView = TaskDealUtil.getPdfShowView(context, fileType, null, leftPosition, topPosition, width, height, mediAddEntity, mixtureList);
            addViewToList(generatorView, AppInfo.VIEW_DOC, true, null);
            view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
            generatorView.updateView(mediAddEntity, true);

        }
    }

    public void parperToShowView(CpListEntity cpEntity) {
        MyLog.playTask("===parperToShowView");
        if (cpEntity == null) {
            playTaskView.showViewError("控件解析失败");
            Log.e("TAG", "parperToShowAreaView: " + 456789);
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
                case AppInfo.VIEW_AREA: //区域显示
                    this.cpListEntityArea = cpEntity;
                    //默认第一次显示第一个控件里面得信息
                    if (cpCacheList == null || cpCacheList.size() < 1) {
                        return;
                    }
                    MyLog.playTask("====准备显示区域==>>" + cpEntity.toString());
                    List<MediAddEntity> areaList = TaskDealUtil.getResourceListPath(mpList);
                    if (areaList == null || areaList.size() < 1) {
                        MyLog.playTask("====准备显示区域==areaList==null");
                        return;
                    }
                    MyLog.playTask("====准备显示区域==areaList==" + areaList.size());
                    SceneEntity areaScentity = getCurrentSencenEntity();
                    Log.e("liujk", "leftPosition : " + leftPosition + " topPosition: " + topPosition + " width: " + width + " height:" + height);
                    generatorView = new ViewImgVideoNetGenerate(context, null, areaScentity, leftPosition, topPosition, width, height, areaList, true, 0, AppInfo.PROGRAM_POSITION_MAIN, true);
                    addViewToList(generatorView, coType, false, null);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    break;
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
//                    generatorView = new ViewImageGenertrator(context, cpEntity, leftPosition, topPosition, width, height, imageList, false);
                    addViewToList(generatorView, coType, false, null);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(null, true);
                    break;
                case AppInfo.VIEW_VIDEO: //视频格式
                    MyLog.playTask("视频格式数据源");
                    List<MediAddEntity> videoList = TaskDealUtil.getResourceListPath(mpList);
                    if (videoList == null || videoList.size() < 1) {
                        MyLog.playTask("====视频的坐标的坐标==竖屏没有需要播放的素材");
                        return;
                    }
                    generatorView = TaskDealUtil.getVideoPlayView(context, cpEntity, leftPosition, topPosition, width, height, videoList, AppInfo.PROGRAM_POSITION_MAIN, false);
                    MyLog.playTask("====视频的坐标的坐标==4k support=" + " / videoList=" + videoList.get(0).getUrl());
                    addViewToList(generatorView, coType, false, null);
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
                    addViewToList(generatorView, coType, false, null);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    break;
                case AppInfo.VIEW_AUDIO:                         //音频
                    List<MediAddEntity> audioList = TaskDealUtil.getResourceListPath(mpList);
                    if (audioList == null || audioList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewAudioGenertrator(context, 0, 0, 1, 1, audioList);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    addViewToList(generatorView, coType, false, null);
                    break;
                case AppInfo.VIEW_WEB_PAGE:     //  网页
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    TextInfo textInfo = txtList.get(0);
                    if (textInfo == null) {
                        return;
                    }
                    String txtContentWeb = textInfo.getTaContent();
                    MyLog.playTask("网络网址=000==" + txtContentWeb);
                    boolean isShowBtn = true;
                    //加一个判断， H5 类型的节目
                    String moveWeb = textInfo.getTaMove();
                    String pmType = textInfo.getPmType();
                    if (pmType.contains(AppInfo.PROGRAM_HTML_5)) {
                        isShowBtn = false;
                        txtContentWeb = TaskDealUtil.getH5IpAddress(txtContentWeb);
                        MyLog.playTask("网络网址=111==" + txtContentWeb);
                    }
                    generatorView = TaskDealUtil.getWebViewBySpeedString(context, moveWeb, leftPosition, topPosition, width, height, txtContentWeb);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    addViewToList(generatorView, coType, false, null);
                    //网页不用这个方法，这个是刷新功能
                    generatorView.updateView(textInfo, isShowBtn);
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
                        addViewToList(generatorView, coType, false, null);
                    }
                    break;
                case AppInfo.VIEW_STREAM_VIDEO:  //流媒体
                    MyLog.playTask("====加载流媒体控件==VIEW_STREAM_VIDEO==" + leftPosition + " / " + topPosition + " / " + width + " / " + height);
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    TextInfo textInfoStream = txtList.get(0);
                    String streamUrl = textInfoStream.getTaContent();
                    MyLog.playTask("====加载流媒体控件==streamUrl==" + streamUrl);
                    String moveStream = textInfoStream.getTaMove();
                    generatorView = TaskDealUtil.getStreamGenViewBySpeed(context, moveStream, cpEntity, leftPosition, topPosition,
                            width, height, streamUrl, coType);
                    addViewToList(generatorView, coType, false, null);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(null, true);
                    break;
                case AppInfo.VIEW_WEATHER:   //天气。
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    TextInfo textInfoWeather = txtList.get(0);
                    if (textInfoWeather == null) {
                        return;
                    }
                    String city = textInfoWeather.getTaAddress();
                    MyLog.playTask("====天气布局的坐标点==>>" + leftPosition + " / " + topPosition + " / " + width + " / " + height +
                            "\n textInfoWeather=" + textInfoWeather.toString());
                    generatorView = TaskDealUtil.getWeatherGenWeatherView(context, leftPosition, topPosition, width, height, textInfoWeather);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    //刷新界面
                    String textColor = textInfoWeather.getTaColor();
                    String bggColor = textInfoWeather.getTaBgColor();
                    WeatherEntity weatherCache = new WeatherEntity(city, "多云", "15℃", "25℃", textColor, bggColor);
                    addViewToList(generatorView, coType, false, weatherCache);
                    generatorView.updateView(weatherCache, true);
                    getWeatherFromWeb(generatorView, weatherCache);
                    break;
                case AppInfo.VIEW_DATE:   //日期
                    MyLog.playTask("====时间的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height);
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }

                    MyLog.playTask("===== 文本数据size: " + txtList.size());
                    generatorView = new ViewDateGenerate(context, leftPosition, topPosition, width, height);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    MyLog.playTask("==== textInfo ： " + txtList.get(0).toString());
                    generatorView.updateView(txtList.get(0), true);
                    addViewToList(generatorView, coType, false, null);
                    break;
                case AppInfo.VIEW_WEEK:   //星期
                    MyLog.playTask("====时间的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height);
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewWeekGenerate(context, leftPosition, topPosition, width, height);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    addViewToList(generatorView, coType, false, null);
                    break;
                case AppInfo.VIEW_TIME:   //时间
                    MyLog.playTask("====时间的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height);
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewTimeOnlyGenerate(context, leftPosition, topPosition, width, height, false);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    addViewToList(generatorView, coType, false, null);
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
                    addViewToList(generatorView, coType, false, null);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(textInfo1, true);
                    break;
                case AppInfo.VIEW_BUTTON:
                    MyLog.playTask("button的坐标===" + leftPosition + "/" + topPosition + "/" + width + "/" + height);
                    if (txtList == null || txtList.size() < 1) {
                        MyLog.playTask("button的坐标===没有数据，终端操作");
                        return;
                    }
//                    MyLog.playTask("button的坐标===txtList总共数据==" + (txtList.size()));
                    for (int i = 0; i < txtList.size(); i++) {
                        TextInfo textInfoCache = txtList.get(i);
                        int parentCoId = textInfoCache.getParentCoId();
//                        MyLog.playTask("button的坐标===parentCoId==" + parentCoId);
                        if (parentCoId == DBTaskUtil.MP_RELATION) {
                            txtList.remove(i);
//                            MyLog.playTask("button的坐标===parentCoId移除==" + textInfoCache.toString());
                        }
                    }
                    if (txtList == null || txtList.size() < 1) {
//                        MyLog.playTask("button的坐标===txtList剩余数据==0");
                        return;
                    }
//                    MyLog.playTask("button的坐标===txtList剩余数据==" + (txtList.size()));
                    TextInfo textShow = txtList.get(0);
                    String txtContent = textShow.getTaContent();
                    generatorView = new ViewButtonGenerate(context, cpEntity, leftPosition, topPosition, width, height, txtContent);
                    addViewToList(generatorView, coType, false, null);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(textShow, true);
                    break;
                case AppInfo.VIEW_COUNT_DOWN://倒计时
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewTimeReduceGenerate(context, cpEntity, leftPosition, topPosition, width, height);
                    addViewToList(generatorView, coType, false, null);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    break;
                case AppInfo.VIEW_HDMI:
                    MyLog.playTask("====Hdmi的坐标点==>>" + leftPosition + " / " + topPosition + " / " + width + " / " + height);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        MyLog.playTask("====Hdmi的坐标点==>>小于5.0.不执行");
                        playTaskView.showToastView(context.getString(R.string.system_is_low));
                        return;
                    }
                    switch (CpuModel.getMobileType()) {
                        case CpuModel.CPU_MODEL_MLOGIC:
                        case CpuModel.CPU_MODEL_T982:
                        case CpuModel.CPU_MODEL_MTK_M11:
                            //下面是 MLOGIC 的业务逻辑
                            generatorView = new ViewHdmiMLogicGenerate(context, cpEntity, leftPosition, topPosition, width, height);
                            Log.e("TAG", "parperToShowView: " + width + "////" + height);
                            addViewToList(generatorView, coType, false, null);
                            view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                            generatorView.updateView(null, true);
                            break;
                        default:
                            playTaskView.showHdmInViewToActivity(leftPosition, topPosition, width, height);
                            break;
                    }
                    break;
            }
        } catch (Exception e) {
            MyLog.ExceptionPrint("播放界面布局异常: " + e.toString());
            e.printStackTrace();
        }
    }

    // 上一次获取天气的时间
    private long lastUpdateWeatherTime = 0;

    /***
     * 获取天气
     */
    private void getWeatherFromWeb(Generator generator, WeatherEntity weatherCache) {
        if (weatherCache == null) {
            MyLog.cdl("=WeatherEntity===获取天气失败，城市==null");
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(context)) {
            MyLog.cdl("=WeatherEntity===获取天气失败，网络异常");
            return;
        }
        long currentTime = System.currentTimeMillis();
        int randomTimeAdd = new Random().nextInt(100) * 1000;
        long timeDistance = (1000 * 60 * 60 * 2) + randomTimeAdd;
        if (currentTime - lastUpdateWeatherTime < timeDistance) {
            MyLog.cdl("=WeatherEntity===获取天气失败，间隔时间小于 2 小时");
            return;
        }
        String city = weatherCache.getCity();
        WeatherHttpRequest.getWeather(city, new WeatherHttpRequest.WeatherStateListener() {
            @Override
            public void getWeatherState(WeatherEntity entity) {
                if (entity == null) {
                    MyLog.cdl("=WeatherEntity===获取天气失败，entity==null");
                    return;
                }
                lastUpdateWeatherTime = System.currentTimeMillis();
                MyLog.playTask("==WeatherEntity==控件获取的天气信息==" + entity.toString());
                weatherCache.setCity(entity.getCity());
                weatherCache.setLowTem(entity.getLowTem());
                weatherCache.setHeightTem(entity.getHeightTem());
                weatherCache.setWeatherInfo(entity.getWeatherInfo());
                generator.updateView(weatherCache, true);
            }

            @Override
            public void getFailed(String desc) {
                MyLog.playTask("获取的天气信息error==" + desc);
            }
        });
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
    public void addViewToList(Generator generatorView, String coType, boolean isRelation, WeatherEntity weatherEntity) {
        MyLog.playTask("======添加view到集合中，类型=" + coType + " /是否是关联==" + isRelation);
        if (generatorView == null) {
            return;
        }
        try {
            //控件播放完毕,回掉监听

            if (coType.equals(AppInfo.VIEW_WEATHER)) {
                genratorViewList.add(new CacheMemory(generatorView, coType, isRelation, weatherEntity));
            } else {
                genratorViewList.add(new CacheMemory(generatorView, coType, isRelation));
            }
            generatorView.setPlayStateChangeListener(new TaskPlayStateListener() {

                @Override
                public void playComplete(int playTag) {
                    MyLog.playTask("=====播放完毕回调====playTag=" + playTag);
                    updateTotalShowSizeToWeb(); //提交统计信息给服务器
                    if (generatorView.getClass() == ViewImgVideoNetGenerate.class) {
                        //混播得View 得和 混播得标记融合
                        if (playTag == TaskPlayStateListener.TAG_PLAY_VIDEO_IMAGE) {
                            changeProjectView(playTag);
                        }
                        return;
                    }
                    changeProjectView(playTag);
                }

                /**
                 * 这是同步播放的单个节目播放完毕回调
                 * @param playTag
                 */
                @Override
                public void playCompletePosition(String etLevel, String taskId, int currentPlayPosition, int playTag) {
                    if (playTag != TaskPlayStateListener.TAG_PLAY_VIDEO_IMAGE) {
                        return;
                    }
                }

                @Override
                public void clickTaskView(CpListEntity cpListEntity, List<String> list, int clickPosition) {
                    if (cpListEntity == null) {
                        return;
                    }
                    MyLog.playTask("=====点击了view===" + cpListEntity.toString());
                    String coType = cpListEntity.getCoType();
                    if (coType.contains(AppInfo.VIEW_HDMI)) {
                        startToSettingActivity();
                        return;
                    }
                    toGoToShowViewActivity(cpListEntity, list, clickPosition);
                }

                @Override
                public void longClickView(CpListEntity cpListEntity, Object object) {
                    MyLog.playMix("===混播监听到点击事件--主界面=longClickView==");
                    if (playTaskView != null) {
                        playTaskView.toClickLongViewListener();
                    }
//                    if (cpListEntity == null) {
//                        return;
//                    }
//                    String textModify = (String) object;
//                    if (textModify == null || textModify.length() < 1) {
//                        return;
//                    }
//                    showModifyContentDialog(cpListEntity, textModify);
                }

                @Override
                public void reStartPlayProgram(String errorDesc) {
                    MyLog.d("DDD", "getTaskToView 222");
                    getTaskToView("播放异常，重启播放一次");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 去设置界面
     */
    private void startToSettingActivity() {
        try {
            Intent intent = new Intent(context, GeneralSetActivity.class);
            intent.putExtra(GeneralSetActivity.TAG_SETTING_INFO, 1);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //提交统计信息
    private void updateTotalShowSizeToWeb() {
        //上传之前，判断一下，有没有权限上传记录
        boolean playUpdate = SharedPerManager.getPlayTotalUpdate();
        if (!playUpdate) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(EtvService.UPDATE_STA_TOTAL_TO_WEB);
        context.sendBroadcast(intent);
    }

    private void showModifyContentDialog(final CpListEntity cpListEntity, String textModify) {
        if (cpListEntity == null) {
            return;
        }
        if (textModify == null || textModify.length() < 1) {
            return;
        }
        EditTextDialog dialog = new EditTextDialog(context);
        dialog.setOnDialogClickListener(new EditTextDialogListener() {
            @Override
            public void clickHiddleView() {

            }

            @Override
            public void commit(String content) {
                if (TextUtils.isEmpty(content)) {
                    playTaskView.showToastView("请输入");
                    return;
                }
                modifyTextInfoToWeb(cpListEntity, content);
            }
        });
        dialog.show("修改字幕", textModify, "提交");
    }

    /**
     * 修改字幕信息
     *
     * @param cpListEntity
     * @param textModify
     */
    private void modifyTextInfoToWeb(CpListEntity cpListEntity, String textModify) {
        String cpId = cpListEntity.getCpidId();
        List<TextInfo> txList = DBTaskUtil.getTxtListInfoById(cpId);
        if (txList == null || txList.size() < 1) {
            return;
        }
        TextInfo textInfo = null;
        for (int i = 0; i < txList.size(); i++) {
            textInfo = txList.get(i);
            int parsentType = textInfo.getParentCoId();
            if (parsentType == DBTaskUtil.MP_DEFAULT) {
                break;
            }
        }
        String textId = textInfo.getTxtId();
        taskModel.modifyTextInfoToWeb(textId, textModify, new TaskRequestListener() {
            @Override
            public void modifyTxtInfoStatues(boolean isSuccess, String desc) {
                if (!isSuccess) {
                    playTaskView.showToastView(desc);
                    return;
                }
                playTaskView.findTaskNew();
            }

            @Override
            public void playNextProgram(boolean isBack, List<SceneEntity> sceneEntities, int tag) {

            }

            @Override
            public void finishMySelf(String errorDesc) {

            }

            @Override
            public void parserJsonOver(String tag, List<TaskWorkEntity> list) {

            }
        });
    }

    //封装节目跳转的集合
    List<ProjectJumpEntity> projectJumpEntities = new ArrayList<ProjectJumpEntity>();

    /**
     * 点击事件处理
     *
     * @param cpListEntity
     * @param list
     */
    private void toGoToShowViewActivity(CpListEntity cpListEntity, List<String> list, int ClickPosition) {
        if (!SharedPerManager.getTaskTouchEnable()) {
            if (projectJumpEntities != null) {
                projectJumpEntities.clear();
            }
            return;
        }
        try {
            if (cpListEntity == null) {
                //点击wei=null,不操作
                MyLog.d("TOUCH", "====/点击wei=null,不操作");
                return;
            }
            String coActionType = cpListEntity.getCoActionType().trim(); //互动类型
            String coLinkAction = cpListEntity.getCoLinkAction();        //互动行为
            String backTime = cpListEntity.getCoScreenProtectTime();    //屏保时间
            String nextSceneEntity = cpListEntity.getCoLinkId();        //跳转场景的 场景ID
            MyLog.touch("=====用户触摸类型=" + coActionType
                    + " /coLinkAction= "
                    + coLinkAction + " / "
                    + backTime + " / "
                    + nextSceneEntity);

            switch (coActionType) {
                case AppInfo.TOUCH_TYPE_NONE:  //没有触摸行为
                    //视威
                    break;
                case AppInfo.TOUCH_TYPE_JUMP_SENCEN: //跳转场景
                    //保存触摸到集合里面
                    ProjectJumpEntity projectJumpEntity = new ProjectJumpEntity();
                    SceneEntity sceneEntityFrom = getCurrentSencenEntity();
                    projectJumpEntity.setSceneEntityFrom(sceneEntityFrom);
                    //执行场景跳转的功能
                    int nextSencenPosition = Integer.parseInt(coLinkAction);
                    MyLog.d("TOUCH", "=====跳转场景==coLinkAction==" + coLinkAction);
                    nextSencenPosition = nextSencenPosition - 1;
                    if (nextSencenPosition < 0) {
                        nextSencenPosition = 0;
                    }
                    //添加记录到集合
                    SceneEntity sceneEntityTo = sceneEntityListMain.get(nextSencenPosition);
                    String scenId = sceneEntityTo.getSenceId();
                    //证明商标
                    MyLog.d("TOUCH", "=====跳转场景=000===" + nextSencenPosition + " /scenId= " + scenId);
                    int projectorTime = getCurrentSencenProjectorTimeByCpEntity(cpListEntity);
                    projectJumpEntity.setSceneEntityTo(sceneEntityTo);
                    projectJumpEntity.setProjectorTime(projectorTime);
                    addProJumpSencenToList(projectJumpEntity, " case AppInfo.TOUCH_TYPE_JUMP_SENCEN: //跳转场景");
                    //====准备布局到界面===============================
                    MyLog.playTask("====TEXT getPmFromTask 2");
                    getPmFromTask(nextSencenPosition, nextSceneEntity, true, "触摸互动场景跳转");
                    break;
                case AppInfo.TOUCH_TYPE_JUMP_WEB:  //跳转网页
                    playTaskView.startViewWebActivty(coLinkAction, backTime);
                    break;
                case AppInfo.TOUCH_TYPE_JUMP_SCREEN: //全屏显示
                    playTaskView.toShowFullScreenView(cpListEntity, list, ClickPosition);
                    break;
                case AppInfo.TOUCH_TYPE_JUMP_APK:
                    if (coLinkAction == null || coLinkAction.length() < 3) {
                        return;
                    }
                    playTaskView.startApkView(coLinkAction, backTime);
                    break;
                case AppInfo.TOUCH_TYPE_JUMP_BACK:   // 6
                    // 返回，播放上一个场景
                    playBackSencenView();
                    break;
                case AppInfo.TOUCH_TYPE_FORWORD_VIEW: //区域关联导向图
                    parperToShowAreaView(cpListEntity, DBTaskUtil.MP_RELATION);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取控件得屏保时间
     *
     * @param cpListEntity
     * @return
     */
    private int getCurrentSencenProjectorTimeByCpEntity(CpListEntity cpListEntity) {
        int projectTime = 0;
        if (cpListEntity == null) {
            return projectTime;
        }
        String time = cpListEntity.getCoScreenProtectTime().trim();
        if (time == null || time.length() < 1) {
            return projectTime;
        }
        projectTime = Integer.parseInt(time);
        if (projectTime < 3) {
            projectTime = 0;
        }
        return projectTime;
    }

    /***
     * 场景时间跳转
     */
    public void autoJumpToNextSencentProjector() {
        if (!SharedPerManager.getTaskTouchEnable()) {
            MyLog.d("TOUCH", "===屏保=====开关未打开=");
            return;
        }
        ProjectJumpEntity projectJumpEntity = getLastProJumpEntity();
        if (projectJumpEntity == null) {
            MyLog.d("TOUCH", "===屏保======获取得projectJumpEntity==null=");
            return;
        }
        long projectorTimeNew = projectJumpEntity.getProjectorTime();
        if (projectorTimeNew < 3) {
            MyLog.d("TOUCH", "===屏保======时间小于3 中断操作=");
            return;
        }
        SceneEntity sceneEntityTo = projectJumpEntity.getSceneEntityTo();
        String pmType = sceneEntityTo.getPmType();                //节目类型  普通，互动
        if (!pmType.contains(AppInfo.PROGRAM_TOUCH)) {
            MyLog.d("TOUCH", "===屏保=======不是互动节目，不跳转===");
            return;
        }
        MyLog.d("TOUCH", "===屏保=======开始计时===" + projectorTimeNew);
        cacelTimerTask();
        timerCheckTask = new Timer(true);
        checkTask = new CheckTask();
        timerCheckTask.schedule(checkTask, projectorTimeNew * 1000);
    }

    /**
     * 自动跳转场景
     * 根据场景设置时间进行跳转
     */
    private void startTimerAutoJumpSencen() {
        MyLog.d("jumpTime", "========startTimerAutoJumpSencen=======");
        int sencentTime = getCurrentSencenPlayTime();
        if (sencentTime < 1) {
            return;
        }
        MyLog.d("jumpTime", "========sencentTime=======" + sencentTime);
        cacelTimerTask();
        timerCheckTask = new Timer(true);
        jumpScenTimeTask = new JumpScenTimeTask();
        timerCheckTask.schedule(jumpScenTimeTask, sencentTime * 1000);
    }

    public void cacelTimerTask() {
        if (timerCheckTask != null) {
            timerCheckTask.cancel();
        }
        if (checkTask != null) {
            checkTask.cancel();
        }
        if (jumpScenTimeTask != null) {
            jumpScenTimeTask.cancel();
        }
        if (handler != null) {
            handler.removeMessages(JUMP_SCENCE_BY_SCENCE_TIME);
            handler.removeMessages(AUTO_JUJLE_TO_OTHER_SENCEN);
        }
    }

    //触摸返回
    private static final int AUTO_JUJLE_TO_OTHER_SENCEN = 5612;
    //根据场景设置得时间来跳转场景
    private static final int JUMP_SCENCE_BY_SCENCE_TIME = 5613;
    //用于无缝切换得延时操作
    private static final int CLEAR_LAST_VIEW_FROM_LIST = 5614;
    //用于双屏任务无缝切换
    public static final int CLEAR_DOUBLE_LAST_VIEW_FROM_LIST = 5615;
    //    //上传录制得报警视频
    public static final int UPDATE_VIDEO_FILR_TO_WEB = 5616;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler.removeMessages(msg.what);
            switch (msg.what) {
                case UPDATE_VIDEO_FILR_TO_WEB:
                    //上传报警视频
                    Intent intent = new Intent();
                    intent.setAction(AppInfo.ONE_KEY_POLICE_FILE_UPDATE);
                    context.sendBroadcast(intent);
                    break;
                case CLEAR_DOUBLE_LAST_VIEW_FROM_LIST: //用来清除副屏的缓存View用于无缝切换
                    //同步模式，这里需要优先删除数据
                    if (myPresentation != null) {
                        //同步模式
                        MyLog.diff("============parsener---准备移除View了");
                        myPresentation.clearLastDiffView();
                        return;
                    }
                    boolean isScreenSame = isLinkDoubleScreen();
                    if (isScreenSame) {
                        return;
                    }
                    if (myPresentation != null) {
                        MyLog.diff("============parsener---准备移除View了");
                        myPresentation.clearLastDiffView();
                    }
                    break;
                case CLEAR_LAST_VIEW_FROM_LIST: //清理上一次的View
                    clearLastView(TAG_CLEARVIEW_HANDLER);
                    break;
                case JUMP_SCENCE_BY_SCENCE_TIME:  //场景自带得时间
                    MyLog.d("jumpTime", "========JUMP_SCENCE_BY_SCENCE_TIME=======");  //场景根据播放时间得自动跳转
                    playNextSencenView(false);
                    break;
                case AUTO_JUJLE_TO_OTHER_SENCEN: //触摸返回场景/自动跳转场景
                    ProjectJumpEntity projectJumpEntity = getLastProJumpEntity();
                    if (projectJumpEntity == null) {
                        String SenceId = sceneEntityListMain.get(0).getSenceId();
                        MyLog.playTask("====TEXT getPmFromTask 3");
                        getPmFromTask(0, SenceId, true, "===触摸返回===projectJumpEntity==null=");
                        return;
                    }
                    SceneEntity sceneEntityFrom = projectJumpEntity.getSceneEntityFrom();
                    if (sceneEntityFrom == null) {
                        String SenceId = sceneEntityListMain.get(0).getSenceId();
                        MyLog.playTask("====TEXT getPmFromTask 4");
                        getPmFromTask(0, SenceId, true, "===触摸返回==sceneEntityFrom==null");
                        return;
                    }
                    String scenEntityId = sceneEntityFrom.getSenceId();
                    int lastPlayPosition = getProjectorScenId(scenEntityId);
                    if (projectJumpEntities != null && projectJumpEntities.size() > 0) {
                        projectJumpEntities.remove(projectJumpEntities.size() - 1);
                    }
                    MyLog.playTaskBack("===屏保=======准备跳转场景======" + lastPlayPosition);
                    String SenceId = sceneEntityListMain.get(lastPlayPosition).getSenceId();
                    MyLog.playTask("====TEXT getPmFromTask 5");
                    getPmFromTask(lastPlayPosition, SenceId, true, "===触摸返回==lastPlayPosition==" + lastPlayPosition);
                    break;
            }
        }
    };

    /**
     * 获取最后面得跳转
     *
     * @return
     */
    public ProjectJumpEntity getLastProJumpEntity() {
        if (projectJumpEntities == null || projectJumpEntities.size() < 1) {
            MyLog.playTaskBack("===屏保=======场景==null中断操作===");
            return null;
        }
        ProjectJumpEntity projectJumpEntity = projectJumpEntities.get(projectJumpEntities.size() - 1);
        return projectJumpEntity;
    }

    private int getProjectorScenId(String scenEntityId) {
        int backPosition = 0;
        if (sceneEntityListMain == null || sceneEntityListMain.size() < 1) {
            return backPosition;
        }
        for (int i = 0; i < sceneEntityListMain.size(); i++) {
            SceneEntity sceneEntityFrom = sceneEntityListMain.get(i);
            String secId = sceneEntityFrom.getSenceId();
            if (secId.contains(scenEntityId)) {
                backPosition = i;
            }
        }
        return backPosition;
    }

    /**
     * 获取当前正在播放的场景
     *
     * @return
     */
    public SceneEntity getCurrentSencenEntity() {
        if (sceneEntityListMain == null || sceneEntityListMain.size() < 1) {
            return null;
        }
        return sceneEntityListMain.get(currentSencenPosition);
    }

    /***
     * 获取任务
     * 获取节目，获取节目中的控件
     * 视频优先。音频次之，图片次之
     * @param playTag
     * 这是测试代码
     */
    private void changeProjectView(int playTag) {
        if (sceneEntityListMain == null || sceneEntityListMain.size() < 1) {
            MyLog.playTask("===当前只有一个节目，不跳转====");
            return;
        }
        SceneEntity sceneEntity = sceneEntityListMain.get(currentSencenPosition);
        if (sceneEntity == null) {
            return;
        }
        String pmType = sceneEntity.getPmType();
        MyLog.playTask("========检查节目得类型==" + pmType);
        if (pmType.contains(AppInfo.PROGRAM_TOUCH)) {
            MyLog.playTask("=====互动节目，不跳转====");
            return;
        }
        //当前有场景切换时间，这里中断操作
        int scenPlayTime = getCurrentSencenPlayTime();
        if (scenPlayTime > 4) {
            //有场景时间，这里中断操作
            MyLog.playTask("=====场景有设定播放时间，这里中断操作====");
            return;
        }
        MyLog.playTask("=====changeProjectView====" + playTag);
        taskModel.playNextProgram(sceneEntityListMain, currentSencenPosition, playTag, AppInfo.TASK_TYPE_DEFAULT, new TaskRequestListener() {
            @Override
            public void modifyTxtInfoStatues(boolean isSuccess, String desc) {

            }

            @Override
            public void playNextProgram(boolean isBack, List<SceneEntity> sceneEntities, int tag) {
                if (!isBack) {
                    return;
                }
                toPlayNextProject(sceneEntities);
            }

            @Override
            public void finishMySelf(String errorDesc) {

            }

            @Override
            public void parserJsonOver(String tag, List<TaskWorkEntity> list) {

            }
        });
    }

    /**
     * 添加场景到集合
     */
    public void addProJumpSencenToList(ProjectJumpEntity projectJumpEntity, String printTag) {
        SceneEntity sceneEntityFrom = projectJumpEntity.getSceneEntityFrom();
        SceneEntity sceneEntityTo = projectJumpEntity.getSceneEntityTo();
        long proTime = projectJumpEntity.getProjectorTime();
        MyLog.playTaskBack("=============添加场景到集合==proTime==" + proTime + "==printTag==" + printTag);
        if (projectJumpEntities == null) {
            return;
        }
        //第一次直接添加
        if (projectJumpEntities.size() < 1) {
            addProJumpToList(new ProjectJumpEntity(sceneEntityFrom, sceneEntityTo, proTime), "第一次添加，直接添加");
            return;
        }
        if (proTime > 0) { //正常设置得触摸返回时间
            addProJumpToList(new ProjectJumpEntity(sceneEntityFrom, sceneEntityTo, proTime), "正常触摸返回，直接添加");
        } else {
//            这个是依据场景得层级依次返回
//            int touch_back_time = SharedPerManager.getScene_task_touch_back_time();
//            addProJumpToList(new ProjectJumpEntity(sceneEntityFrom, sceneEntityTo, touch_back_time), "=添加场景到集合==用户没有设定触摸返回得时间，直接添加");
            //用户没有设定触摸返回得时间
            //直接返回第一个场景
            SceneEntity sceneEntityFirst = sceneEntityListMain.get(0);
            long touch_back_time = SharedPerManager.getScene_task_touch_back_time();
            addProJumpToList(new ProjectJumpEntity(sceneEntityFirst, sceneEntityTo, touch_back_time), "=添加场景到集合==用户没有设定触摸返回得时间，直接添加");
        }
    }

    private void addProJumpToList(ProjectJumpEntity projectJumpEntity, String printTag) {
        SceneEntity sceneEntityFrom = projectJumpEntity.getSceneEntityFrom();
        SceneEntity sceneEntityTo = projectJumpEntity.getSceneEntityTo();
        MyLog.playTaskBack("添加场景到集合场景ID====000===" + printTag);
        if (sceneEntityFrom != null && sceneEntityTo != null) {
            MyLog.playTaskBack("添加场景到集合场景ID==" + projectJumpEntity.getSceneEntityFrom().getSenceId() + " / " + projectJumpEntity.getSceneEntityTo().getSenceId() + " / " + projectJumpEntity.getProjectorTime() + " /" + printTag);
        } else {
            MyLog.playTaskBack("添加场景到集合场景ID==场景有一个是null==" + printTag);
        }
        projectJumpEntities.add(new ProjectJumpEntity(sceneEntityFrom, sceneEntityTo, projectJumpEntity.getProjectorTime()));
    }

    /**
     * 执行返回播放的操作
     */
    public void playBackSencenView() {
        if (sceneEntityListMain == null || sceneEntityListMain.size() < 1) {
            playTaskView.showToastView("当前节目为 null");
            return;
        }
        if (sceneEntityListMain.size() < 2) {
            playTaskView.showToastView("当前节目只有一个场景");
        }
        if (projectJumpEntities == null || projectJumpEntities.size() < 2) {
            MyLog.playTaskBack("=======场景==null中断操作===");
//            getPmFromTask(0, "第二个场景返回默认回到第一个场景");
            return;
        }
        ProjectJumpEntity projump = projectJumpEntities.get(projectJumpEntities.size() - 1);
        SceneEntity sceneEntityFrom = projump.getSceneEntityFrom();
        String scenId = sceneEntityFrom.getSenceId();
        MyLog.playTaskBack("=======点击返回，返回得ID===" + scenId);
        int lastSencenPosition = 0;
        for (int i = 0; i < sceneEntityListMain.size(); i++) {
            SceneEntity sceneEntitySave = sceneEntityListMain.get(i);
            String senIdSave = sceneEntitySave.getSenceId();
            if (senIdSave.contains(scenId)) {
                lastSencenPosition = i;
            }
        }
        if (projectJumpEntities != null || projectJumpEntities.size() > 1) {
            MyLog.playTask("=======点击返回，移除场景===" + projectJumpEntities.size());
            projectJumpEntities.remove(projectJumpEntities.size() - 1);
            MyLog.playTask("=======点击返回，移除场景===" + projectJumpEntities.size());
        }
        MyLog.playTaskBack("====播放结束了，切换节目===" + currentSencenPosition + " / " + lastSencenPosition);
        String SenceId = sceneEntityListMain.get(lastSencenPosition).getSenceId();
        MyLog.playTask("====TEXT getPmFromTask 6");
        getPmFromTask(lastSencenPosition, SenceId, true, "执行返回播放的操作");
    }

    /**
     * 去播放下一个节目
     *
     * @param sceneEntities
     */
    public void toPlayNextProject(List<SceneEntity> sceneEntities) {
        try {
            currentSencenPosition++;
            if (currentSencenPosition > (sceneEntities.size() - 1)) {
                currentSencenPosition = 0;
            }
            MyLog.playTask("====播放结束了，切换节目===" + currentSencenPosition + " / " + sceneEntities.size());
            MyLog.playTask("当前只有" + sceneEntities.size() + "个节目,执行下一步操作");
            String SenceId = sceneEntityListMain.get(currentSencenPosition).getSenceId();
            MyLog.playTask("====TEXT getPmFromTask 7");
            getPmFromTask(currentSencenPosition, SenceId, false, "==去播放下一个节目==");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    DifferentDislay myPresentation = null;


    //播放制定序号节目
    public void toPlayFormulateProject(int Formulateindex) {
        //能进入这个函数，就已经处理了能否播放的判断，这里直接播放就行了。
        Log.e("TAG", "toPlayFormulateProject: " + Formulateindex);
        if (currentSencenPosition == Formulateindex) {
            return;
        }
        currentSencenPosition = Formulateindex;
        if (currentSencenPosition >= sceneEntityListMain.size()) {
            currentSencenPosition = 0;
        }
        MyLog.gpio("====播放GPIO界面===" + currentSencenPosition + " /总长度 " + sceneEntityListMain.size());
        MyLog.gpio("当前只有" + sceneEntityListMain.size() + "个节目,执行下一步操作");
        String SenceId = sceneEntityListMain.get(currentSencenPosition).getSenceId();
        MyLog.playTask("====TEXT getPmFromTask 8");
        getPmFromTask(currentSencenPosition, SenceId, false, "==去播放下一个节目==");
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
                MyLog.diff("33333====haha==当前屏幕得个数= 0 或者 1");
                return;
            }
            MyLog.diff("33333====haha==当前屏幕得个数=" + screenEntityList.size());
            ScreenEntity screenEntity = screenEntityList.get(1);
            Display display = screenEntity.getDisplay();
            int width = screenEntity.getScreenWidth();
            int height = screenEntity.getScreenHeight();
            boolean isScreenSame = isLinkDoubleScreen();
            MyLog.diff("33333====haha===" + width + " / " + height + " /是否联动== " + isScreenSame + " / " + (myPresentation == null));
            if (isScreenSame) { //联动+
                if (myPresentation == null) {
                    myPresentation = new DifferentDislay(context, display, width, height, handler);
                    myPresentation.show();
                }
                myPresentation.setPlayList(sceneEntityListSecond, currentSencenPosition, taskModel);
                return;
            }
            //不联动效果
            if (myPresentation == null) {
                MyLog.diff("33333====haha===非联动效果,这里去加载界面");
                myPresentation = new DifferentDislay(context, display, width, height, handler);
                myPresentation.show();
                myPresentation.setPlayList(sceneEntityListSecond, currentSencenPosition, taskModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 播放上一个场景任务
     */
    public void playPreSencenView(boolean isShowToast) {
        if (sceneEntityListMain == null || sceneEntityListMain.size() < 1) {
            playTaskView.showToastView("当前节目为 null");
            return;
        }
        if (isShowToast && sceneEntityListMain.size() < 2) {
            playTaskView.showToastView("当前节目只有一个场景");
            return;
        }
        ProjectJumpEntity projectJumpEntity = new ProjectJumpEntity();
        projectJumpEntity.setSceneEntityFrom(getCurrentSencenEntity());
        currentSencenPosition--;
        if (currentSencenPosition < 0) {
            currentSencenPosition = sceneEntityListMain.size() - 1;
        }
        MyLog.playTask("====播放结束了，切换节目===" + currentSencenPosition + " / " + sceneEntityListMain.size());
        SceneEntity sceneEntityNext = sceneEntityListMain.get(currentSencenPosition);
        projectJumpEntity.setSceneEntityTo(sceneEntityNext);
        projectJumpEntity.setProjectorTime(0);
        addProJumpSencenToList(projectJumpEntity, "playPreSencenView");
        String SenceId = sceneEntityListMain.get(currentSencenPosition).getSenceId();
        MyLog.playTask("====TEXT getPmFromTask 9");
        getPmFromTask(currentSencenPosition, SenceId, false, "播放上一个场景==playPreSencenView");
    }

    /**
     * 播放下一个场景节目
     */
    public void playNextSencenView(boolean showToast) {
        Log.e("TAG", "playNextSencenView: " + sceneEntityListMain.size());
        if (sceneEntityListMain == null || sceneEntityListMain.size() < 1) {
            playTaskView.showToastView("当前节目为 null");
            return;
        }

        if (showToast && sceneEntityListMain.size() < 2) {
            playTaskView.showToastView("当前节目只有一个场景");
            return;
        }

        ProjectJumpEntity projectJumpEntity = new ProjectJumpEntity();
        projectJumpEntity.setSceneEntityFrom(getCurrentSencenEntity());

        currentSencenPosition++;
        if (currentSencenPosition > (sceneEntityListMain.size() - 1)) {
            currentSencenPosition = 0;
        }
        MyLog.playTask("====播放结束了，切换节目===" + currentSencenPosition + " / " + sceneEntityListMain.size());
        SceneEntity sceneEntityNext = sceneEntityListMain.get(currentSencenPosition);
        projectJumpEntity.setSceneEntityTo(sceneEntityNext);
        projectJumpEntity.setProjectorTime(0);
        addProJumpSencenToList(projectJumpEntity, "playNextSencenView");
        String SenceId = sceneEntityListMain.get(currentSencenPosition).getSenceId();
        MyLog.playTask("====TEXT getPmFromTask 10");
        getPmFromTask(currentSencenPosition, SenceId, false, "播放下一个场景节目");
    }

    /**
     * 快进
     * true 快进
     * false 快退
     *
     * @param b
     */
    public void moveViewForward(boolean b) {
        MyLog.d("haha", "====列表的数量==moveViewForward==" + b);
        if (myPresentation != null) {
            myPresentation.moveViewForward(b);
        }
        if (view_abous == null) {
            MyLog.d("haha", "====列表的数量==view_abous == null==");
            return;
        }
        if (genratorViewList == null || genratorViewList.size() < 1) {
            MyLog.d("haha", "====列表的数量==0==");
            return;
        }
        MyLog.d("haha", "====列表的数量=====" + genratorViewList.size());
        for (int i = 0; i < genratorViewList.size(); i++) {
            Generator genView = genratorViewList.get(i).getGenerator();
            genView.moveViewForward(b);
        }
    }

    /**
     * 暂停播放界面
     */
    public void pauseDisplayView() {
        if (myPresentation != null) {
            myPresentation.pauseDisplayView();
        }
        if (view_abous == null) {
            return;
        }
        if (genratorViewList == null || genratorViewList.size() < 1) {
            return;
        }
        for (int i = 0; i < genratorViewList.size(); i++) {
            Generator genView = genratorViewList.get(i).getGenerator();
            genView.pauseDisplayView();
        }
    }

    /**
     * 恢复播放
     */
    public void resumePlayView() {
        MyLog.playTask("=====恢复播放的功能==============");
        if (myPresentation != null) {
            myPresentation.resumePlayView();
        }
        if (view_abous == null) {
            return;
        }
        if (genratorViewList == null || genratorViewList.size() < 1) {
            return;
        }
        for (int i = 0; i < genratorViewList.size(); i++) {
            Generator genView = genratorViewList.get(i).getGenerator();
            genView.resumePlayView();
        }
    }

    /**
     * 获取当前是否是双屏联动效果
     *
     * @return
     */
    public boolean isLinkDoubleScreen() {
        SceneEntity sceneEntity = getCurrentSencenEntity();
        return TaskDealUtil.isLinkDoubleScreen(sceneEntity);
    }


    //用来缓存上一组没有被清掉的View,下一次加载完毕，在次清理上一组的View
    List<Generator> lastCache = new ArrayList<Generator>();

    /**
     * 1:表示正常的清理View
     * Handler 清理
     * -1 表示界面执行onStop onDestory
     * 需要清理副屏任务
     *
     * @param tag
     */
    public void clearLastView(int tag) {
        MyLog.playTask("=======clearLastView==" + tag);
        try {
            clearLastDoubleScreenView(tag);
            handler.removeMessages(CLEAR_LAST_VIEW_FROM_LIST);
            if (lastCache == null || lastCache.size() < 1) {
                return;
            }
            for (Generator generator : lastCache) {
                MyLog.playTask("=======添加view到集合==-==移除上一次剩下的View==" + generator.getClass().getName());
                view_abous.removeView(generator.getView());
                generator.removeCacheView("clearLastView-parsener");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 1:表示正常的清理View
     * Handler 清理
     * -1 表示界面执行onStop onDestory
     * 需要清理副屏任务
     *
     * @param tag
     */
    public static final int TAG_CLEARVIEW_ONDESTORY = -1;
    public static final int TAG_CLEARVIEW_HANDLER = 1;

    private void clearLastDoubleScreenView(int tag) {
        if (myPresentation == null) {
            return;
        }
        MyLog.playTask("======clearLastDoubleScreenView====" + tag);
        if (tag < 0) {
            myPresentation.clearMemory("===clearLastDoubleScreenView==");
            myPresentation.clearLastDiffView();
            myPresentation.dismiss();
            myPresentation = null;
            return;
        }
        myPresentation.clearLastDiffView();
    }

    /***
     * 清理View缓存
     */
    public void clearMemory() {
        lastCache.clear();
        try {
            //隐藏 Hdmi 悬浮窗效果
            playTaskView.dissHdmInViewToActivity();
            GlideCacheUtil.getInstance().clearImageAllCache(context);
            //去判断一下，如果是互动节目,就直接切换不做无缝，普通节目得话需要无缝切换操作
            String taskType = "1";
            SceneEntity currScenty = getCurrentSencenEntity();
            if (currScenty != null) {
                taskType = currScenty.getPmType();
            }
            MyLog.playTask("=====清理缓存一次==任务类型==" + taskType);
            if (genratorViewList != null || genratorViewList.size() > 0) {
                for (int i = 0; i < genratorViewList.size(); i++) {
                    Generator genView = genratorViewList.get(i).getGenerator();
                    genView.clearMemory();
                    if (taskType.contains(AppInfo.PROGRAM_TOUCH)) {  //互动节目，清理全部view
                        view_abous.removeView(genView.getView());
                        continue;
                    }
                    String coType = genratorViewList.get(i).getCoType();
                    if (TaskDealUtil.isTxtType(coType)) {
                        MyLog.playTask("=====清理缓存一次==移除字幕==");
                        view_abous.removeView(genView.getView());
                    }
                    MyLog.playTask("=======添加view到集合====添加数据到View==" + genView.getClass().getName());
                    lastCache.add(genView);
                }
            }
            cacelTimerTask();
            boolean iScreenSame = isLinkDoubleScreen();//双屏联动
            if (iScreenSame) {
                if (myPresentation != null && myPresentation.isShowing()) {
                    MyLog.playTask("=====清理缓存 副屏移除VIEW====");
                    myPresentation.clearMemory("======playParsener==clearMemory========");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新天气得UI
     */
    public void updateWeatherView() {
        int currentTime = SimpleDateUtil.getHourMin();
        if (currentTime > 0 && currentTime < 2) {
            MyLog.playTask("====时间到了，更新天气以及日期控件======" + currentTime, true);
            clearMemory();
            clearLastView(-1);
            MyLog.d("DDD", "getTaskToView 333");
            getTaskToView("凌晨切换节目");
            return;
        }
        if (genratorViewList == null || genratorViewList.size() < 1) {
            return;
        }
        for (CacheMemory cacheMemory : genratorViewList) {
            String coType = cacheMemory.getCoType();
            if (!coType.equals(AppInfo.VIEW_WEATHER)) {
                continue;
            }
            WeatherEntity weatherEntity = cacheMemory.getWeatherEntity();
            //刷新天气内容
            getWeatherFromWeb(cacheMemory.getGenerator(), weatherEntity);
        }
    }

    /**
     * 关闭副屏得背光
     */
    public void shutDownDiffScreenLight() {
        if (myPresentation != null) {
            myPresentation.clearMemory("===shutDownDiffScreenLight===");
        }
    }

    private Timer timerCheckTask;
    private CheckTask checkTask;
    private JumpScenTimeTask jumpScenTimeTask;

    /**
     * 更新媒体的声音
     * 修稿Logo信息
     */
    public void updateMediaVoiceNum() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (SharedPerManager.getWorkModel() == AppInfo.WORK_MODEL_SINGLE) {
                    logMediaVoice("单机模式拦截");
                    return;
                }
                int mediaNum = DbDevMedia.getCurrentMediaVoice();

                if (mediaNum < 0) {
                    logMediaVoice("mediaNum==<0");
                    return;
                }
                try {
                    int currentNum = VoiceManager.getInstance(context).getCurrentVoiceNum();

                    if (currentNum == mediaNum) {
                        logMediaVoice("currentNum== currentNum == mediaNum");
                        return;
                    }
                    logMediaVoice("currentNum==" + currentNum + " / " + mediaNum);
                    if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
                        VoiceManager.getInstance(context).setMediaVoiceNum(mediaNum);
                        return;
                    }
                    mediaNum = mediaNum * 15 / 100;

                    if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_SENHAN) {
                        //客户的固件，无法判断息屏亮屏，固件问题
                        logMediaVoice("setting media voice ==" + mediaNum);
                        VoiceManager.getInstance(context).setMediaVoiceNum(mediaNum);
                        return;
                    }
                    boolean isBackOn = SystemManagerInstance.getInstance(context).getBackLightTtatues("任务播放Parsener检测休眠zhuangtai");
                    logMediaVoice("isBackOn==" + isBackOn);
                    //休眠状态，不修改音量
                    if (isBackOn) {
                        logMediaVoice("setting media voice ==" + mediaNum);
                        VoiceManager.getInstance(context).setMediaVoiceNum(mediaNum);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        EtvService.getInstance().executor(runnable);
    }

    private void logMediaVoice(String desc) {
        MyLog.cdl("=======logMediaVoice===" + desc);
    }

    /**
     * 局部刷新View
     */
    public void updateGenWebViewRefresh(String action) {
        MyLog.playTask("====网络变化，准备检测并刷新网页=updateGenViewRefresh=" + action);
        refreshWebViewInfo(action);
        //刷新副屏
        if (myPresentation != null) {
            myPresentation.refreshWebViewInfo(action);
        }
    }

    /***
     * 刷新网页
     */
    private void refreshWebViewInfo(String action) {
        if (genratorViewList == null || genratorViewList.size() < 1) {
            return;
        }
        for (int i = 0; i < genratorViewList.size(); i++) {
            String coType = genratorViewList.get(i).getCoType();
            Generator generator = genratorViewList.get(i).getGenerator();
            if (coType.equals(AppInfo.VIEW_WEB_PAGE)) {
                MyLog.playTask("====网络变化，检测到网页，准备刷新==");
                generator.updateTextInfo(action);
            }
        }
    }

    /***
     * 静音
     * @param isMute
     * true 静音
     * false 恢复音量
     */
    public void setMediaVoiceMute(boolean isMute) {
        if (isMute) {
            //静音
            int currentNum = VoiceManager.getInstance(context).getCurrentVoiceNum();
            if (currentNum > 0) {
                SharedPerManager.setLastSaveVoiceNum(currentNum);
            }
            VoiceManager.getInstance(context).setMediaVoiceNum(0);
            return;
        }
        int lastSaveNum = SharedPerManager.getLastSaveVoiceNum();
        VoiceManager.getInstance(context).setMediaVoiceNum(lastSaveNum);
    }

    /***
     * 定时检查插播任务
     */
    public void checkTextInsertTaskInfoByTime() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SceneEntity sceneEntity = getCurrentSencenEntity();
                if (sceneEntity == null) {
                    checkTxtInsertTask(false, "定时检查插播任务", true);
                    return;
                }
                checkTxtInsertTask(true, "定时检查插播任务", true);
            }
        }, 1500);
    }

    private class CheckTask extends TimerTask {
        @Override
        public void run() {
            handler.sendEmptyMessage(AUTO_JUJLE_TO_OTHER_SENCEN);
        }
    }

    /**
     * 场景跳转
     */
    private class JumpScenTimeTask extends TimerTask {
        @Override
        public void run() {
            handler.sendEmptyMessage(JUMP_SCENCE_BY_SCENCE_TIME);
        }

    }

    /**
     * 清理多余得素材
     */
    private void checkTaskDownFile() {
        long currentTime = SimpleDateUtil.getCurrentTimelONG();
        if (currentTime < AppConfig.TIME_CHECK_POWER_REDUCE) {
            MyLog.cdl("系统时间不对，不删除文件", true);
            return;
        }
        List<MpListEntity> listEntities = DBTaskUtil.getTaskListInfoAll();
        if (listEntities == null || listEntities.size() < 1) {
            //素材列表没有数据,
            return;
        }
        String path = AppInfo.BASE_TASK_URL();
        File file = new File(path);
        if (!file.exists()) {
            FileUtil.MKDIRSfILE(path);
            return;
        }
        GetFileFromPathForRunnable runnable = new GetFileFromPathForRunnable(path, new GetFileFromPathForRunnable.QueryFileFromPathListener() {
            @Override
            public void backFileList(boolean isSuccess, List<File> listFileSearch, String errorDesc) {
                if (!isSuccess) {
                    return;
                }
                if (listFileSearch == null || listFileSearch.size() < 1) {
                    return;
                }
                compairDbFileAndlocal(listEntities, listFileSearch);
            }
        });
        EtvService.getInstance().executor(runnable);
    }

    private void compairDbFileAndlocal(List<MpListEntity> listEntities, List<File> listFileSearch) {
        MyLog.playTask("compairDbFileAndlocal==" + listEntities.size() + " / " + listFileSearch.size());
        List<File> listFilelocal = new ArrayList<>();
        listFilelocal.addAll(listFileSearch);
        for (MpListEntity mpListEntity : listEntities) {
            String webFileUrl = mpListEntity.getUrl();
            String webFileName = webFileUrl.substring(webFileUrl.lastIndexOf("/") + 1);
            for (File fileLocal : listFileSearch) {
                String fileLocalName = fileLocal.getName();
                if (webFileName.startsWith(fileLocalName)) {
                    listFilelocal.remove(fileLocal);
                    MyLog.playTask("compairDbFileAndlocal 名字相同==" + webFileName + " / " + fileLocal);
                }
            }
        }
        if (listFilelocal == null || listFilelocal.size() < 1) {
            MyLog.playTask("compairDbFileAndlocal 没有多余的素材删除==");
            return;
        }
        MyLog.playTask("compairDbFileAndlocal 有多余的素材删除==" + listFilelocal.size());
        for (int i = 0; i < listFilelocal.size(); i++) {
            String filePath = listFilelocal.get(i).getPath();
            MyLog.playTask("compairDbFileAndlocal 有多余的素材删除000==" + filePath);
            FileUtil.deleteDirOrFilePath(filePath, "比对数据库,删除多余得素材");
        }
    }
}
