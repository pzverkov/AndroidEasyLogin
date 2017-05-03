package com.sc.easyaccessmodule;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

import android.content.Context;
import android.util.Log;

/**
 * Created by Peter on 24.04.2017.
 */

public class FingerPrintManager {

    private static String SUPPORT_FINGERPRINT_TAG = "FPT";
    private static AuthenticationListener authenticationListener;

    private static AuthenticationListener getAuthenticationListener(final Context context) {
        return new AuthenticationListener() {
            @Override
            public void onSuccess(int moduleTag) {
                setFingerSupport(context, true);
                Log.e("FingerPrintManager", "onSuccess. Tag = " + moduleTag);
            }

            @Override
            public void onFailure(AuthenticationFailureReason failureReason,
                                  boolean fatal, CharSequence errorMessage, int moduleTag,
                                  int errorCode) {
                setFingerSupport(context, false);
                Log.e("FingerPrintManager", "Failure " + errorMessage + " code = " + errorCode);
            }
        };
    }

    public static void init(Context context) {
        Reprint.initialize(context, new Reprint.Logger() {
            @Override
            public void log(String message) {
                Log.d("Reprint", message);
            }

            @Override
            public void logException(Throwable throwable, String message) {
                Log.e("Reprint", message, throwable);
            }
        });
        Reprint.authenticate(getAuthenticationListener(context));
    }

    public static void auth(Context context) {
        Reprint.authenticate(getAuthenticationListener(context));
    }

    private static void setFingerSupport(Context context, boolean support) {
        DataManager.saveBoolean(context, support, SUPPORT_FINGERPRINT_TAG);
    }

    public static boolean getFingerSupport(Context context) {
        return true;
        //return DataManager.loadBoolean(context, SUPPORT_FINGERPRINT_TAG, false);
    }

}
