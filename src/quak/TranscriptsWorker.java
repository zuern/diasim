package quak;

import java.io.File;

/**
 * This class exposes methods to test the various faculties of the diasim library
 */
public abstract class TranscriptsWorker {

    public static void main(String[] args) {
        // Configuration settings for the batch tests.
        String baseDir          = "";
        String corpusName       = "QuakCorpus";
        int monteCarlo          = 20;
        boolean outputExcel     = true;
        boolean outputGraphs    = false;

        // Create the corpus and parse it.
        //TestingTools.CreateCorpus(transcriptsDir,corpusFile + ".corpus");
        //TestingTools.ParseCorpus(corpusFile);

        // Set up the 4 tests we'd like to run.
        TestConfiguration[] tests = new TestConfiguration[] {
                // LEX and SYN using WIN_USE_OtherSpeakerAllOtherTurnWindower
                TestConfiguration.create(
                        baseDir,
                        corpusName,
                        TestConfiguration.RAND_Random4,
                        TestConfiguration.SIM_SYNTACTIC_SIMILARITYMEASURE,
                        TestConfiguration.UNIT_USE_TurnAverageSimilarityMeasure,
                        TestConfiguration.WIN_USE_OtherSpeakerAllOtherTurnWindower,
                        monteCarlo,
                        outputExcel,
                        outputGraphs
                ),
                TestConfiguration.create(
                        baseDir,
                        corpusName,
                        TestConfiguration.RAND_Random4,
                        TestConfiguration.SIM_LEXICAL_SIMILARITYMEASURE,
                        TestConfiguration.UNIT_USE_TurnAverageSimilarityMeasure,
                        TestConfiguration.WIN_USE_OtherSpeakerAllOtherTurnWindower,
                        monteCarlo,
                        outputExcel,
                        outputGraphs
                ),
                // LEX and SYN using WIN_USE_OtherSpeakerTurnWindower
                TestConfiguration.create(
                        baseDir,
                        corpusName,
                        TestConfiguration.RAND_Random4,
                        TestConfiguration.SIM_SYNTACTIC_SIMILARITYMEASURE,
                        TestConfiguration.UNIT_USE_TurnAverageSimilarityMeasure,
                        TestConfiguration.WIN_USE_OtherSpeakerTurnWindower,
                        monteCarlo,
                        outputExcel,
                        outputGraphs
                ),
                TestConfiguration.create(
                        baseDir,
                        corpusName,
                        TestConfiguration.RAND_Random4,
                        TestConfiguration.SIM_LEXICAL_SIMILARITYMEASURE,
                        TestConfiguration.UNIT_USE_TurnAverageSimilarityMeasure,
                        TestConfiguration.WIN_USE_OtherSpeakerTurnWindower,
                        monteCarlo,
                        outputExcel,
                        outputGraphs
                )
        };

        // Run our tests using the supplied parameters.
        TestingTools.RunSingleTest(tests[0]);
    }
}
