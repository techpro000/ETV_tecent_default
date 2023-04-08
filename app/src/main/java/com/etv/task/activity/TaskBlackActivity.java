package com.etv.task.activity;

import android.content.Intent;
import android.os.Bundle;

import com.etv.activity.BaseActivity;

public class TaskBlackActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        initView();
    }

    private void initView() {
        Intent intent = new Intent();
        intent.setClass(TaskBlackActivity.this, PlaySingleActivity.class);
        startActivity(intent);
        finish();
    }

}
