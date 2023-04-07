package com.etv.util;

import android.util.Log;

import com.etv.config.AppInfo;
import com.etv.entity.StorageInfo;
import com.etv.http.util.FileWriteToSdInfoRunnable;
import com.etv.listener.FileMd5CompaireListener;
import com.etv.service.EtvService;
import com.etv.task.entity.LocalEntity;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class FileUtil {

    public static final String TAG = "FileUtil";

    //删除目录或者文件
    public static boolean deleteDirOrFilePath(String Path, String tag) {
        return deleteDirOrFile(new File(Path), tag);
    }

    public static boolean deleteDirOrFile(File file, String tag) {
        MyLog.cdl("文件删除调用====" + (file.getPath()) + " / " + tag, true);
        if (file.isFile()) {
            file.delete();
            return false;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return false;
            }
            for (File f : childFile) {
                deleteDirOrFile(f, tag);
            }
            file.delete();
        }
        FileUtil.creatPathNotExcit("删除文件之后，创建一次");
        return false;
    }


    /***
     * 文件保存路径
     * @param saveFilePath
     * 下载文件路径
     * @param fileLength
     * @return
     */
    public static boolean ifFileHasExict(String saveFilePath, String fileLength) {
        fileLength = fileLength.trim();
        if (fileLength == null || fileLength.length() < 1) {
            fileLength = "1024";
        }
        File fileSave = new File(saveFilePath);
        if (!fileSave.exists()) {
            MyLog.task("=======比对数据库文件是否存在==5555=文件不存在，直接下载:" + saveFilePath);
            return false;
        }
        //文件存在去比对文件大小
        long fileDownLength = fileSave.length();
        if (fileDownLength < 1024 * 2) {
            MyLog.task("=======比对数据库文件是否存在==5555=本地=是文件太小了，直接默认fale==" + fileDownLength + " / " + saveFilePath);
            FileUtil.deleteDirOrFilePath(saveFilePath, "==比对文件，本地文件小于5 kb，删除文件重新下载====");
            return false;
        }
        long downFileLengthLong = Long.parseLong(fileLength);
        if (downFileLengthLong == fileDownLength) {
            return true;
        }
        if (downFileLengthLong > fileDownLength) {
            FileUtil.deleteDirOrFilePath(saveFilePath, "==比对文件，本地文件比服务器得文件大,直接删除，重新下载");
            return false;
        }
        return false;
    }


    public static void MKDIRSfILE(String path) {
        File file = null;
        try {
            file = new File(path);
            if (!file.exists()) {
                boolean isCreate = file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getCreateFileList() {
        List<String> listsPath = new ArrayList<String>();
        listsPath.add(AppInfo.BASE_PATH_INNER + "/etv");   //sdcard/etv
        listsPath.add(AppInfo.BASE_CRASH_LOG());           //sdcard/crashlog
        listsPath.add(AppInfo.BASE_PATH());                //sdcard/etv
        listsPath.add(AppInfo.BASE_FONT_PATH());           //sdcard/etv/font
        listsPath.add(AppInfo.BASE_APK());                 //sdcard/etv/apk
        listsPath.add(AppInfo.BASE_TASK_URL());            //sdcard/etv/task
        listsPath.add(AppInfo.BASE_CACHE());               //sdcard/etv/cache
        listsPath.add(AppInfo.BASE_IMAGE_RECEIVER());      //sdcard/etv/receiver
        listsPath.add(AppInfo.BASE_BGG_IMAGE());            //sdcard/etv/bggimg
        listsPath.add(AppInfo.APP_LOGO_PATH());             //sdcard/etv/logo
        listsPath.add(AppInfo.APP_VIDEO_PATH());            //sdcard/etv/video
        //单机模式才设定下面得路径
        int workModel = SharedPerManager.getWorkModel();
        if (workModel == AppInfo.WORK_MODEL_SINGLE) {
            //双屏任务目录
            listsPath.add(AppInfo.TASK_SINGLE_PATH());         //sdcard/etv/single
            listsPath.add(AppInfo.TASK_SINGLE_PATH() + "/main");     //sdcard/etv/single/main
            listsPath.add(AppInfo.TASK_SINGLE_PATH() + "/main/1");   //sdcard/etv/single/main/1
            listsPath.add(AppInfo.TASK_SINGLE_PATH() + "/main/2");   //sdcard/etv/single/main/2
            listsPath.add(AppInfo.TASK_SINGLE_PATH() + "/main/3");   //sdcard/etv/single/main/3
            listsPath.add(AppInfo.TASK_SINGLE_PATH() + "/main/4");   //sdcard/etv/single/main/5

            listsPath.add(AppInfo.TASK_SINGLE_PATH() + "/double");  //sdcard/etv/single/double
            listsPath.add(AppInfo.TASK_SINGLE_PATH() + "/double/1");   //sdcard/etv/single/double/1
            listsPath.add(AppInfo.TASK_SINGLE_PATH() + "/double/2");   //sdcard/etv/single/double/2
            listsPath.add(AppInfo.TASK_SINGLE_PATH() + "/double/3");   //sdcard/etv/single/double/3
            listsPath.add(AppInfo.TASK_SINGLE_PATH() + "/double/4");   //sdcard/etv/single/double/4
        }
        return listsPath;
    }

    public static void creatPathNotExcit(String printTag) {
        try {
            List<String> listsPath = getCreateFileList();
            for (int i = 0; i < listsPath.size(); i++) {
                String filePath = listsPath.get(i).toString();
                File file = new File(filePath);
//                Log.e("cdl", "====creatPathNotExcit====000==" + filePath);
                if (!file.exists()) {
                    boolean isCreate = file.mkdirs();
                    Log.e("cdl", "====creatPathNotExcit======" + isCreate + "/" + filePath);
                }
            }
        } catch (Exception e) {
            Log.e("cdl", "=====creatPathNotExcit=====" + e.toString());
            e.printStackTrace();
        }
    }

    public static String getTxtInfoFromTxtFile(String filePath) {
        return getTxtInfoFromTxtFile(filePath, TYPE_UTF_8);
    }

    /***
     * 从文本中获取txt信息
     * @param filePath
     * @type type
     * @return
     */
    public static final String TYPE_UTF_8 = "UTF-8";
    public static final String TYPE_GBK = "GBK";

    public static String getTxtInfoFromTxtFile(String filePath, String type) {
        String result = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return result;
            }
            int length = (int) file.length();
            byte[] buff = new byte[length];
            FileInputStream fin = new FileInputStream(file);
            fin.read(buff);
            fin.close();
            result = new String(buff, type);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /***
     * 获取需要处理的文件
     * 外置存储的类型
     * SD 还是USB
     * @return
     */
    public static StorageInfo getStorageFileAction(String path) {
        StorageInfo storageInfo = null;
        try {
            if (path.contains(AppInfo.IP_FILE_NAME)) {
                storageInfo = new StorageInfo(path, StorageInfo.ACTION_MODIFY_IP);
                return storageInfo;
            } else if (path.contains(".apk")) {  //模糊匹配APK
                String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                if (fileName.startsWith("ETV") || fileName.startsWith("etv")) {
                    storageInfo = new StorageInfo(path, StorageInfo.ACTION_INSTALL_ETV_APK);
                    return storageInfo;
                }
            } else if (path.contains(AppInfo.DISONLINE_TASK_DIR_ZIP)) {
                storageInfo = new StorageInfo(path, StorageInfo.ACTION_TASK_DISONLINE);
                return storageInfo;
            } else if (path.contains(AppInfo.SINGLE_TASK_DIR_ZIP)) { //单机版本任务
                storageInfo = new StorageInfo(path, StorageInfo.ACTION_TASK_SINGLE);
                return storageInfo;
            } else if (path.contains(AppInfo.VOICE_MEDIA)) { //触沃-语音替换文件
                storageInfo = new StorageInfo(path, StorageInfo.ACTION_VOICE_MEDIA);
                return storageInfo;
            }
        } catch (Exception e) {
            MyLog.cdl("获取文件路径error==" + e.toString());
        }
        return storageInfo;
    }

    public static String getPrintSize(long size) {
        if (size < 1024) {
            return String.valueOf(size) + "B";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            return String.valueOf(size) + "KB";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            size = size * 100;
            return String.valueOf((size / 100)) + "."
                    + String.valueOf((size % 100)) + "MB";
        } else {
            size = size * 100 / 1024;
            return String.valueOf((size / 100)) + "."
                    + String.valueOf((size % 100)) + "GB";
        }
    }

    public static List<LocalEntity> getFiles() {
        List<LocalEntity> entityList = new ArrayList<>();
        File file = new File(AppInfo.BASE_TASK_URL());
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

    private static long formetFileSize(File file) {
        long fileLength = 0;
        if (file == null) {
            return fileLength;
        }
        fileLength = file.length();
        return fileLength;
    }


    /**
     * 删除单个文件
     *
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
                MyLog.d("liujk", filePathName + "失败！");
                return false;
            }
        } else {
            MyLog.d("liujk", "删除单个文件失败：" + filePathName + "不存在！");
            return false;
        }
    }

    /**
     * 将信息写入SD卡
     *
     * @param registerPath
     * @param wirteJson
     */
    public static void writeMessageInfoToTxt(String registerPath, String wirteJson) {
        FileWriteToSdInfoRunnable runnable = new FileWriteToSdInfoRunnable(wirteJson, registerPath);
        EtvService.getInstance().executor(runnable);
    }


}
