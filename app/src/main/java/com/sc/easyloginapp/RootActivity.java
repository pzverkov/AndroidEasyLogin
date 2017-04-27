package com.sc.easyloginapp;

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
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RootActivity extends AppCompatActivity {

    private static final int REQUEST_AUTH_PIN = 5009;

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
        NotificationManager.showUserNotification(RootActivity.this, "Pattern Confirmed");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleUseCase(FingerPrintConfirmedEvent event) {
        NotificationManager.showUserNotification(RootActivity.this, "FingerPrint Confirmed");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);
    }

    public void onFingerPrintOptionSelected(View v) {
        FingerPrintManager.auth(App.getInstance());
    }

    public void onPinOptionSelected(View v) {
        PatternManager patternManager = new PatternManager(RootActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (patternManager.tryEncrypt(REQUEST_AUTH_PIN)) {
                NotificationManager.showUserNotification(RootActivity.this, "Auth with PIN/Pattern successful");
            } else {
                //Processing with dialog
            }

        } else {
            NotificationManager.showUserNotification(RootActivity.this, "Too old Android version to support our security features");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_AUTH_PIN) {

            // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {

                PatternManager patternManager = new PatternManager(RootActivity.this);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (patternManager.tryEncrypt(REQUEST_AUTH_PIN)) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().post(new PatternConfirmedEvent());
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
}
