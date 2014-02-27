/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util.nlp;

/**
 * @author mpurver
 */
public class Lemma {

    private String root;

    private String pos;

    public Lemma(String rootForm, String posTag) {
        if (rootForm!=null) root = rootForm.toLowerCase();
        if (posTag!=null) pos = posTag.toLowerCase();
    }

    /**
     * @return Returns the root form.
     */
    public String getRoot() {
        return root;
    }

    /**
     * @return Returns the part-of-speech tag.
     */
    public String getPoS() {
        return pos;
    }

    /**
     * @param the
     *            root form
     */
    public void setRoot(String rootForm) {
        root = rootForm.toLowerCase();
    }

    /**
     * @param the
     *            part-of-speech tag
     */
    public void setPoS(String posTag) {
        pos = posTag.toLowerCase();
    }

}