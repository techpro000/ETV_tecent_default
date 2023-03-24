package com.etv.police.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.etv.activity.BaseActivity;
import com.etv.activity.MainActivity;
import com.etv.config.AppInfo;
import com.etv.db.DbPoliceUtil;
import com.etv.entity.PoliceNumEntity;
import com.etv.util.APKUtil;
import com.etv.util.MyLog;
import com.ys.etv.R;

import java.util.List;

public class PoliceCacheActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_police_cache);
        initView();
    }

    public static boolean isViewFront = false;
    private boolean isViewRun = false;

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        String packageName = "com.police.call";
        boolean isInstall = APKUtil.ApkState(PoliceCacheActivity.this, packageName);
        if (!isInstall) {
            showToastView(getString(R.string.no_install));
            startToMainActivity();
            return;
        }
        List<PoliceNumEntity> policeNumEntityList = DbPoliceUtil.getPoliceList();
        if (policeNumEntityList == null || policeNumEntityList.size() < 1) {
            showToastView(getString(R.string.current_has_no_phonenum));
            startToMainActivity();
            return;
        }
        try {
            PackageManager packageManager = getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(packageName);
            startActivity(intent);
        } catch (Exception e) {
            MyLog.phone("打开界面异常：" + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isViewFront = true;
        if (isViewRun) {
            startToMainActivity();
            return;
        }
        isViewRun = true;
    }

    private void startToMainActivity() {
        startActivity(new Intent(PoliceCacheActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isViewFront = false;
    }
}
