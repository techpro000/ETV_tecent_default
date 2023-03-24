package com.etv.util.image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.http.util.ZipImageRunnable;

import com.etv.service.TcpService;
import com.etv.service.TcpSocketService;
import com.etv.util.FileUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.image.zip.Luban;
import com.etv.util.image.zip.OnCompressListener;
import com.google.android.exoplayer.C;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;


/***
 * 此雷仅仅用于 M11 截屏使用
 */
public class ImageCaptureUtil {

    Context context;

    public ImageCaptureUtil(Context context) {
        this.context = context;
    }


    public static void captureScreen( Context context,CaptureImageListener listener) {
        MyLog.update("===================开始截图==" + System.currentTimeMillis());
        FileUtil.creatPathNotExcit("截圖保存文件");
        String picPath = AppInfo.BASE_PATH_INNER + "/capture.jpg";
        File dirFile = new File(picPath);
        if (!dirFile.exists()) {
            try {
                dirFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //请求root权限
            Process su = Runtime.getRuntime().exec("su");
            //以下两句代表重启设备
            String strcmd = "/system/bin/screencap -p " + picPath;
            strcmd = strcmd + "\n exit\n";
            OutputStream os = su.getOutputStream();
            os.write(strcmd.getBytes());
            os.flush();
            os.close();
            if ((su.waitFor() != 0)) {
                throw new SecurityException();
            }
            zipImagePath(context, picPath, listener);
        } catch (Exception e) {
            MyLog.update("===截图失败==" + e.getMessage());
            listener.getCaptureImagePath(false, "");
        }

    }

    private static void zipImagePath(Context context, String picPath, CaptureImageListener listener) {
        MyLog.update("===開始壓縮==");
        ZipImageRunnable runnable = new ZipImageRunnable(context, picPath, listener);
        runnable.run();

//         int type  =  SharedPerUtil.SOCKEY_TYPE();

//  if (type== AppConfig.SOCKEY_TYPE_WEBSOCKET){
//      TcpSocketService.getInstance().getMainExecutor()
//  }
//        public static final int SOCKEY_TYPE_WEBSOCKET = 0;    //webSocket
//        public static final int SOCKEY_TYPE_SOCKET = 1;       //socket

//        TcpSocketService.getInstance().e
    }


}
