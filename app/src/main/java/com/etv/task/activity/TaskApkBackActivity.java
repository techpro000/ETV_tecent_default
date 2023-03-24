package com.etv.task.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.etv.activity.BaseActivity;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.db.DbBggImageUtil;
import com.etv.util.APKUtil;
import com.etv.util.MyLog;
import com.ys.etv.R;

/**
 * 这个界面的作用就是，其他板卡t跳转APK ，返回界面的时候白屏的问题，这里给他一个虚假的界面
 */
public class TaskApkBackActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_task_back);
        AppInfo.startCheckTaskTag = false;
        initView();
    }

    ImageView iv_apk_show;
    public static final String TAG_GO_TO_APK_PACKAGENAME = "TAG_GO_TO_APK_PACKAGENAME";

    int backNum = 0;

    private void initView() {
        backNum = 0;
        iv_apk_show = (ImageView) findViewById(R.id.iv_apk_show);
        int defaultImage = DbBggImageUtil.getDefaultBggImage();
        iv_apk_show.setBackgroundResource(defaultImage);

        Intent intent = getIntent();
        String packageName = intent.getStringExtra(TAG_GO_TO_APK_PACKAGENAME);
        APKUtil.startApp(TaskApkBackActivity.this, packageName);

    }

    @Override
    protected void onResume() {
        super.onResume();
        backNum++;
        MyLog.playTask("=====执行了一次===backNum===" + backNum);
        if (backNum < 2) {
            return;
        }
        startActivity(new Intent(TaskApkBackActivity.this, PlayerTaskActivity.class));
        finish();
    }
}
