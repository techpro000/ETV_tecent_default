package com.etv.config;

import com.etv.entity.RegisterEntity;
import com.etv.util.CodeUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerUtil;
import com.etv.util.TextUtils;
import com.etv.util.aes.AESTool;

public class ApiInfo {

    public static final String IP_DEFAULT_URL_WEBSOCKET = "etv.esoncloud.com";
    public static final String IP_DEFAULT_URL_SOCKET = "etv.ids.esoncloud.com";

  /*  public static final String IP_DEFAULT_URL_WEBSOCKET = "139.9.202.29:9378";
    public static final String IP_DEFAULT_URL_SOCKET = "139.9.202.29:9378";*/


    public static String getAESCodeKey() {
        String key = CodeUtil.getUniquePsuedoID();
        key = key.substring(0, 16);
        return key;
    }

    public static RegisterEntity getRegisteMessageAddNum() {
        RegisterEntity registerEntity = null;
        String macAddress = CodeUtil.getUniquePsuedoID();
        byte[] srtbyte = macAddress.getBytes();
        boolean registeSuccess = true;
        for (int k = 0; k < srtbyte.length; k++) {
            if ((srtbyte[k] > 47 && srtbyte[k] < 58) || (srtbyte[k] > 96 && srtbyte[k] < 123) || (srtbyte[k] > 64 && srtbyte[k] < 91)) {
                // '0'48-'9'57  'a'97-'z'122 'A'65-'Z' 90
                continue;
            }
            registeSuccess = false;
        }
        if (!registeSuccess) {
            MyLog.cdl("====getRegisteMessageAddNum====数据不合格==");
            registerEntity = new RegisterEntity("MAC is not legal", null);
            return registerEntity;
        }
        MyLog.cdl("====getRegisteMessageAddNum====数据合格执行拼接加密过程==");
        byte[] byteRegister = new byte[srtbyte.length + 4];
        byteRegister[0] = 1;
        byteRegister[1] = 35;
        byteRegister[2] = (byte) (srtbyte.length & 0xFF);
        byteRegister[3] = 35;
        for (int i = 4; i < srtbyte.length + 4; i++) {
            byteRegister[i] = srtbyte[i - 4];
        }
        MyLog.cdl("====getRegisteMessageAddNum====srtbyte.length==" + srtbyte.length);
        if (byteRegister.length > 28) {
            //reboot
            MyLog.cdl("====getRegisteMessageAddNum====数据超过28位数了，拦截操作==");
            registerEntity = new RegisterEntity("The LENGTH of the MAC address is invalid !", null);
            return registerEntity;
        }
        registerEntity = new RegisterEntity("Get Mac Success ", byteRegister);
        return registerEntity;
    }

    public static String getWebIpHost() {
        return SharedPerUtil.getWebHostIpAddress();
    }

    public static String getWebPort() {
        return SharedPerUtil.getWebHostPort();
    }

