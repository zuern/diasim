package quak.tests;

/**
 * Describes a set of parameters needed to run a single test using qmul.align.AlignmentTester.
 * @author Kevin
 */
public class TestingConfiguration {
    public String baseDir, corpusRoot, randType, simType, unitType, winType;
    public int monteCarlo;
    public boolean xlsOutput, plotGraphs;

    public TestingConfiguration(String baseDir, String corpusRoot, String randType, String simType, String unitType,
                                String winType, int monteCarlo, boolean xlsOutput, boolean plotGraphs) {
        this.baseDir    = baseDir;
        this.corpusRoot = corpusRoot;
        this.randType   = randType;
        this.simType    = simType;
        this.unitType   = unitType;
        this.winType    = winType;
        this.monteCarlo = monteCarlo;
        this.xlsOutput  = xlsOutput;
        this.plotGraphs = plotGraphs;
    }

    /**
     * Create a configuration to run a test.
     * @param baseDir
     *          The base directory where all the files needed to run the test are stored.
     * @param corpusRoot
     *          The corpus file name
     * @param randType
     *          One of RAND_XXXXX. See qmul.corpus.RandomCorpus for description of what each of these options do.
     * @param simType
     *          One of SIM_XXXX.
     * @param unitType
     *          One of UNIT_XXXX.
     * @param winType
     *          One of WIN_XXXX.
     * @param monteCarlo
     *          Iteration for Monte Carlo
     * @param xlsOutput
     *          True to save an Excel Spreadsheet with the data
     * @param plotGraphs
     *          True to display graphs after running the test.
     * @return
     *          A TestingConfiguration object to pass to quak.tests.TestingTools.runSingleTest or quak.tests.TestingTools.runMultipleTests
     */
    public static TestingConfiguration create (
            String baseDir, String corpusRoot, String randType, String simType, String unitType,
            String winType, int monteCarlo, boolean xlsOutput, boolean plotGraphs
    )
    {
        return new TestingConfiguration( baseDir, corpusRoot, randType, simType, unitType,
                winType, monteCarlo, xlsOutput, plotGraphs);
    }

    // Selects the similiarity measure found in qmul.align
    // lex, tok, syn,
    public static final String SIM_SENTENCE_LEXICAL                     = "lex";
    public static final String SIM_SENTENCE_LEXICAL_TOKEN               = "tok";
    public static final String SIM_SENTENCE_SYNTACTIC                   = "syn";
    public static final String SIM_SENTENCE_LAST_CONSTRUCTION           = "gries";

    // Selects the unit type found in qmul.align
    public static final String UNIT_USE_SENTENCES                       = "sent";
    public static final String UNIT_USE_TurnAverageSimilarityMeasure    = "turn";
    public static final String UNIT_USE_TurnConcatSimilarityMeasure     = "tuco";

    // Selects the windower type found in qmul.window
    public static final String WIN_USE_OtherSpeakerTurnWindower         = "oth";
    public static final String WIN_USE_SameSpeakerTurnWindower          = "sam";
    public static final String WIN_USE_OtherSpeakerAllOtherTurnWindower = "alloth";
    public static final String WIN_USE_SameSpeakerAllOtherTurnWindower  = "allsam";
    public static final String WIN_USE_SentenceWindower                 = "any";


    // For description of what these all do, see RandomCorpus.RAND_XXXX
    /**
     * Create Random corpus using this config:
     * RandomCorpus(corpus, RandomCorpus.RAND_OTHER_TURNS, RandomCorpus.PAD_CUT, RandomCorpus.LENGTH_IN_TURNS, true, true, 0)
     */
    public static final String RAND_Random1                             = "random1";
    /**
     * Create Random corpus using this config:
     * RandomCorpus(corpus, RandomCorpus.RAND_BEST_LENGTH_MATCH, RandomCorpus.PAD_CUT, RandomCorpus.LENGTH_IN_TURNS, true, true, 0)
     */
    public static final String RAND_Random2                             = "random2";
    /**
     * Create Random corpus using this config:
     * RandomCorpus(corpus, RandomCorpus.RAND_BEST_LENGTH_RAND, RandomCorpus.PAD_CUT, RandomCorpus.LENGTH_IN_TURNS, true, true, 0)
     */
    public static final String RAND_Random3                             = "random3";
    /**
     * Create Random corpus using this config:
     * RandomCorpus(corpus, RandomCorpus.RAND_ALL_TURNS, RandomCorpus.PAD_CUT, RandomCorpus.LENGTH_IN_TURNS, true, true, 0)
     */
    public static final String RAND_Random4                             = "random4";
    /**
     * Create Random corpus using this config:
     * RandomCorpus(corpus, RandomCorpus.RAND_ALL_SENTS, RandomCorpus.PAD_CUT, RandomCorpus.LENGTH_IN_SENTS, true, true, 0)
     */
    public static final String RAND_Random5                             = "random5";
    /**
     * Create Random corpus using this config:
     * RandomCorpus(corpus, RandomCorpus.RAND_SAME_SPEAKER, RandomCorpus.PAD_CUT, RandomCorpus.LENGTH_IN_TURNS, true, false, 0)
     */
    public static final String RAND_Random_Same                         = "random_same";
    /**
     * Create Random corpus using this config:
     * RandomCorpus(corpus, RandomCorpus.RAND_S2ME_SPEAKER, RandomCorpus.PAD_CUT, RandomCorpus.LENGTH_IN_TURNS, true, false, 0)
     */
    public static final String RAND_Random_S2me                         = "random_s2me";

}