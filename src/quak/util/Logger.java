package quak.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by Kevin on 2/2/2016.
 * <p/>
 * This helps with logging text to console or to a logfile.
 * The log file is set globally so calling log from any class will log to the same file.
 */
public abstract class Logger {
    private static boolean logToFile = false;
    private static PrintWriter logFile;

    /**
     * Set whether save the log to a file.
     *
     * @param logToFile True: Logs to filePath. False: Does not log. If it has logged before, it closes the log.
     * @param filePath  The path to save the log to.
     */
    public static void setLogToFile(boolean logToFile, String filePath) {
        try {
            if (!logToFile) {
                logFile.close();
                Logger.logToFile = false;
            } else {
                logFile = new PrintWriter(new FileWriter(filePath, false));
                Logger.logToFile = true;
            }
        } catch (IOException ex) {
            System.out.println("There was a problem setting the logfile. Here's the stacktrace: ");
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Log what you are doing. Prints message to console.
     *
     * @param message What you are trying to log
     */
    public static void log(String message) {
        System.out.println(message);
        if (logToFile)
            logFile.println(String.format("%s: %s", new Date().toString(), message));
    }

    /**
     * Saves and closes the logfile.
     *
     * @return True if save and close successful. False otherwise.
     */
    public static boolean close() {
        if (logToFile)
            logFile.close();
        return true;
    }
}
