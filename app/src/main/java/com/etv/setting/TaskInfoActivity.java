package com.etv.setting;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.etv.config.ApiInfo;
import com.etv.config.AppInfo;
import com.ys.model.dialog.MyToastView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.etv.listener.ObjectClickListener;
import com.etv.setting.adapter.ProTaskAdater;
import com.etv.task.db.DBTaskUtil;
import com.etv.task.entity.TaskWorkEntity;
import com.etv.task.model.TaskModelmpl;
import com.etv.util.CodeUtil;
import com.etv.util.FileUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.ys.model.dialog.WaitDialogUtil;
import com.ys.model.dialog.OridinryDialog;
import com.ys.model.listener.OridinryDialogClick;
import com.ys.etv.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class TaskInfoActivity extends SettingBaseActivity implements View.OnClickListener {

    ProTaskAdater adapter;
    ListView list_pro;
    TextView tv_no_date;
    LinearLayout iv_no_data;

    List<TaskWorkEntity> listPro = new ArrayList<TaskWorkEntity>();
    TaskModelmpl taskModelmpl;

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.fragment_task_info);
        initView();
    }

    LinearLayout lin_exit;

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        taskModelmpl = new TaskModelmpl();
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        lin_exit.setOnClickListener(this);
        tv_no_date = (TextView) findViewById(R.id.tv_no_date);
        list_pro = (ListView) findViewById(R.id.list_pro);
        adapter = new ProTaskAdater(TaskInfoActivity.this, listPro);
        list_pro.setAdapter(adapter);
        iv_no_data = (LinearLayout) findViewById(R.id.iv_no_data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTaskFormDb();
    }

    private void getTaskFormDb() {
        listPro.clear();
        adapter.setList(listPro);
        listPro = DBTaskUtil.getTaskInfoList();
        iv_no_data.setVisibility(View.GONE);
        if (listPro == null || listPro.size() < 1) {
            MyLog.task("===========getTaskInfoList====000=====");
            showToastView(getString(R.string.no_data));
            iv_no_data.setVisibility(View.VISIBLE);
            if (SharedPerManager.getWorkModel() == AppInfo.WORK_MODEL_SINGLE) {
                tv_no_date.setText(getString(R.string.work_single));
            } else {
                tv_no_date.setText(getString(R.string.no_data));
            }
            return;
        }
        MyLog.task("===========getTaskInfoList=====listPro====" + listPro.size());
        adapter.setList(listPro);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lin_exit:
                finish();
                break;
        }
    }
}
