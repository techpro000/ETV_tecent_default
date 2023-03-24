//package com.etv.view.image;
//
//import android.content.Context;
//import android.os.Handler;
//import android.os.Message;
//import android.util.AttributeSet;
//import android.view.View;
//import android.widget.RelativeLayout;
//
//import com.etv.util.MyLog;
//import com.etv.view.progress.RopeProgressBar;
//import com.ys.etv.R;
//
///**
// * Created by liuheng on 2015/6/21.
// * <p></p>
// * 如有任何意见和建议可邮件  bmme@vip.qq.com
// */
//public class PhotoViewWithProgress extends RelativeLayout {
//
//
//    Context context;
//
//    public PhotoViewWithProgress(Context context) {
//        this(context, null);
//    }
//
//    public PhotoViewWithProgress(Context context, AttributeSet attrs) {
//        this(context, null, 0);
//    }
//
//    public PhotoViewWithProgress(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        this.context = context;
//        View view = View.inflate(context, R.layout.view_img_progress, null);
//        initView(view);
//        addView(view);
//    }
//
//    PhotoView iv_show;
//    RopeProgressBar progress_load;
//
//    private void initView(View view) {
//        iv_show = (PhotoView) view.findViewById(R.id.iv_show);
//        progress_load = (RopeProgressBar) view.findViewById(R.id.progress_load);
//    }
//
//
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == 1) {
//                int bytesRead = msg.arg1;
//                int contentLength = msg.arg2;
//                int percent = bytesRead * 100 / contentLength;
//                MyLog.e("IMAGE", "=====加载进度===" + percent);
//                progress_load.setProgress(percent);
//            }
//        }
//    };
//
//}
