package com.etv.util;

import android.text.TextUtils;

import com.etv.listener.FileMd5CompaireListener;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class Md5Util {

    /***
     * 比对文件之间得 MD5
     */
    public static void compireFileMd5Info(String md5File, File fileLocal, FileMd5CompaireListener listener) {
        if (TextUtils.isEmpty(md5File)) {
            listener.fileMd5CompaireStatues(false, "File md5 is null");
            return;
        }
        if (fileLocal == null || !fileLocal.exists()) {
            listener.fileMd5CompaireStatues(false, "compaire file is not exist !");
            return;
        }
        Observable.just(md5File).map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String fileMd5) {
                        return compireFileMd5InfoBack(fileMd5, fileLocal);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {
                        listener.fileMd5CompaireStatues(aBoolean, "比对完成");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        listener.fileMd5CompaireStatues(false, throwable.toString());
                    }
                });
    }

    /***
     * 比对文件 MD5
     * @param materMd5
     * @param fileLocal
     * @return
     */
    private static Boolean compireFileMd5InfoBack(String materMd5, File fileLocal) {
        String fileLocalMd5 = getFileMD5(fileLocal);
        int compaireCode = materMd5.compareToIgnoreCase(fileLocalMd5);
        if (compaireCode == 0) {
            MyLog.task("====compireFileMd5InfoBack==校验一致==" + materMd5 + " / " + fileLocalMd5);
            return true;
        }
        MyLog.task("====compireFileMd5InfoBack==校验不同==" + materMd5 + " / " + fileLocalMd5);
        return false;
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return "";
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return bytesToHexString(digest.digest());
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return "";
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


}
