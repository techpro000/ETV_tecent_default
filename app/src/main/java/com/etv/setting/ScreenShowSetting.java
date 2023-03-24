package com.etv.setting;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etv.config.AppInfo;
import com.etv.service.util.EtvServerModule;
import com.etv.service.util.EtvServerModuleImpl;
import com.etv.service.util.TaskServiceView;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.system.CpuModel;
import com.etv.view.dialog.CheckChooiceCustomDialog;
import com.ys.model.dialog.EditTextDialog;
import com.ys.etv.R;
import com.ys.model.dialog.RadioListDialog;
import com.ys.model.entity.RedioEntity;
import com.ys.model.listener.EditTextDialogListener;
import com.ys.model.listener.MoreButtonListener;
import com.ys.model.listener.MoreButtonToggleListener;
import com.ys.model.listener.RadioChooiceListener;
import com.ys.model.view.SettingClickView;
import com.ys.model.view.SettingSwitchView;

import java.util.ArrayList;
import java.util.List;

/***
 * 屏幕显示类型相关设置界面
 */
public class ScreenShowSetting extends SettingBaseActivity implements View.OnClickListener, MoreButtonListener {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.setting_screen_show);
        initView();
        initListener();
    }

    private LinearLayout lin_exit;
    private TextView tv_exit;
    private SettingClickView btn_pic_show_type, btn_video_show_type;
    private SettingClickView btn_touch_show_type;
    private SettingClickView btn_double_show_type;
    private SettingClickView btn_double_image_roate;
    private SettingClickView btn_capture_quetity;
    private SettingClickView btn_wps_show_type;  //wps 文档
    private SettingClickView btn_wps_animal_type;
    private SettingClickView btn_image_load_type;  //图片加载框架
    SettingSwitchView switch_info_from;

    private void initView() {
        switch_info_from = (SettingSwitchView) findViewById(R.id.switch_info_from);
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        tv_exit = (TextView) findViewById(R.id.tv_exit);
        lin_exit.setOnClickListener(this);
        tv_exit.setOnClickListener(this);
        btn_video_show_type = (SettingClickView) findViewById(R.id.btn_video_show_type);
        btn_video_show_type.setOnMoretListener(this);
        btn_touch_show_type = (SettingClickView) findViewById(R.id.btn_touch_show_type);
        btn_touch_show_type.setOnMoretListener(this);
        btn_pic_show_type = (SettingClickView) findViewById(R.id.btn_pic_show_type);
        btn_pic_show_type.setOnMoretListener(this);
        btn_double_show_type = (SettingClickView) findViewById(R.id.btn_double_show_type);
        btn_double_show_type.setOnMoretListener(this);
        btn_double_image_roate = (SettingClickView) findViewById(R.id.btn_double_image_roate);
        btn_double_image_roate.setOnMoretListener(this);
        btn_capture_quetity = (SettingClickView) findViewById(R.id.btn_capture_quetity);
        btn_capture_quetity.setOnMoretListener(this);
        btn_wps_show_type = (SettingClickView) findViewById(R.id.btn_wps_show_type);
        btn_wps_show_type.setOnMoretListener(this);
        btn_wps_animal_type = (SettingClickView) findViewById(R.id.btn_wps_animal_type);
        btn_wps_animal_type.setOnMoretListener(this);
        btn_image_load_type = (SettingClickView) findViewById(R.id.btn_image_load_type);
        btn_image_load_type.setOnMoretListener(this);
        showIrHiddleView();
    }

    private void showIrHiddleView() {
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            btn_touch_show_type.setVisibility(View.GONE);
            btn_double_show_type.setVisibility(View.GONE);
            btn_double_image_roate.setVisibility(View.GONE);
            btn_image_load_type.setVisibility(View.GONE);
        }

        if (CpuModel.CPU_MODEL_3566_11.equals(CpuModel.getMobileType())) {
            btn_touch_show_type.setVisibility(View.GONE);
            btn_double_show_type.setVisibility(View.GONE);
            btn_double_image_roate.setVisibility(View.GONE);
            btn_image_load_type.setVisibility(View.GONE);
        }
    }


    private void initListener() {
        switch_info_from.setOnMoretListener(new MoreButtonToggleListener() {
            @Override
            public void switchToggleView(View view, boolean isChooice) {
                if (isChooice) {
                    checkInfoFromweb();
                }
                SharedPerManager.setInfoFrom(isChooice);
                updateShowView("修改信息来源");
            }
        });
    }

    EtvServerModule etvServerModule;

    private void checkInfoFromweb() {
        if (!NetWorkUtils.isNetworkConnected(ScreenShowSetting.this)) {
            return;
        }
        if (etvServerModule == null) {
            etvServerModule = new EtvServerModuleImpl();
        }
        etvServerModule.queryDeviceInfoFromWeb(ScreenShowSetting.this, new TaskServiceView() {

            @Override
            public void getDevInfoFromWeb(boolean isSuccess, String errorDesc) {

            }
        });
    }


    @Override
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.btn_image_load_type:
                showImageShowTypeDialog();
                break;
            case R.id.btn_wps_animal_type:
                showWpsShowAnimalTypeDialog();
                break;
            case R.id.btn_wps_show_type:
                showWpsShowTypeDialog();
                break;
            case R.id.btn_capture_quetity:
                showCaptureImageQuity();
                break;
            case R.id.btn_double_image_roate:
                showDoubleImageRoateDialog();
                break;
            case R.id.btn_double_show_type:
                showDoubleScreenShowType();
                break;
            case R.id.btn_touch_show_type:
                showTouchShowType();
                break;
            case R.id.btn_video_show_type:
                showVideoShowTypeDialog();
                break;
            case R.id.btn_pic_show_type:
                showPicShowTypeDialog();
                break;
        }
    }


    /***
     * 图片加载框架
     *
     */
    private void showImageShowTypeDialog() {
        int showType = SharedPerUtil.getImageShowType();
        RadioListDialog radioListDialog = new RadioListDialog(ScreenShowSetting.this);
        List<RedioEntity> list = new ArrayList<RedioEntity>();
        list.add(new RedioEntity("Glide"));
        list.add(new RedioEntity("Fresco"));
        radioListDialog.show("ImageShow", list, showType);
        radioListDialog.setRadioChooiceListener(new RadioChooiceListener() {
            @Override
            public void backChooiceInfo(RedioEntity redioEntity, int chooicePosition) {
                SharedPerManager.setImageShowType(chooicePosition);
                showToastView("Success");
                updateShowView("设置图片的加载框架");
            }
        });
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

    /***
     * 文档切换动画
     */
    private void showWpsShowAnimalTypeDialog() {
        int showType = SharedPerManager.geWPSSingleShowAnimalTYpe();
        RadioListDialog radioListDialog = new RadioListDialog(ScreenShowSetting.this);
        List<RedioEntity> list = new ArrayList<RedioEntity>();
        list.add(new RedioEntity(getString(R.string.animal_left_right)));
        list.add(new RedioEntity(getString(R.string.animal_top_bottom)));
        radioListDialog.show("Type", list, showType);
        radioListDialog.setRadioChooiceListener(new RadioChooiceListener() {
            @Override
            public void backChooiceInfo(RedioEntity redioEntity, int chooicePosition) {
                SharedPerManager.setWPSSingleShowAnimalTYpe(chooicePosition);
                showToastView("Success");
                updateShowView("设置图片的显示类型");
            }
        });
    }


    private void showWpsShowTypeDialog() {
        int showType = SharedPerManager.geWPSSingleShowTYpe();
        RadioListDialog radioListDialog = new RadioListDialog(ScreenShowSetting.this);
        List<RedioEntity> list = new ArrayList<RedioEntity>();
        list.add(new RedioEntity(getString(R.string.all_screen_change)));
        list.add(new RedioEntity(getString(R.string.size_screen_change)));
        radioListDialog.show("Type", list, showType);
        radioListDialog.setRadioChooiceListener(new RadioChooiceListener() {
            @Override
            public void backChooiceInfo(RedioEntity redioEntity, int chooicePosition) {
                SharedPerManager.setWPSSingleShowTYpe(chooicePosition, "单机模式，手动设置");
                showToastView("Success");
                updateShowView("设置图片的显示类型");
            }
        });
    }

    private void showCaptureImageQuity() {
        RadioListDialog radioListDialog = new RadioListDialog(ScreenShowSetting.this);
        List<RedioEntity> listShow = new ArrayList<RedioEntity>();
        listShow.add(new RedioEntity(getString(R.string.quetity_low)));
        listShow.add(new RedioEntity(getString(R.string.quetity_middle)));
        listShow.add(new RedioEntity(getString(R.string.quetity_height)));
        int roateNum = SharedPerManager.getCapturequilty();
        radioListDialog.show(getString(R.string.capture_update_height), listShow, roateNum);
        radioListDialog.setRadioChooiceListener(new RadioChooiceListener() {
            @Override
            public void backChooiceInfo(RedioEntity redioEntity, int chooicePosition) {
                SharedPerManager.setCapturequilty(chooicePosition);
                showToastView(getString(R.string.set_success));
                updateShowView("双屏图片角度设置");
            }
        });
    }

    private void showDoubleImageRoateDialog() {
        RadioListDialog radioListDialog = new RadioListDialog(ScreenShowSetting.this);
        List<RedioEntity> listShow = new ArrayList<RedioEntity>();
        listShow.add(new RedioEntity("0"));
        listShow.add(new RedioEntity("90"));
        listShow.add(new RedioEntity("180"));
        listShow.add(new RedioEntity("270"));
        int roateNum = SharedPerManager.getDoubleScreenRoateImage();
        int checkPosition = 0;
        if (roateNum == 0) {
            checkPosition = 0;
        } else if (roateNum == 90) {
            checkPosition = 1;
        } else if (roateNum == 180) {
            checkPosition = 2;
        } else if (roateNum == 270) {
            checkPosition = 3;
        }
        radioListDialog.show("副屏截图旋转角度", listShow, checkPosition);
        radioListDialog.setRadioChooiceListener(new RadioChooiceListener() {
            @Override
            public void backChooiceInfo(RedioEntity redioEntity, int chooicePosition) {
                int roateNum = 0;
                if (chooicePosition == 0) {
                    roateNum = 0;
                } else if (chooicePosition == 1) {
                    roateNum = 90;
                } else if (chooicePosition == 2) {
                    roateNum = 180;
                } else if (chooicePosition == 3) {
                    roateNum = 270;
                }
                SharedPerManager.setDoubleScreenRoateImage(roateNum);
                showToastView(getString(R.string.set_success));
                updateShowView("双屏图片角度设置");
            }
        });
    }

    /***
     * 双屏异显算法
     */
    private void showDoubleScreenShowType() {
        List<RedioEntity> lists = new ArrayList<>();
        lists.add(new RedioEntity(getString(R.string.default_size_show)));
        lists.add(new RedioEntity(getString(R.string.ajust_screen)));
        lists.add(new RedioEntity(getString(R.string.Interface_reverse)));
        lists.add(new RedioEntity(getString(R.string.long_width_change)));
        int showType = SharedPerManager.getDoubleScreenMath();
        RadioListDialog radioListDialog = new RadioListDialog(ScreenShowSetting.this);
        radioListDialog.setRadioChooiceListener(new RadioChooiceListener() {
            @Override
            public void backChooiceInfo(RedioEntity redioEntity, int chooicePosition) {
                SharedPerManager.setDoubleScreenMath(chooicePosition);
                showToastView("Success");
                updateShowView("设置图片的显示类型");
            }
        });
        radioListDialog.show("Type", lists, showType);
    }

    /**
     * 互动显示格式
     */
    private void showTouchShowType() {
        EditTextDialog editTextDialog = new EditTextDialog(ScreenShowSetting.this);
        editTextDialog.setOnDialogClickListener(new EditTextDialogListener() {
            @Override
            public void clickHiddleView() {

            }

            @Override
            public void commit(String content) {
                try {
                    int timeChange = Integer.parseInt(content);
                    if (timeChange < 500 || timeChange > 2000) {
                        showToastView("请输入500~2000之间得数字,值越大动画越慢");
                        return;
                    }
                    SharedPerManager.setPicSwitchingTime(timeChange);
                    showToastView(getString(R.string.set_success));
                    updateShowView("修改动画切换时间");
                } catch (Exception e) {
                    e.printStackTrace();
                    showToastView("请输入时间数字");
                }
            }
        });
        int pidTransTime = SharedPerManager.getPicSwitchingTime();
        editTextDialog.show("图片动画时间(毫秒)", pidTransTime + "", "提交");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateShowView("onResume");
    }

    private void showVideoShowTypeDialog() {
        int showType = SharedPerManager.geVideoSingleShowTYpe();
        RadioListDialog radioListDialog = new RadioListDialog(ScreenShowSetting.this);
        List<RedioEntity> list = new ArrayList<RedioEntity>();
        list.add(new RedioEntity(getString(R.string.all_screen_change)));
        list.add(new RedioEntity(getString(R.string.size_screen_change)));
        radioListDialog.show("Type", list, showType);
        radioListDialog.setRadioChooiceListener(new RadioChooiceListener() {
            @Override
            public void backChooiceInfo(RedioEntity redioEntity, int chooicePosition) {
                SharedPerManager.setVideoSingleShowTYpe(chooicePosition);
                showToastView("Success");
                updateShowView("设置图片的显示类型");
            }
        });
    }

    /***
     * 图片缩放类型
     */
    private void showPicShowTypeDialog() {
        int showType = SharedPerManager.getPicSingleShowTYpe();
        RadioListDialog radioListDialog = new RadioListDialog(ScreenShowSetting.this);
        List<RedioEntity> list = new ArrayList<RedioEntity>();
        list.add(new RedioEntity(getString(R.string.all_screen_change)));
        list.add(new RedioEntity(getString(R.string.size_screen_change)));
        radioListDialog.show("Type", list, showType);
        radioListDialog.setRadioChooiceListener(new RadioChooiceListener() {
            @Override
            public void backChooiceInfo(RedioEntity redioEntity, int chooicePosition) {
                MyLog.netty("====backChooiceInfo=====chooicePosition==" + chooicePosition);
                SharedPerManager.setPicSingleShowTYpe(chooicePosition);
                showToastView("Success");
                updateShowView("设置图片的显示类型");
            }
        });
    }

    private void updateShowView(String tag) {
        btn_image_load_type.setTxtContent(SharedPerUtil.getImageShowType() == AppInfo.IMAGE_TYPE_GLIDE ? "Glide" : "Fresco");
        boolean isFromWeb = SharedPerManager.getInfoFrom();
        switch_info_from.setSwitchStatues(isFromWeb);
        switch_info_from.setTxtContent(isFromWeb ? getString(R.string.screen_double_net_model) : getString(R.string.screen_double_net_local));
        int showTypeAnimal = SharedPerManager.geWPSSingleShowAnimalTYpe();
        if (showTypeAnimal == 0) {
            btn_wps_animal_type.setTxtContent(getString(R.string.animal_left_right));
        } else {
            btn_wps_animal_type.setTxtContent(getString(R.string.animal_top_bottom));
        }
        int showWpsType = SharedPerManager.geWPSSingleShowTYpe();
        btn_wps_show_type.setTxtContent(showWpsType == 0 ? getString(R.string.all_screen_change)
                : getString(R.string.size_screen_change));
        MyLog.cdl("========界面刷新==" + tag);
        int videoShowType = SharedPerManager.geVideoSingleShowTYpe();
        btn_video_show_type.setTxtContent(videoShowType == 0 ? getString(R.string.all_screen_change)
                : getString(R.string.size_screen_change));
        int picShowTYPE = SharedPerManager.getPicSingleShowTYpe();
        btn_pic_show_type.setTxtContent(picShowTYPE == 0 ? getString(R.string.all_screen_change)
                : getString(R.string.size_screen_change));
        int pidTransTime = SharedPerManager.getPicSwitchingTime();
        btn_touch_show_type.setTxtContent(pidTransTime + " ms");
        int showType = SharedPerManager.getDoubleScreenMath();
        String doublwShow;
        if (showType == AppInfo.DOUBLE_SCREEN_SHOW_DEFAULT) {
            doublwShow = getString(R.string.default_size_show);
        } else if (showType == AppInfo.DOUBLE_SCREEN_SHOW_ADAPTER) {
            doublwShow = getString(R.string.ajust_screen);
        } else if (showType == AppInfo.DOUBLE_SCREEN_SHOW_GT_TRANS) {
            //高通反向
            doublwShow = getString(R.string.Interface_reverse);
        } else {
            //长宽志换
            doublwShow = getString(R.string.long_width_change);
        }
        btn_double_show_type.setTxtContent(doublwShow);
        int roateNum = SharedPerManager.getDoubleScreenRoateImage();
        btn_double_image_roate.setTxtContent(roateNum + "");

        int captureType = SharedPerManager.getCapturequilty();
        if (captureType == 0) {
            btn_capture_quetity.setTxtContent(getString(R.string.quetity_low));
        } else if (captureType == 1) {
            btn_capture_quetity.setTxtContent(getString(R.string.quetity_middle));
        } else if (captureType == 2) {
            btn_capture_quetity.setTxtContent(getString(R.string.quetity_height));
        }
    }
}