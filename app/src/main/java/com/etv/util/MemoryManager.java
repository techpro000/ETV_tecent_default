package com.etv.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import com.etv.entity.CacheSizeEntity;

import java.util.List;

public class MemoryManager {

    /**
     * 获取内存状态
     *
     * @param context
     * @return
     */
    public static CacheSizeEntity getCurrentCacheSize(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //最大分配内存
        int memory = activityManager.getMemoryClass();
        System.out.println("memory: " + memory);
        //最大分配内存获取方法2
        float maxMemory = (float) (Runtime.getRuntime().maxMemory() * 1.0 / (1024 * 1024));
        //当前分配的总内存
        float totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0 / (1024 * 1024));
        //剩余内存
        float freeMemory = (float) (Runtime.getRuntime().freeMemory() * 1.0 / (1024 * 1024));
//        MyLog.cdl("maxMemory: " + maxMemory + " \ntotalMemory: " + totalMemory + "\nfreeMemory: " + freeMemory);
        CacheSizeEntity cacheSizeEntity = new CacheSizeEntity(maxMemory, totalMemory, freeMemory);
        return cacheSizeEntity;
    }
}
