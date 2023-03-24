package com.etv.util;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.etv.util.system.CpuModel;

public class ViewSizeChange {


    /***
     * 设置字幕插播的位置
     * @param rela_mat_bgg
     */
    public static void setTextInsertViewPosition(RelativeLayout rela_mat_bgg, int topPosition, int viewheight) {
        RelativeLayout.LayoutParams localLayoutParams = (RelativeLayout.LayoutParams) rela_mat_bgg.getLayoutParams();
        localLayoutParams.topMargin = topPosition;
        localLayoutParams.height = viewheight;
        rela_mat_bgg.setLayoutParams(localLayoutParams);
    }


    public static void setMainLogoPosition(LinearLayout linearLayout) {
        int width = SharedPerUtil.getScreenWidth();
        int height = SharedPerUtil.getScreenHeight();
        if (width - height > 0) { //横屏
            setScreenHroView(linearLayout);
        } else {
            setScreenVerView(linearLayout);
        }
    }

    private static void setScreenVerView(LinearLayout linearLayout) {
        int height = SharedPerUtil.getScreenHeight();
        height = height / 2;
        int viewHeight = 270;
        MyLog.cdl("=====view的高度111===" + viewHeight);
        RelativeLayout.LayoutParams localLayoutParams = (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
        localLayoutParams.topMargin = height - viewHeight * 3 / 2;
        linearLayout.setLayoutParams(localLayoutParams);
    }

    private static void setScreenHroView(LinearLayout linearLayout) {
        int height = SharedPerUtil.getScreenHeight();
        height = height / 2;
        int viewHeight = 150;
        MyLog.cdl("=====view的高度000===" + viewHeight);
        RelativeLayout.LayoutParams localLayoutParams = (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
        localLayoutParams.topMargin = height - 250;
        linearLayout.setLayoutParams(localLayoutParams);
    }

    public static void setRecycleExcelView(LinearLayout view, int size, int viewWidth) {
        int widthShow = viewWidth / size;
        RelativeLayout.LayoutParams localLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        localLayoutParams.width = widthShow;
        localLayoutParams.height = 50;
        view.setLayoutParams(localLayoutParams);
    }

    public static void setLogoPosition(ImageView iv_logo_show) {
        int height = SharedPerUtil.getScreenHeight();
        int hei_half = height / 2;
        int distance = hei_half / 3;
        int top_mar = hei_half - distance;
        RelativeLayout.LayoutParams localLayoutParams = (RelativeLayout.LayoutParams) iv_logo_show.getLayoutParams();
        localLayoutParams.width = 150;
        localLayoutParams.height = 150;
        localLayoutParams.topMargin = top_mar;
        iv_logo_show.setLayoutParams(localLayoutParams);
    }


}
