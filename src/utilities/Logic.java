package utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class Logic {
    private static PrintWriter log;
    private static boolean logWritable = true;

    public static void printToUser(String toPrint) {
        System.out.println(toPrint);
        writeToLog(toPrint);
    }

    private static void writeToLog(String toWrite) {
        if (!logWritable) {
            return;
        }
        if (log == null) {
            try {
                log = new PrintWriter(new FileWriter(Config.LOG_FILE, StandardCharsets.UTF_8, true), true);
            } catch (IOException e) {
                stopLogging(e.getMessage());
                return;
            }
        }
        log.println(toWrite);
        if (log.checkError()) {
            stopLogging("write failed");
        }
    }

    private static void stopLogging(String reason) {
        logWritable = false;
        System.out.println("Could not write " + Config.LOG_FILE + ", so this session is not logged: " + reason);
    }
}
