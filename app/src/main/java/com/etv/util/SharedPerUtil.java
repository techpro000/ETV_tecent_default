package com.etv.util;

import android.text.TextUtils;

public class SharedPerUtil {

    public static int SCREEN_WIDTH = -1;
    public static int SCREEN_HEIGHT = -1;

    public static int getScreenWidth() {
        if (SCREEN_WIDTH > 100) {
            return SCREEN_WIDTH;
        }
        SCREEN_WIDTH = SharedPerManager.getScreenWidth();
        return SCREEN_WIDTH;
    }

    public static int getScreenHeight() {
        if (SCREEN_HEIGHT > 100) {
            return SCREEN_HEIGHT;
        }
        SCREEN_HEIGHT = SharedPerManager.getScreenHeight();
        return SCREEN_HEIGHT;
    }

    public static String SOCKET_DOWN_FILE_PATH = "";

    //获取socket 资源下载路径
    public static String getSocketDownPath() {
        if (SOCKET_DOWN_FILE_PATH.length() > 3) {
            return SOCKET_DOWN_FILE_PATH;
        }
        SOCKET_DOWN_FILE_PATH = SharedPerManager.getResourDownPath();
        return SOCKET_DOWN_FILE_PATH;
    }


    public static String WEBHOST_PORT = "";

    //获取网络请求得IPaddress
    public static String getWebHostPort() {
        if (WEBHOST_PORT.length() > 3) {
            return WEBHOST_PORT;
        }
        WEBHOST_PORT = SharedPerManager.getWebPort();
        return WEBHOST_PORT;
    }

    public static String WEBHOST_IP_ADDRESS = "";

    //获取网络请求得IPaddress
    public static String getWebHostIpAddress() {
        if (WEBHOST_IP_ADDRESS.length() > 3) {
            return WEBHOST_IP_ADDRESS;
        }
        WEBHOST_IP_ADDRESS = SharedPerManager.getWebHost();
        return WEBHOST_IP_ADDRESS;
    }

    public static int DEFAULT_SOCKET_PORT = -1;


    /***
     * 用来缓存当前状态值，
     * 不用重复从sharedPerfance 中取获取消耗内存
     */
    public static int CURRENT_SOCKET_TYPE = -1;

    public static int SOCKEY_TYPE() {
        if (CURRENT_SOCKET_TYPE > -1) {
            return CURRENT_SOCKET_TYPE;
        }
        CURRENT_SOCKET_TYPE = SharedPerManager.getSocketType();
        return CURRENT_SOCKET_TYPE;
    }


    /***
     * 获取图片得加载方式
     * @return
     */
    public static int ImageShowType = -1;

    /***
     * 0  Glide
     * 1  Fresco
     * @return
     */
    public static int getImageShowType() {
        if (ImageShowType > -1) {
            return ImageShowType;
        }
        ImageShowType = SharedPerManager.getImageShowType();
        return ImageShowType;
    }


}
