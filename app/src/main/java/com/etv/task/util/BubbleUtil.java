package com.etv.task.util;

import com.etv.entity.StatisticsEntity;
import com.etv.entity.TimeComparEntity;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jsjm on 2018/10/25.
 */

public class BubbleUtil {


    /***
     * 根据时间先后
     * @param fileList
     * param  isOrder
     * true  正序
     * false 倒叙
     * @return
     */
    public static List<TimeComparEntity> sortByTime(List<TimeComparEntity> fileList, boolean isOrder) {
        Collections.sort(fileList, new Comparator<TimeComparEntity>() {
            @Override
            public int compare(TimeComparEntity statisticsEntity1, TimeComparEntity statisticsEntity2) {
                long offTime1 = statisticsEntity1.getPowerOffTime();
                long offTime2 = statisticsEntity2.getPowerOffTime();
                if (isOrder) {
                    return (int) (offTime1 - offTime2);
                } else {
                    return (int) (offTime2 - offTime1);
                }

            }
        });
        return fileList;
    }


    /***
     * 根据文件名字进行排序
     * @param fileList
     * param  isOrder
     * true  正序
     * false 倒叙
     * @return
     */
    public static List<File> sortByFileName(List<File> fileList, boolean isOrder) {
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (isOrder) {
                    if (o2.isDirectory() && o1.isFile()) {
                        return -1;
                    }
                    if (o2.isFile() && o1.isDirectory()) {
                        return 1;
                    }
                    return o1.getName().compareTo(o2.getName());
                } else {
                    if (o1.isDirectory() && o2.isFile()) {
                        return -1;
                    }
                    if (o1.isFile() && o2.isDirectory()) {
                        return 1;
                    }
                    return o2.getName().compareTo(o1.getName());
                }
            }
        });
        return fileList;
    }


    /***
     * 冒泡算法排序
     * @param arr
     * @return
     * 返回从小到大排序
     */
    public static long[] Bubblesort(long[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    long temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
        return arr;
    }

}
