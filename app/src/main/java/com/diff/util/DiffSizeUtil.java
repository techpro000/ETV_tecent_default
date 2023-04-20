package com.diff.util;

import android.util.Log;

import com.diff.entity.DiffShowEntity;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.util.MyLog;
import com.etv.util.RootCmd;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.system.CpuModel;
import com.etv.util.system.SystemManagerUtil;

public class DiffSizeUtil {

    /***
     * 用来换算-副屏得显示比例
     * @param screenWidth
     * @param screenHeight
     * @return
     */
    public static DiffShowEntity getDiffScreenSizeShow(int screenWidth, int screenHeight) {
        int viewWidth = SharedPerUtil.getScreenWidth();
        int viewHeight = SharedPerUtil.getScreenHeight();
        int doubleShowType = SharedPerManager.getDoubleScreenMath();
        float widthChSize = 1.0f;    //屏幕压缩比例
        float heightChSize = 1.0f;   //屏幕压缩比例
        MyLog.diff("===屏幕尺寸显示==" + doubleShowType + " / " + viewWidth + " / "
            + viewHeight + " /副屏显示==== " + screenWidth + " / " + screenHeight);
        if (doubleShowType == AppInfo.DOUBLE_SCREEN_SHOW_PX30) {
            MyLog.diff("当前模式==长宽互置");
            //最初的配置
//            heightChSize = (float) ((screenHeight * 1.0) / (screenWidth * 1.0));
//            widthChSize = (float) ((screenWidth * 1.0) / (screenHeight * 1.0));
            return getPx30BoardDiffShowEntity(screenWidth, screenHeight, viewWidth, viewHeight);
        }
        if (doubleShowType == AppInfo.DOUBLE_SCREEN_SHOW_DEFAULT) {
            MyLog.diff("当前模式==原尺寸显示");
            //原尺寸显示
            if (screenWidth > viewWidth) {
                widthChSize = (float) ((screenWidth * 1.0) / (viewWidth * 1.0));
                heightChSize = (float) ((screenHeight * 1.0) / (viewHeight * 1.0));
            }
            return new DiffShowEntity(widthChSize, heightChSize);
        }
        if (doubleShowType == AppInfo.DOUBLE_SCREEN_SHOW_ADAPTER) {
            //强制适配
            MyLog.diff("当前模式==强制适配");
            widthChSize = (float) ((viewWidth * 1.0) / (screenWidth * 1.0));
            heightChSize = (float) ((viewHeight * 1.0) / (screenHeight * 1.0));
            return new DiffShowEntity(widthChSize, heightChSize);
        }
        if (doubleShowType == AppInfo.DOUBLE_SCREEN_SHOW_GT_TRANS) {
            MyLog.diff("当前模式==高通反向");
            widthChSize = (float) ((screenWidth * 1.0) / (viewWidth * 1.0));
            heightChSize = (float) ((screenHeight * 1.0) / (viewHeight * 1.0));
            return new DiffShowEntity(widthChSize, heightChSize);
        }
        MyLog.diff("当前模式==未知模式");
        DiffShowEntity diffShowEntity = new DiffShowEntity(widthChSize, heightChSize);
        return diffShowEntity;
    }


    /***
     *
     * @param screenWidth
     * @param screenHeight
     * @param viewWidth
     * 屏幕宽度
     * @param viewHeight
     * 屏幕高度
     * @return
     */
    private static DiffShowEntity getPx30BoardDiffShowEntity(int screenWidth, int screenHeight, int viewWidth, int viewHeight) {
        MyLog.diff("当前模式=screenWidth=" + screenWidth + " / " + screenHeight + " / " + viewWidth + " / " + viewHeight);
        if (SharedPerManager.getWorkModel() == AppInfo.WORK_MODEL_SINGLE) {
            return getPx30BoardDiffShowSingleEntity(screenWidth, screenHeight, viewWidth, viewHeight);
        }
        float widthChSize = 1.0f;    //屏幕压缩比例
        float heightChSize = 1.0f;   //屏幕压缩比例
        if (screenWidth > screenHeight) {
            //副屏横屏
            //1920-1080    1280-800
            MyLog.diff("当前模式=副屏横屏");
            widthChSize = (float) ((screenHeight * 1.0) / (viewWidth * 1.0));
            heightChSize = (float) ((screenWidth * 1.0) / (viewHeight * 1.0));
        } else {
            //副屏竖屏
            //  1920-1080   800-1280
            MyLog.diff("当前模式=副屏竖屏=" + viewHeight + " / " + screenHeight);
            widthChSize = (float) (screenWidth * 1.0 / viewWidth * 1.0);
            heightChSize = (float) ((screenHeight * 1.0) / (viewHeight * 1.0));
        }
        return new DiffShowEntity(widthChSize, heightChSize);
    }

    /**
     * 单机模式，设置宽高比例算法
     *
     * @param screenWidth
     * @param screenHeight
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    private static DiffShowEntity getPx30BoardDiffShowSingleEntity(int screenWidth, int screenHeight, int viewWidth, int viewHeight) {
        float widthChSize = 1.0f;    //屏幕压缩比例
        float heightChSize = 1.0f;   //屏幕压缩比例
        if (screenWidth > screenHeight) {
            //副屏横屏
            widthChSize = (float) ((screenHeight * 1.0) / (screenWidth * 1.0));
            heightChSize = (float) ((screenWidth * 1.0) / (screenHeight * 1.0));
        } else {
            //副屏竖屏
            widthChSize = 1.0f;
            heightChSize = 1.0f;
        }
        MyLog.diff("当前模式==单机模式==" + widthChSize + " / " + heightChSize);
        return new DiffShowEntity(widthChSize, heightChSize);

//        if (viewWidth > viewHeight) {
//            MyLog.diff("当前模式=单机模式==主屏横屏=");
//            //主屏横屏
//            if (screenWidth > screenHeight) {
//                //副屏横屏
//                //1920-1080    1280-800
//                MyLog.diff("当前模式=单机模式==主屏横屏=副屏横屏");
//                widthChSize = (float) ((screenHeight * 1.0) / (screenWidth * 1.0));
//                heightChSize = (float) ((screenWidth * 1.0) / (screenHeight * 1.0));
//            } else {
//                //副屏竖屏
//                //  1920-1080   800-1280
//                MyLog.diff("当前模式=单机模式==主屏横屏=副屏竖屏=" + viewHeight + " / " + screenHeight);
//                widthChSize = 1.0f;
//                heightChSize = 1.0f;
//            }
//            MyLog.diff("当前模式=单机模式===" + widthChSize + " / " + heightChSize);
//            return new DiffShowEntity(widthChSize, heightChSize);
//        }

    }

}
