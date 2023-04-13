package com.etv.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.etv.util.system.SystemManagerInstance;
import com.ys.etv.R;

public class TestActivity extends AppCompatActivity {


    Button login, logout, send;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
    }


    private Handler handler = new Handler();

    //    MyManager myManager ;
    void initView() {
//        myManager = MyManager.getInstance(TestActivity.this);
//        myManager.bindAIDLService(TestActivity.this);

        login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                myManager.turnOffBackLight();
                SystemManagerInstance.getInstance(TestActivity.this).turnBackLightTtatues(false);

            }
        });

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                myManager.turnOffBackLight();
                SystemManagerInstance.getInstance(TestActivity.this).turnBackLightTtatues(true);
            }
        });

    }


}