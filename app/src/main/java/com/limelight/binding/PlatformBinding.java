package com.limelight.binding;

import android.content.Context;
import android.os.Build;

import com.limelight.LimeLog;
import com.limelight.binding.audio.AndroidAudioRenderer;
import com.limelight.binding.crypto.AndroidCryptoProvider;
import com.limelight.nvstream.av.audio.AudioRenderer;
import com.limelight.nvstream.http.LimelightCryptoProvider;

import java.security.KeyStore;
import java.security.Security;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class PlatformBinding {
    private static AudioRenderer audioRenderer;
    private static boolean enabledTls12 = false;

    public static String getDeviceName() {
        String deviceName = android.os.Build.MODEL;
        deviceName = deviceName.replace(" ", "");
        return deviceName;
    }

    public static AudioRenderer getAudioRenderer() {
        if (audioRenderer == null) {
            audioRenderer = new AndroidAudioRenderer();
        }

        return audioRenderer;
    }

    public static LimelightCryptoProvider getCryptoProvider(Context c) {
        return new AndroidCryptoProvider(c);
    }

    // This static initializer ensures that the class can be loaded without errors
    static {
        try {
            // Pre-load any classes that might cause initialization errors
            Class.forName("javax.net.ssl.SSLContext");
            Class.forName("javax.net.ssl.SSLSocketFactory");
        } catch (Exception e) {
            // Ignore exceptions here - we just want to make sure the classes are loaded
            System.err.println("Warning: Error pre-loading SSL classes: " + e.getMessage());
        }
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
            System.out.println("Enabling TLS 1.2 for Android " + Build.VERSION.RELEASE);
            
            try {
                // Enable TLS 1.2 by setting system properties first
                System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
            } catch (Exception e) {
                System.err.println("Error setting https.protocols: " + e.getMessage());
            }
            
            try {
                // Setup TLS context for TLS 1.2 in a simple way that won't cause
                // initialization errors during build
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                
                // Set as default, if possible
                try {
                    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                } catch (Exception e) {
                    System.err.println("Error setting default SSLSocketFactory: " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("Error initializing SSLContext: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Unexpected error in TLS configuration: " + e.getMessage());
        }
    }
}
