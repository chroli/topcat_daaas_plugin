package org.icatproject.topcatdaaasplugin;

import net.schmizz.sshj.*;
import net.schmizz.sshj.userauth.keyprovider.*;
import net.schmizz.sshj.common.*;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


import java.security.*;
import java.util.concurrent.TimeUnit;
import java.io.*;

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

        process.waitFor();

        return IOUtils.readFully(process.getInputStream()).toString();
    }

}



