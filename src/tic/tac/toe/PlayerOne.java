package tic.tac.toe;

import java.io.*;
import java.net.*;
import java.security.Key;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import tcdIO.Terminal;
import static tic.tac.toe.PlayerTwo.two_get_msg_port;
import static tic.tac.toe.PlayerTwo.two_get_signACK_port;
import static tic.tac.toe.PlayerTwo.two_get_sign_port;

import static tic.tac.toe.System_CA.order;

import static tic.tac.toe.System_CA.One_get_signACK; 
import static tic.tac.toe.System_CA.Two_get_msg;
import static tic.tac.toe.System_CA.One_get_sign;
import static tic.tac.toe.System_CA.Two_get_signACK;
import static tic.tac.toe.System_CA.One_get_msg;

/**
 *
 * @author Ayokunle Adeosun
 */
public class PlayerOne implements Runnable{    
    
    static Terminal t = new Terminal("Player One");
    
    static int one_get_sign_port, one_get_signACK_port, one_get_msg_port =0;
    static String input = "A";
    
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
        
        //t.println("PlayerOne: Sending singature to PlayerTwo");
        
        /*
        Now that program has checked for win/draw
        if it's niether, it'll proceed here
        
        The bouncyCastle library is used as a security provider
        */
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        /*
        Gets the user input and 
        inserts it into the grid
        */
        boolean insert_success = false;
        while(insert_success == false){
            getInput();
            insert_success = System_CA.xo.input_X(input);
        }
        
        byte[] data = input.getBytes();
        
        /*
        Create a signature using the RSA cryptosystem
        and sign it with the player's private key 
        */
        Signature sig = Signature.getInstance("RSA");
        sig.initSign(System_CA.Player_One_pair.getPrivate());
        sig.update(data);
        byte[] signatureBytes = sig.sign();
        //System.out.println("Singature:" + new BASE64Encoder().encode(signatureBytes));
        
        sig.initVerify(System_CA.Player_One_pair.getPublic());
        sig.update(data);
        
        /*
        Verify player two's X.509 cert
        byt loading it from the Certificate Authority (Keystore)
        */
        KeyStore ks = KeyStore.getInstance("JKS");
        
        java.io.FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream("keystore_two.ks");
            ks.load(fis, "password".toCharArray());
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        
        /*
        Get the cert and verify that its a valid cert 
        by using player two's public key
        
        if it's the wrong key or cert, stop the program
        */
        X509Certificate player_two_cert = (X509Certificate)ks.getCertificate("player_two_cert");
        try{
        player_two_cert.verify(System_CA.Player_Two_pair.getPublic());
        }catch(Exception e){
            System.out.print("Wrong public key or wrong certificate.");
            System.exit(0);
        }
        
        /*
        Encrypt the signataure with player two's public key.
        BouncyCastle is used with RSA cryptosystem
        */
        Cipher cipher = null;
        byte[] cipherText = null;
        
        boolean success = false;
        while(success == false){
            try{
                SecureRandom random = new SecureRandom();
                cipher = Cipher.getInstance("RSA", "BC");
                cipher.init(Cipher.ENCRYPT_MODE, System_CA.Player_Two_pair.getPublic(), random);
                cipherText = cipher.doFinal(signatureBytes);
                success = true;
            }catch(Exception e){
                success = false;
            }
        }
        /*
        cipher.init(Cipher.DECRYPT_MODE, System_CA.Player_Two_pair.getPrivate());
        byte[] plainText = cipher.doFinal(cipherText);
        //System.out.println("plain : " + new String(plainText));
        */
        
        /*
        Send the cipherText to player Two using the
        given port and call the method that gets an ACK
        */
        String host = "localhost";
        int port = two_get_sign_port;
        
        Socket playerTwo = new Socket(host, port);
        
        PrintStream output = new PrintStream(playerTwo.getOutputStream());
           
        output.write(cipherText);
        
