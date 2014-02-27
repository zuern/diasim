/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract lemmatiser.
 */
public abstract class Lemmatiser implements java.io.Serializable {

    /**
     * Produce a lemma (pair of root form & part-of-speech) from a word string.
     * Should return NULL on error.
     * 
     * @param wordString
     *            the word string to lemmatise
     * @return the corresponding lemma
     * @author mpurver
     */
    public abstract Lemma getLemma(String wordString);

    /**
     * Produce a list of lemmas (pairs of root form & part-of-speech) from a
     * list of word strings. Should return NULL on error.
     * 
     * @param wordList
     *            the list of word strings to lemmatise
     * @return the corresponding list of lemmas
     * @author mpurver
     */
    public List<Lemma> getLemma(List<String> wordList) {
        List<Lemma> lemmaList = new ArrayList<Lemma>();
        for (String word : wordList) {
            lemmaList.add(getLemma(word));
        }
        return lemmaList;
    }

    /**
     * Generate a morphological word string from a lemma (pair of root form &
     * part-of-speech). Should return NULL on error.
     * 
     * @param lemma
     *            the lemma from which to generate
     * @return the corresponding word string
     * @author mpurver
     */
    public abstract String getWord(Lemma lemma);

    /**
     * Produce a list of morphological word strings from a list of lemmas (pairs
     * of root form & part-of-speech). Should return NULL on error.
     * 
     * @param lemmaList
     *            the list of lemmas from which to generate
     * @return the corresponding list of word strings
     * @author mpurver
     */
    public List<String> getWord(List<Lemma> lemmaList) {
        List<String> wordList = new ArrayList<String>();
        for (Lemma lemma : lemmaList) {
            wordList.add(getWord(lemma));
        }
        return wordList;
    }

}