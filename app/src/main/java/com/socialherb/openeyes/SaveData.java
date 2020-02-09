package com.socialherb.openeyes;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Written by J.S. Oh on 2016-03-30.
 */
public class SaveData {

    private static String __SaveDta_Key = "pref";
    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;

    private static SaveData savedata;

    public static SaveData getInstance(Context context) {
        if (pref == null) {
            pref = context.getSharedPreferences(__SaveDta_Key, context.MODE_PRIVATE);
            editor = pref.edit();
            savedata = new SaveData();
        }
        return savedata;
    }

    static void setAlarmPoint(int val) {

        editor.putInt("alarmpoint", val);
        editor.commit();
    }

    static int getAlarmPoint() {

        int result = pref.getInt("alarmpoint", 20);
        return result;
    }

}
