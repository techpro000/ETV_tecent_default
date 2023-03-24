package com.etv.setting;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.etv.activity.BaseActivity;
import com.etv.config.AppInfo;
import com.etv.down.XutilDownFileEntity;
import com.etv.down.XutilDownRunnable;
import com.etv.down.XutilDownStateListener;
import com.etv.service.EtvService;
import com.etv.util.APKUtil;
import com.etv.util.FileUtil;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.util.system.SystemManagerUtil;
import com.ys.etv.R;
import com.ys.model.dialog.EditTextDialog;
import com.ys.model.dialog.OridinryDialog;
import com.ys.model.listener.EditTextDialogListener;
import com.ys.model.listener.MoreButtonListener;
import com.ys.model.listener.MoreButtonToggleListener;
import com.ys.model.listener.OridinryDialogClick;
import com.ys.model.view.MoreButton;
import com.ys.model.view.MoreButtonToggle;

public class VoiceSettingsActivity extends BaseActivity implements View.OnClickListener, MoreButtonListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_settings);
        initView();
        initListener();
    }

    private void initListener() {
        switch_open_tts.setOnMoretListener(new MoreButtonToggleListener() {
            @Override
            public void switchToggleView(View view, boolean isChooice) {
                SharedPerManager.setOpenTTSManager(isChooice);
                initData();
            }
        });
    }

    ProgressBar mPb;
    TextView tv_down_statues;

    MoreButton more_install_apk_statues, more_tts_chooice, more_tts_setting, more_tts_message;
    LinearLayout lin_exit;
    LinearLayout lin_voice_setting;
    MoreButtonToggle switch_open_tts;

    private void initView() {
        switch_open_tts = (MoreButtonToggle) findViewById(R.id.switch_open_tts);
        lin_voice_setting = (LinearLayout) findViewById(R.id.lin_voice_setting);
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        lin_exit.setOnClickListener(this);

        more_tts_setting = (MoreButton) findViewById(R.id.more_tts_setting);
        more_tts_setting.setOnMoretListener(this);

        more_tts_message = (MoreButton) findViewById(R.id.more_tts_message);
        more_tts_message.setOnMoretListener(this);

        more_install_apk_statues = (MoreButton) findViewById(R.id.more_install_apk_statues);
        more_install_apk_statues.setOnMoretListener(this);
        more_tts_chooice = (MoreButton) findViewById(R.id.more_tts_chooice);
        more_tts_chooice.setOnMoretListener(this);
        mPb = findViewById(R.id.avs_pb);
        mPb.setMax(100);
        tv_down_statues = (TextView) findViewById(R.id.tv_down_statues);
    }

    private void showRebootDialog() {
        OridinryDialog oridinryDialog = new OridinryDialog(VoiceSettingsActivity.this);
        oridinryDialog.show(getString(R.string.tip_reboot), getString(R.string.app_reboot_content));
        oridinryDialog.setOnDialogClickListener(new OridinryDialogClick() {
            @Override
            public void sure() {
                SystemManagerUtil.rebootApp(VoiceSettingsActivity.this);
                finish();
            }

            @Override
            public void noSure() {

            }
        });
    }

    @Override
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.more_tts_message:
                showModifyMessageDialog();
                break;
            case R.id.more_tts_setting:
                startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
                break;
            case R.id.more_tts_chooice:
                //语音选择
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCALE_SETTINGS);
                startActivity(intent);
                break;
            case R.id.more_install_apk_statues:
                String googleTssPkg = "com.google.android.tts";
                boolean installed = APKUtil.ApkState(VoiceSettingsActivity.this, googleTssPkg);
                if (installed) {
                    showToastView("The software has been installed");
                    return;
                }
                if (isDownStatues) {
                    showToastView("Is downloading ...");
                    return;
                }
                downApkFromWeb();
                break;
        }
    }

    private void showModifyMessageDialog() {
        EditTextDialog editTextDialog = new EditTextDialog(VoiceSettingsActivity.this);
        editTextDialog.setOnDialogClickListener(new EditTextDialogListener() {
            @Override
            public void clickHiddleView() {

            }

            @Override
            public void commit(String content) {
                if (TextUtils.isEmpty(content)) {
                    showToastView(getString(R.string.input_success_info));
                    return;
                }
                int delayTimeSave = SharedPerManager.getTtsMessageDelay();
                try {
                    delayTimeSave = Integer.parseInt(content);
                } catch (Exception e) {
                    showToastView(getString(R.string.input_success_info));
                    e.printStackTrace();
                }
                SharedPerManager.setTtsMessageDelay(delayTimeSave);
                initData();
            }
        });
        int delayTime = SharedPerManager.getTtsMessageDelay();
        editTextDialog.show(getString(R.string.tts_message_time), delayTime + "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_exit:
                finish();
                break;
        }
    }

    boolean isDownStatues = false;

    private void downApkFromWeb() {
        if (!NetWorkUtils.isNetworkConnected(VoiceSettingsActivity.this)) {
            showToastView("Current Not Net");
            return;
        }
        FileUtil.creatPathNotExcit("下载语音包文件");
        String url = "http://119.23.220.53:8899/resource/apk/google_tts.apk";
        String savePath = AppInfo.APK_PATH() + "/googleTTs.apk";
        XutilDownRunnable downRunnable = new XutilDownRunnable();
        downRunnable.setDownInfo(url, savePath, new XutilDownStateListener() {

            @Override
            public void downStateInfo(XutilDownFileEntity entity) {
                if (entity == null) {
                    return;
                }
                int downStatues = entity.getDownState();
                switch (downStatues) {
                    case XutilDownFileEntity.DOWN_STATE_PROGRESS:
                        isDownStatues = true;
                        int progress = entity.getProgress();
                        int speed = entity.getDownSpeed();
                        mPb.setProgress(progress);
                        tv_down_statues.setText(speed + " KB/S" + " / " + progress + "%");
                        break;
                    case XutilDownFileEntity.DOWN_STATE_SUCCESS:
                        isDownStatues = false;
                        String apkPath = entity.getSavePath();
                        installApkToLocal(apkPath);
                        break;
                    case XutilDownFileEntity.DOWN_STATE_FAIED:
                        isDownStatues = false;
                        String ERRORdESC = entity.getDesc();
                        tv_down_statues.setText("Error:" + ERRORdESC);
                        break;
                }
            }
        });
        downRunnable.setIsDelFile(true);
        downRunnable.setLimitDownSpeed(SharedPerManager.getLimitSpeed());
        EtvService.getInstance().executor(downRunnable);
    }

    private void installApkToLocal(String url) {
        Log.e("install", "===安装的路径==" + url);
        APKUtil apkUtil = new APKUtil(VoiceSettingsActivity.this);
        apkUtil.installApk(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        boolean isOpenTTS = SharedPerManager.getOpenTTSManager();
        switch_open_tts.setChcekcAble(isOpenTTS);
        lin_voice_setting.setVisibility(isOpenTTS ? View.VISIBLE : View.GONE);

        String googleTssPkg = "com.google.android.tts";
        boolean installed = APKUtil.ApkState(VoiceSettingsActivity.this, googleTssPkg);
        if (installed) {
            more_install_apk_statues.setTextTitle(getString(R.string.installted));
        } else {
            more_install_apk_statues.setTextTitle(getString(R.string.
                    no_install));
        }
        more_tts_message.setRigt(SharedPerManager.getTtsMessageDelay() + " S");
    }
}
