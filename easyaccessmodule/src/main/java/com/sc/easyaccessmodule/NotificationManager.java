package com.sc.easyaccessmodule;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by Peter on 27.04.2017.
 */

public class NotificationManager {

    public static void showUserNotification(AppCompatActivity activity, String s) {
        if (EasyConstants.useToasts) {
            Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
        } else {
            //SnackBar
        }
    }
}
