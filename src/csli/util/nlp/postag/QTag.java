/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp.postag;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import qtag.Tagger;
import csli.util.nlp.PoSTagger;

/**
 * Wrapper for Oliver Mason's QTag part-of-speech tagger http://web.bham.ac.uk/O.Mason/software/tagger/
 * 
 * @author mpurver
 */
public class QTag extends PoSTagger {

    private Tagger underlyingTagger;

    private static final String DEFAULT_RESOURCE = "../../util/ext/qtag/qtag-eng";

    /**
     * A QTag tagger using the default English tag resource files
     */
    public QTag() {
        this(DEFAULT_RESOURCE);
    }

    /**
     * A QTag tagger using a specified set of tag resource files
     */
    public QTag(String resource) {
        try {
            this.underlyingTagger = new Tagger(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see csli.util.nlp.PoSTagger#getTagSeparator()
     */
    @Override
    public String getTagSeparator() {
        return "/";
    }

    /*
     * (non-Javadoc)
     * 
     * @see csli.util.nlp.PoSTagger#tag(java.lang.String)
     */
    @Override
    public String tag(String sentence) {
        String[] words = sentence.split("\\s+");
        String[] tags = underlyingTagger.tag(words);
        return join(attach(words, tags));
    }

    /*
     * (non-Javadoc)
     * 
     * @see csli.util.nlp.PoSTagger#tag(java.lang.String[])
     */
    @Override
    public String[] tagWords(String[] words) {
        return attach(words, underlyingTagger.tag(words));
    }

    /*
     * (non-Javadoc)
     * 
     * @see csli.util.nlp.PoSTagger#tagWords(java.util.List)
     */
    @Override
    public List<String> tagWords(List<String> words) {
        return Arrays.asList(attach(words.toArray(new String[words.size()]), underlyingTagger.tag(words)));
    }

    /**
     * Given an array of words and an array of tags, stick the tags on the words
     * 
     * @param words
     *            an array of plain string words
     * @param tags
     *            an array of plain string tags
     * @return an array of tagged words (using this tagger's separator char)
     */
    private String[] attach(String[] words, String[] tags) {
        String[] tagged = new String[words.length];
        for (int i = 0; i < words.length; i++) {
            tagged[i] = words[i] + getTagSeparator() + tags[i];
        }
        return tagged;
    }

    /**
     * Test program. It reads text from a list of files, tags each word, and writes the result to standard output.
     * Usage: tagger file-name file-name ...
     */
    public static void main(String[] args) {
        test(new QTag(), args);
    }

}