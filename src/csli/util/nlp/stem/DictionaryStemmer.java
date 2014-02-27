/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp.stem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import csli.util.Config;
import csli.util.nlp.Stemmer;

/**
 * Dictionary-based stemmer - loads morphological pairs from a dictionary file into a lookup table. Rule files should be
 * in /util/ext - there's one called oald_morph_pairs.txt derived from the Oxford Advanced Learner's Dictionary of
 * English.
 * 
 * @author mpurver
 */
public class DictionaryStemmer extends Stemmer {

    private static HashMap<String, String> rules = new HashMap<String, String>();

    /**
     * If true, return null if word not in dictionary (useful for the first pass in a multi-pass approach); if false,
     * return raw word (probably more useful when used on its own)
     */
    private static boolean strict = false;

    /**
     * If true, stem a word even if it may be a root form already (but there is a possible other root form of which this
     * can be an inflected form); if false, leave things that may be root forms alone.
     */
    private static boolean aggressive = false;

    private static HashSet<String> roots = new HashSet<String>();

    public DictionaryStemmer() {
        // get name of dictionary file to read morphological pairs from
        String ruleFile = Config.main.getFileProperty(this.getClass().getName() + ".ruleFile").getAbsolutePath();
        if (ruleFile == null) {
            System.err.println("Must specify rule file");
            System.exit(0);
        } else {
            System.out.println("Using morphological dictionary file: " + ruleFile);
        }
        // determine strictness
        String strictStr = Config.main.get(this.getClass().getName() + ".strict");
        if (strictStr == null) {
            System.out.println("Strictness left at default: " + strict);
        } else {
            strict = strictStr.equalsIgnoreCase("true");
            System.out.println("Strictness set to: " + strict);
        }
        // determine aggression
        String aggroStr = Config.main.get(this.getClass().getName() + ".aggressive");
        if (aggroStr == null) {
            System.out.println("Aggression left at default: " + aggressive);
        } else {
            aggressive = aggroStr.equalsIgnoreCase("true");
            System.out.println("Aggression set to: " + aggressive);
        }
        // get ready to go
        rules.clear();
        loadRules(ruleFile);
    }

    /*
     * Just look the word up (non-Javadoc)
     * 
     * @see csli.util.nlp.Stemmer#stem(java.lang.String)
     */
    public String stem(String wordString) {
        // if non-aggressive: if this word may already be a root form, don't stem it (prevents e.g. bit -> bite)
        if (!aggressive && roots.contains(wordString.toLowerCase())) {
            return wordString;
        }
        // otherwise stem
        String base = rules.get(wordString.toLowerCase());
        if (base != null) {
            return base;
        } else {
            return (strict ? null : wordString);
        }
    }

    /**
     * Look up morphological pairs in dictionary file. Each line should contain (1) inflected form and (2) base form,
     * separated by white space and both enclosed in double quotes (so that we can allow spaces, single quotes etc.
     * inside if needed).
     */
    private static void loadRules(String fileName) {
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String line;
            Pattern p = Pattern.compile("\"(\\S+?)\"\\s+\"(\\S+)\"");
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    String form = m.group(1).toLowerCase();
                    String base = m.group(2).toLowerCase();
                    // if there is no rule for this form yet, add it
                    if (!rules.containsKey(form)) {
                        rules.put(form, base);
                    } else {
                        // if agressive: overwrite an existing boring rule
                        // if not agressive: overwrite an existing rule if we can be more boring
                        if (aggressive && rules.get(form).equals(form)) {
                            rules.put(form, base);
                        } else if (!aggressive && form.equals(base)) {
                            rules.put(form, base);
                        }
                    }
                    // collect root forms for quick lookup later (avoiding a rules.containsValue() call)
                    if (form.equals(base)) {
                        roots.add(base);
                    }
                }
            }
            try {
                fr.close();
            } catch (Exception e) {
                System.err.println("Error closing file " + fileName);
            }
        } catch (Exception e) {
            System.err.println("Dictionary file " + fileName + " not found");
            System.exit(1);
        }
    }
}