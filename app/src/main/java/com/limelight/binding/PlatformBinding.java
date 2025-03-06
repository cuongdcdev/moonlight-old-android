package com.limelight.binding;

import android.content.Context;
import android.os.Build;

import com.limelight.LimeLog;
import com.limelight.binding.audio.AndroidAudioRenderer;
import com.limelight.binding.crypto.AndroidCryptoProvider;
import com.limelight.nvstream.av.audio.AudioRenderer;
import com.limelight.nvstream.http.LimelightCryptoProvider;

import java.security.Security;

import javax.net.ssl.SSLContext;

public class PlatformBinding {
    private static AudioRenderer audioRenderer;
    private static boolean enabledTls12 = false;

    public static AudioRenderer getAudioRenderer() {
        if (audioRenderer == null) {
            audioRenderer = new AndroidAudioRenderer();
        }

        return audioRenderer;
    }

    public static LimelightCryptoProvider getCryptoProvider(Context c) {
        return new AndroidCryptoProvider(c);
    }

    public static void enableTls12ForOldAndroid() {
        // First check if we've already tried this
        if (enabledTls12) {
            return;
        }
        
        // Mark as attempted
        enabledTls12 = true;
        
        // Only needed for Android 4.4 and below
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return;
        }
        
        try {
            // Basic TLS 1.2 enablement through system properties
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
            
            // Initialize SSLContext
            try {
                SSLContext.getInstance("TLS");
            } catch (Exception e) {
                // Just log this
            }
        } catch (Exception e) {
            // Ignore any errors
        }
    }
}