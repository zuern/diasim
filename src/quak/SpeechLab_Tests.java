package quak;

import quak.tests.TestingConfiguration;
import quak.tests.TestingTools;

import java.io.File;

/**
 * This class exposes methods to test the various faculties of the diasim library
 */
public abstract class SpeechLab_Tests {

    public static void main(String[] args) {
        // Configuration settings for the batch tests.
        String baseDir          = "";
        String corpusName       = "QuakCorpus";
        int monteCarlo          = 1000;
        boolean outputExcel     = true;
        boolean outputGraphs    = false;

        File transcriptsDir     = new File("../\\Latif_Diasim\\formattedTranscripts\\");

        // Create the corpus and parse it
        //TestingTools.CreateCorpus(transcriptsDir, new File("../\\Latif_Diasim\\corpora\\" + corpusName + ".corpus"));
        //TestingTools.ParseCorpus(new File("../\\Latif_Diasim\\corpora\\" + corpusName + ".corpus"));

        TestingConfiguration[] TestConfigs = new TestingConfiguration[] {
                // LEX and SYN using WIN_USE_OtherSpeakerAllOtherTurnWindower
                TestingConfiguration.create(
                        baseDir,
                        corpusName,
                        TestingConfiguration.RAND_Random4,
                        TestingConfiguration.SIM_SENTENCE_SYNTACTIC,
                        TestingConfiguration.UNIT_USE_TurnAverageSimilarityMeasure,
                        TestingConfiguration.WIN_USE_OtherSpeakerAllOtherTurnWindower,
                        monteCarlo,
                        outputExcel,
                        outputGraphs
                ),
                TestingConfiguration.create(
                        baseDir,
                        corpusName,
                        TestingConfiguration.RAND_Random4,
                        TestingConfiguration.SIM_SENTENCE_LEXICAL,
                        TestingConfiguration.UNIT_USE_TurnAverageSimilarityMeasure,
                        TestingConfiguration.WIN_USE_OtherSpeakerAllOtherTurnWindower,
                        monteCarlo,
                        outputExcel,
                        outputGraphs
                ),
                // LEX and SYN using WIN_USE_OtherSpeakerTurnWindower
                TestingConfiguration.create(
                        baseDir,
                        corpusName,
                        TestingConfiguration.RAND_Random4,
                        TestingConfiguration.SIM_SENTENCE_SYNTACTIC,
                        TestingConfiguration.UNIT_USE_TurnAverageSimilarityMeasure,
                        TestingConfiguration.WIN_USE_OtherSpeakerTurnWindower,
                        monteCarlo,
                        outputExcel,
                        outputGraphs
                ),
                TestingConfiguration.create(
                        baseDir,
                        corpusName,
                        TestingConfiguration.RAND_Random4,
                        TestingConfiguration.SIM_SENTENCE_LEXICAL,
                        TestingConfiguration.UNIT_USE_TurnAverageSimilarityMeasure,
                        TestingConfiguration.WIN_USE_OtherSpeakerTurnWindower,
                        monteCarlo,
                        outputExcel,
                        outputGraphs
                )
        };

        TestingTools.RunMultipleTests(TestConfigs);
    }
}