        playerTwo.close();
        getSignACK();
    }
    
    /*
     Sends signature ACK msg to player two
    */
    static void sendSignACK() throws Exception{
        /*
        The while loop works as a scheduler
        making sure that the two players work in
        parallel to each other
        
        It acts as the waiting process
        */
        while(Two_get_signACK != true){
            //t.println("PlayerOne is waiting to: Sending singature ACK to PlayerTwo");
        }
        //t.println("PlayerOne: Sending singature ACK to PlayerTwo");
        //simply sends a "sACK" string to player two
        
        String host = "localhost";
        int port = two_get_signACK_port;
        
        Socket playerTwo = new Socket(host, port);
        
        PrintStream output = new PrintStream(playerTwo.getOutputStream());
           
        output.write("sACK".getBytes());
        
        playerTwo.close();
        
        //proceeds to get the msg
        getMsg();
    }
    
    //Gets signature from player two
    static void getSignature() throws Exception{
        //checks for wins or draws
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
        one_get_sign_port  = server.getLocalPort();
        One_get_sign = true;
        
        /*
        t.println("Server is waiting for an incoming connection from\nHost = "+
                InetAddress.getLocalHost().getCanonicalHostName()
                + "\nPort = " +server.getLocalPort());
        */
        
        Socket playerTwo = server.accept();
        
        InputStream inputStream = playerTwo.getInputStream();  

        // read from the stream  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        byte[] content = new byte[ 256 ];  
        int bytesRead = -1;  
        while( ( bytesRead = inputStream.read( content ) ) != -1 ) {  
            baos.write( content, 0, bytesRead );  
        } // while 

        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() ); 
        
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Key pubKey = System_CA.Player_One_pair.getPublic();
        Key privKey = System_CA.Player_One_pair.getPrivate();
        
        //decrypt the data recieved using player's private
        Cipher cipher = Cipher.getInstance("RSA", "BC");
        cipher.init(Cipher.DECRYPT_MODE, privKey);
        byte[] plainText = cipher.doFinal(content);
        
        playerTwo.close();
        order++;
        
        //send ACK to player two
        sendSignACK();
    }
    
    //sen msg to player two
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
        
        while(Two_get_msg != true){
            //t.println("PlayerOne wiating to: Sending message to PlayerTwo");
        }
                
        byte[] data = input.getBytes();
        
        /*
        Verify player two's X.509 cert
        if the can be verified with player two's public key 
        then proceed
        */
        KeyStore ks = KeyStore.getInstance("JKS");
        
        java.io.FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream("keystore_two.ks");
            ks.load(fis, "password".toCharArray());
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        X509Certificate player_two_cert = (X509Certificate)ks.getCertificate("player_two_cert");
        try{
        player_two_cert.verify(System_CA.Player_Two_pair.getPublic());
        }catch(Exception e){
            System.out.println("Wrong public key or certificate.");
            System.exit(0);
        }
        
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        //encrypt the msg using player one's public key
        SecureRandom random = new SecureRandom();
        Cipher cipher = Cipher.getInstance("RSA", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, System_CA.Player_Two_pair.getPublic(), random);
        byte[] cipherText = cipher.doFinal(data);
        //System.out.println("cipher: " + new String(cipherText));
        
        /*
        cipher.init(Cipher.DECRYPT_MODE, System_CA.Player_Two_pair.getPrivate());
        byte[] plainText = cipher.doFinal(cipherText);
        //System.out.println("plain : " + new String(plainText));
        */
        
        
        String host = "localhost";
        int port = two_get_msg_port;
        
        Socket playerTwo = new Socket(host, port);
        
        PrintStream output = new PrintStream(playerTwo.getOutputStream());
           
        output.write(cipherText);
        
        playerTwo.close();
        getSignature();
    }

    static void getSignACK() throws Exception{
        
                
        ServerSocket server = new ServerSocket(0);
        one_get_signACK_port = server.getLocalPort();
        
        /*
        t.println("Server is waiting for an incoming connection from\nHost = "+
                InetAddress.getLocalHost().getCanonicalHostName()
                + "\nPort = " +server.getLocalPort());
        */
        One_get_signACK = true;
        
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
        
        t.println("Signature Recieved");
        t.print("\n");
        
        
        playerOne.close();
        sendMsg();
        order = 4;
        
    }
    
    static void getMsg() throws Exception{
        
        ServerSocket server = new ServerSocket(0);
        One_get_msg = true;
        one_get_msg_port = server.getLocalPort();
        /*
        System.out.println("Server is waiting for an incoming connection from\nHost = "+
                InetAddress.getLocalHost().getCanonicalHostName()
                + "\nPort = " +server.getLocalPort());*/
        Socket PlayerTwo = server.accept();
        
        InputStream inputStream = PlayerTwo.getInputStream();  

        // read from the stream  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        byte[] content = new byte[ 256 ];  
        int bytesRead = -1;  
        while( ( bytesRead = inputStream.read( content ) ) != -1 ) {  
            baos.write( content, 0, bytesRead );  
        } // while 

        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() ); 
        
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Key pubKey  = System_CA.Player_One_pair.getPublic();
        Key privKey = System_CA.Player_One_pair.getPrivate();
        
        Cipher cipher = Cipher.getInstance("RSA", "BC");
        
        cipher.init(Cipher.DECRYPT_MODE, privKey);
        byte[] plainText = cipher.doFinal(content);
        t.println("Message : " + new String(plainText));
        System_CA.xo.print(t);
        
        PlayerTwo.close();
        order =0;
        
        //sendSignature();
    }
    
    @Override
    public void run() {
        try {
            
            while(true){
                sendSignature();
            }
        } catch (Exception ex) {
            Logger.getLogger(PlayerOne.class.getName()).log(Level.SEVERE, null, ex);
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