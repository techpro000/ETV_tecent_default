package com.etv.listener;

import com.etv.util.poweronoff.entity.TimerDbEntity;

import java.util.List;

public interface TaskChangeListener {

    /***
     * 网络请求成功
     * 返回的实体类
     * 请求的错误信息
     */
    void taskRequestSuccess(boolean isTrue, List<TimerDbEntity> timeList, String errorrDesc);
}
