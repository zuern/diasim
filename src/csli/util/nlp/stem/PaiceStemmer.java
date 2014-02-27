/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp.stem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import csli.util.nlp.Stemmer;

/*******************************************************************************
 * Class: PaiceStemmer * * Purpose: Stemming Algorithm *
 ******************************************************************************/
public class PaiceStemmer extends Stemmer {

    private Vector ruleTable; // array of rules

    private int[] ruleIndex; // index to above

    private boolean preStrip;

    private boolean irregPlurals;

    private String filerules;

    /***************************************************************************
     * Method: PaiceStemmer * * Purpose: init *
     **************************************************************************/
    public PaiceStemmer(String rules, String pre) {
        ruleTable = new Vector();
        ruleIndex = new int[26];
        preStrip = false;
        filerules = rules;
        if (pre.equals("/p")) {
            preStrip = true;
        }
        ReadRules(filerules);
    }

    /***************************************************************************
     * Method: PaiceStemmer * * Purpose: init * version using default rules -
     * mpurver
     **************************************************************************/
    public PaiceStemmer(boolean stripPrefixes, boolean useIrregPlurals) {
        ruleTable = new Vector();
        ruleIndex = new int[26];
        preStrip = stripPrefixes;
        irregPlurals = useIrregPlurals;
        ReadRules(null);
    }

    /***************************************************************************
     * Method: PaiceStemmer * * Purpose: init * version using default rules -
     * mpurver
     **************************************************************************/
    public PaiceStemmer() {
        this(true, true);
    }

    /*
     * Stores default rules as available from
     * http://www.comp.lancs.ac.uk/computing/research/stemming/index.htm @author
     * mpurver
     */
    private String[] DefaultRules() {
        if (irregPlurals) {
            String[] defaultRules = { "ai*2.", "a*1.", "bb1.", "city3s.",
                    "ci2>", "cn1t>", "dd1.", "dei3y>", "deec2ss.", "dee1.",
                    "de2>", "dooh4>", "e1>", "feil1v.", "fi2>", "gni3>",
                    "gai3y.", "ga2>", "gg1.", "ht*2.", "hsiug5ct.", "hsi3>",
                    "hteet*4htoo.", "i*1.", "i1y>", "ji1d.", "juf1s.", "ju1d.",
                    "jo1d.", "jeh1r.", "jrev1t.", "jsim2t.", "jn1d.", "j1s.",
                    "lbaifi6.", "lbai4y.", "lba3>", "lbi3.", "lib2l>", "lc1.",
                    "lufi4y.", "luf3>", "lu2.", "lai3>", "lau3>", "la2>",
                    "ll1.", "mui3.", "mu*2.", "msi3>", "mm1.", "nois4j>",
                    "noix4ct.", "noi3>", "nai3>", "na2>", "nee0.", "ne2>",
                    "nem*2na", "nerdlihc*3.", "nn1.", "pihs4>", "pp1.", "re2>",
                    "rae0.", "ra2.", "ro2>", "ru2>", "rr1.", "rt1>", "rei3y>",
                    "sei3y>", "sis2.", "si2>", "ssen4>", "ss0.", "suo3>",
                    "su*2.", "s*1>", "s0.", "tacilp4y.", "ta2>", "teef*3too.",
                    "tnem4>", "tne3>", "tna3>", "tpir2b.", "tpro2b.", "tcud1.",
                    "tpmus2.", "tpec2iv.", "tulo2v.", "tsis0.", "tsi3>",
                    "tt1.", "uqi3.", "ugo1.", "vis3j>", "vie0.", "vi2>",
                    "ylb1>", "yli3y>", "ylp0.", "yl2>", "ygo1.", "yhp1.",
                    "ymo1.", "ypo1.", "yti3>", "yte3>", "ytl2.", "yrtsi5.",
                    "yra3>", "yro3>", "yfi3.", "ycn2t>", "yca3>", "zi2>",
                    "zy1s.", "end0." };
            return defaultRules;
        } else {
            String[] defaultRules = { "ai*2.", "a*1.", "bb1.", "city3s.",
                    "ci2>", "cn1t>", "dd1.", "dei3y>", "deec2ss.", "dee1.",
                    "de2>", "dooh4>", "e1>", "feil1v.", "fi2>", "gni3>",
                    "gai3y.", "ga2>", "gg1.", "ht*2.", "hsiug5ct.", "hsi3>",
                    "i*1.", "i1y>", "ji1d.", "juf1s.", "ju1d.", "jo1d.",
                    "jeh1r.", "jrev1t.", "jsim2t.", "jn1d.", "j1s.",
                    "lbaifi6.", "lbai4y.", "lba3>", "lbi3.", "lib2l>", "lc1.",
                    "lufi4y.", "luf3>", "lu2.", "lai3>", "lau3>", "la2>",
                    "ll1.", "mui3.", "mu*2.", "msi3>", "mm1.", "nois4j>",
                    "noix4ct.", "noi3>", "nai3>", "na2>", "nee0.", "ne2>",
                    "nn1.", "pihs4>", "pp1.", "re2>", "rae0.", "ra2.", "ro2>",
                    "ru2>", "rr1.", "rt1>", "rei3y>", "sei3y>", "sis2.",
                    "si2>", "ssen4>", "ss0.", "suo3>", "su*2.", "s*1>", "s0.",
                    "tacilp4y.", "ta2>", "tnem4>", "tne3>", "tna3>", "tpir2b.",
                    "tpro2b.", "tcud1.", "tpmus2.", "tpec2iv.", "tulo2v.",
                    "tsis0.", "tsi3>", "tt1.", "uqi3.", "ugo1.", "vis3j>",
                    "vie0.", "vi2>", "ylb1>", "yli3y>", "ylp0.", "yl2>",
                    "ygo1.", "yhp1.", "ymo1.", "ypo1.", "yti3>", "yte3>",
                    "ytl2.", "yrtsi5.", "yra3>", "yro3>", "yfi3.", "ycn2t>",
                    "yca3>", "zi2>", "zy1s.", "end0." };
            return defaultRules;
        }
    }

