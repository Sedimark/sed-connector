/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *       Víctor González (UC)
 *       Pablo Sotres (UC)
 *
 */

package eu.sedimark.edc;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class SeedVaultExtension implements ServiceExtension {

    @Inject
    private Vault vault;

    @Override
    public void initialize(ServiceExtensionContext context) {
        context.getMonitor().withPrefix("SEDIMARK").warning("Seed-Vault extension is just used for DEV purposes, DON'T USE THIS IN PRODUCTION!!.");

        try {
            // Add BouncyCastle as security provider
            Security.addProvider(new BouncyCastleProvider());
            
            // Generate key pair
            KeyPair keyPair = generateKeyPair();
            
            // Generate self-signed certificate
            X509Certificate certificate = generateSelfSignedCertificate(keyPair);
            
            // Convert to PEM format
            String certificatePem = convertCertificateToPem(certificate);
            String privateKeyPem = convertPrivateKeyToPem(keyPair.getPrivate());
            
            // Store in vault
            vault.storeSecret("public-key", certificatePem);
            vault.storeSecret("private-key", privateKeyPem);
            
            context.getMonitor().withPrefix("SEDIMARK").info("Successfully generated and stored new key pair and certificate");
            
        } catch (Exception e) {
            context.getMonitor().withPrefix("SEDIMARK").severe("Failed to generate keys: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize SeedVaultExtension", e);
        }
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    private X509Certificate generateSelfSignedCertificate(KeyPair keyPair) 
            throws OperatorCreationException, CertificateException {
        
        X500Name subject = new X500Name("CN=SEDIMARK Project, O=SEDIMARK, C=EU");
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date();
        Date notAfter = new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)); // 1 year
        
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                subject,
                serialNumber,
                notBefore,
                notAfter,
                subject,
                keyPair.getPublic()
        );
        
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider("BC")
                .build(keyPair.getPrivate());
        
        X509CertificateHolder holder = builder.build(signer);
        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(holder);
    }

    private String convertCertificateToPem(X509Certificate certificate) throws Exception {
        StringWriter stringWriter = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(stringWriter)) {
            pemWriter.writeObject(new PemObject("CERTIFICATE", certificate.getEncoded()));
        }
        return stringWriter.toString();
    }

    private String convertPrivateKeyToPem(PrivateKey privateKey) throws Exception {
        StringWriter stringWriter = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(stringWriter)) {
            pemWriter.writeObject(new PemObject("PRIVATE KEY", privateKey.getEncoded()));
        }
        return stringWriter.toString();
    }
}
