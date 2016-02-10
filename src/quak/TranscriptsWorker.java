package quak;

import qmul.corpus.CorpusParser;
import quak.corpus.TranscriptCorpus;
import quak.util.Logger;

import java.io.File;

/**
 * This class exposes methods to test the various faculties of the diasim library
 */
public abstract class TranscriptsWorker {

    /**
     * Call this method to import a directory of transcripts and save them as a corpus file.
     * @param TranscriptsDirectory
     *          The directory where all the transcripts are saved
     */
    public static void CreateCorpus(File TranscriptsDirectory) {
        TranscriptCorpus corpus = new TranscriptCorpus("QuakCorpus",TranscriptsDirectory,false);

        corpus.setupCorpus();

        corpus.writeToFile(new File("corpus"));
    }

    /**
     * This method will parse the corpus and calculate syntactic information via the stanford parser.
     * It will re-save the corpus back to disk with syntactic information.
     * @param corpusFile
     */
    public static void ParseCorpus(File corpusFile) {
        TranscriptCorpus corpus = (TranscriptCorpus)TranscriptCorpus.readFromFile(corpusFile);

        CorpusParser parser = new CorpusParser();
        parser.setParser(); // Run with default stanford settings
        parser.parse(corpus); // Modifies corpus by adding syntactic information.

        Logger.log("Parsed");
    }

    public static void main(String[] args) {
        //CreateCorpus(new File("data\\dialogues\\"));
        ParseCorpus(new File("data\\corpus"));
    }
}
