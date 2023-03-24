package com.etv.util.weather;

import android.widget.ImageView;

import com.etv.util.MyLog;
import com.ys.etv.R;

import org.json.JSONObject;

public class WeatherParse {
    /***
     * 解析天气
     *
     * @param desc
     * @return
     */

//    {"code":0,"msg":"查询成功！","data":{"high":"33℃","low":"17℃","detail":"晴"}}
    public static WeatherEntity parserWeather(String desc) {
        WeatherEntity entity = null;
        try {
            JSONObject jesonWeather = new JSONObject(desc);
            int code = jesonWeather.getInt("code");
            String msg = jesonWeather.getString("msg");
            if (code != 0) {
                return entity;
            }
            String data = jesonWeather.getString("data");
            JSONObject jsonData = new JSONObject(data);
            String city = jsonData.getString("city");
            String high = jsonData.getString("high");
            String low = jsonData.getString("low");
            String type = jsonData.getString("type");
            entity = new WeatherEntity(city, type, low, high);

        } catch (Exception e) {
            return null;
        }
        return entity;
    }


    public static void setWeatherImage(ImageView iv_weather, String weatherInfo) {
        MyLog.cdl("==========更新天气控件===" + weatherInfo);
        if (weatherInfo.contains("晴")) {
            iv_weather.setBackgroundResource(R.drawable.weather_sun);
        } else if (weatherInfo.contains("云")) {
            iv_weather.setBackgroundResource(R.drawable.weather_cloud_sun);
        } else if (weatherInfo.contains("阴")) {
            iv_weather.setBackgroundResource(R.drawable.weather_yintian);
        } else if (weatherInfo.contains("小雨")) {
            iv_weather.setBackgroundResource(R.drawable.weather_rain_little);
        } else if (weatherInfo.contains("中雨")) {
            iv_weather.setBackgroundResource(R.drawable.weather_rain_middle);
        } else if (weatherInfo.contains("大雨") || weatherInfo.contains("暴雨")) {
            iv_weather.setBackgroundResource(R.drawable.weather_rain_big);
        } else if (weatherInfo.contains("阵雨")) {
            iv_weather.setBackgroundResource(R.drawable.weather_rain_middle);
        } else if (weatherInfo.contains("零星")) {
            iv_weather.setBackgroundResource(R.drawable.weather_rain_little);
        } else if (weatherInfo.contains("雷雨")) {
            iv_weather.setBackgroundResource(R.drawable.weather_leizhenyu);
        } else if (weatherInfo.contains("小雪")) {
            iv_weather.setBackgroundResource(R.drawable.weather_snow_little);
        } else if (weatherInfo.contains("中雪")) {
            iv_weather.setBackgroundResource(R.drawable.weather_snow_middle);
        } else if (weatherInfo.contains("大雪")) {
            iv_weather.setBackgroundResource(R.drawable.weather_snow_big);
        } else if (weatherInfo.contains("阵雪")) {
            iv_weather.setBackgroundResource(R.drawable.weather_zhenyu);
        } else if (weatherInfo.contains("雨夹雪")) {
            iv_weather.setBackgroundResource(R.drawable.weather_zhenyu);
        } else if (weatherInfo.contains("雾")) {
            iv_weather.setBackgroundResource(R.drawable.weather_fog);
        } else {
            iv_weather.setBackgroundResource(R.drawable.weather_cloud_sun);
        }
    }


}
