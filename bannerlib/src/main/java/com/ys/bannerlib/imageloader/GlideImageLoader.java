package com.ys.bannerlib.imageloader;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.ys.bannerlib.BannerConfig;
import com.ys.bannerlib.R;

public class GlideImageLoader implements ImageLoader<ImageView> {

    @Override
    public void displayImage(String imageUrl, ImageView imageView, int position) {
        if (imageUrl == null) {
            return;
        }
        RequestManager manager = Glide.with(imageView.getContext());
        if (imageUrl.endsWith(".gif")) {
            manager.asGif()
                    .load(imageUrl)
                    .into(imageView);
        } else {
            manager.asBitmap()
                    .load(imageUrl)
                    .into(imageView);
        }
    }

    @Override
    public ImageView createDisplayView(ViewGroup parent, int showModel) {
        ImageView imageView = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_imageview, parent, false);
        if (showModel == BannerConfig.SCREEN_SHOW_TYPE_PROPROTIONAL) { //比例缩放
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {            //全局拉伸
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        return imageView;
    }

}
