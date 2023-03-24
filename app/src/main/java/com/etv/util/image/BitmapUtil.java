package com.etv.util.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtil {

    Context context;

    public BitmapUtil(Context context) {
        this.context = context;
    }

    public void takeScreenShot(String imagePath, int left, int top, int width, int height, Frezzlistener listener) {
        try {
            System.out.println("===start截图 =" + System.currentTimeMillis());

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Bitmap b = Bitmap.createBitmap(bitmap, left, top, width,
                    height);

        } catch (Exception e) {
            e.printStackTrace();
            listener.ShotError("屏幕截图异常");
        }
    }


    public interface Frezzlistener {
        /**
         * 截屏异常
         */
        void ShotError(String error);
    }

}