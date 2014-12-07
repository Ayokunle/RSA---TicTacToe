package tic.tac.toe;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;

import sun.security.x509.*;
import java.security.cert.*;
import java.security.*;
import java.math.BigInteger;
import java.util.Date;
import java.io.IOException;

/**
 *
 * @author Ayokunle Adeosun
 */
public class System_CA {
    /*
    These variables are going to be accessible by both Player One 
    and Player Two classes. Except the KeyPairs, 
    each one is only seen by the class it's named after 
    and the public key of the other player.
    To get the KeyPair of the other class, the certain player 
    would have to look into a specific file and search for the other 
    player's certificate to get the private key, it'll need a password to do this.
    
    */
    static KeyPair Player_One_pair, Player_Two_pair;
    static int order = 0;
    
    static boolean Two_get_sign, One_get_signACK, Two_get_msg, 
                   One_get_sign, Two_get_signACK, One_get_msg = false;
    
    /*
    This methos create a X509 cetificate 
    */
    static X509Certificate generateCertificate(String CN, String OU, String O, String L, String S, String C, KeyPair pair, int days, String algorithm)throws GeneralSecurityException, IOException{
        
    PrivateKey privkey = pair.getPrivate();
    X509CertInfo info = new X509CertInfo();
    Date from = new Date();
    Date to = new Date(from.getTime() + days * 86400000l);
    CertificateValidity interval = new CertificateValidity(from, to);
    BigInteger sn = new BigInteger(64, new SecureRandom());
    X500Name owner = new X500Name(CN, OU, O, L, S, C);
    
    info.set(X509CertInfo.VALIDITY, interval);
    info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
    info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
    info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
    info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
    info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
    AlgorithmId algo = new AlgorithmId(AlgorithmId.RSAEncryption_oid);
    info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
    
    // Sign the cert to identify the algorithm that's used.
    X509CertImpl cert = new X509CertImpl(info);
    cert.sign(privkey, algorithm);
    
    // Update the algorith, and resign.
    algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
    info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
    cert = new X509CertImpl(info);
    cert.sign(privkey, algorithm);
    
    return cert;
 }
    
    /*
    This method generates public and private keys for player One
    It uses the BouncyCastle library as an instance 
    to make the key nore secure 
    */
    static public void generateKeysOne() throws Exception {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        SecureRandom random = new SecureRandom();
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
        
        generator.initialize(2048, random);
        Player_One_pair = generator.generateKeyPair();
        Key pubKey = Player_One_pair.getPublic();
        System.out.println("pubKey: " + pubKey.toString());
        Key privKey = Player_One_pair.getPrivate();
        System.out.println("privKey: " + privKey.toString());
        
        String CN = "GeoTrust Global CA";
        String OU = "Java";
        String ON = "Smith";
        String L = "London";
        String S = "London";
        String C = "England";
        
        /*
        A certificate is created and stored in a keystore
        */
        X509Certificate y = System_CA.generateCertificate(CN, OU, ON, L, S, C, Player_One_pair, 30, "SHA1withRSA");
        
        System.out.println("Certificate: " +y.toString());
        
        //create keystore
        KeyStore ks = KeyStore.getInstance("JKS");
        
        java.io.FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream("keystore_one.ks");
            ks.load(null, null);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        
        // save the cert
        ks.setCertificateEntry("player_one_cert", y);
        
        // store away the keystore
        java.io.FileOutputStream fos = null;
        try {
            fos = new java.io.FileOutputStream("keystore_one.ks");
            ks.store(fos, "password".toCharArray());
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        
    }
    
    /*
    Saimilar to the generateKeysOne
    */
    static public void generateKeysTwo() throws Exception {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        SecureRandom random = new SecureRandom();
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
        
        generator.initialize(2048, random);
        Player_Two_pair = generator.generateKeyPair();
        Key pubKey = Player_Two_pair.getPublic();
        System.out.println("pubKey: " + pubKey.toString());
        Key privKey = Player_Two_pair.getPrivate();
        System.out.println("privKey: " + privKey.toString());
        
        String CN = "GeoTrust Global CA";
        String OU = "Java";
        String ON = "Adeosun";
        String L = "Dublin";
        String S = "Dublin";
        String C = "Ireland";
        
        X509Certificate y = System_CA.generateCertificate(CN, OU, ON, L, S, C, Player_Two_pair, 30, "SHA1withRSA");
        
        System.out.println("Certificate: " +y.toString());
        
        KeyStore ks = KeyStore.getInstance("JKS");
        
        java.io.FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream("keystore_two.ks");
            ks.load(null, null);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        
        // save the cert
        ks.setCertificateEntry("player_two_cert", y);
        
        // store the keystore
        java.io.FileOutputStream fos = null;
        try {
            fos = new java.io.FileOutputStream("keystore_two.ks");
            ks.store(fos, "password".toCharArray());
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        
    }
    
    static Game xo;
    public static void main(String[] args) throws Exception {
        
        //Create Game and start the game
        xo = new Game();
    
        generateKeysTwo();
        generateKeysOne();
        
        //PlayerTwo goes forst becuase it's waiting for a messag
        //server/client
        Thread t1 = new Thread((Runnable) new PlayerTwo());
        t1.start();
        Thread t2 = new Thread((Runnable) new PlayerOne());
        t2.start();
        
    }
}   