package com.etv.service.parsener;

import static com.etv.config.AppConfig.APP_TYPE_JIANGJUN_YUNCHENG;

import android.content.Context;
import android.content.Intent;

import com.etv.activity.model.RegisterDevListener;
import com.etv.activity.pansener.InitPansener;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.entity.BggImageEntity;
import com.etv.listener.BitmapCaptureListener;
import com.etv.service.TcpService;
import com.etv.util.ScreenUtil;
import com.etv.util.image.CaptureImageListener;
import com.etv.util.image.ImageCaptureUtil;
import com.etv.util.poweronoff.PowerOnOffManager;
import com.etv.util.poweronoff.db.PowerDbManager;
import com.etv.listener.TaskChangeListener;
import com.etv.service.listener.SysSettingAllInfoListener;
import com.etv.service.listener.SysSettingAllViewInfoListener;
import com.etv.service.listener.TcpServerListener;
import com.etv.service.util.TcpServerModule;
import com.etv.service.util.TcpServerModuleImpl;
import com.etv.service.util.TcpServiceView;
import com.etv.service.view.TcpHartStatuesListener;
import com.etv.service.view.TcpPowerOnOffListener;
import com.etv.util.Biantai;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.util.SimpleDateUtil;
import com.etv.util.poweronoff.entity.TimerDbEntity;
import com.etv.util.rxjava.AppStatuesListener;
import com.etv.util.sdcard.FileFilter;
import com.etv.util.sdcard.MySDCard;
import com.etv.util.tts.TtsManager;

import java.io.File;
import java.util.List;

public class TcpParsener {

    Context context;
    TcpServerModule tcpServerModule;

    public TcpParsener(Context context) {
        this.context = context;
        initOther();
        initTTSManager();
    }

    /****
     * 语音TTS
     * @param tts
     */
    TtsManager ttsManager;

    public void startToSpeak(String tts) {
        MyLog.tts("======startToSpeak=====add=" + tts, true);
        initOther();
        ttsManager.addSpeechMessageToList(tts);
    }

    public void stopToSpeakMessage() {
        MyLog.cdl("停止TTS语音", true);
        initOther();
        ttsManager.stop();
    }


    /***===========================================================================================================
     * int tag_hart
     * 1：  表示心跳，根据标志位来判断是否要请求设备信息
     * -1： 表示连接服务器，已经注册成功了额，不用请求服务器设备信息
     * 用来区别是注册还是心跳
     */
    public void getDevHartStateInfo(int tag_hart, String desc, TcpHartStatuesListener listener) {
        if (Biantai.checkHeartTime()) {
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(context)) {
            MyLog.netty("网络异常，心跳中断", true);
            return;
        }
        if (tag_hart < 0) { //已经注册好了，不用重复注册获取消息，直接发消息
            listener.sendHeartMessage("从其他界面进来");
            return;
        }
        MyLog.netty("======开始心跳==111==" + AppInfo.isDevRegister, true);
        if (AppInfo.isDevRegister) {
            //设备已经注册了，不用重复注册
            listener.sendHeartMessage("设备已经注册，直接发消息");
            return;
        }
        tcpServerModule.getDevHartInfo(context, new TcpServerListener() {
            @Override
            public void getDevHartInfoStatues(boolean isSuccess, String desc) {
                if (isSuccess) {//设备存在
                    MyLog.netty("=服务器中有该设备,去连接socket", true);
                    listener.sendHeartMessage("查询了设备信息返回来");
                    return;
                }
                //设备不存在
                listener.registerDev("====getDevHartStateInfo===设备不存在，这里去注册设备=");  //这里是解决更换服务器之后无法连接的问题
                MyLog.netty("=设备未注册,请回到主界面或重启设备", true);
            }
        });
    }

    /***
     * 注册设备
     * @param context
     * @param userName
     * @param listener
     */
    public void registerDev(Context context, String userName, RegisterDevListener listener) {
        if (!NetWorkUtils.isNetworkConnected(context)) {
            MyLog.cdl("注册设备么有网络，终端操作");
            return;
        }
        if (!SharedPerManager.getSocketLineEnable() && AppInfo.isDevRegister) {
            listener.registerDevState(true, "已经注册成功了，不用重复注册", 0);
            return;
        }
        initOther();
        tcpServerModule.registerDevToWeb(context, userName, listener);
    }

    /***
     * 上传日志到服务器
     */
    public void updateFileWorkInfoToWeb() {
        //上传工作日志
        initOther();
        tcpServerModule.updateWorkInfoTxt();
    }