    /***************************************************************************
     * Method: ReadRules * Returns: void * Receives: * Purpose: read rules in
     * from stemRules and enters them * into ruleTable, ruleIndex is set up to
     * provide * faster access to relevant rules. *
     **************************************************************************/
    private void ReadRules(String stemRules) {
        int ruleCount = 0;
        int j = 0;

        // Acquire each rule in turn. They each take up one line
        if (stemRules == null) {
            String[] rules = DefaultRules();
            ruleCount = rules.length;
            for (int i = 0; i < ruleCount; i++) {
                ruleTable.addElement(rules[i]);
            }
        } else {
            try {
                FileReader fr = new FileReader(stemRules);
                BufferedReader br = new BufferedReader(fr);
                String line = " ";
                try {
                    while ((line = br.readLine()) != null) {
                        ruleCount++;
                        j = 0;
                        String rule = new String();
                        rule = "";
                        while ((j < line.length()) && (line.charAt(j) != ' ')) {
                            rule += line.charAt(j);
                            j++;
                        }
                        ruleTable.addElement(rule);
                    }
                } catch (Exception e) {
                    System.err.println("File Error While Reading Rules" + e);
                    //System.exit(0);
                }
                // try to close file, file is not needed again so if can't close
                // don't exit
                try {
                    fr.close();
                } catch (Exception e) {
                    System.err
                            .println("Error Closing File During Reading Rules");
                }
            } catch (Exception e) {
                System.err.println("Input File" + stemRules + "not found");
                //System.exit(1);
            }
        }

        // Now assign the number of the first rule that starts with each letter
        // (if any) to an alphabetic array to facilitate selection of sections

        char ch = 'a';
        for (j = 0; j < 25; j++) {
            ruleIndex[j] = 0;
        }

        for (j = 0; j < (ruleCount - 1); j++) {
            while (((String) ruleTable.elementAt(j)).charAt(0) != ch) {
                ch++;
                ruleIndex[charCode(ch)] = j;
            }
        }
    }

    /***************************************************************************
     * Method: FirstVowel * Returns: int * Recievs: String word, int last *
     * Purpose: checks lower-case word for position of * the first vowel *
     **************************************************************************/
    private int FirstVowel(String word, int last) {
        int i = 0;
        if ((i < last) && (!(vowel(word.charAt(i), 'a')))) {
            i++;
        }
        if (i != 0) {
            while ((i < last) && (!(vowel(word.charAt(i), word.charAt(i - 1))))) {
                i++;
            }
        }
        if (i < last) {
            return i;
        }
        return last;
    }

