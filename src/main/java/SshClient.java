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

public class SshClient {
    
    private String username;
    private String host;
    private String privateKey;

    public SshClient(String username, String host, String privateKey){
        this.username = username;
        this.host = host;
        this.privateKey = privateKey;
    }

    public String exec(String commandToRun) throws IOException {
        Security.addProvider(new BouncyCastleProvider());
        SSHClient client = new SSHClient();
        
        try {
            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.connect(host);

            PKCS8KeyFile keyFile = new PKCS8KeyFile();
            keyFile.init(new File(privateKey));
            client.authPublickey(username, keyFile);
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
