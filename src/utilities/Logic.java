package utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logic {
    private static boolean logWritable = true;

    public static void printToUser(String toPrint) {
        System.out.println(toPrint);
        writeToLog(toPrint);
    }

    private static void writeToLog(String toWrite) {
        if (!logWritable) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true))) {
            writer.write(toWrite);
            writer.newLine();
        } catch (IOException e) {
            logWritable = false;
            System.out.println("Could not write log.txt, so this session is not logged: " + e.getMessage());
        }
    }
}
