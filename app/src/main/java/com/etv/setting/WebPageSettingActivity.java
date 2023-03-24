package com.etv.setting;

import static com.etv.config.AppConfig.APP_TYPE_RW_DEFAULT_ADDRESS;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.system.CpuModel;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebView;
import com.ys.etv.databinding.ActivityWebSettingBinding;
import com.ys.model.dialog.MyToastView;
import com.ys.etv.R;
import com.ys.model.listener.MoreButtonListener;
import com.ys.model.listener.MoreButtonToggleListener;
import com.ys.model.view.SettingSwitchView;

import java.io.File;

public class WebPageSettingActivity extends SettingBaseActivity implements View.OnClickListener, MoreButtonToggleListener {

    ActivityWebSettingBinding mBinding;

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        mBinding = ActivityWebSettingBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        initView();
    }

    private void initView() {
        mBinding.linExit.setOnClickListener(this);
        mBinding.switchWebShowbutton.setOnMoretListener(this);
        mBinding.switchWebRefresh.setOnMoretListener(this);
        mBinding.switchCache.setOnMoretListener(this);
        mBinding.btnTecentInner.setOnMoretListener(new MoreButtonListener() {
            @Override
            public void clickView(View view) {
                if (mBinding.scrollView.getVisibility() == View.VISIBLE) {
                    mBinding.scrollView.setVisibility(View.GONE);
                } else {
                    mBinding.scrollView.setVisibility(View.VISIBLE);
                }
            }
        });
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            mBinding.btnTecentInner.setVisibility(View.GONE);
        }
    }

    @Override
    public void switchToggleView(View view, boolean isChooice) {
        switch (view.getId()) {
            case R.id.switch_web_refresh:
                SharedPerManager.setWebShowReduce(isChooice);
                updateView();
                break;
            case R.id.switch_cache:
                SharedPerManager.setWebCache(isChooice);
                updateView();
                break;
            case R.id.switch_web_showbutton:
                SharedPerManager.setWebShowButton(isChooice);
                updateView();
                break;
        }
        MyToastView.getInstance().Toast(WebPageSettingActivity.this, isChooice ?
                getString(R.string.success) : getString(R.string.close));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_exit:
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateView();
    }

    private void updateView() {
        boolean isShoWebTimeReduce = SharedPerManager.getWebShowReduce();
        mBinding.switchWebRefresh.setSwitchStatues(isShoWebTimeReduce);
        boolean isShoWebButton = SharedPerManager.getWebShowButton();
        mBinding.switchWebShowbutton.setSwitchStatues(isShoWebButton);
        mBinding.switchCache.setSwitchStatues(SharedPerManager.getWebCache());
        //String checkPath = AppInfo.CHECK_TECENT_APP_PATH;
        //File file = new File(checkPath);
        if (QbSdk.isTbsCoreInited()) {
            mBinding.btnTecentInner.setTxtContent(getString(R.string.statues_install));
        } else {
            mBinding.btnTecentInner.setTxtContent(getString(R.string.statues_install_no));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
