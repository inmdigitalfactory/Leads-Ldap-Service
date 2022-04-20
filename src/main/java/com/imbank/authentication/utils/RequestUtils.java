package com.imbank.authentication.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

@Slf4j
public class RequestUtils {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static String getClientIpAddress() {

        if (RequestContextHolder.getRequestAttributes() == null) {
            return "0.0.0.0";
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        for (String header: IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList)) {
                return ipList.split(",")[0];
            }
        }

        return request.getRemoteAddr();
    }

    public static KeyStore setupTrustStore(String certFileName) {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, Constants.KEYSTORE_PASSWORD.toCharArray());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            try (InputStream cert = new ClassPathResource(certFileName).getInputStream()) {
                Certificate certificate = cf.generateCertificate(cert);
                ks.setCertificateEntry("imbank", certificate);
            }
//            try (InputStream cert = new ClassPathResource("saml.crt").getInputStream()) {
//                File verificationKey = new ClassPathResource("saml.crt").getFile();
//                X509Certificate certificate = X509Support.decodeCertificate(verificationKey);
//                ks.setCertificateEntry(Constants.KEYSTORE_KEY, certificate);
//            }
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                KeyPair keyPair  = keyPairGenerator.generateKeyPair();

                Certificate c = CertUtils.selfSign(keyPair, "dc=example.com");
                ks.setCertificateEntry(Constants.KEYSTORE_KEY, c);
                ks.setKeyEntry(Constants.KEYSTORE_KEY, keyPair.getPrivate(), Constants.KEYSTORE_PASSWORD.toCharArray(), new Certificate[]{c});
            }
            catch (Exception e){
                e.printStackTrace();
            }

            File keystore = new File(Constants.KEYSTORE_FILE_NAME).getAbsoluteFile();
            try (FileOutputStream fos = new FileOutputStream(keystore)) {
                ks.store(fos, Constants.KEYSTORE_PASSWORD.toCharArray());
            }
            log.info("Setting truststore to {}", keystore.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStore", keystore.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStorePassword", Constants.KEYSTORE_PASSWORD);
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return ks;
    }
}
