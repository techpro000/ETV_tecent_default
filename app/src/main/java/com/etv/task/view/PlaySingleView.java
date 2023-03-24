package com.etv.task.view;


import android.widget.AbsoluteLayout;

public interface PlaySingleView {

    /**
     * 重新加载资源
     */
    void retryLoadSource();

    void showWaitDialog(boolean isShow);

    void notResourceTip(String descError);

    AbsoluteLayout getAbsoluLayout();

    void finishView();

    //长按事件触发
    void toClickLongViewListener();

}
