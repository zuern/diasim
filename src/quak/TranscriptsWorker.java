package quak;

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

        // Create the corpus and parse it
        //TestingTools.CreateCorpus(transcriptsDir, new File(corpusName + ".corpus"));
        //TestingTools.ParseCorpus(new File(corpusName + ".corpus"));

        TestingConfiguration[] TestConfigs = new TestingConfiguration[] {
                // LEX and SYN using WIN_USE_OtherSpeakerAllOtherTurnWindower
                TestingConfiguration.create(
                        baseDir,
                        corpusName,
                        TestingConfiguration.RAND_Random4,
                        TestingConfiguration.SIM_SYNTACTIC_SIMILARITYMEASURE,
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
                        TestingConfiguration.SIM_LEXICAL_SIMILARITYMEASURE,
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
                        TestingConfiguration.SIM_SYNTACTIC_SIMILARITYMEASURE,
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
                        TestingConfiguration.SIM_LEXICAL_SIMILARITYMEASURE,
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
