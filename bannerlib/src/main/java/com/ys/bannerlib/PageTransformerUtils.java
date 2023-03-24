package com.ys.bannerlib;

import androidx.viewpager2.widget.ViewPager2;

import com.ys.bannerlib.transformer.AccordionTransformer;
import com.ys.bannerlib.transformer.AlphaChangeformer;
import com.ys.bannerlib.transformer.BackgroundToForegroundTransformer;
import com.ys.bannerlib.transformer.BottomToTopFormer;
import com.ys.bannerlib.transformer.CubeInTransformer;
import com.ys.bannerlib.transformer.CubeOutTransformer;
import com.ys.bannerlib.transformer.DefaultTransformer;
import com.ys.bannerlib.transformer.DepthPageTransformer;
import com.ys.bannerlib.transformer.FlipHorizontalTransformer;
import com.ys.bannerlib.transformer.FlipVerticalTransformer;
import com.ys.bannerlib.transformer.ForegroundToBackgroundTransformer;
import com.ys.bannerlib.transformer.LeftToRightFormer;
import com.ys.bannerlib.transformer.NGGuidePageTransformer;
import com.ys.bannerlib.transformer.RandomTransformer;
import com.ys.bannerlib.transformer.RotateDownTransformer;
import com.ys.bannerlib.transformer.RotateUpTransformer;
import com.ys.bannerlib.transformer.ScaleInOutTransformer;
import com.ys.bannerlib.transformer.TabletTransformer;
import com.ys.bannerlib.transformer.TopToBottomFormer;
import com.ys.bannerlib.transformer.ZoomInTransformer;
import com.ys.bannerlib.transformer.ZoomOutSlideTransformer;
import com.ys.bannerlib.transformer.ZoomOutTranformer;

public class PageTransformerUtils {

    public static ViewPager2.PageTransformer getTransformer(int index) {
        switch (index) {
            case 1:  //随机动画效果
                return RandomTransformer.create();
            case 2:  //左到右
                return DefaultTransformer.create();
            case 3:  //左到右
                return LeftToRightFormer.create();
            case 4:  //上到下
                return TopToBottomFormer.create();
            case 5:  //下到上
                return BottomToTopFormer.create();
            case 6:  //手风琴挤压
                return AccordionTransformer.create();
            case 7: //放大渐变
                return BackgroundToForegroundTransformer.create();
            case 8: //缩小渐变
                return ForegroundToBackgroundTransformer.create();
            case 9:  //3d旋转进
                return CubeInTransformer.create();
            case 10://3d旋转出
                return CubeOutTransformer.create();
            case 11:  //景深渐变
                return DepthPageTransformer.create();
            case 12:  //左右旋转
                return FlipHorizontalTransformer.create();
            case 13:  //上下旋转
                return FlipVerticalTransformer.create();
            case 14:  //逆时针旋转
                return RotateDownTransformer.create();
            case 15:   //顺时针旋转
                return RotateUpTransformer.create();
            case 16: //上下缩放
                return ScaleInOutTransformer.create();
            case 17: //3D画框效果
                return TabletTransformer.create();
            case 18:  //缩放渐变
                return ZoomInTransformer.create();
            case 19:  //放大消隐动画
                return ZoomOutTranformer.create();
            case 20:   //透明渐变
                return ZoomOutSlideTransformer.create();
            case 21:  //渐变缩放
                return AlphaChangeformer.create();
            case 22:
                return NGGuidePageTransformer.create();
            default: //右到左
                return DefaultTransformer.create();
        }
    }
}
