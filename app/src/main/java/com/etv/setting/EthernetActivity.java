package com.etv.setting;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.etv.config.AppInfo;
import com.etv.util.CodeUtil;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.view.SpinerPopWindow;
import com.etv.view.dialog.IntentDialog;
import com.ys.etv.R;
import com.ys.model.dialog.WaitDialogUtil;
import com.ys.rkapi.MyManager;

import java.util.ArrayList;
import java.util.List;

/***
 * 以太网设置界面
 */
public class EthernetActivity extends SettingBaseActivity implements View.OnClickListener {
    TextView sp_network_mode;
    TextView tv_mac_address, tv_ip_address, tv_subnet_mask, tv_gateway, tv_dns1, tv_dns2;
    private SpinerPopWindow<String> mSpinerpopWindow;
    private List<String> list;
    MyManager myManager;
    String mac_address;
    String ip_address;
    String subnet_mask;
    String gateway;
    String dns1;
    String dns2;
    IntentDialog intentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ethernet);
        initData();
        initView();
    }

    WaitDialogUtil waitDialogutil;
    LinearLayout lin_exit;

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        waitDialogutil = new WaitDialogUtil(EthernetActivity.this);
        waitDialogutil.show(getLanguageFromResurce(R.string.load_data));
        handler.sendEmptyMessageDelayed(SEARCH_ETB_INFO, 1000);

        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        lin_exit.setOnClickListener(this);
        tv_mac_address = (TextView) findViewById(R.id.tv_mac_address);
        tv_ip_address = (TextView) findViewById(R.id.tv_ip_address);
        tv_subnet_mask = (TextView) findViewById(R.id.tv_subnet_mask);
        tv_gateway = (TextView) findViewById(R.id.tv_gateway);
        tv_dns1 = (TextView) findViewById(R.id.tv_dns1);
        tv_dns2 = (TextView) findViewById(R.id.tv_dns2);
        sp_network_mode = (TextView) findViewById(R.id.sp_network_mode);
        sp_network_mode.setOnClickListener(clickListener);
        mSpinerpopWindow = new SpinerPopWindow(EthernetActivity.this, list, itemClickListener);
        mSpinerpopWindow.setOnDismissListener(dismissListener);
        myManager = MyManager.getInstance(EthernetActivity.this);
        myManager.bindAIDLService(EthernetActivity.this);
    }

    /**
     * 监听popupwindow取消
     */
    private PopupWindow.OnDismissListener dismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
//            setTextImage(R.mipmap.down);
        }
    };

    /**
     * popupwindow显示的ListView的item点击事件
     */
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mSpinerpopWindow.dismiss();

            sp_network_mode.setText(list.get(position));

            switch (position) {
                case 0:
                    SharedPerManager.setNetworkMode(getLanguageFromResurce(R.string.dynamic_acquisition));
                    myManager.setDhcpIpAddress(EthernetActivity.this);
                    onResume();
                    break;
                case 1:
                    showIntentDialog();
                    break;
            }
        }
    };

    private void showIntentDialog() {
        if (intentDialog == null) {
            intentDialog = new IntentDialog(EthernetActivity.this);
        }
        intentDialog.show(ip_address, subnet_mask, gateway, dns1, dns2);
        intentDialog.setOnDialogClickListener(new IntentDialog.EditTextInitDialogListener() {
            @Override
            public void commit(String ip, String mask, String gateway, String dns1, String dns2) {
                myManager.setStaticEthIPAddress(ip, gateway, mask, dns1, dns2);
                tv_ip_address.setText(ip);
                tv_subnet_mask.setText(mask);
                tv_gateway.setText(gateway);
                tv_dns1.setText(dns1);
                tv_dns2.setText(dns2);
                SharedPerManager.setNetworkMode(getLanguageFromResurce(R.string.single_acquisition));
            }

            @Override
            public void exit() {
                sp_network_mode.setText(SharedPerManager.getNetworkMode());
            }
        });
    }

    /**
     * 显示PopupWindow
     */
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sp_network_mode:
                    mSpinerpopWindow.setWidth(sp_network_mode.getWidth());
                    mSpinerpopWindow.showAsDropDown(sp_network_mode);
                    break;
            }
        }
    };

    /**
     * 初始化数据
     */
    private void initData() {
        list = new ArrayList<String>();
        list.add(getLanguageFromResurce(R.string.dynamic_acquisition));
        list.add(getLanguageFromResurce(R.string.single_acquisition));
    }

    /**
     * 给TextView右边设置图片
     *
     * @param resId
     */
    private void setTextImage(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());// 必须设置图片大小，否则不显示
        sp_network_mode.setCompoundDrawables(null, null, drawable, null);
    }

    private static final int SEARCH_ETB_INFO = 2345;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEARCH_ETB_INFO:
                    waitDialogutil.dismiss();
                    handler.removeMessages(SEARCH_ETB_INFO);
                    updateViewNetInfo();
                    break;
            }
        }
    };


    private void updateViewNetInfo() {
        String netTypeInfo = SharedPerManager.getNetworkMode();
        sp_network_mode.setText(netTypeInfo);
        mac_address = myManager.getEthMacAddress();
        ip_address = CodeUtil.getEthIpAddress(EthernetActivity.this, "====以太网设置界面调用====");
        boolean isNetLine = NetWorkUtils.isNetworkConnected(EthernetActivity.this);
        if (isNetLine) {
            int netType = NetWorkUtils.getNetworkState(EthernetActivity.this);
            if (netType != NetWorkUtils.NETWORK_ETH_NET) {
                ip_address = "0.0.0.0";
            }
        }
        subnet_mask = myManager.getNetMask();
        gateway = myManager.getGateway();
        dns1 = myManager.getEthDns1();
        dns2 = myManager.getEthDns2();

        MyLog.cdl("====获取的yanma==" + subnet_mask + " / " + dns1 + " / " + dns2 + " / " + gateway);
        tv_mac_address.setText(mac_address);
        tv_ip_address.setText(ip_address);
        tv_subnet_mask.setText(subnet_mask);
        tv_gateway.setText(gateway);
        tv_dns1.setText(dns1);
        tv_dns2.setText(dns2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeMessages(SEARCH_ETB_INFO);
        }
        if (myManager != null) {
            myManager.unBindAIDLService(EthernetActivity.this);
        }
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
