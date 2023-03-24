package com.etv.util.image;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.etv.config.AppInfo;
import com.etv.runnable.BitmapWriteLocalRunnable;
import com.etv.runnable.WriteBitmapToLocalListener;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;

import java.nio.ByteBuffer;

/***
 * M11 竖屏截图功能
 *采用屏幕录制实现该功能
 */
public class ImageScreenShotUtil {

    public static ImageScreenShotUtil instance;
    Activity context;

    public static ImageScreenShotUtil getInstance(Activity context) {
        if (instance == null) {
            synchronized (ImageScreenShotUtil.class) {
                if (instance == null) {
                    instance = new ImageScreenShotUtil(context);
                }
            }
        }
        return instance;
    }

    public ImageScreenShotUtil(Activity context) {
        this.context = context;
    }

    private MediaProjectionManager mediaProjectionManager;
    public static final int EVENT_SCREENSHOT = 22;
    private ImageReader mImageReader;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;

    public void takeScreenShot(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaProjectionManager = (MediaProjectionManager)
                    context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            context.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), EVENT_SCREENSHOT);
            MyLog.update("=====takeScreenShot====00000===");
        }
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void getImageInfoFtomIntent(int resultCode, Intent data) {
        int screenwidth = SharedPerUtil.getScreenWidth();
        int screenheight = SharedPerUtil.getScreenHeight();
        int screendensityDpi = SharedPerManager.getDensityDpi();
        MyLog.update("=====takeScreenShot====111111===" + screenwidth + " / " + screenheight + " / " + screendensityDpi);
        mImageReader = ImageReader.newInstance(screenwidth, screenheight, PixelFormat.RGBA_8888, 2);
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        virtualDisplay = mediaProjection.createVirtualDisplay("screen-mirror", screenwidth, screenheight, screendensityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);
        MyLog.update("=====takeScreenShot====111111==2222=");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            Image image = mImageReader.acquireLatestImage();
            if (image == null) {
                MyLog.update("=====takeScreenShot====111111==3333=====image==null====");
                return;
            }
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            int width = image.getWidth();
            int height = image.getHeight();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
            MyLog.update("=====takeScreenShot====222222===");
            bitmapWriteToLocalImagePath(bitmap);
        } catch (Exception e) {
            MyLog.update("=====takeScreenShot====222222===" + e.toString());
            e.printStackTrace();
        }
    }

    private void bitmapWriteToLocalImagePath(Bitmap bitmapSave) {
        String catpturePathCache = AppInfo.CAPTURE_MAIN;
        if (bitmapSave == null) {
            MyLog.update("takeScreenShot===333==bitmapSave==null");
            return;
        }
        BitmapWriteLocalRunnable runnable = new BitmapWriteLocalRunnable(bitmapSave, catpturePathCache, new WriteBitmapToLocalListener() {
            @Override
            public void writeStatues(boolean isSuccess, String path) {
                MyLog.update("takeScreenShot===333==" + isSuccess + " / " + path);
                if (!isSuccess) {
                    return;
                }
                Intent intent1 = new Intent();
                intent1.setAction(AppInfo.SEND_IMAGE_CAPTURE_SUCCESS);
                intent1.putExtra("tag", AppInfo.TAG_UPDATE);
                context.sendBroadcast(intent1);
            }
        });
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void onDestroyImageScreen() {
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader.setOnImageAvailableListener(null, null);
            mImageReader = null;
        }
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if (mediaProjection != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaProjection.stop();
            }
            mediaProjection = null;
        }
    }


}
