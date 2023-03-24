package com.ys.bannerlib;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.ys.bannerlib.adapter.BannerImageItem;
import com.ys.bannerlib.adapter.ImageAdapter;
import com.ys.bannerlib.imageloader.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class BannerHelper<T extends BannerImageItem> {

    private final Banner<T, ImageAdapter<T>> mBanner;
    private final ImageAdapter<T> mAdapter;

    public BannerHelper(Banner<T, ImageAdapter<T>> banner) {
        this(banner, new ArrayList<>());
    }

    public BannerHelper(Banner<T, ImageAdapter<T>> banner, List<T> data) {
        this(banner, data, null);
    }

    public BannerHelper(Banner<T, ImageAdapter<T>> banner, ImageLoader<? extends ImageView> loader) {
        this(banner, null, loader);
    }

    public BannerHelper(Banner<T, ImageAdapter<T>> banner, List<T> data, ImageLoader<? extends ImageView> loader) {
        this.mBanner = banner;
        this.mAdapter = new ImageAdapter<>(data, banner);
        this.mBanner.setAdapter(mAdapter);
//        setImageLoader(loader, );
    }

    public <IV extends ImageView> void setImageLoader(ImageLoader<IV> loader, int showType) {
        mAdapter.setImageLoader(loader, showType);
    }

    public void setDatas(List<T> datas) {
        mBanner.setDatas(datas);
    }

    public void addOnPageChangeCallback(ImageAdapter.OnPageChangeCallback callback) {
        mAdapter.addOnPageChangeCallback(callback);
    }

    public void addOnPageChangePlayCallback(ImageAdapter.OnPageChangePlayCallback callback) {
        mAdapter.addOnPageChangePlayCallback(callback);
    }

    public void setOnClickListener(OnBannerListener<T> listener) {
        mBanner.setOnBannerListener(listener);
    }

    public void setOnLongClickListener(ImageAdapter.OnPageLongClickListener listener) {
        mAdapter.setLongClickListener(listener);
    }


    public void startPlay() {
        int index = getCurrentPageIndex();
        if (index < mBanner.getRealCount()) {
            T data = mAdapter.getData(getCurrentPageIndex());
            Log.e("cdl", "====loopTime====" + data.getLoopTime());
            mBanner.setLoopTime(data.getLoopTime());
        }
        mBanner.start();
    }

    public void stopPlay() {
        mBanner.stop();
        mAdapter.stop();
    }

    public void setCurrentItem(int index) {
        int count = mBanner.getRealCount();
        if (index < count) {
            mBanner.setCurrentItem(index);
        }
    }

    public void prevPage() {
        setPrevNext(-1);
    }

    public void nextPage() {
        setPrevNext(1);
    }

    private void setPrevNext(int prevNext) {
        int count = mBanner.getItemCount();
        if (count == 0) {
            return;
        }
        int next = (mBanner.getCurrentItem() + prevNext) % count;
        mBanner.setCurrentItem(next);
    }

    public void setPageTransformer(int value) {
        mAdapter.setPageTransformer(value);
    }

    public int getCurrentPageIndex() {
        return mAdapter.getCurrentIndex();
    }
}