    /***************************************************************************
     * Method: stripSuffixes * Returns: String * Recievs: String word * Purpose:
     * strips suffix off word and returns stem using * paice stemming algorithm *
     **************************************************************************/
    private String stripSuffixes(String word) {
        // integer variables 1 is positive, 0 undecided, -1 negative equiverlent
        // of pun vars positive undecided negative
        int ruleok = 0;
        int Continue = 0;
        // integer varables
        int pll = 0; // position of last letter
        int xl; // counter for nuber of chars to be replaced and length of
        // stemmed word if rule was aplied
        int pfv; // poition of first vowel
        int prt; // pointer into rule table
        int ir; // index of rule
        int iw; // index of word
        // char variables
        char ll; // last letter
        // String variables eqiverlent of tenchar variables
        String rule = ""; // varlable holding the current rule
        String stem = ""; // string holding the word as it is being stemmed
        // this
        // is returned as a stemmed word.
        // boolean varable
        boolean intact = true; // intact if the word has not yet been stemmed
        // to
        // determin a requirement of some stemming rules

        // set stem = to word
        stem = Clean(word.toLowerCase());

        // set the position of pll to the last letter in the string
        pll = 0;
        // move through the word to find the position of the last letter before
        // a non letter char
        while ((pll + 1 < stem.length())
                && ((stem.charAt(pll + 1) >= 'a') && (stem.charAt(pll + 1) <= 'z'))) {
            pll++;
        }

        if (pll < 1) {
            Continue = -1;
        }
        // find the position of the first vowel
        pfv = FirstVowel(stem, pll);

        iw = stem.length() - 1;

        // repeat until continue == negative ie. -1
        while (Continue != -1) {
            Continue = 0; // SEEK RULE FOR A NEW FINAL LETTER
            ll = stem.charAt(pll); // last letter

            // Check to see if there are any possible rules for stemming
            if ((ll >= 'a') && (ll <= 'z')) {
                prt = ruleIndex[charCode(ll)]; // pointer into rule-table
            } else {
                prt = -1;// 0 is a vaild rule
            }
            if (prt == -1) {
                Continue = -1; // no rule available
            }

            if (Continue == 0)
            // THERE IS A POSSIBLE RULE (OR RULES) : SEE IF ONE WORKS
            {
                rule = (String) ruleTable.elementAt(prt); // Take first rule
                while (Continue == 0) {
                    ruleok = 0;
                    if (rule.charAt(0) != ll) {
                        // rule-letter changes
                        Continue = -1;
                        ruleok = -1;
                    }
                    ir = 1; // index of rule: 2nd character
                    iw = pll - 1; // index of word: next-last letter

                    // repeat untill the rule is not undecided find a rule that
                    // is acceptable
                    while (ruleok == 0) {
                        if ((rule.charAt(ir) >= '0')
                                && (rule.charAt(ir) <= '9')) // rule fully
                        // matched
                        {
                            ruleok = 1;
                        } else if (rule.charAt(ir) == '*') {
                            // match only if word intact
                            if (intact) {
                                ir = ir + 1; // move forwards along rule
                                ruleok = 1;
                            } else {
                                ruleok = -1;
                            }
                        } else if (rule.charAt(ir) != stem.charAt(iw)) {
                            // mismatch of letters
                            ruleok = -1;
                        } else if (iw <= pfv) {
                            // insufficient stem remains
                            ruleok = -1;
                        } else {
                            // move on to compare next pair of letters
                            ir = ir + 1; // move forwards along rule
                            iw = iw - 1; // move backwards along word
                        }
                    }

                    // if the rule that has just been checked is valid
                    if (ruleok == 1) {
                        // CHECK ACCEPTABILITY CONDITION FOR PROPOSED RULE
                        xl = 0; // count any replacement letters
                        while (!((rule.charAt(ir + xl + 1) >= '.') && (rule
                                .charAt(ir + xl + 1) <= '>'))) {
                            xl++;
                        }
                        xl = pll + xl + 48 - ((int) (rule.charAt(ir)));
                        // position of last letter if rule used
                        if (pfv == 0) {
                            // if word starts with vowel...
                            if (xl < 1) {
                                // ...minimal stem is 2 letters
                                ruleok = -1;
                            } else {
                                // ruleok=1; as ruleok must alread be positive
                                // to reach this stage
                            }
                        }
                        // if word start swith consonant...
                        else if ((xl < 2) | (xl < pfv)) {
                            ruleok = -1;
                            // ...minimal stem is 3 letters...
                            // ...including one or more vowel
                        } else {
                            // ruleok=1; as ruleok must alread be positive to
                            // reach this stage
                        }
                    }
                    // if using the rule passes the assertion tests
                    if (ruleok == 1) {
                        // APPLY THE MATCHING RULE
                        intact = false;
                        // move end of word marker to position...
                        // ... given by the numeral.
                        pll = pll + 48 - ((int) (rule.charAt(ir)));
                        ir++;
                        stem = stem.substring(0, (pll + 1));

                        // append any letters following numeral to the word
                        while ((ir < rule.length())
                                && (('a' <= rule.charAt(ir)) && (rule
                                        .charAt(ir) <= 'z'))) {
                            stem += rule.charAt(ir);
                            ir++;
                            pll++;
                        }

                        // if rule ends with '.' then terminate
                        if ((rule.charAt(ir)) == '.') {
                            Continue = -1;
                        } else {
                            // if rule ends with '>' then Continue
                            Continue = 1;
                        }
                    } else {
                        // if rule did not match then look for another
                        prt = prt + 1; // move to next rule in RULETABLE
                        rule = (String) ruleTable.elementAt(prt);
                        if (rule.charAt(0) != ll) {
                            // rule-letter changes
                            Continue = -1;
                        }
                    }
                }
            }
        }
        return stem;
    }

