package com.etv.setting.update.parsener;

import android.content.Context;

import com.etv.setting.update.entity.UpdateInfo;
import com.etv.setting.update.model.UpdateInfoListener;
import com.etv.setting.update.model.UpdateMudel;
import com.etv.setting.update.model.UpdateMudelImpl;
import com.etv.setting.update.view.UpdateView;
import com.etv.util.MyLog;

import java.util.List;

public class UpdateParsener implements UpdateInfoListener {

    UpdateView updateView;
    Context context;
    UpdateMudel updateMudel;

    public UpdateParsener(Context context, UpdateView updateView) {
        this.context = context;
        this.updateView = updateView;
        updateMudel = new UpdateMudelImpl();
    }

    public void installFile(String savePath) {
        if (savePath.endsWith(".apk")) {
            updateMudel.installApk(context, savePath, this);
        } else if (savePath.endsWith(".img") || savePath.endsWith(".zip")) {
            updateMudel.installImg(context, savePath, this);
        }
    }

    /***
     * 获取手机APP里面所有的app信息*/

    public void getUpdateInfo() {
        updateMudel.getUpdateInfo(context, this);
    }

    @Override
    public void getUpdateInfoSuccess(UpdateInfo updateInfo) {
        if (updateInfo == null) {
            MyLog.update("没有需要升级得APK 信息");
            return;
        }
        updateView.updateMainView(updateInfo);
    }

    /***
     *终结APP界面
     * @param errorDesc
     */
    public void overApp(String errorDesc) {
        updateView.updateOver(errorDesc);
    }

    public void updateOverProgressToWeb() {
        updateMudel.updateOverProgressToWeb();
    }
}
