package com.etv.setting;

import android.os.Bundle;
import android.view.View;

import com.etv.util.SharedPerManager;
import com.etv.util.system.CpuModel;
import com.ys.etv.R;
import com.ys.etv.databinding.ActivityControlCenterBinding;
import com.ys.model.listener.MoreButtonToggleListener;

/***
 * 控制中心
 */
public class ControlCenterActivity extends SettingBaseActivity implements MoreButtonToggleListener, View.OnClickListener {

    ActivityControlCenterBinding mBinding;

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        mBinding = ActivityControlCenterBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        initView();
    }

    private void initView() {     
        mBinding.linExit.setOnClickListener(this);
        mBinding.tvExit.setOnClickListener(this);
        mBinding.switchShowWps.setOnMoretListener(this);
        mBinding.switchBlueTeeth.setOnMoretListener(this);
        mBinding.switchPlanPerson.setOnMoretListener(this);
        mBinding.switchVideoSize.setOnMoretListener(this);
        mBinding.switchTaskTouch.setOnMoretListener(this);
        mBinding.switchMainBgg.setOnMoretListener(this);
        mBinding.switchShowToggle.setOnMoretListener(this);
        mBinding.switchDoubleScreen.setOnMoretListener(this);
        mBinding.switchHdmiIn.setOnMoretListener(this);
        hiddenView();
    }

    private void hiddenView() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_exit:
            case R.id.tv_exit:
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateToggleStatues();
    }

    @Override
    public void switchToggleView(View view, boolean isChooice) {
        switch (view.getId()) {
            case R.id.switch_show_wps:
                SharedPerManager.setWpsShowEnable(isChooice);
                showToastView(getLanguageFromResurce(R.string.set_success));
                break;
            case R.id.switch_blue_teeth:
                SharedPerManager.setBluetooth(isChooice);
                showToastView(getLanguageFromResurce(R.string.set_success));
                break;
            case R.id.switch_plan_person:
                SharedPerManager.setAutoRebootDev(isChooice);
                showToastView(getLanguageFromResurce(R.string.set_success));
                break;
            case R.id.switch_video_size:
                SharedPerManager.setVideoMoreSize(isChooice);
                showToastView(getLanguageFromResurce(R.string.set_success));
                break;
            case R.id.switch_task_touch:
                SharedPerManager.setTaskTouchEnable(isChooice);
                showToastView(getLanguageFromResurce(R.string.set_success));
                break;

            case R.id.switch_main_bgg:
                SharedPerManager.setBggImageFromWeb(isChooice);
                showToastView(getLanguageFromResurce(R.string.set_success));
                break;
            case R.id.switch_hdmi_in:
                SharedPerManager.setIfHdmiInSuport(isChooice);
                showToastView(getLanguageFromResurce(R.string.set_success));
                break;
            case R.id.switch_show_toggle:
                SharedPerManager.setShowPaintIcon(isChooice);
                showToastView(getLanguageFromResurce(R.string.set_success));
                break;
            case R.id.switch_double_screen:
                SharedPerManager.setScreenSame(isChooice);
                showToastView(getLanguageFromResurce(R.string.set_success));
                break;
        }
        updateToggleStatues();
    }

    private void updateToggleStatues() {
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            mBinding.switchDoubleScreen.setVisibility(View.GONE);
            mBinding.switchShowToggle.setVisibility(View.GONE);
            mBinding.switchTaskTouch.setVisibility(View.GONE);
            mBinding.switchVideoSize.setVisibility(View.GONE);
            mBinding.switchBlueTeeth.setVisibility(View.GONE);
        }
        mBinding.switchShowWps.setSwitchStatues(SharedPerManager.getWpsShowEnable());
        mBinding.switchBlueTeeth.setSwitchStatues(SharedPerManager.getBluetooth());
        mBinding.switchPlanPerson.setSwitchStatues(SharedPerManager.getAutoRebootDev());
        mBinding.switchVideoSize.setSwitchStatues(SharedPerManager.getVideoMoreSize());
        mBinding.switchTaskTouch.setSwitchStatues(SharedPerManager.getTaskTouchEnable());
        mBinding.switchMainBgg.setSwitchStatues(SharedPerManager.getBggImageFromWeb());
        boolean isOpenScreen = SharedPerManager.getScreenSame();
        String open = getLanguageFromResurce(R.string.screen_same);
        String close = getLanguageFromResurce(R.string.close);
        mBinding.switchDoubleScreen.setTxtContent(isOpenScreen ? open : close);
        mBinding.switchDoubleScreen.setSwitchStatues(isOpenScreen);
        String show = getLanguageFromResurce(R.string.show);
        String hiddle = getLanguageFromResurce(R.string.hiddle);
        boolean isShowPainIcon = SharedPerManager.isShowPaintIcon();
        mBinding.switchShowToggle.setSwitchStatues(isShowPainIcon);
        mBinding.switchShowToggle.setTxtContent(isShowPainIcon ? show : hiddle);
        boolean isSupportHdmi = SharedPerManager.getIfHdmiInSuport();
        mBinding.switchHdmiIn.setSwitchStatues(isSupportHdmi);
    }

}
