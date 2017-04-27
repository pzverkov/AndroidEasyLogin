package com.sc.easyaccessmodule;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Peter on 26.04.2017.
 */

class DataManager {

    private static final String SP_FILE = "dataStorage";

    static boolean saveBoolean(Context context, boolean value, String tag) {
        try {
            SharedPreferences.Editor spe = context.getSharedPreferences(SP_FILE, Context.MODE_PRIVATE).edit();
            spe.putBoolean(tag, value);
            spe.apply();
        } catch (Exception e) {
            Log.w("DataManager", "error save" + e.toString());
            return false;
        }
        return true;
    }

    static boolean loadBoolean(Context context, String tag){
        return loadBoolean(context, tag, false);
    }

    static boolean loadBoolean(Context context, String tag, boolean defaultValue) {
        boolean value = false;
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
            value = sp.getBoolean(tag, defaultValue);
        } catch (Exception e) {
            Log.w("DataManager", "error load" + e.toString());
        }
        return value;
    }

}
