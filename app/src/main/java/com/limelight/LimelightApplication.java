package com.limelight;

import android.app.Application;
import com.limelight.binding.PlatformBinding;
import com.limelight.LimeLog;

public class LimelightApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Enable TLS 1.2 for older Android versions
        LimeLog.info("Initializing TLS 1.2 support for older Android versions");
        PlatformBinding.enableTls12ForOldAndroid();
    }
}