package com.ys.bannerlib.transformer;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

/***
 * 随机特效
 */
public class RandomTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_ALPHA = 0.0f;    //最小透明度

    public static RandomTransformer create(){
        return new RandomTransformer();
    }

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();    //得到view宽
        view.setTranslationY(0);
        view.setScaleX(1);
        view.setScaleY(1);
        view.setRotationX(0);
        view.setRotationY(0);
        view.setRotation(0);
        view.setPivotX(0);
        view.setPivotY(0);
        view.setTranslationY(0);
        view.setVisibility(View.VISIBLE);


        if (position < -1) { // [-Infinity,-1)
            view.setAlpha(0);

        } else if (position <= 1) { // [-1,1]
            if (position < 0) {
                //消失的页面
                view.setTranslationX(-pageWidth * position);  //阻止消失页面的滑动
            } else {
                //出现的页面
                view.setTranslationX(pageWidth);        //直接设置出现的页面到底
                view.setTranslationX(-pageWidth * position);  //阻止出现页面的滑动
            }
            float alphaFactor = Math.max(MIN_ALPHA, 1 - Math.abs(position));
            view.setAlpha(alphaFactor);
        } else { // (1,+Infinity]
            view.setAlpha(0);
        }
    }

    int nowPostion = 0; //当前页面
    Context context;
    ArrayList<Fragment> fragments;

    public void setCurrentItem(Context context, int nowPostion, ArrayList<Fragment> fragments) {
        this.nowPostion = nowPostion;
        this.context = context;
        this.fragments = fragments;
    }

    public void setCurrentItem(int nowPostion) {
        this.nowPostion = nowPostion;
    }

}