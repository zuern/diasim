/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp.postag;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.util.Arrays;
import java.util.List;

import tagger.TestTagger;
import csli.util.nlp.PoSTagger;

/**
 * Wrapper for Adwait Ratnaparkhi's MXPOST maximum entropy-based tagger http://www.cis.upenn.edu/~adwait/statnlp.html
 * 
 * @author mpurver
 */
public class MXPOST extends PoSTagger {

    private static final String DEFAULT_PROJECT = "../../util/ext/mxpost/tagger.project";

    private TagThread underlyingTagger;

    private String[] args;

    public MXPOST() {
        this(DEFAULT_PROJECT);
    }

    public MXPOST(String project) {
        String[] args = { project };
        this.args = args;
        // TestTagger.main(args);
        // underlyingTagger = new TagThread(args);
        // underlyingTagger.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see csli.util.nlp.PoSTagger#getTagSeparator()
     */
    @Override
    public String getTagSeparator() {
        return "_";
    }

    /*
     * (non-Javadoc)
     * 
     * @see csli.util.nlp.PoSTagger#tag(java.lang.String)
     */
    @Override
    public String tag(String sentence) {
        // underlyingTagger.pipe(wordArray);
        PrintStream oldOut = System.out;
        InputStream oldIn = System.in;
        System.setIn(new StringBufferInputStream(sentence));
        OutputStream newOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(newOut));
        TestTagger.main(args);
        System.setIn(oldIn);
        System.setOut(oldOut);
        return newOut.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see csli.util.nlp.PoSTagger#tag(java.util.List)
     */
    @Override
    public List<String> tag(List<String> sentences) {
        // avoid start-up overhead by calling once, one sentence per line
        String paragraph = "";
        for (String sentence : sentences) {
            paragraph += sentence + "\n";
        }
        return Arrays.asList(tag(paragraph).split("\\s*\n\\s*"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see csli.util.nlp.PoSTagger#tag(java.lang.String[])
     */
    @Override
    public String[] tag(String[] sentences) {
        // avoid start-up overhead by calling once, one sentence per line
        String paragraph = "";
        for (String sentence : sentences) {
            paragraph += sentence + "\n";
        }
        return tag(paragraph).split("\\s*\n\\s*");
    }

    private class TagThread extends Thread {

        private String[] arg;

        TagThread(String[] arg) {
            this.arg = arg;
        }

        public void run() {
            TestTagger.main(arg);
        }
    }

    /**
     * Test program. It reads text from a list of files, tags each word, and writes the result to standard output.
     * Usage: tagger file-name file-name ...
     */
    public static void main(String[] args) {
        test(new MXPOST(), args);
    }

}