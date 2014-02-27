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
 * A factory that returns a lemmatiser.
 */
public class LemmatiserFactory implements java.io.Serializable {

    public static final String DEFAULT_KEY = "util.nlp.lemmatiser";

    private static HashMap<String, Lemmatiser> lemmatisers = new HashMap<String, Lemmatiser>();

    /**
     * Get the static instance of the default lemmatiser; if none exists, a new one will be created, of the class
     * specified by the default util.nlp.lemmatiser config key
     * 
     * @return null if the key doesn't exist, class can't be instantiated etc.
     */
    public static Lemmatiser getLemmatiser() {
        return getLemmatiser(DEFAULT_KEY);
    }

    /**
     * Get the static instance of the lemmatiser associated with the given config key OR class name; if none exists,
     * create a new one
     * 
     * @param key
     *            the name of the desired class, or a config key which points at one
     * @return null if the key doesn't exist, class can't be instantiated etc.
     */
    public static Lemmatiser getLemmatiser(String key) {
        if (lemmatisers.get(key) == null) {
            lemmatisers.put(key, getNewLemmatiser(key));
        }
        return lemmatisers.get(key);

    }

    /**
     * Get a new lemmatiser of the class specified by the default util.nlp.lemmatiser config key
     * 
     * @return null if the key doesn't exist, class can't be instantiated etc.
     */
    public static Lemmatiser getNewLemmatiser() {
        return getNewLemmatiser(DEFAULT_KEY);
    }

    /**
     * Get a new lemmatiser of the class specified by the given config keyN OR class name
     * 
     * @param key
     *            the name of the desired class, or a config key which points at one
     * @return null if the key doesn't exist, class can't be instantiated etc.
     */
    public static Lemmatiser getNewLemmatiser(String key) {
        try {
            String lemmatiserName = Config.main.get(key);
            if (lemmatiserName == null) {
                lemmatiserName = key;
            }
            System.out.println("Creating lemmatiser of class " + lemmatiserName + " from key " + key);
            return (Lemmatiser) InstanceFactory.newInstance(lemmatiserName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}