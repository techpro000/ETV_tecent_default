package com.etv.setting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etv.activity.MainActivity;
import com.etv.activity.SettingSysActivity;
import com.etv.adapter.WorkChooiceAdapter;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.entity.WorkChooiceEntity;
import com.etv.task.db.DBTaskUtil;
import com.etv.task.entity.ViewPosition;
import com.etv.util.FileUtil;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.util.poweronoff.PowerOnOffManager;
import com.etv.util.system.SystemManagerUtil;
import com.ys.etv.R;
import com.ys.model.dialog.OridinryDialog;
import com.ys.model.listener.OridinryDialogClick;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作模式的设定
 */
public class WorkChoiceActivity extends SettingBaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.fragment_work_setting);
        initView();
        getDataInfoWork();
    }

    private void getDataInfoWork() {
        workChooiceEntityList.clear();
        int isWorkModel = SharedPerManager.getWorkModel();
        workChooiceEntityList.add(new WorkChooiceEntity(R.mipmap.dialog_net, getLanguageFromResurce(R.string.work_net_down), isWorkModel == 0 ? true : false, AppInfo.WORK_MODEL_NET));
        boolean isShowTask = SharedPerManager.getShowNetDownTask();
        if (isShowTask) {
            workChooiceEntityList.add(new WorkChooiceEntity(R.mipmap.dialog_net_down, getLanguageFromResurce(R.string.work_net_load), isWorkModel == 1 ? true : false, AppInfo.WORK_MODEL_NET_DOWN));
        }
        workChooiceEntityList.add(new WorkChooiceEntity(R.mipmap.dialog_single, getLanguageFromResurce(R.string.work_single), isWorkModel == 2 ? true : false, AppInfo.WORK_MODEL_SINGLE));
        grid_work.setNumColumns(workChooiceEntityList.size());
        workChooiceAdapter.setWorkList(workChooiceEntityList);
    }

    TextView tv_net_status;
    LinearLayout lin_exit;
    Button btn_sys_setting;
    GridView grid_work;
    WorkChooiceAdapter workChooiceAdapter;
    List<WorkChooiceEntity> workChooiceEntityList = new ArrayList<WorkChooiceEntity>();
    RelativeLayout rela_pass_dialog;
    EditText et_username_edit;
    Button btn_del_all, btn_modify;

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        workChooiceAdapter = new WorkChooiceAdapter(WorkChoiceActivity.this, workChooiceEntityList);
        grid_work = (GridView) findViewById(R.id.grid_work);
        grid_work.setAdapter(workChooiceAdapter);
        grid_work.setOnItemClickListener(this);
        rela_pass_dialog = (RelativeLayout) findViewById(R.id.rela_pass_dialog);
        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_JIAIWEI) {
            rela_pass_dialog.setVisibility(View.VISIBLE);
        }
        btn_del_all = (Button) findViewById(R.id.btn_del_all);
        btn_del_all.setOnClickListener(this);
        btn_modify = (Button) findViewById(R.id.btn_modify);
        btn_modify.setOnClickListener(this);
        et_username_edit = (EditText) findViewById(R.id.et_username_edit);
        tv_net_status = (TextView) findViewById(R.id.tv_net_status);
        boolean isNetOpen = NetWorkUtils.isNetworkConnected(WorkChoiceActivity.this);
        tv_net_status.setText(isNetOpen ? getLanguageFromResurce(R.string.normal_show) : getLanguageFromResurce(R.string.network_error));
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        lin_exit.setOnClickListener(this);
        btn_sys_setting = (Button) findViewById(R.id.btn_sys_setting);
        btn_sys_setting.setOnClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        WorkChooiceEntity workChoiceEntity = workChooiceEntityList.get(position);
        final int workModel = workChoiceEntity.getWorkModel();
        String workDesc = workChoiceEntity.getDesc();
        int workModelSave = SharedPerManager.getWorkModel();

        if (workModelSave == workModel) {
            String toastDesc = getLanguageFromResurceWithPosition(R.string.work_toast_repeat, workDesc);
            showToastView(toastDesc);
            return;
        }
        OridinryDialog oridinryDialog = new OridinryDialog(WorkChoiceActivity.this);
        oridinryDialog.setOnDialogClickListener(new OridinryDialogClick() {
            @Override
            public void sure() {
                changeWorkModelType(workModel);
            }

            @Override
            public void noSure() {

            }


        });
        String toastDesc = getString(R.string.work_toast_change);
        oridinryDialog.show(toastDesc, getLanguageFromResurce(R.string.ok), getLanguageFromResurce(R.string.cancel));
    }

    private void changeWorkModelType(int workModel) {
        try {
            MyLog.playTask("clearAllDbInfo 7");
            DBTaskUtil.clearAllDbInfo("=======选择工作状态===");  //清理任务
            //切换工作模式需要停止下载
            sendBroadcast(new Intent(AppInfo.STOP_DOWN_TASK_RECEIVER));
            if (workModel == AppInfo.WORK_MODEL_NET) {
                chooiceNetWorkDown();
            } else if (workModel == AppInfo.WORK_MODEL_NET_DOWN) {
                chooiceNetWorkLoad();
            } else if (workModel == AppInfo.WORK_MODEL_SINGLE) {
                chooiceSingle();
                Log.e("TAG", "changeWorkModelType: "+"切换成单机模式了" );
            }
            //关闭任务，APK下载
            Intent intent = new Intent();
            intent.setAction(AppInfo.STOP_DOWN_TASK_RECEIVER);
            sendBroadcast(intent);
            //同步定时开关机时间
            PowerOnOffManager.getInstance().changePowerOnOffByWorkModel("工作模式修改设定定时开关机");
            //保存工作模式
            SharedPerManager.setWorkModel(workModel, "工作模式设定界面设定得");
            //刷新界面
            getDataInfoWork();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void chooiceNetWorkLoad() {
        MyLog.cdl("修改工作模式: 网络U盘导出==", true);
        showToastView(getLanguageFromResurce(R.string.work_net_load));
        clearSingleSetting();
    }

    public void chooiceNetWorkDown() {
        //删除单机文件夹
        MyLog.cdl("修改工作模式: 网络下发模式==", true);
        String singlePath = AppInfo.TASK_SINGLE_PATH();  //单机版本得目录
        FileUtil.deleteDirOrFilePath(singlePath, "切换网络下发模式。清理文件");
        showToastView(getLanguageFromResurce(R.string.work_net_down));
        clearSingleSetting();
    }

    public void chooiceSingle() {
        //删除task文件目录
        MyLog.cdl("修改工作模式: 单机模式==", true);
        String singlePath = AppInfo.BASE_TASK_URL();  //任务存放目录
        FileUtil.deleteDirOrFilePath(singlePath, "====选择单机模式===");
        showToastView(getLanguageFromResurce(R.string.work_single));
    }

    /**
     * 清空单机模式的设置
     */
    private void clearSingleSetting() {
        SharedPerManager.setPicDistanceTime(10);
        SharedPerManager.setSingleVideoVoiceNum(70);
        SharedPerManager.setWpsDistanceTime(10);
        //先判断主屏
        boolean isHroVerMain = SystemManagerUtil.isScreenHorOrVer(WorkChoiceActivity.this, AppInfo.PROGRAM_POSITION_MAIN);
        if (isHroVerMain) { //横屏
            SharedPerManager.setSingleLayoutTag(ViewPosition.VIEW_LAYOUT_HRO_VIEW);
        } else {//竖屏
            SharedPerManager.setSingleLayoutTag(ViewPosition.VIEW_LAYOUT_VER_VIEW);
        }
        //后判断副屏
        boolean isHroVerSecond = SystemManagerUtil.isScreenHorOrVer(WorkChoiceActivity.this, AppInfo.PROGRAM_POSITION_SECOND);
        if (isHroVerSecond) { //横屏
            SharedPerManager.setSingleSecondLayoutTag(ViewPosition.VIEW_LAYOUT_HRO_VIEW);
        } else {//竖屏
            SharedPerManager.setSingleSecondLayoutTag(ViewPosition.VIEW_LAYOUT_VER_VIEW);
        }
        SharedPerManager.setSinglePicAnimiType(0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_del_all:
                et_username_edit.setText("");
                break;
            case R.id.btn_modify:
                String password = et_username_edit.getText().toString().trim();
                if (password == null || password.length() < 3) {
                    showToastView(getString(R.string.password_error));
                    return;
                }
                String passwordSave = SharedPerManager.getExitpassword();
                if (!password.contains(passwordSave)) {
                    showToastView(getString(R.string.password_error));
                    return;
                }
                rela_pass_dialog.setVisibility(View.GONE);
                break;
            case R.id.btn_sys_setting:
                startActivity(new Intent(WorkChoiceActivity.this, SettingSysActivity.class));
                finish();
                break;
            case R.id.lin_exit:
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivityMain();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startActivityMain() {
        Intent intent = new Intent();
        intent.setClass(WorkChoiceActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}
