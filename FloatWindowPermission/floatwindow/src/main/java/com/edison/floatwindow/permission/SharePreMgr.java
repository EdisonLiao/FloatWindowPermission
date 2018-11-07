package com.example.floatwindow.permission;

/**
 * created by edison 2018/11/3
 */
public class SharePreMgr {

    private static String KEY_ADDED_QUICK_WORD = "quick_add_word";

    public static void addQuickWork(String work){
        PreferenceMgr.getIns().setString(KEY_ADDED_QUICK_WORD,work);
    }

    public static String getAddedQuickWork(){
        return PreferenceMgr.getIns().getString(KEY_ADDED_QUICK_WORD,"");
    }

}
