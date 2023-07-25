package com.varvet.barcodereadersample.Http;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class VolleySingleton {
    private static VolleySingleton volleySingleton;
    private RequestQueue requestQueue;
    private static Context mctx;


    private VolleySingleton(Context context) {
        this.mctx = context;
        this.requestQueue = getRequestQueue();

    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(mctx.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (volleySingleton == null) {
            volleySingleton = new VolleySingleton(context);
        }
        return volleySingleton;
    }

    public <T> void addToRequestQue(Request<T> request) {
        requestQueue.add(request);

    }


    public HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                //return true;
                // verify always returns true, which could cause  insecure network traffic due to     trusting TLS/SSL server certificates for wrong hostnames
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify("https://memories.casamedia.fi/interface/", session);
            }
        };

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public SSLSocketFactory getGlobalSSlFactory() {
        try {

//Use the certificate from raw folder...use below line
//            InputStream inputStream=mctx.getResources().openRawResource(R.raw.test);
//Use the certificate as a String.. I've done the conversion here for String 
            String certificate = "-----BEGIN CERTIFICATE-----\n" +
                    "MIIFMTCCBBmgAwIBAgISBNAGNcMORvei8PzzJA8aej7UMA0GCSqGSIb3DQEBCwUA\n" +
                    "MDIxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MQswCQYDVQQD\n" +
                    "EwJSMzAeFw0yMTExMTYxMzU3MzBaFw0yMjAyMTQxMzU3MjlaMCAxHjAcBgNVBAMT\n" +
                    "FW1lbW9yaWVzLmNhc2FtZWRpYS5maTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCC\n" +
                    "AQoCggEBANeMhDW7w6egdSdbjy6HrtAKhhT++SJmNSpjJEu/+j3X3750mD9cTwf4\n" +
                    "/WAu2jrEYmvL5nxV8sm3Zm3YxzG+lGYFcHuNrgCzDI7xgLGN3ErZaZcRIw6PtQ+V\n" +
                    "JjzZpJbPlNNfMXnhxvyzPMt2so0cbEaQvADPxVzMkWIrdfLHkfq4xyxUweDqaH7x\n" +
                    "9btENFj1nuuFqA68cIanzJuk6WwlgnTmU8RfMb1qUTxV7FdnHKuKzeYsoGCkiYDS\n" +
                    "unz7lXJtbW01vmwGT+5bNXqQtyE6rWys0+YJ/8jhoz/gXyMwzGPV6OpUS6TvEGDl\n" +
                    "ZqGKkuz1gGfr3ojoKG15tPeQPGXcS3cCAwEAAaOCAlEwggJNMA4GA1UdDwEB/wQE\n" +
                    "AwIFoDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwDAYDVR0TAQH/BAIw\n" +
                    "ADAdBgNVHQ4EFgQU1WpEgwKqhEsW6bPbyDeSA50kpSAwHwYDVR0jBBgwFoAUFC6z\n" +
                    "F7dYVsuuUAlA5h+vnYsUwsYwVQYIKwYBBQUHAQEESTBHMCEGCCsGAQUFBzABhhVo\n" +
                    "dHRwOi8vcjMuby5sZW5jci5vcmcwIgYIKwYBBQUHMAKGFmh0dHA6Ly9yMy5pLmxl\n" +
                    "bmNyLm9yZy8wIAYDVR0RBBkwF4IVbWVtb3JpZXMuY2FzYW1lZGlhLmZpMEwGA1Ud\n" +
                    "IARFMEMwCAYGZ4EMAQIBMDcGCysGAQQBgt8TAQEBMCgwJgYIKwYBBQUHAgEWGmh0\n" +
                    "dHA6Ly9jcHMubGV0c2VuY3J5cHQub3JnMIIBBQYKKwYBBAHWeQIEAgSB9gSB8wDx\n" +
                    "AHYARqVV63X6kSAwtaKJafTzfREsQXS+/Um4havy/HD+bUcAAAF9KUDFCgAABAMA\n" +
                    "RzBFAiEAn63ORZBbCgl98Tk1Or9sJN+e9S1oeXRH8T0PwKDuk8oCIHUurCwcGeIg\n" +
                    "yVDae7i+YH4ShPWVakZzjWtVdDvGSfL1AHcAb1N2rDHwMRnYmQCkURX/dxUcEdkC\n" +
                    "wQApBo2yCJo32RMAAAF9KUDGpQAABAMASDBGAiEApfLXqtOFMronrIj7SUCPAUiV\n" +
                    "U/Eh84OY+CqSJhQDMhoCIQDsux6OQ5KAMnc4is3afaE3VVaEFpFHYCeChMZduCpL\n" +
                    "mzANBgkqhkiG9w0BAQsFAAOCAQEAt3cTkR3x6/ZRlD8l72At9MOq9DXMFJA4qyFy\n" +
                    "xHBpL4yyRLSR+hCx0Pvdo+Z1L3rWqsvJCz+wVOvEnSi3cfwAjUA2Lm7cgqLCq8XG\n" +
                    "vzQn3clfCCMF3yqh73vR7T47kUHbpHHXpwADrL/B2zP4VpiGFi8deJ0mSWH1OAe9\n" +
                    "h8WoanmL+KNovW3ZAWbtelRL6ji2daNMmoxL4PXiRPm0sNewNpf9h+FWqzQRdUGt\n" +
                    "I1fPRn4i/lYgllzWo68GOnTXCBcJ34KG6MQE8hi/HnvTRhK0xKgtf33r4nB0+dwx\n" +
                    "ZO8qD3YWrMDUnPn0XLbyMlkEQ1T206UCeUkT4Yr40M2HG/GXUw==\n" +
                    "-----END CERTIFICATE-----";
            byte encodedCert[] = Base64.getDecoder().decode(certificate);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(encodedCert);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca = cf.generateCertificate(inputStream);
            inputStream.close();
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(null, null);

            keyStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "xxxxxxx".toCharArray());

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static X509Certificate convertToX509Cert(String certificateString) throws CertificateException {
        X509Certificate certificate = null;
        CertificateFactory cf = null;
        try {
            if (certificateString != null && !certificateString.trim().isEmpty()) {
                certificateString = certificateString.replace("-----BEGIN CERTIFICATE-----\n", "")
                        .replace("-----END CERTIFICATE-----", ""); // NEED FOR PEM FORMAT CERT STRING
                byte[] certificateData = Base64.getDecoder().decode(certificateString);
                cf = CertificateFactory.getInstance("X509");
                certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));
            }
        } catch (CertificateException e) {
            throw new CertificateException(e);
        }
        return certificate;
    }
}