package quak;

import qmul.corpus.CorpusParser;
import quak.corpus.TextCorpus;
import java.io.File;
import static qmul.align.AlignmentTester.runTest;

/**
 * Provides toolset to execute and evaluate Alignment Tests using the DIASIM Library
 *
 * @author Kevin
 */
public abstract class TestingTools {

    /**
     * Call this method to import a directory of transcripts.
     * @param TranscriptsDirectory
     *      The directory where all the transcripts are saved
     * @return
     *      A TextCorpus object
     */
    public static TextCorpus CreateCorpus(File TranscriptsDirectory) {
        return new TextCorpus("QuakCorpus",TranscriptsDirectory,false);
    }
    /**
     * Call this method to import a directory of transcripts and save them as a corpus file.
     * @param TranscriptsDirectory
     *          The directory where all the transcripts are saved
     */
    public static TextCorpus CreateCorpus(File TranscriptsDirectory, File CorpusFile) {
        TextCorpus corpus = CreateCorpus(TranscriptsDirectory);
        corpus.writeToFile(CorpusFile);
        return corpus;
    }

    /**
     * This method will parse the corpus and calculate syntactic information via the stanford parser.
     * It will re-save the corpus back to disk with syntactic information.
     * @param corpusFile
     *      The corpus to parse
     */
    public static TextCorpus ParseCorpus(File corpusFile) {
        TextCorpus corpus = (TextCorpus) TextCorpus.readFromFile(corpusFile);

        CorpusParser parser = new CorpusParser();
        parser.setParser();                     // Run with default stanford settings
        if (parser.parse(corpus) > 0) {         // Only write to file if it actually parsed something
            corpus.writeToFile(corpusFile);     // Modifies corpus by adding syntactic information.
            return corpus;
        }
        else
        {
            System.out.println("Parser didn't parse anything. Exiting now");
            System.exit(1);
            return null;
        }
    }

    /**
     * Run a test using the parameters stored in t.
     * @param t
     *      The configuration to use for this test. You can create a TestConfiguration object easily using TestConfiguration.create.
     */
    public static void RunSingleTest(TestConfiguration t) {
        // Run the test with the supplied parameters
        runTest(
                t.baseDir,t.corpusRoot,
                t.randType,t.simType,t.unitType,t.winType,
                t.monteCarlo,
                t.xlsOutput,t.plotGraphs);
    }

    /**
     * Run a series of tests, one test for each TestConfiguration found in testParametersArray.
     * @param testParametersArray
     *      The array of configurations for each test to be run with.
     */
    public static void RunMultipleTests(TestConfiguration[] testParametersArray) {
        for (TestConfiguration testParameters : testParametersArray)
            RunSingleTest(testParameters);
    }
}
