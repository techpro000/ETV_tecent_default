package com.ys.bannerlib.imageloader;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.ys.bannerlib.R;

import okhttp3.OkHttpClient;

public class FrescoImageLoader implements ImageLoader<SimpleDraweeView> {

    @Override
    public void displayImage(String imageUrl, SimpleDraweeView imageView, int position) {
        if (imageUrl == null) {
            return;
        }
        imageUrl = imageUrl.startsWith("/") ? "file://" + imageUrl : imageUrl;
        Log.e("FRESOC", "图片加载得网址： " + imageUrl);
        if (imageUrl.endsWith(".gif")) {
            DraweeController draweeController =
                    Fresco.newDraweeControllerBuilder()
                            .setUri(imageUrl)
                            .setAutoPlayAnimations(true) // 设置加载图片完成后是否直接进行播放
                            .build();
            imageView.setController(draweeController);
        } else {
            imageView.setImageURI(imageUrl);
        }
    }

    @Override
    public SimpleDraweeView createDisplayView(ViewGroup parent, int showModel) {
        initFresco(parent.getContext());
        SimpleDraweeView imageView = (SimpleDraweeView) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_draweeview, parent, false);
//        if (showModel == BannerConfig.SCREEN_SHOW_TYPE_PROPROTIONAL) { //比例缩放
//            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        } else {            //全局拉伸
//            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//        }
        return imageView;
    }

    private void initFresco(Context context) {
        if (Fresco.hasBeenInitialized()) {
            return;
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory.newBuilder(context, client)
                .build();
        Fresco.initialize(context, config);
    }
}
