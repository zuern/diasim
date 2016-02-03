package quak;

import csli.util.FileUtils;
import quak.util.Formatting;
import quak.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kevin on 1/20/2016.
 * This class allows for easy formatting of text-based transcripts by using regular expressions.
 * Useful for preparing files to be imported into a corpus.
 * <p/>
 * Run with argument --help for usage info
 */
public class TranscriptFormatter {

    private static File dir;
    private static Pattern findPattern = Pattern.compile("");
    private static String replaceString = "";

    private static boolean runFormatter = true; // If --help is in args, this becomes false.
    private static boolean replaceFirst = true; // True: Replace first occurrence per line. False: Replace ALL per line.
    private static boolean logToFile = false;// True: Saves a log file to dir\log.txt

    public static void main(String[] args) {
        Logger.log("TranscriptFormatter loaded.");

        // Set parameters (Which directory, what patterns to use, etc.)
        setParams(args);

        // Format the files.
        if (runFormatter) FormatFiles(dir);

        // If logging to file was enabled, save the log file to disk.
        Logger.close();
    }

    /**
     * Reads the program arguments and sets variables accordingly.
     *
     * @param args The program arguments that specify the location of files, type of formatting, etc.
     */
    private static void setParams(String[] args) {
        try {
            // Traverse the args
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];

                if (arg.compareTo("-L") == 0) {
                    logToFile = true;
                    Logger.log("Logging to file has been enabled.");
                    Logger.setLogToFile(true, dir.getAbsolutePath() + File.separator + "log.txt");
                } else if (arg.compareTo("-F") == 0) {
                    setFindPattern(args[i + 1]);
                    Logger.log("Set the search pattern to " + args[i + 1]);
                } else if (arg.compareTo("-R") == 0) {
                    replaceString = args[i + 1];
                    Logger.log("Set the replace pattern to " + args[i + 1]);
                } else if (arg.compareTo("-D") == 0) {
                    setDir(args[i + 1]);
                    Logger.log("Set the directory to " + args[i + 1]);
                } else if (arg.compareTo("-rF") == 0) {
                    replaceFirst = true;
                    Logger.log("Set the replace style to \"Replace First\"");
                } else if (arg.compareTo("-rA") == 0) {
                    replaceFirst = false;
                    Logger.log("Set the replace style to \"Replace All\"");
                } else if (arg.compareTo("-h") == 0 || arg.compareTo("--help") == 0) {
                    showHelp();
                    runFormatter = false;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.log("Program arguments were formatted incorrectly. Please see the help file for proper usage.");
            Logger.log("Exiting now...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                System.exit(1);
            }
            System.exit(1);
        }
    }

    /**
     * Formats all files in the directory, saving them in a sister directory called "Formatted".
     * If the Formatted directory exists already, this wipes all the files in it and starts fresh.
     *
     * @return True if successful.
     */
    private static boolean FormatFiles(File directory) {
        try {
            File formattedDir = new File(directory + File.separator + "formatted");
            if (formattedDir.exists())
                formattedDir.delete(); // Wipe the data in that directory
            formattedDir.mkdir();

            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    Logger.log(String.format("Skipping \"%s\" because it is a directory. " +
                            "Any files in this directory haven't been changed.", file.getName()));
                    continue;
                } else if (file.getName().compareTo("log.txt") == 0) continue; // Don't try and format the logfile.
                if (!formatFile(file, formattedDir))
                    return false; // Return false if there is a problem formatting a file
            }
            return true;
        } catch (NullPointerException ex) {
            Logger.log("An error occurred trying to find files in the directory. " +
                    "Perhaps you forgot to escape the slashes in the directory path? " +
                    "Fix this by adding a second slash to each one in the path.");
            return false;
        }
    }


    /**
     * Formats an individual file using the findPattern and replaceString. Saves the file in /formatted/F-{Filename}.
     *
     * @param file        The file to format
     * @param Destination The directory to save the formatted files to
     * @return True if successfully formatted and saved file
     */
    private static boolean formatFile(File file, File Destination) {
        try {
            if (!Destination.isDirectory()) throw new IOException("The Save Destination is not a directory!");

            ArrayList<String> originalLines = new ArrayList<String>();
            ArrayList<String> formattedLines = new ArrayList<String>();

            FileUtils.getFileLines(file, originalLines);

            for (String line : originalLines) {
                Matcher m = findPattern.matcher(line);
                // Perform the replacement and add to formattedLines
                if (replaceFirst)
                    formattedLines.add(m.replaceFirst(replaceString));
                else
                    formattedLines.add(m.replaceAll(replaceString));
            }

            String formattedFilePath =
                    String.format("%s%sf_%s", Destination.getAbsolutePath(), File.separator, file.getName());

            Logger.log(String.format("Formatted \"%s\", saving it to \"%s\".", file.getName(), formattedFilePath));
            FileUtils.writeObjectsToFile(formattedFilePath, formattedLines);

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Outputs usage info to the console
     */
    private static void showHelp() {
        int rightPad = 7;

        Logger.log("Usage:");
        Logger.log(Formatting.padRight("-F", rightPad)
                + "Sets the search pattern (must be a regular expression with backslashes escaped)");
        Logger.log(Formatting.padRight("-R", rightPad)
                + "Sets the replace pattern (must be a regular expression with backslashes escaped)");
        Logger.log(Formatting.padRight("-D", rightPad)
                + "Sets the directory to recursively search for transcripts in");
        Logger.log(Formatting.padRight("-rF", rightPad)
                + "(Default) Replaces the FIRST occurrence of the pattern in the current line.");
        Logger.log(Formatting.padRight("-rA", rightPad)
                + "Replaces ALL occurrences of the pattern in the current line.");
        Logger.log(Formatting.padRight("-L", rightPad)
                + "Saves a log file called \"log.txt\"");
        Logger.log(Formatting.padRight("-h", rightPad)
                + "Display this helpfile.");
        Logger.log(Formatting.padRight("--help", rightPad)
                + "Display this helpfile.");

    }


    private static void setFindPattern(String findPattern) {
        TranscriptFormatter.findPattern = Pattern.compile(findPattern);
    }

    private static void setDir(String dir) {
        TranscriptFormatter.dir = new File(dir);
    }
}