    /***************************************************************************
     * Method: vowel * Returns: boolean * Recievs: char ch, char prev * Purpose:
     * determin whether ch is a vowel or not * uses prev determination when ch ==
     * y *
     **************************************************************************/
    private boolean vowel(char ch, char prev) {
        switch (ch) {
        case 'a':
        case 'e':
        case 'i':
        case 'o':
        case 'u':
            return true;
        case 'y': {
            switch (prev) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return false;
            default:
                return true;
            }
        }
        default:
            return false;
        }
    }

    /***************************************************************************
     * Method: charCode * Returns: int * Recievs: char ch * Purpose: returns the
     * relavent array index for * specified char 'a' to 'z' *
     **************************************************************************/
    private int charCode(char ch) {
        return ((int) ch) - 97;
    }

    /***************************************************************************
     * Method: stripPrefixes * Returns: String * Recievs: String str * Purpose:
     * removes prefixes so that suffix * removal can comence *
     **************************************************************************/
    private String stripPrefixes(String str) {
        String[] prefixes = { "kilo", "micro", "milli", "intra", "ultra",
                "mega", "nano", "pico", "pseudo" };

        int last = prefixes.length;

        for (int i = 0; i < last; i++) {
            if ((str.startsWith(prefixes[i]))
                    && (str.length() > prefixes[i].length())) {
                str = str.substring(prefixes[i].length());
                return str;
            }
        }
        return str;
    }

    /***************************************************************************
     * Method: Clean * Returns: String * Recievs: String str * Purpose: remove
     * all non letter or digit * characters from srt and return *
     **************************************************************************/
    private String Clean(String str) {
        int last = str.length();
        String temp = "";
        for (int i = 0; i < last; i++) {
            if ((str.charAt(i) >= 'a') & (str.charAt(i) <= 'z')) {
                temp += str.charAt(i);
            }
        }
        return temp;
    } // clean

    /** ************************* PUBLIC METHODS *************** */
    /***************************************************************************
     * Method: stripAffixes * Returns: String * Recievs: String str * Purpose:
     * prepares string and calls stripPrefixes * and stripSuffixes *
     **************************************************************************/
    public String stripAffixes(String str) {
        // str = str.toLowerCase(); //change all letters in the input to
        // lowercase
        // str = Clean(str); // remove all chars from string that are not a
        // letter or a digit (why digit?)
        if ((str.length() > 3) && (preStrip)) // if str's length is greater
        // than
        // 2 then remove prefixes
        {
            str = stripPrefixes(str);
        }
        if (str.length() > 3) // if str is not null remove suffix
        {
            str = stripSuffixes(str);
        }
        return str;
    } // stripAffixes

    /*
     * standard interface for stripAffixes (non-Javadoc)
     * 
     * @see csli.util.nlp.Stemmer#stem(java.lang.String)
     */
    public String stem(String str) {
        return stripAffixes(str);
    }

    // ***************MAIN METHOD***************
    /***************************************************************************
     * Method: main * Returns: void * Recives: String args[] input file *
     * Purpose: creates instance of PaiceStemmer and * calls stripAffixes *
     **************************************************************************/
    public static void main(String args[]) {
        PaiceStemmer p;
        if (args.length > 2) {
            p = new PaiceStemmer(args[2], args[3]);
        } else {
            p = new PaiceStemmer(true, true);
        }
        String text;
        StringTokenizer line = new StringTokenizer("");
        String output = "";
        String fileOut = args[1];
        String fileIn = args[0];

        try {
            FileWriter fw = new FileWriter(fileOut);
            BufferedWriter bw = new BufferedWriter(fw);

            FileReader fr = new FileReader(fileIn);
            BufferedReader br = new BufferedReader(fr);
            try {
                while ((text = br.readLine()) != null) {
                    line = new StringTokenizer(text);
                    try {
                        while (line.hasMoreTokens()) {
                            // read word from line and stem word
                            String word = new String();
                            word = line.nextToken();
                            bw.write(p.stripAffixes(word) + " ");
                        }
                        bw.newLine();
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
            } catch (Exception e) {
                System.err.println("File Error During Reading " + e);
                System.exit(0);
            }
            // try to close file, file is not needed again so if can't close
            // don't exit
            try {
                fr.close();
            } catch (Exception e) {
                System.err.println("Error Closing File During Reading " + e);
            }
            try {
                bw.close();
            } catch (Exception e) {
                System.err.println(e);
            }

        } catch (Exception e) {
            System.err.println("File Not Found " + args[0] + " exception " + e);
            System.exit(1);
        }
    }// main
} // class

