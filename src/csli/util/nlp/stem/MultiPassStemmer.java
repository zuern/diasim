/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp.stem;

import java.util.ArrayList;
import java.util.Iterator;

import csli.util.Config;
import csli.util.InstanceFactory;
import csli.util.nlp.Stemmer;

/**
 * Calls a list of word stemmers, either until one is found that succeeds in
 * stemming a word, or in order to use them all cumulatively in turn
 * 
 * @author mpurver
 */
public class MultiPassStemmer extends Stemmer {

    private ArrayList stemmers;

    // if true, we try each stemmer in turn but stop when the first one succeeds
    // (the stemmers must therefore be strict); if false, we cumulatively run
    // each stemmer in turn anyway
    private boolean exclusive = true;

    // if true, return null if all stemmers fail; if false, return raw word
    private static boolean strict = false;

    public MultiPassStemmer() {
        // determine strictness
        String strictStr = Config.main.get(this.getClass().getName()
                + ".strict");
        if (strictStr == null) {
            System.out.println("Strictness left at default: " + strict);
        } else {
            strict = strictStr.equalsIgnoreCase("true");
            System.out.println("Strictness set to: " + strict);
        }
        // get stemmer list
        String listStr = Config.main.get(this.getClass().getName()
                + ".stemmerList");
        if (listStr == null) {
            throw new RuntimeException(
                    "Trying to instantiate MultiPassStemmer, but no config key '"
                            + this.getClass().getName()
                            + ".stemmerList' is defined");
        }
        // initialise list of stemmers
        String[] stemmerList = listStr.equals("") ? new String[0] : listStr
                .split(",");
        init(stemmerList);
    }

    public MultiPassStemmer(String[] stemmerList) {
        // determine strictness
        String strictStr = Config.main.get(this.getClass().getName()
                + ".strict");
        if (strictStr == null) {
            System.out.println("Strictness left at default: " + strict);
        } else {
            strict = strictStr.equalsIgnoreCase("true");
            System.out.println("Strictness set to: " + strict);
        }
        init(stemmerList);
    }

    /**
     * @param stemmerList
     *            list of classes to instantiate as stemmers
     */
    protected void init(String[] stemmerList) {
        String exclStr = Config.main.get(this.getClass().getName()
                + "exclusive");
        if (exclStr == null) {
            System.out.println("Exclusivity left at default: " + exclusive);
        } else {
            exclusive = exclStr.equalsIgnoreCase("true");
            System.out.println("Exclusivity set to: " + exclusive);
        }

        stemmers = new ArrayList();
        for (int i = 0; i < stemmerList.length; i++) {
            if (stemmerList[i] != null && stemmerList[i] != "") {
                System.out.println("Creating multi-pass instance of "
                        + stemmerList[i]);
                Stemmer stemmer = (Stemmer) InstanceFactory
                        .newInstance(stemmerList[i]);
                stemmers.add(stemmer);
            }
        }
    }

    /**
     * cycle over each stemmer. If exclusive, return the result as soon as one
     * succeeds; otherwise, cycle the result into the next stemmer
     * 
     * @return the stemmed base form, or null if none can be found
     * 
     * @see csli.util.nlp.Stemmer#stem(java.lang.String)
     */
    public String stem(String wordString) {
        String base = null;
        boolean stemmed = false;
        for (Iterator it = stemmers.iterator(); it.hasNext();) {
            Stemmer stemmer = (Stemmer) it.next();
            base = stemmer.stem(wordString);
            if (base != null) {
                stemmed = true;
                wordString = base;
                if (exclusive) {
                    return wordString;
                }
            }
        }
        if (!stemmed) {
            System.out.println("All stemmers failed on " + wordString);
            return (strict ? null : wordString);
        } else {
            return wordString;
        }
    }
}