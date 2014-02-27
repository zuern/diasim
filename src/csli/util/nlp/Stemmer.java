/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp;

import java.util.ArrayList;
import java.util.List;

import csli.util.Scored;

/**
 * An abstract word stemmer.
 */
public abstract class Stemmer implements java.io.Serializable {

    /**
     * Stem a word string. Should return NULL on error.
     * 
     * @param wordString
     *            the word string to stem
     * @return the word stem (root form) as a string
     * @author mpurver
     */
    public abstract String stem(String wordString);

    /**
     * Stem a scored word string. Should return NULL on error.
     * 
     * @param wordString
     *            the scored word string to stem
     * @return the word stem (root form) as a scored string
     * @author mpurver
     */
    public Scored<String> stem(Scored<String> wordString) {
        return new Scored<String>(stem(wordString.getObject()), wordString.getScore());
    }

    /**
     * Stem a list of word strings. Should return NULL on error.
     * 
     * @param wordList
     *            the list of word strings to stem
     * @return a list of word stems (root form) as strings
     * @author mpurver
     */
    public List<String> stem(List<String> wordList) {
        List<String> stemList = new ArrayList<String>(wordList.size());
        for (String word : wordList) {
            stemList.add(stem(word));
        }
        return stemList;
    }

    /**
     * Stem a list of scored word strings. Should return NULL on error.
     * 
     * @param wordList
     *            the list of scored word strings to stem
     * @return a list of word stems (root form) as scored strings
     * @author mpurver
     */
    public List<Scored<String>> stemScored(List<Scored<String>> wordList) {
        List<Scored<String>> stemList = new ArrayList<Scored<String>>(wordList.size());
        for (Scored<String> word : wordList) {
            stemList.add(stem(word));
        }
        return stemList;
    }
}