package com.etv.util.weather;

import com.etv.config.ApiInfo;
import com.etv.config.AppInfo;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;

public class WeatherHttpRequest {

    /***
     * 获取天气信息
     * @param listener
     */
    public static void getWeather(String localPotion, final WeatherStateListener listener) {
        int isWorkModel = SharedPerManager.getWorkModel();
        if (isWorkModel == AppInfo.WORK_MODEL_SINGLE) { //单机模式
            listener.getFailed("单机模式");
            return;
        }
        if (localPotion == null || localPotion.length() < 2) {
            listener.getFailed("城市名字不齐全");
            return;
        }
//        String url = "http://wthrcdn.etouch.cn/weather_mini?city=" + localPotion;
        String url = ApiInfo.GET_WEATHER_URL() + localPotion.trim();
        MyLog.i("weather", "===天气请求 url==" + url);
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, String errorDesc, int id) {
                        MyLog.i("weather", "===天气请求失败==" + errorDesc);
                        getWeatherFailed("天气请求失败", listener);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        MyLog.i("weather", "===天气请求成功==" + response);
                        final WeatherEntity entity = WeatherParse.parserWeather(response);
                        if (entity == null) {
                            getWeatherFailed("获取天气失败", listener);
                            return;
                        }
                        listener.getWeatherState(entity);
                    }
                });
    }

    public static void getWeatherFailed(final String desc, final WeatherStateListener listener) {
        if (listener == null) {
            return;
        }
        listener.getFailed(desc);
    }


    public interface WeatherStateListener {
        void getWeatherState(WeatherEntity entity);

        void getFailed(String desc);
    }
}
