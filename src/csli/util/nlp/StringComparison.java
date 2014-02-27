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
import java.util.Iterator;

import csli.util.Config;

/**
 * @author mpurver
 */
public class StringComparison {

    private static boolean caseSens = false;

    private static boolean useSet = false;

    private static Synonyms syn = new Synonyms();

    static {
        if (Config.main.get("util.nlp.comparison.casesens") != null) {
            caseSens = Config.main.getBoolean("util.nlp.comparison.casesens");
            System.out.println("Comparison case sensitivity set to " + caseSens);
        }
        if (Config.main.get("util.nlp.comparison.set") != null) {
            useSet = Config.main.getBoolean("util.nlp.comparison.set");
            System.out.println("Comparison set usage set to " + caseSens);
        }
    }

    /**
     * 
     * Returns the proportion of members of bag which are also in key
     * 
     * @param key
     * @param bag
     * @return
     */
    public static double rawWordCompare(Collection key, Collection bag) {

        double num = 0.0;
        for (Iterator it = bag.iterator(); it.hasNext();) {
            if (key.contains(it.next().toString())) {
                num++;
            }
        }
        return (num > 0.0 ? num / (double) bag.size() : 0.0);

    }

    /**
     * Returns the proportion of members of bag which are also in key
     * 
     * @param key
     * @param bag
     * @return
     */
    public static double rawWordCompare(String key, String bag) {
        return rawWordCompare(stringToWordBag(key), stringToWordBag(bag));
    }

    /**
     * Returns the proportion of non-stopword members of bag which are also in key
     * 
     * @param key
     * @param bag
     * @return
     */
    public static double stoppedWordCompare(Collection key, Collection bag) {
        ArrayList stoppedBag = new ArrayList(bag);
        StopWords.remove(stoppedBag);
        return rawWordCompare(key, stoppedBag);
    }

    /**
     * Returns the proportion of non-stopword members of bag which are also in key
     * 
     * @param key
     * @param bag
     * @return
     */
    public static double stoppedWordCompare(String key, String bag) {
        return stoppedWordCompare(stringToWordBag(key), stringToWordBag(bag));
    }

    /**
     * Splits a string at whitespace into a bag of words (a set if useSet is true, a multiset otherwise), converted to
     * lower case if caseSens is false
     */
    private static Collection stringToWordBag(String str) {
        String[] strs;
        if (caseSens) {
            strs = str.split("\\s+");
        } else {
            strs = str.toLowerCase().split("\\s+");
        }
        Collection bag;
        if (useSet) {
            bag = new HashSet();
        } else {
            bag = new ArrayList();
        }
        for (int i = 0; i < strs.length; i++) {
            bag.add(strs[i]);
        }
        return bag;
    }

    /**
     * Levenshtein string edit distance. From Michael Gilleland, Merriam Park Software
     * 
     * @param s
     * @param t
     * @return the distance
     */
    public static int levenshtein(String string1, String string2) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        String s = (caseSens ? string1 : string1.toLowerCase());
        String t = (caseSens ? string2 : string2.toLowerCase());

        // Step 1
        n = s.length();
        m = t.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];

        // Step 2
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Step 3
        for (i = 1; i <= n; i++) {
            s_i = s.charAt(i - 1);

            // Step 4
            for (j = 1; j <= m; j++) {
                t_j = t.charAt(j - 1);

                // Step 5
                if (s_i == t_j) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                // Step 6
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);

            }

        }

        // Step 7
        return d[n][m];

    }

    /**
     * Returns the proportion of members of bag which are synonyms of elements in key
     * 
     * @param key
     * @param bag
     * @return
     */
    public static double rawSynonymCompare(Collection key, Collection bag) {

        double num = 0.0;
        for (Iterator it = bag.iterator(); it.hasNext();) {
            String b = it.next().toString();
            for (Iterator it2 = key.iterator(); it2.hasNext();) {
                String k = it2.next().toString();
                try {
                    if (syn.areSynonyms(b, k)) {
                        num++;
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // System.exit(0);
                }
            }
        }
        return (num > 0.0 ? num / (double) bag.size() : 0.0);

    }

    /**
     * Returns the proportion of members of bag which are synonyms of elements in key
     * 
     * @param key
     * @param bag
     * @return
     */
    public static double rawSynonymCompare(String key, String bag) {
        return rawSynonymCompare(stringToWordBag(key), stringToWordBag(bag));
    }

    /**
     * Returns the proportion of non-stopword members of bag which are synonyms of elements in key
     * 
     * @param key
     * @param bag
     * @return
     */
    public static double stoppedSynonymCompare(Collection key, Collection bag) {
        ArrayList stoppedBag = new ArrayList(bag);
        StopWords.remove(stoppedBag);
        return rawSynonymCompare(key, bag);
    }

    /**
     * Returns the proportion of non-stopword members of bag which are synonyms of elements in key
     * 
     * @param key
     * @param bag
     * @return
     */
    public static double stoppedSynonymCompare(String key, String bag) {
        return stoppedSynonymCompare(stringToWordBag(key), stringToWordBag(bag));
    }

    /**
     * See whether bag contains all the words in key, in the correct order and adjacent
     * 
     * @param key
     * @param bag
     * @return true if so, false if not
     */
    public static boolean containsAllWordsAdjacent(String key, String bag) {
        String regexp = (caseSens ? "" : "(?i)") + key.replaceAll("\\s+", "\\s+");
        return bag.matches(".*" + regexp + ".*");
    }

    /**
     * See whether bag contains all the non-stop-words in key, in the correct order and adjacent (modulo stop-words)
     * 
     * @param key
     * @param bag
     * @return true if so, false if not
     */
    public static boolean containsAllNonStopWordsAdjacent(String key, String bag) {
        String regexp = (caseSens ? "" : "(?i)") + StopWords.remove(key).replaceAll("\\s+", "\\s+");
        return StopWords.remove(bag).matches(".*" + regexp + ".*");
    }

    /**
     * See whether bag contains all the words in key, in the correct order (but not necessarily adjacent)
     * 
     * @param key
     * @param bag
     * @return true if so, false if not
     */
    public static boolean containsAllWordsInOrder(String key, String bag) {
        String regexp = (caseSens ? "" : "(?i)") + key.replaceAll("\\s+", "\\b.*\\b");
        return bag.matches(".*" + regexp + ".*");
    }

    /**
     * See whether bag contains all the non-stop-words in key, in the correct order (but not necessarily adjacent)
     * 
     * @param key
     * @param bag
     * @return true if so, false if not
     */
    public static boolean containsAllNonStopWordsInOrder(String key, String bag) {
        String regexp = (caseSens ? "" : "(?i)") + StopWords.remove(key).replaceAll("\\s+", "\\b.*\\b");
        return StopWords.remove(bag).matches(".*" + regexp + ".*");
    }

    /**
     * See whether bag contains all the words in key, in any order
     * 
     * @param key
     * @param bag
     * @return true if so, false if not
     */
    public static boolean containsAllWords(String key, String bag) {
        return stringToWordBag(bag).containsAll(stringToWordBag(key));
    }

    /**
     * See whether bag contains all the non-stop-words in key, in any order
     * 
     * @param key
     * @param bag
     * @return true if so, false if not
     */
    public static boolean containsAllNonStopWords(String key, String bag) {
        return stringToWordBag(StopWords.remove(bag)).containsAll(stringToWordBag(StopWords.remove(key)));
    }
}