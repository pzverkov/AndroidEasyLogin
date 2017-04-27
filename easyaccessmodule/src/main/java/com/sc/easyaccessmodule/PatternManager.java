package com.sc.easyaccessmodule;

import static com.sc.easyaccessmodule.EasyConstants.AUTHENTICATION_DURATION_SECONDS;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.greenrobot.eventbus.EventBus;

import com.sc.easyaccessmodule.event.PatternConfirmedEvent;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Peter on 28.02.2017.
 */

// SUPPORTS SINCE VERSION_CODES.M
public class PatternManager {

    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    public static final String DIVIDER = "/";
    private AppCompatActivity activity;

    private static final String KEY_NAME = "pattern_key_auth";
    private static final byte[] SECRET_BYTE_ARRAY = new byte[] { 8, 3, 5, 5, 5,
            7 };

    private KeyguardManager mKeyguardManager;

    public PatternManager(AppCompatActivity activity) {
        this.activity = activity;
        mKeyguardManager = (KeyguardManager) activity
                .getSystemService(Context.KEYGUARD_SERVICE);

        if (!mKeyguardManager.isKeyguardSecure()) {
            // Show a message that the user hasn't set up a lock screen.

            NotificationManager.showUserNotification(activity,
                    activity.getString(R.string.lock_not_set)
                            + activity.getString(R.string.lock_not_set_text));

            // OPEN SETTINGS
            openScreenLockScreenSettings(activity);

            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            createKey();
        }
    }

    /**
     * Tries to encrypt some data with the generated key in {@link #createKey}
     * which is only works if the user has just authenticated via device
     * credentials.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean tryEncrypt(final int requestCode) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_NAME, null);
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES
                    + DIVIDER + KeyProperties.BLOCK_MODE_CBC + DIVIDER
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // Try encrypting something, it will only work if the user
            // authenticated within
            // the last AUTHENTICATION_DURATION_SECONDS seconds.
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            cipher.doFinal(SECRET_BYTE_ARRAY);

            // If the user has recently authenticated, you will reach here.
            EventBus.getDefault().post(new PatternConfirmedEvent());
            return true;
        } catch (UserNotAuthenticatedException e) {
            // User is not authenticated, let's authenticate with device
            // credentials.
            showAuthenticationScreen(requestCode);
            return false;
        } catch (KeyPermanentlyInvalidatedException e) {
            // This happens if the lock screen has been disabled or reset after
            // the key was
            // generated after the key was generated.

            NotificationManager.showUserNotification(activity,
                    "Keys are invalidated after created. Retry the action\n"
                            + e.getMessage());
            return false;
        } catch (BadPaddingException | IllegalBlockSizeException
                | KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used
     * after the user has authenticated with device credentials within the last
     * X seconds.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createKey() {
        // Generate a key to decrypt payment credentials, tokens, etc.
        // This will most likely be a registration step for the user when they
        // are setting up your app.
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);

            // Set the alias of the entry in Android KeyStore where the key will
            // appear
            // and the constrains (purposes) in the constructor of the Builder
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT
                            | KeyProperties.PURPOSE_DECRYPT)
                                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                    .setUserAuthenticationRequired(true)
                                    // Require that the user has unlocked in the
                                    // last AUTHENTICATION_DURATION_SECONDS
                                    // seconds
                                    .setUserAuthenticationValidityDurationSeconds(
                                            AUTHENTICATION_DURATION_SECONDS)
                                    .setEncryptionPaddings(
                                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | InvalidAlgorithmParameterException | KeyStoreException
                | CertificateException | IOException e) {
            throw new RuntimeException("Failed to create a symmetric key", e);
        }
    }

    private void showAuthenticationScreen(int requestCode) {
        // Create the Confirm Credentials screen. You can customize the title
        // and description. Or
        // it will provide a generic one for you if there null
        Intent intent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null,
                    null);
        }
        if (intent != null) {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private static void openScreenLockScreenSettings(Context context) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
        context.startActivity(intent);
    }

}