    /**
     * 获取文件下载地址
     * 如果使用域名得话，直接用域名进行拼接
     * 如果其他客户得 直接使用 ip + port 端口号
     *
     * @return
     */
    public static String getFileDownUrl() {
        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_SOCKET) {
            return SharedPerUtil.getSocketDownPath();
        }
        String ipaddress = getWebIpHost();
        String port = getWebPort();
        String backUrl = "";
        if (ipaddress.contains(IP_DEFAULT_URL_WEBSOCKET)) {
            backUrl = "http://" + IP_DEFAULT_URL_WEBSOCKET;
        } else {
            backUrl = "http://" + ipaddress + ":" + port;
        }
        return backUrl;
    }

    /***
     * 返回IP+端口号
     * @return
     */
    public static String getIpHostWebPort() {
        String ipaddress = getWebIpHost();
        String port = getWebPort();
        if (ipaddress.contains(IP_DEFAULT_URL_SOCKET)) {
            return "http://" + ipaddress;
        }
        if (ipaddress.contains(IP_DEFAULT_URL_WEBSOCKET)) {
            return "http://" + ipaddress;
        }
        String backUrl = "http://" + ipaddress + ":" + port;
        return backUrl;
    }

    //获取基本请求URL
    public static String WEB_BASE_URL() {
        return getIpHostWebPort() + "/etv";
    }

    /***
     * 获取天气的接口
     * @return
     */
    public static String GET_WEATHER_URL() {
        return WEB_BASE_URL() + "/webservice/selectWeatherByCityName?cityName=";
//        http://119.23.220.53:8899/etv/webservice/selectWeatherByCityName?cityName=深圳
//        http://www.won-giant.com/etv/webservice/selectWeatherByCityName?cityName=深圳
    }

    /****
     * 上传一键报警视频接口Url
     * @return
     */
    public static String getUpdateWearVideoUrl() {
        return WEB_BASE_URL() + "/webservice/uploadWarningVideo";
    }

    /****
     * 接口统一
     * 1：内存限制台数接口
     * 2：背景图 logo 请求下载接口
     * 3：定时开关机
     * 4：字体接口
     * 5：系统时间
     * 6：心跳服务器配置
     * 7：设备自定义设
     * 8：文件服务器地址
     * @return
     */
    public static String getDevSettingAllInfo() {
        //userName  clNo
        return WEB_BASE_URL() + "/webservice/getClientBaseInfo";
    }


    /***
     * 提交设备信息给服务器
     * @return
     */
    public static String getUpdateDevToAuthorServer() {
        return "http://47.107.50.81:8080/etvauth/authapi/putSellBoardInfo";
    }

    /**
     * 用户手动删除
     *
     * @return
     */
    public static String DEL_TASK_BY_USER() {
        return WEB_BASE_URL() + "/webservice/deleteTaskByClientNo";
    }

    /***
     * 添加AED记录
     * @return
     */
    public static String ADD_WEAR_OPEREC() {
        return WEB_BASE_URL() + "/webservice/addWarningOperRec";
    }

    /***
     * 获取删除任务的接口
     * @return
     */
    public static String DEL_TASK_REQUEST_URL() {
        return WEB_BASE_URL() + "/webservice/deletetClientEqTaskInfo";
    }

    /**
     * 检查下载资格
     *
     * @return
     */
    public static String CHECK_DOWN_LIMIT() {
        return WEB_BASE_URL() + "/webservice/getDownLoadLimit";
    }

    //设备注册接口
    public static String REGISTER_WEB_TO_DEV() {
        return WEB_BASE_URL() + "/webservice/insertClient";
    }

    //查询定时开关机服务
    public static String POWERONOFF_QUERY() {
        return WEB_BASE_URL() + "/webservice/selectClientByClNo";
    }

    //监控图片上传
    public static String MONITOR_IMAGE_UPDATE() {
        return WEB_BASE_URL() + "/webservice/insertEqMonitorImg";
//        http://192.168.1.20:8899/etv/webservice/insertEqMonitorImg?clientNo=
    }

    //检测升级的功能
    public static String UPDATE_APP_SYSTEM() {
        return WEB_BASE_URL() + "/webservice/selectAllUpgradeFile";
    }

    //修改升级进度的功能
    //修改为100 %
    public static String UPDATE_APP_SYSTEM_TO_OVER() {
        return WEB_BASE_URL() + "/webservice/updateClientUpgradeFileProgress";
    }

    /***
     * 清除升级信息
     * 后边下载100 % 之后服务器自动销毁，这里不自动删除了
     * @return
     */
