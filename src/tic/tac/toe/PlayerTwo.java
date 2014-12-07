/*
Not commented becuase it's just like playerOne class
*/
package tic.tac.toe;

import java.io.*;
import java.net.*;
import java.security.Key;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;
import javax.crypto.Cipher;
import java.util.logging.Level;
import java.util.logging.Logger;
import static tic.tac.toe.PlayerOne.one_get_msg_port;
import static tic.tac.toe.PlayerOne.one_get_signACK_port;
import static tic.tac.toe.PlayerOne.one_get_sign_port;
import static tic.tac.toe.PlayerOne.sendSignature;
import static tic.tac.toe.System_CA.One_get_msg;
import static tic.tac.toe.System_CA.One_get_sign;

import static tic.tac.toe.System_CA.order;

import static tic.tac.toe.System_CA.Two_get_sign;
import static tic.tac.toe.System_CA.One_get_signACK; 
import static tic.tac.toe.System_CA.Two_get_msg;
import static tic.tac.toe.System_CA.Two_get_signACK;

import tcdIO.*;


/**
 *
 * @author Ayokunle Adeosun
 */
public class PlayerTwo implements Runnable{
    static int two_get_sign_port, two_get_msg_port, two_get_signACK_port= 0;
    
    static Terminal t = new Terminal("Player Two");
    
    static String input = " ";
    
    static void getInput(){
        boolean success = false;
        while(success == false){
            try{
                t.println("Enter a number that matches the position");
                
                input  = t.readString();
                boolean isnumber = isNumeric(input);
                if(input.length() == 1 && isnumber == true){
                    success = true;
                }else{
                    t.println("Please try again.");
                    success = false;
                }
            }catch(Exception e){
                
            }
        }
    }
    
    static void sendSignature() throws Exception{
        System_CA.xo.checkWinner();
        System_CA.xo.getIfDraw();
        
        if(System_CA.xo.draw == true){
            t.println("It's a draw");
            t.setEnabled(false);
        }
        if(System_CA.xo.x_wins == true){
            t.println("Player One wins");
            t.setEnabled(false);
        }
        if(System_CA.xo.o_wins == true){
            t.println("Player Two wins");
            t.setEnabled(false);
        }
        while(One_get_sign != true){
            //t.println("PlayerTwo  is waiting to: Sending singature to PlayerOne");
        }
        
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        boolean insert_success = false;
        while(insert_success == false){
            getInput();
            insert_success = System_CA.xo.input_O(input);
        }
        
        byte[] data = input.getBytes();
        
        /*
        Verify X.509 cert
        */
        KeyStore ks = KeyStore.getInstance("JKS");
        
        java.io.FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream("keystore_one.ks");
            ks.load(fis, "password".toCharArray());
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        X509Certificate player_one_cert = (X509Certificate)ks.getCertificate("player_one_cert");
        try{
        player_one_cert. verify(System_CA.Player_One_pair.getPublic());
        }catch(Exception e){
            System.out.print("Wrong public key.");
            System.exit(0);
        }
        
        Signature sig = Signature.getInstance("RSA");
        sig.initSign(System_CA.Player_Two_pair.getPrivate());
        sig.update(data);
        byte[] signatureBytes = sig.sign();
        //System.out.println("Singature:" + new BASE64Encoder().encode(signatureBytes));
        
        sig.initVerify(System_CA.Player_Two_pair.getPublic());
        sig.update(data);
        
        Cipher cipher = null;
        byte[] cipherText = null;
        
        boolean success = false;
        while(success == false){
            try{
                SecureRandom random = new SecureRandom();
                cipher = Cipher.getInstance("RSA", "BC");
                cipher.init(Cipher.ENCRYPT_MODE, System_CA.Player_One_pair.getPublic(), random);
                cipherText = cipher.doFinal(signatureBytes);
                success = true;
            }catch(Exception e){
                success = false;
            }
        }
       
        String host = "localhost";
        int port = one_get_sign_port;
        
        Socket playerOne = new Socket(host, port);
        
        PrintStream output = new PrintStream(playerOne.getOutputStream());
           
        output.write(cipherText);
        
        playerOne.close();
        
        getSignACK();
    }
    
    static void sendMsg() throws Exception{
        System_CA.xo.checkWinner();
        System_CA.xo.getIfDraw();
        
        if(System_CA.xo.draw == true){
            t.println("It's a draw");
            t.setEnabled(false);
        }
        if(System_CA.xo.x_wins == true){
            t.println("Player One wins");
            t.setEnabled(false);
        }
        if(System_CA.xo.o_wins == true){
            t.println("Player Two wins");
            t.setEnabled(false);
        }
        
        while(One_get_msg != true){
            //t.println("PlayerTwo is waiting to: Sending message to PlayerOne");
        }
        
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        byte[] data = input.getBytes();
        
        /*
        Verify X.509 cert
        */
        KeyStore ks = KeyStore.getInstance("JKS");
        
        java.io.FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream("keystore_one.ks");
            ks.load(fis, "password".toCharArray());
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        X509Certificate player_one_cert = (X509Certificate)ks.getCertificate("player_one_cert");
        try{
        player_one_cert. verify(System_CA.Player_One_pair.getPublic());
        }catch(Exception e){
            System.out.print("Wrong public key.");
            System.exit(0);
        }
        
        SecureRandom random = new SecureRandom();
        Cipher cipher = Cipher.getInstance("RSA", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, System_CA.Player_One_pair.getPublic(), random);
        byte[] cipherText = cipher.doFinal(data);
        
        String host = "localhost";
        int port = one_get_msg_port;
        
        Socket playerTwo = new Socket(host, port);
        
        PrintStream output = new PrintStream(playerTwo.getOutputStream());
           
        output.write(cipherText);
        
        playerTwo.close();    
        //getSignature();
    }
    
