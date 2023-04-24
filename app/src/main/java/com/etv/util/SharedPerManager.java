package com.etv.util;

import static com.etv.config.AppConfig.APP_TYPE;
import static com.etv.config.AppConfig.APP_TYPE_HUANGZUNNIANHUA;
import static com.etv.config.AppConfig.APP_TYPE_RW_DEFAULT_ADDRESS;
import static com.etv.config.AppConfig.APP_TYPE_ZB_DEFAULT_ADDRESS;

import android.text.TextUtils;
import android.util.Log;

import com.EtvApplication;
import com.etv.config.ApiInfo;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.task.entity.ViewPosition;
import com.etv.util.system.CpuModel;
import com.etv.util.system.SystemManagerUtil;
import com.etv.util.system.VoiceManager;
import com.ys.bannerlib.BannerConfig;

import java.util.Calendar;
import java.util.Locale;

public class SharedPerManager {

    boolean aaa;

    /***
     * 保存定时开关，开机用来检查是否是开关机时间
     * @return
     */
    public static long getPowerOnTime() {

        long backTime = (long) EtvApplication.getInstance().getData("powerOnTime", 0L);
        return backTime;
    }


    public static void setPowerOnTime(long powerOnTime, String printTag) {
        MyLog.powerOnOff("Shared-OnTime==" + printTag + " / " + powerOnTime, true);
        EtvApplication.getInstance().saveData("powerOnTime", powerOnTime);
    }

    public static long getPowerOffTime() {
        long backTime = (long) EtvApplication.getInstance().getData("powerOffTime", 0L);
        return backTime;
    }

    public static void setPowerOffTime(long powerOffTime, String printTag) {
        MyLog.powerOnOff("Shared-OffTime==" + printTag + " / " + powerOffTime, true);
        EtvApplication.getInstance().saveData("powerOffTime", powerOffTime);
    }

    /***
     * 屏幕设置的时间
     * @return
     */
    public static int getScreenTouchTime() {
        int backTime = (int) EtvApplication.getInstance().getData("ScreenTouchTime", 10);
        return backTime;
    }

    public static void setScreenTouchTime(int ScreenTouchTime) {
        EtvApplication.getInstance().saveData("ScreenTouchTime", ScreenTouchTime);
    }

    /***
     * 是否显示HDMI_IN设置按钮
     * @return
     */
    public static boolean getShowHdmiButton() {
        boolean showHdmiButton = (boolean) EtvApplication.getInstance().getData("showHdmiButton", true);
        return showHdmiButton;
    }

    public static void setShowHdmiButton(boolean showHdmiButton) {
        EtvApplication.getInstance().saveData("showHdmiButton", showHdmiButton);
    }

    /**
     * 上一次保存得音量
     *
     * @return
     */
    public static int getLastSaveVoiceNum() {
        int lastSaveVoiceNum = (int) EtvApplication.getInstance().getData("lastSaveVoiceNum", 7);
        return lastSaveVoiceNum;
    }

    public static void setLastSaveVoiceNum(int lastSaveVoiceNum) {
        EtvApplication.getInstance().saveData("lastSaveVoiceNum", lastSaveVoiceNum);
    }

