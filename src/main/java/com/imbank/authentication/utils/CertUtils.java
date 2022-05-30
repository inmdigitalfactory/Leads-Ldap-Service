package com.imbank.authentication.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.x509.X509V1CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertUtils {


    private static X509Certificate selfSign(KeyPair keyPair, String subjectDN) throws OperatorCreationException, CertificateException, IOException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        Provider bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);
        Date validityBeginDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date validityEndDate = new Date(System.currentTimeMillis() + 20L * 365 * 24 * 60 * 60 * 1000);

        X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
        X500Principal dnName = new X500Principal(subjectDN);

        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setSubjectDN(dnName);
        certGen.setIssuerDN(dnName); // use the same
        certGen.setNotBefore(validityBeginDate);
        certGen.setNotAfter(validityEndDate);
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

        return certGen.generate(keyPair.getPrivate(), "BC");
    }


    public static void generateAndSignCert() throws NoSuchAlgorithmException, CertificateException, IOException, SignatureException, OperatorCreationException, NoSuchProviderException, InvalidKeyException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair  = keyPairGenerator.generateKeyPair();

        saveFile("saml/saml.private.key", keyPair.getPrivate().getEncoded());
        saveFile("saml/saml.public.key", keyPair.getPublic().getEncoded());
        Certificate c = CertUtils.selfSign(keyPair, "dc=example.com");
        saveFile("saml/saml.crt", c.getEncoded());
    }

    private static void saveFile(String name, byte[] bytes) throws IOException {
        FileOutputStream fos = new FileOutputStream(name);
        fos.write(bytes);
        fos.flush();
        fos.close();
    }


}
