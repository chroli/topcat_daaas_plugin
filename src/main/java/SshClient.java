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
        this.host = host;
    }

    public String exec(String commandToRun) throws IOException, InterruptedException {
        logger.debug("exec " + commandToRun);

        
        Properties properties = new Properties();
        String sshPrivateKeyFile = properties.getProperty("sshPrivateKeyFile");
        String sshUsername = properties.getProperty("sshUsername");

        Process process = Runtime.getRuntime().exec(new String[] {
            "/usr/bin/ssh", sshUsername + "@" + host,
            "-i", sshPrivateKeyFile,
            "-o", "StrictHostKeyChecking no",
            "-o", "UserKnownHostsFile /dev/null",
            "-o", "PreferredAuthentications publickey",
            "-o", "ConnectTimeout 3",
            commandToRun
        });

        String out = readInputStream(process.getInputStream());
        String error = readInputStream(process.getErrorStream());

        process.waitFor();

        return out;
    }

    private String readInputStream(InputStream inputStream) throws  IOException {
        StringBuffer out = new StringBuffer();
        BufferedInputStream in = new BufferedInputStream(inputStream);
        byte[] bytes = new byte[1];
        while (in.read(bytes) != -1) {
            out.append(new String(bytes, StandardCharsets.US_ASCII));
        }
        return out.toString();
    }

}



