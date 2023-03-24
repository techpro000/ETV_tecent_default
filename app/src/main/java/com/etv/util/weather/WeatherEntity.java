package com.etv.util.weather;

import java.io.Serializable;

/***
 * 封装天气的实体类
 *
 * @author Administrator
 *
 */
public class WeatherEntity implements Serializable {
    String city; // 城市
    String weatherInfo;// 天气状态
    String lowTem; // 最低温度
    String heightTem;// 最高温度
    String taColor;
    String bggColor;
//    String pm25;

    //    {"code":0,"msg":"查询成功！","data":{"high":"22℃","pm25":"32.0","low":"8℃","city":"保山市","type":"晴"}}
    public WeatherEntity(String city, String weatherInfo, String lowTem, String heightTem) {
        super();
        this.city = city;
        this.weatherInfo = weatherInfo;
        this.lowTem = lowTem;
        this.heightTem = heightTem;
    }

    public WeatherEntity(String city, String weatherInfo, String lowTem, String heightTem, String taColor, String bggColor) {
        super();
        this.city = city;
        this.weatherInfo = weatherInfo;
        this.lowTem = lowTem;
        this.heightTem = heightTem;
        this.taColor = taColor;
        this.bggColor = bggColor;
    }

    public String getBggColor() {
        return bggColor;
    }

    public void setBggColor(String bggColor) {
        this.bggColor = bggColor;
    }

    public WeatherEntity() {
        super();
    }

    public String getTaColor() {
        return taColor;
    }

    public void setTaColor(String taColor) {
        this.taColor = taColor;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getWeatherInfo() {
        return weatherInfo;
    }

    public void setWeatherInfo(String weatherInfo) {
        this.weatherInfo = weatherInfo;
    }

    public String getLowTem() {
        return lowTem;
    }

    public void setLowTem(String lowTem) {
        this.lowTem = lowTem;
    }

    public String getHeightTem() {
        return heightTem;
    }

    public void setHeightTem(String heightTem) {
        this.heightTem = heightTem;
    }

    @Override
    public String toString() {
        return "WeatherEntity{" +
                "city='" + city + '\'' +
                ", weatherInfo='" + weatherInfo + '\'' +
                ", lowTem='" + lowTem + '\'' +
                ", heightTem='" + heightTem + '\'' +
                ", taColor='" + taColor + '\'' +
                '}';
    }
}
