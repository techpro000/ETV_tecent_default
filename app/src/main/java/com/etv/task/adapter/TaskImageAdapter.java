package com.etv.task.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.etv.util.MyLog;
import com.etv.view.image.PhotoView;

import java.util.List;

import com.ys.bannerlib.util.GlideImageUtil;
import com.ys.etv.R;

public class TaskImageAdapter extends PagerAdapter {

    Context context;
    List<String> images;


    public TaskImageAdapter(Context context, List<String> images) {
        MyLog.d("CDL", "===========images=====" + images.size());
        this.context = context;
        this.images = images;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        MyLog.d("CDL", "===========替换一张图片=====");
        PhotoView view = new PhotoView(context);
        view.enable();
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        GlideImageUtil.loadImageDefaultId(context, images.get(position), view, R.mipmap.icon_default_image);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        MyLog.d("CDL", "===========销毁一张图片=====");
        container.removeView((View) object);

    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}
