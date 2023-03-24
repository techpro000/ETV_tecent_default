package com.etv.util.guardian;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.etv.config.AppInfo;
import com.etv.entity.RawSourceEntity;
import com.etv.http.util.FileRawWriteRunnable;
import com.etv.listener.WriteSdListener;
import com.etv.service.EtvService;
import com.etv.util.MyLog;
import com.etv.util.RootCmd;
import com.etv.util.system.CpuModel;
import com.ys.model.dialog.MyToastView;

import java.util.ArrayList;
import java.util.List;

/**
 * 安装截图
 */
public class InstallApkBackUtil {

    Context context;

    public InstallApkBackUtil(Context context) {
        this.context = context;
    }

    /**
     * 安装
     */
    public void installFingerSoFile() {
        RawSourceEntity rawSourceEntity = getResourceEntity();
        if (rawSourceEntity == null) {
            MyToastView.getInstance().Toast(context, "System does not support !");
            return;
        }
        long APK_BACK_FILE_LENGTH = rawSourceEntity.getFileLength();
        if (APK_BACK_FILE_LENGTH < 100) {
            //这里是屏蔽 3399的
            return;
        }
        MyLog.i("write", "==获取的raw守护信息===" + rawSourceEntity.toString());
        int rourseId = rawSourceEntity.getRawId();
        long fileLength = rawSourceEntity.getFileLength();
        String savePath = AppInfo.BASE_CACHE() + "/" + AppInfo.APK_BACK_FILE_NAME;
        FileRawWriteRunnable runnable = new FileRawWriteRunnable(context, rourseId, savePath, fileLength, new WriteSdListener() {
            @Override
            public void writeProgress(int progress) {
                Log.e("write", "===progress===" + progress);
            }

            @Override
            public void writeSuccess(String savePath) {
                MyLog.i("write", "suucess==" + savePath);
                RootCmd.writeFileFingerToSystemLib(savePath, "/system/lib/libinputflinger.so");
            }

            @Override
            public void writrFailed(String errorDesc) {
                Log.e("write", "writrFailed==" + errorDesc);
            }
        });
        runnable.setIdDelOldFile(true);
        EtvService.getInstance().executor(runnable);
    }

    public static RawSourceEntity getResourceEntity() {
        RawSourceEntity rawSourceEntity = null;
        List<RawSourceEntity> lists = new ArrayList<RawSourceEntity>();
        //lists.add(new RawSourceEntity(R.raw.libinputflinger, 276068, "APK跳转返回3288-7.1", 1));
        try { //7.0以下系统
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                return null;
            }
            String cpuModel = CpuModel.getMobileType();
            MyLog.d("write", "===cpuModel===" + cpuModel);
            if (cpuModel.contains("rk3288")) {
                rawSourceEntity = lists.get(0);
            } else if (cpuModel.contains("rk3399")) {
                rawSourceEntity = new RawSourceEntity();
            } else {
                return null;
            }
        } catch (Exception e) {
            MyLog.d("write", "=====获取守护进程Raw id error==" + e.toString());
            e.printStackTrace();
        }
        return rawSourceEntity;
    }
}
