package com.ys.bannerlib.imageloader;

import android.view.ViewGroup;
import android.widget.ImageView;

public interface ImageLoader<T extends ImageView> {
    void displayImage(String imageUrl, T imageView, int position);

    T createDisplayView(ViewGroup parent, int showModel);
}
