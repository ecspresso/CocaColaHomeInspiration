package com.ecspresso.ftp;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FTPS {
    private FTPSClient ftps;
    private final String username;
    private final String password;
    private final String host;
    private final int port;
    private final Logger logger = LoggerFactory.getLogger(FTPS.class);

    public FTPS(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    public FTPS(Properties prop) {
        this(prop.getProperty("username"), prop.getProperty("password"), prop.getProperty("host"), Integer.parseInt(prop.getProperty("port")));
    }

    public void connect() throws IOException {
        logger.info("Kopplar upp mot FTP server {}", host);
        ftps = new FTPSClient();
        ftps.setControlKeepAliveTimeout(300);
        ftps.connect(host, port);
        if(!FTPReply.isPositiveCompletion(ftps.getReplyCode())) {
            ftps.disconnect();
            logger.error("{} vägrade ta emot uppkopplingen.", host);
            System.exit(1);
        }
    }

    public void login() throws IOException {
        ftps.login(username, password);
        ftps.enterLocalPassiveMode();
    }


    public void uploadFile(String absolutePath, String remoteFolder, String remoteFileName) {
        try(BufferedInputStream file = new BufferedInputStream(new FileInputStream(absolutePath))) {
            logger.info("Laddar upp {}", remoteFileName);
            uploadFile(remoteFolder, remoteFileName, file);
        } catch(FileNotFoundException e) {
            logger.error("Kunde inte hitta filen {}", absolutePath, e);
            throw new RuntimeException(e);
        } catch(IOException e) {
            logger.error("Fel vid läsning av fil.", e);
            throw new RuntimeException(e);
        }
    }

    private void uploadFile(String remoteFolder, String remoteFileName, InputStream file) {
        try {
            ftps.changeWorkingDirectory(remoteFolder);
            boolean success = ftps.storeFile(remoteFileName, file);
            logger.info("Lyckades ladda upp filen: {}", success);
        } catch(IOException e) {
            logger.error("Kunde inte ladda upp {} till {}.", remoteFileName, remoteFolder, e);
        }
    }

    public void close() {
        try {
            logger.info("Loggar ut.");
            ftps.logout();
        } catch(IOException e) {
            logger.error("Kunde inte logga ut.", e);
        } finally {
            try {
                logger.info("Kopplar ifrån.");
                ftps.disconnect();
            } catch(IOException e) {
                logger.error("Kunde inte koppla ifrån.", e);
            }
        }
    }
}
