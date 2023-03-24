package com.etv.task.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.etv.config.AppInfo;
import com.etv.task.adapter.TaskImageAdapter;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.ys.etv.R;

import java.util.ArrayList;
import java.util.List;

import cn.hzw.doodle.DoodleActivity;
import cn.hzw.doodle.DoodleParams;
import cn.hzw.doodle.DoodleView;


/***
 * 图片预览界面
 */
public class TaskImageActivity extends TaskActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private TaskImageAdapter adapter;
    public static final String TAG_RECEIVE_MESSAGE = "TAG_RECEIVE_MESSAGE";    //接受得图片集合列表

    @Override
    public void showDownStatuesView(boolean isShow, String desc) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_image);
        initView();
    }

    List<String> images = new ArrayList<String>();
    LinearLayout iv_no_data;
    LinearLayout lin_exit;
    TextView tv_exit;
    Button btn_paint;
    int receivePosition = 0;

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        iv_no_data = (LinearLayout) findViewById(R.id.iv_no_data);
        Intent intent = getIntent();
        images = intent.getStringArrayListExtra(TAG_RECEIVE_MESSAGE);
        if (images == null || images.size() < 1) {
            MyLog.cdl("======图片得数量  00===");
            iv_no_data.setVisibility(View.VISIBLE);
            return;
        }
        receivePosition = intent.getIntExtra("clickPositon", 0);
        //        获取照片的路径，根据位置不对
//        receivePosition = getCurrentPosition(receivePosition);
        current_image_url = images.get(receivePosition);
        iv_no_data.setVisibility(View.GONE);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new TaskImageAdapter(TaskImageActivity.this, images);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCurrentItem(receivePosition);
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        tv_exit = (TextView) findViewById(R.id.tv_exit);
        lin_exit.setOnClickListener(this);
        tv_exit.setOnClickListener(this);
        btn_paint = (Button) findViewById(R.id.btn_paint);
        btn_paint.setOnClickListener(this);
        boolean isShowPaincIcon = SharedPerManager.isShowPaintIcon();
        if (isShowPaincIcon) {
            btn_paint.setVisibility(View.VISIBLE);
        }
    }

    private int getCurrentPosition(int backDefault) {
        Log.e("position", "=======数据计算====000===" + backDefault);
        if (backDefault == 0) {
            backDefault = images.size() - 1;
        } else if (backDefault == images.size() + 1) {
            backDefault = 0;
        } else {
            backDefault = backDefault - 1;
        }
        Log.e("position", "========数据计算====111==" + backDefault);
        return backDefault;
    }

    private int getRealPosition(String receiveFileUrl) {
        if (receiveFileUrl == null || receiveFileUrl.length() < 3) {
            return 0;
        }
        int backDefault = 0;
        try {
            String name = receiveFileUrl.substring(receiveFileUrl.lastIndexOf("/") + 1, receiveFileUrl.length());
            for (int i = 0; i < images.size(); i++) {
                String path = images.get(i).toString();
                if (path.contains(name)) {
                    backDefault = i;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (backDefault == 0) {
            backDefault = images.size() - 1;
        } else if (backDefault == images.size() + 1) {
            backDefault = 0;
        } else {
            backDefault = backDefault - 1;
        }
        return backDefault;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_paint:
                MyLog.cdl("======解析的地址===" + current_image_url);
                // 涂鸦参数
                DoodleParams params = new DoodleParams();
                params.mIsFullScreen = true;
                // 图片路径
                params.mImagePath = current_image_url;
                // 初始画笔大小
                params.mPaintUnitSize = DoodleView.DEFAULT_SIZE;
                // 画笔颜色
                params.mPaintColor = Color.RED;
                // 是否支持缩放item
                params.mSupportScaleItem = true;
                // 启动涂鸦页面
                DoodleActivity.startActivityOnly(TaskImageActivity.this, params);
                break;
            case R.id.lin_exit:
            case R.id.tv_exit:
                finishView();
                break;
        }
    }

    public void finishView() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void checkSdStateFinish() {
        startToMainTaskView();
    }

    @Override
    public void stopOrderToPlay() {
        startToMainTaskView();
    }

    @Override
    public void getTaskInfoNull() {
        startToMainTaskView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishView();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    String current_image_url;

    @Override
    public void onPageSelected(int position) {
        current_image_url = images.get(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}