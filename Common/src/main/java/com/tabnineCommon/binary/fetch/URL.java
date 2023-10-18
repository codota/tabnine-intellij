package com.tabnineCommon.binary.fetch;

import com.tabnineCommon.general.StaticConfig;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class URL {

    private java.net.URL url;
    public URL(String spec) throws MalformedURLException {
        this.url = new java.net.URL(spec);
    }

    public URLConnection openConnection() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        if(this.url.getProtocol().equals("https")) {
            HttpsURLConnection connection = (HttpsURLConnection) this.url.openConnection();
            if (StaticConfig.getIgnoreCertificateErrors()) {
                SSLContext context = SSLContexts.custom().setProtocol("TLS").build();
                context.init(null, new TrustManager[]{new InsecureTrustManager()}, new SecureRandom());
                connection.setSSLSocketFactory(context.getSocketFactory());
            }

            return connection;
        }
        else {
            return this.url.openConnection();
        }
    }
}
