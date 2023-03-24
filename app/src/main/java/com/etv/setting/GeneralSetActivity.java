package com.etv.setting;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;

import com.etv.activity.MainActivity;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.util.APKUtil;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.system.CpuModel;
import com.ys.etv.R;
import com.ys.etv.databinding.ActicityGeneralViewBinding;
import com.ys.model.dialog.RadioListDialog;
import com.ys.model.entity.RedioEntity;
import com.ys.model.listener.MoreButtonListener;
import com.ys.model.listener.MoreButtonToggleListener;
import com.ys.model.listener.RadioChooiceListener;

import java.util.ArrayList;
import java.util.List;

/***
 * 通用设置界面
 */
public class GeneralSetActivity extends SettingBaseActivity implements View.OnClickListener, MoreButtonListener {


    ActicityGeneralViewBinding mBinding;

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        mBinding = ActicityGeneralViewBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        initView();
        initListener();
    }

    private void initListener() {
        mBinding.switchHdmiButton.setOnMoretListener(new MoreButtonToggleListener() {
            @Override
            public void switchToggleView(View view, boolean isChooice) {
                SharedPerManager.setShowHdmiButton(isChooice);
                updateMainView();
            }
        });

        if (!CpuModel.isMLogic()) {
            return;
        }
        mBinding.linExit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                } else {
                    mBinding.settingViewTime.requestFocus();
                }
            }
        });
    }

    public static final String TAG_SETTING_INFO = "TAG_SETTING_INFO";
    private int TAG_FROM = -1; //-1  回到设置界面  1 回到主界面

    private void initView() {
        Intent intent = getIntent();
        TAG_FROM = intent.getIntExtra(TAG_SETTING_INFO, -1);
        mBinding.linExit.setOnClickListener(this);
        mBinding.btnSdmanager.setOnMoretListener(this);
        mBinding.settingViewTime.setOnMoretListener(this);
        mBinding.btnTimePoweronoff.setOnMoretListener(this);
        mBinding.btnLanguageSet.setOnMoretListener(this);
        mBinding.btnVoice.setOnMoretListener(this);
        mBinding.btnHdmiSet.setOnMoretListener(this);
        mBinding.btnHdmiShow.setOnMoretListener(this);
        showOrHiddleView();
    }

    private void showOrHiddleView() {
//        mBinding.btnLanguageSet.setVisibility(View.GONE);
        switch (CpuModel.getMobileType()) {
            case CpuModel.CPU_MODEL_MLOGIC:
                mBinding.btnLanguageSet.setVisibility(View.GONE);
                mBinding.btnHdmiSet.setVisibility(View.VISIBLE);
                mBinding.btnHdmiShow.setVisibility(View.VISIBLE);
                mBinding.switchHdmiButton.setVisibility(View.VISIBLE);
                break;
            case CpuModel.CPU_MODEL_MTK_M11:
                mBinding.btnLanguageSet.setVisibility(View.GONE);
                mBinding.btnHdmiSet.setVisibility(View.GONE);
                mBinding.btnHdmiShow.setVisibility(View.GONE);
                mBinding.btnVoice.setVisibility(View.GONE);
                mBinding.switchHdmiButton.setVisibility(View.GONE);
                break;
            case CpuModel.CPU_MODEL_3566_11:
                mBinding.btnLanguageSet.setVisibility(View.GONE);
                mBinding.btnVoice.setVisibility(View.GONE);
                mBinding.btnHdmiSet.setVisibility(View.GONE);
                mBinding.btnHdmiShow.setVisibility(View.GONE);
                mBinding.switchHdmiButton.setVisibility(View.GONE);
                break;
            default:
                /*mBinding.btnHdmiSet.setVisibility(View.GONE);
                mBinding.btnHdmiShow.setVisibility(View.GONE);
                mBinding.switchHdmiButton.setVisibility(View.GONE);*/
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_exit:
                backViewToFront();
                break;
        }
    }

    private void backViewToFront() {
        if (TAG_FROM < 0) {
            finish();
        } else {
            MainActivity.IS_ORDER_REQUEST_TASK = true;
            startActivity(new Intent(GeneralSetActivity.this, MainActivity.class));
            finish();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backViewToFront();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.btn_voice:
                startActivity(new Intent(GeneralSetActivity.this, VoiceSettingsActivity.class));
                break;
            case R.id.btn_hdmi_set:
                showModifyHdmiNumDialog();
                break;
            case R.id.btn_hdmi_show:
                showModifyHdmiTypeDialog();
                break;
            case R.id.btn_language_set:
                gotoSysLauageSettingView();
                break;
            case R.id.btn_sdmanager:
                startActivity(new Intent(GeneralSetActivity.this, StorageActivity.class));
                break;
            case R.id.setting_view_time:
                startActivity(new Intent(GeneralSetActivity.this, TimeSettingActivity.class));
                break;
            case R.id.btn_time_poweronoff:
                if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_KING_LAM) {
                    APKUtil.startApp(GeneralSetActivity.this, "com.adtv");
                    return;
                }
                int isWorkModel = SharedPerManager.getWorkModel();
                if (isWorkModel == AppInfo.WORK_MODEL_NET) {
                    startActivity(new Intent(GeneralSetActivity.this, PowerOnOffWebActivity.class));
                } else {
                    startActivity(new Intent(GeneralSetActivity.this, PowerOnOffLocalActivity.class));
                }
                break;
        }
    }

    private void showModifyHdmiTypeDialog() {
        RadioListDialog radioListDialog = new RadioListDialog(GeneralSetActivity.this);
        List<RedioEntity> lists = new ArrayList<RedioEntity>();
        lists.add(new RedioEntity(getString(R.string.hdmi_type_auto)));
        lists.add(new RedioEntity(getString(R.string.hdmi_type_full_view)));
        int position = SharedPerManager.getHdmiShowStyle();
        radioListDialog.show("HDMI_TYPE", lists, position);
        radioListDialog.setRadioChooiceListener(new RadioChooiceListener() {
            @Override
            public void backChooiceInfo(RedioEntity redioEntity, int chooicePosition) {
                SharedPerManager.setHdmiShowStyle(chooicePosition);
                updateMainView();
            }
        });
    }

    private void gotoSysLauageSettingView() {
        Intent intent = new Intent();
        int sdkCode = Build.VERSION.SDK_INT;
        if (sdkCode > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && sdkCode < Build.VERSION_CODES.LOLLIPOP) {
            // 4.4 系统
            intent.setAction(Settings.ACTION_LOCALE_SETTINGS);
        } else if (sdkCode > 23 && sdkCode < 26) {
            // 7.1
            intent.setClassName("com.android.settings", "com.android.settings.Settings$InputMethodAndLanguageSettingsActivity");
        } else {
            intent.setAction(Settings.ACTION_LOCALE_SETTINGS);
           // intent.setClassName("com.android.settings", "com.android.settings.Settings$InputMethodAndLanguageSettingsActivity");
        }
        startActivity(intent);
    }

    private void showModifyHdmiNumDialog() {
        RadioListDialog radioListDialog = new RadioListDialog(GeneralSetActivity.this);
        List<RedioEntity> lists = new ArrayList<RedioEntity>();
        lists.add(new RedioEntity("HDMIIN1"));
        lists.add(new RedioEntity("HDMIIN2"));
        String desc = SharedPerManager.getMlogicHdmiPosition();
        int position = 0;
        if (desc.contains(AppInfo.HDMIIN1())) {
            position = 0;
        } else if (desc.contains(AppInfo.HDMIIN2())) {
            position = 1;
        }
        radioListDialog.show("HDMI_NUM", lists, position);
        radioListDialog.setRadioChooiceListener(new RadioChooiceListener() {
            @Override
            public void backChooiceInfo(RedioEntity redioEntity, int chooicePosition) {
                if (chooicePosition == 0) {
                    SharedPerManager.setMlogicHdmiPosition(AppInfo.HDMIIN1());
                } else {
                    SharedPerManager.setMlogicHdmiPosition(AppInfo.HDMIIN2());
                }
                updateMainView();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMainView();
        mBinding.settingViewTime.requestFocus();
    }

    private void updateMainView() {
        mBinding.switchHdmiButton.setSwitchStatues(SharedPerManager.getShowHdmiButton());
        int hdmiType = SharedPerManager.getHdmiShowStyle();
        String hdmiShowDesc = "";
        switch (hdmiType) {
            case 0:
                hdmiShowDesc = getString(R.string.hdmi_type_auto);
                break;
            case 1:
                hdmiShowDesc = getString(R.string.hdmi_type_full_view);
                break;
        }
        mBinding.btnHdmiShow.setTxtContent(hdmiShowDesc);
        String desc = SharedPerManager.getMlogicHdmiPosition();
        if (desc.contains(AppInfo.HDMIIN1())) {
            mBinding.btnHdmiSet.setTxtContent("HDMIIN1");
        } else if (desc.contains(AppInfo.HDMIIN2())) {
            mBinding.btnHdmiSet.setTxtContent("HDMIIN2");
        }
    }

}
