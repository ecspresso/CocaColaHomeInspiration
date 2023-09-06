import com.ecspresso.ftp.FTPS;
import com.ecspresso.mau.MAU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException {
        Logger logger = LoggerFactory.getLogger(Main.class);
        logger.info("---------------------------------------------------------------------------");
        logger.info("Start ny session.");
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        Properties prop = new Properties();
        logger.info("Leta efter ftps.properties.");
        try (FileInputStream fis = new FileInputStream("conf.properties")) {
            logger.info("Läser in ftps.properties.");
            prop.load(fis);
        } catch (IOException e) {
            logger.error("Kunde inte läsa in mejlinställningar.");
            throw new RuntimeException(e);
        }

        String outputLocation = prop.getProperty("outputLocation", "./");
        String indexFolder = prop.getProperty("indexFolder", "./");
        String indexTomorrowFolder = prop.getProperty("indexTomorrowFolder");
        String indexInTwoDaysFolder = prop.getProperty("indexInTwoDaysFolder");

        if(!outputLocation.endsWith("/")) outputLocation = outputLocation + "/";
        if(!indexFolder.endsWith("/")) indexFolder = indexFolder + "/";
        if(!indexTomorrowFolder.endsWith("/")) indexTomorrowFolder = indexTomorrowFolder + "/";
        if(!indexInTwoDaysFolder.endsWith("/")) indexInTwoDaysFolder = indexInTwoDaysFolder + "/";

        MAU mau = new MAU(prop.getProperty("template"), outputLocation);
        mau.run();

        FTPS ftps = new FTPS(prop);
        try {
            ftps.connect();
        } catch(IOException e) {
            logger.error("Kunde inte koppla upp mot FTP servern.");
            throw new RuntimeException(e);
        }

        try {
            ftps.login();
        } catch(IOException e) {
            logger.error("Kunde inte koppla upp mot FTP servern.");
            throw new RuntimeException(e);
        }

        ftps.uploadFile(outputLocation+"indexToday.html", indexFolder, "index.html");
        ftps.uploadFile(outputLocation+"indexTomorrow.html", indexTomorrowFolder, "index.html");
        ftps.uploadFile(outputLocation+"indexInTwoDays.html", indexInTwoDaysFolder, "index.html");

        ftps.close();
        logger.info("Avslutar programmet.");
    }
}
