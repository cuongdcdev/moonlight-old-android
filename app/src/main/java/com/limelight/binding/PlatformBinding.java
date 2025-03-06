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

    public static void enableTls12ForOldAndroid() {
        if (enabledTls12) {
            // Don't apply the fix more than once
            return;
        }

        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                LimeLog.info("Enabling TLS 1.2 for Android " + Build.VERSION.SDK_INT);

                try {
                    // Create a TLS 1.2 compatible SSLContext
                    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init((KeyStore) null);
                    sslContext.init(null, tmf.getTrustManagers(), null);

                    // Create a wrapper TLS 1.2-enabling SSLSocketFactory
                    final SSLSocketFactory delegate = sslContext.getSocketFactory();
                    SSLSocketFactory tls12Factory = new SSLSocketFactory() {
                        @Override
                        public String[] getDefaultCipherSuites() {
                            return delegate.getDefaultCipherSuites();
                        }

                        @Override
                        public String[] getSupportedCipherSuites() {
                            return delegate.getSupportedCipherSuites();
                        }

                        @Override
                        public java.net.Socket createSocket(java.net.Socket socket, String host, int port, boolean autoClose) throws java.io.IOException {
                            SSLSocket s = (SSLSocket) delegate.createSocket(socket, host, port, autoClose);
                            enableTls12OnSocket(s);
                            return s;
                        }

                        @Override
                        public java.net.Socket createSocket(String host, int port) throws java.io.IOException {
                            SSLSocket s = (SSLSocket) delegate.createSocket(host, port);
                            enableTls12OnSocket(s);
                            return s;
                        }

                        @Override
                        public java.net.Socket createSocket(String host, int port, java.net.InetAddress localHost, int localPort) throws java.io.IOException {
                            SSLSocket s = (SSLSocket) delegate.createSocket(host, port, localHost, localPort);
                            enableTls12OnSocket(s);
                            return s;
                        }

                        @Override
                        public java.net.Socket createSocket(java.net.InetAddress host, int port) throws java.io.IOException {
                            SSLSocket s = (SSLSocket) delegate.createSocket(host, port);
                            enableTls12OnSocket(s);
                            return s;
                        }

                        @Override
                        public java.net.Socket createSocket(java.net.InetAddress address, int port, java.net.InetAddress localAddress, int localPort) throws java.io.IOException {
                            SSLSocket s = (SSLSocket) delegate.createSocket(address, port, localAddress, localPort);
                            enableTls12OnSocket(s);
                            return s;
                        }
                        
                        private void enableTls12OnSocket(SSLSocket socket) {
                            // Enable TLSv1.2 on the socket
                            String[] protocols = socket.getEnabledProtocols();
                            String[] updatedProtocols;
                            
                            // Check if TLSv1.2 is already in the list
                            if (!Arrays.asList(protocols).contains("TLSv1.2")) {
                                updatedProtocols = new String[protocols.length + 1];
                                System.arraycopy(protocols, 0, updatedProtocols, 0, protocols.length);
                                updatedProtocols[protocols.length] = "TLSv1.2";
                                socket.setEnabledProtocols(updatedProtocols);
                            }
                        }
                    };

                    // Set as the default SSL socket factory for all connections
                    HttpsURLConnection.setDefaultSSLSocketFactory(tls12Factory);
                    
                    // Also set system properties
                    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
                    
                    // Set security provider properties if possible
                    try {
                        Security.setProperty("crypto.policy", "unlimited");
                    } catch (Exception e) {
                        // Ignore if we can't set this
                        LimeLog.warning("Could not set crypto policy: " + e.getMessage());
                    }
                    
                    enabledTls12 = true;
                    LimeLog.info("TLS 1.2 enabled successfully for Android " + Build.VERSION.SDK_INT);
                } catch (Exception e) {
                    LimeLog.severe("Failed to enable TLS 1.2 on old Android: " + e);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // Catch any other exceptions that might occur
            LimeLog.severe("Unexpected error in TLS 1.2 configuration: " + e);
            e.printStackTrace();
        }
    }
}
