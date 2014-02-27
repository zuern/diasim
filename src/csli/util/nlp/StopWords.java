/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * An English stop word list with methods to check membership, remove members from other collections etc.
 * 
 * @author mpurver
 */
public class StopWords {

    private static String[] swArray = { "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your",
            "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it",
            "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this",
            "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
            "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as",
            "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through",
            "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off",
            "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all",
            "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own",
            "same", "so", "than", "too", "very" };

    private static HashSet<String> swSet = new HashSet<String>();

    static {
        swSet.clear();
        for (int i = 0; i < swArray.length; i++) {
            swSet.add(swArray[i]);
        }
    }

    /**
     * Remove stop words from a Collection of String words
     * 
     * @param data
     */
    public static void remove(Collection<String> data) {
        data.removeAll(swSet);
    }

    /**
     * Remove stop words from an array of String words
     * 
     * @param data
     * @return a new array without the stop words
     */
    public static String[] remove(String[] in) {
        ArrayList<String> cont = new ArrayList<String>();
        for (String s : in) {
            if (!swSet.contains(s)) {
                cont.add(s);
            }
        }
        return cont.toArray(new String[cont.size()]);
    }

    /**
     * Remove stop words from a whitespace-separated String of words
     * 
     * @param data
     * @return a new String without the stop words
     */
    public static String remove(String in) {
        String[] inA = in.split("\\s+");
        String[] outA = remove(inA);
        String out = "";
        for (int i = 0; i < outA.length; i++) {
            out += outA[i] + " ";
        }
        return out;
    }

    /**
     * Check if a String word is a stop word
     * 
     * @param word
     * @return true if word is in the current stop-word set, false if not
     */
    public static boolean contains(String word) {
        return swSet.contains(word);
    }

    /**
     * Get the current stop-word list as an array
     * 
     * @return an array of String stop-words
     */
    public static String[] getArray() {
        return swArray;
    }

    /**
     * Get the current stop-word list as a Collection
     * 
     * @return a Collection (HashSet) of String stop-words
     */
    public static Collection<String> getSet() {
        return swSet;
    }

}