/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp;

import java.util.HashMap;

import csli.util.Config;
import csli.util.InstanceFactory;

/**
 * A factory that returns a word stemmer.
 */
public class StemmerFactory implements java.io.Serializable {

    public static final String DEFAULT_KEY = "util.nlp.stemmer";

    private static HashMap<String, Stemmer> stemmers = new HashMap<String, Stemmer>();

    /**
     * Get the static instance of the default stemmer; if none exists, a new one will be created, of the class specified
     * by the default util.nlp.stemmer config key
     * 
     * @return null if the key doesn't exist, class can't be instantiated etc.
     */
    public static Stemmer getStemmer() {
        return getStemmer(DEFAULT_KEY);
    }

    /**
     * Get the static instance of the stemmer associated with the given config key OR class name; if none exists, create
     * a new one
     * 
     * @param key
     *            the name of the desired class, or a config key which points at one
     * @return null if the key doesn't exist, class can't be instantiated etc.
     */
    public static Stemmer getStemmer(String key) {
        if (stemmers.get(key) == null) {
            stemmers.put(key, getNewStemmer(key));
        }
        return stemmers.get(key);
    }

    /**
     * Get a new stemmer of the class specified by the default util.nlp.stemmer config key
     * 
     * @return null if the key doesn't exist, class can't be instantiated etc.
     */
    public static Stemmer getNewStemmer() {
        return getNewStemmer(DEFAULT_KEY);
    }

    /**
     * Get a new stemmer of the class specified by the given config key OR class name
     * 
     * @param key
     *            the name of the desired class, or a config key which points at one
     * @return null if the key doesn't exist, class can't be instantiated etc.
     */
    public static Stemmer getNewStemmer(String key) {
        try {
            String stemmerName = Config.main.get(key);
            if (stemmerName == null) {
                stemmerName = key;
            }
            System.out.println("Creating word stemmer of class " + stemmerName + " from key " + key);
            return (Stemmer) InstanceFactory.newInstance(stemmerName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}