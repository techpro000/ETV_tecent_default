package com.etv.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etv.util.SharedPerManager;
import com.ys.model.dialog.MyToastView;
import com.ys.etv.R;

public class PersonTotalActivity extends SettingBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.setting_person_total_show);
        initView();
    }

    LinearLayout lin_exit;
    TextView tv_exit;
    EditText et_person_one;
    EditText et_person_two;
    Button btn_submit;

    private void initView() {
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        lin_exit.setOnClickListener(this);
        tv_exit = (TextView) findViewById(R.id.tv_exit);
        tv_exit.setOnClickListener(this);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);
        et_person_one = (EditText) findViewById(R.id.et_person_one);
        et_person_two = (EditText) findViewById(R.id.et_person_two);
        et_person_one.setText(SharedPerManager.getPersonOne());
        et_person_two.setText(SharedPerManager.getPersonTwo());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                savePersonInfoToLoacal();
                break;
            case R.id.lin_exit:
            case R.id.tv_exit:
                finish();
                break;
        }
    }

    private void savePersonInfoToLoacal() {
        String personOne = getPersonOne();
        String personTwo = getPersonTwo();
        SharedPerManager.setPersonOne(personOne);
        SharedPerManager.setPersonTwo(personTwo);
        MyToastView.getInstance().Toast(PersonTotalActivity.this, getString(R.string.modifu_success));
    }

    private String getPersonOne() {
        return et_person_one.getText().toString().trim();
    }

    private String getPersonTwo() {
        return et_person_two.getText().toString().trim();
    }
}
