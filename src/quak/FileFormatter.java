package quak;

import quak.util.FileFormatting.*;
import quak.util.Logger;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Created by Kevin on 2/3/2016.
 *
 * This is a project specific class to format the transcripts provided by the Queen's SpeechLab to prepare them for
 * importing.
 */
public class FileFormatter {

    private static String currentFindPattern;
    private static String currentReplacePattern;

    public static void main(String[] args) {
        // This gives us a callback function to change parameters of the formatter on the fly
        TranscriptFormatter.setFileFormatCallback(new Callable<Integer>() {
            public Integer call() {
                fileFormatting();
                return new Integer(0);
            }
        });

        String dir     = "data\\dialogues\\";

        String[] findPatterns = new String[]{
                "Speaker",
                "[0-9]{2}:[0-9]{2} (S)[0-9]: ",
        };
        String[] replacePatterns = new String[]{
                "S",
                " S",
        };
        ReplaceStyle[] styles = new ReplaceStyle[]{
                ReplaceStyle.ReplaceFIRST,
                ReplaceStyle.ReplaceFIRST,
        };

        if (replacePatterns.length != findPatterns.length || replacePatterns.length != styles.length)
        {
            Logger.log("Parameter arrays for find, replace, and style do not have matching size. Exiting now.");
            System.exit(1);
        }

        for (int i = 0; i < replacePatterns.length; i++) {
            currentFindPattern         = findPatterns[i];
            currentReplacePattern      = replacePatterns[i];
            ReplaceStyle style  = styles[i];
            // Run the transcript formatter, replacing the files each time with the modified version and save a logfile.
            TranscriptFormatter.RunFormatter(currentFindPattern,currentReplacePattern,dir,style,true,true);
        }


        Logger.close();
    }

    /**
     * Runs whenever a file is about to be formatted.
     */
    private static void fileFormatting(){
        // Prefix all speaker names with the filename to uniquely identify them.
        File currentFile = TranscriptFormatter.getCurrentFile();
        String currentFileName = currentFile.getName();

        // Strips file extension from name if it has one.
        if (currentFileName.indexOf(".") > 0){
            currentFileName = currentFileName.substring(0,currentFileName.lastIndexOf("."));
        }

        // Prefix all speaker names with their filename to ensure unique speaker tags
        TranscriptFormatter.setReplaceString(currentFileName+"_"+currentReplacePattern);
    }
}
