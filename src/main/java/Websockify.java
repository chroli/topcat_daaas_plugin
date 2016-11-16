/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author elz24996
 */


public class Websockify {
    
    private static final Logger logger = LoggerFactory.getLogger(Websockify.class);
    
    private static Websockify instance = null;

    public synchronized static Websockify getInstance() {
       if(instance == null) {
          instance = new Websockify();
       }
       return instance;
    }
    
    private Properties properties = Properties.getInstance(); 
    
    public synchronized String getToken(String username, String host) throws Exception {
        String tokensDirectory = properties.getProperty("tokensDirectory", System.getProperty("user.home") + "/tokens");
        return new Websockify.TokenFile(tokensDirectory + "/" + username).getToken(host);
    }
    
    private class TokenFile {
        
        private String filePath;
        private List<String> lines; 
        
        public TokenFile(String filePath) throws Exception {
            this.filePath = filePath;
            
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            file.createNewFile();
            
            this.lines = Files.readAllLines(Paths.get(filePath), Charset.forName("US-ASCII"));
        }
        
        public String getToken(String host) throws Exception {

            for(String line : lines){
                String[] parts = line.split(":\\s+");
                if(parts[1].equals(host  + ":5901")){
                    return parts[0];
                }
            }
            
            String token = generateToken();
            
            lines.add(token + ": " + host + ":5901");
            StringBuilder stringBuilder = new StringBuilder();
            for(String line : lines){
               stringBuilder.append(line + "\n");
            }
            
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(stringBuilder.toString());
            fileWriter.flush();
            fileWriter.close();
            
            return token;

        }
        
        private String generateToken() throws Exception {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(("" + new Random().nextLong()).getBytes());
            byte[] digest = messageDigest.digest();
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : digest) {
                stringBuilder.append(String.format("%02x", b & 0xff));
            }
            return stringBuilder.toString();
        }
        
    }
    
}
