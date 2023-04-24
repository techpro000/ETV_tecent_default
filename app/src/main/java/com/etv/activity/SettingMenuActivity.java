//package com.etv.activity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.etv.config.AppConfig;
//import com.etv.config.AppInfo;
//import com.etv.setting.WorkChoiceActivity;
//import com.etv.util.CodeUtil;
//import com.etv.util.MyLog;
//import com.etv.util.QRCodeUtil;
//import com.etv.util.SharedPerManager;
//import com.etv.util.SharedPerUtil;
//import com.ys.bannerlib.util.GlideImageUtil;
//import com.ys.etv.R;
//import com.ys.model.dialog.MyToastView;
//import com.ys.model.util.ActivityCollector;
//
//
///**
// * 弹窗类 ACTIVITY
// */
//public class SettingMenuActivity extends BaseActivity implements View.OnClickListener {
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dialog_setting_menu);
//        initView();
//    }
//
//    Button btn_work_model;
//    Button btn_exit;
//    RelativeLayout rela_dialog_bgg;
//    ImageView iv_qr_code_scan;
//    TextView tv_ip, tv_mac, tv_version_sys, tv_version_app, tv_nickname, tv_line_type;
//    LinearLayout lin_wechat_code, lin_bind_ercode, lin_bottom_info, lin_all_qrcode;
//    ImageView iv_qr_code_chat;
//    TextView tv_scan_view;
//
//    private void initView() {
//        iv_qr_code_scan = (ImageView) findViewById(R.id.iv_qr_code_scan);
//        btn_work_model = (Button) findViewById(R.id.btn_work_model);
//        btn_exit = (Button) findViewById(R.id.btn_exit);
//        rela_dialog_bgg = (RelativeLayout) findViewById(R.id.rela_dialog_bgg);
//        tv_ip = (TextView) findViewById(R.id.tv_ip);
//        tv_mac = (TextView) findViewById(R.id.tv_mac);
//        tv_version_sys = (TextView) findViewById(R.id.tv_version_sys);
//        tv_version_app = (TextView) findViewById(R.id.tv_version_app);
//        tv_nickname = (TextView) findViewById(R.id.tv_nickname);
//        tv_line_type = (TextView) findViewById(R.id.tv_line_type);
//        tv_scan_view = (TextView) findViewById(R.id.tv_scan_view);
//        iv_qr_code_chat = (ImageView) findViewById(R.id.iv_qr_code_chat);
//        rela_dialog_bgg.setOnClickListener(this);
//        btn_work_model.setOnClickListener(this);
//        btn_exit.setOnClickListener(this);
//        btn_work_model.requestFocus();
//        lin_wechat_code = (LinearLayout) findViewById(R.id.lin_wechat_code);
//        lin_bind_ercode = (LinearLayout) findViewById(R.id.lin_bind_ercode);
//        lin_bottom_info = (LinearLayout) findViewById(R.id.lin_bottom_info);
//        lin_all_qrcode = (LinearLayout) findViewById(R.id.lin_all_qrcode);
//        hiddleConsumerView();
//        showViewInfo();
//    }
//
//
//    public void showViewInfo() {
//        Log.e("5555", "====焦点状态==弹窗显示==");
//        try {
//            String ipaddress = CodeUtil.getIpAddress(SettingMenuActivity.this, "主界面弹窗调用");
//            tv_ip.setText(ipaddress);
//            tv_mac.setText(CodeUtil.getUniquePsuedoID());
//            String sysCodeVersion = CodeUtil.getSysVersion();
//            String appCodeVersion = CodeUtil.getAppVersion(SettingMenuActivity.this);
//            tv_version_sys.setText(sysCodeVersion);
//            tv_version_app.setText(appCodeVersion);
//            tv_nickname.setText(SharedPerManager.getDevNickName());
//            btn_exit.setText(getString(R.string.exit_app));
//            tv_line_type.setText(SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET ? "WebSocket" : "Socket");
//            createErCode();
//            btn_work_model.requestFocus();
//            handler.sendEmptyMessageDelayed(DISSMISS_DIALOG_AUTO, 15 * 1000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    String imagePath = AppInfo.ER_CODE_PATH();
//
//    private void createErCode() {
//        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_LK_QRCODE) {
//            GlideImageUtil.loadImageById(SettingMenuActivity.this, R.mipmap.lk_qrcode, iv_qr_code_scan);
//            tv_scan_view.setText(getString(R.string.scan_dev_code));
//            return;
//        }
//        String devCode = CodeUtil.getUniquePsuedoID();
//        String ipConnect = SharedPerUtil.getWebHostIpAddress();
//        String isLine = "-1";
//        if (AppConfig.isOnline) {
//            isLine = "1";
//        } else {
//            isLine = "-1";
//        }
//        String nickName = SharedPerManager.getDevNickName();
//        String guardianCode = "{\"type\":\"bind\",\"code\":\"" + devCode + "\",\"ip\":\"" + ipConnect + "\",\"isLine\":\"" + isLine + "\",\"nickName\":\"" + nickName + "\"}";
//        MyLog.cdl("====生成二维码===start===" + System.currentTimeMillis() + " / " + guardianCode);
//        QRCodeUtil qrCodeUtil = new QRCodeUtil(SettingMenuActivity.this, new QRCodeUtil.ErCodeBackListener() {
//
//            @Override
//            public void createErCodeState(String errorDes, boolean isCreate, String path) {
//                MyLog.cdl("====生成二维码===" + System.currentTimeMillis() + " / " + errorDes + " / " + isCreate + " / " + path);
//                if (isCreate) {
//                    GlideImageUtil.loadImageNoCache(SettingMenuActivity.this, imagePath, iv_qr_code_scan);
//                } else {
//                    MyToastView.getInstance().Toast(SettingMenuActivity.this, "创建二维码失败");
//                }
//            }
//        });
//        qrCodeUtil.createErCode(guardianCode, imagePath);
//    }
//
//    private static final int DISSMISS_DIALOG_AUTO = 4521;
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case DISSMISS_DIALOG_AUTO:
//                    finiActivityMenu();
//                    break;
//            }
//        }
//    };
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.rela_dialog_bgg:
//                finiActivityMenu();
//                break;
//            case R.id.btn_work_model:
//                startActivity(new Intent(SettingMenuActivity.this, WorkChoiceActivity.class));
//                finish();
//                break;
//            case R.id.btn_exit:
//                showExitBaseDialog(1);
//                ActivityCollector.finishAll();
//                finish();
//                break;
//        }
//    }
//
//    private void finiActivityMenu() {
//        startActivity(new Intent(SettingMenuActivity.this, MainActivity.class));
//        finish();
//    }
//
//    private void hiddleConsumerView() {
//        switch (AppConfig.APP_TYPE) {
//            case AppConfig.APP_TYPE_DEFAULT:
//            case AppConfig.APP_TYPE_LK_QRCODE:
//            case AppConfig.APP_TYPE_THREE_VIEW_STAND:
//                lin_wechat_code.setVisibility(View.VISIBLE);
//                iv_qr_code_chat.setBackgroundResource(R.mipmap.icon_etv_code);
//                break;
//            case AppConfig.APP_TYPE_HUANGZUNNIANHUA:
//            case AppConfig.APP_TYPE_HUANGZUNNIANHUA_ONE_LEY_POLICE:
//            case AppConfig.APP_TYPE_HUANGZUNNIANHUA_DEFAULT_SIZE:
//            case AppConfig.APP_TYPE_HUANGZUNNIANHUA_MATCH_SIZE:
//                lin_wechat_code.setVisibility(View.VISIBLE);
//                iv_qr_code_chat.setBackgroundResource(R.mipmap.icon_qr_hznh);
//                break;
//            case AppConfig.APP_TYPE_HUANGZUNNIANHUA_YUN_OLD_PERSON:
//                lin_all_qrcode.setVisibility(View.GONE);
////                lin_bind_ercode.setVisibility(View.GONE);
//                break;
//            case AppConfig.APP_TYPE_QINGFENG_NOT_QR:
//            case AppConfig.APP_TYPE_SENHAN:
//                lin_all_qrcode.setVisibility(View.GONE);
//                lin_bottom_info.setVisibility(View.GONE);
//                break;
//
//            default:
//                lin_wechat_code.setVisibility(View.VISIBLE);
//                iv_qr_code_chat.setBackgroundResource(R.mipmap.icon_etv_code);
//                break;
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        handler.removeMessages(DISSMISS_DIALOG_AUTO);
//        super.onStop();
//    }
//
//
//}
