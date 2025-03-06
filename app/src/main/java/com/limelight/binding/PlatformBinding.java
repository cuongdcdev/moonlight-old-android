package com.limelight.binding;

import android.content.Context;
import android.os.Build;

import com.limelight.LimeLog;
import com.limelight.binding.audio.AndroidAudioRenderer;
import com.limelight.binding.crypto.AndroidCryptoProvider;
import com.limelight.nvstream.av.audio.AudioRenderer;
import com.limelight.nvstream.http.LimelightCryptoProvider;

import java.security.Security;

import javax.net.ssl.HttpsURLConnection;
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
        
        // Mark as attempted so we don't try multiple times
        enabledTls12 = true;
        
        // Only needed for Android 4.4 and below
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return;
        }
        
        try {
            LimeLog.info("Enabling TLS 1.2 for Android " + Build.VERSION.RELEASE);
            
            try {
                // Enable TLS 1.2 by setting system properties
                System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
            } catch (Exception e) {
                LimeLog.warning("Error setting https.protocols: " + e.getMessage());
            }
            
            try {
                // Set security provider properties if possible
                Security.setProperty("crypto.policy", "unlimited");
            } catch (Exception e) {
                LimeLog.warning("Error setting crypto policy: " + e.getMessage());
            }
            
            try {
                // Initialize SSLContext for TLS 1.2 in a way that won't cause
                // initialization errors
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            } catch (Exception e) {
                LimeLog.warning("Error initializing SSLContext: " + e.getMessage());
            }
            
            LimeLog.info("TLS 1.2 initialization completed");
        } catch (Exception e) {
            LimeLog.warning("Unexpected error enabling TLS 1.2: " + e.getMessage());
        }
    }
}
