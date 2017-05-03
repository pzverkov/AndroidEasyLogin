package com.sc.easyloginapp;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;
import com.sc.easyaccessmodule.EasyConstants;
import com.sc.easyaccessmodule.FingerPrintManager;
import com.sc.easyaccessmodule.NotificationManager;
import com.sc.easyaccessmodule.PatternManager;
import com.sc.easyaccessmodule.event.FingerPrintConfirmedEvent;
import com.sc.easyaccessmodule.event.PatternConfirmedEvent;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class RootActivity extends AppCompatActivity {

    private static final int REQUEST_AUTH_PIN = 5009;
    private MaterialDialog authFingerDialog;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleUseCase(PatternConfirmedEvent event) {
        NotificationManager.showUserNotification(RootActivity.this,
                "Pattern Confirmed");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleUseCase(FingerPrintConfirmedEvent event) {
        NotificationManager.showUserNotification(RootActivity.this,
                "FingerPrint Confirmed");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);
    }

    public void onFingerPrintOptionSelected(View v) {
        if (FingerPrintManager.getFingerSupport(RootActivity.this)) {
            Log.e("FingerPrint", "Trying to auth with finger");
            FingerPrintManager.auth(App.getInstance());

            hideAuthFingerDialog();

            authFingerDialog = showDialogAuthFinger(RootActivity.this, "Login");

            Reprint.authenticate(new AuthenticationListener() {
                @Override
                public void onSuccess(int moduleTag) {
                    hideAuthFingerDialog();

                    goIn();
                }

                @Override
                public void onFailure(AuthenticationFailureReason failureReason,
                        boolean fatal, CharSequence errorMessage, int moduleTag,
                        int errorCode) {
                    hideAuthFingerDialog();
                    NotificationManager.showUserNotification(RootActivity.this,
                            "" + errorMessage);
                }
            });

        } else {
            NotificationManager.showUserNotification(RootActivity.this,
                    "FingerPrint not supporting on this device");
        }
    }

    public void onPinOptionSelected(View v) {
        PatternManager patternManager = new PatternManager(RootActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (patternManager.tryEncrypt(REQUEST_AUTH_PIN)) {
                NotificationManager.showUserNotification(RootActivity.this,
                        "Auth with PIN/Pattern successful");
            } else {
                // Processing with dialog
            }

        } else {
            NotificationManager.showUserNotification(RootActivity.this,
                    "Too old Android version to support our security features");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == REQUEST_AUTH_PIN) {

            // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {

                PatternManager patternManager = new PatternManager(
                        RootActivity.this);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (patternManager.tryEncrypt(REQUEST_AUTH_PIN)) {
                        new Handler(Looper.getMainLooper())
                                .postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        EventBus.getDefault().post(
                                                new PatternConfirmedEvent());
                                    }
                                }, EasyConstants.AUTHENTICATION_DURATION_SECONDS);

                        Log.w("Pattern", "Pattern encrypted");

                    } else {

                        Log.w("Pattern", "Pattern Not encrypted");

                    }
                }
            } else {
                // The user canceled or didn’t complete the lock screen
                // operation. Go to error/cancellation flow.
            }
        }
    }

    private void hideAuthFingerDialog() {
        if (authFingerDialog != null && authFingerDialog.isShowing()) {
            authFingerDialog.dismiss();
        }
    }

    public static MaterialDialog showDialogAuthFinger(
            AppCompatActivity activity, String serviceName) {

        LayoutInflater li = LayoutInflater.from(activity);
        View view = li.inflate(R.layout.dialog_finger_auth, null);
        TextView textView = (TextView) view.findViewById(R.id.text);
        textView.setText(App.getInstance().getString(R.string.title_owner));

        return new MaterialDialog.Builder(activity).cancelable(true)
                .canceledOnTouchOutside(true).
                title(String.format(App.getInstance()
                        .getString(R.string.title_enter_finger), serviceName))
                .customView(view, true)
                .negativeText(
                        App.getInstance().getString(R.string.action_cancel))
                .show();
    }

    private void goIn() {
        NotificationManager.showUserNotification(RootActivity.this,
                "FingerPrint got successfully");
    }
}
