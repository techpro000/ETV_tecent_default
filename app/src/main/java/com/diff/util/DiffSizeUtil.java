package com.diff.util;

import android.util.Log;

import com.diff.entity.DiffShowEntity;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.system.CpuModel;

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
        if (doubleShowType == AppInfo.DOUBLE_SCREEN_SHOW_DEFAULT) {
            //原尺寸显示
            if ( screenWidth > viewWidth){
                widthChSize = (float) ((screenWidth * 1.0) / (viewWidth * 1.0));
                heightChSize = (float) ((screenHeight * 1.0) / (viewHeight * 1.0));
            }
        } else if (doubleShowType == AppInfo.DOUBLE_SCREEN_SHOW_ADAPTER) {
            //强制适配
            widthChSize = (float) ((viewWidth * 1.0) / (screenWidth * 1.0));
            heightChSize = (float) ((viewHeight * 1.0) / (screenHeight * 1.0));
        } else if (doubleShowType == AppInfo.DOUBLE_SCREEN_SHOW_GT_TRANS) {
            //高通翻转
//            widthChSize = (float) ((screenHeight * 1.0) / (screenWidth * 1.0));
//            heightChSize = (float) ((screenWidth * 1.0) / (screenHeight * 1.0));

            widthChSize = (float) ((screenWidth * 1.0) / (viewWidth * 1.0));
            heightChSize = (float) ((screenHeight * 1.0) / (viewHeight * 1.0));

            MyLog.diff("===副屏的比例尺寸==高通反向");
        } else if (doubleShowType == AppInfo.DOUBLE_SCREEN_SHOW_PX30) {
            heightChSize = (float) ((screenHeight * 1.0) / (screenWidth * 1.0));
            widthChSize = (float) ((screenWidth * 1.0) / (screenHeight * 1.0));
        }
        DiffShowEntity diffShowEntity = new DiffShowEntity(widthChSize, heightChSize);
        return diffShowEntity;
    }

}
