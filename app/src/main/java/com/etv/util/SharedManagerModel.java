package com.etv.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.etv.util.system.CpuModel;
import com.tencent.mmkv.MMKV;

/***
 * 用来保存Share基类
 */
public class SharedManagerModel {

    Context context;
    private SharedPreferences mSharedPreferences;

    public SharedManagerModel(Context context, String sheredName) {
        this.context = context;
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            return;
        }
        mSharedPreferences = context.getSharedPreferences(sheredName, 0);
    }

    public static void saveDataMMKV(String key, Object data) {
        MyLog.shared("===MMKVUtil===saveDataMMKV==key=" + key + " / " + data);
        MMKV mmkv = MMKV.defaultMMKV();
        try {
            if (data instanceof Integer) {
                mmkv.encode(key, (Integer) data);
            } else if (data instanceof Boolean) {
                mmkv.encode(key, (Boolean) data);
            } else if (data instanceof String) {
                mmkv.encode(key, (String) data);
            } else if (data instanceof Float) {
                mmkv.encode(key, (Float) data);
            } else if (data instanceof Long) {
                mmkv.encode(key, (Long) data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getDataMMKV(String key, Object defaultObject) {
        MyLog.shared("===MMKVUtil===getDataMMKV==key=" + key + " / " + defaultObject);
        MMKV mmkv = MMKV.defaultMMKV();
        try {
            if (defaultObject instanceof String) {
                return mmkv.decodeString(key, (String) defaultObject);
            } else if (defaultObject instanceof Integer) {
                return mmkv.decodeInt(key, (Integer) defaultObject);
            } else if (defaultObject instanceof Boolean) {
                return mmkv.decodeBool(key, (Boolean) defaultObject);
            } else if (defaultObject instanceof Float) {
                return mmkv.decodeFloat(key, (Float) defaultObject);
            } else if (defaultObject instanceof Long) {
                return mmkv.decodeLong(key, (Long) defaultObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveData(String key, Object data) {
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            saveDataMMKV(key, data);
            return;
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        try {
            if (data instanceof Integer) {
                editor.putInt(key, (Integer) data);
            } else if (data instanceof Boolean) {
                editor.putBoolean(key, (Boolean) data);
            } else if (data instanceof String) {
                editor.putString(key, (String) data);
            } else if (data instanceof Float) {
                editor.putFloat(key, (Float) data);
            } else if (data instanceof Long) {
                editor.putLong(key, (Long) data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    public Object getData(String key, Object defaultObject) {
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            return getDataMMKV(key, defaultObject);
        }
        try {
            if (defaultObject instanceof String) {
                return mSharedPreferences.getString(key, (String) defaultObject);
            } else if (defaultObject instanceof Integer) {
                return mSharedPreferences.getInt(key, (Integer) defaultObject);
            } else if (defaultObject instanceof Boolean) {
                return mSharedPreferences.getBoolean(key, (Boolean) defaultObject);
            } else if (defaultObject instanceof Float) {
                return mSharedPreferences.getFloat(key, (Float) defaultObject);
            } else if (defaultObject instanceof Long) {
                return mSharedPreferences.getLong(key, (Long) defaultObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
