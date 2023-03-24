package com.etv.police.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.etv.adapter.PoliceShowAdapter;
import com.etv.db.DbPoliceUtil;
import com.etv.entity.PoliceNumEntity;
import com.etv.setting.SettingBaseActivity;
import com.etv.util.APKUtil;
import com.etv.util.SharedPerManager;
import com.ys.model.dialog.MyToastView;
import com.ys.etv.R;
import com.ys.model.listener.MoreButtonListener;
import com.ys.model.listener.MoreButtonToggleListener;
import com.ys.model.view.SettingClickView;
import com.ys.model.view.SettingSwitchView;

import java.util.ArrayList;
import java.util.List;

public class PoliceSettingActivity extends SettingBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_setting_police);
        initView();
        initListener();
    }

    SettingSwitchView switch_police_open;
    SettingClickView btn_video_recorder;
    LinearLayout lin_exit;
    TextView tv_exit;
    ListView lv_pilice_list;
    PoliceShowAdapter adapter;
    TextView tv_police_title;
    List<PoliceNumEntity> policeNumEntityList;

    private void initView() {
        tv_police_title = (TextView) findViewById(R.id.tv_police_title);
        btn_video_recorder = (SettingClickView) findViewById(R.id.btn_video_recorder);
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        tv_exit = (TextView) findViewById(R.id.tv_exit);
        lin_exit.setOnClickListener(this);
        tv_exit.setOnClickListener(this);
        switch_police_open = (SettingSwitchView) findViewById(R.id.switch_police_open);
        policeNumEntityList = new ArrayList<PoliceNumEntity>();
        lv_pilice_list = (ListView) findViewById(R.id.lv_pilice_list);
        adapter = new PoliceShowAdapter(PoliceSettingActivity.this, policeNumEntityList, 1);
        lv_pilice_list.setAdapter(adapter);
    }

    private void initListener() {
        btn_video_recorder.setOnMoretListener(new MoreButtonListener() {

            @Override
            public void clickView(View view) {
                if (!ifApkInstall) {
                    showToastView(getString(R.string.no_install));
                    return;
                }
                Intent intent = new Intent();
                intent.setAction("com.police.call.setting");
                startActivity(intent);
            }
        });
        switch_police_open.setOnMoretListener(new MoreButtonToggleListener() {
            @Override
            public void switchToggleView(View view, boolean isChooice) {
                switch (view.getId()) {
                    case R.id.switch_police_open:
                        SharedPerManager.setGpioAction(isChooice);
                        break;
                }
                MyToastView.getInstance().Toast(PoliceSettingActivity.this, getLanguageFromResurce(R.string.set_success));
                updateToggleStatues();
            }
        });
    }


    private void getPolistFromDb() {
        if (policeNumEntityList != null) {
            policeNumEntityList.clear();
            adapter.setList(policeNumEntityList);
        }
        policeNumEntityList = DbPoliceUtil.getPoliceList();
        if (policeNumEntityList == null || policeNumEntityList.size() < 1) {
            showToastView(getString(R.string.no_data));
            return;
        }
        adapter.setList(policeNumEntityList);
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
        getPolistFromDb();
        checkPoliceCallInstall();
    }

    boolean ifApkInstall = false;

    private void checkPoliceCallInstall() {
        String packageName = "com.police.call";
        boolean isInstall = APKUtil.ApkState(PoliceSettingActivity.this, packageName);
        if (isInstall) {
            ifApkInstall = true;
            tv_police_title.setText(getString(R.string.one_key_police) + " ( " + getString(R.string.statues_install) + " )");
        } else {
            tv_police_title.setText(getString(R.string.one_key_police) + " ( " + getString(R.string.statues_install_no) + " )");
        }
    }

    private void updateToggleStatues() {
        switch_police_open.setSwitchStatues(SharedPerManager.getGpioAction());
    }

}