//    public static String DEL_APP_SYSTEM_INFO() {
//        return WEB_BASE_URL() + "/webservice/deleteClientUpgradeFileByClNo";
//    }

    /***
     * 下载状态更新到服务器
     * @return
     */
    public static String UPDATE_DOWN_PROGRESS() {
        return WEB_BASE_URL() + "/webservice/updateEqTaskProgress";
    }

    /***
     * 阿里云设备授权接口
     *    http://ip:端口/etv/webservice/authorizeClient
     * @return
     */
    public static String getDevRegisterAliWeb() {
        return "http://47.107.50.81:8080/etv/webservice/authorizeClient";
    }

    /***
     * 阿里云服务器
     * 查询设备授权状态
     * @return
     */
    public static String getDevAuthorStatues() {
        return "http://47.107.50.81:8080/etv/webservice/selectClientAuthInfo";
    }

    //同步服务器时间接口
    public static String UPDATE_TIME_FROM_WEB() {
//        http://192.168.1.20:8899/etv/webservice/selectCurrentTime
        return WEB_BASE_URL() + "/webservice/selectCurrentTime";
    }

    //修改昵称
    public static String MODIFY_NICK_NAME() {
        return WEB_BASE_URL() + "/webservice/updateClientName";
    }


    /**
     * 获取同步任务得得设备
     *
     * @return
     */
    public static String SAME_TASK_URL() {
        return WEB_BASE_URL() + "/webservice/selectSynchTaskAllClient";
    }

    //查询设备信息
    public static String QUERY_DEV_INFO() {
        return WEB_BASE_URL() + "/webservice/selectClient";
    }

    //提交素材统计信息
    public static String ADD_FILE_NUMBERS_TOTAL() {
        return WEB_BASE_URL() + "/webservice/insertStats";
    }

    //提交设备信息
    public static String UPDATE_DEV_INFO() {
        return WEB_BASE_URL() + "/webservice/updateClientInfo";
    }

    //新增移动流量数据
    public static String UPDATE_FLOW_USAGE() {
        return WEB_BASE_URL() + "/webservice/insertMobileData";
    }

    //获取任务接口
    public static String getClientTaskUrl() {
//        http://119.23.220.53:8899/etv/webservice/selectClientEMTask?clNo=301F9A8073AF18937FE2A447
        return WEB_BASE_URL() + "/webservice/selectClientEMTask";
    }

    //获取SD卡内存的阈值Url
    public static String getSdManagerCheckUrl() {
        return WEB_BASE_URL() + "/webservice/selectRamInfoAll";
    }

    //获取背景图相关得信息
    public static String getBggImageStatuesUrl() {
//        http://119.23.220.53:8899/etv/webservice/selectClientDiySet?
        return WEB_BASE_URL() + "/webservice/selectClientDiySet";
    }

//    /***
//     * 获取设备设置信息
//     * 全部信息
//     * @return
//     */
//    public static String getSelectClientSetInfo() {
//        return WEB_BASE_URL() + "/webservice/selectClientSetInfo";
//    }

    /***
     * 上传日志
     * @return
     */
    public static String getUpdateDevInfo() {
        return WEB_BASE_URL() + "/webservice/uploadEquipmentLog";
    }

    /**
     * 修改字幕内容的属性
     *
     * @return
     */
    public static String updateTxtInfo() {
        return WEB_BASE_URL() + "/webservice/updateSubtitle";
    }

    /***
     * 请求节目字体得接口
     * selectFontInfo
     * 参数：id（非必填）
     * @return
     */
    public static String getFontRequestInfo() {
//        selectFontInfo 参数：id（非必填）
        return WEB_BASE_URL() + "/webservice/selectFontInfo";
    }

    /**
     * 提交img升级进度
     *
     * @return
     */
    public static String getUpdateApkProgress() {
        return WEB_BASE_URL() + "/webservice/updateUpgradeFileProgress";
    }

    /***
     * 获取Socket长连接的IP地址
     * @return
     *   //"ws://192.168.1.97:8080/etv/websocket/socketServer.json?" + uuid;
     */
    public static String getSocketLineAddress() {
        String devId = CodeUtil.getUniquePsuedoID();
        String address = "ws://" + getWebIpHost() + ":" + getWebPort() + "/etv/webservice/linksocket?" + devId;
        return address;
    }


}
