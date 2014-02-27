/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp.lemma;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import csli.util.Config;
import csli.util.nlp.Lemma;
import csli.util.nlp.Lemmatiser;

/**
 * Dictionary-based lemmatiser - loads morphological variant lists from a
 * dictionary file into a lookup table. Rule files should be in /util/ext -
 * there's one called oald_penn_morph_lists.txt derived from the Oxford Advanced
 * Learner's Dictionary of English and using the Penn Treebank PoS tagset.
 * 
 * @author mpurver
 */
public class DictionaryLemmatiser extends Lemmatiser {

    private static HashMap rules1 = new HashMap();

    private static HashMap rules2 = new HashMap();

    // if true, return null if word not in dictionary (useful for the first pass
    // in a multi-pass approach); if false, return raw word (probably more
    // useful when used on its own)
    private static boolean strict = false;

    public DictionaryLemmatiser() {
        // get name of dictionary file to read morphological pairs from
        String ruleFile = Config.main.getFileProperty(
                this.getClass().getName() + ".ruleFile").getAbsolutePath();
        if (ruleFile == null) {
            System.err.println("Must specify rule file");
            throw new IllegalArgumentException("Must specify rule file");
            //WAS:System.exit(0);
        } else {
            System.out.println("Using morphological dictionary file: "
                    + ruleFile);
        }
        // determine strictness
        String strictStr = Config.main.get(this.getClass().getName()
                + ".strict");
        if (strictStr == null) {
            System.out.println("Strictness left at default: " + strict);
        } else {
            strict = strictStr.equalsIgnoreCase("true");
            System.out.println("Strictness set to: " + strict);
        }
        // get ready to go
        rules1.clear();
        rules2.clear();
        loadRules(ruleFile);
    }

    /*
     * Just look the word up (non-Javadoc)
     * 
     * @see csli.util.nlp.Lemmatiser#getLemma(java.lang.String)
     */
    public Lemma getLemma(String wordString) {
        if (rules1.containsKey(wordString.toLowerCase())) {
            return (Lemma) rules1.get(wordString.toLowerCase());
        } else {
            return (strict ? null : new Lemma(wordString, null));
        }
    }

    /*
     * Just look the lemma up (non-Javadoc)
     * 
     * @see csli.util.nlp.Lemmatiser#getWord(java.lang.String)
     */
    public String getWord(Lemma lemma) {
        if (rules2.containsKey(lemma)) {
            return (String) rules2.get(lemma);
        } else {
            return (strict ? null : lemma.getRoot());
        }
    }

    /*
     * Look up morphological pairs in dictionary file. Each line should contain
     * POS:WORD pairs, separated by white space and with WORD enclosed in double
     * quotes (so that we can allow spaces, single quotes etc. inside if
     * needed).
     */
    private static void loadRules(String fileName) {
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String line;
            Pattern p = Pattern.compile("(\\S+?):\"(\\S+?)\"");
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    String root = m.group(2).toLowerCase();
                    while (m.find()) {
                        String pos = m.group(1).toLowerCase();
                        String word = m.group(2).toLowerCase();
                        Lemma lemma = new Lemma(root, pos);
                        if (!rules1.containsKey(word)) {
                            rules1.put(word, lemma);
                        }
                        if (!rules2.containsKey(lemma)) {
                            rules2.put(lemma, word);
                        }
                    }
                }
            }
            try {
                fr.close();
            } catch (Exception e) {
                System.err.println("Error closing file " + fileName);
            }
        } catch (Exception e) {
            //System.err.println("Dictionary file " + fileName + " not found");
            throw new IllegalArgumentException("Dictionary file "+fileName+" not found");
            //WAS:System.exit(1);
        }
    }

    public static void main(String[] args) {
        Config.setConfigDir(args[1]);
        Config.setMainConfigFile(args[0]);
        Lemmatiser l = new DictionaryLemmatiser();
        Lemma le = l.getLemma("calcifies");
        System.out.println(le.getRoot() + " " + le.getPoS());
        String wo = l.getWord(le);
        System.out.println(wo);
    }
}