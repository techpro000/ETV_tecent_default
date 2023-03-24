package com.ys.bannerlib.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerAdapter;
import com.ys.bannerlib.PageChangeListener;
import com.ys.bannerlib.PageTransformerUtils;
import com.ys.bannerlib.imageloader.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ImageAdapter<T extends BannerImageItem> extends BannerAdapter<T, ImageViewHolder> {

    private int mPageTransformerValue = 1;
    private int mCurrentPageTransformerValue;
    private int mCurrentIndex;
    private final Banner mBanner;
    private ImageLoader<? extends ImageView> mImageLoader;

    private List<DelayRunnable> mDelayRunList;
    private OnPageLongClickListener mLongClickListener;
    private final List<OnPageChangeCallback> mOnPageChangeCallback = new ArrayList<>();
    private final List<OnPageChangePlayCallback> mOnPageChangePlayCallback = new ArrayList<>();
    int showType;

    public ImageAdapter(List<T> datas, Banner banner) {
        super(datas);
        this.mBanner = banner;
        initBanner();
    }

    public ImageAdapter(List<T> datas, Banner banner, ImageLoader<? extends ImageView> loader) {
        super(datas);
        this.mBanner = banner;
        this.mImageLoader = loader;
        initBanner();
    }

    public <IV extends ImageView> void setImageLoader(ImageLoader<IV> loader, int showType) {
        this.showType = showType;
        this.mImageLoader = loader;
        notifyDataSetChanged();
    }

    @Override
    public ImageViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (mImageLoader != null) {
            itemView = mImageLoader.createDisplayView(parent, showType);
        }
        return new ImageViewHolder(itemView);
    }


    @Override
    public void onBindView(ImageViewHolder holder, T data, int position, int size) {
        holder.itemView.setOnLongClickListener(v -> {
            if (mLongClickListener != null) {
                return mLongClickListener.onLongClick(v, position);
            }
            return false;
        });
        if (mImageLoader != null) {
            mImageLoader.displayImage(data.getImageUrl(), holder.getImageView(), position);
        }
    }

    public void setPageTransformer(int value) {
        mPageTransformerValue = value;
//        Log.e("banner", "===mPageTransformerValue==setPageTransformer=" + mPageTransformerValue);
    }

    private void initBanner() {
        if (mBanner == null) {
            return;
        }
        mDelayRunList = new ArrayList<>();
        mBanner.addOnPageChangeListener(new PageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentIndex = position;
                BannerImageItem data = getData(position);
                boolean isLast = position == getRealCount() - 1;
                for (OnPageChangeCallback callback : mOnPageChangeCallback) {
                    callback.onPageChange(position, isLast);
                    //mBanner.postDelayed(()-> callback.onPageChange(position, position == getRealCount() - 1), data.getLoopTime());
                }
                for (Runnable run : mDelayRunList) {
                    run.run();
                }
                mDelayRunList.clear();
                for (OnPageChangePlayCallback callback : mOnPageChangePlayCallback) {
                    DelayRunnable delay = new DelayRunnable(position, isLast, callback);
                    mDelayRunList.add(delay);
                    mBanner.postDelayed(delay, data.getLoopTime());
                }
//                Log.e("banner", "===mPageTransformerValue===" + mPageTransformerValue);
                if (!ImageAdapter.this.onPageSelected(position)) {
                    int formerValue = mPageTransformerValue;
                    if (formerValue == 1) {
                        formerValue = new Random().nextInt(22);
                    }
                    mBanner.setPageTransformer(PageTransformerUtils.getTransformer(formerValue));
                    if (data.getLoopTime() > 0) {
                        mBanner.setLoopTime(data.getLoopTime());
                        mBanner.start();
                    }
                }
            }
        });
    }

    public void setLongClickListener(OnPageLongClickListener listener) {
        this.mLongClickListener = listener;
    }

    public void addOnPageChangeCallback(OnPageChangeCallback callback) {
        if (callback != null) {
            mOnPageChangeCallback.add(callback);
        }
    }

    public void addOnPageChangePlayCallback(OnPageChangePlayCallback callback) {
        if (callback != null) {
            mOnPageChangePlayCallback.add(callback);
        }
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public boolean onPageSelected(int position) {
        return false;
    }

    public interface OnPageChangeCallback {
        void onPageChange(int position, boolean isLast);
    }

    public interface OnPageChangePlayCallback {
        void onPageChange(int position, boolean isLast);
    }

    public interface OnPageLongClickListener {
        boolean onLongClick(View v, int position);
    }

    public void stop() {
        for (Runnable run : mDelayRunList) {
            mBanner.removeCallbacks(run);
        }
        mDelayRunList.clear();
    }

    static class DelayRunnable implements Runnable {
        private int position;
        private boolean isLast;
        private boolean isRun;
        private OnPageChangePlayCallback callback;

        public DelayRunnable(int position, boolean isLast, OnPageChangePlayCallback callback) {
            this.position = position;
            this.isLast = isLast;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (!isRun) {
                isRun = true;
                callback.onPageChange(position, isLast);
            }
        }
    }

}
