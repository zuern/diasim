package quak;

import quak.corpus.TranscriptCorpus;

import java.io.File;

/**
 * This class exposes methods to import and create a corpus for analysis with the diasim library.
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


    public static void main(String[] args) {
        CreateCorpus(new File("data\\dialogues\\"));
    }
}
