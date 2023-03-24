package com.etv.setting;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.RelativeLayout;

import com.etv.activity.BaseActivity;
import com.etv.config.AppInfo;
import com.etv.util.SharedPerManager;
import com.ys.etv.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 时间设置界面
 */
public class TimeSettingActivity extends SettingBaseActivity {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        initView();
    }

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        int workModel = SharedPerManager.getWorkModel();
        if (workModel == AppInfo.WORK_MODEL_NET) {
            View view = View.inflate(TimeSettingActivity.this, R.layout.fragment_time_web, null);
            setContentView(view);

            findViewById(R.id.lin_exit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            findViewById(R.id.lin_exit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

        } else {  //单机模式
            Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
            startActivity(intent);
            finish();
        }
    }
}
