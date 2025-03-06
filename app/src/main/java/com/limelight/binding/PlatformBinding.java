package com.limelight.binding;

import android.content.Context;
import android.os.Build;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.security.Security;

import com.limelight.binding.audio.AndroidAudioRenderer;
import com.limelight.binding.crypto.AndroidCryptoProvider;
import com.limelight.nvstream.av.audio.AudioRenderer;
import com.limelight.nvstream.http.LimelightCryptoProvider;

public class PlatformBinding {
    public static String getDeviceName() {
        String deviceName = android.os.Build.MODEL;
        deviceName = deviceName.replace(" ", "");
        return deviceName;
    }

    public static AudioRenderer getAudioRenderer() {
        return new AndroidAudioRenderer();
    }

    public static LimelightCryptoProvider getCryptoProvider(Context c) {
        return new AndroidCryptoProvider(c);
    }

    public static void enableTls12ForOldAndroid() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                // For Android 4.2 and below, enable TLS 1.2 globally
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, null, null);
                
                // Set as default SSL socket factory
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                
                // Also set TLS 1.2 as default for the process
                Security.setProperty("crypto.policy", "unlimited");
                System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
            } catch (Exception e) {
                LimeLog.warning("Failed to enable TLS 1.2 on old Android: " + e.getMessage());
            }
        }
    }
}
