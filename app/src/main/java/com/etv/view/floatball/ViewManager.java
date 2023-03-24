//package com.etv.view.floatball;
//
//import android.content.Context;
//import android.graphics.PixelFormat;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.RelativeLayout;
//
//import com.etv.util.SharedPerManager;
//import com.etv.view.PointBackView;
//import com.ys.etv.R;
//
//public class ViewManager {
//
//    WindowManager windowManager;
//    public static ViewManager instance;
//    Context context;
//    private WindowManager.LayoutParams floatBallParams;
//    private static int view_size_width = 50;
//    private static int view_size_height = 60;
//
//    private ViewManager(Context context) {
//        this.context = context;
//    }
//
//    public static ViewManager getInstance(Context context) {
//        if (instance == null) {
//            instance = new ViewManager(context);
//        }
//        return instance;
//    }
//
//    public void showFloatBall(BackButtonListener listenerInfo) {
//        this.listener = listenerInfo;
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
////        floatBallParams.gravity = Gravity.TOP | Gravity.LEFT;
//        floatBallParams.gravity = Gravity.CENTER | Gravity.LEFT;
//        floatBallParams.type = WindowManager.LayoutParams.TYPE_PHONE;  //TYPE_TOAST
//        floatBallParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
//        floatBallParams.format = PixelFormat.RGBA_8888;
//        viewFloat = LayoutInflater.from(context).inflate(R.layout.view_float_down, null);
//        rela_float = (RelativeLayout) viewFloat.findViewById(R.id.rela_float);
//
//        view_point_touch = (PointBackView) viewFloat.findViewById(R.id.view_point_touch);
//        view_point_touch.setViewClickListener(new PointBackView.ViewClickListener() {
//            @Override
//            public void clickCloseView() {
//                if (listener == null) {
//                    return;
//                }
//                dissFloatBall();
//                listener.clickBackButton();
//            }
//        });
//
//        windowManager.addView(viewFloat, floatBallParams);
//        initlistener();
//    }
//
//    View viewFloat;
//    RelativeLayout rela_float;
//    PointBackView view_point_touch;
////    RelativeLayout rela_float_bgg;
//
//    public void dissFloatBall() {
//        try {
//            if (windowManager == null) {
//                return;
//            }
//            windowManager.removeView(viewFloat);
//            windowManager = null;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void initlistener() {
////        rela_float.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                if (listener == null) {
////                    return;
////                }
////                dissFloatBall();
////                listener.clickBackButton();
////            }
////        });
//    }
//
//
//    public int getScreenWidth() {
//        return SharedPerManager.getScreenWidth();
//    }
//
//
//    BackButtonListener listener;
//
//
//    public interface BackButtonListener {
//        void clickBackButton();
//    }
//
//}
