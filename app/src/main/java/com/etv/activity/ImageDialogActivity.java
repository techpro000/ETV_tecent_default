package com.etv.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etv.setting.SettingBaseActivity;
import com.etv.util.QRCodeUtil;
import com.ys.bannerlib.util.GlideImageUtil;
import com.ys.model.dialog.MyToastView;
import com.ys.etv.R;
import com.etv.config.AppInfo;
import com.etv.util.CodeUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/***
 * 终端设置 , 二维码界面显示
 */
public class ImageDialogActivity extends SettingBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_image_dialog);
        initView();
        createErCode();
    }

    private void createErCode() {

        String guardianCode = CodeUtil.getUniquePsuedoID();

        QRCodeUtil qrCodeUtil = new QRCodeUtil(ImageDialogActivity.this, new QRCodeUtil.ErCodeBackListener() {

            @Override
            public void createErCodeState(String errorDes, boolean isCreate,String path) {

                if (isCreate) {
//                    showImageView();
//                    GlideImageUtil.loadBitmap(getApplicationContext(), bitmap, iv_er_code);

                    GlideImageUtil.loadImageByPath(ImageDialogActivity.this, imagePath, iv_er_code);
                } else {
                    MyToastView.getInstance().Toast(ImageDialogActivity.this, "创建二维码失败");
                }
            }
        });
        qrCodeUtil.createErCode(guardianCode, imagePath);

    }

    ImageView iv_er_code;
    TextView tv_er_ceode;
    LinearLayout lin_exit;
    TextView tv_exit;
    String imagePath = AppInfo.ER_CODE_PATH();


    private void initView() {
        AppInfo.startCheckTaskTag = false;
        String guardianCode = CodeUtil.getUniquePsuedoID();
        tv_er_ceode = (TextView) findViewById(R.id.tv_er_ceode);
        tv_er_ceode.setText(getString(R.string.device_name) + guardianCode);
        iv_er_code = (ImageView) findViewById(R.id.iv_er_code);
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        tv_exit = (TextView) findViewById(R.id.tv_exit);
        lin_exit.setOnClickListener(this);
        tv_exit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lin_exit:
            case R.id.tv_exit:
                finish();
                break;
        }
    }
}
