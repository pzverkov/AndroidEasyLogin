package com.sc.easyaccessmodule;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

import android.content.Context;

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
            }

            @Override
            public void onFailure(AuthenticationFailureReason failureReason,
                                  boolean fatal, CharSequence errorMessage, int moduleTag,
                                  int errorCode) {
                setFingerSupport(context, false);
            }
        };
    }

    public static void init(Context context) {
        Reprint.initialize(context);
        Reprint.authenticate(getAuthenticationListener(context));
    }

    public static void auth(Context context) {
        Reprint.authenticate(getAuthenticationListener(context));
    }

    private static void setFingerSupport(Context context, boolean support) {
        DataManager.saveBoolean(context, support, SUPPORT_FINGERPRINT_TAG);
    }

    public static boolean getFingerSupport(Context context) {
        return DataManager.loadBoolean(context, SUPPORT_FINGERPRINT_TAG, false);
    }

}
