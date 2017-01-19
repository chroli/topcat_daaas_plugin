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

    public String exec(String commandToRun) throws IOException {
        Security.addProvider(new BouncyCastleProvider());
        SSHClient client = new SSHClient();
        
        try {
            Properties properties = new Properties();

            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.connect(host);

            PKCS8KeyFile keyFile = new PKCS8KeyFile();
            keyFile.init(new File(properties.getProperty("ssh_private_key_file")));
            client.authPublickey(properties.getProperty("ssh_username"), keyFile);
            Session session = client.startSession();

            try {
                Command command = session.exec(commandToRun);
                String out =  IOUtils.readFully(command.getInputStream()).toString();
                command.join(10, TimeUnit.SECONDS);
                return out;
            } finally {
                session.close();
            }
        } finally {
            client.disconnect();
        }
        
    }

}