    static void getMsg() throws Exception{
        
        ServerSocket server = new ServerSocket(0);
        two_get_msg_port = server.getLocalPort();
        Two_get_msg = true;
        /*
        System.out.println("Server is waiting for an incoming connection from\nHost = "+
                InetAddress.getLocalHost().getCanonicalHostName()
                + "\nPort = " +server.getLocalPort());*/
        Socket playerOne = server.accept();
        
        InputStream inputStream = playerOne.getInputStream();  

        // read from the stream  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        byte[] content = new byte[ 256 ];  
        int bytesRead = -1;  
        while( ( bytesRead = inputStream.read( content ) ) != -1 ) {  
            baos.write( content, 0, bytesRead );  
        } // while 

        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() ); 
        
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Key pubKey = System_CA.Player_Two_pair.getPublic();
        Key privKey = System_CA.Player_Two_pair.getPrivate();
        
        Cipher cipher = Cipher.getInstance("RSA", "BC");
        
        cipher.init(Cipher.DECRYPT_MODE, privKey);
        byte[] plainText = cipher.doFinal(content);
        t.println("Message : " + new String(plainText));
        System_CA.xo.print(t);
        playerOne.close();
        order++;
        
        sendSignature();
    }
    
    static void getSignature() throws Exception{
        System_CA.xo.print(t);
        
        System_CA.xo.checkWinner();
        System_CA.xo.getIfDraw();
        
        if(System_CA.xo.draw == true){
            t.println("It's a draw");
            t.setEnabled(false);
        }
        if(System_CA.xo.x_wins == true){
            t.println("Player One wins");
            t.setEnabled(false);
        }
        if(System_CA.xo.o_wins == true){
            t.println("Player Two wins");
            t.setEnabled(false);
        }
        
        ServerSocket server = new ServerSocket(0);
        two_get_sign_port = server.getLocalPort();
        Two_get_sign = true;
        
        /*System.out.println("Server is waiting for an incoming connection from\nHost = "+
                InetAddress.getLocalHost().getCanonicalHostName()
                + "\nPort = " +server.getLocalPort());*/
        Socket playerOne = server.accept();
        
        InputStream inputStream = playerOne.getInputStream();  

        // read from the stream  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        byte[] content = new byte[ 256 ];  
        int bytesRead = -1;  
        while( ( bytesRead = inputStream.read( content ) ) != -1 ) {  
            baos.write( content, 0, bytesRead );  
        } // while 
        
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Key pubKey = System_CA.Player_Two_pair.getPublic();
        Key privKey = System_CA.Player_Two_pair.getPrivate();
        
        Cipher cipher = Cipher.getInstance("RSA", "BC");
        
        cipher.init(Cipher.DECRYPT_MODE, privKey);
        byte[] plainText = cipher.doFinal(content);
        
        playerOne.close();
        server.setReuseAddress(true);
        order++;
        
        sendSignACK();
    }
    
    static void sendSignACK() throws Exception{
        while(One_get_signACK != true){
           //t.println("PlayerTwo waiting to: Sending singature ACK to PlayerOne"); 
        }
        
        String host = "localhost";
        int port = one_get_signACK_port;
        
        Socket playerTwo = new Socket(host, port);
        
        PrintStream output = new PrintStream(playerTwo.getOutputStream());
           
        output.write("sACK".getBytes());
        
        playerTwo.close();
        
        order++;
        getMsg();
    }
    
    static void getSignACK() throws Exception{
        
        ServerSocket server = new ServerSocket(0);
        two_get_signACK_port = server.getLocalPort();
        Two_get_signACK = true;
        
        /*System.out.println("Server is waiting for an incoming connection from\nHost = "+
                InetAddress.getLocalHost().getCanonicalHostName()
                + "\nPort = " +server.getLocalPort());*/
        Socket playerOne = server.accept();
        
        InputStream inputStream = playerOne.getInputStream();  

        // read from the stream  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        byte[] content = new byte[ 256 ];  
        int bytesRead = -1;  
        while( ( bytesRead = inputStream.read( content ) ) != -1 ) {  
            baos.write( content, 0, bytesRead );  
        } // while 

        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() ); 
        t.println("Signature Recieved ");
        t.println("");
        
        playerOne.close();
        
        order++;
        sendMsg();
    }

    @Override
    public void run() {
        try {
            
            while(true){
                getSignature();
            }
        } catch (Exception ex) {
            Logger.getLogger(PlayerTwo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static boolean isNumeric(String str){  
        try{  
            double d = Double.parseDouble(str);  
        }catch(NumberFormatException nfe){  
            return false;  
        }  
        return true;  
    }
}