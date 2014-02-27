/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author laidebeu
 * 
 */
public class UtteranceSplitter {

    public static List<String> split(String utt) {
        List<String> uttWords = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(utt, ", ;:.'\"?!$`", true);
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            t = t.trim();
            if (t.length() > 0)
                uttWords.add(t);
        }
        return uttWords;
    }

}
