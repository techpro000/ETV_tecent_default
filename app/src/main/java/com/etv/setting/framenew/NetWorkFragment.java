package com.etv.setting.framenew;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.etv.activity.ImageDialogActivity;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.service.TcpService;
import com.etv.service.TcpSocketService;
import com.etv.setting.TaskInfoActivity;
import com.etv.setting.WebPageSettingActivity;
import com.etv.setting.parsener.TerminallParsener;
import com.etv.setting.view.TerminallView;
import com.etv.util.CodeUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.apwifi.WifiMgr;
import com.etv.util.location.ProCityDialogActivity;
import com.etv.util.system.CpuModel;
import com.ys.etv.databinding.FragmentNetWorkSettingBinding;
import com.ys.model.dialog.MyToastView;
import com.ys.model.dialog.WaitDialogUtil;
import com.ys.model.dialog.EditTextDialog;
import com.ys.model.dialog.OridinryDialog;
import com.ys.model.listener.EditTextDialogListener;
import com.ys.model.listener.MoreButtonListener;
import com.ys.model.listener.MoreButtonToggleListener;
import com.ys.model.listener.OridinryDialogClick;
import com.ys.etv.R;

import cn.hzw.doodle.util.LogUtil;

/**
 * 网络下发模式设置
 */
public class NetWorkFragment extends MessageFragment implements TerminallView, MoreButtonListener {

    /**
     * 百度地图地址更新成功，这里刷新界面
     */
    @Override
    public void updateNetView() {
        MyLog.message("=======onChanged==updateNetView====000==");
        if (waitDialogUtil != null) {
            waitDialogUtil.dismiss();
        }
        updateView();
    }

