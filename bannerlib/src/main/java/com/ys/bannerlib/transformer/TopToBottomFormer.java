package com.ys.bannerlib.transformer;

import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

/***
 * 从上到下 的特效
 */
public class TopToBottomFormer implements ViewPager2.PageTransformer {

    public static TopToBottomFormer create(){
        return new TopToBottomFormer();
    }

    @Override
    public void transformPage(View view, float position) {
        view.setTranslationY(0);
        view.setScaleX(1);
        view.setScaleY(1);
        view.setRotationX(0);
        view.setRotationY(0);
        view.setRotation(0);
        view.setPivotX(0);
        view.setPivotY(0);
        view.setVisibility(View.VISIBLE);

        if (position < -1) { // [-Infinity,-1)
            // 当前页的上一页
            view.setAlpha(0);
        } else if (position <= 1) { // [-1,1]
            view.setAlpha(1);
            // 抵消默认幻灯片过渡
            view.setTranslationX(view.getWidth() * -position);
            //设置从上滑动到Y位置
            float yPosition = -position * view.getHeight();
            view.setTranslationY(yPosition);
        } else { // (1,+Infinity]
            // 当前页的下一页
            view.setAlpha(0);
        }
    }
//
//    @Override
//    public void transformPage(View view, float position) {
//        if (position < -1) { // [-Infinity,-1)
//            // 当前页的上一页
//            view.setAlpha(0);
//        } else if (position <= 1) { // [-1,1]
//            view.setAlpha(1);
//            // 抵消默认幻灯片过渡
//            view.setTranslationX(view.getWidth() * -position);
//            //设置从上滑动到Y位置
//            float yPosition = position * view.getHeight();
//            view.setTranslationY(yPosition);
//        } else { // (1,+Infinity]
//            // 当前页的下一页
//            view.setAlpha(0);
//        }
//    }

}