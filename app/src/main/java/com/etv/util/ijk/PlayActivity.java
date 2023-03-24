//package com.etv.util.ijk;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.shuyu.gsyvideoplayer.GSYVideoManager;
//import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
//import com.ys.etv.R;
//
///**
// * 单独的视频播放页面
// * Created by shuyu on 2016/11/11.
// */
//public class PlayActivity extends AppCompatActivity {
//
//    OrientationUtils orientationUtils;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.activity_play_ijk);
//        init();
//        initListener();
//    }
//
//    private void initListener() {
//        btn_play = (Button) findViewById(R.id.btn_play);
//        btn_play.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startToPlay();
//            }
//        });
//
//        btn_finish = (Button) findViewById(R.id.btn_finish);
//        btn_finish.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//        btn_roate_mirror = (Button) findViewById(R.id.btn_roate_mirror);
//        btn_roate_mirror.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                video_player.setScreenRoateMirror();
//            }
//        });
//
//        btn_roate_screen = (Button) findViewById(R.id.btn_roate_screen);
//        btn_roate_screen.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                video_player.setScreenRoateInfo();
//            }
//        });
//
//        btn_play_size = (Button) findViewById(R.id.btn_play_size);
//        btn_play_size.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                video_player.setPlayScreenSizeChange();
//            }
//        });
//    }
//
//    SampleVideo video_player;
//    Button btn_play, btn_finish, btn_roate_mirror, btn_roate_screen, btn_play_size;
//    TextView tv_play_statues;
//
//    private void init() {
//        tv_play_statues = (TextView) findViewById(R.id.tv_play_statues);
//
//        video_player = (SampleVideo) findViewById(R.id.video_player);
//        video_player.setPlayVideoListener(new IJKPlayerListener() {
//
//            @Override
//            public void onAutoCompletion() {
//                startToPlay();
//            }
//
//            @Override
//            public void onError(int what, int extra) {
//
//            }
//        });
//        //设置旋转
//        orientationUtils = new OrientationUtils(this, video_player);
//        //是否可以滑动调整
//        video_player.setIsTouchWiget(true);
//
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                while (true) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            tv_play_statues.setText(video_player.getCurrentPositionWhenPlaying() + " / " + video_player.getDuration());
//                        }
//                    });
//                }
//            }
//        }.start();
//
//    }
//
//    private void startToPlay() {
//        String source1 = "/sdcard/etv/line/ddd.mp4";
//        video_player.setUpUrl(source1);
//        video_player.startPlayLogic();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        video_player.onVideoPause();
//
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        video_player.onVideoResume();
//    }
//
//    @Override
//    public void onBackPressed() {
//        video_player.setVideoAllCallBack(null);
//        GSYVideoManager.releaseAllVideos();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (orientationUtils != null)
//            orientationUtils.releaseListener();
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.e("cdl", "====onKeyDown=======" + keyCode);
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Log.e("cdl", "====onKeyDown======finid=");
//            finish();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//}
