package com.ericsson.cifwk.tdm.client;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.net.CookieManager;
import java.net.CookiePolicy;

import okhttp3.JavaNetCookieJar;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 * Date: 22/02/2016
 */
public final class RestClientProvider {

    private static RestClientProvider instance = null;
    private Retrofit retrofit;

    private RestClientProvider(String host) {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        TrustManager[] trustedCerts = getTrustedCerts();
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustedCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustedCerts[0])
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS).cookieJar(new JavaNetCookieJar(cookieManager))
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }).build();

            retrofit = new Retrofit.Builder().client(client).baseUrl(host)
                    .addConverterFactory(JacksonConverterFactory.create()).build();

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized RestClientProvider getInstance(String host) {
        if (instance == null) {
            instance = new RestClientProvider(host);
        }
        return instance;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    private TrustManager[] getTrustedCerts() {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        // not using trust store throw exception
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        // not using trust store throw exception
                        throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
        };
        return trustAllCerts;
    }
}
