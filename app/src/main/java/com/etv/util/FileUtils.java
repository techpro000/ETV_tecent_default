package com.etv.util;

import android.util.Log;

import com.etv.task.entity.LocalEntity;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static String SAVE_PATH = "/storage/emulated/0/etv/task/";

    /**
     * 根据文件路径来获取文件的名称，文件大小组装LocalEntity
     * @return
     */
    public static List<LocalEntity> getFiles(){
        List<LocalEntity> entityList = new ArrayList<>();
        File file = new File(SAVE_PATH);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File childFile = files[i];
            String childName = childFile.getName();
            long fileSize = formetFileSize(childFile);
            MyLog.d("liujk", "文件名称： " + childName + " 文件大小： " + fileSize);
            LocalEntity entity = new LocalEntity(childName, String.valueOf(fileSize));
            entityList.add(entity);
        }

        return entityList;

    }

    private static long formetFileSize(File file){
        long fileLength = 0;
        if (file == null){
            return fileLength;
        }
        fileLength = file.length();
        return fileLength;
    }




    /** 删除单个文件
     * @param filePathName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteSingleFile(String filePathName) {
        File file = new File(filePathName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                MyLog.d("liujk", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePathName + "成功！");
                return true;
            } else {
                MyLog.d("liujk",  filePathName + "失败！");
                return false;
            }
        } else {
            MyLog.d("liujk","删除单个文件失败：" + filePathName + "不存在！");
            return false;
        }
    }

}
