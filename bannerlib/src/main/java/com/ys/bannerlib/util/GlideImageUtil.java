package com.ys.bannerlib.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class GlideImageUtil {

    public static void loadImageByPath(Context context, String backFilePath, ImageView iv_back_bgg) {
        if (context == null) {
            return;
        }
        try {
            Glide.with(context)
                    .load(backFilePath)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(iv_back_bgg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadImageById(Context context, int imageId, ImageView iv_back_bgg) {
        if (context == null) {
            return;
        }
        try {
            Glide.with(context).load(imageId).into(iv_back_bgg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void clearViewCache(Context context, ImageView iv_back_bgg) {
        if (iv_back_bgg == null) {
            return;
        }
        if (context == null) {
            return;
        }
        try {
        } catch (Exception e) {
            Glide.with(context).clear(iv_back_bgg);
            e.printStackTrace();
        }

    }

    public static void loadImageCacheDisk(Context context, String logoPath, ImageView logo_image) {
        if (context == null) {
            return;
        }
        try {
            Glide.with(context)
                    .load(logoPath)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .fitCenter()
                    .into(logo_image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadImageNoCache(Context context, String imagePath, ImageView iv_qr_code_scan) {
        if (context == null) {
            return;
        }
        try {
            Glide.with(context)
                    .load(imagePath)
                    .skipMemoryCache(true)                      //禁止Glide内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE)  //不缓存资源
                    .into(iv_qr_code_scan);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /***
     * 加载图片，提供占位符
     * @param context
     * @param imagePath
     * @param iv_main_bgg
     * @param defaultImage
     */
    public static void loadImageDefaultId(Context context, String imagePath, ImageView iv_main_bgg, int defaultImage) {
        if (context == null) {
            return;
        }
        try {
            Glide.with(context)
                    .load(imagePath)
                    .thumbnail(0.1f)
                    .error(defaultImage)
                    .placeholder(defaultImage)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .fitCenter()
                    .into(iv_main_bgg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


//package com.ys.bannerlib.util;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.widget.ImageView;
//
//import com.bumptech.glide.Glide;


//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//
//import java.io.ByteArrayOutputStream;
//
//public class GlideImageUtil {
//
//    public static void loadImageByPath(Context context, String backFilePath, ImageView iv_back_bgg) {
//        Glide.with(context).load(backFilePath).into(iv_back_bgg);
//    }
//
//    public static void loadImageById(Context context, int imageId, ImageView iv_back_bgg) {
//        Glide.with(context).load(imageId).into(iv_back_bgg);
//    }
//
//    public static void clearViewCache(Context context,ImageView iv_back_bgg) {
//        Glide.with(context).clear(iv_back_bgg);
//    }
//
//    public static void loadImageCacheDisk(Context context, String logoPath, ImageView logo_image) {
//        Glide.with(context)
//                .load(logoPath)
//                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//                .fitCenter()
//                .into(logo_image);
//    }
//
//    public static void loadImageNoCache(Context context, String imagePath, ImageView iv_qr_code_scan) {
//        Glide.with(context)
//                .load(imagePath)
//                .skipMemoryCache(true)                      //禁止Glide内存缓存
//                .diskCacheStrategy(DiskCacheStrategy.NONE)  //不缓存资源
//                .into(iv_qr_code_scan);
//    }
//
//
//    /***
//     * 加载图片，提供占位符
//     * @param context
//     * @param imagePath
//     * @param iv_main_bgg
//     * @param defaultImage
//     */
//    public static void loadImageDefaultId(Context context, String imagePath, ImageView iv_main_bgg, int defaultImage) {
//        Glide.with(context)
//                .load(imagePath)
//                .thumbnail(0.1f)
//                .error(defaultImage)
//                .placeholder(defaultImage)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .skipMemoryCache(true)
//                .fitCenter()
//                .into(iv_main_bgg);
//    }
//
//    public static void loadBitmap(Context context, Bitmap bitmap, ImageView view) {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//        byte[] bytes = byteArrayOutputStream.toByteArray();
//        Glide.with(context)
//                .load(bytes)
//                .into(view);
//    }
//}
