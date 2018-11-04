package com.android.permission;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * created by edison 2018/11/3
 */
public class PreferenceMgr {

    private static PreferenceMgr sIns;
    private SharedPreferences mPrefs;

    public static PreferenceMgr getIns(){
        if (sIns == null){
            sIns = new PreferenceMgr();
        }
        return sIns;
    }

    public void init(Context context){
        mPrefs = context.getSharedPreferences("messenger-ball",Context.MODE_PRIVATE);
    }

    public void setString(String key,String value){
        if (mPrefs != null){
            mPrefs.edit().putString(key, value).apply();
        }
    }

    public String getString(String key, String defaultValue){
        return mPrefs == null ? defaultValue : mPrefs.getString(key, defaultValue);
    }



}
