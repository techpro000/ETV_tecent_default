package com.diff.presentation;

import static com.youth.banner.util.LogUtils.TAG;

import android.app.Activity;
import android.app.Presentation;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

import com.diff.entity.DiffShowEntity;
import com.diff.util.DiffSizeUtil;
import com.etv.config.AppInfo;
import com.etv.listener.TaskPlayStateListener;
import com.etv.task.entity.CacheMemory;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.MpListEntity;
import com.etv.task.entity.PositionEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.model.TaskMudel;
import com.etv.task.model.TaskRequestListener;
import com.etv.task.parsener.PlayTaskParsener;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.util.weather.WeatherEntity;
import com.etv.util.weather.WeatherHttpRequest;
import com.etv.view.layout.Generator;
import com.etv.view.layout.ViewButtonGenerate;
import com.etv.view.layout.date.ViewDateGenerate;
import com.etv.view.layout.date.ViewTimeOnlyGenerate;
import com.etv.view.layout.date.ViewTimeReduceGenerate;
import com.etv.view.layout.date.ViewWeekGenerate;
import com.etv.view.layout.mixedswing.ViewImgVideoNetGenerate;
import com.etv.view.layout.music.ViewAudioGenertrator;
import com.etv.view.layout.text.ViewMatQueentestGenerte;
import com.etv.view.layout.text.ViewTexUpGenerate;
import com.etv.view.layout.text.ViewTextSlientGenerate;
import com.etv.view.layout.video.media.ViewVideoGenertrator;
import com.ys.bannerlib.util.GlideCacheUtil;
import com.ys.bannerlib.util.GlideImageUtil;
import com.ys.etv.R;
import com.ys.model.dialog.MyToastView;
import com.ys.model.util.FileMatch;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/***
 * 双屏异显
 */
public class DifferentDislay extends Presentation {

    Activity context;
    List<SceneEntity> sceneEntityListSecond;
    int currentPosition = 0;

    int screenWidth;          //副屏得宽度
    int screenHeight;          //副屏得高度
    float widthChSize = 1;    //屏幕压缩比例
    float heightChSize = 1;   //屏幕压缩比例
    TaskMudel taskModel;
    private Handler handler = null;

    public DifferentDislay(Activity outerContext, Display display, int width, int height, Handler handler) {
        super(outerContext, display);
        MyLog.diff("===双屏初始化==DifferentDislay");
        this.context = outerContext;
        this.screenHeight = height;
        this.screenWidth = width;
        this.handler = handler;
        DiffShowEntity diffShowEntity = DiffSizeUtil.getDiffScreenSizeShow(screenWidth, screenHeight);
        widthChSize = diffShowEntity.getWidthChSize();
        heightChSize = diffShowEntity.getHeightChSize();
        MyLog.diff("===副屏的比例尺寸==" + width + " / " + height + "/haha==" + widthChSize + " / " + heightChSize);
    }

    public void setPlayList(List<SceneEntity> sceneEntityList, int playPosition, TaskMudel taskModel) {
        MyLog.diff("===双屏初始化==setPlayList====" + playPosition);
        this.sceneEntityListSecond = sceneEntityList;
        this.currentPosition = playPosition;
        this.taskModel = taskModel;
        //解决节目场景个数不平等的问题
        if (currentPosition > (sceneEntityList.size() - 1)) {
            currentPosition = 0;
        }
        if (isInitDiffDisplay) {
            //已经初始化了，就直接去播放
            showViewToDev(currentPosition, "===双屏不联动，初始化播放====");
        }
    }

