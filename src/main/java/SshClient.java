package org.icatproject.topcatdaaasplugin;


import org.apache.commons.io.IOUtils;
import java.io.*;
import java.nio.charset.StandardCharsets;

import java.nio.channels.SocketChannel;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SshClient {
    private static final Logger logger = LoggerFactory.getLogger(MachinePool.class);
    
    private String host;

    public SshClient(String host){
        logger.info("new SshClient");

        this.host = host;

        //wait for port to open
        try {
            while(true){
                try {
                    SocketChannel.open(new InetSocketAddress(host, 22)).close();
                    break;
                } catch(Exception e){
                    Thread.sleep(1000);
                }
            }
        } catch(InterruptedException e){
            logger.error("InterruptedException was triggered while waiting for port 22 to open on " + host);
        }

    }

    public String exec(String commandToRun) throws IOException, InterruptedException {
        logger.info("exec " + commandToRun);

        
        Properties properties = new Properties();
        String sshPrivateKeyFile = properties.getProperty("sshPrivateKeyFile");
        String sshUsername = properties.getProperty("sshUsername");

        Process process = Runtime.getRuntime().exec(new String[] {
            "/usr/bin/ssh", sshUsername + "@" + host,
            "-i", sshPrivateKeyFile,
            "-o", "StrictHostKeyChecking no",
            commandToRun
        });

        String out = IOUtils.toString(process.getInputStream(), StandardCharsets.US_ASCII);
        String error = IOUtils.toString(process.getErrorStream(), StandardCharsets.US_ASCII);

        int exitVal = process.waitFor();

        if(exitVal > 0){
            logger.info("exec error: " + commandToRun + ": " + error);
        }

        return out;
    }

}



