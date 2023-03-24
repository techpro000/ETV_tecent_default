package com.etv.task.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etv.config.AppInfo;
import com.etv.task.entity.MediAddEntity;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.view.layout.Generator;
import com.etv.view.layout.mixedswing.ViewImgVideoNetGenerate;
import com.ys.etv.R;
import com.ys.model.util.FileMatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 混播单独控件
 */
public class PlayImaVideoActivity extends TaskActivity implements View.OnClickListener {

    public static final String TAG_RECEIVE_MESSAGE = "TAG_RECEIVE_MESSAGE";    //接受得图片集合列表

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_play_img_video);
        getData();
    }

    @Override
    public void showDownStatuesView(boolean isShow, String desc) {

    }

    List<MediAddEntity> listsEntity = new ArrayList<>();

    private void getData() {
        listsEntity.clear();
        AppInfo.startCheckTaskTag = false;
        iv_no_data = (LinearLayout) findViewById(R.id.iv_no_data);
        Intent intent = getIntent();
        imgVideos = intent.getStringArrayListExtra(TAG_RECEIVE_MESSAGE);
        if (imgVideos == null || imgVideos.size() < 1) {
            MyLog.cdl("======图片得数量  00===");
            iv_no_data.setVisibility(View.VISIBLE);
            return;
        }
        for (int i = 0; i < imgVideos.size(); i++) {
            String filePath = imgVideos.get(i);
            int fileType = FileMatch.fileMatch(filePath);
            String cartonAnim = SharedPerManager.getSinglePicAnimiType() + "";
            String videoNum = SharedPerManager.getSingleVideoVoiceNum() + "";
            String picDistanceTime = SharedPerManager.getPicDistanceTime() + "";
            String pmType = AppInfo.PROGRAM_DEFAULT;
            long fileSize = -1;
            File file = new File(filePath);
            if (file.exists()) {
                fileSize = file.length();
            }
            MediAddEntity mediaAddEntity = new MediAddEntity(filePath, "-1", cartonAnim, picDistanceTime, videoNum, fileType, pmType, fileSize);
            listsEntity.add(mediaAddEntity);
        }
        initView();
    }

    AbsoluteLayout view_abous;
    List<String> imgVideos = new ArrayList<String>();
    LinearLayout iv_no_data;
    Generator generatorView;
    LinearLayout lin_exit;
    TextView tv_exit;

    private void initView() {
        view_abous = (AbsoluteLayout) findViewById(R.id.view_abous);
        view_abous.setBackgroundColor(getResources().getColor(R.color.black));
        iv_no_data.setVisibility(View.GONE);
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        tv_exit = (TextView) findViewById(R.id.tv_exit);
        lin_exit.setOnClickListener(this);
        tv_exit.setOnClickListener(this);
        int width = SharedPerUtil.getScreenWidth();
        int height = SharedPerUtil.getScreenHeight();
        generatorView = new ViewImgVideoNetGenerate(PlayImaVideoActivity.this, null, null, 0, 0, width, height, listsEntity, true, 0, AppInfo.PROGRAM_POSITION_MAIN,false);
        view_abous.addView(generatorView.getView(), generatorView.getLayoutParams());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishView();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearCacheView();
    }

    private void clearCacheView() {
        if (view_abous != null) {
            view_abous.removeAllViews();
        }
        if (generatorView != null) {
            generatorView.clearMemory();
        }
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

}
