package com.etv.util.rxjava;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.etv.task.entity.DownStatuesEntity;

/***
 * 软件状态监听
 */
public class AppStatuesListener extends ViewModel {

    public static AppStatuesListener instance;

    public static AppStatuesListener getInstance() {
        if (instance == null) {
            synchronized (AppStatuesListener.class) {
                if (instance == null) {
                    instance = new AppStatuesListener();
                }
            }
        }
        return instance;
    }


    public static final int LIVE_DATA_POWERONOFF = 5566;  //定时开关机设置完成，这里需要刷新界面
    public static final int LIVE_DATA_SCREEN_CATPTURE = 5567;  //开始截图得通知
//    public static final int LIVE_DATA_SAVE_POWERONOFF_LOG = 5568;  //save  poweronoff log info

    /***
     * default
     */
    public MutableLiveData<Integer> objectLiveDate = new MutableLiveData<Integer>();

    //网络变化
    public MutableLiveData<Boolean> NetChange = new MutableLiveData<Boolean>();
    // 用来更新系统时间得
//    public MutableLiveData<String> timeChangeEvent = new MutableLiveData<String>();
    //用来更新主界面得通知类
    public MutableLiveData<String> UpdateMainBggEvent = new MutableLiveData<String>();
    //更新播放界面音量得问题
    public MutableLiveData<String> UpdateMainMediaVoiceEvent = new MutableLiveData<String>();
    //下载状态更新
    public MutableLiveData<DownStatuesEntity> DownStatuesEntity = new MutableLiveData<DownStatuesEntity>();

}
