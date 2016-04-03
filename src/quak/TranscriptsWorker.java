package quak;

import qmul.align.AlignmentTester;
import qmul.align.SentenceSyntacticSimilarityMeasure;
import qmul.corpus.*;
import qmul.util.similarity.SyntacticSimilarityMeasure;
import qmul.window.SentenceWindower;
import qmul.window.TurnWindower;
import quak.corpus.TextCorpus;
import quak.util.Logger;

import javax.xml.soap.Text;
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
    public static TextCorpus CreateCorpus(File TranscriptsDirectory, File CorpusFile) {
        TextCorpus corpus = new TextCorpus("QuakCorpus",TranscriptsDirectory,false);

        corpus.writeToFile(CorpusFile);

        return corpus;
    }

    /**
     * This method will parse the corpus and calculate syntactic information via the stanford parser.
     * It will re-save the corpus back to disk with syntactic information.
     * @param corpusFile
     */
    public static void ParseCorpus(File corpusFile) {
        TextCorpus corpus = (TextCorpus) TextCorpus.readFromFile(corpusFile);

        CorpusParser parser = new CorpusParser();
        parser.setParser();                 // Run with default stanford settings
        if (parser.parse(corpus) > 0)       // Only write to file if it actually parsed something
            corpus.writeToFile(corpusFile); // Modifies corpus by adding syntactic information.
        else
        {
            Logger.log("Parser didn't parse anything. Exiting now");
            System.exit(1);
        }
    }

    /**
     * Test method to experiment with generating random baseline
     * @param originalCorpus
     *      Where the original corpus is located
     * @param randomizedCorpus
     *      Where to save the randomized corpus.
     */
    private static void GenerateRandomBaseline(File originalCorpus, File randomizedCorpus) {
        // Create the random corpus
        RandomCorpus rc = new RandomCorpus(
                TextCorpus.readFromFile(originalCorpus),
                RandomCorpus.RAND_ALL_TURNS,
                RandomCorpus.PAD_RAND_TURNS,
                RandomCorpus.LENGTH_IN_TURNS,
                true,   // matchGenre
                false); // don't care if speaker matched with self from another file (each file has unique speakers anyways)
        System.out.println();System.out.println();System.out.println();
        for (Dialogue d : rc.getDialogues()) {
            System.out.println("GOT NEXT DIALOGUE: " + d.getId());
            for (DialogueSentence s : d.getSents())
                System.out.println(s.getTranscription());

        rc.writeToFile(randomizedCorpus);
        }
    }

    private static void RunAlignmentTester(String corpusRoot,boolean generateXLSFile, boolean plotGraphs) {
        AlignmentTester<DialogueTurn> aTester = new AlignmentTester<DialogueTurn>();
        aTester.runTest(
                "",                     // baseDir
                corpusRoot,             // CorpusRoot (no idea)
                "random4",              // randType = RAND_ALL_TURNS
                "syn",                  // syntactic similarity measure
                "turn",                 // unitType (no other options)
                "any",                  // winType (no idea what this is)
                0,                      // num repetitions for Monte Carlo
                generateXLSFile,        // XLS output
                plotGraphs              // plot graphs
        );
    }

    public static void main(String[] args) {
        File transcriptsDir     = new File("E:\\K2 Workspace\\Latif_DiaSim\\formattedTranscripts");
        File corpusFile         = new File("E:\\K2 Workspace\\Latif_DiaSim\\corpora\\quakCorpus.corpus");
        transcriptsDir          = new File("data\\dialogues\\");
        corpusFile              = new File("data\\sampledata.corpus");

        if (!transcriptsDir.exists()) {
            System.err.println("transcriptsDir does not exist. Check the filename!");
            System.exit(1);
        }

        //TextCorpus created = CreateCorpus(transcriptsDir,corpusFile);
        //ParseCorpus(corpusFile);
        RunAlignmentTester(corpusFile.toString().split("\\.")[0],true,true);
    }
}