    FragmentNetWorkSettingBinding mBinding;

    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        mBinding = FragmentNetWorkSettingBinding.inflate(getActivity().getLayoutInflater());
        View view = mBinding.getRoot();
        initView(view);
        return view;
    }


    OridinryDialog oridinryDialog;
    WaitDialogUtil waitDialogUtil;

    private TerminallParsener terminallPansener;

    private void initView(View view) {
        mBinding.switchPlayUpdate.setOnMoretListener(new MoreButtonToggleListener() {
            @Override
            public void switchToggleView(View view, boolean isChooice) {
                MyToastView.getInstance().Toast(getActivity(), getLanguageFromResurce(R.string.net_update_chooice));
//                SharedPerManager.setPlayTotalUpdate(isChooice ? 1 : 0);
                updateView();
            }
        });

        mBinding.switchTrafficUpdate.setOnMoretListener(new MoreButtonToggleListener() {
            @Override
            public void switchToggleView(View view, boolean isChooice) {
                MyToastView.getInstance().Toast(getActivity(), getLanguageFromResurce(R.string.net_update_chooice));
//                SharedPerManager.setIfUpdateTraffToWeb(isChooice ? 1 : 0);
                updateView();
            }
        });

        mBinding.btnWifiSetting.setOnMoretListener(this);

        String code = CodeUtil.getEthMAC();
        MyLog.d("cdl", "======Mac = " + code);
        oridinryDialog = new OridinryDialog(getActivity());

        mBinding.btnWebSetting.setOnMoretListener(this);
        mBinding.btnMidifyTouchback.setOnMoretListener(this);
        mBinding.btnMidifyLocation.setOnMoretListener(this);
        mBinding.btnEthSetting.setOnMoretListener(this);
        mBinding.btnTaskPlay.setOnMoretListener(this);
        mBinding.btnSeeErCode.setOnMoretListener(this);
        String id = CodeUtil.getUniquePsuedoID();
        mBinding.btnSeeErCode.setTxtContent(id);
        mBinding.btnCommitNickname.setOnMoretListener(this);

        int workModel = SharedPerManager.getWorkModel();
        if (workModel != AppInfo.WORK_MODEL_NET) {
            mBinding.btnMidifyLocation.setVisibility(View.INVISIBLE);
            mBinding.btnCommitNickname.setVisibility(View.INVISIBLE);
        }
        editTextDialog = new EditTextDialog(getActivity());
        terminallPansener = new TerminallParsener(getActivity(), this);
        terminallPansener.queryNickName();
        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
            TcpService.getInstance().startLocationService(-1);
        } else {
            TcpSocketService.getInstance().startLocationService(-1);
        }
        hiddenView();
        updateView();
    }

    private void hiddenView() {
    }


    @Override
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.btn_midify_location:  //定位
                showLocationDialog();
                break;
            case R.id.btn_wifi_setting:
                // startActivity(new Intent(getActivity(), WifiSettingactivity.class));
                // Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
                Intent wifiSettingsIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(wifiSettingsIntent);
                break;
            case R.id.btn_task_play:
                startActivity(new Intent(getActivity(), TaskInfoActivity.class));
                break;
            case R.id.btn_midify_touchback:
                modifyTouchBackTime();
                break;
            case R.id.btn_web_setting:
                startActivity(new Intent(getActivity(), WebPageSettingActivity.class));
                break;
            case R.id.btn_commit_nickname:      //修改昵称
                int workModel = SharedPerManager.getWorkModel();
                if (workModel != AppInfo.WORK_MODEL_NET) {
                    shotToastView(getString(R.string.not_modify_nick));
                    return;
                }
                showEditSubmitDialog();
                break;
            case R.id.btn_see_er_code:  //查看设备二维码
                Intent intent = new Intent(getActivity(), ImageDialogActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_eth_setting:
//                startActivity(new Intent(getActivity(), EthernetActivity.class));
                //直接跳转到系统设置界面，不用自己得了
                Intent ethIntent = new Intent();
                ethIntent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(ethIntent);
                break;
        }
    }

    private void modifyTouchBackTime() {
        EditTextDialog editTextDialog = new EditTextDialog(getActivity());
        editTextDialog.setOnDialogClickListener(new EditTextDialogListener() {
            @Override
            public void clickHiddleView() {

            }

            @Override
            public void commit(String content) {
                if (content == null || content.length() < 1) {
                    shotToastView(getActivity().getString(R.string.insert_legitimate_num));
                    return;
                }
                long timeSubmit = SharedPerManager.getScene_task_touch_back_time();
                try {
                    timeSubmit = Integer.parseInt(content);
                } catch (Exception e) {
                    shotToastView(getActivity().getString(R.string.enter_num));
                    e.printStackTrace();
                }
                if (timeSubmit < 5) {
                    shotToastView(getActivity().getString(R.string.enter_num_five));
                    return;
                }
                SharedPerManager.setScene_task_touch_back_time(timeSubmit);
                updateView();
            }
        });
        long backTime = SharedPerManager.getScene_task_touch_back_time();
        editTextDialog.show(getActivity().getString(R.string.touch_back), backTime + "", getActivity().getString(R.string.submit));
    }

    public void onResumeView() {
        AppInfo.startCheckTaskTag = false;
        updateView();
    }

    /***
     * 点击定位，选择定位的刷新方式
     */
    private void showLocationDialog() {
        if (oridinryDialog == null) {
            oridinryDialog = new OridinryDialog(getActivity());
        }
        oridinryDialog.show(getLanguageFromResurce(R.string.chooice_refrash), getLanguageFromResurce(R.string.location_hand), getLanguageFromResurce(R.string.location_auto));
        oridinryDialog.setOnDialogClickListener(new OridinryDialogClick() {
            @Override
            public void sure() {
                startActivity(new Intent(getActivity(), ProCityDialogActivity.class));
            }

            @Override
            public void noSure() {
                boolean isWifiOpen = WifiMgr.getInstance(getActivity()).isWifiEnable();
                if (isWifiOpen) {
                    SharedPerManager.setAutoLocation(true);
                    if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
                        TcpService.getInstance().startLocationService(-1);
                    } else {
                        TcpSocketService.getInstance().startLocationService(-1);
                    }
                    return;
                }
                showOpenWifiDialog();
            }
        });
    }

    private void showOpenWifiDialog() {
        OridinryDialog oridinryDialog = new OridinryDialog(getActivity());
        oridinryDialog.setOnDialogClickListener(new OridinryDialogClick() {
            @Override
            public void sure() {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); //直接进入手机中的wifi网络设置界面
            }

            @Override
            public void noSure() {
            }
        });
        oridinryDialog.show(getLanguageFromResurce(R.string.net_wear_title), getLanguageFromResurce(R.string.submit), getLanguageFromResurce(R.string.cancel));
    }

    EditTextDialog editTextDialog;

    private void showEditSubmitDialog() {
        editTextDialog.show(getLanguageFromResurce(R.string.modify_nick), mBinding.btnCommitNickname.getTextContent(), getLanguageFromResurce(R.string.submit));
        editTextDialog.setOnDialogClickListener(new EditTextDialogListener() {
            @Override
            public void clickHiddleView() {

            }

            @Override
            public void commit(String content) {
                if (content == null || content.length() < 2) {
                    shotToastView(getLanguageFromResurce(R.string.insert_legitimate));
                    return;
                }
                if (content.length() > 50) {
                    shotToastView(getLanguageFromResurce(R.string.insert_less));
                }
                SharedPerManager.setDevNickName(content);
                mBinding.btnCommitNickname.setTxtContent(SharedPerManager.getDevNickName());
                terminallPansener.modifyNickName(content);
            }
        });
    }

    private void updateView() {
        int socketType = SharedPerUtil.SOCKEY_TYPE();
        LogUtil.e("cdl", "=======socketType=========" + socketType);
        //流量统计开关
        mBinding.switchTrafficUpdate.setSwitchStatues(SharedPerManager.getIfUpdateTraffToWeb());
        mBinding.switchTrafficUpdate.setTxtContent(SharedPerManager.getIfUpdateTraffToWeb() ? getString(R.string.open) : getString(R.string.close));
        mBinding.switchPlayUpdate.setSwitchStatues(SharedPerManager.getPlayTotalUpdate());
        mBinding.switchPlayUpdate.setTxtContent(SharedPerManager.getPlayTotalUpdate() ? getString(R.string.open) : getString(R.string.close));

        int workModel = SharedPerManager.getWorkModel();
        if (workModel == AppInfo.WORK_MODEL_NET) {
            mBinding.btnMidifyLocation.setTxtContent(SharedPerManager.getAllAddress());
            mBinding.btnCommitNickname.setTxtContent(SharedPerManager.getDevNickName());
        } else if (workModel == AppInfo.WORK_MODEL_NET_DOWN) {
            mBinding.btnMidifyLocation.setTxtContent(getString(R.string.not_modify_nick));
            mBinding.btnCommitNickname.setTxtContent(SharedPerManager.getDevNickName());
        } else if (workModel == AppInfo.WORK_MODEL_SINGLE) {
            mBinding.btnMidifyLocation.setTxtContent(getString(R.string.not_update_net));
            mBinding.btnCommitNickname.setTxtContent(SharedPerManager.getDevNickName());
        }
        terminallPansener.updateDevInfoToWeb(getActivity());
        long touchBackTime = SharedPerManager.getScene_task_touch_back_time();
        mBinding.btnMidifyTouchback.setTxtContent(touchBackTime + " ( S )");

        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            mBinding.btnMidifyTouchback.setVisibility(View.GONE);
            mBinding.btnEthSetting.setVisibility(View.GONE);
        }
    }

    @Override
    public void showWaitDialog(boolean isShow) {
        if (waitDialogUtil == null) {
            waitDialogUtil = new WaitDialogUtil(getActivity());
        }
        if (isShow) {
            waitDialogUtil.show("Dealing...");
        } else {
            waitDialogUtil.dismiss();
        }
    }

    @Override
    public void shotToastView(String toast) {
        MyToastView.getInstance().Toast(getActivity(), toast);
    }

    @Override
    public void queryNickName(boolean isSuccess, String nickName) {
        MyLog.i("cdl", "=======显示的昵称==" + nickName);
        if (isSuccess) {
            SharedPerManager.setDevNickName(nickName);
        }
        mBinding.btnCommitNickname.setTxtContent(SharedPerManager.getDevNickName());
    }

    public String getLanguageFromResurce(int resourceId) {
        String desc = getActivity().getResources().getString(resourceId);
        return desc;
    }

    public String getLanguageFromResurceWithPosition(int resourceId, String desc) {
        String stringStart = getActivity().getResources().getString(resourceId);
        String startResult = String.format(stringStart, desc);
        return startResult;
    }


}
