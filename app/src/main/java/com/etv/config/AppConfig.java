package com.etv.config;

import android.text.TextUtils;

import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.net.ImUtils;
import com.etv.util.sdcard.MySDCard;

public class AppConfig {

    //用来检测当前开机时间是否是正常得时间

    public static long TIME_CHECK_POWER_REDUCE = 20220210101010L;

    public static final int APP_TYPE_DEFAULT = 0;               //default
    public static final int APP_TYPE_SHI_WEI = 1;               //视威-广州
    public static final int APP_TYPE_MIKE = 2;                  //米可
    public static final int APP_TYPE_QINGFENG_DEFAULT = 4;      //广州青蜂--主界面图标显示
    public static final int APP_TYPE_QINGFENG_NOT_QR = 4001;    //广州青蜂--去掉二维码
    public static final int APP_TYPE_HUANGZUNNIANHUA = 5;                        //深圳皇尊年华
    public static final int APP_TYPE_RUIGUAN = 7;                //广州瑞冠电子科技
    public static final int APP_TYPE_JIANGJUN_YUNCHENG = 13;     //运城日报--唯意客户-将军
    public static final int APP_TYPE_AD_JH = 21;                 //深远通--景华广告
    public static final int APP_TYPE_JIAIWEI = 22;               //杰爱威  --定制launcher
    public static final int APP_TYPE_START_ON_POWER = 24;        //千视嘉-开机启动-不用守护进程
    public static final int APP_TYPE_THREE_VIEW_STAND = 27;      //三视立---修改背景默认图片
    public static final int APP_TYPE_THREE_TAI = 28;             //三泰 --设置固定IP
    public static final int APP_TYPE_KING_LAM = 29;              //金朗曼-不用我们得定时开关机
    public static final int APP_TYPE_CW_GPIO = 34;               //触沃-红外感应-语音播报
    public static final int APP_TYPE_LK_QRCODE = 35;             //广州林肯更换二维码
    public static final int APP_TYPE_LK_QRCODE_SHOW_DHL = 47;    //广州林肯更换二维码,显示导航栏
    public static final int APP_TYPE_YOUSE_AUTHOR = 37;           //优色--默认登陆账号 IP
    public static final int APP_TYPE_SENHAN = 39;                 // 珠海涤生涵科技有限公司-去掉中间的二维码功能
    public static final int APP_TYPE_CHUNYN = 40;                 //传音-固定服务器IP
    public static final int APP_TYPE_SCHOOL_STUDY = 44;           //放心学   webSocket
    public static final int APP_TYPE_XINABICN = 46;             // 深圳市芯纳百川技术有限公司

    public static final int APP_TYPE_NO_ERCODE = 100;             //没有二维码的模式
    public static final int APP_TYPE_WISIJIE = 100;             //微世杰--HDMIN-NUM-默认2
    public static final int APP_TYPE_TEST = 101;                  //测试模式-不要轻易打包这个版本
    public static final int APP_TYPE_POLICE_ALERT = 103;//一键报警版本
    public static final int APP_TYPE_TURN_ON_BLUETOOTH = 104;//开启，蓝牙不关闭
    public static final int APP_TYPE_DEFAULT_ADDRESS_USERNAME = 105;  //默认地址:139.9.126.178，用户名:tailg,连接方式:websocket
    public static final int APP_TYPE_TD_SERVICE_CENTER = 106;  //  解决拓达服务中心网显示错位问题
    public static final int APP_TYPE_TY_DEFAULT_ADDRESS = 107;  //广州图艺默认地址:118.31.56.116，连接方式:websocket
    public static final int APP_TYPE_ETV_ESONCLOUD_IP = 108;  //云服务器地址:139.159.152.78，连接方式:socket
    public static final int APP_TYPE_RW_DEFAULT_ADDRESS = 109;  // 融威默认ip地址:10.80.45.10，连接方式,websocket,默认关闭互动按钮
    public static final int APP_TYPE_ZB_DEFAULT_ADDRESS = 110;  // 中百默认ip地址:www.zhongbaizhihui.com


    public static final int APP_TYPE = APP_TYPE_DEFAULT;


    /***0
     * Socket 连接方式
     *
     *     */
    public static final int SOCKEY_TYPE_WEBSOCKET = 0;    //webSocket
    public static final int SOCKEY_TYPE_SOCKET = 1;       //socket


    // 软件是否打印日志
    public static final boolean IF_PRINT_LOG = true;


    /***
     * 守护进程的版本号================================================================================
     */


    public static final int APP_BACK_TIME_MIX = 5;  //互动节目，返回ETV的最小时间

    /***
     */
    public static boolean isOnline = false;

    /***
     * 是否已经初始化腾讯socket的sdk
     */
    public static boolean isInitedTimSDK = false;


    /***
     * 地图定位的间隔时间
     */
    public static final int BAIDU_MAP_LOCATION = 1000 * 10;

    /***
     * 回到主界面，自动检查播放
     */
    public static int CHECK_TIMER_TO_PLAY() {
        String exitPassword = SharedPerManager.getExitpassword();
        if (TextUtils.isEmpty(exitPassword)) {
            return 5;
        }
        return 30;
    }

    //终端发送一个一个空消息。保持服务器在线状态
    public static final int MESSAGE_AUTO_SEND_SOCKET = 3 * 1000;
    //无缝切换的时间 默认 2000
    public static final int Seamless_Switching_Time = 800;
    //单机模式，混播的切换时间  默认 400
    public static final int Seamless_Switching_Single_model = 400;

    /***
     *设备与服务器之间的心跳
     */
    public static int TIME_TO_HART_TO_WEB() {
        return 25 * 1000;
    }

    //心跳超时
    public static int TIME_TO_HART_MORE_TIME() {
        return 5 * 1000;
    }

}
