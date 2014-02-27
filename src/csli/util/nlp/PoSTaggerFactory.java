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
 * A factory that returns a part-of-speech tagger.
 */
public class PoSTaggerFactory {

    public static final String DEFAULT_KEY = "util.nlp.tagger";

    private static HashMap<String, PoSTagger> taggers = new HashMap<String, PoSTagger>();

    /**
     * Get the static instance of the default PoS-tagger; if none exists, a new one will be created, of the class
     * specified by the default util.nlp.tagger config key
     * 
     * @return null if the key doesn't exist, class can't be instantiated etc.
     */
    public static PoSTagger getTagger() {
        return getTagger(DEFAULT_KEY);
    }

    /**
     * Get the static instance of the POS-tagger associated with the given config key OR class name; if none exists,
     * create a new one
     * 
     * @param key
     *            the name of the desired class, or a config key which points at one
     * @return null if the key doesn't exist, class can't be instantiated etc.
     */
    public static PoSTagger getTagger(String key) {
        if (taggers.get(key) == null) {
            taggers.put(key, getNewTagger(key));
        }
        return taggers.get(key);

    }

    /**
     * Get a new PoS-tagger of the class specified by the default util.nlp.tagger config key
     * 
     * @return null if the key doesn't exist, class can't be instantiated etc.
     */
    public static PoSTagger getNewTagger() {
        return getNewTagger(DEFAULT_KEY);
    }

    /**
     * Get a new PoS-tagger of the class specified by the given config key OR class name
     * 
     * @param key
     *            the name of the desired class, or a config key which points at one
     * @return null if the key doesn't exist, class can't be instantiated etc.
     */
    public static PoSTagger getNewTagger(String key) {
        try {
            String taggerName = Config.main.get(key);
            if (taggerName == null) {
                taggerName = key;
            }
            System.out.println("Creating PoS tagger of class " + taggerName + " from key " + key);
            return (PoSTagger) InstanceFactory.newInstance(taggerName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}