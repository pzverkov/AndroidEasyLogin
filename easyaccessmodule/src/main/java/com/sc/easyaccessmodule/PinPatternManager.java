package com.sc.easyaccessmodule;

import android.os.Build;

/**
 * Created by Peter on 25.04.2017.
 */

public class PinPatternManager {

    public static boolean getPatternSupport() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

}
