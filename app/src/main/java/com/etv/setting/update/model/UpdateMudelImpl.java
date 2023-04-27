package com.etv.setting.update.model;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.etv.config.ApiInfo;
import com.etv.config.AppInfo;
import com.etv.entity.AppInfomation;
import com.etv.setting.SystemApkInstallActivity;
import com.etv.setting.update.entity.UpdateInfo;
import com.etv.util.CodeUtil;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.PackgeUtil;
import com.etv.util.SharedPerManager;
import com.etv.util.system.CpuModel;
import com.etv.util.system.SystemManagerInstance;
import com.ys.model.listener.MoreButtonListener;
import com.ys.model.view.MoreButton;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class UpdateMudelImpl implements UpdateMudel {

    UpdateInfoListener listener;
    Context context;

    @Override
    public void getUpdateInfo(Context context, final UpdateInfoListener listener) {
        this.listener = listener;
        this.context = context;
        if (!NetWorkUtils.isNetworkConnected(context)) {
            overApp("当前无网络");
            return;
        }
        String requestUrl = ApiInfo.UPDATE_APP_SYSTEM();
//        http://yun.won-giant.com/webservice/selectAllUpgradeFile?clNo=301F9A86A6F89434
        String clNo = CodeUtil.getUniquePsuedoID();
        OkHttpUtils
            .post()
            .url(requestUrl)
            .addParams("clNo", clNo + "")
            .build()
            .execute(new StringCallback() {

                @Override
                public void onError(Call call, String errorDesc, int id) {
                    MyLog.update(errorDesc);
                    overApp("请求失败:" + errorDesc);
                }

                @Override
                public void onResponse(String json, int id) {
                    MyLog.update("检测升级success==" + json);
                    parsenerUpdateInfo(json);
                }
            });
    }

    private void parsenerUpdateInfo(String json) {
        if (TextUtils.isEmpty(json)) {
            overApp("请求升级信息==null:" + json);
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            int code = jsonObject.getInt("code");
            String msg = jsonObject.getString("msg");
            if (code != 0) {
                overApp(msg);
                return;
            }
            String data = jsonObject.getString("data");
            MyLog.update("====升级信息data==" + data);
            parsenerUpdateInfoData(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parsenerUpdateInfoData(String data) {
        if (TextUtils.isEmpty(data)) {
            return;
        }
        UpdateInfo updateInfo = null;
        try {
            JSONObject jsonData = new JSONObject(data);
            String serverUrl = jsonData.optString("serverUrl");
            JSONArray jsonArray = jsonData.getJSONArray("file");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjectSon = (JSONObject) jsonArray.opt(i);
                String ufOgname = jsonObjectSon.getString("ufOgName");
                String ufPackageName = jsonObjectSon.getString("ufPackageName");
                int ufVersion = jsonObjectSon.getInt("ufVersion");
                String ufSysVerson = jsonObjectSon.getString("ufSysVersion");
                long ufSize = jsonObjectSon.getLong("ufSize");
                String ufSaveUrl = jsonObjectSon.getString("ufSaveUrl");
                int ufState = jsonObjectSon.getInt("ufState");
                if (ufPackageName.equals(SharedPerManager.getPackageNameBySp())) {
                    updateInfo = new UpdateInfo(ufOgname, ufPackageName, ufVersion, ufSysVerson, ufSize, ufSaveUrl, ufState, serverUrl);
                    MyLog.update("====添加的升级信息==" + updateInfo.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        listener.getUpdateInfoSuccess(updateInfo);
    }

    private void overApp(String s) {
        if (listener == null) {
            return;
        }
        listener.overApp(s);
    }

    @Override
    public void installApk(Context context, String savePath, UpdateInfoListener listener) {
        try {
            MyLog.update("APK的安装路径===" + savePath, true);
            Intent intent = new Intent(context, SystemApkInstallActivity.class);
            intent.putExtra(SystemApkInstallActivity.FILE_UPDATE_PATH, savePath);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void installImg(Context context, String savePath, UpdateInfoListener listener) {
        try {
            if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
                SystemManagerInstance.getInstance(context).updateImageOrZipSystem(savePath);
                listener.overApp("准备升级固件");
                return;
            }
            File file = new File(savePath);
            MyLog.update("===========升级固件的文件是否存在==" + file.exists() + "/" + file.length() + " / " + savePath, true);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.YS_UPDATE_FIRMWARE");
            intent.putExtra("img_path", savePath);
            context.sendBroadcast(intent);
            listener.overApp("准备升级固件");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 如果没有升级信息，就把当前下载的所有的APK进度升级到100%
     */
    @Override
    public void updateOverProgressToWeb() {
        String requestUrl = ApiInfo.UPDATE_APP_SYSTEM_TO_OVER();
        String clNo = CodeUtil.getUniquePsuedoID();
        OkHttpUtils
            .post()
            .url(requestUrl)
            .addParams("clNo", clNo + "")
            .build()
            .execute(new StringCallback() {

                @Override
                public void onError(Call call, String errorDesc, int id) {
                    MyLog.update("检测升级修改升级进度success==" + errorDesc);
                }

                @Override
                public void onResponse(String response, int id) {
                    MyLog.update("检测升级修改升级进度success==" + response);
                }
            });
    }
}
