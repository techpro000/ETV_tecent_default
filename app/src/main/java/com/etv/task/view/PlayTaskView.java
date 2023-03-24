package com.etv.task.view;

import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.etv.task.entity.CpListEntity;

import java.util.List;

public interface PlayTaskView {

    /**
     * 加载插播消息
     *
     * @param cpListEntity
     */
    void playInsertTextTaskToPopWindows(boolean isShow, CpListEntity cpListEntity);

    /**
     * 全屏展示View
     * 用来暂停播放，以及恢复播放
     */
    void toShowFullScreenView(CpListEntity cpListEntity, List<String> list, int clickPosition);

    /***
     * 启动webView
     * @param webUrl
     */
    void startViewWebActivty(String webUrl, String backTime);

    void findTaskNew();

    /***
     * 获取信息或者显示信息异常
     * @param errorInfo
     */
    void showViewError(String errorInfo);

    /**
     * 显示Toast view
     *
     * @param toast
     */
    void showToastView(String toast);

    /***
     * 获取跟布局
     * @return
     */
    AbsoluteLayout getAbsoluteLayout();

    ImageView getBggImageView();

    /**
     * 开启APK的界面
     *
     * @param coLinkAction
     */
    void startApkView(String coLinkAction, String backTime);

    /***
     * 加载HdmIn的功能
     */
    void showHdmInViewToActivity(int x, int y, int width, int height);

    void dissHdmInViewToActivity();

    //长按事件触发
    void toClickLongViewListener();

    //播放完成，回去
    void playCompanyBack();


    TextView getM11VideoErrorText();


}
