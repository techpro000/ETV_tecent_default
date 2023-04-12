package com.etv.task.entity;

import com.etv.util.weather.WeatherEntity;
import com.etv.view.layout.Generator;

public class CacheMemory {

    Generator generator;  //播放类View
    String coType;  //控件类型
    boolean isRelation;  //是否是关联控件
    WeatherEntity weatherEntity;

    public CacheMemory(Generator generator, String coType, boolean isRelation) {
        this.generator = generator;
        this.coType = coType;
        this.isRelation = isRelation;
    }

    public CacheMemory(Generator generator, String coType, boolean isRelation, WeatherEntity weatherEntity) {
        this.generator = generator;
        this.coType = coType;
        this.isRelation = isRelation;
        this.weatherEntity = weatherEntity;
    }

    public WeatherEntity getWeatherEntity() {
        return weatherEntity;
    }

    public void setWeatherEntity(WeatherEntity weatherEntity) {
        this.weatherEntity = weatherEntity;
    }

    public boolean isRelation() {
        return isRelation;
    }

    public void setRelation(boolean relation) {
        isRelation = relation;
    }

    public Generator getGenerator() {
        return generator;
    }

    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    public String getCoType() {
        return coType;
    }

    public void setCoType(String coType) {
        this.coType = coType;
    }
}
