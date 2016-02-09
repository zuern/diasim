package quak.util.FileFormatting;

import csli.util.FileUtils;
import quak.util.Formatting;
import quak.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kevin on 1/20/2016.
 * This class allows for easy formatting of text-based transcripts by using regular expressions.
 * Useful for preparing files to be imported into a corpus.
 * Run with argument --help for usage info
 */
public class TranscriptFormatter {

    private static File         dir;
    private static File         currentFile; // Set as the formatter runs, allowing outside access to see current file.
    private static Pattern      findPattern     = Pattern.compile("");
    private static String       replaceString   = "";

    private static boolean      runFormatter    = true;  // If --help is in args, this becomes false.
    private static ReplaceStyle replaceStyle    = ReplaceStyle.ReplaceFIRST;
    private static boolean      overWriteFiles  = false; // By default doesn't overwrite the files during formatting.

    private static Callable<Integer> fileFormatCallback;

    public static void main(String[] args) {
        Logger.log("TranscriptFormatter loaded.");

        // Set parameters (Which directory, what patterns to use, etc.)
        setParams(args);

        // Format the files.
        if (runFormatter) FormatFiles(dir,overWriteFiles);

        // If logging to file was enabled, save the log file to disk.
        Logger.close();
    }

    /**
     * Format ALL files in a specified directory by doing line-by-line REGEX string replacements.
     * @param searchPattern
     *          The regular expression to match with.
     * @param replaceString
     *          The String to replace the matched substring with.
     * @param filesDirectory
     *          The path to the directory containing the files to be formatted. Note: All files in that directory will
     *          be formatted.
     * @param style
     *          The style of replacement to apply to each line (replace only the first match in each line or replace all)
     * @param logToFile
     *          If true, will generate a log file and save to filesDirectory as "log.txt"
     */
    public static void RunFormatter (String searchPattern, String replaceString, String filesDirectory,
                                     ReplaceStyle style, boolean logToFile, boolean overWriteOriginalFiles) {

        Logger.log("TranscriptFormatter loaded.");

        replaceStyle = style;

        setFindPattern(searchPattern);
        setDir(filesDirectory);

        TranscriptFormatter.replaceString   = replaceString;
        TranscriptFormatter.overWriteFiles  = overWriteOriginalFiles;

        Logger.setLogToFile(logToFile,dir.getAbsolutePath() + File.separator + "log.txt");

        FormatFiles(dir,overWriteOriginalFiles);
    }

    /**
     * Returns the current file the formatter is working on. This allows extracting info about file like name, etc.
     */
    public static File getCurrentFile() { return currentFile; }

    /**
     * Set a callback to be run every time a file is about to be formatted. (Lets you change parameters on the fly)
     */
    public static void setFileFormatCallback(Callable<Integer> callback) { fileFormatCallback = callback; }

    /**
     * Returns the replacement string that will be used during formatting.
     */
    public static String getReplaceString() {
        return replaceString;
    }

    /**
     * Change the replacement string.
     * @param r
     *          The string to replace any matched expression with
     */
    public static void setReplaceString(String r) { replaceString = r; }

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
                    replaceStyle = ReplaceStyle.ReplaceFIRST;
                    Logger.log("Set the replace style to \"Replace First\"");
                } else if (arg.compareTo("-rA") == 0) {
                    replaceStyle = ReplaceStyle.ReplaceALL;
                    Logger.log("Set the replace style to \"Replace All\"");
                } else if (arg.compareTo("-O") == 0) {
                    overWriteFiles = true;
                    Logger.log("Set to overwrite files instead of saving formatted files as a copy.");
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
     * @param directory
     *          The directory where the files are located
     * @param modifyOriginalFiles
     *          If set to true, will overwrite the original files in the directory.
     *
     * @return True if successful.
     */
    private static boolean FormatFiles(File directory, boolean modifyOriginalFiles) {
        try {
            File formattedDir;

            if (modifyOriginalFiles)
                formattedDir = new File(directory.getAbsolutePath());
            else
                formattedDir = new File(directory + File.separator + "formatted");

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

            currentFile = file;

            callback(); // run the callback function

            ArrayList<String> originalLines = new ArrayList<String>();
            ArrayList<String> formattedLines = new ArrayList<String>();

            FileUtils.getFileLines(file, originalLines);

            for (String line : originalLines) {
                Matcher m = findPattern.matcher(line);
                // Perform the replacement and add to formattedLines
                if (replaceStyle == ReplaceStyle.ReplaceFIRST)
                    formattedLines.add(m.replaceFirst(replaceString));
                else
                    formattedLines.add(m.replaceAll(replaceString));
            }

            String formattedFilePath =
                    String.format("%s%s%s", Destination.getAbsolutePath(), File.separator, file.getName());

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
        Logger.log(Formatting.padRight("-O",rightPad)
                + "Overwrite files instead of saving in a separate directory.");
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

    private static void callback() {
        try {
            if (fileFormatCallback != null) fileFormatCallback.call();
        }
        catch(Exception ex) {
            Logger.log("ERROR: callback function threw an exception. Removing callback and continuing...");
            fileFormatCallback = null;
        }
    }
}
