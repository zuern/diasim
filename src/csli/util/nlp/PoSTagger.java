/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import csli.util.FileUtils;
import csli.util.Scored;

/**
 * An abstract part-of-speech tagger.
 * 
 * @author mpurver
 */
public abstract class PoSTagger {

    /**
     * Tag a string (possibly a sentence containing multiple words separated by whitespace)
     * 
     * @param sentence
     *            the string to tag. White space taken to separate words. No punctuation/case normalization is performed
     *            here.
     * @return a corresponding string containing the tagged word(s), or null on error
     */
    public abstract String tag(String sentence);

    /**
     * Tag an array of sentences. The default implementation is just to call the tag(String) method on each member.
     * 
     * @param sentences
     *            the array of sentences to tag
     * @return an array of tagged sentences, or null on error
     */
    public String[] tag(String[] sentences) {
        String[] tagged = new String[sentences.length];
        for (int i = 0; i < sentences.length; i++) {
            tagged[i] = tag(sentences[i]);
        }
        return tagged;
    }

    /**
     * Tag a list of sentences. The default implementation is just to call the tag(String) method on each member.
     * 
     * @param sentences
     *            the list of strings to tag
     * @return a list of tagged sentences, or null on error
     */
    public List<String> tag(List<String> sentences) {
        List<String> tagged = new ArrayList<String>();
        for (String sentence : sentences) {
            tagged.add(tag(sentence));
        }
        return tagged;
    }

    /**
     * Tag a sentence represented as an array of words in linear order. The default implementation is to stringify, call
     * the tag(String) method, then split.
     * 
     * @param words
     *            the array of words to tag
     * @return an array of tagged words, or null on error
     */
    public String[] tagWords(String[] words) {
        return tag(join(words)).split("\\s+");
    }

    /**
     * Tag a sentence represented as a list of words in linear order. The default implementation is to stringify, call
     * the tag(String) method, then split.
     * 
     * @param words
     *            the list of words to tag
     * @return a list of tagged words, or null on error
     */
    public List<String> tagWords(List<String> words) {
        String[] w = words.toArray(new String[words.size()]);
        return Arrays.asList(tagWords(w));
    }

    /**
     * Tag a scored sentence
     * 
     * @param sentence
     *            the scored sentence to tag
     * @return the tagged sentence as a scored string
     */
    public Scored<String> tag(Scored<String> sentence) {
        return new Scored<String>(tag(sentence.getObject()), sentence.getScore());
    }

    /**
     * Get the character this tagger uses to separate words from tags.
     * 
     * @return the character
     */
    public abstract String getTagSeparator();

    /**
     * Join an array of words together to make a whitespace-separated sentence
     * 
     * @param words
     *            an array of words
     * @return the sentence with a single space character between each word
     */
    protected static String join(String[] words) {
        String sentence = "";
        for (String word : words) {
            sentence += " " + word;
        }
        if (sentence.length() > 0) {
            sentence = sentence.substring(1);
        }
        return sentence;
    }

    /**
     * A convenience method for development testing.
     * 
     * @param tagger
     * @param args
     */
    protected static void test(PoSTagger tagger, String[] args) {
        String s1 = "hello there nice to meet you";
        String s2 = "world champion breakfast";
        // try them as sentences
        System.out.println("s1 as string: " + tagger.tag(s1));
        System.out.println("s2 as string: " + tagger.tag(s2));
        // try them as word arrays
        System.out.println("s1 as word array: " + Arrays.toString(tagger.tagWords(s1.split("\\s+"))));
        System.out.println("s2 as word array: " + Arrays.toString(tagger.tagWords(s2.split("\\s+"))));
        // try them as sentence arrays
        String[] a = { s1, s2 };
        System.out.println("s1,s2 as sentence array: " + Arrays.toString(tagger.tag(a)));
        // process a file if specified
        for (String arg : args) {
            try {
                ArrayList<String> lines = (ArrayList<String>) FileUtils.getFileLines(arg, null);
                for (String line : lines) {
                    System.out.println("IN: " + line);
                    System.out.println("OUT: " + tagger.tag(line));
                }
            } catch (IOException e) {
                System.out.println("error reading " + arg);
                break;
            }
        }
    }

}