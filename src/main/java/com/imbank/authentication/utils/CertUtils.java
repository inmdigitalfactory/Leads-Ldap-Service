package com.imbank.authentication.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.x509.X509V1CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertUtils {


    public static X509Certificate selfSign(KeyPair keyPair, String subjectDN) throws OperatorCreationException, CertificateException, IOException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
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


}