    boolean isInitDiffDisplay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_layout);
        MyLog.diff("===双屏初始化==onCreate");
        isInitDiffDisplay = true;
        initView();
        showViewToDev(currentPosition, "==============界面初始化===onCreate====");
    }

    ImageView iv_back_bgg;
    private AbsoluteLayout view_abous;

    private void initView() {
        view_abous = (AbsoluteLayout) findViewById(R.id.view_diff);
        iv_back_bgg = (ImageView) findViewById(R.id.iv_back_bgg);
    }

    //用来封装播放view的
    List<CacheMemory> genratorViewList = new ArrayList<CacheMemory>();
    /***
     * 从任务中获取节目
     * @param position
     */
    List<CpListEntity> cpCacheList = new ArrayList<>();

    private void showViewToDev(int position, String tag) {
        MyLog.diff("====当前播放的节目的位置==" + position + " /tag =  " + tag);
        try {
            clearMemory("===刷新界面，重新加载数据前，清理数据===");
            handler.sendEmptyMessageDelayed(PlayTaskParsener.CLEAR_DOUBLE_LAST_VIEW_FROM_LIST, 2000);
            //这里会涉及到重复调用，所以会先清理一次内存
            if (genratorViewList != null) {
                genratorViewList.clear();   //清理控件集合
            }
            if (sceneEntityListSecond == null) {
                MyLog.diff("获取节目场景失败");
                return;
            }
            if (sceneEntityListSecond == null || sceneEntityListSecond.size() < 1) {
                MyLog.diff("获取节目场景失败");
                return;
            }
            MyLog.diff("====当前节目由多少哥场景==" + sceneEntityListSecond.size());
            SceneEntity sceneEntity = sceneEntityListSecond.get(position);
            if (sceneEntity == null) {
                MyLog.diff("获取场景信息异常");
                return;
            }
            List<CpListEntity> cpList = sceneEntity.getListCp();
            if (cpList == null || cpList.size() < 1) {
                return;
            }
            MyLog.playTask("=====当前播放的场景ID===" + sceneEntity.getSenceId());
            cpCacheList.clear();
            cpCacheList = TaskDealUtil.mathCpListOrder(cpList);
            MyLog.diff("====添加到集合的顺序00排序后的个数==" + cpCacheList.size());
            addBackImageInfo(sceneEntity); //添加图片
            for (int i = 0; i < cpCacheList.size(); i++) {
                CpListEntity cpListEntity = cpCacheList.get(i);
                parperToShowView(cpListEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addBackImageInfo(SceneEntity sceneEntity) {
        MyLog.diff("==============加载背景图====");
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
            MyLog.diff("====背景图不存在，加载颜色加载背景图操作");
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

    private void parperToShowView(CpListEntity cpEntity) {
        if (cpEntity == null) {
            return;
        }
        try {
            List<MpListEntity> mpList = new ArrayList<MpListEntity>();     //控件的素材信息
            List<TextInfo> txtList = new ArrayList<TextInfo>();  //控件的文本属性
            String coType = cpEntity.getCoType();             //控件类型
            PositionEntity positionEntity = TaskDealUtil.getCpListPosition(cpEntity);
            System.out.println("aaaaaaaaaaa---36s---> " + positionEntity);
            int leftPosition = positionEntity.getLeftPosition();
            int topPosition = positionEntity.getTopPosition();
            int width = positionEntity.getWidth();
            int height = positionEntity.getHeight();
            System.out.println("aaaaaaaaaaaaaa ====diff布局的坐标点--00-> " + width);
            leftPosition = (int) (leftPosition * widthChSize);
            topPosition = (int) (topPosition * heightChSize);
            width = (int) (width * widthChSize);
            height = (int) (height * heightChSize);
            float viewWidth = leftPosition + width;
            float viewHeight = topPosition + height;
            if (leftPosition < 8 && leftPosition > 0) {
                leftPosition = 0;
            }
            if (topPosition < 8 && topPosition > 0) {
                topPosition = 0;
            }
            //控件的宽度 - 屏幕的宽度
            int disTanceWidth = Math.abs((int) (viewWidth - (screenWidth * widthChSize)));
            System.out.println("aaaaaaaaaaaaaa ====diff布局的坐标点---> " + width);
            System.out.println("aaaaaaaaaaaaaa ====diff布局的坐标点---> " + disTanceWidth + " " + viewWidth + " " + screenWidth + " " + widthChSize);
            if (disTanceWidth < 8) {
                width = width + disTanceWidth;
            }
            int disTanceHeight = Math.abs((int) (viewHeight - (screenHeight * heightChSize)));
            if (disTanceHeight < 8) {
                height = height + disTanceHeight;
            }
            MyLog.diff("====diff布局的坐标点0000==>>" + coType + " / " + cpEntity.getScreenSize());
            MyLog.diff("====diff布局的坐标点1111==>>" + coType + " / " + leftPosition + " / " + topPosition + " / " + width + " / " + height);
            if (TaskDealUtil.isResourceType(coType)) {   //资源类型
                mpList = cpEntity.getMpList();
            } else if (TaskDealUtil.isTxtType(coType)) {  //文本类型
                txtList = cpEntity.getTxList();
            }
            final Generator generatorView;
            switch (coType) {
                case AppInfo.VIEW_IMAGE:  //图片格式
                    List<MediAddEntity> imageList = TaskDealUtil.getResourceListPath(mpList);
                    if (imageList == null || imageList.size() < 1) {
                        MyLog.diff("====准备展示图片==NULL==");
                        return;
                    }
                    generatorView = TaskDealUtil.getImageGenertorViewParsener(context, cpEntity, leftPosition, topPosition, width, height, imageList, false);
                    Log.e(TAG, "parperToShowView: " + width + "///" + height);
                    addViewToList(generatorView, coType);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(null, true);
                    break;
                case AppInfo.VIEW_VIDEO: //视频格式
                    List<MediAddEntity> videoList = TaskDealUtil.getResourceListPath(mpList);
                    if (videoList == null || videoList.size() < 1) {
                        return;
                    }
                    MyLog.diff("====视频的坐标的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height + " / videoList=" + videoList.size());
                    generatorView = new ViewVideoGenertrator(context, cpEntity, leftPosition, topPosition, width, height, videoList, AppInfo.PROGRAM_POSITION_SECOND, false);
                    addViewToList(generatorView, coType);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(null, true);
                    break;
                case AppInfo.VIEW_IMAGE_VIDEO: //混播模式
                    List<MediAddEntity> mixtureList = TaskDealUtil.getResourceListPath(mpList);
                    if (mixtureList == null || mixtureList.size() < 1) {
                        return;
                    }
                    MyLog.diff("混播===列表数量===" + mixtureList.size());
                    generatorView = new ViewImgVideoNetGenerate(context, cpEntity, null, leftPosition, topPosition, width, height, mixtureList, true, 0, AppInfo.PROGRAM_POSITION_SECOND, false);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    addViewToList(generatorView, coType);
                    break;
                case AppInfo.VIEW_AUDIO:                         //音频
                    List<MediAddEntity> audioList = TaskDealUtil.getResourceListPath(mpList);
                    if (audioList == null || audioList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewAudioGenertrator(context, 0, 0, 1, 1, audioList);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    addViewToList(generatorView, coType);
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
                    if (!txtContentWeb.startsWith("http")) {
                        txtContentWeb = "http://" + txtContentWeb;
                    }
                    String moveWeb = textInfo.getTaMove();
                    generatorView = TaskDealUtil.getWebViewBySpeedString(context, moveWeb, leftPosition, topPosition, width, height, txtContentWeb);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    addViewToList(generatorView, coType);
                    break;
                case AppInfo.VIEW_DOC:    //文档
                    List<MediAddEntity> docList = TaskDealUtil.getResourceListPath(mpList);
                    if (docList == null || docList.size() < 1) {
                        return;
                    }
                    MediAddEntity mediAddEntity = docList.get(0);
                    String fileUrl = mediAddEntity.getUrl();
                    int fileType = FileMatch.fileMatch(fileUrl);
                    MyLog.diff("====文档地址===" + fileUrl);
                    generatorView = TaskDealUtil.getPdfShowView(context, fileType, cpEntity, leftPosition, topPosition, width, height, mediAddEntity, docList);
                    if (generatorView != null) {
                        view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                        generatorView.updateView(mediAddEntity, true);
                        addViewToList(generatorView, coType);
                    }
                    break;
                case AppInfo.VIEW_STREAM_VIDEO:            //流媒体
                    MyLog.diff("====加载流媒体控件==VIEW_STREAM_VIDEO==" + leftPosition + " / " + topPosition + " / " + width + " / " + height);
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    TextInfo textInfoStream = txtList.get(0);
                    String streamUrl = textInfoStream.getTaContent();
                    String moveStream = textInfoStream.getTaMove();
                    generatorView = TaskDealUtil.getStreamGenViewBySpeed(context, moveStream, cpEntity, leftPosition, topPosition, width, height, streamUrl, coType);
                    addViewToList(generatorView, coType);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(null, true);
                    break;
                case AppInfo.VIEW_WEATHER:   //天气。
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    final TextInfo textInfoWeather = txtList.get(0);
                    String city = textInfoWeather.getTaAddress();
                    generatorView = TaskDealUtil.getWeatherGenWeatherView(context, leftPosition, topPosition, width, height, textInfoWeather);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    addViewToList(generatorView, coType);
                    WeatherHttpRequest.getWeather(city, new WeatherHttpRequest.WeatherStateListener() {
                        @Override
                        public void getWeatherState(WeatherEntity entity) {
                            MyLog.playTask("控件获取的天气信息==" + entity.toString() + " / " + (textInfoWeather == null));
                            if (textInfoWeather != null) {
                                String textColor = textInfoWeather.getTaColor();
                                String bggColor = textInfoWeather.getTaBgColor();
                                entity.setTaColor(textColor);
                                entity.setBggColor(bggColor);
                            }
                            generatorView.updateView(entity, true);
                        }

                        @Override
                        public void getFailed(String desc) {
                            MyLog.playTask("获取的天气信息error==" + desc);
                        }
                    });
                    break;
                case AppInfo.VIEW_DATE:   //日期
                    MyLog.diff("====时间的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height);
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewDateGenerate(context, leftPosition, topPosition, width, height);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    addViewToList(generatorView, coType);
                    break;
                case AppInfo.VIEW_WEEK:   //日期
                    MyLog.diff("====时间的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height);
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewWeekGenerate(context, leftPosition, topPosition, width, height);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    addViewToList(generatorView, coType);
                    break;
                case AppInfo.VIEW_TIME:   //时间
                    MyLog.diff("====时间的坐标==" + leftPosition + "/ " + topPosition + " /" + width + " / " + height);
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewTimeOnlyGenerate(context, leftPosition, topPosition, width, height, false);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    addViewToList(generatorView, coType);
                    break;
                case AppInfo.VIEW_SUBTITLE:   //字幕
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < txtList.size(); i++) {
                        String txtContent = txtList.get(i).getTaContent();
                        builder.append(txtContent + "");
                    }
                    String taMoveSpeed = txtList.get(0).getTaMoveSpeed();

                    String taMove = txtList.get(0).getTaMove();
                    if (taMove == null || taMove.length() < 1) {
                        taMove = TextInfo.MOVE_LEFT + "";
                    }
                    MyLog.diff("===字幕数值===" + txtList.get(0).toString());
                    MyLog.diff("====字幕的坐标===" + leftPosition + "/" + topPosition + "/" + width + "/" + height + " /taMoveSpeed=" + taMoveSpeed);
                    if (taMoveSpeed.contains("0")) {
                        //静止状态
                        generatorView = new ViewTextSlientGenerate(context, cpEntity, leftPosition, topPosition, width, height, builder.toString());
                    } else { //运动状态
                        if (taMove.contains(TextInfo.MOVE_UP + "")) {      //从下到上
                            generatorView = new ViewTexUpGenerate(context, cpEntity, leftPosition, topPosition, width, height, builder.toString());
//                            generatorView = new ViewTextDownToUpGenerate(context, cpEntity, leftPosition, topPosition, width, height, builder.toString());
                        } else {  //左右位 移动
                            generatorView = new ViewMatQueentestGenerte(context, cpEntity, leftPosition, topPosition, width, height, builder.toString());
                        }
                    }
                    addViewToList(generatorView, coType);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    break;
                case AppInfo.VIEW_BUTTON:   // 按钮
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    StringBuilder builderButton = new StringBuilder();
                    for (int i = 0; i < txtList.size(); i++) {
                        String txtContent = txtList.get(i).getTaContent();
                        builderButton.append(txtContent + "");
                    }
                    MyLog.diff("button的坐标===" + leftPosition + "/" + topPosition + "/" + width + "/" + height);
                    MyLog.diff("button显示===" + builderButton.toString());
                    generatorView = new ViewButtonGenerate(context, cpEntity, leftPosition, topPosition, width, height, builderButton.toString());
                    addViewToList(generatorView, coType);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    break;
                case AppInfo.VIEW_COUNT_DOWN:
                    if (txtList == null || txtList.size() < 1) {
                        return;
                    }
                    generatorView = new ViewTimeReduceGenerate(context, cpEntity, leftPosition, topPosition, width, height);
                    addViewToList(generatorView, coType);
                    view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
                    generatorView.updateView(txtList.get(0), true);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //增加到管理View中，统一清理缓存
    public void addViewToList(Generator generatorView, String cpType) {
        try {
            if (genratorViewList == null) {
                return;
            }
            genratorViewList.add(new CacheMemory(generatorView, cpType, false));
            generatorView.setPlayStateChangeListener(new TaskPlayStateListener() {
                @Override
                public void playComplete(int playTag) {
                    boolean isScreenSame = isLinkDoubleScreen();
                    if (isScreenSame) { //如果不联动，就不往下执行
                        return;
                    }
//                    String playTagShow = TaskPlayStateListener.getPlayTag(playTag);
                    if (generatorView.getClass() == ViewImgVideoNetGenerate.class) {
                        //混播得View 得和 混播得标记融合
                        if (playTag == TaskPlayStateListener.TAG_PLAY_VIDEO_IMAGE) {
                            changeProjectView(playTag);
                        }
                        return;
                    }
                    changeProjectView(playTag);
                }

                @Override
                public void playCompletePosition(String etLevel, String taskId, int currentPlayPosition, int playTag) {

                }

                @Override
                public void clickTaskView(CpListEntity cpListEntity, List<String> list, int clickPosition) {
                    MyLog.diff("=====点击了view 副屏===" + cpListEntity.toString());
                }

                @Override
                public void longClickView(CpListEntity cpListEntity, Object object) {

                }

                @Override
                public void reStartPlayProgram(String errorDesc) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isLinkDoubleScreen() {
        if (sceneEntityListSecond == null || sceneEntityListSecond.size() < 1) {
            return false;
        }
        SceneEntity sceneEntity = sceneEntityListSecond.get(currentPosition);
        return TaskDealUtil.isLinkDoubleScreen(sceneEntity);
    }

    /***
     * 获取任务
     * 获取节目，获取节目中的控件
     * 视频优先。音频次之，图片次之
     * @param playTag
     * 这是测试代码
     */
    private void changeProjectView(int playTag) {
        //判断是否是互动节目
        if (sceneEntityListSecond == null || sceneEntityListSecond.size() < 1) {
            MyLog.diff("===当前只有一个节目，不跳转====");
            return;
        }
        SceneEntity sceneEntity = sceneEntityListSecond.get(currentPosition);
        if (sceneEntity == null) {
            return;
        }
        String pmType = sceneEntity.getPmType();
        MyLog.diff("========检查节目得类型==" + pmType);
        if (pmType.contains(AppInfo.PROGRAM_TOUCH)) {
            MyLog.diff("=====互动节目，不跳转====");
            return;
        }

        MyLog.diff("=====changeProjectView====" + playTag);
        if (taskModel == null) {
            return;
        }
        taskModel.playNextProgram(sceneEntityListSecond, currentPosition, playTag, new TaskRequestListener() {
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
            public void parserJsonOver(String tag) {

            }
        });
    }

    /**
     * 去播放下一个节目
     *
     * @param sceneEntities
     */
    private void toPlayNextProject(List<SceneEntity> sceneEntities) {
        try {
            currentPosition++;
            if (currentPosition > (sceneEntities.size() - 1)) {
                currentPosition = 0;
            }
            MyLog.diff("====播放结束了，切换节目===" + currentPosition + " / " + sceneEntities.size());
            MyLog.diff("当前只有" + sceneEntities.size() + "个节目,执行下一步操作");
            showViewToDev(currentPosition, "=====去播放放下一个节目===toPlayNextProject====");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 快进
     * true 快进
     * false 快退
     *
     * @param b
     */
    public void moveViewForward(boolean b) {
        if (view_abous == null) {
            return;
        }
        if (genratorViewList == null || genratorViewList.size() < 1) {
            return;
        }
        MyLog.d("haha", "====列表的数量=====" + genratorViewList.size());
        for (int i = 0; i < genratorViewList.size(); i++) {
            Generator genView = genratorViewList.get(i).getGenerator();
            genView.moveViewForward(b);
        }
    }

    //用来缓存上一组没有被清掉的View,下一次加载完毕，在次清理上一组的View
    List<Generator> lastCache = new ArrayList<Generator>();

    /***
     * 刷新网页
     */
    public void refreshWebViewInfo(String action) {
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
     * 清理View缓存
     */
    public void clearMemory(String tag) {
        try {
            lastCache.clear();
            MyLog.diff("======clearLastDiffView===-==界面关闭调用清理内存==" + tag);
            if (genratorViewList == null || genratorViewList.size() < 1) {
                MyLog.diff("=======次屏清理VIEW缓存");
                return;
            }
            GlideCacheUtil.getInstance().clearImageAllCache(context);
            for (int i = 0; i < genratorViewList.size(); i++) {
                Generator genView = genratorViewList.get(i).getGenerator();
                String coType = genratorViewList.get(i).getCoType();
                genView.pauseDisplayView();
                if (TaskDealUtil.isTxtType(coType)) {
                    // 1:这次直接把文字类的全部清理掉
                    genView.clearMemory();
                    view_abous.removeView(genView.getView());
                } else {
                    lastCache.add(genView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清理最后保存得View
     */
    public void clearLastDiffView() {
        try {
            if (lastCache == null || lastCache.size() < 1) {
                MyLog.diff("======clearLastDiffView===-==lastCache==null中断==");
                return;
            }
            Iterator<Generator> its = lastCache.iterator();
            while (its.hasNext()) {
                Generator generator = its.next();
                its.remove();
                generator.clearMemory();
                MyLog.diff("======clearLastDiffView===-==移除上一次剩下的View==" + generator);
                view_abous.removeView(generator.getView());
            }
           /* for (Generator generator : lastCache) {
                //后边添加得，这里得去掉
                generator.clearMemory();
                MyLog.diff("======clearLastDiffView===-==移除上一次剩下的View==" + generator);
                view_abous.removeView(generator.getView());
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showToastView(String toast) {
        MyToastView.getInstance().Toast(context, toast);
    }

    /**
     * 暂停界面
     */
    public void pauseDisplayView() {
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

    public void resumePlayView() {
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

    /***
     * 同步模式,副屏 去同步主屏节奏
     * @param next_play_scentnty_position
     */
    public void playPositionScenProgram(int next_play_scentnty_position) {
        MyLog.diff("===同步任务副屏=playPositionScenProgram===" + next_play_scentnty_position);
        currentPosition = next_play_scentnty_position;
        showViewToDev(currentPosition, "同步模式，这里去同步数据");
    }

}