    /***
     * 是否加载 WPS
     * @return
     */
    public static boolean getWpsShowEnable() {
        boolean defaulrOpen = true;
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_RK_3128)) {
            defaulrOpen = false;
        }
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            defaulrOpen = false;
        }
        boolean bluetooth = (boolean) EtvApplication.getInstance().getData("WpsShowEnable", defaulrOpen);
        return bluetooth;
    }

    public static void setWpsShowEnable(boolean WpsShowEnable) {
        EtvApplication.getInstance().saveData("WpsShowEnable", WpsShowEnable);
    }

    /***
     * 设置连接方式
     * @return
     */
    public static int getSocketType() {
        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_JIANGJUN_YUNCHENG
            || AppConfig.APP_TYPE == AppConfig.APP_TYPE_SCHOOL_STUDY
            || AppConfig.APP_TYPE == AppConfig.APP_TYPE_DEFAULT_ADDRESS_USERNAME) {
            return AppConfig.SOCKEY_TYPE_WEBSOCKET;
        }
        int defaultSockeType = AppConfig.SOCKEY_TYPE_SOCKET;
        switch (AppConfig.APP_TYPE) {
            case AppConfig.APP_TYPE_CHUNYN:
            case AppConfig.APP_TYPE_LK_QRCODE_SHOW_DHL:
                defaultSockeType = AppConfig.SOCKEY_TYPE_WEBSOCKET;
                break;
            case AppConfig.APP_TYPE_DEFAULT_ADDRESS_USERNAME:
                defaultSockeType = AppConfig.SOCKEY_TYPE_WEBSOCKET;
                break;
            case AppConfig.APP_TYPE_JIANGJUN_YUNCHENG:
                defaultSockeType = AppConfig.SOCKEY_TYPE_WEBSOCKET;
                break;
            case AppConfig.APP_TYPE_TY_DEFAULT_ADDRESS:
                defaultSockeType = AppConfig.SOCKEY_TYPE_WEBSOCKET;
                break;
            case APP_TYPE_RW_DEFAULT_ADDRESS:
                defaultSockeType = AppConfig.SOCKEY_TYPE_WEBSOCKET;
                break;
            case AppConfig.APP_TYPE_HUANGZUNNIANHUA:
                defaultSockeType = AppConfig.SOCKEY_TYPE_WEBSOCKET;
                break;
//            case AppConfig.APP_TYPE_ETV_ESONCLOUD_IP:
//                defaultSockeType = AppConfig.SOCKEY_TYPE_SOCKET;
//                break;
        }
        int socketType = (int) EtvApplication.getInstance().getData("socketType", defaultSockeType);
        return socketType;
    }

    /****
     * 0  webSocket
     * 1  socket
     * @param socketType
     */
    public static void setSocketType(int socketType) {
        SharedPerUtil.CURRENT_SOCKET_TYPE = socketType;
        EtvApplication.getInstance().saveData("socketType", socketType);
    }

    /***
     * 是否启动蓝牙
     * @return
     */
    public static boolean getBluetooth() {
        boolean defaulrOpen = false;
        switch (AppConfig.APP_TYPE) {
            case AppConfig.APP_TYPE_QINGFENG_DEFAULT:
            case AppConfig.APP_TYPE_QINGFENG_NOT_QR:
                defaulrOpen = true;
                break;
        }
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            defaulrOpen = false;
        }

        boolean bluetooth = (boolean) EtvApplication.getInstance().getData("bluetooth", defaulrOpen);
        return bluetooth;
    }

    public static void setBluetooth(boolean bluetooth) {
        EtvApplication.getInstance().saveData("bluetooth", bluetooth);
    }

    /***
     * 凌晨重启计划
     * @return
     */
    public static boolean getAutoRebootDev() {
        boolean autoRebootDev = (boolean) EtvApplication.getInstance().getData("autoRebootDev", true);
        return autoRebootDev;
    }

    public static void setAutoRebootDev(boolean autoRebootDev) {
        EtvApplication.getInstance().saveData("autoRebootDev", autoRebootDev);
    }

    /***
     * HDMI_IN显示比例
     * 0 自适应
     * 1 全景
     * @param hdmiShowStyle
     */
    public static void setHdmiShowStyle(int hdmiShowStyle) {
        EtvApplication.getInstance().saveData("hdmiShowStyle", hdmiShowStyle);
    }

    public static int getHdmiShowStyle() {
        int ttsMessageDelay = (int) EtvApplication.getInstance().getData("hdmiShowStyle", 1);
        return ttsMessageDelay;
    }

    /***
     * 获取TTS 语音设置
     * @return
     */
    public static int getTtsMessageDelay() {
        int ttsMessageDelay = (int) EtvApplication.getInstance().getData("ttsMessageDelay", 0);
        return ttsMessageDelay;
    }

    /***
     * 语音TTS时间间隔
     * @param
     */
    public static void setTtsMessageDelay(int ttsMessageDelay) {
        EtvApplication.getInstance().saveData("ttsMessageDelay", ttsMessageDelay);
    }

    /***
     * 是否打开TTS语音
     * @return
     */
    public static boolean getOpenTTSManager() {
        boolean openTTSManager = (boolean) EtvApplication.getInstance().getData("openTTSManager", false);
        return openTTSManager;
    }

    public static void setOpenTTSManager(boolean openTTSManager) {
        EtvApplication.getInstance().saveData("openTTSManager", openTTSManager);
    }

    /***
     * HDMI 的插口位置
     * MLOGIC   M11 共用这个接口
     * @return
     */
    public static String getMlogicHdmiPosition() {
        String defaultPositon = AppInfo.HDMIIN1();
        switch (AppConfig.APP_TYPE) {
            case AppConfig.APP_TYPE_WISIJIE:
                defaultPositon = AppInfo.HDMIIN2();
                break;
        }
        String mlogicHdmiPosition = (String) EtvApplication.getInstance().getData("mlogicHdmiPosition", defaultPositon);
        return mlogicHdmiPosition;
    }

    public static void setMlogicHdmiPosition(String mlogicHdmiPosition) {
        EtvApplication.getInstance().saveData("mlogicHdmiPosition", mlogicHdmiPosition);
    }

    /***
     * 4K支持
     * @return
     */
    public static boolean getVideoMoreSize() {
        boolean isDefaultEnable = false;
        if (CpuModel.isMLogic()) {
            isDefaultEnable = true;
        }
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            isDefaultEnable = true;
        }
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_PX30)) {
            isDefaultEnable = true;
        }
        boolean VideoMoreSize = (boolean) EtvApplication.getInstance().getData("VideoMoreSize", isDefaultEnable);
        return VideoMoreSize;
    }

    public static void setVideoMoreSize(boolean VideoMoreSize) {
        EtvApplication.getInstance().saveData("VideoMoreSize", VideoMoreSize);
    }

    /***
     * 数据来源
     * true 网络
     * false 本地设置
     * @return
     */
    public static boolean getInfoFrom() {
        boolean isDefault = true;

        boolean infoFrom = (boolean) EtvApplication.getInstance().getData("infoFrom", isDefault);
        return infoFrom;
    }

    public static void setInfoFrom(boolean infoFrom) {
        EtvApplication.getInstance().saveData("infoFrom", infoFrom);
    }

    /**
     * 是否加载触摸互动任务
     *
     * @return
     */
    public static boolean getTaskTouchEnable() {
        boolean isDefaultEnable = true;
        boolean taskTouchEnable = (boolean) EtvApplication.getInstance().getData("taskTouchEnable", isDefaultEnable);
        return taskTouchEnable;
    }

    public static void setTaskTouchEnable(boolean taskTouchEnable) {
        EtvApplication.getInstance().saveData("taskTouchEnable", taskTouchEnable);
    }

    /****
     * 一键报警得功能
     * @return
     */
    public static boolean getGpioAction() {
        boolean defaultAction = false;
        switch (AppConfig.APP_TYPE) {
            case AppConfig.APP_TYPE_XINABICN:
                defaultAction = true;
                break;
        }
        boolean SleepStatues = (boolean) EtvApplication.getInstance().getData("openPoliceAction", defaultAction);
        return SleepStatues;
    }

    public static void setGpioAction(boolean openPoliceAction) {
        EtvApplication.getInstance().saveData("openPoliceAction", openPoliceAction);
    }

    /***
     * 获取休眠状态
     * true  当前是休眠状态
     * false 当前是屏幕显示状态
     * @return
     */
    public static boolean getSleepStatues() {
        boolean SleepStatues = (boolean) EtvApplication.getInstance().getData("SleepStatues", false);
        MyLog.sleep("=====休眠状态=获取==" + SleepStatues, true);
        return SleepStatues;
    }

    //设备休眠状态
    public static void setSleepStatues(boolean SleepStatues) {
        MyLog.sleep("=====休眠状态==设置=" + SleepStatues, true);
        EtvApplication.getInstance().saveData("SleepStatues", SleepStatues);
    }

    //    网页显示button按钮
    public static boolean getWebShowButton() {
        if (AppConfig.APP_TYPE == APP_TYPE_RW_DEFAULT_ADDRESS) {
            boolean isShow = (boolean) EtvApplication.getInstance().getData("webButtonShow", false);
            MyLog.cdl("========是否显示网页互动按钮===" + isShow);
            return isShow;
        } else {
            boolean isShow = (boolean) EtvApplication.getInstance().getData("webButtonShow", true);
            MyLog.cdl("========是否显示网页互动按钮===" + isShow);
            return isShow;
        }


    }

    //网页显示button按钮
    public static void setWebShowButton(boolean webButtonShow) {
        EtvApplication.getInstance().saveData("webButtonShow", webButtonShow);
    }

    //网页显示刷新倒计时
    public static boolean getWebShowReduce() {
        boolean WebShowReduce = (boolean) EtvApplication.getInstance().getData("WebShowReduce", false);
        MyLog.cdl("========是否显示网页互动按钮===" + WebShowReduce);
        return WebShowReduce;
    }

    //网页显示倒计时
    public static void setWebShowReduce(boolean WebShowReduce) {
        EtvApplication.getInstance().saveData("WebShowReduce", WebShowReduce);
    }


    //网页是否缓存
    public static boolean getWebCache() {
        return (boolean) EtvApplication.getInstance().getData("web_cache", false);
    }

    //设置网页缓存
    public static void setWebCache(boolean cache) {
        EtvApplication.getInstance().saveData("web_cache", cache);
    }

    /***
     * 获取截图质量
     * 0  差
     * 1  中
     * 2 高
     * @return
     */
    public static int getCapturequilty() {
        int currentVoiceNum = ((int) EtvApplication.getInstance().getData("capturequilty", 0));
        return currentVoiceNum;
    }

    /***
     * 设置截图质量
     * @param capturequilty
     */
    public static void setCapturequilty(int capturequilty) {
        EtvApplication.getInstance().saveData("capturequilty", capturequilty);
    }

    /***
     * 获取屏幕的亮度
     * @return
     */
    public static String getScreenLightNum() {
        String cpuModel = ((String) EtvApplication.getInstance().getData("screenLight", "86"));
        MyLog.cdl("=======屏幕亮度===getScreenLightNum==" + cpuModel);
        return cpuModel;
    }

    /***
     * 设置屏幕的亮度
     * @param screenLight
     */
    public static void setScreenLightNum(String screenLight) {
        MyLog.cdl("=======屏幕亮度===setScreenLightNum==" + screenLight);
        EtvApplication.getInstance().saveData("screenLight", screenLight);
    }

    /**
     * 播放统计功能
     *
     * @return
     */
    private static int PlayTotalUpdateCache = -1;

    public static boolean getPlayTotalUpdate() {
        if (PlayTotalUpdateCache != -1) {
            return PlayTotalUpdateCache == 1;
        }
        PlayTotalUpdateCache = ((int) EtvApplication.getInstance().getData("PlayTotalUpdateNew", 0));
        return PlayTotalUpdateCache == 1;
    }

    /***
     * 播放统计  0关闭 1开启
     * @param PlayTotalUpdate
     */
    public static void setPlayTotalUpdate(int PlayTotalUpdate) {
        PlayTotalUpdateCache = PlayTotalUpdate;
        EtvApplication.getInstance().saveData("PlayTotalUpdateNew", PlayTotalUpdate);
    }

    /***
     * 是否从服务器下拉背景
     * @return
     */
    public static boolean getBggImageFromWeb() {
        boolean defaultStatues = true;
        boolean ifHdmiInSuport = ((boolean) EtvApplication.getInstance().getData("BggImageFromWeb", defaultStatues));
        return ifHdmiInSuport;
    }

    /**
     * 设置壁纸背景开关
     *
     * @param BggImageFromWeb
     */
    public static void setBggImageFromWeb(boolean BggImageFromWeb) {
        EtvApplication.getInstance().saveData("BggImageFromWeb", BggImageFromWeb);
    }

    /***
     * 是否支持HDMI_IN得功能
     * @return
     */
    public static boolean getIfHdmiInSuport() {
        boolean isDefault = false;
        return ((boolean) EtvApplication.getInstance().getData("ifHdmiInSuport", isDefault));
    }

    public static void setIfHdmiInSuport(boolean ifHdmiInSuport) {
        EtvApplication.getInstance().saveData("ifHdmiInSuport", ifHdmiInSuport);
    }

    /***
     * 副屏图片旋转上传角度
     */
    public static int getDoubleScreenRoateImage() {
        int doubleScreenRoateImage = ((int) EtvApplication.getInstance().getData("doubleScreenRoateImage", 0));
        return doubleScreenRoateImage;
    }

    /***
     * 副屏图片旋转上传角度
     * @param doubleScreenRoateImage
     */
    public static void setDoubleScreenRoateImage(int doubleScreenRoateImage) {
        EtvApplication.getInstance().saveData("doubleScreenRoateImage", doubleScreenRoateImage);
    }

    /***
     * 获取双屏显示算法适配
     *     public static final int DOUBLE_SCREEN_SHOW_DEFAULT = 0; //原比例显示
     *     public static final int DOUBLE_SCREEN_SHOW_ADAPTER = 1; //强制拉伸
     *     public static final int DOUBLE_SCREEN_SHOW_GT_TRANS = 2; //高通翻转
     * @return
     */
    public static int getDoubleScreenMath() {
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_PX30)) {
            return AppInfo.DOUBLE_SCREEN_SHOW_PX30;
        }
        int defaultMath = AppInfo.DOUBLE_SCREEN_SHOW_DEFAULT;
        int doubleScreenMath = ((int) EtvApplication.getInstance().getData("doubleScreenMath", defaultMath));
        return doubleScreenMath;
    }

    /****
     * 设置双屏异步显示得算法
     * 0：原尺寸显示
     * 1：屏幕强制适配
     * 2：高通反转
     * 3：长宽互置
     * @param doubleScreenMath
     */
    public static void setDoubleScreenMath(int doubleScreenMath) {
        Log.e("cdl", "========setDoubleScreenMath====" + doubleScreenMath);
        EtvApplication.getInstance().saveData("doubleScreenMath", doubleScreenMath);
    }

    /**
     * 获取退出密码
     *
     * @return
     */
    public static String getExitpassword() {
        String exitpassword = ((String) EtvApplication.getInstance().getData("exitpassword", ""));
        if (TextUtils.isEmpty(exitpassword) || exitpassword.contains("null")) {
            exitpassword = "";
        }
        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_JIAIWEI) {
            if (exitpassword == null || exitpassword.length() < 2) {
                exitpassword = "778899";
            }
        }

        return exitpassword;
    }

    public static void setExitpassword(String exitpassword) {
        exitpassword = exitpassword.trim();
        if (exitpassword == null || exitpassword.length() < 1 || exitpassword.contains("null")) {
            exitpassword = "";
        }
        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_JIAIWEI) {
            if (exitpassword == null || exitpassword.length() < 2) {
                exitpassword = "778899";
            }
        }
        EtvApplication.getInstance().saveData("exitpassword", exitpassword);
    }

    /**
     * 用来获取上次提交设备信息得
     * 防止高频率提交设备信息
     * MainActivity
     *
     * @return
     */
    public static long getLastUpdateDevTime() {
        long lastUpdateDevTime = (long) EtvApplication.getInstance().getData("lastUpdateDevTime", 0L);
        return lastUpdateDevTime;
    }

    public static void setLastUpdateDevTime(long lastUpdateDevTime) {
        EtvApplication.getInstance().saveData("lastUpdateDevTime", lastUpdateDevTime);
    }

    /**
     * 是否是正常退出
     * true  手动退出
     * false 异常退出
     *
     * @return
     */
    public static boolean getExitDefault() {
        boolean isBack = (boolean) EtvApplication.getInstance().getData("exitDefault", false);
        return isBack;
    }

    /**
     * 设置是否正常退出
     * 主要是播放保护
     * 如果是手动退出得话就不启动
     * 异常退出，就自己启动
     *
     * @param exitDefault
     */
    public static void setExitDefault(boolean exitDefault) {
        MyLog.cdl("=====是否是正常退出机制===" + exitDefault);
        EtvApplication.getInstance().saveData("exitDefault", exitDefault);
    }

    /**
     * 获取开机启动开关
     * 守护进程
     *
     * @return
     */
    public static boolean getOpenPower() {
        boolean openPower = ((boolean) EtvApplication.getInstance().getData("openPower", true));
        MyLog.cdl("openPower=get=" + openPower);
        return openPower;
    }

    /**
     * 设置开机启动开关
     * 守护进程
     *
     * @param openPower
     */
    public static void setOpenPower(boolean openPower, String printTag) {
        MyLog.cdl("openPower=set=" + openPower + " / " + printTag);
        EtvApplication.getInstance().saveData("openPower", openPower);
    }

    //获取限制速度
    public static int getLimitSpeed() {
        if (limitSpeedCache != -1) {
            return limitSpeedCache;
        }
        limitSpeedCache = ((int) EtvApplication.getInstance().getData("limitspeed", 2000));
        String ipAddress = getWebHost();
        if (ipAddress.contains("119.23.220.53") || ipAddress.contains(ApiInfo.IP_DEFAULT_URL_WEBSOCKET)) {
            limitSpeedCache = 1500;
        }
        if (ipAddress.contains("139.159.152.78") || ipAddress.contains(ApiInfo.IP_DEFAULT_URL_SOCKET)) {
            limitSpeedCache = 1500;
        }
        return limitSpeedCache;
    }

    private static int limitSpeedCache = -1;

    //设置下载速度
    public static void setLimitSpeed(int limitspeed) {
        limitSpeedCache = limitspeed;
        EtvApplication.getInstance().saveData("limitspeed", limitspeed);
    }


    private static int limitCacheNUm = -1;

    //获取限制下载的台数
    public static int getLimitDevNum() {
        if (limitCacheNUm != -1) {
            return limitCacheNUm;
        }
        limitCacheNUm = ((int) EtvApplication.getInstance().getData("limitdevnum", 100));
        return limitCacheNUm;
    }

    //设置限制下载的限制台数
    public static void setLimitDevNum(int limitdevnum) {
        limitCacheNUm = limitdevnum;
        EtvApplication.getInstance().saveData("limitdevnum", limitdevnum);
    }

    /**
     * 获取设备昵称
     *
     * @return
     */
    public static String getDevNickName() {
        String codeUtil = CodeUtil.getUniquePsuedoID();
        String devNickName = ((String) EtvApplication.getInstance().getData("devNickName", codeUtil));
        return devNickName;
    }

    /**
     * 设置设备昵称
     *
     * @param devNickName
     */
    public static void setDevNickName(String devNickName) {
        EtvApplication.getInstance().saveData("devNickName", devNickName);
    }

    public static int getSingleSecondLayoutTag() {
        int singlelayouttag = 0;
        //获取副屏得方向
        boolean isHroVer = SystemManagerUtil.getSecondScreenHrorVer();
        if (isHroVer) { //横屏
            singlelayouttag = ((int) EtvApplication.getInstance().getData("singleSecondlayouttag", ViewPosition.VIEW_LAYOUT_HRO_VIEW));
        } else {//竖屏
            singlelayouttag = ((int) EtvApplication.getInstance().getData("singleSecondlayouttag", ViewPosition.VIEW_LAYOUT_VER_VIEW));
        }
        return singlelayouttag;
    }

    //设置单机模式副屏的布局
    public static void setSingleSecondLayoutTag(int singleSecondlayouttag) {
        EtvApplication.getInstance().saveData("singleSecondlayouttag", singleSecondlayouttag);
    }

    //获取单机模式得布局
    public static int getSingleLayoutTag() {
        int singlelayouttag = 0;
        int width = getScreenWidth();
        int height = getScreenHeight();
        if (width - height > 0) {
            //横屏
            singlelayouttag = ((int) EtvApplication.getInstance().getData("singlelayouttag", ViewPosition.VIEW_LAYOUT_HRO_VIEW));
        } else {//竖屏
            singlelayouttag = ((int) EtvApplication.getInstance().getData("singlelayouttag", ViewPosition.VIEW_LAYOUT_VER_VIEW));
        }
        return singlelayouttag;
    }

    //设置单机模式主屏的布局
    public static void setSingleLayoutTag(int singlelayouttag) {
        EtvApplication.getInstance().saveData("singlelayouttag", singlelayouttag);
    }

    public static String getUserName() {
        String defaultName = "Null";
        switch (AppConfig.APP_TYPE) {
            case AppConfig.APP_TYPE_DEFAULT_ADDRESS_USERNAME:
                defaultName = "tailg";
                break;
            case AppConfig.APP_TYPE_JIANGJUN_YUNCHENG:
                defaultName = "admin";
                break;
            case APP_TYPE_RW_DEFAULT_ADDRESS:
                defaultName = "admin";
                break;
            case APP_TYPE_ZB_DEFAULT_ADDRESS:
                defaultName = "admin";
                break;
            case APP_TYPE_HUANGZUNNIANHUA:
                defaultName = "18863400888";
                break;
        }

        String userNameBack = ((String) EtvApplication.getInstance().getData("devusername", defaultName));
        return userNameBack;
    }

    public static void setUserName(String devusername, String tag) {
        EtvApplication.getInstance().saveData("devusername", devusername);
    }

    /***
     * 保存基本保存路径
     * @param
     */
    public static void setBaseSdPath(String BaseSdPath) {
        MyLog.cdl("保存素材存储目录==" + BaseSdPath, true);
        AppInfo.BASE_PATH = BaseSdPath;
        EtvApplication.getInstance().saveData("BaseSdPath", BaseSdPath);
    }

    public static String getBaseSdPath() {
        String savePath = ((String) EtvApplication.getInstance().getData("BaseSdPath", AppInfo.BASE_PATH_INNER));
        return savePath;
    }

    /***
     * 获取内存阈值的级别
     * 1：低级，删除ETV下面的所有文件
     * 2：中级，删除ETV下面所有的文件，删除所有的大型文件
     * 3：高级，删除SD卡下所有的文件
     * @return
     */
    public static int getSdcardManagerAuthor() {
        int SdcardManagerAuthor = ((int) EtvApplication.getInstance().getData("SdcardManagerAuthor", 1));
        return SdcardManagerAuthor;
    }

    /***
     * 设置内存阈值的级别
     * @param SdcardManagerAuthor
     */
    public static void setSdcardManagerAuthor(int SdcardManagerAuthor) {
        EtvApplication.getInstance().saveData("SdcardManagerAuthor", SdcardManagerAuthor);
    }

    /**
     * 获取端口号
     *
     * @return
     */
    public static String getWebPort() {
        String webPortDefaulr = "8899";
        String webPort = ((String) EtvApplication.getInstance().getData("webPort", webPortDefaulr));
        return webPort;
    }

    /**
     * 保存端口号
     *
     * @param webPort
     */
    public static void setWebPort(String webPort) {
        SharedPerUtil.WEBHOST_PORT = webPort;
        EtvApplication.getInstance().saveData("webPort", webPort);
    }

    /***
     * 获取保存的IP地址
     * @return
     */
    public static String getWebHost() {
//        String ipAddressDefault = ApiInfo.IP_DEFAULT_URL_WEBSOCKET;
        String ipAddressDefault = ApiInfo.IP_DEFAULT_URL_SOCKET;
        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
            ipAddressDefault = ApiInfo.IP_DEFAULT_URL_WEBSOCKET;
        }


        switch (AppConfig.APP_TYPE) {
            case AppConfig.APP_TYPE_CHUNYN:
                ipAddressDefault = "8.209.119.199";
                break;
            case AppConfig.APP_TYPE_THREE_TAI:
                ipAddressDefault = "222.85.141.109";
                break;
            case AppConfig.APP_TYPE_LK_QRCODE_SHOW_DHL:
                ipAddressDefault = "114.132.42.167";
                break;
            case AppConfig.APP_TYPE_DEFAULT_ADDRESS_USERNAME:
                ipAddressDefault = "139.9.126.178";
                break;
            case AppConfig.APP_TYPE_TY_DEFAULT_ADDRESS:
                ipAddressDefault = "118.31.56.116";
                break;
            case AppConfig.APP_TYPE_JIANGJUN_YUNCHENG:
                ipAddressDefault = "111.53.65.254";
                break;
            case APP_TYPE_RW_DEFAULT_ADDRESS:
                ipAddressDefault = "10.80.45.10";
                break;
            case APP_TYPE_ZB_DEFAULT_ADDRESS:
                ipAddressDefault = "www.zhongbaizhihui.com";
                break;
            case APP_TYPE_HUANGZUNNIANHUA:
                ipAddressDefault = "www.won-giant.com";
                break;
        }
        String ipAddress = ((String) EtvApplication.getInstance().getData("webHost", ipAddressDefault));
        return ipAddress;
    }

    /***
     * 保存服务器IP地址
     * @param webHost
     */
    public static void setWebHost(String webHost) {
        SharedPerUtil.WEBHOST_IP_ADDRESS = webHost;
        MyLog.cdl("=========设置的服务器IP地址===" + webHost);
        EtvApplication.getInstance().saveData("webHost", webHost);
    }

    public static void setNetworkMode(String networkmode) {
        EtvApplication.getInstance().saveData("networkmode", networkmode);
    }

    public static String getNetworkMode() {
        String locale = Locale.getDefault().getLanguage();
        if (locale.contains("en")) {  //英文版本
            return ((String) EtvApplication.getInstance().getData("networkmode", "Dynamic acquisition"));
        } else {
            return ((String) EtvApplication.getInstance().getData("networkmode", "动态获取"));
        }
    }

    /***
     * 获取工作模式
     * @return
     * 0 :网络模式
     * 1 :网络下发
     * 2 :单机模式
     */

    public static int WORKMODELBACK = -1;

    public static int getWorkModel() {
        if (WORKMODELBACK != -1) {
            return WORKMODELBACK;
        }
        int workmldeo = ((int) EtvApplication.getInstance().getData("workModel", AppInfo.WORK_MODEL_NET));
        WORKMODELBACK = workmldeo;
        return workmldeo;
    }

    public static void setWorkModel(int workModel, String printTag) {
        if (workModel == AppInfo.WORK_MODEL_NET) {
            MyLog.cdl("切换工作模式==网络下发 /" + printTag, true);
        } else if (workModel == AppInfo.WORK_MODEL_NET_DOWN) {
            MyLog.cdl("切换工作模式==网络导出 /" + printTag, true);
        } else if (workModel == AppInfo.WORK_MODEL_SINGLE) {
            MyLog.cdl("切换工作模式==单机模式 /" + printTag, true);
        }
        WORKMODELBACK = workModel;
        EtvApplication.getInstance().saveData("workModel", workModel);
    }

    public static int getScreenHeight() {
        return ((Integer) EtvApplication.getInstance().getData("screenHeight", -1));
    }

    public static void setScreenHeight(int screenHeight) {
        SharedPerUtil.SCREEN_HEIGHT = screenHeight;
        EtvApplication.getInstance().saveData("screenHeight", screenHeight);
    }

    public static int getScreenWidth() {
        return ((Integer) EtvApplication.getInstance().getData("screenWidth", -1));
    }

    public static void setScreenWidth(int screenWidth) {
        SharedPerUtil.SCREEN_WIDTH = screenWidth;
        EtvApplication.getInstance().saveData("screenWidth", screenWidth);
    }

    public static String getmLatitude() {
        String mLatitude = ((String) EtvApplication.getInstance().getData("mLatitude", "116.413384"));
        MyLog.location("===获取得坐标==mLatitude==" + mLatitude);
        return mLatitude;
    }

    public static void setmLatitude(String mLatitude) {
        EtvApplication.getInstance().saveData("mLatitude", mLatitude);
    }

    public static String getmLongitude() {
        String mLongitude = ((String) EtvApplication.getInstance().getData("mLongitude", "39.910925"));
        MyLog.location("===获取得坐标==mLongitude==" + mLongitude);
        return mLongitude;
    }

    public static void setmLongitude(String mLongitude) {
        EtvApplication.getInstance().saveData("mLongitude", mLongitude);
    }

    public static void setLocalCity(String city) {
        EtvApplication.getInstance().saveData("city", city);
    }

    public static String getLocalCiti() {
        return ((String) EtvApplication.getInstance().getData("city", ""));
    }

    public static String getDevOnwer() {
        return ((String) EtvApplication.getInstance().getData("devOnwer", "官方设备,未注册授权"));
    }

    public static void setDevOnwer(String devOnwer) {
        EtvApplication.getInstance().saveData("devOnwer", devOnwer);
    }

    /***
     * 获取所有的地址
     * @return
     */
    public static String getAllAddress() {
        String address = getProvince() + getLocalCiti() + getArea() + getDetailAddress();
        if (address == null || address.length() < 2) {
            address = "No Address";
        }
        return address;
    }


    public static String getProvince() {
        return ((String) EtvApplication.getInstance().getData("province", ""));
    }

    public static void setProvince(String province) {
        EtvApplication.getInstance().saveData("province", province);
    }

    public static String getArea() {
        return ((String) EtvApplication.getInstance().getData("area", ""));
    }

    public static void setArea(String area) {
        EtvApplication.getInstance().saveData("area", area);
    }

    public static void setDetailAddress(String detailaddress) {
        EtvApplication.getInstance().saveData("detailaddress", detailaddress);
    }

    public static String getDetailAddress() {
        return ((String) EtvApplication.getInstance().getData("detailaddress", " "));
    }

    /***
     * 默认自动定位
     * true  自动定位
     * false 手动定位
     * @return
     */
    public static boolean getAutoLocation() {
        return ((boolean) EtvApplication.getInstance().getData("aotolocation", true));
    }

    public static void setAutoLocation(boolean aotolocation) {
        EtvApplication.getInstance().saveData("aotolocation", aotolocation);
    }

    /***
     * 获取设备的唯一值
     * @return
     */
    public static String getUniquePsuedoID() {
        String code = (String) EtvApplication.getInstance().getData("onlycode", "");
        return code;
    }

    /***
     * 保存设备的唯一值
     * @param onlycode
     */
    public static void setUniquePsuedoID(String onlycode) {
        EtvApplication.getInstance().saveData("onlycode", onlycode);
    }

    /**
     * 图片的间隔时间
     *
     * @return
     */
    public static int getPicDistanceTime() {
        int backTime = (int) EtvApplication.getInstance().getData("picDistanceTime", 10);
        return backTime;
    }

    public static void setPicDistanceTime(int picDistanceTime) {
        EtvApplication.getInstance().saveData("picDistanceTime", picDistanceTime);
    }

    /**
     * 获取WPS文件得切换时间
     *
     * @return
     */
    public static int getWpsDistanceTime() {
        return ((int) EtvApplication.getInstance().getData("wpsDistanceTime", 10));
    }

    public static void setWpsDistanceTime(int wpsDistanceTime) {
        EtvApplication.getInstance().saveData("wpsDistanceTime", wpsDistanceTime);
    }

    /**
     * 获取单机模式图片切换动画
     *
     * @return
     */
    public static int getSinglePicAnimiType() {
        return ((int) EtvApplication.getInstance().getData("singlePicAnimiType", 0));
    }

    public static void setSinglePicAnimiType(int singlePicAnimiType) {
        EtvApplication.getInstance().saveData("singlePicAnimiType", singlePicAnimiType);
    }

    /**
     * 获取单机版本的音量
     *
     * @return
     */
    public static int getSingleVideoVoiceNum() {
        return ((int) EtvApplication.getInstance().getData("videoVoiceNum", 70));
    }

    public static void setSingleVideoVoiceNum(int videoVoiceNum) {
        EtvApplication.getInstance().saveData("videoVoiceNum", videoVoiceNum);
    }

    /**
     * 获取背景音乐的音量
     *
     * @return
     */
    public static int getSingleBackVoiceNum() {
        return ((int) EtvApplication.getInstance().getData("singleBackVoiceNum", 70));
    }

    /**
     * 设置背景音乐的音量
     *
     * @return
     */
    public static void setSingleBackVoiceNum(int singleBackVoiceNum) {
        EtvApplication.getInstance().saveData("singleBackVoiceNum", singleBackVoiceNum);
    }

    /**
     * 是否显示网络导入任务
     *
     * @return
     */
    public static boolean getShowNetDownTask() {
        return ((boolean) EtvApplication.getInstance().getData("showNetDownTask", false));
    }

    /**
     * @param showNetDownTask
     */
    public static void setShowNetDownTask(boolean showNetDownTask) {
        EtvApplication.getInstance().saveData("showNetDownTask", showNetDownTask);
    }

    public static String getPackageNameBySp() {
        if (!TextUtils.isEmpty(packageNameCache)) {
            return packageNameCache;
        }
        packageNameCache = ((String) EtvApplication.getInstance().getData("packageName", "com.ys.etv"));
        return packageNameCache;
    }

    public static String getPackageNameByYuncheng() {
        return ((String) EtvApplication.getInstance().getData("packageName", "com.founder.huanghechenbao"));
    }


    private static String packageNameCache = "";

    public static void setPackageName(String packageName, String tag) {
        packageNameCache = packageName;
        EtvApplication.getInstance().saveData("packageName", packageName);
    }

    //获取上一次统计的流量
    public static long getLastDownTraff() {
        return ((long) EtvApplication.getInstance().getData("lastDownTraff", 0L));
    }

    //记录上一次统计的流量
    public static void setLastDownTraff(long lastDownTraff) {
        EtvApplication.getInstance().saveData("lastDownTraff", lastDownTraff);
    }

    public static long getLastUploadTraff() {
        return ((long) EtvApplication.getInstance().getData("lastUploadTraff", 0L));
    }

    //记录上一次统计的流量
    public static void setLastUploadTraff(long lastUploadTraff) {
        EtvApplication.getInstance().saveData("lastUploadTraff", lastUploadTraff);
    }

    private static int updateTraffEnable = -1;

    /***
     * 是否上传流量给服务器
     * 统计流量  0关闭 1开启
     * @return
     */
    public static boolean getIfUpdateTraffToWeb() {
        if (updateTraffEnable != -1) {
            return updateTraffEnable == 1;
        }
        updateTraffEnable = ((int) EtvApplication.getInstance().getData("UpdateTraffToWeb", 0));
        return updateTraffEnable == 1;
    }

    /***
     * 设置是否上传流量到服务器
     * 统计流量  0关闭 1开启
     */
    public static void setIfUpdateTraffToWeb(int UpdateTraffToWeb) {
        updateTraffEnable = UpdateTraffToWeb;
        EtvApplication.getInstance().saveData("UpdateTraffToWeb", UpdateTraffToWeb);
    }

    /**
     * 获取缓存的守护进程的状态
     *
     * @return
     */
    public static boolean getGuardianStatues() {
        boolean defaultStatues = true;
        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_LK_QRCODE_SHOW_DHL) {
            defaultStatues = false;
        }
        return ((boolean) EtvApplication.getInstance().getData("guardianStatues", defaultStatues));
    }

    /**
     * 开机启动的时候会设置守护进程的状态
     *
     * @param guardianStatues
     */
    public static void setGuardianStatues(boolean guardianStatues) {
        EtvApplication.getInstance().saveData("guardianStatues", guardianStatues);
    }


    /**
     * 是否使用浏览器内核
     * 硬件加速
     *
     * @return
     */
    public static boolean getDevSpeedStatues() {
        return ((boolean) EtvApplication.getInstance().getData("isUseWebViewImp", true));
    }

    /**
     * 设置浏览器内核
     * 硬件加速
     *
     * @param isUseWebViewImp
     */
    public static void setDevSpeedStatues(boolean isUseWebViewImp) {
        MyLog.cdl("======设置浏览器内核====" + isUseWebViewImp);
        EtvApplication.getInstance().saveData("isUseWebViewImp", isUseWebViewImp);
    }

    /**
     * 设置双屏联动
     *
     * @param isScreenSame
     */
    public static void setScreenSame(boolean isScreenSame) {
        EtvApplication.getInstance().saveData("isScreenSame", isScreenSame);
    }

    /**
     * 是否双屏联动
     *
     * @return
     */
    public static boolean getScreenSame() {
        boolean isDefault = true;
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            isDefault = false;
        }
        return ((boolean) EtvApplication.getInstance().getData("isScreenSame", isDefault));
    }

    /**
     * 是否显示画笔图标
     *
     * @return
     */
    public static boolean isShowPaintIcon() {
        return ((boolean) EtvApplication.getInstance().getData("showPaintIcon", false));
    }

    /**
     * 设置显示
     *
     * @param showPaintIcon
     */
    public static void setShowPaintIcon(boolean showPaintIcon) {
        EtvApplication.getInstance().saveData("showPaintIcon", showPaintIcon);
    }

    public static boolean isFirstComeIn() {
        return ((boolean) EtvApplication.getInstance().getData("firstComeIn", true));
    }

    public static void setFirstComeIn(boolean firstComeIn) {
        EtvApplication.getInstance().saveData("firstComeIn", firstComeIn);
    }

    /**
     * 获取触摸默认返回时间
     *
     * @return
     */
    public static long getScene_task_touch_back_time() {
        long defaultTime = 1000 * 3600;
        return ((long) EtvApplication.getInstance().getData("scene_task_touch_back_time", defaultTime));
    }

    /**
     * 设定默认触摸返回时间
     *
     * @return
     */
    public static void setScene_task_touch_back_time(long scene_task_touch_back_time) {
        EtvApplication.getInstance().saveData("scene_task_touch_back_time", scene_task_touch_back_time);
    }

    /**
     * 获取单机模式的图片显示模式
     * 0： 全屏拉伸
     * 1： 比例拉伸
     *
     * @return
     */
    public static int getPicSingleShowTYpe() {
        return ((int) EtvApplication.getInstance().getData("picSingleShowTYpe", BannerConfig.SCREEN_SHOW_TYPE_ALL_GLOBLE));
    }

    public static void setPicSingleShowTYpe(int picSingleShowTYpe) {
        EtvApplication.getInstance().saveData("picSingleShowTYpe", picSingleShowTYpe);
    }

    /**
     * 单机模式视频是否拉伸
     * 0： 全屏拉伸
     * 1： 比例缩放
     *
     * @return
     */
    public static int geVideoSingleShowTYpe() {
//        M98视频缩放类型的定死为全屏拉伸
//        if (CpuModel.isMLogic()) {
//            return BannerConfig.SCREEN_SHOW_TYPE_ALL_GLOBLE;
//        }
        return (int) EtvApplication.getInstance().getData("videoSingleShowTYpe", BannerConfig.SCREEN_SHOW_TYPE_ALL_GLOBLE);
    }

    public static void setVideoSingleShowTYpe(int videoSingleShowTYpe) {
        EtvApplication.getInstance().saveData("videoSingleShowTYpe", videoSingleShowTYpe);
    }

    /****
     * 文档的显示格式
     * 0： 全屏拉伸
     * 1： 比例缩放
     * @return
     */
    public static int geWPSSingleShowTYpe() {
        return 0;
//        int backCode = (int) EtvApplication.getInstance().getData("wpsSingleShowTYpe", BannerConfig.SCREEN_SHOW_TYPE_PROPROTIONAL);
//        return backCode;
    }

    public static void setWPSSingleShowTYpe(int wpsSingleShowTYpe, String printTag) {
//        MyLog.cdl("=========setWPSSingleShowTYpe=====" + wpsSingleShowTYpe + " / printTag = " + printTag);
//        EtvApplication.getInstance().saveData("wpsSingleShowTYpe", wpsSingleShowTYpe);
    }

    /**
     * 获取屏幕了类型
     *
     * @return
     */
    public static final int SCREEN_TYPE_DEFAULT = 0;                          //两个屏幕同一个方向
    public static final int SCREEN_TYPE_DOUBLE_VER_OTHER_HRO = 1;            //主副屏--横屏，是竖屏

    public static int getScreen_type() {
        int screenType = (int) EtvApplication.getInstance().getData("screentype", SCREEN_TYPE_DEFAULT);
        MyLog.screen("======获取屏幕方向类型====" + screenType);
        return (int) EtvApplication.getInstance().getData("screentype", SCREEN_TYPE_DEFAULT);
    }

    /***
     * 设置屏幕类型
     * @param screentype
     */
    public static void setScreen_type(int screentype) {
        MyLog.cdl("======设置屏幕方向类型====" + screentype);
        EtvApplication.getInstance().saveData("screentype", screentype);
    }

    /***
     * 获取图片切换得时间
     * @return
     */
    public static int getPicSwitchingTime() {
        return (int) EtvApplication.getInstance().getData("picSwitchingTime", 800);
    }

    /***
     * BANNER切换时间
     * @param picSwitchingTime
     */
    public static void setPicSwitchingTime(int picSwitchingTime) {
        EtvApplication.getInstance().saveData("picSwitchingTime", picSwitchingTime);
    }

    //人脸授权信息
    public static String getAuthorId() {
        //        String authId = "xLZCDyRn";  //M8 授权
        //        String authId = "Q4GSLrcb";  //FACE88 授权
        return (String) EtvApplication.getInstance().getData("authorId", "");
    }

    public static void setAuthorId(String authorId) {
        EtvApplication.getInstance().saveData("authorId", authorId);
    }

    public static void setPersonOne(String personOne) {
        Log.e("cdl", "====setPersonOne===" + personOne);
        EtvApplication.getInstance().saveData("personOne", personOne);
    }

    public static String getPersonOne() {
        return (String) EtvApplication.getInstance().getData("personOne", "");
    }


    public static void setPersonTwo(String personTwo) {
        Log.e("cdl", "===setPersonTwo====" + personTwo);
        EtvApplication.getInstance().saveData("personTwo", personTwo);
    }

    public static String getPersonTwo() {
        return (String) EtvApplication.getInstance().getData("personTwo", "");
    }

    /***
     * 0 隐藏
     * 1：显示
     * 单机版本显示时间
     * @return
     */
    public static int getShowTimeEnable() {
        int ShowTimeEnable = (int) EtvApplication.getInstance().getData("ShowTimeEnable", 0);
        return ShowTimeEnable;
    }

    public static void setShowTimeEnable(int ShowTimeEnable) {
        EtvApplication.getInstance().saveData("ShowTimeEnable", ShowTimeEnable);
    }

    /***
     * 设置素材得保存路径
     * @param resourceServer
     */
    public static void setResourDownPath(String resourceServer) {
        MyLog.cdl("====setResourDownPath========" + resourceServer);
        EtvApplication.getInstance().saveData("resourceServer", resourceServer);
    }

    public static String getResourDownPath() {
        String resourceServer = (String) EtvApplication.getInstance().getData("resourceServer", ApiInfo.WEB_BASE_URL());
        MyLog.cdl("====getResourDownPath========" + resourceServer);
        return resourceServer;
    }

    public static void setDensityDpi(int densityDpi) {
        EtvApplication.getInstance().saveData("densityDpi", densityDpi);
    }

    public static int getDensityDpi() {
        int densityDpi = (int) EtvApplication.getInstance().getData("densityDpi", 72);
        return densityDpi;
    }

    /***
     * M11- 获取最后设置的音量
     * @return
     */
    public static int getLastMediaVoiceNum() {
        int densityDpi = (int) EtvApplication.getInstance().getData("LastMediaVoiceNum", 70);
        return densityDpi;
    }

    public static void setLastMediaVoiceNum(int LastMediaVoiceNum) {
        EtvApplication.getInstance().saveData("LastMediaVoiceNum", LastMediaVoiceNum);
    }

    public static void setSocketLineEnable(boolean cacheEnable) {
        socketLineEnable = cacheEnable ? 0 : 1;
        EtvApplication.getInstance().saveData("SocketLineEnable", cacheEnable);
    }

    private static int socketLineEnable = -1;

    /***
     * 0   true
     * 1   false
     */
    public static boolean getSocketLineEnable() {
        if (socketLineEnable != -1) {
            return socketLineEnable == 0;
        }
        boolean enable = (boolean) EtvApplication.getInstance().getData("SocketLineEnable", true);
        socketLineEnable = enable ? 0 : 1;
        return socketLineEnable == 0;
    }

}
