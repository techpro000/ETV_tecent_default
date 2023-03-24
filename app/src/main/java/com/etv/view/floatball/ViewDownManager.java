//package com.etv.view.floatball;
//
//import android.content.Context;
//import android.graphics.PixelFormat;
//import android.os.Build;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.etv.util.SharedPerManager;
//import com.ys.etv.R;
//
//public class ViewDownManager {
//
//    WindowManager windowManager;
//    public static ViewDownManager instance;
//    Context context;
//    private WindowManager.LayoutParams floatBallParams;
//    private static int view_size_width = 150;
//    private static int view_size_height = 50;
//
//    public ViewDownManager(Context context) {
//        this.context = context;
//        initFloatBall();
//    }
//
//    View viewFloat;
//    LinearLayout lin_down_tag;
//    TextView tv_down_desc;
//
//    private void initFloatBall() {
//        if (windowManager == null) {
//            windowManager = null;
//        }
//        if (floatBallParams == null) {
//            floatBallParams = null;
//        }
//        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        floatBallParams = new WindowManager.LayoutParams();
//        floatBallParams.width = view_size_width;
//        floatBallParams.height = view_size_height;
////        floatBallParams.gravity = Gravity.CENTER | Gravity.LEFT;
//        floatBallParams.gravity = Gravity.LEFT;
//        floatBallParams.y = 200;
//        //================
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            //Android 8.0
//            floatBallParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        } else {
//            //其他版本
//            floatBallParams.type = WindowManager.LayoutParams.TYPE_PHONE;  //TYPE_TOAST
//        }
////      floatBallParams.type = WindowManager.LayoutParams.TYPE_PHONE;  //TYPE_TOAST
//        floatBallParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
//        floatBallParams.format = PixelFormat.RGBA_8888;
//        viewFloat = LayoutInflater.from(context).inflate(R.layout.view_float_down, null);
//        windowManager.addView(viewFloat, floatBallParams);
//        lin_down_tag = (LinearLayout) viewFloat.findViewById(R.id.lin_down_tag);
//        tv_down_desc = (TextView) viewFloat.findViewById(R.id.tv_down_desc);
//    }
//
//    /**
//     * 更新状态
//     *
//     * @param desc
//     */
//    public void updateDesc(String desc) {
//        boolean isShowView = isViewShow();
//        if (!isShowView) {
//            return;
//        }
//        if (windowManager == null) {
//            return;
//        }
//        if (tv_down_desc == null) {
//            return;
//        }
//        tv_down_desc.setText(desc);
//    }
//
//    /**
//     * 判断Views是否在现实
//     *
//     * @return
//     */
//    public boolean isViewShow() {
//        if (windowManager == null) {
//            return false;
//        }
//        if (lin_down_tag == null) {
//            return false;
//        }
//        int statues = lin_down_tag.getVisibility();
//        if (statues == View.VISIBLE) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public void showFloatView() {
//        if (windowManager == null) {
//            return;
//        }
//        if (lin_down_tag == null) {
//            return;
//        }
//        lin_down_tag.setVisibility(View.VISIBLE);
//    }
//
//    public void dissFloatBall() {
//        try {
//            if (windowManager == null) {
//                return;
//            }
//            if (lin_down_tag == null) {
//                return;
//            }
//            lin_down_tag.setVisibility(View.GONE);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void onDestoryBall() {
//        try {
//            if (windowManager == null) {
//                return;
//            }
//            lin_down_tag = null;
//            windowManager.removeView(viewFloat);
//            windowManager = null;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//}
