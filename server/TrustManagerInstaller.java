/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;
import java.security.cert.X509Certificate;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;

import javax.ejb.Startup;
import javax.ejb.Singleton;
import javax.annotation.PostConstruct;

/**
 *
 * @author elz24996
 */

@Startup
@Singleton
public class TrustManagerInstaller {
    
    @PostConstruct
    public void install() {
        // Create a trust manager that does not validate certificate chains
        // Equivalent to --no-certificate-check in wget
        // Only needed if system does not have access to correct CA keys
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
 
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            System.err.println(e.getClass().getSimpleName() + " setting trust manager: " + e.getMessage());
        }
        // log message
        System.out.println("Trust manager set up successfully");
 
    }
    
}
