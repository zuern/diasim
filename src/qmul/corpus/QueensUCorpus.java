package qmul.corpus;

import java.io.File;

/**
 * Created by Kevin on 1/16/2016.
 */
public class QueensUCorpus extends DialogueCorpus {

    private static final String ID                      = "QueensU";
    private static final String BASE_DIR                = "/import/QUCorpus/";
    private static final String DATA_DIR                = "/import/QUCorpus/Data/";


    /**
     * Create a QueensU corpus, reading data from the default directory.
     */
    public QueensUCorpus() { this(BASE_DIR); }

    /**
     * Creates a QueensU corpus using the specified data directory, reading all data upon instantiation.
     * @param baseDir
     *            Override the default data directory with your own.
     */
    public QueensUCorpus(String baseDir) { this(baseDir, false); }

    /**
     * Creates a QueensU corpus using the specified data directory.
     * @param baseDir
     *          Override the default data directory with your own.
     * @param dynamic
     *          If set to true, corpus will read dialogues from file as required, rather than reading all data when constructed.
     */
    public QueensUCorpus(String baseDir, boolean dynamic) {
        super(ID,new File(baseDir,DATA_DIR), dynamic);
    }

    /**
     * Set up the corpus, reading all dialogues and relevant data in from file. If dynamic, corpus will read dialogues
     * from file as required later, rather than reading all data in when constructed now
     *
     * @return success
     */
    @Override
    public boolean setupCorpus() {
        // TODO: Implement this method
        return false;
    }

    /**
     * Read a dialogue in from disk
     *
     * @param name the name of the file (slightly corpus-dependent whether this includes path, suffix etc)
     * @return success
     */
    @Override
    public boolean loadDialogue(String name) {
        return false;
    }
}