    /***
     * 检查背景图Main
     */
    public void checkBggImageStatues() {
        int workModel = SharedPerManager.getWorkModel();
        if (workModel != AppInfo.WORK_MODEL_NET) {
            MyLog.bgg("==下载背景图===工作模式不对，不检查背景图==");
            return;
        }
        initOther();
        tcpServerModule.getBggImageInfoStatues(new TcpServiceView() {
            @Override
            public void backSuccessBggImageInfo(List<BggImageEntity> lists, String errorDesc) {
                if (lists == null || lists.size() < 1) {
                    AppStatuesListener.getInstance().UpdateMainBggEvent.postValue("原接口，瞎子啊");
                    MyLog.bgg("==下载背景图===当前没有背景图==");
                    return;
                }
                MyLog.bgg("=====下载背景图====数据库文件个数=" + lists.size());
                boolean isDownBggImage = true;
                for (int i = 0; i < lists.size(); i++) {
                    BggImageEntity bggImageEntity = lists.get(i);
                    String fileName = bggImageEntity.getImageName();
                    String fileLocalPath = bggImageEntity.getSavePath() + "/" + fileName;
                    MyLog.bgg("=====下载背景图===帅选数据==" + fileName + " / " + fileLocalPath + " / " + bggImageEntity.getFileStype());
                    File file = new File(fileLocalPath);
                    if (!file.exists()) {
                        isDownBggImage = false;
                        break;
                    }
                }
                // 刷新背景素材成功，这里去判断下载
                if (!isDownBggImage) {
                    MyLog.bgg("=====下载背景图===帅选数据==去下载素材");
                    sendBroadCastToView(AppInfo.CHECK_BGG_IMAGE_TO_DOWN_SHOW);
                } else {
                    //通知界面去刷新数据
                    MyLog.bgg("=====下载背景图===帅选数据==刷新数据");
                    AppStatuesListener.getInstance().UpdateMainBggEvent.postValue("原接口，瞎子啊");
                }
            }
        });
    }


