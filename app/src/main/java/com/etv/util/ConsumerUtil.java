package com.etv.util;

import com.etv.config.AppConfig;

public class ConsumerUtil {

    /**
     * 根据客户删除数据库信息
     *
     * @return true  删除
     * false  不删除
     */
    public static boolean DelAllTaskDbByConsumer() {
        boolean isDelDbTask = true;
        switch (AppConfig.APP_TYPE) {
            default:
                isDelDbTask = true;
                break;
        }
        return isDelDbTask;
    }

}
