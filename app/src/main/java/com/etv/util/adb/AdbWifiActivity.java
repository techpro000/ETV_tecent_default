package com.etv.util.adb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.etv.setting.SettingBaseActivity;
import com.etv.util.RootCmd;
import com.etv.util.apwifi.WifiMgr;
import com.ys.etv.R;
import com.ys.model.dialog.WaitDialogUtil;

public class AdbWifiActivity extends SettingBaseActivity implements View.OnClickListener {
    private TextView hint;
    private Button btn_open_close;
    private boolean toggleStatus = false;
    private TextView tv_wifi_name;
    private TextView tv_ip_address;
    private TextView tv_root_permission;
    private Handler handler = new Handler();
    WaitDialogUtil waitDialogUtil;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                    try {
                        Thread.sleep(10000);
                        int tryTimes = 0;
                        String ipaddress = WifiMgr.getLocalNetIpAddress();
                        while (ipaddress == null && tryTimes < 0) {
                            Thread.sleep(1000);
                        }
                        resetToggleStatus();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    lockToggle();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adb);
        checkCameraPermission();
        initReceiver();
    }

    private void initView() {
        waitDialogUtil = new WaitDialogUtil(AdbWifiActivity.this);
        waitDialogUtil.show("In operation...");
        btn_open_close = (Button) findViewById(R.id.btn_open_close);
        hint = (TextView) findViewById(R.id.hint);
        btn_open_close.setOnClickListener(this);
        tv_wifi_name = (TextView) findViewById(R.id.tv_wifi_name);
        tv_ip_address = (TextView) findViewById(R.id.tv_ip_address);
        tv_root_permission = (TextView) findViewById(R.id.tv_root_permission);
        tv_wifi_name.setOnClickListener(this);
        tv_ip_address.setOnClickListener(this);
        try {
            String apkRoot = "chmod 777 " + getPackageCodePath();
            boolean isRoot = RootCmd.RootCommand(apkRoot);
            Log.e("haha", "=========请求root权限状态===========" + isRoot);
        } catch (Exception e) {
            Log.e("haha", "=========00000===========" + e.toString());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_wifi_name:
            case R.id.tv_ip_address:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;
            case R.id.btn_open_close:
                if (!RootCmd.haveRoot()) {
                    showToast("No Root privileges");
                    return;
                }
                int netType = IntenetUtil.getNetworkState(AdbWifiActivity.this);
                switch (netType) {
                    case IntenetUtil.NETWORN_NONE:
                        showToast("No Net");
                        break;
                    case IntenetUtil.NETWORK_MOBILE:
                        showToast("Mobile Net");
                        break;
                    case IntenetUtil.NETWORN_WIFI:
                        openOrCloseAdb();
                        break;
                    case IntenetUtil.NETWORK_YITAI_NET:
                        changeNetYiTaiNet();
                        break;
                }
                break;
        }
    }

    public void changeNetYiTaiNet() {
        boolean ret = Utility.setAdbWifiStatus(!toggleStatus);
        if (ret) {
            toggleStatus = !toggleStatus;
            updateWifiIpInfo(toggleStatus);
        } else {
            showToast("something wrong");
        }
    }

    private void jujleWifiState() {
        int netType = IntenetUtil.getNetworkState(AdbWifiActivity.this);
        switch (netType) {
            case IntenetUtil.NETWORN_NONE:
                waitDialogUtil.dismiss();
                hint.setText("Net is not connected");
                showToast("No Net");
                break;
            case IntenetUtil.NETWORK_MOBILE:
                waitDialogUtil.dismiss();
                hint.setText("Mobile Net");
                break;
            case IntenetUtil.NETWORN_WIFI:
                Utility.setAdbWifiStatus(true);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resetToggleStatus();
                    }
                }, 1500);
                break;
            case IntenetUtil.NETWORK_YITAI_NET:
                Utility.setAdbWifiStatus(true);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resetToggleStatus();
                    }
                }, 1500);
                break;
        }
    }

    private void resetToggleStatus() {
        if (waitDialogUtil != null) {
            waitDialogUtil.dismiss();
        }
        toggleStatus = Utility.getAdbdStatus();
        updateWifiIpInfo(toggleStatus);
    }

    private void lockToggle() {
        Utility.setAdbWifiStatus(false);
        toggleStatus = false;
        updateWifiIpInfo(toggleStatus);
        hint.setText("wifi is not connected");
        btn_open_close.setText("关闭adb");
        btn_open_close.setBackgroundResource(R.drawable.rect_circle_red);
    }

    public void openOrCloseAdb() {
        waitDialogUtil.show("Dealing ...");
        if (Utility.isWifiConnected(AdbWifiActivity.this)) {
            boolean ret = Utility.setAdbWifiStatus(!toggleStatus);
            if (ret) {
                toggleStatus = !toggleStatus;
                updateWifiIpInfo(toggleStatus);
            } else {
                showToast("something wrong");
            }
        } else {
            lockToggle();
        }
    }

    /***
     * 更新wifi信息
     */
    private void updateWifiIpInfo(boolean toggleStatus) {
        waitDialogUtil.dismiss();
        if (!toggleStatus) {
            hint.setText("");
            btn_open_close.setText("Open Adb");
            btn_open_close.setBackgroundResource(R.drawable.rect_circle_blue);
            showToast("adb wifi service stopped");
        } else {
            String ipaddress = WifiMgr.getLocalNetIpAddress();
            hint.setText("adb connect " + ipaddress + ":" + String.valueOf(Utility.getPort()));
            btn_open_close.setText("Close adb");
            btn_open_close.setBackgroundResource(R.drawable.rect_circle_red);
            showToast("adb wifi service started");
        }
        //===========标题wifi信息=========================================
        int netType = IntenetUtil.getNetworkState(AdbWifiActivity.this);
        boolean isRoot = RootCmd.haveRoot();
        tv_root_permission.setText("Does the device have root : " + isRoot);
        switch (netType) {
            case IntenetUtil.NETWORN_NONE:
                tv_wifi_name.setText("WIFI Unconnected, click Setup WIFI");
                tv_ip_address.setText("WIFI Unconnected, click Setup WIFI");
                break;
            case IntenetUtil.NETWORK_MOBILE:
                tv_wifi_name.setText("Network Type: Mobile Network");
                String ipNet = WifiMgr.getLocalNetIpAddress();
                tv_ip_address.setText("IP : " + ipNet);
                break;
            case IntenetUtil.NETWORN_WIFI:
                WifiInfo wifiInfo = WifiMgr.getInstance(AdbWifiActivity.this).getWifiInfo();
                String wifiName = wifiInfo.getSSID();
                tv_wifi_name.setText("WIFI : " + wifiName);
                String ipaddress = WifiMgr.getLocalNetIpAddress();
                tv_ip_address.setText("IP : " + ipaddress);
                break;
            case IntenetUtil.NETWORK_YITAI_NET:
                tv_wifi_name.setText("Network Type: Ethernet");
                String ipNet_yitai = WifiMgr.getLocalNetIpAddress();
                tv_ip_address.setText("IP : " + ipNet_yitai);
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    private void showToast(String toast) {
        Toast.makeText(AdbWifiActivity.this, toast, Toast.LENGTH_SHORT).show();
    }

    public void checkCameraPermission() {
        initView();
        jujleWifiState();
//        Manifest.permission.ACCESS_COARSE_LOCATION
//        Manifest.permission.ACCESS_FINE_LOCATION
//        Manifest.permission.READ_EXTERNAL_STORAGE
//        Manifest.permission.WRITE_EXTERNAL_STORAGE
//        Manifest.permission.READ_PHONE_STATE
    }
}