    /**
     * 发送广播给
     *
     * @param action
     */
    public void sendBroadCastToView(String action) {
        if (context == null) {
            return;
        }
        try {
            Intent intent = new Intent();
            intent.setAction(action);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 上传截图，给服务器
     * @param tag
     */
    public void updateImageToWeb(String tag,String imagePath) {
        if (tag == null || tag.length() < 1) {
            return;
        }
        try {
            MyLog.update("==截图回来了==准备上传==" + tag, true);
            if (tag.equals(AppInfo.TAG_UPDATE)) {
                initOther();
                tcpServerModule.monitorUpdateImage(context.getApplicationContext(), imagePath);
            }
        } catch (Exception e) {
            MyLog.update("==截图回来了==上传异常==" + e.toString(), true);
            e.printStackTrace();
        }
    }

    private void initOther() {
        if (tcpServerModule == null) {
            tcpServerModule = new TcpServerModuleImpl();
        }
    }

    private void initTTSManager() {
        if (!SharedPerManager.getOpenTTSManager()) {
            return;
        }
        if (ttsManager == null) {
            ttsManager = new TtsManager(context);
        }
    }

    /***
     * 修改昵称
     * @param context
     * @param changeUsername
     */
    public void updateDevInformation(Context context, String changeUsername) {
        initOther();
        tcpServerModule.updateDevInformation(context, changeUsername);
    }

    /**
     * 获取字体信息
     */
    public void getProjectFontInfoFromWeb(String printTag) {
        initOther();
        tcpServerModule.getProjectFontInfoFromWeb(printTag);
    }

    /***
     *  获取后台设备信息
     *  接受到指令，同步后台设置属性
     */
    public void syncSytemInfoSetting() {
        initOther();
        //获取后台设备信息
        tcpServerModule.getSystemSettingInfoTCP();
    }

    MySDCard sdcard;

    public void checkSystemSettingInfo() {
        int currentTime = SimpleDateUtil.getHourMin();
        if (currentTime % 5 != 0) {
            MyLog.message("定时检查SD卡内存以及连接状态==时间不合法" + currentTime);
            return;
        }
        MyLog.message("定时检查SD卡内存以及连接状态==" + currentTime);
        try {
            if (sdcard == null) {
                sdcard = new MySDCard(context);
            }
            String sdcardPath = AppInfo.BASE_SD_PATH();
            String lastSpace = sdcard.getLastSpace(1, sdcardPath);
            long lastLong = (Long.parseLong(lastSpace) / (1024 * 1024));
            int sizeSave = 500;
            if (lastLong < sizeSave) {
                MyLog.cdl("内存不足 " + sizeSave + " M,系统自动清理", true);
                //去清理文件夹
                FileFilter.clearSDcardForCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /****
     * 获取所有得设置设备信息
     *     //内存信息
     *     //定时开关机
     *     //字体
     *     //同步系统时间
     *     //背景图-logo-区间音量
     *     //系统设置
     */
    public void getSystemSettingAllInfo(SysSettingAllViewInfoListener listener) {

        int workModel = SharedPerManager.getWorkModel();
        if (workModel != AppInfo.WORK_MODEL_NET) { //单机模式不接受指令
            MyLog.cdl("当前为单机模式，不联网络取定时开关机");
            PowerDbManager.clearTimeDb("当前为单机模式，不联网络取定时开关机");
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(context)) {
            MyLog.cdl("当前没有网络==");
            return;
        }
        initOther();
        tcpServerModule.getSystemSettingAllInfo(new SysSettingAllInfoListener() {

            @Override
            public void backPowerOnOffTimeInfo(boolean isSuccess, List<TimerDbEntity> timedTaskList, String errorDesc) {
                MyLog.powerOnOff("===获取的定时开关机返回Parsener==" + isSuccess + " / " + errorDesc);
                if (!isSuccess) {
                    listener.clearTimer();
                    MyLog.powerOnOff("===获取的定时开关机失败==" + errorDesc);
                    PowerOnOffManager.getInstance().getPowerOnOffFromDb();
                    return;
                }
                if (timedTaskList == null || timedTaskList.size() < 1) {
                    //没有数据，直接清除数据
                    listener.clearTimer();
                    return;
                }
                MyLog.powerOnOff("===获取的定时开关机数据==" + timedTaskList.size());
                PowerOnOffManager.getInstance().savePowerOnOffTime(timedTaskList, "获取整体系统设置信息", null);
                listener.updateTime();
            }

            @Override
            public void backSuccessBggImageInfo(List<BggImageEntity> lists, String errorDesc) {
                if (lists == null || lists.size() < 1) {
                    AppStatuesListener.getInstance().UpdateMainBggEvent.postValue("获取所有得接口，更新壁纸---");
                    MyLog.bgg("==下载背景图===当前没有背景图==");
                    return;
                }
                MyLog.bgg("=====下载背景图====数据库文件个数=" + lists.size());
                boolean isDownBggImage = true;
                for (int i = 0; i < lists.size(); i++) {
                    BggImageEntity bggImageEntity = lists.get(i);
                    String fileName = bggImageEntity.getImageName();
                    String fileLocalPath = bggImageEntity.getSavePath() + "/" + fileName;
                    MyLog.bgg("=====下载背景图===帅选数据==" + fileName + " / " + fileLocalPath + " / " + bggImageEntity.getFileStype());
                    File file = new File(fileLocalPath);
                    if (!file.exists()) {
                        isDownBggImage = false;
                        break;
                    }
                }
                // 刷新背景素材成功，这里去判断下载
                if (!isDownBggImage) {
                    MyLog.bgg("=====下载背景图===帅选数据==去下载素材");
                    sendBroadCastToView(AppInfo.CHECK_BGG_IMAGE_TO_DOWN_SHOW);
                } else {
                    //通知界面去刷新数据
                    MyLog.bgg("=====下载背景图===帅选数据==刷新数据");
                    AppStatuesListener.getInstance().UpdateMainBggEvent.postValue("获取所有得接口，更新壁纸---");
                }
            }
        });
    }

    public void dealPowernOff(TcpPowerOnOffListener listener) {
        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_KING_LAM) {
            MyLog.powerOnOff("0000==============金浪漫客户，不请求定时开关机");
            return;
        }
        int workModel = SharedPerManager.getWorkModel();
        if (workModel != AppInfo.WORK_MODEL_NET) { //单机模式不接受指令
            MyLog.powerOnOff("当前为单机模式，不联网络取定时开关机");
            PowerDbManager.clearTimeDb("dealPowernOff 当前为单机模式，不联网络取定时开关机");
            return;
        }
        MyLog.netty("准备获取定时开关机任务", true);
        initOther();
        tcpServerModule.getPowerOnOffTask(new TaskChangeListener() {
            @Override
            public void taskRequestSuccess(boolean isTrue, List<TimerDbEntity> timedTaskList, String errorrDesc) {
                if (!isTrue) {
                    MyLog.powerOnOff("==请求定时开关机异常，不做任何操作==" + errorrDesc, true);
                    return;
                }
                if (timedTaskList == null || timedTaskList.size() < 1) {
                    //没有数据，直接清除数据
                    listener.clearTimer();
                    return;
                }
                MyLog.powerOnOff("===获取的定时开关机数据==" + timedTaskList.size());
                PowerOnOffManager.getInstance().savePowerOnOffTime(timedTaskList, "处理定时开关机业务", null);

                listener.updateTime();

            }
        });

    }

    public void startCaptureImage() {
        if (AppConfig.APP_TYPE == APP_TYPE_JIANGJUN_YUNCHENG) {
            ImageCaptureUtil.captureScreen(context, new CaptureImageListener() {
                @Override
                public void getCaptureImagePath(boolean isSuucess, String imagePath) {
                    MyLog.update("=========截圖返回-------------" + isSuucess + " / " + imagePath, true);
                    updateScreenshotToWeb(imagePath);
                }
            });
            return;
        }
        //截图功能统一写到 守护进程里面，不要调用API 截图，API 覆盖主板不完全，切记
        ScreenUtil.getScreenImage(context, AppInfo.TAG_UPDATE, new BitmapCaptureListener() {
            @Override
            public void backCaptureImage(boolean isSuccess, String imagePath) {
                if (!isSuccess){
                    return;
                }
                updateScreenshotToWeb(AppInfo.CAPTURE_MAIN);
            }
        });
    }

    //上传截图到服务器
    private void updateScreenshotToWeb(String imagePath) {
        try {
            MyLog.update("==截图回来了==准备上传==", true);
            initOther();
            updateImageToWeb(AppInfo.TAG_UPDATE,imagePath);
        } catch (Exception e) {
            MyLog.update("==截图回来了==上传异常==" + e.toString());
            e.printStackTrace();
        }
    }

}
